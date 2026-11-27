
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sunderiya.burnoutdetector"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.sunderiya.burnoutdetector"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

// Export Room database schema files.
ksp {
    arg(
        "room.schemaLocation",
        "$projectDir/schemas"
    )
}

dependencies {

    // Existing Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Existing Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // DataStore
    implementation(
        "androidx.datastore:datastore-preferences:1.1.7"
    )

    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Local unit testing
    testImplementation(libs.junit)

    // Android instrumentation testing
    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Room database testing
    androidTestImplementation(libs.androidx.room.testing)

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
