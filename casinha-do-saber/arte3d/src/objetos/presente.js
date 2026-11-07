import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';

export function presente(s = 1, aberto = false) {
  const g = new THREE.Group();
  const caixaM = plastico('#ff4f8b', { rugosidade: 0.25, verniz: 1 });
  const fita = plastico('#ffd23f', { rugosidade: 0.2, verniz: 1 });
  g.add(em(caixa(1.6, 1.3, 1.6, 0.14, caixaM), 0, 0.65, 0));
  g.add(em(caixa(0.3, 1.32, 1.62, 0.05, fita), 0, 0.65, 0));
  g.add(em(caixa(1.62, 1.32, 0.3, 0.05, fita), 0, 0.65, 0));
  const tampa = new THREE.Group();
  tampa.add(em(caixa(1.78, 0.36, 1.78, 0.12, caixaM), 0, 0, 0));
  tampa.add(em(caixa(0.32, 0.38, 1.8, 0.05, fita), 0, 0, 0));
  tampa.add(em(caixa(1.8, 0.38, 0.32, 0.05, fita), 0, 0, 0));
  for (const lado of [-1, 1]) {
    const laco = malha(new THREE.TorusGeometry(0.28, 0.1, 16, 32), fita);
    laco.rotation.set(0, 0, lado * 0.6);
    laco.scale.set(1, 0.8, 0.6);
    em(laco, lado * 0.3, 0.42, 0); tampa.add(laco);
  }
  tampa.add(em(bola(0.13, fita, 16), 0, 0.3, 0));
  if (aberto) { em(tampa, 1.25, 2.15, -0.5); tampa.rotation.set(0.3, 0.2, -0.75); } else em(tampa, 0, 1.4, 0);
  g.add(tampa);
  g.userData.tampa = tampa;
  g.scale.setScalar(s);
  return g;
}
