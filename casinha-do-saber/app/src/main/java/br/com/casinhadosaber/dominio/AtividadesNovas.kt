package br.com.casinhadosaber.dominio

/*
 * Tipos novos de desafio (catálogo completo em docs/plano-1000-fases.md, seção 3).
 *
 * Todos são atividades de mexer: a pergunta é criada com Pergunta.deAtividade(...) e a tela de cada
 * uma fica num arquivo próprio em ui/telas/atividades/novas/ (AtividadePescaria.kt e os demais),
 * chamada pelo `when` de AtividadeInterativa. Regras comuns a todas:
 *  - nunca há tempo correndo contra a criança; o que se mexe (peixe, balão) anda devagar e volta;
 *  - cada movimento errado chama onErro() uma vez (a peça volta tremendo e o narrador diz o nome do
 *    que foi tocado); depois de 2 erros a resposta certa brilha, como nas perguntas de tocar;
 *  - quando a criança termina, onConcluir(semErro) diz se ela fez tudo sem nenhum erro.
 *
 * Os geradores de fase ainda NÃO sorteiam estes tipos: cada um entra no jogo quando a sua tela
 * estiver pronta (ver o plano).
 */

// ------------------------------------------------------------------------------------- pescaria

/**
 * Pescaria: peixinhos nadam devagar num lago, cada um levando uma figura, número ou letra. A criança
 * toca nos peixes [certos] para pescá-los (o peixe pula para o balde). Peixe errado: ele foge
 * nadando, conta um erro e volta depois. Termina quando todos os certos estão no balde.
 */
data class Pescaria(val peixes: List<Opcao>, val certos: Set<Int>) : Atividade {
    init {
        require(peixes.size in 3..8) { "Pescaria com 3 a 8 peixes" }
        require(certos.isNotEmpty() && certos.all { it in peixes.indices } && certos.size < peixes.size) { "Pescaria sem peixe certo ou sem peixe errado" }
    }
}

// ------------------------------------------------------------------------------------- balões

/**
 * Estourar balões: balões coloridos sobem devagar, cada um com uma resposta. Tocar estoura. Os
 * [certos] precisam ser estourados (na ordem da lista, quando [emOrdem]: 1, 2, 3... ou A, B, C).
 * Balão errado (ou fora da ordem) só balança, conta um erro e continua. Balão que chega no alto
 * volta por baixo: nunca se perde nada.
 */
data class EstourarBaloes(val baloes: List<Opcao>, val certos: List<Int>, val emOrdem: Boolean = false) : Atividade {
    init {
        require(baloes.size in 3..9) { "De 3 a 9 balões" }
        require(certos.isNotEmpty() && certos.distinct().size == certos.size && certos.all { it in baloes.indices }) { "Balões certos inválidos" }
    }
}

// ------------------------------------------------------------------------------------- ligar pares

/**
 * Ligar pares: duas colunas; a criança arrasta o dedo de um item da [esquerda] até o par na [direita]
 * e uma linha colorida fica desenhada. Par errado: a linha some, conta um erro. [pares] liga o índice
 * da esquerda ao índice da direita; a direita pode ter uma sobra (distrator).
 */
data class LigarPares(val esquerda: List<Opcao>, val direita: List<Opcao>, val pares: Map<Int, Int>) : Atividade {
    init {
        require(esquerda.size in 2..5 && direita.size in esquerda.size..esquerda.size + 1) { "Ligar pares com 2 a 5 itens" }
        require(pares.keys == esquerda.indices.toSet()) { "Todo item da esquerda precisa de par" }
        require(pares.values.toSet().size == pares.size && pares.values.all { it in direita.indices }) { "Pares repetidos" }
    }
}

// ------------------------------------------------------------------------------------- quebra cabeça

/**
 * Quebra cabeça de arrastar: a [figura] (emoji grande ou desenho) é cortada em [linhas] por [colunas]
 * peças embaralhadas embaixo; a criança arrasta cada peça para a moldura. Peça solta no lugar certo
 * encaixa com um clique; no lugar errado volta tremendo e conta um erro. [rotulos] (opcional, um por
 * peça, na ordem da moldura) escreve algo em cada peça: 1 a 9, de 10 em 10, o alfabeto, sílabas de
 * uma palavra, e então a ordem dos rótulos ajuda a montar. [fixas] já começam no lugar (mais fácil).
 */
