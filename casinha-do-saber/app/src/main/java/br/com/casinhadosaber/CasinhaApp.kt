package br.com.casinhadosaber

import android.app.Application
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat

class CasinhaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Se o app fechar sozinho, anota o motivo no aparelho para a área dos pais mostrar.
        br.com.casinhadosaber.dados.GuardaDeFalhas.instalar(this)
        // Fonte de emojis embutida no app: todos os desenhos aparecem iguais em qualquer celular.
        EmojiCompat.init(BundledEmojiCompatConfig(this).setReplaceAll(true))
    }
}
