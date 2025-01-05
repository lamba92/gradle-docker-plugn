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
    website = ""
    vcsUrl = ""
    plugins {
        create("dockerPlugin") {
            id = "gradle-docker-plugin"
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
    }
}
