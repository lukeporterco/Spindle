import org.gradle.jvm.tasks.Jar

plugins {
    application
}

application {
    mainClass.set("com.mcmodloader.sampleserverfixture.FakeMinecraftServerMain")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}
