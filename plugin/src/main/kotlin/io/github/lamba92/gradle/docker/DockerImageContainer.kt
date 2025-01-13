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
     * Provides access to the `main` Docker image configuration within the container.
     * The `main` Docker image is a predefined and always available image used as the primary or default
     * image in the context of this container. This property allows you to retrieve or further configure
     * the `main` Docker image.
     */
    public val main: NamedDomainObjectProvider<DockerImage>
        get() = named("main")
}
