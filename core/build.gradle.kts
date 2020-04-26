dependencies {

    // Audio formats
    implementation("net.sourceforge.javaflacencoder:java-flac-encoder:0.3.7")

    // Cassandra
    implementation("org.springframework.data:spring-data-cassandra:2.2.6.RELEASE")
    testImplementation("org.testcontainers:cassandra:1.14.0")
}

val testJar by tasks.registering(Jar::class) {
    dependsOn("testClasses")
    classifier = "tests"
    from(project.the<SourceSetContainer>()["test"].output)
}
val testArtifacts: Configuration by configurations.creating
artifacts.add(testArtifacts.name, testJar)