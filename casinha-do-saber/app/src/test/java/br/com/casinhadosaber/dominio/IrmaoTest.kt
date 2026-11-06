package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** O irmãozinho: padrão para saves antigos, ida e volta do texto guardado e falas sem sorteio. */
class IrmaoTest {

    @Test
    fun saveAntigoTemIrmaozinhoLigadoSemNome() {
        val i = Irmao.deTexto(null)
        assertTrue(i.ligado)
        assertEquals(Genero.MENINO, i.genero)
        assertEquals("", i.nome)
        assertEquals("o irmãozinho", i.chamado)
        assertEquals(Irmao(), Perfil().irmao)
    }

    @Test
    fun idaEVoltaDoTexto() {
        for (i in listOf(Irmao(), Irmao(false, Genero.MENINA, "Bia"), Irmao(true, Genero.MENINO, "Theo | o caçula"))) {
            assertEquals(i, Irmao.deTexto(Irmao.paraTexto(i)))
        }
    }

    @Test
    fun textoEstragadoViraOPadrao() {
        for (s in listOf("", "x", "2|MENINO|Theo", "1|GATO|Theo", "1|MENINO")) {
            assertEquals(s, Irmao(), Irmao.deTexto(s))
        }
    }

    @Test
    fun nomeLongoECortado() {
        val i = Irmao.deTexto("1|MENINA|" + "a".repeat(40))
        assertEquals(Irmao.TAMANHO_DO_NOME, i.nome.length)
    }

    @Test
    fun irmazinhaEChamadaNoFeminino() {
        val i = Irmao(genero = Genero.MENINA)
        assertEquals("a irmãzinha", i.chamado)
        assertEquals("Irmãzinha", i.nomeDeFrase)
        assertEquals("Bia", i.copy(nome = "Bia").chamado)
    }

    @Test
    fun falasGiramSemSorteioERespeitamOGeneroDaCrianca() {
        val ana = Perfil(nome = "Ana", genero = Genero.MENINA)
        val leo = Perfil(nome = "Leo", genero = Genero.MENINO)
        assertEquals(Irmao.falaAoTocar(ana, 0), Irmao.falaAoTocar(ana, 5))
        assertTrue(Irmao.falaAoTocar(ana, 0).contains("Ana"))
        assertTrue(Irmao.falaAoTocar(ana, 3).contains("esperta"))
        assertTrue(Irmao.falaAoTocar(leo, 3).contains("esperto"))
        assertFalse(Irmao.falaAoTocar(null, 0).contains("null"))
        assertEquals(Irmao.comemoracao(0), Irmao.comemoracao(5))
    }

    @Test
    fun copiaConheceAChaveDoIrmao() {
        assertEquals('s', Copia.TIPOS_CONHECIDOS["irmao"])
    }
}
