package io.github.tomaspecserke.intellij.plugins.build123d.viewer.util

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope

@Suppress("UnstableApiUsage")
@Service(Service.Level.APP)
class Build123dApplicationScope(private val coroutineScope: CoroutineScope) {
  companion object {
    fun createChildScope(): CoroutineScope {
      return scope().childScope("Build123dApplicationScope")
    }

    fun scope(): CoroutineScope {
      return service<Build123dApplicationScope>().coroutineScope
    }
  }
}
