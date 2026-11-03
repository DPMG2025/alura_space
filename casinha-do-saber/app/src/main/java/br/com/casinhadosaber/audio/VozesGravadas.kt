package br.com.casinhadosaber.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import br.com.casinhadosaber.dominio.ConjuntoDeVozes
import br.com.casinhadosaber.dominio.GuardaDasVozes
import java.io.File

/**
 * As vozes da família no aparelho: grava (AAC em .m4a, com MediaRecorder) e toca (MediaPlayer) as frases
 * de [br.com.casinhadosaber.dominio.VozesDaFamilia]. Os arquivos ficam em `filesDir/vozes_da_familia`
 * (pasta privada do app, fora do backup do Android) e nunca saem do aparelho.
 *
 * Gravar só acontece pela área dos pais, depois do portão de adulto e da permissão do microfone pedida
 * na hora em que o adulto toca em gravar. As telas da criança só tocam o que já foi gravado.
 */
class VozesGravadas(context: Context) {

    private val app = context.applicationContext
    private val principal = Handler(Looper.getMainLooper())
    val guarda by lazy { GuardaDasVozes(File(app.filesDir, PASTA)) }

    /** Muda a cada gravação nova, apagada ou troca de conjunto (a tela dos pais se redesenha). */
    var versao by mutableIntStateOf(0)
        private set

    /** A frase que está sendo gravada agora (conjunto e chave), ou null. */
    var gravando by mutableStateOf<Pair<ConjuntoDeVozes, String>?>(null)
        private set

    /** A frase que está tocando agora, ou null. */
    var tocando by mutableStateOf<String?>(null)
        private set

    private var gravador: MediaRecorder? = null
    private var inicioDaGravacao = 0L
    private var tocador: MediaPlayer? = null
    private var aoTerminarDeTocar: (() -> Unit)? = null

    fun paraTocar(): Set<String> = runCatching { guarda.paraTocar() }.getOrDefault(emptySet())

    fun gravadas(c: ConjuntoDeVozes): Set<String> = runCatching { guarda.gravadas(c) }.getOrDefault(emptySet())

    fun escolher(c: ConjuntoDeVozes) {
        guarda.escolher(c)
        versao++
    }

    fun ligar(sim: Boolean) {
        guarda.ligar(sim)
        versao++
    }

    // ------------------------------------------------------------------ gravar

    /** Começa a gravar a frase [chave] no conjunto [c] (no máximo [DURACAO_MAXIMA_MS]). Devolve se começou. */
    fun comecarGravacao(c: ConjuntoDeVozes, chave: String): Boolean {
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return false
        pararDeTocar()
        terminarGravacao(guardar = false)
        val destino = guarda.provisorio(c, chave)
        // Fica fora do runCatching para ser liberado se prepare() ou start() falharem (gravador ainda é null).
        var r: MediaRecorder? = null
        return runCatching {
            destino.parentFile?.mkdirs()
            destino.delete()
            @Suppress("DEPRECATION")
            r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(app) else MediaRecorder()
            val r = r!!
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioChannels(1)
            r.setAudioSamplingRate(44_100)
            r.setAudioEncodingBitRate(64_000)
            r.setMaxDuration(DURACAO_MAXIMA_MS)
            r.setOutputFile(destino.absolutePath)
            r.setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) principal.post { terminarGravacao(guardar = true) }
            }
            r.prepare()
            r.start()
            gravador = r
            inicioDaGravacao = System.currentTimeMillis()
            gravando = c to chave
            true
        }.getOrElse {
            runCatching { r?.release() }
            gravador = null
            guarda.descartar(c, chave)
            gravando = null
            false
        }
    }

    /**
     * Termina a gravação em andamento. Com [guardar], a gravação nova substitui a antiga (se durou pelo
     * menos [DURACAO_MINIMA_MS]); sem, é jogada fora e a antiga continua. Devolve se guardou.
     */
    fun terminarGravacao(guardar: Boolean = true): Boolean {
        val r = gravador ?: return false
        val (c, chave) = gravando ?: return false
        gravador = null
        gravando = null
        val durou = System.currentTimeMillis() - inicioDaGravacao
        val parouBem = runCatching { r.stop() }.isSuccess
        runCatching { r.release() }
        val ok = guardar && parouBem && durou >= DURACAO_MINIMA_MS && guarda.confirmar(c, chave)
        if (!ok) guarda.descartar(c, chave)
        versao++
        return ok
    }

    fun apagar(c: ConjuntoDeVozes, chave: String) {
        if (tocando == chave) pararDeTocar()
        guarda.apagar(c, chave)
        versao++
    }

    // ------------------------------------------------------------------ tocar

    /** Toca a gravação [chave] do conjunto [c] (o ativo, por padrão) e chama [aoTerminar] no fim (ou se falhar). */
    fun tocar(chave: String, c: ConjuntoDeVozes = guarda.ativo, aoTerminar: () -> Unit = {}) {
        // Gravando: nada toca, para o som não entrar no microfone (os botões "Ouvir" também ficam apagados).
        if (gravador != null) {
            aoTerminar()
            return
        }
        pararDeTocar()
        val arquivo = guarda.arquivo(c, chave)
        if (!arquivo.isFile) {
            aoTerminar()
            return
        }
        val p = MediaPlayer()
        val ok = runCatching {
            p.setDataSource(arquivo.absolutePath)
            p.setOnCompletionListener { terminou(it) }
            p.setOnErrorListener { mp, _, _ ->
                terminou(mp)
                true
            }
            p.prepare()
            p.start()
        }.isSuccess
        if (!ok) {
            runCatching { p.release() }
            aoTerminar()
            return
        }
        tocador = p
        tocando = chave
        aoTerminarDeTocar = aoTerminar
    }

    private fun terminou(p: MediaPlayer) {
        if (p !== tocador) {
            runCatching { p.release() }
            return
        }
        val fim = aoTerminarDeTocar
        aoTerminarDeTocar = null
        tocador = null
        tocando = null
        runCatching { p.release() }
        fim?.invoke()
    }

    /** Para o que estiver tocando, sem chamar o fim (quem parou já sabe). */
    fun pararDeTocar() {
        val p = tocador ?: return
        tocador = null
        tocando = null
        aoTerminarDeTocar = null
        runCatching { p.stop() }
        runCatching { p.release() }
    }

    fun liberar() {
        pararDeTocar()
        terminarGravacao(guardar = false)
    }

    companion object {
        const val PASTA = "vozes_da_familia"
        const val DURACAO_MAXIMA_MS = 8_000
        const val DURACAO_MINIMA_MS = 400L
    }
}
