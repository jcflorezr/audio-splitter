dependencies {
    // Domain context
    implementation(project(":audio-splitter:audio-splitter-domain"))
    implementation(project(":audio-splitter:audio-splitter-adapters"))
    implementation(project(":core"))

    // Util
    implementation("commons-io:commons-io:2.5")
}
