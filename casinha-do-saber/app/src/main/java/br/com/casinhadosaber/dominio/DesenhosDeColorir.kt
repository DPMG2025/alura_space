package br.com.casinhadosaber.dominio

/**
 * Catálogo do Livro de Colorir: desenhos feitos de formas simples num quadrado de 0 a 1 (y cresce para baixo).
 * Cada região leva a cor da versão bonita ([DesenhoDeColorir.coresPadrao]); o quebra-cabeça usa essa versão como imagem.
 * Novos desenhos entram aqui, em [Desenhos.todos], sem mexer em mais nada.
 */
private class Construtor(val id: String, val titulo: String, val colecao: String, val emoji: String) {
    val regioes = ArrayList<Regiao>()
    val cores = ArrayList<Int>()

    private fun add(f: Forma, cor: Int) { regioes += Regiao(f); cores += cor }

    fun el(cx: Float, cy: Float, rx: Float, ry: Float, cor: Int) = add(Forma.Elipse(cx, cy, rx, ry), cor)
    fun bola(cx: Float, cy: Float, r: Float, cor: Int) = el(cx, cy, r, r, cor)
    fun ret(x0: Float, y0: Float, x1: Float, y1: Float, cor: Int) =
        add(Forma.Poligono(listOf(PontoDoDesenho(x0, y0), PontoDoDesenho(x1, y0), PontoDoDesenho(x1, y1), PontoDoDesenho(x0, y1))), cor)
    fun tri(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, cor: Int) =
        add(Forma.Poligono(listOf(PontoDoDesenho(x0, y0), PontoDoDesenho(x1, y1), PontoDoDesenho(x2, y2))), cor)
    fun poli(cor: Int, vararg xy: Float) {
        require(xy.size >= 6 && xy.size % 2 == 0)
        add(Forma.Poligono(List(xy.size / 2) { PontoDoDesenho(xy[2 * it], xy[2 * it + 1]) }), cor)
    }
    /** Estrela de [pontas] pontas. */
    fun estrela(cx: Float, cy: Float, r: Float, cor: Int, pontas: Int = 5) {
        val pts = ArrayList<PontoDoDesenho>()
        for (i in 0 until pontas * 2) {
            val ang = -Math.PI / 2 + i * Math.PI / pontas
            val raio = if (i % 2 == 0) r else r * 0.45f
            pts += PontoDoDesenho(cx + (raio * Math.cos(ang)).toFloat(), cy + (raio * Math.sin(ang)).toFloat())
        }
        add(Forma.Poligono(pts), cor)
    }

    fun sol(cx: Float = 0.85f, cy: Float = 0.15f) { bola(cx, cy, 0.09f, 3) }
    fun nuvem(cx: Float, cy: Float) { el(cx, cy, 0.12f, 0.055f, 6); el(cx - 0.05f, cy - 0.04f, 0.06f, 0.05f, 6) }
    fun chao(topo: Float, cor: Int) = ret(0f, topo, 1f, 1f, cor)
    fun fundo(cor: Int) = ret(0f, 0f, 1f, 1f, cor)

    fun pronto() = DesenhoDeColorir(id, titulo, colecao, emoji, regioes.toList(), cores.toList())
}

private fun desenho(id: String, titulo: String, colecao: String, emoji: String, f: Construtor.() -> Unit): DesenhoDeColorir =
    Construtor(id, titulo, colecao, emoji).apply(f).pronto()

object Desenhos {
    val colecoes: List<Pair<String, String>> = listOf(
        "bichos" to "🐾 Bichos",
        "mar" to "🌊 Mar",
        "casa" to "🏠 Casa e jardim",
        "veiculos" to "🚗 Carros e aviões",
        "espaco" to "🚀 Espaço",
        "festa" to "🎈 Festa",
        "dino" to "🦖 Dinossauros",
        "castelo" to "🏰 Castelos e princesas",
        "fazenda" to "🚜 Fazenda",
        "natureza" to "🌈 Natureza",
        "frutas" to "🍎 Frutas",
        "brinquedos" to "🧸 Brinquedos",
    )

    fun nomeDaColecao(id: String): String = colecoes.firstOrNull { it.first == id }?.second ?: id

