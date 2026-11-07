package br.com.casinhadosaber.ui.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

// Contas de contraste e de tingimento da interface de brinquedo (Fase 9 do PRD visual 3D: acabamento).
// Só cores, sem Android: os testes de unidade conferem as contas.

/** Razão de contraste da WCAG entre duas cores opacas (1 a 21). */
fun contraste(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

/** Contraste mínimo do texto branco num botão desligado (WCAG 3:1 para texto grande, com folga). */
const val CONTRASTE_DO_DESLIGADO = 3.2f

/** O lilás acinzentado para onde a cor de um botão desligado vai: fosco, sem cara de botão vivo. */
private val FOSCO_DO_DESLIGADO = Color(0xFF686286)

/** A partir desta luminância o botão é claro (texto escuro): desligado, ele só clareia. */
private const val LUMINANCIA_DE_BOTAO_CLARO = 0.75f

/** Quanto a face clareia no alto (o degradê do bloco, [br.com.casinhadosaber.ui.tema.Degrades.bloco]). */
private const val CLARO_DO_ALTO = 0.22f

/** O botão é claro (branco, creme), com texto escuro por cima. */
fun botaoClaro(cor: Color): Boolean = cor.luminance() >= LUMINANCIA_DE_BOTAO_CLARO

/**
 * A face de um botão colorido desligado: a própria cor puxada para um lilás fosco, só o bastante para o texto
 * branco ler bem ([CONTRASTE_DO_DESLIGADO] até no alto da face, que é a parte mais clara do degradê). Fica
 * apagada (sem saturação, sem brilho) e não parece ativa, mas o texto continua legível. Botão claro: a cor clareada.
 */
fun faceDesligada(cor: Color): Color {
    if (botaoClaro(cor)) return clarear(cor, 0.5f)
    var t = 0.5f
    while (t <= 1f) {
        val face = lerp(cor, FOSCO_DO_DESLIGADO, t)
        if (contraste(lerp(face, Color.White, CLARO_DO_ALTO), Color.White) >= CONTRASTE_DO_DESLIGADO) return face
        t += 0.05f
    }
    return lerp(FOSCO_DO_DESLIGADO, Color.Black, 0.15f)
}

/** O lábio (base) de um botão desligado, um tom abaixo da face. */
fun baseDesligada(cor: Color, corBase: Color): Color =
    if (botaoClaro(cor)) clarear(corBase, 0.45f) else lerp(faceDesligada(cor), Color.Black, 0.22f)

/**
 * Filtro que pinta a estrela vazia (`estrela_vazia`, plástico cinza claro) na cor do cartão em que ela está, mais
 * clarinha, como nas maquetes: guarda a luz e a sombra da estrela (a luminância) e troca o cinza pela cor.
 */
fun filtroDaEstrelaVazia(corDoCartao: Color, claro: Float = 0.35f): ColorFilter {
    val alvo = lerp(corDoCartao, Color.White, claro)
    // A estrela vazia tem luminância média por volta de 0,83: nesse ponto sai exatamente o [alvo].
    val k = 1f / 0.83f / 3f
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                alvo.red * k, alvo.red * k, alvo.red * k, 0f, 0f,
                alvo.green * k, alvo.green * k, alvo.green * k, 0f, 0f,
                alvo.blue * k, alvo.blue * k, alvo.blue * k, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}
