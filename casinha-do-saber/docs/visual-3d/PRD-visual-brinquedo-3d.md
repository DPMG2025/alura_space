# PRD: visual "brinquedo 3D" da Casinha do Saber

Documento para executar a troca do visual do app inteiro. Escrito em 7 de novembro de 2026, depois que a filha do dono do produto escolheu a opção 2 entre as maquetes (brinquedo 3D) e a opção 3 (aquarela).

## 0. Leia antes de tudo: como executar este PRD

**A execução é grande e demorada.** São dezenas de telas, centenas de imagens e muitas rodadas de CI. Se tudo for feito na sessão principal, o contexto enche, a sessão compacta a toda hora e perde detalhes. Por isso:

**A sessão principal só orquestra. O trabalho pesado é delegado a subagentes.**

Regras da sessão principal (orquestradora):

1. Lê este PRD e o arquivo de estado `docs/visual-3d/ESTADO.md` (criar na primeira sessão; ver seção 9). Não relê o código inteiro.
2. Quebra cada fase (seção 6) em tarefas pequenas e entrega cada uma a um subagente, com um briefing completo (modelo na seção 8).
3. Recebe de volta só um resumo curto. Não abre arquivos grandes nem dezenas de imagens: confere no máximo duas ou três capturas por fase.
4. Faz commit e push, e acompanha o CI (ou delega a espera a um subagente que devolve só "verde" ou o erro).
5. Atualiza `ESTADO.md` ao fim de cada tarefa: o que foi feito, decisões, número da execução do CI, próximos passos. É a memória do projeto entre sessões e depois de cada compactação.

Regras dos subagentes:

* Cada subagente recebe um objetivo fechado, os arquivos que deve ler, o que não pode mexer, como verificar e o critério de aceite.
* Devolve um relatório de no máximo 25 linhas: o que mudou, arquivos tocados, como verificou, o que ficou pendente. Detalhes longos vão para um arquivo em `docs/visual-3d/relatorios/` e o relatório só aponta o caminho.
* Não faz push. Quem faz push é a sessão principal (evita corrida com o CI, que também faz commit das capturas).
* Tarefas independentes rodam em paralelo (por exemplo, personagens e componentes de interface), cada uma em worktree isolada (`isolation: "worktree"`). Um subagente integrador junta os resultados.
* Trabalho mecânico e repetitivo (renderizar lotes de imagens, converter formatos, trocar chamadas em muitas telas) pode ir para um modelo menor (Sonnet). Decisões de arte, arquitetura e revisão ficam com o modelo principal. A skill `orquestrador-sonnet` do dono do produto segue esse mesmo modelo de trabalho.
* Um subagente **revisor independente** avalia as capturas de cada fase sem ver o raciocínio de quem fez, com a régua da seção 7, e devolve nota e lista de problemas.

## 1. Objetivo

Trocar o visual desenhado em código (formas chapadas, emojis de sistema) por um visual de **brinquedo 3D**: personagens e objetos com volume, plástico e massinha, luz macia, sombras de contato e cores vivas, no nível dos melhores apps infantis. O conteúdo (1000 fases, casa que cresce, narração, regras, progresso salvo) não muda.

Referência visual obrigatória: as três maquetes aprovadas em `docs/visual-3d/maquetes/` (`1_inicio.png`, `2_jogo.png`, `3_premio.png`) e o protótipo que as gerou em `docs/visual-3d/prototipo/` (ver seção 4).

## 2. Princípios de arte

