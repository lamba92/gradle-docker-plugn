package com.github.lamba92.gradle.docker

import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class CreateJvmDockerfile @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @get:OutputFile
    val destinationFile = objects.fileProperty()

    @get:Input
    val imageName = objects.property<String>()

    @get:Input
    val imageTag = objects.property<String>()

    @get:Input
    val appName = objects.property<String>()

    @TaskAction
    fun writeFile() {
        destinationFile.get()
            .asFile
            .writeText(
                jvmAppDockerImageString(
                    imageName = imageName.get(),
                    imageTag = imageTag.get(),
                    appName = appName.get()
                )
            )
    }
}