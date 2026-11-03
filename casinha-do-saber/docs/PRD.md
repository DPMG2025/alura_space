# PRD: Casinha do Saber

Documento de produto para continuar o desenvolvimento numa nova sessão. Atualizado em 3 de novembro de 2026 (fim do dia).

## 1. Visão

App Android educativo para crianças a partir de 4 anos. A criança escolhe ser **menino ou menina**, aprende **Números, Letras (português), Inglês e Lógica** e, a cada fase nova vencida, ganha peças para **montar e decorar a sua casinha**. A casa é o motor da motivação.

**Meta do dono do produto:** qualidade de app AAA, nota 5 na Play Store, ou seja, um app extraordinário. "Nota 5" só existe depois de publicar e receber avaliações reais; até lá, o critério é a qualidade AAA.

## 2. Público e princípios

* Crianças de 4 a 6 anos que **ainda não leem**: tudo é narrado em voz alta (TTS do Android, offline), com botões e figuras grandes.
* Pais: área protegida (continha de multiplicação), progresso, limite de tempo, privacidade.
* Errar não pune: a criança ouve o nome do que tocou e tenta de novo; depois de 2 erros a resposta certa brilha.
* Sem internet, sem anúncios, sem coleta de dados, sem compras.
* Arte 100% em código (Canvas do Compose) e emojis da fonte Noto embutida (emoji2-bundled). Não há arquivos de imagem.

## 3. Onde está o código

* Repositório: `DPMG2025/alura_space`, branch **`claude/educational-kids-android-app-7bdyy3`**, pasta `casinha-do-saber/`.
* Kotlin + Jetpack Compose, minSdk 24, targetSdk 35, AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, Gradle 8.14.3.
* Pacote `br.com.casinhadosaber`:
  * `dominio/` (Kotlin puro, testável): `Modelos.kt` (Pergunta, Opcao, Visual, Cena, Atividade, Trilha, Fase), `GeradorDeFases.kt` (todas as fases), `Vocabulario.kt`, `ItensDaCasa.kt` (40 peças, Lugar, Comodo, caixas de sobreposição), `EstadoJogo.kt` (progresso, moedas, loja, missão, histórico, brinde, serialização), `Loja.kt` (produtos, comidas, 16 roupas e 18 acessórios em 6 partes do corpo), `Mundos.kt` (5 mundos de 10 fases), `Treino.kt` (treino sem fim), `VocabularioDosMundos.kt`, `CasaPorDentro.kt` (planta da casa por dentro: dois andares, móveis, escada, `Acao`, caminhada).
  * `dados/Repositorio.kt` (SharedPreferences, limite de tempo diário, som e música).
  * `audio/` : `Narrador.kt` (melhor voz offline por idioma, pausas, inglês mais lento, estado `falando`), `Sons.kt` e `SonsDaCasa.kt` (efeitos sintetizados), `Musica.kt` (música de caixinha gerada).
  * `ui/Moldura.kt` : escala de tablet, toque mínimo de 64dp, largura máxima e paisagem em duas colunas.
  * `ui/componentes/` : `Provador.kt` (gavetas de roupa, chapéu e acessórios do espelho e do guarda roupa), `CenaDaCasa.kt` (casa de fora, arrastar, dia e noite, clima, visitantes, balão de recusa, jardim de baixo), `Isometrico.kt` (projeção da casa por dentro), `Crianca.kt` (personagem), `Comuns.kt` (BotaoFofo, MaoGuia, estrelas), `Confete.kt`, `PortaoDeAdulto.kt`.
  * `ui/telas/` : Perfil (também espelho), BoasVindas, Inicio, Trilha, Jogo, Premio, Quase, Casa, CasaPorDentro, Pais, Dormir.
  * `ui/telas/atividades/` : Cesta, Memória, MontarPalavra, Traçar, `TracosDasLetras.kt` (ordem e sentido dos traços de cada letra) e `Comum.kt` (mãozinha que ensina o gesto).
  * `MainActivity.kt` : navegação (sealed `Tela`), limite de tempo, rotação livre só em tablet.

## 4. Como verificamos (o ciclo que funciona)

O ambiente da sessão **não acessa o Google Maven**, então o APK é compilado no **GitHub Actions** (`.github/workflows/casinha-do-saber.yml`):

