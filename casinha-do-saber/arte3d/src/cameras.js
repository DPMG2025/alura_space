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
    enquadrar: 'fixo',
    fov: 28,
    posicao: [0, 2.0, 13.5],
    alvo: [0, 1.75, 0],
    luzes: { sol: [4, 10, 9], forca: 2.6, area: 5, amb: 0.8 },
    sombraDeContato: true,
  },
};

/** Ajuste final de cor do protótipo (ui2.css: canvas.cena). */
export const filtroFinal = 'saturate(1.22) contrast(1.04)';
