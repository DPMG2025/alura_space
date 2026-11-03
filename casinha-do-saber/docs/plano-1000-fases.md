# Plano das 1000 fases: 250 por caminho, 25 mundos

Documento de referência para os agentes que vão preencher as fases 51 a 250 de cada caminho e criar as telas dos desafios novos. Escrito em 3 de novembro de 2026, junto com a fundação do código (geradores por caminho, 25 mundos, data classes dos desafios novos e telas provisórias).

## 1. Regras gerais

* **Quatro caminhos, 250 fases cada**, em 25 mundos de 10 fases. Os mundos são os mesmos para todos os caminhos (o mundo 6 é a Fazenda em Números, Letras, Inglês e Lógica), cada caminho com o seu conteúdo.
* **Compatibilidade**: as fases 1 a 50 (mundos 1 a 5) ficam exatamente como estão, com o mesmo número e o mesmo nome. O progresso salvo é guardado por número de fase (`TRILHA:indice`), então quem já venceu fases continua de onde parou e a fase 51 abre depois da 50.
* **Público e ponto de chegada**: o mundo 1 é para quem tem 4 anos e ainda não lê; o mundo 25 corresponde ao fim do 2º ano do ensino fundamental (BNCC). A subida é gradual: cada mundo acrescenta uma ideia nova e revisa as anteriores.
* **Sempre 5 perguntas por fase**, com uma pergunta de revisão de fase anterior no meio (já é feito por `GeradorDeFases`). Vence com 4 de 5 de primeira.
* **Variedade dentro da fase**: nenhuma atividade de mexer aparece 3 vezes seguidas na mesma partida (regra conferida por teste para as 250 fases). Uma fase nova deve misturar pelo menos dois tipos de resposta (tocar e um desafio de mexer, ou dois desafios de mexer).
* **Variedade entre fases**: duas fases vizinhas não usam a mesma atividade principal; dentro de um mundo, pelo menos 4 tipos diferentes de atividade.
* **Sem pressão punitiva de tempo**: nada some porque o tempo acabou, nada tira estrela por demora. O que se mexe (peixe, balão, trem) anda devagar e volta.
* **Erro nunca pune**: a criança ouve o nome do que tocou e tenta de novo; depois de 2 erros a resposta certa brilha. Nos desafios novos vale o mesmo contrato (seção 3).
* **Fase 10 de cada mundo é o chefão do mundo** (seção 5).

### Esqueleto de cada mundo

Para a dificuldade subir dentro do mundo e a criança sentir progresso:

| Fase do mundo | Papel |
|---|---|
| 1 | Apresenta a ideia nova com tocar e opções bem diferentes entre si |
| 2 | A mesma ideia com um desafio de mexer |
| 3 | Opções mais parecidas (distratores próximos) |
| 4 | Outro desafio de mexer com a mesma ideia |
| 5 | Marco do mundo (bandeirinha): mistura das fases 1 a 4 |
| 6 | Segunda ideia do mundo |
| 7 | Segunda ideia com desafio de mexer |
| 8 | Ideia do mundo junto com revisão do mundo anterior |
| 9 | Versão mais difícil: menos apoio visual, mais itens, opções parecidas |
| 10 | Chefão do mundo |

## 2. Os 25 mundos

| Mundo | Nome | Emoji | Cor (ARGB) | Fases |
|---|---|---|---|---|
| 1 | Jardim | 🌷 | 0xFFB9F6CA | 1 a 10 |
| 2 | Floresta | 🌳 | 0xFFC8E6C9 | 11 a 20 |
| 3 | Praia | 🏖️ | 0xFFFFF3C4 | 21 a 30 |
| 4 | Montanha | ⛰️ | 0xFFE1D5F5 | 31 a 40 |
| 5 | Espaço | 🚀 | 0xFFCFD8FF | 41 a 50 |
| 6 | Fazenda | 🐄 | 0xFFFFE0B2 | 51 a 60 |
| 7 | Fundo do Mar | 🐠 | 0xFFB3E5FC | 61 a 70 |
| 8 | Selva | 🐒 | 0xFFDCEDC8 | 71 a 80 |
| 9 | Polo Norte | 🐧 | 0xFFE3F2FD | 81 a 90 |
| 10 | Circo | 🎪 | 0xFFFFCDD2 | 91 a 100 |
| 11 | Deserto | 🐫 | 0xFFFFECB3 | 101 a 110 |
| 12 | Cidade | 🏙️ | 0xFFCFD8DC | 111 a 120 |
| 13 | Castelo | 🏰 | 0xFFD1C4E9 | 121 a 130 |
| 14 | Vale dos Dinossauros | 🦕 | 0xFFC5E1A5 | 131 a 140 |
| 15 | Ilha do Tesouro | 🏝️ | 0xFFB2EBF2 | 141 a 150 |
| 16 | Fábrica de Doces | 🍭 | 0xFFF8BBD0 | 151 a 160 |
| 17 | Bosque Encantado | 🍄 | 0xFFE1BEE7 | 161 a 170 |
| 18 | Vulcão | 🌋 | 0xFFFFCCBC | 171 a 180 |
| 19 | Reino das Nuvens | ☁️ | 0xFFE1F5FE | 181 a 190 |
| 20 | Caverna de Cristal | 💎 | 0xFFB2DFDB | 191 a 200 |
| 21 | Parque de Diversões | 🎡 | 0xFFFFF9C4 | 201 a 210 |
| 22 | Cidade dos Robôs | 🤖 | 0xFFD7CCC8 | 211 a 220 |
| 23 | Savana | 🦁 | 0xFFFFE57F | 221 a 230 |
| 24 | Vale das Cores | 🌈 | 0xFFF3E5F5 | 231 a 240 |
| 25 | Castelo das Estrelas | 🌟 | 0xFFC5CAE9 | 241 a 250 |

Os mundos estão em `dominio/Mundos.kt`. O tema do mundo deve aparecer no conteúdo sempre que der (bichos da fazenda na Fazenda, peixes no Fundo do Mar), e a caça na cena usa o fundo do mundo.

## 3. Catálogo dos desafios novos

Todos estão em `dominio/AtividadesNovas.kt` como subtipos de `Atividade` (as regras de cada um são conferidas no `init` e em `AtividadesNovasTest`). A pergunta é criada com `Pergunta.deAtividade(instrucao, fala, atividade, reforco)`. Cada um tem a sua tela em `ui/telas/atividades/novas/`, hoje com uma tela provisória (um botão que conclui sem contar acerto); o `when` exaustivo que chama cada tela fica em `AtividadeInterativa` (`ui/telas/atividades/Comum.kt`), que a `TelaJogo` já usa para toda atividade de mexer.

