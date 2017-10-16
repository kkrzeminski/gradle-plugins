package pl.ivmx.gradle.plugins.duplicatefinder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.tooling.model.Task

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class DuplicateFinderPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("duplicateFinder", DuplicateFinderPluginExtension)

        def duplicateFinder = project.task('duplicateFinder', {
            doLast { task ->
                Map<String, List<String>> exceptions = project.duplicateFinder.exceptions
                def configurations = project.duplicateFinder.configs != null ? project.duplicateFinder.configs : project.configurations
                configurations.findAll { !(it.name in ['apiElements']) }.each { configuration ->
                    def duplicates = configuration
                            .collect { findClassesInJar(it) }
                            .flatten()
                            .groupBy { it.name }
                            .findAll { it.value.size() > 1 }
                            .findAll { !(exceptions.containsKey(it.key) && exceptions.get(it.key).containsAll(it.value.filename)) }
                            .sort { it.key }
                    if (duplicates.size() > 0) {
                        def duplicatedFiles = duplicates.inject([]) { res, en -> res << "'$en.key' : ['" + en.value.filename.join("', '") + "']" }.join(",\n")
                        throw new TaskExecutionException(task, new Exception("$configuration.name: [$duplicatedFiles]".toString()))
                    }
                    println "configuration: " + configuration.name + " is OK"
                }
            }
        })
        duplicateFinder.description = 'Find class duplicates in given configurations'
        duplicateFinder.group = 'Verification'
    }

    static def findClassesInJar(File jar) {
        new ZipFile(jar).entries()
                .findAll { ZipEntry it -> it.name.endsWith('.class') }
                .collect { ZipEntry it -> new ClassInsideJar(it, jar.name) }
    }

}

