@file:Suppress("UnstableApiUsage")

package com.github.lamba92.gradle.docker.tests

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class DockerPluginTests {

    @Test
    fun run() {
        val testProjectPath = System.getenv("TEST_PROJECT_PATH")
            ?: error("Test project path not provided")
        GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(testProjectPath))
            .forwardOutput()
            .withArguments(":dockerBuild")
            .build()
    }
}