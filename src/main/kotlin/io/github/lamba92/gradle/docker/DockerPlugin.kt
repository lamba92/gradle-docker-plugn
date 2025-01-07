@file:Suppress("ktlint:standard:no-unused-imports")

package io.github.lamba92.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

@Suppress("unused")
class DockerPlugin : Plugin<Project> {
    companion object {
        const val EXTENSION_NAME = "docker"
    }

    override fun apply(target: Project): Unit =
        with(target) {
            val dockerExtension =
                extensions.create<DockerExtension>(
                    EXTENSION_NAME,
                    EXTENSION_NAME,
                    DockerImagesContainer(container { DockerImage(it, project) }),
                    DockerRegistriesContainer(container { DockerRegistry(it, objects) }),
                )

            dockerExtension.images.register("main") {
                imageName = project.name
                plugins.withId("org.gradle.application") { configureJvmApplication() }
            }

            val dockerBuildAllTask =
                tasks.register("dockerBuild") {
                    group = "build"
                }

            val dockerPublishAll =
                tasks.register("dockerPush") {
                    group = "publishing"
                }

            val dockerBuildxBuildAllTask =
                tasks.register("dockerBuildxBuild") {
                    group = "build"
                }

            val dockerBuildxPublishAllTask =
                tasks.register("dockerBuildxPush") {
                    group = "publishing"
                }

            tasks.register<Exec>("createBuildxBuilder") {
                group = "docker"
                executable = "docker"
                args("buildx", "create", "--name", "gradle-builder", "--use")
                isIgnoreExitValue = true
            }

            configurePlugin(
                dockerExtension = dockerExtension,
                dockerBuildAllTask = dockerBuildAllTask,
                dockerPublishAll = dockerPublishAll,
                dockerBuildxBuildAllTask = dockerBuildxBuildAllTask,
                dockerBuildxPublishAllTask = dockerBuildxPublishAllTask,
            )
        }
}

private fun Project.configurePlugin(
    dockerExtension: DockerExtension,
    dockerBuildAllTask: TaskProvider<Task>,
    dockerPublishAll: TaskProvider<Task>,
    dockerBuildxBuildAllTask: TaskProvider<Task>,
    dockerBuildxPublishAllTask: TaskProvider<Task>,
) {
    dockerExtension.images.all {
        val dockerPrepareDir =
            project
                .layout
                .buildDirectory
                .dir("docker/prepare/$name")

        val dockerPrepareTask =
            tasks.register<Sync>("dockerPrepare${name.toCamelCase()}") {
                with(files.get())
                into(dockerPrepareDir)
            }

        val baseTag = imageName.map { "$it:${imageVersion.orNull ?: project.version}" }
        val dockerBuildTask =
            tasks.register<Exec>("dockerBuild${name.toCamelCase()}") {
                group = "build"
                dependsOn(dockerPrepareTask)
                inputs.dir(dockerPrepareDir)
                executable = "docker"
                args(
                    buildList {
                        add("build")
                        buildArgs.get().forEach { (key, value) ->
                            addAll("--build-arg", "$key=$value")
                        }
                        addAll("-t", baseTag.get().toString())
                        dockerExtension.registries.forEach { registry ->
                            val prefix = registry.imageTagPrefix.get().suffixIfNot("/")
                            addAll("-t", "$prefix${baseTag.get()}")
                            if (isLatestTag.get()) addAll("-t", "$prefix${imageName.get()}:latest")
                        }
                        add(dockerPrepareDir.get().asFile.absolutePath)
                    },
                )
            }

        dockerBuildAllTask {
            dependsOn(dockerBuildTask)
        }

        configurePublication(
            dockerExtension = dockerExtension,
            dockerImage = this,
            dockerPublishAll = dockerPublishAll,
            baseTag = baseTag,
            dockerBuildTask = dockerBuildTask,
        )

        configureBuildx(
            dockerImage = this,
            dockerExtension = dockerExtension,
            baseTag = baseTag,
            dockerPrepareDir = dockerPrepareDir,
            dockerBuildxAllTask = dockerBuildxBuildAllTask,
            dockerBuildxPublishAllTask = dockerBuildxPublishAllTask,
            dockerPrepareTask = dockerPrepareTask,
        )

        val dockerRunTaskName = if (name == "main") "dockerRun" else "dockerRun${name.toCamelCase()}"
        tasks.register<Exec>(dockerRunTaskName) {
            group = "docker"
            dependsOn(dockerBuildTask)
            executable = "docker"
            args("run", "--rm", baseTag.get().toString())
        }
    }
}

