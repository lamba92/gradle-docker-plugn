package com.github.lamba92.gradle.docker

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware

abstract class DockerExtension(
    private val name: String,
    val images: NamedDomainObjectContainer<DockerImage>,
    val registries: RegistriesContainer
) : ExtensionAware, Named {
    override fun getName() = name
}

operator fun RegistriesContainer.invoke(action: RegistriesContainer.() -> Unit) =
    apply(action)

