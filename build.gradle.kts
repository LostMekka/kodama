buildscript {
    dependencies {
        classpath("de.lostmekka.kodama:kodama-gradle-plugin")
    }
}

plugins {
    kotlin("jvm") version "1.7.10" apply false
}

allprojects {
    group = "de.lostmekka.kodama"
    version = "1.0.0-SNAPSHOT"
}
