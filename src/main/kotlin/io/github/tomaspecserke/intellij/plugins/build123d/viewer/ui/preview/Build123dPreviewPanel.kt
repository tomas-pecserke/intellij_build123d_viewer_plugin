package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview

import com.intellij.openapi.Disposable
import javax.swing.JComponent

interface Build123dPreviewPanel : Disposable {
  fun getComponent(): JComponent
  fun updateModel(json: String)
}
