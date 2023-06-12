plugins {
    id("simprints.infra")
    id("simprints.library.room")
    id("simprints.config.cloud")
}

android {
    namespace = "com.simprints.infra.authlogic"


    buildTypes {
        getByName("release") {
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "30L")
        }
        getByName("staging") {
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infraconfig"))
    implementation(project(":infraevents"))

    implementation(project(":infraeventsync"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraimages"))
    implementation(project(":infrarecentuseractivity"))

    implementation(libs.retrofit.core)
    implementation(libs.playServices.integrity)
    implementation(libs.workManager.work)
}