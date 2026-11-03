# PRD: Casinha do Saber

Documento de produto para continuar o desenvolvimento numa nova sessão. Atualizado em 3 de novembro de 2026.

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
  * `dominio/` (Kotlin puro, testável): `Modelos.kt` (Pergunta, Opcao, Visual, Cena, Atividade, Trilha, Fase), `GeradorDeFases.kt` (todas as fases), `Vocabulario.kt`, `ItensDaCasa.kt` (40 peças, Lugar, Comodo, caixas de sobreposição), `EstadoJogo.kt` (progresso, moedas, loja, missão, histórico, brinde, serialização), `Loja.kt` (produtos, comidas e roupas), `CasaPorDentro.kt` (planta da casa por dentro: dois andares, móveis, escada, `Acao`, caminhada).
  * `dados/Repositorio.kt` (SharedPreferences, limite de tempo diário, som e música).
  * `audio/` : `Narrador.kt` (melhor voz offline por idioma, pausas, inglês mais lento, estado `falando`), `Sons.kt` e `SonsDaCasa.kt` (efeitos sintetizados), `Musica.kt` (música de caixinha gerada).
  * `ui/Moldura.kt` : escala de tablet, toque mínimo de 64dp, largura máxima e paisagem em duas colunas.
  * `ui/componentes/` : `CenaDaCasa.kt` (casa de fora, arrastar, dia e noite, clima, visitantes, balão de recusa, jardim de baixo), `Isometrico.kt` (projeção da casa por dentro), `Crianca.kt` (personagem), `Comuns.kt` (BotaoFofo, MaoGuia, estrelas), `Confete.kt`, `PortaoDeAdulto.kt`.
  * `ui/telas/` : Perfil (também espelho), BoasVindas, Inicio, Trilha, Jogo, Premio, Quase, Casa, CasaPorDentro, Pais, Dormir.
  * `ui/telas/atividades/` : Cesta, Memória, MontarPalavra, Traçar, `TracosDasLetras.kt` (ordem e sentido dos traços de cada letra) e `Comum.kt` (mãozinha que ensina o gesto).
  * `MainActivity.kt` : navegação (sealed `Tela`), limite de tempo, rotação livre só em tablet.

## 4. Como verificamos (o ciclo que funciona)

O ambiente da sessão **não acessa o Google Maven**, então o APK é compilado no **GitHub Actions** (`.github/workflows/casinha-do-saber.yml`):

1. Testes de unidade do domínio (também rodam localmente, num projeto Gradle JVM em `scratchpad/jvmtest` que aponta para `dominio/`, incluindo `CasaPorDentroTest.kt`).
2. **Capturas de tela** com Robolectric + Roborazzi (`CapturasTest.kt`), commitadas pelo CI em `docs/capturas/`. Testes de pixels: chuva e neve só fora da casa, objetos da casa completa sem se amontoar, chapéu encostado na cabeça de cada criança (captura 56), cama recusada com balão (55) e tablet deitado (50 a 54 e 57).
3. Lint, APK debug e release, pacote `.aab`.
4. **Versão de teste para celular**: o CI publica `CasinhaDoSaber.apk` na Release `casinha-teste`:
   https://github.com/DPMG2025/alura_space/releases/download/casinha-teste/CasinhaDoSaber.apk
5. **Emulador Android real** (`JogoNoEmuladorTest.kt`): perfil, boas vindas, fase inteira com um erro (cada tipo de atividade é jogado de verdade: tocar, cesta, memória, montar palavra e traçar pelos traços de `TracosDasLetras`), presente com 3 toques, item entra na casa, arrastar a cama, cama recusada no jardim, fase de cestas, relatório das vozes. Resultados em `docs/emulador/`.
6. **Revisor independente**: um subagente avalia as capturas com régua de app AAA e dá nota. Evolução: 6 → 7 → 7,5 → 8 → 8,5 → 8,5 → 9 → 9 → 8,5 (a régua subiu depois da casa por dentro; nota dada antes do polimento de 3 de novembro).

Ler os logs do CI pelo MCP do GitHub (`actions_list`, `get_job_logs` com `failed_only`; erros de Kotlin aparecem como `e: file://`). Cada push cancela o build anterior (concurrency), inclusive o emulador: não fazer push enquanto o emulador roda. O passo de capturas usa `git pull --rebase -X theirs`; antes de cada push local, `git pull --no-rebase`.

Lição do polimento: `Offset` é classe de valor e não pode ser parâmetro `vararg` (erro de compilação que só o CI pega).

## 5. O que já está pronto

