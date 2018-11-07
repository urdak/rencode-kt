import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.ickis"
version = "1.0-SNAPSHOT"

plugins {
    `maven-publish`
    kotlin("jvm") version "1.3.0"
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java) {
            from(components["java"])
        }
    }
}

dependencies {
    compile(kotlin("stdlib"))
    testCompile("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
