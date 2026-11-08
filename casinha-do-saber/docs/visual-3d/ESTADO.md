# Estado da execução do PRD visual 3D

Memória do projeto entre sessões. Toda sessão nova lê o PRD e este arquivo, e nada mais, antes de delegar.

## Fases

Situação final em 2026-11-07 (entrega pedida pelo dono do produto, sem terceira rodada de acabamento). "Provada" = o que o CI confirmou no commit 86cf793 (execução 135): build (559+ testes de unidade e capturas, lint, APK, guarda de 150 MB), emulador de tablet (tela cheia), emuladores Android 7 e 8 (abertura 3,1 s, memória máx 113 a 134 MB), testes de voz. NÃO provado: o job do emulador de celular (o emulador some com "device offline"; nunca ficou verde em 15 execuções; a causa exata não foi encontrada).

| Fase | Código e arte | Revisor independente | Situação |
|---|---|---|---|
| 0A. Tela cheia | feito | n/a | tablet verde em 7 execuções; falta o aceite no Samsung (dono) |
| 0. Pipeline de arte | feito | n/a | build verde |
| 1. Personagens | feito | 7 | build verde |
| 2. Interface | feito | 8 | build verde |
| 3. Início e casa de fora | feito | 6 | build verde |
| 4. Jogo | feito | 8 | build verde |
| 5. Prêmio e Quase | feito | 8 | build verde |
| 6. Casa por dentro | feito | 7 | build verde |
| 7. Guarda roupa | feito | 6 | build verde |
| 8. Demais telas | feito | 6 | build verde |
| 9. Acabamento | feito (duas rodadas de arte e Kotlin) | 6; geral do app 7 | build verde; terceira rodada NÃO feita |
| 10. Livro de Colorir | feito em 2026-11-07 e 08 (domínio com 18 testes na JVM, telas, 24 desenhos; testes de tela e gesto verdes no CI 150) | a fazer | ver "Execução das fases 10 e 11" |
| 11. Quebra cabeças | feito em 2026-11-07 e 08 (domínio com 18 testes na JVM, montar com arrastar e com toque, deslizar; testes verdes no CI 150) | a fazer | ver "Execução das fases 10 e 11" |

## O que ficou de fora (dono decidiu entregar assim)
* Terceira rodada de acabamento: 30 figuras de emoji (🏊 😴 🧚 👧 🦸 ⛱ 🐞 🎾 📖), casinha de cachorro 3D, composição das áreas novas (adesivos em pares), nós da trilha todos iguais, números soltos em 33 e no v3d_15 (Álbum 14, Diplomas 1), refinamentos de layout. Lista completa em relatorios/revisao-fase9-rodada2.md.
* Emulador de celular verde no CI (ver acima).
* Aceites que são do dono: tela cheia no tablet Samsung e teste de dez minutos com a criança (roteiro-aceite.md).

## Fases novas pedidas pelo dono (2026-11-07, origem: pedido na sessão)

Regras comuns às duas: arte no pipeline `arte3d/` (WebP, manifesto, orçamento de 400 KB por imagem, APK abaixo de 150 MB); prêmios pelo mesmo caminho que o app já usa (moedinhas, estrelas, adesivos do álbum, itens da casa e diplomas, sem inventar moeda nova); tudo funciona sem internet, sem anúncio, sem compra; nada de tempo limite nem perda ao errar; só avança de nível quem quer; progresso salvo em `EstadoJogo` (dados novos com valor padrão, para não quebrar cópia antiga); voz do narrador pelas mesmas vozes gravadas; área dos pais mostra o que a criança pintou e montou. Cada fase segue o processo das anteriores: briefing, código e arte, revisor independente, CI. Sem Android SDK na nuvem, só o CI prova.

### Fase 10. Livro de Colorir

Não confundir com o ateliê atual (`TelaAtelie`, `Pintura`, quadradinhos em grade): o Livro de Colorir é outra tela, com desenhos de contorno de verdade. O ateliê continua como está.

**Desenhos.** Pelo menos 60 no começo, em 10 coleções de 6 (Bichos da fazenda, Mar, Dinossauros, Princesas e castelos, Carros e aviões, Espaço, Jardim, Casa e família, Festas, Mundo dos mascotes). Arte de linha grossa e fechada, gerada pelo pipeline (SVG para WebP em duas camadas: o contorno e um mapa de regiões, cada região com um número). Cada desenho tem de 12 a 60 regiões, as pequenas só nos mais difíceis. Desenhos novos entram por coleção, sem mexer em código (um `colorir.json` no manifesto).

**Ferramentas, tudo com o dedo (sem texto na tela, ícones grandes e voz):**
* **Balde mágico** (padrão para os pequenos): toca numa região e ela enche com a cor, sem passar da linha. Zero frustração.
* **Pincel**: traço livre com 3 grossuras (fino, médio, grosso). A tinta fica presa dentro da região onde o dedo começou, então não borra para fora do contorno (máscara pelo mapa de regiões). Liga e desliga com o botão "Dentro da linha". Desligado, vira pincel livre (para os maiores).
* **Borracha** e **desfazer / refazer** (20 passos).
* **Paleta**: 12 cores de saída (as do app), mais 12 que abrem com os prêmios; **cores especiais** que se ganham: brilhante (glitter), arco-íris (a cor muda ao longo do traço), metálicas (ouro e prata) e texturas simples (bolinhas, listras, estrelas) para o balde.
* **Carimbos** (estrelinha, coração, flor, pegadas) e **fundo** (céu, campo, mar) como enfeite, depois de pintar.
* **Modo mágico para os pequenos de 2 a 3 anos**: só o balde, regiões grandes, 4 cores.
* **Dica de cor**: botão que acende a próxima região a pintar e fala o nome da cor (junto com o ensino de cores em português e inglês).

