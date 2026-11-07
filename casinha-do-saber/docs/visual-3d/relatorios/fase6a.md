# Fase 6, parte A: casa por dentro na projeção isométrica (arte)

Data: 2026-11-07. Só a arte: sem Kotlin e sem testes. Branch `worktree-agent-a9f31e3db30f6e1b5` (worktree
`.claude/worktrees/agent-a9f31e3db30f6e1b5`), a partir de `210d507` (3A e 3B). Commit da arte: `bcd61dd` (este arquivo ganhou o hash num commit seguinte).

## O que foi feito

* `arte3d/src/cameras.js`: tipo de câmera **`isometrico`** (ortográfica) e `ISO` (ângulos e escala da altura).
* `arte3d/src/pagina.js`: enquadramento `isometrico` (recorte justo em pixels inteiros a partir do apoio), sombra de
  contato retangular por pegada e carga do catálogo por fase (`render.html?catalogo=fase6`).
* `arte3d/render.js`: `CATALOGO=fase6 npm run render` usa `src/catalogo_fase6.js` e grava **`manifesto_fase6.json`**
  (render incremental: nunca apaga imagem de outro manifesto; o render completo do catálogo principal também passa a
  considerar válidas as imagens dos `manifesto_<fase>.json`). Campo novo por imagem: `iso` (abaixo).
* `arte3d/src/catalogo_fase6.js` (novo) e `arte3d/src/objetos/casaPorDentro.js` (novo): móveis, enfeites e cascas dos
  cômodos em coordenadas da planta (`PlantaDaCasa`). Os prêmios reusam os modelos da casa de fora (`itensDaCasa.js`)
  encaixados na pegada da planta; os fixos (escadas, guarda-roupa, pia, vaso, lixeira, criado-mudo, tapete) e as
  versões com porta (geladeira) ou sem patinho (banheira) são novos.
* `arte3d/previa_dentro.js` (novo, `npm run previa:dentro`): monta os dois andares como o app deve montar e grava em
  `arte3d/saida/` (não versionado) `previa_dentro.png`, `previa_dentro_grade.png` (grade do piso e caixas de oclusão
  por cima), `previa_dentro_noite.png`, `previa_dentro_sala.png` e `previa_dentro_pecas.png` (variantes, com o apoio
  marcado).
* `catalogo.js` e o `manifesto.json` principal **não** foram tocados.

## A câmera (igual a `ui/componentes/Isometrico.kt`)

`Projecao`: `x = origemX + (gx - gy) * ladrilho / 2`, `y = origemY + (gx + gy) * ladrilho / 4 - z * 0,56 * ladrilho`.
Isso é uma câmera ortográfica com azimute 45 graus e elevação 30 graus (sen 30 = 1/2: o losango do ladrilho tem metade
da altura da largura), com a altura achatada por `0,56 / (cos 30 / raiz 2) = 0,9145` para a unidade z medir
`0,56 x ladrilho`, como no app. Mundo do pipeline = planta: x = x, z do mundo = y da planta, y do mundo = z da planta.

* **Ladrilho das imagens: 64 dp = 128 px** (escala 2 do manifesto). Uma célula do piso tem 128 x 64 px em todas as
  imagens. Uma unidade de altura = 71,68 px.
* No app: `k = Projecao.ladrilho / (iso.ladrilho * escala)` (ladrilho da tela em pixels dividido por 128). Desenhar a
  imagem com o canto em `Projecao.ponto(ref.x, ref.y, ref.z) - apoio * k`, com tamanho `(largura, altura) * k`.
* O recorte é calculado em pixels inteiros a partir do apoio: com `k = 1` a grade cai no pixel exato
  (`previa_dentro_grade.png`: a grade e as caixas de `PlantaDaCasa.caixaDo` batem com os desenhos).

## Manifesto (`app/src/main/assets/arte/manifesto_fase6.json`)

Campos de sempre (`arquivo`, `tipo: "isometrico"`, `largura`, `altura`, `uso` inteiros, `apoio`, `ancoras`) e `iso`:

* `ladrilho: 64`, `unidadeZ: 0.56`, `referencia: {x, y, z}` = o ponto da planta que cai no apoio.
* móveis: `movel` (id da planta), `comodo`, `frente` (`Y`, ou `X` no guarda-roupa), `estado` (`normal`, `aberta`,
  `aberto`), `pegada: {x0, y0, x1, y1, z, altura}` (o lugar de sempre). **A imagem só depende do tamanho da pegada**
  (a câmera é ortográfica): um móvel arrastado usa a mesma imagem, com a referência no centro da pegada nova.
* cascas: `peca` (`laje`, `piso`, `parede`, `acabamento`, `paredeInterna`, `janela`, `porta`, `lampada`), `andar`,
  `comodo`, `tingir` e `ordem` (de trás para a frente).

## Ids e onde ficam

Móveis e enfeites (referência = centro da base da pegada, `(centroX, centroY, z)`, o mesmo `ancora` de `montarPeca`;
quadro e espelho: `(centroX, 0, z)`, na parede):

