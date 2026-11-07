# Fase 2, parte B: sistema de interface (Kotlin)

Data: 2026-11-07. Sem commit nem push por esta tarefa. Personagens, regras do jogo, workflow do CI e `arte3d/`
não foram tocados. **Depende da arte da parte A** (contas, estrelas, ícones da barra e dos caminhos), que ainda
está sem commit: as duas partes precisam entrar juntas.

## Fontes

* `res/font/baloo2_negrito.ttf` (700) e `baloo2_extranegrito.ttf` (800): TTF estáticos oficiais do Google Fonts
  (API css2 com agente antigo, que entrega `.ttf`; Baloo 2 v1.700). Recortados com `pyftsubset` para latim,
  latim estendido A/B e Adicional, pontuação, setas e € (tiram o devanágari): 417 KB para 96 KB cada, com `GPOS`/`GSUB`
  (kerning) mantidos. Conferidos com fontTools (todas as tabelas abrem) e desenhados com PIL ("Ação! Lição 123 çãõéü").
* Fredoka: as TTF que já estavam no app (`fredoka_medio` 500, `fredoka_negrito` 700) são estáticas e servem.
* `licencas/Baloo2-OFL.txt` (do repositório google/fonts) e linha nos créditos do `README.md`.
* `Tema.kt`: `val Baloo2` (pesos até SemiBold usam o 700; ExtraBold e Black o 800). `display*` e `headline*` em
  Baloo 2 (display e headlineLarge em ExtraBold); `title*`, `body*`, `label*` continuam em Fredoka. Alturas de
  linha iguais às de antes (nenhuma tela cresce). A Baloo tem letra um pouco menor no mesmo tamanho (altura das
  maiúsculas 0,60 em contra 0,70 da Fredoka) e largura parecida: nada que cabia deixa de caber.
* Cores novas em `Cores` (do `ui2.css`): `LabioLilas`, `LabioClaro`, `ContornoDoTitulo`, `LabioDoTitulo`,
  `ContornoEscuro`, `TextoDaPilula`; `Degrades.bloco(cor)`.

## Componentes que trocaram de aparência (mesmas assinaturas)

* `BotaoFofo`: bloco de brinquedo. Face em degradê (`Degrades.bloco`), sombra interna no pé, brilho em elipse no
  alto à esquerda (o `radial-gradient(120% 70% at 30% 0%)` do protótipo, feito com `scale` no `DrawScope`), borda
  branca a 75%, lábio `corBase` embaixo com fio `contorno`, sombra macia. Afunda e amassa ao apertar como antes.
  `contorno = null` agora tira a borda branca e o fio; `brilho` e `sombra` seguem valendo.
* `BotaoRedondo(emoji, descricao, ...)`: face clara com ponto de luz em cima à esquerda (`.redondo`), borda branca,
  lábio lilás `#B9A8EC` de 5 dp. O emoji passa por `IconesDaBarra.para(emoji, descricao)`: com ícone 3D, mostra
  `ImagemDeArte` (56% do botão) e o emoji só fica de reserva se a imagem faltar. Mapeados: ✖ fechar, ⬅ ◀ voltar,
  ➡ ▶ voltar espelhado (avançar; menos quando a descrição começa com "Ouvir"/"Tocar"), 🔊 🔇 som, ⚙ ajustes,
  🔒 pais, 🏠 casa, 🛒 🛍 loja. Só emojis que o app já usa, para o teste de emojis do emulador não mudar.
  Botões coloridos (loja aberta amarela, noite azul) usam a própria cor clareada no ponto de luz.
* `Modifier.cartaoFofo`: corpo em degradê, brilho no alto, sombra em duas camadas; lábio e contorno como antes.
* `BalaoDeFala` / `Modifier.balaoDeFala`: lábio lilás sólido de 5 dp (`.balao`), sombra macia, raio 24 dp,
  contorno mais fino. O texto continua `titleMedium` (Fredoka).
* `Estrela` (e `FileiraDeEstrelas`): `estrela_cheia`/`estrela_vazia` (acima de 48 dp, `estrela3d`); sem a arte,
  o desenho antigo. Isso já muda a trilha (14 dp), a área dos pais (16 dp) e o prêmio (64 dp).
* `Modal.kt` e `Setas.kt` não precisaram mudar: o ✖ do `CartaoModal` é um `BotaoRedondo`.

## Componentes novos

* `PecasDaInterface.kt` (Kotlin puro): `IconesDaBarra`, `IconeDaBarra(id, espelhado)`, `idDoIconeDaTrilha(trilha)`,
  `CorDaConta` (ordem da arte), `EstadoDaConta`, `ProgressoVisivel.contas(feitas, total, comAtual)` e
  `ProgressoVisivel.estrelasAcesas(vencidas, total, estrelas)` (primeira estrela já na 1ª fase, última só com tudo).
