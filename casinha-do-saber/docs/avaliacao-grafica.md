# Avaliação gráfica da Casinha do Saber

Avaliação feita em 6 de novembro de 2026, a pedido do dono do produto: a filha não gostou do jogo e a suspeita é o visual. O pedido era ver como melhorar os gráficos (mesmo que fosse preciso reformular o jogo inteiro), deixar tudo mais atraente para crianças e colocar um irmãozinho no jogo.

## Resumo

O jogo não precisa ser refeito do zero. O conteúdo (1000 fases, quatro caminhos, a casa que cresce, as vozes, os livrinhos) é sólido e foi testado a fundo. O que pesa para uma criança de 4 a 6 anos está na camada visual e no afeto: a personagem tinha um rosto quase sem expressão, as telas de jogo pareciam uma ficha de escola (fundo de uma cor só e um cartão branco) e a criança jogava sozinha. Esta rodada ataca esses três pontos. O restante vai como lista de prioridades no fim.

## O que já estava bom

* A **casa por dentro** (vista isométrica, captura 32) é a tela mais bonita do app: estilo único, móveis desenhados à mão, cores quentes. É a referência de qualidade para o resto.
* **Coerência de sistema**: botões, cartões, balões e sombras seguem as mesmas regras em todas as telas.
* **Acessível para quem não lê**: tudo narrado, botões grandes, erro sem punição.

## O que pesava para a criança

1. **Rosto sem vida.** Olhos de pontinho, boca de um traço, sem sobrancelhas, sem bochecha marcada e a mesma cara o tempo todo. Criança pequena se liga em rostos: é pelo rosto que ela sente que o personagem está feliz com ela. O humor "torcendo" só levantava os braços; a cara continuava igual.
2. **Telas de jogo com cara de exercício.** Fundo liso na cor do caminho, um cartão branco no meio e quatro cartões brancos embaixo (capturas 05 e 24). Não há lugar, não há mundo. A personagem fica pequena no canto, como um ícone.
3. **Jogar sozinha.** A personagem não tem ninguém com quem comemorar. Um companheiro que torce, se espanta e ri dá à criança alguém para "mostrar" o acerto, que é uma das maiores motivações nessa idade.
4. **Mistura de estilos.** Os emojis da fonte Noto (brilhantes, com volume) ficam ao lado de desenhos chapados feitos em código. Nos botões da barra (⚙️ 🔊 ⬅️ ✖️) os emojis de sistema dão cara de aplicativo de adulto.
5. **Cores claras demais.** Muitos tons pastel lavados (o laranja do jogo, o lilás do prêmio). Crianças dessa idade preferem cores vivas, contraste forte e contornos bem marcados.
6. **Números abstratos na tela inicial.** "3/250" e barras finas de progresso não dizem nada a quem ainda não lê nem conta até 250.

## O que foi feito nesta rodada

### 1. Rosto novo das crianças (`ui/componentes/RostoDaCrianca.kt`)

* Olhos grandes, escuros embaixo e mais claros em cima, com um brilho grande e um pequeno (o "olho de desenho animado" que as crianças reconhecem na hora).
* Pálpebra marcada e um cílio curvinho no canto de fora (menina).
* Sobrancelhas que mudam com o humor: levantam na surpresa, uma sobe quando está pensando.
* Boca aberta de riso com linguinha e dentinhos; na comemoração, olhinhos fechados de alegria (^ ^) e bocão de "eba!"; pensando, boquinha de lado e olhar para cima; falando, um "o".
* Narizinho, bochechas rosadas que se desfazem na pele e luz e sombra na cabeça.
* Franja com mechas redondinhas em vez de uma borda reta.
* Os olhos continuam no mesmo lugar e a cabeça tem o mesmo tamanho: óculos, máscara, tapa olho e chapéus do guarda roupa encaixam como antes (o teste dos chapéus na cabeça continua valendo).

### 2. O irmãozinho (`dominio/Irmao.kt`, `ui/componentes/Irmaozinho.kt`)

