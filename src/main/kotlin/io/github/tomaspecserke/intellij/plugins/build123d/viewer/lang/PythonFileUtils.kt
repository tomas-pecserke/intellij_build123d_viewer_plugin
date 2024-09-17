package io.github.tomaspecserke.intellij.plugins.build123d.viewer.lang

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.Language
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyFile

fun Language.isPythonLanguage(): Boolean {
  return this == PythonLanguage.INSTANCE
}

fun VirtualFile.hasPythonType(): Boolean {
  return FileTypeRegistry.getInstance().isFileOfType(this, PythonFileType.INSTANCE)
}

fun hasBuild123dImported(project: Project, file: VirtualFile): Boolean {
  val psiFile = PsiManager.getInstance(project).findFile(file) as? PyFile ?: return false
  val visitor = PythonImportVisitor()
  psiFile.accept(visitor)
  return visitor.imports.any {
    it.contains("build123d")
  }
}

fun isPythonScratchFile(project: Project, file: VirtualFile): Boolean {
  if (!ScratchUtil.isScratch(file)) {
    return false
  }
  val language = LanguageUtil.getLanguageForPsi(project, file) ?: return false
  return language.isPythonLanguage()
}
