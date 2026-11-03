# Casinha do Saber: regras de trabalho

Fonte da verdade do produto: `docs/PRD.md`. Regras de conteúdo das fases: `docs/plano-1000-fases.md` (seção 7).

## Orquestração (regra do dono, 2026-11-03)

- Até concluir os pedidos, manter **4 ou 5 subagentes trabalhando ao mesmo tempo**.
- A regra não é absoluta. Quando agentes simultâneos causariam conflito no mesmo código (mesmos arquivos, merge difícil, CI em série), usar menos e explicar o motivo.
- Como dividir o trabalho:
  - Cada agente de implementação trabalha num worktree próprio, criado a partir do branch de desenvolvimento, e só faz commit local. O coordenador junta tudo.
  - Arquivos disjuntos por agente sempre que possível. MainActivity, TelaPais e Repositorio só recebem edições pequenas e localizadas.
  - Um único agente cuida do CI por vez (build, capturas, lint e emulador), porque a concorrência do workflow cancela execuções anteriores.
  - Revisores somente leitura podem rodar em paralelo, sem limite de conflito, e entregam achados verificados para o agente de integração aplicar.
- O APK de teste (Release `casinha-teste`) só é publicado depois que build, emulador e emulador antigo passam.

## Regras do produto que nunca mudam

- Totalmente offline: sem permissão INTERNET nem AD_ID (o CI falha se aparecerem).
- Sem notificações, sem recompensas pagas ou aleatórias, sem castigo e sem perder sequência. O mascote nunca fica triste.
- O limite diário de tempo dos pais continua valendo.
- O nome real da criança nunca entra no código: vem só do perfil, no aparelho.
- O microfone só é pedido na área dos pais.

## Lições técnicas

- Não usar `vararg` de `Offset` (value class).
- O lint trata `UnusedBoxWithConstraintsScope` como erro.
- O Robolectric só desenha na hora da captura.
- Laço infinito com `withFrameNanos` impede o emulador de ficar ocioso: usar `withInfiniteAnimationFrameNanos` ou `rememberInfiniteTransition`.
- `sortedBy` com chave sorteada quebra a ordenação: usar `ordenarUmaVez`.
- Emojis precisam existir no emoji2 1.5.0 (o teste de emojis roda no Android 7 e 8).
- O build Android só compila no GitHub Actions. Localmente dá para checar o domínio com JVM e os tipos da UI com Compose Desktop.
