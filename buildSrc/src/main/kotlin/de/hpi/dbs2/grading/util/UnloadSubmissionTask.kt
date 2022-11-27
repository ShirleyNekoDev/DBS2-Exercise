package de.hpi.dbs2.grading.util

import de.hpi.dbs2.grading.AbstractGradingTask
import de.hpi.dbs2.submitting.PackSubmissionTask
import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.nio.file.StandardCopyOption
import java.io.File
import java.nio.file.Files

abstract class UnloadSubmissionTask : AbstractGradingTask() {
    @TaskAction
    fun unloadSubmission() = Companion.unloadSubmission(
        logger = logger,
        projectDir = project.projectDir,
        exerciseId = _exerciseId
    )

    companion object {
        fun unloadSubmission(
            logger: Logger,
            projectDir: File,
            exerciseId: Int,
        ) {
            val sourceDir = projectDir.resolve("src/main/")

            logger.quiet("Unloading current submission")
            PackSubmissionTask.JVM_LANGUAGE_SRC_DIRS.forEach { impl ->
                val targetDir = sourceDir.resolve(impl).resolve("exercise$exerciseId")

                targetDir.listFiles()?.forEach { file ->
                    val originalFile = file.resolveSibling(file.nameWithoutExtension)
                    when (file.extension) {
                        "backup" -> {
                            Files.move(
                                file.toPath(),
                                originalFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING
                            )
                            logger.info("> Restored file $originalFile")
                        }

                        "delete" -> {
                            Files.delete(file.toPath())
                            Files.deleteIfExists(originalFile.toPath())
                            logger.info("> Removed file $originalFile")
                        }
                    }
                }
            }
            logger.quiet("Submission unloaded")
        }
    }
}
