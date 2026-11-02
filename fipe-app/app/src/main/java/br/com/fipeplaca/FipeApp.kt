package br.com.fipeplaca

import android.app.Application
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.Preferencias
import br.com.fipeplaca.data.RepositorioSalvos

class FipeApp : Application() {
    lateinit var prefs: Preferencias
        private set
    lateinit var repositorio: FipeRepositorio
        private set
    lateinit var salvos: RepositorioSalvos
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Preferencias(this)
        repositorio = FipeRepositorio(prefs)
        salvos = RepositorioSalvos(this)
    }
}
