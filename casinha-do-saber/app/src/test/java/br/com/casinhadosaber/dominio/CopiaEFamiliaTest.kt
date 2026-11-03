package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cópia do progresso (JSON versionado) e várias crianças no mesmo aparelho. */
class CopiaEFamiliaTest {

    /** Um save de antes, como o Repositorio guardava num arquivo só (jogo e ajustes do aparelho juntos). */
    private val saveAntigo: Map<String, ValorGuardado> = mapOf(
        "nome" to ValorGuardado.Texto("Ana \"Bia\" \\ Luz\nzinha ç ã 🦄"),
        "genero" to ValorGuardado.Texto("MENINA"),
        "pele" to ValorGuardado.Inteiro(2),
        "roupa" to ValorGuardado.Inteiro(-1),
        "estrelas" to ValorGuardado.Texto("MATEMATICA:0=3;MATEMATICA:1=2;LOGICA:0=3"),
        "moedas" to ValorGuardado.Inteiro(42),
        "segundos" to ValorGuardado.Longo(12_345_678_901L),
        "dia_missao" to ValorGuardado.Longo(-1),
        "crianca_x" to ValorGuardado.Decimal(0.47f),
        "crianca_y" to ValorGuardado.Decimal(0.99f),
        "viu_dica_arrastar" to ValorGuardado.Logico(true),
        "desenhos" to ValorGuardado.Texto("1:0,0,3;2:5"),
        "som" to ValorGuardado.Logico(false),
        "musica" to ValorGuardado.Logico(true),
        "timer" to ValorGuardado.Inteiro(30),
        "dormindo" to ValorGuardado.Logico(false),
        "dia" to ValorGuardado.Longo(20_395),
        "segundos_hoje" to ValorGuardado.Longo(600),
    )

    @Test
    fun saveAntigoViraCrianca1SemPerderNada() {
        val m = Familia.migrar(saveAntigo)
        // Tudo que é do jogo fica na criança 1, exatamente igual.
        for ((k, v) in saveAntigo) {
            if (k in Familia.AJUSTES_DO_APARELHO) {
                assertEquals("ajuste $k vai para o aparelho", v, m.aparelho[k])
                assertFalse(k in m.crianca1)
            } else {
                assertEquals("chave $k da criança 1", v, m.crianca1[k])
            }
        }
        assertEquals(saveAntigo.size - Familia.AJUSTES_DO_APARELHO.size, m.crianca1.size)
        assertEquals(ValorGuardado.Texto("1"), m.aparelho[Familia.CHAVE_CRIANCAS])
        assertEquals(ValorGuardado.Texto("1"), m.aparelho[Familia.CHAVE_ATIVA])
        // A criança 1 continua no arquivo de sempre (o teste do emulador grava direto nele).
        assertEquals("casinha_do_saber", Familia.arquivo("1"))
        assertEquals("casinha_do_saber_crianca_2", Familia.arquivo("2"))
    }

    @Test
    fun appNovoMigraVazio() {
        val m = Familia.migrar(emptyMap())
        assertTrue(m.crianca1.isEmpty())
        assertEquals(listOf("1"), Familia.idsDeTexto((m.aparelho[Familia.CHAVE_CRIANCAS] as ValorGuardado.Texto).valor))
    }

    @Test
    fun idsDasCriancas() {
        assertEquals("1", Familia.proximoId(emptyList()))
        assertEquals("2", Familia.proximoId(listOf("1")))
        assertEquals("1", Familia.proximoId(listOf("2", "3")))
        assertEquals("3", Familia.proximoId(listOf("1", "2", "4")))
        assertEquals(listOf("1", "3"), Familia.idsDeTexto(" 1,,3,x,1 "))
        assertEquals("1,3", Familia.idsParaTexto(listOf("1", "3")))
        assertEquals("Criança 2", CriancaDaCasa("2", null).nomeParaMostrar())
        assertEquals("Criança 3", CriancaDaCasa("3", Perfil(nome = " ")).nomeParaMostrar())
        assertEquals("Leo", CriancaDaCasa("3", Perfil(nome = "Leo")).nomeParaMostrar())
    }

    private fun copiaDeDuas(): CopiaDoProgresso {
        val m = Familia.migrar(saveAntigo)
        return CopiaDoProgresso(
            criancas = listOf(
                CriancaGuardada("1", m.crianca1),
                CriancaGuardada("2", mapOf("nome" to ValorGuardado.Texto(""), "genero" to ValorGuardado.Texto("MENINO"))),
            ),
            ativa = "2",
            aparelho = m.aparelho.filterKeys { it in Familia.AJUSTES_QUE_VIAJAM },
            feitaEm = Data(2026, 11, 3),
        )
    }

    @Test
    fun idaEVoltaSemPerderNada() {
        val copia = copiaDeDuas()
        val texto = Copia.paraTexto(copia)
        assertTrue(texto.contains("\"versao\": ${Copia.VERSAO}"))
        val volta = Copia.deTexto(texto)
        assertTrue(volta is ResultadoDaCopia.Valida)
        assertEquals(copia, (volta as ResultadoDaCopia.Valida).copia)
        // E de novo: o texto é estável.
        assertEquals(texto, Copia.paraTexto(volta.copia))
        assertEquals(
            listOf(ResumoDaCrianca("Ana \"Bia\" \\ Luz\nzinha ç ã 🦄", 3), ResumoDaCrianca("Criança 2", 0)),
            volta.copia.resumo(),
        )
    }