1. Testes de unidade do domínio (também rodam localmente, num projeto Gradle JVM em `scratchpad/jvmtest` que aponta para `dominio/` e todos os testes de `test/.../dominio`: `AcessoriosTest.kt`, `MundosETreinoTest.kt`, `CasaPorDentroTest.kt` e os demais). Para pegar erros de tipo das telas antes do CI, `scratchpad/compilatudo` compila `dominio/`, `ui/` e `MainActivity.kt` com as bibliotecas Compose Desktop do cache do Gradle e stubs das partes Android (`prep.sh` copia `MainActivity`, `Tema` e `CenaDaCasa` adaptados).
2. **Capturas de tela** com Robolectric + Roborazzi (`CapturasTest.kt`), commitadas pelo CI em `docs/capturas/`. Testes de pixels: chuva e neve só fora da casa, objetos da casa completa sem se amontoar, chapéu encostado na cabeça de cada criança (captura 56), cama recusada com balão (55) e tablet deitado (50 a 54 e 57).
3. Lint, APK debug e release, pacote `.aab`.
4. **Versão de teste para celular**: o CI publica `CasinhaDoSaber.apk` na Release `casinha-teste`:
   https://github.com/DPMG2025/alura_space/releases/download/casinha-teste/CasinhaDoSaber.apk
5. **Emulador Android real** (`JogoNoEmuladorTest.kt`): perfil, boas vindas, fase inteira com um erro (cada tipo de atividade é jogado de verdade: tocar, cesta, memória, montar palavra e traçar pelos traços de `TracosDasLetras`), presente com 3 toques, item entra na casa, arrastar a cama, cama recusada no jardim, fase de cestas, relatório das vozes, vozes gravadas em WAV (`VozesGravadasTest.kt`) e emojis na fonte embutida (`EmojisNaFonteEmbutidaTest.kt`, também no job `emulador-android-antigo`, API 24 e 26, só esse teste). Resultados em `docs/emulador/` (vozes em `vozes/`, Android antigo em `api24/` e `api26/`).
6. **Revisor independente**: um subagente avalia as capturas com régua de app AAA e dá nota. Evolução: 6 → 7 → 7,5 → 8 → 8,5 → 8,5 → 9 → 9 → 8,5 → 9 → 9,5 → 9,5 (a régua subiu depois da casa por dentro; o 9 veio com os móveis arrastáveis, a paisagem com tabuleiro e o caminho limpo; o 9,5, no fim de 3 de novembro, com telhado e céu na casa por dentro, patinho de borracha desenhado, dica de arrastar e os cortes do caminho e da coluna resolvidos; o segundo 9,5, em 3 de novembro à noite, já com a régua mais alta, com roupas e acessórios, 200 fases em 5 mundos e o treino sem fim; o terceiro 9,5, no fim de 3 de novembro, com material dourado, sudoku de uma casinha só, labirinto compacto, "I like" sem entregar a frase e todos os enfeites da casa por dentro desenhados à mão).

Ler os logs do CI pelo MCP do GitHub (`actions_list`, `get_job_logs` com `failed_only`; erros de Kotlin aparecem como `e: file://`). Cada push cancela o build anterior (concurrency), inclusive o emulador: não fazer push enquanto o emulador roda. O passo de capturas usa `git pull --rebase -X theirs`; antes de cada push local, `git pull --no-rebase`.

Lições do polimento: `Offset` é classe de valor e não pode ser parâmetro `vararg` (erro de compilação que só o CI pega). O lint do CI trata `BoxWithConstraints` sem usar `maxWidth` ou `maxHeight` como erro (`UnusedBoxWithConstraintsScope`): nesse caso usar `Box`.

Lição da conferência local: dá para ver telas antes do CI com Compose Desktop 1.5.12 (o único com as bibliotecas de execução no cache), num projeto `scratchpad/ver` que compila só `dominio/`, `Isometrico.kt`, `Enfeites.kt`, `Crianca.kt` (trocando `quadraticTo` por `quadraticBezierTo`) e `CenasDesenhadas.kt`, e salva PNG com `ImageComposeScene`. Por isso desenhos novos ficam em arquivos pequenos, sem depender das telas inteiras.

