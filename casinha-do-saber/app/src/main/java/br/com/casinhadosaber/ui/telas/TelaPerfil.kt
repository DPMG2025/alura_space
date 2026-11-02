package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/** Primeira tela: a criança escolhe quem vai morar na casinha. */
@Composable
fun TelaPerfil(atual: Perfil?, onPronto: (Perfil) -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var genero by rememberSaveable { mutableStateOf(atual?.genero) }
    var pele by rememberSaveable { mutableStateOf(atual?.pele ?: 0) }
    var cabelo by rememberSaveable { mutableStateOf(atual?.cabelo ?: 0) }
    var nome by rememberSaveable { mutableStateOf(atual?.nome.orEmpty()) }
    var roupa by rememberSaveable { mutableStateOf(atual?.roupa ?: -1) }

    LaunchedEffect(Unit) {
        narrador.falar("Olá! Quem vai morar na casinha? Escolha um menino ou uma menina.")
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Casinha do Saber", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(
            "Quem vai morar na casinha?",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (g in Genero.entries) {
                val escolhido = genero == g
                val borda by animateDpAsState(if (escolhido) 5.dp else 0.dp, label = "borda")
                val rotulo = if (g == Genero.MENINO) "Menino" else "Menina"
                Column(
                    Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(28.dp))
                        .border(borda, Cores.Estrela, RoundedCornerShape(28.dp))
                        .clickable(role = Role.RadioButton) {
                            genero = g
                            sons.toque()
                            narrador.falar("$rotulo!")
                        }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Crianca(
                        Perfil(nome, g, pele, cabelo, roupa),
                        Modifier
                            .height(170.dp)
                            .then(if (genero == null) Modifier.pulsando() else Modifier),
                        acenando = escolhido,
                    )
                    Text(rotulo, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Cor da pele", style = MaterialTheme.typography.titleMedium)
        SeletorDeCores(Aparencia.peles, pele, "pele") { pele = it; sons.toque() }
        Spacer(Modifier.height(12.dp))
        Text("Cor do cabelo", style = MaterialTheme.typography.titleMedium)
        SeletorDeCores(Aparencia.cabelos, cabelo, "cabelo") { cabelo = it; sons.toque() }
        Spacer(Modifier.height(12.dp))
        Text("Cor da roupa", style = MaterialTheme.typography.titleMedium)
        val roupaAtual = if (roupa >= 0) roupa else if (genero == Genero.MENINA) 1 else 0
        SeletorDeCores(Aparencia.roupas, roupaAtual, "roupa") { roupa = it; sons.toque() }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it.take(16) },
            label = { Text("Nome (um adulto pode digitar)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge,
        )

        Spacer(Modifier.height(24.dp))
        val g = genero
        BotaoFofo(
            onClick = {
                if (g != null) {
                    sons.acerto()
                    onPronto(Perfil(nome.trim(), g, pele, cabelo, roupa))
                } else {
                    narrador.falar("Primeiro, toque no menino ou na menina.")
                }
            },
            cor = if (g != null) Cores.Certo else Color(0xFFBDBDBD),
            modifier = Modifier.then(if (g != null) Modifier.pulsando() else Modifier),
            descricao = "Pronto",
        ) {
            Text(
                "Pronto! ✔",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun SeletorDeCores(cores: List<Long>, escolhida: Int, nome: String, onEscolher: (Int) -> Unit) {
    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        cores.forEachIndexed { i, c ->
            Box(
                Modifier
                    .size(46.dp)
                    .border(if (i == escolhida) 5.dp else 2.dp, if (i == escolhida) Cores.Estrela else Color.White, CircleShape)
                    .padding(5.dp)
                    .background(Color(c), CircleShape)
                    .semantics { contentDescription = "Cor de $nome ${i + 1}" }
                    .clickable(role = Role.RadioButton) { onEscolher(i) },
            )
        }
        Spacer(Modifier.width(0.dp))
    }
}
