package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.AcessorioMascote
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.Trilha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Fase 8, grupo A (entrada, mascote, descanso e conquistas): a arte que as telas pedem está no manifesto, as poses
 * de cada mascote trocam sem pular (mesma moldura e mesmo apoio), a mãozinha tem a ponta do dedo e os enfeites do
 * mascote caem dentro da imagem. JUnit puro (lê o manifesto versionado em `assets/arte/`).
 */
class PecasDoMascoteTest {

    private val pasta: File = listOf("src/main/assets/arte", "app/src/main/assets/arte")
        .map(::File).firstOrNull { it.isDirectory }
        ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")

    private val manifesto by lazy { ManifestoDaArte.ler(File(pasta, "manifesto.json").readText()) }

    @Test fun aArteDasTelasEstaToda() {
        val ids = PecasDoMascote.idsDaArte + PecasDasTelas.idsDaArte + IconesDaBarra.PAIS + IconesDaBarra.ALBUM
        val faltam = ids.filter { it !in manifesto.imagens }
        assertTrue("falta arte no manifesto: $faltam", faltam.isEmpty())
        for (id in ids) assertTrue("$id sem arquivo", File(pasta, manifesto.imagens.getValue(id).arquivo).isFile)
        assertEquals("mascote_gatinho_feliz", PecasDoMascote.id(EspecieMascote.GATINHO, PoseDoMascote.FELIZ))
        assertEquals("mascote_coruja_parada", PecasDoMascote.idDaCoruja(PoseDoMascote.PARADO))
        assertEquals("tela_figurinha_ingles", PecasDasTelas.figurinha(Trilha.INGLES, dourada = false, ganha = true))
        assertEquals(PecasDasTelas.FIGURINHA_DOURADA, PecasDasTelas.figurinha(Trilha.INGLES, dourada = true, ganha = true))
        assertEquals(PecasDasTelas.FIGURINHA_VAZIA, PecasDasTelas.figurinha(Trilha.LOGICA, dourada = true, ganha = false))
    }

    @Test fun posesTrocamSemPular() {
        val grupos = EspecieMascote.entries.map { e -> PoseDoMascote.entries.map { PecasDoMascote.id(e, it) } } +
            listOf(PoseDoMascote.entries.map(PecasDoMascote::idDaCoruja))
        for (ids in grupos) {
            val poses = ids.map(manifesto.imagens::getValue)
            val primeira = poses.first()
            for (p in poses) {
                assertEquals("${p.id}: largura", primeira.largura, p.largura)
                assertEquals("${p.id}: altura", primeira.altura, p.altura)
                val (ax, ay) = p.fracao(p.apoio)
                val (bx, by) = primeira.fracao(primeira.apoio)
                assertEquals("${p.id}: apoio x", bx, ax, 0.03f)
                assertEquals("${p.id}: apoio y", by, ay, 0.03f)
                assertTrue("${p.id} sem a âncora cabeca", "cabeca" in p.ancoras)
            }
        }
        // Os bichos têm o topo da cabeça (para os enfeites); os quadrados mantêm o desenho do mascote quadrado.
        for (e in EspecieMascote.entries) {
            val info = manifesto.imagens.getValue(PecasDoMascote.id(e, PoseDoMascote.PARADO))
            assertTrue("${info.id} sem topoDaCabeca", "topoDaCabeca" in info.ancoras)
            assertEquals("${info.id} quadrado", info.largura, info.altura)
        }
    }

    @Test fun enfeitesCaemNoBicho() {
        for (e in EspecieMascote.entries) {
            val info = manifesto.imagens.getValue(PecasDoMascote.id(e, PoseDoMascote.PARADO))
            val (_, pes) = info.fracao(info.apoio)
            val (_, cabeca) = info.fracao(info.ancoras.getValue("cabeca"))
            for (a in AcessorioMascote.entries) {
                val (x, y) = PecasDoMascote.lugarDoEnfeite(a, info)
                assertTrue("$a de $e fora da imagem ($x, $y)", x in 0f..1f && y in 0f..1f)
                assertTrue("$a de $e abaixo dos pés", y < pes)
            }
            // Chapéus em cima, cachecol e medalha embaixo da cabeça.
            assertTrue(PecasDoMascote.lugarDoEnfeite(AcessorioMascote.COROA, info).second < cabeca)
            assertTrue(PecasDoMascote.lugarDoEnfeite(AcessorioMascote.MEDALHA, info).second > cabeca)
        }
    }

    @Test fun maoTemAPontaDoDedo() {
        for (id in listOf(PecasDasTelas.MAO_TOQUE, PecasDasTelas.MAO_APERTANDO)) {
            val info = manifesto.imagens.getValue(id)
            val (x, y) = info.fracao(info.ancoras.getValue("ponta"))
            assertTrue("$id: ponta do dedo no alto da imagem ($x, $y)", y < 0.3f && x in 0.2f..0.8f)
        }
        val a = manifesto.imagens.getValue(PecasDasTelas.MAO_TOQUE)
        val b = manifesto.imagens.getValue(PecasDasTelas.MAO_APERTANDO)
        assertEquals("os dois quadros da mão no mesmo recorte", a.largura to a.altura, b.largura to b.altura)
        // As figurinhas: a janelinha (âncora centro) perto do meio, para o emoji ficar dentro dela.
        for (t in Trilha.entries) {
            val f = manifesto.imagens.getValue(PecasDasTelas.figurinha(t, dourada = false, ganha = true))
            val (cx, cy) = f.fracao(f.ancoras.getValue("centro"))
            assertTrue("${f.id}: centro ($cx, $cy)", cx in 0.4f..0.6f && cy in 0.4f..0.6f)
        }
    }

    @Test fun poseDeCadaMomento() {
        assertEquals(PoseDoMascote.PARADO, PoseDoMascote.de())
        assertEquals(PoseDoMascote.FELIZ, PoseDoMascote.de(feliz = true))
        assertEquals(PoseDoMascote.FALANDO, PoseDoMascote.de(feliz = true, falando = true))
        assertEquals(PoseDoMascote.DORMINDO, PoseDoMascote.de(falando = true, dormindo = true))
    }
}
