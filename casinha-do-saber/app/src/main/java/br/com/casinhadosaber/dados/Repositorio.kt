package br.com.casinhadosaber.dados

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import br.com.casinhadosaber.dominio.Aniversario
import br.com.casinhadosaber.dominio.Atelie
import br.com.casinhadosaber.dominio.CadernoEmArquivo
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Mascote
import br.com.casinhadosaber.dominio.Pais
import br.com.casinhadosaber.dominio.TemaFavorito
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.Serializacao
import br.com.casinhadosaber.dominio.Copia
import br.com.casinhadosaber.dominio.CopiaDoProgresso
import br.com.casinhadosaber.dominio.ResultadoDaCopia
import br.com.casinhadosaber.dominio.CriancaDaCasa
import br.com.casinhadosaber.dominio.CriancaGuardada
import br.com.casinhadosaber.dominio.Familia
import br.com.casinhadosaber.dominio.Familia.AJUSTES_DO_APARELHO
import br.com.casinhadosaber.dominio.Familia.CHAVE_ATIVA
import br.com.casinhadosaber.dominio.Familia.CHAVE_CRIANCAS
import br.com.casinhadosaber.dominio.Familia.CHAVE_MIGRADO
import br.com.casinhadosaber.dominio.Familia.DIA
import br.com.casinhadosaber.dominio.Familia.DORMINDO
import br.com.casinhadosaber.dominio.Familia.MUSICA
import br.com.casinhadosaber.dominio.Familia.SEGUNDOS_HOJE
import br.com.casinhadosaber.dominio.Familia.SOM
import br.com.casinhadosaber.dominio.Familia.TIMER
import br.com.casinhadosaber.dominio.ValorGuardado

/**
 * Guarda o progresso no próprio aparelho. O app funciona sem internet e não coleta dados.
 *
 * Várias crianças (ver [Familia]): cada uma tem um arquivo de preferências só dela e [estado] é sempre o da
 * criança que está jogando ([ativa]). Som, música e limite de tempo ficam no arquivo do aparelho e valem
 * para todas. A criança 1 usa o arquivo de antes, então quem já jogava continua com tudo.
 */
class Repositorio(private val context: Context) {

    private val aparelho = context.getSharedPreferences(Familia.ARQUIVO_DO_APARELHO, Context.MODE_PRIVATE)

    init {
        migrarSePreciso()
    }

    /** Id da criança que está jogando. */
    var ativa by mutableStateOf(lerAtiva())
        private set

    private var prefs = arquivoDa(ativa)

    var estado by mutableStateOf(carregar())
        private set

    /** As crianças do aparelho, na ordem em que foram criadas. */
    var criancas by mutableStateOf(lerCriancas())
        private set

    var somLigado by mutableStateOf(aparelho.getBoolean(SOM, true))
        private set

    /** Limite de tempo por dia, em minutos (0 = sem limite). Vale para o aparelho, somando todas as crianças. */
    var limiteMinutos by mutableStateOf(aparelho.getInt(TIMER, 0))
        private set

    fun alternarLimite() {
        limiteMinutos = when (limiteMinutos) { 0 -> 15; 15 -> 30; 30 -> 60; else -> 0 }
        aparelho.edit { putInt(TIMER, limiteMinutos) }
    }

    /** A casinha está "dormindo" (acabou o tempo do dia). Fica salvo: fechar o app não acorda. */
    var dormindo by mutableStateOf(aparelho.getBoolean(DORMINDO, false) && aparelho.getLong(DIA, -1) == hoje())
        private set

    /** Segundos de uso hoje, contando só o tempo com o app aberto (todas as crianças). */
    fun segundosHoje(): Long = if (aparelho.getLong(DIA, -1) == hoje()) aparelho.getLong(SEGUNDOS_HOJE, 0) else 0

    fun somarTempoDeHoje(segundos: Long) {
        val total = segundosHoje() + segundos
        aparelho.edit { putLong(DIA, hoje()); putLong(SEGUNDOS_HOJE, total) }
    }

