# Fase 4, parte A: cenário do jogo e figuras das perguntas (arte)

Data: 2026-11-07. Só arte: sem Kotlin e sem testes. Branch `worktree-agent-ac8ef9faa61f7388b`, feita a partir do
commit 210d507 (fases 3A e 3B), em paralelo às fases 5 e 7. Por isso tudo sai num **catálogo e num manifesto à
parte**: `arte3d/src/catalogo_fase4.js` e `app/src/main/assets/arte/manifesto_fase4.json`. O `src/catalogo.js`
não foi tocado.

## Como gerar

* `CATALOGO=fase4 npm run render [-- ids]`: renderiza só o catálogo da fase 4, de modo incremental (não apaga
  nenhuma outra imagem) e grava o `manifesto_fase4.json`. Cerca de 4 min para as 67 imagens.
* `npm run previa:jogo`: grava em `arte3d/saida/` (não versionado) `previa_jogo.png` (a tela da maquete 2 ao lado
  da maquete), `previa_jogo_cenas.png` (as quatro ilhas com as cinco cenas) e `previa_figuras.png` (a grade das
  figuras no tamanho de uso e a 48 dp, com os emojis e o lugar de cada uma).

**Para o integrador:** espalhar `catalogoDaFase4` no `src/catalogo.js`, rodar o render completo (que junta tudo
no `manifesto.json` e apaga os órfãos) e apagar o `catalogo_fase4.js` e o `manifesto_fase4.json`. Até lá o
`ManifestoDaArteTest` acusa as imagens `jogo_*` e `fig_*` como órfãs (não estão no `manifesto.json`).

## Código

* `render.js`, `src/pagina.js`: `CATALOGO=faseN` (carrega `src/catalogo_faseN.js` pela URL `?catalogo=`),
  campo `dados` da entrada copiado para o manifesto (figuras: `emojis`, `lugar`), câmera fixa com `recorte`,
  `cortaNaBorda` (a ilha passa das bordas de propósito) e âncoras presas à imagem como o apoio.
* `src/cameras.js`: tipo `jogo`, a câmera e a luz do protótipo (`jogo.html`).
* `src/objetos/cenasDoJogo.js` (novo): ilhas dos caminhos e cenas das perguntas.
* `src/objetos/figuras.js` e `src/figuras.js` (novos): os modelos e a lista (id, emojis, lugar).
* `src/objetos/itensDaCasa.js`: só exporta no fim peças e modelos já existentes (gato e cachorro da casa não
  são usados; as figuras de bicho são novas, sentadas e de frente). Reaproveitados: bola, trem, borboleta,
  pipa, bolo, ursinho, lua, estrela (e o patinho da Fase 0).
* `previa_jogo.js` (novo) e o script `previa:jogo` no `package.json`.

## Camadas do cenário (câmera `jogo`)

Todas com **400 x 520 dp** (800 x 1040 px), o mesmo quadro e o mesmo apoio (o centro da ilha). O quadro é o
recorte que começa em y = 100 dp da tela de 400 dp de largura (como a maquete 2): na tela de L dp, desenhar
com largura L a partir de y = 100 x L / 400 (ou alinhar o centro da ilha onde couber entre o balão e as
respostas). Empilhar pela `ordem`: céu em degradê do app, ilha (10), cena (20), e por cima figuras e crianças
em ordem de y (quem está mais embaixo na tela vai por cima).

| id | o que é |
|---|---|
| `jogo_matematica_ilha` | ilha com blocos 1 2 3 e contas de ábaco |
| `jogo_portugues_ilha` | blocos A B C e pilha de livrinhos |
| `jogo_ingles_ilha` | globo e bandeirinhas |
| `jogo_logica_ilha` | argolas de empilhar e cubos |
| `jogo_comum_lagoa` | lagoa com pedras, vitórias-régias e taboas (a maquete 2) |
| `jogo_comum_mesa` | mesa redonda com toalha xadrez, pratinho e jarra de suco |
| `jogo_comum_varal` | varal com pregadores coloridos e cesto de roupa |
| `jogo_comum_cercado` | cerquinha da fazenda, fardos de feno e cocho |
| `jogo_comum_tapete` | tapete de brincar de anéis coloridos e baú de brinquedos |

Os caminhos seguem `dominio/Trilha.name` em minúsculas. As cenas são as mesmas nos quatro caminhos; cada cena
traz a própria sombra na grama. Âncoras (em px da camada):

* ilha: `personagens` (pé das crianças, fundos à esquerda), `centro`, `vaga_1..6` na grama, `ceu_1..4` (figuras
  que voam), `area_esquerda/direita/fundo/frente` (a borda da superfície útil);
* lagoa, mesa, cercado, tapete: `vaga_1..6` (na água, no tampo ou na grama) e `area_*`;
* varal: `vaga_1..5` na corda (prender ali a âncora `topo` da roupa).

Escolha da cena pela pergunta: o `lugar` das figuras da pergunta (abaixo). Com mais figuras que vagas (contar até
10 ou 20), espalhar dentro da elipse das `area_*`. Tamanho das figuras na cena: 72 a 96 dp com até 6, menor com
mais. Na `previa_jogo.png` os 4 patinhos têm 96 dp.

## Figuras (`fig_<nome>`, 96 dp, render 192 x 192 px)

