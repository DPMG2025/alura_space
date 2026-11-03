package br.com.casinhadosaber.dominio

/**
 * Mudanças de estado das sugestões 1 a 7 (todas puras: devolvem um [EstadoJogo] novo).
 * As telas chamam pelo `Repositorio.atualizar { it.… }`, como no resto do app.
 */

// ------------------------------------------------------------------ 1. personalização

/** Guarda o aniversário (null apaga). */
fun EstadoJogo.comAniversario(a: Aniversario?): EstadoJogo = perfil?.let { copy(perfil = it.copy(aniversario = a)) } ?: this

/** Liga ou desliga um tema favorito (no máximo [TemaFavorito.MAXIMO]). */
fun EstadoJogo.alternarTema(t: TemaFavorito): EstadoJogo =
    perfil?.let { copy(perfil = it.copy(temasFavoritos = TemaFavorito.alternar(it.temasFavoritos, t))) } ?: this

// ------------------------------------------------------------------ 2. mascote

val EstadoJogo.estagioDoMascote: EstagioMascote get() = Crescimento.estagio(fasesVencidas)

val EstadoJogo.acessoriosDoMascote: List<AcessorioMascote> get() = Crescimento.acessoriosLiberados(fasesVencidas)

/** Escolhe (ou troca) a espécie. O nome e o acessório continuam. */
fun EstadoJogo.escolherMascote(e: EspecieMascote): EstadoJogo = copy(mascote = mascote.copy(especie = e))

/** Dá nome ao mascote (vazio volta para o nome da espécie). */
fun EstadoJogo.nomearMascote(nome: String): EstadoJogo = copy(mascote = mascote.copy(nome = Mascote.limparNome(nome)))

/** Coloca um acessório já liberado no mascote; tocar no que está em uso tira. Null tira o atual. */
fun EstadoJogo.vestirMascote(a: AcessorioMascote?): EstadoJogo = when {
    a == null || mascote.acessorio == a.name -> copy(mascote = mascote.copy(acessorio = ""))
    a in acessoriosDoMascote -> copy(mascote = mascote.copy(acessorio = a.name))
    else -> this
}

// ------------------------------------------------------------------ 4. eventos

/**
 * Entrega o presente do [evento] (uma vez por ano por evento). Devolve o estado novo e o presente, ou o mesmo
 * estado e null se o presente deste ano já foi entregue.
 */
fun EstadoJogo.receberPresenteDoEvento(evento: EventoAtivo): Pair<EstadoJogo, PresenteEspecial?> {
    if (evento.chave in eventosRecebidos) return this to null
    val p = evento.presente
    return copy(eventosRecebidos = eventosRecebidos + evento.chave, presentesEspeciais = presentesEspeciais + p.id) to p
}

/** Presentes de eventos já ganhos, na ordem do catálogo. */
val EstadoJogo.presentesGanhos: List<PresenteEspecial> get() = Eventos.todosOsPresentes.filter { it.id in presentesEspeciais }

// ------------------------------------------------------------------ 5. ateliê

/**
 * Salva um desenho (substitui o de mesmo id). Devolve null quando é um desenho novo e já há
 * [Atelie.MAXIMO_DE_DESENHOS] guardados: a tela pede para apagar um antes (nada some sem a criança querer).
 */
fun EstadoJogo.salvarDesenho(d: Pintura): EstadoJogo? {
    val i = desenhos.indexOfFirst { it.id == d.id }
    if (i >= 0) return copy(desenhos = desenhos.toMutableList().also { it[i] = d })
    if (desenhos.size >= Atelie.MAXIMO_DE_DESENHOS) return null
    return copy(desenhos = desenhos + d)
}

/** Apaga um desenho (e tira da parede, se estava lá). */
fun EstadoJogo.apagarDesenho(id: String): EstadoJogo =
    copy(desenhos = desenhos.filter { it.id != id }, quadrosNaParede = quadrosNaParede - id)

/**
 * Pendura ou tira um desenho da parede. Com a parede cheia ([Atelie.MAXIMO_NA_PAREDE]), o quadro mais antigo
 * da parede sai para o novo entrar (o desenho continua guardado no ateliê).
 */
fun EstadoJogo.alternarNaParede(id: String): EstadoJogo = when {
    id in quadrosNaParede -> copy(quadrosNaParede = quadrosNaParede - id)
    desenhos.none { it.id == id } -> this
    else -> copy(quadrosNaParede = (quadrosNaParede + id).takeLast(Atelie.MAXIMO_NA_PAREDE))
}

val EstadoJogo.quadrosPendurados: List<Pintura> get() = quadrosNaParede.mapNotNull { id -> desenhos.firstOrNull { it.id == id } }

// ------------------------------------------------------------------ 7. pais

fun EstadoJogo.desafioDosPaisFeito(dia: Long): Boolean = dia in desafiosDosPaisFeitos

/**
 * O adulto marcou o desafio do [dia] como feito (atrás do portão de adulto): +[Pais.MOEDAS_DO_DESAFIO] moedinhas,
 * uma vez por dia, fora do limite diário de moedas (como a missão). Devolve o estado e as moedas ganhas (0 se já tinha feito).
 */
fun EstadoJogo.concluirDesafioDosPais(dia: Long): Pair<EstadoJogo, Int> {
    if (dia in desafiosDosPaisFeitos) return this to 0
    return copy(
        desafiosDosPaisFeitos = Pais.cortarDias(desafiosDosPaisFeitos + dia, dia),
        moedas = moedas + Pais.MOEDAS_DO_DESAFIO,
    ) to Pais.MOEDAS_DO_DESAFIO
}

/** Guarda uma partida no registro do relatório (cortado nos últimos [Pais.DIAS_GUARDADOS] dias). */
fun EstadoJogo.registrarSessao(r: RegistroDeSessao): EstadoJogo = copy(sessoes = Pais.acrescentar(sessoes, r))

/** Atalho para a tela do jogo: monta o registro a partir do [ResultadoPartida]. */
fun EstadoJogo.registrarSessao(fase: Fase, resultado: ResultadoPartida, acertos: Int, total: Int, segundos: Long, dia: Long): EstadoJogo =
    registrarSessao(RegistroDeSessao(dia, fase.trilha, fase.numero, resultado.estrelas, acertos, total, segundos.coerceAtLeast(0), resultado.faseNova))
