package br.com.casinhadosaber

import android.content.pm.ActivityInfo
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
import br.com.casinhadosaber.audio.Musica
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.dominio.registrarSessao
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dados.Repositorio
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.registrarTreino
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.MolduraDoApp
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaCasaPorDentro
import br.com.casinhadosaber.ui.telas.TelaBoasVindas
import br.com.casinhadosaber.ui.telas.TelaDormir
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaPais
import br.com.casinhadosaber.ui.telas.TelaPerfil
import br.com.casinhadosaber.ui.telas.TelaPremio
import br.com.casinhadosaber.ui.telas.TelaQuase
import br.com.casinhadosaber.ui.telas.TelaTrilha

/** As telas do app. A navegação é simples e fica toda aqui. */
sealed interface Tela {
    data object Perfil : Tela
    data object Inicio : Tela
    data object BoasVindas : Tela
    data class Caminho(val trilha: Trilha) : Tela
    data class Jogo(val fase: Fase) : Tela
    data class Premio(
        val fase: Fase,
        val estrelas: Int,
        val item: ItemCasa?,
        val moedas: Int = 0,
        val brinde: Produto? = null,
        /** Fala no lugar de "Você terminou a fase!" (fim do treino). */
        val mensagem: String? = null,
        /** Fim do treino: sem botão de próxima fase. */
        val doTreino: Boolean = false,
        /** Fases vencidas antes da partida (só nas fases normais): cena do chefão e festa do mascote. */
        val fasesAntes: Int? = null,
    ) : Tela
    /** [colocar]: item que acabou de ser ganho e já vai direto para a casa. */
    data class Casa(val colocar: String? = null) : Tela
    /** A casa por dentro (isométrica), onde a criança anda pelos cômodos. */
    data object CasaPorDentro : Tela
    /** Espelho: troca a aparência e volta para [voltar] (sem repetir as boas vindas). */
    data class Espelho(val voltar: Tela) : Tela
    data class Quase(val fase: Fase, val acertos: Int) : Tela
    /** Missão do dia: revisão de fases difíceis, com prêmio em moedinhas. */
    data class Missao(val fase: Fase, val perguntas: List<br.com.casinhadosaber.dominio.Pergunta>) : Tela
    data object Pais : Tela

    /** Treino sem fim do caminho: perguntas das fases vencidas, mais das que a criança erra mais. */
    data class Treino(val trilha: Trilha) : Tela
}

class MainActivity : ComponentActivity() {

