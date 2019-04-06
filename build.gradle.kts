import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.jcflorezr"
version = "0.1-SNAPSHOT"

buildscript {
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var kotlinVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var springVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var springDataVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var jacksonVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var testContainersVersion: String by extra

    @Suppress("UNUSED_VALUE")
    kotlinVersion = "1.3.10"
    @Suppress("UNUSED_VALUE")
    springVersion = "5.1.0.RELEASE"
    @Suppress("UNUSED_VALUE")
    springDataVersion = "2.1.3.RELEASE"
    @Suppress("UNUSED_VALUE")
    jacksonVersion = "2.9.8"
    @Suppress("UNUSED_VALUE")
    testContainersVersion = "1.10.6"
}

val kotlinVersion: String by extra
val springVersion: String by extra
val springDataVersion: String by extra
val jacksonVersion: String by extra
val testContainersVersion: String by extra

plugins {
    kotlin("jvm") version "1.3.10"
    kotlin("plugin.spring") version "1.3.10"
    id("org.jlleitschuh.gradle.ktlint") version "7.2.1"
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "http://dl.bintray.com/ijabz/maven")
}

dependencies {

    // Kotlin
    implementation(kotlin(module = "stdlib-jdk8", version = kotlinVersion))
    implementation(kotlin(module = "reflect", version = kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")

    // Spring core
    implementation("org.springframework:spring-core:$springVersion")

    // Redis
    implementation("org.springframework.data:spring-data-redis:$springDataVersion")
    implementation("redis.clients:jedis:2.9.0")

    // Cassandra
    implementation("org.springframework.data:spring-data-cassandra:$springDataVersion")
    implementation("com.datastax.cassandra:cassandra-driver-core:3.6.0")
    implementation("com.codahale.metrics:metrics-core:3.0.2")

    // Util
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.uuid:java-uuid-generator:3.1.4")
    implementation("commons-io:commons-io:2.5")
    implementation("org.apache.commons:commons-lang3:3.0")
    implementation("org.apache.tika:tika-parsers:1.20")
    implementation("net.jthink:jaudiotagger:2.2.5")
    implementation("net.sourceforge.javaflacencoder:java-flac-encoder:0.3.7")
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
    implementation("org.jflac:jflac-codec:1.5.2")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
    implementation("io.github.microutils:kotlin-logging:1.6.25")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.google.cloud:google-cloud-storage:1.66.0")

    // Testing
    testImplementation("org.springframework:spring-test:$springVersion")
    testImplementation("junit:junit:4.12")
    testImplementation("org.mockito:mockito-core:2.24.5")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:cassandra:$testContainersVersion")
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    // Overwriting the "implementationKotlin" task.
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    doLast { println("Finished compiling Kotlin source code") }
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    // Overwriting the "implementationTestKotlin" task.
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    doLast { println("Finished compiling Kotlin source code for testing") }
}
