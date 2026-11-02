# Passo a passo para publicar na Google Play

O projeto já gera tudo o que a loja pede. Faltam só as etapas que exigem a sua conta.

## 1. Criar a chave de upload (uma vez só, no seu computador)

Com o Java instalado (vem junto com o Android Studio):

```
keytool -genkeypair -v -keystore casinha-upload.jks -alias casinha -keyalg RSA -keysize 2048 -validity 10000
```

Guarde o arquivo `casinha-upload.jks` e as senhas num lugar seguro (por exemplo, um gerenciador de senhas). **Nunca** coloque esse arquivo no GitHub.

## 2. Cadastrar a chave como segredo do GitHub

No repositório: **Settings > Secrets and variables > Actions > New repository secret**. Crie quatro segredos:

| Nome | Valor |
|---|---|
| `CASINHA_KEYSTORE_BASE64` | o conteúdo do arquivo convertido em texto: `base64 -w0 casinha-upload.jks` (no Windows: `certutil -encode casinha-upload.jks saida.txt` e copie o texto entre as linhas BEGIN e END) |
| `CASINHA_KEYSTORE_PASSWORD` | a senha do arquivo |
| `CASINHA_KEY_ALIAS` | `casinha` |
| `CASINHA_KEY_PASSWORD` | a senha da chave |

No próximo push, o CI gera o artefato **casinha-do-saber-play-store** com o arquivo `app-release.aab` já assinado.

## 3. Criar o app no Play Console

1. Crie a conta de desenvolvedor em play.google.com/console (taxa única de 25 dólares e verificação de identidade).
2. **Criar app**: nome "Casinha do Saber: Aprender", idioma português (Brasil), tipo Jogo ou App (escolha **App**, categoria Educação), gratuito.
3. Ative a **Assinatura de apps do Google Play** quando for pedido (é o padrão).

## 4. Preencher as seções obrigatórias

Use os textos de `docs/play-store.md`:

1. **Ficha da loja**: nome, descrição curta, descrição completa, ícone 512x512, imagem de destaque 1024x500 e pelo menos 2 capturas de tela (use `docs/capturas/` ou `docs/emulador/`).
2. **Público alvo e conteúdo**: idades "5 anos ou menos" e "6 a 8 anos". Isso inscreve o app no programa Famílias; aceite a política.
3. **Classificação do conteúdo**: responda o questionário (sem violência, sem compras, sem interação entre usuários). Resultado esperado: Livre.
4. **Segurança dos dados**: "não coleta nem compartilha dados".
5. **Política de privacidade**: publique `docs/politica-de-privacidade.md` (por exemplo, no GitHub Pages) e informe o link.
6. **Anúncios**: "não contém anúncios".

## 5. Testar antes de lançar

1. Em **Teste interno**, envie o `app-release.aab` e adicione o seu email e o de outros pais como testadores.
2. Instale pelo link do teste num celular de verdade e observe 2 ou 3 crianças jogando por 10 minutos. Veja se entendem a narração, se conseguem arrastar os móveis e em que fase travam.
3. Ajuste o que for preciso (novo push gera novo `.aab`; aumente `versionCode` em `app/build.gradle.kts` a cada envio).
4. Contas pessoais novas precisam de um **teste fechado com pelo menos 12 testadores por 14 dias** antes de liberar a produção.

## 6. Lançar

Em **Produção > Criar nova versão**, envie o `.aab`, escreva as novidades e envie para revisão. A revisão de apps para crianças costuma levar alguns dias.

## Depois do lançamento

As avaliações da loja aparecem em **Avaliações e comentários**. Responder aos pais e corrigir rápido o que eles apontarem é o que leva a nota para perto de 5.
