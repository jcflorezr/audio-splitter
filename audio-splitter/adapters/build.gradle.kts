plugins {
    kotlin("jvm")
    `groovy-base`
}

dependencies {
    implementation(kotlin(module = "stdlib-jdk8", version = "1.3.60"))
    implementation(kotlin(module = "reflect", version = "1.3.60"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")

    // Domain context
    implementation(project(":audio-splitter:domain"))
    implementation(project(":core"))

    // Spring
    implementation("org.springframework:spring-core:5.2.0.RELEASE")
    implementation("org.springframework:spring-context:5.2.0.RELEASE")

    // Cloud
    implementation("com.google.cloud:google-cloud-storage:1.66.0")

    // Audio metadata
    implementation("net.jthink:jaudiotagger:2.2.5")

    // Audio formats
    implementation("org.apache.tika:tika-parsers:1.20")
    implementation("net.sourceforge.javaflacencoder:java-flac-encoder:0.3.7")
    implementation("org.jflac:jflac-codec:1.5.2")
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    // Logging
    implementation("io.github.microutils:kotlin-logging:1.6.25")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.3.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Util
    implementation("commons-io:commons-io:2.5")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testImplementation("org.mockito:mockito-core:3.2.4")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
    testImplementation("org.springframework:spring-test:5.2.0.RELEASE")
    testImplementation("org.codehaus.groovy:groovy-all:2.5.7")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}