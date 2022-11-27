package de.hpi.dbs2.submitting

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import de.hpi.dbs2.ExerciseSpecificTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskContainer
import java.nio.file.*

abstract class PackSubmissionTask : ExerciseSpecificTask() {
    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val targetDirectory: DirectoryProperty

    @get:Input
    abstract val groupIdentifier: Property<String>

    @get:Internal
    val _groupId: String
        get() = groupIdentifier.orNull
            ?.takeUnless { it.isBlank() }
            ?: error("Please set your group identifier in the gradle.properties file")

    companion object {
        fun registerExtensionTasks(taskContainer: TaskContainer) {
            REGISTERED_EXERCISES.forEach { id ->
                taskContainer.register("packExercise$id", PackSubmissionTask::class.java) {
                    it.exerciseId.set(id)
                }
            }
        }

        val JVM_LANGUAGE_SRC_DIRS = listOf("kotlin", "java", "groovy", "scala")
    }

    init {
        group = "submission"
        targetDirectory.set(project.file("submissions/"))
        groupIdentifier.set(project.property("groupIdentifier") as String)
        sourceDirectory.set(project.file("src/main/"))
    }

    @TaskAction
    fun pack() {
        logger.quiet("Packing submission for exercise $_exerciseId using groupIdentifier=$_groupId")
        val submissionFile = targetDirectory.get()
            .file("group$_groupId-exercise$_exerciseId.zip").asFile

        Files.deleteIfExists(submissionFile.toPath())
        FileSystems.newFileSystem(submissionFile.toPath(), mapOf("create" to "true")).use { zipFS ->
            Files.writeString(
                zipFS.getPath("groupId.txt"),
                _groupId
            )

            JVM_LANGUAGE_SRC_DIRS.forEach { language ->
                val sourcePath = sourceDirectory.get()
                    .dir(language)
                    .dir("exercise$_exerciseId")
                    .asFile.toPath()
                if (Files.isDirectory(sourcePath)) {
                    val targetPath = zipFS.getPath(language)
                    Files.walk(sourcePath).forEach { source ->
                        val relativePath = sourcePath.relativize(source).toString()
                        val target = targetPath.resolve(relativePath)

                        Files.copy(source, target)

                        if (Files.isRegularFile(source)) {
                            logger.debug("$source -> $submissionFile:/$target")
                            val isChosenImpl = Files.lines(source)
                                .anyMatch { line ->
                                    line.trimStart().startsWith("@ChosenImplementation")
                                        && line.contains("true")
                                }
                            if (isChosenImpl) {
                                Files.writeString(
                                    zipFS.getPath("chosenImplementation.txt"),
                                    target.toString()
                                )
                            }
                        }
                    }
                }
            }
        }

        logger.quiet(
            """
            Submission has been written to: $submissionFile
            Please upload this file to moodle without renaming it!
        """.trimIndent()
        )
    }
}
