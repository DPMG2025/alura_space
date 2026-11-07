package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Trilha

// Peças do jogo em 3D que não dependem do Android (testáveis na JVM): que figura 3D (`fig_*`) entra no lugar
// de cada emoji das perguntas, e a geometria do cenário (a ilha do caminho e as cenas lagoa, mesa, varal...).

/**
 * As figuras 3D das perguntas (`fig_<nome>`, Fase 4 do PRD visual 3D). No manifesto cada figura traz `emojis`
 * (os emojis do conteúdo que ela substitui) e `lugar` (onde ela fica na cena: lagoa, mesa, varal, cercado,
 * tapete, céu ou grama). Emoji sem figura continua emoji (reserva).
 */
object FigurasDaPergunta {
    /** Emojis que já têm uma imagem 3D de outra fase (sem ser `fig_*`), usada como reserva. */
    val RESERVAS: Map<String, String> = mapOf("🪙" to "moeda")

    /** O emoji sem o seletor de variação (U+FE0F) e sem espaços: "⭐️" e "⭐" são a mesma figura. */
    fun normalizar(emoji: String): String = emoji.replace("️", "").trim()

    @Volatile private var indiceDe: Pair<ManifestoDaArte, Map<String, String>>? = null

    /** Emoji (normalizado) para o id da figura, montado uma vez por manifesto. */
    fun indice(manifesto: ManifestoDaArte): Map<String, String> {
        indiceDe?.let { (m, i) -> if (m === manifesto) return i }
        val i = HashMap<String, String>()
        for ((id, info) in manifesto.imagens) {
            if (!id.startsWith(PREFIXO)) continue
            for (e in info.emojis) i.putIfAbsent(normalizar(e), id)
        }
        for ((e, id) in RESERVAS) if (id in manifesto.imagens) i.putIfAbsent(normalizar(e), id)
        indiceDe = manifesto to i
        return i
    }

    /** Id da imagem 3D que substitui [emoji], ou nulo (o emoji fica). */
    fun idPara(manifesto: ManifestoDaArte, emoji: String): String? {
        val e = normalizar(emoji)
        if (e.isEmpty()) return null
        return indice(manifesto)[e]
    }

    /** Tem modelo 3D? */
    fun temModelo(manifesto: ManifestoDaArte, emoji: String): Boolean = idPara(manifesto, emoji) != null

    /**
     * Onde as [figuras] ficam na cena: o `lugar` da primeira que tem modelo (nas contagens todas são iguais),
     * ou nulo se nenhuma tiver.
     */
    fun lugarDaCena(manifesto: ManifestoDaArte, figuras: List<String>): String? {
        for (f in figuras) {
            val id = idPara(manifesto, f) ?: continue
            val lugar = manifesto.imagens[id]?.lugar.orEmpty()
            if (lugar.isNotEmpty()) return lugar
        }
        return null
    }

    const val PREFIXO = "fig_"
}

/** Um ponto do quadro da arte do jogo (800 x 1040 px, câmera `jogo`) e o lado da figura que fica nele. */
data class VagaDaFigura(
    /** Onde fica o pé da figura (ou, [pendurada], o topo dela: as roupas do varal). */
    val x: Float,
    val y: Float,
    /** Lado da figura, em px do quadro. */
    val lado: Float,
    val pendurada: Boolean = false,
)

/** Onde a arte do quadro (800 x 1040 px) é desenhada: canto de cima à esquerda e largura, nas unidades de quem chama. */
data class QuadroDaArte(val x: Float, val y: Float, val largura: Float) {
    val escala: Float get() = largura / QuadroDoJogo.LARGURA
    val altura: Float get() = largura * QuadroDoJogo.ALTURA / QuadroDoJogo.LARGURA
    fun emX(px: Float): Float = x + px * escala
    fun emY(py: Float): Float = y + py * escala
}

/** Uma caixa em px do quadro. */
data class CaixaDaArte(val esquerda: Float, val topo: Float, val direita: Float, val base: Float) {
    val largura: Float get() = direita - esquerda
    val altura: Float get() = base - topo
    fun unir(o: CaixaDaArte) = CaixaDaArte(minOf(esquerda, o.esquerda), minOf(topo, o.topo), maxOf(direita, o.direita), maxOf(base, o.base))
}

