package br.com.casinhadosaber.dominio

/**
 * Datas sem relógio (sugestão 4): tudo recebe a data de fora, então os testes escolhem o dia.
 *
 * O app conta os dias como "dia época" local (dias desde 1º de janeiro de 1970, já com o fuso do aparelho),
 * o mesmo número de `Repositorio.hoje()`. [Data] converte esse número em dia, mês e ano com aritmética pura
 * (sem java.time, que exigiria desugaring no minSdk 24).
 */
data class Data(val ano: Int, val mes: Int, val dia: Int) : Comparable<Data> {
    init {
        require(mes in 1..12 && dia in 1..Calendario.diasNoMes(mes, ano)) { "Data inválida: $dia/$mes/$ano" }
    }

    /** Dias desde 1º de janeiro de 1970 (o mesmo número de `Repositorio.hoje()`). */
    val diaEpoca: Long get() = Calendario.diaEpoca(ano, mes, dia)

    /** 1 = segunda feira ... 7 = domingo. */
    val diaDaSemana: Int get() = Math.floorMod(diaEpoca + 3, 7L).toInt() + 1

    /** Número da semana desde 1970 (semanas de segunda a domingo): cresce sem voltar, bom para rodízios. */
    val semana: Long get() = Calendario.semana(diaEpoca)

    fun maisDias(n: Long): Data = deDiaEpoca(diaEpoca + n)

    override fun compareTo(other: Data): Int = diaEpoca.compareTo(other.diaEpoca)

    companion object {
        fun deDiaEpoca(dia: Long): Data = Calendario.civil(dia)
    }
}

object Calendario {
    fun bissexto(ano: Int): Boolean = (ano % 4 == 0 && ano % 100 != 0) || ano % 400 == 0

