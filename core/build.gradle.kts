dependencies {

    // Audio formats
    implementation("net.sourceforge.javaflacencoder:java-flac-encoder:0.3.7")
}

val testCoreJar by tasks.registering(Jar::class) {
    dependsOn("testClasses")
    classifier = "tests"
    from(project.the<SourceSetContainer>()["test"].output)
}
val testArtifacts: Configuration by configurations.creating
artifacts.add(testArtifacts.name, testCoreJar)