package com.github.lamba92.gradle.docker.tests

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class DockerTests {
    @Test
    fun runDockerBuild() {
        GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(testProjectPath))
            .forwardOutput()
            .withArguments(":dockerBuild")
            .build()
    }

    @Test
    fun runDockerPush() {
        GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(testProjectPath))
            .forwardOutput()
            .withArguments(":dockerPush")
            .build()
    }
}
