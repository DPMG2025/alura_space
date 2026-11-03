package br.com.casinhadosaber.dados

import android.content.Context
import br.com.casinhadosaber.dominio.Copia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.ResultadoDaCopia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * O Repositorio com várias crianças, nas preferências de verdade (Robolectric): o save de antes vira a
 * criança 1 sem perder nada, cada criança tem o seu progresso, e a cópia vai e volta inteira.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepositorioFamiliaTest {

    private val contexto: Context get() = RuntimeEnvironment.getApplication()
    private fun prefs(nome: String) = contexto.getSharedPreferences(nome, Context.MODE_PRIVATE)

    @Before
    fun limpar() {
        for (n in listOf("casinha_do_saber", "casinha_aparelho", "casinha_do_saber_crianca_2", "casinha_do_saber_crianca_3")) {
            prefs(n).edit().clear().commit()
        }
    }

    /** Como o app de antes guardava: um arquivo só, com o jogo e os ajustes do aparelho. */
    private fun gravarSaveAntigo() {
        prefs("casinha_do_saber").edit()
            .putString("nome", "Ana").putString("genero", "MENINA").putInt("pele", 2)
            .putString("estrelas", "MATEMATICA:0=3;MATEMATICA:1=2")
            .putInt("moedas", 17)
            .putLong("segundos", 900)
            .putString("mascote", "GATINHO|Mimi|")
            .putBoolean("som", false)
            .putInt("timer", 30)
            .commit()
    }

    @Test
    fun saveAntigoViraCrianca1() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        assertEquals(1, repo.criancas.size)
        assertEquals("1", repo.ativa)
        assertEquals("Ana", repo.estado.perfil?.nome)
        assertEquals(17, repo.estado.moedas)
        assertEquals(2, repo.estado.fasesVencidas)
        assertEquals(900L, repo.estado.segundosJogados)
        assertEquals("Mimi", repo.estado.mascote.nome)
        // Os ajustes de antes continuam valendo (agora no arquivo do aparelho).
        assertFalse(repo.somLigado)
        assertEquals(30, repo.limiteMinutos)
        // O arquivo da criança 1 não foi mexido (o teste do emulador grava direto nele).
        assertEquals("MATEMATICA:0=3;MATEMATICA:1=2", prefs("casinha_do_saber").getString("estrelas", null))
        // Abrir de novo não migra de novo nem muda nada.
        val outra = Repositorio(contexto)
        assertEquals(repo.estado, outra.estado)
        assertEquals(30, outra.limiteMinutos)
    }

    @Test
    fun cadaCriancaTemOSeuProgresso() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        assertEquals("2", repo.novaCrianca())
        assertEquals("2", repo.ativa)
        assertNull(repo.estado.perfil)
        assertEquals(0, repo.estado.moedas)
        repo.atualizar { it.copy(perfil = Perfil("Leo", Genero.MENINO), moedas = 5) }
        assertEquals(listOf("Ana", "Leo"), repo.criancas.map { it.nomeParaMostrar() })

        repo.trocarCrianca("1")
        assertEquals(17, repo.estado.moedas)
        assertEquals("Ana", repo.estado.perfil?.nome)
        repo.trocarCrianca("2")
        assertEquals(5, repo.estado.moedas)

        // O limite é do aparelho: vale para as duas.
        assertEquals(30, repo.limiteMinutos)
        repo.somarTempoDeHoje(120)
        repo.trocarCrianca("1")
        assertEquals(120L, repo.segundosHoje())

        // Reabrir lembra quem estava jogando.
        repo.trocarCrianca("2")
        val reaberto = Repositorio(contexto)
        assertEquals("2", reaberto.ativa)
        assertEquals(2, reaberto.criancas.size)

        repo.renomearCrianca("1", "Aninha")
        assertEquals("Aninha", Repositorio(contexto).criancas.first().perfil?.nome)

        // Apagar a criança que está jogando passa para a outra; a apagada some.
        repo.apagarCrianca("2")
        assertEquals(listOf("1"), repo.criancas.map { it.id })
        assertEquals("1", repo.ativa)
        assertEquals(17, repo.estado.moedas)
        assertTrue(prefs("casinha_do_saber_crianca_2").all.isEmpty())
    }

    @Test
    fun copiaVaiEVoltaInteira() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        repo.novaCrianca()
        repo.atualizar { it.copy(perfil = Perfil("", Genero.MENINO), moedas = 8) }
        val texto = Copia.paraTexto(repo.copiaDoProgresso())
        val estadoDe2 = repo.estado
        repo.trocarCrianca("1")
        val estadoDe1 = repo.estado

        // Outro aparelho (ou depois de apagar tudo): uma criança nova, sem nada.
        limpar()
        val novo = Repositorio(contexto)
        assertNull(novo.estado.perfil)
        val r = Copia.deTexto(texto) as ResultadoDaCopia.Valida
        novo.restaurar(r.copia)
        assertEquals(2, novo.criancas.size)
        assertEquals("1", novo.ativa)
        assertEquals(estadoDe1, novo.estado)
        assertEquals(30, novo.limiteMinutos)
        assertFalse(novo.somLigado)
        novo.trocarCrianca("2")
        assertEquals(estadoDe2, novo.estado)
        assertEquals("Criança 2", novo.criancas[1].nomeParaMostrar())
    }

    @Test
    fun copiaInvalidaNaoMudaNada() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        val antes = repo.estado
        val r = Copia.deTexto("{\"app\": \"casinha-do-saber\", \"versao\": 99, \"criancas\": []}")
        assertTrue(r is ResultadoDaCopia.Invalida)
        assertEquals(antes, repo.estado)
        assertEquals(antes, Repositorio(contexto).estado)
    }

    @Test
    fun apagarTudoComUmaCriancaVoltaAoComeco() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        repo.apagarTudo()
        assertNull(repo.estado.perfil)
        assertTrue(repo.somLigado)
        assertEquals(0, repo.limiteMinutos)
        assertEquals(1, repo.criancas.size)
    }
}
