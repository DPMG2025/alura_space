# PRD: Casinha do Saber

Documento de produto para continuar o desenvolvimento numa nova sessão. Atualizado em 3 de novembro de 2026.

## 1. Visão

App Android educativo para crianças a partir de 4 anos. A criança escolhe ser **menino ou menina**, aprende **Números, Letras (português), Inglês e Lógica** e, a cada fase nova vencida, ganha peças para **montar e decorar a sua casinha**. A casa é o motor da motivação.

**Meta do dono do produto:** qualidade de app AAA, nota 5 na Play Store, ou seja, um app extraordinário. "Nota 5" só existe depois de publicar e receber avaliações reais; até lá, o critério é a qualidade AAA.

## 2. Público e princípios

* Crianças de 4 a 6 anos que **ainda não leem**: tudo é narrado em voz alta (TTS do Android, offline), com botões e figuras grandes.
* Pais: área protegida (continha de multiplicação), progresso, limite de tempo, privacidade.
* Errar não pune: a criança ouve o nome do que tocou e tenta de novo; depois de 2 erros a resposta certa brilha.
* Sem internet, sem anúncios, sem coleta de dados, sem compras.
* Arte 100% em código (Canvas do Compose) e emojis da fonte Noto embutida (emoji2-bundled). Não há arquivos de imagem.

## 3. Onde está o código

* Repositório: `DPMG2025/alura_space`, branch **`claude/educational-kids-android-app-7bdyy3`**, pasta `casinha-do-saber/`.
* Kotlin + Jetpack Compose, minSdk 24, targetSdk 35, AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, Gradle 8.14.3.
* Pacote `br.com.casinhadosaber`:
  * `dominio/` (Kotlin puro, testável): `Modelos.kt` (Pergunta, Opcao, Visual, Cena, Atividade, Trilha, Fase), `GeradorDeFases.kt` (todas as fases), `Vocabulario.kt`, `ItensDaCasa.kt` (40 peças, Lugar, Comodo), `EstadoJogo.kt` (progresso, moedas, loja, missão, histórico, serialização), `Loja.kt` (produtos).
  * `dados/Repositorio.kt` (SharedPreferences, limite de tempo diário, som e música).
  * `audio/` : `Narrador.kt` (melhor voz offline por idioma, pausas, inglês mais lento, estado `falando`), `Sons.kt` (efeitos sintetizados, pré calculados, tocados em paralelo), `Musica.kt` (música de caixinha gerada).
  * `ui/componentes/` : `CenaDaCasa.kt` (desenho da casa, arrastar, dia e noite, clima, visitantes, jardim extra), `Crianca.kt` (personagem: gênero, pele, cabelo, 3 penteados, roupa, chapéu, expressões, respiração, boca falando), `Comuns.kt` (BotaoFofo, MaoGuia, estrelas, pulsando), `Confete.kt`, `PortaoDeAdulto.kt`.
  * `ui/telas/` : Perfil, BoasVindas, Inicio, Trilha (caminho), Jogo, Premio, Quase, Casa, Pais, Dormir.
  * `MainActivity.kt` : navegação (sealed `Tela`), limite de tempo, tablet (escala 1,3 e largura máxima 640dp).

## 4. Como verificamos (o ciclo que funciona)

O ambiente da sessão **não acessa o Google Maven**, então o APK é compilado no **GitHub Actions** (`.github/workflows/casinha-do-saber.yml`):

1. Testes de unidade do domínio (também rodam localmente, num projeto Gradle JVM em `scratchpad/jvmtest` que aponta para `dominio/`).
2. **Capturas de tela** com Robolectric + Roborazzi (`CapturasTest.kt`), commitadas pelo CI em `docs/capturas/`. Inclui teste de pixels que falha se chuva ou neve aparecerem dentro da casa.
3. Lint, APK debug e release, pacote `.aab`.
4. **Versão de teste para celular**: o CI publica `CasinhaDoSaber.apk` na Release `casinha-teste`:
   https://github.com/DPMG2025/alura_space/releases/download/casinha-teste/CasinhaDoSaber.apk
5. **Emulador Android real** (`JogoNoEmuladorTest.kt`): perfil, boas vindas, fase inteira com um erro, presente com 3 toques, item entra na casa, arrastar a cama, cama recusada no jardim, relatório das vozes. Resultados em `docs/emulador/`.
6. **Revisor independente**: um subagente avalia as capturas com régua de app AAA e dá nota. Evolução: 6 → 7 → 7,5 → 8 → 8,5 → 8,5 → 9 → 9.

Ler os logs do CI pelo MCP do GitHub (`actions_list`, `get_job_logs`). Cada push cancela o build anterior (concurrency); o passo de capturas usa `git pull --rebase -X theirs`.

## 5. O que já está pronto

**Aprendizado**
* 4 caminhos com **25 fases cada (100 fases)** no domínio, 5 perguntas por fase, perguntas sorteadas.
* **4 opções em todas as perguntas de escolha**.
* Fase só é vencida com **4 de 5 acertos de primeira** (3 estrelas com 5, 2 com 4). Abaixo disso, tela "Quase!" com o presente trancado.
* Depois de 2 derrotas seguidas na mesma fase, rodada facilitada com um exemplo resolvido junto, que não conta.
* Uma pergunta de revisão de fase anterior em cada partida; **Missão do dia** (3 perguntas das fases mais fracas, +6 moedas, uma vez por dia).
* Conteúdo: contar até 20, cesta, mais ou menos, maior e menor, formas, somar e tirar até 10, historinhas, antes e depois, de 2 em 2; vogais, consoantes, maiúscula e minúscula, primeira letra, começa igual, leitura simples e difícil, sílabas, quantos pedacinhos, montar palavra, completar, rimas; inglês de cores, animais, números, frutas, corpo, formas, roupas, casa, comida, brinquedos, tempo, família, veículos, ações; lógica de igual, diferente, tamanho, filas AB, ABC, AAB e ABB, sombras, combina, grupos, cesta de classificar, memória, números em fila.

