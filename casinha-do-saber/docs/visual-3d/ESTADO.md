# Estado da execução do PRD visual 3D

Memória do projeto entre sessões. Toda sessão nova lê o PRD e este arquivo, e nada mais, antes de delegar.

## Fases

| Fase | Situação | Data | Prova no CI |
|---|---|---|---|
| 0A. Tela cheia (celular e tablet) | código feito, aguardando CI e confirmação no tablet | 2026-11-07 | pendente |
| 0. Fundação do pipeline (`arte3d/`) | código feito, aguardando CI (captura `v3d_00_arte_de_teste`) | 2026-11-07 | pendente |
| 1. Personagens | pendente | | |
| 2. Sistema de interface | pendente | | |
| 3. Início e casa de fora | pendente | | |
| 4. Jogo | pendente | | |
| 5. Prêmio e Quase | pendente | | |
| 6. Casa por dentro | pendente | | |
| 7. Guarda roupa | pendente | | |
| 8. Demais telas | pendente | | |
| 9. Acabamento | pendente | | |

## Decisões

* 2026-11-07: a branch de trabalho `ccr-162c6a70-urxpmq` foi levada (fast-forward) para o ponto de `claude/casa-saber-graphics-review-02musb`, que tem o app e o PRD. A branch nasceu só com o commit inicial.
* 2026-11-07, Fase 0A: modo imersivo em `MainActivity` (`entrarEmTelaCheia`, chamado em `onResume` e `onWindowFocusChanged`) com `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. As telas já usam `WindowInsets.safeDrawing`, que inclui `displayCutout`, então o recorte da câmera segue respeitado. Dica de fixar o app na área dos pais (`TelaPais`). Teste `TelaCheiaTest` (androidTest) no emulador de celular (job `emulador`, roda todos os testes) e no novo job `emulador-tablet` (perfil `pixel_tablet`, API 34), que também entra no `needs` da publicação do APK.

* 2026-11-07, Fase 0: pipeline em `arte3d/` (`npm run render`), imagens em `app/src/main/assets/arte/` com `manifesto.json`; `ImagemDeArte` (LruCache + inSampleSize); orçamento 400 KB por imagem e 40 MB no total. Relatório em `relatorios/fase0.md`. Escala de render 2x (subir `ESCALA` em `render.js` se faltar nitidez).

## Problemas abertos

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
