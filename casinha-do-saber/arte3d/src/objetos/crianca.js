import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { laco3D } from './laco3D.js';

export function crianca({ corPele = '#b8743d', corCabelo = '#1c1412', corRoupa = '#ff5c97', menina = true, humor = 'feliz', bracos = 'tchau' } = {}) {
  const g = new THREE.Group();
  const mPele = pele(corPele);
  const mCabelo = plastico(corCabelo, { rugosidade: 0.45, verniz: 0.5 });
  const mRoupa = plastico(corRoupa, { rugosidade: 0.55, verniz: 0.25 });
  // pernas e sapatos
  for (const lado of [-1, 1]) {
    const perna = capsula(0.13, 0.42, menina ? mPele : plastico('#2f4f8f', { rugosidade: 0.7, verniz: 0 }));
    em(perna, lado * 0.2, 0.42, 0);
    g.add(perna);
    if (menina) { const meia = cilindro(0.142, 0.142, 0.22, plastico('#ffffff', { rugosidade: 0.8, verniz: 0 })); em(meia, lado * 0.2, 0.3, 0); g.add(meia); }
    const sapato = bola(1, plastico(menina ? '#d6336c' : '#e04848', { rugosidade: 0.2, verniz: 1 }));
    sapato.scale.set(0.2, 0.13, 0.28);
    em(sapato, lado * 0.21, 0.13, 0.06);
    g.add(sapato);
  }
  // corpo
  let corpo;
  if (menina) {
    const pts = [[0.0, 0.6], [0.7, 0.62], [0.74, 0.7], [0.62, 1.05], [0.46, 1.42], [0.42, 1.62], [0.3, 1.72], [0.0, 1.74]].map(([x, y]) => new THREE.Vector2(x, y));
    corpo = malha(new THREE.LatheGeometry(pts, 48), mRoupa);
    g.add(corpo);
    // bolinhas brancas no vestido
    for (const [a, y] of [[-0.5, 0.95], [0, 1.0], [0.5, 0.95], [-0.25, 1.25], [0.25, 1.25]]) {
      const raio = 0.62 - (y - 0.7) * 0.42;
      const p = bola(0.065, plastico('#ffffff', { rugosidade: 0.4 }), 16);
      p.castShadow = false;
      p.scale.set(1, 1, 0.4);
      em(p, Math.sin(a) * raio, y, Math.cos(a) * raio);
      p.lookAt(Math.sin(a) * 5, y, Math.cos(a) * 5);
      g.add(p);
    }
  } else {
    corpo = caixa(0.95, 0.85, 0.66, 0.3, mRoupa);
    em(corpo, 0, 1.25, 0);
    g.add(corpo);
    const short = caixa(0.9, 0.36, 0.62, 0.16, plastico('#2f4f8f', { rugosidade: 0.7, verniz: 0 }));
    em(short, 0, 0.78, 0);
    g.add(short);
    const estrela = estrela3D(0.17, 0.06, plastico('#ffd23f', { rugosidade: 0.25, verniz: 1 }));
    em(estrela, 0, 1.27, 0.34);
    g.add(estrela);
  }
  // braços
  const ombroY = 1.55;
  const poses = {
    tchau: [[-0.35, -0.25], [2.5, 0.2]],
    torcendo: [[-2.5, 0.2], [2.5, 0.2]],
    parado: [[-0.35, -0.2], [0.35, -0.2]],
  }[bracos];
  for (const [i, lado] of [[0, -1], [1, 1]]) {
    const ang = poses[i][0];
    const braco = new THREE.Group();
    em(braco, lado * 0.5, ombroY, 0);
    braco.rotation.z = ang;
    braco.rotation.x = poses[i][1];
    const b = capsula(0.11, 0.42, mPele);
    em(b, 0, -0.3, 0);
    braco.add(b);
    const mao = bola(0.15, mPele, 24);
    em(mao, 0, -0.56, 0);
    braco.add(mao);
    const manga = bola(0.2, mRoupa, 24);
    manga.scale.set(1, 0.9, 1);
    em(manga, 0, -0.04, 0);
    braco.add(manga);
    g.add(braco);
  }
  // cabeça
  const R = 0.78;
  const cabeca = new THREE.Group();
  em(cabeca, 0, 2.38, 0);
  cabeca.userData.matPele = mPele;
  cabeca.add(bola(R, mPele, 64));
  for (const lado of [-1, 1]) { const o = bola(R * 0.2, mPele, 20); em(o, lado * R * 0.98, -R * 0.05, 0); cabeca.add(o); }
  // cabelo: calota inclinada para trás + franja de mechas
  const calota = malha(new THREE.SphereGeometry(R * 1.07, 64, 32, 0, Math.PI * 2, 0, Math.PI * 0.53), mCabelo);
  calota.rotation.x = -0.42;
  cabeca.add(calota);
  const nuca = bola(R * 1.05, mCabelo, 48);
  em(nuca, 0, -R * 0.02, -R * 0.12);
  cabeca.add(nuca);
  for (let k = -2; k <= 2; k++) {
    const mecha = bola(R * 0.26, mCabelo, 24);
    mecha.scale.set(1.25, 0.95, 0.7);
    naSuperficie(mecha, R, k * R * 0.27, R * (0.62 - Math.abs(k) * 0.06), -R * 0.05);
    cabeca.add(mecha);
  }
  if (menina) {
    for (const lado of [-1, 1]) { const coque = bola(R * 0.38, mCabelo, 32); em(coque, lado * R * 0.95, R * 0.4, -R * 0.15); cabeca.add(coque); }
    cabeca.add(laco3D(R, '#ff4f8b', R * 0.45, R * 0.95, R * 0.25));
  } else {
    const topete = bola(R * 0.3, mCabelo, 24); topete.scale.set(0.8, 1.2, 0.8);
    em(topete, R * 0.1, R * 1.05, R * 0.15); topete.rotation.z = -0.4;
    cabeca.add(topete);
  }
  rosto(cabeca, R, { humor, menina });
  g.add(cabeca);
  g.userData.cabeca = cabeca;
  ancora(cabeca, 'cabeca', 0, 0, 0);
  g.traverse(o => { if (o.isMesh && o.castShadow === undefined) o.castShadow = true; });
  return g;
}
