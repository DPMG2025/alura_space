# Fase 9: polimento visual em Kotlin (itens 3 a 8 da revisão das Fases 2, 3, 5 e 7)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-ad27f29b72704faae`, branch
`worktree-agent-ad27f29b72704faae`. Commit do trabalho `4968ffb`, merge de `ccr-162c6a70-urxpmq` em `ccf21fc`
(sem conflitos; trouxe o conserto do `GramaDaIlha` e a Fase 9 de medição). Sem push.

Começado por um agente interrompido (reinício do contêiner) e terminado aqui: o trabalho parcial foi revisado,
corrigido e validado.

## Item a item

1. **TelaInicio (maquete 1)**: feito.
   * Bonecos com o dobro do tamanho: `CenaDaCasa(escalaDasCriancas = 2f)` só na tela inicial; a casa de montar segue
     com 1. Os bonecos maiores são puxados para dentro da cena (`dentroDaCena`), sem corte nas bordas.
   * Espelho (e estante) saíram de cima da casinha (onde tampavam o sol) e foram para o cabeçalho, ao lado dos botões
     redondos; os dois lados têm a mesma largura para a pílula ficar no meio. Descrições iguais às de antes.
   * Estrelas vazias dos cartões na cor do cartão, clarinhas: `EstrelasDeProgresso(corDaVazia = …)` tinge a
     `estrela_vazia` com `filtroDaEstrelaVazia` (matriz de cor, sem imagem nova).
   * Passarinhos: voam só entre as margens (asa inteira dentro) e somem devagar perto delas (`passarinhos(t)`, usado
     no fundo 3D e no desenhado).
   * Missão do dia: pergaminho 3D (`tela_diploma_rolo`) e as moedas que se contam, no lugar de "📜 +6 🪙".
2. **"+N" só com moedas contáveis**: feito. `PecasDoPremio.rotuloDasMoedas` só devolve o número quando as pilhas
   `premio_moedas_N` (N até 5) dão exatamente a conta; `MoedasQueSeContam` é usado na pílula do prêmio, da missão e
   do Relembrar (que antes mostrava "+3" com uma moeda só). O saldo da loja (moeda e saldo total) não é "+N" e ficou.
3. **Contraste**: feito. "Em inglês: <palavra>" agora numa pílula clara com texto roxo escuro (`Cores.TextoDaPilula`).
   Botão colorido desligado (o "Depois") fica fosco (`faceDesligada`: a cor puxada para lilás acinzentado, sem brilho)
   com o texto branco a 3,2:1 ou mais até no alto do degradê; claro desligado só clareia (`Contraste.kt`).
4. **Nomes longos em duas linhas**: feito. Provador e guarda-roupa (capturas v3d_08/09, montados no teste) com
   `maxLines = 2`, centralizados, `TextOverflow.Clip`, colunas de mesma largura; baú da casa (`ItemDoBau`) com
   `maxLines = 2`. O cartão da loja não mostra nome (só miniatura e preço): nada a fazer.
5. **TelaQuase**: feito. Presente 3D `premio_presente_fechado` (46 dp) na pílula, com o desenhado só de reserva
   (nunca emoji). O anel do toque fica dentro da face do botão "De novo" (canto direito, acima do lábio), com espaço
   reservado no texto; `testTag("anel_de_toque")` para o teste conferir que não passa da borda.

## Testes e capturas

* `v3d_04_inicio` (espelho fora do sol, criança com 22% ou mais da largura), `v3d_02` (contraste do desligado),
  `v3d_03`/pílulas (moedas que se contam, vazias na cor do cartão), `v3d_06` ("Em inglês: bed"), `v3d_07` (anel
  dentro do botão), `v3d_08` (nomes em duas linhas): mesmos nomes, asserts novos.
* Nova `v3d_19_polimento` (`CapturasPolimento3DTest`): cinco celulares de 360 dp com os cinco casos e asserts
  (sobreposição espelho x sol, tamanho da criança, moedas, contraste, nomes sem reticências em até duas linhas, anel).
* JUnit novo: `ContrasteTest`, `PecasDoPremioTest.numeroSoAoLadoDeMoedasQueDaoAConta`.

## Validação (sem SDK do Android)

* Compilação do código principal com Gradle temporário `--offline` (Kotlin 2.0.21, Compose Desktop 1.7.3, stubs do
  Android): `compileKotlin` verde com domínio, componentes, tema, atividades e as telas Inicio, Premio, Quase,
  Relembrar, TelasDoMascote e outras. Fora do Desktop: uma linha de `PlatformTextStyle` (API só Android, antiga) e
  `TelaCasa` (depende de `CenaDaArea`); a mudança na `TelaCasa` é só `maxLines = 1` para 2.
* JUnit puro: `PecasDoPremioTest` 6/6, `ContrasteTest` 3/4 verdes; `estrelaVaziaNaCorDoCartao` só cria o
  `ColorFilter`, que no Desktop pede a Skia nativa (fora do cache offline); no Android é JVM normal.
* Corrigido na revisão do trabalho parcial: constante `LARGURA_DA_CRIANCA_NA_CASA` declarada depois de quem a usava
  (erro de compilação), bonecos dobrados podiam sair da borda, e `ID_MOEDA` privado repetido no pacote `telas`
  (renomeado `ID_MOEDA_DO_RELEMBRAR`).
* Os testes Robolectric/Roborazzi (capturas) não rodam aqui: conferir no próximo CI.

## Pendente

* Rodar no CI as capturas `v3d_02/03/04/06/07/08` e a nova `v3d_19_polimento` e olhar as imagens.
* `JogoNoEmuladorTest` não usa os textos e descrições alterados (grep); o espelho e a estante mantêm as descrições.
