# Fase 3, parte B: casa de fora e tela inicial em camadas 3D (Kotlin)

Data: 2026-11-07. Sem commit nem push por esta tarefa. Domínio, regras, narração, workflow do CI e `arte3d/` não
foram tocados. Usa a arte da parte A (já no manifesto); nenhuma camada faltou.

## Arquivos

* **Novo** `ui/componentes/CamadasDaCasa.kt` (Kotlin puro): `AparenciaDaCasa` (estruturas, cores dos cômodos,
  telhado, porta, papel, noite, neve no telhado, abajur; `de(estado, noite)`), `CamadaDaCasa(id, cor, estrutura)` e
  `CamadasDaCasa`: `camadas(aparencia)` monta a pilha fixa na ordem da tabela da fase3a (chão 10, lago 12, caminho 13,
  cerca 14, chaminé 18, cômodos 20, papel 22, janelas 24, porta 26, paredes 30, escada 32, telhado 40, janela do
  telhado 41, neve 42); `empilhar` (ordena pela `ordem` do manifesto, nulo se faltar um id), `disponivel`,
  `idsPossiveis`, `multiplicar`, `idDoItem` (`item_<id>`, `item_sol_noite` de noite), `lugarDoItem` (apoio no pé,
  largura do item x 1,08, como `arte3d/previa_casa.js`).
* **Novo** `ui/componentes/CasaEmCamadas.kt`: `ComposicaoDaCasa.composto(contexto, camadas, largura)` compõe a pilha
  num único `Bitmap` (largura até 800 px; altura até 1,65 da largura, porque o chão desce no jardim de baixo), com
  `PorterDuffColorFilter(MULTIPLY)` para tingir. As camadas são decodificadas **sem cache** (`ArteDoApp.decodificar`,
  novo) e recicladas logo depois: só o composto fica (LRU próprio, 1/16 da memória, no mínimo 14 MB). Também
  `lembrarCasaEmCamadas` (composto + estrelas, nuvens, nuvem de chuva e clima, lidos pelo cache da `ArteDoApp`),
  `CasaPartida` (abaixo / estrutura nova / acima, em meia resolução, só para a queda da estrutura nova) e
  `ItemDaArte` (item pela arte, apoio no pé, filtro da noite, cachorro e gato virando para onde andam e saltitando).
* `CenaDaCasa.kt` (**mesma API pública**; `semCeu` agora também vale com `preencherAltura`, como já dizia o comentário):
  * Com a arte toda, o `Canvas` da cena chama `desenharCasaEmCamadas`: céu do app (degradê novo, azul para pêssego),
    `ceu_estrelas_1/2` piscando alternadas de noite, `ceu_nuvem_1/2` indo e voltando de dia, `ceu_nuvem_chuva` no
    clima 1, o composto (ou a casa partida enquanto a estrutura nova cai), a fumaça e o contorno pontilhado de antes.
  * A chuva e a neve (`ceu_chuva`/`ceu_neve`, emendadas na vertical, 2 e 1 imagens por volta do relógio de 4 s) são
    um `Canvas` por cima de itens e crianças, recortadas nas paredes e no triângulo do telhado como antes.
  * Itens: `item_<id>` em `ObjetoNaCena` (mesmas posições, arrasto, encaixe, recusa, vida); a arte já tem a sombra de
    contato, então a elipse do app sai nesses; de noite, filtro de multiplicação (`NOITE`) em vez da camada
    `Offscreen` (que recortaria a folga da arte): novo parâmetro interno `escurecerDeNoite` do `ObjetoNaCena`.
  * `ConteudoDoItem` (baú, prêmio, balão de "não") mostra a arte do item quando há; bichos e itens das áreas novas
    seguem com o desenho antigo.
  * Reserva: sem toda a arte da casa (ou se uma camada não decodificar), a casa inteira volta ao `desenharCasa`
    antigo, intacto; por item, sem `item_<id>` ou sem decodificar, o desenho antigo do item. Passarinhos e fumaça
    continuam desenhados (a arte não tem).
  * Na tela alta, o fundo de fora da cena é o céu e a grama lisa da arte (`A9E56F`) a partir da metade da cena.
