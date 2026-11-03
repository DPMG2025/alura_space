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
 * Efeitos sonoros gerados no próprio app (sem arquivos de áudio).
 *
 * Cada efeito é calculado uma vez só e tocado por um conjunto de canais que se revezam:
 * toques rápidos se sobrepõem na hora, sem fila e sem atraso. A altura varia um pouquinho
 * a cada toque, para não cansar o ouvido.
 */
class Sons {

    private val taxa = 22050
    private val trabalho = Executors.newSingleThreadExecutor()
    private val canais = arrayOfNulls<AudioTrack>(6)
    private var proximoCanal = 0
    var ativo = true

    init {
        // Calcula todos os sons logo no início, fora da tela, para o primeiro toque já sair na hora.
        trabalho.execute {
            runCatching { listOf(acertoPcm, erroPcm, toquePcm, colocarPcm, premioPcm, pegarPcm, pousarPcm, moedaPcm, chacoalharPcm, zumPcm, piuPcm) }
        }
    }

    // ----------------------------------------------------------------- efeitos (públicos)

    fun acerto() = tocar(acertoPcm)
    fun erro() = tocar(erroPcm, variar = false)
    fun toque() = tocar(toquePcm)
    fun colocar() = tocar(colocarPcm)
    fun premio() = tocar(premioPcm, variar = false)

    /** Pegar um objeto (pop curtinho). */
    fun pegar() = tocar(pegarPcm)

    /** Objeto pousando no chão (baque fofo). */
    fun pousar() = tocar(pousarPcm)

    /** Moedinha (dois sininhos agudos). */
    fun moeda() = tocar(moedaPcm)

    /** Presente chacoalhando. */
    fun chacoalhar() = tocar(chacoalharPcm)

    /** Algo caindo do céu e se encaixando (o telhado, as paredes). */
    fun zum() = tocar(zumPcm, variar = false)

    /** Piadinha de bichinho (cada visitante com um tom). */
    fun piu(tom: Int = 0) = tocar(piuPcm[tom.mod(piuPcm.size)])

    // ----------------------------------------------------------------- síntese

    private class Nota(val freq: Double, val ms: Int, val volume: Double = 0.30)

    private fun notas(vararg n: Nota): ShortArray {
        val total = n.sumOf { it.ms * taxa / 1000 }
        val pcm = ShortArray(total)
        var pos = 0
        for (nota in n) {
            val qtd = nota.ms * taxa / 1000
            for (i in 0 until qtd) {
                val t = i.toDouble() / taxa
                val env = min(1.0, i / (taxa * 0.006)) * exp(-3.0 * i / qtd)
                val onda = sin(2 * PI * nota.freq * t) + 0.3 * sin(4 * PI * nota.freq * t)
                pcm[pos + i] = (onda / 1.3 * env * nota.volume * Short.MAX_VALUE).toInt().toShort()
            }
            pos += qtd
        }
        return pcm
    }

    /** Ruído com decaimento e filtro passa-baixa simples (cutoff de 0 a 1). */
    private fun ruido(ms: Int, volume: Double, corte: (Double) -> Double, decaimento: Double = 6.0): ShortArray {
        val qtd = ms * taxa / 1000
        val r = Random(ms)
        var filtrado = 0.0
        return ShortArray(qtd) { i ->
            val p = i.toDouble() / qtd
            val a = corte(p).coerceIn(0.01, 1.0)
            filtrado += a * ((r.nextDouble() * 2 - 1) - filtrado)
            val env = min(1.0, i / (taxa * 0.002)) * exp(-decaimento * p)
            (filtrado * env * volume * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun deslizar(de: Double, ate: Double, ms: Int, volume: Double): ShortArray {
        val qtd = ms * taxa / 1000
        var fase = 0.0
        return ShortArray(qtd) { i ->
            val p = i.toDouble() / qtd
            fase += 2 * PI * (de + (ate - de) * p) / taxa
            val env = min(1.0, i / (taxa * 0.004)) * exp(-3.0 * p)
            (sin(fase) * env * volume * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun juntar(vararg partes: ShortArray): ShortArray {
        val out = ShortArray(partes.sumOf { it.size })
        var pos = 0
        for (p in partes) { p.copyInto(out, pos); pos += p.size }
        return out
    }

    private fun misturar(a: ShortArray, b: ShortArray): ShortArray =
        ShortArray(maxOf(a.size, b.size)) { i ->
            ((a.getOrElse(i) { 0 }.toInt() + b.getOrElse(i) { 0 }.toInt()).coerceIn(-32767, 32767)).toShort()
        }

    private val acertoPcm by lazy { notas(Nota(523.25, 80), Nota(659.25, 80), Nota(783.99, 200)) }
    private val erroPcm by lazy { notas(Nota(392.0, 110, 0.18), Nota(311.13, 200, 0.18)) }
    private val toquePcm by lazy { notas(Nota(880.0, 45, 0.14)) }
    private val colocarPcm by lazy { notas(Nota(659.25, 55, 0.22), Nota(987.77, 110, 0.22)) }
    private val premioPcm by lazy {
        notas(Nota(523.25, 100), Nota(659.25, 100), Nota(783.99, 100), Nota(1046.5, 150), Nota(783.99, 90), Nota(1046.5, 360))
    }
    private val pegarPcm by lazy { misturar(ruido(25, 0.25, { 0.6 }, 9.0), notas(Nota(1200.0, 30, 0.12))) }
    private val pousarPcm by lazy { misturar(notas(Nota(80.0, 70, 0.55)), ruido(60, 0.12, { 0.15 }, 8.0)) }
    private val moedaPcm by lazy { misturar(notas(Nota(1568.0, 300, 0.16)), juntar(ShortArray(taxa * 60 / 1000), notas(Nota(2093.0, 260, 0.14)))) }
    private val chacoalharPcm by lazy {
        val pausa = ShortArray(taxa * 40 / 1000)
        juntar(ruido(45, 0.3, { 0.5 }, 7.0), pausa, ruido(45, 0.3, { 0.5 }, 7.0), pausa, ruido(45, 0.3, { 0.5 }, 7.0))
    }
    private val zumPcm by lazy { misturar(ruido(380, 0.28, { p -> 0.05 + 0.5 * p }, 2.5), juntar(ShortArray(taxa * 330 / 1000), pousarPcm)) }
    private val piuPcm by lazy {
        listOf(1800.0 to 2600.0, 900.0 to 1300.0, 1300.0 to 1000.0, 2200.0 to 3000.0, 600.0 to 900.0, 1500.0 to 2100.0)
            .map { (a, b) -> juntar(deslizar(a, b, 90, 0.18), deslizar(a * 1.1, b * 1.1, 80, 0.14)) }
    }

    // ----------------------------------------------------------------- reprodução

    private fun tocar(pcm: ShortArray, variar: Boolean = true) {
        if (!ativo) return
        trabalho.execute {
            runCatching {
                val i = proximoCanal
                proximoCanal = (proximoCanal + 1) % canais.size
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
                // Variação de ±4% na altura: o mesmo som nunca soa igualzinho.
                if (variar) faixa.playbackRate = (taxa * (0.96 + Random.nextDouble() * 0.08)).toInt()
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
