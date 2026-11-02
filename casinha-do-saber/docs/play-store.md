# Publicação na Google Play

Material pronto para a ficha da loja e para os formulários do Play Console.

## Ficha da loja (pt-BR)

**Nome do app** (até 30 caracteres): Casinha do Saber: Aprender

**Descrição curta** (até 80 caracteres):
Números, letras, inglês e lógica para crianças. Ganhe peças e monte sua casa!

**Descrição completa:**

Na Casinha do Saber, a criança aprende brincando e cada conquista vira uma peça da sua casa!

🏠 MONTE A SUA CASINHA
Escolha ser um menino ou uma menina, com a cor da pele, do cabelo e da roupa. A cada fase vencida, um presente aparece: terreno, paredes, telhado, porta, cama, sofá, geladeira, cachorrinho, laguinho com patinho e muito mais. São 40 peças! Toque para construir, arraste os móveis para onde quiser e pinte as paredes de cada cômodo.

🔢 NÚMEROS: contar, comparar quantidades, somar e subtrair com figuras.
🔤 LETRAS: vogais, consoantes, primeira letra, sílabas, rimas e as primeiras palavras.
🌎 INGLÊS: cores, animais, números, frutas, corpo, casa e brinquedos, com a pronúncia em inglês.
🧩 LÓGICA: igual e diferente, grande e pequeno, sequências, grupos e o que combina.

👂 FEITO PARA QUEM AINDA NÃO LÊ
Todas as perguntas são faladas em voz alta. Botões grandes, figuras grandes e nada de texto obrigatório.

💛 SEM FRUSTRAÇÃO
Errar faz parte: a criança ouve o nome do que tocou e tenta de novo. Ninguém perde, todo mundo avança.

🔒 SEGURO PARA A FAMÍLIA
Sem anúncios. Sem compras. Sem internet. Nenhum dado é coletado. Área dos pais protegida para ver o progresso.

Para crianças a partir de 4 anos.

**Categoria:** Educação. **Tags:** educação infantil, alfabetização, matemática, inglês para crianças.

**Capturas de tela:** use as imagens de `docs/capturas/` (geradas automaticamente pelo CI).

## Público alvo e conteúdo

* Faixa etária alvo: 5 anos ou menos e 6 a 8 anos. Isso coloca o app no programa **Famílias** do Google Play.
* Classificação indicativa: Livre (questionário IARC: sem violência, sem conteúdo sensível, sem interação entre usuários, sem compras).
* Anúncios: não contém anúncios.
* Compras no app: nenhuma.

## Segurança dos dados (Data safety)

* O app coleta ou compartilha dados do usuário? **Não.**
* O progresso fica salvo apenas no aparelho (SharedPreferences) e pode entrar no backup do próprio Android do usuário.
* O app não pede nenhuma permissão perigosa e não acessa a internet.
* Política de privacidade: publique o texto de `politica-de-privacidade.md` (por exemplo, no GitHub Pages) e informe o link.

## Checklist técnico

* targetSdk 35 e minSdk 24 (Android 7 ou mais novo).
* Ícone adaptativo e ícone monocromático (Android 13).
* Release com R8 (código reduzido) e recursos reduzidos.
* Gerar o pacote: `./gradlew bundleRelease` com uma chave de upload própria (veja o README).
* Funciona totalmente offline, inclusive a voz (motor de "Texto para fala" do aparelho).
