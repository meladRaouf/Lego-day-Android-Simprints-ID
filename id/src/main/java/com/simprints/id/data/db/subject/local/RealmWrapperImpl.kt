package com.simprints.id.data.db.subject.local

import android.content.Context
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.security.LocalDbKey
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.db.subject.migration.SubjectsRealmConfig
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.infra.logging.Simber
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.withContext

class RealmWrapperImpl(
    private val appContext: Context,
    private val secureDataManager: SecureLocalDbKeyProvider,
    private val loginInfoManager: LoginInfoManager,
    private val dispatcher: DispatcherProvider,
) :
    RealmWrapper {
    /**
     * Use realm instance in from IO threads
     * throws RealmUninitialisedException
     */
    override suspend fun <R> useRealmInstance(block: (Realm) -> R): R =
        withContext(dispatcher.io()) {
            Simber.tag(CrashReportTag.REALM_DB.name)
                .d("[RealmWrapperImpl] getting new realm instance")
            Realm.getInstance(config).use(block)
        }

    private val config: RealmConfiguration by lazy {
        Realm.init(appContext)
        createAndSaveRealmConfig(getLocalDbKey())
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): RealmConfiguration =
        SubjectsRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)

    private fun getLocalDbKey(): LocalDbKey =
        loginInfoManager.getSignedInProjectIdOrEmpty().let {
            return if (it.isNotEmpty()) {
                secureDataManager.getLocalDbKeyOrThrow(it)
            } else {
                throw RealmUninitialisedException("No signed in project id found")
            }
        }
}