package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview.jcef

import com.intellij.idea.AppMode
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.project.BaseProjectDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.components.JBViewport
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.intellij.ui.jcef.executeJavaScript
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview.Build123dPreviewPanel
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.util.Build123dApplicationScope
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.util.Build123dPluginScope
import io.ktor.util.encodeBase64
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.awt.BorderLayout
import javax.swing.JComponent

class JcefBuild123dPreviewPanel(
  private val project: Project?,
  private val virtualFile: VirtualFile?
) : JCEFHtmlPanel(isOffScreenRendering(), null, null), Build123dPreviewPanel, UserDataHolder by UserDataHolderBase() {
  companion object {
    @Suppress("UnstableApiUsage")
    val canBeUsed: Boolean
      get() = !AppMode.isRemoteDevHost() && JBCefApp.isSupported()

    private fun isOffScreenRendering(): Boolean = Registry.`is`("ide.browser.jcef.build123dView.osr.enabled")

    private val contents = JcefBuild123dPreviewPanel::class.java.getResource("/viewer/index.html")!!.readText()
  }

  private val coroutineScope =
    project?.let(Build123dPluginScope::createChildScope) ?: Build123dApplicationScope.createChildScope()

  private val projectRoot = coroutineScope.async(context = Dispatchers.Default) {
    if (virtualFile != null && project != null) {
      BaseProjectDirectories.getInstance(project).getBaseDirectoryFor(virtualFile)
    } else null
  }

  private var previewInnerComponent: JComponent? = null

  constructor() : this(null, null)

  init {
    if (!canBeUsed) {
      throw IllegalStateException("Tried to create a JCEF panel, but JCEF is not supported in the current environment")
    }
  }

  private val panelComponent by lazy { createComponent() }

  override fun getComponent(): JComponent {
    return panelComponent
  }

  private fun createComponent(): JComponent {
    previewInnerComponent = super.getComponent()
    if (project == null || virtualFile == null) {
      return previewInnerComponent!!
    }

    val panel = JBLoadingPanel(BorderLayout(), this)

    coroutineScope.async(context = Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
      panel.startLoading()
      val viewPort = JBViewport()
      viewPort.add(previewInnerComponent)

      loadHTML(contents)
      jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 10)

      panel.add(viewPort)
      projectRoot.await()
      panel.stopLoading()
    }
    return panel
  }

  override fun updateModel(json: String) {
    coroutineScope.async(context = Dispatchers.EDT, start = CoroutineStart.UNDISPATCHED) {
      openDevtools()
      val encoded = json.encodeBase64()
      executeJavaScript(
        """
        var json = atob('$encoded')
        var models = JSON.parse(json)
        loadModels(models)
        """.trimIndent()
      ).let {
        fileLogger().warn("JS: $it")
      }
    }
  }
}
