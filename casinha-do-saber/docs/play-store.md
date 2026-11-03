# Publicação na Google Play

Material pronto para a ficha da loja e para os formulários do Play Console. Os textos foram conferidos com o app atual (veja `docs/PRD.md`, seção 5) e com os limites do Play Console. Os números entre parênteses são a contagem de caracteres.

## Ficha da loja (português do Brasil)

**Nome do app** (até 30 caracteres; este tem 26):

```
Casinha do Saber: Aprender
```

**Descrição curta** (até 80 caracteres; esta tem 77):

```
Números, letras, inglês e lógica para crianças. Ganhe peças e monte sua casa!
```

**Descrição completa** (até 4000 caracteres; esta tem cerca de 3100). Copie o bloco abaixo:

```
Na Casinha do Saber, a criança aprende brincando e cada fase vencida vira uma peça da sua casinha.

MONTE A SUA CASINHA
A criança escolhe ser menino ou menina e escolhe a cor da pele, do cabelo, o penteado e a roupa. A cada fase nova vencida, ganha um presente para a casa: terreno, paredes, telhado, porta, cama, sofá, geladeira, cachorrinho, laguinho com patinho e muito mais. São 40 peças. Dá para arrastar os objetos pelo jardim e pelos cômodos, pintar as paredes e ver a casa de dia e de noite.

ENTRE NA CASA
A porta leva para dentro: uma casa de dois andares com escada, quarto, banheiro, sala e cozinha. Os móveis mudam de lugar com o dedo. A geladeira abre e guarda as comidinhas, o guarda roupa guarda as roupas, e a torneira, a TV, o fogão, a banheira e as luzes funcionam com um toque.

ROUPAS E ACESSÓRIOS
São 16 roupas (pijama, fada, astronauta, pirata, dinossauro e outras) e 18 acessórios, como óculos, cachecol, mochila e balão. Tudo se compra na lojinha com as moedinhas ganhas nas fases e se experimenta no espelho.

200 FASES EM 5 MUNDOS
Quatro caminhos com 50 fases cada, passando pelos mundos Jardim, Floresta, Praia, Montanha e Espaço:
NÚMEROS: contar até 20, comparar, somar e subtrair, de 10 em 10, dezenas e unidades com material dourado, dobro e metade, horas cheias, moedinhas e probleminhas narrados.
LETRAS: vogais e consoantes, som das letras, sílabas, rimas, montar palavras, ler palavras e frases curtas e traçar letras com o dedo.
INGLÊS: cores, animais, números, frutas, corpo, casa, brinquedos, escola, sentimentos e frases como "I like", com a pronúncia em inglês.
LÓGICA: igual e diferente, sequências, sombras, grupos, jogo da memória, sudoku de figuras e labirinto.

ATIVIDADES DE MEXER
Além de tocar na resposta certa, a criança arrasta frutas para a cesta, separa grupos, monta palavras com sílabas, encontra os pares da memória e traça letras e números seguindo as setinhas. Uma mãozinha mostra o gesto antes.

MODO TREINO
Depois de vencer as primeiras fases, o botão Treinar sorteia perguntas das fases já vencidas, com mais atenção ao que a criança ainda erra.

FEITO PARA QUEM AINDA NÃO LÊ
As perguntas são faladas em voz alta, os botões e as figuras são grandes e nenhuma leitura é obrigatória para jogar.

SEM FRUSTRAÇÃO
Errar faz parte: a criança ouve o nome do que tocou e tenta de novo. Depois de duas tentativas, a fase fica mais fácil e começa com um exemplo resolvido junto.

ÁREA DOS PAIS
Protegida por uma continha, mostra o progresso de cada caminho e de cada mundo, o tempo jogado e permite escolher um limite diário de 15, 30 ou 60 minutos, ajustar sons, música e vozes e apagar o progresso.

CELULAR E TABLET
No tablet as telas ficam maiores e o app também funciona deitado.

SEGURO PARA A FAMÍLIA
Sem anúncios. Sem compras com dinheiro de verdade. Sem internet: funciona totalmente offline. Nenhum dado é coletado ou enviado.

Indicado para crianças de 4 a 6 anos.

Observação: a narração usa o motor de "Texto para fala" do próprio aparelho. Se a voz não aparecer, instale ou atualize o "Serviço de fala do Google" e baixe a voz em português.
```

**Categoria:** Educação. **Tags sugeridas:** educação infantil, alfabetização, matemática, inglês para crianças, jogos educativos.

**Email de contato:** guilhermegatdef@gmail.com. **Site:** pode ficar em branco. **Política de privacidade:** veja `docs/publicar.md`, seção 4.

**Novidades desta versão** (primeiro envio):

```
Primeira versão da Casinha do Saber: 200 fases de números, letras, inglês e lógica, casa com 40 peças para montar e decorar, roupas, acessórios e modo Treino.
```

### Imagens da ficha

Geradas pelo CI no tamanho exato (teste `LojaPlayTest`) e salvas em `docs/loja/`:

