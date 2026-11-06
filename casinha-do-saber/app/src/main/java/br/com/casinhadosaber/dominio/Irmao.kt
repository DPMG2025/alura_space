package br.com.casinhadosaber.dominio

/**
 * O irmãozinho (ou a irmãzinha) que mora na casinha com a criança: menor, tem a mesma pele e o mesmo cabelo
 * dela, comemora os acertos, fica pensativo nos erros e fala quando é tocado. Vem ligado e sem nome para
 * todo mundo (saves antigos também); na área dos pais o adulto pode dar o nome do irmão de verdade, trocar
 * para irmãzinha ou tirar.
 */
data class Irmao(
    val ligado: Boolean = true,
    val genero: Genero = Genero.MENINO,
    /** Nome escolhido pelo adulto (vazio = "o irmãozinho" ou "a irmãzinha"). Nunca vem do código. */
    val nome: String = "",
) {
    /** Como o narrador chama: o nome, ou "o irmãozinho"/"a irmãzinha". */
    val chamado: String
        get() = nome.ifBlank { if (genero == Genero.MENINA) "a irmãzinha" else "o irmãozinho" }

    /** O nome com letra maiúscula para começar frase ("Irmãozinho", "Bia"). */
    val nomeDeFrase: String
        get() = nome.ifBlank { if (genero == Genero.MENINA) "Irmãzinha" else "Irmãozinho" }

    companion object {
        const val TAMANHO_DO_NOME = 16

        /** Guardado como "1|MENINO|Theo" (ligado, gênero, nome). O nome é o último pedaço e pode ter "|". */
        fun paraTexto(i: Irmao): String = "${if (i.ligado) 1 else 0}|${i.genero.name}|${i.nome}"

        /** Lê o texto guardado. Ausente ou estragado vira o padrão (ligado, irmãozinho, sem nome). */
        fun deTexto(s: String?): Irmao {
            if (s.isNullOrEmpty()) return Irmao()
            val partes = s.split("|", limit = 3)
            if (partes.size != 3) return Irmao()
            val ligado = when (partes[0]) {
                "1" -> true
                "0" -> false
                else -> return Irmao()
            }
            val genero = Genero.entries.firstOrNull { it.name == partes[1] } ?: return Irmao()
            return Irmao(ligado, genero, Familia.cortarNome(partes[2].trim(), TAMANHO_DO_NOME))
        }

        /** O que o irmão fala quando a criança toca nele (gira pela lista, sem sorteio). */
        fun falaAoTocar(crianca: Perfil?, vez: Int): String {
            val quem = crianca?.nome?.takeIf { it.isNotBlank() }
            val esperto = if (crianca?.genero == Genero.MENINO) "esperto" else "esperta"
            val falas = listOf(
                if (quem != null) "Oi, $quem! Vamos brincar?" else "Oi! Vamos brincar?",
                "Hi hi hi! Faz cosquinha!",
                "Eu também quero aprender!",
                "Você é muito $esperto!",
                "Eba! Eu gosto da nossa casinha!",
            )
            return falas[Math.floorMod(vez, falas.size)]
        }

        /** Comemoração curtinha do irmão depois de um acerto (gira pela lista). */
        fun comemoracao(vez: Int): String {
            val falas = listOf("Eba!", "Uhu!", "Isso!", "Viva!", "Que legal!")
            return falas[Math.floorMod(vez, falas.size)]
        }
    }
}
