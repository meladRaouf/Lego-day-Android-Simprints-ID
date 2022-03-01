package com.simprints.id.testtools.di

import com.simprints.id.Application
import com.simprints.id.activities.alert.AlertActivityTest
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivityTest
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentPresenterTest
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivityTest
import com.simprints.id.activities.consent.ConsentActivityTest
import com.simprints.id.activities.login.LoginActivityTest
import com.simprints.id.activities.longConsent.PrivacyNoticeActivityUnitTest
import com.simprints.id.activities.settings.SettingsActivityTest
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionFragmentTest
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleViewModelTest
import com.simprints.id.data.prefs.SettingsPreferencesManagerTest
import com.simprints.id.di.*
import com.simprints.id.secure.ProjectSecretManagerTest
import com.simprints.id.services.sync.images.ImageUpSyncWorkerTest
import com.simprints.id.services.sync.subjects.down.workers.EventDownSyncCountWorkerTest
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        PreferencesModule::class,
        SerializerModule::class,
        DataModule::class,
        SyncModule::class,
        DashboardActivityModule::class,
        SecurityModule::class,
        ViewModelModule::class
    ]
)
interface AppComponentForTests : AppComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun dataModule(dataModule: DataModule): Builder
        fun syncModule(syncModule: SyncModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder
        fun securityModule(securityModule: SecurityModule): Builder
        fun viewModelModule(viewModelModule: ViewModelModule): Builder

        fun build(): AppComponentForTests
    }

    fun inject(checkLoginFromMainLauncherActivityTest: CheckLoginFromMainLauncherActivityTest)
    fun inject(projectSecretManagerTest: ProjectSecretManagerTest)
    fun inject(alertActivityTest: AlertActivityTest)
    fun inject(settingsPreferencesManagerTest: SettingsPreferencesManagerTest)
    fun inject(moduleViewModelTest: ModuleViewModelTest)
    fun inject(consentActivityTest: ConsentActivityTest)
    fun inject(subjectsDownSyncCountWorkerTest: EventDownSyncCountWorkerTest)
    fun inject(checkLoginFromIntentPresenterTest: CheckLoginFromIntentPresenterTest)
    fun inject(checkLoginFromIntentActivityTest: CheckLoginFromIntentActivityTest)
    fun inject(settingsActivityTest: SettingsActivityTest)
    fun inject(moduleSelectionFragmentTest: ModuleSelectionFragmentTest)
    fun inject(loginActivityTest: LoginActivityTest)
    fun inject(privacyNoticeActivityUnitTest: PrivacyNoticeActivityUnitTest)

    @ExperimentalCoroutinesApi
    fun inject(imageUpSyncWorkerTest: ImageUpSyncWorkerTest)
}
