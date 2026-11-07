package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Loja
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A casa de fora em camadas 3D (Fase 3): a pilha que o app monta para cada estado tem toda a arte no manifesto,
 * na ordem da tabela de `docs/visual-3d/relatorios/fase3a.md`, com os cômodos tingidos e a noite certa; os 40 itens
 * têm imagem e o ponto de apoio cai no pé. JUnit puro (lê o manifesto versionado em `assets/arte/`).
 */
class CamadasDaCasaTest {

    private val pasta: File = listOf("src/main/assets/arte", "app/src/main/assets/arte")
        .map(::File).firstOrNull { it.isDirectory }
        ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")

    private val manifesto by lazy { ManifestoDaArte.ler(File(pasta, "manifesto.json").readText()) }

    /** As estruturas na ordem em que a criança ganha (os prêmios de `ItensDaCasa.ordem`). */
    private val naOrdemDoJogo = ItensDaCasa.ordem.mapNotNull { it.estrutura }

    @Test fun aArteDaCasaEstaToda() {
        val faltam = CamadasDaCasa.idsPossiveis().filter { it !in manifesto.imagens }
        assertTrue("faltam camadas da casa no manifesto: $faltam", faltam.isEmpty())
        assertTrue(CamadasDaCasa.disponivel(manifesto))
        // As cores da arte são as da loja.
        assertEquals(Loja.coresDoTelhado.size, CamadasDaCasa.CORES_DO_TELHADO)
        assertEquals(Loja.coresDaPorta.size, CamadasDaCasa.CORES_DA_PORTA)
    }

    @Test fun camadasDaCenaTemOQuadroDaCena() {
        for (id in CamadasDaCasa.idsPossiveis().filter { it.startsWith("casa_") || it in CamadasDaCasa.ESTRELAS || it.startsWith("ceu_chuva") || it.startsWith("ceu_neve") }) {
            val info = manifesto.imagens.getValue(id)
            assertEquals("$id: largura", CamadasDaCasa.LARGURA_DA_ARTE, info.largura)
            assertTrue("$id: altura ${info.altura}", info.altura == CamadasDaCasa.ALTURA_DA_CENA || info.altura == CamadasDaCasa.ALTURA_DO_CHAO)
            // A cena da arte tem a proporção da cena do app (o mesmo quadro, alinhado no topo).
            assertEquals(PROPORCAO_DA_CENA_NO_TESTE, CamadasDaCasa.ALTURA_DA_CENA.toFloat() / CamadasDaCasa.LARGURA_DA_ARTE, 0.001f)
        }
    }

    /** Todas as etapas da construção (na ordem do jogo), de dia e de noite, com e sem neve, abajur e papéis. */
    @Test fun todaPilhaPossivelExisteEEstaEmOrdem() {
        var conferidas = 0
        for (n in 0..naOrdemDoJogo.size) {
            val estruturas = naOrdemDoJogo.take(n).toSet()
            for (noite in listOf(false, true)) for (neve in listOf(false, true)) for (papel in 0..3) for (abajur in listOf(false, true)) {
                val a = AparenciaDaCasa(
                    estruturas = estruturas,
                    coresDosComodos = Comodo.entries.associateWith { it.cores[papel % it.cores.size] },
                    corTelhado = papel, corPorta = papel, papel = papel, noite = noite,
                    neveNoTelhado = neve && Estrutura.TELHADO in estruturas, abajur = abajur,
                )
                val camadas = CamadasDaCasa.camadas(a)
                val pilha = CamadasDaCasa.empilhar(manifesto, camadas)
                assertNotNull("pilha sem arte: ${camadas.map { it.id }}", pilha)
                // Já sai na ordem do manifesto (nada muda de lugar ao empilhar) e sem repetir.
                assertEquals(camadas.map { it.id }, pilha!!.map { it.first.id })
                val ordens = pilha.map { it.second.ordem }
                assertEquals("fora de ordem: ${camadas.map { it.id }}", ordens.sorted(), ordens)
                assertEquals(camadas.size, camadas.map { it.id }.toSet().size)
                // O chão é sempre a primeira camada.
                assertEquals(CamadasDaCasa.idDoChao(Estrutura.TERRENO in estruturas), camadas.first().id)
                conferidas++
            }
        }
        assertTrue(conferidas > 100)
    }

