# Fase 7, parte A: guarda-roupa em camadas 3D (só arte)

Data: 2026-11-07. Commit local, sem push.
Worktree: `/home/user/alura_space/.claude/worktrees/agent-ae46d05ffd667eb77`, branch
`worktree-agent-ae46d05ffd667eb77` (criada a partir de `ce09c73`, a ponta de `ccr-162c6a70-urxpmq`).
Sem Kotlin, sem testes. Nenhuma imagem de outra fase foi tocada nem apagada; `manifesto.json` não mudou.

## O que foi feito

* `arte3d/src/objetos/guardaRoupa.js` (novo): os 16 trajes, 18 acessórios e 4 chapéus montados em volta da
  MESMA figura da criança (`personagens.js > partes`), na câmera `personagem` (quadro 170 x 260 dp, 340 x 520 px,
  apoio nos pés em (170, 466) px), com a mesma luz fixa, o filtro final (`saturate(1.22) contrast(1.04)`) e os
  mesmos materiais (tecido = o da roupa básica, plástico com verniz, ouro, vinil, vidro). Os papéis da Fase 1A
  (`ver`, `recorte`, `sombra`, `capta`) valem aqui por `camadaDoGuardaRoupa()`, com partes auxiliares
  `tronco` (recorta o que fica atrás do corpo), `sapatosBase` (a calça não pinta o sapato) e `maos`.
  Estampas que acompanham o corpo (listras, cinto, colete, xadrez) são uma "casca" colada na superfície com
  textura presa ao mundo; botões, estrelas, flores e emblemas são postos na superfície por raio (`sobre`).
* Chapéus em 3D com a luz dos bonecos (a coroa, o boné, a cartola e a flor deixam de ser emoji chapado) e com
  sombra macia sobre a cabeça e o cabelo (`capta`).
* `arte3d/src/catalogo_fase7.js` (novo): 115 camadas. `render.js` e `src/pagina.js`: `CATALOGO=fase7` renderiza
  só esse catálogo (página com `?catalogo=fase7`), sempre incremental, manifesto próprio
  `app/src/main/assets/arte/manifesto_fase7.json` (o mesmo formato, mais o campo `esconde`). Sem `CATALOGO`, nada
  muda no comportamento antigo.
* `arte3d/previa_fase7.js` (novo, `node previa_fase7.js [trajes|acessorios|combos]`): empilha e tinge e grava
  `saida/previa_{trajes,acessorios,combos}.png`. A função `camadasComGuardaRoupa` é a referência para o app.

Comando: `cd arte3d && CATALOGO=fase7 node render.js` (115 camadas, ~7 min; ou `-- <ids>`).
Resultado: 115 WebP, 635 KB no total, maior 15,9 KB; nenhuma camada encosta na borda.

## Ordem de empilhamento (camadas da criança + Fase 7; desenhar da menor para a maior)

| ordem | grupo | o quê |
|---|---|---|
| 10 | sombra | `crianca_sombra` |
| 14 | roupaTras | `roupa_{heroi,abelha,fada,realeza,dinossauro}_tras` (capa, asinhas, manto, rabo) |
| 16 | costas | `acessorio_{mochila,asas,capa}` |
| 20 a 25 | cabelo de trás | (Fase 1A) |
| 30 | corpo | `crianca_corpo` (pernas) |
| 35 | pes | `crianca_pes_*` ou `roupa_{heroi,astronauta,futebol,bailarina,chuva,pirata,realeza,dinossauro}_pes` |
| 36 | pesAcessorio | `acessorio_{tenis,botas,pantufas}` |
| 40 | roupa | `crianca_roupa_*` ou o traje: `roupa_<traje>[_menina\|_menino]` |
| 45 | estampa | `crianca_roupa_*_estampa` ou `roupa_<traje>[_g]_estampa` (só trajes tingidos) |
| 50 | cabeca | (Fase 1A) |
| 52 | costasFrente | `acessorio_mochila_frente` (alças), `acessorio_capa_frente` (fecho) |
| 53 | pescoco | `acessorio_{colar,cachecol,gravata}` (o queixo recorta por cima) |
| 60 a 64 | rosto | boca, olhos, sobrancelhas (Fase 1A) |
| 66 | tapaOlho | `roupa_pirata_tapaolho` |
| 67 | rosto | `acessorio_{oculos,oculosdesol,mascara}` |
| 70 a 74 | cabelo da frente, brilho, laço | (Fase 1A) |
| 75 | cabelo | `acessorio_{laco,tiara,presilha}` |
| 76 | trajeCabeca | `roupa_{abelha,astronauta,chuva,chef,pirata,dinossauro}_cabeca` |
| 77 | chapeu | `chapeu_{coroa,bone,cartola,flor}` |
| 78 | mao | `acessorio_{varinha,balao,ursinho}_{baixo,cima}` |
| 80 | bracos | (Fase 1A) |
| 82 | mangas | `roupa_<traje>_mangas_<pose>` |

## Regras de uso (o app)

