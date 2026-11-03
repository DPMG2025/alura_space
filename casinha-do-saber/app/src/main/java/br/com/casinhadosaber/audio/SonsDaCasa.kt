package br.com.casinhadosaber.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Sons da casa por dentro (água, interruptor, porta da geladeira, TV, descarga, bolhas, nham nham),
 * gerados no próprio app como em [Sons]. Calculados uma vez, fora da tela, e tocados em canais que se
 * revezam. Respeita o som ligado/desligado de [sons].
 */
class SonsDaCasa(private val sons: Sons) {

    private val taxa = 22050
    private val trabalho = Executors.newSingleThreadExecutor()
    private val canais = arrayOfNulls<AudioTrack>(4)
    private var proximo = 0

    init {
        trabalho.execute { runCatching { listOf(aguaPcm, cliquePcm, portaPcm, tvPcm, descargaPcm, bolhasPcm, nhamPcm, passoPcm, chiadoPcm, pegarPcm, pousarPcm, quackPcm, tampaPcm) } }
    }

    fun agua() = tocar(aguaPcm)
    fun clique() = tocar(cliquePcm)
    fun porta() = tocar(portaPcm)
    fun tv() = tocar(tvPcm)
    fun descarga() = tocar(descargaPcm)
    fun bolhas() = tocar(bolhasPcm)
    fun nham() = tocar(nhamPcm)
    fun passo() = tocar(passoPcm, volumeVariado = true)
    fun chiado() = tocar(chiadoPcm)

    /** Levantou um móvel para arrastar (um "fiu" subindo). */
    fun pegar() = tocar(pegarPcm)

    /** Pousou o móvel no lugar novo (um "tum" macio de madeira). */
    fun pousar() = tocar(pousarPcm)

    /** Patinho de borracha: quac quac. */
    fun quack() = tocar(quackPcm)

    /** Tampa da lixeira abrindo e fechando. */
    fun tampa() = tocar(tampaPcm)

    // ----------------------------------------------------------------- síntese

