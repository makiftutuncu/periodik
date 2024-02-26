# periodik-core

This is the core module of periodik. It is a read-only property delegate that can provide a value updated periodically.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Building a Periodik Property](#building-a-periodik-property)
3. [Customizations](#customizations)

## Getting Started

To get started, add `periodik-core` as a dependency to your existing project.

For Gradle with Kotlin DSL, add following to `build.gradle.kts`:

```kotlin
dependencies {
  implementation('dev.akif:periodik-core:0.1.0')
}
```
For Gradle, add following to `build.gradle`:

```kotlin
dependencies {
  implementation 'dev.akif:periodik-core:0.1.0'
}
```
For Maven, add following to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.akif</groupId>
    <artifactId>periodik-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Building a Periodik Property

In order to build a `Periodik` for a property of type `A`, you should build and instance of `Periodik<A>` object and delegate your property to it.

`periodik-core` provides a builder DSL as [PeriodikBuilder](src/main/kotlin/dev/akif/periodik/PeriodikBuilder.kt) so that these `Periodik` objects can be created inline and in an easy way. This is made available via [periodik](src/main/kotlin/dev/akif/periodik.kt) function. To get the final `Periodik` object, a [Schedule](src/main/kotlin/dev/akif/periodik/Schedule.kt) needs to be provided and the builder needs to be **built**.

Here's a simple example:

```kotlin
import dev.akif.periodik
import dev.akif.periodik.Schedule
import kotlin.time.Duration.Companion.seconds

class MyClass {
    val property by periodik<String>(Schedule.every(2.seconds)).build {
        // `currentInstant`, `log` and other values in the `Periodik` are accessible!
        val time = "Time: ${currentInstant()}!"
        log(time)
        time
    }
}
```

This will create an eagerly initialized property, whose value will be refreshed every 2 seconds.

periodik supports Kotlin's coroutines. You can use `buildSuspending` instead of `build` if your function to get the data is a suspending function.

## Customizations

There are many aspects of a periodik property. You can customize the behavior by following methods before your final build step. To see all available customization methods, please check out the [docs](https://javadoc.io/doc/dev.akif/periodik-core/latest/dev.akif.periodik/-periodik-builder/index.html).