* `InterfaceDeBrinquedo.kt`: `TextoComContorno(texto, modifier, estilo, cor, corDoContorno, corDoLabio, espessura,
  profundidade, maxLinhas, alinhamento)` (camadas de contorno decorativas, fora da acessibilidade; folga em volta para
  o traço não ser cortado), `Modifier.pilula()`, `Pilula`, `PilulaDeValor(icone, valor)`, `EstrelasDeProgresso`
  (com `brilharUltima`), `ContasDeProgresso(feitas, total, cor)` (a da vez amarela e maior; encolhem para caber),
  `IconeDeTrilha(trilha)`; em `Comuns.kt`, `Modifier.faceDeBrinquedo` e `IconeDeArteDaBarra`.

## Uso nas telas (só trocas simples)

* `TelaInicio`: título "Casinha do Saber" com `TextoComContorno`; ícone 3D do caminho no lugar do emoji (40 dp,
  o mesmo espaço); botão da área dos pais com a face nova e a engrenagem 3D no lugar do ⚙️ (toque longo igual).
* `TelaTrilha`: ícone 3D do caminho no cabeçalho (quando não há botão do mapa).
* Ficam para as Fases 3 e 8: o "x/250" e a barrinha dos caminhos no início (trocar por `EstrelasDeProgresso` com
  `estrelasAcesas`), placar em pílula, contas no jogo (`TelaJogo` ainda não usa `ContasDeProgresso`).
* Área dos pais: só fonte, botões, cartões e estrelinhas novos (pelos componentes).

## Capturas (novas, nenhuma renomeada)

`CapturasInterface3DTest` (Robolectric, 360 x 800 dp): `v3d_02_interface_botoes` (título, 10 botões redondos com
ícones 3D, respostas 3 5 4 6 em blocos coloridos com número de contorno escuro, "Pôr na casinha", branco e
desabilitado) e `v3d_03_interface_pilulas` (barra do jogo ✖ + contas + som, pílulas 27 e +5, contas de todas as
cores, 12 contas encolhidas, estrelas com a brilhando, 4 ícones dos caminhos, balão, dois cartões). Antes de
capturar, confere: arte no manifesto e decodificável; cada botão da barra existe pela descrição e **nenhum emoji
mapeado aparece**; **nenhum texto com `hasVisualOverflow`** (cortado ou com reticências); nenhum "/" na tela.
As capturas 15, 16 e 17 (pais, início e jogo em 360 dp) mudam sozinhas pelos componentes.

## Verificação feita aqui

* `PecasDaInterfaceTest` (novo, 4 testes) e `ManifestoDaArteTest` (6): verdes, JUnit puro com Kotlin 2.0.21 num
  projeto Gradle temporário fora do repo (`workingDir` = `app/`).
* Compilação sem erro contra Compose Multiplatform desktop 1.7.3 (API do BOM 2024.12.01), com stubs mínimos de
  `android.*`, `LocalContext`, `R.font` e `Font(resId)`: `Tema.kt`, `Comuns.kt`, `InterfaceDeBrinquedo.kt`,
  `PecasDaInterface.kt`, `ImagemDeArte.kt`, `ManifestoDaArte.kt`, `Isometrico.kt`, todo o `dominio/` e o
  **`CapturasInterface3DTest`** (com `ui-test-junit4-desktop` e stubs de Roborazzi e Robolectric).
  `TelaInicio.kt` e `TelaTrilha.kt` revisados à mão (imports e assinaturas). Rodar as capturas não deu: o runtime
  desktop pede artefatos do `dl.google.com` (bloqueado). Quem prova o visual é o CI.

## Pendências e pedidos de arte

* **Ícone branco** (casa, e talvez seta) para botões coloridos como o verde "Pôr na casinha" da maquete 3:
  variante do `ROXO` em `interface.js`. Hoje dá para usar só o glifo roxo.
* Ícones da barra que ainda são emoji: 🗺️ mapa, 📒 álbum, 🪞 espelho, ☀️/🌙 dia e noite, 📚 estante, 🔁 de novo,
  ⏹️/⏺️/👂 (piano e gravação), ↩️ desfazer, 📄 papel novo, 🗑️ apagar. Pedir à arte: mapa, livro/álbum, espelho,
  sol, lua, repetir, desfazer, play/ouvir (triângulo), parar, gravar, lixeira.
* Risco a olhar no CI: a borda branca e o brilho em botões muito pequenos (12 a 24 dp de raio) e o contraste do
  texto branco em botões amarelos claros (sem contorno, como antes).
