package br.com.casinhadosaber

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.VocabularioDosMundos
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

/**
 * Grava em WAV, com o Narrador do app (mesma voz, velocidade e tom), os sons esticados das fases
 * de som das letras e algumas falas típicas em português e inglês. O CI guarda os arquivos em
 * docs/emulador/vozes/ para o dono ouvir, com um relatório de voz, idioma, qualidade e duração.
 * Falha se algum arquivo vier vazio ou com duração absurda.
 */
@RunWith(AndroidJUnit4::class)
class VozesGravadasTest {

    private val contexto: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val armazenamento = TestStorage()

    /** Medidas de um WAV: duração total, trecho com som (sem os silêncios das pontas) e quantos blocos de som separados. */
    data class Medida(val taxa: Int, val total: Double, val comSom: Double, val blocos: Int)

    private fun medir(arquivo: File): Medida? {
        val bytes = arquivo.readBytes()
        if (bytes.size < 44) return null
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var canais = 1; var taxa = 0; var bits = 16
        var pos = 12
        var inicio = -1; var tamanho = 0
        while (pos + 8 <= bytes.size) {
            val id = String(bytes, pos, 4, Charsets.US_ASCII)
            val tam = b.getInt(pos + 4)
            if (id == "fmt ") { canais = b.getShort(pos + 10).toInt(); taxa = b.getInt(pos + 12); bits = b.getShort(pos + 22).toInt() }
            if (id == "data") {
                inicio = pos + 8
                tamanho = if (tam <= 0 || inicio + tam > bytes.size) bytes.size - inicio else tam
                break
            }
            pos += 8 + tam + (tam and 1)
        }
        if (inicio < 0 || taxa <= 0 || bits != 16) return null
        val amostras = tamanho / 2 / canais
        val total = amostras.toDouble() / taxa
        // Energia em janelas de 10 ms: acima de 4% do pico conta como som.
        val janela = taxa / 100
        val energias = (0 until amostras / janela).map { j ->
            var soma = 0.0
            for (k in 0 until janela) {
                val v = b.getShort(inicio + ((j * janela + k) * canais) * 2).toDouble()
                soma += v * v
            }
            sqrt(soma / janela)
        }
        val pico = energias.maxOrNull() ?: 0.0
        if (pico < 50) return Medida(taxa, total, 0.0, 0)
        val comSom = energias.map { it > pico * 0.04 }
        val primeiro = comSom.indexOfFirst { it }
        val ultimo = comSom.indexOfLast { it }
        // Blocos separados por pelo menos 120 ms de silêncio (um som soletrado vira vários blocos).
        var blocos = 0; var silencio = 99
        for (j in primeiro..ultimo) {
            if (comSom[j]) { if (silencio >= 12) blocos++; silencio = 0 } else silencio++
        }
        return Medida(taxa, total, (ultimo - primeiro + 1) / 100.0, blocos)
    }

