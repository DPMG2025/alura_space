package br.com.casinhadosaber.dominio

import java.io.File

/*
 * Vozes da família: na área dos pais (atrás do portão de adulto), o pai ou a mãe grava a própria voz
 * nas frases que o narrador mais fala (elogios, incentivos, chegada, vitória, boa noite). Quando a
 * frase da vez tem gravação, o narrador toca a gravação; quando não tem, fala com a voz do aparelho
 * (TTS), exatamente como antes. Tudo fica só no aparelho (pasta privada do app, fora do backup).
 */

/** Para que serve a frase (agrupa os botões na área dos pais). */
enum class GrupoDeFrase(val titulo: String) {
    ELOGIO("Elogios"),
    INCENTIVO("Incentivos"),
    CHEGADA("Chegada"),
    VITORIA("Vitória"),
    NOITE("Boa noite"),
}

/**
 * Uma frase que a família pode gravar. [texto] é o que aparece para o adulto ler em voz alta (e a
 * fala do narrador quando não há gravação). Em [comNome], o adulto grava já com o nome da criança.
 */
data class FraseDaFamilia(val chave: String, val texto: String, val grupo: GrupoDeFrase, val comNome: Boolean = false)

/** Conjuntos de gravações: a família pode gravar com a voz da mamãe e com a do papai e escolher qual toca. */
enum class ConjuntoDeVozes(val titulo: String, val emoji: String) {
    MAMAE("Voz da mamãe", "👩"),
    PAPAI("Voz do papai", "👨"),
}

/**
 * Um pedaço do que o narrador vai dizer: o [trecho] falado pela voz do aparelho e, quando a frase é uma
 * das que a família pode gravar, a [chave] dela.
 */
data class ParteDaFala(val trecho: Trecho, val chave: String? = null)

/** Um passo da fala já decidida: tocar uma gravação da família ou falar trechos com a voz do aparelho. */
sealed interface PassoDaFala {
    data class Gravacao(val chave: String) : PassoDaFala
    data class Voz(val trechos: List<Trecho>) : PassoDaFala
}

object VozesDaFamilia {

    /** As frases mais frequentes do app, na ordem em que aparecem na área dos pais. */
    val frases: List<FraseDaFamilia> = listOf(
        // Os seis elogios e os três incentivos seguem a ordem de FrasesComNome (o teste confere).
        FraseDaFamilia("muito_bem", "Muito bem!", GrupoDeFrase.ELOGIO),
        FraseDaFamilia("isso_mesmo", "Isso mesmo!", GrupoDeFrase.ELOGIO),
        FraseDaFamilia("voce_conseguiu", "Você conseguiu!", GrupoDeFrase.ELOGIO),
        FraseDaFamilia("que_orgulho", "Que orgulho!", GrupoDeFrase.ELOGIO),
        FraseDaFamilia("mandou_bem", "Mandou bem!", GrupoDeFrase.ELOGIO),
        FraseDaFamilia("pensou_direitinho", "Parabéns! Você pensou direitinho.", GrupoDeFrase.ELOGIO),
        FraseDaFamilia("tentar_de_novo", "Vamos tentar de novo? Você consegue!", GrupoDeFrase.INCENTIVO),
        FraseDaFamilia("quase", "Quase! Olhe com calma.", GrupoDeFrase.INCENTIVO),
        FraseDaFamilia("errar_faz_parte", "Errar faz parte de aprender.", GrupoDeFrase.INCENTIVO),
        FraseDaFamilia("tente_outra_vez", "Tente outra vez!", GrupoDeFrase.INCENTIVO),
        FraseDaFamilia("oi_nome", "Oi, {nome}!", GrupoDeFrase.CHEGADA, comNome = true),
        FraseDaFamilia("que_bom_te_ver", "Que bom ver você!", GrupoDeFrase.CHEGADA),
        FraseDaFamilia("vamos_brincar", "Vamos brincar e aprender?", GrupoDeFrase.CHEGADA),
        FraseDaFamilia("casinha_feliz", "A casinha ficou feliz!", GrupoDeFrase.CHEGADA),
        FraseDaFamilia("tres_estrelas", "Três estrelas! Incrível!", GrupoDeFrase.VITORIA),
        FraseDaFamilia("voce_venceu", "Você venceu! Muito bem!", GrupoDeFrase.VITORIA),
        FraseDaFamilia("letra_bonita", "Que letra bonita!", GrupoDeFrase.VITORIA),
        FraseDaFamilia("boa_noite", "Boa noite! Durma bem.", GrupoDeFrase.NOITE),
        FraseDaFamilia("te_amo", "Eu te amo!", GrupoDeFrase.NOITE),
        FraseDaFamilia("ate_amanha", "Até amanhã!", GrupoDeFrase.NOITE),
    )