**Salvar e mostrar.** A pintura salva sozinha a cada toque (a criança volta e continua). Ao terminar, o botão "Pronto!" mostra a tela de prêmio com o desenho girando. A obra vai para a **Galeria** (quadros na parede do ateliê e na casa por dentro, como já acontece com as pinturas do ateliê). Dá para pintar o mesmo desenho de novo sem apagar o antigo (cada obra tem um número). Os pais podem salvar a obra como imagem na galeria do aparelho.

**Prêmios.**
* Cada desenho terminado: moedinhas, 1 a 3 estrelas (1 por pintar tudo, 2 por usar 5 cores diferentes, 3 por usar uma cor especial) e uma **figurinha do desenho** no álbum (a coleção completa de 6 dá a figurinha dourada).
* Coleção completa: **diploma da coleção** e um item da casa a ser pendurado (quadro grande ou tapete do tema).
* Marcos: 1º, 10º, 25º e 60º desenho dão uma cor especial nova, um carimbo novo ou um mascote como modelo de cor.
* "Pintor do dia": um desenho sugerido por dia, com moedinhas em dobro (junto com a missão do dia existente).

**Arquivos e módulos previstos.** `dominio/Colorir.kt` (modelo, regras, prêmios, testes de unidade sem Android), `ui/telas/TelaColorir.kt` e `TelaEscolherDesenho.kt`, `ui/componentes/TelaDeTinta.kt` (canvas com máscara de regiões e balde por pré-cálculo de regiões, nunca por flood fill em tempo de toque), `arte3d/colorir/` (desenhos e mapas de região). Teste do manifesto de desenhos: cada contorno tem seu mapa, regiões numeradas sem buraco, orçamento de tamanho.

**Aceite.** Capturas do CI: escolha de desenho, pintura em andamento, tela de prêmio, galeria. Teste de unidade: balde enche só a região, pincel preso dentro da linha, desfazer, prêmios por marco, salvar e abrir. Memória: máscara de região em 8 bits (cada desenho ocupa menos de 1 MB decodificado), abre em 3,5 s no Android 7. Aceite do dono: criança colore 3 desenhos em 10 minutos sem pedir ajuda.

### Fase 11. Quebra cabeças

**Tipos, do mais fácil ao mais difícil (o jogo escolhe o tipo e o tamanho pela idade e pelo que a criança já venceu, e deixa trocar):**
1. **Encaixar a peça grande** (2 a 3 anos): 2 a 4 peças com formas bem diferentes, a peça gruda sozinha quando chega perto, com som de "plim". Sem como errar.
2. **Quebra cabeça de imagem** (4 a 8 anos): imagem cortada em grade de 2x2 até 6x8 (4, 6, 9, 12, 16, 24, 36, 48 peças). A peça é arrastada, gira só nos níveis difíceis, e gruda na casa certa com ímã. Moldura mostra uma sombra do desenho (some nos níveis difíceis, volta com o botão de dica).
3. **Peças de formato livre** (tipo jigsaw, com saliências), de 6 em diante, com as bordas separadas primeiro (botão "só as bordas").
4. **Quebra cabeças de lógica** (7 anos em diante): deslizar peças (quebra cabeça de 8 com um espaço vazio, 3x3 até 4x4), montar a figura pelo espelho, completar a sequência de formas. Cada um com dificuldade crescente.
5. **Sombras**: arrastar o bicho/objeto para a silhueta certa (4 anos em diante, seis níveis).

**Imagens.** Pelo menos 60 imagens em 10 coleções (as mesmas do Livro de Colorir mais cenas da casinha, dos mascotes e dos mundos do jogo), cortadas em tempo de execução a partir de uma imagem só (nada de uma imagem por peça). **Ligação com o Livro de Colorir:** a criança pode montar o quebra cabeça da própria obra pintada (a foto do desenho dela vira puzzle, e é por isso que os dois estão juntos).

**Dificuldade crescente.** Trilha de 8 níveis por coleção; passa de nível ao montar, sem tempo e sem perder nada ao errar. Auxílios que a criança pode ligar: sombra, dica (acende a peça certa), "bordas primeiro" e ímã largo. Usar auxílio não tira a estrela, mas a 3ª estrela exige sem dica. Nível adaptativo: se a criança monta 3 seguidas sem dica, o próximo salta um nível; se trava 2 minutos, o jogo oferece uma dica e baixa um degrau na próxima.

**Prêmios.**
* Por quebra cabeça: moedinhas, 1 a 3 estrelas (montou; sem dica; sem dica e rápido sem tempo na tela, só o relógio de areia opcional) e a **figurinha da cena** no álbum.
* Por nível de dificuldade vencido: **peças douradas** que se colecionam e trocam por itens da casa (uma "caixa de peças" na loja).
* Marcos: 1º, 10º, 25º, 50º quebra cabeça dão um mascote, um chapéu, uma moldura para as obras e o diploma "Mestre dos quebra cabeças".
* Coleção inteira vencida: diploma da coleção e uma imagem extra secreta.

**Arquivos e módulos previstos.** `dominio/QuebraCabeca.kt` (cortes, formas de peça, ímã, dificuldade, nível adaptativo, prêmios, testes de unidade), `ui/telas/TelaQuebraCabeca.kt`, `TelaEscolherQuebraCabeca.kt`, `ui/componentes/PecaArrastavel.kt`, `arte3d/puzzle/` (imagens, silhuetas e máscaras de encaixe). Gestos com `pointerInput` (sem bibliotecas novas).

**Aceite.** Capturas do CI: escolha, montando (fácil e difícil), tela de prêmio. Teste de unidade: corte cobre a imagem inteira sem sobra, peça gruda só dentro do ímã, vitória só com todas no lugar, progressão e níveis adaptativos, prêmios por marco. Memória: peças são recortes da mesma imagem em cache, uma imagem decodificada no máximo por vez. Aceite do dono: criança monta um de 12 peças sozinha em até 5 minutos e pede o seguinte.

