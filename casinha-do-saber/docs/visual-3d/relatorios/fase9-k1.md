# Fase 9, segunda rodada de Kotlin, grupo K1: trilha, mapa, ateliê, livros, diplomas e jogos

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a5074200178713d04`, branch
`worktree-agent-a5074200178713d04`, zerada em `ccr-162c6a70-urxpmq` (`22e94e1`, com a arte dos lotes 1, 2 e 3).
Commit local, sem push (hash no fim). Domínio, narração, workflow, `arte3d/`, `PecasDaInterface.kt`, `TelaCasa`,
`TelaCasaPorDentro`, `Provador`, `MainActivity` e `androidTest` não foram tocados. Nenhuma imagem nova.

## Itens da revisão (relatorios/revisao-fases-4-6-8-9.md)

* **2. Trilha e mapa (v3d_16, 04)**: feito.
  * Nó do caminho: o ícone 3D do **tipo** da fase no lugar do emoji e do número (`idDoNoDaFase`): `icone_no_chefao`
    (👑 ou 10ª fase do mundo), `icone_no_revisao` (🔁), `icone_no_desafio` (⚡), `icone_no_novidade` (✨/🌟),
    `icone_no_fase` (o resto); fechada: `icone_no_bloqueado` grande no meio. `fase.emoji` ficou só de reserva sem a
    arte. **Nenhum número na bolinha**: "Fase 24, título" segue só na descrição (o emulador procura "Fase N,").
  * Bandeirinha do marco 🚩 → `icone_no_marco` (vencida segue o troféu 3D); selo da vencida ✔ → `icone_certo_branco`.
  * "Treinar" 🏋️ → `icone_no_treino`; "Já sei bastante!" 🧭 → `icone_bussola`.
  * Cabeçalho: o item do próximo prêmio sem arte da casa (🐶 🐞) cai na figura 3D (`FiguraLeveDoEmoji`); 🎁 já era
    `premio_presente_fechado`. 🎾 não tem figura: continua emoji (falta arte, abaixo).
  * Plaquinha do mundo: figura 3D do mundo e só o nome ("Mundo 7" saiu; "Mundo 7: Selva" na descrição). O nome
    nunca corta em 360 dp: uma linha que encolhe até 80% ou duas linhas menores (`NomeDoMundoNaPlaca`).
  * Relembrar: o número na Corujinha virou contas laranja (até 3); a descrição "…: N para hoje" ficou igual.
  * Mapa: ilhas com a figura 3D do mundo (`FiguraLeveDoEmoji`, um Canvas por ilha); nome sem "3."; 📍 →
    `icone_alfinete`; coroa: `icone_no_chefao` apagado até vencer o chefão, `premio_coroa` dourada depois; a
    pílula de medalhas mostra a medalha e cinco estrelas (`ProgressoVisivel.estrelasAcesas`) em vez do número.
  * Resultado do nível: a pílula mostra a figura e o nome do mundo (novo parâmetro `destaqueFigura` e
    `destaqueDescricao` em `ConviteDaCorujinha`, com padrão; descrição "Mundo N: nome").
* **3. Ateliê e livros (v3d_17)**: feito.
  * Ateliê: 🧹 → `icone_vassoura`; ✔ da cor escolhida → `icone_certo_branco` (roxo `icone_certo` no amarelo e no
    bege); 🖼️ (botão e selinho da miniatura) → `item_quadro` 3D. "Meus desenhos: 1 de 12   🖼️ na sala: 0 de 3" virou
    `ContasDaGaleria`: pincel 3D + 12 contas roxas, quadro 3D + 3 contas amarelas; os números só na descrição
    ("Meus desenhos: 1 de 12. Na sala: 0 de 3"). O aviso de pasta cheia continua texto.
  * Estante: capas com a figura 3D do livrinho, marcador do lido em 3D, 🔖 → `icone_marcador`, marcadores ganhos
    como figuras 3D contáveis (FlowRow), fim do livrinho com o marcador 3D e "Novo marcador na estante" com o ícone.
    Respostas da leitura já eram `FiguraDaPergunta`. Lápis, balde e borracha já eram 3D.
* **6. Diplomas (v3d_15)**: feito.
  * "Faltam 55 fases novas para crescer!" (cantinho do mascote, `TelasDoMascote.kt`) virou "Jogue fases novas para
    crescer!" ao lado das contas (que já existiam); o número foi para a descrição das contas. Campeão: troféu 3D.
  * Enfeites guardados: no lugar de 🔒 e de "60", "120", "200", "400", "700", o presente 3D fechado
    (`premio_presente_fechado`) com o cadeado `icone_no_bloqueado` no canto e uma barrinha de brinquedo que enche com
    as fases vencidas (`BarraDoEnfeiteGuardado`); a descrição ganhou "…, ainda guardado, chega com N fases".
    Enfeites liberados com figura 3D quando há.
  * Parede: "Complete as 10 fases" → "Complete todas as fases"; quadros e mundo mais perto com figura 3D e nome
    (sem "Mundo N" visível; até duas linhas, `minLines = 2` para os quadros terem a mesma altura).
  * **16 (painel do diploma)**: sem `shadow(18.dp)` (blur grande); atrás do papel, halo claro radial, sombra
    desenhada e moldura branca de 5 dp (`desenharDestaqueDoDiploma`): o diploma destaca do cabeçalho escurecido.
    O emoji do mundo virou figura 3D.
* **9. Lagoa e morros**: feito para as cenas do jogo. `ComposicaoDoJogo.camada(..., bordasMacias = true)` apaga as
  pontas (9% de cada lado, degradê `DST_IN`) **uma vez, no bitmap guardado** (chave do cache inclui o modo): lagoa,
  varal, cercado e tapete (que tocam a borda da arte) não terminam mais em corte reto no meio da tela. A ilha do
  caminho só ganha as pontas macias quando é mais estreita que a tela (deitado/tablet). A "faixa de morros" da
  captura 16 é da `TelaInicio`/`CenaDaCasa` (outro grupo): não mexi.
* **10. Cartas e balão**: feito.
  * Memória (`AtividadeMemoria`) e cartas viradas (`AtividadeCartasViradas`): a carta é uma tecla de brinquedo com o
    `ladrilho` do jogo (face em degradê, brilho no alto, borda branca, lábio escuro de 6 dp) e sombra desenhada, no
    lugar da cor chapada com `shadow` de elevação; o verso usa a estrela 3D em vez de ⭐. Marcas `carta_N`,
    `virada_*` e descrições iguais.
  * Balão sobre o globo (17): `cenarioDoJogo` desce a ilha (`ilhaAbaixoDoBalao`) até os enfeites do fundo (globo,
    blocos) ficarem abaixo do balão (186 dp em telas baixas, 218 dp nas outras), sem a ilha passar do pé da tela.
* **11. Capturas 06 e 08**: `06_jogo_subtrair` agora usa a fase "Tirar até 5" (índice 11; era "Maior e menor
  número") e `08_jogo_silabas` a fase "Pedacinhos" (índice 11; era "Traçar letras"). Nomes iguais. A posição dos
  bonecos entre jogos é da `TelaJogo` (fora da lista): não mexi.
* **12. Copo de leite**: `FiguraDaPergunta` desenha as 18 figuras quase brancas (`fig_leite`, `fig_ovo`, `fig_nuvem`,
  `fig_gelo`...; medidas na arte, 82% ou mais de pixels claros) com um contorno lilás meio transparente (a silhueta
  tingida em 8 direções, `desenharArteComContorno`), sem blur.
* **16 (título x texto do pato)**: "O pato nada" e "A pata nada" são do domínio (`Livrinhos.kt`, a história tem o
  pato e a pata): não mexi.

## Peças novas (nomes conferidos com grep, sem repetir no pacote)

* `ui/componentes/ArteLeve.kt`: `FiguraLeveDoEmoji` (figura 3D por emoji num Canvas, com `alfa` e `silhueta`),
  `DrawScope.desenharArteComContorno`; `desenharArte` ganhou `filtro: ColorFilter? = null`.
* `CenarioDoJogo.kt`: `ComposicaoDoJogo.camada(..., bordasMacias = false)` (padrão compatível com o teste),
  `FIGURAS_CLARAS_K1`, `ilhaAbaixoDoBalao` (privados).
* Privados nas telas: `idDoNoDaFase`, `NomeDoMundoNaPlaca`, `ContasDaGaleria`, `ID_DO_QUADRO`,
  `desenharDestaqueDoDiploma`, `BarraDoEnfeiteGuardado`, `LABIO_DA_CARTA`, `LABIO_DA_CARTA_N4`.

## Memória

Só o cache: os ícones e figuras novos passam por `lembrarArteLeve` (LRU da `ArteDoApp`, bitmap amostrado para o
tamanho de uso) e são desenhados em Canvas (sem `BoxWithConstraints` por figura). Os nós do caminho dividem 5
bitmaps de 50 dp. O mapa segura 25 figuras de 64 dp (cerca de 3,7 MB em xxhdpi, nativo). Nenhum bitmap em campo
estático, nenhum blur novo; saíram duas sombras de elevação (cartas) e a de 18 dp do diploma. As bordas macias são
feitas uma vez por bitmap guardado.

## Testes e capturas

* Nova `v3d_20_trilha_sem_numeros` (`CapturasTelas3DTest.trilhaSemNumeros`): cinco celulares de 360 dp (Números com
  mapa e Relembrar, Letras, Inglês no mundo 14 "Vale dos Dinossauros", Lógica nova, mapa). Confere a arte nova no
  manifesto, "Fase 1, ", "Fase 26, ", "Mundo 14: Vale dos Dinossauros", "Mundo 1, Jardim, completo", `abrir_nivel`,
  `abrir_relembrar`, nenhum texto só com número nem "Mundo N"/"N." e nenhum texto saindo do celular.
* `v3d_15`, `v3d_16`, `v3d_17` e as demais: mesmos nomes e asserts (conferi com grep: "Mapa dos mundos", "Álbum de
  figurinhas", "Voltar", "Voltar para o caminho", "Mundo 1, Jardim, completo", "Fase 1, ", `abrir_nivel`,
  `nivel_*`, `abrir_relembrar`, `livrinho_*`, `pagina_*`, `resposta_certa`, `quadro_de_pintar`, `ferramenta_*`,
  `cor_*`, `diploma_aberto`, `figurinha_ganha`, `carta_*`, "3 de N livrinhos lidos", "Página 2 de 5"). Mudam
  sozinhas: 04, 06, 07, 08, 10, 17, 26 e as s5_/s6_ do caminho, mapa, ateliê, estante, álbum e diplomas.
* Compilação: Gradle temporário `--offline` (Kotlin 2.0.21, Compose Desktop 1.7.3 = API do BOM 2024.12.01, stubs de
  `android.*`), `scratchpad/k1` (`k1_prepara.sh`): `compileKotlin` verde com domínio, todo `ui/componentes` (menos
  `CenaDaArea`), `ui/tema`, `ui/telas/atividades` e as telas TelaTrilha, TelaMapa, TelaDescobrirNivel,
  TelaRelembrar, TelaLivrinhos, CenaDoLivrinho, CenaDaHistoria, CenasDesenhadas, TelaAtelie, TelaPiano, TelaInicio,
  TelaPremio, TelaQuase, TelasDoMascote, TelaDiplomas e TelaAlbum; `compileTestKotlin` verde com o
  `CapturasTelas3DTest` (stubs de Robolectric e Roborazzi). Stubs novos só no projeto temporário (`Canvas.drawRect`,
  `LinearGradient`, `PorterDuffXfermode`, `Bitmap.copy`). As capturas não rodam aqui (sem SDK): conferir no CI.

## Fora da lista de arquivos

* `TelasDoMascote.kt` (só `CantinhoDoMascote` e a nova `BarraDoEnfeiteGuardado`): o "Faltam 55 fases" e os
  cadeados numerados do item 6 moram lá. Pode encostar no que outro agente fizer no arquivo.
* `CapturasTest.kt` (índices das capturas 06 e 08, item 11) e `CapturasTelas3DTest.kt` (captura nova).

## Falta arte (continuam emoji de reserva)

* Figuras: 🎾 (prêmio "bola de tênis" no cabeçalho), mundos 🌷 🏖️ 🏙️ 🏰 🏝️ 💎 🎡, capas e marcadores 🌰 🎉 🏨 🐉 🐣 🐿️
  👧 👵 🗺️ 🤡 🥗 🥣 🧚 (e 🌷 🏖️ das capas).
* Ícone para o 🎉 do chefão na história (usei `estrela_brilhando`).

## Riscos para olhar no CI

* A ilha do jogo desce até 78 dp em 360 x 720 (o globo e os blocos saem de trás do balão): conferir 05 a 10, 17,
  v3d_11 e v3d_12.
* O tamanho do ícone do tipo no nó (50 dp numa bolinha de 88) e o contorno das figuras claras.
* As pontas macias da lagoa (9%) com patinhos nas vagas da ponta.

## Commit

Código, testes e este relatório: `ce6d820` (branch `worktree-agent-a5074200178713d04`, sem push); o hash entrou aqui num segundo commit.
