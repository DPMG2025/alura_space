package br.com.fipeplaca.data

/** Fonte das configuracoes usadas pelo repositorio (permite testes sem Android). */
interface ConfigConexao {
    val tokenPlaca: String
    val urlPlaca: String
    val tokenFipe: String
}
