import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
}

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
    doLast { println("my version: $version") }
}

gradlePlugin {
    plugins {
        create("kodamaPlugin") {
            id = "de.lostmekka.kodama"
            implementationClass = "de.lostmekka.kodama.gradle.KodamaGradlePlugin"
        }
    }
}
