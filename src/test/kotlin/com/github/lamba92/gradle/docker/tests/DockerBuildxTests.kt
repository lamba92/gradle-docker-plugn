package com.github.lamba92.gradle.docker.tests

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

class DockerBuildxTests {

    @Test
    fun runDockerBuildxBuild() {
        GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(testProjectPath))
            .forwardOutput()
            .withArguments(":dockerBuildxBuild")
            .build()
    }

    @Test
    fun runDockerBuildxPush() {
        GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(testProjectPath))
            .forwardOutput()
            .withArguments(":dockerBuildxPush")
            .build()
    }
}