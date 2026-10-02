package com.example.ciphermatrix

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoHelper {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "CipherMatrixVaultKey"
    private const val GCM_TAG_LENGTH = 128 // bits
    private const val IV_LENGTH = 12 // bytes, standard for GCM

    // 🔑 Gets the existing key from the Android Keystore, or generates
    // a new one on first use. The key itself never leaves secure hardware
    // and never appears anywhere in source code.
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    // 🔒 Encrypt plaintext. A fresh random IV is generated per call and
    // prepended to the ciphertext before Base64 encoding.
    fun encrypt(plainText: String): String {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            Log.e("CipherMatrix_Crypto", "Encryption failed: ${e.message}")
            ""
        }
    }

    // 🔓 Decrypt Base64 ciphertext back to the original plaintext.
    // The IV is read back from the first 12 bytes of the payload.
    fun decrypt(cipherText: String): String {
        if (cipherText.isEmpty()) return "Error: Ciphertext is empty"
        return try {
            val secretKey = getOrCreateSecretKey()
            val combined = Base64.decode(cipherText, Base64.NO_WRAP)

            val iv = combined.copyOfRange(0, IV_LENGTH)
            val encryptedBytes = combined.copyOfRange(IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CipherMatrix_Crypto", "Decryption failed: ${e.message}")
            "Error Decrypting"
        }
    }
}