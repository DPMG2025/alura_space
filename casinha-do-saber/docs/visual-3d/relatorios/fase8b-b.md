# Fase 8, parte B, grupo B: trilha, mapa, nível, relembrar, livrinhos, história, ateliê e piano (Kotlin)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a2e33bf3d36cc727d`, branch
`worktree-agent-a2e33bf3d36cc727d`, zerada em `ccr-162c6a70-urxpmq` (`faf7693`). Commit local, sem push (hash no fim).
Domínio, narração, workflow, `arte3d/`, `PecasDaInterface.kt` e telas de outros grupos não foram tocados. Usa só a
arte que já está no manifesto (Fases 2, 4, 5 e 8A); nenhuma imagem nova.

## Arquivos

* **Novo** `ui/componentes/ArteLeve.kt`: `lembrarArteLeve(id, lado)` (ícone pequeno como `ImageBitmap`, pelo LRU da
  `ArteDoApp`), `DrawScope.desenharArte`, `FileiraDeArteLeve` (vários ícones num Canvas só), `EstrelasLeves`,
  `ContasLeves`, `Modifier.ceuDeBrinquedo(comGrama)` (o céu das maquetes com três nuvens e, se pedido, os morrinhos
  de grama da ilha, tudo em vetor e parado), `CeuDeBrinquedo`, `GramaDaIlha`.
* `TelaTrilha.kt` (mesma assinatura): céu de brinquedo com a grama da ilha embaixo (os botões de baixo ficam na
  grama); título com `TextoComContorno` que diminui até caber (`TituloQueCabe`, medido com `TextMeasurer`, sem
  `hasVisualOverflow`); ícone 3D do caminho (sem o botão do mapa, como antes); pílula do próximo prêmio com o
  presente 3D (`premio_presente_fechado`) e o item 3D (`item_<id>`, emoji de reserva); **o "x/250" virou cinco
  estrelas numa pílula** (`ProgressoVisivel.estrelasAcesas`, o número fica só na descrição). Faixas dos mundos como
  tapetes de brinquedo (lábio e borda branca); número da fase em Baloo 2; selo de vencida com borda branca;
  cadeado 3D (`icone_pais`) nas fechadas; troféu 3D (`premio_trofeu`) no marco vencido (🚩 continua: sem arte);
  estrelas das plaquinhas pelo `EstrelasLeves`. Novos `internal`: `IconeLeve` e `TituloQueCabe` (usados também
  pelas outras telas do grupo).
* `TelaMapa.kt`: céu parado; ícone do caminho e "Mapa: Números" com contorno; as medalhas numa pilulazinha
  (`tela_medalha` + número de medalhas; "x de 25 mundos completos" só na descrição); ilhas como discos de
  brinquedo (lábio, brilho, sem blur); coroa 3D (`premio_coroa`), cadeado 3D, medalha 3D; **as 10 fases de cada
  mundo em contas 3D** (`ContasLeves`, cor por caminho). Descrições e `testTag("mundo_N")` iguais.
* `TelaRelembrar.kt` (`ConviteDaCorujinha`, usado também pelo nível): céu com grama, título com contorno (ícone ao
  lado via novo parâmetro `icone`), balão do sistema (`balaoDeFala`, ponta embaixo), destaque em pílula
  (`PilulaDeValor("moeda", "+5")`, estrela 3D, "🌷 Mundo 3"; novo parâmetro `destaqueIcone`). Títulos sem o 🦉.
* `TelaDescobrirNivel.kt`: convite e resultado com o ícone 3D do caminho ao lado do título (sem o emoji no texto).
* `TelaLivrinhos.kt`: estante com céu, `tela_livro_fechado` no cabeçalho, título com contorno e **estrelas no lugar
  de "3 de N livrinhos lidos"**; marcadores em cartão; cadeado 3D nos livros fechados. Livrinho aberto:
  `tela_livro_aberto` ao lado do título, **páginas em contas** ("2/5" saiu; "Página 2 de 5" na descrição),
  palavras num cartão com lábio, setas e som 3D nos botões (`icone_voltar`, `icone_avancar`, `icone_som_ligado`),
  pergunta num balão com o som 3D em linha (texto alternativo "🔊", mesma semântica), respostas com
  `FiguraDaPergunta` (3D quando há modelo), fim com o marcador saindo do livro aberto 3D e a estante 3D no botão.
