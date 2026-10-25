package com.dev.myapplication.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class EncryptedPayload(
    val encryptedBase64: String,
    val ivBase64: String
)

object KeystoreVaultManager {

    private const val KEY_ALIAS = "PulseMind_Vault_Key_AES256"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    init {
        try {
            generateSecretKeyIfNeeded()
        } catch (_: Exception) {}
    }

    private fun generateSecretKeyIfNeeded() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val parameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(parameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {}
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (_: Exception) {
            null
        }
    }

    fun encrypt(plainText: String): EncryptedPayload {
        return try {
            val secretKey = getSecretKey() ?: throw IllegalStateException("KeyStore key unavailable")
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            EncryptedPayload(
                encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
                ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            )
        } catch (_: Exception) {
            EncryptedPayload(encryptedBase64 = "ENCRYPTION_ERROR", ivBase64 = "")
        }
    }

    fun decrypt(payload: EncryptedPayload): String {
        return try {
            val secretKey = getSecretKey() ?: return "DECRYPTION_ERROR"
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivBytes = Base64.decode(payload.ivBase64, Base64.NO_WRAP)
            val spec = GCMParameterSpec(128, ivBytes)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val encryptedBytes = Base64.decode(payload.encryptedBase64, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            "DECRYPTION_ERROR"
        }
    }

    fun isKeyStoreAvailable(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            keyStore.containsAlias(KEY_ALIAS)
        } catch (_: Exception) {
            false
        }
    }
}
