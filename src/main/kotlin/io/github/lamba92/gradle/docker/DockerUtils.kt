package io.github.lamba92.gradle.docker

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

@JvmName("getOrRegisterTyped")
internal inline fun <reified T : Task> TaskContainer.getOrRegister(
    name: String,
    noinline action: T.() -> Unit,
): TaskProvider<T> {
    if (name in names) return named<T>(name)
    return register<T>(name, action)
}

internal fun TaskContainer.getOrRegister(
    name: String,
    action: (Task.() -> Unit)? = null,
): TaskProvider<Task> {
    if (name in names) return named(name)
    return action?.let { register(name, it) } ?: register(name)
}

/**
 * Generates a Dockerfile as string for a JVM-based application.
 *
 * @param imageName The base Docker image name.
 * @param imageTag The tag of the base Docker image.
 * @param appName The name of the JVM application.
 * @return A formatted Dockerfile string configured with the specified image details and application setup.
 */
internal fun jvmAppDockerImageString(
    imageName: String,
    imageTag: String,
    appName: String,
): String =
    """
    FROM $imageName:$imageTag

    COPY bin $appName/bin
    COPY lib $appName/lib

    CMD ["$appName/bin/$appName"]
    """.trimIndent()

public fun getJvmAppErrorMessage(
    imageName: String,
    projectPath: String,
): String =
    """
    To configure Docker image '$imageName' as a JVM App you need to apply the Gradle 'application' plugin.
    Add 'application' to the plugins block in the script of project $projectPath:

    ```kotlin
    plugins {
        application
    }
    ```
    """.trimIndent()

internal fun <T> MutableList<T>.addAll(vararg elements: T) = addAll(elements)

internal fun String.toCamelCase() =
    split(Regex("[\\s\\-_.]+")) // Split by spaces, hyphens, underscores, or dots
        .filter { it.isNotEmpty() } // Remove empty segments
        .joinToString("") { it.lowercase().capitalized() }

internal fun String.suffixIfNot(string: String): String = if (endsWith(string)) this else "$this$string"