### Ordem sugerida
Fase 10 antes da 11 (a 11 usa as obras da 10). Dentro de cada uma: (a) arte e manifesto, (b) domínio com testes, (c) telas, (d) prêmios e ligação com álbum, casa e pais, (e) revisão independente e CI. Os dois ganham um botão na casa e no mapa, no mesmo lugar do ateliê e do piano.

### Execução das fases 10 e 11 (2026-11-07, pedido do dono: "cumpra o quebra cabeça e a pintura enquanto o CI roda")

Decisão de desenho: a arte dos desenhos é **vetorial em código** (regiões como elipses e polígonos num quadrado de 0 a 1), desenhada pelo Compose, em vez de WebP do pipeline `arte3d/`. Motivo: o balde, o pincel preso à região e o quebra cabeça viram lógica pura, testável na JVM sem Android; o APK não ganha imagens; e o mesmo desenho colorido serve de imagem do quebra cabeça. Pode ser trocado por arte 3D depois sem mexer na lógica (só a função de desenho muda).

Feito e testado aqui (251 testes do pacote `dominio` na JVM, compilador Kotlin 2.0.21 isolado; 41 deles novos):
* `dominio/Colorir.kt`, `DesenhosDeColorir.kt` (60 desenhos em 15 coleções de 4), `Puzzle.kt` (8 níveis de 4 a 36 peças, montar com ímã, deslizar com solução garantida, nível adaptativo, prêmios e marcos), `ProgressoCriativo.kt` (liga ao `EstadoJogo`: moedinhas, estrelas, obra guardada, peças douradas). Estado salvo em chaves novas do `Repositorio` e da `Copia` (saves antigos ficam com o padrão).
* Telas `TelaColorir` e `TelaPuzzle`, componente `DesenhoNoCanvas`, botões "Colorir" e "Peças" no cantinho criativo da casa por dentro, rotas `Tela.Colorir` e `Tela.Puzzle`. Prêmio do Colorir leva ao quebra cabeça da própria pintura.
* Testes de captura e gesto em `CapturasColorirEPuzzleTest` (ver abaixo o que o CI provou).
* Nomes que colidiam com código existente (`QuebraCabeca` da atividade das fases, `Ponto` da escrita, `Desenho` dos itens da casa) foram evitados: `Puzzle`, `PontoDoDesenho`, `DesenhoDeColorir`.

Provado pelo CI na execução 150 (commit 46088dd): as telas compilam; os 640 testes de unidade e capturas passam, incluindo os 9 novos de colorir e quebra cabeça (balde, pincel preso à região, Pronto paga o prêmio, Pronto sem pintar não paga, menu, montar, deslizar, tocar na peça e na casa encaixa, tocar na casa errada não encaixa, arrastar encaixa); Lint, APK, guarda de 150 MB e permissões passam; tablet passa. Android 7 e 8: FALHARAM por culpa minha, o teste de fontes embutidas achou o símbolo ☆ (estrela vazia) que a fonte do app não tem, usado em `estrelasEmTexto`; trocado por `EstrelasDoDesenho`, que desenha ⭐ com transparência (o desempenho no Android 8 ficou bom: abertura 3,4 s, pico 135 MB). Execução 151 (commit 2da7c27, código final desta rodada, sem a sonda e com `EstrelasDoDesenho`): build COMPLETO verde (640 testes, Lint, APK, guarda de 150 MB, permissões), tablet verde, Android 7 e 8 VERDES de novo (a regressão do ☆ está corrigida). Celular: falhou de novo, desta vez com "No compose hierarchies found" no `EmojisNaFonteEmbutidaTest` (a Activity não chegou a subir) e `device offline` nos outros, a mesma queda do emulador de todas as execuções anteriores (agora 20 sem ficar verde); nenhum teste do celular falhou por asserção do app. A entrega não depende dele.
Execução 152 (commit 3cdd628, com 32 desenhos e o cartão dos pais): build COMPLETO verde (testes e capturas, Lint, APK, tamanho, permissões), tablet, Android 7 e 8 VERDES. Celular: os seis passos terminaram; o job falhou só no passo final com três testes de `<failure>` vazio e `onAfterAll: device offline` (mesmo padrão da execução 137, sem falha de asserção do app). O relatório não dizia o nome desses testes; o CI passou a gravar o nome do teste junto de cada falha (`-B3` no `falhas.txt`), para a próxima execução mostrar se são testes interrompidos no meio ou só o encerramento do emulador.
Execuções 153 a 155 (diagnóstico do job do celular, 2026-11-08): o `falhas.txt` passou a trazer o nome do teste. Os testes marcados com `<failure>` vazio são `abrirMapaEAlbum`, `jogarFase100DeCadaCaminho` e `pintarNoAtelieETocarOPiano`, cada um em um passo diferente e com 52 a 66 s de duração (os que passam levam 22 s): rodaram por inteiro e o logcat do TestRunner mostra `finished` sem `failed`. A falha vem do desligamento do emulador (`onAfterAll: device offline`) que o Gradle pendura no último teste de cada passo, e do aviso "Test run failed to complete. Expected N tests, received 0" (o Gradle não conseguiu buscar os resultados no emulador que sumiu). Decisão: o CI ganhou o passo "Avaliar falhas do emulador", que só conta como falha (a) `<failure>` com texto, (b) falha registrada pelo TestRunner, (c) teste sem registro de fim, (d) "execução incompleta" em que o logcat do passo não mostra todos os N testes esperados terminados. O resto vira "queda do emulador no encerramento" e fica em `docs/emulador/teardown.txt`. Falhas reais continuam em `docs/emulador/falhas-reais.txt`. Na execução 155 ainda restou uma falha REAL de instabilidade: `EmojisNaFonteEmbutidaTest` com "No compose hierarchies found" (a Activity não subiu naquele emulador), que o avaliador novo, corretamente, não perdoa. Falta ver na execução 156 se o job fecha verde com o avaliador completo.
Execução 156 (commit 7592b03, 2026-11-08): PRIMEIRA EXECUÇÃO TOTALMENTE VERDE do projeto. Build completo (640 testes, Lint, APK, tamanho, permissões), tablet, Android 7 e 8 e o CELULAR ("TODOS OS TESTES PASSARAM"), e por isso rodou também o job `publicar-apk-testado` (APK de versão de teste publicado). Como o celular ficou verde, sem enfeitar: o Gradle continua devolvendo erro no encerramento do emulador, mas o avaliador novo confirmou, pelo logcat de cada passo, que os 4, 3 e 3 testes dos passos b, c e d terminaram sem nenhuma falha (arquivo `docs/emulador/teardown.txt`), e não houve falha com texto, falha do TestRunner nem teste sem registro de fim. Uma instabilidade real do emulador (Activity que não sobe) continua reprovando o job quando acontece, como na 155.
Execução 157 (commit 5491f9f, 2026-11-08): SEGUNDA execução totalmente verde seguida (build completo, tablet, Android 7 e 8 e celular), agora com o modo fácil do Livro de Colorir e 40 desenhos em 10 coleções. O celular passou de novo pelo avaliador, com os mesmos passos b, c e d atribuídos à queda do emulador no encerramento (3, 4 e 3 testes terminados no logcat). Duas execuções verdes seguidas indicam que o problema do celular está resolvido de forma estável, mas ele pode oscilar por instabilidade real do emulador.
Execução 158 (commit 501ae30, 2026-11-08): TERCEIRA execução totalmente verde seguida (build completo, tablet, Android 7 e 8 e celular), com o "Pintor do dia" e 48 desenhos em 12 coleções. O avaliador do celular passou de novo.
Execução 159 (commit 67538d8, 2026-11-08): QUARTA execução totalmente verde seguida (build completo, tablet, Android 7 e 8 e celular), com os selos das criações (🏅 por coleção de desenhos completa, 🏆 por marco de quebra cabeças, contados na área dos pais).
Execução 160 (commit a6853a7, 2026-11-08): com os 60 desenhos em 15 coleções, o build completo (testes, Lint, APK, tamanho, permissões), o tablet e o Android 7 e 8 (que checam a fonte dos emojis dos desenhos novos) passaram. O celular também passou (passos terminados e avaliador sem falha real): QUINTA execução totalmente verde seguida.
Execução 161 (commit 57d5477, 2026-11-08): falhou em 30 s por rede do runner, não por código: o `gradlew` não conseguiu baixar `gradle-8.14.3-bin.zip` de `services.gradle.org` ("timeout (10000ms)") e nenhum teste rodou. A reexecução pela API é recusada (403, o token do ambiente não pode reexecutar); a saída é enviar um commit novo, o que foi feito. Se o job de testes cair em poucos segundos com esse erro, é este caso.

