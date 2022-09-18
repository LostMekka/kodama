rootProject.name = "kodama"
include("kodama-compiler-plugin")
include("kodama-core")
include("kodama-gradle-plugin")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

//includeBuild("kodama-gradle-plugin") {
//    dependencySubstitution {
//        substitute(module("de.lostmekka.kodama:kodama-gradle-plugin")).using(project(":"))
//    }
//}