* `ImagemDeArte.kt`: `ArteDoApp.decodificar` (decodificação sem cache; `bitmap` passa a usá-la).
* `TelaInicio.kt` (mesma assinatura): fundo de céu em degradê da maquete 1 (sem `FundoDecorado`), cabeçalho com
  ajustes, pílula da estrela com o total de estrelas e som, título grande embaixo (em telas baixas e deitadas o
  título vai na linha dos botões, sem a pílula); a casa sem moldura, sem o céu próprio (`semCeu`) e com a grama se
  desfazendo no rosado atrás dos caminhos. Saiu o "🏠 x de 40 coisas". Caminhos: ícone 3D grande no canto de cima,
  nome em branco com contorno escuro e **5 estrelas** (`ProgressoVisivel.estrelasAcesas`, `EstrelasDeProgresso`);
  saiu a barrinha e o "x/250" (o número fica só na descrição de acessibilidade, que os testes do emulador usam).
  Espelho, estante, Corujinha, missão, presentes, mãozinha, narração e área dos pais (toque longo) iguais.

## Testes e capturas

* **Novo** `CamadasDaCasaTest` (JUnit puro, 8 testes): toda a arte da casa no manifesto e com o quadro da cena;
  todas as pilhas das etapas do jogo (de dia, noite, neve, papel 0 a 3, abajur) existem e saem na ordem do
  manifesto; cada estrutura põe a sua camada; tingimento dos cômodos (e o `tingir` do manifesto) e noite (janelas
  acesas não escurecem; abajur clareia); `AparenciaDaCasa.de` do `EstadoJogo`; os 40 itens têm imagem, o apoio cai
  no pé e geladeira/fogão/mesa mantêm a proporção do domínio; `multiplicar`.
* `CapturasTest.chuvaNuncaCaiDentroDeCasa` **adaptado**: mesma regra e mesmas áreas; a neve parada no telhado agora é
  uma camada da arte que desce nos beirais, então os pixels sob o alfa de `casa_neve_no_telhado` (com a margem) não
  contam como "dentro" no clima 2. Conferido em Python que, com a casa completa, toda a área testada é opaca.
* **Novo** `CapturasCasa3DTest`: `v3d_04_inicio` (comparar com `maquetes/1_inicio.png`; antes confere o composto,
  que não há "/", "de 250" nem "coisas" em texto e que os 4 caminhos e a casinha seguem achados pela descrição) e
  `v3d_05_casa_em_camadas` (etapas 0 a 4, casa completa, noite, chuva, neve e cores da loja).
* As capturas 03, 12, 13, 14, 14b, 20, 20b (e 02, 03b, 15b, 16, 18, 21, 22, 23, 50, 53, 55, 74) mudam sozinhas.
  Também mudam o prêmio e o baú (11, 38, 39), que agora mostram o item 3D. Nenhuma captura foi renomeada.

## Verificação feita aqui

* JVM puro (Kotlin 2.0.21, Gradle `--offline` em projeto temporário, `workingDir` = `app/`): **242 testes, 0
  falhas** (todos os de `dominio/` e `ui/componentes/`, incluindo os 8 novos).
* Compilação sem erro nem aviso contra Compose Multiplatform desktop 1.7.3 (API do BOM 2024.12.01), com stubs de
  `android.*`, `LocalContext`, `LocalConfiguration`, áudio e duas telas: `CamadasDaCasa`, `CasaEmCamadas`,
  `CenaDaCasa` (sem a linha `PlatformTextStyle`, que só existe no Android), `ImagemDeArte`, `TelaInicio`, Crianca,
  Irmaozinho, Bichos, Enfeites do evento, Comuns, Fundos e todo o `dominio/`; e os testes `CapturasCasa3DTest` e o
  trecho adaptado do teste da chuva (com stubs de Robolectric e Roborazzi). Não rodam aqui: quem prova o visual é o CI.
* Maquete em PIL da tela inicial em 393 x 852 dp com as mesmas contas (cena, apoio dos itens, grama): casa no meio
  da tela entre o título e os caminhos, como a maquete 1.

## Pendências e avisos

* A casa por dentro, as áreas novas, a loja e o cabeçalho da `TelaCasa` ("x de 40 coisas") ficam para as fases 6 e 8.
* Cachorro e gato na casa de fora agora são a imagem 3D (passeiam, viram e saltitam); `BichoDesenhado` ficou para o
  baú, o prêmio, as áreas novas e como reserva.
* Memória: um composto de 800 x 1320 px tem 4,2 MB; enquanto uma estrutura nova cai há mais três em meia resolução
  (~1 MB cada); de noite as duas camadas de estrelas e, com clima, a da chuva ficam no cache da `ArteDoApp`.
* A composição é síncrona na primeira vez de cada aparência (decodifica até ~15 camadas); se pesar em API 24, mover
  para `produceState` com IO na Fase 9.
* Riscos para olhar nas capturas do CI: o encontro da grama com o rosado na tela inicial, o tamanho do título em
  360 dp e se o WebP decodifica no Robolectric (sem isso, a casa cai no desenho antigo e `CapturasCasa3DTest` acusa).
