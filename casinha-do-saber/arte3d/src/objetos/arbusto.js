import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';

export function arbusto(cor = '#4fae3e', s = 1) {
  const g = new THREE.Group();
  const m = plastico(cor, { rugosidade: 0.6, verniz: 0.1 });
  for (const [x, y, z, r] of [[0, 0.3, 0, 0.42], [-0.38, 0.22, 0.05, 0.3], [0.38, 0.22, 0, 0.32]]) g.add(em(bola(r, m, 24), x, y, z));
  g.scale.setScalar(s);
  return g;
}
