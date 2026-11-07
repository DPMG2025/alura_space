package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.ParteDoCorpo
import br.com.casinhadosaber.dominio.Perfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Escolha das camadas 3D dos personagens contra o manifesto real (`assets/arte/manifesto.json`) e as contas das
 * animações e do chapéu. JUnit puro, sem Robolectric.
 */
class CamadasDosPersonagensTest {

    private val pasta: File = listOf("src/main/assets/arte", "app/src/main/assets/arte")
        .map(::File).firstOrNull { it.isDirectory }
        ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")

    private val manifesto by lazy { ManifestoDaArte.ler(File(pasta, "manifesto.json").readText()) }

    /** Todas as crianças possíveis: 2 gêneros x 3 estilos x 4 peles x 4 cabelos, com e sem chapéu. */
    private val perfis: List<Perfil> = Genero.entries.flatMap { g ->
        (0..2).flatMap { estilo ->
            (0..3).flatMap { cor ->
                listOf("", "👑").map { chapeu ->
                    Perfil("X", g, pele = cor, cabelo = 3 - cor, estiloCabelo = estilo, chapeu = chapeu, irmao = Irmao(genero = g))
                }
            }
        }
    }

    @Test fun manifestoTrazOsCamposDasCamadas() {
        val cabeca = manifesto.imagens.getValue("crianca_cabeca")
        assertEquals("cabeca", cabeca.grupo)
        assertEquals("pele", cabeca.tingir)
        assertEquals(50, cabeca.ordem)
        assertEquals("crianca", cabeca.personagem)
        assertNull(manifesto.imagens.getValue("crianca_sombra").tingir)
        // Objetos da Fase 0 ficam sem os campos das camadas.
        assertEquals("", manifesto.imagens.getValue("moeda").grupo)
    }

    @Test fun todaCombinacaoTemTodasAsCamadasNaOrdem() {
        for (perfil in perfis) for (personagem in PersonagemDaArte.entries) {
            assertTrue("$personagem de $perfil", CamadasDosPersonagens.disponivel(manifesto, personagem, perfil))
            for (q in CamadasDosPersonagens.todosOsQuadros()) {
                val ids = CamadasDosPersonagens.ids(personagem, perfil, q)
                assertEquals("ids repetidos em $ids", ids.size, ids.toSet().size)
                val camadas = CamadasDosPersonagens.empilhar(manifesto, ids)
                assertNotNull("faltam camadas de $personagem: ${ids.filter { it !in manifesto.imagens }}", camadas)
                camadas!!
                assertEquals(camadas.map { it.ordem }.sorted(), camadas.map { it.ordem })
                assertTrue(camadas.all { it.personagem == personagem.prefixo })
                // Todas no mesmo quadro (alinhadas pixel a pixel) e com os pés no mesmo lugar.
                assertEquals(1, camadas.map { Triple(it.largura, it.altura, it.apoio) }.toSet().size)
                // Uma camada por grupo.
                assertEquals(camadas.size, camadas.map { it.grupo }.toSet().size)
            }
        }
    }