* `CenaDoLivrinho.kt`: moldura com lábio (sem sombra de elevação), figuras 3D quando há modelo (emoji com a
  sombrinha como reserva).
* `CenaDaHistoria.kt`: céu, título com contorno na cor do mundo, balão do sistema (saiu a ponta desenhada à mão),
  pratinho do amigo como disco de brinquedo com a figura 3D, nome em pílula, "Vamos lá!" com contorno.
* `CenasDesenhadas.kt`: material dourado e labirinto em cartões de brinquedo (lábio). Desenho igual.
* `TelaAtelie.kt`: céu, `tela_paleta` no título, ferramentas com `tela_lapis`/`tela_balde`/`tela_borracha`
  (🧹 continua: sem arte), cores da paleta como gotinhas de plástico (brilho e sombra interna), quadro e miniaturas
  com sombra desenhada, `tela_pincel` no aviso da galeria vazia, 🗑️ com `icone_lixeira` (🖼️ continua).
* `TelaPiano.kt`: céu, `tela_piano` no título, notinhas que sobem das teclas com `tela_nota_musical` (a desenhada
  fica de reserva), estrela 3D na tecla da vez. **As teclas continuam desenhadas** (esticam com a tela).
* `Comuns.kt` (fora da lista de `ui/telas`, mas é onde mora o 👆): `MaoGuia` e `MaoQueArrasta` usam
  `ui_mao_toque`/`ui_mao_toque_apertando` com a ponta do dedo no mesmo ponto (20 dp, âncora `ponta`), 👆 de
  reserva. As telas do grupo não tinham 👆 próprio. **Pode encostar no que o grupo A fizer em `Comuns.kt`**: a
  mudança é só nessas duas funções e numa nova `MaoDeArte` privada.
* `CartoesDasVozes.kt`: nada a mudar; os botões já são `BotaoRedondo` e o `IconesDaBarra` troca ⏺️ ⏹️ ▶️ 🗑️ sozinho.

