package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.Visual
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier
import java.util.IdentityHashMap
import kotlin.random.Random

/**
 * Cobertura das figuras 3D (Fase 9 do PRD visual 3D, "um teste que lista quais emojis do vocabulário já têm
 * modelo"). Sorteia as fases 1 a 100 dos quatro caminhos com [SORTEIOS] sementes fixas, como a contagem da fase 4A,
 * e conta em quantas perguntas cada emoji aparece (na cena, nas opções e na atividade; a fala não entra).
 *
 * Grava o relatório em `build/relatorios/cobertura-emojis.md` (nada no repositório) e imprime o resumo. Só falha
 * se a parte das aparições coberta por um modelo (`fig_*` ou reserva) cair abaixo de [MINIMO_COBERTO].
 */
class CoberturaDeEmojisTest {

    private val pastaDoApp: File by lazy {
        listOf("src/main/assets/arte", "app/src/main/assets/arte").map { File(it).absoluteFile }.firstOrNull { it.isDirectory }
            ?.parentFile?.parentFile?.parentFile?.parentFile
            ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")
    }

    private val manifesto by lazy {
        ManifestoDaArte.ler(File(pastaDoApp, "src/main/assets/arte/manifesto.json").readText())
    }

    @Test
    fun emojisDoVocabularioComModelo3D() {
        val presencas = HashMap<String, Int>()
        var perguntas = 0
        for (semente in 0 until SORTEIOS) {
            val gerador = GeradorDeFases(Random(semente))
            for (trilha in Trilha.entries) {
                for (fase in gerador.fases(trilha).take(FASES)) {
                    for (p in gerador.perguntas(fase)) {
                        perguntas++
                        for (e in emojisDa(p)) presencas[e] = (presencas[e] ?: 0) + 1
                    }
                }
            }
        }
        assertTrue("o sorteio não achou emojis", presencas.size > 50)

        val comModelo = presencas.filterKeys { FigurasDaPergunta.temModelo(manifesto, it) }
        val semModelo = presencas.filterKeys { it !in comModelo }
        val total = presencas.values.sum()
        val coberto = comModelo.values.sum()
        val percentual = 100.0 * coberto / total
        val figurasUsadas = comModelo.keys.mapNotNull { FigurasDaPergunta.idPara(manifesto, it) }.toSet()
        val figurasNoManifesto = manifesto.imagens.keys.count { it.startsWith(FigurasDaPergunta.PREFIXO) }

        val maisUsados = semModelo.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        val texto = buildString {
            appendLine("# Cobertura dos emojis das perguntas por figuras 3D")
            appendLine()
            appendLine("Gerado pelo `CoberturaDeEmojisTest`: $SORTEIOS sorteios das fases 1 a $FASES dos quatro caminhos")
            appendLine("($perguntas perguntas). Aparição = uma pergunta em que o emoji aparece (cena, opções ou atividade).")
            appendLine()
            appendLine("| | |")
            appendLine("|---|---|")
            appendLine("| Emojis diferentes | ${presencas.size} |")
            appendLine("| Com modelo 3D (`fig_*` ou reserva) | ${comModelo.size} (${figurasUsadas.size} imagens; $figurasNoManifesto `fig_*` no manifesto) |")
            appendLine("| Sem modelo | ${semModelo.size} |")
            appendLine("| Aparições | $total |")
            appendLine("| Aparições cobertas | $coberto (${"%.1f".format(java.util.Locale.ROOT, percentual)} %) |")
            appendLine()
            appendLine("## Os $TOP_SEM_MODELO mais frequentes sem modelo")
            appendLine()
            appendLine("| # | emoji | código | aparições |")
            appendLine("|---|---|---|---|")
            maisUsados.take(TOP_SEM_MODELO).forEachIndexed { i, (e, n) -> appendLine("| ${i + 1} | $e | ${codigo(e)} | $n |") }
            appendLine()
            appendLine("## Com modelo (aparições)")
            appendLine()
            appendLine(
                comModelo.entries.sortedByDescending { it.value }
                    .joinToString(" · ") { (e, n) -> "$e ${FigurasDaPergunta.idPara(manifesto, e)} $n" },
            )
            appendLine()
            appendLine("## Todos os sem modelo (aparições)")
            appendLine()
            appendLine(maisUsados.joinToString(" · ") { (e, n) -> "$e $n" })
        }
        val saida = File(pastaDoApp, "build/relatorios/cobertura-emojis.md")
        saida.parentFile.mkdirs()
        saida.writeText(texto)
        println(
            "Cobertura de emojis: ${presencas.size} diferentes, ${comModelo.size} com modelo, " +
                "${"%.1f".format(java.util.Locale.ROOT, percentual)} % das aparições cobertas. Relatório: ${saida.absolutePath}",
        )

        assertTrue(
            "só ${"%.1f".format(java.util.Locale.ROOT, percentual)} % das aparições de emoji têm modelo 3D (mínimo $MINIMO_COBERTO %)",
            percentual >= MINIMO_COBERTO,
        )
    }

