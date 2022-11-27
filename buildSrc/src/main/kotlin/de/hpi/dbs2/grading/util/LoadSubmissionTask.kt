package de.hpi.dbs2.grading.util

import de.hpi.dbs2.grading.AbstractGradingTask
import de.hpi.dbs2.submitting.PackSubmissionTask
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.nio.file.Files

abstract class LoadSubmissionTask : AbstractGradingTask() {
    @get:Input
    abstract val groupId: Property<String>

    @Option(option = "groupId", description = "Group ID")
    fun setGroupIdOption(value: String) {
        groupId.set(value)
    }

    @get:Internal
    val _groupId: String
        get() = (groupId.orNull ?: error("Please provide the groupId via --groupId=x"))

    @TaskAction
    fun loadSubmission(): String? = Companion.loadSubmission(
        logger = logger,
        projectDir = project.projectDir,
        exerciseId = _exerciseId,
        groupId = _groupId,
    )

    companion object {
        fun loadSubmission(
            logger: Logger,
            projectDir: File,
            exerciseId: Int,
            groupId: String,
        ): String? {
            val sourceDir = projectDir.resolve("src/main/")

            val groupDir = projectDir
                .resolve("submissions/exercise$exerciseId/group$groupId/")
                .also { require(it.isDirectory) }

            var implLang: String? = groupDir.resolve("chosenImplementation.txt")
                .takeIf { it.exists() }
                ?.let { Files.readString(it.toPath()) }
            var anyValidSubmissionFileFound = false

            logger.quiet("Loading submission of group $groupId")
            PackSubmissionTask.JVM_LANGUAGE_SRC_DIRS.forEach { impl ->
                val implDir = groupDir.resolve(impl)
                if (!implDir.isDirectory) return@forEach

                val targetDir = sourceDir.resolve(impl).resolve("exercise$exerciseId")

                implDir.listFiles()?.forEach { implFile ->
                    val targetFile = targetDir.resolve(implFile.name)
                    logger.debug("> Copying implementation file $implFile -> $targetFile")
                    if (targetFile.exists()) {
                        val backupFile = targetDir.resolve("${targetFile.name}.backup")
                        Files.move(targetFile.toPath(), backupFile.toPath())

                        if (implLang == null) {
                            val isChosenImpl = Files.lines(implFile.toPath())
                                .anyMatch { line ->
                                    line.trimStart().startsWith("@ChosenImplementation")
                                        && line.contains("true")
                                }
                            if (isChosenImpl) {
                                implLang = impl
                            }
                        }
                        logger.info("> Replaced file $targetFile")
                    } else {
                        val tombstoneFile = targetDir.resolve("${targetFile.name}.delete")
                        Files.createFile(tombstoneFile.toPath())
                        logger.info("> Created file $targetFile")
                    }
                    Files.copy(implFile.toPath(), targetFile.toPath())
                    anyValidSubmissionFileFound = true
                }
            }
            if (!anyValidSubmissionFileFound) {
                logger.error("> Submission structure invalid")
            } else {
                logger.quiet("Submission of group $groupId loaded")
                logger.quiet("Implementation language: $implLang")
            }
            return implLang
        }
    }
}
