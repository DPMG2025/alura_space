@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package br.com.fipeplaca.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fipeplaca.data.Placa

/** Cabecalho com degrade da marca; desenha por baixo da barra de status. */
@Composable
fun Cabecalho(
    titulo: String,
    subtitulo: String? = null,
    acoes: @Composable () -> Unit = {},
    compacto: Boolean = false,
    conteudo: @Composable ColumnScope.() -> Unit = {},
) {
    val c = Extras.cores
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(degradeMarca())
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = if (compacto) 0.dp else 12.dp, bottom = if (compacto) 12.dp else 20.dp),
    ) {
        if (!compacto) Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                Text(titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White,
                    modifier = Modifier.semantics { heading() })
                if (subtitulo != null) {
                    Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f))
                }
            }
            acoes()
        }
        Column(Modifier.padding(end = 8.dp), content = conteudo)
    }
}

/** Placa no padrao Mercosul (faixa azul com BRASIL), apenas exibicao. */
@Composable
fun PlacaMercosul(placa: String, modifier: Modifier = Modifier, altura: Dp = 56.dp) {
    val texto = Placa.formatar(placa).ifBlank { "ABC1D23" }
    // Placa no formato antigo (ABC-1234) usa a moldura cinza, sem a faixa Mercosul
    val mercosul = '-' !in texto
    MolduraPlaca(modifier.clearAndSetSemantics { contentDescription = "Placa ${texto.toList().joinToString(" ")}" }, altura, mercosul) {
        Text(
            texto,
            style = estiloPlaca(altura, maxWidth, texto.length, LocalDensity.current.fontScale),
            color = Color(0xFF111111),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Campo de digitacao com aparencia de placa Mercosul. */
@Composable
fun CampoPlaca(valor: String, onValor: (String) -> Unit, onBuscar: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    val altura = 76.dp
    MolduraPlaca(modifier, altura) {
        val estilo = estiloPlaca(altura, maxWidth, 7, LocalDensity.current.fontScale)
        BasicTextField(
            value = valor,
            onValueChange = onValor,
            enabled = habilitado,
            singleLine = true,
            textStyle = estilo.copy(color = Color(0xFF111111), textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                // Teclado sem sugestoes nem texto "em composicao" (sublinhado) para a placa
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Extras.cores.placaAzul),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Placa do veículo" },
            decorationBox = { campo ->
                Box(contentAlignment = Alignment.Center) {
                    if (valor.isEmpty()) {
                        Text("ABC1D23", style = estilo, color = Color(0x73111111),
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    campo()
                }
            },
        )
    }
}

/** Fonte monoespacada que cabe na largura disponivel (cada caractere ocupa cerca de 0,7 em). */
private fun estiloPlaca(altura: Dp, largura: Dp, caracteres: Int, escalaFonte: Float = 1f): TextStyle {
    val porAltura = altura.value * 0.46f
    val porLargura = largura.value / (caracteres.coerceAtLeast(1) * 0.70f)
    // A placa e um elemento grafico de tamanho fixo: o texto acompanha a moldura, nao a escala de fonte
    val tamanho = minOf(porAltura, porLargura) / escalaFonte
    return TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = tamanho.sp,
        letterSpacing = (tamanho * 0.08f).sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )
}

@Composable
private fun estiloFaixa(tamanho: Float): TextStyle {
    val t = tamanho / LocalDensity.current.fontScale
    return TextStyle(
    color = Color.White,
    fontWeight = FontWeight.Bold,
    fontSize = t.sp,
    lineHeight = t.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    )
}

@Composable
private fun MolduraPlaca(modifier: Modifier, altura: Dp, mercosul: Boolean = true, conteudo: @Composable BoxWithConstraintsScope.() -> Unit) {
    if (!mercosul) {
        Box(
            modifier.clip(RoundedCornerShape(altura * 0.14f)).background(Color(0xFFC9CDD3))
                .border(if (altura < 44.dp) 1.5.dp else 2.dp, Color(0xFF3A3F47), RoundedCornerShape(altura * 0.14f)),
        ) {
            BoxWithConstraints(
                Modifier.fillMaxWidth().height(altura).padding(horizontal = altura * 0.12f),
                contentAlignment = Alignment.Center,
                content = conteudo,
            )
        }
        return
    }
    val faixa = altura * 0.26f
    val compacta = altura < 44.dp
    Column(
        modifier
            .clip(RoundedCornerShape(altura * 0.14f))
            .background(Color.White)
            .border(if (compacta) 1.5.dp else 2.dp, Color(0xFF1A1A1A), RoundedCornerShape(altura * 0.14f)),
    ) {
        Row(
            Modifier.fillMaxWidth().height(faixa).background(Extras.cores.placaAzul).padding(horizontal = altura * 0.14f)
                .clearAndSetSemantics {}, // "BR BRASIL" e bandeira sao decorativos
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!compacta) Text("BR", style = estiloFaixa(faixa.value * 0.55f))
            Text(
                "BRASIL", style = estiloFaixa(faixa.value * 0.6f).copy(letterSpacing = (faixa.value * 0.12f).sp),
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f), maxLines = 1, softWrap = false,
            )
            // Bandeira estilizada
            Box(Modifier.size(width = faixa * 0.95f, height = faixa * 0.64f).background(Color(0xFF009C3B)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(faixa * 0.34f).clip(CircleShape).background(Color(0xFF002776)))
            }
        }
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(altura - faixa).padding(horizontal = altura * 0.12f),
            contentAlignment = Alignment.Center,
            content = conteudo,
        )
    }
}

/**
 * Desenha o elemento [deslocamento] acima da sua posicao e devolve esse espaco ao layout,
 * para sobrepor cartoes ao cabecalho sem deixar sobra no fim da tela.
 */
fun Modifier.subir(deslocamento: Dp): Modifier = layout { medida, restricoes ->
    val p = medida.measure(restricoes)
    val dy = deslocamento.roundToPx()
    layout(p.width, (p.height - dy).coerceAtLeast(0)) { p.place(0, -dy) }
}

/** Orientacao neutra (nao e erro), com acao opcional. */
@Composable
fun CartaoAviso(icone: ImageVector, titulo: String, mensagem: String, acao: String? = null, onAcao: () -> Unit = {}) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icone, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(2.dp))
                Text(mensagem, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium)
                if (acao != null) {
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onAcao, shape = MaterialTheme.shapes.medium) { Text(acao) }
                }
            }
        }
    }
}

