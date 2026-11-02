package br.com.casinhadosaber.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Efeitos sonoros gerados na hora (sem arquivos de áudio): notas suaves,
 * baixas o bastante para não cobrir a voz do narrador.
 */
class Sons {

    private val fila = Executors.newSingleThreadExecutor()
    var ativo = true

    private data class Nota(val freq: Double, val ms: Int, val volume: Double = 0.30)

    fun acerto() = tocar(listOf(Nota(523.25, 90), Nota(659.25, 90), Nota(783.99, 220)))
    fun erro() = tocar(listOf(Nota(392.0, 110, 0.18), Nota(311.13, 220, 0.18)))
    fun toque() = tocar(listOf(Nota(880.0, 50, 0.15)))
    fun colocar() = tocar(listOf(Nota(659.25, 60, 0.22), Nota(987.77, 120, 0.22)))
    fun premio() = tocar(
        listOf(
            Nota(523.25, 110), Nota(659.25, 110), Nota(783.99, 110),
            Nota(1046.5, 160), Nota(783.99, 100), Nota(1046.5, 380),
        ),
    )

    private fun tocar(notas: List<Nota>) {
        if (!ativo) return
        fila.execute {
            runCatching { tocarAgora(notas) }
        }
    }

    private fun tocarAgora(notas: List<Nota>) {
        val taxa = 22050
        val amostras = notas.sumOf { it.ms * taxa / 1000 }
        val pcm = ShortArray(amostras)
        var pos = 0
        for (n in notas) {
            val qtd = n.ms * taxa / 1000
            for (i in 0 until qtd) {
                val t = i.toDouble() / taxa
                // ataque rápido e decaimento suave, com um harmônico para soar como sininho
                val envelope = min(1.0, i / (taxa * 0.008)) * exp(-3.0 * i / qtd)
                val onda = sin(2 * PI * n.freq * t) + 0.3 * sin(4 * PI * n.freq * t)
                pcm[pos + i] = (onda / 1.3 * envelope * n.volume * Short.MAX_VALUE).toInt().toShort()
            }
            pos += qtd
        }
        val trilha = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        try {
            trilha.write(pcm, 0, pcm.size)
            trilha.play()
            Thread.sleep(notas.sumOf { it.ms }.toLong() + 60)
        } finally {
            trilha.release()
        }
    }

    fun desligar() = fila.shutdownNow()
}
