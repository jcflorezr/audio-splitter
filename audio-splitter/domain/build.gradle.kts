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

    // Domain context
    implementation(project(":core"))

    // Util
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testRuntime("org.junit.vintage:junit-vintage-engine:5.4.2")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
    testImplementation("org.codehaus.groovy:groovy-all:2.5.7")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
    }
}