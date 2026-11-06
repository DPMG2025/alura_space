package br.com.casinhadosaber.dominio

enum class Genero { MENINO, MENINA }

/** Personagem da criança. Os índices apontam para as paletas em [Aparencia]. */
data class Perfil(
    val nome: String = "",
    val genero: Genero = Genero.MENINA,
    val pele: Int = 0,
    val cabelo: Int = 0,
    /** Cor da roupa; -1 usa a cor padrão do menino (azul) ou da menina (rosa). */
    val roupa: Int = -1,
    /** Chapéu comprado na loja (emoji), ou vazio. */
    val chapeu: String = "",
    /** 0 = liso, 1 = cacheado, 2 = tranças (menina) ou espetadinho (menino). */
    val estiloCabelo: Int = 0,
    /** Roupa especial do guarda-roupa ([Loja.trajes]), ou vazio para a roupa de sempre. */
    val traje: String = "",
    /**
     * Acessórios do guarda-roupa ([Loja.acessorios]): no máximo um por parte do corpo (id do acessório).
     * Vazio por padrão: saves antigos não têm este campo.
     */
    val acessorios: Map<ParteDoCorpo, String> = emptyMap(),
    /** Dia e mês do aniversário (opcional; saves antigos não têm). Liga a festa de aniversário ([Eventos]). */
    val aniversario: Aniversario? = null,
    /** Até 3 temas de que a criança mais gosta ([TemaFavorito]); as perguntas preferem figuras desses temas. */
    val temasFavoritos: List<TemaFavorito> = emptyList(),
    /** O irmãozinho que mora na casinha ([Irmao]); ligado por padrão, inclusive em saves antigos. */
    val irmao: Irmao = Irmao(),
) {
    val corDaRoupa: Long
        get() = Aparencia.roupas.getOrNull(roupa) ?: if (genero == Genero.MENINA) Aparencia.roupas[1] else Aparencia.roupas[0]
}

object Aparencia {
    val peles = listOf(0xFFFFDBB4, 0xFFF1C27D, 0xFFC68642, 0xFF8D5524)
    val cabelos = listOf(0xFF3B2314, 0xFF1C1C1C, 0xFFE6BE8A, 0xFFB55239)
    val roupas = listOf(0xFF4D96FF, 0xFFFF6FAE, 0xFF3DBE6B, 0xFFFFA62B, 0xFF9B6BFF, 0xFFFF5E5B)
}

/** Lugar de um objeto na cena (mesmo sistema de coordenadas de [ItemCasa]). */
data class Posicao(val x: Float, val y: Float)

