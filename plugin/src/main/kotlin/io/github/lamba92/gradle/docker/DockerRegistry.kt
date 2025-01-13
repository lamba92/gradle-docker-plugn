package io.github.lamba92.gradle.docker

import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

/**
 * Represents a Docker registry configuration.
 *
 * This class allows defining the name of a Docker registry along with associated properties,
 * such as a prefix for image tags.
 *
 * @property registryName The name of the Docker registry in the [DockerRegistryContainer].
 * @property imageTagPrefix A property for setting a prefix to be used with Docker image tags when working with this registry.
 */
public class DockerRegistry(
    public val registryName: String,
    objectFactory: ObjectFactory,
) : Named {
    public val imageTagPrefix: Property<String> = objectFactory.property<String>()

    override fun getName(): String = registryName
}
