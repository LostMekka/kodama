import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.6.21-1.0.5"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.0")
    implementation("com.google.auto.service:auto-service-annotations:1.0.1")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf(
        "-opt-in=kotlin.RequiresOptIn",
    )
}
