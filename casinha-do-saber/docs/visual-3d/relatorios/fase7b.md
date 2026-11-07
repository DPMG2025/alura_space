# Fase 7, parte B: guarda-roupa em camadas 3D no app (Kotlin)

Data: 2026-11-07. Commit local, sem push.
Worktree: `/home/user/alura_space/.claude/worktrees/agent-a58e7642880f49fa4`, branch
`worktree-agent-a58e7642880f49fa4` (zerada antes em `ccr-162c6a70-urxpmq`, `f824f97`, com toda a arte integrada).
Commit do trabalho: `c1be7b4` (este relatório recebeu o hash num commit seguinte, só de documentação).

## O que mudou

* `ui/componentes/CamadasDosPersonagens.kt` (Kotlin puro):
  * `idsDaCrianca(manifesto, perfil, quadro)` monta, além do corpo de sempre, o traje, os acessórios e o chapéu,
    como `camadasComGuardaRoupa` de `arte3d/previa_fase7.js`: traje `roupa_<t>` ou, se não existir,
    `roupa_<t>_menina|_menino` (gênero do perfil); `_estampa`, `_tras`, `_pes`, `_mangas_<pose dos braços>` e
    `_tapaolho` quando existem no manifesto; `_cabeca` só sem chapéu. Acessórios: um por parte; MAO usa `_cima`
    torcendo e `_baixo` nas outras poses; COSTAS acrescenta `_frente` se existir. Chapéu: o emoji do `Perfil.chapeu`
    vira a camada (`CAMADAS_DOS_CHAPEUS`: 👑 coroa, 🧢 boné, 🎩 cartola, 🌸 flor).
  * `empilhar` aplica o `esconde` do manifesto: cada camada esconde os grupos listados nas camadas das **outras
    peças** (`pecaDe`: `crianca`, `irmao`, `roupa_<t>`, `acessorio_<a>`, `chapeu_<c>`); depois ordena pela `ordem`.
  * Novos: `camadas(manifesto, personagem, perfil, quadro)`, `camadaDoChapeu`, `chapeuEmEmoji`, `pecaDe`.
    `ids(...)` ganhou o manifesto como primeiro parâmetro. `usaCamadas` saiu (não há mais caso "sem camadas").
  * `disponivel` confere os 112 quadros com a roupa do perfil; se faltar uma camada obrigatória, dá falso e a
    `Crianca` usa o desenho antigo (só como reserva).
* `PersonagemEmCamadas.kt`: `podeUsar` não recusa mais traje nem acessório. O cache de compostos ficou entre 4 MB
  e **24 MB** (antes 1/16 da memória sem teto). Continua um bitmap por combinação (camadas + cores + tamanho).
* `Crianca.kt`: mesma assinatura. Os 4 chapéus da loja entram na pilha (luz 3D, sombra macia); o
  `ChapeuNaCabeca` com emoji pela âncora `cabeca`/`topoDaCabeca` do manifesto só fica para um emoji sem camada.
* `Provador.kt` (espelho e guarda-roupa): todas as gavetas mostram a criança vestindo a peça (`animado = false`,
  58 dp), com o ícone pequeno no canto (a mochila quase não aparece de frente); "Tirar" mostra a criança sem a peça
  com ✖. Novo `miniaturaVestindo(perfil, produto)` (na roupa, sem chapéu, para a cabeça do traje aparecer).
* `TelaCasaPorDentro.kt > Lojinha`: roupas, acessórios e chapéus aparecem na criança (miniatura parada, 52 dp,
  `LazyRow`), com o ícone pequeno ao lado. A cena da casa por dentro continua com o desenho antigo (Fase 6).
* Sem mudança: domínio, regras, narração, workflow, arte3d, TelaInicio/TelaCasa/TelaPremio (a `TelaPremio` já usa
  `Crianca(perfil.vestindo(p), animado = false)`, então o brinde de roupa ou chapéu sai em camadas sozinho).

## Testes

* `CamadasDosPersonagensTest` (JUnit puro, contra o `manifesto.json` real): 16 testes. Adaptados os antigos à
  assinatura nova; o de "traje fica no desenho antigo" foi trocado. Novos: todos os trajes x 5 chapéus (com e sem) x
  2 gêneros, os 18 acessórios e 3 crianças com um de cada parte + traje + chapéu, em todos os 112 quadros (camadas
  no manifesto, em ordem, mesmo quadro, uma por grupo, nada escondido sobrando); traje e gênero; mangas por pose e
  cabeça do traje x chapéu; tapa-olho x óculos; acessórios por parte (mão cima/baixo, costas frente/trás, pés
  tiram o sapato, tiara tira o laço, desconhecido ignorado); chapéus da loja = camadas, emoji estranho fica de fora;
  `pecaDe`; camada faltando cai no desenho antigo.
