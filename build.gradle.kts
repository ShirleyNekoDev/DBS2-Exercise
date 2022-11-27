import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import de.hpi.dbs2.submitting.PackSubmissionTask
import de.hpi.dbs2.grading.*
import de.hpi.dbs2.grading.util.*

plugins {
    kotlin("jvm") version "1.7.20"
    id("java")

    id("com.github.ben-manes.versions") version "0.42.0"
    id("org.jetbrains.dokka") version "1.7.20"
    idea
}

group = "de.hpi.dbs2"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")

    implementation("com.github.ajalt.clikt:clikt:3.5.0")

    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("com.google.guava:guava:31.1-jre")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.5.3")
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
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