/**
 * Geometria do cenário do jogo (relatório da fase 4A): as camadas `jogo_*` têm todas 800 x 1040 px (400 x 520 dp),
 * o mesmo quadro e o mesmo apoio. A ilha do caminho fica atrás de tudo; as cenas comuns (lagoa, mesa, varal,
 * cercado, tapete) vão por cima da ilha, com as figuras nas âncoras `vaga_1..6`.
 */
object QuadroDoJogo {
    const val LARGURA = 800
    const val ALTURA = 1040

    /** Altura do centro da ilha no quadro (âncora `centro`, 534 de 1040). */
    const val CENTRO_DA_ILHA = 534f / ALTURA

    /** Lugares das figuras que têm uma cena comum própria (`jogo_comum_<lugar>`). */
    val LUGARES_COM_CENA = listOf("lagoa", "mesa", "varal", "cercado", "tapete")

    fun idDaIlha(trilha: Trilha): String = "jogo_" + trilha.name.lowercase() + "_ilha"

    fun idDaCena(lugar: String): String = "jogo_comum_$lugar"

    /**
     * Onde desenhar a ilha numa tela de [largura] x [altura]: com a largura da tela (deitado, a largura que cabe na
     * altura) e o centro da ilha a 43% da altura, entre o balão da pergunta e as respostas (como a maquete 2).
     */
    fun daIlha(largura: Float, altura: Float): QuadroDaArte {
        val l = minOf(largura, altura * 0.9f).coerceAtLeast(1f)
        val alturaDoQuadro = l * ALTURA / LARGURA
        return QuadroDaArte((largura - l) / 2f, altura * 0.43f - alturaDoQuadro * CENTRO_DA_ILHA, l)
    }

    /** Parte da cena com desenho (alfa), em px do quadro: o resto da camada é transparente. */
    private val desenhoDaCena = mapOf(
        "lagoa" to CaixaDaArte(0f, 278f, 800f, 824f),
        "mesa" to CaixaDaArte(17f, 279f, 772f, 736f),
        "varal" to CaixaDaArte(0f, 273f, 800f, 772f),
        "cercado" to CaixaDaArte(0f, 208f, 800f, 592f),
        "tapete" to CaixaDaArte(17f, 339f, 800f, 786f),
    )

    /** Onde ficam as figuras sem cena própria (grama e céu): o miolo da ilha. */
    private val MIOLO_DA_ILHA = CaixaDaArte(150f, 430f, 650f, 690f)

    fun desenhoDaCena(lugar: String): CaixaDaArte? = desenhoDaCena[lugar]

    /**
     * Onde ficam [n] figuras numa cena com estas [ancoras] (as da cena comum; sem cena, as da ilha):
     * até o número de vagas, uma figura por `vaga_k` (na ordem), de 190 px (até 3) ou 170 px (até 6);
     * mais que isso, espalhadas em fileiras de até 5 dentro da área útil (`area_*`), menores.
     * No varal as figuras ficam [VagaDaFigura.pendurada]s; no céu, flutuam um pouco acima da grama.
     * A lista sai de trás para a frente (quem está mais embaixo é desenhado por cima).
     */
    fun vagas(n: Int, ancoras: Map<String, PontoDaArte>, lugar: String?): List<VagaDaFigura> {
        if (n <= 0) return emptyList()
        val varal = lugar == "varal"
        val noCeu = lugar == "ceu"
        val nomeadas = (1..9).mapNotNull { k -> ancoras["vaga_$k"] }
        val lista = if (n <= nomeadas.size) {
            val lado = when {
                varal -> if (n <= 3) 170f else 150f
                n <= 3 -> 190f
                else -> 170f
            }
            nomeadas.take(n).map { VagaDaFigura(it.x.toFloat(), it.y.toFloat(), lado, pendurada = varal) }
        } else {
            espalhar(n, areaUtil(ancoras))
        }
        val subir = if (noCeu) 80f else 0f
        return lista.map { if (subir > 0f) it.copy(y = it.y - subir) else it }.sortedBy { it.y }
    }

