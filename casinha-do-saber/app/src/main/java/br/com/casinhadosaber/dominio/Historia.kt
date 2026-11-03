package br.com.casinhadosaber.dominio

/**
 * História dos mundos (sugestão 3): cada caminho tem 25 capítulos, um por mundo ([Mundos]).
 *
 * A Corujinha Professora apresenta cada mundo, e cada caminho tem um amigo que acompanha a criança
 * ([Personagem]). Os guardiões dos chefões são sempre amigos: o chefão é um desafio, nunca um inimigo.
 * Os textos são curtos (1 a 3 frases) e feitos para o narrador ler em voz alta.
 *
 * Uso nas telas: [introducao] ao entrar num mundo (primeira fase dele, [Mundos.comecaMundo]) e
 * [cenaDoChefao] na vitória da 10ª fase ([Mundos.ehChefao]).
 */
enum class Personagem(val nome: String, val emoji: String) {
    CORUJINHA("Corujinha Professora", "🦉"),
    TITO("Tito, o esquilo contador", "🐿️"),
    LIA("Lia, a lagartinha das letras", "🐛"),
    BOB("Bob, o ursinho viajante", "🧸"),
    PIPO("Pipo, o robozinho curioso", "🤖"),
}

/** Um capítulo: o mundo [mundo] (índice de 0 a 24) do caminho [trilha]. */
data class Capitulo(
    val trilha: Trilha,
    val mundo: Int,
    /** Quem fala na abertura (sempre a Corujinha) e o amigo do caminho que aparece junto. */
    val narradora: Personagem,
    val amigo: Personagem,
    /** Lido ao entrar no mundo. */
    val introducao: String,
    /** O guardião do chefão do mundo (um amigo, nunca um vilão). */
    val guardiao: String,
    /** Lido quando a criança vence a 10ª fase do mundo. */
    val cenaDoChefao: String,
)

object Historia {

    /** O amigo de cada caminho. */
    fun amigoDe(trilha: Trilha): Personagem = when (trilha) {
        Trilha.MATEMATICA -> Personagem.TITO
        Trilha.PORTUGUES -> Personagem.LIA
        Trilha.INGLES -> Personagem.BOB
        Trilha.LOGICA -> Personagem.PIPO
    }

    /** O que a Corujinha conta ao chegar em cada mundo (igual para os quatro caminhos). */
    private val cenas = listOf(
        "A Corujinha Professora abriu o portão do Jardim, cheio de flores coloridas.",
        "Na Floresta, as árvores altas balançam com o vento e os passarinhos cantam.",
        "Chegamos à Praia! A areia está quentinha e as ondas fazem chuá, chuá.",
        "A trilha sobe a Montanha devagarinho, e lá de cima dá para ver tudo.",
        "Um foguete levou a turma para o Espaço, onde as estrelas brilham pertinho.",
        "Na Fazenda, a vaquinha faz muuu e as galinhas ciscam no quintal.",
        "A turma colocou a roupa de mergulho e desceu ao Fundo do Mar, cheio de peixinhos.",
        "Na Selva, os macaquinhos pulam de galho em galho e dão risada.",
        "Brrr! No Polo Norte tudo é branquinho, e os pinguins andam balançando.",
        "O Circo abriu a lona colorida, e o palhaço acenou para a turma.",
        "No Deserto, o camelo leva a turma passeando pela areia dourada.",
        "Na Cidade, os carros param no sinal vermelho e as pessoas passeiam na calçada.",
        "A ponte do Castelo desceu devagar, e a turma entrou no grande salão.",
        "No Vale dos Dinossauros, um dinossauro grandão e muito gentil disse olá.",
        "A turma chegou de barquinho à Ilha do Tesouro, com um mapa cheio de pistas.",
        "Na Fábrica de Doces tem cheirinho de bolo e pirulitos de todas as cores.",
        "No Bosque Encantado, os cogumelos brilham e as fadinhas dão um alô.",
        "O Vulcão está dormindo tranquilo, e a terra em volta é quentinha.",
        "Um balão levou a turma ao Reino das Nuvens, macias como algodão.",
        "Na Caverna de Cristal, as pedras brilham com todas as cores.",
        "No Parque de Diversões tem roda gigante, carrossel e muita alegria.",
        "Na Cidade dos Robôs tudo faz bip bop, e os robôs adoram ajudar.",
        "Na Savana, o leão cochila na sombra e a girafa come folhinhas lá do alto.",
        "No Vale das Cores, cada flor, cada pedra e cada rio tem uma cor diferente.",
        "Lá no alto fica o Castelo das Estrelas, a última parada da grande viagem.",
    )

