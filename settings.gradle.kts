import java.net.URI // This import is still needed

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

        // --- THIS IS THE FIX ---
        // No more tricks. Just the correct URL.
        maven { url = URI("https://jitpack.io") }
    }
}

rootProject.name = "AsthmaManager"
include(":app")