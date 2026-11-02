package br.com.casinhadosaber.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Música de fundo suave e original (escala pentatônica, como caixinha de música),
 * gerada na hora e tocada em loop bem baixinho para não atrapalhar a voz.
 */
class Musica {

    private var trilha: AudioTrack? = null
    private var tocando = false

    @Synchronized
    fun tocar() {
        tocando = true
        val atual = trilha
        if (atual != null) {
            runCatching { atual.play() }
            return
        }
        thread(name = "musica") {
            val nova = runCatching { criar() }.getOrNull() ?: return@thread
            synchronized(this) {
                if (trilha == null) trilha = nova else nova.release()
                if (tocando) runCatching { trilha?.play() }
            }
        }
    }

    @Synchronized
    fun pausar() {
        tocando = false
        runCatching { trilha?.pause() }
    }

    @Synchronized
    fun liberar() {
        tocando = false
        runCatching { trilha?.stop() }
        trilha?.release()
        trilha = null
    }

    private fun criar(): AudioTrack {
        val taxa = 22050
        val batida = 0.6 // segundos
        // Notas: C4 D4 E4 G4 A4 C5 (índices 0..5); -1 = pausa
        val escala = doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25)
        val melodia = intArrayOf(
            2, 3, 4, 3, 2, 1, 0, 1,
            2, 3, 4, 5, 4, 3, 2, -1,
            3, 4, 5, 4, 3, 2, 1, 2,
            0, 1, 2, 3, 2, 1, 0, -1,
        )
        val baixos = doubleArrayOf(130.81, 98.00, 110.00, 98.00, 87.31, 130.81, 98.00, 130.81)
        val total = (melodia.size * batida * taxa).toInt()
        val mix = DoubleArray(total)
        fun nota(inicio: Int, freq: Double, dur: Double, vol: Double) {
            val n = (dur * taxa).toInt()
            for (i in 0 until n) {
                val idx = inicio + i
                if (idx >= total) break
                val t = i.toDouble() / taxa
                val env = min(1.0, i / (taxa * 0.01)) * exp(-2.8 * t / dur)
                mix[idx] += vol * env * (sin(2 * PI * freq * t) + 0.25 * sin(4 * PI * freq * t) + 0.1 * sin(6 * PI * freq * t))
            }
        }
        melodia.forEachIndexed { i, n ->
            if (n >= 0) nota((i * batida * taxa).toInt(), escala[n], batida * 1.6, 0.16)
        }
        baixos.forEachIndexed { i, f -> nota((i * 4 * batida * taxa).toInt(), f, batida * 4, 0.10) }
        val pcm = ShortArray(total) { (mix[it].coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.5).toInt().toShort() }

        val t = AudioTrack.Builder()
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
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        t.write(pcm, 0, pcm.size)
        t.setLoopPoints(0, pcm.size, -1)
        t.setVolume(0.35f)
        return t
    }
}
