package br.com.casinhadosaber.dominio

import java.io.File
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/*
 * Escrever com o dedo: o narrador diz uma palavra (com a figura) e a criança escreve letra por letra,
 * em letra de forma maiúscula (a que o app ensina primeiro), numa área grande com as linhas do caderno.
 * Cada letra é reconhecida comparando o traço com os modelos de TracosDasLetras ([LeitorDeLetras]).
 * As palavras escritas ficam no "Meu caderno" da área dos pais ([Caderno]).
 */

// ------------------------------------------------------------------------------------- atividade

/**
 * Escrever a palavra: [palavra] em maiúsculas de A a Z (sem acento nem cedilha), de 2 a 6 letras.
 * [figura] mostra o que é a palavra; [idioma] é o da voz que diz a palavra (inglês nas fases de inglês).
 * Erro nunca pune: depois de 2 tentativas numa letra aparece o "mostrar como".
 */
data class EscreverPalavra(
    val palavra: String,
    val figura: String? = null,
    val idioma: Idioma = Idioma.PT,
) : Atividade {
    val letras: List<Char> get() = palavra.toList()

    init {
        require(palavra.length in 2..6) { "Escrever palavras de 2 a 6 letras" }
        require(Escrita.podeEscrever(palavra)) { "Só letras de A a Z sem acento: $palavra" }
    }
}

object Escrita {
    /** Tamanho das palavras que as fases pedem para escrever. */
    val TAMANHOS = 3..5

    /** Só letras maiúsculas de A a Z (as que têm traço em TracosDasLetras e nenhum acento para desenhar). */
    fun podeEscrever(palavra: String): Boolean = palavra.isNotEmpty() && palavra.all { it in 'A'..'Z' }

    /** As palavras de [lista] boas para escrever: de 3 a 5 letras, sem acento nem cedilha. */
    fun palavrasParaEscrever(lista: List<Palavra>): List<Palavra> = lista.filter { it.texto.length in TAMANHOS && podeEscrever(it.texto) }
}

// ------------------------------------------------------------------------------------- reconhecer a letra

/** Um ponto de um traço (qualquer unidade: pixels da tela ou de 0 a 1). */
data class Ponto(val x: Float, val y: Float)

/** O que o leitor achou de uma letra escrita. */
data class LeituraDaLetra(
    /** Vale como a letra pedida? */
    val aceita: Boolean,
    /** A letra mais parecida com o que foi escrito (ou null quando não deu para ler nada). */
    val melhor: Char?,
    /** Distância até a letra pedida (menor é mais parecido). */
    val distanciaEsperada: Float,
    /** Distância até a letra mais parecida. */
    val distanciaMelhor: Float,
)

/**
 * Lê uma letra escrita com o dedo, com qualquer ordem e sentido dos traços: cada letra vira uma nuvem
 * de [N] pontos (reamostrada pelo comprimento dos traços, do tamanho de uma caixa e centrada) e a
 * nuvem é comparada com a de cada modelo pelo casamento guloso de pontos (o reconhecedor "\$P" de
 * Vatavu, Anthony e Wobbrock). Os [modelos] são os traços de cada letra (pode haver mais de um jeito
 * de escrever a mesma letra), na proporção em que aparecem na tela.
 *
 * Aceitar é generoso (a criança tem 4 a 6 anos e já sabe qual letra escrever): vale quando a letra
 * pedida é a mais parecida ou fica quase empatada com a mais parecida; não vale quando o desenho é
 * claramente outra letra ou um rabisco.
 */
class LeitorDeLetras(modelos: Map<Char, List<List<List<Ponto>>>>) {

    private val nuvens: Map<Char, List<List<Ponto>>> = modelos.mapValues { (_, jeitos) -> jeitos.map { nuvem(it) }.filter { it.isNotEmpty() } }

    val letras: Set<Char> get() = nuvens.keys

    /** Distância do desenho ([tracos]) até a letra [c] (o melhor dos jeitos de escrever). */
    fun distancia(tracos: List<List<Ponto>>, c: Char): Float {
        val n = nuvem(tracos)
        if (n.isEmpty()) return Float.MAX_VALUE
        return nuvens[c].orEmpty().minOfOrNull { casar(n, it) } ?: Float.MAX_VALUE
    }

    fun ler(tracos: List<List<Ponto>>, esperada: Char): LeituraDaLetra {
        val n = nuvem(tracos)
        if (n.isEmpty()) return LeituraDaLetra(false, null, Float.MAX_VALUE, Float.MAX_VALUE)
        val distancias = nuvens.mapValues { (_, jeitos) -> jeitos.minOfOrNull { casar(n, it) } ?: Float.MAX_VALUE }
        val (melhor, dMelhor) = distancias.minBy { it.value }
        val dEsperada = distancias[esperada] ?: Float.MAX_VALUE
        val aceita = dEsperada <= LONGE_DEMAIS && (melhor == esperada || dEsperada <= dMelhor * FOLGA)
        return LeituraDaLetra(aceita, melhor, dEsperada, dMelhor)
    }

