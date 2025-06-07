pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // BU SƏTRİN OLDUĞUNDAN ƏMİN OLUN:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RJ Export Call Logs Free"
include(":app")
