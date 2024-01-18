import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.dokka") version "1.9.10"
}

allprojects {
    group = "dev.akif"
    version = "0.0.1"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            failOnWarning.set(true)
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
            skipDeprecated.set(false)
            suppressGeneratedFiles.set(true)
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

repositories {
    mavenCentral()
}