| id | móvel da planta | âncoras |
|---|---|---|
| `dentro_escada`, `dentro_escada_de_cima` | escada (sobe) e escadaDeCima (vão com corrimão) | |
| `dentro_sofa`, `dentro_gato`, `dentro_livros`, `dentro_tv`, `dentro_tapete` | sala | |
| `dentro_mesa`, `dentro_bolo`, `dentro_lixeira`, `dentro_fogao` | cozinha | |
| `dentro_geladeira`, `dentro_geladeira_aberta` | geladeira (aberta: prateleiras; a porta abre para a ESQUERDA, dobradiça em x0: na dobradiça x1 do desenho atual ela taparia o interior nesta câmera) | `comida_0..8` (aberta) |
| `dentro_quadro`, `dentro_cama`, `dentro_ursinho`, `dentro_criado`, `dentro_abajur`, `dentro_violao` | quarto | |
| `dentro_guardaroupa`, `dentro_guardaroupa_aberto` | guarda-roupa, frente X | `cabide_0..5` (aberto) |
| `dentro_planta`, `dentro_banheira`, `dentro_vaso`, `dentro_pia`, `dentro_espelho`, `dentro_patinho` | banheiro | banheira `agua`; pia `bico`, `bacia` |

Cascas (referência = canto `(0, 0, 0)` do andar, `Projecao.ponto(0, 0)`; desenhar nesta ordem, antes dos móveis):

| ordem | id | observação |
|---|---|---|
| 0 | `dentro_laje_de_baixo`, `dentro_laje_de_cima` | espessura do piso (0,35) |
| 1 | `dentro_piso_{sala,cozinha,quarto,banheiro}` | tábuas (sala, quarto) e lajotas 1 x 1 (cozinha, banheiro) |
| 2 | `dentro_parede_{sala,cozinha,quarto,banheiro}` | **branca: tingir** multiplicando por `EstadoJogo.corDo(comodo)`; rodapé, azulejos; o da esquerda leva a parede x = 0 |
| 2,5 | `dentro_janela` (ref. `(meio da janela, 0, 1,32)`), `dentro_janela_redonda` (ref. `(4,975, 0, 1,55)`), `_noite` de cada | janelas do app: de cima 3,22; de baixo 2,1 e 6,05 |
| 2,5 | `dentro_porta_{0..4}` (índice = `corPorta`) | ref. `(0, 3,95, 0)`, na parede x = 0 da sala |
| 3 | `dentro_acabamento_de_baixo`, `dentro_acabamento_de_cima` | topo das paredes, colunas das pontas e a divisória do meio |

Peças soltas: `dentro_parede_interna` (um ladrilho; ref. `(meio, y + 0,5, 0)` para y = 0, 1, 2; entra na ordem do
pintor com os móveis, como hoje) e `dentro_lampada_acesa` / `dentro_lampada_apagada` (ref. = a lâmpada,
`(centro, 2,62)` ou, no banheiro, `(7,6; 2,0; 2,85)`; o fio sobe 0,88).

## Oclusão (`OcultacaoNaCasaTest`, `PlantaDaCasa.ordenarParaDesenho`)

A ordem não muda: o app continua ordenando as `Caixa` da planta e só troca as formas pela imagem. Os modelos foram
encaixados na pegada de cada móvel (largura e fundo exatos; altura perto da do desenho atual), então as caixas de
oclusão continuam valendo: ver `previa_dentro_grade.png`. Passam um pouco da caixa para cima só o que já passava no
desenho vetorial (cabeceira da cama, antenas da TV, panela do fogão, encosto do sofá), sempre encostados na parede
do fundo, onde nada fica atrás.

## Pendências para a parte B (Kotlin) e para o integrador

* **Integrar o manifesto:** juntar `manifesto_fase6.json` ao `manifesto.json` (ou ensinar o `ManifestoDaArte` a ler os
  dois). Até lá o `ManifestoDaArteTest.nenhumArquivoOrfao` acusa os `dentro_*.webp` (esperado nesta branch).
* Ficam vetoriais no app: papel de parede (listras, bolinhas, estrelinhas), telhado visto por dentro, brilho das
  lâmpadas e do abajur, tela animada da TV, comidas da geladeira (âncoras `comida_*`), roupas do guarda-roupa
  (âncoras `cabide_*`), setinhas da escada, realce de arraste.
* Cômodo apagado: escurecer o piso e a parede do cômodo (as imagens são separadas por cômodo).
* Áreas novas (`ComodosNovos`, `CenaDaArea`) não são isométricas: fora desta parte.

## Verificação

* `CATALOGO=fase6 npm run render`: 51 imagens, cerca de 3 min, **364 KB** no total (a maior, `dentro_piso_sala`,
  15 KB). `uso` inteiro em todas (largura e altura pares) e apoio dentro da imagem.
* `npm run previa:dentro` conferido em quatro rodadas contra a maquete 1: piso de tábuas quentes e lajotas, paredes
  pastel tingidas, móveis de plástico com sombra de contato. Correções: o verniz do piso refletia o ambiente e
  estourava o piso visto de 30 graus (piso sem verniz e sol mais fraco nele), banheira oca para a água aparecer,
  corrimão da escada na altura das paredes, geladeira aberta com a porta para a esquerda e prateleiras à vista.
* Grade: em `previa_dentro_grade.png` e `previa_dentro_sala.png` a grade do piso (Projecao com ladrilho de 128 px)
  passa nas juntas das lajotas e das tábuas, e as caixas de `PlantaDaCasa.caixaDo` contornam cada móvel.
