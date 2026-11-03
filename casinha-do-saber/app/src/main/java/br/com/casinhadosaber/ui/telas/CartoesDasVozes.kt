package br.com.casinhadosaber.ui.telas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import br.com.casinhadosaber.dominio.CadernoEmArquivo
import br.com.casinhadosaber.dominio.ConjuntoDeVozes
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.FraseDaFamilia
import br.com.casinhadosaber.dominio.GrupoDeFrase
import br.com.casinhadosaber.dominio.PaginaDoCaderno
import br.com.casinhadosaber.ui.LocalCriancaAtiva
import br.com.casinhadosaber.dominio.VozesDaFamilia
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.novas.LetraEscrita

/**
 * Quantas vezes o app pediu a permissão do microfone nesta execução. Só a área dos pais pede (quando o
 * adulto toca em gravar); o teste no emulador confere que jogar nunca pede.
 */
object PedidoDoMicrofone {
    @Volatile var pedidos = 0
}

private val LilasDosPais = Color(0xFFF3EEFF)

/**
 * Vozes da família (atrás do portão de adulto): o pai ou a mãe grava a própria voz nas frases que o
 * narrador mais fala. A permissão do microfone só é pedida aqui, quando o adulto toca em gravar. As
 * gravações ficam só no aparelho e nunca são enviadas.
 */