**Casa**
* 40 peças em ordem (terreno, paredes, telhado, porta, cama...); estruturas "caem do céu", objetos podem ser arrastados.
* Regras: móveis só dentro de casa, coisas de jardim só fora (o objeto volta e o narrador explica); móveis pousam no piso do cômodo; elogio quando vai para o cômodo certo; quadros desviam das janelas.
* Dia e noite, chuva e neve (só do lado de fora), animações (bichos respiram, pato boia, borboleta voa), brilhinhos ao tocar, visitantes passeando, jardim extra decorado em telas altas.
* Pintar paredes por cômodo; loja com **moedinhas** (1 por acerto de primeira, peso maior em fase difícil, até 30 por dia): telhados, portas, papéis de parede, chapéus, visitantes, chuva e neve; **experimentar antes de comprar**; o que foi comprado continua seu.
* **Depois das 40 peças, cada fase nova dá um presente da loja de graça + 5 moedas** (domínio pronto: `ResultadoPartida.brinde`).

**Experiência**
* Primeira tela com personagem grande, penteados, escolhas faladas; boas vindas no terreno; mãozinha guia (MaoGuia) onde tocar.
* Presente abre com 3 toques e raios de luz; prêmio vai direto para a casa.
* Área dos pais: precisão por caminho e por fase (últimas 10), "Praticando agora" com atividade fora da tela, tempo jogado, limite diário de 15/30/60 min ("a casinha vai dormir", persistente), instalar vozes melhores, som e música, apagar progresso.
* Material da Play Store: `docs/play-store.md`, `docs/politica-de-privacidade.md`, `docs/publicar.md` (chave de upload via segredos do GitHub).

## 6. Em andamento (começar por aqui)

**Feedback do dono do produto nesta sessão:**

1. **4 opções em tudo.** Feito no domínio.
2. **Mais tipos de atividade**, não só ouvir e tocar. O domínio já gera, em `Pergunta.atividade`:
   * `Atividade.Cesta` (arrastar N figuras para a cesta e tocar no certinho; ou classificar, quando `errados` não é vazio: só as do grupo entram);
   * `Atividade.Memoria` (pares; `falas` diz a palavra ao virar a carta, por exemplo em inglês);
   * `Atividade.MontarPalavra` (arrastar sílabas na ordem; há sílabas extras);
   * `Atividade.Tracar` (passar o dedo sobre a letra pontilhada);
   * `Visual.Sombra` (opções desenhadas como silhueta; já renderizado em `TelaJogo`).

   **Falta a interface de Cesta, Memória, Montar palavra e Traçar em `TelaJogo.kt`.** Hoje essas perguntas aparecem com uma única opção "✔" (`Pergunta.deAtividade`), o que é um placeholder e **não pode ir para a criança assim**. Cada atividade deve chamar o mesmo fluxo de resposta (acertou de primeira ou não, avança, confete, som).
3. **100 fases** (25 por caminho): domínio pronto. Revisar no app: o caminho (`TelaTrilha`) agora tem 25 bolinhas; a tela de prêmio precisa mostrar o **brinde** (`resultado.brinde`) quando não há peça da casa; a área dos pais lista 100 fases (avaliar agrupar por caminho com recolher).
4. **Trocar a aparência da criança a qualquer momento**: adicionar botão 🪞 (espelho ou guarda-roupa) na tela da casa e no início, que abre `TelaPerfil` e volta para onde estava.
5. **Casa em 3D onde a criança anda**, com mais espaço para comprar roupas, encher a geladeira e mobiliar. Decisão proposta (aguardando confirmação do dono): **casa em perspectiva isométrica (2,5D)** desenhada em código, estilo Toca Boca ou The Sims, com:
   * a criança andando (tocar no chão e ela vai até lá), entrando nos cômodos;
   * geladeira que abre e recebe comidas compradas;
   * guarda-roupa para trocar de roupa (roupas viram itens da loja);
   * mais móveis e objetos interativos.

   3D "de verdade" (Filament ou SceneView) exige modelos 3D de artista; sem eles fica pior que o desenho atual. Se o dono quiser 3D real, é preciso conseguir modelos prontos (loja de modelos glTF).

## 7. Pendências apontadas pelo revisor (nota atual 9/10)

* Objetos do jardim ainda apertados na faixa de cima (as coordenadas dos objetos só vão de 0 a 1 na cena; o jardim extra das telas altas é decorativo).
* Tablet: hoje só retrato, centralizado e 30% maior; avaliar paisagem.
* Testes de regressão visual além da chuva (sobreposição de objetos, chapéus).
* Confirmar a voz em português num celular real (no emulador escolheu `pt-br-x-pte-local`, qualidade 400).

## 8. Publicação (fora do alcance da sessão)

Criar a chave de upload e os segredos `CASINHA_KEYSTORE_*`, conta no Play Console, programa Famílias, teste interno e teste fechado (12 testadores por 14 dias para contas novas), testar com crianças reais e então produção. Passo a passo em `docs/publicar.md`.

## 9. Preferências do dono do produto

* Comunicação em português, sem hífen nem travessão no texto corrido.
* Quer ver o app no celular: manter o link da Release `casinha-teste` atualizado a cada build.
