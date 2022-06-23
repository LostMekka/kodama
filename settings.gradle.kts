rootProject.name = "kodama"
include("kodama-core")
include("kodama-compiler-plugin")
include("kodama-samples")

includeBuild("kodama-gradle-plugin") {
    dependencySubstitution {
        substitute(module("de.lostmekka.kodama:kodama-gradle-plugin")).using(project(":"))
    }
}
