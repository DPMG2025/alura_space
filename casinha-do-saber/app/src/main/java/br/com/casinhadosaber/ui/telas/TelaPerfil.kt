package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

private val nomesDoCabelo = listOf("castanho", "preto", "loiro", "ruivo")
private val nomesDaRoupa = listOf("azul", "rosa", "verde", "laranja", "roxa", "vermelha")

/**
 * Primeira tela: a criança monta quem vai morar na casinha. Tudo é falado e mostrado
 * num personagem grande que pula a cada escolha; o nome fica com o adulto.
 */
@Composable
fun TelaPerfil(atual: Perfil?, onPronto: (Perfil) -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var genero by rememberSaveable { mutableStateOf(atual?.genero) }
    var pele by rememberSaveable { mutableStateOf(atual?.pele ?: 0) }
    var cabelo by rememberSaveable { mutableStateOf(atual?.cabelo ?: 0) }
    var estilo by rememberSaveable { mutableStateOf(atual?.estiloCabelo ?: 0) }
    var roupa by rememberSaveable { mutableStateOf(atual?.roupa ?: -1) }
    var nome by rememberSaveable { mutableStateOf(atual?.nome.orEmpty()) }
    var mostrarNome by rememberSaveable { mutableStateOf(false) }
    var mudancas by remember { mutableIntStateOf(0) }
    val pulo = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        narrador.falar("Olá! Quem vai morar na casinha? Escolha um menino ou uma menina.")
    }
    LaunchedEffect(mudancas) {
        if (mudancas > 0) {
            pulo.animateTo(-24f, tween(120))
            pulo.animateTo(0f, spring(dampingRatio = 0.35f))
        }
    }

    fun escolher(fala: String, mudar: () -> Unit) {
        mudar()
        mudancas++
        sons.toque()
        narrador.falar(fala)
    }

    val g = genero
    val perfil = Perfil(nome.trim(), g ?: Genero.MENINA, pele, cabelo, roupa, chapeu = atual?.chapeu.orEmpty(), estiloCabelo = estilo)

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Casinha do Saber", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(Modifier.height(8.dp))

        if (g == null) {
            // Ainda não escolheu: os dois cartões grandes, com a mãozinha mostrando.
            Spacer(Modifier.height(72.dp))
            Text("Quem vai morar na casinha?", style = MaterialTheme.typography.headlineMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            Box {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (opcao in Genero.entries) {
                        val rotulo = if (opcao == Genero.MENINO) "Menino" else "Menina"
                        Column(
                            Modifier
                                .weight(1f)
                                .background(Color.White, RoundedCornerShape(28.dp))
                                .clickable(role = Role.RadioButton) { escolher("$rotulo!") { genero = opcao } }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Crianca(Perfil("", opcao, pele, cabelo, -1), Modifier.height(260.dp).pulsando(), acenando = true)
                            Text(rotulo, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                MaoGuia(Modifier.align(Alignment.Center), espera = 2000)
            }
            return@Column
        }

        // Personagem grande, que pula a cada escolha.
        Box(
            Modifier
                .size(230.dp)
                .background(Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Crianca(perfil, Modifier.height(190.dp).graphicsLayer { translationY = pulo.value * density }, acenando = true)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (opcao in Genero.entries) {
                val rotulo = if (opcao == Genero.MENINO) "Menino" else "Menina"
                Text(
                    if (opcao == Genero.MENINO) "👦" else "👧",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .background(if (opcao == g) Cores.Estrela else Color.White, CircleShape)
                        .semantics { contentDescription = rotulo }
                        .clickable { escolher("$rotulo!") { genero = opcao } }
                        .padding(10.dp),
                )
            }
        }

        Linha("🧑", "pele") {
            Aparencia.peles.forEachIndexed { i, c ->
                Bolinha(Color(c), i == pele, "Cor de pele ${i + 1}") { escolher("Que lindo!") { pele = i } }
            }
        }
        Linha("💇", "cor do cabelo") {
            Aparencia.cabelos.forEachIndexed { i, c ->
                Bolinha(Color(c), i == cabelo, "Cabelo ${nomesDoCabelo[i]}") { escolher("Cabelo ${nomesDoCabelo[i]}!") { cabelo = i } }
            }
        }
        Linha("✂️", "penteado") {
            val nomesEstilo = if (g == Genero.MENINA) listOf("liso", "cacheado", "trancinhas") else listOf("liso", "cacheado", "espetadinho")
            for (i in 0..2) {
                Box(
                    Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(if (i == estilo) 4.dp else 2.dp, if (i == estilo) Cores.Estrela else Color.White, CircleShape)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        .semantics { contentDescription = "Cabelo ${nomesEstilo[i]}" }
                        .clickable { escolher("Cabelo ${nomesEstilo[i]}!") { estilo = i } },
                    contentAlignment = Alignment.TopCenter,
                ) {
                    // Só a cabeça: o personagem grande, recortado no círculo.
                    Crianca(perfil.copy(estiloCabelo = i, chapeu = ""), Modifier.height(110.dp).padding(top = 6.dp))
                }
            }
        }
        Linha("👕", "roupa") {
            val roupaAtual = if (roupa >= 0) roupa else if (g == Genero.MENINA) 1 else 0
            Aparencia.roupas.forEachIndexed { i, c ->
                Bolinha(Color(c), i == roupaAtual, "Roupa ${nomesDaRoupa[i]}") { escolher("Roupa ${nomesDaRoupa[i]}!") { roupa = i } }
            }
        }

        Spacer(Modifier.height(12.dp))
        // O nome é com o adulto (a criança pequena ainda não escreve).
        if (mostrarNome) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it.take(16) },
                label = { Text("Nome da criança (opcional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge,
            )
        } else {
            Text(
                if (nome.isBlank()) "✏️ Adulto: escrever o nome" else "✏️ $nome",
                style = MaterialTheme.typography.titleMedium,
                color = Cores.TextoSuave,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .clickable { mostrarNome = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        Box {
            BotaoFofo(
                onClick = {
                    sons.acerto()
                    onPronto(perfil.copy(genero = g))
                },
                cor = Cores.Certo,
                modifier = Modifier.pulsando(),
                descricao = "Pronto",
            ) {
                Text(
                    "Pronto! ✔",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 14.dp),
                )
            }
            MaoGuia(Modifier.align(Alignment.BottomEnd), espera = 9000)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Uma linha de escolhas com um ícone na frente (sem depender de leitura). */
@Composable
private fun Linha(icone: String, descricao: String, conteudo: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .semantics(mergeDescendants = false) { contentDescription = "Escolher $descricao" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icone, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.width(44.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { conteudo() }
    }
}

@Composable
private fun Bolinha(cor: Color, escolhida: Boolean, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .border(if (escolhida) 4.dp else 2.dp, if (escolhida) Cores.Estrela else Color.White, CircleShape)
            .padding(4.dp)
            .background(cor, CircleShape)
            .semantics { contentDescription = descricao }
            .clickable(role = Role.RadioButton, onClick = onClick),
    )
}
