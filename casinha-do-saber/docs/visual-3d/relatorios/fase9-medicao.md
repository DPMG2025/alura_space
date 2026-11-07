# Fase 9: medição e guardas (desempenho no Android antigo, tamanho do APK, cobertura de emojis)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-ae25af16c7192bc22`, branch
`worktree-agent-ae25af16c7192bc22`, feita a partir de `ccr-162c6a70-urxpmq` (af890fd, fases 0 a 8). Commit local, sem
push (o hash está no fim). Só testes e CI: telas, domínio, narração, `arte3d/` e os jobs do emulador de celular e de
tablet não foram tocados.

## O que entrou

* **`androidTest/.../DesempenhoTest.kt`** (novo, 1 teste, `aberturaEMemoriaNasTelasPrincipais`). Save de uma criança
  (Ana, casa construída, mascote escolhido, 30 moedas, fases 1 a 6 dos números vencidas). Mede:
  * abertura: `SystemClock.elapsedRealtime()` de `ActivityScenario.launch(MainActivity)` até o nó "Caminho Números"
    existir na semântica (tela inicial composta). Duas medidas: a 1ª atividade do processo e a 2ª no mesmo processo;
  * memória: `Debug.getMemoryInfo` (PSS total e os resumos java-heap, native-heap e graphics, em MB) depois de 1,5 s
    de relógio do Compose e 0,5 s real em cada tela: tela inicial, casa (por fora), casa por dentro com a lojinha de
    comidas e roupas aberta (a roupa é experimentada na criança) e a primeira pergunta da fase 7 dos números (com o
    botão da história do mundo, se aparecer). Cada passo relança a atividade, sem depender do botão voltar;
  * grava `desempenho.txt` pelo `TestStorage` (reescrito a cada linha, para não perder o que já foi medido se um passo
    falhar) e `Log.w("DESEMPENHO", ...)`; inclui API, aparelho, `memoryClass`, `largeMemoryClass` e `maxMemory`;
  * asserções frouxas: 1ª abertura < 10 s e PSS < 400 MB em todas as telas.
  * Ressalvas: a instrumentação roda no processo do app, então o PSS inclui um pouco do executor de testes; o
    `Application` já existe quando o teste começa, então a abertura medida é a da atividade (sem o início do processo).
* **Workflow, job `emulador-android-antigo`** (API 24 e 26): roda
  `class=br.com.casinhadosaber.EmojisNaFonteEmbutidaTest,br.com.casinhadosaber.DesempenhoTest`; timeout de 40 para 45
  min; passo novo "Desempenho no Android antigo" (`if: always()`) que imprime o `desempenho.txt` no log; o passo de
  versionar já copiava todo `*.txt` da saída adicional, então o arquivo vai para `docs/emulador/api24/` e `api26/`
  (mensagem do commit do bot: "Atualiza emojis e desempenho ..."). Com o Orchestrator (global no `build.gradle.kts`)
  cada um dos dois testes roda no seu processo.
* **Workflow, job `build`**: passo "Conferir tamanho do APK (limite 150 MB)" logo depois de "Gerar APK e pacote da Play
  Store": `stat -c %s` em `app/build/outputs/apk/debug/*.apk`, `apk/release/*.apk` e `bundle/release/*.aab`, imprime o
  tamanho de cada um e falha se algum APK passar de 150 MB (ou se faltar um dos arquivos). O `.aab` só é impresso. O
  limite da pasta `assets/arte` (40 MB) continua no `ManifestoDaArteTest` (`ORCAMENTO_TOTAL`), sem duplicar. Não fiz
  `TamanhoDoApkTest` em JVM: os testes de unidade rodam antes de o APK existir.
* **`test/.../ui/componentes/CoberturaDeEmojisTest.kt`** (novo, JUnit puro): 12 sementes fixas (`Random(0..11)`), fases
  1 a 100 dos quatro caminhos, 5 perguntas cada (24 000 perguntas). Percorre por reflexão a cena, as opções e a
  atividade de cada pergunta (todas as classes de `dominio`, inclusive as atividades novas), sem a fala (`Trecho`) e
  sem o "✔" de enfeite das perguntas de mexer. Separa os emojis com o mesmo critério da tarefa `listarEmojis` do build
  (sequências com ZWJ, tons de pele, teclas; − → ★ e as formas de texto ficam de fora) e normaliza o U+FE0F como o
  `FigurasDaPergunta`. Aparição = uma pergunta em que o emoji aparece. Com modelo = `FigurasDaPergunta.temModelo`
  (`fig_*` ou a reserva 🪙 → `moeda`). Grava `app/build/relatorios/cobertura-emojis.md`, imprime o resumo e só falha
  abaixo de 50 % das aparições cobertas.
* **`docs/visual-3d/relatorios/cobertura-emojis.md`**: cópia estática da saída do teste rodado uma vez aqui.

## Números da cobertura (hoje)

377 emojis diferentes; 64 com modelo (59 imagens: 58 `fig_*` e a `moeda`); 313 sem modelo; 52 284 aparições, 28 167
cobertas (**53,9 %**). A fase 4A dava 57 % com outra contagem (cada emoji repetido da conta valia uma aparição e as
atividades novas não entravam). Mais frequentes sem modelo: 🐙 🦀 🐬 🦜 🦈 🐳 🦃 🐚 🐍 🦐 ❄ 🟦 🐊 🥛 👟 🐐 🌳 ⛄ 🍿 🍋 (lista de 40
no relatório). **Atenção:** a folga até a guarda de 50 % é de 3,9 pontos; conteúdo novo com emojis sem modelo pode
derrubar o teste. Aí a saída é modelar os mais frequentes (bichos do mar primeiro), não baixar o mínimo.

## Verificação

* JVM (Gradle 8.14.3 `--offline`, Kotlin 2.0.21, JUnit 4, projeto temporário fora do repositório com `dominio/`,
  `FigurasDaPergunta.kt` e `ManifestoDaArte.kt`, `workingDir` = `app/`): `CoberturaDeEmojisTest` 1/1 (3 s),
  `FigurasDaPerguntaTest` 6/6 e `ManifestoDaArteTest` 7/7 verdes.
* `DesempenhoTest` não compilado (sem SDK do Android): revisado à mão contra os outros testes do emulador (mesmas
  chaves de save, `esperar`/`tocar` como o `abrirFase` do `JogoNoEmuladorTest`, `PartidaNaTela`, `TestStorage`).
  APIs usadas existem no API 24 (`Debug.MemoryInfo.getMemoryStat` é do API 23). Quem prova é o CI.
* YAML validado com `python3 yaml.safe_load` (5 jobs; passos do `build` e do `emulador-android-antigo` conferidos).

## Riscos

* Primeira vez que a `MainActivity` roda no API 24/26 do CI (antes só rodava o teste de emojis, numa
  `ComponentActivity`). Se o app falhar ali, o teste acusa: é o objetivo.
* O emulador antigo do CI não tem aceleração de GPU; a abertura pode ficar perto do limite de 10 s. Se for ruído do
  CI, ver os números em `docs/emulador/api24/desempenho.txt` antes de mexer no limite.

## Commit

Testes, workflow e relatórios: `6204f85` (branch `worktree-agent-ae25af16c7192bc22`, sem push); este hash entrou
num commit seguinte, só neste arquivo.
