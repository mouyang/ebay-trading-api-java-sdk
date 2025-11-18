apply(plugin = "maven-publish")

group = "com.ebay.developer"
version = "2.0.0-${project.properties["ebayApiVersion"]}-SNAPSHOT"

configure<PublishingExtension> {
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