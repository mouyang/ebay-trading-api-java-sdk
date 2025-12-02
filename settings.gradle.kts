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

/**
 * It is preferred to use the project properties directly and making it mandatory instead of setting a default value in
 * gradle.extra.  It is done for IntelliJ/Android Studio compatibility; a build sync will fail because (to my
 * knowledge) there is no way to set project properties in a run configuration.  This will result in the value being
 * set to null.
 *
 * TODO Research IntelliJ/Android Studio more deeply into seeing if there is a way to provide a project property during
 * build sync or it is not possible.
 */
gradle.extra["ebayApiVersion"]  = gradle.startParameter.projectProperties["ebayApiVersion"] ?: "1331"
if (gradle.extra["ebayApiVersion"] == "1331") {
    include(":trading-api-version-1331")
}