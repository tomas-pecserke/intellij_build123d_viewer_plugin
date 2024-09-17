package io.github.tomaspecserke.intellij.plugins.build123d.viewer.ui.preview

import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import io.github.tomaspecserke.intellij.plugins.build123d.viewer.Build123dBundle

@Suppress("UnstableApiUsage")
class Build123dEditorWithPreview(
    editor: TextEditor,
    preview: Build123dPreviewFileEditor
): TextEditorWithPreview(
    editor,
    preview,
    Build123dBundle.message("build123d.editor.name"),
    Layout.SHOW_EDITOR_AND_PREVIEW,
    false
) {
    init {
        // allow launching actions while in preview mode;
        // FIXME: better solution IDEA-354102
        editor.editor.contentComponent.putClientProperty(ActionUtil.ALLOW_ACTION_PERFORM_WHEN_HIDDEN, true)
        preview.setMainEditor(editor.editor)
    }

    override fun onLayoutChange(oldValue: Layout?, newValue: Layout?) {
        super.onLayoutChange(oldValue, newValue)
        // Editor tab will lose focus after switching to JCEF preview for some reason.
        // So we should explicitly request focus for our editor here.
        if (newValue == Layout.SHOW_PREVIEW) {
            requestFocusForPreview()
        }
    }

    private fun requestFocusForPreview() {
        val preferredComponent = myPreview.preferredFocusedComponent
        if (preferredComponent != null) {
            preferredComponent.requestFocus()
            return
        }
        myPreview.component.requestFocus()
    }
}
