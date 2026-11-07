# Fase 6, parte B: casa por dentro com a arte 3D (Kotlin)

Data: 2026-11-07. Commit local, sem push.
Worktree: `/home/user/alura_space/.claude/worktrees/agent-a0cc6985a4f5a6c58`, branch
`worktree-agent-a0cc6985a4f5a6c58` (zerada antes em `ccr-162c6a70-urxpmq`, `bdb62d1`, com a arte da 6A já no
`manifesto.json`). Commit do trabalho: ver `git log` da branch (o hash está no relatório de entrega; este arquivo
entra no mesmo commit).

## O que mudou

* **Novo** `ui/componentes/CamadasDaCasaPorDentro.kt` (Kotlin puro):
  * `GradeIsometrica`: as contas da projeção (`x`, `y`, `UNIDADE_Z = 0,56`). `Projecao` (`Isometrico.kt`) agora
    chama estas contas, então o teste JVM testa a mesma conta que o app usa.
  * `CamadasDaCasaPorDentro.lugar(info, escala, ladrilho, origem, referência)`: canto =
    `ponto(referência) - apoio x k`, tamanho = imagem x k, com `k = ladrilho da tela / (iso.ladrilho x escala)` (128 px).
    `ancora(...)` leva uma âncora da imagem para a tela.
  * `idDoMovel` (escada, escada de cima, geladeira e guarda-roupa abertos, o resto `dentro_<id>`),
    `referenciaDoMovel` (centro da base da pegada; quadro e espelho `(centroX, 0, z)`), referências da porta
    `(0; 3,95; 0)`, das janelas (`(centro, 0, 1,32)`: 2,1 e 6,05 embaixo, 3,22 em cima; redonda `(4,975; 0; 1,55)`),
    da parede interna (meio de cada ladrilho), das lâmpadas (`luzDo`: centro do cômodo a 2,62; banheiro `(7,6; 2; 2,85)`).
  * `AparenciaDoAndar` (andar, cores dos cômodos, papel, janelas, porta e cor, noite, tapete) e `cascas(ap)`: o
    fundo do andar na ordem do manifesto (laje 0, pisos 1, tapete, paredes 2 tingidas pela cor do cômodo, papel de
    parede do app, janelas e porta 2,5, acabamento 3). As 4 cascas com o apoio fora da imagem
    (`dentro_piso_banheiro/cozinha`, `dentro_parede_banheiro/cozinha`) se alinham pela mesma regra (referência
    `(0, 0, 0)`), sem caso especial.
* **Novo** `ui/componentes/CasaPorDentroEmCamadas.kt`: `ArteDaCasaPorDentro` (criada pela tela num `remember`,
  `limpar()` no `onDispose`; **nenhum bitmap em objeto estático**):
  * imagens dos móveis decodificadas já reduzidas para o tamanho de uso (`inSampleSize`), num mapa da tela (até 72);
  * **fundo do andar composto UMA vez** num bitmap por (aparência do andar, tamanho do ladrilho), no máximo **2**
    (um por andar), com o ladrilho do composto limitado ao da arte (128 px: um andar dá ~850 x 700 px, 2,4 MB; nunca
    cresce com a tela, é esticado no desenho). As camadas do fundo são lidas sem cache e recicladas logo depois de
    desenhadas. Composição com `CanvasDrawScope` num `ImageBitmap` (paredes tingidas com `BlendMode.Modulate`).
    Se faltar ou não der para ler uma casca, aquela casca entra no composto pelo desenho vetorial antigo; se nem o
    bitmap couber (OOM), o andar inteiro volta ao vetor.
* `ui/telas/TelaCasaPorDentro.kt` (mesma assinatura + `noite: Boolean = false`, para as janelas de noite):
  * `montarAndar`: fundo pelo composto; o desenho antigo do fundo virou `formasDaCasca` (reserva por casca e o papel
    de parede, que é sempre do app). Parede interna e lâmpadas (`dentro_lampada_acesa/apagada`, brilho do app) pela
    arte, cada uma com a reserva vetorial.
  * `montarPeca` monta a peça antiga (que continua dando a área de toque, a âncora e os pontos) e, havendo a imagem,
    troca as formas por ela. Geladeira e guarda-roupa só usam a arte com as duas imagens (fechada e aberta).
    Ficam vetoriais, presos à arte: tela da TV (paralelogramo medido na imagem da TV), comidas da geladeira nas
    âncoras `comida_0..8`, roupas nos `cabide_0..5` (recortadas na borda da porta da esquerda, px 118 da imagem),
    água da pia (`bico`, `bacia`), bolhas (`agua`), chamas do fogão, brilho do abajur, setinha da escada.
  * Cômodo apagado: o véu escuro de antes no piso e nas paredes (agora por polígono do cômodo) e as imagens dos
    móveis multiplicadas por um azul escuro.
  * Ordem de desenho igual: o mesmo `PlantaDaCasa.ordenarParaDesenho` das caixas; só a forma vira imagem.
  * A criança é desenhada em camadas 3D (`desenharPersonagemEmCamadas`, o mesmo das outras telas, com roupa e chapéu
    da loja; reserva: o desenho antigo). **O irmãozinho anda dentro de casa** (perfil com irmão ligado): segue o
    rastro da criança a 0,75 ladrilho, troca de andar junto, entra na ordem do pintor como a criança (quem está mais
    ao fundo primeiro), fala ao ser tocado (`Irmao.falaAoTocar`) e não deixa arrastar o móvel embaixo dele.
