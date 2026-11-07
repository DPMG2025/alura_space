# Integração da arte das Fases 3A, 5A e 7A

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