    @Test fun cadaEstruturaPoeASuaCamada() {
        val tudo = AparenciaDaCasa(Estrutura.entries.toSet())
        val ids = CamadasDaCasa.camadas(tudo).map { it.id }
        val esperado = listOf(
            "casa_etapa_1_terreno", "casa_lago", "casa_caminho", "casa_cerca", "casa_chamine",
            "casa_comodo_quarto", "casa_comodo_banheiro", "casa_comodo_sala", "casa_comodo_cozinha",
            "casa_janelas", "casa_etapa_4_porta_0", "casa_etapa_2_paredes", "casa_escada", "casa_etapa_3_telhado_0",
        )
        assertEquals(esperado, ids)
        // Sem terreno: o lote vazio com a placa; nada mais.
        assertEquals(listOf("casa_etapa_0_vazio"), CamadasDaCasa.camadas(AparenciaDaCasa(emptySet())).map { it.id })
        // Janelas, porta e escada só com paredes; a chaminé só com telhado (como no desenho antigo).
        val semParedes = CamadasDaCasa.camadas(AparenciaDaCasa(setOf(Estrutura.TERRENO, Estrutura.JANELAS, Estrutura.PORTA, Estrutura.ESCADA, Estrutura.CHAMINE))).map { it.id }
        assertEquals(listOf("casa_etapa_1_terreno"), semParedes)
        // Cores da loja, papel de parede e clima.
        val loja = CamadasDaCasa.camadas(tudo.copy(corTelhado = 5, corPorta = 4, papel = 2, neveNoTelhado = true)).map { it.id }
        assertTrue("casa_etapa_3_telhado_5" in loja && "casa_etapa_4_porta_4" in loja && "casa_papel_2" in loja && "casa_neve_no_telhado" in loja)
        // Índices fora da faixa (save estranho) ficam na arte que existe.
        assertTrue("casa_etapa_3_telhado_0" in CamadasDaCasa.camadas(tudo.copy(corTelhado = 9)).map { it.id })
        assertNull(CamadasDaCasa.idDoPapel(0))
        assertEquals("ceu_chuva", CamadasDaCasa.doClima(1))
        assertEquals("ceu_neve", CamadasDaCasa.doClima(2))
        assertNull(CamadasDaCasa.doClima(0))
    }

    @Test fun tingimentoDosComodosENoite() {
        val cores = mapOf(Comodo.QUARTO to 0xFFFFE3ECL, Comodo.BANHEIRO to 0xFFD7F3FFL, Comodo.SALA to 0xFFFFF1C9L, Comodo.COZINHA to 0xFFE2F7D5L)
        val dia = CamadasDaCasa.camadas(AparenciaDaCasa(Estrutura.entries.toSet(), cores))
        for ((comodo, cor) in cores) {
            val camada = dia.single { it.id == CamadasDaCasa.COMODOS.getValue(comodo) }
            assertEquals(cor, camada.cor)
            // O manifesto diz qual cômodo a camada tinge (o nome do cômodo em minúsculas).
            assertEquals(comodo.name.lowercase(), manifesto.imagens.getValue(camada.id).tingir)
        }
        // De dia, só os cômodos são tingidos.
        assertTrue(dia.filter { it.id !in CamadasDaCasa.COMODOS.values }.all { it.cor == null })

        val noite = CamadasDaCasa.camadas(AparenciaDaCasa(Estrutura.entries.toSet(), cores, noite = true))
        val ids = noite.map { it.id }
        assertTrue("casa_janelas_noite" in ids && "casa_janela_do_telhado_noite" in ids && "casa_janelas" !in ids)
        for (c in noite) {
            when (c.id) {
                // As janelas acesas não escurecem.
                "casa_janelas_noite", "casa_janela_do_telhado_noite" -> assertNull(c.id, c.cor)
                in CamadasDaCasa.COMODOS.values -> assertNotNull(c.id, c.cor)
                else -> assertEquals(c.id, CamadasDaCasa.NOITE, c.cor)
            }
        }
        val quarto = noite.single { it.id == "casa_comodo_quarto" }.cor!!
        assertEquals(CamadasDaCasa.multiplicar(0xFFFFE3EC, CamadasDaCasa.NOITE), quarto)
        // Com o abajur, os cômodos ficam mais claros que sem ele.
        val comAbajur = CamadasDaCasa.camadas(AparenciaDaCasa(Estrutura.entries.toSet(), cores, noite = true, abajur = true))
            .single { it.id == "casa_comodo_quarto" }.cor!!
        assertTrue(brilho(comAbajur) > brilho(quarto))
    }

