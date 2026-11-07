import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';

export function nuvem(s = 1) {
  const g = new THREE.Group();
  const m = plastico('#ffffff', { rugosidade: 0.85, verniz: 0, emissivo: '#dfefff' });
  for (const [x, y, r] of [[0, 0, 0.7], [-0.75, -0.15, 0.5], [0.75, -0.12, 0.55], [0.3, 0.3, 0.5]]) {
    const b = bola(r, m, 24); b.castShadow = false; b.receiveShadow = false; g.add(em(b, x, y, 0));
  }
  g.scale.set(s, s * 0.8, s * 0.6);
  return g;
}
