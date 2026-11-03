package br.com.casinhadosaber.dominio

/** Partes fixas da casa, desenhadas na cena (a criança toca para construir). */
enum class Estrutura { TERRENO, PAREDES, TELHADO, PORTA, JANELAS, ESCADA, CHAMINE, CERCA, CAMINHO, LAGO }

/** Onde o objeto pode ficar. */
enum class Lugar { DENTRO, FORA, CEU, QUALQUER }

/** Móveis desenhados à mão (não existem como emoji). */
enum class Desenho { GELADEIRA, FOGAO, MESA }

/**
 * Um prêmio para a casa. Objetos podem ser arrastados; estruturas ficam no lugar certo.
 *
 * Posições normalizadas: [x] é o centro (fração da largura) e [y] é onde fica o "pé"
 * do objeto (fração da altura da cena). [tamanho] é a largura em fração da largura da cena.
 *
 * Valores de [y] entre 1 e 2 (só em posições guardadas, nunca aqui) querem dizer "no jardim de baixo",
 * a faixa extra de grama das telas altas: 1 é o topo desse jardim e 2 é o fundo dele. Assim a posição
 * vale em qualquer altura de tela, e as posições antigas (de 0 a 1) continuam iguais.
 */
data class ItemCasa(
    val id: String,
    val nome: String,
    val comArtigo: String,
    val nomeEn: String,
    val icone: String,
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val tamanho: Float = 0.08f,
    val estrutura: Estrutura? = null,
    val desenho: Desenho? = null,
) {
    val ehEstrutura get() = estrutura != null
    val lugar: Lugar get() = ItensDaCasa.lugarDe(id)
    val comodo: Comodo? get() = ItensDaCasa.comodoDe(id)
}

/** Retângulo em frações da cena: [esq] e [dir] da largura, [topo] e [base] da altura. */
data class CaixaNaCena(val esq: Float, val topo: Float, val dir: Float, val base: Float) {
    val area get() = (dir - esq).coerceAtLeast(0f) * (base - topo).coerceAtLeast(0f)

    /** Quanto da menor das duas caixas fica coberto pela outra (0 = nada, 1 = inteira). */
    fun sobreposicao(outra: CaixaNaCena): Float {
        val l = minOf(dir, outra.dir) - maxOf(esq, outra.esq)
        val a = minOf(base, outra.base) - maxOf(topo, outra.topo)
        if (l <= 0f || a <= 0f) return 0f
        val menor = minOf(area, outra.area)
        return if (menor <= 0f) 0f else l * a / menor
    }
}

enum class Comodo(val nome: String, val cores: List<Long>) {
    QUARTO("Quarto", listOf(0xFFFFE3EC, 0xFFE8E0FF, 0xFFFFF4C2, 0xFFD9F5E5, 0xFFFFE0C7)),
    BANHEIRO("Banheiro", listOf(0xFFD7F3FF, 0xFFE0FFF4, 0xFFFFF0F5, 0xFFF0E8FF, 0xFFFFFBE0)),
    SALA("Sala", listOf(0xFFFFF1C9, 0xFFFFE3EC, 0xFFDDF0FF, 0xFFE6F9D9, 0xFFF3E5FF)),
    COZINHA("Cozinha", listOf(0xFFE2F7D5, 0xFFFFF1C9, 0xFFFFE6D5, 0xFFDDF0FF, 0xFFFFE3EC)),
}

object ItensDaCasa {

    private fun estrutura(id: String, nome: String, comArtigo: String, en: String, icone: String, e: Estrutura) =
        ItemCasa(id, nome, comArtigo, en, icone, estrutura = e)

    private fun objeto(id: String, nome: String, comArtigo: String, en: String, emoji: String, x: Float, y: Float, t: Float) =
        ItemCasa(id, nome, comArtigo, en, emoji, x, y, t)

    private fun movel(id: String, nome: String, comArtigo: String, en: String, icone: String, d: Desenho, x: Float, y: Float, t: Float) =
        ItemCasa(id, nome, comArtigo, en, icone, x, y, t, desenho = d)

