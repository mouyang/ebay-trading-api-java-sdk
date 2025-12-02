plugins {
    `java-library`
    `maven-publish`
}

apply(from = "${rootDir}/trading-api-core-java/build.gradle.kts")
apply(from = "${rootDir}/trading-api-core-maven/build.gradle.kts")

dependencies {
    compileOnly(project(":trading-api-sdkcore"))
    implementation("com.ebay.developer:trading-api-eBLBaseComponents:${project.version}")
    implementation("org.apache.commons:commons-text:1.9")
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("apiVersion") {
            afterEvaluate {
                /* By default, the artifactId is the folder name which has the eBay API version in it.  This is not
                   necessary because it is embeddded in the artifact itself.
                 */
                artifactId = "trading-api-version"
                from(components["java"])
            }
        }
    }
    repositories {
        mavenLocal()
    }
}