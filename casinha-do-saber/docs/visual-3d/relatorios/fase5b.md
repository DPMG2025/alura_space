# Fase 5, parte B: Prêmio e Quase no visual brinquedo 3D (Kotlin)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a5516d9bc189a9300`, branch
`worktree-agent-a5516d9bc189a9300` (partindo de `ccr-162c6a70-urxpmq`, f824f97). Commit local, sem push; o hash está
na mensagem de entrega. Regras do jogo, domínio, narração, workflow, `arte3d/`, `TelaInicio` e `TelaCasa` não foram
tocados. Usa só a arte da 5A já integrada ao manifesto.

## Arquivos

* **Novo** `ui/componentes/PecasDoPremio.kt` (Kotlin puro): ids da arte (`premio_*`, `quase_*`, `estrela3d`),
  `quadroDoPresente(i)` (5 quadros, 110 ms cada, o prêmio sai a partir do quadro 2), `idDoPremio` (`item_<id>`),
  `pilhasDeMoedas(n)` (pilhas de 5, 3, 2 e 1, até 3 pilhas, da maior para a menor), `idsDoConfete` (24),
  `estagioDaSemente(acertos, minimo)` (1 a 3; a flor, 4, é vencer) e `montarPalco` (coisas de pé nas âncoras do pódio:
  o apoio de cada uma cai na âncora, o palco cresce para cima só o necessário).
* **Novo** `ui/componentes/PremioEmCamadas.kt`: `PresenteQueAbre` (quadros 0 a 4; fechado dá pulinhos e mostra o
  `AnelDeToque`; ao abrir, o `premio_clarao` gira atrás e o prêmio sobe da âncora `boca` para o facho de luz, crescendo
  de 25% a 100%), `AnelDeToque` (anéis que pingam, no lugar da mão 👆), `PilulaDeMoedas` (moedas 3D contáveis + o
  número ao lado), `EstrelaDoPremio` (`estrela3d`; a não ganha fica cinza translúcida por `ColorMatrix`, sem ampliar a
  `estrela_vazia` de 48 dp), `PalcoDaFesta` + `NoPalco` (pódio com âncoras `esquerda`/`direita`/`centro` do manifesto),
  `Confete3D` (explosão com os `premio_confete_*`; sem a arte, o `Confete` antigo), `ChuvaDeConfete` (confete 3D caindo
  devagar no fundo, uma camada só; parado com animações desligadas) e `IconeDeCasaBranca` (a casinha branca do botão,
  vetorial como no protótipo).
