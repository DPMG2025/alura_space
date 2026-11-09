package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.Loja
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
                val ids = CamadasDosPersonagens.ids(manifesto, personagem, perfil, q)
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
        val feliz = CamadasDosPersonagens.idsDaCrianca(manifesto, menina, QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ))
        assertTrue("crianca_olhos_aberto_cilios" in feliz)
        assertTrue("crianca_boca_feliz" in feliz)
        assertTrue("crianca_laco" in feliz)
        assertTrue("crianca_cabelo_frente_menina_liso" in feliz)
        val torcendo = CamadasDosPersonagens.idsDaCrianca(manifesto, menino, QuadroDoPersonagem(PoseDosBracos.TORCENDO, Humor.TORCENDO))
        assertTrue("crianca_olhos_alegre" in torcendo)
        assertTrue("crianca_cabelo_tras_menino_espetado" in torcendo)
        assertFalse(torcendo.any { it.contains("laco") })
        val piscando = CamadasDosPersonagens.idsDaCrianca(manifesto, menina, QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.PENSANDO, olhosFechados = true))
        assertTrue("crianca_olhos_fechado_cilios" in piscando)
        assertTrue("crianca_sobrancelhas_pensando" in piscando)
        val falando = CamadasDosPersonagens.idsDaCrianca(manifesto, menina, QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ, bocaAberta = true))
        assertTrue("crianca_boca_surpreso" in falando)
        // Com chapéu ou acessório no cabelo, o laço sai (como no desenho antigo).
        assertFalse("crianca_laco" in CamadasDosPersonagens.idsDaCrianca(manifesto, menina.copy(chapeu = "👑"), QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ)))
        // Tranças: elásticos, sem laço.
        val trancas = CamadasDosPersonagens.idsDaCrianca(manifesto, menina.copy(estiloCabelo = 2), QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ))
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

    // ------------------------------------------------------------------ guarda-roupa (Fase 7)

    private val parado = QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ)

    /** As camadas visíveis (depois do `esconde`), na ordem de desenho. */
    private fun visiveis(perfil: Perfil, q: QuadroDoPersonagem = parado): List<String> =
        CamadasDosPersonagens.camadas(manifesto, PersonagemDaArte.CRIANCA, perfil, q)?.map { it.id }
            ?: throw AssertionError("faltam camadas: ${CamadasDosPersonagens.idsDaCrianca(manifesto, perfil, q).filter { it !in manifesto.imagens }}")

    /** Confere uma pilha: todas no manifesto, em ordem, no mesmo quadro, da criança e uma por grupo. */
    private fun confereAPilha(perfil: Perfil, q: QuadroDoPersonagem) {
        val ids = CamadasDosPersonagens.idsDaCrianca(manifesto, perfil, q)
        assertEquals("ids repetidos em $ids", ids.size, ids.toSet().size)
        val camadas = CamadasDosPersonagens.empilhar(manifesto, ids)
        assertNotNull("faltam camadas de $perfil em $q: ${ids.filter { it !in manifesto.imagens }}", camadas)
        camadas!!
        assertEquals(camadas.map { it.ordem }.sorted(), camadas.map { it.ordem })
        assertTrue(camadas.all { it.personagem == "crianca" })
        assertEquals(1, camadas.map { Triple(it.largura, it.altura, it.apoio) }.toSet().size)
        assertEquals("dois do mesmo grupo em ${camadas.map { it.id }}", camadas.size, camadas.map { it.grupo }.toSet().size)
        // Nada do que ficou na pilha está escondido por outra peça.
        for (c in camadas) for (o in camadas) {
            if (CamadasDosPersonagens.pecaDe(o.id) != CamadasDosPersonagens.pecaDe(c.id)) {
                assertFalse("${o.id} devia esconder ${c.id}", c.grupo in o.esconde)
            }
        }
    }

    @Test fun todoTrajeAcessorioEChapeuTemTodasAsCamadas() {
        val chapeus = listOf("") + CamadasDosPersonagens.CAMADAS_DOS_CHAPEUS.keys
        val perfis = Genero.entries.flatMap { g ->
            val base = Perfil("X", g, pele = 1, cabelo = 2, estiloCabelo = if (g == Genero.MENINA) 0 else 2)
            Loja.trajes.flatMap { t -> chapeus.map { c -> base.copy(traje = t.id, chapeu = c) } } +
                Loja.acessorios.map { a -> base.copy(acessorios = mapOf(a.parte to a.id)) } +
                // Um de cada parte ao mesmo tempo, com traje e chapéu (como a captura 59).
                (0 until 3).map { k ->
                    base.copy(
                        acessorios = ParteDoCorpo.entries.associateWith { parte -> Loja.acessorios.filter { it.parte == parte }[k].id },
                        traje = Loja.trajes[k * 5].id,
                        chapeu = chapeus[k + 1],
                    )
                }
        }
        for (perfil in perfis) {
            assertTrue("sem camadas para $perfil", CamadasDosPersonagens.disponivel(manifesto, PersonagemDaArte.CRIANCA, perfil))
            for (q in CamadasDosPersonagens.todosOsQuadros()) confereAPilha(perfil, q)
        }
    }

    @Test fun trajeTrocaARoupaDeSempreEUsaOGenero() {
        val menina = Perfil("Ana", Genero.MENINA, estiloCabelo = 0)
        val menino = Perfil("Leo", Genero.MENINO, estiloCabelo = 0)
        // Listras: silhueta por gênero, tingida com a cor do perfil, com estampa; o sapato de sempre continua.
        val listras = visiveis(menina.copy(traje = "listras"))
        assertTrue("roupa_listras_menina" in listras && "roupa_listras_menina_estampa" in listras)
        assertFalse(listras.any { it.startsWith("crianca_roupa_") })
        assertTrue("crianca_pes_menina" in listras)
        assertEquals("roupa", manifesto.imagens.getValue("roupa_listras_menina").tingir)
        assertTrue("roupa_listras_menino" in visiveis(menino.copy(traje = "listras")))
        // Herói: capa atrás e botas próprias (o sapato de sempre sai).
        val heroi = visiveis(menino.copy(traje = "heroi"))
        assertTrue(listOf("roupa_heroi_menino", "roupa_heroi_menino_estampa", "roupa_heroi_tras", "roupa_heroi_pes").all { it in heroi })
        assertFalse("crianca_pes_menino" in heroi)
        assertTrue(heroi.indexOf("roupa_heroi_tras") < heroi.indexOf("crianca_corpo"))
        // Realeza muda com o gênero; vestido e macacão servem em qualquer criança (a mesma camada).
        assertTrue("roupa_realeza_menina" in visiveis(menina.copy(traje = "realeza")))
        assertTrue("roupa_realeza_menino" in visiveis(menino.copy(traje = "realeza")))
        assertTrue("roupa_vestido" in visiveis(menino.copy(traje = "vestido")))
        assertTrue("roupa_macacao" in visiveis(menina.copy(traje = "macacao")))
        // Trajes coloridos não tingem.
        assertNull(manifesto.imagens.getValue("roupa_pijama").tingir)
        // Traje desconhecido (save antigo) é a roupa de sempre.
        assertEquals(visiveis(menina), visiveis(menina.copy(traje = "nao_existe")))
    }

    @Test fun mangasSeguemOsBracosECabecaDoTrajeSaiComChapeu() {
        val menina = Perfil("Ana", Genero.MENINA, estiloCabelo = 0)
        for (pose in PoseDosBracos.entries) {
            val ids = visiveis(menina.copy(traje = "astronauta"), QuadroDoPersonagem(pose, Humor.FELIZ))
            assertTrue("roupa_astronauta_mangas_${pose.sufixo}" in ids)
            assertEquals(1, ids.count { it.contains("_mangas_") })
            assertTrue("crianca_bracos_${pose.sufixo}" in ids)
            // As mangas compridas vão por cima dos braços.
            assertTrue(ids.indexOf("roupa_astronauta_mangas_${pose.sufixo}") > ids.indexOf("crianca_bracos_${pose.sufixo}"))
        }
        // Capacete sem chapéu (e o laço dos coques some debaixo dele); com o boné, o boné no lugar do capacete.
        val semChapeu = visiveis(menina.copy(traje = "astronauta"))
        assertTrue("roupa_astronauta_cabeca" in semChapeu)
        assertFalse("crianca_laco" in semChapeu)
        val comBone = visiveis(menina.copy(traje = "astronauta", chapeu = "🧢"))
        assertTrue("chapeu_bone" in comBone)
        assertFalse(comBone.any { it.startsWith("roupa_") && it.endsWith("_cabeca") })
        // Pirata: tapa-olho, que sai com óculos.
        assertTrue("roupa_pirata_tapaolho" in visiveis(menina.copy(traje = "pirata")))
        val pirataDeOculos = visiveis(menina.copy(traje = "pirata", acessorios = mapOf(ParteDoCorpo.ROSTO to "oculos")))
        assertFalse("roupa_pirata_tapaolho" in pirataDeOculos)
        assertTrue("acessorio_oculos" in pirataDeOculos)
    }

    @Test fun acessoriosPorParteDoCorpo() {
        val menino = Perfil("Leo", Genero.MENINO, estiloCabelo = 1)
        // Na mão: para cima só torcendo.
        val balao = menino.copy(acessorios = mapOf(ParteDoCorpo.MAO to "balao"))
        assertTrue("acessorio_balao_baixo" in visiveis(balao))
        assertTrue("acessorio_balao_baixo" in visiveis(balao, QuadroDoPersonagem(PoseDosBracos.TCHAU2, Humor.FELIZ)))
        assertTrue("acessorio_balao_cima" in visiveis(balao, QuadroDoPersonagem(PoseDosBracos.TORCENDO, Humor.TORCENDO)))
        // Costas: a peça de trás e as alças (ou o fecho) na frente.
        val mochila = visiveis(menino.copy(acessorios = mapOf(ParteDoCorpo.COSTAS to "mochila")))
        assertTrue("acessorio_mochila" in mochila && "acessorio_mochila_frente" in mochila)
        assertTrue(mochila.indexOf("acessorio_mochila") < mochila.indexOf("crianca_corpo"))
        assertTrue(mochila.indexOf("acessorio_mochila_frente") > mochila.indexOf("crianca_cabeca"))
        assertTrue("acessorio_asas" in visiveis(menino.copy(acessorios = mapOf(ParteDoCorpo.COSTAS to "asas"))))
        // Pés: o tênis tira o sapato de sempre e o do traje.
        val tenis = visiveis(menino.copy(traje = "heroi", acessorios = mapOf(ParteDoCorpo.PES to "tenis")))
        assertTrue("acessorio_tenis" in tenis)
        assertFalse("roupa_heroi_pes" in tenis || "crianca_pes_menino" in tenis)
        // Cabelo: a tiara tira o laço da menina.
        val tiara = visiveis(Perfil("Ana", Genero.MENINA, estiloCabelo = 1, acessorios = mapOf(ParteDoCorpo.CABELO to "tiara")))
        assertTrue("acessorio_tiara" in tiara)
        assertFalse("crianca_laco" in tiara)
        // Pescoço por cima da roupa e embaixo do rosto.
        val gravata = visiveis(menino.copy(acessorios = mapOf(ParteDoCorpo.PESCOCO to "gravata")))
        assertTrue(gravata.indexOf("acessorio_gravata") > gravata.indexOf("crianca_roupa_menino"))
        assertTrue(gravata.indexOf("acessorio_gravata") < gravata.indexOf("crianca_boca_feliz"))
        // Acessório desconhecido ou na parte errada é ignorado.
        assertEquals(visiveis(menino), visiveis(menino.copy(acessorios = mapOf(ParteDoCorpo.ROSTO to "balao"))))
        assertEquals(visiveis(menino), visiveis(menino.copy(acessorios = mapOf(ParteDoCorpo.ROSTO to "nao_existe"))))
    }

    @Test fun chapeusDaLojaSaoCamadas() {
        val chapeusDaLoja = Loja.produtos.filter { it.categoria == br.com.casinhadosaber.dominio.Categoria.CHAPEU }.map { it.valor }
        assertEquals(chapeusDaLoja.toSet(), CamadasDosPersonagens.CAMADAS_DOS_CHAPEUS.keys)
        val menino = Perfil("Leo", Genero.MENINO)
        for (c in chapeusDaLoja) {
            val id = CamadasDosPersonagens.camadaDoChapeu(c)!!
            val info = manifesto.imagens.getValue(id)
            assertEquals("chapeu", info.grupo)
            // A âncora da cabeça do chapéu é a mesma da cabeça da criança (a camada já está no lugar).
            assertEquals(manifesto.imagens.getValue("crianca_cabeca").ancoras["cabeca"], info.ancoras["cabeca"])
            val ids = visiveis(menino.copy(chapeu = c))
            assertEquals(1, ids.count { it.startsWith("chapeu_") })
            assertTrue(id in ids)
            // Por cima do cabelo da frente, embaixo dos braços.
            assertTrue(ids.indexOf(id) > ids.indexOf("crianca_cabelo_frente_menino_liso"))
            assertTrue(ids.indexOf(id) < ids.indexOf("crianca_bracos_parado"))
            assertFalse(CamadasDosPersonagens.chapeuEmEmoji(menino.copy(chapeu = c)))
        }
        // Emoji sem camada (save estranho): fica fora da pilha e vai como emoji pela âncora.
        val estranho = menino.copy(chapeu = "🎓")
        assertTrue(CamadasDosPersonagens.chapeuEmEmoji(estranho))
        assertFalse(visiveis(estranho).any { it.startsWith("chapeu_") })
        assertFalse(CamadasDosPersonagens.chapeuEmEmoji(menino))
    }

    @Test fun pecaDeCadaCamada() {
        assertEquals("crianca", CamadasDosPersonagens.pecaDe("crianca_roupa_menina_estampa"))
        assertEquals("irmao", CamadasDosPersonagens.pecaDe("irmao_laco"))
        assertEquals("roupa_pirata", CamadasDosPersonagens.pecaDe("roupa_pirata_tapaolho"))
        assertEquals("roupa_heroi", CamadasDosPersonagens.pecaDe("roupa_heroi_menina_estampa"))
        assertEquals("acessorio_capa", CamadasDosPersonagens.pecaDe("acessorio_capa_frente"))
        assertEquals("chapeu_bone", CamadasDosPersonagens.pecaDe("chapeu_bone"))
    }

    @Test fun faltandoUmaCamadaDoTrajeFicaODesenhoAntigo() {
        val p = Perfil("X", Genero.MENINO, traje = "pirata")
        assertTrue(CamadasDosPersonagens.disponivel(manifesto, PersonagemDaArte.CRIANCA, p))
        val semPirata = ManifestoDaArte(manifesto.versao, manifesto.escala, manifesto.imagens - "roupa_pirata")
        assertFalse(CamadasDosPersonagens.disponivel(semPirata, PersonagemDaArte.CRIANCA, p))
        // A roupa de sempre continua montada pelas camadas.
        assertTrue(CamadasDosPersonagens.disponivel(semPirata, PersonagemDaArte.CRIANCA, p.copy(traje = "")))
    }
}
