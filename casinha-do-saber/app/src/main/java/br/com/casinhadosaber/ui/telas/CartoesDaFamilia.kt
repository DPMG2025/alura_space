package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import br.com.casinhadosaber.ui.componentes.VeuDoModal
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.tema.Raios
import br.com.casinhadosaber.dominio.CopiaDoProgresso
import br.com.casinhadosaber.dominio.ResumoDaCrianca
import br.com.casinhadosaber.dominio.CriancaDaCasa
import br.com.casinhadosaber.dominio.Familia
import br.com.casinhadosaber.dominio.resumo
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.tema.Cores

/*
 * Cartões da área dos pais para as várias crianças e para a cópia do progresso (atrás do portão de adulto).
 * Marcas de teste: "familia_adicionar", "familia_jogar_<id>", "familia_renomear_<id>", "familia_apagar_<id>",
 * "familia_nome", "familia_salvar_nome", "familia_confirmar_apagar", "copia_guardar", "copia_enviar",
 * "copia_recuperar", "copia_confirmar", "dialogo_cancelar", "copia_aviso_ok".
 */

private val Lilas = Color(0xFFD9CFF0)

/** O que a área dos pais pode fazer com as crianças do aparelho (vem do Repositorio, em MainActivity). */
data class AcoesDaFamilia(
    val criancas: List<CriancaDaCasa>,
    val ativa: String,
    val onAdicionar: () -> Unit,
    val onJogarCom: (String) -> Unit,
    val onRenomear: (String, String) -> Unit,
    val onApagar: (String) -> Unit,
)

/**
 * Cópia do progresso: guardar (arquivo escolhido pelo adulto), enviar por outro app e recuperar.
 * [pendente] é uma cópia já lida e validada esperando a confirmação; [aviso] é uma mensagem para o adulto
 * (cópia guardada, arquivo recusado).
 */
data class AcoesDaCopia(
    val onGuardar: () -> Unit,
    val onEnviar: (() -> Unit)?,
    val onRecuperar: () -> Unit,
    val pendente: CopiaDoProgresso? = null,
    val onConfirmar: () -> Unit = {},
    val onCancelar: () -> Unit = {},
    val aviso: String? = null,
    val onFecharAviso: () -> Unit = {},
    /** O que está no aparelho agora (mostrado ao lado da cópia na confirmação). */
    val atual: List<ResumoDaCrianca> = emptyList(),
    /** Desfaz a última recuperação (volta o progresso de antes dela). Null esconde o botão. */
    val onDesfazer: (() -> Unit)? = null,
)

@Composable
private fun BotaoClaro(texto: String, tag: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    BotaoFofo(onClick = onClick, modifier = modifier.testTag(tag), cor = Color.White, corBase = Lilas, habilitado = habilitado) {
        Text(
            texto,
            style = MaterialTheme.typography.titleMedium,
            color = if (habilitado) Cores.Texto else Cores.TextoSuave,
            modifier = Modifier.padding(12.dp),
        )
    }
}

