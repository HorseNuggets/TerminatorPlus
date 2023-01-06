pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "TerminatorPlus"
include("TerminatorPlus-Plugin")
include("TerminatorPlus-API")

// set the version