    fun dormir() {
        dormindo = true
        aparelho.edit { putLong(DIA, hoje()); putBoolean(DORMINDO, true) }
    }

    /** Um adulto liberou: começa uma nova contagem. */
    fun acordar() {
        dormindo = false
        aparelho.edit { putBoolean(DORMINDO, false); putLong(SEGUNDOS_HOJE, 0) }
    }

    fun hoje(): Long = System.currentTimeMillis().let { it + java.util.TimeZone.getDefault().getOffset(it) } / 86_400_000L

    /** Hoje como dia, mês e ano (para eventos, surpresa da semana e desafio dos pais). */
    fun hojeData(): Data = Data.deDiaEpoca(hoje())

    var musicaLigada by mutableStateOf(aparelho.getBoolean(MUSICA, true))
        private set

    fun alternarMusica() {
        musicaLigada = !musicaLigada
        aparelho.edit { putBoolean(MUSICA, musicaLigada) }
    }

    fun atualizar(mudanca: (EstadoJogo) -> EstadoJogo) {
        estado = mudanca(estado)
        salvar(estado)
    }

    fun alternarSom() {
        somLigado = !somLigado
        aparelho.edit { putBoolean(SOM, somLigado) }
    }

    /**
     * Apaga o progresso da criança que está jogando (ela volta para a criação do personagem).
     * Com uma criança só, também volta os ajustes do aparelho ao padrão, como antes.
     */
    fun apagarTudo() {
        prefs.edit(commit = true) { clear() }
        cadernoDa(ativa).apagarTudo()
        descartarDesfazer()
        estado = EstadoJogo()
        if (criancas.size <= 1) {
            aparelho.edit(commit = true) { AJUSTES_DO_APARELHO.forEach { remove(it) } }
            somLigado = true
            musicaLigada = true
            dormindo = false
            limiteMinutos = 0
        }
        criancas = lerCriancas()
    }

    // ------------------------------------------------------------------ várias crianças

    /** Passa a jogar com a criança [id] (o progresso de quem estava jogando já está salvo). */
    fun trocarCrianca(id: String) {
        if (id == ativa || criancas.none { it.id == id }) return
        ativa = id
        aparelho.edit(commit = true) { putString(CHAVE_ATIVA, id) }
        prefs = arquivoDa(id)
        estado = carregar()
    }

    /** Cria uma criança nova, vazia, e passa a jogar com ela (o app abre a criação do personagem). */
    fun novaCrianca(): String? {
        val ids = criancas.map { it.id }
        if (ids.size >= Familia.MAXIMO) return null
        val id = Familia.proximoId(ids)
        arquivoDa(id).edit(commit = true) { clear() }
        // Um id pode ser reaproveitado depois de apagar uma criança: a nova começa com o caderno vazio.
        cadernoDa(id).apagarTudo()
        aparelho.edit(commit = true) { putString(CHAVE_CRIANCAS, Familia.idsParaTexto(ids + id)) }
        criancas = lerCriancas()
        trocarCrianca(id)
        criancas = lerCriancas()
        return id
    }

    fun renomearCrianca(id: String, nome: String) {
        val limpo = Familia.cortarNome(nome.trim())
        if (id == ativa) {
            estado.perfil?.let { p -> atualizar { it.copy(perfil = p.copy(nome = limpo)) } }
        } else {
            val arquivo = arquivoDa(id)
            if (arquivo.contains(GENERO)) arquivo.edit(commit = true) { putString(NOME, limpo) }
        }
        criancas = lerCriancas()
    }

    /** Apaga uma criança (atrás do portão de adulto e de uma confirmação). A última vira um começo do zero. */
    fun apagarCrianca(id: String) {
        val ids = criancas.map { it.id }
        if (id !in ids) return
        if (ids.size == 1) {
            apagarTudo()
            return
        }
        val resto = ids - id
        if (id == ativa) trocarCrianca(resto.first())
        arquivoDa(id).edit(commit = true) { clear() }
        cadernoDa(id).apagarTudo()
        descartarDesfazer()
        aparelho.edit(commit = true) { putString(CHAVE_CRIANCAS, Familia.idsParaTexto(resto)) }
        criancas = lerCriancas()
    }

