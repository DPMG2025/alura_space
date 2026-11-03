package br.com.casinhadosaber.dados

import android.content.Context
import br.com.casinhadosaber.dominio.CadernoEmArquivo
import br.com.casinhadosaber.dominio.Copia
import br.com.casinhadosaber.dominio.CopiaDoProgresso
import br.com.casinhadosaber.dominio.CriancaGuardada
import br.com.casinhadosaber.dominio.PaginaDoCaderno
import br.com.casinhadosaber.dominio.Ponto
import br.com.casinhadosaber.dominio.RegistroDeRevisao
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.ResultadoDaCopia
import br.com.casinhadosaber.dominio.ValorGuardado
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
        java.io.File(contexto.filesDir, "caderno").deleteRecursively()
        java.io.File(contexto.filesDir, "antes-de-recuperar.json").delete()
        java.io.File(contexto.filesDir, "recuperando.json").delete()
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
        val estadoDe2 = repo.estado
        repo.trocarCrianca("1")
        val estadoDe1 = repo.estado
        // A cópia é feita com a criança 1 jogando: é ela que volta ativa.
        val texto = Copia.paraTexto(repo.copiaDoProgresso())

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

    /** Relembrar, nível e livrinhos ficam no arquivo de cada criança e vão e voltam na cópia. */
    @Test
    fun ritmoELivrinhosSaoDeCadaCriancaEVaoNaCopia() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        val trilha = Trilha.entries.first()
        repo.atualizar {
            it.copy(
                revisoes = listOf(RegistroDeRevisao(20_000L, trilha, 3, true)),
                liberadaAte = mapOf(trilha to 20),
                nivelFeito = setOf(trilha),
                livrinhosLidos = setOf(br.com.casinhadosaber.dominio.Livrinhos.porId.keys.first()),
            )
        }
        val estadoDe1 = repo.estado
        assertTrue(prefs("casinha_do_saber").contains("revisoes"))
        assertFalse(prefs("casinha_aparelho").contains("revisoes"))
        repo.novaCrianca()
        assertTrue(repo.estado.revisoes.isEmpty())
        assertTrue(repo.estado.liberadaAte.isEmpty())
        assertTrue(repo.estado.nivelFeito.isEmpty())
        assertTrue(repo.estado.livrinhosLidos.isEmpty())
        repo.trocarCrianca("1")
        assertEquals(estadoDe1, repo.estado)

        val texto = Copia.paraTexto(repo.copiaDoProgresso())
        assertTrue(texto.contains("\"livrinhos_lidos\""))
        limpar()
        val novo = Repositorio(contexto)
        novo.restaurar((Copia.deTexto(texto) as ResultadoDaCopia.Valida).copia)
        assertEquals(estadoDe1, novo.estado)
        assertEquals(setOf(br.com.casinhadosaber.dominio.Livrinhos.porId.keys.first()), novo.estado.livrinhosLidos)
        assertEquals(20, novo.estado.liberadaAte[trilha])
    }

    /** Cada criança tem o seu caderno; apagar uma criança apaga o dela e deixa o das outras. */
    @Test
    fun cadernoDeCadaCriancaEApagarCrianca() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        val pagina = PaginaDoCaderno("GATO", 20_000L, "GATO".map { listOf(listOf(Ponto(0.2f, 0.2f), Ponto(0.8f, 0.8f))) })
        CadernoEmArquivo.daCrianca(contexto.filesDir, "1").guardar(pagina)
        // A criança 1 continua no caminho de antes das várias crianças.
        assertEquals(listOf("GATO"), CadernoEmArquivo.em(contexto.filesDir).ler().map { it.palavra })
        repo.novaCrianca()
        val de2 = CadernoEmArquivo.daCrianca(contexto.filesDir, "2")
        assertTrue(de2.ler().isEmpty())
        de2.guardar(pagina.copy(palavra = "SAPO"))
        assertEquals(listOf("SAPO"), de2.ler().map { it.palavra })
        repo.apagarCrianca("2")
        assertTrue(de2.ler().isEmpty())
        assertEquals(listOf("GATO"), CadernoEmArquivo.em(contexto.filesDir).ler().map { it.palavra })
        repo.apagarTudo()
        assertTrue(CadernoEmArquivo.em(contexto.filesDir).ler().isEmpty())
    }

    /** Recuperar guarda o progresso de antes; desfazer volta ele e apaga o arquivo guardado. */
    @Test
    fun recuperarEDesfazer() {
        gravarSaveAntigo()
        val repo = Repositorio(contexto)
        val antes = repo.estado
        val copia = CopiaDoProgresso(
            criancas = listOf(CriancaGuardada("1", mapOf("nome" to ValorGuardado.Texto("Leo"), "genero" to ValorGuardado.Texto("MENINO"), "moedas" to ValorGuardado.Inteiro(3)))),
            ativa = "1",
        )
        assertFalse(repo.podeDesfazerRecuperacao)
        repo.recuperar(copia)
        assertEquals("Leo", repo.estado.perfil?.nome)
        assertTrue(repo.podeDesfazerRecuperacao)
        assertTrue(Repositorio(contexto).podeDesfazerRecuperacao)
        assertTrue(repo.desfazerRecuperacao())
        assertEquals(antes, repo.estado)
        assertFalse(repo.podeDesfazerRecuperacao)
        assertFalse(java.io.File(contexto.filesDir, "antes-de-recuperar.json").exists())
    }

    /** O app fechou no meio da recuperação: ao abrir de novo, ela termina (nada fica pela metade). */
    @Test
    fun recuperacaoInterrompidaTerminaAoAbrir() {
        gravarSaveAntigo()
        Repositorio(contexto).novaCrianca()
        val copia = CopiaDoProgresso(
            criancas = listOf(CriancaGuardada("1", mapOf("nome" to ValorGuardado.Texto("Leo"), "genero" to ValorGuardado.Texto("MENINO")))),
            ativa = "1",
        )
        // Como se o app tivesse parado logo depois de marcar "recuperando".
        java.io.File(contexto.filesDir, "recuperando.json").writeText(Copia.paraTexto(copia))
        prefs("casinha_aparelho").edit().putBoolean("recuperando", true).commit()
        val repo = Repositorio(contexto)
        assertEquals(1, repo.criancas.size)
        assertEquals("Leo", repo.estado.perfil?.nome)
        assertFalse(prefs("casinha_aparelho").contains("recuperando"))
        assertTrue(prefs("casinha_do_saber_crianca_2").all.isEmpty())
        assertFalse(java.io.File(contexto.filesDir, "recuperando.json").exists())
    }

    /** Som, música e limite vão sempre na cópia, mesmo nunca mexidos (com os valores padrão). */
    @Test
    fun copiaLevaOsAjustesMesmoPadrao() {
        val repo = Repositorio(contexto)
        val a = repo.copiaDoProgresso().aparelho
        assertEquals(ValorGuardado.Logico(true), a["som"])
        assertEquals(ValorGuardado.Logico(true), a["musica"])
        assertEquals(ValorGuardado.Inteiro(0), a["timer"])
    }

    /** Um conjunto de textos nas preferências vai e volta na cópia (o app de hoje não grava, mas não pode perder). */
    @Test
    fun conjuntoNasPreferenciasVaiEVolta() {
        gravarSaveAntigo()
        prefs("casinha_do_saber").edit().putStringSet("algum_conjunto", setOf("a", "b")).commit()
        val repo = Repositorio(contexto)
        val texto = Copia.paraTexto(repo.copiaDoProgresso())
        limpar()
        Repositorio(contexto).restaurar((Copia.deTexto(texto) as ResultadoDaCopia.Valida).copia)
        assertEquals(setOf("a", "b"), prefs("casinha_do_saber").getStringSet("algum_conjunto", null))
    }
}
