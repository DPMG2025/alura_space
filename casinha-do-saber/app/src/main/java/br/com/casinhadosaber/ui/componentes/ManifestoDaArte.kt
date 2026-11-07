package br.com.casinhadosaber.ui.componentes

/**
 * Manifesto da arte 3D pré-renderizada (`assets/arte/manifesto.json`), gerado por `arte3d/render.js`.
 * Kotlin puro (sem Android) para o teste de unidade ler o mesmo arquivo com o mesmo código do app.
 */
class ManifestoDaArte(
    val versao: Int,
    /** Pixels da imagem por dp de uso (o render sai no dobro do tamanho em que aparece). */
    val escala: Int,
    val imagens: Map<String, InfoDaArte>,
) {
    companion object {
        val VAZIO = ManifestoDaArte(versao = 1, escala = 2, imagens = emptyMap())

        fun ler(json: String): ManifestoDaArte {
            val raiz = LeitorJson(json).ler() as? Map<*, *> ?: error("manifesto da arte: a raiz deve ser um objeto")
            val imagens = (raiz["imagens"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()
            return ManifestoDaArte(
                versao = numero(raiz, "versao"),
                escala = numero(raiz, "escala"),
                imagens = imagens.entries.associate { (k, v) ->
                    val id = k as String
                    id to info(id, v as? Map<*, *> ?: error("manifesto da arte: '$id' deve ser um objeto"))
                },
            )
        }

        private fun info(id: String, m: Map<*, *>): InfoDaArte {
            val uso = m["uso"] as? Map<*, *> ?: error("manifesto da arte: '$id' sem 'uso'")
            val ancoras = (m["ancoras"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()
            return InfoDaArte(
                id = id,
                arquivo = m["arquivo"] as? String ?: error("manifesto da arte: '$id' sem 'arquivo'"),
                tipo = m["tipo"] as? String ?: "",
                largura = numero(m, "largura"),
                altura = numero(m, "altura"),
                usoLargura = numero(uso, "largura"),
                usoAltura = numero(uso, "altura"),
                apoio = ponto(m["apoio"], "$id.apoio"),
                ancoras = ancoras.entries.associate { (k, v) -> (k as String) to ponto(v, "$id.ancoras.$k") },
            )
        }

        private fun numero(m: Map<*, *>, chave: String): Int =
            (m[chave] as? Number)?.toInt() ?: error("manifesto da arte: falta o número '$chave'")

        private fun ponto(v: Any?, onde: String): PontoDaArte {
            val m = v as? Map<*, *> ?: error("manifesto da arte: '$onde' deve ser {x, y}")
            return PontoDaArte(numero(m, "x"), numero(m, "y"))
        }
    }
}

/** Um ponto na imagem, em pixels da imagem (origem no canto de cima à esquerda). */
data class PontoDaArte(val x: Int, val y: Int)

/**
 * Uma imagem da arte: [largura] x [altura] em pixels, mostrada em até [usoLargura] x [usoAltura] dp.
 * [apoio] é onde a peça encosta no chão (meio da base); [ancoras] são pontos úteis do modelo
 * (centro da cabeça para chapéus, mãos para acessórios).
 */
data class InfoDaArte(
    val id: String,
    val arquivo: String,
    val tipo: String,
    val largura: Int,
    val altura: Int,
    val usoLargura: Int,
    val usoAltura: Int,
    val apoio: PontoDaArte,
    val ancoras: Map<String, PontoDaArte>,
) {
    /** [ponto] como fração da imagem (0 a 1), para posicionar em qualquer tamanho de exibição. */
    fun fracao(ponto: PontoDaArte): Pair<Float, Float> = ponto.x.toFloat() / largura to ponto.y.toFloat() / altura
}

/**
 * Fator de redução (inSampleSize) para decodificar uma imagem de [largura] x [altura] que vai aparecer
 * em [alvoLargura] x [alvoAltura] pixels: a maior potência de 2 que não deixa nenhum lado menor que o
 * alvo. Menos memória e decodificação mais rápida nos aparelhos antigos.
 */
fun amostraDaArte(largura: Int, altura: Int, alvoLargura: Int, alvoAltura: Int): Int {
    val l = alvoLargura.coerceAtLeast(1)
    val a = alvoAltura.coerceAtLeast(1)
    var amostra = 1
    while (largura / (amostra * 2) >= l && altura / (amostra * 2) >= a) amostra *= 2
    return amostra
}

/** Leitor de JSON mínimo (objetos, listas, textos, números, true, false, null), sem dependências. */
internal class LeitorJson(private val s: String) {
    private var i = 0

    fun ler(): Any? {
        val v = valor()
        espacos()
        if (i != s.length) erro("sobra texto")
        return v
    }

    private fun erro(o: String): Nothing = throw IllegalArgumentException("JSON inválido na posição $i: $o")

    private fun espacos() {
        while (i < s.length && s[i].isWhitespace()) i++
    }

    private fun atual(): Char = if (i < s.length) s[i] else erro("fim inesperado")

    private fun valor(): Any? {
        espacos()
        return when (val c = atual()) {
            '{' -> objeto()
            '[' -> lista()
            '"' -> texto()
            't' -> palavra("true", true)
            'f' -> palavra("false", false)
            'n' -> palavra("null", null)
            else -> if (c == '-' || c.isDigit()) numero() else erro("caractere inesperado '$c'")
        }
    }

    private fun palavra(p: String, v: Any?): Any? {
        if (!s.startsWith(p, i)) erro("esperava $p")
        i += p.length
        return v
    }

    private fun objeto(): Map<String, Any?> {
        i++
        val m = LinkedHashMap<String, Any?>()
        espacos()
        if (atual() == '}') { i++; return m }
        while (true) {
            espacos()
            if (atual() != '"') erro("esperava uma chave")
            val k = texto()
            espacos()
            if (atual() != ':') erro("esperava ':'")
            i++
            m[k] = valor()
            espacos()
            when (atual()) {
                ',' -> i++
                '}' -> { i++; return m }
                else -> erro("esperava ',' ou '}'")
            }
        }
    }

    private fun lista(): List<Any?> {
        i++
        val l = ArrayList<Any?>()
        espacos()
        if (atual() == ']') { i++; return l }
        while (true) {
            l += valor()
            espacos()
            when (atual()) {
                ',' -> i++
                ']' -> { i++; return l }
                else -> erro("esperava ',' ou ']'")
            }
        }
    }

    private fun texto(): String {
        i++
        val b = StringBuilder()
        while (true) {
            val c = atual()
            i++
            when (c) {
                '"' -> return b.toString()
                '\\' -> {
                    val e = atual()
                    i++
                    when (e) {
                        'n' -> b.append('\n')
                        't' -> b.append('\t')
                        'r' -> b.append('\r')
                        'b' -> b.append('\b')
                        'f' -> b.append('\u000C')
                        'u' -> {
                            if (i + 4 > s.length) erro("escape \\u incompleto")
                            b.append(s.substring(i, i + 4).toInt(16).toChar())
                            i += 4
                        }
                        else -> b.append(e)
                    }
                }
                else -> b.append(c)
            }
        }
    }

    private fun numero(): Double {
        val inicio = i
        while (i < s.length && (s[i].isDigit() || s[i] in "+-.eE")) i++
        return s.substring(inicio, i).toDoubleOrNull() ?: erro("número inválido")
    }
}