    val todos: List<DesenhoDeColorir> = listOf(
        // ---------------------------------------------------------------- Bichos
        desenho("gato", "Gatinho", "bichos", "🐱") {
            fundo(3)
            chao(0.78f, 4)
            tri(0.3f, 0.34f, 0.26f, 0.14f, 0.42f, 0.26f, 2)
            tri(0.7f, 0.34f, 0.74f, 0.14f, 0.58f, 0.26f, 2)
            el(0.5f, 0.72f, 0.22f, 0.2f, 2)
            bola(0.5f, 0.42f, 0.22f, 2)
            el(0.4f, 0.4f, 0.04f, 0.05f, 12)
            el(0.6f, 0.4f, 0.04f, 0.05f, 12)
            tri(0.47f, 0.5f, 0.53f, 0.5f, 0.5f, 0.55f, 9)
            el(0.5f, 0.75f, 0.1f, 0.12f, 3)
        },
        desenho("passaro", "Passarinho", "bichos", "🐦") {
            fundo(6)
            nuvem(0.25f, 0.2f)
            sol()
            ret(0.1f, 0.52f, 0.9f, 0.57f, 10)
            el(0.5f, 0.42f, 0.2f, 0.14f, 7)
            bola(0.66f, 0.3f, 0.1f, 7)
            tri(0.74f, 0.29f, 0.74f, 0.35f, 0.86f, 0.32f, 2)
            tri(0.3f, 0.4f, 0.1f, 0.32f, 0.3f, 0.5f, 8)
            el(0.48f, 0.42f, 0.09f, 0.06f, 5)
            bola(0.69f, 0.28f, 0.015f, 12)
            chao(0.8f, 4)
        },
        desenho("coelho", "Coelhinho", "bichos", "🐰") {
            fundo(9)
            chao(0.8f, 4)
            el(0.4f, 0.2f, 0.06f, 0.18f, 11)
            el(0.6f, 0.2f, 0.06f, 0.18f, 11)
            el(0.4f, 0.22f, 0.025f, 0.12f, 9)
            el(0.6f, 0.22f, 0.025f, 0.12f, 9)
            el(0.5f, 0.68f, 0.2f, 0.18f, 11)
            bola(0.5f, 0.45f, 0.18f, 11)
            bola(0.43f, 0.42f, 0.02f, 12)
            bola(0.57f, 0.42f, 0.02f, 12)
            bola(0.5f, 0.5f, 0.03f, 1)
            el(0.5f, 0.74f, 0.1f, 0.1f, 3)
        },
        desenho("borboleta", "Borboleta", "bichos", "🦋") {
            fundo(6)
            chao(0.85f, 4)
            el(0.32f, 0.38f, 0.2f, 0.17f, 9)
            el(0.68f, 0.38f, 0.2f, 0.17f, 9)
            el(0.34f, 0.62f, 0.15f, 0.13f, 8)
            el(0.66f, 0.62f, 0.15f, 0.13f, 8)
            bola(0.33f, 0.38f, 0.06f, 3)
            bola(0.67f, 0.38f, 0.06f, 3)
            el(0.5f, 0.5f, 0.035f, 0.2f, 12)
            bola(0.5f, 0.28f, 0.04f, 12)
        },
        // ---------------------------------------------------------------- Mar
        desenho("peixe", "Peixinho", "mar", "🐠") {
            fundo(6)
            ret(0f, 0.86f, 1f, 1f, 3)
            tri(0.76f, 0.5f, 0.95f, 0.32f, 0.95f, 0.68f, 3)
            el(0.5f, 0.5f, 0.3f, 0.18f, 2)
            tri(0.42f, 0.34f, 0.6f, 0.34f, 0.5f, 0.2f, 1)
            bola(0.36f, 0.46f, 0.035f, 12)
            bola(0.18f, 0.28f, 0.03f, 7)
            bola(0.12f, 0.16f, 0.02f, 7)
        },
        desenho("baleia", "Baleia", "mar", "🐳") {
            fundo(6)
            ret(0f, 0.7f, 1f, 1f, 7)
            tri(0.74f, 0.55f, 0.95f, 0.35f, 0.9f, 0.62f, 7)
            el(0.45f, 0.58f, 0.34f, 0.2f, 7)
            el(0.45f, 0.65f, 0.28f, 0.1f, 6)
            bola(0.28f, 0.52f, 0.03f, 12)
            el(0.4f, 0.26f, 0.015f, 0.08f, 5)
            el(0.35f, 0.2f, 0.07f, 0.02f, 5)
            el(0.45f, 0.2f, 0.07f, 0.02f, 5)
        },
        desenho("polvo", "Polvo", "mar", "🐙") {
            fundo(5)
            ret(0f, 0.9f, 1f, 1f, 3)
            el(0.2f, 0.72f, 0.06f, 0.16f, 8)
            el(0.35f, 0.78f, 0.06f, 0.14f, 8)
            el(0.5f, 0.8f, 0.06f, 0.12f, 8)
            el(0.65f, 0.78f, 0.06f, 0.14f, 8)
            el(0.8f, 0.72f, 0.06f, 0.16f, 8)
            bola(0.5f, 0.4f, 0.26f, 8)
            bola(0.4f, 0.38f, 0.045f, 12)
            bola(0.6f, 0.38f, 0.045f, 12)
            el(0.5f, 0.52f, 0.07f, 0.03f, 9)
        },
        desenho("estrela_do_mar", "Estrela do mar", "mar", "⭐") {
            fundo(6)
            chao(0.72f, 3)
            estrela(0.5f, 0.52f, 0.34f, 2)
            bola(0.5f, 0.52f, 0.07f, 1)
            bola(0.2f, 0.82f, 0.03f, 11)
            bola(0.8f, 0.86f, 0.04f, 10)
        },
        // ---------------------------------------------------------------- Casa e jardim
        desenho("casinha", "Casinha", "casa", "🏠") {
            fundo(6)
            sol()
            nuvem(0.25f, 0.18f)
            chao(0.78f, 4)
            ret(0.25f, 0.45f, 0.75f, 0.82f, 3)
            tri(0.2f, 0.45f, 0.5f, 0.2f, 0.8f, 0.45f, 1)
            ret(0.44f, 0.6f, 0.56f, 0.82f, 10)
            ret(0.3f, 0.52f, 0.4f, 0.62f, 7)
            ret(0.6f, 0.52f, 0.7f, 0.62f, 7)
            ret(0.64f, 0.26f, 0.7f, 0.4f, 11)
        },
        desenho("arvore", "Árvore", "casa", "🌳") {
            fundo(6)
            sol(0.15f, 0.15f)
            chao(0.8f, 4)
            ret(0.44f, 0.5f, 0.56f, 0.82f, 10)
            bola(0.5f, 0.35f, 0.24f, 4)
            bola(0.35f, 0.45f, 0.14f, 4)
            bola(0.65f, 0.45f, 0.14f, 4)
            bola(0.45f, 0.3f, 0.04f, 1)
            bola(0.6f, 0.4f, 0.04f, 1)
            bola(0.4f, 0.5f, 0.04f, 1)
        },
        desenho("flor", "Flor", "casa", "🌷") {
            fundo(6)
            chao(0.8f, 4)
            ret(0.48f, 0.45f, 0.52f, 0.85f, 4)
            el(0.37f, 0.67f, 0.1f, 0.04f, 4)
            el(0.63f, 0.72f, 0.1f, 0.04f, 4)
            bola(0.5f, 0.2f, 0.09f, 9)
            bola(0.5f, 0.42f, 0.09f, 9)
            bola(0.38f, 0.31f, 0.09f, 9)
            bola(0.62f, 0.31f, 0.09f, 9)
            bola(0.5f, 0.31f, 0.07f, 3)
        },
        desenho("cachorro", "Casinha do cachorro", "casa", "🐶") {
            fundo(6)
            chao(0.78f, 4)
            ret(0.2f, 0.48f, 0.72f, 0.82f, 2)
            tri(0.15f, 0.48f, 0.46f, 0.25f, 0.77f, 0.48f, 1)
            el(0.46f, 0.7f, 0.12f, 0.12f, 12)
            bola(0.46f, 0.62f, 0.07f, 10)
            el(0.84f, 0.7f, 0.07f, 0.1f, 10)
            bola(0.84f, 0.58f, 0.07f, 10)
        },
        // ---------------------------------------------------------------- Carros e aviões
        desenho("carro", "Carrinho", "veiculos", "🚗") {
            fundo(6)
            nuvem(0.7f, 0.2f)
            chao(0.7f, 11)
            poli(1, 0.2f, 0.55f, 0.3f, 0.38f, 0.65f, 0.38f, 0.78f, 0.55f)
            ret(0.12f, 0.52f, 0.88f, 0.72f, 1)
            ret(0.34f, 0.43f, 0.46f, 0.52f, 6)
            ret(0.52f, 0.43f, 0.64f, 0.52f, 6)
            bola(0.3f, 0.74f, 0.09f, 12)
            bola(0.7f, 0.74f, 0.09f, 12)
            bola(0.3f, 0.74f, 0.04f, 11)
            bola(0.7f, 0.74f, 0.04f, 11)
        },
        desenho("aviao", "Avião", "veiculos", "✈️") {
            fundo(6)
            nuvem(0.2f, 0.75f)
            nuvem(0.8f, 0.25f)
            tri(0.2f, 0.5f, 0.1f, 0.3f, 0.34f, 0.46f, 7)
            tri(0.4f, 0.5f, 0.55f, 0.18f, 0.62f, 0.5f, 7)
            tri(0.4f, 0.5f, 0.55f, 0.82f, 0.62f, 0.5f, 7)
            el(0.5f, 0.5f, 0.38f, 0.1f, 11)
            el(0.74f, 0.48f, 0.08f, 0.04f, 6)
            bola(0.5f, 0.49f, 0.025f, 6)
            bola(0.4f, 0.49f, 0.025f, 6)
        },
        desenho("barco", "Barquinho", "veiculos", "⛵") {
            fundo(6)
            sol()
            ret(0f, 0.7f, 1f, 1f, 7)
            tri(0.5f, 0.15f, 0.5f, 0.6f, 0.78f, 0.6f, 3)
            tri(0.46f, 0.25f, 0.46f, 0.6f, 0.24f, 0.6f, 1)
            ret(0.48f, 0.12f, 0.5f, 0.64f, 10)
            poli(10, 0.2f, 0.62f, 0.8f, 0.62f, 0.7f, 0.78f, 0.3f, 0.78f)
        },
        desenho("trem", "Trenzinho", "veiculos", "🚂") {
            fundo(6)
            chao(0.72f, 4)
            ret(0f, 0.7f, 1f, 0.74f, 11)
            ret(0.12f, 0.45f, 0.5f, 0.68f, 1)
            ret(0.5f, 0.35f, 0.62f, 0.68f, 7)
            ret(0.22f, 0.3f, 0.3f, 0.45f, 12)
            ret(0.55f, 0.28f, 0.7f, 0.35f, 1)
            ret(0.66f, 0.48f, 0.9f, 0.66f, 3)
            bola(0.2f, 0.72f, 0.07f, 12)
            bola(0.42f, 0.72f, 0.07f, 12)
            bola(0.58f, 0.72f, 0.05f, 12)
            bola(0.78f, 0.72f, 0.06f, 12)
        },
        // ---------------------------------------------------------------- Espaço
        desenho("foguete", "Foguete", "espaco", "🚀") {
            fundo(12)
            estrela(0.15f, 0.2f, 0.05f, 3)
            estrela(0.85f, 0.3f, 0.04f, 3)
            estrela(0.2f, 0.8f, 0.04f, 3)
            estrela(0.8f, 0.8f, 0.05f, 3)
            tri(0.4f, 0.75f, 0.5f, 0.98f, 0.6f, 0.75f, 2)
            tri(0.34f, 0.7f, 0.2f, 0.84f, 0.38f, 0.5f, 1)
            tri(0.66f, 0.7f, 0.8f, 0.84f, 0.62f, 0.5f, 1)
            el(0.5f, 0.45f, 0.14f, 0.32f, 11)
            tri(0.36f, 0.28f, 0.5f, 0.05f, 0.64f, 0.28f, 1)
            bola(0.5f, 0.42f, 0.07f, 6)
        },
        desenho("lua", "Lua e estrelas", "espaco", "🌙") {
            fundo(12)
            bola(0.5f, 0.5f, 0.3f, 3)
            bola(0.62f, 0.44f, 0.26f, 12)
            estrela(0.2f, 0.2f, 0.06f, 3)
            estrela(0.8f, 0.2f, 0.05f, 11)
            estrela(0.2f, 0.78f, 0.05f, 11)
            estrela(0.82f, 0.8f, 0.07f, 3)
        },
        desenho("planeta", "Planeta", "espaco", "🪐") {
            fundo(12)
            estrela(0.15f, 0.2f, 0.05f, 3)
            estrela(0.85f, 0.15f, 0.04f, 11)
            estrela(0.85f, 0.85f, 0.05f, 3)
            el(0.5f, 0.5f, 0.42f, 0.1f, 9)
            bola(0.5f, 0.5f, 0.24f, 8)
            el(0.42f, 0.42f, 0.07f, 0.04f, 7)
            bola(0.58f, 0.6f, 0.05f, 7)
        },
        desenho("alienigena", "Alienígena", "espaco", "👽") {
            fundo(12)
            estrela(0.12f, 0.15f, 0.05f, 3)
            estrela(0.88f, 0.2f, 0.04f, 3)
            el(0.5f, 0.82f, 0.18f, 0.16f, 4)
            el(0.5f, 0.42f, 0.28f, 0.3f, 4)
            el(0.4f, 0.4f, 0.07f, 0.1f, 12)
            el(0.6f, 0.4f, 0.07f, 0.1f, 12)
            el(0.5f, 0.6f, 0.08f, 0.02f, 1)
            el(0.28f, 0.1f, 0.01f, 0.07f, 4)
            bola(0.28f, 0.05f, 0.025f, 1)
            el(0.72f, 0.1f, 0.01f, 0.07f, 4)
            bola(0.72f, 0.05f, 0.025f, 1)
        },
        // ---------------------------------------------------------------- Festa
        desenho("bolo", "Bolo de aniversário", "festa", "🎂") {
            fundo(9)
            chao(0.8f, 10)
            ret(0.2f, 0.5f, 0.8f, 0.8f, 3)
            ret(0.2f, 0.5f, 0.8f, 0.58f, 11)
            ret(0.3f, 0.3f, 0.7f, 0.5f, 9)
            ret(0.38f, 0.14f, 0.42f, 0.3f, 7)
            ret(0.58f, 0.14f, 0.62f, 0.3f, 4)
            el(0.4f, 0.1f, 0.025f, 0.04f, 2)
            el(0.6f, 0.1f, 0.025f, 0.04f, 2)
        },
        desenho("balao", "Balões", "festa", "🎈") {
            fundo(6)
            ret(0.19f, 0.7f, 0.21f, 1f, 11)
            ret(0.49f, 0.74f, 0.51f, 1f, 11)
            ret(0.79f, 0.68f, 0.81f, 1f, 11)
            el(0.2f, 0.5f, 0.12f, 0.17f, 1)
            el(0.5f, 0.5f, 0.12f, 0.17f, 3)
            el(0.8f, 0.46f, 0.12f, 0.17f, 8)
            el(0.2f, 0.2f, 0.1f, 0.14f, 4)
            el(0.5f, 0.2f, 0.1f, 0.14f, 9)
            el(0.8f, 0.18f, 0.1f, 0.14f, 2)
        },
        desenho("presente", "Presente", "festa", "🎁") {
            fundo(8)
            chao(0.82f, 11)
            ret(0.22f, 0.45f, 0.78f, 0.82f, 1)
            ret(0.18f, 0.36f, 0.82f, 0.48f, 3)
            ret(0.46f, 0.36f, 0.54f, 0.82f, 3)
            el(0.4f, 0.28f, 0.1f, 0.08f, 3)
            el(0.6f, 0.28f, 0.1f, 0.08f, 3)
            bola(0.5f, 0.3f, 0.04f, 2)
        },
        desenho("chapeu", "Chapéu de festa", "festa", "🥳") {
            fundo(5)
            estrela(0.15f, 0.2f, 0.06f, 3)
            estrela(0.85f, 0.25f, 0.05f, 9)
            estrela(0.2f, 0.8f, 0.05f, 1)
            estrela(0.82f, 0.78f, 0.06f, 3)
            tri(0.3f, 0.85f, 0.5f, 0.2f, 0.7f, 0.85f, 8)
            poli(3, 0.4f, 0.55f, 0.6f, 0.55f, 0.65f, 0.65f, 0.35f, 0.65f)
            poli(1, 0.33f, 0.75f, 0.67f, 0.75f, 0.7f, 0.85f, 0.3f, 0.85f)
            bola(0.5f, 0.17f, 0.06f, 2)
        },
        // ---------------------------------------------------------------- Dinossauros
        desenho("trex", "Tiranossauro", "dino", "🦖") {
            fundo(6)
            sol()
            chao(0.82f, 4)
            tri(0.2f, 0.6f, 0.02f, 0.8f, 0.24f, 0.74f, 4)
            el(0.42f, 0.58f, 0.24f, 0.2f, 4)
            poli(4, 0.55f, 0.5f, 0.62f, 0.3f, 0.8f, 0.28f, 0.72f, 0.52f)
            el(0.78f, 0.3f, 0.13f, 0.09f, 4)
            bola(0.8f, 0.27f, 0.018f, 12)
            ret(0.32f, 0.72f, 0.42f, 0.9f, 5)
            ret(0.5f, 0.72f, 0.6f, 0.9f, 5)
            bola(0.4f, 0.5f, 0.04f, 3)
            bola(0.5f, 0.62f, 0.035f, 3)
        },
        desenho("tricerato", "Tricerátopo", "dino", "🦕") {
            fundo(3)
            chao(0.8f, 4)
            ret(0.3f, 0.7f, 0.4f, 0.88f, 11)
            ret(0.5f, 0.7f, 0.6f, 0.88f, 11)
            el(0.42f, 0.6f, 0.26f, 0.18f, 11)
            el(0.7f, 0.5f, 0.15f, 0.16f, 9)
            el(0.76f, 0.58f, 0.13f, 0.1f, 11)
            tri(0.78f, 0.5f, 0.92f, 0.3f, 0.86f, 0.52f, 1)
            tri(0.66f, 0.46f, 0.7f, 0.28f, 0.74f, 0.46f, 1)
            bola(0.78f, 0.55f, 0.018f, 12)
        },
        desenho("pterodatilo", "Pterodátilo", "dino", "🦅") {
            fundo(6)
            nuvem(0.2f, 0.2f)
            nuvem(0.78f, 0.78f)
            tri(0.45f, 0.5f, 0.02f, 0.3f, 0.12f, 0.72f, 8)
            tri(0.55f, 0.5f, 0.98f, 0.3f, 0.88f, 0.72f, 8)
            el(0.5f, 0.52f, 0.09f, 0.2f, 2)
            el(0.5f, 0.3f, 0.07f, 0.08f, 2)
            tri(0.56f, 0.28f, 0.78f, 0.32f, 0.56f, 0.35f, 3)
            bola(0.52f, 0.27f, 0.015f, 12)
        },
        desenho("ovo_dino", "Ovo de dinossauro", "dino", "🥚") {
            fundo(5)
            chao(0.78f, 3)
            el(0.5f, 0.8f, 0.32f, 0.1f, 10)
            el(0.5f, 0.55f, 0.17f, 0.24f, 3)
            bola(0.45f, 0.5f, 0.04f, 4)
            bola(0.57f, 0.62f, 0.05f, 4)
            bola(0.6f, 0.5f, 0.03f, 4)
            tri(0.4f, 0.36f, 0.5f, 0.46f, 0.6f, 0.36f, 12)
        },
        // ---------------------------------------------------------------- Castelos e princesas
        desenho("castelo", "Castelo", "castelo", "🏰") {
            fundo(6)
            sol()
            chao(0.82f, 4)
            ret(0.25f, 0.45f, 0.75f, 0.84f, 11)
            ret(0.16f, 0.3f, 0.32f, 0.84f, 11)
            ret(0.68f, 0.3f, 0.84f, 0.84f, 11)
            tri(0.13f, 0.3f, 0.24f, 0.1f, 0.35f, 0.3f, 1)
            tri(0.65f, 0.3f, 0.76f, 0.1f, 0.87f, 0.3f, 1)
            poli(10, 0.43f, 0.84f, 0.43f, 0.64f, 0.5f, 0.56f, 0.57f, 0.64f, 0.57f, 0.84f)
            ret(0.2f, 0.42f, 0.28f, 0.54f, 7)
            ret(0.72f, 0.42f, 0.8f, 0.54f, 7)
        },
        desenho("coroa", "Coroa", "castelo", "👑") {
            fundo(9)
            ret(0.22f, 0.55f, 0.78f, 0.72f, 3)
            poli(3, 0.22f, 0.55f, 0.22f, 0.3f, 0.36f, 0.44f, 0.5f, 0.24f, 0.64f, 0.44f, 0.78f, 0.3f, 0.78f, 0.55f)
            bola(0.5f, 0.64f, 0.035f, 1)
            bola(0.34f, 0.64f, 0.03f, 8)
            bola(0.66f, 0.64f, 0.03f, 8)
            bola(0.22f, 0.27f, 0.03f, 1)
            bola(0.5f, 0.21f, 0.03f, 1)
            bola(0.78f, 0.27f, 0.03f, 1)
        },
        desenho("princesa", "Princesa", "castelo", "👸") {
            fundo(8)
            chao(0.88f, 4)
            tri(0.5f, 0.4f, 0.22f, 0.9f, 0.78f, 0.9f, 9)
            el(0.5f, 0.44f, 0.1f, 0.12f, 9)
            bola(0.5f, 0.28f, 0.085f, 2)
            el(0.5f, 0.2f, 0.11f, 0.07f, 10)
            tri(0.43f, 0.17f, 0.5f, 0.07f, 0.57f, 0.17f, 3)
            bola(0.47f, 0.28f, 0.012f, 12)
            bola(0.53f, 0.28f, 0.012f, 12)
        },
        desenho("dragao", "Dragão", "castelo", "🐉") {
            fundo(8)
            chao(0.82f, 4)
            tri(0.2f, 0.62f, 0.02f, 0.5f, 0.22f, 0.74f, 4)
            tri(0.38f, 0.5f, 0.52f, 0.2f, 0.62f, 0.5f, 1)
            el(0.45f, 0.62f, 0.25f, 0.18f, 4)
            el(0.75f, 0.45f, 0.12f, 0.1f, 4)
            tri(0.84f, 0.45f, 0.98f, 0.36f, 0.98f, 0.54f, 2)
            bola(0.78f, 0.41f, 0.016f, 12)
            ret(0.34f, 0.74f, 0.44f, 0.9f, 5)
            ret(0.52f, 0.74f, 0.62f, 0.9f, 5)
        },
        // ---------------------------------------------------------------- Fazenda
        desenho("vaca", "Vaquinha", "fazenda", "🐮") {
            fundo(6)
            chao(0.78f, 4)
            ret(0.15f, 0.5f, 0.2f, 0.72f, 11)
            el(0.45f, 0.6f, 0.27f, 0.18f, 11)
            bola(0.35f, 0.55f, 0.06f, 12)
            bola(0.55f, 0.65f, 0.05f, 12)
            ret(0.28f, 0.72f, 0.35f, 0.9f, 11)
            ret(0.55f, 0.72f, 0.62f, 0.9f, 11)
            el(0.78f, 0.45f, 0.12f, 0.1f, 11)
            tri(0.72f, 0.38f, 0.75f, 0.27f, 0.79f, 0.37f, 3)
            el(0.85f, 0.5f, 0.07f, 0.05f, 9)
            bola(0.8f, 0.42f, 0.015f, 12)
        },
        desenho("porco", "Porquinho", "fazenda", "🐷") {
            fundo(6)
            chao(0.78f, 4)
            tri(0.24f, 0.58f, 0.1f, 0.5f, 0.24f, 0.66f, 9)
            el(0.5f, 0.6f, 0.28f, 0.2f, 9)
            ret(0.33f, 0.74f, 0.41f, 0.9f, 9)
            ret(0.58f, 0.74f, 0.66f, 0.9f, 9)
            bola(0.76f, 0.52f, 0.13f, 9)
            tri(0.68f, 0.42f, 0.72f, 0.3f, 0.78f, 0.42f, 1)
            el(0.86f, 0.56f, 0.06f, 0.05f, 2)
            bola(0.78f, 0.48f, 0.015f, 12)
        },
        desenho("galinha", "Galinha", "fazenda", "🐔") {
            fundo(6)
            chao(0.8f, 4)
            tri(0.3f, 0.55f, 0.18f, 0.4f, 0.3f, 0.66f, 2)
            el(0.5f, 0.6f, 0.22f, 0.2f, 3)
            el(0.44f, 0.6f, 0.1f, 0.07f, 2)
            ret(0.45f, 0.77f, 0.48f, 0.9f, 2)
            ret(0.54f, 0.77f, 0.57f, 0.9f, 2)
            bola(0.62f, 0.35f, 0.1f, 3)
            el(0.62f, 0.24f, 0.04f, 0.05f, 1)
            tri(0.71f, 0.35f, 0.83f, 0.38f, 0.71f, 0.42f, 2)
            bola(0.65f, 0.33f, 0.014f, 12)
        },
        desenho("trator", "Trator", "fazenda", "🚜") {
            fundo(6)
            chao(0.75f, 4)
            ret(0.34f, 0.28f, 0.38f, 0.46f, 11)
            ret(0.3f, 0.45f, 0.72f, 0.66f, 4)
            ret(0.5f, 0.24f, 0.7f, 0.45f, 1)
            ret(0.54f, 0.29f, 0.66f, 0.41f, 6)
            bola(0.6f, 0.7f, 0.15f, 12)
            bola(0.28f, 0.76f, 0.09f, 12)
            bola(0.6f, 0.7f, 0.06f, 11)
            bola(0.28f, 0.76f, 0.035f, 11)
        },
        // ---------------------------------------------------------------- Natureza
        desenho("arco_iris", "Arco-íris", "natureza", "🌈") {
            fundo(6)
            nuvem(0.2f, 0.2f)
            nuvem(0.8f, 0.22f)
            el(0.5f, 0.6f, 0.4f, 0.4f, 1)
            el(0.5f, 0.6f, 0.34f, 0.34f, 2)
            el(0.5f, 0.6f, 0.28f, 0.28f, 3)
            el(0.5f, 0.6f, 0.22f, 0.22f, 4)
            el(0.5f, 0.6f, 0.16f, 0.16f, 7)
            el(0.5f, 0.6f, 0.1f, 0.1f, 8)
            ret(0f, 0.6f, 1f, 1f, 4)
        },
        desenho("cogumelo", "Cogumelo", "natureza", "🍄") {
            fundo(5)
            chao(0.8f, 4)
            ret(0.42f, 0.5f, 0.58f, 0.82f, 3)
            el(0.5f, 0.48f, 0.3f, 0.22f, 1)
            bola(0.38f, 0.4f, 0.05f, 3)
            bola(0.55f, 0.35f, 0.06f, 3)
            bola(0.64f, 0.5f, 0.04f, 3)
        },
        desenho("montanha", "Montanhas", "natureza", "⛰️") {
            fundo(6)
            sol()
            tri(0.1f, 0.8f, 0.4f, 0.3f, 0.7f, 0.8f, 11)
            tri(0.33f, 0.43f, 0.4f, 0.3f, 0.47f, 0.43f, 14)
            tri(0.5f, 0.8f, 0.75f, 0.42f, 0.95f, 0.8f, 10)
            chao(0.78f, 4)
        },
        desenho("girassol", "Girassol", "natureza", "🌻") {
            fundo(6)
            chao(0.82f, 4)
            ret(0.48f, 0.48f, 0.52f, 0.86f, 4)
            el(0.4f, 0.7f, 0.07f, 0.03f, 4)
            bola(0.5f, 0.13f, 0.07f, 3)
            bola(0.62f, 0.18f, 0.07f, 2)
            bola(0.67f, 0.3f, 0.07f, 3)
            bola(0.62f, 0.42f, 0.07f, 2)
            bola(0.5f, 0.47f, 0.07f, 3)
            bola(0.38f, 0.42f, 0.07f, 2)
            bola(0.33f, 0.3f, 0.07f, 3)
            bola(0.38f, 0.18f, 0.07f, 2)
            bola(0.5f, 0.3f, 0.1f, 10)
        },
        // ---------------------------------------------------------------- Frutas
        desenho("maca", "Maçã", "frutas", "🍎") {
            fundo(5)
            chao(0.85f, 4)
            ret(0.48f, 0.18f, 0.52f, 0.34f, 10)
            el(0.6f, 0.2f, 0.1f, 0.05f, 4)
            bola(0.5f, 0.58f, 0.28f, 1)
            bola(0.4f, 0.5f, 0.05f, 9)
        },
        desenho("banana", "Banana", "frutas", "🍌") {
            fundo(6)
            chao(0.85f, 4)
            poli(3, 0.15f, 0.4f, 0.25f, 0.55f, 0.4f, 0.7f, 0.6f, 0.78f, 0.8f, 0.72f, 0.9f, 0.6f, 0.92f, 0.5f, 0.85f, 0.55f, 0.75f, 0.65f, 0.6f, 0.68f, 0.45f, 0.6f, 0.32f, 0.45f, 0.25f, 0.32f)
            ret(0.1f, 0.28f, 0.2f, 0.4f, 10)
        },
        desenho("melancia", "Melancia", "frutas", "🍉") {
            fundo(6)
            chao(0.82f, 3)
            poli(4, 0.92f,0.3f,0.899f,0.43f,0.84f,0.547f,0.747f,0.64f,0.63f,0.699f,0.5f,0.72f,0.37f,0.699f,0.253f,0.64f,0.16f,0.547f,0.101f,0.43f,0.08f,0.3f)
            poli(1, 0.85f,0.3f,0.833f,0.408f,0.783f,0.506f,0.706f,0.583f,0.608f,0.633f,0.5f,0.65f,0.392f,0.633f,0.294f,0.583f,0.217f,0.506f,0.167f,0.408f,0.15f,0.3f)
            bola(0.4f, 0.5f, 0.025f, 12)
            bola(0.5f, 0.56f, 0.025f, 12)
            bola(0.6f, 0.5f, 0.025f, 12)
        },
        desenho("uva", "Cacho de uvas", "frutas", "🍇") {
            fundo(5)
            ret(0.48f, 0.08f, 0.52f, 0.24f, 10)
            el(0.62f, 0.14f, 0.1f, 0.05f, 4)
            bola(0.36f, 0.34f, 0.07f, 8)
            bola(0.5f, 0.34f, 0.07f, 8)
            bola(0.64f, 0.34f, 0.07f, 8)
            bola(0.42f, 0.48f, 0.07f, 8)
            bola(0.58f, 0.48f, 0.07f, 8)
            bola(0.35f, 0.5f, 0.07f, 9)
            bola(0.65f, 0.5f, 0.07f, 9)
            bola(0.5f, 0.62f, 0.07f, 8)
            bola(0.5f, 0.76f, 0.07f, 9)
        },
        // ---------------------------------------------------------------- Brinquedos
        desenho("bola", "Bola", "brinquedos", "⚽") {
            fundo(6)
            chao(0.8f, 4)
            bola(0.5f, 0.55f, 0.32f, 7)
            el(0.5f, 0.55f, 0.32f, 0.1f, 3)
            bola(0.5f, 0.55f, 0.1f, 1)
        },
        desenho("ursinho", "Ursinho", "brinquedos", "🧸") {
            fundo(9)
            bola(0.33f, 0.25f, 0.08f, 10)
            bola(0.67f, 0.25f, 0.08f, 10)
            el(0.5f, 0.78f, 0.22f, 0.2f, 10)
            el(0.5f, 0.8f, 0.13f, 0.13f, 2)
            bola(0.5f, 0.4f, 0.2f, 10)
            el(0.5f, 0.46f, 0.09f, 0.07f, 2)
            bola(0.5f, 0.44f, 0.02f, 12)
            bola(0.42f, 0.35f, 0.016f, 12)
            bola(0.58f, 0.35f, 0.016f, 12)
        },
        desenho("pipa", "Pipa", "brinquedos", "🪁") {
            fundo(6)
            nuvem(0.2f, 0.2f)
            nuvem(0.8f, 0.85f)
            poli(1, 0.5f, 0.1f, 0.75f, 0.4f, 0.5f, 0.75f, 0.25f, 0.4f)
            tri(0.5f, 0.1f, 0.75f, 0.4f, 0.5f, 0.4f, 3)
            tri(0.25f, 0.4f, 0.5f, 0.4f, 0.5f, 0.75f, 3)
            ret(0.49f, 0.75f, 0.51f, 0.92f, 11)
            bola(0.5f, 0.82f, 0.03f, 8)
        },
        desenho("blocos", "Blocos de montar", "brinquedos", "🧱") {
            fundo(3)
            chao(0.85f, 10)
            ret(0.2f, 0.55f, 0.5f, 0.85f, 1)
            ret(0.5f, 0.55f, 0.8f, 0.85f, 7)
            ret(0.35f, 0.25f, 0.65f, 0.55f, 4)
            bola(0.35f, 0.7f, 0.07f, 3)
            bola(0.65f, 0.7f, 0.07f, 3)
            bola(0.5f, 0.4f, 0.07f, 3)
        },
    )

    val porId: Map<String, DesenhoDeColorir> = todos.associateBy { it.id }

    fun daColecao(colecao: String): List<DesenhoDeColorir> = todos.filter { it.colecao == colecao }
}
