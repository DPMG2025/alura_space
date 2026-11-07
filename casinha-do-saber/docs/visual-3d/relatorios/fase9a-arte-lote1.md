# Fase 9, arte em lote 1: mais figuras das perguntas

Data: 2026-11-07. Só arte: sem Kotlin e sem testes. Feita a partir de `ccr-162c6a70-urxpmq` (917ee8b), num
catálogo e num manifesto à parte, como as fases 4 a 8.

## Resultado

* **160 figuras novas** `fig_*` (meta: 120), no molde das 58 da Fase 4: brinquedo de plástico com verniz, 96 dp
  (192 x 192 px), câmera `objeto` (no chão, com sombra de contato) ou `item` (voa ou fica no varal, sem sombra),
  âncoras `topo` e `centro`, apoio no pé, campos `emojis` e `lugar` no manifesto.
* Os emojis delas somam **19048 das 24117 aparições** que ainda não tinham modelo na contagem do
  `cobertura-emojis.md`: a cobertura estimada sobe de 53,9 % para **cerca de 90 %** (47215 de 52284). A conta usa
  a contagem antiga do `CoberturaDeEmojisTest`; o teste dá o número exato depois da integração.
* 164 dos 313 emojis sem modelo agora têm figura (mais variantes que o jogo ainda não usa, como 🐋 🐪 🦖 🏡 🍨 👢
  📘 🏅 ☔ 🌞). Nenhum emoji repete um que já tinha figura, e nenhum id existe no `manifesto.json`.

## Como gerar e conferir

* `CATALOGO=fase9a npm run render [-- ids]` (em `arte3d/`): grava as imagens em `app/src/main/assets/arte/` e o
  `manifesto_fase9a.json`, de modo incremental (não apaga nada). Cerca de 7 min para as 160. O `render.js` já
  aceitava o nome `fase9a`, sem mudança.
* `node previa_fase9a.js [ids]`: grava em `arte3d/saida/` (não versionado) `previa_fase9a_1..4.png`, a grade das
  figuras a 96 e a 48 dp, com emojis e lugar, e na primeira fila 6 figuras da Fase 4 (gato, peixe, maçã,
  carrinho, camiseta, sapo) para comparar. Com ids, só elas, em `previa_fase9a_sel.png`.

**Para o integrador:** espalhar `catalogoDaFase9a` (de `src/catalogo_fase9a.js`) no `src/catalogo.js`, rodar o
render completo (junta tudo no `manifesto.json`) e apagar o `manifesto_fase9a.json`. Até lá o
`ManifestoDaArteTest` acusa as 160 imagens como órfãs. O `manifesto.json` não foi tocado.

## Código

* `arte3d/src/catalogo_fase9a.js` (novo): a lista `[nome, emojis, lugar, voa]` e as entradas do catálogo.
* `arte3d/src/objetos/figurasDoLote9a.js` (novo): os modelos. Moldes novos: `calcado` (tênis, sapato, bota,
  patins), `fuso` (golfinho, tubarão), `caminhao` (caminhão e caminhão de bombeiro), `quadrado`, `coracaoDe`,
  `citrico`, `assentar` (põe o ponto mais baixo em y = 0, para figuras giradas).
* `arte3d/src/objetos/figuras.js`: só exporta no fim as peças já existentes (`pecasDasFiguras`: plástico,
  torneado, tubo, estufado, carinha, bichinho...). `src/figuras.js`: `comAncoras` exportado.
* `arte3d/src/objetos/itensDaCasa.js`: só exporta mais modelos já existentes (`maisModelosDosItens`).
* Reaproveitados: árvore, nuvem (também na chuva), arco-íris, cama, livros, violão, bicicleta (itens da casa);
  lápis (sem a âncora `ponta`), paleta, piano, medalha (enfeites da Fase 8); troféu (prêmio); presente.
* `arte3d/previa_fase9a.js` (novo): a prévia acima.

## As figuras (emojis, lugar)

* Mar e água (lagoa): polvo 🐙, caranguejo 🦀, golfinho 🐬, tubarao 🦈, baleia 🐳🐋, concha 🐚, camarao 🦐,
  lula 🦑, jacare 🐊, peixinho 🐠, baiacu 🐡, onda 🌊, barco ⛵, navio 🚢, canoa 🛶.
* Bichos (grama): papagaio 🦜, cobra 🐍, zebra 🦓, girafa 🦒, coruja 🦉, gorila 🦍, urso 🐻, urso_polar 🐻‍❄️,
  onca 🐆, preguica 🦥, lagartixa 🦎, caracol 🐌, formiga 🐜, camelo 🐫🐪, panda 🐼, dinossauro 🦕🦖;
  (cercado): peru 🦃, cabra 🐐, boi 🐂.
* Natureza e lugares (grama, ou céu para o que voa): arvore 🌳, boneco_de_neve ⛄☃️, floco_de_neve ❄️ (céu),
  sol ☀️🌞 (céu), chuva 🌧️ (céu), nuvem ☁️ (céu), palmeira 🌴, cacto 🌵, girassol 🌻, broto 🌱, arco_iris 🌈
  (céu), raio ⚡ (céu), rosa 🌹, cogumelo 🍄, trevo 🍀, fogo 🔥, pedra 🪨, montanha ⛰️🏔️, vulcao 🌋, osso 🦴,
  casa 🏠🏡, escola 🏫, circo 🎪.
