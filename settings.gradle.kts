pluginManagement {
    plugins {
        id("com.android.library") version "8.1.4"
        id("org.jetbrains.kotlin.android") version "1.9.20"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "morphkit"
