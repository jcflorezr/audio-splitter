import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    `groovy-base`
}

dependencies {
    // Domain context
    implementation(project(":audio-transcriber:audio-transcriber-domain"))
    implementation(project(":core"))

    // Cloud
    implementation("com.google.cloud:google-cloud-storage:1.66.0")
    implementation("com.google.cloud:google-cloud-speech:1.22.0")

    // Test
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.4.2")
    testImplementation("org.codehaus.groovy:groovy-all:2.5.7")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation(project(":core", "testArtifacts"))
}