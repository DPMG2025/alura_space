package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.com.casinhadosaber.dominio.Vocabulario
import kotlinx.coroutines.delay

/*
 * Auxiliares das telas de Balança, Mercadinho, Relógio e Pintar por código (prefixo N3 para não
 * colidir com os auxiliares das outras telas novas do mesmo pacote).
 */

/**
 * A criança está parada há um tempinho? Liga a mãozinha que ensina o gesto: logo no começo, mais
 * cedo quando a ajuda já está valendo e de novo sempre que ela parar de mexer.
 */
@Composable
internal fun paradaN3(interacoes: Int, terminou: Boolean, ativa: Boolean, ajuda: Boolean, ajudaInicial: Boolean): Boolean {
    var parada by remember { mutableStateOf(false) }
    LaunchedEffect(interacoes, terminou, ativa, ajuda) {
        parada = false
        if (!ativa || terminou) return@LaunchedEffect
        delay(
            when {
                ajudaInicial && interacoes == 0 -> 700L
                interacoes == 0 -> 1400L
                ajuda -> 3000L
                else -> 7000L
            },
        )
        parada = true
    }
    return parada
}

/**
 * Uma escolha de itens de [valores] (cada um no máximo uma vez) que soma [alvo], a que mais
 * aproveita os que a criança já colocou ([colocados]) e menos pede para tirar. Nulo se não houver.
 * Até 12 itens: dá para olhar todas as escolhas.
 */
internal fun escolhaQueSomaN3(valores: List<Int>, alvo: Int, colocados: Set<Int> = emptySet()): Set<Int>? {
    val n = valores.size.coerceAtMost(12)
    var melhor: Set<Int>? = null
    var nota = Int.MIN_VALUE
    for (m in 0 until (1 shl n)) {
        var soma = 0
        for (i in 0 until n) if (m and (1 shl i) != 0) soma += valores[i]
        if (soma != alvo) continue
        val s = (0 until n).filter { m and (1 shl it) != 0 }.toSet()
        // Aproveitar o que já está colocado vale mais; tirar custa; menos peças é mais fácil.
        val pontos = (s intersect colocados).size * 100 - (colocados - s).size * 100 - s.size
        if (pontos > nota) {
            nota = pontos
            melhor = s
        }
    }
    return melhor
}

/** Dinheiro escrito: 350 → "R$ 3,50". */
internal fun reaisN3(centavos: Int): String = "R$ ${centavos / 100},${(centavos % 100).toString().padStart(2, '0')}"

/** Dinheiro falado: 350 → "três reais e cinquenta centavos"; 100 → "um real"; 50 → "cinquenta centavos". */
internal fun reaisPorExtensoN3(centavos: Int): String {
    val r = centavos / 100
    val c = centavos % 100
    val parteReais = when (r) {
        0 -> ""
        1 -> "um real"
        else -> "${Vocabulario.porExtenso(r)} reais"
    }
    val parteCentavos = when (c) {
        0 -> ""
        1 -> "um centavo"
        else -> "${Vocabulario.porExtenso(c)} centavos"
    }
    return when {
        r == 0 && c == 0 -> "zero real"
        r == 0 -> parteCentavos
        c == 0 -> parteReais
        else -> "$parteReais e $parteCentavos"
    }
}

/** Hora falada: 3:00 → "três horas"; 1:00 → "uma hora"; 3:30 → "três e meia"; 3:15 → "três e quinze". */
internal fun horaPorExtensoN3(hora: Int, minuto: Int): String {
    val h = Vocabulario.porExtenso(hora, feminino = true)
    return when (minuto) {
        0 -> if (hora == 1) "uma hora" else "$h horas"
        30 -> "$h e meia"
        else -> "$h e ${Vocabulario.porExtenso(minuto)}"
    }
}

/** Hora escrita como no relógio digital: 3:05 → "3:05". */
internal fun horaDigitalN3(hora: Int, minuto: Int): String = "$hora:${minuto.toString().padStart(2, '0')}"
