plugins {
    kotlin("jvm")
    id("de.lostmekka.kodama")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("de.lostmekka.kodama:kodama-core"))
            .using(project(":kodama-core"))
        substitute(module("de.lostmekka.kodama:kodama-compiler-plugin"))
            .using(project(":kodama-compiler-plugin"))
    }
}
