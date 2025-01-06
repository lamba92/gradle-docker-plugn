@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version "2.0.21"
    application
    id("gradle-docker-plugin")
}

group = "com.github.lamba92"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(8)
}

application {
    mainClass = "com.github.lamba92.gradle.docker.tests.MainKt"
}
