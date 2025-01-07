package io.github.lamba92.gradle.docker

import org.gradle.api.Named
import org.gradle.api.plugins.ExtensionAware

/**
 * Represents a Docker extension entity, managing Docker images and registries configuration
 * within a Gradle project context.
 *
 * @param extensionName The name of the extension.
 * @property images A container for managing Docker images.
 * @property registries A container for managing Docker registries.
 */
public abstract class DockerExtension(
    public val extensionName: String,
    public val images: DockerImageContainer,
    public val registries: DockerRegistryContainer,
) : ExtensionAware, Named {
    override fun getName(): String = extensionName

    /**
     * Configures the Docker registries container using the specified configuration action.
     *
     * @return The configured `DockerRegistriesContainer` instance.
     */
    public fun registries(action: DockerRegistryContainer.() -> Unit): DockerRegistryContainer = registries.apply(action)

    /**
     * Configures the Docker images container using the specified configuration action.
     *
     * @return The configured `DockerImagesContainer` instance.
     */
    public fun images(action: DockerImageContainer.() -> Unit): DockerImageContainer = images.apply(action)
}
