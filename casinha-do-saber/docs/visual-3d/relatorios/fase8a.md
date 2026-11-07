# Fase 8, parte A: arte das demais telas (só arte)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a77549d2667286bc4`, branch
`worktree-agent-a77549d2667286bc4` (zerada em `ccr-162c6a70-urxpmq`, `bdb62d1`). Commit local, sem push; o hash está na
mensagem de entrega. Sem Kotlin e sem testes.

## Arquivos

* **Novo** `arte3d/src/catalogo_fase8.js`: 56 entradas. Render: `CATALOGO=fase8 npm run render` (incremental), manifesto
  à parte `app/src/main/assets/arte/manifesto_fase8.json` para o integrador juntar (`...catalogoFase8` no `catalogo.js`).
* **Novos** `arte3d/src/objetos/iconesDaBarra.js` (17 glifos), `mascotes.js` (Corujinha e os 3 mascotes),
  `enfeites.js` (mão, diplomas, figurinhas, livros, dormir, piano, ateliê).
* `arte3d/src/objetos/interface.js`: só ganhou `export` em `roxo`, `brancoPlastico`, `glifo`, `traco`, `arcoDeTubo`,
  `poligono`, `extrudar` (as imagens da Fase 2 não mudam).
* **Novo** `arte3d/previa_fase8.js`: folha de prévia em `arte3d/saida/previa_fase8.png` (não versionada): ícones dentro
  do botão redondo claro, casinha branca no botão verde, poses e enfeites sobre fundos das telas.
* 56 WebP novos em `app/src/main/assets/arte/` (495 KB no total, maior 25 KB). Nenhuma imagem de outra fase mudou.
  Nenhuma câmera nova: tudo nas câmeras `item` e `objeto`.

## Ids (tipo, uso em dp; arquivo no dobro)

**Ícones da barra** (`item`, 48, âncora `centro`, mesmo estilo dos 8 da Fase 2: glifo roxo `#5a3df2` em relevo):

| id | emoji / onde |
|---|---|
| `icone_avancar` | ➡️ ▶️ (próxima página do álbum, próxima fase): seta própria, sem espelhar a luz |
| `icone_mapa` | 🗺️ TelaTrilha |
| `icone_album` | 📒 TelaMapa |
| `icone_estante` | 📚 TelaLivrinhos |
| `icone_espelho` | 🪞 TelaInicio, TelaCasa, TelaCasaPorDentro |
| `icone_sol` / `icone_lua` | ☀️ / 🌙 virar dia e noite (TelaCasa) |
| `icone_pintar` | 🎨 pintar as paredes (TelaCasa): paleta roxa com bolinhas de tinta coloridas |
| `icone_tocar` | ▶️ quando a descrição começa com "Ouvir"/"Tocar" (CartoesDasVozes) |
| `icone_parar` | ⏹️ (piano e gravação) |
| `icone_gravar` | ⏺️ microfone (CartoesDasVozes) |
| `icone_ouvir` | 👂 "Ouvir a música" (TelaPiano): fone de ouvido (a orelha não lia bem a 30 dp) |
| `icone_repetir` | 🔁 (TelaPiano) |
| `icone_desfazer` | ↩️ (TelaAtelie) |
| `icone_papel_novo` | 📄 (TelaAtelie) |
| `icone_lixeira` | 🗑️ (CartoesDasVozes, ateliê) |
| `icone_casa_branca` | glifo branco da casa para o botão verde "Pôr na casinha" (maquete 3): a porta é vão, o verde aparece |

O 🔊 de "Ouvir de novo" já é `icone_som_ligado`. Para a 8B: acrescentar os ids ao `IconesDaBarra.porEmoji`/`todos`.

**Interface** (`item`, 72): `ui_mao_toque` e `ui_mao_toque_apertando` (luva branca de punho roxo, dedo para cima, no
lugar do 👆 da `MaoGuia`/`Comum.kt`). Âncora `ponta` = ponta do dedo; os dois quadros têm a mesma moldura invisível
(mesmo recorte), o segundo sobe um pouco e mostra o anel lilás do toque.

**Mascotes** (`objeto`, com sombra de contato; poses com a mesma moldura invisível, mesmo recorte e mesmo apoio, para
trocar sem pular):

