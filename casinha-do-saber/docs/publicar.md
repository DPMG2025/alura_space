# Passo a passo para publicar na Google Play

O projeto já gera tudo o que a loja pede: o pacote `.aab`, as imagens da ficha (`docs/loja/`), os textos e as respostas dos formulários (`docs/play-store.md`) e a política de privacidade. Faltam só as etapas que exigem a sua conta.

## 1. Criar a chave de upload (uma vez só, no seu computador)

A chave de upload assina o pacote que você envia à Play. Guarde o arquivo `casinha-upload.jks` e as senhas num lugar seguro (por exemplo, um gerenciador de senhas e uma cópia num pendrive). **Nunca** coloque esse arquivo no GitHub. Se perder a chave, dá para pedir uma nova ao suporte da Play (com a Assinatura de apps do Google Play ativada), mas demora.

### No Windows (PowerShell)

O `keytool` vem junto com o Android Studio. Abra o PowerShell na pasta onde quer guardar a chave e rode:

```
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v -keystore casinha-upload.jks -alias casinha -keyalg RSA -keysize 2048 -validity 10000
```

Ele pede uma senha (anote), seu nome, cidade, estado e o código do país (`BR`). Use a mesma senha para o arquivo e para a chave, se ele perguntar as duas.

### No Linux ou no Mac

```
keytool -genkeypair -v -keystore casinha-upload.jks -alias casinha -keyalg RSA -keysize 2048 -validity 10000
```

## 2. Cadastrar a chave como segredo do GitHub

O GitHub guarda a chave em texto (base64). Para converter:

* **Windows, PowerShell (recomendado):** na pasta da chave, rode o comando abaixo. O texto vai direto para a área de transferência, pronto para colar.
  ```
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("casinha-upload.jks")) | Set-Clipboard
  ```
* **Windows, Prompt de Comando:** `certutil -encode casinha-upload.jks saida.txt`, abra `saida.txt` e copie só o texto entre as linhas `BEGIN` e `END` (sem essas duas linhas). Apague `saida.txt` depois.
* **Linux:** `base64 -w0 casinha-upload.jks`. **Mac:** `base64 -i casinha-upload.jks`.

No repositório: **Settings > Secrets and variables > Actions > New repository secret**. Crie quatro segredos:

| Nome | Valor |
|---|---|
| `CASINHA_KEYSTORE_BASE64` | o texto copiado acima |
| `CASINHA_KEYSTORE_PASSWORD` | a senha do arquivo |
| `CASINHA_KEY_ALIAS` | `casinha` |
| `CASINHA_KEY_PASSWORD` | a senha da chave (a mesma, se você usou uma só) |

### Onde baixar o `.aab` assinado

O workflow `.github/workflows/casinha-do-saber.yml` faz assim: o passo "Preparar chave de upload" decodifica o segredo `CASINHA_KEYSTORE_BASE64` num arquivo temporário e grava o caminho em `CASINHA_KEYSTORE_PATH`; o passo "Gerar APK e pacote da Play Store" roda `bundleRelease` com as senhas, e o `app/build.gradle.kts` usa essa chave no release. Sem os segredos, o release sai com a chave de debug, que serve só para teste e é **recusada** pela Play.

Depois de cadastrar os segredos, faça qualquer push no branch (ou rode o workflow à mão em **Actions > Casinha do Saber (Android) > Run workflow**). Quando o job `build` terminar:

1. Abra a execução em **Actions**.
2. Em **Artifacts**, no fim da página, baixe **casinha-do-saber-play-store**.
3. Descompacte o zip: dentro está `app-release.aab`, já assinado. Esse é o arquivo que vai para o Play Console.

No log do passo "Preparar chave de upload" deve aparecer "Chave de upload configurada". Os artefatos ficam disponíveis por 90 dias.

## 3. Criar a conta e o app no Play Console

1. Crie a conta de desenvolvedor **pessoal** em play.google.com/console (taxa única de 25 dólares, verificação de identidade com documento e verificação de um celular Android pelo app Google Play Console).
2. **Criar app**: nome "Casinha do Saber: Aprender", idioma padrão português (Brasil), tipo **App**, **Gratuito**. Aceite as declarações.
3. Ative a **Assinatura de apps do Google Play** quando for pedido (é o padrão): a Play guarda a chave final e você só usa a chave de upload.

## 4. Política de privacidade (link)

O repositório é público, então o arquivo já abre direto no GitHub. Use este link no Play Console (**Conteúdo do app > Política de privacidade** e na ficha da loja):

https://github.com/DPMG2025/alura_space/blob/claude/educational-kids-android-app-7bdyy3/casinha-do-saber/docs/politica-de-privacidade.md

Cuidados: o link deixa de funcionar se o repositório virar privado, se o branch for apagado ou renomeado, ou se o arquivo mudar de lugar. Depois de juntar o branch no principal, troque `claude/educational-kids-android-app-7bdyy3` pelo nome do branch principal no link.

**Alternativa com GitHub Pages** (endereço mais limpo, página feita para celular): em **Settings > Pages**, escolha **Deploy from a branch**, o branch `claude/educational-kids-android-app-7bdyy3` (ou o principal) e a pasta **/ (root)**, e salve. Em alguns minutos a página fica em:

https://dpmg2025.github.io/alura_space/casinha-do-saber/docs/politica-de-privacidade.html

Abra o link no celular para conferir antes de colar no Play Console. O conteúdo é o mesmo de `politica-de-privacidade.md`; se mudar um, mude o outro.