    @Test
    fun gravarSonsEsticadosEFalasTipicas() {
        lateinit var narrador: Narrador
        InstrumentationRegistry.getInstrumentation().runOnMainSync { narrador = Narrador(contexto) }
        val limite = System.currentTimeMillis() + 30_000
        while (!narrador.estaPronto && System.currentTimeMillis() < limite) Thread.sleep(200)
        assertTrue("o motor de voz iniciou", narrador.estaPronto)
        Thread.sleep(500)

        // (nome do arquivo, fala, é som esticado?)
        val gravacoes = mutableListOf<Triple<String, Trecho, Boolean>>()
        for (s in VocabularioDosMundos.sonsDasLetras) {
            val letra = s.letra.lowercase()
            gravacoes += Triple("som_$letra", pt(s.som), true)
            gravacoes += Triple("som_${letra}_reforco", pt("${s.som}... ${s.palavra}!"), false)
        }
        val m = VocabularioDosMundos.sonsDasLetras.first()
        listOf(
            "pt_pergunta_som" to pt("Escute: ${m.som}. ${m.palavra}. Que letra faz o som ${m.som}?"),
            "pt_toque" to pt("Toque no número três."),
            "pt_parabens" to pt("Muito bem! Você acertou."),
            "pt_familia" to pt("A família do bê: ba, be, bi, bo, bu. Toque no bo."),
            "en_apple" to en("Apple"),
            "en_i_like" to en("I like the red ball."),
            "en_cat" to en("Cat"),
        ).forEach { (nome, t) -> gravacoes += Triple(nome, t, false) }

        val relatorio = StringBuilder()
        relatorio.appendLine("Vozes do narrador gravadas no emulador (Android API ${android.os.Build.VERSION.SDK_INT})")
        relatorio.appendLine("Vozes escolhidas: ${narrador.descricaoDasVozes()}")
        relatorio.appendLine("Vozes de boa qualidade (400 ou mais): ${narrador.vozesBoas}")
        relatorio.appendLine("Arquivo | idioma | duração total | com som | blocos | texto")
        val problemas = mutableListOf<String>()
        val pasta = File(contexto.cacheDir, "vozes").apply { mkdirs() }

        for ((nome, trecho, esticado) in gravacoes) {
            // A fala inteira como o narrador fala: frase por frase, com o mesmo idioma.
            val frases = narrador.frases(listOf(trecho))
            val partes = frases.mapIndexed { i, (frase, ingles) ->
                val arquivo = File(pasta, "$nome-$i.wav").apply { delete() }
                val terminou = CountDownLatch(1)
                var ok = false
                narrador.gravar(frase, ingles, arquivo) { ok = it; terminou.countDown() }
                terminou.await(30, TimeUnit.SECONDS)
                if (!ok) problemas += "$nome: o motor não gravou \"$frase\""
                arquivo
            }
            val juntado = juntar(partes, File(pasta, "$nome.wav"))
            val medida = juntado?.let { medir(it) }
            val idioma = if (frases.any { it.second }) "en-US" else "pt-BR"
            if (medida == null || medida.total <= 0.0 || medida.comSom <= 0.0) {
                problemas += "$nome: arquivo vazio ou sem som"
                relatorio.appendLine("$nome.wav | $idioma | VAZIO | | | ${trecho.texto}")
                continue
            }
            juntado!!.inputStream().use { entrada -> armazenamento.openOutputFile("$nome.wav").use { entrada.copyTo(it) } }
            relatorio.appendLine("$nome.wav | $idioma | %.2f s | %.2f s | ${medida.blocos} | ${trecho.texto}".format(medida.total, medida.comSom))
            val maximo = if (esticado) 6.0 else 15.0
            if (medida.comSom < 0.3 || medida.total > maximo) problemas += "$nome: duração absurda (%.2f s com som, %.2f s no total)".format(medida.comSom, medida.total)
        }
        relatorio.appendLine()
        relatorio.appendLine("Critério: nenhum arquivo vazio; som esticado com pelo menos 0,3 s de som e no máximo 6 s; frases até 15 s.")
        relatorio.appendLine("Blocos: trechos de som separados por silêncio de 120 ms ou mais (um som esticado bom é 1 bloco).")
        relatorio.appendLine(if (problemas.isEmpty()) "RESULTADO: OK" else "PROBLEMAS:\n" + problemas.joinToString("\n"))
        armazenamento.openOutputFile("vozes_relatorio.txt").use { it.write(relatorio.toString().toByteArray()) }
        InstrumentationRegistry.getInstrumentation().runOnMainSync { narrador.desligar() }
        assertTrue("problemas nas vozes: $problemas", problemas.isEmpty())
    }

    /** Junta os WAV das frases (mesmo formato) num só, com 0,22 s de silêncio entre eles, como o narrador fala. */
    private fun juntar(partes: List<File>, destino: File): File? {
        val validas = partes.filter { it.length() > 44 }
        if (validas.isEmpty()) return null
        if (validas.size == 1) return validas[0].copyTo(destino, overwrite = true)
        val primeiro = validas[0].readBytes()
        val b = ByteBuffer.wrap(primeiro).order(ByteOrder.LITTLE_ENDIAN)
        val taxa = b.getInt(24); val canais = b.getShort(22).toInt()
        val pausa = ByteArray(taxa * 22 / 100 * 2 * canais)
        val dados = java.io.ByteArrayOutputStream()
        validas.forEachIndexed { i, f ->
            if (i > 0) dados.write(pausa)
            dados.write(dadosDe(f.readBytes()))
        }
        val corpo = dados.toByteArray()
        val cab = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        cab.put("RIFF".toByteArray()).putInt(36 + corpo.size).put("WAVE".toByteArray())
        cab.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(canais.toShort()).putInt(taxa)
            .putInt(taxa * canais * 2).putShort((canais * 2).toShort()).putShort(16)
        cab.put("data".toByteArray()).putInt(corpo.size)
        destino.outputStream().use { it.write(cab.array()); it.write(corpo) }
        return destino
    }

    private fun dadosDe(bytes: ByteArray): ByteArray {
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var pos = 12
        while (pos + 8 <= bytes.size) {
            val id = String(bytes, pos, 4, Charsets.US_ASCII)
            val tam = b.getInt(pos + 4)
            if (id == "data") {
                val fim = if (tam <= 0 || pos + 8 + tam > bytes.size) bytes.size else pos + 8 + tam
                return bytes.copyOfRange(pos + 8, fim)
            }
            pos += 8 + tam + (tam and 1)
        }
        return ByteArray(0)
    }
}
