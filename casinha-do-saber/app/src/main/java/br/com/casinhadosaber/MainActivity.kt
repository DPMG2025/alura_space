package br.com.casinhadosaber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dados.Repositorio
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaPais
import br.com.casinhadosaber.ui.telas.TelaPerfil
import br.com.casinhadosaber.ui.telas.TelaPremio
import br.com.casinhadosaber.ui.telas.TelaTrilha

/** As telas do app. A navegação é simples e fica toda aqui. */
sealed interface Tela {
    data object Perfil : Tela
    data object Inicio : Tela
    data class Caminho(val trilha: Trilha) : Tela
    data class Jogo(val fase: Fase) : Tela
    data class Premio(val fase: Fase, val estrelas: Int, val item: ItemCasa?) : Tela
    data object Casa : Tela
    data object Pais : Tela
}

class MainActivity : ComponentActivity() {

    private lateinit var narrador: Narrador
    private lateinit var sons: Sons
    private lateinit var repositorio: Repositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        narrador = Narrador(this)
        sons = Sons()
        repositorio = Repositorio(this)

        setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    App(repositorio)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        narrador.parar()
    }

    override fun onDestroy() {
        narrador.desligar()
        sons.desligar()
        super.onDestroy()
    }

    @Composable
    private fun App(repo: Repositorio) {
        val estado = repo.estado
        var tela by remember { mutableStateOf(if (estado.perfil == null) Tela.Perfil else Tela.Inicio) }
        val gerador = remember { GeradorDeFases() }

        LaunchedEffect(repo.somLigado) {
            narrador.ativo = repo.somLigado
            sons.ativo = repo.somLigado
            if (!repo.somLigado) narrador.parar()
        }

        fun irPara(nova: Tela) {
            narrador.parar()
            tela = nova
        }

        BackHandler(enabled = tela != Tela.Inicio && !(tela == Tela.Perfil && estado.perfil == null)) {
            irPara(
                when (val t = tela) {
                    is Tela.Jogo -> Tela.Caminho(t.fase.trilha)
                    is Tela.Premio -> Tela.Caminho(t.fase.trilha)
                    else -> Tela.Inicio
                },
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Cores.Fundo),
        ) {
            AnimatedContent(
                targetState = tela,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "telas",
            ) { atual ->
                when (atual) {
                    Tela.Perfil -> TelaPerfil(estado.perfil) { perfil ->
                        repo.atualizar { it.copy(perfil = perfil) }
                        irPara(Tela.Inicio)
                    }

                    Tela.Inicio -> TelaInicio(
                        estado = estado,
                        somLigado = repo.somLigado,
                        onAlternarSom = repo::alternarSom,
                        onAbrirCasa = { irPara(Tela.Casa) },
                        onAbrirTrilha = { irPara(Tela.Caminho(it)) },
                        onAreaDosPais = { irPara(Tela.Pais) },
                    )

                    is Tela.Caminho -> TelaTrilha(
                        trilha = atual.trilha,
                        estado = estado,
                        onVoltar = { irPara(Tela.Inicio) },
                        onJogar = { irPara(Tela.Jogo(it)) },
                    )

                    is Tela.Jogo -> TelaJogo(
                        fase = atual.fase,
                        perfil = estado.perfil,
                        onSair = { irPara(Tela.Caminho(atual.fase.trilha)) },
                        onConcluir = { erros ->
                            val estrelas = EstadoJogo.estrelasPorErros(erros)
                            var premio: ItemCasa? = null
                            repo.atualizar { e ->
                                val (novo, item) = e.registrarVitoria(atual.fase, estrelas)
                                premio = item
                                novo
                            }
                            irPara(Tela.Premio(atual.fase, estrelas, premio))
                        },
                    )

                    is Tela.Premio -> {
                        val proxima = gerador.fases(atual.fase.trilha).getOrNull(atual.fase.numero + 1)
                        TelaPremio(
                            fase = atual.fase,
                            estrelas = atual.estrelas,
                            item = atual.item,
                            perfil = estado.perfil,
                            temProxima = proxima != null,
                            onColocarNaCasa = { irPara(Tela.Casa) },
                            onProxima = { proxima?.let { irPara(Tela.Jogo(it)) } },
                            onVoltar = { irPara(Tela.Caminho(atual.fase.trilha)) },
                        )
                    }

                    Tela.Casa -> TelaCasa(
                        estado = estado,
                        onAtualizar = repo::atualizar,
                        onVoltar = { irPara(Tela.Inicio) },
                        onJogar = { irPara(Tela.Inicio) },
                    )

                    Tela.Pais -> TelaPais(
                        estado = estado,
                        somLigado = repo.somLigado,
                        onAlternarSom = repo::alternarSom,
                        onEditarPersonagem = { irPara(Tela.Perfil) },
                        onApagarTudo = {
                            repo.apagarTudo()
                            irPara(Tela.Perfil)
                        },
                        onVoltar = { irPara(Tela.Inicio) },
                    )
                }
            }
        }
    }
}
