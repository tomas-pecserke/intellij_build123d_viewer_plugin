package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.io.awaitExit
import com.jetbrains.python.sdk.pythonSdk
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.Build123dBundle
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview.jcef.JcefBuild123dPreviewPanelProvider
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.util.Build123dPluginScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeListener
import java.io.ByteArrayOutputStream
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.io.path.createTempDirectory

class Build123dPreviewFileEditor(
  private val project: Project,
  private val file: VirtualFile,
  private val document: Document
) : UserDataHolder by UserDataHolderBase(), FileEditor {
  companion object {
    private val renderScript = Build123dPreviewFileEditor::class.java.getResource("/python/render.py")!!.readText()
  }

  val panelProvider: Build123dPreviewPanelProvider = JcefBuild123dPreviewPanelProvider()

  private val previewPanelWrapper: JPanel = JPanel(BorderLayout()).apply {
    addComponentListener(AttachPanelOnVisibilityChangeListener())
  }
  private var panel: Build123dPreviewPanel? = null
  private var mainEditor = MutableStateFlow<Editor?>(null)
  private var isDisposed: Boolean = false
  private val coroutineScope = Build123dPluginScope.createChildScope(project)

  init {
    document.addDocumentListener(ReparseContentDocumentListener(), this)
    coroutineScope.launch(Dispatchers.EDT) {
      attachPreviewPanel()
    }
  }

  fun setMainEditor(editor: Editor) {
    check(mainEditor.value == null)
    mainEditor.value = editor
  }

  override fun getComponent(): JComponent {
    return previewPanelWrapper
  }

  override fun getPreferredFocusedComponent(): JComponent? {
    return panel?.getComponent()
  }

  override fun getName(): String {
    return Build123dBundle.message("build123d.editor.preview.name")
  }

  override fun setState(state: FileEditorState) {}

  override fun isModified(): Boolean {
    return false
  }

  override fun isValid(): Boolean {
    return true
  }

  override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

  override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

  override fun getFile(): VirtualFile {
    return file
  }

  override fun dispose() {
    if (panel != null) {
      detachPreviewPanel()
    }
    isDisposed = true
    coroutineScope.cancel()
  }

  @RequiresEdt
  private fun updatePreview() {
    if (!file.isValid || isDisposed) {
      fileLogger().warn("Invalid or disposed file.")
      return
    }

    coroutineScope.launch {
      val meshes = renderSTLs(file)
      panel?.updateModel(meshes)
    }
  }

  @RequiresEdt
  private fun detachPreviewPanel() {
    val panel = this.panel
    if (panel != null) {
      previewPanelWrapper.remove(panel.getComponent())
      Disposer.dispose(panel)
      this.panel = null
    }
  }

  @RequiresEdt
  private fun attachPreviewPanel() {
    val panel = panelProvider.createPreviewPanel(project, file)
    this.panel = panel
    previewPanelWrapper.add(panel.getComponent(), BorderLayout.CENTER)
    if (previewPanelWrapper.isShowing) {
      previewPanelWrapper.validate()
    }
    previewPanelWrapper.repaint()
    updatePreview()
  }

  private inner class AttachPanelOnVisibilityChangeListener : ComponentAdapter() {
    override fun componentShown(event: ComponentEvent) {
      if (panel == null) {
        coroutineScope.launch(Dispatchers.EDT) {
          attachPreviewPanel()
        }
      }
    }

    override fun componentHidden(event: ComponentEvent) {
      if (panel != null) {
        detachPreviewPanel()
      }
    }
  }

  private inner class ReparseContentDocumentListener : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
      coroutineScope.launch(Dispatchers.EDT) {
        updatePreview()
      }
    }
  }

  suspend fun python(
    code: String,
    interpreterPath: String = "python"
  ): PythonResult {
    val process = ProcessBuilder(interpreterPath, "-c", code)
      .directory(File(file.parent.path))
      .start()

    val output = ByteArrayOutputStream().let { IOUtils.copy(process.inputStream, it); it }
    val error = ByteArrayOutputStream().let { IOUtils.copy(process.errorStream, it); it }

    return PythonResult(
      process.awaitExit(),
      output.toString(Charsets.UTF_8) ?: "",
      error.toString(Charsets.UTF_8) ?: ""
    )
  }

  data class PythonResult(
    val exitCode: Int,
    val output: String,
    val error: String
  )

  suspend fun pythonCall(code: String): String {
    val pythonSdk = project.pythonSdk?.homePath
    if (pythonSdk == null) {
      throw IllegalStateException("No Python SDK configured for this project.")
    }

    python(code, pythonSdk).let {
      if (it.exitCode != 0) {
        throw PythonProcessException(it.exitCode, it.output, it.error)
      }
      return it.output
    }
  }

  class PythonProcessException(
    val exitCode: Int,
    val output: String,
    val error: String
  ) : Exception("Python process failed with exit code $exitCode: $error")

  suspend fun renderSTLs(file: VirtualFile): String {
    val tmp = createTempDirectory("ij_build123d_").toFile()
    val meshes = try {
      tmp.deleteOnExit()
      val path = tmp.absolutePath.replace("\\", "\\\\")

      var content = readAction { document.text }
      // execute as if it was imported
      content = "__name__ = '" + file.nameWithoutExtension + "'\n\n" + content
      // add rendering code
      content += "\n\ntmp = '$path'\n\n$renderScript"
      pythonCall(content)

      tmp.listFiles()
        .map { it.nameWithoutExtension to it.readText() }
        .toMap()
    } finally {
      tmp.deleteRecursively()
    }

    return jacksonObjectMapper().writeValueAsString(meshes)
  }
}
