package com.github.lamba92.gradle.docker

import org.gradle.api.Named
import org.gradle.api.plugins.ExtensionAware

abstract class DockerExtension(
    private val name: String,
    val images: DockerImagesContainer,
    val registries: DockerRegistriesContainer,
) : ExtensionAware, Named {
    override fun getName() = name

    fun registries(action: DockerRegistriesContainer.() -> Unit) = registries.apply(action)

    fun images(action: DockerImagesContainer.() -> Unit) = images.apply(action)
}
