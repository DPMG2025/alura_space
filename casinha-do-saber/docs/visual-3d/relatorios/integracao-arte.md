# Integração da arte das Fases 3A, 5A e 7A (rodada 1)

Data: 2026-11-07.

## O que juntou

* `arte3d/src/catalogo.js` importa `catalogoFase5` (`catalogo_fase5.js`) e o catálogo de `catalogo_fase7.js` depois da
  casa (3A). Um `npm run render` completo gera tudo num único `app/src/main/assets/arte/manifesto.json` e apaga
  `manifesto_fase5.json` e `manifesto_fase7.json` (órfãos).
* `render.js` e `src/pagina.js` mantêm os recursos das três fases: câmeras `casa`/`casaItem` (3A), `?catalogo=` com
  `CATALOGO=faseN` incremental e manifesto à parte para revisão, sombra de contato por item e pela base da caixa
  (5A), campo `esconde` no manifesto (7A). Nenhum marcador de conflito restante.
* `InfoDaArte` (`ManifestoDaArte.kt`) ganhou `esconde: List<String> = emptyList()`, lido do manifesto.
* `previa_fase7.js` lê o `manifesto_fase7.json` só se ele existir (depois da integração está tudo no principal).

## Totais

* 363 imagens, 2,51 MB no total (orçamento 40 MB), maior 50 KB (`premio_clarao`; limite 400 KB).
* Por fase: 201 da base e da 3A (personagens, interface, casa de fora e 40 itens), 47 da 5A (`premio_*`, `quase_*`),
  115 da 7A (`roupa_*`, `acessorio_*`, `chapeu_*`; 38 com `esconde`).

## Verificação

* Render completo sem erro nem `CORTADO` (cerca de 25 min no SwiftShader).
* Todas as imagens das fases anteriores saíram idênticas às versionadas, exceto 9 da 5A
  (`premio_confete_bolinha_*` e `premio_presente_abrindo_0/1/2/4`): as versionadas não batiam com o código da 5A
  (nenhum arquivo usado por elas mudou desde o commit da 5A, e o render só da 5A dá o mesmo que o completo). Ficam as
  novas, que são o que o código gera hoje: a pastilha do confete aparece mais de frente e a caixa aberta mostra a
  luz de dentro.
* `ManifestoDaArteTest` 6/6 e `CamadasDosPersonagensTest` 10/10 verdes (JVM puro, Gradle temporário `--offline`,
  Kotlin 2.0.21, `workingDir` = `app/`).

# Rodada 2: Fases 4A (jogo) e 6A (casa por dentro)

Data: 2026-11-07. Branches `worktree-agent-ac8ef9faa61f7388b` (4A, commits f4410a1 e 7160248) e
`worktree-agent-a9f31e3db30f6e1b5` (6A, commits bcd61dd e 19b089a), as duas feitas a partir de 210d507 (sem as
fases 5 e 7), juntadas por `git merge` sobre a rodada 1.

## O que juntou

* Conflitos em `render.js`, `src/pagina.js`, `src/cameras.js` e `package.json`, resolvidos mantendo tudo: câmeras
  `casa`/`casaItem` (3A), `jogo` (4A) e `isometrico` com `ISO` (6A); `?catalogo=` e `CATALOGO=faseN` da rodada 1
  (a 6A tinha uma versão própria, que apagava órfãos do catálogo à parte e poupava as imagens dos manifestos à
  parte no render completo: ficou a da rodada 1, em que o render completo apaga os manifestos à parte); sombras
  (5A), `esconde` (7A), `dados` copiados para o manifesto (4A: `emojis`, `lugar`) e `iso` (6A); `previa:jogo` e
  `previa:dentro` no `package.json`.
* `src/catalogo.js` espalha `catalogoDaFase4` (`catalogo_fase4.js`) e o catálogo de `catalogo_fase6.js`. Os
  catálogos à parte ficam, como os das fases 5 e 7, para o render incremental `CATALOGO=faseN`.
* `manifesto_fase4.json` e `manifesto_fase6.json` juntados ao `manifesto.json` por script (Python), sem re-render:
  entradas e imagens exatamente como vieram das branches (a ida e volta do JSON dá os mesmos bytes; chaves em ordem,
  como o `ordenar()` do `render.js`). Os dois manifestos à parte foram apagados.
* `previa_jogo.js` e `previa_dentro.js` leem o `manifesto.json` (e o manifesto à parte só se existir); as duas
  prévias rodaram.
* `InfoDaArte` (`ManifestoDaArte.kt`) ganhou `emojis: List<String>`, `lugar: String` e `iso: Map<String, Any?>`
  (objetos aninhados como mapas, números como `Double`), todos com padrão vazio, e `numeroIso`/`textoIso`
  (caminho com ponto, como `"referencia.x"`). Teste novo `camposDasFases4E6TemPadraoVazioESaoLidos`.
* `ManifestoDaArteTest`: na câmera `isometrico` o apoio não precisa cair dentro da imagem. É a referência da grade
  (`iso.referencia` projetada), e nas cascas `dentro_piso_banheiro`, `dentro_piso_cozinha`,
  `dentro_parede_banheiro` e `dentro_parede_cozinha` a referência (0, 0, 0) da planta fica fora do recorte justo
  (o relatório da 6A diz "apoio dentro da imagem", o que não vale para essas quatro). As âncoras seguem conferidas.

## Totais

* 481 imagens, 3,78 MB no total (orçamento 40 MB), maior 50 KB (`premio_clarao`; limite 400 KB).
* Novas nesta rodada: 67 da 4A (9 `jogo_*`, 58 `fig_*` com `emojis` e `lugar`) e 51 da 6A (`dentro_*` com `iso`).

## Verificação

* Script: todo arquivo do manifesto existe, nenhum órfão, nenhum id ou arquivo repetido, largura e altura batem com
  o cabeçalho do WebP e com `uso x escala`, âncoras dentro da imagem.
* Render pelo catálogo principal de 5 ids (`fig_abelha`, `dentro_abajur`, `dentro_parede_banheiro`,
  `jogo_comum_lagoa`, `moeda`): o `manifesto.json` saiu byte a byte igual ao juntado por script; as imagens, iguais,
  exceto `jogo_comum_lagoa`, com ruído de até 19/255 em 2% dos pixels (ficou a da branch). O render completo não foi
  rodado (cerca de 25 min).
* `ManifestoDaArteTest` 7/7 e `CamadasDosPersonagensTest` 10/10 verdes (JVM puro, Gradle temporário `--offline`,
  Kotlin 2.0.21, `workingDir` = `app/`).
