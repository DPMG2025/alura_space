# Fase 3, parte A: casa de fora em diorama de ilha (arte)

Data: 2026-11-07. Sem commit nem push por esta tarefa. Kotlin, workflow e arte dos personagens e da interface não
foram tocados: as 122 entradas antigas do manifesto e os WebP antigos saíram idênticos no render completo.

## O que foi feito

* `arte3d/src/objetos/cenaDaCasa.js` (novo): mundo da cena, ilha, casa por partes, estruturas, céu e as camadas.
* `arte3d/src/objetos/itensDaCasa.js` (novo): os 40 itens de `dominio/ItensDaCasa.ordem`, a lua e as nuvens.
* `arte3d/src/casa.js` (novo): entradas do catálogo (ids, ordem, grupo, tingir, uso). Entra em `catalogo.js`.
* `arte3d/src/cameras.js`: tipos `casa` (câmera fixa da cena) e `casaItem` (itens no mesmo ângulo).
* `arte3d/src/pagina.js`: enquadramento `cena`, apoio fixo da cena e sombra de contato por item.
* `arte3d/previa_casa.js` (novo, `npm run previa:casa`): empilha camadas e itens como o app e grava em
  `arte3d/saida/` `previa_casa.png` (casa completa com os itens nas posições do domínio e as crianças),
  `previa_casa_etapas.png`, `previa_casa_noite.png` (noite, chuva, neve, cores da loja) e `previa_casa_itens.png`.

## A câmera da cena (como o app empilha)

A câmera `casa` olha na horizontal com deslocamento de lente, de modo que o plano da frente da casa cai **exatamente
nas frações da cena do `CenaDaCasa`** (x da largura, y da altura, altura = 1,15 x largura). A casa segue a `Planta`
(paredes de 0,10 a 0,90 e de 0,30 a 0,78; telhado de (0,04; 0,315) à cumeeira (0,5; 0,075)); janelas, porta,
chaminé, cerca, caminho e laguinho estão nos lugares do desenho atual. O piso de cima tem a faixa de 0,506 a 0,545
(o pé dos móveis de cima é 0,528) e o de baixo de 0,727 a 0,793 (pé 0,775).

* Toda camada da cena tem **400 x 460 dp** (800 x 920 px), a cena inteira, alinhada no topo: desenhar cada uma em
  `(0, 0)` com a largura da cena. O chão (`casa_etapa_0_vazio`, `casa_etapa_1_terreno`) e o `casa_caminho` têm
  **400 x 660 dp**: continuam 200 dp para baixo (o jardim de baixo das telas altas, até 0,43 da largura); cortar o
  que sobrar.
* Apoio de todas as camadas: (400, 730) px = meio da base da frente da casa.
* `ordem`: desenhar da menor para a maior. `tingir` nos fundos de cômodo = nome do cômodo (`Comodo.name` em
  minúsculas): multiplicar pela `EstadoJogo.corDo(comodo)` como nos personagens.
* De noite: escurecer as camadas com filtro (o céu, a grama e a casa) e trocar `casa_janelas` por `casa_janelas_noite`,
  pôr `casa_janela_do_telhado_noite` e `item_sol_noite` no lugar do sol, e as estrelas no céu.

| ordem | id | quando |
|---|---|---|
| 2 | `ceu_estrelas_1`, `ceu_estrelas_2` | noite (alternar para piscar; posições do app) |
| 10 | `casa_etapa_0_vazio` | sem TERRENO: ilha com lote de terra, placa em branco (o texto do app vai em 0,36..0,64 x 0,62..0,71) e tijolinhos |
| 10 | `casa_etapa_1_terreno` | com TERRENO: grama, base de madeira, flores, árvores e morros |
| 12 | `casa_lago` | LAGO |
| 13 | `casa_caminho` | CAMINHO (pedras do app e continuação no jardim de baixo) |
| 14 | `casa_cerca` | CERCA |
| 18 | `casa_chamine` | CHAMINE (só com TELHADO; a fumaça segue animada pelo app) |
| 20 | `casa_comodo_{quarto,banheiro,sala,cozinha}` | PAREDES; tingir com a cor do cômodo |
| 22 | `casa_papel_{1,2,3}` | papel de parede da loja (listras, bolinhas, estrelinhas; 0 = sem camada) |
| 24 | `casa_janelas` / `casa_janelas_noite` | JANELAS |
| 26 | `casa_etapa_4_porta_{0..4}` | PORTA, índice = `corPorta` (`Loja.coresDaPorta`) |
| 30 | `casa_etapa_2_paredes` | PAREDES (paredes, pisos, divisórias; sombra da casa na grama) |
| 32 | `casa_escada` | ESCADA |
| 40 | `casa_etapa_3_telhado_{0..5}` | TELHADO, índice = `corTelhado` (`Loja.coresDoTelhado`) |
| 41 | `casa_janela_do_telhado_noite` | noite com TELHADO |
| 42 | `casa_neve_no_telhado` | clima 2 com TELHADO |
| 90 | `ceu_chuva`, `ceu_neve` | clima 1 e 2: emendáveis na vertical (rolar a imagem); recortar paredes e telhado como hoje |

