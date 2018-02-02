package com.simprints.id.secure

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NonceManager {

    companion object {
        fun requestNonce(nonceScope: NonceScope): Single<String> {
            val jsonNonceScope = JsonHelper.toJson(nonceScope)

            val headersMapType = object : TypeToken<HashMap<String, String>>() {}.type
            val headers = Gson().fromJson<HashMap<String, String>>(jsonNonceScope, headersMapType)

            return ApiService.nonce(headers, "AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}