Preservado (conferido no diff): todas as `testTag` e `contentDescription` ("Caminho…" é da TelaInicio; "Mapa dos
mundos", "Álbum de figurinhas", "Voltar", "Voltar para o caminho", "Mundo 1, Jardim, completo", "Fase N, …",
`abrir_nivel`, `nivel_agora_nao`, `nivel_comecar`, `abrir_relembrar`, `abrir_estante`, `livrinho_*`, `palavra_*`,
`pagina_*`, `resposta*`, `quadro_de_pintar`, `cor_*`, `ferramenta_*`, `tecla_*`, "Voltar para a casa", "Você
tocou"). Assinaturas públicas iguais (`ConviteDaCorujinha` só ganhou parâmetros com padrão).

## Memória (o emulador cai perto de abrirMapaEAlbum)

* **Sem animação de fundo**: trilha e mapa usavam `FundoDecorado` (bolhas e estrelinhas numa transição infinita,
  redesenhando uma camada da tela inteira a cada quadro). Agora `ceuDeBrinquedo`, montado uma vez por tamanho.
* **Sem bitmap grande**: a ilha do jogo (800 x 1040) não entrou no fundo; o céu e a grama são vetor. Nenhum campo
  estático guarda bitmap; tudo passa pelo LRU da `ArteDoApp`.
* **Menos subcomposições**: cada `ImagemDeArte` é um `BoxWithConstraints`. Antes: até 60 na trilha (3 estrelas por
  plaquinha). Agora: uma fileira de estrelas ou contas é **um Canvas** que desenha bitmaps divididos
  (`FileiraDeArteLeve`); o mapa tem 25 Canvas de contas (250 contas, 2 bitmaps de 64 px) em vez de 250 `Box`.
  Ícones repetidos (cadeado, coroa, medalha, troféu, notinha das 8 teclas) são `IconeLeve`: um bitmap pequeno
  (amostrado para o tamanho de uso) desenhado num Canvas. `ImagemDeArte` ficou só para 1 a 4 ícones de cabeçalho.
* **Sem sombra de elevação** (`Modifier.shadow`, que vira camada com blur): saíram do convite da Corujinha, da
  história (2), da estante e do livrinho (3), da cena do livrinho, do quadro do ateliê (10 dp, o maior) e das
  miniaturas. No lugar, formas deslocadas desenhadas (lábio e sombra), como `cartaoFofo`/`balaoDeFala`.
* Sugestão para a Fase 9: um `ImagemDeArte` sem `BoxWithConstraints` quando o tamanho é fixo, e conferir o heap
  nativo no `DiagnosticoTelasTest` com uma tela de caminho e uma de mapa.

## Capturas

* **Novo** `CapturasTelas3DTest` (Robolectric, 1800 x 720 dp hdpi, cinco celulares de 360 x 720 lado a lado, sem
  escala de tablet): `v3d_16_trilha_e_mapa` (caminho com mapa e estrelas, caminho novo com "Já sei bastante!" e o
  presente, mapa, convite do nível, caminho de Inglês todo vencido) e `v3d_17_atelie_piano_livros` (ateliê com
  desenho, piano seguindo a música, estante, página e pergunta do livrinho). Antes de capturar confere a arte no
  manifesto e decodificável, as descrições e marcas dos testes do emulador, "250 de 250 fases vencidas", "Página 2
  de 5", e que **nenhum texto passa da borda do seu celular** (fora de fileira que rola de lado) e **nenhum texto
  tem "n/m"**.
* Mudam sozinhas: 04, 40, 41, 51, 61, s5_*, s6_caminho_com_mapa, s6_mapa_*, s10_02 a s10_15, e as que mostram a
  mãozinha (32b e as de atividades com `MaoGuia`). Nenhuma captura foi renomeada.

## Verificação feita aqui

* Compilação sem erro contra Compose Multiplatform desktop 1.7.3 (API do BOM 2024.12.01), Kotlin 2.0.21, Gradle
  temporário `--offline` (`scratchpad/f8bb`, stubs de `android.*`, áudio, `LocalContext`, `LocalConfiguration`,
  `R.font`, `BackHandler`, `CartaoDosPais` e `SonsDoPiano`): as 10 telas do grupo, `Ambiente.kt`, `Moldura.kt`,
  todo o `ui/componentes/` (menos `CenaDaCasa`, `CenaDaArea`, `BichosDaCasa`, intocados), `ui/tema`,
  `ui/telas/atividades` e `dominio/`; e o `CapturasTelas3DTest` (stubs de Robolectric e Roborazzi).
  `CartoesDasVozes.kt` não mudou. As capturas não rodam aqui (sem SDK): quem prova o visual é o CI.

## Riscos e pendências

* Olhar no CI: o título com contorno encolhido em 360 dp ("Mapa: Números"), a pílula do prêmio com item sem arte
  3D (cai no emoji), o tamanho das contas no mapa (13 dp) e a mãozinha 3D na posição do dedo.
* Ainda emoji (sem arte): 🚩 do marco, 📍 do mapa, 🏋️ do treinar, 🧭 do nível, 🧹 limpar, 🖼️ pendurar, 🔖, 🎵 e os
  emojis das músicas, os emojis dos mundos e das fases (conteúdo).
* `CartaoDoRitmo` (área dos pais) e `CartoesDasVozes` ficaram como estavam.

## Commit

Código, teste e este relatório: `2b533f9` (branch `worktree-agent-a2e33bf3d36cc727d`, sem push); o hash entrou neste relatório num segundo commit.
