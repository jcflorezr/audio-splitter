plugins {
    `groovy-base`
}

dependencies {
    // Domain context
    implementation(project(":audio-splitter:audio-splitter-domain"))
    implementation(project(":core"))

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

    // Util
    implementation("commons-io:commons-io:2.5")

    // Test
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.4.2")
    testImplementation("org.codehaus.groovy:groovy-all:2.5.7")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation(project(":core", "testArtifacts"))
}