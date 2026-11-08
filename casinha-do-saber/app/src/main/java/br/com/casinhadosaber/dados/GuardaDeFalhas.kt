package br.com.casinhadosaber.dados

import android.content.Context
import android.os.Build
import br.com.casinhadosaber.dominio.Falha
import br.com.casinhadosaber.dominio.RegistroDeFalhas

/**
 * Anota no aparelho quando o app fecha sozinho (erro não tratado) e depois deixa o Android fechar como sempre.
 * A área dos pais mostra o registro ([ler]) e o adulto copia o detalhe. Nada sai do aparelho sozinho.
 */
object GuardaDeFalhas {
    private const val ARQUIVO = "falhas_do_app"
    private const val CHAVE = "falhas"

    /** Nome da tela aberta agora (a MainActivity atualiza a cada troca de tela). */
    @Volatile
    var telaAtual: String = "abertura"

    private var instalada = false

    fun instalar(context: Context) {
        if (instalada) return
        instalada = true
        val app = context.applicationContext
        val anterior = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, erro ->
            // Anotar nunca pode atrapalhar o fechamento normal: qualquer problema aqui é engolido.
            runCatching { anotar(app, RegistroDeFalhas.montar(System.currentTimeMillis(), telaAtual, erro)) }
            if (anterior != null) anterior.uncaughtException(thread, erro) else android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    /** Grava na hora (commit), porque o processo vai morrer logo em seguida. */
    fun anotar(context: Context, f: Falha) {
        val prefs = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)
        val lista = RegistroDeFalhas.acrescentar(RegistroDeFalhas.deTexto(prefs.getString(CHAVE, null)), f)
        prefs.edit().putString(CHAVE, RegistroDeFalhas.paraTexto(lista)).commit()
    }

    fun ler(context: Context): List<Falha> = runCatching {
        RegistroDeFalhas.deTexto(context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).getString(CHAVE, null))
    }.getOrDefault(emptyList())

    fun limpar(context: Context) {
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).edit().remove(CHAVE).apply()
    }

    fun versao(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    }.getOrDefault("?")

    val aparelho: String get() = "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}
