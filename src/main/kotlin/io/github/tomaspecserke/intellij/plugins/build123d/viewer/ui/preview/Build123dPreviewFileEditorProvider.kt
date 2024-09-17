package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview

import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDocument
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.lang.isPythonScratchFile
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.lang.hasBuild123dImported
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.lang.hasPythonType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UnstableApiUsage")
class Build123dPreviewFileEditorProvider : WeighedFileEditorProvider(), AsyncFileEditorProvider {
  override fun accept(project: Project, file: VirtualFile): Boolean {
    return (file.hasPythonType() || isPythonScratchFile(project, file))
        && hasBuild123dImported(project, file)
  }

  override suspend fun createFileEditor(
    project: Project,
    file: VirtualFile,
    document: Document?,
    editorCoroutineScope: CoroutineScope
  ): FileEditor {
    return withContext(Dispatchers.EDT) {
      Build123dPreviewFileEditor(project, file, document!!)
    }
  }

  override fun createEditor(project: Project, file: VirtualFile): FileEditor {
    return Build123dPreviewFileEditor(project, file, file.findDocument()!!)
  }

  override fun getEditorTypeId(): String = "build123d-preview-editor"

  override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}
