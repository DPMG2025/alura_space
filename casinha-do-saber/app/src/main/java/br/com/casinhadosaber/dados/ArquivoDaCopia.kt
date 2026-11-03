package br.com.casinhadosaber.dados

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import br.com.casinhadosaber.dominio.Copia
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Lê e escreve o arquivo da cópia do progresso. Sem internet: o arquivo vai para onde o adulto escolher
 * (seletor de arquivos do Android) ou para o app que ele escolher no "enviar" (Drive, WhatsApp, e-mail).
 * Nada aqui lança exceção: falhas viram false ou null e a tela mostra uma mensagem amigável.
 */
object ArquivoDaCopia {

    /** Grava [texto] no arquivo que o adulto criou pelo seletor (ACTION_CREATE_DOCUMENT). */
    fun escrever(context: Context, uri: Uri, texto: String): Boolean = runCatching {
        val saida = context.contentResolver.openOutputStream(uri) ?: return false
        saida.use { it.write(texto.toByteArray(Charsets.UTF_8)) }
        true
    }.getOrDefault(false)

    /** Lê o arquivo escolhido (ACTION_OPEN_DOCUMENT), no máximo [Copia.TAMANHO_MAXIMO] bytes; null se não der. */
    fun ler(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            val buffer = ByteArray(64 * 1024)
            val tudo = ByteArrayOutputStream()
            while (true) {
                val n = entrada.read(buffer)
                if (n < 0) break
                tudo.write(buffer, 0, n)
                if (tudo.size() > Copia.TAMANHO_MAXIMO) return null
            }
            tudo.toString(Charsets.UTF_8.name())
        }
    }.getOrNull()

    /**
     * Abre o "enviar" do Android com a cópia (FileProvider, sem permissão de armazenamento): o adulto escolhe
     * Drive, WhatsApp, e-mail. O arquivo temporário fica no cache do app (fora do backup).
     */
    fun enviar(context: Context, texto: String, nome: String): Boolean = runCatching {
        val pasta = File(context.cacheDir, "copias").apply { mkdirs() }
        pasta.listFiles()?.forEach { it.delete() }
        val arquivo = File(pasta, nome)
        arquivo.writeText(texto, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.copias", arquivo)
        val envio = Intent(Intent.ACTION_SEND)
            .setType(Copia.TIPO_DO_ARQUIVO)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_SUBJECT, nome)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        envio.clipData = ClipData.newRawUri(nome, uri)
        context.startActivity(Intent.createChooser(envio, "Enviar a cópia do progresso"))
        true
    }.getOrDefault(false)
}
