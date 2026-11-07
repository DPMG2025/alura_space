package br.com.casinhadosaber.ui.componentes

// Peças do Prêmio e do Quase no visual brinquedo 3D (Fase 5 do PRD visual 3D; arte da `relatorios/fase5a.md`)
// que não dependem do Android: ids da arte, os quadros do presente, as moedas contáveis e a montagem do pódio.

object PecasDoPremio {
    const val PRESENTE_FECHADO = "premio_presente_fechado"
    const val CLARAO = "premio_clarao"
    const val PODIO = "premio_podio"
    const val ESTRELA_GRANDE = "estrela3d"
    const val ESTRELA_DORMINDO = "quase_estrela_dormindo"

    /** Os quadros do presente abrindo: 0 fechado, 1 a tampa levanta, 2 sobe com luz, 3 voando, 4 aberto. */
    const val QUADROS_DO_PRESENTE = 5

    /** Tempo de cada quadro do presente abrindo, em ms (os 5 quadros cabem em meio segundo). */
    const val DURACAO_DO_QUADRO = 110L

    /** A partir deste quadro o prêmio começa a sair da caixa (a tampa já subiu e a luz acendeu). */
    const val QUADRO_DO_PREMIO = 2

    fun quadroDoPresente(i: Int): String = "premio_presente_abrindo_${i.coerceIn(0, QUADROS_DO_PRESENTE - 1)}"

    /** A peça da casa em 3D (a mesma imagem da casa de fora, `item_<id>`). */
    fun idDoPremio(idDoItem: String): String = "item_$idDoItem"

    /** As pilhas de moedas que existem na arte (`premio_moedas_N`), da maior para a menor. */
    val PILHAS = listOf(5, 3, 2, 1)

    fun idDasMoedas(pilha: Int): String = "premio_moedas_$pilha"

    /**
     * As moedas ganhas como pilhas que se contam (5, 3, 2 e 1 moedas), da maior para a menor, no máximo [maximo]
     * pilhas. Até 13 moedas (e com 15), a soma das pilhas é exatamente [moedas]; nos outros casos as pilhas mostram
     * quase tudo e o número fica de fora ([rotuloDasMoedas]). Nenhuma moeda, nenhuma pilha.
     */
    fun pilhasDeMoedas(moedas: Int, maximo: Int = 3): List<Int> {
        val pilhas = mutableListOf<Int>()
        var falta = moedas
        while (falta > 0 && pilhas.size < maximo) {
            val p = PILHAS.first { it <= falta }
            pilhas += p
            falta -= p
        }
        return pilhas
    }

    /**
     * O número que pode ir ao lado das pilhas de [moedas] ("+5"): só quando as pilhas mostram exatamente essas
     * moedas (cada uma é um `premio_moedas_N` com N moedas que se contam). Quando as pilhas não dão a conta certa,
     * ou não há moeda, nenhum número: nunca um número sozinho ou diferente do que se vê.
     */
    fun rotuloDasMoedas(moedas: Int, maximo: Int = 3): String? =
        if (moedas > 0 && pilhasDeMoedas(moedas, maximo).sum() == moedas) "+$moedas" else null

    /** Sem a arte das pilhas: no máximo estas moedas soltas (`moeda`), e o número só se couberem todas. */
    const val MOEDAS_SOLTAS = 5

    val FORMAS_DO_CONFETE = listOf("papel", "estrela", "bolinha", "serpentina")
    val CORES_DO_CONFETE = listOf("vermelho", "amarelo", "verde", "azul", "roxo", "rosa")

    /** Os 24 confetes 3D (`premio_confete_<forma>_<cor>`). */
    val idsDoConfete: List<String> = FORMAS_DO_CONFETE.flatMap { f -> CORES_DO_CONFETE.map { c -> "premio_confete_${f}_$c" } }

    /** Estágios da semente do Quase: 1 semente, 2 broto, 3 plantinha com botão, 4 flor. */
    const val ESTAGIOS_DA_SEMENTE = 4

    fun idDaSemente(estagio: Int): String = "quase_semente_${estagio.coerceIn(1, ESTAGIOS_DA_SEMENTE)}"

    /**
     * Até onde a semente cresce no Quase com [acertos] de primeira, de [minimo] para vencer: nunca a flor (a flor é
     * vencer a fase), mas sempre pelo menos a semente; mais acertos, planta maior.
     */
    fun estagioDaSemente(acertos: Int, minimo: Int): Int {
        if (minimo <= 0) return ESTAGIOS_DA_SEMENTE - 1
        val a = acertos.coerceIn(0, minimo)
        return (1 + a * (ESTAGIOS_DA_SEMENTE - 1) / minimo).coerceIn(1, ESTAGIOS_DA_SEMENTE - 1)
    }

    /** Toda a arte que as duas telas pedem (fora o prêmio, que depende do item). */
    val idsDaArte: List<String> =
        listOf(PRESENTE_FECHADO, CLARAO, PODIO, ESTRELA_GRANDE, ESTRELA_DORMINDO) +
            (0 until QUADROS_DO_PRESENTE).map(::quadroDoPresente) +
            PILHAS.map(::idDasMoedas) +
            idsDoConfete +
            (1..ESTAGIOS_DA_SEMENTE).map(::idDaSemente)
}

/**
 * Uma coisa de pé no pódio: o ponto [apoioX], [apoioY] (frações da caixa dela, por exemplo os pés da criança) fica
 * sobre a âncora [ancoraX], [ancoraY] do pódio (frações da imagem do pódio), [acima] mais alto (para flutuar).
 */
data class PecaDoPalco(
    val ancoraX: Float,
    val ancoraY: Float,
    val largura: Float,
    val altura: Float,
    val apoioX: Float = 0.5f,
    val apoioY: Float = 1f,
    val acima: Float = 0f,
)

/** Retângulo de uma peça no palco (mesma unidade de quem montou: dp). */
data class CaixaNoPalco(val x: Float, val y: Float, val largura: Float, val altura: Float)

/** O palco montado: altura total, onde começa o pódio e a caixa de cada peça (na ordem dada). */
data class Palco(val altura: Float, val topoDoPodio: Float, val caixas: List<CaixaNoPalco>)

/**
 * Monta o palco: o pódio de [larguraDoPodio] x [alturaDoPodio] fica embaixo e as [pecas] de pé nas âncoras dele.
 * O palco cresce para cima só o necessário para nenhuma peça ser cortada no alto; as peças podem passar dos lados.
 */
fun montarPalco(larguraDoPodio: Float, alturaDoPodio: Float, pecas: List<PecaDoPalco>): Palco {
    // Topo de cada peça medido a partir do topo do pódio (negativo = acima dele).
    val topos = pecas.map { p -> p.ancoraY * alturaDoPodio - p.acima - p.apoioY * p.altura }
    val maisAlto = topos.minOrNull() ?: 0f
    val sobra = if (maisAlto < 0f) -maisAlto else 0f
    val caixas = pecas.mapIndexed { i, p ->
        CaixaNoPalco(
            x = p.ancoraX * larguraDoPodio - p.apoioX * p.largura,
            y = sobra + topos[i],
            largura = p.largura,
            altura = p.altura,
        )
    }
    return Palco(sobra + alturaDoPodio, sobra, caixas)
}
