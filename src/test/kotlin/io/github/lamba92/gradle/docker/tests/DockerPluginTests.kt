@file:Suppress("UnstableApiUsage")

package io.github.lamba92.gradle.docker.tests

val testProjectPath
    get() =
        System.getenv("TEST_PROJECT_PATH")
            ?: error("Test project path not provided")