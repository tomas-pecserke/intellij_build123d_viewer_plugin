package io.github.tomaspecserke.intellij.plugins.build123d.viewer

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

object Build123dBundle {
    @NonNls
    private const val BUNDLE = "messages.Build123dBundle"
    private val INSTANCE = DynamicBundle(Build123dBundle::class.java, BUNDLE)

    @Nls
    fun message(
        @PropertyKey(resourceBundle = "messages.Build123dBundle") key: String,
        vararg params: Object
    ) = INSTANCE.getMessage(key, params)
}
