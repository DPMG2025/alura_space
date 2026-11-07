# Fase 1, parte A: personagens em camadas (arte)

Data: 2026-11-07. Sem commit nem push por esta tarefa.

## O que foi feito

* `arte3d/src/objetos/personagens.js` (novo): a criança e o irmãozinho montados por **partes** (pernas,
  sapatos, roupa, estampa, mangas, braços por pose, cabeça, olhos, boca com bochechas, sobrancelhas,
  cabelo de trás e da frente por estilo, laço, elásticos das tranças). Cada camada é a figura inteira com
  um papel por parte:
  * `ver`: aparece na imagem;
  * `recorte`: some, mas apaga o que está atrás dela (alfa 0, `NoBlending`). Ex.: o braço que passa atrás
    da cabeça, o olho que afunda na pele, a manga de onde o braço sai;
  * `sombra`: não aparece, mas faz sombra (a cabeça escurece o peito);
  * `capta`: não aparece, mas recebe a sombra (`ShadowMaterial`): a franja escurece a testa.
  Só partes que existem em toda combinação recortam, então qualquer empilhamento fica certo.
* Pele, cabelo e cor da roupa saem em neutro claro (`#d4d4d4`, com luz e sombra) para o app tingir por
  multiplicação. Olhos, boca, bochechas, estampas, sapatos, laço e a jardineira do irmão saem coloridos.
  Sobrancelhas saem em cinza médio e são tingidas com a cor do cabelo (ficam um tom mais escuras).
* **Brilho do cabelo:** multiplicar não clareia, então o cabelo preto perderia os reflexos. Cada cabelo tem
  uma camada `*_brilho*` (só os reflexos, em branco translúcido, sem tingir), feita renderizando o mesmo
  cabelo em preto brilhante e convertendo a luminância em alfa (`posProcesso: 'brilho'` em `pagina.js`).
* `arte3d/src/camadas.js` (novo): a lista das 97 camadas (60 da criança, 37 do irmão; ver tabela), com
  `grupo`, `tingir`, `ordem` e `personagem`. Entra no `catalogo.js`.
* `cameras.js`, tipo `personagem`: lente 26°, câmera em (0; 2,7; 9,6) olhando (0; 1,8; 0), luz fixa
  (não depende do conteúdo da camada) e `quadro` 170 x 260 dp. A criança usa o quadro inteiro
  (340 x 520 px). O irmão usa um recorte de 130 x 196 dp (260 x 392 px) **do mesmo quadro**, centrado e
  alinhado embaixo: mesma escala, então criança e irmão lado a lado, cada um no seu tamanho de uso, ficam
  na proporção certa. O apoio (pés, origem do mundo) é o mesmo em todas as camadas de cada personagem:
  criança (170, 466) px, irmão (130, 338) px.
* `pagina.js`: recorte do quadro (`setViewOffset`), luz fixa, apoio fixo, pós-processo de brilho e checagem
  de borda (camada que encosta na borda faz o render falhar com `CORTADO`). `render.js`: grava os campos
  extras no manifesto.
* `arte3d/previa.js` (`npm run previa`): empilha e tinge no canvas do Chromium (multiply + destination-in,
  o mesmo que `ColorFilter.tint(cor, BlendMode.Modulate)`) e grava `arte3d/saida/previa_personagens.png`
  (não versionado): 4 combinações com o irmão e mais 4 crianças para cobrir os 6 cabelos.
  A função `camadasDaCrianca` / `camadasDoIrmao` dela é a referência de escolha de camadas para a parte B.

## Manifesto

Campos novos nas camadas: `grupo`, `tingir` (`"pele"`, `"cabelo"`, `"roupa"` ou `null`), `ordem`
(de trás para a frente; desenhar da menor para a maior) e `personagem` (`"crianca"`/`"irmao"`).
Os objetos da Fase 0 ficam como estavam. Âncoras (pixels da imagem):

* `*_cabeca`: `cabeca` (centro), `topoDaCabeca` (para chapéus), `olhoEsquerdo`, `olhoDireito`, `boca`;
* `*_bracos_*`: `maoEsquerda`, `maoDireita` (esquerda/direita **da tela**);
* todas: `apoio` = pés.

## Ordem de empilhamento e ids

Estilos: `Perfil.estiloCabelo` 0, 1, 2 = menina `liso` (coques), `cacheado`, `trancas`; menino `liso`
(topete), `cacheado`, `espetado`.