* `mascote_coruja_{parada,falando,feliz,dormindo}` (180 x 200): Corujinha Professora no galho, óculos roxos, capelo
  com pingente dourado, livrinho azul. Falando: bico aberto e asa gesticulando; feliz: olhos ^ ^ e asa erguida;
  dormindo: olhos fechados, cabeça inclinada e Zzz. Âncoras `cabeca` e `bico`.
* `mascote_{cachorrinho,gatinho,dragaozinho}_{parado,falando,feliz,dormindo}` (200 x 200): de pé, cabeça grande, cores
  de `MascoteDesenhado.kt` (mais saturadas); coleira com plaquinha no cachorro e no gato; dragão com chifres, crista,
  asinhas e rabo de coração. Falando: boca aberta e acenando; feliz: braços para cima, olhos ^ ^; dormindo: Zzz.
  Âncoras `cabeca` e `topoDaCabeca` (para os acessórios do mascote no futuro). O estágio é escala no app
  (`EstagioMascote.escala`), como no desenho antigo.

**Enfeites** (`tela_*`):

* Diplomas: `tela_diploma_rolo` (objeto 120), `tela_medalha` (item 80 x 110, âncora `topo`), `tela_selo` (item 80).
  O troféu já existe: `premio_trofeu` (Fase 5).
* Álbum: `tela_figurinha_{matematica,portugues,ingles,logica}` (borda na cor de `Trilha.corArgb`),
  `tela_figurinha_dourada` (chefão), `tela_figurinha_vazia` (lugar pontilhado), item 80 x 96, âncora `centro` no meio
  da janelinha branca onde o app desenha o emoji da figurinha.
* Livrinhos e história: `tela_livro_fechado` (objeto 120), `tela_livro_aberto` (objeto 160 x 120).
* Dormir: `tela_lua_dormindo` (item 160, lua de touca), `tela_estrela_noite` (item 40), `tela_zzz` (item 64).
* Piano: `tela_piano` (objeto 200 x 140, oito teclas nas cores de `Nota`), `tela_nota_musical` (item 40, para as
  notinhas que sobem das teclas).
* Ateliê: `tela_paleta` (item 120), `tela_pincel` (item 96), `tela_lapis` (âncora `ponta`), `tela_balde`,
  `tela_borracha` (item 64; ferramentas ✏️ 🪣 🧽 de `Ferramenta`).

## Verificação

* `CATALOGO=fase8 npm run render`: 56 imagens em cerca de 3 min, sem erro nem `CORTADO`; manifesto à parte com
  apoio e âncoras dentro das imagens.
* Prévia `node previa_fase8.js` olhada em várias iterações: glifos com profundidade e verniz como os da Fase 2;
  mascotes em vinil saturado com brilho e sombra de contato lilás; enfeites com cores vivas.
* Correções na iteração: detalhes brancos dos glifos que caíam todos no centro (posição sobrescrita), orelha trocada
  por fone de ouvido, mascotes mais saturados e mais justos no recorte, listras do gato, bico da coruja maior, mão
  mais gordinha, paleta inclinada para a câmera (de cima ela sumia).

## Fora (pendências)

* Acessórios do mascote (`AcessorioMascote`: laço, boné, óculos, cachecol, mochila, medalha, coroa, capa) em 3D: as
  âncoras `cabeca`/`topoDaCabeca` já estão no manifesto; faltam as peças e a regra de camadas.
* Diploma aberto/certificado grande (`TelaDiplomas` desenha o papel em Canvas) e papel de parede.
* Teclas soltas do piano (as do Compose são esticadas no layout; uma imagem distorceria): só o pianinho de enfeite.
* Ícones menos frequentes que seguem emoji: 🖼️ pendurar na sala, 🔖, ✔️, 📍, 🚩, 🏋️, 🎁 da trilha, ✂️/👕/💇 do perfil,
  👦👧🧑 do perfil, 🌱 de quem vai jogar, e os emojis de título (📜, 🎹, 🎉).
* Figurinhas com o desenho próprio (o app continua com o emoji por cima do fundo 3D).
* Áreas novas da casa, itens `quarto2.*`/`pet.*` e as 17 atividades novas (não entraram nesta parte).
