plugins {
    `java-library`
    `maven-publish`
}

group = "com.ebay.developer"
version = "2.0.0-${project.properties["ebayApiVersion"]}-SNAPSHOT"

dependencies {
    compileOnly(project(":trading-api-sdkcore"))
    implementation("com.ebay.developer:trading-api-eBLBaseComponents:${project.version}")
    implementation("org.apache.commons:commons-text:1.9")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            afterEvaluate {
                from(components["java"])
            }
        }
    }
    repositories {
        mavenLocal()
    }
}