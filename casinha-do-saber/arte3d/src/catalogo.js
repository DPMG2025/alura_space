// Lista do que o pipeline renderiza. Cada entrada vira <id>.webp em app/src/main/assets/arte/
// e uma linha no manifesto.json.
//
//   id     nome do arquivo e chave no manifesto (minúsculas, sem acento)
//   tipo   câmera e luz (ver cameras.js)
//   uso    maior tamanho em que a imagem aparece no app, em dp; o render sai no dobro (ESCALA em render.js)
//   montar devolve o THREE.Object3D; âncoras são marcadas no modelo com ancora() (kit/formas.js)
import { estrelaDourada } from './objetos/estrela3D.js';
import { moedaDoPlacar } from './objetos/moeda.js';
import { patinhoDaLagoa } from './objetos/patinho.js';

export const catalogo = [
  { id: 'estrela3d', tipo: 'item', uso: { largura: 120, altura: 120 }, montar: estrelaDourada },
  { id: 'moeda', tipo: 'item', uso: { largura: 64, altura: 64 }, montar: moedaDoPlacar },
  { id: 'patinho', tipo: 'objeto', uso: { largura: 120, altura: 120 }, montar: patinhoDaLagoa },
];
