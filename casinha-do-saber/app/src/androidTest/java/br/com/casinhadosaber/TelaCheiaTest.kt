package br.com.casinhadosaber

import android.content.Context
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Fase 0A do PRD do visual 3D: com o app aberto, a barra de status, a de navegação (e a barra de tarefas do
 * tablet) não podem estar visíveis. Roda no perfil de celular e no de tablet do CI.
 */
@RunWith(AndroidJUnit4::class)
class TelaCheiaTest {

    private var cenario: ActivityScenario<MainActivity>? = null

    @After
    fun fechar() {
        cenario?.close()
    }

    private fun barrasVisiveis(c: ActivityScenario<MainActivity>): Boolean {
        val visivel = AtomicBoolean(true)
        c.onActivity { atividade ->
            val insets = ViewCompat.getRootWindowInsets(atividade.window.decorView)
            visivel.set(insets?.isVisible(WindowInsetsCompat.Type.systemBars()) ?: true)
        }
        return visivel.get()
    }

    @Test
    fun barrasDoSistemaFicamEscondidas() {
        val contexto: Context = InstrumentationRegistry.getInstrumentation().targetContext
        contexto.getSharedPreferences("casinha_do_saber", Context.MODE_PRIVATE).edit().clear().commit()
        val c = ActivityScenario.launch(MainActivity::class.java).also { cenario = it }
        // O sistema leva um instante para aplicar o modo imersivo depois que a janela ganha o foco.
        var tentativas = 0
        while (barrasVisiveis(c) && tentativas < 50) {
            Thread.sleep(100)
            tentativas++
        }
        assertFalse("As barras do sistema continuam visíveis com o app aberto", barrasVisiveis(c))
        assertTrue(tentativas < 50)
    }
}
