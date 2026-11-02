package br.com.casinhadosaber.ui

import androidx.compose.runtime.staticCompositionLocalOf
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons

val LocalNarrador = staticCompositionLocalOf<Narrador> { error("Narrador não configurado") }
val LocalSons = staticCompositionLocalOf<Sons> { error("Sons não configurados") }