    private val amigoNumeros = listOf(
        "Tito, o esquilo, quer contar as flores com você.",
        "Tito juntou pinhas e quer saber quantas ficam se tirar algumas.",
        "Tito está contando conchinhas na areia, de dez em dez.",
        "Tito vai medir a montanha e guardar moedinhas no bolso.",
        "Tito quer contar as estrelas do céu, até cem!",
        "Na fila dos bichos, Tito quer saber quem é o primeiro e quem é o último.",
        "Tito precisa juntar peixinhos até formar dez.",
        "Os macacos comeram bananas, e Tito quer saber quantas sobraram.",
        "Tito vai pular pelos números até cem no gelo escorregadio.",
        "No picadeiro, Tito separa dezenas e unidades com o material dourado.",
        "Tito soma dezenas para encher os cantis da caravana.",
        "Tito abriu um mercadinho e precisa da sua ajuda para pagar com moedas e notas.",
        "Tito quer medir as torres do castelo e pesar os baús.",
        "O dinossauro perdeu a hora! Tito vai ensinar a ler o relógio.",
        "Tito achou um mapa cheio de formas: cubos, bolas, cones e cilindros.",
        "Tito quer juntar balas e pirulitos com contas de dois números.",
        "As fadinhas guardaram frutinhas, e Tito quer saber quantas faltam.",
        "Perto do vulcão, Tito conta pedrinhas de cem em cem.",
        "Tito soma as nuvens, e às vezes vai um!",
        "Tito procura cristais e quer saber quantos cada um tem a mais.",
        "No carrossel, os cavalinhos andam em grupos iguais. Tito quer contar todos.",
        "Os robôs querem repartir as pecinhas igualzinho. Tito pede a sua ajuda.",
        "Tito fez um gráfico dos bichos da savana. Vamos ler juntos?",
        "Tito descobriu padrões de números escondidos nas cores.",
        "Tito vai usar tudo o que aprendeu para chegar ao alto do castelo.",
    )

    private val amigoLetras = listOf(
        "Lia, a lagartinha, mostra as vogais escondidas nas flores.",
        "Lia encontrou palavras pequenas penduradas nas árvores.",
        "Lia escuta o som de cada letra junto com as ondas.",
        "Lia sobe a montanha juntando letras como CH, LH e NH.",
        "Lia quer escrever palavras bem grandes no céu estrelado.",
        "Lia e os bichos da fazenda brincam com as famílias das sílabas.",
        "Lia ouviu um som de ão no fundo do mar. Vamos procurar?",
        "Na selva, Lia canta os sons ai, ei, oi, au e eu.",
        "Lia escreve mar, sol e pés na neve.",
        "No circo, Lia mostra que carro e caro são palavras diferentes.",
        "Lia aprende quando usar Ç e quando usar J nas dunas do deserto.",
        "Lia lê as placas da cidade, com BL, CR e FL.",
        "No castelo, Lia ensina o X, o CH e o H que não faz barulho.",
        "Lia descobriu que antes de P e B a gente escreve M.",
        "Lia achou frases no mapa do tesouro, mas as palavras estão fora de ordem.",
        "Lia lê bilhetes e listas na fábrica de doces, com ponto e exclamação.",
        "No bosque, Lia transforma um em muitos e menino em menina.",
        "Lia brinca com palavras contrárias, grandonas e pequenininhas.",
        "Lia organiza as palavras das nuvens em ordem alfabética.",
        "Lia encontrou historinhas curtas escritas nos cristais.",
        "No parque, Lia bate palmas para separar as sílabas.",
        "Os robôs ditam palavras, e Lia ajuda a escrever direitinho.",
        "Lia lê convites, receitas e poemas com os bichos da savana.",
        "Lia conta histórias com começo, meio e fim.",
        "Lia vai ler bem bonito para abrir a porta do castelo.",
    )

