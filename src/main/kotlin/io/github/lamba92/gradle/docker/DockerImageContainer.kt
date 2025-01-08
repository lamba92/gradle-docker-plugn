package io.github.lamba92.gradle.docker

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider

/**
 * Container for managing a collection of [DockerImage] objects.
 * This class is a delegate of [NamedDomainObjectContainer]<[DockerImage]> and provides functionality
 * to configure and retrieve Docker images, specifically identifying the `main` Docker image.
 */
public class DockerImageContainer(
    private val delegate: NamedDomainObjectContainer<DockerImage>,
) : NamedDomainObjectContainer<DockerImage> by delegate {
    /**
     * Configures the `main` Docker image with the specified action.
     * The `main` Docker image is the default Docker image used by the plugin and always exists.
     *
     * @param action A lambda to configure the `main` Docker image, applied to the instance of [DockerImage].
     * @return A [NamedDomainObjectProvider]<[DockerImage]> representing the `main` Docker image.
     */
    public fun main(action: DockerImage.() -> Unit): NamedDomainObjectProvider<DockerImage> = named("main", action)
}
