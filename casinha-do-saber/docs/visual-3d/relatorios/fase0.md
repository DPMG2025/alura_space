# Fase 0: fundação do pipeline de arte 3D

Data: 2026-11-07. Sem commit nem push (fica para a sessão principal).

## O que foi feito

### `casinha-do-saber/arte3d/` (pipeline)

* `package.json`: `three@0.160.0` e `sharp@0.35.5` (WebP). `playwright` está como dependência *peer opcional*: o npm não instala outra cópia e o `require('playwright')` usa a instalação global do ambiente, que casa com os navegadores em `/opt/pw-browsers`. Uma cópia nova do Playwright pediria outra versão do Chromium. Em outra máquina basta ter o Playwright instalado (global ou `npm i playwright`) e rodar `npx playwright install chromium`.
* `package-lock.json` versionado. O `.gitignore` ignora `node_modules/` e `saida/` (prévias em PNG).
* Kit do protótipo dividido em módulos:
  * `src/kit/palco.js` (renderizador, luzes), `materiais.js` (plástico, massinha, pele, ouro), `formas.js` (caixa, bola, cápsula, cilindro, `naSuperficie`, e `ancora()`, que é nova), `rosto.js`; `src/kit.js` reexporta tudo.
  * `src/objetos/`: um arquivo por objeto: estrela3D, moeda, patinho, crianca, irmaozinho, laco3D, casinha, ilhaDeGrama, arvore, arbusto, flor, nuvem, presente, confete, icones (fonte da three por caminho absoluto `/node_modules/...`). Conferi que todos carregam e montam no Chromium.
  * Evoluções: `estrelaDourada()` (bisel mais liso, inclinada); `moedaDoPlacar()` (moeda com aro saltado e um leve giro); `patinhoDaLagoa()` (cor `#ffc400`, em três quartos). No protótipo os olhos do patinho ficavam por dentro da cabeça e não apareciam (nem na maquete 2); agora estão na superfície, com brilho. Âncoras: `centro` (estrela, moeda), `cabeca` e `topoDaCabeca` (patinho), `cabeca` (criança e irmãozinho, para a Fase 1).
* `src/cameras.js`: câmera fixa por tipo.
  * `objeto`: no chão, de frente e um pouco de cima, com sombra de contato (mancha macia). É o caso do patinho.
  * `item`: flutua, de frente e sem sombra. É o caso da estrela e da moeda.
  * `icone`: três quartos.
  * `personagem`: posição e lente fixas, sem recorte, para as camadas da Fase 1 ficarem alinhadas pixel a pixel.

  Nos tipos `ajustar`, a direção e a lente são fixas e a imagem é recortada em volta do objeto com `setViewOffset`, o que não distorce a perspectiva. O filtro final do protótipo (`saturate(1.22) contrast(1.04)`) é aplicado num canvas 2D.
* `src/catalogo.js`: lista do que renderizar (`id`, `tipo`, `uso` em dp, `montar`). Hoje traz `estrela3d` (item, 120 dp), `moeda` (item, 64 dp) e `patinho` (objeto, 120 dp).
* `src/pagina.js` e `pagina/render.html` são o lado do navegador: `window.renderizar(id, escala, superamostra)` devolve o PNG, o apoio e as âncoras em pixels.
* `render.js` (`npm run render`, ou `npm run render -- moeda` para renderizar só alguns):
  1. Sobe um servidor local na porta 0, abre o Chromium headless com `--use-angle=swiftshader --enable-unsafe-swiftshader --ignore-gpu-blocklist` e renderiza no dobro do tamanho de uso (`ESCALA = 2`), com superamostragem 2x reduzida por Lanczos.
  2. Grava `app/src/main/assets/arte/<id>.webp` (qualidade 85, alfa 100, effort 6) e o `manifesto.json` com chaves ordenadas. As prévias em PNG vão para `arte3d/saida/`.
  3. Sem argumentos, o manifesto fica igual ao catálogo e os arquivos órfãos são apagados.
* Fontes Baloo 2 e Fredoka copiadas do protótipo para `arte3d/fonts/` e carregadas pela `render.html`.

### Manifesto (`app/src/main/assets/arte/manifesto.json`)

```
{ "versao": 1, "escala": 2, "imagens": { "<id>": {
    "arquivo": "<id>.webp", "tipo": "item|objeto|...", "largura": px, "altura": px,
    "uso": {"largura": dp, "altura": dp}, "apoio": {"x": px, "y": px},
    "ancoras": {"<nome>": {"x": px, "y": px}} } } }
```

`apoio` é a projeção do meio da base (y mínimo) da caixa do objeto. As âncoras são os objetos `ancora()` do modelo, projetados pela câmera final.

### Kotlin

