package br.com.casinhadosaber.ui.componentes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.io.IOException

/**
 * Imagem da arte 3D pré-renderizada (`assets/arte/<id>.webp`, ver `arte3d/` e o PRD visual 3D).
 *
 * Sem tamanho no [modifier], aparece no tamanho de uso do manifesto (em dp), limitado pelo espaço
 * disponível; com tamanho, preenche esse tamanho mantendo a proporção ([escala]). A imagem é
 * decodificada já reduzida (inSampleSize) para o tamanho em que aparece e fica num cache em memória.
 *
 * [filtro] serve para tingir (pele, cabelo, roupa: `ColorFilter.tint(cor, BlendMode.Modulate)`).
 * Se a imagem não existir no manifesto ou não puder ser lida, mostra [reserva] (por exemplo, o emoji).
 */
@Composable
fun ImagemDeArte(
    id: String,
    modifier: Modifier = Modifier,
    descricao: String? = null,
    filtro: ColorFilter? = null,
    alinhamento: Alignment = Alignment.Center,
    escala: ContentScale = ContentScale.Fit,
    reserva: (@Composable () -> Unit)? = null,
) {
    val contexto = LocalContext.current
    val info = remember(id) { ArteDoApp.info(contexto, id) }
    BoxWithConstraints(modifier, contentAlignment = alinhamento, propagateMinConstraints = true) {
        if (info == null) {
            reserva?.invoke()
        } else {
            val densidade = LocalDensity.current
            val usoL = with(densidade) { info.usoLargura.dp.roundToPx() }
            val usoA = with(densidade) { info.usoAltura.dp.roundToPx() }
            // tamanho final em pixels, igual ao que o Modifier.size abaixo vai dar dentro destas restrições
            val alvoL = usoL.coerceIn(constraints.minWidth, constraints.maxWidth)
            val alvoA = usoA.coerceIn(constraints.minHeight, constraints.maxHeight)
            val amostra = amostraDaArte(info.largura, info.altura, alvoL, alvoA)
            val imagem = remember(info, amostra) { ArteDoApp.bitmap(contexto, info, amostra)?.asImageBitmap() }
            if (imagem == null) {
                reserva?.invoke()
            } else {
                Image(
                    bitmap = imagem,
                    contentDescription = descricao,
                    modifier = Modifier.size(info.usoLargura.dp, info.usoAltura.dp),
                    alignment = alinhamento,
                    contentScale = escala,
                    colorFilter = filtro,
                )
            }
        }
    }
}

/** Manifesto e cache das imagens da arte 3D, compartilhados pelo app inteiro. */
object ArteDoApp {
    const val PASTA = "arte"

    @Volatile private var manifesto: ManifestoDaArte? = null

    fun manifesto(contexto: Context): ManifestoDaArte =
        manifesto ?: synchronized(this) {
            manifesto ?: lerManifesto(contexto).also { manifesto = it }
        }

    fun info(contexto: Context, id: String): InfoDaArte? = manifesto(contexto).imagens[id]

    private fun lerManifesto(contexto: Context): ManifestoDaArte = try {
        contexto.assets.open("$PASTA/manifesto.json").bufferedReader().use { ManifestoDaArte.ler(it.readText()) }
    } catch (e: IOException) {
        ManifestoDaArte.VAZIO
    }

    /** Cache por arquivo e redução, medido em KB; usa até 1/8 da memória do app (padrão do Android). */
    private val cache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt().coerceAtLeast(4 * 1024),
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
    }

    /** A imagem decodificada com [amostra] (inSampleSize), do cache quando já foi lida; nula se faltar o arquivo. */
    fun bitmap(contexto: Context, info: InfoDaArte, amostra: Int): Bitmap? {
        val chave = "${info.arquivo}@$amostra"
        cache.get(chave)?.let { return it }
        val opcoes = BitmapFactory.Options().apply {
            inSampleSize = amostra
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val lido: Bitmap? = try {
            contexto.assets.open("$PASTA/${info.arquivo}").use { BitmapFactory.decodeStream(it, null, opcoes) }
        } catch (e: IOException) {
            null
        }
        val bmp = lido ?: return null
        bmp.prepareToDraw()
        cache.put(chave, bmp)
        return bmp
    }

    fun limparCache() = cache.evictAll()
}
