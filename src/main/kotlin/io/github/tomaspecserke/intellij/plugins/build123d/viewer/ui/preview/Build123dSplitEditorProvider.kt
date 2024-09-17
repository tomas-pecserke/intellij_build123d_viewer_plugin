package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreviewProvider

@Suppress("UnstableApiUsage")
class Build123dSplitEditorProvider : TextEditorWithPreviewProvider(Build123dPreviewFileEditorProvider()) {
  override fun createSplitEditor(firstEditor: TextEditor, secondEditor: FileEditor): FileEditor {
    require(secondEditor is Build123dPreviewFileEditor) { "Secondary editor should be Build123dPreviewFileEditor" }
    return Build123dEditorWithPreview(
      firstEditor,
      secondEditor
    )
  }
}