    /** Emojis (normalizados, sem U+FE0F) que a criança vê na pergunta: cena, opções e atividade, sem a fala. */
    private fun emojisDa(p: Pergunta): Set<String> {
        val achados = HashSet<String>()
        val vistos = IdentityHashMap<Any, Unit>()
        fun visitar(o: Any?) {
            when (o) {
                null, is Trecho, is Enum<*>, is Number, is Boolean, is Char -> return
                is String -> { for (e in emojisDoTexto(o)) achados += FigurasDaPergunta.normalizar(e); return }
                is Pair<*, *> -> { visitar(o.first); visitar(o.second); return }
                is Map<*, *> -> { o.forEach { (k, v) -> visitar(k); visitar(v) }; return }
                is Iterable<*> -> { o.forEach { visitar(it) }; return }
                is Array<*> -> { o.forEach { visitar(it) }; return }
            }
            if (vistos.put(o, Unit) != null) return
            // Só as classes do domínio (cena, opções, atividades e as peças delas).
            var classe: Class<*>? = o.javaClass
            if (!classe!!.name.startsWith("br.com.casinhadosaber.dominio")) return
            while (classe != null && classe != Any::class.java) {
                for (campo in classe.declaredFields) {
                    if (Modifier.isStatic(campo.modifiers) || campo.isSynthetic) continue
                    campo.isAccessible = true
                    visitar(campo.get(o))
                }
                classe = classe.superclass
            }
        }
        visitar(p.cena)
        // Pergunta de mexer: a única opção é o "✔" de enfeite (Pergunta.deAtividade), que a criança não vê.
        val soEnfeite = p.atividade !is Atividade.Escolha && p.opcoes.size == 1 && p.opcoes[0].visual is Visual.Texto
        if (!soEnfeite) visitar(p.opcoes)
        visitar(p.atividade)
        return achados
    }

    private fun codigo(e: String) = e.codePoints().toArray().joinToString(" ") { "U+%04X".format(it) }

    companion object {
        const val SORTEIOS = 12
        const val FASES = 100
        const val TOP_SEM_MODELO = 40
        /** Guarda contra regressão: a fase 4A cobria 57 % das aparições. */
        const val MINIMO_COBERTO = 50.0

        private val SIMBOLOS_DE_TEXTO = setOf(0x2192, 0x2212, 0x2605, 0x232B, 0x25CF, 0x25B2, 0x25A0, 0x25AC)

        private fun eBase(cp: Int) = cp >= 0x1F000 && cp !in 0x1F3FB..0x1F3FF && cp < 0xE0000 ||
            cp in 0x2190..0x2BFF && cp !in SIMBOLOS_DE_TEXTO || cp == 0x3030 || cp == 0x303D || cp == 0x3297 || cp == 0x3299 ||
            cp == 0x00A9 || cp == 0x00AE || cp == 0x203C || cp == 0x2049 || cp == 0x2122 || cp == 0x2139

        private fun eJunta(cp: Int) = cp == 0xFE0F || cp == 0x20E3 || cp in 0x1F3FB..0x1F3FF || cp in 0xE0020..0xE007F

        /**
         * Os emojis de um texto, com o mesmo critério da tarefa `listarEmojis` do build (que confere a fonte
         * embutida): sequências com U+200D, tons de pele, U+FE0F e teclas (1️⃣) ficam inteiras; os símbolos de
         * texto (→ − ★ ⌫ ● ▲ ■ ▬), letras e números ficam de fora.
         */
        fun emojisDoTexto(texto: String): List<String> {
            val cps = texto.codePoints().toArray()
            val achados = ArrayList<String>()
            var i = 0
            while (i < cps.size) {
                val cp = cps[i]
                val tecla = (cp in 0x30..0x39 || cp == 0x23 || cp == 0x2A) && i + 2 < cps.size && cps[i + 1] == 0xFE0F && cps[i + 2] == 0x20E3
                if (!eBase(cp) && !tecla) { i++; continue }
                val seq = StringBuilder().appendCodePoint(cp)
                var j = i + 1
                while (j < cps.size) {
                    val c = cps[j]
                    if (eJunta(c)) { seq.appendCodePoint(c); j++ }
                    else if (c == 0x200D && j + 1 < cps.size && eBase(cps[j + 1])) { seq.appendCodePoint(c).appendCodePoint(cps[j + 1]); j += 2 }
                    else break
                }
                achados += seq.toString()
                i = j
            }
            return achados
        }
    }
}
