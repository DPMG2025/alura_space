package br.com.fipeplaca

import android.app.Application
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.Preferencias

class FipeApp : Application() {
    lateinit var prefs: Preferencias
        private set
    lateinit var repositorio: FipeRepositorio
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Preferencias(this)
        repositorio = FipeRepositorio(prefs)
    }
}
