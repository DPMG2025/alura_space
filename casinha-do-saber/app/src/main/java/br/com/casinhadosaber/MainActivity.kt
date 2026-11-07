package br.com.casinhadosaber

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import br.com.casinhadosaber.ui.LocalCriancaAtiva
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
import br.com.casinhadosaber.ui.componentes.CantinhoDaSala
import br.com.casinhadosaber.ui.telas.AvisoDeConquistas
import br.com.casinhadosaber.ui.telas.NovasConquistas
import br.com.casinhadosaber.ui.telas.TelaAlbum
import br.com.casinhadosaber.ui.telas.TelaAtelie
import br.com.casinhadosaber.ui.telas.TelaDiplomas
import br.com.casinhadosaber.ui.telas.TelaMapa
import br.com.casinhadosaber.ui.telas.TelaPiano
import br.com.casinhadosaber.ui.telas.TelaQuemVaiJogar
import br.com.casinhadosaber.ui.telas.AcoesDaCopia
import br.com.casinhadosaber.ui.telas.AcoesDaFamilia
import br.com.casinhadosaber.dados.ArquivoDaCopia
import br.com.casinhadosaber.dominio.Copia
import br.com.casinhadosaber.dominio.CopiaDoProgresso
import br.com.casinhadosaber.dominio.ResultadoDaCopia
import br.com.casinhadosaber.dominio.resumo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import br.com.casinhadosaber.dominio.Livrinhos
import br.com.casinhadosaber.dominio.Nivelamento
import br.com.casinhadosaber.dominio.aplicarNivel
import br.com.casinhadosaber.dominio.concluirRelembrar
import br.com.casinhadosaber.dominio.paraRelembrar
import br.com.casinhadosaber.dominio.terminarLivrinho
import br.com.casinhadosaber.ui.telas.TelaConviteNivel
import br.com.casinhadosaber.ui.telas.TelaConviteRelembrar
import br.com.casinhadosaber.ui.telas.TelaEstante
import br.com.casinhadosaber.ui.telas.TelaFimRelembrar
import br.com.casinhadosaber.ui.telas.TelaLivrinho
import br.com.casinhadosaber.ui.telas.TelaResultadoNivel
import br.com.casinhadosaber.ui.telas.perguntasDoRelembrar

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

    /** "Quem vai jogar?": ao abrir, quando o aparelho tem mais de uma criança. */
    data object QuemVaiJogar : Tela

    /** Treino sem fim do caminho: perguntas das fases vencidas, mais das que a criança erra mais. */
    data class Treino(val trilha: Trilha) : Tela

    // ---- Modo criativo e conquistas (ateliê, piano, mapa, diplomas e álbum)
    data object Atelie : Tela
    data object Piano : Tela
    data class Mapa(val trilha: Trilha) : Tela
    data class Diplomas(val voltar: Tela) : Tela
    data class Album(val trilha: Trilha, val mundo: Int, val voltar: Tela) : Tela

    // ---- No ritmo da criança: Relembrar (revisão espaçada), Descobrir o nível e livrinhos para ler sozinho
    /** Convite da Corujinha para relembrar (de um caminho, ou de todos quando [trilha] é null). */
    data class Relembrar(val trilha: Trilha?, val voltar: Tela) : Tela
    data class RodadaRelembrar(
        val fases: List<Pair<Trilha, Int>>,
        val fase: Fase,
        val perguntas: List<br.com.casinhadosaber.dominio.Pergunta>,
        val voltar: Tela,
    ) : Tela
    data class FimRelembrar(val acertos: Int, val total: Int, val moedas: Int, val voltar: Tela) : Tela
    data class Nivel(val trilha: Trilha, val voltar: Tela) : Tela
    data class NivelJogo(val trilha: Trilha, val voltar: Tela) : Tela
    data class NivelResultado(val trilha: Trilha, val resultados: List<Boolean>) : Tela
    data class Estante(val voltar: Tela) : Tela
    data class Livrinho(val id: String, val voltar: Tela) : Tela
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
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons, LocalCriancaAtiva provides repositorio.ativa) {
                    App(repositorio)
                }
            }
        }
    }

    /**
     * Modo imersivo: esconde a barra de status e a de navegação (e a barra de tarefas do tablet, no Android 12L
     * ou mais novo). Um deslize da borda mostra as barras por instantes e elas somem sozinhas. O recorte da
     * câmera continua respeitado pelas telas (`WindowInsets.safeDrawing` inclui o `displayCutout`).
     */
    private fun entrarEmTelaCheia() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    /** Teclado, diálogos do sistema e a troca de apps devolvem as barras: esconde de novo ao recuperar o foco. */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) entrarEmTelaCheia()
    }

    override fun onPause() {
        super.onPause()
        narrador.parar()
        musica.pausar()
        fecharTempoDaSessao()
        inicioDaSessao = 0
    }

    /**
     * Soma o tempo desde [inicioDaSessao] na criança que está jogando (área dos pais) e na contagem do
     * aparelho, e recomeça a contar. Chamado ao pausar e antes de trocar de criança ou recuperar uma cópia,
     * para o tempo não ir para a criança errada.
     */
    private fun fecharTempoDaSessao() {
        if (inicioDaSessao <= 0) return
        val agora = System.currentTimeMillis()
        val segundos = (agora - inicioDaSessao) / 1000
        repositorio.atualizar { it.copy(segundosJogados = it.segundosJogados + segundos) }
        repositorio.somarTempoDeHoje(segundos)
        inicioDaSessao = agora
    }

    override fun onStop() {
        super.onStop()
        // App em segundo plano no meio de uma gravação da família: joga fora (a gravação antiga continua).
        // Sem isso, o limite de 8 s guardaria silêncio por cima de uma gravação boa.
        narrador.familia.terminarGravacao(guardar = false)
    }

    override fun onResume() {
        super.onResume()
        inicioDaSessao = System.currentTimeMillis()
        entrarEmTelaCheia()
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
        var tela by remember {
            mutableStateOf(
                when {
                    repo.criancas.size > 1 -> Tela.QuemVaiJogar
                    estado.perfil == null -> Tela.Perfil
                    else -> Tela.Inicio
                },
            )
        }
        val gerador = remember { GeradorDeFases() }
        /** Figurinhas e diplomas ganhos na vitória, mostrados quando a criança sai do prêmio. */
        var conquistas by remember { mutableStateOf(NovasConquistas()) }
        // Dia ou noite na casa: o botão da casa de fora decide, e a casa por dentro abre com o mesmo céu.
        var noiteNaCasa by remember { mutableStateOf(false) }
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
        // No piano a música de fundo pausa (as teclas soam sozinhas).
        val noPiano = tela == Tela.Piano
        LaunchedEffect(repo.somLigado, repo.musicaLigada, noPiano) {
            musicaPermitida = repo.somLigado && repo.musicaLigada && !noPiano
            if (musicaPermitida) musica.tocar() else musica.pausar()
        }

        fun irPara(nova: Tela) {
            narrador.parar()
            tela = nova
        }

        // Várias crianças e cópia do progresso (área dos pais).
        val familiaECopia = familiaECopia(repo, irPara = ::irPara, aoTrocar = { conquistas = NovasConquistas() })

        BackHandler(
            enabled = tela != Tela.Inicio && tela != Tela.QuemVaiJogar &&
                !(tela == Tela.Perfil && estado.perfil == null && repo.criancas.size <= 1),
        ) {
            irPara(
                when (val t = tela) {
                    // Criança nova ainda sem personagem: volta para a escolha (ela fica lá com um 🌱).
                    Tela.Perfil -> if (estado.perfil == null) Tela.QuemVaiJogar else Tela.Inicio
                    is Tela.Jogo -> Tela.Caminho(t.fase.trilha)
                    is Tela.Premio -> Tela.Caminho(t.fase.trilha)
                    is Tela.Quase -> Tela.Caminho(t.fase.trilha)
                    is Tela.Missao -> Tela.Inicio
                    is Tela.Treino -> Tela.Caminho(t.trilha)
                    is Tela.Espelho -> t.voltar
                    Tela.CasaPorDentro -> Tela.Casa()
                    Tela.Atelie, Tela.Piano -> Tela.CasaPorDentro
                    is Tela.Mapa -> Tela.Caminho(t.trilha)
                    is Tela.Diplomas -> t.voltar
                    is Tela.Album -> t.voltar
                    is Tela.Relembrar -> t.voltar
                    is Tela.RodadaRelembrar -> t.voltar
                    is Tela.FimRelembrar -> t.voltar
                    is Tela.Nivel -> t.voltar
                    is Tela.NivelJogo -> t.voltar
                    is Tela.NivelResultado -> Tela.Caminho(t.trilha)
                    is Tela.Estante -> t.voltar
                    is Tela.Livrinho -> Tela.Estante(t.voltar)
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
                        relembrar = estado.paraRelembrar(repo.hoje()).size,
                        onRelembrar = { irPara(Tela.Relembrar(null, Tela.Inicio)) },
                        onEstante = { irPara(Tela.Estante(Tela.Inicio)) },
                    )

                    is Tela.Caminho -> TelaTrilha(
                        trilha = atual.trilha,
                        estado = estado,
                        onVoltar = { irPara(Tela.Inicio) },
                        onJogar = { irPara(Tela.Jogo(it)) },
                        onTreinar = { irPara(Tela.Treino(atual.trilha)) },
                        onMapa = { irPara(Tela.Mapa(atual.trilha)) },
                        relembrar = estado.paraRelembrar(repo.hoje(), atual.trilha).size,
                        onRelembrar = { irPara(Tela.Relembrar(atual.trilha, atual)) },
                        onDescobrirNivel = { irPara(Tela.Nivel(atual.trilha, atual)) },
                        onEstante = if (atual.trilha == Trilha.PORTUGUES) ({ irPara(Tela.Estante(atual)) }) else null,
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
                            val antes = repo.estado.estrelas
                            val resultado = repo.estado.registrarPartida(atual.fase, acertos, resultados.size, repo.hoje(), resultados)
                            // Teto de 20 minutos: o app pode ter ficado pausado no meio da partida.
                            val segundos = ((System.nanoTime() - inicioDaPartida) / 1_000_000_000L).coerceIn(0L, 20 * 60L)
                            repo.atualizar {
                                resultado.estado.registrarSessao(atual.fase, resultado, acertos, resultados.size, segundos, repo.hoje())
                            }
                            conquistas += NovasConquistas.entre(antes, resultado.estado.estrelas)
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
                        noiteInicial = noiteNaCasa,
                        onNoite = { noiteNaCasa = it },
                    )

                    Tela.CasaPorDentro -> TelaCasaPorDentro(
                        estado = estado,
                        onAtualizar = repo::atualizar,
                        onVoltar = { irPara(Tela.Casa()) },
                        onEspelho = { irPara(Tela.Espelho(Tela.CasaPorDentro)) },
                        cantinho = CantinhoDaSala(
                            onAtelie = { irPara(Tela.Atelie) },
                            onPiano = { irPara(Tela.Piano) },
                            onDiplomas = { irPara(Tela.Diplomas(Tela.CasaPorDentro)) },
                            onAlbum = { irPara(Tela.Album(Trilha.entries.maxBy { estado.vencidasNa(it) }, -1, Tela.CasaPorDentro)) },
                        ),
                        noite = noiteNaCasa,
                    )

                    Tela.Atelie -> TelaAtelie(estado, repo::atualizar, onVoltar = { irPara(Tela.CasaPorDentro) })

                    Tela.Piano -> TelaPiano(onVoltar = { irPara(Tela.CasaPorDentro) })

                    is Tela.Mapa -> TelaMapa(
                        trilha = atual.trilha,
                        estado = estado,
                        onVoltar = { irPara(Tela.Caminho(atual.trilha)) },
                        onAlbum = { mundo -> irPara(Tela.Album(atual.trilha, mundo, atual)) },
                    )

                    is Tela.Diplomas -> TelaDiplomas(estado, onVoltar = { irPara(atual.voltar) })

                    is Tela.Album -> TelaAlbum(estado, atual.trilha, atual.mundo, onVoltar = { irPara(atual.voltar) })

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
                        onVoltar = { irPara(if (repo.estado.perfil == null) Tela.Perfil else Tela.Inicio) },
                        familia = familiaECopia.first,
                        copia = familiaECopia.second,
                        onDescobrirNivel = { t -> irPara(Tela.Nivel(t, Tela.Pais)) },
                    )

                    Tela.QuemVaiJogar -> TelaQuemVaiJogar(repo.criancas) { id ->
                        fecharTempoDaSessao()
                        repo.trocarCrianca(id)
                        conquistas = NovasConquistas()
                        irPara(if (repo.estado.perfil == null) Tela.Perfil else Tela.Inicio)
                    }

                    is Tela.Relembrar -> {
                        val itens = estado.paraRelembrar(repo.hoje(), atual.trilha)
                        // Nada para hoje (já relembrou): volta, sem convite vazio.
                        if (itens.isEmpty()) LaunchedEffect(Unit) { irPara(atual.voltar) }
                        TelaConviteRelembrar(
                            nome = estado.perfil?.nome,
                            quantas = itens.size,
                            onVamos = {
                                val fases = br.com.casinhadosaber.dominio.Relembrar.fasesDaRodada(itens)
                                val rodada = perguntasDoRelembrar(gerador, fases)
                                irPara(if (rodada == null) atual.voltar else Tela.RodadaRelembrar(fases, rodada.first, rodada.second, atual.voltar))
                            },
                            onAgoraNao = { irPara(atual.voltar) },
                        )
                    }

                    is Tela.RodadaRelembrar -> TelaJogo(
                        fase = atual.fase,
                        perfil = estado.perfil,
                        perguntasProntas = atual.perguntas,
                        onSair = { irPara(atual.voltar) },
                        onConcluir = { resultados ->
                            val (novo, moedas) = repo.estado.concluirRelembrar(atual.fases, resultados, repo.hoje())
                            repo.atualizar { novo }
                            irPara(Tela.FimRelembrar(resultados.count { it }, resultados.size, moedas, atual.voltar))
                        },
                    )

                    is Tela.FimRelembrar -> TelaFimRelembrar(estado.perfil?.nome, atual.acertos, atual.total, atual.moedas) { irPara(atual.voltar) }

                    is Tela.Nivel -> TelaConviteNivel(
                        trilha = atual.trilha,
                        nome = estado.perfil?.nome,
                        onComecar = { irPara(Tela.NivelJogo(atual.trilha, atual.voltar)) },
                        onAgoraNao = { irPara(atual.voltar) },
                    )

                    is Tela.NivelJogo -> {
                        val fasesDoCaminho = remember(atual.trilha) { gerador.fases(atual.trilha) }
                        fun pergunta(r: List<Boolean>) =
                            gerador.perguntaDaFase(fasesDoCaminho[Nivelamento.de(atual.trilha, r).faseDaPergunta.coerceIn(0, fasesDoCaminho.lastIndex)])
                        TelaJogo(
                            fase = fasesDoCaminho.first(),
                            perfil = estado.perfil,
                            onSair = { irPara(atual.voltar) },
                            treino = ::pergunta,
                            fimDoTreino = { r -> Nivelamento.de(atual.trilha, r).terminou },
                            onConcluir = { r ->
                                // Saiu no meio (✖ ou voltar): não aplica nada nem marca o nível como feito.
                                if (r.isEmpty() || !Nivelamento.de(atual.trilha, r).terminou) {
                                    irPara(atual.voltar)
                                } else {
                                    repo.atualizar { it.aplicarNivel(Nivelamento.de(atual.trilha, r)) }
                                    irPara(Tela.NivelResultado(atual.trilha, r))
                                }
                            },
                        )
                    }

                    is Tela.NivelResultado -> TelaResultadoNivel(Nivelamento.de(atual.trilha, atual.resultados), estado.perfil?.nome) {
                        irPara(Tela.Caminho(atual.trilha))
                    }

                    is Tela.Estante -> TelaEstante(
                        estado = estado,
                        onAbrir = { irPara(Tela.Livrinho(it.id, atual.voltar)) },
                        onVoltar = { irPara(atual.voltar) },
                    )

                    is Tela.Livrinho -> {
                        val livro = Livrinhos.porId[atual.id]
                        if (livro == null) {
                            LaunchedEffect(Unit) { irPara(Tela.Estante(atual.voltar)) }
                        } else {
                            TelaLivrinho(
                                livro = livro,
                                nome = estado.perfil?.nome,
                                onTerminar = { repo.atualizar { it.terminarLivrinho(livro.id) } },
                                onVoltar = { irPara(Tela.Estante(atual.voltar)) },
                                genero = estado.perfil?.genero,
                            )
                        }
                    }
                }
                }
            }
            // Figurinha ou diploma novo: uma faixa embaixo, depois do prêmio (nunca durante uma partida).
            val podeAvisar = tela == Tela.Inicio || tela is Tela.Caminho || tela is Tela.Casa || tela == Tela.CasaPorDentro
            if (!conquistas.vazia && podeAvisar) {
                val novas = conquistas
                AvisoDeConquistas(
                    novas = novas,
                    nome = estado.perfil?.nome,
                    onAbrir = {
                        conquistas = NovasConquistas()
                        val f = novas.figurinhas.firstOrNull()
                        irPara(
                            if (novas.diplomas.isNotEmpty() || f == null) Tela.Diplomas(tela)
                            else Tela.Album(f.trilha, f.mundo, tela),
                        )
                    },
                    onFim = { conquistas = NovasConquistas() },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                )
            }
        }
    }

    /**
     * Ações da área dos pais para as várias crianças e para a cópia do progresso (arquivo escolhido pelo
     * adulto pelo seletor do Android; o app não usa internet). A cópia lida fica pendente até a confirmação.
     */
    @Composable
    private fun familiaECopia(repo: Repositorio, irPara: (Tela) -> Unit, aoTrocar: () -> Unit): Pair<AcoesDaFamilia, AcoesDaCopia> {
        var pendente by remember { mutableStateOf<CopiaDoProgresso?>(null) }
        var aviso by remember { mutableStateOf<String?>(null) }
        val guardar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Copia.TIPO_DO_ARQUIVO)) { uri ->
            if (uri != null) {
                val ok = ArquivoDaCopia.escrever(this, uri, Copia.paraTexto(repo.copiaDoProgresso()))
                aviso = if (ok) {
                    "Cópia guardada! Para recuperar, toque em \"Recuperar cópia\" e escolha esse arquivo."
                } else {
                    "Não deu para guardar a cópia nesse lugar. Tente outra pasta."
                }
            }
        }
        val recuperar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                when (val r = Copia.deTexto(ArquivoDaCopia.ler(this, uri))) {
                    is ResultadoDaCopia.Valida -> pendente = r.copia
                    is ResultadoDaCopia.Invalida -> aviso = r.motivo
                }
            }
        }
        val familia = AcoesDaFamilia(
            criancas = repo.criancas,
            ativa = repo.ativa,
            onAdicionar = {
                fecharTempoDaSessao()
                if (repo.novaCrianca() != null) { aoTrocar(); irPara(Tela.Perfil) }
            },
            onJogarCom = { id ->
                fecharTempoDaSessao()
                repo.trocarCrianca(id)
                aoTrocar()
                irPara(if (repo.estado.perfil == null) Tela.Perfil else Tela.Inicio)
            },
            onRenomear = repo::renomearCrianca,
            onApagar = { id -> fecharTempoDaSessao(); repo.apagarCrianca(id); aoTrocar() },
        )
        val copia = AcoesDaCopia(
            onGuardar = { runCatching { guardar.launch(Copia.nomeDoArquivo(repo.hojeData())) } },
            onEnviar = {
                val ok = ArquivoDaCopia.enviar(this, Copia.paraTexto(repo.copiaDoProgresso()), Copia.nomeDoArquivo(repo.hojeData()))
                if (!ok) aviso = "Não deu para abrir o envio agora. Use \"Guardar uma cópia\"."
            },
            onRecuperar = { runCatching { recuperar.launch(arrayOf("*/*")) } },
            pendente = pendente,
            // O que está no aparelho agora, para o adulto comparar com a cópia antes de recuperar.
            atual = if (pendente != null) repo.copiaDoProgresso().resumo() else emptyList(),
            onConfirmar = {
                fecharTempoDaSessao()
                pendente?.let { repo.recuperar(it) }
                pendente = null
                aoTrocar()
                aviso = "Pronto! O progresso da cópia voltou. Se foi engano, toque em \"Desfazer a recuperação\"."
            },
            onDesfazer = if (repo.podeDesfazerRecuperacao) {
                {
                    fecharTempoDaSessao()
                    val ok = repo.desfazerRecuperacao()
                    aoTrocar()
                    aviso = if (ok) "Pronto! O progresso de antes da recuperação voltou." else "Não deu para desfazer a recuperação."
                }
            } else {
                null
            },
            onCancelar = { pendente = null },
            aviso = aviso,
            onFecharAviso = { aviso = null },
        )
        return familia to copia
    }

    private fun irParaInicio(atual: Tela, mudar: (Tela) -> Unit) {
        if (atual !is Tela.Perfil) mudar(Tela.Inicio)
    }
}
