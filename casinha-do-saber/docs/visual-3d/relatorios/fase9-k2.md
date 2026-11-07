# Fase 9, segunda rodada de Kotlin, grupo K2: casa de fora e por dentro, áreas novas, guarda-roupa, espelho e noite

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-ad4aca35f2a9cdef6`, branch
`worktree-agent-ad4aca35f2a9cdef6`, zerada antes em `ccr-162c6a70-urxpmq` (`22e94e1`, com as 825 imagens). Commit do
trabalho: `7107211` (este relatório vem num commit seguinte, só de documentação). Sem push. Itens da revisão
(`revisao-fases-4-6-8-9.md`) deste grupo: 1, 4, 5, 7, 13, 14 e 15. Nenhuma arte nova: só a do manifesto.

## Item a item

1. **Áreas novas (v3d_18, v3d_18b)**: feito.
   * Pílulas dos lugares e a próxima área trancada (`TelaCasa.LugaresDaCasa`) e as portinhas dos cômodos novos
     (`ComodosNovos`) com `icone_area_<AreaDaCasa.id>` (20 áreas, pelo id, nunca pelo emoji; a figura do emoji fica
     só de reserva): `IconeDaAreaK2`.
   * Coisas das áreas: `ArteDasAreas.para` já resolve as `fig_*` novas pelo emoji (conferido: 245 de 356 coisas com
     3D; a boia 🛟 ganhou `fig_boia`, o teste antigo que a esperava como emoji foi corrigido). Composição:
     `ArteDasAreas.posicaoNaIlhaK2` dá a cada coisa ainda no lugar de partida (a grade do domínio) um desvio fixo
     pelo id (até 0,035 da largura e 0,022 da altura), sem sair da ilha nem subir acima do horizonte; o que a
     criança arrastou fica exatamente onde ela soltou (arrastar a partir do ponto desenhado, sem salto).
   * Obra: martelinhos 3D (`icone_martelo`) no lugar de 🔨/🚧.
2. **Casa por dentro (32, 32b, 33, 35, v3d_13)**: feito.
   * Escada: `icone_subir`/`icone_descer` (bitmap da arte, mesmo ponto e mesmo balanço da setinha; o emoji só se a
     imagem faltar). Novos campos `icone`/`iconeEm` na `Peca`.
   * Moeda do topo: `moeda` 3D + saldo em Baloo 2 numa pílula; descrição "Você tem N moedinhas" mantida.
   * Comida: na geladeira aberta (âncoras `comida_i`), na mão e no painel da geladeira e da lojinha pela
     `FiguraDaPergunta`/`fig_*` (emoji de reserva); abas das prateleiras com `icone_frutas`/`icone_leite`/
     `icone_congelador` (pelo enum); botão de comprar com `icone_loja` + ícone da prateleira; `🫙` pela figura se
     houver. O "zzz" de dormir virou `tela_zzz`.
   * Luz e céu: o fundo agora é o céu da casa de fora (7CC4FA, B8E2FF, FFE6D6) e a grama da arte (A9E56F), os
     morrinhos na mesma grama; a faixa de céu usa o `item_sol` e as nuvens `ceu_nuvem_1/2` da casa de fora (o
     desenho antigo fica de reserva).
3. **Guarda-roupa e espelho (34, 36)**: feito.
   * `Provador`: abas com `icone_roupas`, `icone_chapeus`, `icone_rosto`, `icone_pescoco`, `icone_costas`,
     `icone_mao`, `icone_pes`, `icone_cabelo` (`IconesDaCasaK2.daGaveta`); ícone do canto pela figura 3D da peça,
     ✖ pelo `icone_fechar`, preço com a moeda 3D, "usando" com `icone_certo`, gaveta vazia com `icone_loja`.
   * Espelho (`TelaPerfil`): linhas com `icone_pele`, `icone_cor_do_cabelo`, `icone_penteado`, `icone_roupa`; o
     "✏️ Adulto: escrever o nome" virou `icone_escrever` + "Adulto: escrever o nome".
   * Vazio branco à direita do retrato (painel do guarda-roupa, captura 34): agora tem o título "Guarda-roupa"
     com contorno (diminui para caber em 360 dp) e uma pílula com os ícones 3D das gavetas que estão vestidas.
   * Lojinhas (de fora e de dentro): `RotuloDaLojaK2` (cadeado, certo + "usando"/"é seu", certo + congelador, mão
     + moeda + preço) no lugar de "✔ usando", "✔ 🧊", "👆 🪙 3".
4. **Noite do interior (v3d_13b)**: feito. A `MainActivity` guarda `noiteNaCasa` (o botão sol/lua da casa de fora,
   novo `TelaCasa(onNoite)`; a casa de fora também reabre como estava) e passa `noite` à `TelaCasaPorDentro`. De
   noite: céu escuro igual ao de fora, grama e morrinhos multiplicados por `CamadasDaCasa.NOITE`, lua 3D
   (`item_sol_noite`), estrelinhas no lugar das nuvens e as janelas noturnas (já existiam).
5. **13: baú, silhueta, espelho sobre a nuvem**: feito.
   * 🎁: a aba do baú já usava `premio_presente_fechado`; o 🎁 que sobrava era o aviso "🎁 3 presentes!" da tela
     inicial (capturas 03/14): agora o presente 3D + "3 presentes!".
   * Próximo presente com cadeado: `ArteDasAreas.silhuetaK2` escolhe a arte real (`item_cama`, a figura da coisa
     ou da obra, como a vaca; por último o ícone da área) e `SilhuetaDoProximoK2` a tinge de cinza azulado por
     matriz de cor (mantém luz e sombra), no lugar da mancha marrom.
   * Nuvens das áreas saíram do canto de cima à esquerda (x 0,34 e 0,70), onde fica o botão do espelho.
   * ✔ da casa completa e 🔨 das obras do baú em 3D.
6. **14: mão de dica solta sobre o telhado (03)**: feito. A caixa da mão (64 dp, mais alta que o aviso) puxava o
   `BottomEnd` para baixo; agora a mão alinha no `TopEnd` com a ponta do dedo no canto de baixo do aviso.
7. **15: refazer a captura 11**: `CapturasTest.premio` abre o presente antes (como 38/39): mostra o objeto ganho e
   "Pôr na casinha". Mesmo nome.

## Símbolos novos (varridos com grep; um só de cada)

`IconesDaCasaK2` (`PecasDaInterface.kt`), `SituacaoNaLojaK2` e `RotuloDaLojaK2` (`Provador.kt`), `IconeDaAreaK2`
(internal, `TelaCasa.kt`); `ArteDasAreas.silhuetaK2` e `posicaoNaIlhaK2`; privados com sufixo K2 em `TelaCasa` e
`TelaCasaPorDentro`. Em `IconesDaBarra`: `SUBIR`, `DESCER`, `BUSSOLA`, `DICA`, `MARTELO`, `MARCADOR`, `VASSOURA`,
`BROTO`, `ALFINETE`, `ESCREVER`, `TREINO`, `BANDEIRA`, `CERTO`, `CERTO_BRANCO`, `BAU` (todos em `todos`); `porEmoji`
ganhou ⬆️ ⬇️ 🧭 💡 🔨 🔖 🧹 🌱 📍 ✏️ 🏋️ 🚩 ✔️ (os de conteúdo, como 🎈 🍎 👕 🎁 🪙, não entram; o outro grupo pode
usar `IconesDaBarra.para` ou as constantes nas telas dele).

## Preservado

Descrições e testTags (grep antes): "Casa por dentro...", "Você tem N moedinhas", "Geladeira aberta",
"Guarda-roupa aberto", "<prateleira>: N", nomes das gavetas, "Escolher <linha>", "Gaveta vazia...", "Próximo
presente: ...", "Baú de presentes, ...", "Lojinha, você tem N moedinhas", `lugares_da_casa`, `lugar_*`,
`comodo_novo_*`, `comodo_aberto_*`, `area_*`, `mao_da_dica`, `mascote_na_casa`, "Entrar na casa", "Espelho:
trocar a aparência". Assinaturas só ganharam parâmetros com padrão (`TelaCasa.onNoite`).

## Testes e capturas

* `PecasDaInterfaceTest`: novos `botoesSoltosDaFase9CViramIcones` e `abasDaCasaPeloEnumOuPeloId` (todos os ids no
  manifesto, 20 áreas). `ArteDasAreasTest`: boia corrigida, novos `silhuetaDoProximoPresenteUsaAArteDeVerdade` e
  `lugarDePartidaGanhaUmDesvioPequenoEOArrastadoNao`.
* Nova `CapturasCasaEAreasK2Test`: `v3d_21_casa_e_areas_k2` (casa por dentro de noite com o guarda-roupa, geladeira
  de dia, horta, baú vazio com a silhueta; confere arte, descrições e nenhum 🪙 ✔ 🎁 ⬆ ⬇ 🔨 de sistema) e um caso em
  360 dp (guarda-roupa: textos dentro da tela, sem 🪙 ⬆ ⬇).
* Mudam sozinhas, mesmos nomes: 03, 11 (agora aberto), 12 a 14b, 18, 32 a 37, 58 a 60, v3d_08/09 (provador),
  v3d_13, v3d_13b, v3d_18, v3d_18b, v3d_19.

## Verificação feita aqui (sem SDK do Android)

* JVM puro (Gradle temporário `--offline`, Kotlin 2.0.21, `workingDir` = `app/`): `PecasDaInterfaceTest` 6/6,
  `ArteDasAreasTest` 6/6, `CamadasDaCasaPorDentroTest` 10/10, `CamadasDaCasaTest` 8/8, `FigurasDaPerguntaTest` 6/6,
  `ManifestoDaArteTest` 7/7: 43 testes, 0 falhas.
* Compose Desktop 1.7.3 (API do BOM 2024.12.01) com stubs de Android/áudio/Robolectric: `TelaCasa`,
  `TelaCasaPorDentro`, `ComodosNovos`, `TelaPerfil`, `TelaInicio`, todo `ui/componentes`, `dominio` e
  `atividades` compilam sem erro nem aviso novo; `CapturasCasaEAreasK2Test` e `CapturasAreasNovas3DTest` compilam.
  `MainActivity` (Android) conferida à mão. As capturas não rodam aqui: quem prova o visual é o CI.

## Memória

* Tudo por `ImagemDeArte` (cache do app) ou pelo cache da `ArteDaCasaPorDentro` (criado pela tela, limpo ao sair):
  setas (2), sol/lua (1), nuvens (2), zzz (1) e as comidas que estão na geladeira, todas decodificadas no tamanho
  de uso (ladrilho x 0,3 a 1,7). Nenhum bitmap em estático; o filtro cinza da silhueta é uma matriz de cor num
  `remember`; sem blur. Estrelinhas da noite: 9 círculos por quadro.

## Pendências e riscos para o CI

* Sem arte (fica emoji de reserva): 111 coisas das áreas (🦄 🎾 🏰 🎠 🔭 🌍 🛋️...), a placa 🚧 da obra (virou
  martelo), o 🫙 da geladeira vazia se não houver figura. O botão do andar (casinha com escada) segue desenhado.
* Olhar nas capturas: o tamanho da seta 3D sobre a escada (0,62 ladrilho), o sol 3D no céu de dentro, o contraste
  da silhueta cinza no cartão creme, o título do guarda-roupa em 360 dp e a mão encostada no aviso da tela inicial.