* **Traje**: `roupa_<traje>` se existir, senão `roupa_<traje>_menina|_menino` (gênero do perfil: só `listras`,
  `heroi`, `realeza` mudam de silhueta). Mais, se existirem: `_estampa`, `roupa_<traje>_tras`, `_pes`,
  `_mangas_<pose dos braços>` (`parado`, `tchau1..4`, `torcendo`, `pensando`), `_cabeca` (só **sem chapéu**) e
  `roupa_pirata_tapaolho` (some com acessório no rosto, pelo `esconde`).
* **Tingir**: `tingir: "roupa"` (cor do perfil) em `roupa_listras_*`, `roupa_heroi_*`, `roupa_fada`,
  `roupa_vestido`, `roupa_macacao` (a camisa). Todo o resto sai colorido (`tingir: null`).
* **Acessórios**: um por parte. ROSTO/PESCOCO/PES/CABELO: `acessorio_<id>`. COSTAS: `acessorio_<id>` e, se
  existir, `acessorio_<id>_frente`. MAO: `acessorio_<id>_cima` quando os braços estão em `torcendo`, senão
  `_baixo` (a mão esquerda da tela só tem essas duas posições; âncora `maoEsquerda`).
* **Chapéu**: Produto `valor` -> camada: 👑 `chapeu_coroa`, 🧢 `chapeu_bone`, 🎩 `chapeu_cartola`,
  🌸 `chapeu_flor`. Âncoras `cabeca` e `topoDaCabeca` gravadas, mas a camada já está no lugar: basta empilhar
  (o `ChapeuNaCabeca` com emoji pode sair).
* **`esconde`** (manifesto): lista de grupos que a camada tira das **outras peças** (as camadas da mesma peça não
  se escondem). Traje: `roupa`, `estampa` (+ `pes` se tem sapato próprio). Acessório dos pés: `pes` (tira o sapato
  básico e o do traje). Rosto: `tapaOlho`. Cabelo e cabeça do traje: `laco`. Chapéu: `laco`, `trajeCabeca`.
  Peça = prefixo + nome (`roupa_pirata`, `acessorio_capa`, `chapeu_bone`; `crianca_*` é a criança).
* Nenhum traje esconde braços ou pernas: as mangas compridas vêm por cima dos braços (camada 82, com a mão
  recortada) e as calças por cima das pernas (40). Todos os trajes têm as manguinhas no lugar das básicas, então o
  braço continua saindo da manga em qualquer traje.
* O irmãozinho não tem guarda-roupa (como no PRD).

## Lista de ids (115)

* Trajes (88): `roupa_listras_{menina,menino}` (+`_estampa`), `roupa_pijama`, `roupa_heroi_{menina,menino}`
  (+`_estampa`), `roupa_abelha`, `roupa_fada` (+`_estampa`), `roupa_astronauta`, `roupa_vestido` (+`_estampa`),
  `roupa_macacao` (+`_estampa`), `roupa_futebol`, `roupa_bailarina`, `roupa_chuva`, `roupa_medico`, `roupa_chef`,
  `roupa_pirata`, `roupa_realeza_{menina,menino}`, `roupa_dinossauro`; `_tras` (5), `_pes` (8), `_cabeca` (6) e
  `roupa_pirata_tapaolho` como na tabela; `roupa_{pijama,astronauta,chuva,medico,chef,dinossauro}_mangas_{parado,
  tchau1,tchau2,tchau3,tchau4,torcendo,pensando}` (42).
* Acessórios (23): `acessorio_{oculos,oculosdesol,mascara,colar,cachecol,gravata,mochila,mochila_frente,asas,capa,
  capa_frente,tenis,botas,pantufas,laco,tiara,presilha}`, `acessorio_{varinha,balao,ursinho}_{baixo,cima}`.
* Chapéus (4): `chapeu_{coroa,bone,cartola,flor}`.

## Verificação

Três rodadas de prévia (`saida/previa_*.png`, 16 trajes, 22 acessórios e chapéus, 12 combinações com poses).
Corrigido no caminho: faixa do tapa-olho contornando a cabeça, sombras compridas das hastes dos óculos no rosto
(o rosto agora só recorta, sem captar sombra), sombra dos chapéus mais leve (0,2), touca de chef saindo do quadro,
aba do chapéu de chuva entrando no rosto, pala do boné vista de lado.

## Pendências e avisos

* Cabelos com volume passam para fora de algumas peças (espetado acima do boné, cacheado da menina fora do
  capacete do astronauta): a camada de cima não esconde o que está fora da sua silhueta. Aceitável (cara de
  brinquedo); se incomodar, renderizar uma variante de cabelo "amassado" para quando há chapéu.
* A mochila aparece pouco de frente (as alças são o principal); a faixa do tapa-olho deixa uma sombrinha na bochecha.
* `previa_fase7.js` usa `manifesto.json` + `manifesto_fase7.json`; o integrador junta os dois e o
  `ManifestoDaArteTest` precisa aceitar o campo `esconde` (o leitor JSON ignora campos desconhecidos? conferir).
* `arte3d/node_modules` nesta worktree é um link simbólico para o checkout principal (não versionado).
