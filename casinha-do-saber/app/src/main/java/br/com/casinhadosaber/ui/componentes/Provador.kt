package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.dominio.Gaveta
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.PrateleiraDaGeladeira
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.dominio.temNa
import br.com.casinhadosaber.dominio.tirando
import br.com.casinhadosaber.dominio.usa
import br.com.casinhadosaber.dominio.vestindo
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.tema.Baloo2
import br.com.casinhadosaber.ui.tema.Cores

/**
 * Provador do espelho e do guarda-roupa: uma aba por gaveta (roupas, chapéus e cada parte do corpo),
 * com ícone grande e toque de 64dp. Tocar num item que é seu veste (e o narrador diz o nome, em português
 * e em inglês); o botão ✖ da gaveta tira o que está nela.
 *
 * Cada item mostra a criança vestindo a peça (camadas 3D, miniatura parada: um bitmap composto uma vez e
 * guardado), com o ícone pequeno no canto para reconhecer peças que quase não aparecem de frente (a mochila).
 *
 * Com [precoDe], mostra também o que ainda não é seu, com o preço: tocar nele chama [onVestir] do mesmo jeito,
 * e quem chamou decide (no guarda-roupa, experimentar e depois comprar). Sem [precoDe] (espelho), só o que é seu.
 */
@Composable
fun Provador(
    perfil: Perfil,
    possui: (Produto) -> Boolean,
    onVestir: (Produto) -> Unit,
    onTirar: (Gaveta) -> Unit,
    modifier: Modifier = Modifier,
    precoDe: ((Produto) -> Int)? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var aba by rememberSaveable { mutableStateOf(Gaveta.ROUPAS) }
    val itens = Loja.daGaveta(aba).filter { precoDe != null || possui(it) }

    Column(modifier.fillMaxWidth()) {
        // Abas das gavetas.
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (g in Gaveta.entries) {
                val escolhida = g == aba
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(if (escolhida) Color(0xFFFFF0F6) else Color(0xFFEDE6FF))
                        .semantics {
                            contentDescription = g.nome
                            selected = escolhida
                        }
                        .clickable(role = Role.Tab) {
                            aba = g
                            sons.toque()
                            narrador.falar(g.nome)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    // O ícone 3D da gaveta (pelo enum, não pelo emoji: 🎈 também é conteúdo); o emoji só de reserva.
                    ImagemDeArte(
                        IconesDaCasaK2.daGaveta(g),
                        Modifier
                            .size(if (escolhida) 44.dp else 38.dp)
                            .graphicsLayer { alpha = if (escolhida) 1f else 0.7f },
                    ) { Text(g.icone, fontSize = if (escolhida) 34.sp else 28.sp) }
                    // Bolinha verde: tem alguma coisa vestida nesta gaveta.
                    if (perfil.temNa(g)) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 6.dp)
                                .size(12.dp)
                                .background(Cores.Certo, CircleShape),
                        )
                    }
                }
            }
        }
        // Conteúdo da gaveta escolhida.
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF0F6), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp, topEnd = 20.dp))
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tirar = if (aba == Gaveta.ROUPAS) "Roupa de sempre" else "Tirar"
            ItemDoProvador(
                descricao = tirar,
                usando = !perfil.temNa(aba),
                preco = null,
                onClick = {
                    sons.toque()
                    narrador.falar(if (aba == Gaveta.ROUPAS) "A roupa de sempre!" else "Pronto, tirei!")
                    onTirar(aba)
                },
            ) {
                if (aba == Gaveta.ROUPAS) {
                    Crianca(perfil.copy(traje = "", chapeu = ""), Modifier.height(ALTURA_DA_MINIATURA), animado = false)
                } else {
                    // A criança sem nada nesta gaveta, com o ✖ no canto.
                    Crianca(perfil.tirando(aba), Modifier.height(ALTURA_DA_MINIATURA), animado = false)
                    IconeNoCanto("✖️", IconesDaBarra.FECHAR)
                }
            }
            for (p in itens) {
                val tem = possui(p)
                ItemDoProvador(
                    descricao = p.nome + if (tem || precoDe == null) "" else ", ${precoDe(p)} moedinhas",
                    usando = perfil.usa(p),
                    preco = if (tem || precoDe == null) null else precoDe(p),
                    apagado = !tem,
                    onClick = {
                        if (tem) {
                            narrador.falar(
                                if (p.nomeEn.isNotBlank()) listOf(pt("${p.nome}! Em inglês:"), en(p.nomeEn)) else listOf(pt("${p.nome}!")),
                            )
                        }
                        onVestir(p)
                    },
                ) {
                    Crianca(miniaturaVestindo(perfil, p), Modifier.height(ALTURA_DA_MINIATURA), animado = false)
                    IconeNoCanto(p.icone)
                }
            }
            if (itens.isEmpty()) {
                // Gaveta vazia (no espelho): mostra que dá para comprar na lojinha da casa.
                Box(
                    Modifier
                        .graphicsLayer { alpha = 0.6f }
                        .semantics { contentDescription = "Gaveta vazia. Compre na lojinha da casa." }
                        .clickable { narrador.falar("Essa gaveta está vazia. Compre na lojinha da casa!") }
                        .padding(14.dp),
                ) {
                    ImagemDeArte(IconesDaBarra.LOJA, Modifier.size(40.dp)) { Text("🛒", fontSize = 34.sp) }
                }
            }
        }
    }
}