    private val amigoIngles = listOf(
        "Bob, o ursinho, diz hello! Ele vai ensinar cores e animais em inglês.",
        "Bob mostra a casa, os brinquedos e a família em inglês.",
        "Bob levou a mochila da escola para a praia. School, sun, sea!",
        "Bob quer saber do que você gosta. I like!",
        "Bob flutua no espaço e aprende os contrários: big e small.",
        "Bob conheceu a vaquinha que faz moo. It is a cow!",
        "Bob mergulhou e viu um peixe azul. A blue fish!",
        "Na selva, Bob viu uma cobra comprida e um macaco pequeno. Long e small!",
        "Bob vestiu um casaco para o frio. I wear a coat!",
        "No circo, Bob conta de dez em dez em inglês.",
        "Bob brinca de esconder: in, on, under!",
        "Bob passeia pela cidade: school, park, market.",
        "Bob visita os quartos do castelo. There is a bed!",
        "Bob e o dinossauro pulam juntos. I can jump!",
        "Bob marca os dias no mapa do tesouro. Today is Monday!",
        "Bob experimenta os doces. I like cake!",
        "Bob desenha um rosto no bosque. I have two eyes!",
        "Bob conta como se sente hoje. I am happy!",
        "Bob olha para o céu das nuvens. It is sunny!",
        "Bob canta o alfabeto em inglês entre as pedras brilhantes.",
        "Bob ganhou brinquedos no parque: two balls, three cars!",
        "Bob apresenta o robô dele. This is my robot!",
        "Bob descreve os bichos da savana: a big gray elephant!",
        "Bob faz perguntas no vale: What color is it?",
        "Bob quer entender tudo o que ouvir para chegar às estrelas.",
    )

    private val amigoLogica = listOf(
        "Pipo, o robozinho, procura o que é igual e o que é diferente.",
        "Pipo encontrou filas de figuras na floresta. Qual vem depois?",
        "Pipo achou uma figura que não combina com as outras conchinhas.",
        "Pipo precisa atravessar o labirinto da montanha.",
        "Pipo quer completar um quadro de planetas, sem repetir nenhum.",
        "Pipo separa os bichos pela cor e pelo tamanho.",
        "Pipo guardou a sequência de luzes dos peixinhos. Você lembra?",
        "Pipo quer saber o que é direita e o que é esquerda na selva.",
        "Pipo montou um quebra cabeça de gelo, mas faltam peças.",
        "No circo, Pipo brinca de espelho.",
        "Pipo achou um quadro de figuras desenhado na areia.",
        "Pipo precisa de comandos para atravessar a cidade.",
        "Pipo virou detetive no castelo e tem duas pistas.",
        "Pipo coloca os dinossauros do menor para o maior.",
        "Pipo lê o mapa do tesouro pelas linhas e pelas colunas.",
        "Pipo descobriu que dois bolos pesam o mesmo que um abacaxi!",
        "Pipo quer lembrar sequências bem compridas no bosque.",
        "Pipo aprendeu um truque novo: repetir o comando duas vezes.",
        "Pipo encontrou um quadro de nuvens. O que está faltando?",
        "Pipo encaixa os cristais como peças de um quebra cabeça.",
        "No parque, Pipo quer descobrir quem é quem.",
        "Pipo ajuda os robôs a desviar das pedras do caminho.",
        "Pipo separa os animais da savana em dois grupos.",
        "Pipo quer resolver quadros coloridos de figuras.",
        "Pipo junta memória, comandos e padrões para a última aventura.",
    )