@Composable
fun CartaoVozesDaFamilia(nome: String?, abertoNoComeco: Boolean = false) {
    val narrador = LocalNarrador.current
    val vozes = narrador.familia
    val contexto = LocalContext.current
    // Lê de novo a cada gravação nova, apagada ou troca de conjunto.
    val versao = vozes.versao
    val ativo = remember(versao) { vozes.guarda.ativo }
    val ligadas = remember(versao) { vozes.guarda.ligadas }
    val gravadas = remember(versao, ativo) { vozes.gravadas(ativo) }
    var aberto by rememberSaveable { mutableStateOf(abertoNoComeco) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var esperandoPermissao by remember { mutableStateOf<String?>(null) }

    // Sair da área dos pais termina a gravação que estiver em andamento.
    DisposableEffect(Unit) {
        onDispose {
            vozes.terminarGravacao(guardar = true)
            vozes.pararDeTocar()
        }
    }

    fun comecar(chave: String) {
        narrador.parar()
        aviso = if (vozes.comecarGravacao(ativo, chave)) null else "Não deu para gravar agora. Confira se outro app está usando o microfone."
    }

    val pedirMicrofone = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permitiu ->
        val chave = esperandoPermissao
        esperandoPermissao = null
        if (permitiu && chave != null) {
            comecar(chave)
        } else if (!permitiu) {
            aviso = "Sem o microfone não dá para gravar. O app continua falando com a voz do aparelho."
        }
    }

    fun gravarOuParar(chave: String) {
        val agora = vozes.gravando
        if (agora != null) {
            val guardou = vozes.terminarGravacao(guardar = true)
            if (agora.second != chave) return
            aviso = if (guardou) null else "Ficou curtinho demais. Toque em gravar, fale a frase e toque em parar."
            return
        }
        if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            comecar(chave)
        } else {
            esperandoPermissao = chave
            PedidoDoMicrofone.pedidos++
            pedirMicrofone.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    CartaoDosPais(Modifier.testTag("cartao_vozes_da_familia")) {
        Text("🎙️ Vozes da família", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "Grave a sua voz nas frases que o app mais fala: elogios, incentivos, chegada, vitória e boa noite. " +
                "A criança ouve você no lugar da voz do aparelho. As gravações ficam só neste aparelho e nunca são enviadas.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        Spacer(Modifier.height(10.dp))
        // Qual voz toca: mamãe ou papai (cada um com as suas gravações).
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            for (c in ConjuntoDeVozes.entries) {
                val escolhido = c == ativo
                val quantas = remember(versao, c) { vozes.gravadas(c).size }
                BotaoFofo(
                    onClick = {
                        vozes.terminarGravacao(guardar = true)
                        vozes.escolher(c)
                    },
                    modifier = Modifier.weight(1f).testTag("voz_${c.name.lowercase()}"),
                    cor = if (escolhido) Cores.Roxo else Color.White,
                    corBase = if (escolhido) Color(0xFF4B3A8C) else Color(0xFFD9CFF0),
                    descricao = "${c.titulo}${if (escolhido) ", escolhida" else ""}, $quantas gravadas",
                ) {
                    Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${c.emoji} ${c.titulo}", style = MaterialTheme.typography.titleMedium, color = if (escolhido) Color.White else Cores.Texto)
                        Text(
                            "$quantas de ${VozesDaFamilia.frases.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (escolhido) Color.White.copy(alpha = 0.85f) else Cores.TextoSuave,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        BotaoFofo(
            onClick = { vozes.ligar(!ligadas) },
            cor = Color.White,
            corBase = Color(0xFFD9CFF0),
            modifier = Modifier.testTag("vozes_ligadas"),
        ) {
            Text(
                if (ligadas) "🔊 Usar as vozes gravadas: sim" else "🔇 Usar as vozes gravadas: não (só a voz do aparelho)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        BotaoFofo(
            onClick = { aberto = !aberto },
            cor = LilasDosPais,
            corBase = Color(0xFFD9CFF0),
            modifier = Modifier.fillMaxWidth().testTag("vozes_abrir"),
        ) {
            Text(
                if (aberto) "Esconder as frases" else "🎙️ Gravar as frases (${gravadas.size} de ${VozesDaFamilia.frases.size} na ${ativo.titulo.lowercase()})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp),
            )
        }
        aviso?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Cores.Errado, modifier = Modifier.testTag("vozes_aviso"))
        }
        if (aberto) {
            for (grupo in GrupoDeFrase.entries) {
                Spacer(Modifier.height(12.dp))
                Text(grupo.titulo, style = MaterialTheme.typography.titleMedium, color = Cores.Roxo)
                for (f in VozesDaFamilia.frases.filter { it.grupo == grupo }) {
                    Spacer(Modifier.height(6.dp))
                    LinhaDaFrase(
                        frase = f,
                        texto = VozesDaFamilia.textoParaGravar(f, nome),
                        gravada = f.chave in gravadas,
                        gravando = vozes.gravando?.let { it.first == ativo && it.second == f.chave } == true,
                        tocando = vozes.tocando == f.chave,
                        // Gravando outra frase: "Ouvir" fica apagado (o som entraria no microfone).
                        podeOuvir = vozes.gravando == null,
                        onGravar = { gravarOuParar(f.chave) },
                        onOuvir = {
                            narrador.parar()
                            vozes.tocar(f.chave, ativo)
                        },
                        onApagar = { vozes.apagar(ativo, f.chave) },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Dica: fale perto do celular, num lugar quieto, do jeito carinhoso de sempre. Cada frase grava até 8 segundos. " +
                    "Na saudação, diga o nome da criança (ou um apelido). Desligar as vozes gravadas volta para a voz do aparelho sem apagar nada.",
                style = MaterialTheme.typography.bodyMedium,
                color = Cores.TextoSuave,
            )
        }
    }
}

@Composable
private fun LinhaDaFrase(
    frase: FraseDaFamilia,
    texto: String,
    gravada: Boolean,
    gravando: Boolean,
    tocando: Boolean,
    podeOuvir: Boolean,
    onGravar: () -> Unit,
    onOuvir: () -> Unit,
    onApagar: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(if (gravando) Color(0xFFFFE9E9) else LilasDosPais, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("frase_${frase.chave}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text("\"$texto\"", style = MaterialTheme.typography.titleMedium, color = Cores.Texto)
            Text(
                when {
                    gravando -> "Gravando... toque em ⏹️ para terminar"
                    gravada -> "✅ Gravada"
                    else -> "Voz do aparelho"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (gravando) Cores.Errado else if (gravada) Cores.Certo else Cores.TextoSuave,
            )
        }
        BotaoRedondo(
            if (gravando) "⏹️" else "⏺️",
            if (gravando) "Parar de gravar" else if (gravada) "Gravar de novo" else "Gravar",
            onGravar,
            modifier = Modifier.testTag("gravar_${frase.chave}"),
            cor = if (gravando) Cores.Errado else Color.White,
            tamanho = 52.dp,
        )
        if (gravada && !gravando) {
            BotaoRedondo(
                if (tocando) "🔊" else "▶️",
                if (podeOuvir) "Ouvir" else "Ouvir (termine a gravação primeiro)",
                { if (podeOuvir) onOuvir() },
                modifier = Modifier.testTag("ouvir_${frase.chave}").alpha(if (podeOuvir) 1f else 0.35f),
                tamanho = 52.dp,
            )
            BotaoRedondo("🗑️", "Apagar a gravação", onApagar, modifier = Modifier.testTag("apagar_${frase.chave}"), tamanho = 52.dp)
        }
    }
}

// ------------------------------------------------------------------------------------- meu caderno

/** Cartão que leva ao "Meu caderno": as últimas palavras que a criança escreveu com o dedo. */
@Composable
fun CartaoMeuCaderno(paginas: List<PaginaDoCaderno>, onAbrir: () -> Unit) {
    CartaoDosPais(Modifier.testTag("cartao_caderno")) {
        Text("📒 Meu caderno", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            if (paginas.isEmpty()) "As palavras que a criança escrever com o dedo (no caminho das Letras) aparecem aqui, com a letra dela."
            else "${paginas.size} palavras escritas com o dedo. Veja a letra da criança mudando com o tempo.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        if (paginas.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            for (p in paginas.takeLast(2).reversed()) PalavraDoCaderno(p, ladoDaLetra = 34)
            Spacer(Modifier.height(8.dp))
            BotaoFofo(onClick = onAbrir, cor = Color.White, corBase = Color(0xFFD9CFF0), modifier = Modifier.testTag("abrir_caderno")) {
                Text("📖 Abrir o caderno", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

private fun Data.curtinha() = "${dia}/${mes.toString().padStart(2, '0')}"

@Composable
private fun PalavraDoCaderno(p: PaginaDoCaderno, ladoDaLetra: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFFFFDF5), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE9E1C8), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(78.dp)) {
            Text(p.palavra, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cores.Texto)
            Text(Data.deDiaEpoca(p.dia).curtinha(), style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (letra in p.letras) {
                LetraEscrita(
                    letra,
                    Color(0xFF2F4FA8),
                    Modifier.size(ladoDaLetra.dp, (ladoDaLetra * 1.2f).dp),
                    linhas = true,
                )
            }
        }
    }
}

/** "Meu caderno" (área dos pais): as últimas 100 palavras escritas, da mais nova para a mais antiga. */
@Composable
fun TelaCaderno(paginas: List<PaginaDoCaderno>, onVoltar: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBEF))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("tela_caderno"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 48.dp)
            Spacer(Modifier.width(12.dp))
            Text("📒 Meu caderno", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "As palavras que a criança escreveu com o dedo, do jeitinho dela (as últimas ${br.com.casinhadosaber.dominio.Caderno.MAXIMO}). " +
                "Fica só neste aparelho.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        Spacer(Modifier.height(12.dp))
        if (paginas.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "✏️ Ainda não há palavras. Elas aparecem quando a criança escreve com o dedo no caminho das Letras.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            for (p in paginas.reversed()) PalavraDoCaderno(p, ladoDaLetra = 44)
        }
    }
}

/** O caderno da criança [id] no aparelho. */
fun cadernoDa(contexto: Context, id: String): CadernoEmArquivo = CadernoEmArquivo.daCrianca(contexto.filesDir, id)

/** O caderno da criança que está jogando (lido de novo a cada abertura da área dos pais e ao trocar de criança). */
@Composable
fun lembrarCaderno(): List<PaginaDoCaderno> {
    val contexto = LocalContext.current
    val id = LocalCriancaAtiva.current
    return remember(id) { cadernoDa(contexto, id).ler() }
}