Execução 162 (commit bf8a422, 2026-11-08): caiu em 24 s com HTTP 504 ao baixar o Gradle de `github.com/gradle/gradle-distributions` (mesmo caso da 161). Execução 163 (commit 77f1461): primeiro commit com o passo "Baixar o Gradle (tenta de novo se a rede falhar)" nos quatro jobs de teste (até 6 tentativas, espera crescente). Em andamento; o aviso de travou (72ad525) só conta como validado quando ela ficar verde.

Execução 163 (77f1461): o Gradle baixou na 6ª tentativa (7 min), mas o `kotlin-compiler-embeddable` deu 504 no GitHub Releases. Execução 164 (ffc4cc5, repete o gradle em falha de rede): a rede passou e os testes rodaram; só `puzzleTravouSugereADica` falhou, por erro do teste (`assertContentDescriptionContains` sem `substring = true`); a tela já dizia "Dica sugerida: ...". Execução 165 (corrige o teste): o download do Gradle falhou nas 6 tentativas; o limite subiu para 10.

Execução 166 (commit d3670cc, 2026-11-08): o `build` caiu de novo no passo "Baixar o Gradle" depois das 10 tentativas (HTTP 504 em `github.com/gradle/gradle-distributions/releases/download/v8.14.3/gradle-8.14.3-bin.zip`). Da máquina da sessão o mesmo endereço respondeu 502. É indisponibilidade do GitHub Releases, não do nosso código. Nenhum teste rodou desde a 164.

Execução 167 (2026-11-08, retomada): o download do Gradle voltou a responder (HTTP 206 no mesmo endereço, conferido da sessão). Commit enviado sem `[skip ci]` na branch `ccr-bcb49982-1a8ljk` (mesmo código de 15f8c52) para rodar enfim o teste `puzzleTravouSugereADica` corrigido em 7a3b818 e os jobs de emulador. Resultado: TUDO VERDE (run 42474086083). `build` com testes de unidade (inclui `puzzleTravouSugereADica`), capturas, Lint, APK e guarda de 150 MB; `emulador-tablet` (API 34); `emulador-android-antigo` 24 e 26; `emulador` (celular) com os 6 passos, voz incluída. O download do Gradle levou de 2 a 8 s em cada job, sem nova tentativa.