/** Cartao padrao do app, com titulo opcional. */
@Composable
fun Secao(
    modifier: Modifier = Modifier,
    titulo: String? = null,
    acao: @Composable () -> Unit = {},
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(18.dp)) {
            if (titulo != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).semantics { heading() })
                    acao()
                }
                Spacer(Modifier.height(10.dp))
            }
            conteudo()
        }
    }
}

/** Par rotulo e valor em grade de duas colunas. */
@Composable
fun GradeInfo(itens: List<Pair<String, String?>>) {
    val visiveis = itens.filter { !it.second.isNullOrBlank() }
    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        visiveis.forEach { (rotulo, valor) ->
            Column(Modifier.weight(1f)) {
                Text(rotulo.uppercase(), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                Text(valor!!, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
        // Mantem a ultima celula com largura de meia linha quando a quantidade e impar.
        if (visiveis.size % 2 == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
fun CartaoErro(mensagem: String, acao: String? = null, onAcao: () -> Unit = {}) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.large,
        // Leitor de tela anuncia o erro assim que ele aparece
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(mensagem, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                if (acao != null) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onAcao,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) { Text(acao) }
                }
            }
        }
    }
}

/** Estado vazio com icone em circulo, titulo e texto de apoio. */
@Composable
fun EstadoVazio(icone: ImageVector, titulo: String, texto: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(icone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp)) }
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** Paleta suave para avatares; a cor e fixa para cada nome. */
private val TONS = listOf(0xFF1747B0, 0xFF00695C, 0xFF5E35B1, 0xFFB4501A, 0xFFAD1457, 0xFF2E7D32, 0xFF00695F, 0xFF6D4C41)

private val TONS_ESCURO = listOf(0xFF9DBBFF, 0xFF6FD6C6, 0xFFC5A8FF, 0xFFFFB38A, 0xFFFF94BD, 0xFF8FD694, 0xFF7FD8E0, 0xFFD7B8A8)

/** Avatar circular com as iniciais (marcas e salvos). */
@Composable
fun Avatar(texto: String, modifier: Modifier = Modifier, tamanho: Dp = 40.dp) {
    val iniciais = texto.split(' ', '-', '/').filter { it.isNotBlank() && it[0].isLetterOrDigit() }
        .take(2).joinToString("") { it.take(1) }.uppercase().ifBlank { "?" }
    val indice = (texto.lowercase().hashCode() and 0x7fffffff) % TONS.size
    val escuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val tom = Color(if (escuro) TONS_ESCURO[indice] else TONS[indice])
    Box(
        modifier.size(tamanho).clip(CircleShape).background(tom.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(iniciais, color = tom, fontWeight = FontWeight.Bold, fontSize = (tamanho.value * 0.36f).sp)
    }
}

/** Icone dentro de um circulo na cor da marca. */
@Composable
fun IconeCirculo(icone: ImageVector, tamanho: Dp = 44.dp) {
    Box(
        Modifier.size(tamanho).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) { Icon(icone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(tamanho * 0.5f)) }
}

/** Bloco cinza pulsante usado como esqueleto de carregamento. */
@Composable
fun Esqueleto(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "esqueleto")
    val alfa by t.animateFloat(
        initialValue = 0.35f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse), label = "alfa",
    )
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f * alfa + 0.04f)))
}

@Composable
fun CartaoCarregando() {
    Secao {
        Esqueleto(Modifier.width(140.dp).height(14.dp))
        Spacer(Modifier.height(12.dp))
        Esqueleto(Modifier.width(220.dp).height(34.dp))
        Spacer(Modifier.height(18.dp))
        repeat(3) {
            Row {
                Esqueleto(Modifier.weight(1f).height(32.dp))
                Spacer(Modifier.width(12.dp))
                Esqueleto(Modifier.weight(1f).height(32.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
