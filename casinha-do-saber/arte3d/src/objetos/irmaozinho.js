import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';
import { estrela3D } from './estrela3D.js';

export function irmaozinho({ corPele = '#b8743d', corCabelo = '#1c1412', humor = 'feliz', bracos = 'parado' } = {}) {
  const g = new THREE.Group();
  const mPele = pele(corPele);
  const mCabelo = plastico(corCabelo, { rugosidade: 0.45, verniz: 0.5 });
  const blusa = plastico('#ffd23f', { rugosidade: 0.55, verniz: 0.2 });
  const jardineira = plastico('#22b8a8', { rugosidade: 0.6, verniz: 0.15 });
  for (const lado of [-1, 1]) {
    const perna = capsula(0.11, 0.18, mPele); em(perna, lado * 0.16, 0.3, 0); g.add(perna);
    const sapato = bola(1, plastico('#e63946', { rugosidade: 0.2, verniz: 1 })); sapato.scale.set(0.16, 0.11, 0.22); em(sapato, lado * 0.17, 0.11, 0.05); g.add(sapato);
  }
  const corpo = caixa(0.78, 0.72, 0.6, 0.28, blusa); em(corpo, 0, 0.82, 0); g.add(corpo);
  const calca = caixa(0.8, 0.42, 0.62, 0.2, jardineira); em(calca, 0, 0.6, 0); g.add(calca);
  const peito = caixa(0.46, 0.34, 0.08, 0.06, jardineira); em(peito, 0, 0.9, 0.29); g.add(peito);
  for (const lado of [-1, 1]) {
    const alca = caixa(0.09, 0.42, 0.06, 0.03, jardineira); em(alca, lado * 0.2, 1.0, 0.3); alca.rotation.z = lado * 0.15; g.add(alca);
    const botao = bola(0.04, plastico('#fff3b0', { rugosidade: 0.2, verniz: 1 }), 12); em(botao, lado * 0.17, 1.02, 0.35); g.add(botao);
  }
  const est = estrela3D(0.1, 0.04, plastico('#fff3b0', { rugosidade: 0.3, verniz: 1 })); em(est, 0, 0.88, 0.34); g.add(est);
  const angs = bracos === 'torcendo' ? [2.6, -2.6] : [0.45, -0.45];
  for (const [i, lado] of [[0, -1], [1, 1]]) {
    const braco = new THREE.Group(); em(braco, lado * 0.42, 1.06, 0);
    braco.rotation.z = -angs[i];
    const b = capsula(0.1, 0.24, mPele); em(b, 0, -0.2, 0); braco.add(b);
    const mao = bola(0.13, mPele, 20); em(mao, 0, -0.38, 0); braco.add(mao);
    const manga = bola(0.16, blusa, 20); em(manga, 0, -0.03, 0); braco.add(manga);
    g.add(braco);
  }
  const R = 0.68;
  const cabeca = new THREE.Group(); em(cabeca, 0, 1.72, 0);
  cabeca.userData.matPele = mPele;
  cabeca.add(bola(R, mPele, 64));
  for (const lado of [-1, 1]) { const o = bola(R * 0.2, mPele, 20); em(o, lado * R * 0.98, -R * 0.05, 0); cabeca.add(o); }
  const calota = malha(new THREE.SphereGeometry(R * 1.04, 64, 32, 0, Math.PI * 2, 0, Math.PI * 0.42), mCabelo);
  calota.rotation.x = -0.5; cabeca.add(calota);
  // cachinho no alto
  const cacho = malha(new THREE.TorusGeometry(R * 0.16, R * 0.07, 12, 24, Math.PI * 1.6), mCabelo);
  em(cacho, R * 0.05, R * 1.08, R * 0.1); cacho.rotation.y = 0.4; cabeca.add(cacho);
  rosto(cabeca, R, { humor, menina: false });
  ancora(cabeca, 'cabeca', 0, 0, 0);
  g.add(cabeca);
  return g;
}