**Contrato comum das telas** (o mesmo de Cesta, Memória, Montar palavra e Traçar): parâmetros `(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)`; cada movimento errado chama `onErro()` uma vez, com som de erro e o narrador dizendo o nome do que foi tocado; depois de 2 erros a resposta certa brilha e a mãozinha mostra o gesto; `onConcluir(semErro)` é chamado uma vez só no fim; só aceita toques quando `ativa`; com `ajudaInicial` (exemplo da rodada facilitada) a ajuda aparece desde o começo. A mãozinha (`MaoGuia`, `Comum.kt`) ensina o gesto na primeira vez.

### 3.1 Pescaria

* **Como joga**: peixinhos nadam devagar num lago, cada um levando uma figura, número ou letra. A criança pesca os peixes pedidos; o peixe pula para o balde.
* **Gesto**: tocar no peixe.
* **Acerto e erro**: acerta quando todos os certos estão no balde. Peixe errado foge nadando, diz o próprio nome e volta depois (conta um erro).
* **Conteúdo**: números (pesque os peixes com 7; pesque os pares), letras (pesque as vogais; pesque o que começa com B), inglês (fish the red ones), lógica (pesque só os que voam).
* **Data class**: `Pescaria(peixes: List<Opcao>, certos: Set<Int>)`. Arquivo: `AtividadePescaria.kt`.

### 3.2 Estourar balões

* **Como joga**: balões sobem devagar com respostas escritas ou desenhadas; tocar estoura com um pof. Pode pedir uma ordem (1, 2, 3 ou A, B, C).
* **Gesto**: tocar.
* **Acerto e erro**: acerta ao estourar todos os certos (na ordem, quando pedido). Balão errado só balança (um erro). Balão que chega no alto volta por baixo.
* **Conteúdo**: resultado de contas, números em ordem, letras do alfabeto em ordem, palavras em inglês ouvidas, figuras de um grupo.
* **Data class**: `EstourarBaloes(baloes: List<Opcao>, certos: List<Int>, emOrdem: Boolean = false)`. Arquivo: `AtividadeBaloes.kt`.

### 3.3 Ligar pares

* **Como joga**: duas colunas; a criança liga cada item da esquerda ao seu par da direita e uma linha colorida fica desenhada.
* **Gesto**: arrastar o dedo de um item até o outro.
* **Acerto e erro**: acerta quando todos os pares estão ligados. Par errado: a linha some (um erro).
* **Conteúdo**: número e quantidade, conta e resultado, maiúscula e minúscula, palavra e figura, palavra em inglês e figura, figura e sombra.
* **Data class**: `LigarPares(esquerda: List<Opcao>, direita: List<Opcao>, pares: Map<Int, Int>)` (a direita pode ter uma sobra). Arquivo: `AtividadeLigarPares.kt`.

### 3.4 Quebra cabeça de arrastar

* **Como joga**: uma figura cortada em peças embaralhadas; a criança monta na moldura. As peças podem levar rótulos (1 a 9, de 10 em 10, alfabeto, sílabas) e então a ordem dos rótulos ajuda.
* **Gesto**: arrastar a peça até a moldura.
* **Acerto e erro**: peça no lugar certo encaixa com clique; no errado volta tremendo (um erro). Termina com a figura inteira se mexendo.
* **Conteúdo**: lógica espacial (2 a 16 peças), sequências numéricas, alfabeto, sílabas de uma palavra.
* **Data class**: `QuebraCabeca(figura: String, linhas: Int, colunas: Int, rotulos: List<String> = emptyList(), fixas: Set<Int> = emptySet())`. Arquivo: `AtividadeQuebraCabeca.kt`.

### 3.5 Trem para ordenar

* **Como joga**: a locomotiva puxa engates vazios e os vagões soltos ficam embaixo; cada vagão vai para o engate certo. Com tudo engatado, o trem apita e sai andando.
* **Gesto**: arrastar o vagão.
* **Acerto e erro**: vagão no engate certo engata; no errado volta (um erro). Alguns vagões podem vir fixos (pistas) e outros sobrando (extras).
* **Conteúdo**: números em ordem crescente e decrescente, de 2 em 2, de 5 em 5, de 10 em 10, centenas, alfabeto, dias da semana e meses, palavras em ordem alfabética, tamanhos do menor para o maior.
* **Data class**: `Trem(vagoes: List<Opcao>, fixos: Set<Int> = emptySet(), extras: List<Opcao> = emptyList())`. Arquivo: `AtividadeTrem.kt`.

### 3.6 Balança

* **Como joga**: balança de dois pratos; um lado já tem peso; a criança põe pesos no outro até a balança ficar reta.
* **Gesto**: arrastar pesos para o prato (e de volta para tirar).
* **Acerto e erro**: acerta quando equilibra. Se passar do peso, a balança pende para o outro lado (um erro) e a criança tira um peso.
* **Conteúdo**: composição de números (7 = 3 + 4), fatos de 10, igualdade, dobro, lógica de pesado e leve com figuras.
* **Data class**: `Balanca(ladoEsquerdo: List<Int>, pesos: List<Int>, figura: String = "🧱", mostrarNumeros: Boolean = true)`. Arquivo: `AtividadeBalanca.kt`.

### 3.7 Caça ao tesouro na cena

* **Como joga**: uma cena grande do mundo com figuras espalhadas, algumas meio escondidas atrás de moitas e pedras; a criança acha as pedidas e elas vão para a barra de achados.
* **Gesto**: tocar.
* **Acerto e erro**: acerta ao achar todas. Figura errada diz o próprio nome (um erro).
* **Conteúdo**: contar (ache as 5 estrelas), letras (ache o que começa com M), inglês (find the cat), lógica (ache as 3 diferenças, ache o que não combina).
* **Data class**: `CacaNaCena(mundo: Int, itens: List<ItemNaCena>, certos: Set<Int>)`, com `ItemNaCena(opcao: Opcao, x: Float, y: Float, escala: Float = 1f)`. Arquivo: `AtividadeCacaNaCena.kt`.

### 3.8 Siga a sequência (Simon de sons e cores)

* **Como joga**: de 2 a 4 botões grandes com cor, figura e som. O jogo acende e toca uma sequência devagar; a criança repete no tempo dela.
* **Gesto**: tocar.
* **Acerto e erro**: acerta ao repetir a sequência inteira. Toque fora da ordem: o jogo mostra a sequência de novo (um erro), sem voltar ao começo do jogo.
* **Conteúdo**: memória (2 a 7 toques), sons das letras, palavras em inglês, números falados.
* **Data class**: `SigaASequencia(botoes: List<Opcao>, cores: List<Long>, sequencia: List<Int>)`. Arquivo: `AtividadeSigaASequencia.kt`.

