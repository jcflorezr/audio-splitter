plugins {
    `groovy-base`
}

dependencies {

    // Domain context
    implementation(project(":core"))

    // Test
    testRuntime("org.junit.vintage:junit-vintage-engine:5.4.2")
    testImplementation("org.codehaus.groovy:groovy-all:2.5.7")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}