/** Altura da criança nas miniaturas do provador, da lojinha e do guarda-roupa. */
private val ALTURA_DA_MINIATURA = 58.dp

/**
 * O perfil para a miniatura de [p]: a criança vestindo a peça. Na roupa, sem chapéu (o chapéu esconderia a
 * cabeça do traje, como o capacete do astronauta ou a touca do chef).
 */
fun miniaturaVestindo(perfil: Perfil, p: Produto): Perfil =
    if (p.categoria == Categoria.ROUPA) perfil.vestindo(p).copy(chapeu = "") else perfil.vestindo(p)

/**
 * O ícone da peça, pequeno, no canto de cima da miniatura (só para reconhecer; a peça está na criança): a figura
 * 3D do emoji (ou o ícone [id] da arte, quando dado); o emoji só de reserva.
 */
@Composable
private fun BoxScope.IconeNoCanto(icone: String, id: String? = null) {
    val emoji: @Composable () -> Unit = { Text(icone, fontSize = 15.sp, maxLines = 1) }
    Box(
        Modifier
            .align(Alignment.TopEnd)
            .background(Color.White.copy(alpha = 0.85f), CircleShape)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (id != null) ImagemDeArte(id, Modifier.size(18.dp), reserva = emoji) else FiguraDaPergunta(icone, 18.dp, reserva = emoji)
    }
}

/** O que a lojinha (de fora e de dentro) e o guarda-roupa mostram embaixo de cada coisa. */
enum class SituacaoNaLojaK2 { TRANCADO, USANDO, NA_GELADEIRA, E_SEU, PREVIA, PRECO }

/**
 * Rodapé de uma coisa da lojinha ou do guarda-roupa, com os ícones 3D no lugar dos emojis: o cadeado, o certo e
 * "usando" ou "é seu" (o congelador para a comida que já está na geladeira), ou a moeda 3D e o preço (com a
 * mãozinha na prévia). Decorativo: quem fala é o cartão em volta.
 */
@Composable
fun RotuloDaLojaK2(situacao: SituacaoNaLojaK2, preco: Int, modifier: Modifier = Modifier, tamanho: TextUnit = 16.sp) {
    val lado = (tamanho.value * 1.15f).dp
    val estilo = TextStyle(fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold, fontSize = tamanho, lineHeight = tamanho * 1.15f)
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        val certo: @Composable () -> Unit = {
            ImagemDeArte(IconesDaBarra.CERTO, Modifier.size(lado)) { Text("✔", style = estilo, color = Cores.Certo) }
        }
        val moeda: @Composable () -> Unit = {
            ImagemDeArte(IconesDaCasaK2.MOEDA, Modifier.size(lado)) { Text("🪙", fontSize = tamanho) }
        }
        when (situacao) {
            SituacaoNaLojaK2.TRANCADO -> ImagemDeArte(IconesDaBarra.PAIS, Modifier.size(lado)) { Text("🔒", fontSize = tamanho) }
            SituacaoNaLojaK2.USANDO -> { certo(); Text("usando", style = estilo, color = Cores.Certo, maxLines = 1) }
            SituacaoNaLojaK2.E_SEU -> { certo(); Text("é seu", style = estilo, color = Cores.Certo, maxLines = 1) }
            SituacaoNaLojaK2.NA_GELADEIRA -> {
                certo()
                ImagemDeArte(IconesDaCasaK2.daPrateleira(PrateleiraDaGeladeira.CONGELADOR), Modifier.size(lado)) { Text("🧊", fontSize = tamanho) }
            }
            SituacaoNaLojaK2.PREVIA -> {
                ImagemDeArte("ui_mao_toque", Modifier.size(lado)) { Text("👆", fontSize = tamanho) }
                moeda()
                Text("$preco", style = estilo, color = Cores.TextoDaPilula, maxLines = 1)
            }
            SituacaoNaLojaK2.PRECO -> { moeda(); Text("$preco", style = estilo, color = Cores.TextoDaPilula, maxLines = 1) }
        }
    }
}

@Composable
private fun ItemDoProvador(
    descricao: String,
    usando: Boolean,
    /** Preço de quem ainda não é seu (moeda 3D e o número); nulo, nada (ou o certo, quando está usando). */
    preco: Int?,
    onClick: () -> Unit,
    apagado: Boolean = false,
    desenho: @Composable BoxScope.() -> Unit,
) {
    Column(
        Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (usando) Color(0xFFDFF7E6) else Color.White)
            .border(if (usando) 4.dp else 2.dp, if (usando) Cores.Certo else Color(0xFFEDE6FF), RoundedCornerShape(18.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = descricao
                selected = usando
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = 64.dp, height = 64.dp)
                .graphicsLayer { alpha = if (apagado) 0.6f else 1f },
            contentAlignment = Alignment.Center,
        ) { desenho() }
        when {
            preco != null -> RotuloDaLojaK2(SituacaoNaLojaK2.PRECO, preco, tamanho = 15.sp)
            // Sobre o verde claro do "usando": o certo 3D de fundo claro.
            usando -> ImagemDeArte(IconesDaBarra.CERTO, Modifier.size(20.dp)) {
                Text("✔", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Cores.Certo, maxLines = 1)
            }
        }
    }
}
