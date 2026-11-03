package br.com.casinhadosaber.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.speech.tts.Voice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import br.com.casinhadosaber.dominio.Idioma
import br.com.casinhadosaber.dominio.ParteDaFala
import br.com.casinhadosaber.dominio.PassoDaFala
import br.com.casinhadosaber.dominio.VozesDaFamilia
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Lê as instruções em voz alta. Crianças de 4 anos ainda não leem, então toda pergunta é falada.
 *
 * Para soar o mais natural possível sem gravações: escolhe a melhor voz instalada (offline) de cada
 * idioma, fala devagar, faz pausas entre as frases e diz as palavras em inglês ainda mais devagar.
 *
 * Vozes da família: as falas montadas com [ParteDaFala] (elogios, incentivos, chegada, vitória e boa
 * noite) tocam a gravação do pai ou da mãe quando a frase foi gravada na área dos pais ([familia]);
 * sem gravação, tudo é falado pela voz do aparelho, como sempre.
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
    private val gravacoes = ConcurrentHashMap<String, (Boolean) -> Unit>()

    /** As vozes gravadas pela família (área dos pais). */
    val familia: VozesGravadas by lazy { VozesGravadas(context) }
    private val principal = Handler(Looper.getMainLooper())

    /** Fim de cada fala com gravação no meio: o id da última frase de um passo leva ao passo seguinte. */
    private val fimDoPasso = ConcurrentHashMap<String, () -> Unit>()

    /** Cada fala nova (ou parar) troca a vez: os passos de uma fala antiga não continuam. */
    @Volatile private var vez = 0

    /** Uma fala com gravação da família está em andamento (o onStop de um tts.stop() antigo não a encerra). */
    @Volatile private var sequenciaDaFamilia = false

    /** Fala com gravação que chegou num passo de voz antes do motor ficar pronto: continua no onInit. */
    private var planoPendente: Triple<List<PassoDaFala>, Int, Int>? = null

    /** O motor de voz já respondeu e está pronto para falar? */
    val estaPronto: Boolean get() = pronto

    /** Descrição das vozes escolhidas (para o relatório do teste no emulador). */
    fun descricaoDasVozes(): String =
        "pt: ${vozPt?.name} (qualidade ${vozPt?.quality}); en: ${vozEn?.name} (qualidade ${vozEn?.quality})"

    /** Há voz em português e em inglês de boa qualidade? (a área dos pais sugere instalar se não houver) */
    var vozesBoas = true
        private set

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val r = tts.isLanguageAvailable(Locale.US)
        inglesDisponivel = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        vozPt = melhorVoz(portugues)
        vozEn = melhorVoz(Locale.US)
        vozesBoas = (vozPt?.quality ?: 0) >= Voice.QUALITY_HIGH && (vozEn?.quality ?: 0) >= Voice.QUALITY_HIGH
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { if (utteranceId == null || !gravacoes.containsKey(utteranceId)) falando = true }
            override fun onDone(utteranceId: String?) {
                utteranceId?.let { gravacoes.remove(it) }?.let { it(true); return }
                utteranceId?.let { fimDoPasso.remove(it) }?.let { fim -> principal.post { fim() }; return }
                if (utteranceId == ultimaFala) falando = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                utteranceId?.let { gravacoes.remove(it) }?.let { it(false); return }
                utteranceId?.let { fimDoPasso.remove(it) }?.let { fim -> principal.post { fim() }; return }
                if (!sequenciaDaFamilia) falando = false
            }
            // O tts.stop() de uma fala com gravação chega aqui depois, quando a gravação já está tocando.
            override fun onStop(utteranceId: String?, interrupted: Boolean) { if (!sequenciaDaFamilia) falando = false }
        })
        // Só fica pronto depois de escolher as vozes: assim a primeira fala já sai com a voz boa.
        pronto = true
        pendente?.let { falar(it) }
        pendente = null
        planoPendente?.let { (plano, i, minhaVez) ->
            planoPendente = null
            principal.post { seguir(plano, i, minhaVez) }
        }
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
        pararFamilia()
        if (!pronto) {
            pendente = trechos
            return
        }
        falarAgora(trechos)
    }

    /**
     * Fala os [trechos] (a primeira frase corta o que estava sendo dito). [aoTerminar], se houver, é chamado
     * quando a última frase termina. Devolve se falou alguma coisa.
     */
    private fun falarAgora(trechos: List<Trecho>, aoTerminar: (() -> Unit)? = null): Boolean {
        val lista = frases(trechos)
        lista.forEachIndexed { i, (frase, ingles) ->
            if (i > 0) tts.playSilentUtterance(if (ingles) 300L else 220L, TextToSpeech.QUEUE_ADD, null)
            configurar(ingles)
            val id = "fala-${System.nanoTime()}"
            ultimaFala = id
            // O fim fica registrado antes de falar: a voz pode terminar antes desta função voltar.
            if (i == lista.lastIndex && aoTerminar != null) fimDoPasso[id] = aoTerminar
            tts.speak(frase, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, id)
        }
        return lista.isNotEmpty()
    }

    /**
     * Fala com as vozes da família: cada parte com gravação toca a gravação, as outras são faladas pela voz
     * do aparelho, na ordem ([VozesDaFamilia.planejar]). Sem nenhuma gravação é igual a [falar].
     */
    fun falarPartes(partes: List<ParteDaFala>) {
        if (!ativo || partes.isEmpty()) return
        val plano = VozesDaFamilia.planejar(partes, familia.paraTocar())
        if (plano.none { it is PassoDaFala.Gravacao }) {
            falar(partes.map { it.trecho })
            return
        }
        pararFamilia()
        pendente = null
        if (pronto) tts.stop()
        val minhaVez = ++vez
        sequenciaDaFamilia = true
        falando = true
        seguir(plano, 0, minhaVez)
    }

    private fun seguir(plano: List<PassoDaFala>, i: Int, minhaVez: Int) {
        if (minhaVez != vez) return
        if (i >= plano.size) {
            sequenciaDaFamilia = false
            falando = false
            return
        }
        val proximo = { seguir(plano, i + 1, minhaVez) }
        falando = true
        when (val passo = plano[i]) {
            is PassoDaFala.Gravacao -> familia.tocar(passo.chave) { principal.post { proximo() } }
            is PassoDaFala.Voz -> when {
                // Motor de voz ainda abrindo: guarda o resto da fala e continua no onInit (nada se perde).
                !pronto -> planoPendente = Triple(plano, i, minhaVez)
                !falarAgora(passo.trechos, proximo) -> principal.post { proximo() }
            }
        }
    }

    /** Interrompe uma fala com gravação no meio (a gravação para e os passos seguintes não acontecem). */
    private fun pararFamilia() {
        vez++
        sequenciaDaFamilia = false
        planoPendente = null
        fimDoPasso.clear()
        familia.pararDeTocar()
    }

    /**
     * Grava uma frase num arquivo WAV com a mesma voz, velocidade e tom da narração
     * (usado no emulador do CI para conferir as falas). [aoTerminar] recebe se deu certo.
     */
    fun gravar(frase: String, ingles: Boolean, arquivo: File, aoTerminar: (Boolean) -> Unit) {
        if (!pronto) { aoTerminar(false); return }
        val id = "gravacao-${System.nanoTime()}"
        gravacoes[id] = aoTerminar
        configurar(ingles && inglesDisponivel)
        if (tts.synthesizeToFile(frase, Bundle(), arquivo, id) != TextToSpeech.SUCCESS) gravacoes.remove(id)?.invoke(false)
    }

    /** As frases que o narrador fala, na ordem, cada uma com o idioma (true = inglês). */
    fun frases(trechos: List<Trecho>): List<Pair<String, Boolean>> = trechos.flatMap { trecho ->
        val ingles = trecho.idioma == Idioma.EN && inglesDisponivel
        // Frases separadas por pontuação viram falas com pausinhas entre elas.
        trecho.texto.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }.map { it to ingles }
    }

    private fun configurar(ingles: Boolean) {
        val voz = if (ingles) vozEn else vozPt
        if (voz != null) runCatching { tts.voice = voz } else tts.setLanguage(if (ingles) Locale.US else portugues)
        tts.setSpeechRate(if (ingles) 0.75f else 0.88f)
        tts.setPitch(1.1f)
    }

    fun parar() {
        pendente = null
        pararFamilia()
        falando = false
        if (pronto) tts.stop()
    }

    fun desligar() {
        pararFamilia()
        familia.liberar()
        tts.stop()
        tts.shutdown()
    }
}
