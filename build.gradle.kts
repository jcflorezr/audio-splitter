import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.60"
    war
}

allprojects {

    group = "net.jcflorezr"
    project.version = "0.2-SNAPSHOT"

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }

    repositories {
        jcenter()
        mavenCentral()
        maven(url = "http://dl.bintray.com/ijabz/maven")
    }
}

