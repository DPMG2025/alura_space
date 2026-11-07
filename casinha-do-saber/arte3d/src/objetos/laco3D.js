import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';

export function laco3D(R, cor, x, y, z) {
  const g = new THREE.Group();
  const m = plastico(cor, { rugosidade: 0.25, verniz: 1 });
  for (const lado of [-1, 1]) {
    const asa = malha(new THREE.ConeGeometry(R * 0.2, R * 0.38, 24), m);
    asa.rotation.z = lado * Math.PI / 2;
    em(asa, lado * R * 0.17, 0, 0);
    g.add(asa);
  }
  g.add(bola(R * 0.09, m, 16));
  em(g, x, y, z);
  g.rotation.z = -0.3;
  return g;
}
