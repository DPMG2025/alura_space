# FIPE pela Placa (Android)

Aplicativo Android (Kotlin + Jetpack Compose) que consulta o valor de mercado de veículos pela tabela FIPE.

## Funcionalidades

1. **Busca pela placa**: digite a placa (antiga `ABC1234` ou Mercosul `ABC1D23`). O app identifica marca, modelo, ano, cor e cidade, encontra o código FIPE e mostra o valor de mercado. Quando a placa corresponde a mais de uma versão na FIPE, todas aparecem, ordenadas pela compatibilidade.
2. **Busca por marca, modelo e ano**: todas as marcas da tabela FIPE para carros, motos e caminhões, com filtro por texto em cada etapa.
3. **Histórico do preço**: evolução do valor FIPE no ano atual, nos últimos 12 meses ou nos últimos 24 meses, com gráfico, variação percentual, diferença em reais e tabela mês a mês. Funciona tanto na busca pela placa quanto na busca manual.

## De onde vêm os dados

| Dado | Fonte | Token |
|---|---|---|
| Marcas, modelos, anos e valor atual | API FIPE pública v1 (`parallelum.com.br/fipe/api/v1`) | Não precisa |
| Valor por código FIPE e histórico mensal | API FIPE v2 (`fipe.parallelum.com.br/api/v2`) | Opcional (sem token: cerca de 500 consultas por dia) |
| Placa para marca, modelo e código FIPE | API Placas (`apiplacas.com.br`), configurável | Obrigatório |

A placa não faz parte da tabela FIPE: a ligação placa e veículo só existe em bases de consulta veicular, que são pagas. Por isso a busca pela placa exige o token de um desses serviços. O padrão é a API Placas (URL `https://wdapi2.com.br/consulta/{placa}/{token}`), mas em **Configurações** dá para trocar a URL por outro provedor com resposta parecida; o leitor da resposta aceita variações de nome dos campos.

Cada mês do histórico é uma consulta à API v2. Um histórico de 24 meses gasta 24 consultas; os resultados ficam em cache enquanto o app está aberto. Para uso intenso, gere um token gratuito em fipe.online e informe em Configurações.

## Como obter o APK

O workflow `.github/workflows/fipe-android.yml` compila o app a cada push que altera `fipe-app/`. Abra a aba **Actions** do repositório, entre na execução mais recente de "App Android FIPE" e baixe o artefato `fipe-pela-placa-apk`. Instale o `app-release.apk` no celular (é preciso permitir instalação de fontes desconhecidas).

Para embutir os tokens no APK, cadastre os segredos `PLACA_API_TOKEN` e `FIPE_API_TOKEN` em Settings, Secrets and variables, Actions. Sem eles, basta informar os tokens na tela de Configurações do app.

## Compilar localmente

Requisitos: Android Studio (ou JDK 17 e Android SDK 35).

```bash
cd fipe-app
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # testes unitários
```

Os tokens também podem ir no arquivo `fipe-app/local.properties` (não versionado):

```
PLACA_API_TOKEN=seu_token
FIPE_API_TOKEN=seu_token_opcional
```

## Estrutura

```
app/src/main/java/br/com/fipeplaca/
  data/   APIs (Retrofit), modelos, leitor da resposta de placa, repositório, formatos
  ui/     telas em Compose (Placa, Marca e modelo, Configurações), ViewModels, gráfico
```
