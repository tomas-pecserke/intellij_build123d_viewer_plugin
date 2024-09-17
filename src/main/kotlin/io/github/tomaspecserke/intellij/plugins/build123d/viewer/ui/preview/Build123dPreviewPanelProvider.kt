package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface Build123dPreviewPanelProvider {
  fun createPreviewPanel(): Build123dPreviewPanel
  fun createPreviewPanel(project: Project, virtualFile: VirtualFile) = createPreviewPanel()
}
