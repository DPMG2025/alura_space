package br.com.casinhadosaber.ui.componentes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Confere a arte 3D versionada em `assets/arte/` contra o manifesto: toda imagem listada existe,
 * cabe no orçamento de tamanho e tem as medidas declaradas; nenhum arquivo sobra sem estar no manifesto.
 * Quem gera os arquivos é `arte3d/` (`npm run render`).
 */
class ManifestoDaArteTest {

    companion object {
        /** Orçamento por imagem (WebP com perda, qualidade 85, no dobro do tamanho de uso). */
        const val ORCAMENTO_POR_IMAGEM = 400L * 1024
        /** Orçamento da pasta inteira; o APK todo pode ter até 150 MB (PRD, seção 7). */
        const val ORCAMENTO_TOTAL = 40L * 1024 * 1024
    }

    /** A pasta de assets da arte; o Gradle roda os testes na pasta do módulo (app/). */
    private val pasta: File = listOf("src/main/assets/arte", "app/src/main/assets/arte")
        .map(::File).firstOrNull { it.isDirectory }
        ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")

    private val manifesto by lazy { ManifestoDaArte.ler(File(pasta, "manifesto.json").readText()) }

    @Test fun manifestoTemImagens() {
        assertEquals(1, manifesto.versao)
        assertTrue("o manifesto não pode estar vazio", manifesto.imagens.isNotEmpty())
    }

    @Test fun todaImagemDoManifestoExisteECabeNoOrcamento() {
        for (info in manifesto.imagens.values) {
            val f = File(pasta, info.arquivo)
            assertTrue("${info.id}: falta o arquivo ${info.arquivo}", f.isFile)
            assertTrue(
                "${info.id}: ${f.length() / 1024} KB passa do orçamento de ${ORCAMENTO_POR_IMAGEM / 1024} KB",
                f.length() <= ORCAMENTO_POR_IMAGEM,
            )
        }
        val total = pasta.listFiles().orEmpty().sumOf { it.length() }
        assertTrue("a arte soma ${total / 1024} KB, acima de ${ORCAMENTO_TOTAL / 1024} KB", total <= ORCAMENTO_TOTAL)
    }

    @Test fun nenhumArquivoOrfao() {
        val listados = manifesto.imagens.values.map { it.arquivo }.toSet() + "manifesto.json"
        val orfaos = pasta.listFiles().orEmpty().map { it.name }.filter { it !in listados }
        assertTrue("arquivos em assets/arte fora do manifesto: $orfaos", orfaos.isEmpty())
        assertEquals("dois ids apontam para o mesmo arquivo", manifesto.imagens.size, listados.size - 1)
    }

    @Test fun medidasBatemComOArquivoEPontosFicamDentroDaImagem() {
        for (info in manifesto.imagens.values) {
            val f = File(pasta, info.arquivo)
            if (!f.isFile) continue // já acusado no teste acima
            assertTrue("${info.id}: só WebP", info.arquivo.endsWith(".webp"))
            val medidas = tamanhoDoWebp(f.readBytes())
            assertNotNull("${info.id}: não é um WebP válido", medidas)
            val (l, a) = medidas!!
            assertEquals("${info.id}: largura do arquivo", info.largura, l)
            assertEquals("${info.id}: altura do arquivo", info.altura, a)
            assertEquals("${info.id}: largura = uso x escala", info.usoLargura * manifesto.escala, info.largura)
            assertEquals("${info.id}: altura = uso x escala", info.usoAltura * manifesto.escala, info.altura)
            // Na câmera isométrica (casa por dentro) o apoio é a referência da grade (iso.referencia projetada):
            // nas cascas dos cômodos (piso, parede) ela pode cair fora do recorte justo, e o app só a usa para
            // posicionar a imagem. As âncoras continuam dentro.
            val pontos = if (info.tipo == "isometrico") info.ancoras else info.ancoras + ("apoio" to info.apoio)
            for ((nome, p) in pontos) {
                assertTrue("${info.id}: '$nome' fora da imagem: $p", p.x in 0..info.largura && p.y in 0..info.altura)
            }
        }
    }

