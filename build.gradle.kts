import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import de.hpi.dbs2.submitting.PackSubmissionTask
import de.hpi.dbs2.grading.*
import de.hpi.dbs2.grading.util.*

plugins {
    kotlin("jvm") version "1.9.20"
    id("java")

    id("com.github.ben-manes.versions") version "0.49.0"
    id("org.jetbrains.dokka") version "1.9.10"
    idea
}

group = "de.hpi.dbs2"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.10")

    implementation("com.github.ajalt.clikt:clikt:4.2.1")

    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("com.google.guava:guava:32.1.3-jre")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    withType<JavaExec> {
        enableAssertions = true
    }
    test {
        enableAssertions = true
        testLogging {
            showStandardStreams = true
        }
        useJUnitPlatform()
    }
    withType<KotlinJvmCompile> {
        kotlinOptions {
            apiVersion = "1.7"
            languageVersion = "1.7"
            freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }

    register<PackSubmissionTask>("packSubmission")
    PackSubmissionTask.registerExtensionTasks(this)

    register<UnpackSubmissionsTask>("unpackSubmissions")
    register<LoadSubmissionTask>("loadSubmission")
    register<UnloadSubmissionTask>("unloadSubmission")
    register<GenerateReportTask>("createReport")

    withType<DependencyUpdatesTask> {
        val unstable = Regex("^.*?(?:alpha|beta|unstable|ea).*\$", RegexOption.IGNORE_CASE)
        rejectVersionIf {
            candidate.version.matches(unstable)
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
