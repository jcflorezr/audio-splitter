import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint") version "7.2.1"
}

dependencies {
    // Kotlin
    implementation(kotlin(module = "stdlib-jdk8", version = "1.3.60"))
    implementation(kotlin(module = "reflect", version = "1.3.60"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")

    // Domain context
    implementation(project(":audio-transcriber:audio-transcriber-domain"))
    implementation(project(":audio-transcriber:audio-transcriber-adapters"))
    implementation(project(":core"))

    // Spring
    implementation("org.springframework:spring-core:5.2.0.RELEASE")
    implementation("org.springframework:spring-context:5.2.0.RELEASE")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    // Util
    implementation("commons-io:commons-io:2.5")

    // Logging
    implementation("io.github.microutils:kotlin-logging:1.6.25")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.3.0")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testImplementation("org.mockito:mockito-core:3.2.4")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
    testImplementation("org.springframework:spring-test:5.2.0.RELEASE")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
    }
}