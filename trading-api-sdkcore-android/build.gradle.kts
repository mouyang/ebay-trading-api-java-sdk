plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

group = "com.ebay.developer"
version = "2.0.0-SNAPSHOT"

android {
    namespace = "com.ebay.sdk"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    publishing {
        // https://proandroiddev.com/how-to-locally-test-your-android-or-kmp-library-using-maven-local-b1283824d628
        singleVariant("release")
        // END https://proandroiddev.com/how-to-locally-test-your-android-or-kmp-library-using-maven-local-b1283824d628
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.20.0"))
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.2.1"))
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("com.ebay.developer:trading-api-eBLBaseComponents:2.0.0-SNAPSHOT")
    implementation("com.squareup.okhttp3:okhttp-android")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations")
    implementation("javax.xml.stream:stax-api:1.0-2")
    implementation("xerces:xercesImpl:2.12.2") {
        exclude(group = "xml-apis", module = "xml-apis")
    }
}

// https://proandroiddev.com/how-to-locally-test-your-android-or-kmp-library-using-maven-local-b1283824d628
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = groupId
            artifactId = artifactId
            version = version
            // Wait for Android to finish configuration
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
// END https://proandroiddev.com/how-to-locally-test-your-android-or-kmp-library-using-maven-local-b1283824d628