* **Mundo de brinquedo em ilhas.** Cada cena é um diorama sobre uma ilhinha de grama flutuando num céu em degradê (azul em cima, pêssego e rosa embaixo). A casa, a lagoa do jogo e o pódio do prêmio seguem essa ideia.
* **Materiais:** plástico brilhante com verniz para brinquedos e objetos; massinha fosca para terra, troncos e tecidos; pele levemente acetinada.
* **Luz:** sol quente do alto à direita, luz de céu, recorte frio por trás, sombras macias. Saturação levemente aumentada no fim (no protótipo: `saturate(1.22) contrast(1.04)`).
* **Personagens:** cabeça grande (chibi), olhos grandes com dois brilhos, sobrancelhas, bochechas rosadas, boca aberta com linguinha, olhinhos fechados "^ ^" ao comemorar. O irmãozinho é menor, de jardineira, e tem a mesma pele e o mesmo cabelo da criança.
* **Interface:** botões como blocos de brinquedo (face colorida em degradê, lábio escuro embaixo, brilho em cima, borda branca), botões redondos claros, pílulas, balões de fala; fonte **Baloo 2** (títulos e números) e **Fredoka** (textos); números e títulos em branco com contorno escuro.
* **Nada de número abstrato para a criança.** Progresso aparece como estrelas, contas coloridas e coisas que se veem (como nas maquetes), nunca "3/250".
* **Ícones dos caminhos em 3D:** blocos 1 2 3 (Números), blocos A B C (Letras), globo (Inglês), argolas de empilhar (Lógica).

## 3. Decisão técnica: imagens pré-renderizadas em camadas

O app continua em Kotlin e Jetpack Compose. A arte 3D é **renderizada fora do app** e entra como imagens (WebP com transparência). O app monta as cenas com essas imagens e anima com transformações (pulo, amassar e esticar, balanço, piscar).

Por que não 3D em tempo real (Filament, SceneView, Unity):

* As capturas do CI (Robolectric) não renderizam GPU; perderíamos a verificação visual automática que segura a qualidade hoje.
* O app roda desde o Android 7.0 (API 24) e em celulares simples; imagem pronta é leve e previsível.
* Não reescreve o app (63 mil linhas, 1000 fases, testes no emulador).

Por que isso dá conta da qualidade: a aparência das maquetes vem da modelagem e da luz, que ficam idênticas numa imagem pré-renderizada.

### 3.1 O pipeline de arte

* Pasta nova `casinha-do-saber/arte3d/`: projeto Node com `three@0.160.0` e o kit do protótipo (`kit.js`) evoluído. Cada objeto é um módulo que monta o modelo 3D em código.
* Um script de render (Playwright com o Chromium headless e WebGL por SwiftShader, como em `prototipo/shoot.js`) gera as imagens com fundo transparente, em câmera fixa por tipo de cena, e salva em `app/src/main/assets/arte/` (ou `res/drawable-nodpi/`) como WebP.
* Junto, um `manifesto.json` com, para cada imagem: tamanho, ponto de apoio (os pés, o meio da base), e âncoras úteis (centro da cabeça para chapéus, mãos para acessórios).
* As imagens geradas são versionadas no repositório (o app não depende do pipeline para compilar). Um job opcional do CI pode rodar o pipeline e falhar se as imagens commitadas estiverem desatualizadas.
* **Vantagem:** a arte é código. Uma sessão do Claude consegue criar e ajustar qualquer objeto sem artista, e tudo é reproduzível.

### 3.2 Personagens em camadas (sem explosão de combinações)

A criança tem pele (4), cabelo (4 cores e 3 estilos), gênero, cor de roupa (6), 16 roupas, 18 acessórios e chapéus. Renderizar cada combinação é impossível. Solução: **camadas na mesma câmera**, desenhadas de trás para a frente:

1. cabelo de trás (coques, tranças, nuca);
2. corpo e pernas (pele);
3. roupa (por traje e por formato de corpo);
4. braços (por pose: parado, tchau em 4 quadros, torcendo, pensando);
5. cabeça (pele);
6. rosto (por humor: feliz, torcendo, pensando, surpreso, olhos fechados para piscar; com e sem cílios);
7. cabelo da frente (por estilo);
8. acessórios por parte do corpo e chapéu.

**Cores por tingimento:** pele, cabelo e cor da roupa são renderizados num material neutro claro e tingidos no app (`ColorFilter` com multiplicação), mantendo a luz e a sombra. Estampas e trajes especiais são camadas coloridas próprias.

O mesmo vale para o irmãozinho (com menos peças: sem roupas da loja por enquanto).

### 3.3 Componentes que trocam por dentro, com a mesma API

Para não tocar nas mais de 20 telas que já usam os personagens:

