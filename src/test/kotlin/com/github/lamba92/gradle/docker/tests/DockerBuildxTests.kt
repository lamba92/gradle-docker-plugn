package com.github.lamba92.gradle.docker.tests

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class DockerBuildxTests {
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
