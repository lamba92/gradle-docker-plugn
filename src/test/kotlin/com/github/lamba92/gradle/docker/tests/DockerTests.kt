package com.github.lamba92.gradle.docker.tests

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class DockerTests {
    @Test
    fun dockerBuild() {
        GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(testProjectPath))
            .forwardOutput()
            .withArguments(":dockerBuild")
            .build()
    }

    @Test
    fun dockerPush() {
        GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(testProjectPath))
            .forwardOutput()
            .withArguments(":dockerPush")
            .build()
    }

    @Test
    fun dockerRun() {
        val result =
            GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(File(testProjectPath))
                .forwardOutput()
                .withArguments(":dockerRun")
                .build()
        assert("Hello Kotlin!" in result.output)
    }
}
