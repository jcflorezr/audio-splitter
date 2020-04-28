import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val kotlinVersion: String by project
val jacksonVersion: String by project
val springVersion: String by project
val springDataVersion: String by project
val jUnitVersion: String by project
val mockitoVersion: String by project
val hamcrestVersion: String by project
val kotlinLoggingVersion: String by project
val logbackVersion: String by project
val slf4jKotlinCoroutinesVersion: String by project
val cassandraDriverVersion: String by project
val codaHaleMetricsVersion: String by project
val testContainersVersion: String by project
val kotlinCoroutinesVersion: String by project
val commonsIOVersion: String by project
val groovyVersion: String by project
val spockVersion: String by project

plugins {
    kotlin("jvm") version "1.3.70"
    `groovy-base`
    war
    id("org.jmailen.kotlinter") version "2.3.2"
}

allprojects {
    group = "net.jcflorezr.transcriber"
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

/*
    Dependencies which are common in all sub projects
 */
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jmailen.kotlinter")

    dependencies {
        // Kotlin
        implementation(kotlin(module = "stdlib-jdk8", version = kotlinVersion))
        implementation(kotlin(module = "reflect", version = kotlinVersion))

        // Jackson
        implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

        // Logging
        implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")

        // Test
        testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
        testRuntime("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
        testImplementation("org.hamcrest:hamcrest-core:$hamcrestVersion")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

/*
    Dependencies which are common in some of the sub projects
 */
project.allprojects
    .forEach { currentProject ->

        currentProject.takeIf { it.name.contains("adapters") || it.name.contains("application") }?.also {
            project(currentProject.path) {
                dependencies {
                    // Kotlin
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

                    // Spring
                    implementation("org.springframework:spring-core:$springVersion")
                    implementation("org.springframework:spring-context:$springVersion")

                    // Logging
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$slf4jKotlinCoroutinesVersion")

                    // Test
                    testImplementation("org.mockito:mockito-core:$mockitoVersion")
                    testImplementation("org.springframework:spring-test:$springVersion")
                }

                currentProject.takeIf { it.name.contains("adapter") }?.also { adaptersProject ->
                    project(adaptersProject.path) {
                        dependencies {
                            // Cassandra
                            implementation("org.springframework.data:spring-data-cassandra:$springDataVersion")
                            implementation("com.datastax.cassandra:cassandra-driver-core:$cassandraDriverVersion")
                            implementation("com.codahale.metrics:metrics-core:$codaHaleMetricsVersion")

                            // Test
                            testImplementation("org.testcontainers:cassandra:$testContainersVersion")
                            testImplementation(project(":core", "testArtifacts"))
                        }
                    }
                }
            }
        }

        currentProject.takeIf { it.name.contains("adapters") || it.name.contains("domain") }?.also {
            project(currentProject.path) {
                apply(plugin = "org.gradle.groovy-base")

                dependencies {
                    // Test
                    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$jUnitVersion")
                    testImplementation("org.codehaus.groovy:groovy-all:$groovyVersion")
                    testImplementation("org.spockframework:spock-core:$spockVersion")
                }
            }
        }
    }

/*
    Dependencies among the modules of the domain, this is how the Domain Context is assembled
 */

// Audio Splitter

project(":audio-splitter:audio-splitter-domain") {
    dependencies {
        implementation(project(":core"))
    }
}

project(":audio-splitter:audio-splitter-adapters") {
    dependencies {
        implementation(project(":audio-splitter:audio-splitter-domain"))
        implementation(project(":core"))
    }
}

project(":audio-splitter:audio-splitter-application") {
    dependencies {
        implementation(project(":audio-splitter:audio-splitter-domain"))
        implementation(project(":audio-splitter:audio-splitter-adapters"))
        implementation(project(":core"))
    }
}

// Audio Transcriber

project(":audio-transcriber:audio-transcriber-domain") {
    dependencies {
        implementation(project(":core"))
    }
}

project(":audio-transcriber:audio-transcriber-adapters") {
    dependencies {
        implementation(project(":audio-transcriber:audio-transcriber-domain"))
        implementation(project(":core"))
    }
}

project(":audio-transcriber:audio-transcriber-application") {
    dependencies {
        implementation(project(":audio-transcriber:audio-transcriber-domain"))
        implementation(project(":audio-transcriber:audio-transcriber-adapters"))
        implementation(project(":core"))
    }
}