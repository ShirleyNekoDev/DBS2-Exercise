import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.7.20"
    idea
}

group = "de.hpi.dbs2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}

tasks {
    withType<KotlinJvmCompile> {
        kotlinOptions {
            apiVersion = "1.7"
            languageVersion = "1.7"
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