    /** Ordem dos prêmios: o primeiro vem na primeira fase vencida, em qualquer trilha. */
    val ordem: List<ItemCasa> = listOf(
        estrutura("terreno", "Terreno", "o terreno com grama", "yard", "🌱", Estrutura.TERRENO),
        estrutura("paredes", "Paredes", "as paredes", "walls", "🧱", Estrutura.PAREDES),
        estrutura("telhado", "Telhado", "o telhado", "roof", "🏠", Estrutura.TELHADO),
        estrutura("porta", "Porta", "a porta", "door", "🚪", Estrutura.PORTA),
        objeto("cama", "Cama", "a cama", "bed", "🛏️", 0.27f, 0.528f, 0.16f),
        estrutura("janelas", "Janelas", "as janelas", "windows", "🪟", Estrutura.JANELAS),
        objeto("sofa", "Sofá", "o sofá", "sofa", "🛋️", 0.44f, 0.775f, 0.13f),
        objeto("sol", "Sol", "o sol", "sun", "☀️", 0.12f, 0.16f, 0.13f),
        estrutura("escada", "Escada", "a escada", "stairs", "🪜", Estrutura.ESCADA),
        objeto("arvore", "Árvore", "a árvore", "tree", "🌳", 0.07f, 0.95f, 0.17f),
        movel("geladeira", "Geladeira", "a geladeira", "fridge", "🧊", Desenho.GELADEIRA, 0.85f, 0.775f, 0.075f),
        objeto("ursinho", "Ursinho", "o ursinho", "teddy bear", "🧸", 0.31f, 0.495f, 0.06f),
        movel("fogao", "Fogão", "o fogão", "stove", "🍳", Desenho.FOGAO, 0.76f, 0.775f, 0.075f),
        objeto("cachorro", "Cachorrinho", "o cachorrinho", "dog", "🐶", 0.20f, 0.95f, 0.07f),
        objeto("tv", "Televisão", "a televisão", "TV", "📺", 0.54f, 0.775f, 0.075f),
        movel("mesa", "Mesa", "a mesa", "table", "🍽️", Desenho.MESA, 0.655f, 0.775f, 0.09f),
        objeto("flores", "Flores", "as flores", "flowers", "🌷", 0.60f, 0.99f, 0.06f),
        objeto("banheira", "Banheira", "a banheira", "bathtub", "🛁", 0.72f, 0.528f, 0.14f),
        estrutura("chamine", "Chaminé", "a chaminé", "chimney", "💨", Estrutura.CHAMINE),
        objeto("abajur", "Abajur", "o abajur", "lamp", "💡", 0.36f, 0.375f, 0.055f),
        objeto("gato", "Gatinho", "o gatinho", "cat", "🐱", 0.37f, 0.775f, 0.06f),
        objeto("quadro", "Quadro", "o quadro", "picture", "🖼️", 0.19f, 0.43f, 0.07f),
        objeto("planta", "Plantinha", "a plantinha", "plant", "🪴", 0.47f, 0.528f, 0.06f),
        estrutura("cerca", "Cerca", "a cerca", "fence", "🪵", Estrutura.CERCA),
        objeto("nuvem", "Nuvem", "a nuvem", "cloud", "☁️", 0.80f, 0.15f, 0.13f),
        objeto("livros", "Livros", "os livros", "books", "📚", 0.52f, 0.665f, 0.06f),
        objeto("bola", "Bola", "a bola", "ball", "⚽", 0.665f, 0.99f, 0.055f),
        objeto("espelho", "Espelho", "o espelho", "mirror", "🪞", 0.60f, 0.45f, 0.06f),
        estrutura("caminho", "Caminho", "o caminho de pedrinhas", "path", "🪨", Estrutura.CAMINHO),
        objeto("bicicleta", "Bicicleta", "a bicicleta", "bike", "🚲", 0.60f, 0.92f, 0.09f),
        objeto("violao", "Violão", "o violão", "guitar", "🎸", 0.14f, 0.528f, 0.06f),
        objeto("bolo", "Bolo", "o bolo", "cake", "🎂", 0.655f, 0.735f, 0.05f),
        estrutura("lago", "Laguinho", "o laguinho", "pond", "💧", Estrutura.LAGO),
        objeto("pato", "Patinho", "o patinho", "duck", "🦆", 0.80f, 0.93f, 0.06f),
        objeto("trem", "Trenzinho", "o trenzinho", "train", "🚂", 0.31f, 0.995f, 0.07f),
        objeto("borboleta", "Borboleta", "a borboleta", "butterfly", "🦋", 0.20f, 0.86f, 0.06f),
        objeto("pipa", "Pipa", "a pipa", "kite", "🪁", 0.95f, 0.27f, 0.07f),
        objeto("escorregador", "Escorregador", "o escorregador", "slide", "🛝", 0.92f, 0.86f, 0.08f),
        objeto("arcoiris", "Arco-íris", "o arco-íris", "rainbow", "🌈", 0.27f, 0.12f, 0.12f),
        objeto("estrela", "Estrela de campeão", "a estrela de campeão", "star", "⭐", 0.50f, 0.095f, 0.07f),
    )

    val porId: Map<String, ItemCasa> = ordem.associateBy { it.id }

