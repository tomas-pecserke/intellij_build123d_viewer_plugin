package io.github.tomaspecserke.intellij.plugins.build123d.viewer.lang

import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyRecursiveElementVisitor

@Suppress("UnstableApiUsage")
class PythonImportVisitor : PyRecursiveElementVisitor() {
  private val importList = mutableListOf<String>()

  val imports: List<String>
    get() = importList.toList()

  override fun visitPyFromImportStatement(node: PyFromImportStatement) {
    super.visitPyFromImportStatement(node)
    val name = node.importSourceQName?.toString()
    if (name != null) {
      importList.add(name)
    }
  }
}
