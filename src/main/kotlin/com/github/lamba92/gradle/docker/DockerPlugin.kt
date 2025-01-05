@file:Suppress("unused")

package com.github.lamba92.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

class DockerPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "docker"
    }

    override fun apply(target: Project): Unit = with(target) {
        val dockerExtension = extensions.create<DockerExtension>(
            EXTENSION_NAME,
            EXTENSION_NAME,
            container { DockerImage(it, project) },
            RegistriesContainer(container { DockerRegistry(it, objects) })
        )

        dockerExtension.images.register("main") {
            imageName = project.name
            if (plugins.hasPlugin("org.gradle.application")) configureJvmApplication()
        }

        val dockerBuildAllTask = tasks.register("dockerBuild") {
            group = "build"
        }

        val dockerBuildxAllTask = tasks.register("dockerBuildx") {
            group = "build"
        }

        val dockerBuildxPublishAllTask = tasks.register("dockerBuildxPublish") {
            group = "build"
        }

        val dockerPublishAll = tasks.register("dockerPublish") {
            group = "publishing"
        }

        configureLogin(dockerExtension)

        tasks.register<Exec>("createBuildxBuilder") {
            group = "docker"
            executable = "docker"
            args("buildx", "create", "--name", "gradle-builder", "--use")
        }

        configurePlugin(dockerExtension, dockerBuildAllTask, dockerPublishAll, dockerBuildxAllTask, dockerBuildxPublishAllTask)
    }
}

fun Project.configureLogin(dockerExtension: DockerExtension) {
    val dockerLoginAll = tasks.register("dockerLogin") {
        group = "docker"
    }

    dockerExtension.registries.all {
        val dockerLoginTaskName = "dockerLogin${registryName.capitalized()}"
        val loginTask = tasks.register<Exec>(dockerLoginTaskName) {
            group = "docker"
            executable = "docker"
            args("login", "-u", username.get(), "-p", password.get(), url.get())
        }

        dockerLoginAll {
            dependsOn(loginTask)
        }
    }
}

private fun Project.configurePlugin(
    dockerExtension: DockerExtension,
    dockerBuildAllTask: TaskProvider<Task>,
    dockerPublishAll: TaskProvider<Task>,
    dockerBuildxAllTask: TaskProvider<Task>,
    dockerBuildxPublishAllTask: TaskProvider<Task>
) {
    dockerExtension.images.all {
        val dockerPrepareTaskName = "dockerPrepare${imageName.get().capitalized()}"

        val dockerPrepareDir =
            project
                .layout
                .buildDirectory
                .dir("docker/prepare/${imageName.get()}")

        val dockerPrepareTask = tasks.register<Sync>(dockerPrepareTaskName) {
            with(files.get())
            into(dockerPrepareDir)
        }
        val baseTag = "${imageName.get()}:${imageVersion.get()}"
        val dockerBuildTaskName = "dockerBuild${imageName.get().capitalized()}"
        val dockerBuildTask = tasks.register<Exec>(dockerBuildTaskName) {
            group = "build"
            dependsOn(dockerPrepareTask)
            inputs.dir(dockerPrepareDir)
            executable = "docker"
            args(buildList {
                add("build")
                buildArgs.get().forEach { (key, value) ->
                    addAll("--build-arg", "$key=$value")
                }
                addAll("-t", baseTag)
                dockerExtension.registries.forEach { registry ->
                    addAll("-t", "${registry.imageTagPrefix.get()}$baseTag")
                }
                add(dockerPrepareDir.get().asFile.absolutePath)
            })
        }

        dockerBuildAllTask {
            dependsOn(dockerBuildTask)
        }

        configurePublication(
            dockerExtension = dockerExtension,
            dockerImage = this,
            dockerPublishAll = dockerPublishAll,
            baseTag = baseTag
        )

        configureBuildx(
            dockerImage = this,
            dockerExtension = dockerExtension,
            baseTag = baseTag,
            dockerPrepareDir = dockerPrepareDir,
            dockerBuildxAllTask = dockerBuildxAllTask,
            dockerBuildxPublishAllTask = dockerBuildxPublishAllTask
        )
    }
}

private fun Project.configureBuildx(
    dockerImage: DockerImage,
    dockerExtension: DockerExtension,
    baseTag: String,
    dockerPrepareDir: Provider<Directory>,
    dockerBuildxAllTask: TaskProvider<Task>,
    dockerBuildxPublishAllTask: TaskProvider<Task>
) {
    val dockerBuildxTaskName = "dockerBuildxBuild${dockerImage.imageName.get().capitalized()}"
    fun buildxArgs(publish: Boolean) = buildList {
        addAll("buildx", "build")
        dockerImage.platforms.get()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(",")
            ?.let { addAll("--platform", it) }
        dockerImage.buildArgs.get().forEach { (key, value) ->
            addAll("--build-arg", "$key=$value")
        }
        dockerExtension.registries.forEach { registry ->
            addAll("--tag", "${registry.imageTagPrefix.get()}$baseTag")
        }
        if (publish) this.add("--push")
        this.add(dockerPrepareDir.get().asFile.absolutePath)
    }

    val dockerBuildxBuildTask = tasks.register<Exec>(dockerBuildxTaskName) {
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
        buildxArgs = ::buildxArgs
    )
}

private fun Project.configureBuildxPublishing(
    dockerExtension: DockerExtension,
    dockerImage: DockerImage,
    dockerBuildxPublishAllTask: TaskProvider<Task>,
    buildxArgs: (Boolean) -> List<String>
) {
    dockerExtension.registries.all {
        val dockerBuildxPublishTaskName =
            "dockerBuildxPublish${dockerImage.imageName.get()}To$registryName"
        val dockerBuildxPublishTask = tasks.register<Exec>(dockerBuildxPublishTaskName) {
            group = "build"
            executable = "docker"
            args(buildxArgs(true))
        }
        val publishAllBuildxToThisRepositoryTaskName = "publishAllBuildxImagesTo$registryName"
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
    baseTag: String
) {
    dockerExtension.registries.all {
        val dockerPublishTaskName = "dockerPublish${dockerImage.imageName.get()}To$registryName"
        val dockerPublishTask = tasks.register<Exec>(dockerPublishTaskName) {
            group = "publishing"
            executable = "docker"
            args("push", "${imageTagPrefix.get()}/$baseTag")
        }

        val publishAllToThisRepositoryTaskName = "publishAllImagesTo$registryName"
        val publishAllToThisRepositoryTask =
            tasks.getOrRegister(publishAllToThisRepositoryTaskName) {
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