* Criança pequena, cabeçuda, de jardineira, com **a mesma pele e o mesmo cabelo** da criança do jogo (são da mesma família). Irmãozinho de blusa amarela e jardineira verde água com estrela; irmãzinha de blusa lilás, jardineira coral com coração e rabinho no alto da cabeça com laço.
* Usa os mesmos rostos da criança e tem vida própria: balança de um lado para o outro, pisca num ritmo diferente (os dois nunca piscam juntos) e dá tchau.
* **Onde aparece:**
  * **Jogo:** ao lado da criança. No acerto, torce com os bracinhos para cima e pula um tiquinho depois dela; no erro, fica pensativo junto.
  * **Prêmio:** comemora ao lado da criança (e do mascote, quando há; os três encolhem um pouco para caber).
  * **"Quase!":** dá tchau, animando para tentar de novo.
  * **Casa e tela inicial:** fica sempre ao lado da criança e vai junto quando ela muda de lugar. Tocar nele faz ele pular e falar ("Oi, Ana! Vamos brincar?", "Hi hi hi! Faz cosquinha!", "Você é muito esperta!"...). As falas giram sem sorteio e respeitam se a criança é menino ou menina.
* **Área dos pais**, cartão "Irmãozinho no jogo": irmãozinho, irmãzinha ou sem irmão, e o nome (pode ser o do irmão de verdade). Vem ligado para todo mundo, inclusive saves antigos, sem nome ("o irmãozinho").
* Guardado na chave `irmao` do perfil (`1|MENINO|Theo`), conhecida pela cópia do progresso. Texto estragado vira o padrão. Testes em `IrmaoTest.kt`.

### 3. Cenário nas telas de jogo (`ui/componentes/CenarioDoJogo.kt`)

* No lugar do fundo de uma cor só, um lugar ao ar livre: céu na cor do caminho (laranja vira fim de tarde, roxo vira céu lilás), sol com halo, nuvens fofas, dois morros, arbustos com frutinhas, florzinhas na cor do caminho e, lá no morro, a casinha de telhado vermelho (o caminho de volta para casa).
* Tudo parado (nada se mexe atrás das atividades) e desenhado uma vez por tamanho.

### Capturas novas

`CapturasIrmaoTest.kt` gera: `70_rostos_e_irmaos` (todas as crianças e irmãos em cada humor), `71_jogo_com_irmao`, `72_jogo_letras_com_irma`, `73_premio_com_irmao`, `74_casa_com_irmao`, `75_pais_irmaozinho` e `75b_pais_irmazinha`. As capturas antigas (jogo, prêmio, casa) são refeitas pelo CI já com o visual novo.

## Próximos passos recomendados (por ordem de impacto)

1. **Testar com a filha antes de mexer em mais coisa.** Dez minutos de observação valem mais que qualquer régua: em que tela ela sorri, em que tela ela larga o celular, se ela toca no irmãozinho, se ela quer dar o nome do irmão de verdade. Anotar e decidir o próximo passo por isso.
2. **Tela inicial sem números.** Trocar "3/250" e as barrinhas por algo visual: a trilha de pegadas até a próxima recompensa, ou um potinho que enche de estrelas. Os quatro botões dos caminhos podem ganhar uma ilustração própria (um bichinho contando, um livrinho, um balão com "hello", um quebra cabeça) desenhada no traço da casa por dentro.
3. **Ícones desenhados nos botões da barra.** Fechar, voltar, som e ajustes desenhados em código, no mesmo traço da casa, em vez de emoji do sistema.
4. **Corpo com mais vida.** A cabeça já tem expressão; o corpo ainda é rígido. Pulo com "amassar e esticar" no acerto, os dois irmãos dando as mãos no prêmio, dancinha na vitória de 3 estrelas.
5. **Cores mais vivas no prêmio e nos cartões.** Trocar o lilás lavado do prêmio por um céu mais saturado (no estilo do cenário novo) e dar contorno mais escuro aos cartões de resposta.
6. **Figuras das perguntas.** Os emojis funcionam, mas destoam do resto. Um caminho gradual: desenhar em código os 20 ou 30 objetos que mais aparecem nas fases iniciais (frutas, bichos, brinquedos), no traço da casa por dentro, e manter o emoji para o resto.
7. **O irmãozinho em mais lugares.** Dentro da casa (andando atrás da criança), no caminho das fases e na escolha do personagem.

## Como conferir

* As capturas ficam em `docs/capturas/` (o CI refaz a cada push).
* Para ver no celular: baixar o APK do artefato `casinha-do-saber-apk` na aba Actions do GitHub, na execução mais recente do branch.
