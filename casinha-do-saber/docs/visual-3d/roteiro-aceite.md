# Roteiro de aceite (dono do produto e criança)

O que o CI e os subagentes não conseguem fechar sozinhos. Resultado de cada item entra no `ESTADO.md` (seção "Aceites").

## 1. Tela cheia no tablet Samsung (Fase 0A)
Instalar o APK mais novo (hoje: `docs/apk-parcial/CasinhaDoSaber-parcial.apk`, ou o da última execução verde do CI, na página de Actions, artefato `casinha-do-saber-apk`). Em Configurações, permitir instalar de fontes desconhecidas se o Android pedir.
Conferir, com o tablet deitado e em pé:
* [ ] Com o app aberto, nenhuma barra aparece (nem a de status, nem a de navegação, nem a barra de tarefas com os ícones de apps embaixo).
* [ ] Deslizar da borda inferior mostra as barras por instantes e elas somem sozinhas.
* [ ] Abrir e fechar o teclado (a área dos pais pede texto) e voltar: as barras somem de novo.
* [ ] Nada fica embaixo da câmera frontal.
* [ ] Opcional: na área dos pais há uma dica de como fixar o app (Configurações, Segurança, Fixar janelas).
Anotar: aparelho, versão do Android, e se algum atalho da barra de tarefas ainda foi tocado.

## 2. Teste de dez minutos com a criança (fases 1, 4 e 5 do PRD)
Sentar ao lado, não ensinar, só observar e anotar. Duas rodadas: a primeira com o personagem dela e o irmãozinho, a segunda com o jogo e o prêmio.
Anotar:
* Onde sorri (tela e momento).
* Onde toca sem querer (barra, botão pequeno, canto da tela).
* Onde desiste ou pede ajuda.
* O que ela acha do boneco (parece ela? o irmãozinho?) e do prêmio (entende o presente, as moedas?).
* Algum texto ou desenho que ela não entende (emoji velho, número).
* Se o jogo trava ou demora (nota do aparelho).
O resultado pode mudar a ordem do que falta.

## 3. Como registrar
Acrescentar ao `ESTADO.md`, em "Aceites": data, quem testou, aparelho, o que passou e o que falhou.