    @Test fun humorEPiscarEscolhemORosto() {
        val menina = Perfil("Ana", Genero.MENINA, estiloCabelo = 0)
        val menino = Perfil("Leo", Genero.MENINO, estiloCabelo = 2)
        val feliz = CamadasDosPersonagens.idsDaCrianca(menina, QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ))
        assertTrue("crianca_olhos_aberto_cilios" in feliz)
        assertTrue("crianca_boca_feliz" in feliz)
        assertTrue("crianca_laco" in feliz)
        assertTrue("crianca_cabelo_frente_menina_liso" in feliz)
        val torcendo = CamadasDosPersonagens.idsDaCrianca(menino, QuadroDoPersonagem(PoseDosBracos.TORCENDO, Humor.TORCENDO))
        assertTrue("crianca_olhos_alegre" in torcendo)
        assertTrue("crianca_cabelo_tras_menino_espetado" in torcendo)
        assertFalse(torcendo.any { it.contains("laco") })
        val piscando = CamadasDosPersonagens.idsDaCrianca(menina, QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.PENSANDO, olhosFechados = true))
        assertTrue("crianca_olhos_fechado_cilios" in piscando)
        assertTrue("crianca_sobrancelhas_pensando" in piscando)
        val falando = CamadasDosPersonagens.idsDaCrianca(menina, QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ, bocaAberta = true))
        assertTrue("crianca_boca_surpreso" in falando)
        // Com chapéu ou acessório no cabelo, o laço sai (como no desenho antigo).
        assertFalse("crianca_laco" in CamadasDosPersonagens.idsDaCrianca(menina.copy(chapeu = "👑"), QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ)))
        // Tranças: elásticos, sem laço.
        val trancas = CamadasDosPersonagens.idsDaCrianca(menina.copy(estiloCabelo = 2), QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ))
        assertTrue("crianca_enfeite_trancas" in trancas)
        assertFalse("crianca_laco" in trancas)
        // Irmãzinha com cílios e laço; irmãozinho sem.
        val irma = CamadasDosPersonagens.idsDoIrmao(menina.copy(irmao = Irmao(genero = Genero.MENINA)), QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ))
        assertTrue("irmao_olhos_aberto_cilios" in irma && "irmao_laco" in irma)
        val irmao = CamadasDosPersonagens.idsDoIrmao(menina, QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ))
        assertTrue("irmao_olhos_aberto" in irmao && "irmao_laco" !in irmao)
    }

    @Test fun estiloForaDaFaixaNaoQuebra() {
        val p = Perfil("X", Genero.MENINO, estiloCabelo = 9)
        assertEquals("espetado", CamadasDosPersonagens.estiloDoCabelo(p))
        assertEquals("liso", CamadasDosPersonagens.estiloDoCabelo(p.copy(estiloCabelo = -1)))
    }

    @Test fun bracosPorHumorETchauEmQuatroQuadros() {
        assertEquals(PoseDosBracos.TORCENDO, CamadasDosPersonagens.bracos(Humor.TORCENDO, PoseDosBracos.TCHAU1))
        assertEquals(PoseDosBracos.PENSANDO, CamadasDosPersonagens.bracos(Humor.PENSANDO))
        assertEquals(PoseDosBracos.PARADO, CamadasDosPersonagens.bracos(Humor.SURPRESO, PoseDosBracos.TCHAU3))
        assertEquals(PoseDosBracos.TCHAU3, CamadasDosPersonagens.bracos(Humor.FELIZ, PoseDosBracos.TCHAU3))
        val ciclo = (0 until 60).map { CamadasDosPersonagens.quadroDoTchau(it / 60f) }
        assertEquals(
            setOf(PoseDosBracos.TCHAU1, PoseDosBracos.TCHAU2, PoseDosBracos.TCHAU3, PoseDosBracos.TCHAU4),
            ciclo.toSet(),
        )
        // Vai e volta: um quadro só pula para o vizinho (e do último volta ao primeiro pelo 2).
        val ordem = listOf(PoseDosBracos.TCHAU1, PoseDosBracos.TCHAU2, PoseDosBracos.TCHAU3, PoseDosBracos.TCHAU4)
        for (k in 1 until ciclo.size) {
            assertTrue(kotlin.math.abs(ordem.indexOf(ciclo[k]) - ordem.indexOf(ciclo[k - 1])) <= 1)
        }
        assertEquals(CamadasDosPersonagens.quadroDoTchau(0.1f), CamadasDosPersonagens.quadroDoTchau(1.1f))
    }

    @Test fun puloAmassaEsticaESobe() {
        val quadros = (0..200).map { CamadasDosPersonagens.pulo(it / 200f) }
        // Começa e termina parado, no chão.
        for (p in listOf(quadros.first(), CamadasDosPersonagens.pulo(0.9999f))) {
            assertEquals(0f, p.subida, 0.001f)
            assertEquals(1f, p.escalaX, 0.02f)
            assertEquals(1f, p.escalaY, 0.02f)
        }
        // Amassa antes de sair e ao pousar; estica no ar.
        assertTrue(CamadasDosPersonagens.pulo(0.12f).escalaY < 0.92f)
        assertTrue(CamadasDosPersonagens.pulo(0.12f).escalaX > 1.05f)
        assertTrue(CamadasDosPersonagens.pulo(0.78f).escalaY < 0.94f)
        assertTrue(CamadasDosPersonagens.pulo(0.2f).escalaY > 1.03f)
        val alto = quadros.maxOf { it.subida }
        assertTrue("sobe pouco: $alto", alto in 0.1f..0.2f)
        // Sem saltos de um quadro para o outro, e o volume quase não muda.
        for (k in 1 until quadros.size) {
            assertTrue(kotlin.math.abs(quadros[k].escalaY - quadros[k - 1].escalaY) < 0.05f)
            assertTrue(kotlin.math.abs(quadros[k].subida - quadros[k - 1].subida) < 0.02f)
        }
        for (p in quadros) assertEquals(1f, p.escalaX * p.escalaY, 0.07f)
        // Nunca afunda no chão.
        assertTrue(quadros.all { it.subida >= 0f })
    }

    @Test fun coresDeTingir() {
        val p = Perfil("X", Genero.MENINO, pele = 2, cabelo = 3, roupa = 4)
        assertEquals(Aparencia.peles[2], CamadasDosPersonagens.cor(p, "pele"))
        assertEquals(Aparencia.cabelos[3], CamadasDosPersonagens.cor(p, "cabelo"))
        assertEquals(Aparencia.roupas[4], CamadasDosPersonagens.cor(p, "roupa"))
        assertNull(CamadasDosPersonagens.cor(p, null))
        // Índices estranhos (saves antigos) voltam para a primeira cor.
        assertEquals(Aparencia.peles[0], CamadasDosPersonagens.cor(p.copy(pele = 17), "pele"))
        assertEquals(Aparencia.roupas[0], CamadasDosPersonagens.cor(p.copy(roupa = -1), "roupa"))
    }

    @Test fun trajeEAcessorioFicamComODesenhoAntigo() {
        val p = Perfil("X", Genero.MENINA)
        assertTrue(CamadasDosPersonagens.usaCamadas(p))
        assertTrue(CamadasDosPersonagens.usaCamadas(p.copy(chapeu = "👑")))
        // Traje desconhecido (save antigo) conta como roupa de sempre.
        assertTrue(CamadasDosPersonagens.usaCamadas(p.copy(traje = "nao_existe")))
        assertFalse(CamadasDosPersonagens.usaCamadas(p.copy(traje = "listras")))
        assertFalse(CamadasDosPersonagens.usaCamadas(p.copy(acessorios = mapOf(ParteDoCorpo.ROSTO to "oculos"))))
    }

    @Test fun quadroNaCaixaPoeOsPesNoLugar() {
        val info = manifesto.imagens.getValue("crianca_corpo")
        // Caixa com a proporção do quadro: preenche a largura.
        val c = CamadasDosPersonagens.caixaDoQuadro(info, 170f, 260f, PersonagemDaArte.CRIANCA.apoioNaCaixa)
        assertEquals(0.5f, c.escala, 0.0001f)
        assertEquals(85f, c.x(info.apoio.x.toFloat()), 0.01f)
        assertEquals(260f * PersonagemDaArte.CRIANCA.apoioNaCaixa, c.y(info.apoio.y.toFloat()), 0.01f)
        // Caixa mais larga (telas que dão outra proporção): cabe pela altura e fica centrado.
        val larga = CamadasDosPersonagens.caixaDoQuadro(info, 400f, 260f, 0.95f)
        assertEquals(0.5f, larga.escala, 0.0001f)
        assertEquals(200f, larga.x(info.apoio.x.toFloat()), 0.01f)
        assertEquals(PROPORCAO_CRIANCA, info.largura.toFloat() / info.altura, 0.005f)
        val irmao = manifesto.imagens.getValue("irmao_corpo")
        assertEquals(PROPORCAO_IRMAO, irmao.largura.toFloat() / irmao.altura, 0.005f)
        // Nada do personagem sai por cima da caixa (o alto do cabelo fica dentro): topo do quadro perto de 0.
        for (p in PersonagemDaArte.entries) {
            val ref = manifesto.imagens.getValue(CamadasDosPersonagens.cabeca(p))
            val cx = CamadasDosPersonagens.caixaDoQuadro(ref, ref.largura.toFloat(), ref.altura.toFloat(), p.apoioNaCaixa)
            val topoDoCabelo = if (p == PersonagemDaArte.CRIANCA) 60f else 29f // alto do topete e da palmeirinha (px do quadro)
            assertTrue("$p sai por cima: ${cx.y(topoDoCabelo)}", cx.y(topoDoCabelo) >= -2f)
        }
    }

    @Test fun chapeuPelaAncoraDaCabeca() {
        val cabeca = manifesto.imagens.getValue("crianca_cabeca")
        val centro = cabeca.ancoras.getValue("cabeca")
        val topo = cabeca.ancoras.getValue("topoDaCabeca")
        val r = CamadasDosPersonagens.raioDaCabeca(cabeca)!!
        val lugar = CamadasDosPersonagens.lugarDoChapeu(cabeca)!!
        assertEquals(centro.x.toFloat(), lugar.centroX, 0.01f)
        // A aba fica entre o topo da cabeça e os olhos.
        val olhos = cabeca.ancoras.getValue("olhoEsquerdo").y
        assertTrue(lugar.base > topo.y && lugar.base < olhos && lugar.base < centro.y)
        assertTrue(lugar.largura in r * 1.5f..r * 2.2f)
        // Sem âncora, sem chapéu (e sem quebrar).
        assertNull(CamadasDosPersonagens.lugarDoChapeu(cabeca.copy(ancoras = emptyMap())))
    }
}
