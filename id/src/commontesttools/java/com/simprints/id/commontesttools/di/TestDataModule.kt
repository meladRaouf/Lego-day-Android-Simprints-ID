package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.security.LocalDbKey
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.RealmWrapper
import com.simprints.id.data.db.subject.local.RealmWrapperImpl
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.di.DataModule
import com.simprints.id.network.BaseUrlProvider
import com.simprints.testtools.common.di.DependencyRule
import dagger.Provides

class TestDataModule(
    private val projectLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRemoteDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val personLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val imageRepositoryRule: DependencyRule = DependencyRule.RealRule
) : DataModule() {

    override fun provideProjectLocalDataSource(
        realmWrapper: RealmWrapper
    ): ProjectLocalDataSource =
        projectLocalDataSourceRule.resolveDependency {
            super.provideProjectLocalDataSource(
             realmWrapper
            )
        }

    override fun provideProjectRemoteDataSource(simApiClientFactory: SimApiClientFactory): ProjectRemoteDataSource =
        projectRemoteDataSourceRule.resolveDependency {
            super.provideProjectRemoteDataSource(simApiClientFactory)
        }

    override fun provideProjectRepository(
        projectLocalDataSource: ProjectLocalDataSource,
        projectRemoteDataSource: ProjectRemoteDataSource,
        remoteConfigWrapper: RemoteConfigWrapper
    ): ProjectRepository = projectRepositoryRule.resolveDependency {
        super.provideProjectRepository(
            projectLocalDataSource,
            projectRemoteDataSource,
            remoteConfigWrapper
        )
    }


    override fun provideSubjectRepository(
        subjectLocalDataSource: SubjectLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource
    ): SubjectRepository = personRepositoryRule.resolveDependency {
        super.provideSubjectRepository(
            subjectLocalDataSource,
            eventRemoteDataSource
        )
    }

    override fun provideImageRepository(
        context: Context,
        baseUrlProvider: BaseUrlProvider,
        remoteDbManager: RemoteDbManager
    ): ImageRepository = imageRepositoryRule.resolveDependency {
        super.provideImageRepository(context, baseUrlProvider, remoteDbManager)
    }

    override fun provideLongConsentLocalDataSource(
        context: Context,
        loginInfoManager: LoginInfoManager
    ): LongConsentLocalDataSource =
        longConsentLocalDataSourceRule.resolveDependency {
            super.provideLongConsentLocalDataSource(
                context,
                loginInfoManager
            )
        }

    override fun provideLongConsentRepository(
        longConsentLocalDataSource: LongConsentLocalDataSource,
        longConsentRemoteDataSource: LongConsentRemoteDataSource
    ): LongConsentRepository =
        longConsentRepositoryRule.resolveDependency {
            super.provideLongConsentRepository(
                longConsentLocalDataSource,
                longConsentRemoteDataSource
            )
        }

    override fun providePersonLocalDataSource(
     realmWrapper: RealmWrapper
    ): SubjectLocalDataSource =
        personLocalDataSourceRule.resolveDependency {
            super.providePersonLocalDataSource(
              realmWrapper
            )
        }
   }