    companion object {
        /** Pontos de cada nuvem. */
        const val N = 32

        /**
         * Acima disso não vale (rabisco ou desenho muito diferente). Medido com os modelos de verdade: uma letra
         * escrita torta fica abaixo de 1,3 da sua; um rabisco qualquer fica acima de 1,55 de todas.
         */
        const val LONGE_DEMAIS = 1.55f

        /**
         * Quase empate com a letra mais parecida também vale (O e G, B e D ficam pertinho). A letra parecida
         * escrita bem feita fica muito mais perto dela mesma, então não passa.
         */
        const val FOLGA = 1.3f

        /** Comprimento mínimo do desenho, em fração do lado da área: menos que isso é só um toque. */
        const val COMPRIMENTO_MINIMO = 0.12f

        /** Comprimento total dos traços. */
        fun comprimento(tracos: List<List<Ponto>>): Float =
            tracos.sumOf { t -> t.zipWithNext { a, b -> dist(a, b).toDouble() }.sum() }.toFloat()

        private fun dist(a: Ponto, b: Ponto): Float {
            val dx = a.x - b.x
            val dy = a.y - b.y
            return sqrt(dx * dx + dy * dy)
        }

        /** A nuvem de [N] pontos do desenho: reamostrada, do tamanho de uma caixa de lado 1 e centrada. Vazia sem traço. */
        fun nuvem(tracos: List<List<Ponto>>): List<Ponto> {
            val validos = tracos.filter { it.isNotEmpty() }
            if (validos.isEmpty()) return emptyList()
            val total = comprimento(validos)
            val pontos = if (total <= 1e-6f) {
                // Só toques (sem comprimento): todos os pontos repetidos até N.
                val todos = validos.flatten()
                List(N) { todos[it % todos.size] }
            } else {
                reamostrar(validos, total)
            }
            return normalizar(pontos)
        }

        /** [N] pontos igualmente espaçados ao longo dos traços (sem ligar o fim de um ao começo do outro). */
        private fun reamostrar(tracos: List<List<Ponto>>, total: Float): List<Ponto> {
            val passo = total / (N - 1)
            val r = mutableListOf(tracos.first().first())
            var acumulado = 0f
            for (traco in tracos) {
                // O primeiro ponto do traço seguinte é um "pulo" do lápis: não conta comprimento.
                var anterior = traco.first()
                var i = 1
                while (i < traco.size) {
                    val atual = traco[i]
                    val d = dist(anterior, atual)
                    if (d > 0f && acumulado + d >= passo) {
                        val t = (passo - acumulado) / d
                        val q = Ponto(anterior.x + t * (atual.x - anterior.x), anterior.y + t * (atual.y - anterior.y))
                        r += q
                        anterior = q
                        acumulado = 0f
                        if (r.size >= N) break
                    } else {
                        acumulado += d
                        anterior = atual
                        i++
                    }
                }
                if (r.size >= N) break
            }
            val ultimo = tracos.last().last()
            while (r.size < N) r += ultimo
            return r.take(N)
        }

        /** Escala para uma caixa de lado 1 (mantendo a proporção) e centra no meio dos pontos. */
        private fun normalizar(pontos: List<Ponto>): List<Ponto> {
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE
            for (p in pontos) {
                minX = min(minX, p.x); maxX = max(maxX, p.x)
                minY = min(minY, p.y); maxY = max(maxY, p.y)
            }
            val lado = max(maxX - minX, maxY - minY).takeIf { it > 1e-6f } ?: 1f
            val escalados = pontos.map { Ponto((it.x - minX) / lado, (it.y - minY) / lado) }
            val cx = escalados.sumOf { it.x.toDouble() }.toFloat() / escalados.size
            val cy = escalados.sumOf { it.y.toDouble() }.toFloat() / escalados.size
            return escalados.map { Ponto(it.x - cx, it.y - cy) }
        }

        /** Casamento guloso das duas nuvens (o menor dos dois sentidos, a partir de vários começos). */
        fun casar(a: List<Ponto>, b: List<Ponto>): Float {
            val n = min(a.size, b.size)
            if (n == 0) return Float.MAX_VALUE
            val passo = max(1, floor(sqrt(n.toDouble())).toInt())
            var melhor = Float.MAX_VALUE
            var i = 0
            while (i < n) {
                melhor = min(melhor, min(distanciaDaNuvem(a, b, i, n), distanciaDaNuvem(b, a, i, n)))
                i += passo
            }
            return melhor
        }

        private fun distanciaDaNuvem(a: List<Ponto>, b: List<Ponto>, inicio: Int, n: Int): Float {
            val usado = BooleanArray(n)
            var soma = 0f
            var i = inicio
            do {
                var indice = -1
                var menor = Float.MAX_VALUE
                for (j in 0 until n) {
                    if (usado[j]) continue
                    val d = dist(a[i], b[j])
                    if (d < menor) {
                        menor = d
                        indice = j
                    }
                }
                usado[indice] = true
                val peso = 1f - ((i - inicio + n) % n) / n.toFloat()
                soma += peso * menor
                i = (i + 1) % n
            } while (i != inicio)
            return soma
        }
    }
}

