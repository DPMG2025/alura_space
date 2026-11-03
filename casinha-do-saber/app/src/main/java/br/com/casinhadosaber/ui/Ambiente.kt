package br.com.casinhadosaber.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Familia

val LocalNarrador = staticCompositionLocalOf<Narrador> { error("Narrador não configurado") }
val LocalSons = staticCompositionLocalOf<Sons> { error("Sons não configurados") }

/**
 * Id da criança que está jogando (`Repositorio.ativa`), para o que fica em arquivo por criança, como o
 * "Meu caderno". Sem fornecedor (capturas, testes) é a criança 1, no caminho de sempre.
 */
val LocalCriancaAtiva = compositionLocalOf { Familia.PRIMEIRA }
