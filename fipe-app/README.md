# FIPE pela Placa (Android)

Aplicativo Android (Kotlin + Jetpack Compose) que consulta o valor de mercado de veículos pela tabela FIPE.

## Telas

| Busca pela placa | Resultado | Modo escuro |
|---|---|---|
| ![Placa](docs/capturas/01_placa_inicial.png) | ![Resultado](docs/capturas/03_placa_resultado.png) | ![Escuro](docs/capturas/04_placa_resultado_escuro.png) |

| Tabela FIPE | Valor e histórico | Salvos | Ajustes |
|---|---|---|---|
| ![Marcas](docs/capturas/06_tabela_marcas.png) | ![Tabela](docs/capturas/08_tabela_resultado.png) | ![Salvos](docs/capturas/09_salvos.png) | ![Ajustes](docs/capturas/11_ajustes.png) |

As capturas são geradas automaticamente pelo CI a cada alteração (Robolectric + Roborazzi), então sempre mostram a versão atual.

## Funcionalidades

1. **Busca pela placa**: campo no formato de placa Mercosul, aceita placa antiga (`ABC1234`) e Mercosul (`ABC1D23`). Mostra marca, modelo, ano, cor, cidade e situação, encontra o código FIPE e o valor de mercado. Quando a placa corresponde a mais de uma versão na FIPE, todas aparecem com o grau de compatibilidade para o usuário escolher.
2. **Tabela FIPE completa**: todas as marcas, modelos e anos de carros, motos e caminhões, com busca por texto (vários termos, sem acento), marcas populares em destaque, indicador de etapas clicável e destaque para veículos zero km.
3. **Histórico do preço**: evolução no ano atual, em 12 ou em 24 meses, com gráfico interativo (toque ou arraste para ver cada mês), variação percentual, diferença em reais, máximo, mínimo, média, variação mensal média, tabela mês a mês e projeção para 12 meses pela tendência.
4. **Salvos**: favoritos e consultas recentes guardados no aparelho. Reabrir uma placa salva não gasta crédito do serviço de placas.
5. **Compartilhar e copiar**: envia o resultado (com a variação do período) por WhatsApp, e-mail etc.; toque no código FIPE para copiar.
6. **Acabamento**: tema claro e escuro (ou do sistema), tela de abertura, animações, esqueleto de carregamento, mensagens de erro claras, retorno tátil no gráfico e descrições de acessibilidade.

## Qualidade

1. Testes de unidade: validação de placa, formatos, leitor da resposta de placa, estatísticas, projeção, datas e regras dos salvos.
2. Testes de integração com servidor HTTP simulado (MockWebServer): busca manual completa, placa até o valor FIPE, histórico ignorando meses ausentes, uso do token e mensagens de erro (401, 429, sem conexão).
3. Testes de captura de tela de todas as telas, em tema claro e escuro.
4. CI no GitHub Actions: roda os testes, publica as capturas e gera os APKs debug e release (com R8).

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
