package com.github.lamba92.gradle.docker

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.file.CopySpec
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.property

class DockerImage(
    private val name: String,
    private val project: Project
) : Named {

    val imageName: Property<String> =
        project.objects
            .property<String>()
            .convention(name)

    val imageVersion: Property<String> =
        project.objects
            .property<String>()
            .convention(project.provider { project.version.toString() })

    val buildArgs: MapProperty<String, String> =
        project.objects.mapProperty()

    val platforms: ListProperty<DockerPlatform> =
        project.objects.listProperty()

    val files =
        project.objects.property<CopySpec>()

    fun files(action: CopySpec.() -> Unit) {
        files = project.copySpec {
            files.orNull?.let { with(it) }
            action()
        }
    }

    fun configureJvmApplication(
        baseImageName: String = "openjdk",
        baseImageTag: String? = null
    ) {
        val hasApplicationPlugin = project.plugins.hasPlugin("org.gradle.application")
        if (!hasApplicationPlugin) {
            project.logger.error(getJvmAppErrorMessage(name, project.path))
            return
        }

        val actualJdkVersion = baseImageTag ?: getJavaMajorVersion()

        val createDockerfileTaskName = buildString {
            append("create")
            append(project.name.toCamelCase())
            append(baseImageName.toCamelCase())
            append(actualJdkVersion)
            append("JvmAppDockerfile")
        }

        val createDockerfileTask =
            project.tasks.getOrRegister<CreateJvmDockerfile>(createDockerfileTaskName) {
                appName = project.name
                imageTag = actualJdkVersion
                imageName = baseImageName
                destinationFile =
                    project
                        .layout
                        .buildDirectory
                        .file("dockerfiles/${project.name}-${imageName.get()}-${imageTag.get()}.dockerfile")
            }

        files {
            from(project.tasks.named<Sync>(DistributionPlugin.TASK_INSTALL_NAME))
            from(createDockerfileTask) {
                rename { "Dockerfile" }
            }
        }
    }

    override fun getName() = name
}