    private fun tom(freq: Double, ms: Int, volume: Double, decaimento: Double = 3.0): ShortArray {
        val qtd = ms * taxa / 1000
        return ShortArray(qtd) { i ->
            val t = i.toDouble() / taxa
            val env = min(1.0, i / (taxa * 0.004)) * exp(-decaimento * i / qtd)
            ((sin(2 * PI * freq * t) + 0.25 * sin(4 * PI * freq * t)) / 1.25 * env * volume * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun ruido(ms: Int, volume: Double, semente: Int, corte: (Double) -> Double, envelope: (Double) -> Double): ShortArray {
        val qtd = ms * taxa / 1000
        val r = Random(semente)
        var f = 0.0
        return ShortArray(qtd) { i ->
            val p = i.toDouble() / qtd
            f += corte(p).coerceIn(0.01, 1.0) * ((r.nextDouble() * 2 - 1) - f)
            (f * envelope(p) * min(1.0, i / (taxa * 0.01)) * volume * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun deslizar(de: Double, ate: Double, ms: Int, volume: Double): ShortArray {
        val qtd = ms * taxa / 1000
        var fase = 0.0
        return ShortArray(qtd) { i ->
            val p = i.toDouble() / qtd
            fase += 2 * PI * (de + (ate - de) * p) / taxa
            (sin(fase) * min(1.0, i / (taxa * 0.004)) * exp(-2.5 * p) * volume * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun juntar(vararg partes: ShortArray): ShortArray {
        val out = ShortArray(partes.sumOf { it.size })
        var pos = 0
        for (p in partes) { p.copyInto(out, pos); pos += p.size }
        return out
    }

    private fun misturar(a: ShortArray, b: ShortArray, inicioB: Int = 0): ShortArray =
        ShortArray(maxOf(a.size, b.size + inicioB)) { i ->
            val vb = if (i >= inicioB) b.getOrElse(i - inicioB) { 0 }.toInt() else 0
            (a.getOrElse(i) { 0 }.toInt() + vb).coerceIn(-32767, 32767).toShort()
        }

    private fun silencio(ms: Int) = ShortArray(ms * taxa / 1000)

    /** Água da torneira: chiado suave que sobe e desce, com gotinhas. */
    private val aguaPcm by lazy {
        var base = ruido(1600, 0.22, 7, { 0.35 + 0.1 * sin(it * 40) }, { p -> if (p < 0.85) 1.0 else (1 - p) / 0.15 })
        for (k in 0 until 6) base = misturar(base, deslizar(900.0 + k * 130, 1500.0 + k * 90, 50, 0.08), (k * 4500 + 1200) % base.size)
        base
    }
    private val cliquePcm by lazy { misturar(ruido(18, 0.35, 3, { 0.9 }, { 1 - it }), tom(2400.0, 25, 0.12, 8.0)) }
    private val portaPcm by lazy { juntar(deslizar(260.0, 420.0, 140, 0.12), misturar(tom(90.0, 90, 0.4, 5.0), ruido(70, 0.15, 5, { 0.2 }, { 1 - it }))) }
    private val tvPcm by lazy { juntar(tom(784.0, 90, 0.16), tom(988.0, 90, 0.16), tom(1175.0, 90, 0.16), tom(1568.0, 220, 0.18)) }
    private val descargaPcm by lazy {
        misturar(ruido(1800, 0.3, 11, { p -> 0.5 - 0.35 * p }, { p -> if (p < 0.15) p / 0.15 else 1 - (p - 0.15) / 0.85 }), deslizar(500.0, 180.0, 900, 0.08), 6000)
    }
    private val bolhasPcm by lazy {
        juntar(*Array(7) { k -> juntar(deslizar(500.0 + (k * 173) % 400, 1100.0 + (k * 211) % 600, 55, 0.13), silencio(40 + (k * 37) % 60)) })
    }
    private val nhamPcm by lazy { juntar(deslizar(330.0, 260.0, 120, 0.2), silencio(60), deslizar(350.0, 270.0, 140, 0.2)) }
    private val passoPcm by lazy { misturar(tom(150.0, 40, 0.18, 9.0), ruido(30, 0.06, 9, { 0.3 }, { 1 - it })) }
    private val pegarPcm by lazy { deslizar(380.0, 760.0, 120, 0.14) }
    private val pousarPcm by lazy { misturar(tom(110.0, 140, 0.42, 6.0), misturar(ruido(60, 0.12, 17, { 0.25 }, { 1 - it }), tom(660.0, 70, 0.08, 7.0), 900)) }
    private val quackPcm by lazy {
        val q = { misturar(deslizar(760.0, 520.0, 110, 0.16), deslizar(1520.0, 1040.0, 110, 0.06)) }
        juntar(q(), silencio(70), q())
    }
    private val tampaPcm by lazy { juntar(tom(520.0, 50, 0.14, 6.0), silencio(160), misturar(tom(180.0, 80, 0.3, 6.0), ruido(40, 0.12, 19, { 0.5 }, { 1 - it }))) }
    private val chiadoPcm by lazy { ruido(700, 0.16, 13, { 0.8 }, { p -> if (p < 0.1) p * 10 else 1 - p }) }

    // ----------------------------------------------------------------- reprodução

    private fun tocar(pcm: ShortArray, volumeVariado: Boolean = false) {
        if (!sons.ativo) return
        trabalho.execute {
            runCatching {
                val i = proximo
                proximo = (proximo + 1) % canais.size
                canais[i]?.release()
                val faixa = AudioTrack.Builder()
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
                faixa.write(pcm, 0, pcm.size)
                if (volumeVariado) faixa.playbackRate = (taxa * (0.9 + Random.nextDouble() * 0.2)).toInt()
                faixa.play()
                canais[i] = faixa
            }
        }
    }

    fun desligar() {
        trabalho.execute { canais.forEach { runCatching { it?.release() } } }
        trabalho.shutdown()
    }
}
