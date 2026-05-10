plugins {
    `java-library`
}

dependencies {
    implementation(project(":spindle-loader-core"))
    implementation(project(":spindle-loader-api"))
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":spindle-loader-cli"))
    testImplementation(project(":sample-server-fixture"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
