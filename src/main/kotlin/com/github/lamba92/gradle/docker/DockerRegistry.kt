package com.github.lamba92.gradle.docker

import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

class DockerRegistry(
    val registryName: String,
    objectFactory: ObjectFactory
) : Named {
    val imageTagPrefix = objectFactory.property<String>()
    val url = objectFactory.property<String>()

    override fun getName() = registryName
}