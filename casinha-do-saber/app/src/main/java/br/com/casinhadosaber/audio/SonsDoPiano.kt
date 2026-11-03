package br.com.casinhadosaber.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import br.com.casinhadosaber.dominio.Nota
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * As teclas do piano do modo criativo, geradas no próprio app (sem arquivos de áudio e sem internet), como em
 * [Sons]: cada nota é calculada uma vez, fora da tela, e tocada em canais que se revezam (acordes e toques
 * rápidos se sobrepõem). Respeita o som ligado ou desligado de [sons].
 */
class SonsDoPiano(private val sons: Sons) {

    private val taxa = 22050
    private val trabalho = Executors.newSingleThreadExecutor()
    private val canais = arrayOfNulls<AudioTrack>(6)
    private var proximo = 0

    /** Som de caixinha de música: a fundamental com dois harmônicos que somem mais rápido. */
    private val pcm: Map<Nota, ShortArray> by lazy { Nota.entries.associateWith { nota(it.frequencia) } }

    init {
        trabalho.execute { runCatching { pcm.size } }
    }

    fun tocar(n: Nota) {
        if (!sons.ativo) return
        trabalho.execute {
            runCatching {
                val dados = pcm.getValue(n)
                val i = proximo
                proximo = (proximo + 1) % canais.size
                canais[i]?.release()
                val faixa = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(taxa)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(dados.size * 2)
                    .build()
                faixa.write(dados, 0, dados.size)
                faixa.play()
                canais[i] = faixa
            }
        }
    }

    private fun nota(freq: Double, ms: Int = 900, volume: Double = 0.32): ShortArray {
        val qtd = ms * taxa / 1000
        return ShortArray(qtd) { i ->
            val t = i.toDouble() / taxa
            val p = i.toDouble() / qtd
            val ataque = min(1.0, i / (taxa * 0.004))
            val onda = sin(2 * PI * freq * t) +
                0.35 * sin(4 * PI * freq * t) * exp(-6.0 * p) +
                0.15 * sin(6 * PI * freq * t) * exp(-10.0 * p)
            (onda / 1.5 * ataque * exp(-4.2 * p) * volume * Short.MAX_VALUE).toInt().toShort()
        }
    }

    fun desligar() {
        trabalho.execute { canais.forEach { runCatching { it?.release() } } }
        trabalho.shutdown()
    }
}