Lições das capturas (Robolectric): a tela só é desenhada na hora da captura, então o que é calculado em `drawWithCache` não existe antes dela (a geometria da casa por dentro agora também é montada em `onSizeChanged`). Esperas de animação que precisam aparecer nas capturas contam o tempo pelos quadros (`esperarQuadros`, com `withFrameNanos`), não com `delay`. Não fazer push enquanto o emulador roda, nem quando o laço de espera termina por tempo esgotado: conferir o estado antes.

## 5. O que já está pronto

**Aprendizado**
* 4 caminhos com **50 fases cada (200 fases)** em **5 mundos de 10 fases** (Jardim, Floresta, Praia, Montanha, Espaço; `Mundos.kt`), 5 perguntas por fase, perguntas sorteadas, **4 opções em todas as perguntas de escolha**. As 25 fases antigas mantêm o número (o progresso salvo continua valendo); as fases 26 a 50 ficam no fim de `GeradorDeFases.kt` e o vocabulário delas em `VocabularioDosMundos.kt`.
* Conteúdo das fases 26 a 50 (BNCC da educação infantil e 1º ano): **Números** contar até 20, traçar algarismos, somar e tirar até 20, de 10 em 10 até 100, dezenas e unidades, dobro e metade, pesado, leve e alto, horas cheias, moedinhas (com cesta para pagar), gráfico de figuras, problemas narrados (inclusive de dois passos), posições na fila e lados das formas, sequências, comparar até 100. **Letras** som das letras (fonético), famílias silábicas, sílaba que falta, letras pequenas (b d p q), traçar minúsculas, CH LH NH, BR PR TR, palavras de 3 e 4 sílabas, montar com letras, ler frases e escolher a figura, ordem alfabética, quantas letras, como se escreve, última letra. **Inglês** school, nature, feelings, jobs, verbs, wild animals, opposites, números até 20, How many?, What color is it?, frases "I like", "I am", "I can". **Lógica** filas de 4 e 5, intrusos (simples e com dois critérios), cor e forma, memória de 6, **labirinto** (`Cena.Labirinto`: quadro recortado em volta do caminho, caminho de terra com pedrinhas, a criança no começo e a casinha na chegada; perguntas de lado e de quantas pedrinhas; as respostas de lado são **setas desenhadas** em Canvas, grossas, arredondadas e laranja, em `ui/componentes/Setas.kt` e `Visual.Seta`, com a mesma narração "para cima", "para a esquerda") e **sudoku** de figuras 4 por 4 em `Cena.Grade` (só a casinha amarela a descobrir; nos mais difíceis, outras casinhas viram tampinhas de madeira fechadas), cesta com dois critérios. **Dezenas e unidades** com material dourado (`Cena.MaterialDourado`: barrinhas de dez cubinhos e cubinhos soltos). No "I like" o balão diz "Leia e escolha a figura" e a frase aparece só no cartão.
* **Treino sem fim** por caminho (botão "Treinar" embaixo do caminho, depois da primeira fase vencida): `Treino.kt` sorteia perguntas das fases vencidas com peso nas de menor precisão, abre fases mais novas a cada 3 acertos seguidos e volta com 2 erros seguidos; o ✖ termina e mostra o prêmio com moedinhas (dentro do limite diário) e o histórico das fases é atualizado.
* **Treino e voltar do Android**: o voltar do Android no treino termina igual ao ✖ (guarda moedinhas e histórico, uma vez só), com teste em `CapturasTest` (`voltarNoTreinoTerminaComoOX`).
* Fase só é vencida com **4 de 5 acertos de primeira** (3 estrelas com 5, 2 com 4). Abaixo disso, tela "Quase!" com o presente trancado.
* Depois de 2 derrotas seguidas na mesma fase, rodada facilitada com um exemplo resolvido junto, que não conta.
* Uma pergunta de revisão de fase anterior em cada partida; **Missão do dia** (3 perguntas das fases mais fracas, +6 moedas, uma vez por dia).
* **Atividades de mexer, com o gesto ensinado** por uma mãozinha que faz o movimento: **Cesta** (contar e tocar no certinho, ou classificar só as do grupo; contador dentro da cesta), **Memória** (espiadinha e pares falados), **Montar palavra** (sílabas arrastadas para os encaixes, com sílabas extras), **Traçar** (ponto verde de início, setas numeradas e traços na ordem escolar, para todas as letras). Todas usam o mesmo fluxo de resposta (acerto de primeira, confete, som); o acerto fica verde no progresso.
* Conteúdo: contar até 20, cesta, mais ou menos, maior e menor, formas, somar e tirar até 10, historinhas, antes e depois, de 2 em 2; vogais, consoantes, maiúscula e minúscula, primeira letra, começa igual, leitura simples e difícil, sílabas, quantos pedacinhos, montar palavra, completar, rimas, traçar; inglês de cores, animais, números, frutas, corpo, formas, roupas, casa, comida, brinquedos, tempo, família, veículos, ações; lógica de igual, diferente, tamanho, filas AB, ABC, AAB e ABB, sombras, combina, grupos, cesta de classificar, memória, números em fila.