* `Crianca(perfil, modifier, acenando, humor, falando, animado)` passa a montar as camadas, com a mesma assinatura.
* `Irmaozinho(perfil, modifier, humor, acenando, animado)` idem.
* `BotaoFofo`, `BotaoRedondo`, `BalaoDeFala`, `cartaoFofo`, `FundoDecorado` e `cenarioDoJogo` trocam de aparência mantendo os parâmetros.

Assim cada fase muda muito visual com pouco risco.

## 4. O protótipo aprovado (ponto de partida da arte)

Em `docs/visual-3d/prototipo/`:

* `kit.js`: palco (renderizador com tom ACES, sombras macias, ambiente `RoomEnvironment`), materiais (`plastico`, `massinha`, `pele`), formas (caixa arredondada, bola, cápsula, cilindro), e os modelos: `crianca`, `irmaozinho`, `casinha` (casa de bonecas aberta, dois andares, móveis), `ilhaDeGrama`, `arvore`, `arbusto`, `flor`, `nuvem`, `patinho`, `presente`, `moeda`, `estrela3D`, `confete`.
* `icones.js`: os quatro ícones dos caminhos (blocos com números e letras em 3D, globo, argolas).
* `ui2.css`: o visual dos botões, pílulas e balões.
* `inicio.html`, `jogo.html`, `premio.html`: as três telas das maquetes.
* `shoot.js`: tira a foto de cada tela com o Chromium headless.

Para rodar: na pasta do protótipo, `npm init -y && npm i three@0.160.0`, depois `python3 -m http.server 8765` e `node shoot.js inicio.html saida.png`. Lições já aprendidas:

* Chromium headless precisa de `--use-angle=swiftshader --enable-unsafe-swiftshader --ignore-gpu-blocklist` para WebGL.
* Na three 0.160 o `TextGeometry` usa `height` (não `depth`) para a espessura; com o nome errado a letra fica enorme.
* Sombra com listras no cabelo: `shadow.bias = -0.0008` e `normalBias = 0.05`.
* Amarelo e azul claros desbotam com o tom ACES: usar cores mais saturadas e menos `envMapIntensity` (veja o `patinho`).
* Duas superfícies na mesma altura brigam (a água sumiu sob a borda da lagoa): deixar sempre uma acima da outra.

## 5. Inventário do que muda

Telas (`ui/telas/`): Perfil (escolha do personagem), Quem vai jogar, Boas vindas, Início, Trilha e Mapa, Jogo (com as cenas de pergunta e as 17 atividades de `atividades/novas/` e as de `atividades/`), Prêmio, Quase, Casa de fora, Casa por dentro (isométrica), áreas novas da casa (`ComodosNovos`, `FundosDasAreas`), Loja e Provador (espelho e guarda roupa), Mascote, Ateliê e Piano, Livrinhos e História, Álbum e Diplomas, Relembrar e Descobrir o nível, Dormir. A Área dos pais só ganha a fonte e os botões novos (é tela de adulto, continua sóbria).

Componentes (`ui/componentes/`): Crianca, RostoDaCrianca, Irmaozinho, Comuns (botões), Fundos, CenarioDoJogo, CenaDaCasa, CenaDaArea, BichosDaCasa, MascoteDesenhado, Corujinha, Enfeites, EnfeitesDoEvento, Confete, Provador, Setas, Modal.

Figuras das perguntas: hoje são emojis. Substituir por modelos 3D começando pelos mais frequentes nas fases iniciais (frutas, bichos, brinquedos, objetos da casa, formas), mantendo o emoji como reserva para o que ainda não tiver modelo. Um teste lista quais emojis do vocabulário já têm modelo.

## 6. Fases

Cada fase termina com: CI verde (testes, capturas, lint, emuladores), capturas novas commitadas pelo CI, nota do revisor independente e `ESTADO.md` atualizado. Só se passa de fase com a anterior verde.