* `CapturasTest.chapeuFicaNaCabeca` (captura 56) **adaptado**: confere que a criança vestida usa camadas; o "não
  cobre o rosto" usa só a diferença forte (corpo do chapéu, não a sombra macia) e o limite é a linha dos olhos
  (âncora `olhoEsquerdo`); a flor é de lado (fica dentro da largura da cabeça em vez de centrada); as outras regras
  continuam. Ganhou duas colunas: **macacão** (entre a boca e os pés, centrado) e **óculos** (cobre os dois olhos, na
  linha dos olhos, centrado). Conferido antes numa simulação em Python (PIL) da composição no mesmo tamanho
  (170 x 380 px): todas as regras passam; a mais justa é o boné (corpo a 3 px acima da linha dos olhos).
* `CapturasArte3DTest`: a criança de listras e a de pirata + cartola + balão usam camadas.
* Capturas novas (`CapturasGuardaRoupa3DTest`, sem renomear nenhuma): `v3d_08_guarda_roupa` (16 trajes),
  `v3d_09_acessorios` (18 + 3 combinações, uma torcendo), `v3d_10_chapeus` (4 chapéus em meninas e meninos e
  4 chapéus sobre trajes com cabeça). Antes de capturar, confere que a peça muda o composto (mínimos medidos na
  simulação: gravata ~9 pixels de amostra, por isso o mínimo dos acessórios é 4).
* Capturas 18, 19, 56, 58, 59 e 60 não precisaram de código novo: usam `Crianca`, que agora monta tudo por camadas.

## Verificação feita aqui

* JVM puro (Gradle 8.14 temporário, Kotlin 2.0.21, `--offline`, `workingDir` = `app/`): `CamadasDosPersonagensTest`
  16/16 e `ManifestoDaArteTest` 6/6 verdes.
* Compose: `ManifestoDaArte`, `CamadasDosPersonagens`, `PersonagemEmCamadas`, `Crianca`, `Irmaozinho`,
  `RostoDaCrianca`, `ImagemDeArte`, `Fundos`, `Provador`, todo o `dominio/`, o `CapturasGuardaRoupa3DTest` e um
  excerto com o `chapeuFicaNaCabeca` inteiro compilaram sem erro nem aviso contra Compose Multiplatform desktop 1.7.3
  (mesma API do BOM 2024.12.01) com stubs mínimos de Android/Robolectric. `TelaCasaPorDentro`, `CapturasTest` e
  `CapturasArte3DTest` inteiros não (dependências demais); a parte mudada foi revisada à mão. Quem prova é o CI.
* Prévia em Python dos 16 trajes, 18 acessórios e 4 chapéus sobre traje com a mesma escolha de camadas: tudo no lugar.

## Memória

* Um composto por combinação, em cache LRU de no máximo 24 MB; miniatura de 58 dp ~35 KB; as camadas soltas vêm
  do cache da `ArteDoApp` decodificadas com `inSampleSize` 4 nas miniaturas (~44 KB cada).
* A composição só acontece numa falta do cache (nunca por quadro de animação). As miniaturas são paradas.
* O provador é um `Row` com rolagem (não preguiçoso): ao abrir a gaveta de roupas compõe 17 miniaturas de uma vez
  (uns 60 a 70 bitmaps pequenos decodificados na primeira vez). Se o emulador ainda sofrer, trocar por `LazyRow`.

## Pendências de arte (para a 7A ou a Fase 9)

* Cabelo espetado e cacheado passam por cima do boné e do capacete (conhecido).
* A aba do boné desce até o alto dos olhos e faz sombra no rosto: o teste passa por 3 px; uma aba um pouco mais alta
  daria folga.
* A mochila de frente quase só mostra as alças; a gravata borboleta é muito pequena nas miniaturas.
* A flor fica de lado na cabeça (não é chapéu de cima): o teste trata como enfeite de cabelo.
* Na loja de fora (`TelaCasa`, captura 18) os chapéus continuam como ícone emoji no cartão (tela fora do escopo).
* A cena da casa por dentro e o desenho antigo (reserva) ainda desenham o chapéu em emoji.
