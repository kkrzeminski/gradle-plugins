package pl.ivmx.gradle.plugins.duplicatefinder

import java.util.zip.ZipEntry

class ClassInsideJar {
    String name;
    String hashcode;//TODO DM add feature: compare classes by hascode
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
