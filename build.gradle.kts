plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.freefair.lombok") version "6.4.3"
}

tasks {
    jar {
        archiveFileName.set("server.jar")
    }
}

group = "net.minestom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.Minestom:Minestom:3c0abb0409")
    implementation("de.articdive:jnoise:3.0.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        manifest {
            attributes (
                "Main-Class" to "net.minestom.arena.Main",
                "Multi-Release" to true
            )
        }
        archiveBaseName.set("arena")
        mergeServiceFiles()
    }

    build { dependsOn(shadowJar) }
}