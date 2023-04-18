plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    namespace = "com.simprints.infra.eventsync"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
        }
        getByName("staging") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }

    sourceSets {
        // Adds exported room schema location as test app assets.
        getByName("debug") {
            assets.srcDirs("$projectDir/schemas")
        }
        getByName("test") {
            java.srcDirs("$projectDir/src/debug")
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    api(project(":core"))
    implementation(project(":infralogging"))
    api(project(":infraevents"))
    implementation(project(":infranetwork"))
    implementation(project(":infraenrolmentrecords"))

    implementation(libs.androidX.room.ktx)
    kapt(libs.androidX.room.compiler)

    implementation(libs.androidX.lifecycle.livedata.ktx)
    runtimeOnly(libs.kotlin.coroutinesAndroid)
    api(libs.sqlCipher.core)
    implementation(libs.workManager.work)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    // DI
    implementation(libs.hilt)
    implementation(libs.hilt.work)
    kapt(libs.hilt.kapt)
    kapt(libs.hilt.compiler)

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.coroutines.test)
    testImplementation(libs.testing.robolectric.annotation)
    testImplementation(libs.testing.koTest.kotlin.assert)
    testImplementation(libs.testing.androidX.room)
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.testing.work)

    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.mockk.android)

}