data class QuebraCabeca(
    val figura: String,
    val linhas: Int,
    val colunas: Int,
    val rotulos: List<String> = emptyList(),
    val fixas: Set<Int> = emptySet(),
) : Atividade {
    val pecas: Int get() = linhas * colunas

    init {
        require(linhas in 1..4 && colunas in 2..4) { "Quebra cabeça de 2 a 16 peças" }
        require(rotulos.isEmpty() || rotulos.size == linhas * colunas) { "Um rótulo por peça" }
        require(fixas.all { it in 0 until linhas * colunas } && fixas.size < linhas * colunas) { "Peças fixas inválidas" }
    }
}

// ------------------------------------------------------------------------------------- trem

/**
 * Trem para ordenar: a locomotiva puxa engates vazios; os vagões soltos ficam nos trilhos de baixo.
 * A criança arrasta cada vagão para o engate certo, na ordem de [vagoes] (crescente, decrescente, de
 * 2 em 2, alfabeto, dias da semana, do menor para o maior). [fixos] já começam engatados (pistas).
 * [extras] são vagões que não entram no trem. Vagão no engate errado volta e conta um erro. Com tudo
 * engatado, o trem apita e sai andando.
 */
data class Trem(val vagoes: List<Opcao>, val fixos: Set<Int> = emptySet(), val extras: List<Opcao> = emptyList()) : Atividade {
    init {
        require(vagoes.size in 3..7) { "Trem de 3 a 7 vagões" }
        require(fixos.all { it in vagoes.indices } && fixos.size < vagoes.size) { "Vagões fixos inválidos" }
        require(extras.size <= 3 && extras.none { e -> vagoes.any { it.visual == e.visual } }) { "Vagão extra igual a um do trem" }
    }
}

// ------------------------------------------------------------------------------------- balança

/**
 * Balança de dois pratos: o prato da esquerda tem [ladoEsquerdo] (pesos com número, ou figuras com
 * [figura] quando [mostrarNumeros] é falso). A criança arrasta pesos de [pesos] para o prato da
 * direita até a balança ficar reta (soma igual). Se passar do peso, a balança pende, conta um erro e a
 * criança pode tirar um peso de volta. Também serve para lógica (qual pesa mais) com figuras.
 */
data class Balanca(
    val ladoEsquerdo: List<Int>,
    val pesos: List<Int>,
    val figura: String = "🧱",
    val mostrarNumeros: Boolean = true,
) : Atividade {
    val alvo: Int get() = ladoEsquerdo.sum()

    init {
        require(ladoEsquerdo.isNotEmpty() && ladoEsquerdo.all { it > 0 } && pesos.all { it > 0 }) { "Pesos positivos" }
        require(pesos.size in 2..8) { "De 2 a 8 pesos para escolher" }
        require(temSoma(pesos, ladoEsquerdo.sum())) { "Nenhuma combinação equilibra a balança" }
    }
}

/** Alguma parte de [valores] (cada um usado no máximo uma vez) soma [alvo]? */
internal fun temSoma(valores: List<Int>, alvo: Int): Boolean {
    var possiveis = setOf(0)
    for (v in valores) possiveis = possiveis + possiveis.map { it + v }.filter { it <= alvo }
    return alvo in possiveis
}

// ------------------------------------------------------------------------------------- caça na cena

/** Uma figura espalhada na cena da caça: [x] e [y] de 0 a 1 (centro da figura), [escala] 1 = normal. */
data class ItemNaCena(val opcao: Opcao, val x: Float, val y: Float, val escala: Float = 1f) {
    init {
        require(x in 0f..1f && y in 0f..1f && escala in 0.4f..2f) { "Figura fora da cena" }
    }
}

/**
 * Caça ao tesouro: uma cena grande do mundo [mundo] (índice em [Mundos.todos], para o fundo
 * combinar) com figuras espalhadas, algumas meio escondidas atrás de moitas e pedras. A criança acha e
 * toca nos [certos] ("ache as 3 estrelas", "ache o que começa com B", "find the cat"). Cada achado
 * ganha um brilho e vai para a barra de achados; figura errada diz o próprio nome e conta um erro.
 */
data class CacaNaCena(val mundo: Int, val itens: List<ItemNaCena>, val certos: Set<Int>) : Atividade {
    init {
        require(itens.size in 4..16) { "Cena com 4 a 16 figuras" }
        require(certos.isNotEmpty() && certos.all { it in itens.indices } && certos.size < itens.size) { "Nada para achar" }
    }
}