**Roupas e acessórios**
* **16 roupas** (listras, pijama, herói, abelha, fada, astronauta, vestido, macacão, futebol, bailarina, capa de chuva, médico, chef, pirata, realeza, dinossauro), todas desenhadas em código na criança e livres para menino e menina (captura 58).
* **18 acessórios** em 6 partes do corpo (rosto, pescoço, costas, mão, pés, cabelo), um por parte ao mesmo tempo (`Perfil.acessorios`), desenhados na criança e combináveis com roupa e chapéu (captura 59).
* **Provador** (`Provador.kt`): espelho e guarda roupa com abas por gaveta, cada item mostrado já vestido na própria criança (captura 60). Acessórios entram na loja e também como **brinde** no prêmio.

**Caminho (trilha)**
* 50 bolinhas por caminho, com uma faixa de cor e uma plaquinha por mundo, botão **Treinar** flutuante embaixo com esmaecido (captura 61), **marcos** (desafio com troféu), estrelas por fase, criança parada na fase da vez e rolagem até ela; versão própria para tablet.
* Traçado em curvas que sai sempre de lado da bolinha: os pontinhos não passam mais por trás dos nomes das fases.
* **Paisagem**: tabuleiro em fileiras (serpente) com três fases por linha, numa moldura larga; a volta do caminho fica a 24 dp da borda (o pontinho aparece inteiro).
* Ao abrir, a rolagem para exatamente no começo de um degrau, sem passar do fim do caminho: nenhuma plaquinha fica cortada embaixo do cabeçalho.

**Casa de fora**
* 40 peças em ordem; estruturas "caem do céu", objetos podem ser arrastados; móveis só dentro, coisas de jardim só fora.
* **Cama recusada**: volta tremendo, aparece um balão "não pode" e o cômodo certo brilha.
* Dia e noite, chuva e neve (só fora), animações, visitantes, **jardim de baixo** nas telas altas, onde os objetos do jardim agora podem ficar (antes era só decoração).
* Abas 🎁 (presentes) e 🛒 (loja) com toque de 64dp; pintar paredes; loja com moedinhas, experimentar antes de comprar.
* **Brinde no prêmio**: depois das 40 peças, cada fase nova dá um presente da loja de graça + 5 moedas, mostrado na tela de prêmio (presente recomposto, abre com 3 toques).

