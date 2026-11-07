# Fase 1, parte B: `Crianca` e `Irmaozinho` em camadas 3D (Kotlin)

Data: 2026-11-07. Sem commit nem push por esta tarefa.

## O que mudou

### Arquivos novos (`app/src/main/java/br/com/casinhadosaber/ui/componentes/`)

* `CamadasDosPersonagens.kt` (Kotlin puro, sem Android nem Compose; testável na JVM):
  * `enum class Humor` **mudou para cá** (mesmo pacote, então nenhum import muda). Também ficam aqui
    `PROPORCAO_CRIANCA = 170f / 260f` (0,654) e `PROPORCAO_IRMAO = 130f / 196f` (0,663), as proporções dos quadros novos.
  * `PersonagemDaArte` (`CRIANCA`, `IRMAO`; prefixo do id e `apoioNaCaixa`), `PoseDosBracos` (parado, tchau1..4,
    torcendo, pensando), `QuadroDoPersonagem` (braços, humor, olhos fechados, boca aberta). O nome não é
    `Personagem` porque `dominio.Personagem` (Historia.kt) já existe.
  * `CamadasDosPersonagens`: `idsDaCrianca` / `idsDoIrmao` (mesma escolha de `arte3d/previa.js`, com a tabela
    humor → olhos/boca/sobrancelhas/braços da Fase 1A; cílios na menina e na irmãzinha; laço da menina só sem
    chapéu nem acessório de cabelo; elásticos nas tranças), `empilhar` (ordena por `ordem` do manifesto; nulo se
    faltar um id), `disponivel` (confere TODOS os quadros possíveis de um perfil), `cor` (pele, cabelo e roupa
    reais de `Aparencia` e `Perfil.corDaRoupa`), `usaCamadas` (sem traje e sem acessório), `caixaDoQuadro`
    (Fit, centrado, pés em `apoioNaCaixa` da altura), `lugarDoChapeu` (âncoras `cabeca` e `topoDaCabeca`),
    `quadroDoTchau` (1, 2, 3, 4, 3, 2) e `pulo` (amassar e esticar por quadros-chave).
* `PersonagemEmCamadas.kt` (Android/Compose):
  * `ComposicaoDosPersonagens`: compõe a pilha num **único** `Bitmap` ARGB por (ids das camadas, cores, largura,
    altura), com `Canvas.drawBitmap` e `PorterDuffColorFilter(cor, MULTIPLY)` nas camadas com `tingir`
    (igual ao `BlendMode.Modulate` e ao multiply da prévia). `LruCache` próprio medido em KB, até 1/16 da memória
    (mínimo 4 MB). As camadas soltas vêm de `ArteDoApp.bitmap` (com `inSampleSize` pelo tamanho final).
    O composto nunca passa do tamanho da arte (340 x 520 na criança): ampliar não ganha nitidez.
  * `podeUsar(contexto, personagem, perfil)`: camadas completas no manifesto + leitor de WebP funcionando (testado
    uma vez, decodificando a cabeça com redução 4) + (criança) sem traje nem acessório.
  * `DrawScope.desenharPersonagemEmCamadas(...)` e o `@Composable PersonagemEmCamadas(...)` (interno), que faz as
    animações e põe o chapéu.

### Arquivos alterados

* `Crianca.kt` e `Irmaozinho.kt`: **mesmas assinaturas** (`Crianca(perfil, modifier, acenando, humor, falando,
  animado)`, `Irmaozinho(perfil, modifier, humor, acenando, animado)`). Por dentro: se `podeUsar`, monta as camadas;
  senão (traje, acessório, id faltando, WebP ilegível), o desenho antigo, intacto. Se a composição falhar na hora
  de desenhar, o mesmo quadro cai no desenho antigo (`reserva`).
  * O desenho antigo mantém a sua proporção (`PROPORCAO_DESENHO_CRIANCA = 0.62`, `PROPORCAO_DESENHO_IRMAO = 0.74`)
    e é centrado e encostado embaixo na caixa nova (`noQuadroDoDesenho`), sem esticar. `desenharCrianca` e
    `desenharIrmaozinho` (públicas) fazem isso sozinhas; a casa por dentro (`TelaCasaPorDentro`, Fase 6) continua
    com o desenho antigo e não precisou mudar.
  * `RostoDaCrianca.kt`: sem mudança (só o desenho antigo usa; nada externo).
* `ManifestoDaArte.kt`: `InfoDaArte` ganhou `grupo`, `tingir`, `ordem`, `personagem` (com valores padrão, então
  os objetos da Fase 0 e quem constrói `InfoDaArte` não mudam).

## Animações (só transformações e troca de bitmap; nada que o Robolectric não renderize)

| animação | como |
|---|---|
| respirar | `graphicsLayer.scaleY` 1 → 1,025 (1,4 s, vai e volta), a partir dos pés |
| piscar | olhos `fechado` por ~4% do ciclo (criança 3,8 s, irmão 3,1 s: nunca juntos) |
| tchau | `acenando` e feliz: braços tchau1..4 em 720 ms (1, 2, 3, 4, 3, 2) |
| falar | `falando`: boca alterna humor ↔ surpreso a cada 160 ms |
| comemorar | torcendo: braços para cima e pulo com amassar e esticar em ciclo de 1,1 s (agacha 0,88, sai esticado 1,08, sobe 14% da altura, pousa amassado 0,90) |
| ginga | só o irmãozinho: `rotationZ` ±2,5° (como antes) |

O quadro (braços, olhos, boca) é um `derivedStateOf`: o desenho só é refeito quando ele muda; respirar, pulo e
ginga mexem só a camada. Parado (`animado = false` ou animações desligadas): um composto fixo; acenando mostra
tchau2. Uma criança acenando e piscando usa uns 8 compostos no cache.