// ------------------------------------------------------------------------------------- siga a sequência

/**
 * Siga a sequência (Simon de sons e cores): de 2 a 4 botões grandes, cada um com cor ([cores]),
 * figura e som (a fala da [botoes]). O jogo acende e toca a [sequencia] devagar; a criança repete
 * tocando na mesma ordem, no tempo dela. Toque fora da ordem: o jogo mostra a sequência de novo e
 * conta um erro (não volta ao começo do jogo). Serve para memória, sons das letras, palavras em inglês.
 */
data class SigaASequencia(val botoes: List<Opcao>, val cores: List<Long>, val sequencia: List<Int>) : Atividade {
    init {
        require(botoes.size in 2..4 && cores.size == botoes.size) { "De 2 a 4 botões, uma cor por botão" }
        require(sequencia.size in 2..7 && sequencia.all { it in botoes.indices }) { "Sequência de 2 a 7 toques" }
    }
}

// ------------------------------------------------------------------------------------- pintar

/** Uma cor da legenda do pintar: [codigo] é o que aparece nas casinhas que levam esta cor. */
data class CorDaLegenda(val codigo: String, val argb: Long, val fala: List<Trecho> = emptyList())

/** Uma casinha do desenho: mostra [rotulo] ("3", "2+1", "A", "red") e deve ser pintada com a cor de [codigo]. */
data class CasaDePintar(val rotulo: String, val codigo: String?)

/**
 * Pintar por código: um desenho quadriculado (pixel art) de [colunas] colunas. A criança toca uma
 * cor da [legenda] (pote de tinta) e depois as casinhas cujo rótulo leva àquela cor. Casinha certa
 * fica pintada; cor errada escorre, a casinha fica branca de novo e conta um erro. Casinhas sem código
 * ficam brancas. Quando tudo está pintado, o desenho aparece inteiro e brilha.
 */
data class PintarPorCodigo(val colunas: Int, val casas: List<CasaDePintar>, val legenda: List<CorDaLegenda>) : Atividade {
    val linhas: Int get() = casas.size / colunas

    init {
        require(colunas in 3..MAX_COLUNAS && casas.size % colunas == 0 && casas.size / colunas in 3..8) {
            "Desenho de 3 a $MAX_COLUNAS casinhas de largura e 3 a 8 de altura"
        }
        require(legenda.size in 2..5 && legenda.map { it.codigo }.toSet().size == legenda.size) { "Legenda de 2 a 5 cores distintas" }
        val codigos = legenda.map { it.codigo }.toSet()
        require(casas.all { it.codigo == null || it.codigo in codigos }) { "Casinha com código fora da legenda" }
        require(casas.any { it.codigo != null }) { "Nada para pintar" }
    }

    companion object {
        /**
         * Largura máxima do desenho: num celular de 360 dp cada casinha fica com pelo menos 48 dp
         * ((360 − 2 × 16 de margem − 20 de folga) / 6, cerca de 51 dp). Desenhos maiores crescem para baixo.
         */
        const val MAX_COLUNAS = 6
    }
}

// ------------------------------------------------------------------------------------- ditado

/**
 * Ditado de ouvir e montar: o narrador fala a palavra (botão de ouvir de novo sempre à mão) e a
 * criança arrasta letras (ou sílabas) do teclado de peças para as casinhas, na ordem. Diferente de
 * montar palavra: sem a figura como pista quando [figura] é nulo, e pode ser em inglês ([idioma]).
 * Peça na casinha errada volta e conta um erro.
 */
data class Ditado(
    val partes: List<String>,
    val extras: List<String>,
    val idioma: Idioma = Idioma.PT,
    val figura: String? = null,
) : Atividade {
    val palavra: String get() = partes.joinToString("")

    init {
        require(partes.size in 2..10 && partes.all { it.isNotBlank() }) { "Ditado de 2 a 10 peças" }
        require(extras.size <= 4 && extras.none { it in partes }) { "Peça extra igual a uma da palavra" }
    }
}

// ------------------------------------------------------------------------------------- dominó

/** Meia peça de dominó: [chave] diz com o que ela combina (duas meias com a mesma chave se encaixam). */
data class MeiaPeca(val chave: String, val visual: Visual, val fala: List<Trecho> = emptyList())

