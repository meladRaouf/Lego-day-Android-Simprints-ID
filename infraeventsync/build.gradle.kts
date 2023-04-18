plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.infra.eventsync"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures.buildConfig = true
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
    implementation(project(":infraevents"))
    implementation(project(":infraconfig"))
    implementation(project(":infralogin"))
    implementation(project(":infraenrolmentrecords"))

    implementation(libs.workManager.work)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
