# Fase 9C: arte em lote 3 (ícones de interface que ainda eram emoji)

Data: 2026-11-07. Só arte: sem Kotlin, sem testes, `manifesto.json` intocado. Worktree
`/home/user/alura_space/.claude/worktrees/agent-a5fdba9dfd1acfa1c`, zerada em `ccr-162c6a70-urxpmq` (`917ee8b`);
commit local, sem push.

## Arquivos

* **Novo** `arte3d/src/objetos/iconesDoLote3.js`: 57 modelos no estilo dos ícones da barra (glifo roxo `#5a3df2` em
  relevo, girado como em `interface.js > glifo`, detalhes brancos; lilás `#8f7bff` e roxo escuro `#3f25c4` só para
  separar partes do mesmo glifo).
* **Novo** `arte3d/src/catalogo_fase9c.js` (`CATALOGO=fase9c npm run render`): todos `tipo: 'item'`, `uso` 48 x 48,
  âncora `centro`, mesma câmera dos `icone_*`. Para integrar: `...catalogoFase9c` no `catalogo.js` e juntar
  `manifesto_fase9c.json` ao `manifesto.json`.
* **Novo** `app/src/main/assets/arte/manifesto_fase9c.json` + 57 WebP 96 x 96 (196 KB no total, o maior 5,4 KB).
  Nenhuma imagem de outra fase mudou; nenhum id existia no `manifesto.json`.
* **Novo** `arte3d/previa_fase9c.js`: `saida/previa_fase9c.png` (novos ao lado de 6 antigos: fechar, casa, loja, pais,
  mapa, espelho, em 48 dp e 32 dp no botão redondo, nas pílulas de aba, selinho do certo sobre 5 cores, setas sobre a
  escada) e `saida/previa_fase9c_grande.png` (todos a 96 dp). `saida/` não é versionada.

## Varredura (emojis de ícone de botão, aba, nó ou cabeçalho sem id em `IconesDaBarra`)

Fora do escopo (outros agentes): `fase.emoji` dentro da bolinha (conteúdo da fase: 🃏🧩🎲🔎…, segue `FiguraDaPergunta`),
conteúdo das áreas e dos produtos, figurinhas. Já tinham arte e não foram refeitos: 🪙 (`moeda`), 🎁 (`premio_presente_fechado`),
🏅 (`tela_medalha`), 👆 (`ui_mao_toque`), 💤 (`tela_zzz`), 📜 (`tela_diploma_rolo`), 🔁 (`icone_repetir`), 🔒 (`icone_pais`),
🛒, 🏠, 🚪 (casa), 🎮 (tocar). Ficaram sem ícone (raros ou de adulto): ❔ (diploma vazio), ✅/➕/➖ (área dos pais),
🫙 (geladeira vazia, é ilustração), 🧒 (CenasDesenhadas/Mercadinho, é personagem), 👀/👋 (atividades).

## Mapa emoji -> id (para `IconesDaBarra.porEmoji` ou o mapa de cada tela)

