rootProject.name = "kodama-samples"

pluginManagement {
    includeBuild("../kodama")
}
includeBuild("../kodama") {
    dependencySubstitution {
        substitute(module("de.lostmekka.kodama:kodama-gradle-plugin")).using(project(":"))
    }
}
