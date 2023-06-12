plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.fingerprint"

    defaultConfig {
        testInstrumentationRunner = "com.simprints.fingerprint.CustomTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildTypes {
        getByName("release") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "1440L")
        }

        getByName("staging") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }

        getByName("debug") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {

    // Simprints
    implementation(project(":infraevents"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":fingerprint:infra:matcher"))
    implementation(project(":fingerprint:scanner"))
    implementation(project(":infraconfig"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infraimages"))
    implementation(project(":infrarecentuseractivity"))
    implementation(project(":featurealert"))
    implementation(project(":featureexitform"))

    // If mock/dummy BT adapter is required test implementation can be switched to regular one
    testImplementation(project(":fingerprint:scannermock"))
    //implementation(project(":fingerprintscannermock"))

    implementation(libs.retrofit.core)
    runtimeOnly(libs.jackson.core)

    // Kotlin
    runtimeOnly(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    // Android X
    implementation(libs.androidX.ui.viewpager2)
    implementation(libs.workManager.work)
}