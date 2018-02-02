package com.simprints.id.secure.cryptography

import javax.crypto.Cipher
import android.util.Base64
import java.security.PublicKey


class AsymmetricEncrypter(publicKeyString: String) {

    private val rsaCipher: Cipher = AsymmetricEncryptionHelper().getCipher()
    private val publicKey: PublicKey = AsymmetricEncryptionHelper().getPublicKeyFromBase64String(publicKeyString)

    fun encrypt(string: String): String {
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val plainBytes = string.toByteArray()
        val encryptedBytes = rsaCipher.doFinal(plainBytes)
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

}