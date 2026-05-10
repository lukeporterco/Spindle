plugins {
    application
}

dependencies {
    implementation(project(":spindle-loader-core"))
    implementation(project(":target-minecraft"))
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":sample-game"))
    testImplementation(project(":sample-mod"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.spindle.core.LoaderMain")
}