data class EstadoJogo(
    val perfil: Perfil? = null,
    /** "TRILHA:fase" para estrelas (1 a 3). Só fases vencidas aparecem aqui. */
    val estrelas: Map<String, Int> = emptyMap(),
    /** Itens já colocados na casa. Estruturas também entram aqui (a posição delas é ignorada). */
    val colocados: Map<String, Posicao> = emptyMap(),
    val coresComodos: Map<Comodo, Int> = emptyMap(),
    /** Onde a criança (o personagem) está na cena. */
    val posicaoCrianca: Posicao = Posicao(0.47f, 0.99f),
    /** Para a área dos pais: perguntas respondidas e acertadas de primeira, por caminho. */
    val respondidas: Map<Trilha, Int> = emptyMap(),
    val acertosDePrimeira: Map<Trilha, Int> = emptyMap(),
    val segundosJogados: Long = 0,
    /** Derrotas seguidas em cada fase ("TRILHA:fase"). Com 2 ou mais, a fase fica mais fácil. */
    val tentativasSemVencer: Map<String, Int> = emptyMap(),
    /** Loja: moedinhas, o que já foi comprado e o que está em uso. */
    val moedas: Int = 0,
    val moedasHoje: Int = 0,
    val diaDasMoedas: Long = -1,
    val comprados: Set<String> = emptySet(),
    val corTelhado: Int = 0,
    val corPorta: Int = 0,
    val papelDeParede: Int = 0,
    val clima: Int = 0,
    val visitantes: Set<String> = emptySet(),
    /** Últimos resultados de primeira por fase ("TRILHA:fase" -> "1101..."), no máximo 10. */
    val historico: Map<String, String> = emptyMap(),
    /** Dia em que a missão do dia já foi feita. */
    val diaDaMissao: Long = -1,
    /**
     * Casa por dentro: móveis que a criança arrastou para outro lugar (id -> canto (x0, y0) da pegada, em
     * ladrilhos). Vazio = tudo no lugar de sempre (saves antigos não têm este campo).
     */
    val moveisDentro: Map<String, Posicao> = emptyMap(),
    /** A mãozinha já mostrou que os móveis da casa por dentro podem ser arrastados (a dica da primeira visita). */
    val viuDicaDeArrastar: Boolean = false,
    /** Mascote da casa ([Mascote.especie] null = ainda não escolheu). O estágio vem de [fasesVencidas] ([Crescimento]). */
    val mascote: Mascote = Mascote(),
    /** Enfeites ganhos nos eventos do ano (ids de [Eventos.todosOsPresentes]). */
    val presentesEspeciais: Set<String> = emptySet(),
    /** Eventos cujo presente do ano já foi entregue ([EventoAtivo.chave], como "NATAL:2026"). */
    val eventosRecebidos: Set<String> = emptySet(),
    /** Desenhos do ateliê ([Atelie.MAXIMO_DE_DESENHOS] no máximo) e os que estão pendurados na parede (ids). */
    val desenhos: List<Pintura> = emptyList(),
    val quadrosNaParede: List<String> = emptyList(),
    /** Dias (dia época) em que o desafio dos pais foi marcado como feito (últimos [Pais.DIAS_GUARDADOS] dias). */
    val desafiosDosPaisFeitos: Set<Long> = emptySet(),
    /** Registro curto das partidas para o relatório da semana (últimos [Pais.DIAS_GUARDADOS] dias). */
    val sessoes: List<RegistroDeSessao> = emptyList(),
    /** Revisões feitas no "Relembrar" ([Relembrar]); com [sessoes], dão a fila de revisão espaçada. */
    val revisoes: List<RegistroDeRevisao> = emptyList(),
    /**
     * "Descobrir o nível" ([Nivelamento]): por caminho, as fases até este índice (inclusive) ficam abertas mesmo
     * sem a anterior vencida. Abertas, nunca vencidas: estrelas e presentes continuam vindo de jogar.
     */
    val liberadaAte: Map<Trilha, Int> = emptyMap(),
    /** Caminhos em que o "Descobrir o nível" já foi feito (não oferece de novo na primeira visita). */
    val nivelFeito: Set<Trilha> = emptySet(),
    /** Livrinhos lidos até o fim ([Livrinhos]): os marcadores da estante. */
    val livrinhosLidos: Set<String> = emptySet(),
) {
    /** A missão do dia aparece uma vez por dia, depois que a criança já venceu alguma fase. */
    fun missaoDisponivel(dia: Long): Boolean = fasesVencidas > 0 && diaDaMissao != dia

    /** As fases vencidas em que a criança mais erra de primeira (para a missão de revisão). */
    fun fasesParaRevisar(todas: List<Fase>, quantas: Int = 2): List<Fase> =
        todas.filter { estrelasDa(it) > 0 }
            .sortedBy { precisaoDaFase(chave(it.trilha, it.numero)) ?: 100 }
            .take(quantas)

    /** Conclui a missão: com 2 ou mais acertos de primeira, ganha [MOEDAS_DA_MISSAO] (fora do limite diário). */
    fun concluirMissao(dia: Long, acertos: Int): Pair<EstadoJogo, Int> {
        val ganhas = if (acertos >= 2) MOEDAS_DA_MISSAO else 2
        return copy(diaDaMissao = dia, moedas = moedas + ganhas) to ganhas
    }

    val totalDeEstrelas: Int get() = estrelas.values.sum()

    /**
     * Toca num produto da loja: se já é seu, só passa a usar (ou tira, no caso de chapéu e visitante);
     * se não é, compra com moedinhas. Devolve null quando faltam moedas.
     */
    fun usar(p: Produto): EstadoJogo? {
        val base = if (p.id in comprados) {
            this
        } else {
            val preco = Loja.preco(p, this)
            if (moedas < preco) return null
            copy(moedas = moedas - preco, comprados = comprados + p.id)
        }
        val v = p.valor
        return when (p.categoria) {
            Categoria.TELHADO -> base.copy(corTelhado = if (corTelhado.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.PORTA -> base.copy(corPorta = if (corPorta.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.PAREDE -> base.copy(papelDeParede = if (papelDeParede.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.CLIMA -> base.copy(clima = if (clima.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.CHAPEU -> base.copy(perfil = base.perfil?.copy(chapeu = if (base.perfil.chapeu == v) "" else v))
            Categoria.VISITANTE -> base.copy(visitantes = if (v in visitantes && p.id in comprados) visitantes - v else visitantes + v)
            // Comida comprada vai para a geladeira e continua lá (não é gasta).
            Categoria.COMIDA -> base
            Categoria.ROUPA -> base.copy(perfil = base.perfil?.copy(traje = if (base.perfil.traje == v && p.id in comprados) "" else v))
            // Acessório: vai para a sua parte do corpo (troca o que estava lá); tocar de novo no que já é seu tira.
            Categoria.ACESSORIO -> {
                val parte = Loja.acessorioPorId[v]?.parte ?: return base
                val atuais = base.perfil?.acessorios.orEmpty()
                base.copy(perfil = base.perfil?.copy(acessorios = if (atuais[parte] == v && p.id in comprados) atuais - parte else atuais + (parte to v)))
            }
        }
    }

    /**
     * Veste um acessório que já é seu na [parte] do corpo (troca o que estava lá). Vazio tira o dessa parte.
     * Devolve o mesmo estado se o acessório não é seu ou não é dessa parte.
     */
    fun vestirAcessorio(parte: ParteDoCorpo, id: String): EstadoJogo {
        val p = perfil ?: return this
        if (id.isEmpty()) return copy(perfil = p.copy(acessorios = p.acessorios - parte))
        val a = Loja.acessorioPorId[id] ?: return this
        if (a.parte != parte || "acessorio$id" !in comprados) return this
        return copy(perfil = p.copy(acessorios = p.acessorios + (parte to id)))
    }

    /** Veste o que já é seu (roupa, chapéu ou acessório). O que ainda não foi comprado não muda nada. */
    fun vestirProduto(p: Produto): EstadoJogo {
        val perfil = perfil ?: return this
        if (p.id !in comprados || p.categoria !in setOf(Categoria.ROUPA, Categoria.CHAPEU, Categoria.ACESSORIO)) return this
        return copy(perfil = perfil.vestindo(p))
    }

    /** Tira o que está na [gaveta] (a roupa volta a ser a de sempre). Continua sendo seu. */
    fun tirar(gaveta: Gaveta): EstadoJogo = perfil?.let { copy(perfil = it.tirando(gaveta)) } ?: this

    /**
     * Veste uma roupa do guarda-roupa (só o que já foi comprado). Vazio volta para a roupa de sempre.
     * Devolve o mesmo estado se a roupa não é sua.
     */
    fun vestir(traje: String): EstadoJogo {
        val p = perfil ?: return this
        if (traje.isNotEmpty() && Loja.trajes.none { it.id == traje && "roupa$traje" in comprados }) return this
        return copy(perfil = p.copy(traje = traje))
    }

    /** Fase em que a criança mais erra de primeira, entre as já liberadas (para os pais). */
    fun precisaoDaFase(chave: String): Int? = historico[chave]?.takeIf { it.isNotEmpty() }?.let { 100 * it.count { c -> c == '1' } / it.length }

    /** Depois de 2 tentativas sem vencer, a fase vem com só 2 opções por pergunta. */
    fun facilitada(fase: Fase): Boolean = (tentativasSemVencer[chave(fase.trilha, fase.numero)] ?: 0) >= 2

    /** Porcentagem de acertos de primeira no caminho (null se ainda não jogou). */
    fun precisao(t: Trilha): Int? {
        val total = respondidas[t] ?: 0
        return if (total == 0) null else (100 * (acertosDePrimeira[t] ?: 0)) / total
    }

    /**
     * Registra uma partida. A fase só é vencida com pelo menos [MINIMO_PARA_VENCER] acertos de primeira:
     * assim o presente da casa vem do aprendizado, não de tocar em tudo.
     */
    fun registrarPartida(fase: Fase, acertos: Int, total: Int, dia: Long = 0, resultados: List<Boolean> = emptyList()): ResultadoPartida {
        // Moedinhas: uma por acerto de primeira, até MOEDAS_POR_DIA por dia (incentiva voltar, sem maratona).
        val hojeJa = if (diaDasMoedas == dia) moedasHoje else 0
        val k0 = chave(fase.trilha, fase.numero)
        // Praticar o que ainda é difícil (ou a fase mais nova) vale o dobro; repetir o que já domina vale metade.
        val precisaoAntes = precisaoDaFase(k0)
        val maisNova = estrelas[k0] == null
        val porAcerto = when {
            maisNova || (precisaoAntes != null && precisaoAntes < 70) -> 2.0
            precisaoAntes != null && precisaoAntes >= 90 && (historico[k0]?.length ?: 0) >= 5 -> 0.5
            else -> 1.0
        }
        val ganhas = (acertos * porAcerto).toInt().coerceAtMost((MOEDAS_POR_DIA - hojeJa).coerceAtLeast(0))
        val novoHistorico = ((historico[k0] ?: "") + resultados.joinToString("") { if (it) "1" else "0" }).takeLast(10)
        val comEstatistica = copy(
            respondidas = respondidas + (fase.trilha to (respondidas[fase.trilha] ?: 0) + total),
            acertosDePrimeira = acertosDePrimeira + (fase.trilha to (acertosDePrimeira[fase.trilha] ?: 0) + acertos),
            moedas = moedas + ganhas,
            moedasHoje = hojeJa + ganhas,
            diaDasMoedas = dia,
            historico = if (resultados.isEmpty()) historico else historico + (k0 to novoHistorico),
        )
        val estrelas = estrelasPorAcertos(acertos, total)
        val k = chave(fase.trilha, fase.numero)
        if (estrelas == 0) {
            val derrotas = comEstatistica.copy(tentativasSemVencer = tentativasSemVencer + (k to (tentativasSemVencer[k] ?: 0) + 1))
            return ResultadoPartida(derrotas, 0, null, ganhas, faseNova = false)
        }
        val primeiraVez = k !in this.estrelas
        val (novo, item) = comEstatistica.copy(tentativasSemVencer = tentativasSemVencer - k).registrarVitoria(fase, estrelas)
        // Depois de todos os prêmios da casa (as 40 peças e as áreas da casa que cresce), cada fase nova dá um
        // presente da loja (de graça) e moedinhas extras.
        if (primeiraVez && item == null) {
            val brinde = Loja.produtos.firstOrNull { it.id !in novo.comprados }
            val comBrinde = if (brinde != null) {
                (novo.copy(moedas = novo.moedas + Loja.preco(brinde, novo)).usar(brinde) ?: novo).copy(moedas = novo.moedas + MOEDAS_DO_BRINDE)
            } else {
                novo.copy(moedas = novo.moedas + MOEDAS_DO_BRINDE)
            }
            return ResultadoPartida(comBrinde, estrelas, null, ganhas + MOEDAS_DO_BRINDE, brinde, faseNova = true)
        }
        return ResultadoPartida(novo, estrelas, item, ganhas, faseNova = primeiraVez)
    }
    val fasesVencidas: Int get() = estrelas.size
    val itensGanhos: List<ItemCasa> get() = ItensDaCasa.ganhos(fasesVencidas)
    /**
     * O que espera no baú. As coisas de uma área nova só aparecem depois que a criança constrói a obra dela
     * (a obra vem primeiro, com o martelinho).
     */
    val itensNoBau: List<ItemCasa> get() = itensGanhos.filter { it.id !in colocados && (it.area == null || it.obra || "${it.area}.obra" in colocados) }

    fun estrelasDa(fase: Fase): Int = estrelas[chave(fase.trilha, fase.numero)] ?: 0

    fun vencidasNa(trilha: Trilha): Int = estrelas.keys.count { it.startsWith(trilha.name + ":") }

    /**
     * A primeira fase de cada trilha está sempre aberta; as outras abrem quando a anterior é vencida
     * (ou quando o "Descobrir o nível" abriu o caminho até elas, [liberadaAte]).
     */
    fun liberada(fase: Fase): Boolean = liberada(fase.trilha, fase.numero)

    /** O mesmo que [liberada] pela trilha e o número da fase (para quem não tem o [Fase] na mão). */
    fun liberada(trilha: Trilha, numero: Int): Boolean =
        numero == 0 || numero <= (liberadaAte[trilha] ?: 0) || estrelas.containsKey(chave(trilha, numero - 1))

    fun temEstrutura(e: Estrutura): Boolean =
        ItensDaCasa.ordem.firstOrNull { it.estrutura == e }?.id?.let { it in colocados } ?: false

    fun corDo(comodo: Comodo): Long = comodo.cores[(coresComodos[comodo] ?: 0) % comodo.cores.size]

    /**
     * Registra uma fase vencida. Retorna o novo estado e o item ganho, se a fase foi vencida pela primeira vez.
     * Repetir uma fase pode melhorar as estrelas, mas não dá outro item.
     */
    fun registrarVitoria(fase: Fase, novasEstrelas: Int): Pair<EstadoJogo, ItemCasa?> {
        val k = chave(fase.trilha, fase.numero)
        val primeiraVez = k !in this.estrelas
        val melhor = maxOf(estrelas[k] ?: 0, novasEstrelas.coerceIn(1, 3))
        val novo = copy(estrelas = estrelas + (k to melhor))
        val premio = if (primeiraVez) ItensDaCasa.proximo(fasesVencidas) else null
        return novo to premio
    }

    fun colocar(item: ItemCasa, pos: Posicao = Posicao(item.x, item.y)): EstadoJogo =
        copy(colocados = colocados + (item.id to pos))

    fun guardar(item: ItemCasa): EstadoJogo =
        if (item.ehEstrutura) this else copy(colocados = colocados - item.id)

    fun pintar(comodo: Comodo): EstadoJogo =
        copy(coresComodos = coresComodos + (comodo to ((coresComodos[comodo] ?: 0) + 1) % comodo.cores.size))

    companion object {
        fun chave(trilha: Trilha, fase: Int) = "${trilha.name}:$fase"

        const val MOEDAS_POR_DIA = 30
        const val MOEDAS_DA_MISSAO = 6
        const val MOEDAS_DO_BRINDE = 5

        /** 4 de 5 de primeira: chutando ao acaso, quase nunca se vence (cerca de 4%). */
        const val MINIMO_PARA_VENCER = 4

        /** 5 de primeira = 3 estrelas; 4 = 2 estrelas; menos = tentar de novo. Na fase facilitada, 4 vale 1 estrela. */
        fun estrelasPorAcertos(acertos: Int, total: Int): Int = when {
            acertos < MINIMO_PARA_VENCER -> 0
            acertos >= total -> 3
            else -> 2
        }
    }
}

data class ResultadoPartida(
    val estado: EstadoJogo,
    val estrelas: Int,
    val item: ItemCasa?,
    val moedasGanhas: Int = 0,
    /** Presente da loja, para as fases depois de todos os prêmios da casa. */
    val brinde: Produto? = null,
    /** A fase foi vencida pela primeira vez nesta partida. */
    val faseNova: Boolean = false,
) {
    val venceu get() = estrelas > 0
}

/** Converte o estado em textos simples para guardar no aparelho (e de volta). */
object Serializacao {

    fun estrelasParaTexto(m: Map<String, Int>): String = m.entries.joinToString(";") { "${it.key}=${it.value}" }

    fun textoParaEstrelas(s: String?): Map<String, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val n = v.toIntOrNull() ?: return@mapNotNull null
        val trilha = k.substringBefore(":")
        if (Trilha.entries.none { it.name == trilha }) return@mapNotNull null
        k to n.coerceIn(1, 3)
    }.toMap()

    fun posicoesParaTexto(m: Map<String, Posicao>): String =
        m.entries.joinToString(";") { "${it.key},${it.value.x},${it.value.y}" }

    fun textoParaPosicoes(s: String?): Map<String, Posicao> = s.orEmpty().split(";").mapNotNull { parte ->
        val p = parte.split(",")
        if (p.size != 3 || p[0] !in ItensDaCasa.porId) return@mapNotNull null
        val x = p[1].toFloatOrNull() ?: return@mapNotNull null
        val y = p[2].toFloatOrNull() ?: return@mapNotNull null
        p[0] to Posicao(x, y)
    }.toMap()

    /** Lugares dos móveis de dentro: só ids da planta ([PlantaDaCasa.porId]) e números válidos. */
    fun textoParaMoveisDentro(s: String?): Map<String, Posicao> = s.orEmpty().split(";").mapNotNull { parte ->
        val p = parte.split(",")
        if (p.size != 3 || p[0] !in PlantaDaCasa.porId) return@mapNotNull null
        val x = p[1].toFloatOrNull()?.takeIf { it.isFinite() } ?: return@mapNotNull null
        val y = p[2].toFloatOrNull()?.takeIf { it.isFinite() } ?: return@mapNotNull null
        p[0] to Posicao(x, y)
    }.toMap()

    fun trilhasParaTexto(m: Map<Trilha, Int>): String = m.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    fun textoParaTrilhas(s: String?): Map<Trilha, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val t = Trilha.entries.firstOrNull { it.name == k } ?: return@mapNotNull null
        t to (v.toIntOrNull() ?: return@mapNotNull null)
    }.toMap()

    fun mapaParaTexto(m: Map<String, Int>): String = m.entries.joinToString(";") { "${it.key}=${it.value}" }

    fun textoParaMapa(s: String?): Map<String, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        k to (v.toIntOrNull() ?: return@mapNotNull null)
    }.toMap()

    fun mapaDeTextoParaTexto(m: Map<String, String>): String = m.entries.joinToString(";") { "${it.key}=${it.value}" }

    fun textoParaMapaDeTexto(s: String?): Map<String, String> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        if (v.any { it != '0' && it != '1' }) return@mapNotNull null
        k to v
    }.toMap()

    /** Acessórios vestidos, como "ROSTO=oculos;MAO=balao". */
    fun acessoriosParaTexto(m: Map<ParteDoCorpo, String>): String = m.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    /** Saves antigos (sem o campo) ou texto estragado viram "sem acessórios"; ids desconhecidos ou da parte errada somem. */
    fun textoParaAcessorios(s: String?): Map<ParteDoCorpo, String> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val p = ParteDoCorpo.entries.firstOrNull { it.name == k } ?: return@mapNotNull null
        if (Loja.acessorioPorId[v]?.parte != p) return@mapNotNull null
        p to v
    }.toMap()

    fun coresParaTexto(m: Map<Comodo, Int>): String = m.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    fun textoParaCores(s: String?): Map<Comodo, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val c = Comodo.entries.firstOrNull { it.name == k } ?: return@mapNotNull null
        c to (v.toIntOrNull() ?: 0)
    }.toMap()
}