data class PecaDomino(val a: MeiaPeca, val b: MeiaPeca)

/**
 * Dominó: a fileira começa com a [ponta] livre. A criança arrasta, da mão ([mao]), a peça que combina
 * com a ponta (3 bolinhas com o número 3, letra grande com a pequena, palavra em inglês com a figura,
 * figura com a sombra); a peça encaixa virando se for preciso e a outra metade vira a nova ponta.
 * [jogadas] peças precisam ser encaixadas. Peça que não combina volta e conta um erro.
 * Em cada jogada exatamente uma peça da mão combina com a ponta (conferido aqui).
 */
data class Domino(val ponta: MeiaPeca, val mao: List<PecaDomino>, val jogadas: Int) : Atividade {
    /** As peças na ordem certa de encaixe (índices em [mao]). */
    val ordem: List<Int> = run {
        val usadas = mutableListOf<Int>()
        var chave = ponta.chave
        repeat(jogadas) {
            val servem = mao.indices.filter { it !in usadas && (mao[it].a.chave == chave || mao[it].b.chave == chave) }
            require(servem.size == 1) { "Na jogada ${usadas.size + 1} deveria haver uma peça só que combina (há ${servem.size})" }
            val i = servem.single()
            usadas += i
            chave = if (mao[i].a.chave == chave) mao[i].b.chave else mao[i].a.chave
        }
        usadas
    }

    init {
        require(jogadas in 1..5 && mao.size in jogadas..jogadas + 3) { "Dominó de 1 a 5 jogadas, com até 3 peças sobrando" }
    }
}

// ------------------------------------------------------------------------------------- esconde esconde

/**
 * Esconde esconde: o [bicho] acena e se esconde atrás de um dos [esconderijos] (moitas, caixas,
 * baldes, cada um com uma figura, número, letra ou cor). Com [trocas] > 0 os esconderijos trocam de
 * lugar devagar, [trocas] vezes, e a criança segue com o olho; com [trocas] = 0 a pista vem na fala da
 * pergunta ("ele está atrás do número 7", "behind the red box"). Tocar no esconderijo [certo] faz o
 * bicho pular de alegria; no errado sai outra coisa engraçada (uma borboleta, um sapo) e conta um erro.
 */
data class EscondeEsconde(val bicho: String, val esconderijos: List<Opcao>, val certo: Int, val trocas: Int = 0) : Atividade {
    init {
        require(esconderijos.size in 2..6 && certo in esconderijos.indices) { "De 2 a 6 esconderijos" }
        require(trocas in 0..8) { "No máximo 8 trocas" }
    }
}

// ------------------------------------------------------------------------------------- cartas viradas

/**
 * Cartas viradas em ordem: as [cartas] ficam viradas para baixo. A criança vira uma por vez procurando
 * a próxima de [ordem] (1, 2, 3...; A, B, C...; one, two, three). Achou: a carta fica aberta e vai para
 * a fila de cima. Virou outra: ela fica aberta um instante (para lembrar onde está), vira de volta e
 * conta um erro. Sem relógio: a "corrida" é só contra a própria memória.
 */
data class CartasViradas(val cartas: List<Opcao>, val ordem: List<Int>) : Atividade {
    init {
        require(cartas.size in 3..12) { "De 3 a 12 cartas" }
        require(ordem.size in 2..cartas.size && ordem.distinct().size == ordem.size && ordem.all { it in cartas.indices }) { "Ordem inválida" }
    }
}

// ------------------------------------------------------------------------------------- mercadinho

/**
 * Mercadinho: o [produto] (emoji, chamado [nome]) tem etiqueta de [preco] (em centavos). A criança
 * arrasta moedas e cédulas da carteira ([dinheiro], valores em centavos, cada item é uma peça) para o
 * balcão até pagar o preço exato. Com [troco], o cliente paga [pago] e a criança devolve a diferença.
 * Se passar do valor, a última moeda volta para a carteira e conta um erro.
 */
