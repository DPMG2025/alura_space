package br.com.casinhadosaber.dados

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import br.com.casinhadosaber.dominio.DesenhoDeColorir
import br.com.casinhadosaber.dominio.Obra
import br.com.casinhadosaber.ui.componentes.desenharObra
import java.io.File

/**
 * A obra do Livro de Colorir como imagem, para os pais guardarem: desenha a obra num PNG quadrado e abre o
 * "enviar" do Android (Fotos, Drive, WhatsApp). Mesmo caminho da cópia do progresso ([ArquivoDaCopia]):
 * FileProvider no cache, sem permissão de armazenamento nem internet. Nada aqui lança exceção.
 */
object ImagemDaObra {
    const val LADO = 1024

    /** A obra desenhada num bitmap [lado] por [lado], com papel branco atrás. */
    fun bitmap(d: DesenhoDeColorir, o: Obra, lado: Int = LADO): Bitmap {
        val imagem = ImageBitmap(lado, lado)
        val tela = Canvas(imagem)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, tela, Size(lado.toFloat(), lado.toFloat())) {
            drawRect(Color.White)
            desenharObra(d, o, lado.toFloat())
        }
        return imagem.asAndroidBitmap()
    }

    fun nomeDoArquivo(d: DesenhoDeColorir): String = "casinha-${d.id}.png"

    fun enviar(context: Context, d: DesenhoDeColorir, o: Obra): Boolean = runCatching {
        val pasta = File(context.cacheDir, "copias/obras").apply { mkdirs() }
        pasta.listFiles()?.forEach { it.delete() }
        val arquivo = File(pasta, nomeDoArquivo(d))
        arquivo.outputStream().use { bitmap(d, o).compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.copias", arquivo)
        val envio = Intent(Intent.ACTION_SEND)
            .setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_SUBJECT, d.titulo)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        envio.clipData = ClipData.newRawUri(d.titulo, uri)
        context.startActivity(Intent.createChooser(envio, "Guardar a pintura").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    }.getOrDefault(false)
}