    @Test fun aparenciaDoEstadoDoJogo() {
        var e = EstadoJogo()
        for (item in ItensDaCasa.ordem) e = e.colocar(item)
        e = e.copy(clima = 2, corTelhado = 3, corPorta = 2, papelDeParede = 1).pintar(Comodo.SALA)
        val a = AparenciaDaCasa.de(e, noite = true)
        assertEquals(Estrutura.entries.toSet(), a.estruturas)
        assertTrue(a.neveNoTelhado && a.noite && a.abajur)
        assertEquals(e.corDo(Comodo.SALA), a.coresDosComodos[Comodo.SALA])
        assertEquals(3, a.corTelhado)
        assertEquals(2, a.corPorta)
        assertEquals(1, a.papel)
        // Igualdade é o que decide refazer o composto: o mesmo estado dá a mesma aparência.
        assertEquals(a, AparenciaDaCasa.de(e, noite = true))
        assertFalse(AparenciaDaCasa.de(e, noite = false).abajur)
        // Chuva não muda a pilha (cai por cima); só a neve põe a neve no telhado.
        assertFalse(AparenciaDaCasa.de(e.copy(clima = 1), noite = false).neveNoTelhado)
    }

    @Test fun todoItemDaCasaTemImagemEApoioNoPe() {
        for (item in ItensDaCasa.ordem) {
            val id = CamadasDaCasa.idDoItem(item.id, noite = false)
            val info = manifesto.imagens[id]
            assertNotNull("falta a imagem do item ${item.id} ($id)", info)
            val lugar = CamadasDaCasa.lugarDoItem(info!!, 100f)
            // O ponto de apoio cai no pé (0, 0) e a imagem tem a largura do item com a folguinha.
            assertEquals(0f, lugar.esquerda + info.apoio.x * lugar.largura / info.largura, 0.01f)
            assertEquals(0f, lugar.topo + info.apoio.y * lugar.altura / info.altura, 0.01f)
            assertEquals(100f * CamadasDaCasa.FOLGA_DO_ITEM, lugar.largura, 0.01f)
            assertTrue("${item.id}: a imagem precisa ficar quase toda acima do pé", lugar.topo < 0f && lugar.base >= 0f)
            // Os móveis altos e baixos guardam a proporção do domínio (geladeira, fogão, mesa).
            if (item.desenho != null) {
                val proporcao = info.altura.toFloat() / info.largura
                assertEquals(item.id, ItensDaCasa.proporcaoDe(item), proporcao, 0.1f)
            }
        }
        assertEquals("item_sol_noite", CamadasDaCasa.idDoItem("sol", noite = true))
        assertEquals("item_cama", CamadasDaCasa.idDoItem("cama", noite = true))
        assertNotNull(manifesto.imagens["item_sol_noite"])
        assertTrue(CamadasDaCasa.ITENS_ACESOS.all { it in manifesto.imagens })
    }

    @Test fun multiplicarCores() {
        assertEquals(0xFFFFFFFF, CamadasDaCasa.multiplicar(0xFFFFFFFF, 0xFFFFFFFF))
        assertEquals(0xFF123456, CamadasDaCasa.multiplicar(0xFF123456, 0xFFFFFFFF))
        assertEquals(0xFF000000, CamadasDaCasa.multiplicar(0xFF123456, 0xFF000000))
        assertEquals(0xFF808080, CamadasDaCasa.multiplicar(0xFFFFFFFF, 0xFF808080))
    }

    private fun brilho(c: Long) = ((c shr 16) and 0xFF) + ((c shr 8) and 0xFF) + (c and 0xFF)

    private companion object {
        /** `PROPORCAO_CENA` do app (altura / largura da cena). */
        const val PROPORCAO_DA_CENA_NO_TESTE = 1.15f
    }
}
