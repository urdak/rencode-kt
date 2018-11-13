import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.jetbrains.dokka.gradle.DokkaTask
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
    id("org.jetbrains.dokka") version "0.9.17"
}

repositories {
    mavenCentral()
}

group = "net.ickis"
version = "1.0"

val sourcesJar = task<Jar>("sourcesJar") {
    dependsOn(tasks["classes"])
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

tasks.withType<DokkaTask> {
    reportUndocumented = false
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

val javadocJar = task<Jar>("javadocJar") {
    dependsOn("dokka")
    classifier = "javadoc"
    from(buildDir.resolve("javadoc"))
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            groupId = "net.ickis"
            artifactId = project.name
            version = project.version.toString()
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
        name = project.name
        setLicenses("MIT")
        version(closureOf<VersionConfig> {
            name = project.version.toString()
            released = Date().toString()
        })
        setPublications("mavenJava")
        vcsUrl = "https://github.com/urdak/rencode-kt"
        publish = true
    })
}

dependencies {
    implementation(kotlin("stdlib"))
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
