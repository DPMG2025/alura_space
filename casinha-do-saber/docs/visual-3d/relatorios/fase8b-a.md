# Fase 8, parte B, grupo A: entrada, mascote, descanso e conquistas no visual brinquedo 3D (Kotlin)

Data: 2026-11-07. Worktree `/home/user/alura_space/.claude/worktrees/agent-a9e400f5af7bd1033`, branch
`worktree-agent-a9e400f5af7bd1033` (zerada em `ccr-162c6a70-urxpmq`, `faf7693`). Commit local, sem push; hash no fim.
Domínio, narração, workflow, `arte3d/`, `PecasDaInterface.kt` e as telas de outros grupos não foram tocados. Só a
arte da 8A já integrada ao manifesto (nenhum id faltou).

## Arquivos

* **Novo** `ui/componentes/PecasDoMascote.kt` (Kotlin puro): `PoseDoMascote` (parado, falando, feliz, dormindo;
  `de(feliz, falando, dormindo)`), `PecasDoMascote.id(especie, pose)` (`mascote_<bicho>_<pose>`), `idDaCoruja(pose)`
  (`mascote_coruja_parada/falando/feliz/dormindo`), `lugarDoEnfeite(acessorio, info)` (ponto do enfeite pelas
  âncoras `cabeca`/`topoDaCabeca` e o apoio) e `PecasDasTelas` (ids `ui_mao_toque*`, `item_terreno`, nuvens,
  `tela_diploma_rolo`, `tela_medalha`, `tela_selo`, `tela_figurinha_<caminho>/dourada/vazia`, `tela_lua_dormindo`,
  `tela_estrela_noite`, `tela_zzz`).
* **Novo** `ui/componentes/CenarioDaEntrada.kt`: `CeuDaEntrada` (degradê da maquete 1), `NuvensDoCeu` (duas nuvens
  3D boiando, uma transição), `IlhaAtras(largura, pes)` (a ilhinha `item_terreno` atrás de quem pisa, medida sem
  limite para a folga transparente não encolher a ilha), `TituloDeBrinquedo` (branco com contorno roxo),
  `TextoDoBotao` (branco com contorno escuro), `VerdeDoPronto`/`LabioDoVerde` (o verde do "Pôr na casinha").
* `ui/componentes/Comuns.kt`: **só** `MaoGuia` e `MaoQueArrasta` (assinaturas iguais): o 👆 virou
  `ui_mao_toque` / `ui_mao_toque_apertando` (o quadro apertando quando o dedo encosta), com a âncora `ponta` no
  mesmo lugar de antes (20 dp da esquerda e do topo) e o anel branco que pinga; o emoji ficou de reserva. Vale para
  todas as telas que usam a mãozinha.
* `ui/componentes/MascoteDesenhado.kt`: `MascoteAnimado` (novo parâmetro opcional `pose`; sem ele, feliz quando
  `pulando`, parado no resto) e `MascoteParado` mostram a arte 3D: escala do estágio a partir do apoio (os pés),
  brilho dourado atrás do grande e do campeão, respiro e pulinho como antes, e o enfeite em uso como emoji no ponto
  do `lugarDoEnfeite` (a arte dos enfeites ainda não existe). O Canvas antigo é a reserva (sem arte ou sem decodificar).
* `ui/componentes/Corujinha.kt`: `Corujinha(modifier, falando, pose = null)`: `mascote_coruja_falando` enquanto
  fala, `parada` no resto; o Canvas antigo é a reserva. Muda sozinha a história do mundo, os livrinhos e o relembrar.
* Telas (mesmas assinaturas):
  * `TelaPerfil`: céu com nuvens; "Casinha do Saber" e "Quem vai morar na casinha?" com contorno (o título de
    pouco contraste da revisão); o menino e a menina 3D de pé numa ilhinha, dando tchau (tocar num deles escolhe,
    afunda com mola), e dois blocos "Menino" (azul) e "Menina" (rosa) embaixo; a primeira escolha fica no meio da
    tela (sem o rodapé vazio). Saíram a mão 👆 entre os cartões, o terreno plano, a casa tracejada e os morros. A mão
    3D aponta o bloco "Menina" depois de 2,5 s. Na montagem: boneco na ilha (no espelho, a moldura dourada de
    antes), menino/menina em pílulas, ícones das linhas em botõezinhos claros, cores como contas de plástico,
    "Adulto: escrever o nome" em pílula, "Pronto!" verde com contorno (sem ✔).
  * `TelaQuemVaiJogar`: céu, título com contorno, blocos com lábio da própria cor, cada criança de pé numa ilhinha;
    criança nova (sem personagem) é a sementinha 3D `quase_semente_2` (antes 🌱).
  * `TelaBoasVindas`: "Vamos construir a sua casinha!" com contorno, sem a faixa branca; céu da maquete.
  * `TelasDoMascote`: cartões `cartaoFofo`; na escolha, cada bicho em 3D, o marcado na pose feliz; no cantinho, a
    pose falando ao abrir e a cada toque (1,8 s), depois feliz; a barrinha virou 5 contas verdes; enfeites guardados
    com o cadeado 3D (`icone_pais`) no lugar do 🔒; festa na pose feliz; "Pronto!" e "Oba!" verdes com contorno.
  * `TelaAlbum`: ícone 3D do álbum e título com contorno; o "x de y figurinhas" virou pílula (figurinha dourada +
    número; o total fica na descrição); abas com o ícone 3D do caminho; o "n de 11" da página virou contas na cor do
    caminho; cada figurinha é `tela_figurinha_<caminho>` (colada), `_dourada` (chefão) ou `_vazia` (falta), com o
    emoji na âncora `centro` (silhueta quando falta). O cartão desenhado ficou de reserva.
  * `TelaDiplomas`: rolo 3D e título com contorno; pílula com a medalha e o número; cabeçalhos com o ícone 3D do
    caminho; parede vazia com o rolo clarinho na moldura e contas das fases do mundo (sem "n de 10"); `tela_selo` nos
    quadros e `tela_medalha` no diploma aberto (os selos desenhados de reserva).
  * `TelaDormir`: `tela_lua_dormindo` balançando com o `tela_zzz` subindo, seis `tela_estrela_noite` piscando sobre o
    céu de antes, "Boa noite!" e "A casinha volta amanhã!" com contorno azul (sem 🌙 e 💤), "Adulto" com o cadeado 3D.
  * `AvisoDeConquistas`: rolo 3D do diploma ou a figurinha 3D com o emoji dela (+N quando são várias); sem 📜 e 📒.

