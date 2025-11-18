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