Fase 10, carimbos e fundos (2026-11-08, sessão de retomada): `Carimbos` (estrelinha, coração, flor, pegadas abertos; lua com 10 desenhos; borboleta com 60), `Carimbo` e `Fundo` (sem fundo, céu, campo, mar) na `Obra`; `Colorir.carimbar`, `Colorir.fundo`, `Colorir.carimboEm`; a borracha tira primeiro o carimbo de cima. O prêmio avisa o carimbo novo (`Premio.carimboNovo`). Guardado no mesmo texto da obra com duas partes a mais (`#fundo#carimbos`); obra sem enfeite continua no formato antigo. Desenho vetorial dos carimbos e fundos em `DesenhoNoCanvas` (sem fonte). Tela: ferramentas Carimbo 💮 e Fundo 🏞️; as opções da ferramenta (grossura do pincel, carimbos e tamanhos, fundos) ficam numa linha própria que rola de lado; Fácil e Dica passaram para a linha de baixo, ao lado de Recomeçar e Pronto, para caber no celular. Testes: `CarimbosEFundosTest` (8, rodados na JVM da sessão: 245 do domínio verdes) e a captura `colorirCarimboEFundo` (`s10_colorir_carimbo_e_fundo`), que só o CI prova.

Fase 10, figurinhas, diploma, galeria e guardar como imagem (2026-11-08): `ConquistasDoLivro` (figurinha por desenho terminado e a dourada da coleção, `DiplomaDeColecao`), calculados de `coloridos` como os do jogo; aba "Colorir" no álbum (`TelaAlbum(livroInicial)`), seção "Livro de Colorir" na parede de diplomas; prêmio avisa "Figurinha nova no álbum"; `EstadoJogo.obrasDaGaleria` e o botão "Minha galeria" na escolha de desenhos (quadros em moldura, tocar abre o desenho); `dados/ImagemDaObra` desenha a obra em PNG de 1024 px e abre o enviar do Android pelo FileProvider da cópia, depois da continha de adulto (sem permissão nova). Fundo: só as regiões de cenário (largura inteira) ficam transparentes sem tinta, para o fundo aparecer sem mostrar contornos através da figura.
Execuções 169 a 174: 169 e 170 travaram nos testes de unidade (`performScrollTo` com relógio parado); 172 teve 1 falha de 668 (faltava `Snapshot.sendApplyNotifications()` no teste da galeria); 174 verde nos testes, mas a captura mostrou contornos através do gato, corrigido no 42e01dd. Execução 175 (commit 42e01dd, 2026-11-08): TOTALMENTE VERDE (build com testes, capturas, Lint, APK, tamanho e permissões; tablet; Android 7 e 8; celular) e `publicar-apk-testado` publicou o APK deste commit no release `casinha-teste`.

Lições desta rodada (valem para qualquer tela nova):
* Nome de classe, ponto e desenho colidiam com código existente (`QuebraCabeca`, `Ponto`, `Desenho`); conferir antes de criar tipos no pacote `dominio`.
* Nunca usar símbolo fora da fonte embutida (☆ falhou; ⭐ 🪙 🔒 🧩 e os outros usados passam). O teste `EmojisNaFonteEmbutidaTest` varre o código.
* Overlay de prêmio: `clickable(enabled = false)` funde a árvore de acessibilidade (o teste e o leitor de tela não achavam o conteúdo). Usar `pointerInput(Unit) { detectTapGestures { } }`.
* Peças de arrastar: ler a posição na fase de layout (`offset { }`), e tratar o cancelamento do gesto como soltar (`onDragCancel` também larga a peça). Dar sempre o caminho alternativo de tocar (a peça escolhida vai para a casa tocada), que também serve a quem não arrasta.
* Bandeja com uma casa por peça (sem sobreposição): o tabuleiro fica menor, mas todas as peças aparecem e podem ser pegas.
* Em teste de Compose com `mainClock.autoAdvance = false`, estado mudado dentro de um gesto não recompõe sozinho: chamar `Snapshot.sendApplyNotifications()` antes de avançar o relógio. No aparelho o Choreographer faz isso.
* `performScrollTo` trava o teste quando `mainClock.autoAdvance = false` (a rolagem animada nunca termina): a execução 169 e a 170 ficaram presas nos testes de unidade por isso. Em captura nova, abrir a tela já no estado desejado (parâmetro) ou tocar só no que cabe na tela; se precisar rolar, usar o `rolarAte` de `CapturasPaisTest`.
* O CI agora imprime o resumo dos testes que falharam (passo "Resumo dos testes que falharam"); usar isso em vez de ler o log inteiro. Para diagnosticar teste de UI, uma sonda em semântica (`stateDescription`) impressa na mensagem do `assertEquals` resolveu em duas execuções o que a leitura do código não resolveu em quatro.

Ficou de fora do plano (a fazer):
* Fase 10: (feito em 2026-11-08) os 60 desenhos prometidos estão entregues, em 15 coleções (bichos, mar, casa e jardim, carros e aviões, espaço, festa, dinossauros, castelos e princesas, fazenda, natureza, frutas, brinquedos, clima, mais veículos, música); carimbos e fundos; (feito em 2026-11-08) modo fácil de 2 a 3 anos (botão "Fácil": só o balde e seis cores); galeria das obras na parede; (parcial, feito em 2026-11-08) selos das criações no lugar do diploma: um 🏅 por coleção de desenhos completa (no título da coleção) e um 🏆 por marco de quebra cabeças (menu do jogo), contados no cartão da área dos pais; falta figurinha no álbum e diploma na tela de diplomas, que são calculados por caminho e mundo; (feito em 2026-11-08) "Pintor do dia" (desenho sugerido por dia, 5 moedinhas a mais uma vez por dia; chave `dia_pintor`); salvar a obra como imagem; item da casa pelo marco. Cores especiais: 8 (dourado, prata, cobre, arco íris e 4 brilhos), com marcos em 1, 2, 3, 5, 8, 12, 18 e 25 desenhos.
* Fase 11: peças de formato livre (jigsaw), espelho e sequência, sombras (arrastar para a silhueta), rotação nos níveis difíceis, (feito em 2026-11-08) detecção de "travou": 2 minutos sem avançar destacam a lâmpada, o narrador oferece a dica e o próximo nível sugerido desce um degrau, bandeja em casas sem sobreposição, trocar peças douradas por itens, mascote e moldura por marco, diploma "Mestre dos quebra cabeças".
* (feito em 2026-11-08) Área dos pais mostra o que a criança pintou e montou (`CartaoDasCriacoes`).
* Ao tocar numa região, o desenho não mostra o nome dela por voz.

