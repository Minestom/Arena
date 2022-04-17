plugins {
    id("java")
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
    implementation("com.github.Minestom:Minestom:0f767da5f0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
