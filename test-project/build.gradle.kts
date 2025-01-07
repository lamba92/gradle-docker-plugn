@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version "2.0.21"
    application
    id("io.github.lamba92.docker")
}

group = "com.github.lamba92"
version = "1.0"

kotlin {
    jvmToolchain(8)
}

application {
    mainClass = "io.github.lamba92.gradle.docker.tests.MainKt"
}

docker {
    registries {
        System.getenv("REPOSITORY_OWNER")
            ?.let { githubContainerRegistry(it) }
    }
}