| emoji | id | onde está o emoji hoje |
|---|---|---|
| 👗 | `icone_roupas` | `Gaveta.ROUPAS` (Provador: abas do guarda-roupa e do espelho) |
| 🎩 | `icone_chapeus` | `Gaveta.CHAPEUS` |
| 👓 | `icone_rosto` | `ParteDoCorpo.ROSTO` / `Gaveta.ROSTO` |
| 🧣 | `icone_pescoco` | `ParteDoCorpo.PESCOCO` |
| 🎒 | `icone_costas` | `ParteDoCorpo.COSTAS` |
| 🎈 | `icone_mao` | `ParteDoCorpo.MAO` (só na aba; o 🎈 de conteúdo continua `fig_balao`) |
| 👟 | `icone_pes` | `ParteDoCorpo.PES` |
| 🎀 | `icone_cabelo` | `ParteDoCorpo.CABELO` |
| 🍎 | `icone_frutas` | `PrateleiraDaGeladeira.FRUTAS` (aba; no conteúdo 🍎 segue `fig_maca`) |
| 🥛 | `icone_leite` | `PrateleiraDaGeladeira.LEITE` |
| 🧊 | `icone_congelador` | `PrateleiraDaGeladeira.CONGELADOR` (floco de neve) e o "✔ 🧊" da loja de comidas |
| 🧑 | `icone_pele` | TelaPerfil `Linha("🧑", "pele")` |
| 💇 | `icone_cor_do_cabelo` | TelaPerfil `Linha("💇", "cor do cabelo")` |
| ✂️ | `icone_penteado` | TelaPerfil `Linha("✂️", "penteado")` |
| 👕 | `icone_roupa` | TelaPerfil `Linha("👕", "roupa")` (aba; no conteúdo segue `fig_camiseta`) |
| ✏️ | `icone_escrever` | TelaPerfil "✏️ Adulto: escrever o nome" |
| ⭐ (nó comum) | `icone_no_fase` | tipo de nó "fase comum" (opcional; a bolinha mostra `fase.emoji`) |
| 👑 | `icone_no_chefao` | chefão do mundo (fase.emoji 👑) e TelaMapa (coroa do mundo; dourado = alfa 1, apagado = 0,4) |
| 🏆 | `icone_no_trofeu` | marco vencido em glifo (o 3D dourado `premio_trofeu` continua para o destaque) |
| 🏋️ | `icone_no_treino` | botão Treinar (TelaTrilha) |
| 🔁 (nó) | `icone_no_revisao` | fases de revisão (fase.emoji 🔁) e Relembrar |
| ⚡ | `icone_no_desafio` | fases de desafio (fase.emoji ⚡) |
| ✨ / 🌟 | `icone_no_novidade` | jeito de jogar novo |
| 🔒 (nó) | `icone_no_bloqueado` | cadeado da bolinha fechada (mais gordo que `icone_pais`, lê a 24-26 dp) |
| 🚩 | `icone_no_marco` | bandeirinha do marco (TelaTrilha) |
| ✔ (fundo claro) | `icone_certo` | "✔ usando", "✔ é seu" (TelaCasa, loja) e `Text(" ✔")` da casa completa |
| ✔ (fundo colorido) | `icone_certo_branco` | selinho da fase vencida (TelaTrilha), Provador rodapé "✔", AtividadeCesta, TelaAtelie cor escolhida |
| 📍 | `icone_alfinete` | "você está aqui" (TelaMapa) |
| ⬆️ | `icone_subir` | seta da escada (TelaCasaPorDentro, `medirEmoji(... "⬆️")`): tem contorno branco para ler sobre a cena |
| ⬇️ | `icone_descer` | idem, "⬇️" |
| (baú) | `icone_bau` | aba "Baú de presentes" (TelaCasa; hoje `ID_PRESENTE`) |
| 🔨 | `icone_martelo` | obra no baú (`ItemDoBau`, TelaCasa) |
| 🔖 | `icone_marcador` | TelaLivrinhos |
| 🧭 | `icone_bussola` | TelaDescobrirNivel |
| 🧹 | `icone_vassoura` | TelaAtelie (limpar) |
| 💡 | `icone_dica` | AtividadeEscrever (dica) |
| 🌱 | `icone_broto` | TelaQuemVaiJogar, CartoesDaFamilia, CartoesDosPais (perfil novo) |
| 🛏️ | `icone_area_quarto2` | `AreaDaCasa.icone` (TelaCasa: pílulas dos lugares e "faltam N fases") |
| 🐾 | `icone_area_pet` | idem |
| 🏊 | `icone_area_piscina` | idem (boia listrada) |
| 🛝 | `icone_area_parquinho` | idem |
| 🥕 | `icone_area_horta` | idem |
| 🧸 | `icone_area_brinquedos` | idem |
| 🌳 | `icone_area_arvore` | idem (árvore com casinha branca) |
| 🌷 | `icone_area_flores` | idem |
| 🎨 (área) | `icone_area_estudio` | idem (duas notas; o 🎨 de pintar parede continua `icone_pintar`) |
| 🍢 | `icone_area_gourmet` | idem |
| 📚 (área) | `icone_area_biblioteca` | idem (livro aberto; o botão 📚 continua `icone_estante`) |
| 🔭 | `icone_area_observatorio` | idem |
| 🐄 | `icone_area_fazendinha` | idem (celeiro) |
| ⚽ | `icone_area_quadra` | idem |
| 🐠 | `icone_area_aquario` | idem |
| 🎬 | `icone_area_cinema` | idem |
| 🎣 | `icone_area_lago` | idem |
| 🔬 | `icone_area_laboratorio` | idem (frasco) |
| 🦋 | `icone_area_estufa` | idem |
| 🎉 | `icone_area_festa` | idem (chapeuzinho de festa) |

Sugestão para o Kotlin: as abas e áreas mapeiam pelo enum/id (`"icone_area_" + area.id`, um `idDoIcone` em `Gaveta` e
`PrateleiraDaGeladeira`), não pelo emoji, porque 🎈 🍎 👕 🥕 ⚽ 🧸 também são conteúdo com `fig_*`. Os botões soltos
(🔖 🧭 🧹 💡 🌱 📍 🔨 ✏️) podem entrar no `IconesDaBarra.porEmoji`. Usar `icone_certo_branco` só sobre fundo colorido.

## Verificação

* `CATALOGO=fase9c npm run render`: 57 imagens, sem erro nem `CORTADO` (cerca de 1,6 s cada).
* Conferência (script Python sobre o manifesto à parte): todo arquivo existe; largura e altura = cabeçalho do WebP
  (VP8X/VP8L/VP8) = uso x 2; todos abaixo de 400 KB (maior 5,4 KB); âncora `centro`; nenhum id já no `manifesto.json`.
* Prévias olhadas a 32 e 48 dp ao lado dos antigos: mesmo roxo, mesmo verniz, mesmo giro e ocupação parecida.
  Iterações: mochila refeita (era um bloco que parecia geladeira: agora topo em arco, alças e bolso com tampa),
  "cor do cabelo" refeito (touca com franja de três ondas e gota branca), vara de pescar e peixe mais grossos (a linha
  sumia a 32 dp), capa do livro aberto maior (as páginas brancas tomavam o ícone).

## Pendências

* Kotlin: ligar os ids (mapa acima) e trocar o `medirEmoji("⬆️/⬇️")` da escada por bitmap da arte.
* A bolinha da trilha continua com `fase.emoji` (conteúdo); os ícones de tipo de nó estão prontos se o app passar a
  mostrar o tipo em vez do emoji, ou como selinho.
