package br.com.fipeplaca.data

import android.content.Context

/** Configuracoes do usuario: token e URL do provedor de placas, token da FIPE v2. */
class Preferencias(context: Context) : ConfigConexao {
    private val sp = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    // Decifrados uma vez e mantidos em memoria (o Keystore nao e consultado a cada leitura)
    private var cacheTokenPlaca: String? = null
    private var cacheTokenFipe: String? = null

    /** true quando um token guardado nao pode ser decifrado (ex.: restauracao em outro aparelho). */
    var tokenIlegivel: Boolean = false
        private set

    private fun ler(chave: String): String {
        val bruto = sp.getString(chave, null)
        val valor = Cifra.decifrar(bruto)
        if (!bruto.isNullOrEmpty() && valor.isEmpty()) tokenIlegivel = true
        return valor
    }

    override var tokenPlaca: String
        get() = cacheTokenPlaca ?: ler(K_TOKEN_PLACA).also { cacheTokenPlaca = it }
        set(v) {
            cacheTokenPlaca = v.trim()
            tokenIlegivel = false
            sp.edit().putString(K_TOKEN_PLACA, Cifra.cifrar(v.trim())).apply()
        }

    override var urlPlaca: String
        get() = sp.getString(K_URL_PLACA, null)?.takeIf { it.isNotBlank() } ?: URL_PADRAO
        set(v) = sp.edit().putString(K_URL_PLACA, v.trim()).apply()

    override var tokenFipe: String
        get() = cacheTokenFipe ?: ler(K_TOKEN_FIPE).also { cacheTokenFipe = it }
        set(v) {
            cacheTokenFipe = v.trim()
            tokenIlegivel = false
            sp.edit().putString(K_TOKEN_FIPE, Cifra.cifrar(v.trim())).apply()
        }

    /** Tema do app: "sistema", "claro" ou "escuro". */
    var tema: String
        get() = sp.getString(K_TEMA, null) ?: "sistema"
        set(v) = sp.edit().putString(K_TEMA, v).apply()

    companion object {
        private const val K_TEMA = "tema"
        /** Modelo da API Placas (apiplacas.com.br). {placa} e {token} sao substituidos. */
        const val URL_PADRAO = "https://wdapi2.com.br/consulta/{placa}/{token}"
        private const val K_TOKEN_PLACA = "token_placa"
        private const val K_URL_PLACA = "url_placa"
        private const val K_TOKEN_FIPE = "token_fipe"
    }
}
