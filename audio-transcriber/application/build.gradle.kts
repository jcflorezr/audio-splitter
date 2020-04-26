dependencies {
    // Domain context
    implementation(project(":audio-transcriber:audio-transcriber-domain"))
    implementation(project(":audio-transcriber:audio-transcriber-adapters"))
    implementation(project(":core"))

    // Util
    implementation("commons-io:commons-io:2.5")
}