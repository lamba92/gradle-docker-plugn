@file:Suppress("ktlint:standard:no-unused-imports")

package io.github.lamba92.gradle.docker

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.assign

class DockerRegistriesContainer(
    private val delegate: NamedDomainObjectContainer<DockerRegistry>,
) : NamedDomainObjectContainer<DockerRegistry> by delegate {
    companion object {
        const val DOCKER_HUB = "dockerHub"
        const val AMAZON_ECR = "amazonEcr"
        const val GITHUB_CONTAINER_REGISTRY = "ghcr"
        const val GOOGLE_ARTIFACT_REGISTRY = "googleArtifactRegistry"
    }

    fun dockerHub(dockerHubUsername: String) {
        if (DOCKER_HUB !in names) register(DOCKER_HUB)

        named(DOCKER_HUB) {
            imageTagPrefix = dockerHubUsername
        }
    }

    fun amazonEcr(
        accountId: String,
        region: String,
    ) {
        if (AMAZON_ECR !in names) register(AMAZON_ECR)

        named(AMAZON_ECR) {
            imageTagPrefix = "$accountId.dkr.ecr.$region.amazonaws.com"
        }
    }

    fun githubContainerRegistry(githubUsername: String) {
        if (GITHUB_CONTAINER_REGISTRY !in names) register(GITHUB_CONTAINER_REGISTRY)

        named(GITHUB_CONTAINER_REGISTRY) {
            imageTagPrefix = "ghcr.io/$githubUsername"
        }
    }

    fun ghcr(githubUsername: String) = githubContainerRegistry(githubUsername)

    fun googleArtifactRegistry(
        projectId: String,
        region: String,
        registryName: String,
    ) {
        if (GOOGLE_ARTIFACT_REGISTRY !in names) register(GOOGLE_ARTIFACT_REGISTRY)

        named(GOOGLE_ARTIFACT_REGISTRY) {
            imageTagPrefix = "$region-docker.pkg.dev/$projectId/$registryName"
        }
    }
}
