# Estado da execução do PRD visual 3D

Memória do projeto entre sessões. Toda sessão nova lê o PRD e este arquivo, e nada mais, antes de delegar.

## Fases

Situação em 2026-11-07, 17:00 UTC. "Provada" = CI verde completo (build, tablet, celular, Android 7 e 8) naquele commit.

| Fase | Código e arte | Prova no CI | Revisor independente |
|---|---|---|---|
| 0A. Tela cheia | feito | tablet verde nas execuções 121, 122, 123, 125 e 128; confirmação no Samsung pendente | n/a |
| 0. Pipeline de arte | feito | build verde | n/a |
| 1. Personagens | feito | build verde; emulador de celular pendente | 7 (revisao-fase1.md) |
| 2. Interface | feito | build verde; emulador de celular pendente | 8 (revisao-fases-2-3-5-7.md) |
| 3. Início e casa de fora | feito | build verde (execução 128); celular pendente | 6 |
| 4. Jogo | feito (4A arte, 4B Kotlin) | build verde (128); celular pendente | pendente (revisão final) |
| 5. Prêmio e Quase | feito | build verde; celular pendente | 8 |
| 6. Casa por dentro | feito (6A, 6B) | build verde (128); celular pendente | pendente (revisão final) |
| 7. Guarda roupa | feito (7A, 7B, polimento de arte) | build verde; celular pendente | 6 |
| 8. Demais telas | feito (8A arte; 8B grupos A, B, C) | build verde (128); celular pendente | pendente (revisão final) |
| 9. Acabamento | polimento visual e medição feitos, locais/enviados; revisão final pendente | build verde; Android 7 e 8 (desempenho) rodando na 128 | pendente |

Pendências que não são do código: confirmação da tela cheia no tablet Samsung (dono do produto) e os testes de dez minutos com a criança (fases 1, 4 e 5).

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

* 2026-11-07, execução 128 (commit 962c3ad, Fases 0A a 8B + orquestrador + medição): build, tablet e Android 7 e 8 VERDES. Celular: com o Android Test Orchestrator, 23 testes rodaram com 0 falhas (todos passam); o emulador sumiu durante o 24º (DesempenhoTest, que não era para rodar no celular), de novo ~6 min depois do início, como em todas as quedas (121 a 128). Desempenho medido: Android 7 abre em 3,1 s (0,9 s a quente), memória máxima 113 MB; Android 8: 3,1 s, 134 MB (limite 384 MB). Decisão: os testes do celular rodam em dois emuladores seguidos (1: Diagnóstico, Emojis, TelaCheia, Vozes; 2: JogoNoEmuladorTest), cada um abaixo de 6 minutos; DesempenhoTest só nos Androids antigos.

## Problemas abertos

* Emulador de celular caindo em abrirMapaEAlbum desde a Fase 2B (ver acima). Bloqueia marcar as Fases 0A a 2 como provadas.

* Pedidos de arte da 2B: glifo branco (casa) para o botão verde; ícones de mapa, álbum, espelho, sol, lua, repetir, desfazer, ouvir, parar, gravar e lixeira.

* Memória das camadas (690 KB cada decodificada): recortar pelo contorno na Fase 9.

* Dúvida da Fase 0: o Robolectric pode não decodificar WebP; se a captura `v3d_00` falhar com "não decodificou", trocar o formato nos testes ou usar PNG nas capturas.
* Job de CI que confere imagens desatualizadas ainda não existe (opcional).

* Sem Android SDK na nuvem: nada de Fase 0A foi compilado nem testado localmente. Quem prova é o CI.
* Aceite da Fase 0A depende do dono do produto confirmar no tablet Samsung (One UI) que nenhuma barra aparece.
* O CI não rodou ainda nesta branch.

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
