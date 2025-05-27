plugins {
    kotlin("jvm") version "2.1.21"
    idea
}

repositories {
    mavenCentral()
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

dependencies {
    dokka(project(":core"))
    dokka(project(":slf4j"))
}

dokka {
    dokkaSourceSets.main {
        enableJdkDocumentationLink = true
        enableKotlinStdLibDocumentationLink = true
        reportUndocumented = true
        skipDeprecated = false
        skipEmptyPackages = true
        suppressGeneratedFiles = true
    }
}