### 3.9 Pintar por código

* **Como joga**: desenho quadriculado com rótulos nas casinhas e uma legenda de potes de tinta; o desenho aparece conforme a criança pinta.
* **Gesto**: tocar no pote e depois nas casinhas.
* **Acerto e erro**: casinha certa fica pintada; cor errada escorre e a casinha volta a ficar branca (um erro). Termina com o desenho brilhando.
* **Conteúdo**: números (código 3), contas (código 2 + 1, cor do resultado), letras (vogais de uma cor, consoantes de outra), inglês (código red), lógica (formas).
* **Data class**: `PintarPorCodigo(colunas: Int, casas: List<CasaDePintar>, legenda: List<CorDaLegenda>)`, com `CasaDePintar(rotulo: String, codigo: String?)` e `CorDaLegenda(codigo: String, argb: Long, fala: List<Trecho> = emptyList())`. Arquivo: `AtividadePintar.kt`.

### 3.10 Ditado de ouvir e montar

* **Como joga**: o narrador fala uma palavra (botão de ouvir de novo sempre à mão); a criança monta com letras ou sílabas, sem a figura como pista quando ela não vem.
* **Gesto**: arrastar peças para as casinhas.
* **Acerto e erro**: peça certa encaixa; errada volta (um erro). Termina com a palavra lida inteira.
* **Conteúdo**: ortografia (R e RR, S e SS, ão e am, encontros), palavras em inglês soletradas (cat, dog), sílabas difíceis.
* **Data class**: `Ditado(partes: List<String>, extras: List<String>, idioma: Idioma = Idioma.PT, figura: String? = null)`. Arquivo: `AtividadeDitado.kt`.

### 3.11 Dominó

* **Como joga**: uma fileira com uma ponta livre; da mão, a criança escolhe a peça que combina com a ponta; a outra metade vira a nova ponta.
* **Gesto**: arrastar a peça até a ponta.
* **Acerto e erro**: peça que combina encaixa (girando se preciso); a que não combina volta (um erro). Em cada jogada só uma peça serve (conferido no `init`).
* **Conteúdo**: quantidade e número, conta e resultado, maiúscula e minúscula, figura e letra inicial, palavra em inglês e figura, figura e sombra.
* **Data class**: `Domino(ponta: MeiaPeca, mao: List<PecaDomino>, jogadas: Int)`, com `MeiaPeca(chave: String, visual: Visual, fala: List<Trecho> = emptyList())` e `PecaDomino(a: MeiaPeca, b: MeiaPeca)`. Arquivo: `AtividadeDomino.kt`.

### 3.12 Esconde esconde

* **Como joga**: um bicho acena e se esconde atrás de um de vários esconderijos (moitas, caixas, baldes). Ou os esconderijos trocam de lugar devagar e a criança segue com o olho, ou a pista vem falada (atrás do número 7, behind the red box, atrás da caixa que começa com B).
* **Gesto**: tocar no esconderijo.
* **Acerto e erro**: o bicho pula de alegria; no esconderijo errado sai outra coisa engraçada, como uma borboleta (um erro).
* **Conteúdo**: atenção e memória visual (trocas), números, letras, cores e preposições em inglês, posição (à esquerda do, entre).
* **Data class**: `EscondeEsconde(bicho: String, esconderijos: List<Opcao>, certo: Int, trocas: Int = 0)`. Arquivo: `AtividadeEscondeEsconde.kt`.

### 3.13 Cartas viradas em ordem

* **Como joga**: cartas viradas para baixo; a criança vira uma por vez procurando a próxima da ordem (1, 2, 3; A, B, C; one, two, three). A carta achada vai para a fila de cima.
* **Gesto**: tocar para virar.
* **Acerto e erro**: carta certa fica aberta; outra carta fica aberta um instante (para lembrar onde está) e vira de volta (um erro). A corrida é só contra a própria memória, sem relógio.
* **Conteúdo**: sequências numéricas, alfabeto, números em inglês, etapas de uma história.
* **Data class**: `CartasViradas(cartas: List<Opcao>, ordem: List<Int>)`. Arquivo: `AtividadeCartasViradas.kt`.

### 3.14 Mercadinho

* **Como joga**: um produto com etiqueta de preço; a criança paga com moedas e cédulas da carteira. Nos mundos mais altos, o cliente paga e a criança devolve o troco.
* **Gesto**: arrastar moedas e cédulas para o balcão.
* **Acerto e erro**: acerta quando o valor é exato. Se passar, a última moeda volta para a carteira (um erro).
* **Conteúdo**: reconhecer moedas e cédulas, somar valores, equivalências (duas de 50 centavos valem 1 real), troco.
* **Data class**: `Mercadinho(produto: String, nome: String, preco: Int, dinheiro: List<Int>, troco: Boolean = false, pago: Int = 0)` (valores em centavos, só moedas e cédulas reais). Arquivo: `AtividadeMercadinho.kt`.

### 3.15 Acertar o relógio

* **Como joga**: relógio de ponteiros grande; a criança arrasta os ponteiros até a hora pedida (falada e, nos mundos altos, também escrita em relógio digital).
* **Gesto**: arrastar o ponteiro (o dos minutos pula de 5 em 5).
* **Acerto e erro**: na hora certa toca o cuco; na errada o narrador diz a hora marcada (um erro).
* **Conteúdo**: horas cheias, meias horas, quartos de hora, leitura de 5 em 5 minutos.
* **Data class**: `Relogio(hora: Int, minuto: Int, digital: Boolean = false)`. Arquivo: `AtividadeRelogio.kt`.

### 3.16 Programar o robô

* **Como joga**: grade com o robô, a bateria e pedras; a criança monta uma fila de setas e toca no ▶; o robô anda seguindo a fila. Nos mundos altos aparece o bloco 2 vezes (laço).
* **Gesto**: arrastar setas para a fila e tocar no ▶.
* **Acerto e erro**: o robô chega na bateria. Bateu ou parou fora: volta ao começo e a seta errada pisca (um erro).
* **Conteúdo**: pensamento computacional, lateralidade, contagem de passos.
* **Data class**: `ProgramarRobo(linhas: Int, colunas: Int, inicio: Int, chegada: Int, obstaculos: Set<Int> = emptySet(), maxComandos: Int = 6, repetir: Boolean = false)`. Arquivo: `AtividadeProgramarRobo.kt`.

### 3.17 Montar a frase

