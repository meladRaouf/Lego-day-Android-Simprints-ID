package com.simprints.fingerprint.commontesttools.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.FingerIdentifier
import com.simprints.testtools.common.di.DependencyRule.RealRule
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.PreferencesModule
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.tools.serializers.Serializer
import com.simprints.testtools.common.di.DependencyRule

// Duplicated from id/commontestttools

class TestPreferencesModule(var remoteConfigRule: DependencyRule = RealRule,
                            var settingsPreferencesManagerRule: DependencyRule = RealRule)
    : PreferencesModule() {

    override fun provideRemoteConfig(): FirebaseRemoteConfig =
        remoteConfigRule.resolveDependency { super.provideRemoteConfig() }

    override fun provideSettingsPreferencesManager(
        prefs: ImprovedSharedPreferences,
        remoteConfigWrapper: RemoteConfigWrapper,
        fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
        groupSerializer: Serializer<GROUP>,
        modalitySerializer: Serializer<Modality>,
        languagesStringArraySerializer: Serializer<Array<String>>,
        moduleIdOptionsStringSetSerializer: Serializer<Set<String>>,
        peopleDownSyncTriggerToBooleanSerializer: Serializer<Map<PeopleDownSyncTrigger, Boolean>>
    ): SettingsPreferencesManager =
        settingsPreferencesManagerRule.resolveDependency { super.provideSettingsPreferencesManager(prefs, remoteConfigWrapper, fingerIdToBooleanSerializer, groupSerializer, modalitySerializer, languagesStringArraySerializer, moduleIdOptionsStringSetSerializer, peopleDownSyncTriggerToBooleanSerializer) }
}
