package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Registro de fechamentos: monta a falha com a causa, guarda as 5 mais novas e lê de volta sem quebrar. */
class RegistroDeFalhasTest {

    private fun erro(): Throwable = try {
        try {
            throw IllegalStateException("bitmap grande\\ndemais")
        } catch (e: Exception) {
            throw RuntimeException("na tela de prêmio", e)
        }
    } catch (e: Exception) {
        e
    }

    @Test
    fun `monta a falha com a tela o tipo e a causa mais funda`() {
        val f = RegistroDeFalhas.montar(1000L, "Premio", erro())
        assertEquals("Premio", f.tela)
        assertEquals("java.lang.RuntimeException", f.tipo)
        assertEquals("na tela de prêmio", f.mensagem)
        assertTrue(f.pilha.any { it.startsWith("Causa: java.lang.IllegalStateException") })
        assertTrue(f.pilha.size <= RegistroDeFalhas.LINHAS_DA_PILHA)
        assertEquals("RuntimeException", RegistroDeFalhas.nomeCurto(f))
    }

    @Test
    fun `guarda so as cinco mais novas com a mais nova primeiro`() {
        var lista = emptyList<Falha>()
        for (i in 1..7) lista = RegistroDeFalhas.acrescentar(lista, Falha(i.toLong(), "T$i", "E", "", emptyList()))
        assertEquals(listOf(7L, 6L, 5L, 4L, 3L), lista.map { it.quando })
    }

    @Test
    fun `texto vai e volta igual e ignora lixo`() {
        val a = RegistroDeFalhas.montar(42L, "Jogo", erro())
        val b = Falha(7L, "Casa", "java.lang.OutOfMemoryError", "Failed to allocate", listOf("  em x"))
        val volta = RegistroDeFalhas.deTexto(RegistroDeFalhas.paraTexto(listOf(a, b)))
        assertEquals(listOf(a, b), volta)
        assertTrue(RegistroDeFalhas.deTexto(null).isEmpty())
        assertTrue(RegistroDeFalhas.deTexto("lixo").isEmpty())
        assertEquals(1, RegistroDeFalhas.deTexto("abc\u001Fa\u001Fb\u001Fc\u001E9\u001FT\u001FE\u001FM").size)
    }

    @Test
    fun `texto para copiar traz versao aparelho tela e pilha`() {
        val f = Falha(1L, "Premio", "java.lang.OutOfMemoryError", "Failed to allocate", listOf("  em a.b(C.kt:1)"))
        val t = RegistroDeFalhas.textoParaCopiar(f, "1.0.0", "Samsung SM-T500, Android 12", "08/11 14:30")
        assertTrue(t.contains("1.0.0"))
        assertTrue(t.contains("SM-T500"))
        assertTrue(t.contains("na tela Premio"))
        assertTrue(t.contains("OutOfMemoryError: Failed to allocate"))
        assertTrue(t.endsWith("em a.b(C.kt:1)"))
    }
}
