dependencies {
    // Audio formats
    implementation("net.sourceforge.javaflacencoder:java-flac-encoder:0.3.7")
}

val integrationTestCoreJar by tasks.registering(Jar::class) {
    dependsOn("integrationTestClasses")
    archiveClassifier.convention("integration-test")
    archiveClassifier.set("integration-test")
    from(project.the<SourceSetContainer>()["integrationTest"].output)
}
val integrationTestArtifact: Configuration by configurations.creating

val componentTestCoreJar by tasks.registering(Jar::class) {
    dependsOn("componentTestClasses")
    archiveClassifier.convention("component-test")
    archiveClassifier.set("component-test")
    from(project.the<SourceSetContainer>()["componentTest"].output)
}
val componentTestArtifact: Configuration by configurations.creating

artifacts.add(integrationTestArtifact.name, integrationTestCoreJar)
artifacts.add(componentTestArtifact.name, componentTestCoreJar)