    /** Guardião de cada chefão e o que ele faz quando a criança vence. */
    private val chefoes = listOf(
        "Dona Joaninha" to "A Dona Joaninha, guardiã do Jardim, bateu as asinhas de alegria e deu um beijinho de flor.",
        "Seu Urso" to "O Seu Urso da Floresta deu um abraço bem apertado e um pote de mel.",
        "Caranguejo Cacá" to "O Caranguejo Cacá fez uma dança de lado e abriu o portão da Praia.",
        "Bode da Montanha" to "O Bode da Montanha tocou o sininho lá do pico: você chegou ao topo!",
        "Estrela Cadente" to "A Estrela Cadente passou bem pertinho e desenhou um coração no céu.",
        "Vaca Mimosa" to "A vaca gigante Mimosa deu um muuu de festa e chamou a fazenda toda para comemorar.",
        "Polvo Otávio" to "O polvo Otávio bateu palmas com os oito braços!",
        "Rei Macaco" to "O Rei Macaco pendurou uma medalha de folhas no seu pescoço.",
        "Família Urso Polar" to "A família de ursos polares fez um bolo de neve para comemorar.",
        "Palhaço Pipoca" to "O Palhaço Pipoca fez malabarismo e jogou confete colorido em você.",
        "Camelo Sábio" to "O Camelo Sábio sorriu e mostrou um oásis cheio de água fresquinha.",
        "Guarda Luzia" to "A guarda Luzia acendeu todos os sinais verdes da cidade para você passar.",
        "Rainha do Castelo" to "A Rainha do Castelo abriu a sala do trono e tocou a trombeta.",
        "Dino Gigante" to "O Dino Gigante deu um rugido baixinho, de alegria, e um abraço de dinossauro.",
        "Papagaio Pirata" to "O Papagaio Pirata gritou: tesouro encontrado! E o baú brilhou.",
        "Confeiteira Doçura" to "A Confeiteira Doçura fez um bolo de sete andares em sua homenagem.",
        "Fada do Bosque" to "A Fada do Bosque espalhou brilhinhos, e os cogumelos acenderam.",
        "Salamandra Sol" to "A Salamandra Sol soltou fumacinhas em forma de coração.",
        "Rei das Nuvens" to "O Rei das Nuvens fez chover pétalas de flor em cima de você.",
        "Toupeira Mineira" to "A Toupeira Mineira acendeu a lanterna, e todos os cristais brilharam juntos.",
        "Dona Roda Gigante" to "A roda gigante parou lá no alto, e o parque inteiro bateu palmas.",
        "Robô Gigante" to "O Robô Gigante fez bip bip de alegria e piscou todas as luzes.",
        "Leão Rei" to "O Leão Rei rugiu de felicidade, e a savana inteira comemorou.",
        "Camaleão Colorido" to "O Camaleão Colorido ficou de todas as cores ao mesmo tempo, só para você.",
        "Rainha das Estrelas" to "A Rainha das Estrelas acendeu o céu inteiro: você chegou ao fim da grande viagem!",
    )

    private fun fechamento(trilha: Trilha, mundo: Int): String = when {
        mundo == Mundos.todos.lastIndex -> "A Corujinha Professora disse: estou muito orgulhosa de você!"
        trilha == Trilha.MATEMATICA -> "Tito, o esquilo, contou: mais um mundo completo!"
        trilha == Trilha.PORTUGUES -> "Lia, a lagartinha, escreveu no céu: parabéns!"
        trilha == Trilha.INGLES -> "Bob, o ursinho, disse: great job! Muito bem!"
        else -> "Pipo, o robozinho, piscou as luzes: missão cumprida!"
    }

    private fun falasDoAmigo(trilha: Trilha): List<String> = when (trilha) {
        Trilha.MATEMATICA -> amigoNumeros
        Trilha.PORTUGUES -> amigoLetras
        Trilha.INGLES -> amigoIngles
        Trilha.LOGICA -> amigoLogica
    }

    /** O capítulo do mundo de índice [mundo] (0 a 24; fora disso, o mais perto). */
    fun capitulo(trilha: Trilha, mundo: Int): Capitulo {
        val m = mundo.coerceIn(0, Mundos.todos.lastIndex)
        val (guardiao, festa) = chefoes[m]
        return Capitulo(
            trilha = trilha,
            mundo = m,
            narradora = Personagem.CORUJINHA,
            amigo = amigoDe(trilha),
            introducao = cenas[m] + " " + falasDoAmigo(trilha)[m],
            guardiao = guardiao,
            cenaDoChefao = festa + " " + fechamento(trilha, m),
        )
    }

    /** O capítulo do mundo desta fase. */
    fun capitulo(fase: Fase): Capitulo = capitulo(fase.trilha, Mundos.de(fase).indice)

    /** Texto da abertura do mundo, só na primeira fase dele (null nas outras). */
    fun introducao(fase: Fase): String? = if (Mundos.comecaMundo(fase)) capitulo(fase).introducao else null

    /** Texto da vitória no chefão, só na 10ª fase do mundo (null nas outras). */
    fun cenaDoChefao(fase: Fase): String? = if (Mundos.ehChefao(fase)) capitulo(fase).cenaDoChefao else null

    /** Os 25 capítulos de um caminho, na ordem (para um livro de histórias na casa, por exemplo). */
    fun capitulos(trilha: Trilha): List<Capitulo> = Mundos.todos.indices.map { capitulo(trilha, it) }
}
