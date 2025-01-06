@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "gradle-docker-plugin-test-project"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    rulesMode = RulesMode.FAIL_ON_PROJECT_RULES
}