package br.com.fipeplaca.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fipeplaca.BuildConfig
import br.com.fipeplaca.FipeApp
import br.com.fipeplaca.data.Compartilhar
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.PontoHistorico
import br.com.fipeplaca.data.VeiculoPlaca
import kotlinx.coroutines.launch

private data class Aba(val titulo: String, val icone: ImageVector)

private val ABAS = listOf(
    Aba("Placa", Icons.Filled.Search),
    Aba("Tabela", Icons.AutoMirrored.Filled.List),
    Aba("Salvos", Icons.Filled.Favorite),
    Aba("Ajustes", Icons.Filled.Settings),
)

@Composable
fun AppRaiz() {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FipeApp
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    var aba by rememberSaveable { mutableIntStateOf(0) }
    var ajustesSalvos by remember { mutableIntStateOf(0) }
    var confirmarLimpeza by remember { mutableStateOf(false) }

    val placaVm: PlacaViewModel = viewModel()
    val manualVm: ManualViewModel = viewModel()
    val placa by placaVm.estado.collectAsStateWithLifecycle()
    val histPlaca by placaVm.historico.estado.collectAsStateWithLifecycle()
    val manual by manualVm.estado.collectAsStateWithLifecycle()
    val histManual by manualVm.historico.estado.collectAsStateWithLifecycle()
    val salvos by placaVm.itensSalvos.collectAsStateWithLifecycle()
    val tema by app.tema.collectAsStateWithLifecycle()

    fun aviso(texto: String) = escopo.launch { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(texto) }
    fun compartilhar(r: FipeResultado, v: VeiculoPlaca?, pontos: List<PontoHistorico>) {
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, Compartilhar.texto(r, v, pontos))
        }
        contexto.startActivity(Intent.createChooser(envio, "Compartilhar consulta"))
    }
    fun copiar(codigo: String) {
        clipboard.setText(AnnotatedString(codigo))
        aviso("Código FIPE $codigo copiado")
    }
    fun favoritou(id: String, antes: Boolean) = aviso(if (antes) "Removido dos favoritos" else "Salvo nos favoritos")

    BackHandler(enabled = aba == 0 && placa.veiculo != null) { placaVm.limpar() }
    BackHandler(enabled = aba == 1 && manual.etapa != Etapa.MARCAS) { manualVm.voltar() }
    BackHandler(enabled = aba != 0 && !(aba == 1 && manual.etapa != Etapa.MARCAS)) { aba = 0 }

    if (confirmarLimpeza) {
        AlertDialog(
            onDismissRequest = { confirmarLimpeza = false },
            title = { Text("Limpar recentes?") },
            text = { Text("As consultas recentes serão apagadas. Os favoritos continuam salvos.") },
            confirmButton = {
                TextButton(onClick = { confirmarLimpeza = false; app.salvos.limparRecentes(); aviso("Recentes apagados") }) { Text("Limpar") }
            },
            dismissButton = { TextButton(onClick = { confirmarLimpeza = false }) { Text("Cancelar") } },
        )
    }

    // Telas largas ou deitadas: barra lateral no lugar da barra inferior (mais altura para o conteudo)
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val lateral = config.screenWidthDp >= 600 && config.screenWidthDp > config.screenHeightDp
    // Cada aba guarda o proprio estado (rolagem, campos, sub-abas) ao trocar de aba
    val estadosDasAbas = androidx.compose.runtime.saveable.rememberSaveableStateHolder()

  androidx.compose.foundation.layout.Box {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar, Modifier.imePadding()) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { if (!lateral) BarraNavegacao(aba) { aba = it } },
    ) { padding ->
      Row(Modifier.padding(padding).consumeWindowInsets(padding)) {
        if (lateral) TrilhoNavegacao(aba) { aba = it }
        Crossfade(
            targetState = aba,
            label = "abas",
            // O teclado nao cobre o conteudo; em paisagem, o conteudo respeita o recorte da tela
            modifier = Modifier.weight(1f).imePadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) { atual ->
          estadosDasAbas.SaveableStateProvider(atual) {
            when (atual) {
                0 -> {
                    val id = placa.veiculo?.let { ConsultaSalva.idPlaca(it.placa) }
                    val fav = id != null && salvos.any { it.id == id && it.favorito }
                    TelaPlaca(
                        estado = placa, historico = histPlaca, recentes = salvos, favorito = fav,
                        semTokenFipe = app.prefs.tokenFipe.isBlank(),
                        acoes = AcoesPlaca(
                            alterarPlaca = placaVm::alterarPlaca,
                            consultar = placaVm::consultar,
                            limpar = placaVm::limpar,
                            abrirSalvo = placaVm::abrirSalvo,
                            selecionarVersao = placaVm::selecionarVersao,
                            favoritar = { placaVm.alternarFavorito(); if (id != null) favoritou(id, fav) },
                            compartilhar = { r -> compartilhar(r, placa.veiculo, histPlaca.pontos) },
                            copiarCodigo = ::copiar,
                            mudarPeriodo = placaVm.historico::mudarPeriodo,
                            abrirConfiguracoes = { aba = 3 },
                            buscarManual = { tipo, marca -> manualVm.preencherPorPlaca(tipo, marca); aba = 1 },
                            abrirTabela = { aba = 1 },
                        ),
                    )
                }
                1 -> {
                    val id = manual.resultado?.let { ConsultaSalva.idManual(it) }
                    val fav = id != null && salvos.any { it.id == id && it.favorito }
                    TelaManual(
                        estado = manual, historico = histManual, favorito = fav,
                        semTokenFipe = app.prefs.tokenFipe.isBlank(),
                        acoes = AcoesManual(
                            selecionarTipo = { manualVm.selecionarTipo(it) },
                            alterarFiltro = manualVm::alterarFiltro,
                            selecionarMarca = manualVm::selecionarMarca,
                            selecionarModelo = manualVm::selecionarModelo,
                            selecionarAno = manualVm::selecionarAno,
                            voltar = { manualVm.voltar() },
                            irPara = manualVm::irPara,
                            tentarNovamente = manualVm::tentarNovamente,
                            novaConsulta = manualVm::novaConsulta,
                            favoritar = { manualVm.alternarFavorito(); if (id != null) favoritou(id, fav) },
                            compartilhar = { r -> compartilhar(r, null, histManual.pontos) },
                            copiarCodigo = ::copiar,
                            mudarPeriodo = manualVm.historico::mudarPeriodo,
                            recarregarHistorico = { manual.resultado?.let { manualVm.historico.carregar(it) } },
                            abrirConfiguracoes = { aba = 3 },
                        ),
                    )
                }
                2 -> TelaSalvos(
                    itens = salvos,
                    acoes = AcoesSalvos(
                        abrir = { c ->
                            if (c.porPlaca) { placaVm.abrirSalvo(c); aba = 0 } else { manualVm.abrirSalvo(c); aba = 1 }
                        },
                        alternarFavorito = { app.salvos.alternarFavorito(it) },
                        remover = { id ->
                            val item = salvos.firstOrNull { it.id == id }
                            app.salvos.remover(id)
                            // Sem dispensar o aviso anterior: cada exclusao mantem o seu "Desfazer" (os avisos entram em fila)
                            escopo.launch {
                                val r = snackbar.showSnackbar("Consulta excluída", actionLabel = "Desfazer", duration = SnackbarDuration.Short)
                                if (r == SnackbarResult.ActionPerformed && item != null) app.salvos.restaurar(item)
                            }
                        },
                        limparRecentes = { confirmarLimpeza = true },
                    ),
                )
                else -> androidx.compose.runtime.key(ajustesSalvos) { TelaConfiguracoes(
                    inicial = Configuracoes(app.prefs.tokenPlaca, app.prefs.urlPlaca, app.prefs.tokenFipe),
                    versao = BuildConfig.VERSION_NAME,
                    tema = tema,
                    onTema = app::definirTema,
                    onSalvar = { cfg ->
                        app.prefs.tokenPlaca = cfg.tokenPlaca
                        app.prefs.urlPlaca = cfg.urlPlaca
                        app.prefs.tokenFipe = cfg.tokenFipe
                        ajustesSalvos++
                        placaVm.historico.esquecerCache()
                        manualVm.historico.esquecerCache()
                        aviso("Ajustes salvos")
                    },
                ) }
            }
          }
        }
      }
    }
    // Faixa fixa atras da barra de status: os icones (claros) continuam legiveis quando o conteudo rola
    Spacer(
        Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)
            .background(Extras.cores.gradienteInicio),
    )
  }
}

@Composable
private fun TrilhoNavegacao(aba: Int, onAba: (Int) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical)),
    ) {
        Spacer(Modifier.weight(1f))
        ABAS.forEachIndexed { i, a ->
            NavigationRailItem(
                selected = aba == i,
                onClick = { onAba(i) },
                icon = { Icon(a.icone, contentDescription = null) },
                label = { Text(a.titulo) },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun BarraNavegacao(aba: Int, onAba: (Int) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        ABAS.forEachIndexed { i, a ->
            NavigationBarItem(
                selected = aba == i,
                onClick = { onAba(i) },
                icon = { Icon(a.icone, contentDescription = null) },
                label = { Text(a.titulo) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}
