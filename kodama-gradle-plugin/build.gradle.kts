import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("java-gradle-plugin")
}

group = "de.lostmekka.kodama"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("gradle-plugin-api"))
    compileOnly(kotlin("gradle-plugin"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf(
        "-opt-in=kotlin.RequiresOptIn",
    )
    doLast { println("my version: $version") }
}

gradlePlugin {
    plugins {
        create("redactedPlugin") {
            id = "de.lostmekka.kodama"
            implementationClass = "de.lostmekka.kodama.gradle.KodamaGradlePlugin"
        }
    }
}
