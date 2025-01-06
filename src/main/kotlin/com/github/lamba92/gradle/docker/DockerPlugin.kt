package com.github.lamba92.gradle.docker

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

        val dockerPublishAll = tasks.register("dockerPush") {
            group = "publishing"
        }

        val dockerBuildxBuildAllTask = tasks.register("dockerBuildxBuild") {
            group = "build"
        }

        val dockerBuildxPublishAllTask = tasks.register("dockerBuildxPush") {
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
            dockerBuildxPublishAllTask = dockerBuildxPublishAllTask
        )
    }
}

private fun Project.configurePlugin(
    dockerExtension: DockerExtension,
    dockerBuildAllTask: TaskProvider<Task>,
    dockerPublishAll: TaskProvider<Task>,
    dockerBuildxBuildAllTask: TaskProvider<Task>,
    dockerBuildxPublishAllTask: TaskProvider<Task>
) {
    dockerExtension.images.all {
        val dockerPrepareTaskName = "dockerPrepare${imageName.get().toCamelCase()}"

        val dockerPrepareDir =
            project
                .layout
                .buildDirectory
                .dir("docker/prepare/${imageName.get()}")

        val dockerPrepareTask = tasks.register<Sync>(dockerPrepareTaskName) {
            with(files.get())
            into(dockerPrepareDir)
        }
        val baseTag = provider {
            val actualImageVersion = imageVersion.orElse(project.version.toString()).get()
            "${imageName.get()}:$actualImageVersion"
        }
        val dockerBuildTaskName = "dockerBuild${imageName.get().toCamelCase()}"
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
                addAll("-t", baseTag.get().toString())
                dockerExtension.registries.forEach { registry ->
                    addAll("-t", "${registry.imageTagPrefix.get()}${baseTag.get()}")
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
            baseTag = baseTag,
            dockerBuildTask = dockerBuildTask
        )

        configureBuildx(
            dockerImage = this,
            dockerExtension = dockerExtension,
            baseTag = baseTag,
            dockerPrepareDir = dockerPrepareDir,
            dockerBuildxAllTask = dockerBuildxBuildAllTask,
            dockerBuildxPublishAllTask = dockerBuildxPublishAllTask
        )
    }
}

private fun Project.configureBuildx(
    dockerImage: DockerImage,
    dockerExtension: DockerExtension,
    baseTag: Provider<String>,
    dockerPrepareDir: Provider<Directory>,
    dockerBuildxAllTask: TaskProvider<Task>,
    dockerBuildxPublishAllTask: TaskProvider<Task>
) {
    val dockerBuildxTaskName = "dockerBuildxBuild${dockerImage.imageName.get().toCamelCase()}"
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
            addAll("--tag", "${registry.imageTagPrefix.get().suffixIfNot("/")}${baseTag.get()}")
        }
        when {
            publish -> add("--push")
            else ->  add("--load")
        }
        add(dockerPrepareDir.get().asFile.absolutePath)
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
    baseTag: Provider<String>,
    dockerBuildTask: TaskProvider<Exec>
) {
    dockerExtension.registries.all {
        val dockerPublishTaskName = "dockerPublish${dockerImage.imageName.get()}To$registryName"
        val dockerPublishTask = tasks.register<Exec>(dockerPublishTaskName) {
            dependsOn(dockerBuildTask)
            group = "publishing"
            executable = "docker"
            args("push", "${imageTagPrefix.get().suffixIfNot("/")}${baseTag.get()}")
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

