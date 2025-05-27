import java.time.LocalDate

plugins {
    kotlin("jvm") version "2.1.21"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jetbrains.dokka") version "2.0.0"
}

allprojects {
    group = "dev.akif"
    version = "0.2.0"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    dokka {
        moduleName = "${rootProject.name}-${project.name}"
        dokkaPublications.html {
            failOnWarning = true
            suppressObviousFunctions = true
        }
        pluginsConfiguration.html {
            footerMessage = "&#169; ${LocalDate.now().year} Mehmet Akif Tütüncü"
            separateInheritedMembers = true
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
