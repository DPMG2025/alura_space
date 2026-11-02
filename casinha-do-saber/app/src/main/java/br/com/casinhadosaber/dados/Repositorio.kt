package br.com.casinhadosaber.dados

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.Serializacao

/** Guarda o progresso no próprio aparelho. O app funciona sem internet e não coleta dados. */
class Repositorio(context: Context) {

    private val prefs = context.getSharedPreferences("casinha_do_saber", Context.MODE_PRIVATE)

    var estado by mutableStateOf(carregar())
        private set

    var somLigado by mutableStateOf(prefs.getBoolean(SOM, true))
        private set

    /** Limite de tempo por sessão, em minutos (0 = sem limite). */
    var limiteMinutos by mutableStateOf(prefs.getInt(TIMER, 0))
        private set

    fun alternarLimite() {
        limiteMinutos = when (limiteMinutos) { 0 -> 15; 15 -> 30; else -> 0 }
        prefs.edit { putInt(TIMER, limiteMinutos) }
    }

    var musicaLigada by mutableStateOf(prefs.getBoolean(MUSICA, true))
        private set

    fun alternarMusica() {
        musicaLigada = !musicaLigada
        prefs.edit { putBoolean(MUSICA, musicaLigada) }
    }

    fun atualizar(mudanca: (EstadoJogo) -> EstadoJogo) {
        estado = mudanca(estado)
        salvar(estado)
    }

    fun alternarSom() {
        somLigado = !somLigado
        prefs.edit { putBoolean(SOM, somLigado) }
    }

    fun apagarTudo() {
        prefs.edit { clear() }
        estado = EstadoJogo()
        somLigado = true
        musicaLigada = true
    }

    private fun carregar(): EstadoJogo {
        val perfil = if (prefs.contains(GENERO)) {
            Perfil(
                nome = prefs.getString(NOME, "").orEmpty(),
                genero = Genero.entries.firstOrNull { it.name == prefs.getString(GENERO, null) } ?: Genero.MENINA,
                pele = prefs.getInt(PELE, 0),
                cabelo = prefs.getInt(CABELO, 0),
                roupa = prefs.getInt(ROUPA, -1),
            )
        } else {
            null
        }
        return EstadoJogo(
            perfil = perfil,
            estrelas = Serializacao.textoParaEstrelas(prefs.getString(ESTRELAS, null)),
            colocados = Serializacao.textoParaPosicoes(prefs.getString(COLOCADOS, null)),
            coresComodos = Serializacao.textoParaCores(prefs.getString(CORES, null)),
            posicaoCrianca = Posicao(prefs.getFloat(CRIANCA_X, 0.47f), prefs.getFloat(CRIANCA_Y, 0.99f)),
            respondidas = Serializacao.textoParaTrilhas(prefs.getString(RESPONDIDAS, null)),
            acertosDePrimeira = Serializacao.textoParaTrilhas(prefs.getString(ACERTOS, null)),
            segundosJogados = prefs.getLong(SEGUNDOS, 0),
            tentativasSemVencer = Serializacao.textoParaMapa(prefs.getString(TENTATIVAS, null)),
        )
    }

    private fun salvar(e: EstadoJogo) = prefs.edit {
        e.perfil?.let {
            putString(NOME, it.nome)
            putString(GENERO, it.genero.name)
            putInt(PELE, it.pele)
            putInt(CABELO, it.cabelo)
            putInt(ROUPA, it.roupa)
        }
        putString(ESTRELAS, Serializacao.estrelasParaTexto(e.estrelas))
        putString(COLOCADOS, Serializacao.posicoesParaTexto(e.colocados))
        putString(CORES, Serializacao.coresParaTexto(e.coresComodos))
        putFloat(CRIANCA_X, e.posicaoCrianca.x)
        putFloat(CRIANCA_Y, e.posicaoCrianca.y)
        putString(RESPONDIDAS, Serializacao.trilhasParaTexto(e.respondidas))
        putString(ACERTOS, Serializacao.trilhasParaTexto(e.acertosDePrimeira))
        putLong(SEGUNDOS, e.segundosJogados)
        putString(TENTATIVAS, Serializacao.mapaParaTexto(e.tentativasSemVencer))
    }

    private companion object {
        const val NOME = "nome"
        const val GENERO = "genero"
        const val PELE = "pele"
        const val CABELO = "cabelo"
        const val ROUPA = "roupa"
        const val ESTRELAS = "estrelas"
        const val COLOCADOS = "colocados"
        const val CORES = "cores"
        const val CRIANCA_X = "crianca_x"
        const val CRIANCA_Y = "crianca_y"
        const val SOM = "som"
        const val RESPONDIDAS = "respondidas"
        const val ACERTOS = "acertos"
        const val SEGUNDOS = "segundos"
        const val TENTATIVAS = "tentativas"
        const val TIMER = "timer"
        const val MUSICA = "musica"
    }
}
