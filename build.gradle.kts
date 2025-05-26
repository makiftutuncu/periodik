plugins {
    kotlin("jvm") version "2.1.21"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.dokka") version "1.9.10"
}

allprojects {
    group = "dev.akif"
    version = "0.1.0"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    tasks.dokkaHtml.configure {
        dokkaSourceSets.configureEach {
            failOnWarning.set(true)
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
            skipDeprecated.set(false)
            suppressGeneratedFiles.set(true)
            includes.from("Module.md")
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
