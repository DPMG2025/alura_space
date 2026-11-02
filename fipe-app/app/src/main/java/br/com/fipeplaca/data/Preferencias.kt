package br.com.fipeplaca.data

import android.content.Context
import br.com.fipeplaca.BuildConfig

/** Configuracoes do usuario: token e URL do provedor de placas, token da FIPE v2. */
class Preferencias(context: Context) {
    private val sp = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    var tokenPlaca: String
        get() = sp.getString(K_TOKEN_PLACA, null) ?: BuildConfig.PLACA_API_TOKEN
        set(v) = sp.edit().putString(K_TOKEN_PLACA, v.trim()).apply()

    var urlPlaca: String
        get() = sp.getString(K_URL_PLACA, null)?.takeIf { it.isNotBlank() } ?: URL_PADRAO
        set(v) = sp.edit().putString(K_URL_PLACA, v.trim()).apply()

    var tokenFipe: String
        get() = sp.getString(K_TOKEN_FIPE, null) ?: BuildConfig.FIPE_API_TOKEN
        set(v) = sp.edit().putString(K_TOKEN_FIPE, v.trim()).apply()

    companion object {
        /** Modelo da API Placas (apiplacas.com.br). {placa} e {token} sao substituidos. */
        const val URL_PADRAO = "https://wdapi2.com.br/consulta/{placa}/{token}"
        private const val K_TOKEN_PLACA = "token_placa"
        private const val K_URL_PLACA = "url_placa"
        private const val K_TOKEN_FIPE = "token_fipe"
    }
}