* `ui/componentes/ManifestoDaArte.kt`, em Kotlin puro (sem Android): `ManifestoDaArte.ler(json)`, `InfoDaArte` (com `fracao(ponto)`), `PontoDaArte`, `amostraDaArte(...)` (a maior potência de 2 que não deixa nenhum lado menor que o alvo) e um leitor de JSON mínimo (`LeitorJson`, interno). Escolhi não usar `org.json` porque no teste JVM puro ele é só um stub.
* `ui/componentes/ImagemDeArte.kt`:
  * `@Composable ImagemDeArte(id, modifier, descricao, filtro, alinhamento, escala, reserva)`. Usa um `BoxWithConstraints(propagateMinConstraints = true)`. Sem tamanho, a imagem aparece no tamanho de uso do manifesto, limitada pelo espaço; com tamanho, preenche esse tamanho (Fit).
  * O `inSampleSize` é calculado pelo tamanho final em pixels.
  * `reserva` aparece quando o id não existe ou a imagem não decodifica, e servirá para o emoji de reserva da Fase 4.
  * `filtro` serve para tingir na Fase 1 (`ColorFilter.tint(cor, BlendMode.Modulate)`).
  * `ArteDoApp` guarda o manifesto, lido uma vez, e um `LruCache<String, Bitmap>` medido em KB (1/8 da memória), com chave `arquivo@amostra`. A decodificação é síncrona: é determinística nas capturas e barata para imagens pequenas. Se as cenas grandes pesarem, dá para trocar por `produceState` com IO na Fase 9.
* Nenhuma lib nova. As APIs usadas existem no Compose 1.7 (BOM 2024.12.01): `BoxWithConstraints(modifier, contentAlignment, propagateMinConstraints, content)` e `Image(bitmap, contentDescription, modifier, alignment, contentScale, colorFilter)`.

### Testes

* `src/test/.../ui/componentes/ManifestoDaArteTest.kt` é JUnit puro, sem Robolectric. Confere:
  * que a versão do manifesto é 1 e que ele não está vazio;
  * que todo arquivo do manifesto existe;
  * o orçamento: `ORCAMENTO_POR_IMAGEM` = 400 KB e `ORCAMENTO_TOTAL` = 40 MB para a pasta;
  * que não há arquivo órfão nem dois ids no mesmo arquivo;
  * que a largura e a altura batem com o cabeçalho do WebP (VP8X, VP8L e VP8) e com uso x escala;
  * que o apoio e as âncoras ficam dentro da imagem;
  * o leitor de JSON e o `amostraDaArte`.
* `src/test/.../ui/CapturasArte3DTest.kt` (Robolectric e Roborazzi, mesma configuração do `CapturasTest`) gera a captura **nova** `v3d_00_arte_de_teste.png`:
  * as três imagens no tamanho de uso, em 48 dp (redução 2x) e o patinho grande em 220 dp, sobre um céu em degradê;
  * um id inexistente, que mostra a reserva;
  * antes da captura, confere que cada WebP sai dos assets do app com a largura certa e com alfa, e que as seis imagens estão na árvore semântica.

  Nenhuma captura existente foi tocada.

## Verificação

* `npm run render` gera as três imagens em cerca de 7 s:

  | Imagem | Tamanho | Peso |
  |---|---|---|
  | estrela3d | 240x240 | 7,0 KB |
  | moeda | 128x128 | 4,9 KB |
  | patinho | 240x240 | 12,5 KB |

  Duas execuções seguidas deram arquivos idênticos byte a byte (md5).
* Abri as imagens compostas sobre xadrez e sobre céu azul. O fundo é transparente (canto com alfa 0) e a borda é limpa. O visual bate com as maquetes 2 e 3: ouro com verniz, patinho de borracha brilhante, sombra de contato macia.
* `ManifestoDaArteTest` foi compilado e rodado de verdade com Kotlin 2.0.21 e JUnit 4, num projeto Gradle temporário fora do repo, com `workingDir` = `app/`: 6 de 6 verdes. Com um `orfao.webp` posto de propósito, o teste de órfãos falhou como esperado.
* `ImagemDeArte.kt` e `CapturasArte3DTest.kt` não foram compilados: falta o SDK do Android e o `dl.google.com` está bloqueado. Revisei os imports e as assinaturas à mão.

## Riscos e pendências

* **Decodificação de WebP no Robolectric (modo NATIVE).** Espero que funcione, porque o modo nativo usa os decodificadores reais. Se falhar, o `CapturasArte3DTest` acusa com a mensagem "'<id>' não decodificou". A saída seria gerar PNG só para o teste ou trocar o formato.
* **Escala de saída.** O manifesto grava "uso x 2" (`escala: 2`), como pede o PRD. Em celulares xxhdpi (3x), uma imagem no tamanho de uso máximo fica levemente ampliada. Para mais nitidez, basta subir `ESCALA` em `render.js`: o teste exige `largura = uso x escala`, então os dois andam juntos.
* Job opcional do CI para conferir se as imagens commitadas estão atualizadas: não feito, porque não era para mexer no workflow.
* As licenças das fontes (OFL) já devem estar em `casinha-do-saber/licencas/`. Não conferi.