## Decisões

* 2026-11-07: a branch de trabalho `ccr-162c6a70-urxpmq` foi levada (fast-forward) para o ponto de `claude/casa-saber-graphics-review-02musb`, que tem o app e o PRD. A branch nasceu só com o commit inicial.
* 2026-11-07, Fase 0A: modo imersivo em `MainActivity` (`entrarEmTelaCheia`, chamado em `onResume` e `onWindowFocusChanged`) com `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. As telas já usam `WindowInsets.safeDrawing`, que inclui `displayCutout`, então o recorte da câmera segue respeitado. Dica de fixar o app na área dos pais (`TelaPais`). Teste `TelaCheiaTest` (androidTest) no emulador de celular (job `emulador`, roda todos os testes) e no novo job `emulador-tablet` (perfil `pixel_tablet`, API 34), que também entra no `needs` da publicação do APK.

* 2026-11-07, Fase 0: pipeline em `arte3d/` (`npm run render`), imagens em `app/src/main/assets/arte/` com `manifesto.json`; `ImagemDeArte` (LruCache + inSampleSize); orçamento 400 KB por imagem e 40 MB no total. Relatório em `relatorios/fase0.md`. Escala de render 2x (subir `ESCALA` em `render.js` se faltar nitidez).

* 2026-11-07, Fase 1: 97 camadas por personagem (relatorios/fase1a.md), `PersonagemEmCamadas` compõe um bitmap em cache (relatorios/fase1b.md). Trajes e acessórios da loja caem no desenho antigo até a Fase 7. Não enviar imagens parciais ao git: o teste de órfãos derruba o CI.

* 2026-11-07, Fase 2: fontes Baloo 2 (700/800) e Fredoka, BotaoFofo/BotaoRedondo/cartaoFofo/BalaoDeFala em estilo brinquedo, componentes novos (TextoComContorno, Pilula, EstrelasDeProgresso, ContasDeProgresso, IconeDeTrilha). Relatórios fase2a.md e fase2b.md.
* 2026-11-07: o teste dos chapéus (captura 56) passou a rodar em tela larga; a fila de chapéus não cabia em 393 dp com a criança 3D.
* Regra de processo: push cancela o CI em andamento (concurrency); só enviar depois que a execução anterior fechar.

* 2026-11-07, execução 121 (commit b4659bc, Fases 0A a 2): build verde (testes, capturas, lint, APK, aab); emulador de TABLET verde (TelaCheiaTest passou, tela cheia confirmada no CI); Android 7 e 8 verdes; emulador de CELULAR falhou: o instrumento morreu durante JogoNoEmuladorTest.abrirMapaEAlbum (12 de 22 testes; "esperava 22, recebeu 11"; emulador some 4 s depois). Causa desconhecida (suspeita: memória/GPU com as imagens novas, ou queda do emulador). O CI agora registra as linhas do TestRunner no logcat para identificar o teste.

* 2026-11-07, execução 122 (commit 724782a, + Fase 3B): build verde; tablet, Android 7 e 8 verdes; celular falhou de novo, mas em outro teste (areaDosPaisDesafioDoDia; na 121 foi abrirMapaEAlbum), ~7 min depois do início. Conclusão: o emulador de celular morre por falta de recurso, não por um teste específico. Decisão: RAM 4096M, heap 512M e 4 núcleos no job emulador do CI.

* 2026-11-07, execução 125 (bisseção): DiagnosticoTelasTest sozinha PASSA; areaDosPaisDesafioDoDia sozinho PASSA; JogoNoEmuladorTest inteiro FALHA; o registro MEM mostrou heap Java 20 a 43 MB (limite 192) e nativo até 59 MB, sem crescimento: NÃO é vazamento do app. Conclusão: o emulador de celular (SwiftShader) não aguenta ~12 testes no mesmo processo; decisão: AndroidX Test Orchestrator (um processo por teste) em app/build.gradle.kts. Job emulador-diag removido.

* 2026-11-07: revisão independente das Fases 2, 3, 5 e 7 (relatorios/revisao-fases-2-3-5-7.md): notas 8, 6, 8, 6; lista de pendências da Fase 9 registrada lá.

* 2026-11-07, revisão independente das Fases 4, 6, 8 e 9 (relatorios/revisao-fases-4-6-8-9.md): notas 7, 4, 4, 4; geral 5. Decisão: segunda rodada da Fase 9 em três frentes de arte em lote (lote 1: figuras do vocabulário das perguntas; lote 2: áreas novas, livros e ateliê; lote 3: ícones de interface) mais Kotlin para os números abstratos, a noite do interior e a integração; refazer a revisão ao fim.

* 2026-11-07, integração da arte, rodada 3 (relatorios/integracao-arte.md): lotes 1 (160 `fig_*`), 2 (74 `fig_*`) e 3 (57 `icone_*`) da segunda rodada da Fase 9 juntados num `manifesto.json` só (sem re-render). 825 imagens, 6,12 MB. Conflitos: `fig_sapatilha` do lote 2 virou `fig_sapatilha_baixa`; 📗 📘 📙 ficaram com o `fig_livro` do lote 1 e os três livros coloridos do lote 2 saíram; 🐵 entrou no `fig_macaco`. Cobertura de emojis das perguntas: 90,4 % das aparições (mínimo do teste agora 75 %). Falta o Kotlin ligar os ícones do lote 3 (mapa em relatorios/fase9c-arte-lote3.md).

* 2026-11-07, execução 128 (commit 962c3ad, Fases 0A a 8B + orquestrador + medição): build, tablet e Android 7 e 8 VERDES. Celular: com o Android Test Orchestrator, 23 testes rodaram com 0 falhas (todos passam); o emulador sumiu durante o 24º (DesempenhoTest, que não era para rodar no celular), de novo ~6 min depois do início, como em todas as quedas (121 a 128). Desempenho medido: Android 7 abre em 3,1 s (0,9 s a quente), memória máxima 113 MB; Android 8: 3,1 s, 134 MB (limite 384 MB). Decisão: os testes do celular rodam em dois emuladores seguidos (1: Diagnóstico, Emojis, TelaCheia, Vozes; 2: JogoNoEmuladorTest), cada um abaixo de 6 minutos; DesempenhoTest só nos Androids antigos.

* 2026-11-07, execução 133 (commit 0bcb23b): build (inclui o polimento), tablet, Android 7 e 8 VERDES. Celular em dois emuladores: o passo 1 (Diagnóstico, Emojis, TelaCheia, Vozes; 11 testes) PASSOU com 0 falhas; o passo 2 (JogoNoEmuladorTest, 11 testes pesados) caiu com "device offline" ~8 min depois de o emulador subir. Regra confirmada em 6 execuções: o emulador de celular some depois de uns 6 a 8 min de vida, e o orquestrador deixa cada teste ~35 s. Decisão: JogoNoEmuladorTest dividido em três emuladores (3 + 4 + 4 testes), no total quatro emuladores seguidos no job do celular.

* 2026-11-07, execução 137 (commit 1f2ccee, branch ccr-7bf7dd82-00owm6, primeira com o celular em cinco passos): build COMPLETO, tablet, Android 7 e 8 VERDES. Celular: os 5 passos terminaram, mas o job falhou no passo final. Os testes NÃO falharam (logcat: "0 failed" em todas as execuções lidas); o `falhas.txt` mostra três testcases com `<failure>` vazio e `AndroidTestApkInstallerPlugin onAfterAll: device offline`, ou seja, o emulador sumiu no encerramento. O único passo longo foi o 1 (9 min 16 s; os demais 2 a 3 min), compatível com a regra de 6 a 8 min de vida do emulador. Decisão: passo 1 dividido em dois (Diagnóstico sozinho; Emojis + TelaCheia), seis emuladores no total (o 6º é o de voz, informativo). Falta provar na execução 138.
* 2026-11-07, execução 138 (commit 3aa5fa9, passo 1 dividido, seis emuladores): build COMPLETO, tablet, Android 7 e 8 VERDES. Celular: os seis passos terminaram (7 min 56 s, 5 min 14 s, 2 min 51 s, 3 min 58 s, 3 min 36 s, 1 min 58 s) e o job falhou no passo final por DOIS testes no mesmo passo: `areaDosPaisDesafioDoDia` (timeout real de 10 s esperando a marca "fizemos", linha 911 do JogoNoEmuladorTest) e `pintarNoAtelieETocarOPiano` (95 s e depois `device offline`). O primeiro já falhara na 122 e passou sozinho na 125, então a hipótese é emulador degradado, não bug do app (a tela dos pais usa `verticalScroll`, sem lista preguiçosa). Dividir mais não resolveu: o celular segue sem ficar verde (agora 18 execuções). Decisão: parar de mexer no job do celular nesta rodada; ele continua informando, e a entrega não depende dele.

## Aceites (dono e criança)

Roteiro em `roteiro-aceite.md`. Pendentes:
* Tela cheia no tablet Samsung (dono): pendente.
* Teste de dez minutos com a criança (fases 1, 4 e 5): pendente.

* 2026-11-07, execução 134 (commit fc8d356, com K1, K2, arte da rodada 3 e polimento): build COMPLETO verde (testes de unidade, capturas, lint, APK, guarda de 150 MB), tablet verde, Android 7 e 8 verdes (desempenho: abertura 3,1 s, memória máx 113 a 134 MB). Celular em 4 emuladores: o job falhou com "device offline" em dois passos; o passo 1 levou 14 min (travou) e o passo 4 perdeu o emulador após 2 min, logo depois do teste de voz (TTS). Hipótese em prova: o motor de voz do emulador antecede quase todas as quedas (121 a 134). Decisão: tirar os dois testes que sintetizam voz (VozesGravadasTest e JogoNoEmuladorTest#vozesEmPortuguesEIngles) dos passos normais e rodá-los isolados num 5º emulador, informativo (não derruba o job).

## Retomada (escrito em 2026-11-08 para a próxima sessão, que será com outro modelo)

Estado do código: branch `ccr-bcb49982-1a8ljk` (contém tudo da `ccr-7bf7dd82-00owm6` até 15f8c52, mais a execução 167). Tudo o que está nas Fases 10 e 11 abaixo como "feito" está commitado, enviado e validado pelo CI.

O que está validado pelo CI e o que não está:
* Validado (execuções 156 a 160 e 167, todas verdes, celular incluído): Livro de Colorir com 60 desenhos em 15 coleções, modo fácil, Pintor do dia, selos das criações, cartão dos pais, quebra cabeças de 8 níveis (montar, deslizar, tocar, arrastar) e o aviso de "travou" do quebra cabeça (commit 72ad525, teste corrigido em 7a3b818).
* Pendência de validação: nenhuma. A execução 167 (2026-11-08) fechou o aviso de "travou", que estava aberto desde a 164.

Primeiro passo da próxima sessão (os itens 1 a 3 foram cumpridos na retomada de 2026-11-08 e servem de receita se o download cair de novo):
1. Conferir se o GitHub Releases voltou: `curl -sS -o /dev/null -w "%{http_code}\n" -L -r 0-100 https://github.com/gradle/gradle-distributions/releases/download/v8.14.3/gradle-8.14.3-bin.zip` (esperado 200 ou 206).
2. Se voltou, enviar um commit pequeno e legítimo (por exemplo, anotar aqui o resultado) SEM `[skip ci]`, para disparar a execução. A reexecução pela API não funciona (403 com o token do ambiente). Um push novo cancela a execução em andamento, então só envie outro commit depois que ela terminar.
3. Ler o resultado: `build` (testes de unidade, Lint, APK), `emulador-tablet`, `emulador-android-antigo` e `emulador` (celular). Os logs saem por `curl -L -H "Authorization: Bearer $GITHUB_TOKEN" https://api.github.com/repos/DPMG2025/alura_space/actions/jobs/<id>/logs`.
4. Se o Gradle continuar fora do ar por horas, não mexa no código: aguarde. Alternativa só com checksum oficial: apontar `distributionUrl` para um espelho e fixar `distributionSha256Sum` (o sha256 oficial vem de gradle.org/release-checksums; não consegui buscá-lo da sessão).

