import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    kotlin("jvm")
    `groovy-base`
    id("org.jlleitschuh.gradle.ktlint") version "7.2.1"
}

dependencies {
    implementation(kotlin(module = "stdlib-jdk8", version = "1.3.60"))
    implementation(kotlin(module = "reflect", version = "1.3.60"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")

    // Domain context
    implementation(project(":audio-transcriber:audio-transcriber-domain"))
    implementation(project(":core"))

    // Spring
    implementation("org.springframework:spring-core:5.2.0.RELEASE")
    implementation("org.springframework:spring-context:5.2.0.RELEASE")

    // Cassandra
    implementation("org.springframework.data:spring-data-cassandra:2.2.6.RELEASE")
    implementation("com.datastax.cassandra:cassandra-driver-core:3.7.2")
    implementation("com.codahale.metrics:metrics-core:3.0.2")

    // Cloud
    implementation("com.google.cloud:google-cloud-storage:1.66.0")
    implementation("com.google.cloud:google-cloud-speech:1.22.0")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    // Logging
    implementation("io.github.microutils:kotlin-logging:1.6.25")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.3.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testRuntime("org.junit.vintage:junit-vintage-engine:5.4.2")
    testImplementation("org.mockito:mockito-core:3.2.4")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
    testImplementation("org.springframework:spring-test:5.2.0.RELEASE")
    testImplementation("org.codehaus.groovy:groovy-all:2.5.7")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("org.testcontainers:cassandra:1.14.0")
    testImplementation(project(":core", "testArtifacts"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
    }
}