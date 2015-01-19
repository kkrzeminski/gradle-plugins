package pl.ivmx.gradle.plugins.duplicatefinder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class DuplicateFinderPlugin implements Plugin<Project> {
    class ClassInsideJar {
        String name;
        String hashcode;
        String filename;

        ClassInsideJar(ZipEntry zipEntry, String filename) {
            name = zipEntry.name.substring(0, zipEntry.name.length() - 6).replaceAll('/', '.')
            hashcode = zipEntry.hashCode()
            this.filename = filename
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            ClassInsideJar aClass = (ClassInsideJar) o

            if (name != aClass.name) return false

            return true
        }

        int hashCode() {
            return name.hashCode()
        }
    }

    void apply(Project project) {
        project.extensions.create("duplicateFinder", DuplicateFinderPluginExtension)

        def duplicateFinder = project.task('duplicateFinder') << { task ->
            def Map<String, List<String>> exeptions = project.duplicateFinder.exceptions
            def configurations = project.duplicateFinder.configs != null ? project.duplicateFinder.configs : project.configurations;
            configurations.each { configuration ->
                def duplicates = configuration
                        .collect { findClassesInJar(it) }
                        .flatten()
                        .groupBy { it.name }
                        .findAll { it.value.size() > 1 }
                        .findAll { !(exeptions.containsKey(it.key) && exeptions.get(it.key).containsAll(it.value.filename)) }
                        .sort { it.key }
                if (duplicates.size() > 0) {
                    throw new TaskExecutionException(task, new Exception(configuration.name + " [" + duplicates.inject([]) { res, en -> res << "'$en.key' : ['" + en.value.filename.join("', '") + "']" }.join(",\n") + "]"))
                }
                println "configuration: " + configuration.name + " is OK"
            }
        }
        duplicateFinder.description = 'Find class duplicates in given configurations'
        duplicateFinder.group = 'Verification'
    }

    def findClassesInJar(File jar) {
        new ZipFile(jar).entries()
                .findAll { it.name.endsWith('.class') }
                .collect { new ClassInsideJar(it, jar.name) }
    }


}

