@file:OptIn(ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package br.com.fipeplaca.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.Texto
import br.com.fipeplaca.data.TipoVeiculo
import br.com.fipeplaca.data.VeiculoPlaca

/** Acoes da tela de placa (separadas para permitir previas e testes de captura). */
data class AcoesPlaca(
    val alterarPlaca: (String) -> Unit = {},
    val consultar: () -> Unit = {},
    val limpar: () -> Unit = {},
    val abrirSalvo: (ConsultaSalva) -> Unit = {},
    val selecionarVersao: (Int) -> Unit = {},
    val favoritar: () -> Unit = {},
    val compartilhar: (FipeResultado) -> Unit = {},
    val copiarCodigo: (String) -> Unit = {},
    val mudarPeriodo: (br.com.fipeplaca.data.PeriodoHistorico) -> Unit = {},
    val abrirConfiguracoes: () -> Unit = {},
    val buscarManual: (TipoVeiculo, String?) -> Unit = { _, _ -> },
    val abrirTabela: () -> Unit = {},
    val recarregarHistorico: () -> Unit = {},
)

@Composable
fun TelaPlaca(
    estado: PlacaUi,
    historico: HistoricoUi,
    recentes: List<ConsultaSalva>,
    favorito: Boolean,
    acoes: AcoesPlaca,
    modifier: Modifier = Modifier,
    semTokenFipe: Boolean = false,
    placaConfigurada: Boolean = true,
) {
    val foco = LocalFocusManager.current
    val buscar = { foco.clearFocus(); acoes.consultar() }

    val rolagem = rememberScrollState()
    val resultadoVisivel = remember { BringIntoViewRequester() }
    // Quando o resultado chega, rola ate ele (o valor nao fica escondido abaixo da busca)
    var ultimoRolado by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(estado.veiculo?.placa, estado.carregando) {
        val chave = estado.veiculo?.placa
        if (chave != null && !estado.carregando && chave != ultimoRolado) {
            ultimoRolado = chave
            kotlinx.coroutines.delay(150)
            resultadoVisivel.bringIntoView()
        } else if (chave == null) ultimoRolado = null
    }
    Column(modifier.fillMaxSize().verticalScroll(rolagem)) {
        Cabecalho(
            titulo = "FIPE pela Placa",
            subtitulo = "Valor de mercado e histórico em segundos",
        ) { Spacer(Modifier.height(56.dp)) }

        // Cartao de busca sobreposto ao cabecalho
        Secao(Modifier.padding(horizontal = 16.dp).subir(48.dp)) {
            Text("Digite a placa do veículo", style = MaterialTheme.typography.titleMedium)
            Text("Placa antiga ou Mercosul", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            CampoPlaca(estado.placa, acoes.alterarPlaca, buscar, Modifier.fillMaxWidth(), habilitado = !estado.carregando)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = buscar,
                    enabled = !estado.carregando && estado.placa.length == 7,
                    modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    if (estado.carregando) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(10.dp))
                        Text("Consultando…")
                    } else {
                        Icon(Icons.Filled.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Consultar valor")
                    }
                }
                if (estado.placa.isNotEmpty() || estado.veiculo != null) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = acoes.limpar) { Icon(Icons.Filled.Clear, "Limpar") }
                }
            }
        }

        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (estado.semToken) {
                CartaoAviso(
                    Icons.Filled.Settings,
                    "Falta conectar o serviço de placas",
                    "Para buscar pela placa, informe o token do serviço de consulta veicular (por exemplo, API Placas). " +
                        "A busca por marca e modelo funciona sem token.",
                    "Configurar agora",
                    acoes.abrirConfiguracoes,
                )
            }
            estado.erro?.let { CartaoErro(it) }

            if (estado.carregando) {
                CartaoCarregando()
            }

            val v = estado.veiculo
            if (v == null && !estado.carregando && !placaConfigurada && !estado.semToken) {
                CartaoAviso(
                    Icons.Filled.Info, "Busca pela placa precisa de configuração",
                    "A placa é identificada por um serviço de consulta veicular (pago). Sem ele, consulte pela Tabela FIPE, " +
                        "que é gratuita e tem todas as marcas e modelos.",
                    "Abrir a Tabela", acoes.abrirTabela,
                )
            }
            if (v == null && !estado.carregando) {
                ConteudoInicial(recentes, acoes.abrirSalvo)
                AtalhoTabela(acoes.abrirTabela)
            }

            AnimatedVisibility(
                visible = v != null,
                enter = fadeIn() + slideInVertically { it / 8 },
                modifier = Modifier.bringIntoViewRequester(resultadoVisivel),
            ) {
                if (v != null) ResultadoPlaca(v, estado, historico, favorito, acoes, semTokenFipe)
            }
        }
    }
}