private fun Project.configureBuildx(
    dockerImage: DockerImage,
    dockerExtension: DockerExtension,
    baseTag: Provider<String>,
    dockerPrepareDir: Provider<Directory>,
    dockerBuildxAllTask: TaskProvider<Task>,
    dockerBuildxPublishAllTask: TaskProvider<Task>,
    dockerPrepareTask: TaskProvider<Sync>,
) {
    fun buildxArgs(publish: Boolean) =
        buildList {
            addAll("buildx", "build")
            dockerImage.platforms.get()
                .takeIf { it.isNotEmpty() }
                ?.joinToString(",")
                ?.let { addAll("--platform", it) }
            dockerImage.buildArgs.get().forEach { (key, value) ->
                addAll("--build-arg", "$key=$value")
            }
            dockerExtension.registries.forEach { registry ->
                val prefix = registry.imageTagPrefix.get().suffixIfNot("/")
                addAll("--tag", "$prefix${baseTag.get()}")
                if (dockerImage.isLatestTag.get()) {
                    addAll("--tag", "$prefix${dockerImage.imageName.get()}:latest")
                }
            }
            when {
                publish -> add("--push")
                else -> add("--load")
            }
            add(dockerPrepareDir.get().asFile.absolutePath)
        }

    val dockerBuildxBuildTask =
        tasks.register<Exec>("dockerBuildxBuild${dockerImage.name.toCamelCase()}") {
            group = "build"
            executable = "docker"
            args(buildxArgs(false))
        }

    dockerBuildxAllTask {
        dependsOn(dockerBuildxBuildTask)
    }

    configureBuildxPublishing(
        dockerExtension = dockerExtension,
        dockerImage = dockerImage,
        dockerBuildxPublishAllTask = dockerBuildxPublishAllTask,
        buildxArgs = ::buildxArgs,
        dockerPrepareTask = dockerPrepareTask,
    )
}

private fun Project.configureBuildxPublishing(
    dockerExtension: DockerExtension,
    dockerImage: DockerImage,
    dockerBuildxPublishAllTask: TaskProvider<Task>,
    buildxArgs: (Boolean) -> List<String>,
    dockerPrepareTask: TaskProvider<Sync>,
) {
    dockerExtension.registries.all {
        val dockerBuildxPublishTaskName =
            "dockerBuildxPublish${dockerImage.name.toCamelCase()}To${registryName.toCamelCase()}"
        val dockerBuildxPublishTask =
            tasks.register<Exec>(dockerBuildxPublishTaskName) {
                dependsOn(dockerPrepareTask)
                group = "build"
                executable = "docker"
                args(buildxArgs(true))
            }
        val publishAllBuildxToThisRepositoryTaskName = "publishAllBuildxImagesTo${registryName.toCamelCase()}"
        val publishAllToThisRepositoryTask =
            tasks.getOrRegister(publishAllBuildxToThisRepositoryTaskName) {
                group = "publishing"
            }
        publishAllToThisRepositoryTask {
            dependsOn(dockerBuildxPublishTask)
        }
        dockerBuildxPublishAllTask {
            dependsOn(dockerBuildxPublishTask)
        }
    }
}

private fun Project.configurePublication(
    dockerExtension: DockerExtension,
    dockerImage: DockerImage,
    dockerPublishAll: TaskProvider<Task>,
    baseTag: Provider<String>,
    dockerBuildTask: TaskProvider<Exec>,
) {
    dockerExtension.registries.all {
        val dockerPublishTask =
            tasks.register<Exec>("dockerPublish${dockerImage.name.toCamelCase()}To${registryName.toCamelCase()}") {
                dependsOn(dockerBuildTask)
                group = "publishing"
                executable = "docker"
                args("push", "${imageTagPrefix.get().suffixIfNot("/")}${baseTag.get()}")
            }

        val publishAllToThisRepositoryTask =
            tasks.getOrRegister("publishAllImagesTo${registryName.toCamelCase()}") {
                group = "publishing"
            }
        publishAllToThisRepositoryTask {
            dependsOn(dockerPublishTask)
        }
        dockerPublishAll {
            dependsOn(dockerPublishTask)
        }
    }
}