* **Como joga**: cartões com palavras embaralhadas (e alguns extras); a criança monta a frase na linha. Quando completa, o narrador lê a frase e a figura se mexe.
* **Gesto**: arrastar cartões.
* **Acerto e erro**: cartão no lugar certo encaixa; no errado volta (um erro).
* **Conteúdo**: ordem das palavras, letra maiúscula e ponto final, frases em inglês (I like apples).
* **Data class**: `MontarFrase(palavras: List<String>, extras: List<String> = emptyList(), idioma: Idioma = Idioma.PT, figura: String? = null)`. Arquivo: `AtividadeMontarFrase.kt`.

### Atividades que já existem

Tocar (`Atividade.Escolha`), Cesta, Memória, Montar palavra e Traçar continuam valendo em todos os mundos, junto com as cenas que já existem (conta com figuras, sequência, quadro e sudoku, material dourado, labirinto, setas).

## 4. Progressão por caminho

Cada tabela lista, por mundo, o objetivo pedagógico da faixa de 10 fases, a régua de dificuldade e as atividades usadas. A ordem das atividades segue o esqueleto da seção 1 e a regra de variedade. Os mundos 1 a 5 já estão prontos e servem de referência.

### 4.1 Números (Matemática)

Régua de dificuldade: tamanho dos números (até 10, 20, 100, 1000), apoio visual (figuras, depois material dourado, depois só o número), distância dos distratores (longe, depois vizinhos de 1), quantidade de itens para arrastar (até 5, depois até 12) e passos do problema (um, depois dois).

| Mundo | Fases | Objetivo pedagógico | Dificuldade | Atividades |
|---|---|---|---|---|
| 1 Jardim | 1 a 10 | Contar até 10, cesta, maior e menor, formas | Até 10, com figuras | Tocar, Cesta, Memória |
| 2 Floresta | 11 a 20 | Somar e tirar até 10, antes e depois, números até 20 | Até 20, com figuras | Tocar, Cesta, Memória |
| 3 Praia | 21 a 30 | Contar até 20, traçar algarismos, somar e tirar até 20, de 10 em 10 | Até 20 e dezenas | Tocar, Traçar, Cesta |
| 4 Montanha | 31 a 40 | Dezenas e unidades, dobro e metade, medidas, horas, moedinhas | Até 50 | Tocar, Cesta, material dourado |
| 5 Espaço | 41 a 50 | Até 100, gráfico, problemas, posições, sequências, comparar | Até 100 | Tocar, Cesta, Memória |
| 6 Fazenda | 51 a 60 | Números ordinais (1º ao 10º), comparar com os sinais maior, menor e igual | Até 30, distratores vizinhos | Tocar, Trem, Pescaria, Balança; chefão: Trem + Balança + Tocar |
| 7 Fundo do Mar | 61 a 70 | Fatos de 10 (pares que somam 10), completar a dezena | Somas até 20 | Balança, Pescaria, Dominó, Tocar; chefão: Balança + Pescaria + Ligar pares |
| 8 Selva | 71 a 80 | Subtração até 20 e família de fatos (8 + 5, 13 menos 5) | Até 20 sem figuras na metade das perguntas | Balões, Ligar pares, Cesta, Tocar; chefão: Balões + Dominó + Tocar |
| 9 Polo Norte | 81 a 90 | Números até 100: ler, escrever, sucessor, antecessor, reta numérica | Até 100 | Trem, Cartas viradas, Tocar, Traçar; chefão: Trem + Cartas viradas + Tocar |
| 10 Circo | 91 a 100 | Valor posicional: decompor 45 em 40 + 5, material dourado até 99 | Até 99, sem figuras | Tocar (material dourado), Balança, Pintar por código, Ligar pares; chefão: Pintar + Balança + Tocar |
| 11 Deserto | 101 a 110 | Somar e tirar dezenas inteiras e dezena mais unidade sem reagrupar (23 + 4, 30 + 20) | Até 99 | Balões, Dominó, Trem, Tocar; chefão: Dominó + Balões + Pescaria |
| 12 Cidade | 111 a 120 | Dinheiro: moedas e cédulas até 20 reais, pagar o valor exato, equivalências | Até 20 reais | Mercadinho, Ligar pares, Tocar, Cesta; chefão: Mercadinho + Ligar pares + Tocar |
| 13 Castelo | 121 a 130 | Medidas: comprimento em palmos e centímetros, massa na balança, capacidade | Comparar e medir até 30 cm | Balança, Trem (do menor ao maior), Tocar, Caça na cena; chefão: Balança + Trem + Caça |
| 14 Vale dos Dinossauros | 131 a 140 | Tempo: horas cheias e meias, dias da semana, meses, calendário | Hora e meia hora | Relógio, Trem (dias e meses), Cartas viradas, Tocar; chefão: Relógio + Trem + Tocar |
| 15 Ilha do Tesouro | 141 a 150 | Geometria: figuras planas e espaciais (cubo, esfera, cilindro, cone), lados e cantos | Formas parecidas | Caça na cena, Ligar pares, Quebra cabeça, Tocar; chefão: Caça + Quebra cabeça + Ligar pares |
| 16 Fábrica de Doces | 151 a 160 | Adição com dois algarismos sem reagrupar e problemas de juntar | Até 99 | Balões, Pintar por código, Mercadinho, Tocar; chefão: Pintar + Balões + Tocar (problema) |
| 17 Bosque Encantado | 161 a 170 | Subtração com dois algarismos sem reagrupar, problemas de tirar e comparar | Até 99 | Pescaria, Dominó, Balança, Tocar; chefão: Dominó + Pescaria + Tocar (problema) |
| 18 Vulcão | 171 a 180 | Centenas: contar de 100 em 100, números até 1000, compor e decompor | Até 1000 | Trem, Cartas viradas, Ligar pares, Tocar; chefão: Trem + Ligar pares + Tocar |
| 19 Reino das Nuvens | 181 a 190 | Adição com reagrupamento (vai um) até 99 e cálculo mental | Até 99, dois passos | Balões, Balança, Pintar por código, Tocar; chefão: Balança + Pintar + Balões |
| 20 Caverna de Cristal | 191 a 200 | Subtração com reagrupamento, quantos a mais e quantos faltam | Até 99, dois passos | Dominó, Pescaria, Mercadinho (troco), Tocar; chefão: Mercadinho + Dominó + Tocar |
| 21 Parque de Diversões | 201 a 210 | Ideia de multiplicação: grupos iguais, dobro e triplo, contar de 2, 5 e 10 | Até 50 | Cesta (grupos), Trem (saltos), Balões, Tocar; chefão: Cesta + Trem + Balões |
| 22 Cidade dos Robôs | 211 a 220 | Ideia de divisão: repartir igual, metade, terça parte, quarta parte | Até 40 | Cesta (repartir), Balança, Ligar pares, Tocar; chefão: Cesta + Balança + Ligar pares |
| 23 Savana | 221 a 230 | Tabelas e gráficos de colunas; chance: impossível, pouco provável, muito provável, certo | Tabelas de 3 a 5 linhas | Tocar (gráfico), Caça na cena, Pintar por código, Cartas viradas; chefão: Pintar + Caça + Tocar |
| 24 Vale das Cores | 231 a 240 | Padrões e regularidades (regra mais 3, menos 2), pares e ímpares, ordem até 1000 | Até 1000 | Trem, Pescaria (pares e ímpares), Balões, Tocar; chefão: Trem + Pescaria + Balões |
| 25 Castelo das Estrelas | 241 a 250 | Revisão do 2º ano: problemas de dois passos com dinheiro, tempo e medidas | Tudo junto | Mercadinho, Relógio, Balança, Dominó, Tocar; fase 250: grande chefão final com uma pergunta de cada mundo |

