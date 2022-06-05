import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
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
    implementation("com.github.Minestom:Minestom:b112248ae81af858728ee68c106900f4a9ea115a")
    implementation("de.articdive:jnoise:3.0.2")
    implementation("io.prometheus:simpleclient:0.15.0")
    implementation("io.prometheus:simpleclient_hotspot:0.15.0")
    implementation("io.prometheus:simpleclient_httpserver:0.15.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    named<ShadowJar>("shadowJar") {
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