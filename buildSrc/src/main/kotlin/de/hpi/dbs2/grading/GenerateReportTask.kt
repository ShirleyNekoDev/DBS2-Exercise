package de.hpi.dbs2.grading

import de.hpi.dbs2.grading.util.LoadSubmissionTask
import de.hpi.dbs2.grading.util.UnloadSubmissionTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.xml.parsers.DocumentBuilderFactory

abstract class GenerateReportTask : AbstractGradingTask() {
    @TaskAction
    fun run() {
        val cmdExecutor = Executors.newSingleThreadExecutor()

        val submissionsDir = project.projectDir.resolve("submissions/")
        val exerciseDir = submissionsDir.resolve("exercise$_exerciseId/")
            .also { require(it.isDirectory) }

        val projectInUnsafeState =
            cmdExecutor.runCmd("git ls-files -om --exclude-standard").any {
                it != "gradle.properties"
                    && !it.startsWith("buildSrc")
                    && !it.startsWith("src/test/")
            }
        check(!projectInUnsafeState) { "please git stash your current changes before continuing" }

        val groupDirs = exerciseDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("group")
        }!!

        logger.quiet("Validating submissions")
        val reportFile = exerciseDir.resolve("report.csv")
        PrintWriter(reportFile).use {
            it.println("group, chosenImplementation, executedTests, failedTests, skippedTests, note")

            groupDirs.forEach { groupDir ->
                val groupId = groupDir.name.substring(5)
                withLoadedSubmission(groupId) { chosenImpl ->
                    it.print("$groupId, $chosenImpl, ")
                    validateSubmission(it, cmdExecutor)
                    it.println(", ")
                }
            }
        }
        logger.quiet("Report written to $reportFile")

        cmdExecutor.shutdownNow()
    }

    fun validateSubmission(writer: PrintWriter, cmdExecutor: ExecutorService) {
        cmdExecutor.runCmd(
            "./gradlew.bat test --tests \"exercise$_exerciseId.*\" --build-cache -q",
            ignoreExitCode = true
        )

        val testReportFiles = project.projectDir.resolve("build/test-results/test/")
            .listFiles { file ->
                file.isFile
                && file.name.startsWith("TEST-exercise$_exerciseId")
                && file.extension == "xml"
            }!!
            .distinct()

        parseTestReports(testReportFiles).apply {
            writer.print("$executedTests, $failedTests, $skippedTests")
        }

        testReportFiles.forEach {
            Files.deleteIfExists(it.toPath())
        }
    }

    fun parseTestReports(testReportFiles: Collection<File>): TestResults {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        var executedTests = 0
        var failedTests = 0
        var skippedTests = 0

        logger.info("> Parsing test reports")
        testReportFiles.forEach { testReport ->
            val xmlDocument = builder.parse(testReport)
            xmlDocument.getElementsByTagName("testsuite").forEach { testSuite ->
                executedTests += testSuite.getAttribute("tests").toInt()
                failedTests += testSuite.getAttribute("failures").toInt()
                failedTests += testSuite.getAttribute("errors").toInt()
                skippedTests += testSuite.getAttribute("skipped").toInt()
            }
        }
        return TestResults(executedTests, failedTests, skippedTests)
    }

    data class TestResults(
        val executedTests: Int,
        val failedTests: Int,
        val skippedTests: Int,
    )

    fun NodeList.forEach(callback: (Node) -> Unit) {
        (0 until length).forEach { i ->
            callback(item(i))
        }
    }

    fun Node.getAttribute(attribute: String): String =
        attributes.getNamedItem(attribute)?.nodeValue
            ?: error("attribute \"$attribute\" not found")


    fun withLoadedSubmission(
        groupId: String,
        block: (implLang: String?) -> Unit
    ) {
        logger.quiet("Processing submission of group $groupId")

        var implLang: String? = LoadSubmissionTask.loadSubmission(
            logger = logger,
            projectDir = project.projectDir,
            exerciseId = _exerciseId,
            groupId = groupId,
        )

        block(implLang)

        UnloadSubmissionTask.unloadSubmission(
            logger = logger,
            projectDir = project.projectDir,
            exerciseId = _exerciseId,
        )

    }

    fun ExecutorService.runCmd(
        command: String,
        ignoreExitCode: Boolean = false
    ): List<String> {
        logger.info("> Running \"$command\"")
        val process = Runtime.getRuntime().exec(command)
        val processOutput = process.inputReader()
        val output = submit<List<String>> {
            buildList {
                processOutput.use {
                    it.lines().forEach(::add)
                }
            }
        }
        process.waitFor().also { exitCode ->
            if (!ignoreExitCode) {
                require(exitCode == 0) { "execution failed $exitCode" }
            }
        }
        return output.get()
    }
}