    private val porChave = frases.associateBy { it.chave }

    /** Frases sem nome, pelo texto exato (para achar a chave de uma fala já pronta). */
    private val porTexto = frases.filter { !it.comNome }.associateBy { it.texto }

    fun frase(chave: String): FraseDaFamilia? = porChave[chave]

    /** O texto que o adulto lê na hora de gravar, com o nome da criança no lugar de {nome}. */
    fun textoParaGravar(f: FraseDaFamilia, nome: String?): String =
        if (f.comNome) FrasesComNome.montar(f.texto, f.texto.replace(", {nome}", "").replace("{nome}", ""), nome) else f.texto

    /** A parte da fala de um [texto] em português: com a chave quando ele é uma das frases gravaveis. */
    fun parte(texto: String): ParteDaFala = ParteDaFala(pt(texto), porTexto[texto]?.chave)

    /**
     * Uma fala com o nome que começa com uma saudação ("Oi, Ana! Que bom ver você!"): a saudação vira a
     * gravação "oi_nome" (o adulto grava já dizendo o nome) e o resto, quando é uma frase gravável, a dele.
     */
    fun saudacao(textoComNome: String): List<ParteDaFala> {
        val i = textoComNome.indexOf("! ")
        if (i < 0) return listOf(parte(textoComNome))
        val oi = textoComNome.substring(0, i + 1)
        val resto = textoComNome.substring(i + 2)
        return listOf(ParteDaFala(pt(oi), "oi_nome"), parte(resto))
    }

    private val chavesDosElogios = listOf("muito_bem", "isso_mesmo", "voce_conseguiu", "que_orgulho", "mandou_bem", "pensou_direitinho")
    private val chavesDosIncentivos = listOf("tentar_de_novo", "quase", "errar_faz_parte")

    /** O elogio de [FrasesComNome.elogio] com a chave da frase gravável. */
    fun elogio(nome: String?, indice: Int): ParteDaFala =
        ParteDaFala(pt(FrasesComNome.elogio(nome, indice)), chavesDosElogios[Math.floorMod(indice, chavesDosElogios.size)])

    /** O incentivo de [FrasesComNome.incentivo] com a chave da frase gravável. */
    fun incentivo(nome: String?, indice: Int): ParteDaFala =
        ParteDaFala(pt(FrasesComNome.incentivo(nome, indice)), chavesDosIncentivos[Math.floorMod(indice, chavesDosIncentivos.size)])

    /** A vitória de [FrasesComNome.vitoria] com a chave da frase gravável. */
    fun vitoria(nome: String?, estrelas: Int): ParteDaFala =
        ParteDaFala(pt(FrasesComNome.vitoria(nome, estrelas)), if (estrelas >= 3) "tres_estrelas" else "voce_venceu")

