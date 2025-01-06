package com.github.lamba92.gradle.docker

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.assign

class RegistriesContainer(
    private val delegate: NamedDomainObjectContainer<DockerRegistry>
) : NamedDomainObjectContainer<DockerRegistry> by delegate {

    companion object {
        const val DOCKER_HUB = "dockerHub"
        const val AMAZON_ECR = "amazonEcr"
        const val GITHUB_CONTAINER_REGISTRY = "ghcr"
        const val GOOGLE_ARTIFACT_REGISTRY = "googleArtifactRegistry"
    }

    fun dockerHub(username: String) {
        if (DOCKER_HUB !in names) register(DOCKER_HUB)

        named(DOCKER_HUB) {
            url = "https://index.docker.io/v1/"
            imageTagPrefix = "$username/"
        }
    }

    fun amazonEcr(accountId: String, region: String) {
        if (AMAZON_ECR !in names) register(AMAZON_ECR)

        named(AMAZON_ECR) {
            url = "$accountId.dkr.ecr.$region.amazonaws.com"
            imageTagPrefix = "$accountId/"
        }
    }

    fun githubContainerRegistry(username: String) {
        if (GITHUB_CONTAINER_REGISTRY !in names) register(GITHUB_CONTAINER_REGISTRY)

        named(GITHUB_CONTAINER_REGISTRY) {
            url = "https://ghcr.io"
            imageTagPrefix = "$username/"
        }
    }

    fun googleArtifactRegistry(
        projectId: String,
        location: String,
        repositoryId: String,
    ) {
        if (GOOGLE_ARTIFACT_REGISTRY !in names) register(GOOGLE_ARTIFACT_REGISTRY)

        named(GOOGLE_ARTIFACT_REGISTRY) {
            url = "$location-docker.pkg.dev"
            imageTagPrefix = "$projectId/$repositoryId/"
        }
    }
}