    /**
     * Onde as coisas do jardim ficam quando a tela é alta e tem o jardim de baixo (y de 1 a 2, ver [ItemCasa]).
     * Vale só para objetos que a criança ainda não arrastou: o resto da cena fica com espaço para respirar.
     * Os lugares desviam do caminho de pedrinhas (x perto de 0,30), dos arbustos dos cantos, do canteiro e do
     * cantinho do mascote (canto de baixo à esquerda): o cachorrinho fica logo acima dele.
     */
    val noJardimDeBaixo: Map<String, Posicao> = mapOf(
        "cachorro" to Posicao(0.17f, 1.15f),
        "bicicleta" to Posicao(0.50f, 1.50f),
        "bola" to Posicao(0.62f, 1.42f),
        "escorregador" to Posicao(0.76f, 1.45f),
        "trem" to Posicao(0.40f, 1.92f),
        "flores" to Posicao(0.56f, 1.92f),
    )

    fun lugarDe(id: String): Lugar = when (id) {
        "sol", "nuvem", "arcoiris", "pipa", "estrela", "borboleta" -> Lugar.CEU
        "arvore", "flores", "bicicleta", "pato", "escorregador" -> Lugar.FORA
        "cama", "sofa", "geladeira", "fogao", "mesa", "tv", "banheira", "abajur", "quadro",
        "livros", "espelho", "violao", "bolo" -> Lugar.DENTRO
        else -> Lugar.QUALQUER
    }

    /** O cômodo "certo" de cada móvel (para o elogio de arrumação). */
    fun comodoDe(id: String): Comodo? = when (id) {
        "cama", "ursinho", "violao" -> Comodo.QUARTO
        "banheira", "espelho" -> Comodo.BANHEIRO
        "sofa", "tv", "livros" -> Comodo.SALA
        "geladeira", "fogao", "mesa", "bolo" -> Comodo.COZINHA
        else -> null
    }

    val total get() = ordem.size

    /** Altura da cena dividida pela largura (a mesma de `PROPORCAO_CENA`, na interface). */
    const val PROPORCAO_DA_CENA = 1.15f

    /** Altura do desenho dividida pela largura: móveis desenhados têm a sua; emojis são quadrados. */
    fun proporcaoDe(item: ItemCasa): Float = when (item.desenho) {
        Desenho.GELADEIRA -> 2.1f
        Desenho.FOGAO -> 1.35f
        Desenho.MESA -> 0.7f
        null -> 1f
    }

    /** Pares que ficam um em cima do outro de propósito (o ursinho deitado na cama, o bolo na mesa). */
    val empilhadosDeProposito: Set<Set<String>> = setOf(setOf("cama", "ursinho"), setOf("mesa", "bolo"))

    /**
     * Caixa que o desenho de [item] ocupa com o pé em [pos], em frações da largura (x) e da altura (y)
     * da cena. O emoji usa ~84% da sua caixa (a fonte deixa uma margem), os móveis desenhados, 100%.
     */
    fun caixaNaCena(item: ItemCasa, pos: Posicao = Posicao(item.x, item.y)): CaixaNaCena {
        val miolo = if (item.desenho == null) 0.84f else 1f
        val largura = item.tamanho * miolo
        val altura = item.tamanho * proporcaoDe(item) * miolo / PROPORCAO_DA_CENA
        return CaixaNaCena(pos.x - largura / 2, pos.y - altura, pos.x + largura / 2, pos.y)
    }

    /**
     * Objetos (não estruturas) que, nas posições padrão da casa completa, cobrem mais que [limite]
     * do menor dos dois. Os [empilhadosDeProposito] não contam. Cada par vem com a fração coberta.
     */
    fun sobreposicoesPadrao(limite: Float = 0.25f): List<Triple<String, String, Float>> {
        val objetos = ordem.filter { !it.ehEstrutura }
        val caixas = objetos.associate { it.id to caixaNaCena(it) }
        val achadas = mutableListOf<Triple<String, String, Float>>()
        for (i in objetos.indices) for (j in i + 1 until objetos.size) {
            val a = objetos[i].id
            val b = objetos[j].id
            if (setOf(a, b) in empilhadosDeProposito) continue
            val f = caixas.getValue(a).sobreposicao(caixas.getValue(b))
            if (f > limite) achadas += Triple(a, b, f)
        }
        return achadas
    }

    /** Itens já conquistados, dado o número de fases vencidas (contando todas as trilhas). */
    fun ganhos(fasesVencidas: Int): List<ItemCasa> = ordem.take(fasesVencidas.coerceIn(0, ordem.size))
}
