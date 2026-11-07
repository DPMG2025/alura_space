# Fase 8, parte B, grupo C: casa de fora, áreas novas e as 17 atividades novas (Kotlin)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a4b531b7337d8f584`, branch
`worktree-agent-a4b531b7337d8f584`, zerada antes em `ccr-162c6a70-urxpmq` (`faf7693`). Commit local, sem push; o hash
está na mensagem de entrega. Sem mudança em domínio, narração, workflow, `arte3d/`, `PecasDaInterface.kt` nem nas
telas dos outros grupos. Nenhuma arte nova: só a que já está no manifesto.

## Casa de fora (`TelaCasa.kt`)

* Fundo em céu de brinquedo (azul, clarinho, pêssego e rosa, como a tela inicial).
* Cabeçalho: título branco com contorno (`TextoComContorno`) que **diminui para caber** numa linha em 360 dp
  (`TituloQueCabe`); na página de uma área, o título é o nome da área. Saiu o "x de 40 coisas" e o rótulo "3/44":
  o progresso são **5 estrelas 3D** (`ProgressoVisivel.estrelasAcesas`), com o número só na descrição de
  acessibilidade ("A casa tem N de 40 coisas" / `Progresso.rotulo`). Sol, lua, pintar e espelho já saem 3D pelo
  `BotaoRedondo` (IconesDaBarra); o botão da noite ficou lavanda claro para o glifo roxo ler.
* Botões: "Jogar" com o triângulo 3D num círculo branco e a palavra com contorno escuro (sem 🎮); "Entrar na casa"
  com `icone_casa` + `icone_avancar` (sem 🚪👉); atalhos da coluna deitada com ícones 3D; dica de pintar com pincel,
  mãozinha (`ui_mao_toque`) e casa 3D; cadeado, troféu (`premio_trofeu`) e casinha 3D no baú vazio.
* Abas do baú como pílulas de brinquedo: `premio_presente_fechado` + quantos presentes; `icone_loja` + `moeda` +
  moedas. Preço dos cartões: moeda 3D e o número em Baloo 2 (mãozinha 3D na prévia).
* **Loja de fora com chapéus 3D**: o cartão do chapéu mostra a criança do perfil com o chapéu (camadas da Fase 7,
  `miniaturaVestindo`, miniatura parada); visitantes e climas usam a figura 3D do emoji quando há (`FiguraDaPergunta`).
* Lugares da casa (`lugar_*`) como pílulas: casa 3D, ou o ícone da área + 3 estrelinhas; a próxima área trancada com
  o cadeado 3D. Barra lateral da casa: pílula com a casinha e 5 estrelas. Aviso do presente do evento e a
  prateleira de presentes usam a figura 3D quando há.
* Preservados (grep): `lugares_da_casa`, `lugar_casa`, `lugar_<id>`, `mascote_na_casa`, "Entrar na casa", "Jogar",
  "Virar dia/noite", "Pintar as paredes", "Espelho: trocar a aparência", "Baú de presentes, ...", "Lojinha, você
  tem N moedinhas", "<item>. Toque para colocar na casa.", "Próximo presente: ...", o texto "Minha casinha" que o
  teste do emulador espera.

## Áreas novas (`CenaDaArea.kt`, `FundosDasAreas.kt`, `ComodosNovos.kt`, novo `ArteDasAreas.kt`)

* Fundo de fora: céu da maquete (azul para pêssego), grama lisa da arte 3D (`A9E56F`) com luz no alto, **beirada da
  ilha** (grama em bolinhas sobre a terra de massinha, rente ao pé), vinheta mais leve; de dia, as **nuvens 3D**
  `ceu_nuvem_1/2` passeiam por cima (as desenhadas ficam de reserva). Cômodos: base de madeira de casinha de bonecas.
* Coisas das áreas em 3D (`ArteDasAreas.para`, Kotlin puro): cachorro e gato = `item_cachorro`/`item_gato` (viram
  para onde andam, pelo `ItemDaArte`); mesmo tipo e mesmo emoji de uma peça da casa = `item_<tipo>` (cama, ursinho,
  pato, trem, pipa, bola...); senão a figura das perguntas pelo emoji (`fig_*`: maçã, coelho, abelha, estrela...).
  Sem arte, o emoji/desenho antigo (reserva também se a imagem não abrir). O baú e o cômodo novo usam o mesmo
  (`IconeDaCoisa`). Arrastar, guardar no baú, profundidade, passeio e obra iguais.
* Cômodo novo aberto: céu de brinquedo, nome com contorno que cabe, 5 estrelas no lugar de "x de y coisas"
  (descrição mantida); portinhas com seta e figura 3D. `comodo_novo_*`, `comodo_aberto_*`, `area_*` mantidas.