/** As crianças do aparelho: quem está jogando, trocar, adicionar, renomear e apagar. */
@Composable
fun CartaoDasCriancas(familia: AcoesDaFamilia) {
    var renomeando by rememberSaveable { mutableStateOf<String?>(null) }
    var apagando by rememberSaveable { mutableStateOf<String?>(null) }
    CartaoDosPais {
        Text("👧👦 Crianças deste aparelho", style = MaterialTheme.typography.titleLarge)
        Text(
            "Cada criança tem a sua casa, as suas estrelas, o mascote, o álbum, as moedinhas e os desenhos. " +
                "Com mais de uma, o app pergunta \"Quem vai jogar?\" ao abrir. O limite de tempo vale para o aparelho: soma o tempo de todas.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        for (c in familia.criancas) {
            Spacer(Modifier.height(10.dp))
            val jogando = c.id == familia.ativa
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (jogando) Cores.Estrela.copy(alpha = 0.18f) else Color(0xFFF7F4FF), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(width = 48.dp, height = 64.dp), contentAlignment = Alignment.BottomCenter) {
                    val perfil = c.perfil
                    if (perfil != null) Crianca(perfil, Modifier.height(64.dp)) else Text("🌱", style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.nomeParaMostrar(), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        if (jogando) "jogando agora" else "toque em Jogar para trocar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Cores.TextoSuave,
                    )
                }
                if (!jogando) {
                    BotaoFofo(
                        onClick = { familia.onJogarCom(c.id) },
                        modifier = Modifier.testTag("familia_jogar_${c.id}"),
                        cor = Cores.Certo,
                        profundidade = 4.dp,
                    ) {
                        Text("Jogar", color = Color.White, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                }
                if (c.perfil != null) {
                    BotaoFofo(
                        onClick = { renomeando = c.id },
                        modifier = Modifier.testTag("familia_renomear_${c.id}"),
                        cor = Color.White,
                        corBase = Lilas,
                        descricao = "Trocar o nome",
                        profundidade = 4.dp,
                    ) { Text("✏️", modifier = Modifier.padding(10.dp)) }
                    Spacer(Modifier.width(6.dp))
                }
                BotaoFofo(
                    onClick = { apagando = c.id },
                    modifier = Modifier.testTag("familia_apagar_${c.id}"),
                    cor = Color.White,
                    corBase = Lilas,
                    descricao = "Apagar",
                    profundidade = 4.dp,
                ) { Text("🗑️", modifier = Modifier.padding(10.dp)) }
            }
        }
        Spacer(Modifier.height(12.dp))
        val cabe = familia.criancas.size < Familia.MAXIMO
        BotaoClaro(if (cabe) "➕ Adicionar uma criança" else "Já são ${Familia.MAXIMO} crianças", "familia_adicionar", familia.onAdicionar, habilitado = cabe)
    }

    renomeando?.let { id ->
        val c = familia.criancas.firstOrNull { it.id == id }
        if (c == null) {
            renomeando = null
        } else {
            DialogoRenomear(
                atual = c.perfil?.nome.orEmpty(),
                onSalvar = { nome -> renomeando = null; familia.onRenomear(id, nome) },
                onCancelar = { renomeando = null },
            )
        }
    }
    apagando?.let { id ->
        val c = familia.criancas.firstOrNull { it.id == id }
        if (c == null) {
            apagando = null
        } else {
            JanelaDosPais(onFechar = { apagando = null }) {
                CaixaDeDialogo(
                    titulo = "Apagar ${c.nomeParaMostrar()}?",
                    texto = "A casa, as estrelas, o mascote, o álbum e o personagem dessa criança serão apagados. " +
                        "As outras crianças não mudam. Isso não pode ser desfeito (a não ser por uma cópia guardada antes).",
                    confirmar = "Apagar",
                    corConfirmar = Cores.Errado,
                    tagConfirmar = "familia_confirmar_apagar",
                    onConfirmar = { apagando = null; familia.onApagar(id) },
                    onCancelar = { apagando = null },
                )
            }
        }
    }
}

@Composable
private fun DialogoRenomear(atual: String, onSalvar: (String) -> Unit, onCancelar: () -> Unit) {
    var nome by rememberSaveable { mutableStateOf(atual) }
    JanelaDosPais(onFechar = onCancelar) {
        Column(
            Modifier
                .widthIn(max = 420.dp)
                .cartaoFofo(cor = Cores.Creme, raio = Raios.Enorme)
                .padding(20.dp),
        ) {
            Text("Nome da criança", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = br.com.casinhadosaber.dominio.Familia.cortarNome(it) },
                label = { Text("Nome (opcional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth().testTag("familia_nome"),
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancelar) { Text("Cancelar") }
                TextButton(onClick = { onSalvar(nome) }, modifier = Modifier.testTag("familia_salvar_nome")) { Text("Salvar") }
            }
        }
    }
}

