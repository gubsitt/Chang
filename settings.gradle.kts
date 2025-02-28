pluginManagement {
    repositories {
        google() // ✅ โหลด Google Services
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

// ✅ ตั้งค่า Root Project
rootProject.name = "Chang"
include(":app")

