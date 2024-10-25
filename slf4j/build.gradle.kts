import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import java.net.URI
import java.time.LocalDate

plugins {
    kotlin("jvm") version "1.9.22"
    `java-library`
    `maven-publish`
    idea
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.10")
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = JvmTarget.JVM_17.target
        languageVersion = KotlinVersion.KOTLIN_1_9.version
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events = setOf(
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.FAILED
        )
        exceptionFormat = TestExceptionFormat.SHORT
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
}

tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks.dokkaHtmlPartial.configure {
    dokkaSourceSets {
        configureEach {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URI("https://github.com/makiftutuncu/periodik/blob/main/slf4j/src/main/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "&#169; ${LocalDate.now().year} Mehmet Akif Tütüncü"
        separateInheritedMembers = true
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "${rootProject.name}-${project.name}"
            from(components["java"])
            artifact(tasks["dokkaHtmlJar"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(project.name)
                description.set("Periodik is a read-only property delegate that can provide a value updated periodically")
                url.set("https://github.com/makiftutuncu/periodik")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("makiftutuncu")
                        name.set("Mehmet Akif Tütüncü")
                        email.set("m.akif.tutuncu@gmail.com")
                        url.set("https://akif.dev")
                    }
                }
                scm {
                    url.set("https://github.com/makiftutuncu/periodik")
                }
            }
        }
    }
}


signing {
    setRequired {
        !project.version.toString().endsWith("-SNAPSHOT")
                && gradle.taskGraph.allTasks.any { it is PublishToMavenRepository }
    }
    val signingKey = properties["signingKey"] as String?
    val signingPassword = properties["signingPassword"] as String?
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}
