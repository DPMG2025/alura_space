# Fase 2, parte A: ícones 3D e peças de interface (arte)

Data: 2026-11-07. Sem commit nem push por esta tarefa. Kotlin, workflow e camadas dos personagens não foram tocados
(os 97 WebP dos personagens saíram idênticos byte a byte no render completo).

## O que foi feito

* `arte3d/src/objetos/icones.js` (reescrito): os quatro ícones dos caminhos com a **Baloo 2 ExtraBold** em 3D
  (antes era a helvetiker da three). Blocos de brinquedo com cantos redondos, verniz e letra branca em relevo;
  globo de mesa com continentes de plástico, meridiano e haste dourados, inclinado; argolas do arco-íris na base roxa
  com a bolinha rosa. Cores como na maquete 1 (3 amarelo, 1 vermelho, 2 azul; C roxo, A verde, B laranja).
* `arte3d/fonts/baloo2_800.typeface.json` (novo, 36 KB): a Baloo 2 800 convertida da woff2 que já estava em
  `arte3d/fonts/` para o formato da three (fontTools, só 0-9, A-Z, a-z, `+?!`). Contornos TrueType, sem inversão.
  Para regerar: decomprimir a woff2 com fontTools e gravar `glyphs[c].o` com `m`, `l`, `q x y cx cy`.
* `arte3d/src/objetos/interface.js` (novo): estrelas (cheia dourada, vazia em cinza lilás, brilhando com halo
  quente, raios e quatro faíscas), contas do progresso (bolinha levemente achatada, 6 cores + vazia perolada) e os
  ícones da barra como **glifos roxos (`#5a3df2`) em plástico com verniz**, levemente girados para mostrar a
  espessura, feitos de traços com pontas redondas (cápsulas), arcos de tubo e formas extrudadas com bisel redondo.
  Foram pensados para ficar dentro do botão redondo claro (`ui2.css: .redondo`), como o X, a engrenagem e o
  alto-falante das maquetes.
* `src/catalogo.js`: 22 entradas novas (lista abaixo). `src/pagina.js`: carrega a fonte 3D antes de `PRONTO` e
  prende o `apoio` dentro da imagem (nos glifos finos e girados o meio da base da caixa projetava 1 a 3 px fora do
  recorte e o `ManifestoDaArteTest` acusou; nas imagens antigas nada muda).

## Ids (tipo, uso em dp; arquivo no dobro)

| id | tipo | uso | KB |
|---|---|---|---|
| `icone_trilha_matematica` (blocos 1 2 3) | icone (três quartos) | 96 | 7,5 |
| `icone_trilha_portugues` (blocos A B C) | icone | 96 | 7,7 |
| `icone_trilha_ingles` (globo) | icone | 96 | 7,7 |
| `icone_trilha_logica` (argolas) | icone | 96 | 7,1 |
| `estrela_cheia`, `estrela_vazia` | item | 48 | 2,9 / 2,4 |
| `estrela_brilhando` (com halo e faíscas) | item | 72 | 8,6 |
| `conta_vermelha`, `conta_laranja`, `conta_amarela`, `conta_verde`, `conta_azul`, `conta_roxa`, `conta_vazia` | item | 32 | até 2,0 |
| `icone_fechar`, `icone_voltar`, `icone_som_ligado`, `icone_som_desligado`, `icone_ajustes` (engrenagem), `icone_pais` (cadeado), `icone_casa`, `icone_loja` (sacola com estrela) | item | 48 | até 4,0 |
| `moeda` (já existia, Fase 0) | item | 64 | 4,9 |

Os sufixos das trilhas seguem `dominio/Trilha` em minúsculas (`MATEMATICA`, `PORTUGUES`, `INGLES`, `LOGICA`):
`"icone_trilha_" + trilha.name.lowercase()`. Contas na ordem do enum de cores: `CORES_DAS_CONTAS` em `interface.js`.
A estrela grande do prêmio continua sendo `estrela3d` (120 dp).

**Coração/vida:** o app não tem vidas (só a almofada de coração, item da casa, que é da Fase 3/6); não foi feito.

## Verificação

* Prévias (não versionadas) em `arte3d/saida/`: `previa_trilhas.png`, `previa_barra.png`, `previa_progresso.png`,
  `previa_v2.png`. Cada peça em tamanho nativo, a 80 px e a 40 px, sobre céu claro e sobre roxo escuro, e dentro de
  um botão redondo claro. Duas rodadas: na segunda, a engrenagem ganhou dentes mais retos (parecia flor), o roxo
  ficou mais azulado (como o das maquetes), as letras dos blocos mais brancas e o halo da estrela brilhando menor.
  Todas legíveis em 48 dp (e ainda reconhecíveis a 40 px); fundo transparente, borda limpa nos dois fundos.
* `npm run render` completo: 122 imagens em cerca de 5 min, nenhum órfão, pasta `assets/arte` com 776 KB; a maior
  peça nova tem 8,6 KB. Depois do ajuste do apoio, re-render só de `estrela_brilhando`, `icone_ajustes`, `icone_fechar`.
* `ManifestoDaArteTest`: 6 de 6 verdes (Kotlin 2.0.21 + JUnit 4, Gradle 8.14.3 `--offline` num projeto temporário
  fora do repositório, `workingDir` = `app/`).

## Pendências para a parte B (Kotlin)

* Os glifos da barra são roxos. Para o ícone de casa em branco dentro do botão verde (maquete 3, "Pôr na casinha"),
  ou se quiser glifos de outra cor, renderizar uma variante (trocar `ROXO` por parâmetro) em vez de tingir.
* O tamanho de uso dos glifos é 48 dp (o botão da maquete tem 58 dp com glifo de cerca de 30 dp): sobra nitidez.
* Licença OFL da Baloo 2 não está em `casinha-do-saber/licencas/` (só a da Fredoka); a fonte já era usada desde a
  Fase 0 e agora também vira geometria. Vale acrescentar o arquivo.
