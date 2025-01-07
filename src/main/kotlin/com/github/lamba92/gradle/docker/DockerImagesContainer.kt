package com.github.lamba92.gradle.docker

import org.gradle.api.NamedDomainObjectContainer

class DockerImagesContainer(
    private val delegate: NamedDomainObjectContainer<DockerImage>,
) : NamedDomainObjectContainer<DockerImage> by delegate {
    fun main(action: DockerImage.() -> Unit) = named("main", action)
}
