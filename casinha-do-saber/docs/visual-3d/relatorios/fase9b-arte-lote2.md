# Fase 9B, arte, lote 2: figuras das áreas novas, dos livrinhos, da história e do ateliê

Data: 2026-11-07. Só arte, sem Kotlin nem testes. A worktree foi zerada em `ccr-162c6a70-urxpmq` (917ee8b). O commit
é local, sem push. O `manifesto.json` não foi tocado.

## Levantamento (`arte3d/emojis_fase9b.py`)

O script lê os emojis de quatro fontes:

* todas as coisas das 20 áreas de `CasaQueCresce`, contadas como a criança as ganha (a cópia N de cada tipo), mais
  o ícone da obra;
* `Livrinhos.kt`, `TelaLivrinhos.kt` e `CenaDoLivrinho.kt` (capas, marcadores, figuras das páginas e perguntas);
* `Historia.kt` e `CenaDaHistoria.kt`;
* `TelaAtelie.kt` e `Loja.kt` (brindes).

Depois tira da lista:

* o que já tem `fig_*` (campo `emojis` dos manifestos) ou a `moeda`;
* as coisas que o `ArteDasAreas` já mostra como `item_<tipo>` (mesmo tipo e mesmo emoji de uma peça da casa) e o
  cachorro e o gato;
* **todo emoji do vocabulário das perguntas**: `dominio/fases/*`, `Vocabulario.kt`, `VocabularioDosMundos.kt`,
  `GeradorDeFases.kt`, `AtividadesNovas.kt` e a lista de `cobertura-emojis.md`. Esses ficam com o outro agente.

Resultado: 374 emojis diferentes. 55 já tinham figura, **244 são do vocabulário (pulados)** e restaram 75 alvos. A
meta de 150 não cabia: depois da regra de não colidir com o outro lote, só sobram 75 emojis no escopo.

## O que foi feito: 74 figuras (`arte3d/src/catalogo_fase9b.js`, `src/objetos/figurasFase9b.js`)

As figuras seguem a ordem das áreas que a criança alcança primeiro. Todas têm 96 dp (192 x 192 px), `emojis`,
`lugar`, apoio e as âncoras `topo` e `centro`, como as figuras da Fase 4. O estilo também é o delas: plástico com
verniz, sombra de contato nos objetos e itens que voam sem sombra. Nenhum id existe no `manifesto.json` (o
validador confere) e os nomes foram comparados com as palavras do vocabulário: `fig_lanterna`, `fig_porquinho`,
`fig_cavalinho`, `fig_patins_de_gelo` e `fig_montanha` ficaram livres para o outro lote.

| área ou origem | figuras |
|---|---|
| quarto novo | lanterna_de_papel 🏮, almofada_coracao 💖, sapatilha 🥿 |
| área do pet | frisbee 🥏, poodle 🐩, novelo 🧶 |
| livrinhos, história, ateliê e loja | margarida 🌼, lotus 🪷, carinha_rindo 😄, bola_de_confete 🎊, bolinha_marrom 🟤, pinheiro 🌲, casinhas 🏘️, ninho_vazio 🪹, carinha_abraco 🤗, carinha_feliz 😊, marcador 🔖, montanha_sol 🌄, quadrado_marrom 🟫, tronco 🪵, carinha_preocupada 😟, esponja 🧽 (a borracha do ateliê), colar_de_contas 📿, bolinha_branca ⚪ |
| piscina | boia 🛟, guarda_sol ⛱️, mascara_de_mergulho 🤿 |
| parquinho | bolhas 🫧, patinete 🛴, skate 🛹, escorregador 🛝 (o modelo da casa) |
| horta, brinquedos, árvore e flores | pimentao 🫑, carro_de_corrida 🏎️, ninho 🪺, hibisco 🌺, fonte ⛲ |
| estúdio | saxofone 🎷, banjo 🪕, atabaque 🪘, radio 📻 |
| varanda gourmet | espetinho 🍢, panquecas 🥞 |
| biblioteca | livro_verde 📗, livro_azul 📘, livro_laranja 📙 |
| observatório | lua_cheia 🌕 |
| fazendinha | porco_da_fazenda 🐖, pintinho_da_fazenda 🐤, cavalo_da_fazenda 🐎 (os modelos das perguntas) |
| quadra | bola_de_rugbi 🏈, bola_de_beisebol ⚾, pingue_pongue 🏓, peteca 🏸, gol 🥅, patins_de_rodinha 🛼, taco_de_hoquei 🏒, cronometro ⏱️ |
| aquário | coral 🪸, lagosta 🦞 |
| cinema | camera_de_cinema 🎥, projetor 📽️, rolo_de_filme 🎞️ |
| lago | castor 🦫, lontra 🦦 |
| laboratório | microscopio 🔬, frasco ⚗️, placa_de_petri 🧫, dna 🧬, termometro 🌡️, esquadro 📐 |
| salão de festas | pinata 🪅, globo_de_luz 🪩, estrelinha_de_festa 🎇, chapeu_de_festa 🥳 |

Os emojis do vocabulário que aparecem nas áreas também não têm modelo. São 244, entre eles 🐬🐳🦈🐙🦀🍉🍦🚀🤖🎸📚
etc. Eles estão no lote do outro agente.

### Ainda sem modelo neste escopo

* ↩️, o "Desfazer" do ateliê. É um glifo de interface: o caminho é um ícone da barra (`icone_*`), não uma figura.

### Mudanças fora dos arquivos novos

Só exportações, que não mudam nenhuma imagem existente:

* `figuras.js` ganhou `pecasDasFiguras`;
* `itensDaCasa.js` passou a incluir `escorregador` em `modelosDosItens`.

## Como gerar e conferir

* `CATALOGO=fase9b npm run render`: as 74 imagens saem em cerca de 3 min, com o `manifesto_fase9b.json` à parte.
  O render é incremental e não apaga nada.
* `node previa_fase9b.js`: grava `saida/previa_fase9b.png`, a grade das novas a 96 e a 48 dp, ao lado de 8 figuras
  antigas, com o lugar de cada uma.
* `python3 arte3d/validar_fase9b.py`: 74 imagens, 0 erros, 657 KB no total, a maior com 14,4 KB (limite de 400 KB).
  Confere a existência, a largura e a altura do cabeçalho do WebP (= uso x 2), o id e os emojis fora do
  `manifesto.json`, e o apoio e as âncoras dentro da imagem.

### Iteração

Revisei a grade inteira ao lado das antigas e refiz cinco figuras:

* frisbee: o torneado aberto deixava ver através dele; virou um disco maciço com aro.
* bolhas: estavam apagadas demais; ficaram mais opacas.
* pimentão: lia como maçã; ganhou gomos mais fundos e ficou mais alto.
* raquete da peteca: aumentada.
* sapatilha: corpo e sola refeitos.

Os outros modelos já batiam com o padrão das figuras da Fase 4.

## Para o integrador

1. Espalhar `catalogoDaFase9b` no `src/catalogo.js`.
2. Rodar o render completo, ou juntar o `manifesto_fase9b.json` ao `manifesto.json` por script, como na rodada 2
   da integração.
3. Apagar o manifesto à parte.

Até lá, o `ManifestoDaArteTest` acusa os `fig_*` novos como órfãos. O app não precisa de Kotlin novo:
`ArteDasAreas` e `FigurasDaPergunta` já escolhem a figura pelo emoji.
