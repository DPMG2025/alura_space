package br.com.casinhadosaber.dominio

/**
 * Para os pais (sugestão 7): desafio do dia para fazer junto, longe da tela, e relatório da semana.
 *
 * O desafio do dia gira pela data (sem sorteio): os 60 desafios aparecem um por dia, alternando as áreas.
 * Quem marca como feito é o adulto, atrás do portão de adulto; o prêmio é fixo ([MOEDAS_DO_DESAFIO]).
 * O relatório usa um registro curto das partidas ([RegistroDeSessao]), guardado só no aparelho e
 * cortado nos últimos [DIAS_GUARDADOS] dias.
 */
enum class AreaDoDesafio(val nome: String, val emoji: String) {
    LEITURA("Leitura e escrita", "🔤"),
    MATEMATICA("Matemática", "🔢"),
    INGLES("Inglês", "🌎"),
    LOGICA("Lógica", "🧩"),
}

data class DesafioDosPais(val indice: Int, val area: AreaDoDesafio, val texto: String)

/** Uma partida jogada (fase, treino não entra): o suficiente para o relatório, nada além disso. */
data class RegistroDeSessao(
    /** Dia época local (o mesmo de `Repositorio.hoje()`). */
    val dia: Long,
    val trilha: Trilha,
    /** Número da fase (começa em 0, como [Fase.numero]). */
    val fase: Int,
    /** Estrelas da partida (0 = não venceu desta vez). */
    val estrelas: Int,
    val acertos: Int,
    val total: Int,
    /** Segundos jogados nesta partida. */
    val segundos: Long,
    /** A fase foi vencida pela primeira vez nesta partida. */
    val faseNova: Boolean = false,
) {
    val erros: Int get() = (total - acertos).coerceAtLeast(0)
}

/** Habilidade (uma fase de um caminho) com a precisão de primeira na semana. */
data class Habilidade(val trilha: Trilha, val fase: Int, val nome: String, val acertos: Int, val total: Int) {
    val precisao: Int get() = if (total == 0) 0 else 100 * acertos / total
}

data class RelatorioSemanal(
    /** Segunda feira e domingo da semana. */
    val inicio: Data,
    val fim: Data,
    val minutosJogados: Int,
    val diasJogados: Int,
    val partidas: Int,
    /** Fases vencidas pela primeira vez na semana. */
    val fasesNovas: Int,
    /** Fases diferentes vencidas na semana (novas ou repetidas). */
    val fasesVencidas: Int,
    /** Minutos por caminho. */
    val minutosPorCaminho: Map<Trilha, Int>,
    val pontosFortes: List<Habilidade>,
    val paraReforcar: List<Habilidade>,
    /** 2 ou 3 ideias de conversa para os pais. */
    val sugestoes: List<String>,
) {
    val vazio: Boolean get() = partidas == 0
}

object Pais {
    const val MOEDAS_DO_DESAFIO = 5
    const val DIAS_GUARDADOS = 60
    const val MAXIMO_DE_REGISTROS = 600

    private val leitura = listOf(
        "Encontrem juntos 5 coisas na casa que começam com a letra M.",
        "Leiam juntos o nome de 3 produtos na cozinha.",
        "Escolham uma palavra e batam palmas para cada pedacinho dela.",
        "Procurem a letra do nome da criança em embalagens e revistas.",
        "Contem uma história antes de dormir e perguntem: o que aconteceu no fim?",
        "Inventem juntos uma rima com a palavra gato.",
        "Escrevam juntos um bilhete carinhoso para alguém da família.",
        "Achem 3 palavras escritas na rua ou numa embalagem e leiam juntos.",
        "Brinquem de adivinhar: eu vejo uma coisa que começa com B.",
        "Façam juntos a lista de compras: a criança desenha ou escreve.",
        "Leiam juntos uma placa ou cartaz e descubram o que ele avisa.",
        "Falem 5 palavras que terminam com ão.",
        "Escrevam o nome da criança com letras de massinha, pedrinhas ou palitos.",
        "Escolham um livro e contem a história trocando o final.",
        "Digam palavras bem compridas e contem quantos pedacinhos têm.",
    )

