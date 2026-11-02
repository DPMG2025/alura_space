package br.com.fipeplaca.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Cifra os tokens com AES/GCM usando uma chave guardada no Android Keystore (nunca sai do aparelho).
 * Valores antigos em texto puro continuam legiveis e sao cifrados na proxima gravacao.
 */
object Cifra {
    private const val ALIAS = "fipe_tokens"
    private const val PREFIXO = "k1:"

    private fun chave(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gerador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gerador.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return gerador.generateKey()
    }

    fun cifrar(texto: String): String {
        if (texto.isEmpty()) return ""
        return try {
            val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, chave()) }
            PREFIXO + Base64.encodeToString(c.iv + c.doFinal(texto.toByteArray()), Base64.NO_WRAP)
        } catch (e: Exception) {
            texto // sem Keystore disponivel (ambiente de teste): guarda como antes
        }
    }

    fun decifrar(valor: String?): String {
        if (valor.isNullOrEmpty()) return ""
        if (!valor.startsWith(PREFIXO)) return valor // valor antigo, ainda em texto puro
        return try {
            val dados = Base64.decode(valor.removePrefix(PREFIXO), Base64.NO_WRAP)
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, chave(), GCMParameterSpec(128, dados, 0, 12))
            String(c.doFinal(dados, 12, dados.size - 12))
        } catch (e: Exception) {
            "" // chave perdida (ex.: restauracao em outro aparelho): o usuario informa o token de novo
        }
    }
}