* `ui/telas/TelaPremio.kt` (mesma assinatura): fundo do protótipo (`#8a6cff → #b783ff → #ff9fcf → #ffc2dc`), raios e
  brilho quente centrados no presente, confete 3D; "Parabéns, <nome>!" com `TextoComContorno`; 3 estrelas 3D (a do meio
  maior e mais alta, as dos lados inclinadas), com o mesmo estouro de antes; pílula de moedas; o nome ganho (e "Em
  inglês") ou o convite acima do palco; **pódio** com o presente no centro (flutuando 16 dp), a criança na `esquerda` e
  o irmãozinho na `direita`, os dois `Humor.TORCENDO` (braços para cima, olhos ^ ^, pulo); o mascote fica no lugar do
  irmão ou, com os dois, na frente entre a criança e o presente. Botão verde **"Pôr na casinha"** (`BotaoFofo`
  `#34C46A`/lábio `#1C9552`, casinha branca, texto com contorno escuro; "Ver na casinha" no brinde); voltar e próxima
  como botões redondos com a seta 3D (⬅️/▶️ mapeados para `icone_voltar`). O layout não pula mais ao abrir (antes a
  criança encolhia). Removidos: 🪙 (aparecia como 🏛 em alguns aparelhos), 🏠, 🗺️ e a mão 👆.
* `ui/telas/TelaQuase.kt` (mesma assinatura): céu azul para pêssego e rosa; "Quase!" e "A sementinha está crescendo!"
  com contorno; pílula com 5 contas (`conta_verde`/`conta_vazia`, a quarta maior) e o `premio_presente_fechado` como meta;
  cena com luz macia, `quase_estrela_dormindo` boiando e a semente que cresce quadro a quadro (650 ms) do estágio 1 até o
  dos acertos, com amassar e esticar a cada troca; criança torcendo e irmão dando tchau; "De novo" com contorno e o
  `AnelDeToque`. Removidos 🔒, 🎁, 🔄, 🗺️ e a mão 👆.

## Testes e capturas

* Mantidos (conferidos com grep): `contentDescription` "Presente. Toque para abrir." (fechado; aberto = nome do item ou
  do brinde), "Colocar na casa"/"Ver na casa" (o texto visível agora é "Pôr na casinha"/"Ver na casinha"), "Voltar para
  o caminho", "Próxima fase", "Tentar de novo", "Mais N moedinhas". Nenhum testTag existia nessas telas.
* **Novo** `PecasDoPremioTest` (JUnit puro, 5 testes): toda a arte no manifesto (e `item_<id>` dos 40 prêmios), âncoras
  do pódio e a `boca` nos 5 quadros (mesmo recorte), pilhas de moedas, semente sem flor, conta do palco.
* **Novo** `CapturasPremio3DTest` (Robolectric, 393 x 852): `v3d_06_premio` (Ana com o irmão, cama, 3 estrelas, 5 moedas,
  presente aberto) e `v3d_07_quase` (3 acertos, semente no estágio 3). Antes de capturar: arte no manifesto e
  decodificável, os nomes acima presentes e nenhum emoji (🪙 👆 🏛 🏠 🗺 / 🔒 🎁 🔄) na tela.
* Mudam sozinhas: 11, 11b, 38, 39, 57, 73 (e as do mascote `s4_*` com o prêmio, `loja_celular_03`, `e07`/`e08`/`e16` do
  emulador). Nenhuma captura renomeada.

## Verificação feita aqui

* JVM puro (Gradle temporário `--offline`, Kotlin 2.0.21, `workingDir` = `app/`): todos os testes de `dominio/` e
  `ui/componentes/` (247: 242 de antes + 5 novos), 0 falhas.
* Compilação sem erro nem aviso contra Compose Multiplatform desktop 1.7.3 (API do BOM 2024.12.01), com stubs de
  `android.*`, `LocalContext`, áudio, `CenaDaHistoria`, `FestaDoMascote` e `MascoteAnimado`: `TelaPremio`, `TelaQuase`,
  `PremioEmCamadas`, `PecasDoPremio`, `Confete`, os componentes das fases 0 a 3 e o `CapturasPremio3DTest` (stubs de
  Robolectric e Roborazzi). Rodar as capturas não dá aqui (sem SDK; o runtime desktop pede o `dl.google.com`). Quem
  prova o visual é o CI.
* Conta do layout (393 x 852, aberto): topo ~220 dp, palco ~290 dp (pódio 360 x 127), botões ~140 dp: ~715 dp, cabe
  sem rolar. Em 360 dp de largura o pódio fica com 336 dp. Abaixo de 600 dp de altura tudo encolhe e a coluna rola.

## Memória

* Nada guardado em estáticos além do `LruCache` da `ArteDoApp` que já existia; tudo por `ImagemDeArte` ou
  `ArteDoApp.bitmap`, decodificado no tamanho de exibição (inSampleSize).
* Fechado: 1 quadro (440 x 480, 0,8 MB), 1 `estrela3d` (as 3 estrelas dividem a mesma), pódio (0,65 MB), moedas e 24
  confetes de 48 px (~0,2 MB no total). Ao abrir, os quadros 1 a 4 são decodificados um a um, quando aparecem (não os 5
  de uma vez), mais o clarão (0,77 MB) e o item (0,64 MB); os quadros que já passaram ficam só no cache e podem ser
  despejados. Pico por volta de 6 MB de imagens, contra os compostos dos personagens (já em cache próprio).
* Animações infinitas: uma camada para os raios, uma para a chuva de confete, os pulinhos do presente (só fechado) e o
  giro do clarão (só aberto); o confete da explosão some depois de 1,6 s.

## Pendências e pedidos de arte

* **Mão de toque 3D** (para a `MaoGuia`, que ainda usa 👆 em outras telas): aqui entrou o anel de toque desenhado.
* **Ícone branco da casa** (pedido da 2B continua): o botão usa uma casinha vetorial branca igual à do protótipo.
* **Ícone do mapa** (🗺️): troquei por ⬅️ (seta 3D) no "Voltar para o caminho" destas duas telas.
* O brinde da loja ainda mostra o emoji do produto (`Produto.icone`) ou o desenho antigo (telhado, porta, papel): falta
  arte 3D de produto (chapéus, roupas e acessórios entram com a Fase 7).
* Itens das áreas novas (`quarto2.*`, `pet.*` etc.) não têm `item_<id>`: saem da caixa com o desenho antigo do
  `IconeDoItem` (Fase 8).
* A narração do Quase continua falando do presente ("O presente quer sair da caixa!"): o presente aparece como a meta na
  pílula; a semente e a estrela dormindo são o visual principal.
* Riscos para olhar no CI: o pulo do `TORCENDO` passando do alto do palco (sem recorte, deve ficar bem), a tampa voando
  por cima da cabeça do irmãozinho e o tamanho do título em 360 dp.