**Aprendizado**
* 4 caminhos com **25 fases cada (100 fases)**, 5 perguntas por fase, perguntas sorteadas, **4 opções em todas as perguntas de escolha**.
* Fase só é vencida com **4 de 5 acertos de primeira** (3 estrelas com 5, 2 com 4). Abaixo disso, tela "Quase!" com o presente trancado.
* Depois de 2 derrotas seguidas na mesma fase, rodada facilitada com um exemplo resolvido junto, que não conta.
* Uma pergunta de revisão de fase anterior em cada partida; **Missão do dia** (3 perguntas das fases mais fracas, +6 moedas, uma vez por dia).
* **Atividades de mexer, com o gesto ensinado** por uma mãozinha que faz o movimento: **Cesta** (contar e tocar no certinho, ou classificar só as do grupo; contador dentro da cesta), **Memória** (espiadinha e pares falados), **Montar palavra** (sílabas arrastadas para os encaixes, com sílabas extras), **Traçar** (ponto verde de início, setas numeradas e traços na ordem escolar, para todas as letras). Todas usam o mesmo fluxo de resposta (acerto de primeira, confete, som); o acerto fica verde no progresso.
* Conteúdo: contar até 20, cesta, mais ou menos, maior e menor, formas, somar e tirar até 10, historinhas, antes e depois, de 2 em 2; vogais, consoantes, maiúscula e minúscula, primeira letra, começa igual, leitura simples e difícil, sílabas, quantos pedacinhos, montar palavra, completar, rimas, traçar; inglês de cores, animais, números, frutas, corpo, formas, roupas, casa, comida, brinquedos, tempo, família, veículos, ações; lógica de igual, diferente, tamanho, filas AB, ABC, AAB e ABB, sombras, combina, grupos, cesta de classificar, memória, números em fila.

**Caminho (trilha)**
* 25 bolinhas por caminho com **marcos** (desafio com troféu), estrelas por fase, criança parada na fase da vez e rolagem até ela; versão própria para tablet.

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

**Experiência**
* Primeira tela com personagem grande, penteados, escolhas faladas; boas vindas no terreno; mãozinha guia onde tocar; botão 🪞 de espelho no início e na casa.
* Presente abre com 3 toques e raios de luz; prêmio vai direto para a casa.
* **Área dos pais** com as 100 fases **agrupadas por caminho** (recolher e abrir), precisão, "Praticando agora", tempo jogado, limite diário de 15/30/60 min, vozes, som e música, apagar progresso.
* **Tablet**: retrato 30% maior; **paisagem** com início e casa em duas colunas e as outras telas numa coluna central com sombra suave sobre o céu.
* Material da Play Store: `docs/play-store.md`, `docs/politica-de-privacidade.md`, `docs/publicar.md`.

**Testes**
* Sobreposição dos objetos da casa completa, chapéu na cabeça (menina e menino, todos os chapéus), chuva só fora (teste corrigido), cama recusada, capturas em paisagem, planta da casa por dentro (`CasaPorDentroTest.kt`), emulador jogando todos os tipos de atividade.

## 6. O que resta (começar por aqui)

1. **Revisor independente** de novo, sobre as capturas deste polimento (24 a 35, 40, 41, 50 a 57, emulador e12 a e16), para ver se a nota volta a 9 ou mais.
2. **Casa por dentro**: a criança ainda não carrega objetos de um cômodo para outro (os móveis de dentro têm lugar fixo); avaliar arrastar móveis dentro de casa e mais objetos interativos por cômodo.
3. **Paisagem**: a coluna da direita da casa fica com muito espaço vazio quando não há loja aberta; o caminho em paisagem mostra poucas fases por vez.
4. **Caminho**: os pontinhos do caminho passam por trás dos nomes das fases em alguns trechos.
5. **Voz**: confirmar a voz em português num celular real (no emulador escolheu `pt-br-x-pte-local`, qualidade 400).
6. **Publicação** (seção 8).

3D "de verdade" (Filament ou SceneView) só faz sentido com modelos 3D de artista (loja de modelos glTF); a casa isométrica em código foi a escolha.

## 7. Pendências apontadas pelo revisor (última nota 8,5/10)

* As do item 6 acima. As anteriores (jardim apertado, tablet só em retrato, testes de sobreposição e chapéu) foram resolvidas neste polimento.

## 8. Publicação (fora do alcance da sessão)

Criar a chave de upload e os segredos `CASINHA_KEYSTORE_*`, conta no Play Console, programa Famílias, teste interno e teste fechado (12 testadores por 14 dias para contas novas), testar com crianças reais e então produção. Passo a passo em `docs/publicar.md`.

## 9. Preferências do dono do produto

* Comunicação em português, sem hífen nem travessão no texto corrido.
* Quer ver o app no celular: manter o link da Release `casinha-teste` atualizado a cada build.