    // ------------------------------------------------------------------ cópia do progresso

    /** O estado inteiro (todas as crianças) para guardar num arquivo. */
    fun copiaDoProgresso(): CopiaDoProgresso = CopiaDoProgresso(
        criancas = criancas.map { c ->
            CriancaGuardada(c.id, lerMapa(arquivoDa(c.id)).filterKeys { it !in AJUSTES_DO_APARELHO })
        },
        ativa = ativa,
        // Sempre os três, mesmo nunca mexidos: recuperar volta exatamente o que estava valendo (padrões inclusive).
        aparelho = mapOf(
            SOM to ValorGuardado.Logico(somLigado),
            MUSICA to ValorGuardado.Logico(musicaLigada),
            TIMER to ValorGuardado.Inteiro(limiteMinutos),
        ),
        feitaEm = hojeData(),
    )

    /** Há como desfazer a última recuperação (o progresso de antes dela está guardado no aparelho)? */
    var podeDesfazerRecuperacao by mutableStateOf(arquivoAntesDeRecuperar().isFile)
        private set

    init {
        // Depois de todos os campos acima (estado, crianças, ajustes) estarem lidos.
        terminarRecuperacaoInterrompida()
    }

    /**
     * Recupera a cópia guardando antes o progresso atual em `antes-de-recuperar.json`, para o adulto poder
     * desfazer ("Desfazer a recuperação" na área dos pais).
     */
    fun recuperar(copia: CopiaDoProgresso) {
        runCatching { arquivoAntesDeRecuperar().writeText(Copia.paraTexto(copiaDoProgresso())) }
        restaurar(copia)
        podeDesfazerRecuperacao = arquivoAntesDeRecuperar().isFile
    }

    /** Volta o progresso de antes da última recuperação e apaga o arquivo guardado. Devolve se deu certo. */
    fun desfazerRecuperacao(): Boolean {
        val arquivo = arquivoAntesDeRecuperar()
        val texto = runCatching { arquivo.readText() }.getOrNull()
        val ok = when (val r = Copia.deTexto(texto)) {
            is ResultadoDaCopia.Valida -> { restaurar(r.copia); true }
            is ResultadoDaCopia.Invalida -> false
        }
        runCatching { arquivo.delete() }
        podeDesfazerRecuperacao = false
        return ok
    }

    /** Apagar uma criança apaga também o progresso guardado para desfazer (ele tem os dados dela). */
    private fun descartarDesfazer() {
        runCatching { arquivoAntesDeRecuperar().delete() }
        podeDesfazerRecuperacao = false
    }

    private fun arquivoAntesDeRecuperar() = java.io.File(context.filesDir, ANTES_DE_RECUPERAR)
    private fun arquivoRecuperando() = java.io.File(context.filesDir, RECUPERANDO)

    /**
     * O app fechou (bateria, sistema) no meio de uma recuperação: a cópia ficou guardada em
     * `recuperando.json`, então termina de aplicar. Sem o arquivo (ou estragado), só tira a marca.
     */
    private fun terminarRecuperacaoInterrompida() {
        if (!aparelho.getBoolean(CHAVE_RECUPERANDO, false)) return
        val r = Copia.deTexto(runCatching { arquivoRecuperando().readText() }.getOrNull())
        if (r is ResultadoDaCopia.Valida) {
            restaurar(r.copia)
        } else {
            aparelho.edit(commit = true) { remove(CHAVE_RECUPERANDO) }
            runCatching { arquivoRecuperando().delete() }
        }
    }

