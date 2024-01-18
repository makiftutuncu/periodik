# Periodik

Periodik is a read-only property delegate that can provide a value refreshed periodically. It is inspired by [periodic](https://github.com/dvgica/periodic).

| Latest Version | Java Version | Kotlin Version |
|----------------| ------------ |----------------|
| 0.0.1          | 17           | 1.9.21         |

## Table of Contents

1. [Modules](#modules)
2. [Examples](#examples)
3. [Development & Testing](#development--testing)
4. [Releases](#releases)
5. [Contributing](#contributing)
6. [License](#license)

## Modules

This project consists of following modules. You can click on a module for more information and detailed instructions.

| Name                     | Details             | Documentation                                                                                                                                                                 |
|--------------------------|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [core](core/README.md)   | Core implementation | [![](https://img.shields.io/badge/docs-0.0.1-brightgreen.svg?style=for-the-badge&logo=kotlin&color=0095d5&labelColor=333333)](https://javadoc.io/doc/dev.akif/periodik-core)  |
| [slf4j](slf4j/README.md) | SLF4J integration   | [![](https://img.shields.io/badge/docs-0.0.1-brightgreen.svg?style=for-the-badge&logo=kotlin&color=0095d5&labelColor=333333)](https://javadoc.io/doc/dev.akif/periodik-slf4j) |

## Examples

There is an example project on https://github.com/makiftutuncu/periodik-example.

## Development & Testing

This project is written in Kotlin and built with Gradle. Standard Gradle tasks like `clean`, `compileKotlin`, `compileTestKotlin` and `test` can be used during development and testing.

If you don't have Gradle installed, you can replace `gradle` commands with `./gradlew` to use Gradle wrapper.

To test your changes during development:

1. Bump your version in [build.gradle.kts](build.gradle.kts#L9) and append `-SNAPSHOT`.
2. Run `gradle publishToMavenLocal` to publish artifacts with your changes to your local Maven repository.
3. In the project you use spring-boot-crud, update the version of spring-boot-crud dependencies to your new snapshot version. Make sure you have `mavenLocal()` in your `repositories` in your build definition for this to work.

## Releases

Artifacts of this project are published to Maven Central along with their sources and documentations. They are versioned according to [semantic versioning](https://semver.org). CI/CD is managed by GitHub Actions. See [.github](.github) for more details on these workflows.

## Contributing

All contributions are welcome, including requests to highlight your project using this library. Please feel free to send a pull request. Thank you.

## License

This project is licensed with MIT License. See [LICENSE](LICENSE) for more details.