Os itens e as crianças vão por cima de todas as camadas da casa (exceto chuva e neve).

## Itens soltos (câmera `casaItem`; apoio = meio da base; sombra de contato só no que pisa no chão)

`item_<id do domínio>` para os 40: terreno, paredes, telhado, porta, cama, janelas, sofa, sol, escada, arvore,
geladeira, ursinho, fogao, cachorro, tv, mesa, flores, banheira, chamine, abajur, gato, quadro, planta, cerca, nuvem,
livros, bola, espelho, caminho, bicicleta, violao, bolo, lago, pato, trem, borboleta, pipa, escorregador, arcoiris,
estrela. Mais `item_sol_noite` (lua com estrelinhas). As 10 estruturas também têm imagem solta, para o prêmio e o baú.

* Tamanho de uso de 96 a 200 dp; geladeira 96 x 200, fogão 120 x 160, mesa 180 x 126 (as proporções do
  `ItensDaCasa.proporcaoDe`); os outros são quase quadrados. O app deve usar a proporção do manifesto.
* Têm sombra de contato 3D (o app pode largar a elipse dele nesses): cama, sofá, árvore, geladeira, fogão, cachorro,
  tv, mesa, flores, banheira, gato, planta, bola, bicicleta, violão, trem, escorregador, terreno, paredes, escada,
  cerca. Sem sombra: céu, parede, o que boia (pato) e o que fica em cima de outro (ursinho, bolo, livros, abajur).
* Cachorro e gato hoje são `BichoDesenhado` (animados); as imagens servem de reserva ou para o baú e o prêmio.

Céu: `ceu_nuvem_1`, `ceu_nuvem_2`, `ceu_nuvem_3` (enfeite que passeia) e `ceu_nuvem_chuva` (nuvem cinza do clima 1).
O sol é o item `item_sol` (não há sol de enfeite separado, como no app hoje).

## Verificação

* `npm run render` completo: 201 imagens (79 novas), cerca de 4 min, nenhum órfão. Pasta `assets/arte` com
  **2,0 MB** (as novas somam 1,0 MB); a maior imagem nova tem 44 KB (`casa_papel_2`).
* `ManifestoDaArteTest`: 6 de 6 verdes (Kotlin 2.0.21 + JUnit 4, Gradle `--offline` em projeto temporário fora do
  repositório, `workingDir` = `app/`).
* Prévias conferidas contra a maquete 1 em três rodadas: grama mais saturada, interior sem sombra (a sombra do teto
  deixava os cômodos cinzentos), sombra da casa só perto dela (antes cortava num retângulo), pedras do caminho
  achatadas, pisos de madeira mais quentes, planta (que saía vazia) e gato corrigidos. Resultado: casa de bonecas
  aberta de dois andares, telhado rosa de plástico com cumeeira redonda, frontão com janela redonda, chaminé,
  árvores e morros na ilha, como a maquete.

## Pendências e avisos para a parte B (Kotlin)

* **Memória:** uma camada da cena decodificada tem 800 x 920 x 4 = 2,9 MB (o chão, 4,2 MB). Compor a pilha da casa
  uma vez num bitmap por estado (etapas, cores, papel, noite) e guardar, como nos personagens; animar só itens,
  crianças, nuvens, estrelas e chuva por cima.
* Camadas de céu em 3D que ficaram de fora: passarinhos de enfeite (o app pode manter os riscos) e a fumaça da
  chaminé (pode usar `ceu_nuvem_2` pequena e translúcida).
* Arco-íris e pipa ficaram bonitos em 3D; nenhum item ficou para depois.
* Os itens são vistos de frente e um pouco de cima, como a casa; o ursinho, o bolo e os livros não têm sombra porque
  ficam em cima de outro objeto.
