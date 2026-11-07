# Fase 5, parte A: arte do Prêmio e do Quase

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a7fe13964a84652f2`, branch
`worktree-agent-a7fe13964a84652f2` (partindo de `ccr-162c6a70-urxpmq`, ce09c73). Commit local, sem push.
Sem Kotlin, sem testes.

## O que foi feito

* `arte3d/src/objetos/premio.js` (novo): presente rosa de fita amarela **oco** (parede extrudada com borda
  boleada, forro mais escuro por dentro), tampa com laço de duas alças, facho de luz quente, clarão, faíscas
  saindo; pódio branco torneado com faixa amarela e friso dourado (oval: z 0,62, como o palco quase de frente
  da maquete); pilhas de moedas em pirâmide (reusa `moeda()`); faíscas; confete em 4 formas x 6 cores;
  coroa e troféu; estrela dormindo (forma e cor da `estrela_vazia`, olhinhos fechados, bochechas, "z z");
  semente num vasinho em 4 estágios.
* `arte3d/src/catalogo_fase5.js` (novo): as 47 entradas. **Não** mexi no `catalogo.js` nem no `manifesto.json`.
* `render.js`: `CATALOGO=fase5 npm run render` (ou `CATALOGO=fase5 node render.js <ids>`) abre
  `render.html?catalogo=fase5`, renderiza só esse catálogo, sempre incremental (nunca apaga arquivos) e grava
  `app/src/main/assets/arte/manifesto_fase5.json`. Sem `CATALOGO` o comportamento é o de antes.
* `src/pagina.js`: carrega `catalogo_<nome>.js` pelo parâmetro `?catalogo=`; se o modelo tiver
  `userData.base`, a sombra de contato é a dessa parte (a caixa), não a do conjunto (tampa voando, moldura).
* `src/objetos/interface.js`: só `export` em `halo()` e `faisca()` (reusados).
* `arte3d/previa_premio.js` (novo): monta a tela do Prêmio como a maquete 3 só com as imagens do app
  (estrelas `estrela3d`, moeda, presente abrindo, clarão, patinho como prêmio, pódio, irmãos em camadas
  torcendo, confete), lado a lado com a maquete, mais um painel do Quase: `arte3d/saida/previa_premio.png`.

Reusados sem duplicar: `estrela3d`, `estrela_cheia`, `estrela_vazia`, `estrela_brilhando`, `moeda`.

## Ids (tipo, uso em dp; arquivo no dobro). Total 528 KB, maior 50 KB (`premio_clarao`)

| id | tipo | uso | âncoras |
|---|---|---|---|
| `premio_presente_fechado` | objeto | 180 | boca, centro, tampa |
| `premio_presente_abrindo_0` .. `_4` (0 fechado, 1 tampa levanta, 2 sobe com luz, 3 voando com brilho máximo e faíscas, 4 aberto com luz macia) | objeto | 220 x 240 | boca, centro, tampa |
| `premio_caixa_aberta` (vazia, sem tampa, sem luz: o prêmio sai da `boca`) | objeto | 180 | boca, centro |
| `premio_tampa` (tampa solta, para animar à parte) | item | 120 | centro |
| `premio_clarao` (luz com raios, atrás do presente) | item | 220 | centro |
| `premio_podio` | objeto | 340 x 120 | centro, esquerda, direita, fundo (onde ficam os pés) |
| `premio_moedas_1`, `_2`, `_3`, `_5` | objeto | 72 | centro |
| `premio_faisca_branca`, `premio_faisca_dourada` | item | 32 | centro |
| `premio_confete_{papel,estrela,bolinha,serpentina}_{vermelho,amarelo,verde,azul,roxo,rosa}` (24) | item | 24 | centro |
| `premio_coroa` | item | 96 | centro |
| `premio_trofeu` | objeto | 120 | centro |
| `quase_estrela_dormindo` | item | 120 | centro |
| `quase_semente_1` .. `_4` (semente, broto, plantinha com botão, flor) | objeto | 100 x 140 | topoDaTerra |

Os 5 quadros de abrir têm **o mesmo recorte, a mesma luz e o mesmo apoio** (moldura invisível com a tampa em
todas as posições); os 4 estágios da semente também. Dá para trocar um pelo outro sem a caixa pular.

## Verificação

* Render em ~2 min; prévias em `arte3d/saida/` (folhas de contato e `previa_premio.png`). Iterações: caixa oca
  (o furo da extrusão falhava porque o canto do furo saía do contorno: raios iguais), pódio (perfil do torno
  invertido mostrava o lado de dentro; achatado para oval), alças do troféu, pontas da coroa, moedas em
  pirâmide (contáveis: cada estrela aparece), confete bolinha virou pastilha.
* Os 97 WebP dos personagens e os demais não foram tocados.

## Para o integrador

* Juntar: no `catalogo.js`, `import { catalogoFase5 } from './catalogo_fase5.js'` e `...catalogoFase5`; as
  entradas de `manifesto_fase5.json` vão para `manifesto.json`; depois apagar `manifesto_fase5.json`.
* Até juntar, o `ManifestoDaArteTest` acusa os `premio_*`/`quase_*` como órfãos, e um `npm run render`
  completo (sem `CATALOGO`) os apagaria.
* No app: o Quase hoje usa `🔒` e `🎁` (emojis); `icone_pais` (cadeado roxo) e `premio_presente_fechado`
  servem. "Você é um campeão!" (casa completa) pode usar `premio_coroa`/`premio_trofeu`.
