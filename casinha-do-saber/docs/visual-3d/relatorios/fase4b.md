# Fase 4, parte B: o jogo no visual brinquedo 3D (Kotlin)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a43e99d1facc7c943`, branch
`worktree-agent-a43e99d1facc7c943`, feita a partir de `ccr-162c6a70-urxpmq` (bdb62d1: arte integrada e fases 1 a 3,
5 e 7). Commit local, sem push (o hash está no fim). Regras do jogo, domínio, narração, workflow, `arte3d/`,
TelaInicio, TelaCasa, TelaPremio e as camadas da criança não foram tocados. Usa só a arte da 4A que já estava no
manifesto; nenhuma imagem nova.

## Arquivos

* **Novo** `ui/componentes/FigurasDaPergunta.kt` (Kotlin puro):
  * `FigurasDaPergunta`: índice emoji → `fig_<nome>` montado uma vez por manifesto a partir do campo `emojis`
    (o seletor U+FE0F é ignorado: ⭐ e ⭐️ são a mesma figura), `idPara`, `temModelo`, `lugarDaCena` (o `lugar` da
    primeira figura com modelo) e `RESERVAS` (🪙 → `moeda`, da fase 0).
  * `QuadroDoJogo`: ids `jogo_<caminho>_ilha` e `jogo_comum_<lugar>`; `daIlha(largura, altura)` (a ilha com a
    largura da tela, ou a que cabe em 0,9 da altura quando deitado, e o centro da ilha a 43% da altura, como a
    maquete 2); `vagas(n, ancoras, lugar)` (uma figura por `vaga_k` até o número de vagas, 190 px até 3 e 170 px até
    6; mais que isso, fileiras de até 5 dentro da área útil das âncoras `area_*`, ou no miolo da ilha; no varal
    penduradas pela âncora `topo`; no céu 80 px acima da grama; ordenadas de trás para a frente); `caixaVisivel` e
    `paraCaber` (a cena e as figuras cabem inteiras e centradas na caixa da figura, sem passar do tamanho da ilha).
* `ui/componentes/CenarioDoJogo.kt` (o desenho antigo fica como reserva, intacto):
  * `@Composable Modifier.cenarioDoJogo(cor, trilha)`: céu em degradê do app (o mesmo da tela inicial) e a ilha do
    caminho numa camada só. Sem a arte da ilha, chama o `cenarioDoJogo(cor)` de antes.
  * `ComposicaoDoJogo.camada(contexto, info, largura)`: as camadas grandes (800 x 1040) lidas sem passar pelo cache
    da `ArteDoApp`, reduzidas para a largura de desenho (em passos de 64 px, no máximo 800) e guardadas num
    `LruCache` próprio (1/16 da memória, mínimo 12 MB: a ilha e duas cenas). Como `ComposicaoDaCasa`.
  * `FiguraDaPergunta(emoji, tamanho, modifier, reserva)`: `ImagemDeArte(fig_*)` quando o emoji tem modelo; senão
    (ou se a imagem não decodificar) a `reserva`, que por padrão é o emoji. Decorativa, como o emoji era.
  * `FigurasNaCena(figuras, trilha)`: a cena comum do `lugar` (lagoa, mesa, varal até 5, cercado, tapete) com as
    figuras nas vagas; grama, céu e figuras sem modelo ficam direto na grama da ilha que está atrás.
* `ui/componentes/InterfaceDeBrinquedo.kt`: `PilulaDeContas(contas: List<id to escala>)`, a fileira de contas 3D
  com cor por conta; `ContasDeProgresso` passou a usá-la (mesma assinatura e mesmo desenho).
* `ui/telas/TelaJogo.kt` (mesma assinatura):
  * fundo `cenarioDoJogo(cor, fase.trilha)`;
  * barra: ✖ e 🔊 continuam `BotaoRedondo` (ícones 3D); as bolinhas com ★ viraram `PilulaDeContas`: verde =
    acertou de primeira, azul = com ajuda, amarela e maior = a da vez, perolada = ainda vem (no treino, as 6 últimas);
  * balão: `BalaoDaPergunta`, com o ícone 3D de som como `InlineTextContent`; o texto alternativo é "🔊", então a
    semântica continua "🔊 instrução" (o `JogoNoEmuladorTest` lê a instrução por `hasText("🔊")`);
  * crianças maiores (maquete 2): 132 dp ao lado do balão (100 dp em telas com menos de 700 dp de altura), 96 dp nas
    atividades de mexer (76 dp em tela baixa), 170 dp sem figura; o irmãozinho com 80% da altura (antes 66%);
  * `Cena.Emojis`: várias figuras iguais (contar) vão para `FigurasNaCena`; uma figura só fica grande na grama,
    pulsando; listas de figuras diferentes (com espaço, balança) mantêm a grade, num `cartaoFofo` com `FiguraDaPergunta`;
  * respostas: blocos coloridos vermelho, azul, verde e amarelo (as cores da `v3d_02`); número ou palavra em
    branco com contorno escuro (`TextoComContorno`); figuras num tom clarinho da cor com o lábio na cor viva.
    Perguntas de cor (opção `Visual.Cor`, ou "cor"/"color" na instrução) ficam com os blocos claros de antes. Certa:
    verde com pulo; errada: apagada e tremendo; com a ajuda ligada, as outras ficam a 60% e a certa pulsa. O ✔ e o
    ✖ das respostas viraram um selo desenhado (sem caractere);
  * `Conta`, `Cena.Palavra` e `Grade` em `cartaoFofo`; figuras de `Sequencia`, `Grade`, `Conta` e das opções (uma
    ou em grupo) por `FiguraDaPergunta`, com o `Text` de antes como reserva.