    /**
     * Troca todo o progresso pelo da cópia (já validada por [br.com.casinhadosaber.dominio.Copia.deTexto]).
     * Grava cada arquivo inteiro e só no fim troca a lista de crianças. A contagem de tempo de hoje fica como está.
     */
    fun restaurar(copia: CopiaDoProgresso) {
        // A lista de antes vem do arquivo (não do estado em memória), para valer também ao terminar uma
        // recuperação interrompida.
        val antigos = idsGuardados()
        val novos = copia.criancas.map { it.id }
        // 1. Marca "recuperando" com a cópia guardada: se o app fechar no meio, termina ao abrir.
        val guardouACopia = runCatching { arquivoRecuperando().writeText(Copia.paraTexto(copia)) }.isSuccess
        if (guardouACopia) aparelho.edit(commit = true) { putBoolean(CHAVE_RECUPERANDO, true) }
        // 2. Os arquivos das crianças da cópia.
        for (c in copia.criancas) {
            arquivoDa(c.id).edit(commit = true) {
                clear()
                c.dados.filterKeys { it !in AJUSTES_DO_APARELHO }.forEach { (k, v) -> por(k, v) }
            }
        }
        // 3. A lista nova e quem joga, de uma vez.
        aparelho.edit(commit = true) {
            copia.aparelho.filterKeys { it in Familia.AJUSTES_QUE_VIAJAM }.forEach { (k, v) -> por(k, v) }
            putString(CHAVE_CRIANCAS, Familia.idsParaTexto(novos))
            putString(CHAVE_ATIVA, copia.ativa)
        }
        // 4. Só agora apaga quem não está na cópia (antes disso, a lista antiga ainda apontava para eles).
        for (id in antigos - novos.toSet()) {
            arquivoDa(id).edit(commit = true) { clear() }
            cadernoDa(id).apagarTudo()
        }
        // 5. Pronto: tira a marca.
        aparelho.edit(commit = true) { remove(CHAVE_RECUPERANDO) }
        runCatching { arquivoRecuperando().delete() }
        somLigado = aparelho.getBoolean(SOM, true)
        musicaLigada = aparelho.getBoolean(MUSICA, true)
        limiteMinutos = aparelho.getInt(TIMER, 0)
        ativa = copia.ativa
        prefs = arquivoDa(ativa)
        estado = carregar()
        criancas = lerCriancas()
    }

    private fun SharedPreferences.Editor.por(k: String, v: ValorGuardado) {
        when (v) {
            is ValorGuardado.Texto -> putString(k, v.valor)
            is ValorGuardado.Inteiro -> putInt(k, v.valor)
            is ValorGuardado.Longo -> putLong(k, v.valor)
            is ValorGuardado.Decimal -> putFloat(k, v.valor)
            is ValorGuardado.Logico -> putBoolean(k, v.valor)
            is ValorGuardado.Conjunto -> putStringSet(k, v.valor)
        }
    }

    private fun lerMapa(p: SharedPreferences): Map<String, ValorGuardado> = p.all.mapNotNull { (k, v) ->
        when (v) {
            is String -> k to ValorGuardado.Texto(v)
            is Int -> k to ValorGuardado.Inteiro(v)
            is Long -> k to ValorGuardado.Longo(v)
            is Float -> k to ValorGuardado.Decimal(v)
            is Boolean -> k to ValorGuardado.Logico(v)
            is Set<*> -> k to ValorGuardado.Conjunto(v.filterIsInstance<String>().toSet())
            else -> null
        }
    }.toMap()

    /** O "Meu caderno" da criança [id] (arquivo próprio, fora das preferências, do backup e da cópia). */
    private fun cadernoDa(id: String): CadernoEmArquivo = CadernoEmArquivo.daCrianca(context.filesDir, id)

    private fun arquivoDa(id: String): SharedPreferences =
        context.getSharedPreferences(Familia.arquivo(id), Context.MODE_PRIVATE)

    /**
     * Primeira vez com a família: o arquivo único de antes vira a criança 1 (fica onde está, nada é copiado)
     * e os ajustes do aparelho passam para o arquivo do aparelho. Só roda uma vez.
     */
    private fun migrarSePreciso() {
        if (aparelho.contains(CHAVE_MIGRADO)) return
        val m = Familia.migrar(lerMapa(arquivoDa(Familia.PRIMEIRA)))
        aparelho.edit(commit = true) { m.aparelho.forEach { (k, v) -> por(k, v) } }
    }