* Visitantes do jardim (`BichosDaCasa.VisitanteAndando`): figura 3D quando há (sem espelhar, para a luz não trocar).

## As 17 atividades novas (`atividades/novas/`) e `atividades/EstiloDoJogo.kt`

* `ladrilho` (atividades/, usado por Ditado, Dominó, Ligar pares, Montar a frase e pelas antigas) agora é bloco de
  brinquedo: degradê, brilho em elipse, borda branca por dentro, lábio colorido (mesma assinatura e layout).
* Botões redondos de ouvir (ditado, escrever, montar a frase): `BotaoDeOuvirN4` com `faceDeBrinquedo`. Robô: setas,
  play/apagar e a fila em pílula. Siga a sequência: **`ContasDeProgresso`** no lugar das bolinhas, botão de repetir
  com `icone_repetir`, mãozinha 3D. Cartas viradas: face de brinquedo. Escrever: dica com face amarela, borracha 3D.
* Barra de achados (pescaria, balões, caça na cena): pílula com contas peroladas 3D (`conta_vazia`) nos lugares que
  faltam. Fontes: números e letras em Baloo 2 (balança, mercadinho, relógio, pintar por código, figuras de texto).
  Céus da caça, balões e esconde-esconde no azul/pêssego e grama da arte. Figuras continuam por `FiguraDaPergunta`.
* Lógica, testTags (`peixe_certo`, `balao_certo_k`, `seq_repetir`, `seq_botao_i`, `robo_*`, `ditado_*`,
  `escrever_*`, `virada_ordem_k`, `barra_achados` etc.) e descrições sem mudança.

## Testes e capturas

* **Novo** `ArteDasAreasTest` (JUnit puro, 4 testes contra o manifesto real).
* **Novo** `CapturasAreasNovas3DTest`: `v3d_18_areas_novas` (pet, horta, parquinho e quarto novo; antes confere que
  há 3D em cada área e as nuvens no manifesto) e `v3d_18b_casa_na_area_do_pet` (título da área, sem "/" nem "coisas").
* `CapturasCasaQueCresceTest.lugaresDaCasa` **adaptado**: procurava o texto "Quarto novo 3/44"; agora confere o
  nome da área como texto e o rótulo na descrição (pílula do lugar e estrelas do cabeçalho).
* Mudam sozinhas: 12 a 14b, 18, 20, 20b, s8_*, as do baú/loja e as das atividades novas (`n1_*`..`n4_*`, e2x do
  emulador). Nenhuma renomeada. Chuva só fora de casa e arrastar na área: código da cena da casa intacto; o
  arrasto da área continua no mesmo `ObjetoNaCena` (a boia não tem arte, segue emoji).

## Verificação feita aqui

* JVM puro (Gradle temporário `--offline`, Kotlin 2.0.21): `ArteDasAreasTest` 4/4 e `FigurasDaPerguntaTest` 6/6.
* Compilação contra Compose Multiplatform desktop 1.7.3 (API do BOM 2024.12.01) com stubs de Android/áudio:
  todo `ui/componentes`, `dominio`, `TelaCasa`, `ComodosNovos`, `TelaCasaPorDentro` e **todo `ui/telas/atividades`**
  sem erro (só os 2 avisos antigos do robô); os testes `CapturasAreasNovas3DTest` e `CapturasCasaQueCresceTest`
  compilam com stubs de Robolectric/Roborazzi. Quem prova o visual é o CI.

## Memória

* Nada em estático além do cache da `ArteDoApp`; tudo por `ImagemDeArte`/`ItemDaArte` (amostra no tamanho de uso).
  Uma área mostra até ~45 coisas: as 3D são `fig_*` de 192 px e `item_*` de 320 a 400 px decodificados reduzidos;
  as duas nuvens são as da casa. Os chapéus da loja são 4 miniaturas paradas no cache de compostos (Fase 7).

## Pendências

* Arte própria das áreas (miolos: piscina, casinha do cachorro, tanque de areia... seguem desenhados em Canvas) e
  das ~80% de coisas sem `fig_*` (boias, guarda-sol, blocos...).
* O cachorrinho 3D é um só: os cachorros da área do pet perderam a pelagem diferente (o desenho antigo fica de
  reserva). O 👋, 🧒, 👀, 🔨, 🚧 e o "?" ainda são emoji/texto.
* Riscos para o CI: o tamanho do título em 360 dp com nomes longos (encolhe até 16 sp), a beirada da ilha com coisas
  postas no pé da cena (y ~0,99) e a leitura do glifo roxo nos botões coloridos.
