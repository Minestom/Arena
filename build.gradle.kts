plugins {
    id("java")
}

group = "net.minestom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.Minestom:Minestom:ff7098a083")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}