### 4.2 Letras (Português)

Régua de dificuldade: unidade (letra, sílaba, palavra, frase, texto curto), tamanho das palavras (2 a 4 sílabas), dificuldade ortográfica (sílabas simples, depois nasais, dígrafos e encontros), apoio (figura e áudio, depois só áudio, depois só texto) e semelhança dos distratores (letras parecidas como b e d, sílabas da mesma família).

| Mundo | Fases | Objetivo pedagógico | Dificuldade | Atividades |
|---|---|---|---|---|
| 1 Jardim | 1 a 10 | Vogais, traçar, primeira letra, maiúscula e minúscula | Letras | Tocar, Traçar, Memória |
| 2 Floresta | 11 a 20 | Ler palavras simples, sílabas, montar e completar palavras, rimas | Palavras de 2 sílabas | Tocar, Montar palavra |
| 3 Praia | 21 a 30 | Som das letras, famílias silábicas, sílaba que falta | Sílabas | Tocar, Montar palavra |
| 4 Montanha | 31 a 40 | Letras pequenas, CH LH NH, BR PR TR, frases, ordem alfabética | Palavras de 3 sílabas | Tocar, Traçar, Memória, Montar |
| 5 Espaço | 41 a 50 | Palavras de 4 sílabas, quantas letras, como se escreve, última letra, frases | Palavras grandes | Tocar, Montar, Traçar |
| 6 Fazenda | 51 a 60 | Todas as famílias silábicas simples, leitura de palavras de 2 e 3 sílabas | Sílabas CV com todas as consoantes | Pescaria (sílabas), Ditado (sílabas), Montar palavra, Tocar; chefão: Ditado + Pescaria + Tocar |
| 7 Fundo do Mar | 61 a 70 | Nasais: ão, ãe, an, en, in, on, un e o til | Palavras de 2 e 3 sílabas | Balões, Ligar pares, Montar palavra, Tocar; chefão: Balões + Ligar pares + Ditado |
| 8 Selva | 71 a 80 | Ditongos ai, ei, oi, au, eu, ou; palavras de 3 sílabas | Palavras de 3 sílabas | Dominó (figura e palavra), Caça na cena, Ditado, Tocar; chefão: Dominó + Caça + Ditado |
| 9 Polo Norte | 81 a 90 | Sílabas com R, S e L no fim (mar, pés, sol) | Palavras de 2 a 4 sílabas | Ditado (letras), Pescaria, Quebra cabeça (sílabas), Tocar; chefão: Ditado + Quebra cabeça + Tocar |
| 10 Circo | 91 a 100 | Dígrafos RR, SS, QU, GU | Pares parecidos (caro e carro) | Ligar pares, Balões, Ditado, Tocar; chefão: Ligar pares + Ditado + Balões |
| 11 Deserto | 101 a 110 | C e Ç, G e J (ge, gi), S com som de Z entre vogais | Distratores ortográficos | Pescaria, Dominó, Ditado, Tocar; chefão: Pescaria + Dominó + Ditado |
| 12 Cidade | 111 a 120 | Encontros consonantais BL, CL, FL, GL, PL, CR, DR, FR, GR, VR | Palavras de 2 a 4 sílabas | Montar palavra, Caça na cena, Ditado, Tocar; chefão: Caça + Ditado + Montar |
| 13 Castelo | 121 a 130 | X e CH, H no começo da palavra, Z e S | Distratores ortográficos | Balões, Ligar pares, Ditado, Tocar; chefão: Ligar pares + Balões + Ditado |
| 14 Vale dos Dinossauros | 131 a 140 | M antes de P e B, M e N no fim da sílaba | Palavras de 3 e 4 sílabas | Dominó, Pescaria, Ditado, Tocar; chefão: Dominó + Pescaria + Ditado |
| 15 Ilha do Tesouro | 141 a 150 | Frases: ordem das palavras, maiúscula no começo e ponto final | Frases de 3 a 5 palavras | Montar a frase, Caça na cena, Tocar, Quebra cabeça; chefão: Montar a frase + Caça + Tocar |
| 16 Fábrica de Doces | 151 a 160 | Pontuação (ponto, interrogação, exclamação) e leitura de bilhete e lista | Frases de 4 a 6 palavras | Montar a frase, Ligar pares, Balões, Tocar; chefão: Montar a frase + Ligar pares + Tocar (bilhete) |
| 17 Bosque Encantado | 161 a 170 | Singular e plural, masculino e feminino | Palavras e frases curtas | Dominó, Pescaria, Montar a frase, Tocar; chefão: Dominó + Pescaria + Montar a frase |
| 18 Vulcão | 171 a 180 | Sinônimos, antônimos, aumentativo e diminutivo | Palavras de uso comum | Ligar pares, Balões, Cartas viradas, Tocar; chefão: Ligar pares + Balões + Tocar |
| 19 Reino das Nuvens | 181 a 190 | Ordem alfabética pela segunda letra, achar palavras num dicionário ilustrado | Listas de 4 a 6 palavras | Trem, Cartas viradas, Caça na cena, Tocar; chefão: Trem + Cartas viradas + Tocar |
| 20 Caverna de Cristal | 191 a 200 | Entender texto curto: quem, onde, o que aconteceu | Textos de 3 a 4 frases | Tocar (texto), Montar a frase, Caça na cena, Ligar pares; chefão: texto + Montar a frase + Caça |
| 21 Parque de Diversões | 201 a 210 | Separar sílabas, sílaba mais forte, acentos (á, é, ó, ê, ô) | Palavras de 3 e 4 sílabas | Quebra cabeça (sílabas), Pescaria, Ditado, Tocar; chefão: Ditado + Quebra cabeça + Pescaria |
| 22 Cidade dos Robôs | 211 a 220 | Ditado com dificuldades: R e RR, S e SS, ão e am, L e U no fim | Só áudio, sem figura | Ditado, Balões, Dominó, Tocar; chefão: Ditado + Dominó + Balões |
| 23 Savana | 221 a 230 | Textos curtos: receita, convite, poema com rimas, parlendas | Textos de 4 a 6 linhas | Montar a frase, Ligar pares, Siga a sequência (versos), Tocar; chefão: Montar a frase + Ligar pares + Tocar (texto) |
| 24 Vale das Cores | 231 a 240 | Começo, meio e fim; ligar frases com e, mas, porque | Histórias de 3 quadros | Cartas viradas (ordem da história), Montar a frase, Trem, Tocar; chefão: Cartas viradas + Montar a frase + Trem |
| 25 Castelo das Estrelas | 241 a 250 | Revisão do 2º ano: leitura fluente de frases e textos curtos, escrita de palavras | Tudo junto | Ditado, Montar a frase, Caça na cena, Dominó, Tocar; fase 250: grande chefão final |

