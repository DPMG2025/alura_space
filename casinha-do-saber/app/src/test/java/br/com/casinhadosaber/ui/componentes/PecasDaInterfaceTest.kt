package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Trilha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Peças do visual brinquedo 3D sem Android: ícones da barra, ícones dos caminhos, contas e estrelas. */
class PecasDaInterfaceTest {

    private val manifesto by lazy {
        val pasta = listOf("src/main/assets/arte", "app/src/main/assets/arte").map(::File).first { it.isDirectory }
        ManifestoDaArte.ler(File(pasta, "manifesto.json").readText())
    }

    @Test fun emojisDaBarraViramIcones() {
        assertEquals(IconeDaBarra(IconesDaBarra.FECHAR), IconesDaBarra.para("✖️"))
        assertEquals(IconeDaBarra(IconesDaBarra.FECHAR), IconesDaBarra.para("✖"))
        assertEquals(IconeDaBarra(IconesDaBarra.VOLTAR), IconesDaBarra.para("⬅️"))
        assertEquals(IconeDaBarra(IconesDaBarra.VOLTAR), IconesDaBarra.para("◀️"))
        assertEquals(IconeDaBarra(IconesDaBarra.VOLTAR, espelhado = true), IconesDaBarra.para("▶️", "Próxima fase"))
        assertEquals(IconeDaBarra(IconesDaBarra.SOM_LIGADO), IconesDaBarra.para("🔊"))
        assertEquals(IconeDaBarra(IconesDaBarra.SOM_DESLIGADO), IconesDaBarra.para("🔇"))
        assertEquals(IconeDaBarra(IconesDaBarra.AJUSTES), IconesDaBarra.para("⚙️"))
        assertEquals(IconeDaBarra(IconesDaBarra.LOJA), IconesDaBarra.para("🛒"))
        // "▶" de ouvir é o "play", não avançar; emojis sem arte continuam emojis.
        assertNull(IconesDaBarra.para("▶️", "Ouvir"))
        assertNull(IconesDaBarra.para("🗺️"))
        assertNull(IconesDaBarra.para("🪞"))
    }

    @Test fun todoIconeDaBarraEDosCaminhosEstaNoManifesto() {
        val ids = IconesDaBarra.todos + Trilha.entries.map(::idDoIconeDaTrilha) + CorDaConta.entries.map { it.id } +
            CorDaConta.VAZIA + listOf(ID_ESTRELA_CHEIA, ID_ESTRELA_VAZIA, ID_ESTRELA_BRILHANDO)
        for (id in ids) assertTrue("'$id' fora do manifesto", id in manifesto.imagens)
        assertEquals("icone_trilha_matematica", idDoIconeDaTrilha(Trilha.MATEMATICA))
    }

    @Test fun contasDaFileira() {
        val f = EstadoDaConta.FEITA
        val a = EstadoDaConta.ATUAL
        val x = EstadoDaConta.FALTA
        assertEquals(listOf(f, f, a, x, x), ProgressoVisivel.contas(2, 5))
        assertEquals(listOf(f, f, x, x, x), ProgressoVisivel.contas(2, 5, comAtual = false))
        assertEquals(listOf(f, f, f), ProgressoVisivel.contas(3, 3))
        assertEquals(listOf(f, f, f), ProgressoVisivel.contas(9, 3))
        assertEquals(listOf(a, x), ProgressoVisivel.contas(-1, 2))
        assertTrue(ProgressoVisivel.contas(0, 0).isEmpty())
    }

    @Test fun estrelasAcesasDoCaminho() {
        assertEquals(0, ProgressoVisivel.estrelasAcesas(0, 250, 5))
        assertEquals(1, ProgressoVisivel.estrelasAcesas(1, 250, 5))
        assertEquals(2, ProgressoVisivel.estrelasAcesas(100, 250, 5))
        assertEquals(4, ProgressoVisivel.estrelasAcesas(249, 250, 5))
        assertEquals(5, ProgressoVisivel.estrelasAcesas(250, 250, 5))
        assertEquals(5, ProgressoVisivel.estrelasAcesas(300, 250, 5))
        // Nunca anda para trás.
        var antes = 0
        for (v in 0..250) {
            val agora = ProgressoVisivel.estrelasAcesas(v, 250, 5)
            assertTrue(agora >= antes)
            antes = agora
        }
    }
}
