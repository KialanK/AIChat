pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        versions("1.20.1","1.20.2","1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21.1", "1.21.2", "1.21.3", "1.21.4")
    }
    create(rootProject)
}

rootProject.name = "AIChat"