@Composable
private fun ConteudoInicial(recentes: List<ConsultaSalva>, abrir: (ConsultaSalva) -> Unit) {
    val placas = recentes.filter { it.porPlaca }.take(6)
    if (placas.isNotEmpty()) {
        Text("Consultas recentes", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            placas.forEach { c ->
                Column(
                    Modifier.width(150.dp)
                        .clickable(onClickLabel = "Abrir consulta") { abrir(c) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Placa ${c.veiculo!!.placa}, ${c.titulo}, ${c.principal?.valor.orEmpty()}"
                        },
                ) {
                    PlacaMercosul(c.veiculo!!.placa, Modifier.fillMaxWidth(), altura = 44.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(c.titulo, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(c.principal?.valor.orEmpty(), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Secao(titulo = "Como funciona") {
            Passo("1", "Digite a placa", "Aceita o formato antigo (ABC1234) e o Mercosul (ABC1D23).")
            Passo("2", "Veja o valor FIPE", "Identificamos o modelo e o código FIPE e mostramos o valor de mercado atual.")
            Passo("3", "Acompanhe o histórico", "Evolução do preço no ano, em 12 ou em 24 meses, com gráfico.")
        }
    }
}

@Composable
private fun AtalhoTabela(abrir: () -> Unit) {
    Surface(
        onClick = abrir,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconeCirculo(Icons.AutoMirrored.Filled.List)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Não tem a placa?", style = MaterialTheme.typography.titleSmall)
                Text("Consulte qualquer carro, moto ou caminhão pela marca, modelo e ano.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun Passo(numero: String, titulo: String, texto: String) {
    Row(Modifier.padding(vertical = 6.dp)) {
        Avatar(numero, tamanho = 32.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ResultadoPlaca(v: VeiculoPlaca, estado: PlacaUi, historico: HistoricoUi, favorito: Boolean, acoes: AcoesPlaca, semTokenFipe: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Secao {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlacaMercosul(v.placa, Modifier.width(148.dp), altura = 48.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(listOfNotNull(v.marca?.let(Texto::marca), v.modelo?.let(Texto::modelo)).joinToString(" "), style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    when {
                        estado.avisoAtualizacao != null -> Text(estado.avisoAtualizacao,
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        estado.doSalvo -> Text("Atualizando o valor…", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            TextButton(onClick = acoes.limpar, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)) {
                Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Consultar outra placa")
            }
            GradeInfo(
                listOf(
                    "Ano" to listOfNotNull(v.anoFabricacao, v.anoModelo).distinct().joinToString("/").ifBlank { null },
                    "Cor" to v.cor,
                    "Combustível" to v.combustivel,
                    "Cidade" to listOfNotNull(v.municipio, v.uf).joinToString(" · ").ifBlank { null },
                    "Situação" to v.situacao,
                    "Chassi (final)" to v.chassiFinal,
                )
            )
        }

        if (v.fipe.isEmpty()) {
            CartaoErro(
                "O serviço de placas não informou o código FIPE deste veículo. Continue pela busca de marca e modelo.",
                "Buscar por marca e modelo",
            ) { acoes.buscarManual(v.tipo, v.marca) }
        } else {
            ValoresPlaca(v, estado, historico, favorito, acoes, semTokenFipe)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ValoresPlaca(v: VeiculoPlaca, estado: PlacaUi, historico: HistoricoUi, favorito: Boolean, acoes: AcoesPlaca, semTokenFipe: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        val sel = v.fipe.getOrNull(estado.selecionado) ?: v.fipe.first()
        CartaoValorFipe(
            sel, favorito, acoes.favoritar,
            onCompartilhar = { acoes.compartilhar(sel) },
            onCopiarCodigo = { acoes.copiarCodigo(sel.codigoFipe) },
        )

        if (v.fipe.size > 1) {
            Secao(titulo = "Versões compatíveis (${v.fipe.size})") {
                Text("A placa corresponde a mais de uma versão na tabela FIPE. Escolha a do seu veículo:",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Column(Modifier.selectableGroup()) {
                v.fipe.forEachIndexed { i, r ->
                    // Opcao de selecao unica: o leitor de tela anuncia "selecionado"
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (i == estado.selecionado) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        border = BorderStroke(1.dp, if (i == estado.selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(selected = i == estado.selecionado, role = Role.RadioButton) { acoes.selecionarVersao(i) },
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = i == estado.selecionado, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(Texto.modelo(r.modelo), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(listOf(r.codigoFipe, r.score?.let { "compatibilidade $it%" }).filterNotNull().joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(r.valor.orEmpty(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                }
            }
        }

        CartaoHistorico(historico, acoes.mudarPeriodo, acoes.recarregarHistorico, acoes.abrirConfiguracoes, semTokenFipe)

        OutlinedButton(
            onClick = { acoes.buscarManual(v.tipo, v.marca) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(),
        ) { Text("Não é este modelo? Buscar por marca e modelo") }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "A tabela FIPE é referência de preço médio; o valor de venda pode variar com estado de conservação e quilometragem.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
