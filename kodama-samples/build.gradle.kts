plugins {
    kotlin("jvm") version "1.7.10"
    id("de.lostmekka.kodama")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}
