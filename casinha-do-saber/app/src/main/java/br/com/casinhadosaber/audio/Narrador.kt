package br.com.casinhadosaber.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.speech.tts.Voice
import br.com.casinhadosaber.dominio.Idioma
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import java.util.Locale

/**
 * Lê as instruções em voz alta. Crianças de 4 anos ainda não leem, então toda pergunta é falada.
 *
 * Para soar o mais natural possível sem gravações: escolhe a melhor voz instalada (offline) de cada
 * idioma, fala devagar, faz pausas entre as frases e diz as palavras em inglês ainda mais devagar.
 */
class Narrador(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var pronto = false
    private var pendente: List<Trecho>? = null
    private val portugues = Locale("pt", "BR")
    private var vozPt: Voice? = null
    private var vozEn: Voice? = null
    private var inglesDisponivel = true
    var ativo = true

    /** Está falando agora? (a criança personagem mexe a boca junto) */
    var falando by mutableStateOf(false)
        private set
    @Volatile private var ultimaFala = ""

    /** Descrição das vozes escolhidas (para o relatório do teste no emulador). */
    fun descricaoDasVozes(): String =
        "pt: ${vozPt?.name} (qualidade ${vozPt?.quality}); en: ${vozEn?.name} (qualidade ${vozEn?.quality})"

    /** Há voz em português e em inglês de boa qualidade? (a área dos pais sugere instalar se não houver) */
    var vozesBoas = true
        private set

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        pronto = true
        val r = tts.isLanguageAvailable(Locale.US)
        inglesDisponivel = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        vozPt = melhorVoz(portugues)
        vozEn = melhorVoz(Locale.US)
        vozesBoas = (vozPt?.quality ?: 0) >= Voice.QUALITY_HIGH && (vozEn?.quality ?: 0) >= Voice.QUALITY_HIGH
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { falando = true }
            override fun onDone(utteranceId: String?) { if (utteranceId == ultimaFala) falando = false }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { falando = false }
            override fun onStop(utteranceId: String?, interrupted: Boolean) { falando = false }
        })
        pendente?.let { falar(it) }
        pendente = null
    }

    /** A voz offline de maior qualidade para o idioma (e o país, quando der). */
    private fun melhorVoz(local: Locale): Voice? = runCatching {
        val doIdioma = tts.voices.orEmpty().filter { it.locale.language == local.language && !it.isNetworkConnectionRequired }
        val ordem = compareByDescending<Voice> { it.locale.country == local.country }.thenByDescending { it.quality }.thenBy { it.latency }
        // Primeiro só vozes já instaladas; se não houver, qualquer voz offline do idioma.
        doIdioma.filter { !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }.sortedWith(ordem).firstOrNull()
            ?: doIdioma.sortedWith(ordem).firstOrNull()
    }.getOrNull()

    fun falar(texto: String) = falar(listOf(pt(texto)))

    fun falar(trechos: List<Trecho>) {
        if (!ativo || trechos.isEmpty()) return
        if (!pronto) {
            pendente = trechos
            return
        }
        var primeiro = true
        for (trecho in trechos) {
            val ingles = trecho.idioma == Idioma.EN && inglesDisponivel
            // Frases separadas por pontuação viram falas com pausinhas entre elas.
            val frases = trecho.texto.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
            for (frase in frases) {
                if (!primeiro) tts.playSilentUtterance(if (ingles) 300L else 220L, TextToSpeech.QUEUE_ADD, null)
                configurar(ingles)
                val id = "fala-${System.nanoTime()}"
                ultimaFala = id
                tts.speak(frase, if (primeiro) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, id)
                primeiro = false
            }
        }
    }

    private fun configurar(ingles: Boolean) {
        val voz = if (ingles) vozEn else vozPt
        if (voz != null) runCatching { tts.voice = voz } else tts.setLanguage(if (ingles) Locale.US else portugues)
        tts.setSpeechRate(if (ingles) 0.75f else 0.88f)
        tts.setPitch(1.1f)
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
