package com.simprints.id.testtools.testingapi.models

data class TestProject(val id: String,
                       val name: String,
                       val description: String,
                       val creator: String,
                       val legacyId: String,
                       val secret: String)