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