* Comportamento igual: arrastar (mesmas caixas, mesmo realce), tocar, abrir, acender, pintar, dica da mãozinha.

## Testes

* **Novo** `CamadasDaCasaPorDentroTest` (JUnit puro, 10 testes, contra o `manifesto.json` real): a grade é a do app;
  todos os ids pedidos existem, câmera `isometrico`, ladrilho 64 dp / 128 px, unidade z 0,56 e nenhum `dentro_*`
  sobrando; canto = referência projetada - apoio x k em 4 tamanhos; cada móvel (fechado e aberto) tem `movel`,
  `comodo`, `frente`, `estado`, pegada = caixa de oclusão e referência = a do app; a imagem de cada móvel cobre a
  caixa dele (6 px de tolerância a 128 px) e fica centrada; arrastar só desloca; pisos e paredes cobrem o cômodo
  (incluindo as 4 cascas com o apoio fora); janelas, porta e parede interna nos lugares do desenho antigo; lâmpada
  com o apoio na âncora `luz`; âncoras das animações dentro das imagens; fundo na ordem do manifesto, variantes de
  noite, porta e tapete, só as paredes tingidas.
* `OcultacaoNaCasaTest` **adaptado** (o teste de oclusão continua igual e ganhou
  `as imagens 3D dos moveis usam as mesmas caixas da oclusao`): a pegada de cada imagem é a caixa que a ordem usa.
* **Novo** `CapturasCasaPorDentro3DTest`: `v3d_13_casa_por_dentro` e `v3d_13b_casa_por_dentro_noite` (papel de
  bolinhas, outras cores). Antes de capturar confere que o fundo dos dois andares sai da arte (nenhuma casca no vetor,
  piso e parede opacos no composto) e que todos os móveis têm imagem legível.
* Capturas 32, 32b, 33, 34, 35 (e 36, 37, s8_14, as da loja `loja_*_por_dentro` e a do cantinho) mudam sozinhas.
  Nenhuma renomeada.

## Verificação feita aqui

* JVM puro (Gradle 8 temporário `--offline`, Kotlin 2.0.21, `workingDir` = `app/`): **266 testes, 0 falhas**
  (`dominio/` inteiro, `CamadasDaCasaPorDentroTest`, `CamadasDaCasaTest`, `CamadasDosPersonagensTest`,
  `ManifestoDaArteTest`, `PecasDaInterfaceTest`, `PecasDoPremioTest`).
* Compose: `TelaCasaPorDentro`, `ComodosNovos`, todo o `ui/componentes/` e `dominio/`, e o
  `CapturasCasaPorDentro3DTest` compilaram **sem erro nem aviso** nos arquivos novos e mudados contra Compose
  Multiplatform desktop 1.7.3 (mesma API do BOM 2024.12.01), com stubs de Android, áudio e Robolectric. As capturas
  não rodam aqui (sem SDK): quem prova o visual é o CI.
* Prévia em Python (PIL) dos dois andares com a mesma regra de alinhamento, tingimento e ordem: tudo no lugar,
  a tela da TV medida cai sobre a tela azul, as roupas aparecem entre as portas do guarda-roupa.

## Memória

* Por andar visível, um composto de até ~2,4 MB (ladrilho limitado a 128 px); no máximo 2 guardados (troca de andar
  não recompõe). Imagens dos móveis: dezenas de KB cada, decodificadas no tamanho de uso. Tudo some ao sair da tela.
* A composição só acontece numa falta do cache (mudar a cor da parede, o papel, a porta, noite ou o tamanho), nunca
  por quadro. Durante o arraste a aparência do andar não muda (o tapete não sai do lugar), então nada é recomposto.
* A criança e o irmão usam o cache já existente de `ComposicaoDosPersonagens` (estático, de fases anteriores, com
  teto de 24 MB); não foi mexido.

## Pendências e avisos

* Nenhuma arte faltou. Pedidos de arte (opcionais): a tela da TV como âncora (`tela`) em vez do paralelogramo
  medido; a borda da porta da esquerda do guarda-roupa aberto como âncora (hoje a constante 118 px); roupas
  penduradas como camadas tingíveis (hoje retângulos coloridos).
* `noite` é só parâmetro da tela (a `MainActivity` não passa: a noite é um estado local da `TelaCasa`). Ligar isso
  fica para a Fase 8/9.
* As áreas novas (`ComodosNovos`, `CenaDaArea`) e o telhado visto por dentro seguem vetoriais.
* Riscos a olhar nas capturas do CI: a criança 3D e o irmão no tamanho da cena (altura 2,15 e 1,42 unidades), o
  escurecimento dos móveis com a luz apagada e o encontro do composto com o gramado.