**Casa por dentro (isométrica 2,5D)**
* Porta da casa leva para dentro: **dois andares** com as mesmas cores e cômodos da casa de fora, **escada** que sobe e desce (setinha que balança), a criança anda até onde se toca.
* **Geladeira** que abre, com abas de comidas (seleção acessível como aba) e comidas compradas na loja; **guarda roupa** com roupas compradas; **espelho** que abre o perfil para trocar a aparência e volta para onde estava.
* **Objetos interativos**: torneira, TV, fogão, banheira, cama (dormir), descarga, lâmpadas que acendem e apagam.
* Loja de **comidas e roupas** dentro da casa; listas que mostram que rolam.
* **Arrastar móveis**: segurar (ou sair arrastando) leva o móvel ladrilho por ladrilho, com realce verde quando cabe e vermelho quando não cabe; as posições ficam salvas (`moveisDentro` no `EstadoJogo`, chave `moveis_dentro` no `Repositorio`) e a criança dá um passinho se o móvel cair onde ela está.
* **Patinho de borracha** amarelo desenhado em Canvas (corpo, rabinho, bico laranja, olho, asinha), balançando na água da banheira (quac quac, "duck"), e **lixeira** com tampa, com sons próprios em `SonsDaCasa`.
* **Telhado aberto** no andar de cima, como numa casinha de bonecas: forro de madeira com vigas, empena com janelinha redonda, telhas na cor do telhado de fora e chaminé (quando a casa já tem). Aparece quando sobra altura (celular em pé); com painel aberto ou tela baixa, a casa usa a altura toda sem ele. Na faixa de céu acima, **sol e nuvens** que se mexem devagar.
* **Enfeites desenhados à mão** (`ui/componentes/Enfeites.kt`): gato laranja sentado no sofá, ursinho com lacinho na cama, violão encostado, plantinha no vaso de barro, pilha de livros, bolo de dois andares com velinha, lixeira com tampa e vaso sanitário, todos em Canvas no traço dos móveis. A casa por dentro não tem mais emoji ao lado de móvel vetorial (só as setinhas da escada e as comidas da geladeira).
* Quarto sem sobreposição: o **violão** fica no canto da frente do quarto e a **plantinha** foi para o canto da frente do banheiro.
* **Nada esconde nada**: os livros ficam entre o sofá e a TV, recuados para a TV não tapar a pilha; no banheiro a ordem é banheira (embaixo da janela redonda), vaso e pia com o espelho, e o vaso aparece inteiro. O teste `OcultacaoNaCasaTest` mede, na projeção isométrica, quanto do contorno de cada objeto fica coberto por quem é desenhado na frente (no máximo 20%; só lixeira, fogão e geladeira, lado a lado na parede da cozinha, podem chegar a 40%).
* **Dica de arrastar**: na primeira visita, depois de uns segundos parada, a mãozinha (`MaoQueArrasta`, em `Comuns.kt`) segura um móvel (de preferência o sofá), leva para o lado com o realce verde e solta, com a narração "Psiu! Os móveis mudam de lugar. Segure um e arraste!". Fica salvo que já viu (`viuDicaDeArrastar`, chave `viu_dica_arrastar`); enquanto a criança nunca arrastou nada, a dica volta depois de 30 s parada (no máximo uma vez por minuto). Qualquer toque interrompe. A escolha do móvel está no domínio (`PlantaDaCasa.sugestaoDeArraste`, com teste). Captura 32b.

**Experiência**
* Primeira tela com personagem grande, penteados, escolhas faladas; boas vindas no terreno; mãozinha guia onde tocar; botão 🪞 de espelho no início e na casa.
* Presente abre com 3 toques e raios de luz; prêmio vai direto para a casa.
* **Área dos pais** com as 200 fases **agrupadas por caminho e por mundo** (recolher e abrir), precisão, "Praticando agora", tempo jogado, limite diário de 15/30/60 min, vozes, som e música, apagar progresso.
* **Tablet**: retrato 30% maior; **paisagem** com início e casa em duas colunas (a coluna da casa tem atalhos de espelho, pintar e entrar, e o baú vazio vira um cartão com o troféu ou o próximo presente e a barrinha de quantas coisas a casa tem, sem vão no meio) e as outras telas numa coluna central com sombra suave sobre o céu.
* Material da Play Store: `docs/play-store.md`, `docs/politica-de-privacidade.md`, `docs/publicar.md`.

**Testes**
* 85 testes de domínio (inclusive acessórios, mundos e treino; material dourado, sudoku com uma casinha só a descobrir, labirinto compacto sem atalho e balão que nunca entrega a frase de ler). Capturas novas: 58 a 60 (roupas, acessórios, provador), 61 (caminho com treino e mundo 3), 62 a 66 (dezenas e unidades, ler frases, I like, sudoku 4 por 4, labirinto grande) e 66b (labirinto com as setas desenhadas).
* Sobreposição dos objetos da casa completa, chapéu na cabeça (menina e menino, todos os chapéus), chuva só fora (teste corrigido), cama recusada, capturas em paisagem, planta da casa por dentro (`CasaPorDentroTest.kt`), emulador jogando todos os tipos de atividade.

## 6. O que resta (começar por aqui)

Feitos em 3 de novembro (build e emulador verdes, run 41598771511): setas do labirinto desenhadas em Canvas e casa por dentro sem objeto escondido (livros fora de trás da TV, vaso sanitário inteiro, com teste de ocultação). Resta só o que depende do dono:

1. **Voz** (evidência no emulador, falta só o ouvido): `VozesGravadasTest.kt` grava em WAV, com o próprio Narrador (mesma voz, velocidade e tom), os 12 sons esticados das fases de som das letras, os reforços ("mmmmm... macaco!") e falas típicas em português e inglês, em `docs/emulador/vozes/` (31 arquivos), com `vozes_relatorio.txt` (voz, idioma, qualidade, duração total, duração com som e blocos de som). No emulador API 34 (run 41605671938) o narrador escolheu `pt-br-x-pte-local` e `en-us-x-tpf-local`, ambas qualidade 400; nenhum arquivo vazio e todos dentro do critério (som esticado entre 0,3 s e 6 s de som): `som_m.wav` 2,24 s, `som_s.wav` 2,38 s, `som_o.wav` 0,77 s, `en_i_like.wav` 1,80 s. Os sons contínuos saem em um bloco só (sem soletrar); `som_f.wav` aparece em 4 blocos, provavelmente o chiado fraco do F oscilando, e é o primeiro a ouvir. O teste também revelou uma corrida no Narrador (ficava pronto antes de escolher as vozes), já corrigida. Só um celular real confirma: a voz que vem instalada no aparelho do dono (pode não ser a mesma do emulador) e se os sons soam naturais para uma criança. Para ouvir: abrir os WAV de `docs/emulador/vozes/` no GitHub.
2. **Emojis na fonte embutida: resolvido com evidência.** A tarefa Gradle `listarEmojis` varre o código e gera a lista de todos os emojis do app (359); `EmojisNaFonteEmbutidaTest.kt` roda no emulador Android 7.0 (API 24), 8.0 (API 26) e 14 (API 34), com o EmojiCompat do app iniciado pela fonte embutida. Resultado igual nos três: 356 viram EmojiSpan, 0 sem glifo; 🪙 🪶 🪟 viram EmojiSpan. Os 3 restantes (▶ ✔ ✖ sem seletor de emoji) têm glifo e aparecem de propósito como símbolo de texto, pintados na cor do botão. Captura no API 26: `docs/emulador/api26/emojis_api26.png`; relatórios em `docs/emulador/api24/` e `api26/`. Um celular antigo de verdade só acrescentaria a confirmação de fabricantes com fonte própria, que o EmojiCompat contorna do mesmo jeito.
3. **Teste com crianças** de verdade (seção 8).
4. **Publicação** (seção 8).

3D "de verdade" (Filament ou SceneView) só faz sentido com modelos 3D de artista (loja de modelos glTF); a casa isométrica em código foi a escolha.

## 7. Pendências apontadas pelo revisor (última nota 9,5/10)

Nota 9,5 no fim de 3 de novembro, sobre as capturas 32 e 62 a 66, com build e emulador verdes (run 41594702111). Os quatro achados de fase e o estilo misto da casa foram resolvidos; a nota não sobe para 10 porque o que falta não se resolve com código: uso real com crianças e voz num celular de verdade.

Pontos fortes novos: material dourado com barrinhas de dez e cubinhos soltos separados, fácil de contar (62); sudoku com uma só casinha amarela e tampinhas que não parecem falta de figura (65); labirinto justo ao caminho, com pedrinhas para contar, a criança e a casinha, sem fileiras vazias de árvore (66); "I like" com a frase só no cartão (64); casa por dentro com todos os enfeites no mesmo traço vetorial (32).

Avaliação pedagógica: a representação de dezenas agora segue o material concreto usado na escola (barrinha de dez e cubinho), o que liga a contagem ao valor posicional; o labirinto passou a pedir contagem de pedrinhas, mais concreta que "passos". O resto da avaliação anterior continua valendo: bom complemento à alfabetização e à matemática da educação infantil ao 1º ano, sem substituir leitura compartilhada.

O que segura o 10:

* Só o uso real confirma: voz num celular de verdade e teste com crianças (seção 8).
* Os detalhes de acabamento apontados (setas em emoji no labirinto, livros e vaso meio escondidos na casa por dentro) foram resolvidos depois desta nota.

## 8. Publicação (fora do alcance da sessão)

Criar a chave de upload e os segredos `CASINHA_KEYSTORE_*`, conta no Play Console, programa Famílias, teste interno e teste fechado (12 testadores por 14 dias para contas novas), testar com crianças reais e então produção. Passo a passo em `docs/publicar.md`.

## 9. Preferências do dono do produto

* Comunicação em português, sem hífen nem travessão no texto corrido.
* Quer ver o app no celular: manter o link da Release `casinha-teste` atualizado a cada build.
