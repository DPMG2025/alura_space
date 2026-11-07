// Câmeras fixas por tipo de imagem. Todo objeto do mesmo tipo sai com o mesmo ângulo e a mesma luz,
// para as imagens combinarem entre si no app.
//
// enquadrar:
//   'ajustar' - a direção e a lente são fixas; a imagem é recortada (setViewOffset) em volta do
//               objeto, com [margem], sem distorcer a perspectiva. Para objetos soltos.
//   'fixo'    - posição, alvo e lente fixos, sem recorte: camadas do mesmo tipo (personagens) ficam
//               alinhadas pixel a pixel (PRD, seção 3.2).
export const tiposDeCamera = {
  objeto: {
    // coisas que ficam no chão (bichos, brinquedos): com sombra de contato
    enquadrar: 'ajustar',
    fov: 30,
    direcao: [0.0, 0.42, 1.0], // de frente, um pouco de cima
    margem: 1.06,
    luzes: { sol: [3, 6, 5], forca: 2.4, area: 3, amb: 0.9 },
    sombraDeContato: true,
  },
  item: {
    // coisas que flutuam (estrelas, moedas, prêmios): de frente, sem sombra no chão
    enquadrar: 'ajustar',
    fov: 30,
    direcao: [0.0, 0.18, 1.0],
    margem: 1.05,
    luzes: { sol: [3, 6, 5], forca: 2.4, area: 3, amb: 0.9 },
    sombraDeContato: false,
  },
  icone: {
    enquadrar: 'ajustar',
    fov: 30,
    direcao: [0.29, 0.32, 1.0], // três quartos, como os ícones dos caminhos no protótipo
    margem: 1.06,
    luzes: { sol: [3, 6, 5], forca: 2.4, area: 3, amb: 0.9 },
    sombraDeContato: false,
  },
  personagem: {
    // criança e irmãozinho em camadas (PRD, seção 3.2): posição, alvo e lente fixos, sem recorte em volta
    // do objeto, para todas as camadas ficarem alinhadas pixel a pixel. [quadro] é o quadro inteiro em dp
    // (o da criança); uma camada com uso menor (o irmão) é um recorte do mesmo quadro, centrado e alinhado
    // embaixo, na mesma escala. A luz é fixa (não depende do que está em cada camada).
    enquadrar: 'fixo',
    fov: 26,
    posicao: [0, 2.7, 9.6],
    alvo: [0, 1.8, 0],
    quadro: { largura: 170, altura: 260 },
    centro: [0, 1.7, 0],
    luzes: { sol: [4, 10, 9], forca: 2.6, area: 3.2, amb: 0.8 },
    sombraDeContato: false,
  },
  casa: {
    // Diorama da casa de fora (Fase 3): câmera fixa que olha na horizontal, de um pouco acima da casa, com
    // deslocamento de lente (setViewOffset), então as verticais ficam retas e o plano z = 0 (a frente da
    // casa) cai exatamente nas frações da cena do app (CenaDaCasa: x da largura, y da altura, altura =
    // 1,15 x largura). Todas as camadas da casa saem com o mesmo quadro e a mesma luz: o app só empilha.
    // Camadas mais altas que o quadro (o chão, que continua no jardim de baixo) estendem o quadro para baixo.
    // Medidas do mundo em src/objetos/cenaDaCasa.js (CENA).
    enquadrar: 'cena',
    fov: 40,
    luzes: { sol: [3.5, 12, 10], forca: 2.4, area: 12, amb: 1.0 },
    centro: [0, 2.6, -1.2],
    sombraDeContato: false,
  },
  casaItem: {
    // Itens da casa (os 40 prêmios) soltos, no mesmo ângulo da câmera 'casa' (de frente, um pouco de cima).
    // Sombra de contato por item (entrada do catálogo): só no que pisa no chão.
    enquadrar: 'ajustar',
    fov: 30,
    direcao: [0.0, 0.3, 1.0],
    margem: 1.05,
    luzes: { sol: [3, 6, 5], forca: 2.4, area: 3, amb: 0.9 },
    sombraDeContato: false,
  },
  jogo: {
    // Cenário do jogo (Fase 4): a ilha de cada caminho e as cenas das perguntas (lagoa, mesa, varal...),
    // como na maquete 2_jogo. Câmera e luz do protótipo (jogo.html: de (0; 15,5; 23,5) olhando para
    // (0; -2,2; -1), 34 graus na tela de 393 x 852 dp). [quadro] é essa tela inteira, na largura de 400 dp;
    // cada camada é o recorte [recorte] dela (de y = 100 dp para baixo), o mesmo para todas, então o app
    // só empilha. A ilha passa das bordas dos lados de propósito (cortaNaBorda).
    enquadrar: 'fixo',
    fov: 34,
    posicao: [0, 15.5, 23.5],
    alvo: [0, -2.2, -1.0],
    quadro: { largura: 400, altura: 867 },
    recorte: { x: 0, y: 100 },
    centro: [0, 0, 0],
    luzes: { sol: [5, 12, 8], forca: 2.9, area: 9, amb: 0.6 },
    sombraDeContato: false,
    cortaNaBorda: true,
  },
  isometrico: {
    // Casa por dentro (Fase 6): câmera ORTOGRÁFICA com a mesma projeção de ui/componentes/Isometrico.kt
    // (Projecao): isométrica 2:1, azimute 45 graus e elevação 30 graus (sen 30 = 1/2, o losango do ladrilho
    // tem metade da altura da largura). Mundo em ladrilhos: x do mundo = x da planta, z do mundo = y da
    // planta (do fundo para a frente) e y do mundo = altura z da planta. Na tela, um ladrilho tem
    // [ladrilho] dp de largura (meioL = ladrilho / 2, meioA = ladrilho / 4) e uma unidade de altura mede
    // unidadeZ = 0,56 x ladrilho, como no app. A câmera pura daria cos 30 / raiz 2 = 0,6124 x ladrilho por
    // unidade de altura; [escalaAltura] = 0,56 / 0,6124 achata o mundo em y para bater com o app.
    // Recorte justo em volta do objeto, em pixels inteiros a partir do ponto de referência (o apoio), então
    // a grade do piso cai no mesmo pixel em todas as imagens.
    enquadrar: 'isometrico',
    ladrilho: 64, // dp por ladrilho na imagem (128 px no dobro); o app escala por tw / (ladrilho x densidade)
    unidadeZ: 0.56,
    margemPx: 6, // folga transparente em volta, em pixels finais
    fov: 30, // só para o palco; a câmera usada é ortográfica
    luzes: { sol: [3, 5.5, 7.5], forca: 1.8, area: 6, amb: 0.55 },
    sombraDeContato: false,
  },
};

/** Ângulos e escala da câmera 'isometrico' (Isometrico.kt): azimute 45, elevação 30. */
export const ISO = {
  azimute: Math.PI / 4,
  elevacao: Math.PI / 6,
  get escalaAltura() { return tiposDeCamera.isometrico.unidadeZ / (Math.cos(this.elevacao) / Math.SQRT2); },
};

/** Ajuste final de cor do protótipo (ui2.css: canvas.cena). */
export const filtroFinal = 'saturate(1.22) contrast(1.04)';
