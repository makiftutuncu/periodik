# periodik-slf4j

This is the SLF4J integration module of Periodik to help configure logging of `Periodik` to use an `org.slf4j.Logger`.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Setting a Logger](#setting-a-logger)

## Getting Started

To get started, add periodik-slf4j as a dependency to your existing project.

For Gradle with Kotlin DSL, add following to `build.gradle.kts`:

```kotlin
dependencies {
  implementation('dev.akif:periodik-slf4j:0.0.2')
}
```
For Gradle, add following to `build.gradle`:

```kotlin
dependencies {
  implementation 'dev.akif:periodik-slf4j:0.0.2'
}
```
For Maven, add following to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.akif</groupId>
    <artifactId>periodik-slf4j</artifactId>
    <version>0.0.2</version>
</dependency>
```

## Setting a Logger

In order to make a periodik property to use an SLF4J logger, you can use [loggingWithSlf4j](src/main/kotlin/dev/akif/periodik/periodikslf4j.kt) extension before building. Here's the same example from periodik-core documents demonstrating this.

```kotlin
import dev.akif.periodik
import dev.akif.periodik.loggingWithSlf4j
import dev.akif.periodik.Schedule
import kotlin.time.Duration.Companion.seconds

class MyClass {
    val property: String by periodik()
        .on(Schedule.every(2.seconds))
        .loggingWithSlf4j()
        .build {
            "Time: ${System.currentTimeMillis()}!"
        }
}
```

This will use a `dev.akif.periodik.Periodik` logger by default. It will log the debug messages at `DEBUG` level, regular log messages at `INFO` level and error messages at `ERROR` level on this logger.

It is also possible provide a different logger by passing an `org.slf4j.Logger` object to `loggingWithSlf4j` method.