### 4.3 Inglês

A BNCC só traz inglês a partir do 6º ano; aqui a referência é o nível Pre A1 (vocabulário e frases curtas ouvidas). Régua de dificuldade: tamanho do vocabulário do mundo (8, depois 12 e 16 palavras), apoio (figura com tradução falada, depois só a palavra em inglês), forma (palavra solta, depois expressão de duas palavras, depois frase curta, depois pergunta e resposta) e semelhança dos distratores (mesma categoria, sons parecidos).

| Mundo | Fases | Objetivo pedagógico | Dificuldade | Atividades |
|---|---|---|---|---|
| 1 Jardim | 1 a 10 | Cores, animais, números 1 a 10, frutas, corpo, formas, roupas | Palavras soltas | Tocar, Memória |
| 2 Floresta | 11 a 20 | Casa, comida, brinquedos, tempo, família, veículos, ações | Palavras soltas | Tocar, Memória |
| 3 Praia | 21 a 30 | School, nature, feelings, números até 15 | Palavras soltas | Tocar, Memória |
| 4 Montanha | 31 a 40 | Jobs, verbs, I like, What color, How many, wild animals | Frases curtas | Tocar, Memória |
| 5 Espaço | 41 a 50 | Opposites, números até 20, I am, I can | Frases curtas | Tocar, Memória |
| 6 Fazenda | 51 a 60 | Farm animals e os sons deles, It is a cow | 8 palavras | Pescaria, Siga a sequência (sons), Ligar pares, Tocar; chefão: Siga a sequência + Pescaria + Tocar |
| 7 Fundo do Mar | 61 a 70 | Sea animals com cor: a blue fish | Duas palavras | Pescaria, Pintar por código (cores), Dominó, Tocar; chefão: Pintar + Pescaria + Dominó |
| 8 Selva | 71 a 80 | Adjetivos big, small, long, short, tall com animais | Duas palavras | Esconde esconde, Ligar pares, Balões, Tocar; chefão: Esconde esconde + Balões + Tocar |
| 9 Polo Norte | 81 a 90 | Roupas de frio e calor: I wear a coat | Frase curta | Caça na cena, Montar a frase, Memória, Tocar; chefão: Caça + Montar a frase + Tocar |
| 10 Circo | 91 a 100 | Números 20 a 50 de 10 em 10, How old are you? | Números ouvidos | Balões, Trem, Cartas viradas, Tocar; chefão: Trem + Balões + Cartas viradas |
| 11 Deserto | 101 a 110 | Preposições in, on, under, next to | Frase com lugar | Esconde esconde (pista falada), Caça na cena, Tocar, Montar a frase; chefão: Esconde esconde + Caça + Tocar |
| 12 Cidade | 111 a 120 | Lugares da cidade (school, park, hospital, market) e transportes | 12 palavras | Ligar pares, Programar o robô (go to the park), Dominó, Tocar; chefão: Robô + Ligar pares + Tocar |
| 13 Castelo | 121 a 130 | Cômodos e móveis: There is a bed in the bedroom | Frase com lugar | Caça na cena, Montar a frase, Pescaria, Tocar; chefão: Caça + Montar a frase + Pescaria |
| 14 Vale dos Dinossauros | 131 a 140 | Can e can't com ações: I can jump | Frase curta | Siga a sequência (ações), Ligar pares, Balões, Tocar; chefão: Siga a sequência + Balões + Tocar |
| 15 Ilha do Tesouro | 141 a 150 | Dias da semana: Today is Monday | Sequência de 7 | Trem, Cartas viradas, Ditado (dias), Tocar; chefão: Trem + Cartas viradas + Tocar |
| 16 Fábrica de Doces | 151 a 160 | Comida, I like e I don't like | Frase com negação | Mercadinho (contar em inglês), Pescaria, Montar a frase, Tocar; chefão: Montar a frase + Pescaria + Tocar |
| 17 Bosque Encantado | 161 a 170 | Rosto e corpo: I have two eyes | 16 palavras | Pintar por código, Quebra cabeça (rosto), Ligar pares, Tocar; chefão: Pintar + Quebra cabeça + Tocar |
| 18 Vulcão | 171 a 180 | Sentimentos: How are you? I am happy | Pergunta e resposta | Ligar pares, Dominó, Balões, Tocar; chefão: Dominó + Ligar pares + Tocar |
| 19 Reino das Nuvens | 181 a 190 | Tempo e estações: It is sunny, It is winter | Frase curta | Caça na cena, Siga a sequência, Montar a frase, Tocar; chefão: Caça + Montar a frase + Tocar |
| 20 Caverna de Cristal | 191 a 200 | Alfabeto em inglês e soletrar palavras curtas (cat, dog, sun) | Letras ouvidas | Ditado (inglês), Balões (letras), Cartas viradas, Tocar; chefão: Ditado + Balões + Cartas viradas |
| 21 Parque de Diversões | 201 a 210 | Brinquedos e plural: two balls, three cars | Número com plural | Cesta, Pescaria, Ligar pares, Tocar; chefão: Cesta + Pescaria + Tocar |
| 22 Cidade dos Robôs | 211 a 220 | My, your, he, she: This is my robot | Frase curta | Montar a frase, Programar o robô (comandos em inglês), Dominó, Tocar; chefão: Robô + Montar a frase + Tocar |
| 23 Savana | 221 a 230 | Descrever animais: a big gray elephant | Três palavras | Esconde esconde, Caça na cena, Pintar por código, Tocar; chefão: Caça + Pintar + Esconde esconde |
| 24 Vale das Cores | 231 a 240 | Perguntas What is it? Where is it? What color is it? | Pergunta e resposta | Ligar pares, Montar a frase, Balões, Tocar; chefão: Montar a frase + Ligar pares + Balões |
| 25 Castelo das Estrelas | 241 a 250 | Revisão: entender frases curtas ouvidas e responder | Tudo junto | Siga a sequência, Montar a frase, Caça na cena, Dominó, Tocar; fase 250: grande chefão final |

