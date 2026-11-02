package br.com.fipeplaca.data

import android.content.Context

/** Configuracoes do usuario: token e URL do provedor de placas, token da FIPE v2. */
class Preferencias(context: Context) : ConfigConexao {
    private val sp = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    override var tokenPlaca: String
        get() = sp.getString(K_TOKEN_PLACA, null).orEmpty()
        set(v) = sp.edit().putString(K_TOKEN_PLACA, v.trim()).apply()

    override var urlPlaca: String
        get() = sp.getString(K_URL_PLACA, null)?.takeIf { it.isNotBlank() } ?: URL_PADRAO
        set(v) = sp.edit().putString(K_URL_PLACA, v.trim()).apply()

    override var tokenFipe: String
        get() = sp.getString(K_TOKEN_FIPE, null).orEmpty()
        set(v) = sp.edit().putString(K_TOKEN_FIPE, v.trim()).apply()

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
