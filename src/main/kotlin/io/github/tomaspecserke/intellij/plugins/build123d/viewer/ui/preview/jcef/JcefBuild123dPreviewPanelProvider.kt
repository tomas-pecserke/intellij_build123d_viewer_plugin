package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview.jcef

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview.Build123dPreviewPanel
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview.Build123dPreviewPanelProvider

class JcefBuild123dPreviewPanelProvider : Build123dPreviewPanelProvider {
  override fun createPreviewPanel(): Build123dPreviewPanel {
    return JcefBuild123dPreviewPanel()
  }

  override fun createPreviewPanel(project: Project, virtualFile: VirtualFile): Build123dPreviewPanel {
    return JcefBuild123dPreviewPanel(project, virtualFile)
  }
}