**Fase 0A. Tela cheia de verdade, também no tablet (fazer primeiro).** Pedido do dono do produto: a criança joga num **tablet Samsung** e, como o app não fica em tela cheia, ela vive tocando nos atalhos da barra de tarefas do One UI (os ícones de apps embaixo). No celular a tela cheia já parece funcionar. Causa encontrada: `MainActivity` só chama `enableEdgeToEdge()`, que desenha por baixo das barras, mas não as esconde; no tablet a barra de tarefas continua visível e tocável. Fazer:
* modo imersivo com `WindowInsetsControllerCompat`: esconder `WindowInsetsCompat.Type.systemBars()` (barra de status e barra de navegação, o que também recolhe a barra de tarefas do tablet no Android 12L ou mais novo) com `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`, para as barras só voltarem com um deslize da borda e sumirem sozinhas de novo;
* reaplicar ao voltar para o app e quando a janela recupera o foco (`onResume` e `onWindowFocusChanged`), porque teclado, diálogos do sistema e a troca de apps devolvem as barras;
* conferir que as telas continuam respeitando o recorte da câmera (`displayCutout`) depois que os insets das barras zerarem; nada pode ficar embaixo da câmera frontal;
* vale para celular e tablet, deitado e em pé;
* na área dos pais, uma dica curta de como **fixar o app** no Android (Configurações, Segurança, Fixar janelas), para quem quiser impedir que a criança saia do jogo;
* teste no emulador com perfil de tablet (Android 14): com o app aberto, as barras do sistema não estão visíveis (`WindowInsets.isVisible(systemBars())` falso); e o mesmo teste no perfil de celular.
Aceite: no tablet Samsung da criança, nenhuma barra nem atalho aparece durante o jogo; o dono do produto confirma no aparelho. Esta fase não depende da arte e pode ser feita antes da Fase 0, numa tarefa só.

**Fase 0. Fundação do pipeline.** Criar `arte3d/` a partir do protótipo; script de render em lote com manifesto; formato e pasta das imagens no app; carregador de imagens no Compose com cache e redução de tamanho para aparelhos antigos; um teste que confere se toda imagem do manifesto existe e cabe no orçamento de tamanho. Aceite: uma imagem de teste aparece numa captura do CI.

**Fase 1. Personagens.** Criança e irmão em camadas (seção 3.2), poses, humores e piscar; `Crianca` e `Irmaozinho` com a mesma API; animações (respirar, pulo com amassar e esticar, tchau, comemorar). Adaptar o teste dos chapéus (captura 56) para a âncora da cabeça do manifesto. Aceite: capturas 01, 05, 11, 70 com os personagens novos; teste dos chapéus verde.

**Fase 2. Sistema de interface.** Fontes Baloo 2 e Fredoka (Tema.kt), botões de bloco, redondos, pílulas, balões, contas do progresso, estrelas; ícones da barra (fechar, voltar, som, ajustes) desenhados no mesmo estilo; ícones 3D dos caminhos. Aceite: capturas de início, jogo e pais com os componentes novos, sem texto cortado em 360 dp.

**Fase 3. Início e casa de fora.** Diorama da casa em ilha, com as etapas de construção (terreno, paredes, telhado, porta) e os 40 itens da casa como imagens no mesmo ângulo; arrastar e soltar continuam iguais (as posições do domínio não mudam); dia e noite e clima por camadas; tela inicial como na maquete 1, sem "x/250". Aceite: capturas 03, 12, 13, 14, 14b, 20, 20b; teste da chuva só fora de casa adaptado e verde.

**Fase 4. Jogo.** Cenário de ilha por caminho; cenas das perguntas (lagoa, mesa, varal e outras) com modelos 3D; respostas em blocos coloridos; as 40 a 60 figuras mais usadas em 3D; atividades de mexer (cesta, memória, montar palavra, traçar) com peças novas. Aceite: capturas 05 a 10, 24 a 31, 62 a 66 e as do jogo com irmão; jogo no emulador sem regressão.

**Fase 5. Prêmio e Quase.** Como a maquete 3: estrelas 3D, presente que abre em quadros, prêmio saindo da caixa, pódio, confete, os dois irmãos comemorando, moedas. Aceite: capturas 11, 11b, 38, 39, 57, 73.

**Fase 6. Casa por dentro.** Móveis e enfeites renderizados na mesma projeção isométrica de `Isometrico.kt` (câmera ortográfica no pipeline com o mesmo ângulo), um sprite por móvel e por direção quando houver; o teste de oclusão (`OcultacaoNaCasaTest`) continua valendo. O irmãozinho também anda dentro de casa. Aceite: capturas 32 a 37.

