plugins {
    `java-library`
    `maven-publish`
}

apply(from = "${rootDir}/trading-api-core-java/build.gradle.kts")
apply(from = "${rootDir}/trading-api-core-maven/build.gradle.kts")

dependencies {
    compileOnly(project(":trading-api-eBLBaseComponents"))
    implementation("com.ebay.developer:trading-api-eBLBaseComponents:${project.version}")
    implementation("jakarta.xml.ws:jakarta.xml.ws-api:4.0.2")
    implementation("org.slf4j:slf4j-api:1.7.5")
    implementation("xalan:xalan:2.7.3")
    implementation("org.apache.servicemix.bundles:org.apache.servicemix.bundles.oro:2.0.8_5")
    implementation("org.apache.commons:commons-text:1.9")
}