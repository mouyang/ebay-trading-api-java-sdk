plugins {
    `java-library`
}

// placeholder to signal this project builds a Java 11 artifact
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
}

dependencies {
}