**Fase 7. Guarda roupa.** 16 roupas, 18 acessórios e os chapéus como camadas; provador e loja. Aceite: capturas 18, 19, 56, 58, 59, 60.

**Fase 8. Demais telas.** Trilha e mapa, perfil, quem vai jogar, boas vindas, mascote, ateliê, piano, livrinhos, história, álbum, diplomas, relembrar, áreas novas da casa, dormir, as 17 atividades novas. Pode ser dividida por grupos de telas, um subagente por grupo, em paralelo.

**Fase 9. Acabamento.** Desempenho em API 24 (memória e tempo de abertura), conferir que o APK ficou abaixo de 150 MB, emojis restantes, revisão final com nota, teste com a criança (ver seção 7).

## 7. Qualidade e verificação

* **Sem SDK do Android local**: o ambiente da nuvem não baixa do `dl.google.com`. A verificação do app é pelo CI, que roda testes de unidade, capturas (Robolectric e Roborazzi), lint, APK e emuladores com Android 14, 7.0 e 8.0, e publica o APK testado em Releases (`casinha-teste`). O pipeline de arte roda localmente (Chromium e npm funcionam).
* **Tamanho do app:** o limite é **150 MB** (definido pelo dono do produto; hoje o APK tem 12 MB). Não é uma preocupação central: a prioridade é a qualidade da arte. Ainda assim, usar WebP (com perda, qualidade perto de 85) e gerar cada imagem no dobro do maior tamanho em que aparece, para não pesar na memória dos aparelhos antigos.
* **Tela cheia:** em todas as telas, celular e tablet, deitado e em pé, as barras do sistema ficam escondidas (Fase 0A). Toda captura de tablet e todo teste de emulador com perfil de tablet confirmam isso.
* **Régua do revisor** (nota de 0 a 10 por tela): fidelidade às maquetes; personagens expressivos e consistentes entre telas; nada cortado ou sobreposto em 360 dp, 393 dp e tablet deitado; contraste do texto; nenhum emoji de sistema na barra; nenhum número abstrato para a criança; coerência de luz e sombra entre imagens.
* **Teste com a criança** ao fim das fases 1, 4 e 5: dez minutos de observação, anotar onde ela sorri, onde toca, onde desiste. O resultado entra no `ESTADO.md` e pode mudar a ordem das fases seguintes.
* **Não quebrar o que existe:** progresso salvo, cópia do progresso, irmãozinho (`Perfil.irmao`), narração, limite de tempo, área dos pais. Saves antigos abrem sem perda.

## 8. Modelo de briefing para subagente

```
Objetivo: <uma frase>
Contexto: Casinha do Saber, app Android em Kotlin/Compose em casinha-do-saber/.
  Leia: docs/visual-3d/PRD-visual-brinquedo-3d.md (seções X e Y) e <arquivos>.
  Referência visual: docs/visual-3d/maquetes/<arquivo>.
Faça: <lista curta e concreta>
Não mexa em: <arquivos ou comportamentos>
Verifique: <comando local, captura esperada, teste>
Aceite: <critério objetivo>
Relatório: no máximo 25 linhas (o que mudou, arquivos, verificação, pendências).
  Detalhes longos em docs/visual-3d/relatorios/<tarefa>.md. Não faça push.
```

## 9. Arquivo de estado (`docs/visual-3d/ESTADO.md`)

Criar na primeira sessão, com:

* tabela das fases (pendente, em andamento, feita), com a data e a execução do CI que comprovou;
* decisões tomadas (com data) e o porquê;
* problemas abertos;
* próxima tarefa a delegar, já com o briefing pronto.

Toda sessão nova começa lendo este PRD e o `ESTADO.md`, e nada mais, antes de delegar.

## 10. Fora do escopo

* 3D em tempo real no app (pode ser reavaliado depois da fase 9 só para a casa, se houver motivo).
* Mudar fases, regras pedagógicas ou a narração.
* Publicação na Play Store (segue `docs/publicar.md`).
