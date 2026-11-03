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
        limiteMinutos = when (limiteMinutos) { 0 -> 15; 15 -> 30; 30 -> 60; else -> 0 }
        prefs.edit { putInt(TIMER, limiteMinutos) }
    }

    /** A casinha está "dormindo" (acabou o tempo do dia). Fica salvo: fechar o app não acorda. */
    var dormindo by mutableStateOf(prefs.getBoolean(DORMINDO, false) && prefs.getLong(DIA, -1) == hoje())
        private set

    /** Segundos de uso hoje, contando só o tempo com o app aberto. */
    fun segundosHoje(): Long = if (prefs.getLong(DIA, -1) == hoje()) prefs.getLong(SEGUNDOS_HOJE, 0) else 0

    fun somarTempoDeHoje(segundos: Long) {
        val total = segundosHoje() + segundos
        prefs.edit { putLong(DIA, hoje()); putLong(SEGUNDOS_HOJE, total) }
    }

    fun dormir() {
        dormindo = true
        prefs.edit { putLong(DIA, hoje()); putBoolean(DORMINDO, true) }
    }

    /** Um adulto liberou: começa uma nova contagem. */
    fun acordar() {
        dormindo = false
        prefs.edit { putBoolean(DORMINDO, false); putLong(SEGUNDOS_HOJE, 0) }
    }

    fun hoje(): Long = System.currentTimeMillis().let { it + java.util.TimeZone.getDefault().getOffset(it) } / 86_400_000L

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
        dormindo = false
        limiteMinutos = 0
    }

    private fun carregar(): EstadoJogo {
        val perfil = if (prefs.contains(GENERO)) {
            Perfil(
                nome = prefs.getString(NOME, "").orEmpty(),
                genero = Genero.entries.firstOrNull { it.name == prefs.getString(GENERO, null) } ?: Genero.MENINA,
                pele = prefs.getInt(PELE, 0),
                cabelo = prefs.getInt(CABELO, 0),
                roupa = prefs.getInt(ROUPA, -1),
                chapeu = prefs.getString(CHAPEU, "").orEmpty(),
                estiloCabelo = prefs.getInt(ESTILO, 0),
                traje = prefs.getString(TRAJE, "").orEmpty(),
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
            moedas = prefs.getInt(MOEDAS, 0),
            moedasHoje = prefs.getInt(MOEDAS_HOJE, 0),
            diaDasMoedas = prefs.getLong(DIA_MOEDAS, -1),
            comprados = prefs.getString(COMPRADOS, "").orEmpty().split(",").filter { it.isNotBlank() }.toSet(),
            corTelhado = prefs.getInt(COR_TELHADO, 0),
            corPorta = prefs.getInt(COR_PORTA, 0),
            papelDeParede = prefs.getInt(PAPEL, 0),
            clima = prefs.getInt(CLIMA, 0),
            visitantes = prefs.getString(VISITANTES, "").orEmpty().split(",").filter { it.isNotBlank() }.toSet(),
            historico = Serializacao.textoParaMapaDeTexto(prefs.getString(HISTORICO, null)),
            diaDaMissao = prefs.getLong(DIA_MISSAO, -1),
            moveisDentro = Serializacao.textoParaMoveisDentro(prefs.getString(MOVEIS_DENTRO, null)),
            viuDicaDeArrastar = prefs.getBoolean(DICA_ARRASTAR, false),
        )
    }

    private fun salvar(e: EstadoJogo) = prefs.edit {
        e.perfil?.let {
            putString(NOME, it.nome)
            putString(GENERO, it.genero.name)
            putInt(PELE, it.pele)
            putInt(CABELO, it.cabelo)
            putInt(ROUPA, it.roupa)
            putString(CHAPEU, it.chapeu)
            putInt(ESTILO, it.estiloCabelo)
            putString(TRAJE, it.traje)
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
        putInt(MOEDAS, e.moedas)
        putInt(MOEDAS_HOJE, e.moedasHoje)
        putLong(DIA_MOEDAS, e.diaDasMoedas)
        putString(COMPRADOS, e.comprados.joinToString(","))
        putInt(COR_TELHADO, e.corTelhado)
        putInt(COR_PORTA, e.corPorta)
        putInt(PAPEL, e.papelDeParede)
        putInt(CLIMA, e.clima)
        putString(HISTORICO, Serializacao.mapaDeTextoParaTexto(e.historico))
        putLong(DIA_MISSAO, e.diaDaMissao)
        putString(VISITANTES, e.visitantes.joinToString(","))
        putString(MOVEIS_DENTRO, Serializacao.posicoesParaTexto(e.moveisDentro))
        putBoolean(DICA_ARRASTAR, e.viuDicaDeArrastar)
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
        const val DORMINDO = "dormindo"
        const val DIA = "dia"
        const val SEGUNDOS_HOJE = "segundos_hoje"
        const val MOEDAS = "moedas"
        const val MOEDAS_HOJE = "moedas_hoje"
        const val DIA_MOEDAS = "dia_moedas"
        const val COMPRADOS = "comprados"
        const val COR_PORTA = "cor_porta"
        const val PAPEL = "papel"
        const val CLIMA = "clima"
        const val HISTORICO = "historico"
        const val DIA_MISSAO = "dia_missao"
        const val CHAPEU = "chapeu"
        const val ESTILO = "estilo_cabelo"
        const val TRAJE = "traje"
        const val COR_TELHADO = "cor_telhado"
        const val VISITANTES = "visitantes"
        const val MUSICA = "musica"
        const val MOVEIS_DENTRO = "moveis_dentro"
        const val DICA_ARRASTAR = "viu_dica_arrastar"
    }
}
