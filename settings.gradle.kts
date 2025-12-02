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
 * knowledge) there is no way to set project properties in a run configuration.  Without this, the value will be null.
 *
 * A side effect of this is that build sync will only be relative to the default API version.  You can still build the
 * project in IntelliJ/Android Studio by specifying the version in a build configuration.
 *
 * TODO Research IntelliJ/Android Studio more deeply into seeing if there is a way to provide a project property during
 * build sync or it is not possible.
 */
gradle.extra["ebayApiVersion"] = gradle.startParameter.projectProperties["ebayApiVersion"] ?: "1331"
val apiVersionFile = file("""trading-api-version/${gradle.extra["ebayApiVersion"]}""")
if (apiVersionFile.exists() && apiVersionFile.isDirectory) {
    include(""":trading-api-version:${gradle.extra["ebayApiVersion"]}""")
}