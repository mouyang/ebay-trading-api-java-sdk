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
        mavenLocal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "trading-api-parent"
include(":trading-api-core-java")
include(":trading-api-maven-publish")
include(":trading-api-eBLBaseComponents")
include(":trading-api-sdkcore")
include(":trading-api-sdkcore-android")

if (gradle.startParameter.projectProperties["ebayApiVersion"] == "1331") {
    include(":trading-api-version:1331")
}