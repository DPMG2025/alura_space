import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';

export function flor(cor, s = 1) {
  const g = new THREE.Group();
  const caule = cilindro(0.02, 0.02, 0.3, massinha('#3f9a3a'), 8); em(caule, 0, 0.15, 0); g.add(caule);
  const m = plastico(cor, { rugosidade: 0.35, verniz: 0.6 });
  for (let k = 0; k < 5; k++) {
    const a = k * Math.PI * 2 / 5;
    const p = bola(0.07, m, 12); p.scale.set(1, 0.5, 1);
    em(p, Math.cos(a) * 0.08, 0.32, Math.sin(a) * 0.08); g.add(p);
  }
  g.add(em(bola(0.055, plastico('#ffe066', { rugosidade: 0.3 }), 12), 0, 0.34, 0));
  g.scale.setScalar(s);
  return g;
}
