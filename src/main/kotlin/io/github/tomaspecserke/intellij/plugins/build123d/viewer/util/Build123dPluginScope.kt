package io.github.tomaspecserke.intellij.plugins.build123d.viewer.util

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope

@Suppress("UnstableApiUsage")
@Service(Service.Level.PROJECT)
class Build123dPluginScope(private val coroutineScope: CoroutineScope) {
    companion object {
        fun createChildScope(project: Project): CoroutineScope {
            return scope(project).childScope("Build123dPluginScope")
        }

        fun scope(project: Project): CoroutineScope {
            return project.service<Build123dPluginScope>().coroutineScope
        }
    }
}