    @Test fun leitorJsonEntendeOFormato() {
        val m = ManifestoDaArte.ler(
            """
            {"versao": 1, "escala": 2, "extra": [true, false, null, -1.5e2, "a\"bé"],
             "imagens": {"x": {"arquivo": "x.webp", "tipo": "item", "largura": 20, "altura": 10,
               "uso": {"largura": 10, "altura": 5}, "apoio": {"x": 10, "y": 9}, "ancoras": {"mao": {"x": 3, "y": 4}}}}}
            """.trimIndent(),
        )
        val x = m.imagens["x"] ?: error("faltou a imagem x")
        assertEquals(PontoDaArte(3, 4), x.ancoras["mao"])
        assertEquals(0.5f to 0.9f, x.fracao(x.apoio))
        assertEquals(listOf(true, false, null, -150.0, "a\"bé"), (LeitorJson("""[true,false,null,-1.5e2,"a\"bé"]""").ler()))
    }

    @Test fun camposDasFases4E6TemPadraoVazioESaoLidos() {
        val m = ManifestoDaArte.ler(
            """
            {"versao": 1, "escala": 2, "imagens": {
             "fig": {"arquivo": "fig.webp", "tipo": "item", "largura": 4, "altura": 4, "uso": {"largura": 2, "altura": 2},
               "apoio": {"x": 2, "y": 3}, "ancoras": {}, "emojis": ["\ud83d\udc1d", "x"], "lugar": "ceu"},
             "dentro": {"arquivo": "dentro.webp", "tipo": "isometrico", "largura": 4, "altura": 4, "uso": {"largura": 2, "altura": 2},
               "apoio": {"x": 2, "y": 3}, "ancoras": {}, "iso": {"ladrilho": 64, "movel": "cama", "referencia": {"x": 1.5, "y": 2, "z": 0}}}}}
            """.trimIndent(),
        )
        val fig = m.imagens.getValue("fig")
        assertEquals(listOf("\uD83D\uDC1D", "x"), fig.emojis)
        assertEquals("ceu", fig.lugar)
        assertTrue(fig.iso.isEmpty())
        val dentro = m.imagens.getValue("dentro")
        assertTrue(dentro.emojis.isEmpty())
        assertEquals("", dentro.lugar)
        assertEquals(64.0, dentro.numeroIso("ladrilho"))
        assertEquals(1.5, dentro.numeroIso("referencia.x"))
        assertEquals("cama", dentro.textoIso("movel"))
        assertEquals(null, dentro.numeroIso("pegada.x0"))
    }

    @Test fun reducaoEscolheAMaiorPotenciaDeDoisQueNaoFicaMenorQueOAlvo() {
        assertEquals(1, amostraDaArte(240, 240, 240, 240))
        assertEquals(1, amostraDaArte(240, 240, 200, 200))
        assertEquals(2, amostraDaArte(240, 240, 80, 80))
        assertEquals(4, amostraDaArte(240, 240, 60, 60))
        assertEquals(1, amostraDaArte(240, 240, 400, 400))
        assertTrue(amostraDaArte(240, 240, 0, 0) >= 1)
    }

    /** Largura e altura de um WebP (VP8X, VP8L ou VP8), lidas do cabeçalho; nulo se não for WebP. */
    private fun tamanhoDoWebp(b: ByteArray): Pair<Int, Int>? {
        fun u(i: Int) = b[i].toInt() and 0xFF
        fun txt(i: Int) = String(b, i, 4, Charsets.US_ASCII)
        if (b.size < 30 || txt(0) != "RIFF" || txt(8) != "WEBP") return null
        return when (txt(12)) {
            "VP8X" -> (1 + (u(24) or (u(25) shl 8) or (u(26) shl 16))) to (1 + (u(27) or (u(28) shl 8) or (u(29) shl 16)))
            "VP8L" -> {
                val bits = u(21) or (u(22) shl 8) or (u(23) shl 16) or (u(24) shl 24)
                (1 + (bits and 0x3FFF)) to (1 + ((bits shr 14) and 0x3FFF))
            }
            "VP8 " -> ((u(26) or (u(27) shl 8)) and 0x3FFF) to ((u(28) or (u(29) shl 8)) and 0x3FFF)
            else -> null
        }
    }
}