* Comidas (mesa): leite 🥛, pipoca 🍿, limao 🍋, gelo 🧊, pera 🍐, queijo 🧀, bala 🍬, melancia 🍉, sorvete
  🍦🍨, tomate 🍅, abacaxi 🍍, pizza 🍕, maca_verde 🍏, laranja 🍊, biscoito 🍪, mel 🍯, alface 🥬, pepino 🥒,
  ovo_frito 🍳, copo 🥤, coco 🥥, pirulito 🍭, batata 🥔, brocolis 🥦, milho 🌽, pessego 🍑, vela 🕯️.
* Roupas e calçados (varal, `item`, presos pelo `topo`): tenis 👟, chinelo 🩴, calca 👖, cartola 🎩, bota 🥾👢,
  bermuda 🩳, casaco 🧥, vestido 👗, maio 🩱, oculos 🕶️👓, sapato 👞, patins ⛸️, sapatilha 🩰.
* Partes do corpo (tapete): dente 🦷, mao ✋🖐️, pe 🦶, boca 👄, olho 👁️, orelha 👂, nariz 👃, perna 🦵.
* Formas e cores da Lógica (tapete; corações no céu): quadrado_azul 🟦, quadrado_vermelho 🟥, quadrado_verde 🟩,
  quadrado_amarelo 🟨, quadrado_roxo 🟪, quadrado_laranja 🟧, triangulo 🔺, losango 🔷, coracao_amarelo 💛,
  coracao_azul 💙, coracao_roxo 💜, coracao_laranja 🧡, coracao_verde 💚.
* Escola, casa e brinquedos (tapete): lapis ✏️, dado 🎲, tesoura ✂️, livros 📚, livro 📕📘📗📙, livro_aberto 📖,
  mochila 🎒, mala 🧳, giz_de_cera 🖍️, regua 📏, trompete 🎺, computador 💻, chave 🔑, paleta 🎨,
  escova_de_dente 🪥, cadeira 🪑, microfone 🎤, quebra_cabeca 🧩, tijolo 🧱, robo 🤖, ioio 🪀, guarda_chuva ☂️☔
  (céu), violao 🎸, fone 🎧, cama 🛏️, trofeu 🏆, medalha 🥇🏅, caneta 🖊️, videogame 🎮, piano 🎹, lampada 💡,
  cesto 🧺, presente 🎁, esqui 🎿, treno 🛷.
* Transportes: caminhao_de_bombeiro 🚒, caminhao 🚚, onibus 🚌, bicicleta 🚲 (tapete), trator 🚜 (cercado),
  foguete 🚀 e helicoptero 🚁 (céu).

Os ids seguem o nome do vocabulário do app (`Objeto.singular`, `Palavra.texto` ou o nome que as fases dão ao
emoji), em minúsculas, sem acento e com `_`.

## Emojis ainda sem modelo (os 40 mais frequentes, aparições da contagem antiga)

🧚 150 · 🏃 139 · 😴 129 · 👧 106 · 🤡 106 · 🤹 105 · 👶 104 · 😢 101 · 🌃 97 · 👦 97 · 💃 93 · 🧗 87 · 😀 86 ·
🤒 84 · 🏊 81 · 📝 75 · 👩 69 · 🕸 62 · 😠 58 · 🚩 58 · 😨 57 · 👴 56 · 💋 55 · 😋 55 · 🏛 54 · 👷 54 · ✍ 53 ·
🐵 52 · 🪟 52 · 😂 49 · 👨 47 · 😭 47 · 📮 46 · 👮 45 · 👵 45 · 😲 45 · 🚶 45 · 🩺 45 · 🍃 44 · 🍾 44.
Ao todo 149. Quase todos são pessoas, ações e carinhas (melhor resolvidos com as crianças da Fase 3 e um lote de
rostos), mais lugares e objetos raros. Sugestões: 🐵 pode entrar nos `emojis` do `fig_macaco` (é o mesmo bicho)
quando o integrador juntar o manifesto; próximo lote: carinhas (😴😢😀🤒😠😨😋😂😭😲), 🕸 🚩 📝 🪟 📮 🩺 🍃 🍾,
relógios (🕐 a 🕛), setas (⬅➡⬆⬇) e as comidas e bichos raros que sobram (🦘 🐺 🦢 🐿 🍔 🥪 🍫 ...).

## Verificação

* Render sem erro e sem figura cortada; 160 imagens, 1287 KB no total; a maior é `fig_floco_de_neve` (13,2 KB),
  longe do orçamento de 400 KB por imagem.
* Script de conferência (cabeçalho do WebP lido à mão, VP8X/VP8L/VP8): todas existem, largura e altura do
  cabeçalho = manifesto (192 x 192), apoio e âncoras dentro da imagem, só `topo` e `centro`, `lugar` válido,
  `emojis` não vazio, nenhum id no `manifesto.json`.
* Prévias comparadas com 6 figuras da Fase 4 em três rodadas. Refeitas depois da primeira: concha (saía
  achatada e solta do chão), golfinho e osso (assentados no chão), fogo (lenha à vista e três línguas de chama),
  floco de neve (azul mais forte; sumia no céu claro), trevo (quatro folhas de coração em volta do meio), pizza,
  biscoito, ovo frito e livro aberto (inclinados para a câmera), alface (folhas em volta de um miolo), pirulito
  (espiral branca nas duas faces), tênis, sapato, bota e patins (molde `calcado`), sapatilha (fitas curtas),
  nariz (perfil, como o emoji), onda (crista de espuma), régua, trompete, escova, esqui, ioiô, lâmpada, mão; o
  jacaré e a lagartixa ganharam cabeça maior e rabo enrolado para cima (eram compridos e baixos, pequenos no
  quadro).