    private fun idsGuardados(): List<String> =
        Familia.idsDeTexto(aparelho.getString(CHAVE_CRIANCAS, null)).ifEmpty { listOf(Familia.PRIMEIRA) }

    private fun lerAtiva(): String {
        val ids = idsGuardados()
        return aparelho.getString(CHAVE_ATIVA, null)?.takeIf { it in ids } ?: ids.first()
    }

    private fun lerCriancas(): List<CriancaDaCasa> =
        idsGuardados().map { id -> CriancaDaCasa(id, if (id == ativa) estado.perfil else lerPerfil(arquivoDa(id))) }

    private fun carregar(): EstadoJogo = carregarCom(lerPerfil(prefs))

    private fun lerPerfil(prefs: SharedPreferences): Perfil? =
        if (prefs.contains(GENERO)) {
            Perfil(
                nome = prefs.getString(NOME, "").orEmpty(),
                genero = Genero.entries.firstOrNull { it.name == prefs.getString(GENERO, null) } ?: Genero.MENINA,
                pele = prefs.getInt(PELE, 0),
                cabelo = prefs.getInt(CABELO, 0),
                roupa = prefs.getInt(ROUPA, -1),
                chapeu = prefs.getString(CHAPEU, "").orEmpty(),
                estiloCabelo = prefs.getInt(ESTILO, 0),
                traje = prefs.getString(TRAJE, "").orEmpty(),
                // Saves antigos não têm a chave: vira "sem acessórios".
                acessorios = Serializacao.textoParaAcessorios(prefs.getString(ACESSORIOS, null)),
                // Campos das sugestões 1 a 7: saves antigos não têm as chaves e ficam com o padrão.
                aniversario = Aniversario.deTexto(prefs.getString(ANIVERSARIO, null)),
                temasFavoritos = TemaFavorito.deTexto(prefs.getString(TEMAS, null)),
            )
        } else {
            null
        }

