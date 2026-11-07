import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';
import { patinho } from './patinho.js';

export function casinha() {
  const g = new THREE.Group();
  const parede = plastico('#fff1d6', { rugosidade: 0.7, verniz: 0.05 });
  const madeira = plastico('#d99a5b', { rugosidade: 0.5, verniz: 0.4 });
  const telhado = plastico('#ef476f', { rugosidade: 0.35, verniz: 0.7 });
  const L = 4.2, A = 3.2, P = 2.4;
  // piso e andares
  g.add(em(caixa(L + 0.3, 0.22, P + 0.3, 0.08, madeira), 0, 0.11, 0));
  g.add(em(caixa(L, 0.16, P, 0.06, madeira), 0, A / 2 + 0.1, 0));
  // paredes do fundo (cores por cômodo) e laterais
  const cores = [['#ffd6e6', -L / 4, A * 0.75 + 0.1], ['#d6ecff', L / 4, A * 0.75 + 0.1], ['#fff1c9', -L / 4, A * 0.25 + 0.2], ['#dff5dc', L / 4, A * 0.25 + 0.2]];
  for (const [c, x, y] of cores) g.add(em(caixa(L / 2 - 0.02, A / 2 - 0.1, 0.16, 0.05, plastico(c, { rugosidade: 0.75, verniz: 0 })), x, y, -P / 2 + 0.08));
  for (const lado of [-1, 1]) g.add(em(caixa(0.18, A + 0.1, P, 0.06, parede), lado * (L / 2 - 0.09), A / 2 + 0.15, 0));
  g.add(em(caixa(0.12, A, P * 0.98, 0.04, parede), 0, A / 2 + 0.15, -0.02));
  // telhado
  for (const lado of [-1, 1]) {
    const t = caixa(L * 0.68, 0.22, P + 0.6, 0.1, telhado);
    t.rotation.z = -lado * 0.62;
    em(t, lado * L * 0.27, A + 0.95, 0);
    g.add(t);
  }
  const frontao = new THREE.Shape(); frontao.moveTo(-L / 2, 0); frontao.lineTo(0, 1.45); frontao.lineTo(L / 2, 0); frontao.closePath();
  const fr = malha(new THREE.ExtrudeGeometry(frontao, { depth: 0.12, bevelEnabled: true, bevelSize: 0.03, bevelThickness: 0.03 }), parede);
  em(fr, 0, A + 0.2, -P / 2 + 0.05); g.add(fr);
  const chamine = caixa(0.45, 1.0, 0.45, 0.06, plastico('#b5523b', { rugosidade: 0.7, verniz: 0 })); em(chamine, 1.1, A + 1.5, -0.3); g.add(chamine);
  const janelaRedonda = malha(new THREE.TorusGeometry(0.28, 0.07, 16, 32), plastico('#ffffff', { rugosidade: 0.3 }));
  em(janelaRedonda, 0, A + 0.85, -P / 2 + 0.22); g.add(janelaRedonda);
  g.add(em(malha(new THREE.CircleGeometry(0.27, 32), plastico('#9fd8ff', { emissivo: '#bfe6ff', rugosidade: 0.1 })), 0, A + 0.85, -P / 2 + 0.2));
  // janelas no fundo
  for (const [x, y] of [[-L / 4, A * 0.78], [L / 4 + 0.4, A * 0.28 + 0.15], [-0.6, A * 0.28 + 0.15]]) {
    g.add(em(caixa(0.8, 0.75, 0.08, 0.05, plastico('#ffffff', { rugosidade: 0.3 })), x, y, -P / 2 + 0.2));
    g.add(em(caixa(0.62, 0.58, 0.04, 0.03, plastico('#a8dcff', { emissivo: '#cfeaff', rugosidade: 0.05 })), x, y, -P / 2 + 0.24));
    g.add(em(caixa(0.05, 0.6, 0.06, 0.02, plastico('#ffffff')), x, y, -P / 2 + 0.26));
  }
  // porta
  const porta = caixa(0.75, 1.25, 0.1, 0.12, plastico('#9b5b34', { rugosidade: 0.45, verniz: 0.5 })); em(porta, -L / 2 + 0.75, 0.85, -P / 2 + 0.22); g.add(porta);
  g.add(em(bola(0.06, plastico('#ffd23f', { rugosidade: 0.15, verniz: 1 }), 12), -L / 2 + 1.0, 0.8, -P / 2 + 0.3));
  // móveis: cama (quarto), banheira (banheiro), sofá (sala), geladeira + fogão (cozinha)
  const yAndar = A / 2 + 0.18;
  const cama = new THREE.Group();
  cama.add(em(caixa(1.3, 0.3, 0.8, 0.1, plastico('#4d96ff', { rugosidade: 0.6, verniz: 0.1 })), 0, 0.3, 0));
  cama.add(em(caixa(1.3, 0.12, 0.8, 0.06, madeira), 0, 0.12, 0));
  cama.add(em(caixa(0.12, 0.7, 0.8, 0.05, madeira), -0.65, 0.35, 0));
  cama.add(em(caixa(0.36, 0.16, 0.6, 0.08, plastico('#ffffff', { rugosidade: 0.8, verniz: 0 })), -0.42, 0.5, 0));
  em(cama, -L / 4, yAndar, -0.3); g.add(cama);
  const urso = new THREE.Group(); const mu = massinha('#b07a4f');
  urso.add(em(bola(0.17, mu, 20), 0, 0.17, 0)); urso.add(em(bola(0.14, mu, 20), 0, 0.42, 0));
  for (const l of [-1, 1]) urso.add(em(bola(0.06, mu, 12), l * 0.1, 0.53, 0));
  em(urso, -L / 4 + 0.3, yAndar + 0.4, -0.2); g.add(urso);
  const banheira = new THREE.Group();
  banheira.add(em(caixa(1.2, 0.5, 0.7, 0.22, plastico('#ffffff', { rugosidade: 0.15, verniz: 1 })), 0, 0.3, 0));
  banheira.add(em(caixa(1.0, 0.06, 0.5, 0.03, plastico('#7fd0ff', { rugosidade: 0.05, verniz: 1, emissivo: '#bfe9ff' })), 0, 0.52, 0));
  for (let k = 0; k < 6; k++) banheira.add(em(bola(0.1 + (k % 3) * 0.03, plastico('#ffffff', { rugosidade: 0.9, verniz: 0, opacidade: 0.9 }), 12), -0.35 + k * 0.14, 0.6, (k % 2) * 0.12 - 0.05));
  banheira.add(em(patinho(0.25), 0.25, 0.5, 0.05));
  em(banheira, L / 4, yAndar, -0.2); g.add(banheira);
  const sofa = new THREE.Group(); const ms = plastico('#22b8a8', { rugosidade: 0.7, verniz: 0.05 });
  sofa.add(em(caixa(1.3, 0.3, 0.6, 0.12, ms), 0, 0.28, 0));
  sofa.add(em(caixa(1.3, 0.55, 0.18, 0.09, ms), 0, 0.5, -0.24));
  for (const l of [-1, 1]) sofa.add(em(caixa(0.18, 0.42, 0.6, 0.08, ms), l * 0.65, 0.35, 0));
  sofa.add(em(caixa(0.32, 0.28, 0.12, 0.08, plastico('#ffd23f', { rugosidade: 0.7 })), -0.35, 0.55, -0.1));
  em(sofa, -0.35, 0.22, -0.4); g.add(sofa);
  const gel = caixa(0.6, 1.3, 0.55, 0.12, plastico('#f4f7fb', { rugosidade: 0.15, verniz: 1 })); em(gel, L / 2 - 0.55, 0.87, -0.55); g.add(gel);
  g.add(em(caixa(0.05, 0.3, 0.06, 0.02, plastico('#c0c8d4')), L / 2 - 0.35, 1.1, -0.26));
  const fogao = caixa(0.6, 0.7, 0.55, 0.08, plastico('#ffffff', { rugosidade: 0.2, verniz: 1 })); em(fogao, L / 2 - 1.25, 0.57, -0.55); g.add(fogao);
  g.add(em(caixa(0.4, 0.3, 0.04, 0.03, plastico('#2b2b3a', { rugosidade: 0.1, verniz: 1 })), L / 2 - 1.25, 0.5, -0.26));
  const bolo = new THREE.Group();
  bolo.add(em(cilindro(0.2, 0.2, 0.18, plastico('#ffb3c9', { rugosidade: 0.6 })), 0, 0.09, 0));
  bolo.add(em(cilindro(0.13, 0.13, 0.14, plastico('#ffffff', { rugosidade: 0.6 })), 0, 0.25, 0));
  bolo.add(em(bola(0.04, plastico('#ff3355', { rugosidade: 0.2, verniz: 1 }), 12), 0, 0.36, 0));
  em(bolo, L / 2 - 1.25, 0.92, -0.55); g.add(bolo);
  // luzinha quente dentro
  const luz = new THREE.PointLight('#ffd9a0', 1.2, 5, 2); luz.position.set(0, A * 0.45, 1.3); g.add(luz);
  return g;
}
