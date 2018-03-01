package com.simprints.id.data.db.local

import com.simprints.libcommon.Person
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.tools.Constants
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration

interface LocalDbManager {

    // Lifecycle
    fun signInToLocal(projectId: String, localDbKey: String): Single<Unit>
    fun signOutOfLocal()
    fun isLocalDbInitialized(projectId: String): Boolean

    // Data transfer
    fun savePersonInLocal(fbPerson: fb_Person)
    fun loadPersonFromLocal(destinationList: MutableList<Person>, guid: String, callback: DataCallback)
    fun loadPeopleFromLocal(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?)
    fun getPeopleCountFromLocal(group: Constants.GROUP, userId: String, moduleId: String): Long

    // Database instances
    fun getValidRealmConfig(): RealmConfiguration
    fun getRealmInstance(): Realm
}