    private fun carregarCom(perfil: Perfil?): EstadoJogo {
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
        ).let { base ->
            // Sugestões 1 a 7: cada leitura tolera chave ausente ou texto estragado (vira o padrão).
            val desenhos = Atelie.textoParaDesenhos(prefs.getString(DESENHOS, null))
            base.copy(
                mascote = Mascote.deTexto(prefs.getString(MASCOTE, null)),
                presentesEspeciais = prefs.getString(PRESENTES_ESPECIAIS, "").orEmpty().split(",").filter { it.isNotBlank() }.toSet(),
                eventosRecebidos = prefs.getString(EVENTOS_RECEBIDOS, "").orEmpty().split(",").filter { it.isNotBlank() }.toSet(),
                desenhos = desenhos,
                quadrosNaParede = Atelie.textoParaParede(prefs.getString(QUADROS_PAREDE, null), desenhos),
                desafiosDosPaisFeitos = Pais.textoParaDias(prefs.getString(DESAFIOS_PAIS, null)),
                sessoes = Pais.textoParaRegistros(prefs.getString(SESSOES, null)),
                // Relembrar, Descobrir o nível e Livrinhos: chaves novas, saves antigos ficam com o padrão.
                revisoes = br.com.casinhadosaber.dominio.Relembrar.deTexto(prefs.getString(REVISOES, null)),
                liberadaAte = br.com.casinhadosaber.dominio.SerializacaoDoNivel.textoParaLiberada(prefs.getString(LIBERADA_ATE, null)),
                nivelFeito = br.com.casinhadosaber.dominio.SerializacaoDoNivel.textoParaFeito(prefs.getString(NIVEL_FEITO, null)),
                livrinhosLidos = br.com.casinhadosaber.dominio.Livrinhos.textoParaLidos(prefs.getString(LIVRINHOS, null)),
            )
        }
    }

    private fun salvar(e: EstadoJogo) {
        gravar(e)
        // O nome e a aparência aparecem na escolha de quem vai jogar.
        if (criancas.firstOrNull { it.id == ativa }?.perfil != e.perfil) {
            criancas = criancas.map { if (it.id == ativa) it.copy(perfil = e.perfil) else it }
        }
    }

    private fun gravar(e: EstadoJogo) = prefs.edit {
        e.perfil?.let {
            putString(NOME, it.nome)
            putString(GENERO, it.genero.name)
            putInt(PELE, it.pele)
            putInt(CABELO, it.cabelo)
            putInt(ROUPA, it.roupa)
            putString(CHAPEU, it.chapeu)
            putInt(ESTILO, it.estiloCabelo)
            putString(TRAJE, it.traje)
            putString(ACESSORIOS, Serializacao.acessoriosParaTexto(it.acessorios))
            putString(ANIVERSARIO, Aniversario.paraTexto(it.aniversario))
            putString(TEMAS, TemaFavorito.paraTexto(it.temasFavoritos))
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
        putString(MASCOTE, Mascote.paraTexto(e.mascote))
        putString(PRESENTES_ESPECIAIS, e.presentesEspeciais.joinToString(","))
        putString(EVENTOS_RECEBIDOS, e.eventosRecebidos.joinToString(","))
        putString(DESENHOS, Atelie.desenhosParaTexto(e.desenhos))
        putString(QUADROS_PAREDE, Atelie.paredeParaTexto(e.quadrosNaParede))
        putString(DESAFIOS_PAIS, Pais.diasParaTexto(e.desafiosDosPaisFeitos))
        putString(SESSOES, Pais.registrosParaTexto(e.sessoes))
        putString(REVISOES, br.com.casinhadosaber.dominio.Relembrar.paraTexto(e.revisoes))
        putString(LIBERADA_ATE, br.com.casinhadosaber.dominio.SerializacaoDoNivel.liberadaParaTexto(e.liberadaAte))
        putString(NIVEL_FEITO, br.com.casinhadosaber.dominio.SerializacaoDoNivel.feitoParaTexto(e.nivelFeito))
        putString(LIVRINHOS, br.com.casinhadosaber.dominio.Livrinhos.lidosParaTexto(e.livrinhosLidos))
    }

    private companion object {
        /** Progresso de antes da última recuperação (para desfazer), em `filesDir`. */
        const val ANTES_DE_RECUPERAR = "antes-de-recuperar.json"
        /** A cópia sendo recuperada, enquanto a recuperação não termina. */
        const val RECUPERANDO = "recuperando.json"
        /** Marca no arquivo do aparelho: uma recuperação começou e ainda não terminou. */
        const val CHAVE_RECUPERANDO = "recuperando"

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
        const val RESPONDIDAS = "respondidas"
        const val ACERTOS = "acertos"
        const val SEGUNDOS = "segundos"
        const val TENTATIVAS = "tentativas"
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
        const val ACESSORIOS = "acessorios"
        const val COR_TELHADO = "cor_telhado"
        const val VISITANTES = "visitantes"
        const val MOVEIS_DENTRO = "moveis_dentro"
        const val DICA_ARRASTAR = "viu_dica_arrastar"
        const val ANIVERSARIO = "aniversario"
        const val TEMAS = "temas_favoritos"
        const val MASCOTE = "mascote"
        const val PRESENTES_ESPECIAIS = "presentes_especiais"
        const val EVENTOS_RECEBIDOS = "eventos_recebidos"
        const val DESENHOS = "desenhos"
        const val QUADROS_PAREDE = "quadros_parede"
        const val DESAFIOS_PAIS = "desafios_pais"
        const val SESSOES = "sessoes"
        const val REVISOES = "revisoes"
        const val LIBERADA_ATE = "liberada_ate"
        const val NIVEL_FEITO = "nivel_feito"
        const val LIVRINHOS = "livrinhos_lidos"
    }
}
