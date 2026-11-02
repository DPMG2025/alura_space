package br.com.fipeplaca

import android.app.Application
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.Preferencias
import br.com.fipeplaca.data.RepositorioSalvos
import kotlinx.coroutines.flow.MutableStateFlow

class FipeApp : Application() {
    lateinit var prefs: Preferencias
        private set
    lateinit var repositorio: FipeRepositorio
        internal set // substituivel nos testes
    lateinit var salvos: RepositorioSalvos
        private set

    /** Tema escolhido em Ajustes; a Activity observa e aplica na hora. */
    val tema = MutableStateFlow("sistema")

    fun definirTema(valor: String) {
        prefs.tema = valor
        tema.value = valor
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Preferencias(this)
        repositorio = FipeRepositorio(prefs, diretorioCache = java.io.File(cacheDir, "http"))
        salvos = RepositorioSalvos(this)
        tema.value = prefs.tema
    }
}