### 4.4 Lógica

Régua de dificuldade: quantidade de elementos (2, depois 3, 4 e 6), memória (pares de 2 a 8, sequência de 2 a 7 toques, espiadinha de 3 segundos caindo para 1,5 segundo, sempre com botão de espiar de novo nos primeiros mundos), tamanho do labirinto e da grade (3 por 3 até 6 por 6), casas a descobrir no sudoku (1 a 6), comandos do robô (2 a 8, depois laço), peças do quebra cabeça (2 a 16) e quantidade de critérios (um atributo, depois dois, depois três).

| Mundo | Fases | Objetivo pedagógico | Dificuldade | Atividades |
|---|---|---|---|---|
| 1 Jardim | 1 a 10 | Igual, diferente, tamanho, filas AB, sombras, grupos | 2 a 3 elementos | Tocar, Memória, Cesta |
| 2 Floresta | 11 a 20 | Filas ABC, AAB, ABB, números em fila, memória grande | 3 a 5 elementos | Tocar, Memória, Cesta |
| 3 Praia | 21 a 30 | Filas de 4 e 5, intrusos, cor e forma, memória de 6 | 4 a 6 elementos | Tocar, Memória, Cesta |
| 4 Montanha | 31 a 40 | Labirinto, sudoku de uma casa, cesta com dois critérios | Grades 3 por 3 a 4 por 4 | Tocar, Cesta, Memória |
| 5 Espaço | 41 a 50 | Labirinto grande, sudoku com tampas, filas de 5 | Grade 4 por 4 | Tocar, Memória, Cesta |
| 6 Fazenda | 51 a 60 | Classificar por dois atributos, filas de cor e forma | 2 critérios | Cesta, Ligar pares, Quebra cabeça (4 peças), Tocar; chefão: Quebra cabeça + Cesta + Tocar |
| 7 Fundo do Mar | 61 a 70 | Memória de sequência (Simon de 2 a 3 toques) e pares | Sequência de 3 | Siga a sequência, Memória, Pescaria, Tocar; chefão: Siga a sequência + Memória + Pescaria |
| 8 Selva | 71 a 80 | Direita e esquerda a partir do personagem, labirintos médios | Grade 4 por 4 | Programar o robô (2 a 3 setas), Tocar (labirinto), Esconde esconde, Caça na cena; chefão: Robô + Esconde esconde + Tocar |
| 9 Polo Norte | 81 a 90 | Quebra cabeça e completar a figura | 4 a 6 peças | Quebra cabeça, Tocar, Ligar pares (sombras), Memória; chefão: Quebra cabeça + Ligar pares + Tocar |
| 10 Circo | 91 a 100 | Simetria e espelho, figura que completa o espelho | Figuras de 4 a 6 partes | Pintar por código (espelhar), Tocar, Dominó, Cartas viradas; chefão: Pintar + Dominó + Tocar |
| 11 Deserto | 101 a 110 | Sudoku 4 por 4 com 2 casas a descobrir | 2 casas | Tocar (sudoku), Esconde esconde (3 trocas), Siga a sequência (4), Cesta; chefão: sudoku + Esconde esconde + Siga a sequência |
| 12 Cidade | 111 a 120 | Programar: sequência de comandos | 3 a 5 setas | Programar o robô, Trem (passos de uma tarefa), Tocar, Caça na cena; chefão: Robô + Trem + Caça |
| 13 Castelo | 121 a 130 | Se e então; detetive com duas pistas | 2 pistas | Esconde esconde (pista falada), Tocar, Ligar pares, Cartas viradas; chefão: Esconde esconde + Tocar + Ligar pares |
| 14 Vale dos Dinossauros | 131 a 140 | Seriação de 5 por tamanho, ordem temporal de uma história em 3 quadros | 5 elementos | Trem, Cartas viradas, Balança (mais pesado), Tocar; chefão: Trem + Balança + Cartas viradas |
| 15 Ilha do Tesouro | 141 a 150 | Mapas e coordenadas (linha e coluna, A1, B2) | Grade 4 por 4 | Caça na cena (coordenadas), Programar o robô, Tocar, Pintar por código; chefão: Caça + Robô + Pintar |
| 16 Fábrica de Doces | 151 a 160 | Balança lógica: equivalências com figuras (2 bolos pesam 1 abacaxi) | 2 trocas | Balança, Tocar, Dominó, Cesta; chefão: Balança + Dominó + Tocar |
| 17 Bosque Encantado | 161 a 170 | Memória de sequência até 5 e cartas viradas | Sequência de 5, 8 cartas | Siga a sequência, Cartas viradas, Memória, Tocar; chefão: Siga a sequência + Cartas viradas + Memória |
| 18 Vulcão | 171 a 180 | Programar com repetição (bloco 2 vezes) | 4 a 6 comandos com laço | Programar o robô, Trem, Tocar, Quebra cabeça (9 peças); chefão: Robô + Quebra cabeça + Trem |
| 19 Reino das Nuvens | 181 a 190 | Padrões de crescimento e matriz 3 por 3 (o que falta) | 3 critérios | Tocar (matriz), Pintar por código, Trem, Ligar pares; chefão: matriz + Pintar + Trem |
| 20 Caverna de Cristal | 191 a 200 | Quebra cabeça de 9 a 12 peças, encaixar formas | 9 a 12 peças | Quebra cabeça, Caça na cena, Dominó, Tocar; chefão: Quebra cabeça + Dominó + Caça |
| 21 Parque de Diversões | 201 a 210 | Quem é quem (tabela lógica 2 por 2), intrusos com 3 critérios | 3 critérios | Tocar, Ligar pares, Esconde esconde (5 trocas), Cesta; chefão: Ligar pares + Esconde esconde + Tocar |
| 22 Cidade dos Robôs | 211 a 220 | Programar com obstáculos e mais de um caminho | Grade 5 por 5, até 8 comandos | Programar o robô, Siga a sequência (6), Trem, Tocar; chefão: Robô + Siga a sequência + Trem |
| 23 Savana | 221 a 230 | Diagramas de grupos (dentro dos dois círculos) | 2 conjuntos | Cesta (dois grupos), Ligar pares, Caça na cena, Tocar; chefão: Cesta + Caça + Ligar pares |
| 24 Vale das Cores | 231 a 240 | Sudoku 4 por 4 com várias casas e matriz de cor e forma | 4 a 6 casas | Tocar (sudoku), Pintar por código, Quebra cabeça (16), Cartas viradas; chefão: sudoku + Pintar + Quebra cabeça |
| 25 Castelo das Estrelas | 241 a 250 | Revisão: desafios combinados de memória, programação e padrões | Tudo junto | Robô, Siga a sequência, Quebra cabeça, Balança, Tocar; fase 250: grande chefão final |

