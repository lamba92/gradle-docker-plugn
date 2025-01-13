@file:Suppress("ktlint:standard:no-unused-imports")

package io.github.lamba92.gradle.docker

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.file.CopySourceSpec
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
import org.intellij.lang.annotations.Language

/**
 * Represents a Docker image configuration within a Gradle project. This class allows setting and managing
 * properties relevant to an image, such as name, version, platforms, build arguments, and associated file
 * specifications. It also supports configuration for JVM applications.
 *
 * @property isLatestTag Indicates whether an additional "latest" tag should be applied to this Docker image. Defaults to `true`.
 * @property imageName Specifies the name of the Docker image. Defaults to the name in the [DockerImageContainer].
 * @property imageVersion Defines the version of the Docker image. If not set, the version will be derived from the project version.
 * @property buildArgs A map of build arguments to provide during the Docker image build process (`--build-arg`). Defaults to an empty map.
 * @property platforms A list of platforms to build the Docker image for using Docker Buildx. Defaults to `linux/amd64` and `linux/arm64/v8`.
 * @property files A copy specification detailing the source files to associate with the Docker image. If the `application` plugin is applied,
 * the Dockerfile will be generated automatically and the application files will be bundled with the image.
 */
public class DockerImage(
    private val name: String,
    private val project: Project,
) : Named {
    public companion object DockerPlatform {
        public const val LINUX_AMD64: String = "linux/amd64"
        public const val LINUX_ARM64: String = "linux/arm64"
        public const val LINUX_ARM64_V8: String = "linux/arm64/v8"
        public const val LINUX_ARM_V7: String = "linux/arm/v7"
        public const val LINUX_ARM_V6: String = "linux/arm/v6"
        public const val LINUX_S390X: String = "linux/s390x"
        public const val LINUX_PPC64LE: String = "linux/ppc64le"
        public const val LINUX_386: String = "linux/386"
    }

    public val isLatestTag: Property<Boolean> =
        project.objects
            .property<Boolean>()
            .convention(true)

    public val imageName: Property<String> =
        project.objects
            .property<String>()
            .convention(name)

    public val imageVersion: Property<String> =
        project.objects
            .property<String>()

    public val buildArgs: MapProperty<String, String> =
        project.objects.mapProperty()

    public val platforms: ListProperty<String> =
        project.objects
            .listProperty<String>()
            .convention(listOf(LINUX_AMD64, LINUX_ARM64_V8))

    public val files: Property<CopySpec> =
        project.objects.property<CopySpec>()

    /**
     * Configures files for the `docker build` directory.
     */
    public fun files(action: CopySourceSpec.() -> Unit) {
        files =
            project.copySpec {
                files.orNull?.let { with(it) }
                action()
            }
    }

    /**
     * Configures a JVM-based Docker application using a specified base image name and tag.
     * This method checks if the Gradle 'application' plugin is applied to the project,
     * creates a custom Dockerfile task, and sets up the necessary files for the Docker build directory.
     *
     * @param baseImageName The name of the base Docker image to use for the application. Defaults to "eclipse-temurin".
     * @param baseImageTag The tag of the base Docker image to use for the application. Defaults to "21-alpine".
     * @param additionalConfiguration Any additional configuration to append to the dockerfile.
     */
    public fun configureJvmApplication(
        baseImageName: String = "eclipse-temurin",
        baseImageTag: String = "21-alpine",
        @Language("Dockerfile")
        additionalConfiguration: String? = null,
    ) {
        val hasApplicationPlugin = project.plugins.hasPlugin("org.gradle.application")
        if (!hasApplicationPlugin) {
            project.logger.error(getJvmAppErrorMessage(name, project.path))
            return
        }

        val createDockerfileTaskName =
            buildString {
                append("create")
                append(project.name.toCamelCase())
                append(baseImageName.toCamelCase())
                append(baseImageTag.toCamelCase())
                append("JvmAppDockerfile")
            }

        val createDockerfileTask =
            project.tasks.getOrRegister<CreateJvmDockerfile>(createDockerfileTaskName) {
                appName = project.name
                imageTag = baseImageTag
                imageName = baseImageName
                additionalConfig = additionalConfiguration
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

    override fun getName(): String = name
}