    @Test
    fun nomeDoArquivo() {
        assertEquals("casinha-do-saber-copia-2026-11-03.json", Copia.nomeDoArquivo(Data(2026, 11, 3)))
    }

    @Test
    fun versaoMaisNovaERecusada() {
        val texto = Copia.paraTexto(copiaDeDuas()).replace("\"versao\": ${Copia.VERSAO}", "\"versao\": ${Copia.VERSAO + 1}")
        assertEquals(ResultadoDaCopia.Invalida(Copia.VERSAO_NOVA), Copia.deTexto(texto))
        val semVersao = Copia.paraTexto(copiaDeDuas()).replace("\"versao\": ${Copia.VERSAO},", "")
        assertEquals(ResultadoDaCopia.Invalida(Copia.ESTRAGADA), Copia.deTexto(semVersao))
        val versaoZero = Copia.paraTexto(copiaDeDuas()).replace("\"versao\": ${Copia.VERSAO}", "\"versao\": 0")
        assertEquals(ResultadoDaCopia.Invalida(Copia.ESTRAGADA), Copia.deTexto(versaoZero))
    }

    @Test
    fun arquivosQueNaoSaoCopiaSaoRecusadosSemExcecao() {
        val bom = Copia.paraTexto(copiaDeDuas())
        val ruins = listOf(
            null, "", "   ", "olá", "{", "[]", "{}", "null", "\"texto\"",
            "{\"app\": \"outro-app\", \"versao\": 1}",
            bom.substring(0, bom.length / 2),
            bom + "lixo",
            "[".repeat(10_000),
            "{\"app\": \"casinha-do-saber\", \"versao\": 1, \"criancas\": []}",
        )
        for (r in ruins) {
            val resultado = Copia.deTexto(r)
            assertTrue("deveria recusar: ${r?.take(40)}", resultado is ResultadoDaCopia.Invalida)
        }
        assertEquals(ResultadoDaCopia.Invalida(Copia.NAO_E_COPIA), Copia.deTexto("{\"app\": \"outro\", \"versao\": 1}"))
    }

    @Test
    fun copiaComDadoDoTipoErradoERecusadaInteira() {
        val bom = Copia.paraTexto(copiaDeDuas())
        // "moedas" é número: como texto, derrubaria o app ao carregar. A cópia toda é recusada.
        val moedasTexto = bom.replace("\"moedas\": {\"t\": \"i\", \"v\": \"42\"}", "\"moedas\": {\"t\": \"s\", \"v\": \"42\"}")
        assertTrue(moedasTexto != bom)
        assertEquals(ResultadoDaCopia.Invalida(Copia.ESTRAGADA), Copia.deTexto(moedasTexto))
        val numeroRuim = bom.replace("\"v\": \"42\"", "\"v\": \"quarenta\"")
        assertEquals(ResultadoDaCopia.Invalida(Copia.ESTRAGADA), Copia.deTexto(numeroRuim))
        val tipoDesconhecido = bom.replace("{\"t\": \"i\", \"v\": \"42\"}", "{\"t\": \"x\", \"v\": \"42\"}")
        assertEquals(ResultadoDaCopia.Invalida(Copia.ESTRAGADA), Copia.deTexto(tipoDesconhecido))
        val idRepetido = bom.replace("{\"id\": \"2\"", "{\"id\": \"1\"")
        assertEquals(ResultadoDaCopia.Invalida(Copia.ESTRAGADA), Copia.deTexto(idRepetido))
        val idRuim = bom.replace("{\"id\": \"2\"", "{\"id\": \"../x\"")
        assertEquals(ResultadoDaCopia.Invalida(Copia.ESTRAGADA), Copia.deTexto(idRuim))
    }

    @Test
    fun ajustesQueNaoViajamFicamDeFora() {
        // Mesmo que alguém edite a cópia, a contagem de hoje e o "dormindo" não entram.
        val c = copiaDeDuas().copy(
            aparelho = mapOf(
                "timer" to ValorGuardado.Inteiro(15),
                "segundos_hoje" to ValorGuardado.Longo(0),
                "dormindo" to ValorGuardado.Logico(false),
            ),
        )
        val volta = Copia.deTexto(Copia.paraTexto(c)) as ResultadoDaCopia.Valida
        assertEquals(mapOf("timer" to ValorGuardado.Inteiro(15)), volta.copia.aparelho)
    }

    @Test
    fun ativaInexistenteVoltaParaAPrimeira() {
        val texto = Copia.paraTexto(copiaDeDuas()).replace("\"ativa\": \"2\"", "\"ativa\": \"9\"")
        assertEquals("1", (Copia.deTexto(texto) as ResultadoDaCopia.Valida).copia.ativa)
    }
}