    private lateinit var narrador: Narrador
    private lateinit var sons: Sons
    private val musica = Musica()
    private var musicaPermitida = false
    private var inicioDaSessao = 0L
    private lateinit var repositorio: Repositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Celular fica sempre em pé (manifesto). Tablet pode deitar: o início e a casa viram duas colunas
        // e as outras telas ficam centralizadas com a escala ajustada à altura (ver MolduraDoApp).
        if (resources.configuration.smallestScreenWidthDp >= 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        }
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
        musica.pausar()
        // Tempo de uso, só para a área dos pais (fica no aparelho).
        if (inicioDaSessao > 0) {
            val segundos = (System.currentTimeMillis() - inicioDaSessao) / 1000
            repositorio.atualizar { it.copy(segundosJogados = it.segundosJogados + segundos) }
            repositorio.somarTempoDeHoje(segundos)
            inicioDaSessao = 0
        }
    }

    override fun onResume() {
        super.onResume()
        inicioDaSessao = System.currentTimeMillis()
        if (musicaPermitida) musica.tocar()
    }

    override fun onDestroy() {
        narrador.desligar()
        musica.liberar()
        sons.desligar()
        super.onDestroy()
    }

    @Composable
    private fun App(repo: Repositorio) {
        val estado = repo.estado
        var tela by remember { mutableStateOf(if (estado.perfil == null) Tela.Perfil else Tela.Inicio) }
        val gerador = remember { GeradorDeFases() }
        // Limite de tempo do dia: conta só o tempo com o app aberto e fica salvo no aparelho.
        LaunchedEffect(repo.limiteMinutos, repo.dormindo) {
            while (repo.limiteMinutos > 0 && !repo.dormindo) {
                val agora = if (inicioDaSessao > 0) (System.currentTimeMillis() - inicioDaSessao) / 1000 else 0
                if (repo.segundosHoje() + agora >= repo.limiteMinutos * 60L) repo.dormir()
                kotlinx.coroutines.delay(10_000)
            }
        }
        if (repo.dormindo && repo.limiteMinutos > 0) {
            BackHandler { }
            TelaDormir(estado) {
                inicioDaSessao = System.currentTimeMillis()
                repo.acordar()
                irParaInicio(tela) { tela = it }
            }
            return
        }

        LaunchedEffect(repo.somLigado) {
            narrador.ativo = repo.somLigado
            sons.ativo = repo.somLigado
            if (!repo.somLigado) narrador.parar()
        }
        LaunchedEffect(repo.somLigado, repo.musicaLigada) {
            musicaPermitida = repo.somLigado && repo.musicaLigada
            if (musicaPermitida) musica.tocar() else musica.pausar()
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
                    is Tela.Quase -> Tela.Caminho(t.fase.trilha)
                    is Tela.Missao -> Tela.Inicio
                    is Tela.Treino -> Tela.Caminho(t.trilha)
                    is Tela.Espelho -> t.voltar
                    Tela.CasaPorDentro -> Tela.Casa()
                    else -> Tela.Inicio
                },
            )
        }

        // Em tablets, o app fica centralizado com largura de celular grande (botões e textos no tamanho certo
        // para a mão da criança), e o fundo de céu ocupa as laterais. Em paisagem, o início e a casa usam
        // a largura toda, em duas colunas, e o caminho vira fileiras de fases lado a lado (ver MolduraDoApp).
        Box(
            Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo))),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter,
        ) {
            AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = tela,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "telas",
            ) { atual ->
                MolduraDoApp(larga = atual == Tela.Inicio || atual is Tela.Casa || atual is Tela.Caminho) {
                when (atual) {
                    Tela.Perfil -> TelaPerfil(estado.perfil) { perfil ->
                        val primeiraVez = estado.perfil == null
                        repo.atualizar { it.copy(perfil = perfil) }
                        irPara(if (primeiraVez) Tela.BoasVindas else Tela.Inicio)
                    }

                    Tela.BoasVindas -> TelaBoasVindas(estado) { irPara(Tela.Inicio) }

                    Tela.Inicio -> TelaInicio(
                        estado = estado,
                        somLigado = repo.somLigado,
                        onAlternarSom = repo::alternarSom,
                        onAbrirCasa = { irPara(Tela.Casa()) },
                        onEspelho = { irPara(Tela.Espelho(Tela.Inicio)) },
                        onAbrirTrilha = { irPara(Tela.Caminho(it)) },
                        onAreaDosPais = { irPara(Tela.Pais) },
                        missaoDisponivel = estado.missaoDisponivel(repo.hoje()),
                        onMissao = {
                            val revisar = estado.fasesParaRevisar(Trilha.entries.flatMap { gerador.fases(it) })
                            if (revisar.isNotEmpty()) irPara(Tela.Missao(revisar.first(), gerador.perguntasDaMissao(revisar)))
                        },
                    )

                    is Tela.Caminho -> TelaTrilha(
                        trilha = atual.trilha,
                        estado = estado,
                        onVoltar = { irPara(Tela.Inicio) },
                        onJogar = { irPara(Tela.Jogo(it)) },
                        onTreinar = { irPara(Tela.Treino(atual.trilha)) },
                    )

                    is Tela.Treino -> {
                        val treino = remember(atual) { br.com.casinhadosaber.dominio.Treino(atual.trilha, repo.estado, gerador) }
                        TelaJogo(
                            fase = treino.fases.first(),
                            perfil = estado.perfil,
                            onSair = { irPara(Tela.Caminho(atual.trilha)) },
                            treino = { resultados -> treino.proxima(resultados) },
                            onConcluir = { resultados ->
                                val r = repo.estado.registrarTreino(treino.feitas.zip(resultados), repo.hoje())
                                repo.atualizar { r.estado }
                                if (r.total == 0) {
                                    irPara(Tela.Caminho(atual.trilha))
                                } else {
                                    val estrelas = when {
                                        r.acertos * 10 >= r.total * 8 -> 3
                                        r.acertos * 2 >= r.total -> 2
                                        else -> 1
                                    }
                                    val vez = when (r.acertos) {
                                        0 -> null
                                        1 -> "uma vez"
                                        else -> "${br.com.casinhadosaber.dominio.Vocabulario.porExtenso(r.acertos, feminino = true)} vezes"
                                    }
                                    irPara(
                                        Tela.Premio(
                                            treino.fases.first(), estrelas, null, r.moedas,
                                            mensagem = if (vez == null) {
                                                "Treino terminado! Errar também ensina. Vamos treinar de novo outra hora!"
                                            } else {
                                                "Treino terminado! Você acertou de primeira $vez. Treinar deixa a cabeça forte!"
                                            },
                                            doTreino = true,
                                        ),
                                    )
                                }
                            },
                        )
                    }

                    is Tela.Jogo -> {
                    // Começo da partida, para o resumo da semana da área dos pais (relógio que não volta atrás).
                    val inicioDaPartida = remember(atual) { System.nanoTime() }
                    TelaJogo(
                        fase = atual.fase,
                        perfil = estado.perfil,
                        facilitada = estado.facilitada(atual.fase),
                        comHistoria = estado.estrelasDa(atual.fase) == 0,
                        onSair = { irPara(Tela.Caminho(atual.fase.trilha)) },
                        onConcluir = { resultados ->
                            val acertos = resultados.count { it }
                            val resultado = repo.estado.registrarPartida(atual.fase, acertos, resultados.size, repo.hoje(), resultados)
                            // Teto de 20 minutos: o app pode ter ficado pausado no meio da partida.
                            val segundos = ((System.nanoTime() - inicioDaPartida) / 1_000_000_000L).coerceIn(0L, 20 * 60L)
                            repo.atualizar {
                                resultado.estado.registrarSessao(atual.fase, resultado, acertos, resultados.size, segundos, repo.hoje())
                            }
                            irPara(
                                if (resultado.venceu) {
                                    Tela.Premio(atual.fase, resultado.estrelas, resultado.item, resultado.moedasGanhas, resultado.brinde, fasesAntes = resultado.estado.fasesVencidas - if (resultado.faseNova) 1 else 0)
                                } else {
                                    Tela.Quase(atual.fase, acertos)
                                },
                            )
                        },
                    )
                    }

                    is Tela.Missao -> TelaJogo(
                        fase = atual.fase,
                        perfil = estado.perfil,
                        perguntasProntas = atual.perguntas,
                        onSair = { irPara(Tela.Inicio) },
                        onConcluir = { resultados ->
                            val (novo, moedas) = repo.estado.concluirMissao(repo.hoje(), resultados.count { it })
                            repo.atualizar { novo }
                            irPara(Tela.Premio(atual.fase, if (resultados.count { it } >= 2) 3 else 1, null, moedas))
                        },
                    )

                    is Tela.Quase -> TelaQuase(
                        fase = atual.fase,
                        acertos = atual.acertos,
                        perfil = estado.perfil,
                        onTentarDeNovo = { irPara(Tela.Jogo(atual.fase)) },
                        onVoltar = { irPara(Tela.Caminho(atual.fase.trilha)) },
                    )

                    is Tela.Premio -> {
                        val proxima = gerador.fases(atual.fase.trilha).getOrNull(atual.fase.numero + 1)
                        TelaPremio(
                            fase = atual.fase,
                            estrelas = atual.estrelas,
                            item = atual.item,
                            moedas = atual.moedas,
                            perfil = estado.perfil,
                            temProxima = proxima != null && !atual.doTreino,
                            onColocarNaCasa = { irPara(Tela.Casa(atual.item?.id)) },
                            onProxima = { proxima?.let { irPara(Tela.Jogo(it)) } },
                            onVoltar = { irPara(Tela.Caminho(atual.fase.trilha)) },
                            brinde = atual.brinde,
                            mensagem = atual.mensagem,
                            estado = estado,
                            fasesAntes = atual.fasesAntes,
                        )
                    }

                    is Tela.Casa -> TelaCasa(
                        estado = estado,
                        colocarAoAbrir = atual.colocar,
                        onAtualizar = repo::atualizar,
                        onVoltar = { irPara(Tela.Inicio) },
                        onJogar = { irPara(Tela.Inicio) },
                        onEspelho = { irPara(Tela.Espelho(Tela.Casa())) },
                        onEntrar = { irPara(Tela.CasaPorDentro) },
                        hoje = repo.hojeData(),
                        perguntarMascote = true,
                    )

                    Tela.CasaPorDentro -> TelaCasaPorDentro(
                        estado = estado,
                        onAtualizar = repo::atualizar,
                        onVoltar = { irPara(Tela.Casa()) },
                        onEspelho = { irPara(Tela.Espelho(Tela.CasaPorDentro)) },
                    )

                    is Tela.Espelho -> TelaPerfil(
                        atual = estado.perfil,
                        espelho = true,
                        roupas = Loja.noGuardaRoupa(estado),
                        comprados = estado.comprados,
                        onVoltar = { irPara(atual.voltar) },
                    ) { perfil ->
                        repo.atualizar { it.copy(perfil = perfil) }
                        irPara(atual.voltar)
                    }

                    Tela.Pais -> TelaPais(
                        estado = estado,
                        somLigado = repo.somLigado,
                        onAlternarSom = repo::alternarSom,
                        musicaLigada = repo.musicaLigada,
                        onAlternarMusica = repo::alternarMusica,
                        limiteMinutos = repo.limiteMinutos,
                        onAlternarLimite = repo::alternarLimite,
                        hoje = repo.hoje(),
                        onAtualizar = repo::atualizar,
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

    private fun irParaInicio(atual: Tela, mudar: (Tela) -> Unit) {
        if (atual !is Tela.Perfil) mudar(Tela.Inicio)
    }
}
