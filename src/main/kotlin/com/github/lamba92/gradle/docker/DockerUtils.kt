package com.github.lamba92.gradle.docker

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

inline fun <reified T : Task> TaskContainer.getOrRegister(name: String, noinline action: T.() -> Unit): TaskProvider<T> {
    if (name in names) return named<T>(name)
    return register<T>(name, action)
}

fun TaskContainer.getOrRegister(name: String, action: (Task.() -> Unit)? = null): TaskProvider<Task> {
    if (name in names) return named(name)
    return action?.let { register(name, it) } ?: register(name)
}

fun jvmAppDockerImageString(
    imageName: String,
    imageTag: String,
    appName: String
) = """
FROM $imageName:$imageTag

COPY bin $appName/bin
COPY lib $appName/lib

CMD ["$appName/bin/$appName"]
""".trimIndent()

fun getJvmAppErrorMessage(imageName: String, projectPath: String) =
    """
To configure Docker image '$imageName' as a JVM App you need to apply the Gradle 'application' plugin.
Add 'application' to the plugins block in the script of project ${projectPath}:

```kotlin
plugins {
    application
}
```
""".trimIndent()

fun <T> MutableList<T>.addAll(vararg elements: T) =
    addAll(elements)