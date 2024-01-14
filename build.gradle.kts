plugins {
    kotlin("jvm") version "1.9.21"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.dokka") version "1.8.10"
}

allprojects {
    group = "dev.akif"
    version = "0.0.1-SNAPSHOT"

    apply(plugin = "org.jetbrains.dokka")

}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

repositories {
    mavenCentral()
}
