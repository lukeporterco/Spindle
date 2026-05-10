plugins {
    `java-library`
}

dependencies {
    api(project(":spindle-loader-api"))
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":spindle-loader-cli"))
    testImplementation(project(":target-minecraft"))
    testImplementation(project(":sample-server-fixture"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