## 5. Preencher a ficha e as seções obrigatórias

Use os textos e as respostas de `docs/play-store.md`:

1. **Ficha principal da loja**: nome, descrição curta, descrição completa, email de contato e as imagens de `docs/loja/`:
   * Ícone do app: `docs/loja/icone_512.png`.
   * Imagem de destaque: `docs/loja/destaque_1024x500.png`.
   * Capturas do telefone: `docs/loja/loja_celular_01.png` a `loja_celular_08.png`, nessa ordem.
   * Tablet de 7 polegadas: `loja_tablet7_01.png` a `loja_tablet7_04.png`.
   * Tablet de 10 polegadas: `loja_tablet10_01.png` a `loja_tablet10_04.png`.

   Para baixar as imagens do GitHub: abra a pasta `casinha-do-saber/docs/loja` no site, clique na imagem e use o botão de download (ou baixe o repositório inteiro em **Code > Download ZIP**).
2. **Conteúdo do app**: acesso ao app, anúncios, ID de publicidade, classificação do conteúdo, público alvo (5 anos ou menos e 6 a 8 anos, que inscreve o app no programa Famílias), segurança dos dados, apps governamentais, recursos financeiros, saúde e notícias. As respostas estão prontas em `docs/play-store.md`.
3. **Categoria do app e dados de contato**: Educação; email guilhermegatdef@gmail.com.

## 6. Teste interno (opcional, rápido)

Em **Testar e lançar > Teste interno**, crie uma versão, envie o `app-release.aab` e adicione o seu email como testador. Serve para conferir em minutos que o pacote instala pela Play. Não conta para a exigência do teste fechado.

## 7. Teste fechado: 12 testadores por 14 dias (obrigatório para conta pessoal nova)

Contas pessoais criadas depois de novembro de 2023 só liberam a produção depois de um **teste fechado com pelo menos 12 testadores que aceitaram o convite e ficaram inscritos por 14 dias seguidos**. Convide uns 15 a 20, porque sempre alguém esquece de aceitar ou desinstala.

### Montar a lista de testadores

Os testadores precisam de uma Conta Google (a mesma usada na Play Store do celular deles). Duas formas:

* **Lista de emails**: em **Testar e lançar > Teste fechado > Gerenciar faixa (Alpha) > Testadores**, clique em **Criar lista de emails**, dê um nome (por exemplo, "Famílias testadoras") e cole os emails separados por vírgula. Marque a lista e salve.
* **Grupo do Google**: crie um grupo em groups.google.com (por exemplo, `casinhatestadores@googlegroups.com`), permita que qualquer pessoa com o link entre ou adicione os membros, e informe o endereço do grupo na mesma tela de testadores. É mais fácil para quem quer entrar sozinho.

Em seguida, crie a versão do teste fechado, envie o `app-release.aab`, escolha os países (Brasil) e mande para revisão. Depois de aprovada (pode levar alguns dias), copie o **link de participação** que aparece na aba Testadores.

### Mensagem pronta para os testadores

```
Oi! Estou publicando o Casinha do Saber, um app educativo para crianças de 4 a 6 anos (números, letras, inglês e lógica, montando uma casinha). A Google exige um teste de 14 dias antes do lançamento e eu preciso da sua ajuda.

1. No celular Android, abra este link com a mesma Conta Google da Play Store: [LINK DE PARTICIPAÇÃO]
2. Toque no botão para virar testador e depois em "Baixar no Google Play".
3. Instale o app e abra pelo menos uma vez.
4. Deixe o app instalado por 14 dias e, se puder, abra de vez em quando (nem que seja por 2 minutos) ou deixe a criança brincar.
5. Se algo parecer estranho, me conte o que aconteceu, em que tela e o modelo do celular. Dá também para mandar comentário pela própria Play Store.

Obrigado!
```

### O que pedir e acompanhar

* Que **não desinstalem** e **não saiam do programa** durante os 14 dias: quem sai deixa de contar.
* Que abram o app algumas vezes no período. A Google olha se o teste teve uso de verdade e pergunta sobre isso no pedido de produção.
* Uma impressão rápida: a criança entendeu a narração? Conseguiu arrastar os móveis? Em que fase travou? A voz em português apareceu?
* Acompanhe em **Teste fechado > Testadores** quantos aceitaram (o número tem que ficar em 12 ou mais durante os 14 dias seguidos).
* Se mudar algo no app durante o teste, aumente `versionCode` em `app/build.gradle.kts`, deixe o CI gerar o novo `.aab` e envie uma nova versão na mesma faixa. Atualizar a versão não zera a contagem dos 14 dias.

### Pedir o acesso à produção

Depois dos 14 dias, o **Painel** mostra o botão **Solicitar acesso à produção**. O formulário pergunta como foi o teste (quantos testadores, que tipo de retorno veio, o que mudou) e se o app está pronto. Responda com o que aconteceu de verdade. A análise costuma levar até 7 dias.

## 8. Lançar

Em **Produção > Criar nova versão**, envie o `.aab` (com `versionCode` maior que o do teste, se tiver mudado algo), escreva as novidades e envie para revisão. Apps do programa Famílias costumam levar alguns dias na revisão.

## Depois do lançamento

As avaliações aparecem em **Avaliações e comentários**. Responder aos pais e corrigir rápido o que eles apontarem é o que leva a nota para perto de 5.
