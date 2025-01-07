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

    fun dockerHub(username: String) {
        if (DOCKER_HUB !in names) register(DOCKER_HUB)

        named(DOCKER_HUB) {
            imageTagPrefix = username
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

    fun githubContainerRegistry(username: String) {
        if (GITHUB_CONTAINER_REGISTRY !in names) register(GITHUB_CONTAINER_REGISTRY)

        named(GITHUB_CONTAINER_REGISTRY) {
            imageTagPrefix = "ghcr.io/$username"
        }
    }

    fun googleArtifactRegistry(
        projectId: String,
        region: String,
        repositoryName: String,
    ) {
        if (GOOGLE_ARTIFACT_REGISTRY !in names) register(GOOGLE_ARTIFACT_REGISTRY)

        named(GOOGLE_ARTIFACT_REGISTRY) {
            imageTagPrefix = "$region-docker.pkg.dev/$projectId/$repositoryName"
        }
    }
}
