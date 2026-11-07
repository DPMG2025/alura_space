import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';

export function arvore({ copa = '#5cbf4a', s = 1 } = {}) {
  const g = new THREE.Group();
  const tronco = cilindro(0.14, 0.22, 1.2, massinha('#9a6a44')); em(tronco, 0, 0.6, 0); g.add(tronco);
  const m = plastico(copa, { rugosidade: 0.6, verniz: 0.1 });
  for (const [x, y, z, r] of [[0, 1.6, 0, 0.75], [-0.5, 1.35, 0.1, 0.5], [0.5, 1.4, 0.05, 0.55], [0.1, 2.05, -0.05, 0.5], [0.05, 1.45, 0.45, 0.45]]) {
    g.add(em(bola(r, m, 32), x, y, z));
  }
  for (const [x, y, z] of [[0.35, 1.7, 0.55], [-0.45, 1.5, 0.5], [0.6, 1.25, 0.4]]) g.add(em(bola(0.09, plastico('#ff4d4d', { rugosidade: 0.2, verniz: 1 }), 16), x, y, z));
  g.scale.setScalar(s);
  return g;
}