* Atividades, só a troca do `Text(emoji)` por `FiguraDaPergunta` (com o mesmo `Text` como reserva): cesta (figuras
  e cópia da mãozinha), memória (carta de uma figura e de quantidade), montar a palavra (figura e o alto-falante,
  agora o ícone 3D de som), `N1Figura` (ComumNovas1), `VisualN2` (ComumNovas2), `VisualN4` (ComumNovas4), balança,
  montar frase, ditado, escrever, esconde-esconde (bicho e surpresa) e mercadinho (produto). As atividades novas
  que desenham pelas três funções comuns (pescaria, balões, ligar pares, caça, trem, dominó, cartas viradas, siga
  a sequência) mudam junto. Nenhuma `testTag`, `contentDescription` ou `semantics` mudou (conferido no diff).
  Ficaram em emoji: a silhueta (`Visual.Sombra`), o quebra-cabeça (a figura é recortada em peças), a mãozinha 👆,
  o ⭐ do verso da carta e do traçar, 🔁, 💡, 🧽, 👋, 🧒 e os relógios.

## Memória (os emuladores do CI caíram duas vezes)

* Figuras: só `ImagemDeArte` (192 x 192 px, cache LRU da `ArteDoApp`), decodificadas no tamanho em que aparecem.
* Camadas grandes: no máximo três vivas (a ilha e as cenas da pergunta que sai e da que entra), cada uma lida,
  reduzida e com o original reciclado na hora; nada de bitmap em campo estático fora dos dois caches LRU.
* O cenário é um bitmap só (a ilha); o céu é um degradê. A cena comum desliza junto com as figuras na troca de
  pergunta, para não trocar o fundo de repente.

## Testes e capturas

* **Novo** `FigurasDaPerguntaTest` (JUnit puro, 6 testes, contra o `manifesto.json` real): emojis com modelo
  (com e sem U+FE0F, dois emojis da mesma figura, a reserva 🪙), emojis sem modelo (🦓 🐬 🐙 🍋 🟦 🦃 ❄️ 🏠 ☀️),
  letras, números e sinais nunca viram figura; toda `fig_*` tem `emojis`, `lugar` válido, tipo e âncora `topo`, e
  nenhum emoji está em duas figuras; `lugarDaCena`; as 4 ilhas e as 5 cenas no manifesto com as âncoras; vagas
  (ordem, varal, céu, 7 a 20 figuras dentro da área e sem repetir lugar); quadros da ilha (em pé e deitado) e da
  cena (cabe e fica centrada).
* **Novo** `CapturasJogo3DTest` (Robolectric, 393 x 852): `v3d_11_jogo_contar` (4 patinhos na lagoa da ilha dos
  números, Ana e o irmão, respostas 3 5 4 6) e `v3d_12_jogo_letras` (figuras 3D nas respostas, ilha das letras).
  Antes de capturar confere a arte (compõe e decodifica), o balão com "🔊" e a instrução, `opcao_certa` e `opcao`,
  "Sair da fase" e "Ouvir de novo", e que não há ✖ nem ★ em texto.
* Mudam sozinhas: 05 a 10, 17, 17b, 24 a 31, 52, 62 a 66 e as do jogo em `CapturasIrmaoTest`, `CapturasNovas*`,
  `CapturasRitmoTest`, `CapturasVozEscritaTest`. Nenhuma captura foi renomeada.

## Verificação feita aqui

* JVM puro (Kotlin 2.0.21, Gradle temporário `--offline`, `workingDir` = `app/`): `FigurasDaPerguntaTest` 6/6,
  `ManifestoDaArteTest` 7/7, `PecasDaInterfaceTest` 4/4.
* Compilação sem erro contra Compose Multiplatform desktop 1.7.3 (API do BOM 2024.12.01), com os stubs de
  `android.*`, áudio, `BackHandler` e `LocalContext` das fases anteriores: `TelaJogo`, `CenaDaHistoria`,
  `CenasDesenhadas`, todas as atividades (`atividades/` e `atividades/novas/`), todo o `ui/componentes/` (menos
  `CenaDaCasa`, `CenaDaArea` e `BichosDaCasa`, que pedem APIs só do Android e não foram mexidos), `ui/tema` e
  `dominio/`; e o `CapturasJogo3DTest` (stubs de Robolectric e Roborazzi). As capturas não rodam aqui (sem SDK do
  Android): quem prova o visual é o CI.

## Riscos e pendências

* Olhar no CI: o tamanho dos patinhos e da lagoa (a cena encolhe para caber entre o balão e as respostas; em
  360 x 640 fica menor), o encontro da ilha com as respostas, e se o balão com o ícone em linha fica bem alinhado.
* A ilha fica no fundo da tela inteira, com o centro a 43% da altura; as figuras ficam na caixa da pergunta (não
  dependem de coordenadas globais). Em tela deitada a ilha fica do tamanho da altura, centrada.
* Arte que falta (pedir à fase 4A, segundo lote): os 312 emojis sem modelo (lista na fase4a), começando por formas
  da Lógica (🟦🟩🟪🟨🟥🔺🔷), bichos do mar (🐬🐙🦀🐳🦈🦐), peru, zebra, girafa, cobra, cabra, coruja, papagaio, jacaré;
  uma versão "silhueta" das figuras sem a sombra de contato (para `Visual.Sombra`); um ícone 3D de "certo" e
  "errado" para o selo das respostas; e cenas para mais figuras (o varal só tem 5 vagas).
* Não foi feito: figuras 3D dentro do quebra-cabeça (a figura é fatiada em peças), a mãozinha 👆 e os ícones 🔁 💡
  🧽 (ficam para a Fase 8, com a arte da barra).

## Commit

Código, testes e este relatório: ver `git log -1` na branch `worktree-agent-a43e99d1facc7c943`.
