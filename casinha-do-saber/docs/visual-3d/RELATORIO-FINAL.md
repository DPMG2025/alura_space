# Relatório final: visual "brinquedo 3D" da Casinha do Saber

Estado em 2026-11-07. Este arquivo resume o que foi feito; o detalhe vivo está em `ESTADO.md` e nos relatórios de `relatorios/`.

## O que foi entregue
* **Pipeline de arte (`arte3d/`)**: Three.js + Playwright geram imagens WebP com transparência em `app/src/main/assets/arte/`, com um `manifesto.json` único (825 imagens, 6,1 MB: 289 figuras `fig_*`, 86 ícones `icone_*`, personagens em 97 camadas por perfil, casa, prêmio, guarda roupa, jogo, interface).
* **Personagens em camadas** com tingimento (pele, cabelo, roupa), poses, humores, piscar, tchau, comemorar; roupas, acessórios e chapéus em camadas com a luz dos bonecos.
* **Interface**: fontes Baloo 2 e Fredoka, botões em bloco, pílulas, balões, estrelas e contas de progresso, ícones da barra em 3D (sem emoji de sistema na barra).
* **Telas**: início e casa de fora em diorama; jogo com cenário de ilha e 289 figuras 3D (90% das aparições dos emojis das perguntas); prêmio e Quase; casa por dentro isométrica com sprites 3D e noite; guarda roupa, loja e espelho; trilha, mapa, ateliê, piano, livros, diplomas, álbum, mascotes, áreas novas e as 17 atividades novas.
* **Tela cheia** (Fase 0A): modo imersivo no celular e no tablet; dica de fixar o app na área dos pais.
* **Guardas de qualidade no CI**: tamanho do APK abaixo de 150 MB, teste de desempenho em Android 7 e 8, cobertura mínima de emojis (75%), manifesto sem órfãos e dentro do orçamento, teste de tela cheia em emulador de tablet, celular em vários emuladores.

## Medidas
* APK de release: 16,8 MB (limite 150 MB).
* Android 7: abre em 3,1 s a frio (0,9 s a quente), memória máxima 113 MB; Android 8: 3,1 s, 134 MB (limite do aparelho 384 MB).
* Testes de unidade: mais de 560, todos verdes no último build.

## Revisões independentes
Fase 1: 7 (personagens). Fases 2, 3, 5, 7: 8, 6, 8, 6. Fases 4, 6, 8, 9: 7, 4, 4, 4 (geral 5) antes da segunda rodada de acabamento (arte em lote e Kotlin K1/K2). A revisão final depois dessa rodada ainda precisa ser refeita com as capturas do CI.

## O que o CI provou e o que não
* Build, lint, tamanho, tablet (tela cheia), Android 7 e 8: verdes.
* Emulador de celular: a causa das quedas foi encontrada (o emulador some depois de 6 a 8 minutos de vida; o app não vaza memória). Em seis execuções os testes que rodaram passaram. A divisão em vários emuladores seguidos é a correção em prova.

## O que depende do dono do produto (ver `roteiro-aceite.md`)
* Confirmar a tela cheia no tablet Samsung.
* O teste de dez minutos com a criança (fases 1, 4 e 5).

## Lições do processo
* Trabalho pesado em subagentes, cada um em worktree isolada, com integração por um integrador único: funcionou, mas conflitos entre grupos paralelos custam rodadas de CI (uma `val` duplicada derrubou o build).
* Todo push cancela o CI em andamento (grupo de concorrência): enviar só depois de ler o resultado.
* Testes de captura com `didOverflow*` dão falso positivo com a Baloo 2 no Robolectric; usar `isLineEllipsized`, `lineCount` e os limites do nó.
* Imagens parciais no git derrubam o teste de órfãos: só enviar manifesto e imagens consistentes.
* O token do workflow não cria releases neste repositório: o APK parcial é guardado em `docs/apk-parcial/`.

## Pendências conhecidas (sem arte)
* 111 de 356 coisas das áreas novas ainda em emoji; 149 emojis de pessoas, ações e carinhas sem figura; mundos 🌷 🏖️ 🏙️ 🏰 🏝️ 💎 🎡; acessórios do mascote; diploma grande.
