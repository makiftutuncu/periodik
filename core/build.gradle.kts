import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version "2.1.21"
    `java-library`
    `maven-publish`
    idea
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.21") {
        exclude("org.junit.jupiter")
    }
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
        jvmTarget.set(JvmTarget.JVM_21)
        languageVersion.set(KotlinVersion.fromVersion(KotlinVersion.KOTLIN_2_1.version))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events(
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
    val dokkaTask = project(":docs").tasks.named("dokkaGeneratePublicationHtml")
    dependsOn(dokkaTask)
    from(dokkaTask.map { it.outputs.files })
    archiveClassifier.set("javadoc")
}

dokka {
    dokkaSourceSets.main {
        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/makiftutuncu/periodik/blob/main/core/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }
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
