plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("realm-android")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    namespace = "com.simprints.infra.realm"
}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":infralogin"))
    implementation(project(":infrasecurity"))

    implementation(project(":infralogging"))

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Unit Tests

    testImplementation(libs.testing.robolectric.core)
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines.test)
}