| ordem | grupo | tingir | criança | irmão |
|---|---|---|---|---|
| 10 | sombra | null | `crianca_sombra` | `irmao_sombra` |
| 20 | cabeloTras | cabelo | `crianca_cabelo_tras_{g}_{estilo}` (6) | (não tem) |
| 22 | cabeloBrilhoTras | null | `crianca_cabelo_brilho_tras_{g}_{estilo}` (6) | |
| 25 | enfeiteCabelo | null | `crianca_enfeite_trancas` (só tranças) | |
| 30 | corpo | pele | `crianca_corpo` | `irmao_corpo` |
| 35 | pes | null | `crianca_pes_{menina,menino}` | `irmao_pes_{menina,menino}` |
| 40 | roupa | roupa (irmão: null) | `crianca_roupa_{menina,menino}` | `irmao_roupa_{menina,menino}` (jardineira colorida) |
| 45 | estampa | null | `crianca_roupa_{menina,menino}_estampa` | |
| 50 | cabeca | pele | `crianca_cabeca` | `irmao_cabeca` |
| 60 | boca | null | `crianca_boca_{feliz,torcendo,pensando,surpreso}` | `irmao_boca_*` (4) |
| 62 | olhos | null | `crianca_olhos_{aberto,alegre,fechado,pensando,surpreso}[_cilios]` (10) | `irmao_olhos_*` (10) |
| 64 | sobrancelhas | cabelo | `crianca_sobrancelhas_{feliz,torcendo,pensando,surpreso}` | `irmao_sobrancelhas_*` (4) |
| 70 | cabeloFrente | cabelo | `crianca_cabelo_frente_{g}_{estilo}` (6) | `irmao_cabelo_{menina,menino}` |
| 72 | cabeloBrilho | null | `crianca_cabelo_brilho_{g}_{estilo}` (6) | `irmao_cabelo_brilho_{menina,menino}` |
| 74 | laco | null | `crianca_laco` (menina, liso e cacheado, sem chapéu/acessório de cabelo) | `irmao_laco` (irmãzinha) |
| 80 | bracos | pele | `crianca_bracos_{parado,tchau1..4,torcendo,pensando}` | `irmao_bracos_*` (7) |
| (100+) | acessórios e chapéu | | Fase 7 | |

`{g}` = `menina` ou `menino`. Os braços vêm por último: as mangas fazem parte da roupa e os braços saem
de dentro delas (recorte), e a mão do "pensando" fica na frente do queixo.

Humor para camadas (como na prévia):

| humor | olhos | boca | sobrancelhas | braços sugeridos |
|---|---|---|---|---|
| FELIZ | aberto | feliz | feliz | parado ou tchau1..4 |
| TORCENDO | alegre (^ ^) | torcendo | torcendo | torcendo |
| PENSANDO | pensando (olhar para cima) | pensando | pensando | pensando |
| SURPRESO (e falando) | surpreso | surpreso | surpreso | parado |
| piscar | fechado | (a do humor) | (a do humor) | |

Cílios: `_cilios` para menina (e irmãzinha). Falando: alternar a boca do humor com `boca_surpreso`.

## Verificação

* `npm run render`: 100 imagens (3 objetos da Fase 0 + 60 camadas da criança + 37 do irmão), cerca de 4 min,
  nenhuma camada cortada na borda, nenhum órfão. Pasta `assets/arte` com **644 KB** no total; a maior
  camada tem 20 KB. Duas execuções seguidas dão os mesmos bytes.
* `ManifestoDaArteTest`: 6 de 6 verdes (Kotlin 2.0.21 + JUnit 4 num projeto Gradle temporário fora do
  repositório, `--offline` com o cache, `workingDir` = `app/`).
* Prévia aberta e conferida em zoom em várias rodadas. Corrigido no caminho: bolinhas do vestido por
  dentro do tecido, elásticos das tranças escondidos, sobrancelhas embaixo da franja, nuca aparecendo
  embaixo do queixo e por cima da calota, roupa sem volume (neutro branco estourava na luz; agora
  `#d4d4d4`), cabelo de trás sem brilho. Resultado: alinhamento sem costura, olhos grandes com dois
  brilhos e cílios, sobrancelhas por humor, bochechas, boca aberta com linguinha e dentinhos, ^ ^ ao
  torcer; as poses de tchau e torcendo lembram as maquetes 1 e 3.

## Pendências e avisos para a parte B

* **Memória:** cada camada é do tamanho do quadro (340 x 520 px, ARGB = 690 KB decodificada); uma
  criança tem cerca de 13 camadas. Sugestão: compor a pilha uma vez num bitmap por (perfil, pose, humor)
  e guardar no cache, em vez de desenhar 13 imagens por quadro; ou reduzir com `inSampleSize`.
* Proporção do quadro da criança: 170/260 = 0,654 (o desenho atual usa 0,62). O irmão: 130/196.
* O tingimento escurece um pouco as cores claras (o neutro é `#d4d4d4`, cerca de 83% no sol); se ficar
  escuro demais no app, clarear a cor antes de tingir ou subir `NEUTRO` em `personagens.js`.
* Trajes da loja, acessórios e chapéus em 3D: Fase 7 (a âncora `topoDaCabeca` já existe).
* O irmão não tem estilos de cabelo (cabelinho curto com cacho; irmãzinha com palmeirinha e laço).
