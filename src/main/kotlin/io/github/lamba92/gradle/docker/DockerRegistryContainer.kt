@file:Suppress("ktlint:standard:no-unused-imports")

package io.github.lamba92.gradle.docker

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.assign

/**
 * Container for managing [DockerRegistry] objects in a Gradle project.
 * This class provides predefined support for common Docker registries such as Docker Hub, Amazon ECR,
 * GitHub Container Registry, and Google Artifact Registry, while also allowing advanced customization.
 */
public class DockerRegistryContainer(
    private val delegate: NamedDomainObjectContainer<DockerRegistry>,
) : NamedDomainObjectContainer<DockerRegistry> by delegate {
    public companion object {
        public const val DOCKER_HUB: String = "dockerHub"
        public const val AMAZON_ECR: String = "amazonEcr"
        public const val GITHUB_CONTAINER_REGISTRY: String = "ghcr"
        public const val GOOGLE_ARTIFACT_REGISTRY: String = "googleArtifactRegistry"
    }

    /**
     * Configures a Docker registry for Docker Hub and assigns the specified username as the image tag prefix.
     *
     * @param dockerHubUsername The username associated with the Docker Hub account, used as the image tag prefix.
     */
    public fun dockerHub(dockerHubUsername: String) {
        if (DOCKER_HUB !in names) register(DOCKER_HUB)

        named(DOCKER_HUB) {
            imageTagPrefix = dockerHubUsername
        }
    }

    /**
     * Configures an Amazon Elastic Container Registry (ECR) Docker registry based on
     * the provided AWS account ID and region. The image tag prefix is derived from
     * these parameters.
     *
     * @param accountId The AWS account ID used to define the ECR registry.
     * @param region The AWS region where the ECR registry is located.
     */
    public fun amazonEcr(
        accountId: String,
        region: String,
    ) {
        if (AMAZON_ECR !in names) register(AMAZON_ECR)

        named(AMAZON_ECR) {
            imageTagPrefix = "$accountId.dkr.ecr.$region.amazonaws.com"
        }
    }

    /**
     * Configures a GitHub Container Registry for use with Docker.
     * The image tag prefix is automatically set based on the provided GitHub username.
     *
     * @param githubUsername The GitHub username used to define the registry's image tag prefix.
     */
    public fun githubContainerRegistry(githubUsername: String) {
        if (GITHUB_CONTAINER_REGISTRY !in names) register(GITHUB_CONTAINER_REGISTRY)

        named(GITHUB_CONTAINER_REGISTRY) {
            imageTagPrefix = "ghcr.io/$githubUsername"
        }
    }

    /**
     * Configures a GitHub Container Registry for use with Docker.
     * The image tag prefix is automatically set based on the provided GitHub username.
     *
     * @param githubUsername The GitHub username used to define the registry's image tag prefix.
     */
    public fun ghcr(githubUsername: String): Unit = githubContainerRegistry(githubUsername)

    /**
     * Configures a Google Artifact Registry as a Docker registry.
     * Sets the image tag prefix based on the specified project ID, region, and registry name.
     *
     * @param projectId The Google Cloud project ID used for the artifact registry.
     * @param region The Google Cloud region where the artifact registry is located.
     * @param registryName The name of the Google Artifact Registry.
     */
    public fun googleArtifactRegistry(
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