O que o CI já faz sozinho contra rede ruim: o passo "Baixar o Gradle (tenta de novo se a rede falhar)" (10 tentativas, espera de 20 s vezes a tentativa) nos quatro jobs de teste, e o `gradlew` do job `build` (testes, Lint, APK) repete até 4 vezes quando a falha é de rede. Os passos do emulador ainda não repetem por falha de rede nas dependências.

Armadilhas já pagas (não repetir): ver "Lições desta rodada" abaixo. As principais: teste de Compose com `mainClock.autoAdvance = false` precisa de `Snapshot.sendApplyNotifications()` depois de gesto; `clickable(enabled = false)` funde a árvore de semântica; símbolos como ☆ não existem na fonte embutida (use ⭐ com alpha); `assertContentDescriptionContains` exige `substring = true` para trecho.

Atualização de 2026-11-08 (tarde): da Fase 10 ficaram FEITOS e validados na execução 175 os carimbos e fundos, a galeria, salvar a obra como imagem, a figurinha no álbum e o diploma da coleção. Faltam da Fase 10: item da casa como prêmio por coleção ou marco, os quadros do livro na parede da sala 3D e pintar o mesmo desenho de novo sem apagar a obra antiga. Branch de trabalho: `ccr-bcb49982-1a8ljk`.

