@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish.plugin)
}

group = "com.github.lamba92"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(8)
}

gradlePlugin {
    website = "https://github.com/lamba92/gradle-docker-plugn"
    vcsUrl = "https://github.com/lamba92/gradle-docker-plugn"
    plugins {
        create("dockerPlugin") {
            id = "gradle-docker-plugin"
            displayName = "Gradle Docker Plugin"
            implementationClass = "com.github.lamba92.gradle.docker.DockerPlugin"
            tags = listOf("docker", "ci/cd", "container", "jvm")
            description = "Integrate seamlessly Docker in your build."
        }
    }
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        environment(
            "TEST_PROJECT_PATH",
            rootProject.layout.projectDirectory.dir("test-project").asFile.absolutePath
        )
    }
}