    private val matematica = listOf(
        "Encontrem 5 coisas vermelhas na casa e contem juntos.",
        "Contem os degraus de uma escada, subindo e descendo.",
        "Arrumem a mesa e contem os pratos, os copos e os talheres.",
        "Separem os brinquedos em grupos e vejam qual grupo tem mais.",
        "Brinquem de mercadinho com moedas de verdade ou de papel.",
        "Meçam a mesa com palmos e depois com passos.",
        "Olhem um relógio juntos e descubram a hora do lanche.",
        "Dividam 10 bolachas igualmente entre duas pessoas.",
        "Contem de 2 em 2 usando os pares de meias da gaveta.",
        "Achem formas na casa: um círculo, um quadrado e um triângulo.",
        "Pesem duas frutas com as mãos e digam qual é mais pesada.",
        "Contem quantos passos dá da cama até a porta.",
        "Façam uma receita simples e meçam os ingredientes juntos.",
        "Olhem o calendário e descubram quantos dias faltam para o fim de semana.",
        "Joguem um dado ou dois e somem os pontos.",
    )

    private val ingles = listOf(
        "Digam em inglês as cores de 5 coisas da casa.",
        "Contem até 10 em inglês pulando a cada número.",
        "Brinquem de Simon says: Simon says jump!",
        "Falem o nome em inglês de 3 frutas da cozinha.",
        "Cantem juntos uma música em inglês que a criança conhece.",
        "Apontem partes do corpo e falem em inglês: head, hands, feet.",
        "Digam bom dia em inglês para todo mundo da casa: good morning!",
        "Brinquem de achar: find something blue!",
        "Falem em inglês como estão se sentindo hoje: happy, sleepy, hungry.",
        "Nomeiem em inglês 3 animais de brinquedo ou de livro.",
        "Digam em inglês o que vão comer no jantar.",
        "Brinquem de contar objetos em inglês: one, two, three!",
        "Digam em inglês o nome dos cômodos da casa.",
        "Falem o tempo lá fora em inglês: sunny, rainy, cloudy.",
        "Escolham uma palavra em inglês do dia e usem várias vezes.",
    )

    private val logica = listOf(
        "Arrumem os sapatos da casa do menor para o maior.",
        "Brinquem de esconder um brinquedo e dar pistas: está perto, está longe.",
        "Montem um quebra cabeça juntos.",
        "Façam uma fila de colheres e garfos alternados e peçam o próximo.",
        "Guiem um ao outro pela sala com comandos: dois passos para a frente, vire à direita.",
        "Joguem jogo da memória com cartas ou fotos.",
        "Separem as roupas lavadas por cor e por dono.",
        "Inventem uma sequência de palmas e pés para o outro repetir.",
        "Descubram o que não combina: banana, maçã, sapato e uva.",
        "Construam uma torre com blocos ou caixas e contem os andares.",
        "Desenhem um mapinha da casa com um tesouro escondido.",
        "Brinquem de espelho: um faz um gesto e o outro copia.",
        "Contem uma história em 3 partes e peçam para a criança pôr em ordem.",
        "Façam um labirinto com almofadas e atravessem juntos.",
        "Brinquem de adivinhar o objeto pelas pistas: é redondo e quica.",
    )

    /** Os 60 desafios, alternando as áreas (leitura, matemática, inglês, lógica, leitura...). */
    val desafios: List<DesafioDosPais> = (0 until leitura.size).flatMap { i ->
        listOf(
            AreaDoDesafio.LEITURA to leitura[i],
            AreaDoDesafio.MATEMATICA to matematica[i],
            AreaDoDesafio.INGLES to ingles[i],
            AreaDoDesafio.LOGICA to logica[i],
        )
    }.mapIndexed { i, (a, t) -> DesafioDosPais(i, a, t) }

    /** O desafio do [dia] (dia época): gira pela lista inteira, sem repetir antes de 60 dias. */
    fun desafioDoDia(dia: Long): DesafioDosPais = desafios[Math.floorMod(dia, desafios.size.toLong()).toInt()]