data class Mercadinho(
    val produto: String,
    val nome: String,
    val preco: Int,
    val dinheiro: List<Int>,
    val troco: Boolean = false,
    val pago: Int = 0,
) : Atividade {
    /** Quanto a criança precisa juntar no balcão. */
    val alvo: Int get() = if (troco) pago - preco else preco

    init {
        require(preco > 0 && dinheiro.all { it in VALORES }) { "Moeda ou cédula que não existe" }
        require(!troco || pago > preco) { "Sem troco para dar" }
        require(dinheiro.size in 2..10 && temSoma(dinheiro, if (troco) pago - preco else preco)) { "A carteira não paga o valor exato" }
    }

    companion object {
        /** Moedas e cédulas do real, em centavos. */
        val VALORES = setOf(5, 10, 25, 50, 100, 200, 500, 1000, 2000, 5000, 10000)
    }
}

// ------------------------------------------------------------------------------------- relógio

/**
 * Acertar o relógio: um relógio de ponteiros grande; a criança arrasta o ponteiro dos minutos (que
 * pula de 5 em 5) e o das horas até marcar [hora]:[minuto] (pedido falado e, com [digital], também
 * escrito em relógio digital ao lado). Ao soltar na hora certa o relógio toca o cuco; na errada o
 * narrador diz a hora que ficou marcada e conta um erro.
 */
data class Relogio(val hora: Int, val minuto: Int, val digital: Boolean = false) : Atividade {
    init {
        require(hora in 1..12 && minuto in 0..55 && minuto % 5 == 0) { "Hora de 1 a 12, minutos de 5 em 5" }
    }
}

// ------------------------------------------------------------------------------------- programar o robô

/**
 * Programar o robô: grade de [linhas] por [colunas] casinhas (índice = linha * colunas + coluna) com o
 * robô em [inicio], a bateria em [chegada] e pedras em [obstaculos]. A criança arrasta setas para a
 * fila de comandos (no máximo [maxComandos]) e toca no ▶; o robô anda seguindo a fila. Bateu na pedra
 * ou na borda, ou parou fora da chegada: ele volta ao começo, a seta errada pisca e conta um erro.
 * Com [repetir], aparece o bloco "2 vezes" (laço) para programas mais curtos.
 */
data class ProgramarRobo(
    val linhas: Int,
    val colunas: Int,
    val inicio: Int,
    val chegada: Int,
    val obstaculos: Set<Int> = emptySet(),
    val maxComandos: Int = 6,
    val repetir: Boolean = false,
) : Atividade {
    /** Menor número de passos do início até a chegada (sem passar por pedras). */
    val menorCaminho: Int = run {
        val total = linhas * colunas
        val dist = IntArray(total) { -1 }
        val fila = ArrayDeque(listOf(inicio))
        if (inicio in 0 until total) dist[inicio] = 0
        while (fila.isNotEmpty()) {
            val c = fila.removeFirst()
            val l = c / colunas
            val k = c % colunas
            for ((dl, dk) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                val nl = l + dl
                val nk = k + dk
                if (nl !in 0 until linhas || nk !in 0 until colunas) continue
                val n = nl * colunas + nk
                if (n in obstaculos || dist[n] >= 0) continue
                dist[n] = dist[c] + 1
                fila.addLast(n)
            }
        }
        dist.getOrElse(chegada) { -1 }
    }

    init {
        require(linhas in 2..6 && colunas in 2..6) { "Grade de 2 a 6 casinhas por lado" }
        require(inicio != chegada && inicio !in obstaculos && chegada !in obstaculos) { "Início e chegada livres e diferentes" }
        require(menorCaminho > 0) { "Não há caminho até a chegada" }
        require(repetir || menorCaminho <= maxComandos) { "Caminho maior que a fila de comandos" }
    }
}

// ------------------------------------------------------------------------------------- montar frase

/**
 * Montar a frase: as [palavras] (na ordem certa) aparecem embaralhadas em cartões, junto com [extras];
 * a criança arrasta cada cartão para a linha da frase. Quando completa, o narrador lê a frase inteira
 * (em [idioma]) e a [figura], se houver, se mexe. Cartão no lugar errado volta e conta um erro.
 */
data class MontarFrase(
    val palavras: List<String>,
    val extras: List<String> = emptyList(),
    val idioma: Idioma = Idioma.PT,
    val figura: String? = null,
) : Atividade {
    val frase: String get() = palavras.joinToString(" ")

    init {
        require(palavras.size in 2..8 && palavras.all { it.isNotBlank() }) { "Frase de 2 a 8 palavras" }
        require(extras.size <= 3 && extras.none { it in palavras }) { "Cartão extra igual a uma palavra da frase" }
    }
}
