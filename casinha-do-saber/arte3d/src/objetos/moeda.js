import { THREE, plastico, malha, cilindro, em, ancora } from '../kit.js';
import { estrela3D } from './estrela3D.js';

/** Moeda dourada com aro saltado e estrela em relevo, de frente para a câmera (+z). */
export function moeda(s = 1) {
  const g = new THREE.Group();
  const ouro = plastico('#ffc933', { rugosidade: 0.2, verniz: 1 });
  const c = cilindro(0.5, 0.5, 0.14, ouro, 64); c.rotation.x = Math.PI / 2; g.add(c);
  // aro arredondado: dá o brilho de borda que separa a moeda do fundo
  for (const z of [-0.07, 0.07]) {
    const aro = malha(new THREE.TorusGeometry(0.47, 0.045, 16, 64), plastico('#ffb81a', { rugosidade: 0.18, verniz: 1 }));
    g.add(em(aro, 0, 0, z));
  }
  const e = estrela3D(0.25, 0.04, plastico('#ffe58a', { rugosidade: 0.15, verniz: 1 }), 5, { segmentos: 6 }); em(e, 0, 0, 0.08); g.add(e);
  ancora(g, 'centro', 0, 0, 0);
  g.scale.setScalar(s);
  return g;
}

/** A moeda do placar, girada um pouco para mostrar a espessura. */
export function moedaDoPlacar() {
  const g = new THREE.Group();
  const m = moeda(1);
  m.rotation.set(0.12, -0.38, 0);
  g.add(m);
  return g;
}