    /** Guarda só os dias dos últimos [DIAS_GUARDADOS] dias a partir de [hoje]. */
    fun cortarDias(dias: Set<Long>, hoje: Long): Set<Long> = dias.filter { it > hoje - DIAS_GUARDADOS && it <= hoje }.toSet()

    // ------------------------------------------------------------------ registro das partidas

    /** Acrescenta [r] e corta o que passou de [DIAS_GUARDADOS] dias (contando do dia mais novo) e de [MAXIMO_DE_REGISTROS]. */
    fun acrescentar(registros: List<RegistroDeSessao>, r: RegistroDeSessao): List<RegistroDeSessao> = cortar(registros + r)

    fun cortar(registros: List<RegistroDeSessao>): List<RegistroDeSessao> {
        val maisNovo = registros.maxOfOrNull { it.dia } ?: return registros
        return registros.filter { it.dia > maisNovo - DIAS_GUARDADOS }.takeLast(MAXIMO_DE_REGISTROS)
    }

    /** "dia,TRILHA,fase,estrelas,acertos,total,segundos,nova;..." */
    fun registrosParaTexto(registros: List<RegistroDeSessao>): String = registros.joinToString(";") {
        "${it.dia},${it.trilha.name},${it.fase},${it.estrelas},${it.acertos},${it.total},${it.segundos},${if (it.faseNova) 1 else 0}"
    }

    /** Saves antigos (sem a chave) ou texto estragado: as linhas que não dá para ler somem, as outras ficam. */
    fun textoParaRegistros(s: String?): List<RegistroDeSessao> = cortar(
        s.orEmpty().split(";").mapNotNull { linha ->
            val p = linha.split(",")
            if (p.size != 8) return@mapNotNull null
            val trilha = Trilha.entries.firstOrNull { it.name == p[1] } ?: return@mapNotNull null
            val numeros = listOf(p[0], p[2], p[3], p[4], p[5], p[6], p[7]).map { it.toLongOrNull() ?: return@mapNotNull null }
            if (numeros.drop(1).any { it < 0 }) return@mapNotNull null
            RegistroDeSessao(numeros[0], trilha, numeros[1].toInt(), numeros[2].toInt().coerceIn(0, 3), numeros[3].toInt(), numeros[4].toInt(), numeros[5], numeros[6] == 1L)
        },
    )

    fun diasParaTexto(dias: Set<Long>): String = dias.sorted().joinToString(",")

    fun textoParaDias(s: String?): Set<Long> = s.orEmpty().split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()

    // ------------------------------------------------------------------ relatório

    private val conversas: Map<Trilha, List<String>> = mapOf(
        Trilha.MATEMATICA to listOf(
            "Na hora do lanche, pergunte quantas frutas há no prato e quantas sobram se alguém comer uma.",
            "Peça ajuda para contar o troco numa compra pequena.",
            "Pergunte que horas são no relógio da cozinha antes do jantar.",
            "Conversem sobre o que é mais pesado e mais leve nas compras do mercado.",
        ),
        Trilha.PORTUGUES to listOf(
            "Pergunte qual foi a palavra mais engraçada que apareceu no jogo.",
            "Leiam juntos o nome das coisas no caminho para a escola.",
            "Peça para contar uma história com começo, meio e fim sobre o dia.",
            "Brinquem de descobrir palavras que rimam no jantar.",
        ),
        Trilha.INGLES to listOf(
            "Pergunte como se diz o nome de um bicho em inglês.",
            "Peça para ensinar a vocês uma palavra nova em inglês.",
            "Usem as cores em inglês para escolher a roupa do dia.",
            "Contem os degraus ou os carrinhos em inglês.",
        ),
        Trilha.LOGICA to listOf(
            "Peça para explicar como pensou para resolver um desafio difícil.",
            "Brinquem de dar comandos de robô para chegar até a cozinha.",
            "Montem juntos uma sequência de brinquedos e peçam o próximo.",
            "Conversem sobre o que fazer quando um desafio parece difícil: tentar de novo com calma.",
        ),
    )