## Chapéu (captura 56)

O emoji vai pela âncora: centro em `cabeca.x`, aba em `topoDaCabeca.y + 0,5 r` (r = `cabeca.y - topoDaCabeca.y`),
largura 1,9 r, fonte 0,82 da largura (a conta antiga). Ele fica dentro da camada animada: pula e respira junto.
`chapeuFicaNaCabeca` (CapturasTest) foi **adaptado, não apagado**: a criança agora tem 130 dp de altura
(85 x 130 dp, 170 x 260 px inteiros); a cabeça vem do manifesto (`crianca_cabeca`) posta na caixa por
`CamadasDosPersonagens.caixaDoQuadro` (a mesma conta do componente); confere antes que os dois perfis usam as
camadas. As regras continuam: chapéu desenhado, não cortado no alto, não flutua (base ≥ topo - 0,15 r), não cobre
o rosto (base ≤ centro), centrado (± 0,3 r) e não largo demais (≤ 2,6 r).

## Testes e capturas

* Novo `ui/componentes/CamadasDosPersonagensTest.kt` (JUnit puro, 10 testes): todas as combinações (2 gêneros x
  3 estilos x 4 cores x com/sem chapéu x 112 quadros, criança e irmão) têm todas as camadas, em ordem, no mesmo
  quadro e uma por grupo; rosto por humor e piscar; laço e tranças; estilo fora da faixa; braços e tchau; pulo
  (começa e termina parado, amassa e estica, sem saltos, volume quase constante, nunca abaixo do chão); cores de
  tingir; traje e acessório ficam no desenho antigo; caixa do quadro; chapéu pela âncora.
* `CapturasArte3DTest.personagensEmCamadas` (novo, captura **nova** `v3d_01_personagens.png`): os 6 cabelos, peles,
  cores e roupas, irmãozinho e irmãzinha, um com chapéu, os 4 quadros do tchau e o piscar. Antes da captura confere
  o composto: lido, canto transparente, corpo opaco, e pele escura dá figura mais escura (tingimento funcionou).
* As capturas 01, 05, 11 e 70 não mudaram de código: usam perfis sem traje, então mostram os personagens novos.
  Nenhuma captura foi renomeada.

## Verificação feita aqui

* JVM puro (Gradle temporário, Kotlin 2.0.21, `--offline`, `workingDir` = `app/`): `ManifestoDaArteTest` 6/6 e
  `CamadasDosPersonagensTest` 10/10 verdes.
* **Compilação do Compose:** `ManifestoDaArte`, `CamadasDosPersonagens`, `PersonagemEmCamadas`, `Crianca`,
  `Irmaozinho`, `RostoDaCrianca` e `ImagemDeArte`, mais todo o `dominio/`, compilaram sem erro e sem aviso contra o
  Compose Multiplatform desktop 1.7.3 (mesma API do Compose 1.7 do BOM 2024.12.01; baixado do Maven Central) com
  stubs mínimos para `android.graphics.*`, `LruCache`, `Context`, `LocalContext`, `asImageBitmap` e
  `animacoesLigadas`. Os testes Robolectric (`CapturasTest`, `CapturasArte3DTest`) não foram compilados (sem SDK);
  revisados à mão. Quem prova é o CI.
* Conferi em Python (PIL) a composição e a caixa: com os pés em 95% da altura, nada sai por cima e a sombra passa
  um pouco embaixo (como no desenho antigo); a caixa do chapéu cobre o alto da cabeça sem chegar aos olhos.

## Pontos de atenção

* **Tamanho aparente:** o quadro novo tem margens (cabelo começa em 12% da altura, pés em 90%); com os pés descidos
  para 95% da caixa, a criança 3D ocupa de 6% a 95% da altura (a antiga, de 6% a 99%): fica uns 5% menor e com a
  cabeça proporcionalmente maior. As telas não mudaram; se precisar, ajustar nas Fases 3 a 8.
* **Proporções novas** (`PROPORCAO_CRIANCA` 0,62 → 0,654; `PROPORCAO_IRMAO` 0,74 → 0,663) mexem um pouco no layout de
  quem as usa (`TelaTrilha`, `CenaDaCasa`, `TelaCasaPorDentro`): caixas 5% mais largas na criança e 10% mais
  estreitas no irmão. Na casa por dentro o chapéu do desenho antigo ficou ~5% maior (a conta usa a largura).
* **Trajes e acessórios** (16 roupas, 18 acessórios) ainda não têm camadas: com qualquer um deles a criança usa o
  desenho antigo. Telas com miniaturas de trajes (provador, loja, guarda-roupa, capturas 18, 58 a 60) misturam
  os dois estilos até a Fase 7. Faltam no manifesto: camadas de traje por formato de corpo e de acessório por parte.
* **Memória:** camadas soltas (até ~690 KB cada, decodificadas) no cache da `ArteDoApp` (1/8 da memória) e compostos
  (até 690 KB cada) num cache de 1/16. Em aparelho com heap de 128 MB pode haver despejo e recomposição; para a
  Fase 9: recortar cada camada pelo retângulo do alfa ao decodificar (olhos, boca e braços ficam bem menores).
* **WebP no Robolectric** (risco aberto da Fase 0): se não decodificar, `podeUsar` dá falso e o app cai no desenho
  antigo; os testes `chapeuFicaNaCabeca` e `personagensEmCamadas` acusam com mensagem clara.
* `TelaCasaPorDentro` desenha a criança direto no `DrawScope` (sem `Context`): continua com o desenho antigo (Fase 6).
