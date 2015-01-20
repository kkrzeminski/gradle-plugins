# gradle-plugins
The gradle-plugins project brings some tooling for Gradle build.
## Usage
To use it, include the required JARs via `buildscript {}` and apply the plugin:

``` groovy
buildscript {
    repositories { maven { url "http://repository_address" } }
    dependencies {
        classpath 'pl.ivmx.gradle.plugins:gradle-plugins:0.4'
    }
}
apply plugin: 'duplicate-finder'
```

``` groovy
duplicateFinder {
    configs = [configurations.runtime, configurations.compile]
}
```

It checks for duplicated classes in runtime and compile configurations.

## Building
To build plugin, simply execute gradle task: `build`.

If you wish to deploy it to local repository use `publish` task.