    private val conversasGerais = listOf(
        "Pergunte o que mais gostou de fazer no app esta semana.",
        "Peça para mostrar a casinha e contar o que ganhou de novo.",
        "Elogie o esforço, não só os acertos: tentar de novo também é aprender.",
    )

    /** Nome padrão de uma habilidade, quando a tela não passa o título da fase. */
    fun nomePadrao(trilha: Trilha, fase: Int): String = "${trilha.titulo}, fase ${fase + 1}"

    /**
     * Relatório da semana (segunda a domingo) que contém o dia [dia].
     * [nomeDaFase] dá o título de cada fase (as telas passam o título do [GeradorDeFases]).
     */
    fun relatorioDaSemana(
        registros: List<RegistroDeSessao>,
        dia: Long,
        nomeDaFase: (Trilha, Int) -> String = ::nomePadrao,
    ): RelatorioSemanal {
        val semana = Calendario.semana(dia)
        val inicioDia = semana * 7 - 3
        val daSemana = registros.filter { Calendario.semana(it.dia) == semana }
        val habilidades = daSemana.groupBy { it.trilha to it.fase }.map { (k, rs) ->
            Habilidade(k.first, k.second, nomeDaFase(k.first, k.second), rs.sumOf { it.acertos }, rs.sumOf { it.total })
        }.filter { it.total > 0 }
        // Ponto forte: pelo menos uma partida inteira e quase tudo de primeira. Reforçar: abaixo de 70%.
        val fortes = habilidades.filter { it.total >= 5 && it.precisao >= 90 }
            .sortedWith(compareByDescending<Habilidade> { it.precisao }.thenByDescending { it.total }.thenBy { it.trilha.ordinal }.thenBy { it.fase })
            .take(3)
        val reforcar = habilidades.filter { it.precisao < 70 }
            .sortedWith(compareBy<Habilidade> { it.precisao }.thenByDescending { it.total }.thenBy { it.trilha.ordinal }.thenBy { it.fase })
            .take(3)
        val minutosPorCaminho = daSemana.groupBy { it.trilha }.mapValues { (_, rs) -> (rs.sumOf { it.segundos } / 60).toInt() }
        return RelatorioSemanal(
            inicio = Data.deDiaEpoca(inicioDia),
            fim = Data.deDiaEpoca(inicioDia + 6),
            minutosJogados = (daSemana.sumOf { it.segundos } / 60).toInt(),
            diasJogados = daSemana.map { it.dia }.distinct().size,
            partidas = daSemana.size,
            fasesNovas = daSemana.count { it.faseNova },
            fasesVencidas = daSemana.filter { it.estrelas > 0 }.map { it.trilha to it.fase }.distinct().size,
            minutosPorCaminho = minutosPorCaminho,
            pontosFortes = fortes,
            paraReforcar = reforcar,
            sugestoes = sugestoes(daSemana, reforcar, semana),
        )
    }

    /**
     * Ideias de conversa: primeiro do caminho que mais precisa de reforço, depois do mais jogado, e uma geral.
     * Gira pela semana para não repetir sempre a mesma frase.
     */
    private fun sugestoes(daSemana: List<RegistroDeSessao>, reforcar: List<Habilidade>, semana: Long): List<String> {
        val giro = Math.floorMod(semana, 4L).toInt()
        val caminhos = (reforcar.map { it.trilha } +
            daSemana.groupBy { it.trilha }.entries.sortedWith(compareByDescending<Map.Entry<Trilha, List<RegistroDeSessao>>> { e -> e.value.sumOf { it.segundos } }.thenBy { it.key.ordinal }).map { it.key })
            .distinct().take(2)
        val porCaminho = caminhos.map { t -> conversas.getValue(t).let { it[giro % it.size] } }
        val geral = conversasGerais[Math.floorMod(semana, conversasGerais.size.toLong()).toInt()]
        return (porCaminho + geral).let { if (it.size < 2) it + conversasGerais.first { g -> g !in it } else it }.take(3)
    }
}
