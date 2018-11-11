import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.util.Date
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    `java-library` // prevents a bintray publishing bug
    `maven-publish`
    kotlin("jvm") version "1.3.0"
    id("com.jfrog.bintray") version "1.8.4"
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java) {
            from(components["java"])
            groupId = "net.ickis"
            artifactId = "rencode-kt"
            version = "1.0"
        }
    }
}

val credentials = Files.newInputStream(rootProject.buildFile.toPath().resolveSibling("local.properties")).use {
    val props = Properties()
    props.load(it)
    props
}

bintray {
    user = credentials["bintray.user"] as? String
    key = credentials["bintray.apikey"] as? String
    pkg(closureOf<PackageConfig> {
        repo = "maven"
        name = "rencode-kt"
        setLicenses("MIT")
        version(closureOf<VersionConfig> {
            name = "1.0"
            released = Date().toString()
        })
        setPublications("mavenJava")
        vcsUrl = "https://github.com/urdak/rencode-kt"
        publish = true
    })
}

dependencies {
    compile(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
