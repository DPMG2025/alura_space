package br.com.casinhadosaber.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import br.com.casinhadosaber.dominio.Idioma
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import java.util.Locale

/**
 * Lê as instruções em voz alta. Crianças de 4 anos ainda não leem,
 * então toda pergunta é falada. Trechos em inglês usam a voz em inglês.
 */
class Narrador(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var pronto = false
    private var pendente: List<Trecho>? = null
    private val portugues = Locale("pt", "BR")
    private var inglesDisponivel = true
    var ativo = true

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        pronto = true
        val r = tts.isLanguageAvailable(Locale.US)
        inglesDisponivel = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        tts.setPitch(1.08f)
        pendente?.let { falar(it) }
        pendente = null
    }

    fun falar(texto: String) = falar(listOf(pt(texto)))

    fun falar(trechos: List<Trecho>) {
        if (!ativo || trechos.isEmpty()) return
        if (!pronto) {
            pendente = trechos
            return
        }
        trechos.forEachIndexed { i, trecho ->
            val ingles = trecho.idioma == Idioma.EN && inglesDisponivel
            tts.setLanguage(if (ingles) Locale.US else portugues)
            tts.setSpeechRate(if (ingles) 0.8f else 0.92f)
            val modo = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(trecho.texto, modo, null, "fala-$i-${System.nanoTime()}")
        }
    }

    fun parar() {
        pendente = null
        if (pronto) tts.stop()
    }

    fun desligar() {
        tts.stop()
        tts.shutdown()
    }
}