## 5. Chefão do mundo

* **Fase 10 de cada mundo** (`Mundos.ehChefao(fase)`). As 5 perguntas usam as três atividades principais do mundo (listadas no fim de cada linha das tabelas) e a revisão do meio vem do próprio mundo.
* **Visual**: bolinha maior no caminho com uma coroa, apresentação com o chefão do tema (vaca gigante na Fazenda, polvo no Fundo do Mar, robô gigante na Cidade dos Robôs) desenhado em código. O chefão não é inimigo: é um amigo que propõe o desafio e comemora junto.
* **Recompensa maior**: 3 estrelas valem uma figurinha dourada do álbum, um baú surpresa e o dobro de moedinhas da fase (respeitando o limite diário).
* **Grande chefão final** (fase 250 de cada caminho): uma pergunta de cada faixa de 5 mundos, troféu do caminho e certificado na área dos pais.

## 6. Engajamento a longo prazo

Implementação fica para outro agente. Tudo offline, sem dados pessoais, sem compras e sem punição.

* **Recompensa do chefão**: além do item da casa ou do brinde, o chefão vencido dá figurinha dourada, baú e moedinhas extras (seção 5). Guardar em `EstadoJogo` um conjunto `chefoes` com as chaves `TRILHA:mundo`.
* **Álbum de figurinhas por mundo**: cada mundo tem 12 figurinhas do tema (bichos da fazenda, peixes, dinossauros). Ganha uma por fase vencida pela primeira vez no mundo, mais a dourada do chefão. O álbum abre pelo início, com página por mundo e espaço vazio com a silhueta da figurinha que falta. Figurinha repetida (de fase repetida) vira moedinha. Campo sugerido: `figurinhas: Set<String>` serializado como as estrelas.
* **Sequência de dias**: conta dias seguidos em que a criança jogou pelo menos uma fase ou treino. Mostra um calendário de solzinhos; a cada 3 dias, um baú. Quebrar a sequência não tira nada: o contador recomeça e os solzinhos já ganhos ficam guardados como coleção ("dias de sol"). Campos: `diasSeguidos`, `ultimoDiaJogado`, `diasDeSol`.
* **Baús surpresa**: abrem com 3 toques (o mesmo gesto do presente). Conteúdo sorteado entre moedinhas, uma figurinha, um enfeite da loja ou uma roupa. Nunca vende baú, nunca pede dinheiro de verdade.
* **Conquistas e medalhas**: medalhas por marcos de aprendizado, não por tempo de tela: primeiro mundo completo, 10 chefões, 100 fases, todas as vogais traçadas, primeira compra no mercadinho, primeiro programa do robô, 7 dias de sol. Ficam numa estante na casa por dentro. Campo: `medalhas: Set<String>`.
* **Eventos da semana**: um tema por semana, calculado pela data local (sem internet), por exemplo semana do mar, dos dinossauros, das cores. Durante a semana, a missão do dia usa o tema e dá uma figurinha especial do evento. Nada se perde se a criança não jogar na semana.
* **Cuidado com o limite de tempo**: todos esses recursos respeitam o limite diário definido pelos pais e não mandam notificação.

## 7. Como o código está organizado para o trabalho em paralelo

* `dominio/fases/FasesComum.kt`: `ModeloDeFase(titulo, emoji, criar)`, a base `FasesDoCaminho` (utilidades usadas por mais de um caminho, `provisoria`, `TOTAL_DE_FASES = 250`, `FASES_PRONTAS = 50`).
* `dominio/fases/FasesNumeros.kt`, `FasesLetras.kt`, `FasesIngles.kt`, `FasesLogica.kt`: cada um com `override fun fase(numero: Int): ModeloDeFase` (numero de 1 a 250). Hoje as fases 1 a 50 vêm da lista `ate50` e as fases 51 a 250 de `provisoria(numero, ate50)`, que repete a fase equivalente do mundo 5 (41 a 50).
* `dominio/fases/VocabularioNumeros.kt`, `VocabularioLetras.kt`, `VocabularioIngles.kt`, `VocabularioLogica.kt`: vocabulário novo de cada caminho.
* `dominio/GeradorDeFases.kt`: só monta a partida (5 perguntas, revisão no meio, missão do dia e a regra de não repetir a mesma atividade de mexer 3 vezes seguidas). A API pública não mudou. Regras de variedade (`regrasDa`, conferidas em `VariedadeDasFasesTest` nas 1000 fases): do mundo 6 em diante, no máximo 2 perguntas do mesmo desafio de mexer e pelo menos 3 jeitos de jogar por partida; o chefão dos mundos 6 a 25 tem no máximo 2 perguntas de tocar (40%) e pelo menos 3 desafios de mexer diferentes. Quando a fase sozinha não cumpre as regras, a pergunta que falta vem das fases anteriores do mesmo mundo (depois do mundo anterior). O pintar por código tem no máximo 6 colunas (`PintarPorCodigo.MAX_COLUNAS`); os desenhos grandes crescem para baixo.
* `dominio/AtividadesNovas.kt`: data classes do catálogo. `ui/telas/atividades/novas/`: uma tela por tipo.

Para preencher um mundo novo num caminho: trocar, no arquivo do caminho, a chamada `provisoria` pelas fases do mundo (por exemplo uma lista `mundo6` com 10 `ModeloDeFase`, escolhida pelo intervalo de `numero`), com o vocabulário no `Vocabulario` do caminho. Para ligar um desafio novo: fazer a tela no arquivo dele, depois sortear o tipo nos geradores, acrescentar o nome do tipo ao conjunto `tiposConhecidos` de `MilFasesTest` e dar a ele um ramo de validação próprio em `DominioTest` e `MundosETreinoTest` (hoje o ramo `else` desses testes falha se um tipo novo for sorteado).