    /**
     * Decide como falar: cada parte com chave gravada vira uma [PassoDaFala.Gravacao]; as outras são
     * faladas pela voz do aparelho, juntando as vizinhas num passo só (com as pausas de sempre).
     * Sem nenhuma gravação, o resultado é um único passo de voz com todos os trechos (o de antes).
     */
    fun planejar(partes: List<ParteDaFala>, gravadas: Set<String>): List<PassoDaFala> {
        val passos = mutableListOf<PassoDaFala>()
        val voz = mutableListOf<Trecho>()
        for (p in partes) {
            if (p.chave != null && p.chave in gravadas) {
                if (voz.isNotEmpty()) passos += PassoDaFala.Voz(voz.toList()).also { voz.clear() }
                passos += PassoDaFala.Gravacao(p.chave)
            } else if (p.trecho.texto.isNotBlank()) {
                voz += p.trecho
            }
        }
        if (voz.isNotEmpty()) passos += PassoDaFala.Voz(voz.toList())
        return passos
    }
}

/**
 * Onde as gravações ficam: uma pasta por conjunto dentro de [pasta] (no app, `filesDir/vozes_da_familia`,
 * fora do backup do Android) e um índice pequeno em `indice.txt` com o conjunto que toca e se as vozes
 * estão ligadas. Cada gravação é o arquivo `<CONJUNTO>/<chave>.m4a`; a lista das gravadas vem dos arquivos.
 */
class GuardaDasVozes(val pasta: File) {

    /** O conjunto que o narrador usa. */
    var ativo: ConjuntoDeVozes = ConjuntoDeVozes.MAMAE
        private set

    /** Vozes da família ligadas? (desligar volta à voz do aparelho sem apagar nada) */
    var ligadas: Boolean = true
        private set

    private val indice get() = File(pasta, "indice.txt")

    init {
        runCatching {
            if (indice.isFile) {
                val valores = indice.readLines().mapNotNull { l -> l.split("=", limit = 2).takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() } }.toMap()
                ativo = ConjuntoDeVozes.entries.firstOrNull { it.name == valores["ativo"] } ?: ConjuntoDeVozes.MAMAE
                ligadas = valores["ligadas"] != "false"
            }
        }
    }

    private fun salvarIndice() {
        runCatching {
            pasta.mkdirs()
            indice.writeText("ativo=${ativo.name}\nligadas=$ligadas\n")
        }
    }

    fun escolher(c: ConjuntoDeVozes) {
        ativo = c
        salvarIndice()
    }

    fun ligar(sim: Boolean) {
        ligadas = sim
        salvarIndice()
    }

    fun arquivo(c: ConjuntoDeVozes, chave: String): File = File(File(pasta, c.name), "$chave.m4a")

    /** Arquivo provisório da gravação em andamento (só vira a gravação quando termina bem). */
    fun provisorio(c: ConjuntoDeVozes, chave: String): File = File(File(pasta, c.name), "$chave.gravando")

    fun gravadas(c: ConjuntoDeVozes): Set<String> =
        VozesDaFamilia.frases.map { it.chave }.filter { arquivo(c, it).let { a -> a.isFile && a.length() > 0 } }.toSet()

    /** As chaves que o narrador pode tocar agora (vazio quando as vozes estão desligadas). */
    fun paraTocar(): Set<String> = if (ligadas) gravadas(ativo) else emptySet()

    /** Promove a gravação provisória a definitiva (troca a antiga, se houver). */
    fun confirmar(c: ConjuntoDeVozes, chave: String): Boolean {
        val p = provisorio(c, chave)
        if (!p.isFile || p.length() == 0L) {
            p.delete()
            return false
        }
        val a = arquivo(c, chave)
        a.delete()
        return p.renameTo(a)
    }

    fun descartar(c: ConjuntoDeVozes, chave: String) {
        provisorio(c, chave).delete()
    }

    fun apagar(c: ConjuntoDeVozes, chave: String) {
        arquivo(c, chave).delete()
    }

    fun apagarTudo() {
        pasta.deleteRecursively()
        ativo = ConjuntoDeVozes.MAMAE
        ligadas = true
    }
}
