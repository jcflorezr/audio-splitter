import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
val dockerJavaClientVersion: String by project
val kotlinCoroutinesVersion: String by project
val commonsIOVersion: String by project
val groovyVersion: String by project
val spockVersion: String by project

plugins {
    kotlin("jvm") version "1.3.70"
    `groovy-base`
    war
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
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

    afterEvaluate {
        tasks["ktlintKotlinScriptCheck"].dependsOn(tasks["ktlintKotlinScriptFormat"])
    }
}

subprojects {

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }

    /*
        Dependencies which are common in all sub projects
    */
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
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
        testImplementation("org.hamcrest:hamcrest-core:$hamcrestVersion")
    }

    /*
        Dependencies which are common between 'adapters' and 'application' sub projects
     */
    isAdaptersOrApplicationProject()?.apply {
        project(path) {

            sourceSets {
                create("integrationTest") {
                    compileClasspath += sourceSets.main.get().output
                    runtimeClasspath += sourceSets.main.get().output
                }
            }.also {
                configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
            }

            val integrationTest = task<Test>("integrationTest") {
                description = "Runs the integration tests"
                group = "verification"
                testClassesDirs = sourceSets["integrationTest"].output.classesDirs
                classpath = sourceSets["integrationTest"].runtimeClasspath
                mustRunAfter(tasks["test"])
            }

            tasks.check { dependsOn(integrationTest) }

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

                // Integration Test
                testImplementation(project(":core", "testArtifacts"))
            }

            isAdaptersProject()?.apply {
                project(path) {
                    dependencies {
                        // Cassandra
                        implementation("org.springframework.data:spring-data-cassandra:$springDataVersion")
                        implementation("com.datastax.cassandra:cassandra-driver-core:$cassandraDriverVersion")
                        implementation("com.codahale.metrics:metrics-core:$codaHaleMetricsVersion")
                    }
                }
            }
        }
    }

    containsIntegrationTestsModule()?.apply {
        project(path) {
            dependencies {
                testImplementation("org.springframework.data:spring-data-cassandra:$springDataVersion")
                testImplementation("org.testcontainers:cassandra:$testContainersVersion")
            }
        }
    }

    isAdaptersOrDomainProject()?.apply {
        project(path) {
            apply(plugin = "org.gradle.groovy-base")

            dependencies {
                // Test
                testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$jUnitVersion")
                testImplementation("org.codehaus.groovy:groovy-all:$groovyVersion")
                testImplementation("org.spockframework:spock-core:$spockVersion")
            }
        }
    }

    afterEvaluate {
        tasks["ktlintMainSourceSetCheck"].dependsOn(tasks["ktlintMainSourceSetFormat"])
        tasks["ktlintTestSourceSetCheck"].dependsOn(tasks["ktlintTestSourceSetFormat"])
        tasks.findByPath("$path:ktlintIntegrationTestSourceSetCheck")
            ?.dependsOn(tasks["ktlintIntegrationTestSourceSetFormat"])
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

/*
    Helper functions
 */

fun Project.isAdaptersProject(): Project? = name.takeIf { it.contains("adapters") }?.run { this@isAdaptersProject }
fun Project.isApplicationProject(): Project? = name.takeIf { it.contains("application") }?.run { this@isApplicationProject }
fun Project.isDomainProject(): Project? = name.takeIf { it.contains("domain") }?.run { this@isDomainProject }
fun Project.isCoreProject(): Project? = name.takeIf { it.contains("core") }?.run { this@isCoreProject }

fun Project.containsIntegrationTestsModule(): Project? = run {
    isAdaptersProject() ?: isDomainProject() ?: isCoreProject()
}?.run { this@containsIntegrationTestsModule }

fun Project.isAdaptersOrApplicationProject(): Project? = run {
    isAdaptersProject() ?: isApplicationProject()
}?.run { this@isAdaptersOrApplicationProject }

fun Project.isAdaptersOrDomainProject(): Project? = run {
    isAdaptersProject() ?: isDomainProject()
}?.run { this@isAdaptersOrDomainProject }