## Testes e capturas

* Conferidos com grep e mantidos: `hasText("Menina")` e `contentDescription "Pronto"` (emulador), o texto
  "Vamos construir a sua casinha!", `quem_vai_jogar`, `crianca_<id>`, `mascote_<ESPECIE>`, `mascote_pronto`,
  `festa_mascote`, `figurinha_ganha`/`figurinha_falta`, `dica_do_album`, `diploma`, `diploma_aberto`,
  `aviso_conquista`, as descrições dos botões e do mascote (nome), "Corujinha Professora".
* **Novo** `PecasDoMascoteTest` (JUnit puro, 5 testes): arte toda no manifesto e com arquivo; poses com a mesma
  moldura e o mesmo apoio; enfeites dentro do bicho (chapéus acima da cabeça, medalha abaixo); a mão com `ponta` e os
  dois quadros no mesmo recorte; a janelinha das figurinhas perto do meio; poses por momento.
* **Novo** `CapturasEntradaEMascote3DTest` (Robolectric, painéis de 360 dp lado a lado, hdpi, cada painel com a
  configuração de um celular de 360 dp para a escala do tablet não entrar): `v3d_14_entrada` (perfil, quem vai jogar,
  boas vindas) e `v3d_15_mascote_e_conquistas` (escolha do mascote com o gatinho marcado, cantinho do dragãozinho de
  lacinho, álbum com o aviso de figurinha, diploma aberto, boa noite). Antes de capturar: arte decodificável, as
  marcas e nomes acima, nenhum 👆 🌱 📒 📜 🔒 ✔ na tela e nenhum texto saindo do painel (fora as abas que rolam).
* `CapturasIrmaoTest.rostos` (captura 70, nome igual): dois humores por fileira, cada par numa metade (antes os quatro
  passavam da borda direita e se sobrepunham).
* Mudam sozinhas: 01, 01b, 70, `s2_*` (mascote), as da Corujinha (`s3_*` e as de livrinhos e relembrar), `s6_*` (álbum, diplomas, aviso),
  `s9_*` (quem vai jogar), as do prêmio com o mascote, as de dormir e todas as que mostram a mãozinha (`MaoGuia`), e
  `e01`/`e01b` do emulador. Nenhuma captura renomeada.

## Verificação feita aqui

* JVM puro (Gradle temporário `--offline`, Kotlin 2.0.21, `workingDir` = `app/`): **270 testes, 0 falhas** (todo o
  `dominio/` e os de `ui/componentes/`, com os 5 novos).
* Compilação sem erro nem aviso contra Compose Multiplatform desktop 1.7.3 (API do BOM 2024.12.01), com os stubs da
  6B mais `faseDaVez`, `Sons.premio`, `Narrador.parar/falando` e `android.content.res.Configuration`: todo o
  `ui/componentes/`, `Moldura.kt`, `tema/`, `dominio/`, as 8 telas do grupo e o `CapturasEntradaEMascote3DTest`.
  `CapturasIrmaoTest` revisado à mão (precisa de telas de outros grupos). Rodar as capturas não dá aqui: quem prova o
  visual é o CI.

## Memória

* Tudo por `ImagemDeArte` (LruCache da `ArteDoApp`, decodificado no tamanho de exibição); nada em estático além dela.
* Perfil: 2 bonecos, a ilha (até ~400 px), 2 nuvens e a mão (144 px). Dormir: lua (320 px), zzz e **uma** estrela
  de 80 px desenhada seis vezes. Álbum: no máximo 4 fundos de figurinha (os 11 cartões da página dividem o mesmo
  bitmap por tipo). Mascote: só a pose da vez (até 400 px); a troca de pose decodifica uma imagem por vez.
* Animações infinitas: nuvens (uma transição), estrelas da noite (uma), lua e zzz (uma), as do mascote como antes.

## Pendências

* Enfeites do mascote em 3D (hoje o emoji no ponto da âncora) e a arte do diploma aberto (o pergaminho é Canvas).
* Emojis sem arte que ficaram: 🧑 💇 ✂️ 👕 ✏️ das linhas do perfil, os emojis dos mundos e das figurinhas, 🏆 e 🎉 de
  títulos do mascote, os enfeites do mascote.
* Riscos para olhar no CI: a ilha passando um pouco da caixa (folga transparente, sem corte); o lacinho do mascote
  pela âncora (posição aproximada); a figurinha torta com o emoji fora da janelinha em ângulos grandes (até 2,7°).