/** Uma caixa de confirmação com título, texto e dois botões (desenhada fora do Dialog para as capturas). */
@Composable
fun CaixaDeDialogo(
    titulo: String,
    texto: String,
    confirmar: String,
    tagConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: (() -> Unit)?,
    corConfirmar: Color = Cores.Roxo,
    conteudoExtra: @Composable () -> Unit = {},
) {
    Column(
        Modifier
            .widthIn(max = 440.dp)
            .cartaoFofo(cor = Cores.Creme, raio = Raios.Enorme)
            // Com muitas crianças (agora e na cópia) a caixa pode passar da tela: rola.
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(titulo, style = MaterialTheme.typography.titleLarge, color = Cores.Texto)
        Spacer(Modifier.height(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge, color = Cores.Texto)
        conteudoExtra()
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            if (onCancelar != null) {
                BotaoFofo(onClick = onCancelar, modifier = Modifier.testTag("dialogo_cancelar"), cor = Color.White, corBase = Lilas, profundidade = 4.dp) {
                    Text("Cancelar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
            }
            BotaoFofo(onClick = onConfirmar, modifier = Modifier.testTag(tagConfirmar), cor = corConfirmar, profundidade = 4.dp) {
                Text(confirmar, color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }
        }
    }
}

/** Cartão da cópia do progresso, sem internet: o arquivo fica com a família. */
@Composable
fun CartaoCopiaDoProgresso(copia: AcoesDaCopia) {
    CartaoDosPais {
        Text("💾 Cópia do progresso", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "Guarde uma cópia de tudo (todas as crianças) num arquivo e leve para onde quiser: Google Drive, " +
                "WhatsApp para você mesmo, Downloads ou um celular novo. O app não usa internet: a cópia só sai do aparelho se você mandar.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        BotaoClaro("📁 Guardar uma cópia", "copia_guardar", copia.onGuardar, Modifier.fillMaxWidth())
        copia.onEnviar?.let {
            Spacer(Modifier.height(10.dp))
            BotaoClaro("📤 Enviar a cópia por outro app", "copia_enviar", it, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(10.dp))
        BotaoClaro("📥 Recuperar cópia", "copia_recuperar", copia.onRecuperar, Modifier.fillMaxWidth())
        copia.onDesfazer?.let {
            Spacer(Modifier.height(10.dp))
            BotaoClaro("↩️ Desfazer a recuperação", "copia_desfazer", it, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Se o backup do Google estiver ligado no celular, o progresso também vai junto no backup do aparelho.",
            style = MaterialTheme.typography.bodySmall,
            color = Cores.TextoSuave,
        )
    }
}

/** Confirmação antes de recuperar: mostra quem está na cópia e avisa que o progresso atual será trocado. */
@Composable
fun ConfirmarRecuperarCopia(
    copia: CopiaDoProgresso,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    atual: List<ResumoDaCrianca> = emptyList(),
) {
    val data = copia.feitaEm?.let { " de ${it.dia}/${it.mes.toString().padStart(2, '0')}/${it.ano}" }.orEmpty()
    CaixaDeDialogo(
        titulo = "Recuperar esta cópia$data?",
        texto = "Isso troca o progresso atual pelo da cópia. O que está no aparelho agora será substituído " +
            "(dá para desfazer depois, na área dos pais).",
        confirmar = "Recuperar",
        tagConfirmar = "copia_confirmar",
        onConfirmar = onConfirmar,
        onCancelar = onCancelar,
        conteudoExtra = {
            if (atual.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Agora, no aparelho:", style = MaterialTheme.typography.titleMedium, color = Cores.Texto)
                for (r in atual) LinhaDoResumo(r)
            }
            Spacer(Modifier.height(10.dp))
            Text("Na cópia:", style = MaterialTheme.typography.titleMedium, color = Cores.Texto)
            for (r in copia.resumo()) LinhaDoResumo(r)
        },
    )
}

@Composable
private fun LinhaDoResumo(r: ResumoDaCrianca) {
    Text(
        "• ${r.nome}: ${r.fasesVencidas} ${if (r.fasesVencidas == 1) "fase vencida" else "fases vencidas"}",
        style = MaterialTheme.typography.bodyLarge,
        color = Cores.Texto,
    )
}

/** Diálogos da cópia (confirmação e avisos), chamados pela área dos pais. */
@Composable
fun DialogosDaCopia(copia: AcoesDaCopia) {
    copia.pendente?.let { p ->
        JanelaDosPais(onFechar = copia.onCancelar) {
            ConfirmarRecuperarCopia(p, copia.onConfirmar, copia.onCancelar, copia.atual)
        }
    }
    copia.aviso?.let { texto ->
        JanelaDosPais(onFechar = copia.onFecharAviso) {
            CaixaDeDialogo(
                titulo = "Cópia do progresso",
                texto = texto,
                confirmar = "OK",
                tagConfirmar = "copia_aviso_ok",
                onConfirmar = copia.onFecharAviso,
                onCancelar = null,
            )
        }
    }
}

/**
 * Janela por cima da área dos pais: o véu violeta do sistema de design ([VeuDoModal]) no lugar do
 * escurecido preto da janela do Android (que ficava marrom), e tocar fora fecha.
 */
@Composable
internal fun JanelaDosPais(onFechar: () -> Unit, conteudo: @Composable () -> Unit) {
    Dialog(onDismissRequest = onFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // Tira o escurecido da janela do diálogo: quem escurece é o véu.
        val vista = LocalView.current
        SideEffect { (vista.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }
        VeuDoModal(onTocarFora = onFechar, descricaoFora = "Fechar") {
            // Toque dentro do cartão não fecha (o véu atrás é que fecha).
            Box(Modifier.pointerInput(Unit) { detectTapGestures { } }, contentAlignment = Alignment.Center) { conteudo() }
        }
    }
}