| Campo do Play Console | Arquivo | Tamanho |
|---|---|---|
| Ícone do app | `icone_512.png` | 512 x 512, PNG de 32 bits, sem cantos arredondados (a Play arredonda) |
| Imagem de destaque | `destaque_1024x500.png` | 1024 x 500, PNG de 24 bits, sem transparência |
| Capturas de tela do telefone | `loja_celular_01.png` a `loja_celular_08.png` | 1080 x 1920 (9:16) |
| Capturas de tablet de 7 polegadas | `loja_tablet7_01.png` a `loja_tablet7_04.png` | 1200 x 1920 |
| Capturas de tablet de 10 polegadas | `loja_tablet10_01.png` a `loja_tablet10_04.png` | 1920 x 1200 |

Todas seguem as regras atuais: PNG sem canal alfa nas capturas e no destaque, lado entre 320 e 3840 pixels e lado maior no máximo o dobro do menor. As capturas de celular, na ordem: início com a casa, cesta de arrastar, prêmio, casa completa, casa por dentro, espelho com roupas e acessórios, caminho com os mundos e traçar letra.

## Formulários do Play Console (Conteúdo do app)

### Acesso ao app
* Resposta: **Todas as funcionalidades estão disponíveis sem acesso especial.**
* O app não tem login, conta nem senha. A Área dos pais abre respondendo uma continha de multiplicação que aparece na própria tela (não é um login). Se quiser, escreva isso no campo de instruções.

### Anúncios
* O app contém anúncios? **Não.**

### ID de publicidade
* O app usa o ID de publicidade? **Não.**
* Conferido: o `AndroidManifest.xml` não pede `com.google.android.gms.permission.AD_ID` e nenhuma dependência (Compose, AndroidX, emoji2) acrescenta essa permissão. Por segurança, o manifesto agora tem a linha `tools:node="remove"` para a permissão `AD_ID`, e o CI confere o manifesto final do release: falha se aparecer `AD_ID` ou `INTERNET`.

### Classificação do conteúdo (questionário IARC)
* Email de contato: guilhermegatdef@gmail.com.
* Categoria: **Referência, notícias ou educação** (se o formulário oferecer, ou "Todos os outros tipos de apps").
* Violência, medo, sexualidade, linguagem imprópria, drogas, álcool, tabaco, humor grosseiro: **Não** para todas.
* Jogos de azar ou apostas simuladas: **Não** (as moedinhas são ganhas nas fases e não têm valor real).
* Os usuários interagem ou trocam conteúdo entre si: **Não.**
* Compartilha a localização do usuário: **Não.**
* Permite compras de produtos digitais: **Não.**
* Acesso irrestrito à internet: **Não** (o app não acessa a internet).
* Resultado esperado: **Livre** no Brasil (ClassInd L), PEGI 3, ESRB Everyone.

### Público alvo e conteúdo
* Faixas etárias: **5 anos ou menos** e **6 a 8 anos** (o app é para crianças de 4 a 6 anos, segundo o PRD).
* Com essas faixas, o app entra nas regras do programa **Famílias**: aceite a política de Famílias. O app já cumpre: sem anúncios, sem SDKs de terceiros, sem coleta de dados, sem links para fora, sem compras, política de privacidade publicada.
* O app pode atrair crianças sem querer? Não se aplica (ele é para crianças).
* Ficha da loja voltada a crianças: sim, os textos e imagens são adequados.

### Segurança dos dados
* O app coleta ou compartilha algum dos tipos de dados do usuário exigidos? **Não.**
* Com "Não", o formulário termina sem as perguntas de criptografia e exclusão. Se o Console pedir, os dados ficam só no aparelho e a Área dos pais tem "Apagar todo o progresso".
* Motivo: o progresso (nome opcional, aparência, estrelas, moedinhas, arrumação da casa) fica salvo apenas no aparelho, em SharedPreferences. O backup automático do Android do próprio usuário não conta como coleta pelo desenvolvedor.
* Permissões do app: nenhuma permissão perigosa, sem internet, sem microfone, sem câmera, sem localização.

### Apps governamentais
* O app foi criado por um órgão do governo ou em nome dele? **Não.**

### Recursos financeiros
* Resposta: **Meu app não oferece nenhum recurso financeiro.**

### Saúde
* O app tem recursos de saúde? **Não** (marque que não tem nenhum recurso de saúde).

### Apps de notícias
* O app é de notícias? **Não.**

### Outras declarações que podem aparecer
* Política de privacidade: link na seção 4 de `docs/publicar.md`.
* Apps de VPN, rastreamento de localização, conteúdo gerado pelo usuário, acesso a todos os arquivos, permissões de foto e vídeo: **não se aplica / Não**.
* Preço: **Gratuito**. Países: comece pelo **Brasil** (o app é em português; as fases de inglês são parte do conteúdo).

## Checklist técnico

* targetSdk 35 e minSdk 24 (Android 7 ou mais novo).
* Ícone adaptativo e ícone monocromático (Android 13).
* Release com R8 (código reduzido) e recursos reduzidos.
* Pacote `.aab` assinado gerado pelo CI quando os segredos da chave de upload existem (veja `docs/publicar.md`).
* Funciona totalmente offline, inclusive a voz (motor de "Texto para fala" do aparelho).
* A cada novo envio, aumente `versionCode` em `app/build.gradle.kts`.