    /** A área útil da cena (das âncoras `area_*`, encolhida para dentro da elipse) ou o miolo da ilha. */
    fun areaUtil(ancoras: Map<String, PontoDaArte>): CaixaDaArte {
        val e = ancoras["area_esquerda"]
        val d = ancoras["area_direita"]
        val f = ancoras["area_fundo"]
        val t = ancoras["area_frente"]
        if (e == null || d == null || f == null || t == null) return MIOLO_DA_ILHA
        val cx = (e.x + d.x) / 2f
        val cy = (f.y + t.y) / 2f
        val rx = (d.x - e.x) / 2f * 0.78f
        val ry = (t.y - f.y) / 2f * 0.72f
        return CaixaDaArte(cx - rx, cy - ry, cx + rx, cy + ry)
    }

    /** [n] figuras em fileiras de até 5 dentro de [area], centradas (as fileiras de trás são as de cima). */
    private fun espalhar(n: Int, area: CaixaDaArte): List<VagaDaFigura> {
        val porLinha = if (n <= 8) 4 else 5
        val linhas = (n + porLinha - 1) / porLinha
        val largura = area.largura / porLinha
        val altura = if (linhas == 1) area.altura else area.altura / (linhas - 1).coerceAtLeast(1)
        val lado = minOf(largura * 1.05f, (if (linhas == 1) area.altura else altura) * 1.7f, 170f)
        val resultado = ArrayList<VagaDaFigura>(n)
        for (l in 0 until linhas) {
            val naLinha = minOf(porLinha, n - l * porLinha)
            // Fileiras alternadas meio passo para o lado (como bolinhas num tabuleiro), para não virar coluna.
            val desvio = if (l % 2 == 1 && naLinha == porLinha) largura * 0.25f else 0f
            val y = if (linhas == 1) (area.topo + area.base) / 2f else area.topo + altura * l
            val inicio = (area.esquerda + area.direita) / 2f - largura * (naLinha - 1) / 2f + desvio
            for (k in 0 until naLinha) resultado += VagaDaFigura(inicio + largura * k, y, lado)
        }
        return resultado
    }

    /**
     * A caixa (px do quadro) que precisa aparecer: as [vagas] com o tamanho das figuras e, se houver cena, o
     * desenho dela ([desenho]).
     */
    fun caixaVisivel(vagas: List<VagaDaFigura>, desenho: CaixaDaArte?): CaixaDaArte {
        var caixa = desenho
        for (v in vagas) {
            val c = if (v.pendurada) CaixaDaArte(v.x - v.lado / 2f, v.y, v.x + v.lado / 2f, v.y + v.lado)
            else CaixaDaArte(v.x - v.lado / 2f, v.y - v.lado * 0.85f, v.x + v.lado / 2f, v.y + v.lado * 0.15f)
            caixa = caixa?.unir(c) ?: c
        }
        return caixa ?: MIOLO_DA_ILHA
    }

    /**
     * O quadro da arte que mostra [caixa] inteira numa área de [largura] x [altura] (mesma unidade), centrada, sem
     * passar do tamanho com que a ilha aparece numa tela desta largura ([larguraMaxima] do quadro).
     */
    fun paraCaber(caixa: CaixaDaArte, largura: Float, altura: Float, larguraMaxima: Float): QuadroDaArte {
        val pelaLargura = largura / caixa.largura.coerceAtLeast(1f)
        val pelaAltura = altura / caixa.altura.coerceAtLeast(1f)
        val maxima = larguraMaxima / LARGURA
        val escala = minOf(pelaLargura, pelaAltura, maxima).coerceAtLeast(0.01f)
        val cx = (caixa.esquerda + caixa.direita) / 2f
        val cy = (caixa.topo + caixa.base) / 2f
        return QuadroDaArte(largura / 2f - cx * escala, altura / 2f - cy * escala, LARGURA * escala)
    }
}