    fun diasNoMes(mes: Int, ano: Int): Int = when (mes) {
        2 -> if (bissexto(ano)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    /** Dias desde 1970-01-01 (algoritmo "days from civil" de Howard Hinnant). */
    fun diaEpoca(ano: Int, mes: Int, dia: Int): Long {
        val y = (if (mes <= 2) ano - 1 else ano).toLong()
        val era = Math.floorDiv(y, 400L)
        val yoe = y - era * 400
        val mp = if (mes > 2) mes - 3 else mes + 9
        val doy = (153L * mp + 2) / 5 + dia - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    /** O contrário de [diaEpoca]. */
    fun civil(diaEpoca: Long): Data {
        val z = diaEpoca + 719468
        val era = Math.floorDiv(z, 146097L)
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
        val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
        val y = (yoe + era * 400 + if (m <= 2) 1 else 0).toInt()
        return Data(y, m, d)
    }

    /** Semana (de segunda a domingo) que contém o [diaEpoca]. 1970-01-01 foi uma quinta feira. */
    fun semana(diaEpoca: Long): Long = Math.floorDiv(diaEpoca + 3, 7L)

    /** Domingo de Páscoa do [ano] (calendário gregoriano, algoritmo de Meeus, Jones e Butcher). */
    fun pascoa(ano: Int): Data {
        val a = ano % 19
        val b = ano / 100
        val c = ano % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val mes = (h + l - 7 * m + 114) / 31
        val dia = (h + l - 7 * m + 114) % 31 + 1
        return Data(ano, mes, dia)
    }
}

/** Enfeite especial ganho de presente num evento (fica para sempre, na casa). */
data class PresenteEspecial(val id: String, val nome: String, val emoji: String)

/**
 * Eventos do ano. [decoracao] é o id do tema de enfeites que a casa usa durante o evento;
 * [presentes] gira um por ano ([Eventos.presenteDoAno]).
 */
enum class TipoDeEvento(val nome: String, val emoji: String, val decoracao: String, val presentes: List<PresenteEspecial>) {
    ANIVERSARIO(
        "Aniversário", "🎂", "festa",
        listOf(
            PresenteEspecial("aniversario_bolo", "Bolo de aniversário", "🎂"),
            PresenteEspecial("aniversario_presente", "Caixa de presente", "🎁"),
            PresenteEspecial("aniversario_baloes", "Balões de festa", "🎈"),
            PresenteEspecial("aniversario_confete", "Bola de confete", "🎊"),
        ),
    ),
    NATAL(
        "Natal", "🎄", "natal",
        listOf(
            PresenteEspecial("natal_arvore", "Arvorezinha de Natal", "🎄"),
            PresenteEspecial("natal_boneco", "Boneco de neve", "⛄"),
            PresenteEspecial("natal_sino", "Sininho dourado", "🔔"),
            PresenteEspecial("natal_estrela", "Estrela de Natal", "🌟"),
        ),
    ),
    PASCOA(
        "Páscoa", "🐣", "pascoa",
        listOf(
            PresenteEspecial("pascoa_pintinho", "Pintinho de Páscoa", "🐣"),
            PresenteEspecial("pascoa_ovo", "Ovo pintado", "🥚"),
            PresenteEspecial("pascoa_cesta", "Cestinha de Páscoa", "🧺"),
            PresenteEspecial("pascoa_coelho", "Coelhinho de pelúcia", "🐇"),
        ),
    ),
    FESTA_JUNINA(
        "Festa Junina", "🌽", "junina",
        listOf(
            PresenteEspecial("junina_milho", "Espiga de milho", "🌽"),
            PresenteEspecial("junina_chapeu", "Chapéu de palha", "👒"),
            PresenteEspecial("junina_pipoca", "Pipoca quentinha", "🍿"),
            PresenteEspecial("junina_sanfona", "Sanfona", "🪗"),
        ),
    ),
    DIA_DAS_CRIANCAS(
        "Dia das Crianças", "🪁", "criancas",
        listOf(
            PresenteEspecial("criancas_pipa", "Pipa colorida", "🪁"),
            PresenteEspecial("criancas_ioio", "Ioiô", "🪀"),
            PresenteEspecial("criancas_bola", "Bola de praia", "🏐"),
            PresenteEspecial("criancas_carrinho", "Carrinho de corrida", "🏎️"),
        ),
    ),
}

/** Um evento acontecendo na data pedida. [chave] ("NATAL:2026") marca o presente do ano. */
data class EventoAtivo(val tipo: TipoDeEvento, val ano: Int) {
    val chave: String get() = "${tipo.name}:$ano"
    val presente: PresenteEspecial get() = Eventos.presenteDoAno(tipo, ano)
    val decoracao: String get() = tipo.decoracao

    /** O que o narrador fala ao abrir o app durante o evento (com o nome, se houver). */
    fun saudacao(nome: String?): String = when (tipo) {
        TipoDeEvento.ANIVERSARIO -> FrasesComNome.parabensAniversario(nome)
        TipoDeEvento.NATAL -> FrasesComNome.montar("Feliz Natal, {nome}! A casinha ganhou enfeites e um presente!", "Feliz Natal! A casinha ganhou enfeites e um presente!", nome)
        TipoDeEvento.PASCOA -> FrasesComNome.montar("Feliz Páscoa, {nome}! Um presente se escondeu na casinha!", "Feliz Páscoa! Um presente se escondeu na casinha!", nome)
        TipoDeEvento.FESTA_JUNINA -> FrasesComNome.montar("Olha a festa junina, {nome}! Tem bandeirinha e milho na casinha!", "Olha a festa junina! Tem bandeirinha e milho na casinha!", nome)
        TipoDeEvento.DIA_DAS_CRIANCAS -> FrasesComNome.montar("Feliz Dia das Crianças, {nome}! Tem presente para você!", "Feliz Dia das Crianças! Tem presente para você!", nome)
    }
}

object Eventos {
    /** Dias antes do Dia das Crianças (12 de outubro) em que a festa já aparece. */
    const val DIAS_ANTES_DIA_DAS_CRIANCAS = 3

    /** Presente do evento no [ano]: gira pela lista, um diferente a cada ano. */
    fun presenteDoAno(tipo: TipoDeEvento, ano: Int): PresenteEspecial = tipo.presentes[Math.floorMod(ano, tipo.presentes.size)]

    val todosOsPresentes: List<PresenteEspecial> = TipoDeEvento.entries.flatMap { it.presentes }
    val presentePorId: Map<String, PresenteEspecial> = todosOsPresentes.associateBy { it.id }

    /** Se o [tipo] acontece na [data] (o aniversário depende do perfil, então só com [aniversario]). */
    fun acontece(tipo: TipoDeEvento, data: Data, aniversario: Aniversario? = null): Boolean = when (tipo) {
        TipoDeEvento.ANIVERSARIO -> aniversario?.ehNoDia(data) == true
        TipoDeEvento.NATAL -> data.mes == 12 && data.dia in 1..26
        TipoDeEvento.PASCOA -> {
            val p = Calendario.pascoa(data.ano).diaEpoca
            // Do Domingo de Ramos (uma semana antes) até a segunda depois da Páscoa.
            data.diaEpoca in (p - 7)..(p + 1)
        }
        TipoDeEvento.FESTA_JUNINA -> data.mes == 6
        TipoDeEvento.DIA_DAS_CRIANCAS -> data.mes == 10 && data.dia in (12 - DIAS_ANTES_DIA_DAS_CRIANCAS)..12
    }

    /** Todos os eventos da [data], o aniversário primeiro (a festa da criança é a mais importante). */
    fun doDia(data: Data, aniversario: Aniversario? = null): List<EventoAtivo> =
        TipoDeEvento.entries.filter { acontece(it, data, aniversario) }.map { EventoAtivo(it, data.ano) }

    /** O evento que manda na decoração da casa hoje, ou null num dia comum. */
    fun principal(data: Data, aniversario: Aniversario? = null): EventoAtivo? = doDia(data, aniversario).firstOrNull()
}

/**
 * Surpresa da semana: toda segunda feira um bichinho novo vem visitar a casa (de graça, a semana toda)
 * e a partir daí fica à venda na loja para sempre. Nada é sorteado e nada se perde: se a criança não
 * jogar numa semana, o bichinho daquela semana continua na loja depois.
 */
data class SurpresaDaSemana(
    val semana: Long,
    /** Quem visita a casa nesta semana (aparece no jardim, sem comprar). */
    val visitante: Produto,
    /** O produto que entrou na loja nesta semana (null depois que o catálogo inteiro já entrou). */
    val novidade: Produto?,
)

object Surpresas {
    /** Segunda feira em que a primeira surpresa chega (lançamento das surpresas). */
    val INICIO = Data(2026, 11, 2)

    private val bichos = listOf(
        "🦉" to "Corujinha", "🐸" to "Sapinho", "🐼" to "Panda", "🦋" to "Borboleta",
        "🐧" to "Pinguim", "🐨" to "Coala", "🦄" to "Unicórnio", "🐞" to "Joaninha",
        "🦒" to "Girafinha", "🐘" to "Elefantinho", "🦩" to "Flamingo", "🐝" to "Abelhinha",
        "🦆" to "Patinho", "🐌" to "Caracol", "🐑" to "Ovelhinha", "🦓" to "Zebrinha",
    )

    /** Os visitantes semanais, na ordem em que chegam. Mesma categoria e formato dos visitantes da [Loja]. */
    val catalogo: List<Produto> = bichos.map { (e, n) -> Produto("visitante$e", n, e, 8, Categoria.VISITANTE, e) }

    /** Quantas semanas se passaram desde [INICIO] (negativo antes dele). */
    fun semanasDesdeOInicio(data: Data): Long = data.semana - INICIO.semana

    /** A surpresa da semana da [data]; null antes do [INICIO]. */
    fun daSemana(data: Data): SurpresaDaSemana? {
        val n = semanasDesdeOInicio(data)
        if (n < 0) return null
        val visitante = catalogo[Math.floorMod(n, catalogo.size.toLong()).toInt()]
        return SurpresaDaSemana(data.semana, visitante, catalogo.getOrNull(n.toInt()))
    }

    /** Tudo o que já entrou na loja até a [data] (cresce a cada semana e nunca diminui). */
    fun liberadas(data: Data): List<Produto> {
        val n = semanasDesdeOInicio(data)
        return if (n < 0) emptyList() else catalogo.take((n + 1).coerceAtMost(catalogo.size.toLong()).toInt())
    }
}