Contagem real: o gerador de fases do app (`GeradorDeFases`, compilado fora do repositório) com 12 sorteios das
fases 1 a 100 dos quatro caminhos, presença de cada emoji por pergunta (cena, opções, cesta, memória, montar
palavra). 375 emojis diferentes; as 58 figuras cobrem **57 %** das aparições.

O id é o nome do vocabulário do app (`Vocabulario.kt`: `Objeto.singular` ou `Palavra.texto`, minúsculo e sem
acento). No manifesto, cada figura tem `emojis` (os que ela substitui) e `lugar`. Tipo `objeto` (pisa no chão,
com sombra de contato 3D) ou `item` (voa ou fica pendurado, sem sombra). Âncoras: `topo` e `centro`; apoio = o pé.

* Bichos: gato 🐱🐈, peixe 🐟, sapo 🐸, cachorro 🐶🐕, pato 🦆, cavalo 🐴, vaca 🐮🐄, porco 🐷, tartaruga 🐢,
  borboleta 🦋, leao 🦁, pintinho 🐥, macaco 🐒, elefante 🐘, coelho 🐰🐇, galo 🐓, passarinho 🐦, ovelha 🐑,
  galinha 🐔, pinguim 🐧, joaninha 🐞, foca 🦭, tigre 🐯, abelha 🐝, rato 🐭.
* Comidas: maca 🍎, banana 🍌, morango 🍓, uva 🍇, cenoura 🥕, bolo 🎂, cereja 🍒, pao 🍞, ovo 🥚, bolinho 🧁.
* Brinquedos: bola ⚽, balao 🎈, carrinho 🚗, ursinho 🧸, trem 🚂, aviao ✈️, pipa 🪁, tambor 🥁.
* Roupas (varal): luva 🧤, cachecol 🧣, camiseta 👕, bone 🧢, meia 🧦.
* Céu e natureza: estrela ⭐🌟, coracao ❤️, lua 🌙, flor 🌸.
* Bolinhas de cor da Lógica: bolinha_vermelha 🔴, bolinha_azul 🔵, bolinha_verde 🟢, bolinha_amarela 🟡,
  bolinha_roxa 🟣, bolinha_laranja 🟠.

Lugares: lagoa (peixe, sapo, pato, tartaruga, pinguim, foca), cercado (cavalo, vaca, porco, pintinho, galo,
ovelha, galinha), mesa (as comidas), varal (as roupas), tapete (brinquedos de chão e bolinhas), céu (borboleta,
abelha, balão, avião, pipa, estrela, coração, lua) e grama (os outros bichos e a flor).

## Emojis ainda sem modelo (os 60 mais usados, com o número de aparições)

− 354 (sinal da conta, não é figura) · 🟦 234 · 🦃 218 · 🐬 186 · 🐙 184 · 👟 184 · 🍋 177 · 🦀 175 · 🦓 174 ·
🥛 173 · 🐳 172 · ❄️ 171 · 🦈 169 · 🍐 167 · 🍬 156 · 🐐 156 · 👖 151 · 🦒 150 · 🐍 150 · 🏠 147 · 🍉 145 ·
🧀 144 · 🍦 140 · ☀️ 138 · 🍿 136 · 📚 133 · 🦉 130 · 😴 129 · ⛵ 128 · 🦜 127 · 🌊 126 · 🔺 126 · 👗 125 · 🛷 123 ·
⛄ 123 · 🍅 122 · 🍍 119 · 🎲 118 · 🏃 117 · 🧚 117 · 🟩 116 · 🟪 115 · 🐊 115 · 🟨 112 · ✋ 111 · 🎒 110 ·
✏️ 110 · 📕 109 · 🔷 108 · 🌧️ 107 · 🧊 107 · 🦐 106 · 🍕 105 · 🦷 105 · 🐻 105 · 🟥 104 · 👶 104 · 🪙 103 ·
🚢 103 · 🏫 103. Ao todo 312 emojis sem modelo (a lista completa sai rodando a contagem de novo). 🪙 já tem a
`moeda` (Fase 0) e ☀️ e 🏠 têm `item_sol` e `item_paredes` (Fase 3), que o app pode usar como reserva.
Próximos lotes sugeridos: quadrados e formas da Lógica (🟦🟩🟪🟨🟥🟧🔺🔷, baratos), bichos do mar (🐬🐙🦀🐳🦈🦐),
peru, zebra, girafa, cobra, cabra, coruja, papagaio, jacaré, e as comidas e roupas que faltam.

## Verificação

* Render sem erro; 67 imagens, 808 KB no total; a maior é `jogo_comum_cercado` (47 KB), longe do orçamento de
  400 KB. Todos os apoios e âncoras ficam dentro da imagem.
* Prévias comparadas com a maquete 2 em três rodadas: água da lagoa mais azul; o celeiro do cercado saiu (batia
  no enfeite do caminho), o varal ficou mais perto e mais baixo, cesto e baú foram para onde aparecem; a meia foi
  refeita (o tubo torcia as listras) e o focinho do porco virou para a frente.
* Pendências para a parte B (Kotlin): montar `cenarioDoJogo` com as camadas, trocar emojis por `fig_*` pelo campo
  `emojis` (com o emoji como reserva) e o teste que lista os emojis com modelo (PRD, seção 5).

## Commit

Arte, código e este relatório: `f4410a1` (branch `worktree-agent-ac8ef9faa61f7388b`, sem push).