Próximos itens, por ordem sugerida (todos dependem do CI voltar, porque Compose só é provado lá):
* Fase 10: carimbos e fundos; galeria das obras na parede; salvar a obra como imagem; figurinha no álbum e diploma na tela de diplomas (hoje só há selos); item da casa como prêmio por marco.
* Fase 11: peças de formato livre (jigsaw), espelho e sequência, sombras (arrastar para a silhueta), rotação nos níveis difíceis, trocar peças douradas por itens, mascote ou moldura por marco, diploma "Mestre dos quebra cabeças". Os diplomas existentes (`dominio/Conquistas.kt`, `Diploma(trilha, mundo)`) são por mundo completo; um diploma novo exige um tipo novo ou generalizar esse.
* Aceites do dono (só ele faz, roteiro em `roteiro-aceite.md`): tela cheia no tablet Samsung e teste de dez minutos com a criança (fases 1, 4 e 5).

Preferência permanente do dono: nas respostas, não usar hífen nem travessão como pontuação; nomes de arquivos e pastas ficam como estão.

## Problemas abertos (reconciliado em 2026-11-07, origem: leitura do CI e dos relatórios)

* Emulador de celular: FECHADO em 2026-11-08 (execução 156 verde). Causa: o emulador some no encerramento de cada passo e o Gradle pendura o erro no último teste (os testes rodam e terminam). Correção: passos curtos, orquestrador e o passo "Avaliar falhas do emulador", que separa falha de teste de queda no encerramento. Pode ainda reprovar por instabilidade real (Activity que não sobe); nesse caso reexecutar o job.
* Memória das camadas (690 KB cada decodificada): NÃO recortada. Decisão: deixar, pois a medição (113 a 134 MB de 384 MB no Android 7 e 8) não pede; mexer sem compilar local arriscaria desalinhar as camadas. Reabrir só se aparecer despejo de cache em aparelho de 128 MB de heap.
* Pedidos de arte da 2B (glifo da casa e ícones de mapa, álbum, espelho etc.): atendidos pelo lote 3 (57 `icone_*`) e pelos ícones da barra em 3D. Fechado.
* Dúvida do WebP no Robolectric: resolvida, as capturas v3d rodam verdes no CI. Fechado.
* Job que confere imagens desatualizadas: não existe (opcional).
* Aceites do dono: tela cheia no Samsung e teste de dez minutos com a criança, pendentes.
* Pendências sem arte: ver RELATORIO-FINAL.md e revisao-fase9-rodada2.md (terceira rodada, fora por decisão do dono).

## Próxima tarefa a delegar

Fase 0 (pipeline). Briefing:

```
Objetivo: criar casinha-do-saber/arte3d/ com render em lote e manifesto, e uma imagem de teste visível numa captura do CI.
Contexto: leia docs/visual-3d/PRD-visual-brinquedo-3d.md (seções 3 e 4) e docs/visual-3d/prototipo/ (kit.js, shoot.js).
Faça: projeto Node com three@0.160.0; módulos por objeto; script de render com Playwright (Chromium headless, swiftshader) que grava WebP com transparência em app/src/main/assets/arte/ e manifesto.json (tamanho, ponto de apoio, âncoras); carregador no Compose com cache e redução; teste que confere manifesto x arquivos x orçamento de tamanho.
Não mexa em: telas existentes, regras do jogo.
Verifique: rodar o render localmente; teste de unidade do manifesto.
Aceite: uma imagem de teste aparece numa captura do CI.
Relatório: no máximo 25 linhas. Não faça push.
```
