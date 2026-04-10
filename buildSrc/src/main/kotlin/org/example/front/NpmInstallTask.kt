package org.example.front

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

@CacheableTask
abstract class NpmInstallTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val packageFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val nodeModulesDir: DirectoryProperty

    @get:OutputFile
    abstract val marker: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val workingDir: DirectoryProperty

    init {
        group = "build"
        description = "Install npm dependencies"
    }

    @TaskAction
    fun install() {
        execOperations.exec {
            workingDir = this@NpmInstallTask.workingDir.get().asFile
            commandLine("npm", "ci")
        }
        marker.get().asFile.apply {
            parentFile.mkdirs()
            writeText("installed")
        }
    }
}