// ------------------------------------------------------------------------------------- meu caderno

/**
 * Uma palavra escrita pela criança: a [palavra], o [dia] (dia época) e, para cada letra, os traços como
 * ela escreveu, com x e y de 0 a 1 dentro da área de escrever (a altura é a da área, não a do quadrado).
 */
data class PaginaDoCaderno(val palavra: String, val dia: Long, val letras: List<List<List<Ponto>>>)

/**
 * O caderno guarda as últimas [MAXIMO] palavras escritas, num texto compacto (uma palavra por linha):
 * `PALAVRA|dia|letra;letra...`, cada letra com os traços separados por `/` e cada traço com os pontos
 * em números de 0 a 99 (`x,y,x,y...`). Cada traço guarda até [PONTOS_POR_TRACO] pontos.
 */
object Caderno {
    const val MAXIMO = 100
    const val PONTOS_POR_TRACO = 24

    fun adicionar(paginas: List<PaginaDoCaderno>, nova: PaginaDoCaderno): List<PaginaDoCaderno> = (paginas + nova).takeLast(MAXIMO)

    /** No máximo [maximo] pontos do traço, espalhados por ele (sempre com o primeiro e o último). */
    fun simplificar(traco: List<Ponto>, maximo: Int = PONTOS_POR_TRACO): List<Ponto> {
        if (traco.size <= maximo) return traco
        return List(maximo) { i -> traco[(i.toLong() * (traco.size - 1) / (maximo - 1)).toInt()] }
    }

    private fun q(v: Float): Int = (v.coerceIn(0f, 1f) * 99f).roundToInt()

    fun escrever(paginas: List<PaginaDoCaderno>): String = buildString {
        for (p in paginas) {
            append(p.palavra).append('|').append(p.dia).append('|')
            append(
                p.letras.joinToString(";") { letra ->
                    letra.joinToString("/") { traco -> simplificar(traco).joinToString(",") { "${q(it.x)},${q(it.y)}" } }
                },
            )
            append('\n')
        }
    }

    /** Lê o texto do caderno; linhas estragadas são puladas. */
    fun ler(texto: String): List<PaginaDoCaderno> = texto.lineSequence().mapNotNull { linha ->
        runCatching {
            val partes = linha.split('|')
            if (partes.size != 3 || partes[0].isBlank()) return@runCatching null
            val letras = if (partes[2].isEmpty()) emptyList() else partes[2].split(';').map { letra ->
                if (letra.isEmpty()) emptyList() else letra.split('/').map { traco ->
                    val n = traco.split(',').map { it.toInt() / 99f }
                    n.chunked(2).filter { it.size == 2 }.map { Ponto(it[0], it[1]) }
                }
            }
            PaginaDoCaderno(partes[0], partes[1].toLong(), letras)
        }.getOrNull()
    }.toList().takeLast(MAXIMO)
}

/**
 * O caderno guardado num arquivo, fora do backup do Android e fora da cópia do progresso. Cada criança tem o
 * seu: a criança 1 continua em `filesDir/caderno/caderno.txt` (o caminho de antes das várias crianças) e as
 * outras em `filesDir/caderno/<id>.txt`.
 */
class CadernoEmArquivo(private val arquivo: File) {

    fun ler(): List<PaginaDoCaderno> = runCatching { if (arquivo.isFile) Caderno.ler(arquivo.readText()) else emptyList() }.getOrDefault(emptyList())

    fun guardar(nova: PaginaDoCaderno) {
        runCatching {
            arquivo.parentFile?.mkdirs()
            arquivo.writeText(Caderno.escrever(Caderno.adicionar(ler(), nova)))
        }
    }

    fun apagarTudo() {
        runCatching { arquivo.delete() }
    }

    companion object {
        /** O caderno da criança 1 dentro de [pastaDoApp] (`Context.filesDir`), no caminho de sempre. */
        fun em(pastaDoApp: File) = daCrianca(pastaDoApp, Familia.PRIMEIRA)

        /** O caderno da criança [id] dentro de [pastaDoApp] (`Context.filesDir`). */
        fun daCrianca(pastaDoApp: File, id: String): CadernoEmArquivo {
            // Ids são só dígitos (Familia.idsDeTexto); qualquer outra coisa cai no caderno da criança 1.
            val nome = if (id == Familia.PRIMEIRA || id.isEmpty() || !id.all(Char::isDigit)) "caderno.txt" else "$id.txt"
            return CadernoEmArquivo(File(File(pastaDoApp, "caderno"), nome))
        }
    }
}
