// Os 40 itens da casa (dominio/ItensDaCasa.ordem) e as peças soltas do céu, cada um sozinho, na câmera
// 'casaItem' (o mesmo ângulo da casa: de frente, um pouco de cima). O app põe cada item pelo pé (o apoio
// do manifesto) na posição do domínio. As estruturas (terreno, paredes, telhado...) também ganham uma
// imagem solta, para o prêmio e o baú; na cena elas são as camadas de cenaDaCasa.js.
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { arvore } from './arvore.js';
import { flor } from './flor.js';
import { patinho } from './patinho.js';
import { pecas } from './cenaDaCasa.js';

const grupo = (...f) => { const g = new THREE.Group(); for (const o of f) if (o) g.add(o); return g; };
const brilhante = (cor, o = {}) => plastico(cor, { rugosidade: 0.28, verniz: 0.8, ...o });
const fosco = (cor) => plastico(cor, { rugosidade: 0.7, verniz: 0.08 });
const madeira = () => plastico('#d9975a', { rugosidade: 0.5, verniz: 0.35 });
const madeiraClara = () => plastico('#efbf86', { rugosidade: 0.5, verniz: 0.35 });
const preto = () => plastico('#1d1210', { rugosidade: 0.12, verniz: 1 });
const branco = () => plastico('#ffffff', { rugosidade: 0.3, verniz: 0.6 });
const luz = (cor = '#ffffff') => plastico(cor, { rugosidade: 0.1, emissivo: cor });

/** Cápsula de [a] até [b]. */
function segmento(a, b, r, mat) {
  const va = new THREE.Vector3(...a), vb = new THREE.Vector3(...b);
  const d = vb.clone().sub(va);
  const m = capsula(r, Math.max(0.001, d.length()), mat);
  m.position.copy(va).add(vb).multiplyScalar(0.5);
  m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), d.normalize());
  return m;
}

/** Olhinho brilhante (de frente para +z). */
function olho(r) {
  const g = new THREE.Group();
  const o = bola(r, preto(), 24); o.scale.set(0.85, 1.05, 0.6); g.add(o);
  g.add(em(bola(r * 0.36, luz(), 12), r * 0.28, r * 0.35, r * 0.45));
  return g;
}

/** Sorriso: arco de toro na frente (+z). */
function sorriso(r, tubo, abertura = 2.2, cor = '#7d1f24') {
  const geo = new THREE.TorusGeometry(r, tubo, 10, 28, abertura);
  geo.rotateZ(-Math.PI / 2 - abertura / 2);
  return malha(geo, plastico(cor, { rugosidade: 0.3, verniz: 0.6 }), { sombra: false });
}

function bochecha(r) {
  const b = bola(r, plastico('#ff7a8a', { rugosidade: 0.8, verniz: 0, opacidade: 0.55 }), 16);
  b.scale.set(1, 0.65, 0.3);
  b.castShadow = false;
  return b;
}

/** Rostinho fofo (olhos, sorriso, bochechas) no plano z = 0, para o sol, a estrela, a nuvem. */
function rostinho(s) {
  const g = new THREE.Group();
  for (const l of [-1, 1]) {
    g.add(em(olho(s * 0.13), l * s * 0.3, s * 0.08, 0));
    g.add(em(bochecha(s * 0.13), l * s * 0.5, -s * 0.16, -0.01));
  }
  g.add(em(sorriso(s * 0.16, s * 0.035), 0, -s * 0.14, 0));
  return g;
}

// ---------------------------------------------------------------------------------------------
// Móveis

function cama() {
  const g = new THREE.Group();
  const m = madeira();
  g.add(em(caixa(2.1, 0.3, 1.3, 0.1, m), 0, 0.35, 0));
  for (const [x, z] of [[-0.95, -0.55], [0.95, -0.55], [-0.95, 0.55], [0.95, 0.55]]) g.add(em(cilindro(0.08, 0.08, 0.25, m, 16), x, 0.12, z));
  const cabeceira = caixa(0.2, 1.25, 1.3, 0.1, m);
  g.add(em(cabeceira, -1.02, 0.75, 0));
  g.add(em(bola(0.12, m, 16), -1.02, 1.42, 0.55), em(bola(0.12, m, 16), -1.02, 1.42, -0.55));
  g.add(em(caixa(0.18, 0.75, 1.3, 0.08, m), 1.02, 0.5, 0));
  g.add(em(caixa(2.0, 0.26, 1.2, 0.1, fosco('#fbf6ef')), 0, 0.62, 0));
  g.add(em(caixa(1.35, 0.32, 1.26, 0.13, plastico('#4d96ff', { rugosidade: 0.62, verniz: 0.12 })), 0.3, 0.66, 0));
  g.add(em(caixa(0.22, 0.34, 1.28, 0.1, fosco('#ffffff')), -0.38, 0.67, 0));
  // estrelinhas no cobertor
  const am = brilhante('#ffd23f');
  for (const [x, z] of [[0.15, 0.3], [0.65, -0.2], [0.55, 0.45]]) { const e = estrela3D(0.1, 0.02, am); e.rotation.x = -Math.PI / 2; g.add(em(e, x, 0.84, z)); }
  const trav = caixa(0.5, 0.22, 0.85, 0.11, fosco('#ffffff'));
  trav.rotation.z = 0.25;
  g.add(em(trav, -0.7, 0.85, 0));
  g.rotation.y = -0.35;
  return g;
}

function sofa() {
  const g = new THREE.Group();
  const m = plastico('#22b8a8', { rugosidade: 0.65, verniz: 0.1 });
  g.add(em(caixa(2.0, 0.42, 0.95, 0.16, m), 0, 0.42, 0));
  g.add(em(caixa(2.0, 0.85, 0.3, 0.14, m), 0, 0.78, -0.36));
  for (const l of [-1, 1]) g.add(em(caixa(0.32, 0.62, 1.0, 0.14, m), l * 1.0, 0.5, 0.02));
  for (const l of [-1, 1]) g.add(em(caixa(0.86, 0.16, 0.8, 0.07, plastico('#33cbbb', { rugosidade: 0.6, verniz: 0.1 })), l * 0.44, 0.68, 0.06));
  g.add(em(caixa(0.5, 0.42, 0.16, 0.12, plastico('#ffd23f', { rugosidade: 0.6 })), -0.5, 0.95, -0.12));
  const almo = caixa(0.42, 0.38, 0.16, 0.12, plastico('#ff6fae', { rugosidade: 0.6 }));
  almo.rotation.z = -0.15;
  g.add(em(almo, 0.55, 0.93, -0.12));
  for (const l of [-1, 1]) g.add(em(cilindro(0.07, 0.05, 0.2, madeira(), 12), l * 0.95, 0.1, 0.3));
  g.rotation.y = -0.18;
  return g;
}

function geladeira() {
  const g = new THREE.Group();
  const m = brilhante('#f4f8fc', { rugosidade: 0.18, verniz: 1 });
  g.add(em(caixa(1.0, 1.95, 0.85, 0.16, m), 0, 1.03, 0));
  g.add(em(caixa(0.96, 0.04, 0.04, 0.02, plastico('#b8c4d0')), 0, 1.38, 0.43));
  for (const [y, a] of [[1.66, 0.36], [1.0, 0.5]]) g.add(em(capsula(0.035, a, plastico('#c0c8d4', { rugosidade: 0.2, verniz: 1 })), 0.36, y, 0.47));
  for (const l of [-1, 1]) g.add(em(cilindro(0.06, 0.06, 0.08, plastico('#9aa6b4'), 12), l * 0.36, 0.03, 0.28));
  // ímãs: coração e estrela
  const c = new THREE.Shape(); c.moveTo(0, -0.1); c.bezierCurveTo(-0.16, 0.02, -0.08, 0.14, 0, 0.06); c.bezierCurveTo(0.08, 0.14, 0.16, 0.02, 0, -0.1);
  g.add(em(malha(new THREE.ExtrudeGeometry(c, { depth: 0.03, bevelEnabled: true, bevelSize: 0.015, bevelThickness: 0.015 }), brilhante('#ff4f8b')), -0.2, 1.62, 0.43));
  g.add(em(estrela3D(0.09, 0.03, brilhante('#ffc21a')), -0.15, 0.85, 0.45));
  g.rotation.y = -0.3;
  return g;
}

function fogao() {
  const g = new THREE.Group();
  const m = brilhante('#ffffff', { rugosidade: 0.2, verniz: 1 });
  g.add(em(caixa(1.2, 1.05, 0.95, 0.12, m), 0, 0.56, 0));
  g.add(em(caixa(1.18, 0.06, 0.92, 0.03, plastico('#d7dee6')), 0, 1.09, 0));
  g.add(em(caixa(0.78, 0.48, 0.05, 0.08, plastico('#2b2b3a', { rugosidade: 0.08, verniz: 1 })), 0, 0.5, 0.48));
  g.add(em(capsula(0.035, 0.62, plastico('#c0c8d4')), 0, 0.82, 0.52).rotateZ(Math.PI / 2));
  for (let i = 0; i < 4; i++) g.add(em(cilindro(0.06, 0.06, 0.08, brilhante('#ff6b5a'), 12).rotateX(Math.PI / 2), -0.39 + i * 0.26, 0.97, 0.48));
  for (const [x, z] of [[-0.28, -0.2], [0.28, -0.2], [-0.28, 0.2], [0.28, 0.2]]) g.add(em(cilindro(0.17, 0.17, 0.05, plastico('#3a3a48', { rugosidade: 0.3 }), 24), x, 1.12, z));
  // panelinha vermelha com tampa
  const p = brilhante('#ef476f');
  g.add(em(cilindro(0.3, 0.27, 0.36, p, 32), 0.28, 1.33, 0.2));
  g.add(em(cilindro(0.31, 0.31, 0.06, p, 32), 0.28, 1.53, 0.2));
  g.add(em(bola(0.07, brilhante('#ffd23f'), 12), 0.28, 1.6, 0.2));
  for (const l of [-1, 1]) g.add(em(capsula(0.04, 0.12, p).rotateZ(Math.PI / 2), 0.28 + l * 0.37, 1.4, 0.2));
  g.rotation.y = -0.25;
  return g;
}

function mesa() {
  const g = new THREE.Group();
  const m = madeiraClara();
  g.add(em(cilindro(1.15, 1.15, 0.14, m, 48), 0, 0.95, 0));
  g.add(em(cilindro(1.17, 1.17, 0.05, plastico('#d9975a'), 48), 0, 0.88, 0));
  // toalhinha xadrez (quadradinhos vermelhos)
  const v = fosco('#ff6b6b');
  for (let i = -1; i <= 1; i++) for (let j = -1; j <= 1; j++) if ((i + j) % 2 === 0) g.add(em(caixa(0.32, 0.02, 0.32, 0.01, v), i * 0.32, 1.03, j * 0.32));
  const pe = madeira();
  for (const [x, z] of [[-0.75, -0.55], [0.75, -0.55], [-0.75, 0.55], [0.75, 0.55]]) g.add(em(cilindro(0.08, 0.06, 0.9, pe, 16), x, 0.45, z));
  return g;
}

function tv() {
  const g = new THREE.Group();
  const corpo = brilhante('#ff8a5c');
  g.add(em(caixa(1.6, 1.15, 0.6, 0.22, corpo), 0, 0.82, 0));
  g.add(em(caixa(1.15, 0.85, 0.06, 0.16, plastico('#24305e', { rugosidade: 0.06, verniz: 1, emissivo: '#1a2a5c' })), -0.12, 0.82, 0.3));
  const r = plastico('#ffffff', { rugosidade: 0.1, emissivo: '#ffffff', opacidade: 0.55 });
  g.add(em(caixa(0.06, 0.4, 0.01, 0.02, r).rotateZ(-0.6), -0.45, 0.95, 0.34));
  for (let i = 0; i < 2; i++) g.add(em(cilindro(0.07, 0.07, 0.06, brilhante('#ffd23f'), 16).rotateX(Math.PI / 2), 0.6, 1.0 - i * 0.26, 0.31));
  for (const l of [-1, 1]) {
    g.add(segmento([l * 0.1, 1.38, 0], [l * 0.5, 1.95, 0], 0.03, plastico('#5a5a6a')));
    g.add(em(bola(0.07, brilhante('#ef476f'), 12), l * 0.5, 1.97, 0));
    g.add(em(cilindro(0.06, 0.08, 0.26, plastico('#8d5a3b'), 12), l * 0.55, 0.13, 0));
  }
  g.add(em(caixa(0.4, 0.14, 0.3, 0.06, corpo), 0, 1.42, 0));
  g.rotation.y = -0.15;
  return g;
}

function banheira() {
  const g = new THREE.Group();
  g.add(em(caixa(1.9, 0.75, 1.0, 0.32, brilhante('#ffffff', { rugosidade: 0.12, verniz: 1 })), 0, 0.55, 0));
  g.add(em(caixa(1.6, 0.08, 0.72, 0.04, plastico('#7fd0ff', { rugosidade: 0.05, verniz: 1, emissivo: '#bfe9ff' })), 0, 0.88, 0));
  for (const [x, z] of [[-0.75, -0.35], [0.75, -0.35], [-0.75, 0.35], [0.75, 0.35]]) g.add(em(bola(0.12, plastico('#ffc93c', { rugosidade: 0.15, verniz: 1 }), 16), x, 0.12, z));
  const espuma = plastico('#ffffff', { rugosidade: 0.9, verniz: 0, emissivo: '#eef6ff' });
  for (let k = 0; k < 9; k++) g.add(em(bola(0.14 + (k % 3) * 0.05, espuma, 16), -0.7 + k * 0.17, 0.98 + (k % 2) * 0.05, (k % 3) * 0.14 - 0.15));
  g.add(em(patinho(0.36, '#ffc400').rotateY(-0.5), 0.45, 0.88, 0.1));
  // torneira
  g.add(segmento([-0.95, 0.9, -0.3], [-0.95, 1.25, -0.3], 0.05, plastico('#c0c8d4', { rugosidade: 0.15, verniz: 1 })));
  g.add(segmento([-0.95, 1.25, -0.3], [-0.72, 1.25, -0.3], 0.05, plastico('#c0c8d4', { rugosidade: 0.15, verniz: 1 })));
  g.rotation.y = -0.2;
  return g;
}

function abajur() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.32, 0.38, 0.14, brilhante('#ff8fab'), 32), 0, 0.07, 0));
  g.add(em(bola(0.26, brilhante('#ff8fab'), 32), 0, 0.36, 0));
  g.add(em(cilindro(0.04, 0.04, 0.5, plastico('#f2d6a6'), 12), 0, 0.75, 0));
  g.add(em(cilindro(0.3, 0.55, 0.62, plastico('#fff1b8', { rugosidade: 0.6, verniz: 0.1, emissivo: '#ffd77a' }), 40, true), 0, 1.25, 0));
  g.add(em(bola(0.16, luz('#fff3c4'), 16), 0, 1.05, 0));
  g.add(em(malha(new THREE.TorusGeometry(0.55, 0.035, 8, 40), plastico('#ff8fab')).rotateX(Math.PI / 2), 0, 0.94, 0));
  return g;
}

function quadro() {
  const g = new THREE.Group();
  g.add(em(caixa(1.5, 1.2, 0.14, 0.08, plastico('#ffc93c', { rugosidade: 0.25, verniz: 0.9 })), 0, 0.6, 0));
  g.add(em(caixa(1.24, 0.94, 0.06, 0.04, fosco('#a9e1ff')), 0, 0.6, 0.06));
  const morro = bola(0.65, fosco('#7cc56b'), 32); morro.scale.set(1, 0.5, 0.12);
  g.add(em(morro, -0.25, 0.18 + 0.06, 0.08));
  const morro2 = bola(0.5, fosco('#5fae52'), 32); morro2.scale.set(1, 0.55, 0.1);
  g.add(em(morro2, 0.35, 0.2 + 0.04, 0.1));
  g.add(em(bola(0.15, brilhante('#ffb703', { emissivo: '#ffcf5a' }), 20), 0.35, 0.86, 0.1));
  const casinha = caixa(0.22, 0.18, 0.04, 0.02, fosco('#fff4e0'));
  g.add(em(casinha, -0.2, 0.52, 0.15));
  const t = new THREE.Shape(); t.moveTo(-0.15, 0); t.lineTo(0, 0.13); t.lineTo(0.15, 0); t.closePath();
  g.add(em(malha(new THREE.ExtrudeGeometry(t, { depth: 0.04, bevelEnabled: false }), fosco('#ef476f')), -0.2, 0.61, 0.13));
  g.rotation.z = 0.04;
  return g;
}

function planta() {
  const g = new THREE.Group();
  const vaso = plastico('#e07a4f', { rugosidade: 0.6, verniz: 0.2 });
  g.add(em(cilindro(0.42, 0.3, 0.62, vaso, 32), 0, 0.31, 0));
  g.add(em(cilindro(0.47, 0.47, 0.14, vaso, 32), 0, 0.62, 0));
  g.add(em(cilindro(0.4, 0.4, 0.05, massinha('#7a4a2a'), 32), 0, 0.68, 0));
  const folha = plastico('#3fae4a', { rugosidade: 0.45, verniz: 0.4 });
  const clara = plastico('#5fcf5a', { rugosidade: 0.45, verniz: 0.4 });
  const n = 9;
  for (let i = 0; i < n; i++) {
    const a = (i / n) * Math.PI * 2;
    const incl = 0.5 + (i % 3) * 0.2;
    const f = bola(0.2, i % 2 ? folha : clara, 20);
    f.scale.set(0.7, 2.2, 0.35);
    const piv = new THREE.Group();
    piv.add(em(f, 0, 0.42, 0));
    piv.rotation.set(0, a, incl);
    g.add(em(piv, 0, 0.7, 0));
  }
  g.add(segmento([0, 0.7, 0], [0, 1.35, 0], 0.04, folha));
  const topo = bola(0.22, clara, 20); topo.scale.set(0.7, 1.8, 0.4);
  g.add(em(topo, 0, 1.45, 0));
  return g;
}

function livros() {
  const g = new THREE.Group();
  const livro = (cor, l, a, p) => {
    const b = new THREE.Group();
    b.add(caixa(l, a, p, 0.04, brilhante(cor)));
    b.add(em(caixa(l - 0.06, a * 0.78, p + 0.01, 0.02, fosco('#fffaf0')), 0.04, 0, 0));
    return b;
  };
  const pilha = [['#ef476f', 1.2, 0.22, 0.85, 0.05], ['#4d96ff', 1.1, 0.2, 0.8, -0.08], ['#3dbe6b', 1.0, 0.24, 0.78, 0.12]];
  let y = 0;
  for (const [cor, l, a, p, r] of pilha) { const b = livro(cor, l, a, p); b.rotation.y = r; g.add(em(b, 0, y + a / 2, 0)); y += a; }
  const empe = livro('#ffc93c', 0.95, 0.2, 0.7);
  empe.rotation.set(0, -0.3, Math.PI / 2 - 0.12);
  g.add(em(empe, 0.72, 0.48, 0.0));
  // maçã em cima
  g.add(em(bola(0.17, brilhante('#ff3355'), 20), -0.15, y + 0.16, 0.05));
  g.add(em(cilindro(0.02, 0.02, 0.1, massinha('#6b3e1f'), 8), -0.15, y + 0.36, 0.05));
  return g;
}

function espelho() {
  const g = new THREE.Group();
  const aro = malha(new THREE.TorusGeometry(0.62, 0.11, 20, 48), brilhante('#ff8fab'));
  aro.scale.set(0.8, 1.05, 1);
  g.add(em(aro, 0, 0.76, 0));
  const vidro = malha(new THREE.CircleGeometry(0.62, 48), plastico('#cdeeff', { rugosidade: 0.02, verniz: 1, emissivo: '#bfe6ff', ambiente: 1.2 }));
  vidro.scale.set(0.8, 1.05, 1);
  g.add(em(vidro, 0, 0.76, -0.02));
  const r = plastico('#ffffff', { rugosidade: 0.1, emissivo: '#ffffff', opacidade: 0.8 });
  g.add(em(caixa(0.08, 0.5, 0.01, 0.03, r).rotateZ(-0.6), -0.15, 0.95, 0.01));
  g.add(em(caixa(0.05, 0.25, 0.01, 0.02, r).rotateZ(-0.6), 0.08, 0.68, 0.01));
  // lacinho no alto
  const laco = brilhante('#ffd23f');
  for (const l of [-1, 1]) { const b = bola(0.11, laco, 16); b.scale.set(1.3, 0.8, 0.6); g.add(em(b, l * 0.12, 1.47, 0.04)); }
  g.add(em(bola(0.07, laco, 12), 0, 1.47, 0.08));
  return g;
}

function violao() {
  const g = new THREE.Group();
  const corpo = plastico('#f0a04b', { rugosidade: 0.3, verniz: 0.8 });
  const b1 = cilindro(0.5, 0.5, 0.26, corpo, 48); b1.rotation.x = Math.PI / 2; g.add(em(b1, 0, 0.52, 0));
  const b2 = cilindro(0.38, 0.38, 0.26, corpo, 48); b2.rotation.x = Math.PI / 2; g.add(em(b2, 0, 1.1, 0));
  g.add(em(caixa(0.5, 0.5, 0.26, 0.1, corpo), 0, 0.82, 0));
  g.add(em(cilindro(0.17, 0.17, 0.02, preto(), 32).rotateX(Math.PI / 2), 0, 0.92, 0.14));
  g.add(em(malha(new THREE.TorusGeometry(0.19, 0.02, 8, 32), plastico('#8d4a1f')), 0, 0.92, 0.135));
  g.add(em(caixa(0.36, 0.08, 0.06, 0.02, plastico('#6b3a1a')), 0, 0.5, 0.15));
  const braco = plastico('#7a4321', { rugosidade: 0.4, verniz: 0.5 });
  g.add(em(caixa(0.16, 1.1, 0.1, 0.04, braco), 0, 1.85, 0.06));
  g.add(em(caixa(0.26, 0.36, 0.1, 0.06, braco), 0, 2.5, 0.04));
  for (const l of [-1, 1]) for (let k = 0; k < 3; k++) g.add(em(bola(0.04, plastico('#e8e8f0'), 8), l * 0.17, 2.4 + k * 0.1, 0.04));
  const corda = plastico('#ffffff', { rugosidade: 0.2 });
  for (let k = -2; k <= 2; k++) g.add(em(cilindro(0.008, 0.008, 1.95, corda, 6), k * 0.025, 1.47, 0.15));
  g.rotation.z = -0.12;
  return g;
}

function bolo() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.75, 0.75, 0.06, branco(), 40), 0, 0.03, 0));
  g.add(em(cilindro(0.55, 0.55, 0.4, plastico('#ffb3c9', { rugosidade: 0.6 }), 40), 0, 0.26, 0));
  g.add(em(cilindro(0.4, 0.4, 0.32, plastico('#fff1f5', { rugosidade: 0.6 }), 40), 0, 0.62, 0));
  const cob = plastico('#ffffff', { rugosidade: 0.5, verniz: 0.3 });
  g.add(em(malha(new THREE.TorusGeometry(0.53, 0.06, 12, 40), cob).rotateX(Math.PI / 2), 0, 0.46, 0));
  for (let i = 0; i < 10; i++) { const a = (i / 10) * Math.PI * 2; const p = bola(0.06, cob, 10); p.scale.set(1, 1.6, 1); g.add(em(p, Math.cos(a) * 0.54, 0.38, Math.sin(a) * 0.54)); }
  g.add(em(malha(new THREE.TorusGeometry(0.38, 0.05, 12, 40), plastico('#ff6fae', { rugosidade: 0.4 })).rotateX(Math.PI / 2), 0, 0.78, 0));
  for (let i = 0; i < 6; i++) { const a = (i / 6) * Math.PI * 2 + 0.3; g.add(em(bola(0.06, brilhante('#ff3355'), 12), Math.cos(a) * 0.4, 0.5, Math.sin(a) * 0.4)); }
  for (const [x, cor] of [[-0.14, '#4d96ff'], [0.14, '#3dbe6b']]) {
    g.add(em(cilindro(0.04, 0.04, 0.3, brilhante(cor), 12), x, 0.93, 0));
    const chama = bola(0.06, luz('#ffb703'), 12); chama.scale.set(0.8, 1.5, 0.8);
    g.add(em(chama, x, 1.15, 0));
  }
  return g;
}

function ursinho() {
  const g = new THREE.Group();
  const m = massinha('#c08552');
  const claro = massinha('#e8c49a');
  const corpo = bola(0.45, m, 32); corpo.scale.set(1, 1.05, 0.9); g.add(em(corpo, 0, 0.5, 0));
  const barriga = bola(0.26, claro, 24); barriga.scale.set(1, 1.1, 0.5); g.add(em(barriga, 0, 0.5, 0.25));
  const cab = bola(0.4, m, 32); g.add(em(cab, 0, 1.18, 0));
  for (const l of [-1, 1]) {
    g.add(em(bola(0.15, m, 16), l * 0.3, 1.48, -0.02));
    g.add(em(bola(0.08, plastico('#ffb3c9', { rugosidade: 0.7 }), 12), l * 0.3, 1.48, 0.08));
    g.add(em(olho(0.055), l * 0.14, 1.25, 0.36));
    const braco = capsula(0.12, 0.3, m); braco.rotation.z = l * 0.6; g.add(em(braco, l * 0.45, 0.62, 0.08));
    const perna = capsula(0.14, 0.2, m); perna.rotation.x = Math.PI / 2; g.add(em(perna, l * 0.25, 0.17, 0.3));
    const pata = bola(0.09, claro, 12); pata.scale.set(1, 1, 0.4); g.add(em(pata, l * 0.25, 0.17, 0.52));
  }
  const fucinho = bola(0.17, claro, 20); fucinho.scale.set(1.15, 0.85, 0.8); g.add(em(fucinho, 0, 1.08, 0.33));
  g.add(em(bola(0.06, preto(), 12), 0, 1.13, 0.47));
  g.add(em(sorriso(0.06, 0.012, 2.0, '#4a2a1a'), 0, 1.03, 0.47));
  const laco = brilhante('#ef476f');
  for (const l of [-1, 1]) { const b = bola(0.1, laco, 12); b.scale.set(1.3, 0.8, 0.6); g.add(em(b, l * 0.12, 0.85, 0.34)); }
  g.add(em(bola(0.06, laco, 12), 0, 0.85, 0.38));
  return g;
}

// ---------------------------------------------------------------------------------------------
// Bichos e brinquedos

function cachorro() {
  // filhote bege de orelhas marrons (maquete 1), virado três quartos
  const g = new THREE.Group();
  const m = plastico('#e9c38f', { rugosidade: 0.6, verniz: 0.15 });
  const esc = plastico('#a8673a', { rugosidade: 0.6, verniz: 0.15 });
  const corpo = capsula(0.38, 0.55, m); corpo.rotation.z = Math.PI / 2; g.add(em(corpo, 0, 0.62, 0));
  for (const [x, z] of [[-0.38, 0.2], [-0.38, -0.2], [0.38, 0.2], [0.38, -0.2]]) {
    g.add(em(capsula(0.12, 0.3, m), x, 0.25, z));
    const pata = bola(0.13, plastico('#f6dfbd', { rugosidade: 0.6 }), 12); pata.scale.set(1, 0.6, 1.2);
    g.add(em(pata, x, 0.07, z + 0.03));
  }
  const rabo = segmento([-0.62, 0.75, 0], [-0.85, 1.1, 0], 0.07, m); g.add(rabo);
  const cab = new THREE.Group();
  cab.add(bola(0.42, m, 32));
  const fuc = bola(0.22, plastico('#f6dfbd', { rugosidade: 0.6 }), 20); fuc.scale.set(1.1, 0.85, 1); cab.add(em(fuc, 0, -0.12, 0.33));
  cab.add(em(bola(0.08, preto(), 12), 0, -0.04, 0.53));
  cab.add(em(sorriso(0.08, 0.016, 2.0, '#4a2a1a'), 0, -0.19, 0.52));
  const lingua = bola(0.06, plastico('#ff7f8c', { rugosidade: 0.4 }), 12); lingua.scale.set(1, 1.3, 0.5); cab.add(em(lingua, 0.02, -0.29, 0.5));
  for (const l of [-1, 1]) {
    cab.add(em(olho(0.07), l * 0.17, 0.1, 0.37));
    const orelha = bola(0.2, esc, 20); orelha.scale.set(0.55, 1.15, 0.35); orelha.rotation.z = l * 0.35;
    cab.add(em(orelha, l * 0.4, -0.02, 0.0));
  }
  const mancha = bola(0.1, esc, 12); mancha.scale.set(1.4, 1, 0.4); cab.add(em(mancha, 0.15, 0.28, 0.3));
  cab.rotation.y = 0.5;
  g.add(em(cab, 0.55, 1.05, 0));
  // coleira vermelha com medalhinha
  const col = malha(new THREE.TorusGeometry(0.27, 0.05, 10, 32), brilhante('#ef476f'));
  col.rotation.set(Math.PI / 2, 0.5, 0.3);
  g.add(em(col, 0.5, 0.8, 0));
  g.add(em(bola(0.06, brilhante('#ffc93c'), 12), 0.62, 0.68, 0.2));
  g.rotation.y = -0.55;
  return g;
}

function gato() {
  const g = new THREE.Group();
  const m = plastico('#ffa94d', { rugosidade: 0.6, verniz: 0.15 });
  const claro = plastico('#fff1df', { rugosidade: 0.6, verniz: 0.1 });
  const corpo = bola(0.45, m, 32); corpo.scale.set(1, 1.1, 0.9); g.add(em(corpo, 0, 0.48, 0));
  const peito = bola(0.27, claro, 20); peito.scale.set(1, 1.2, 0.5); g.add(em(peito, 0, 0.55, 0.3));
  for (const l of [-1, 1]) { const p = bola(0.13, claro, 12); p.scale.set(1, 0.7, 1.3); g.add(em(p, l * 0.17, 0.08, 0.35)); }
  const rabo = new THREE.TorusGeometry(0.35, 0.07, 12, 32, Math.PI * 1.2);
  g.add(em(malha(rabo, m).rotateY(Math.PI / 2), -0.1, 0.38, -0.25).rotateX(0.2));
  const cab = new THREE.Group();
  cab.add(bola(0.38, m, 32));
  for (const l of [-1, 1]) {
    const o = malha(new THREE.ConeGeometry(0.14, 0.26, 4), m); o.rotation.z = -l * 0.35; cab.add(em(o, l * 0.22, 0.34, 0));
    const oi = malha(new THREE.ConeGeometry(0.07, 0.15, 4), plastico('#ffb3c9')); oi.rotation.z = -l * 0.35; cab.add(em(oi, l * 0.215, 0.32, 0.06));
    cab.add(em(olho(0.065), l * 0.15, 0.06, 0.33));
    for (const k of [-1, 1]) {
      const bigode = cilindro(0.008, 0.008, 0.3, plastico('#ffffff'), 6); bigode.rotation.z = Math.PI / 2 + k * 0.15 * l;
      cab.add(em(bigode, l * 0.32, -0.08 + k * 0.04, 0.3));
    }
  }
  const foc = bola(0.13, claro, 16); foc.scale.set(1.3, 0.8, 0.8); cab.add(em(foc, 0, -0.1, 0.3));
  cab.add(em(bola(0.04, plastico('#ff6f91', { rugosidade: 0.3, verniz: 0.8 }), 12), 0, -0.04, 0.4));
  cab.add(em(sorriso(0.05, 0.012, 2.4, '#5a2a1a'), 0, -0.1, 0.4));
  g.add(em(cab, 0, 1.12, 0.05));
  g.rotation.y = 0.3;
  return g;
}

function bola_() {
  const g = new THREE.Group();
  const r = 0.55;
  g.add(em(bola(r, brilhante('#ffffff', { rugosidade: 0.25 }), 48), 0, r, 0));
  // 12 pentágonos pretos nas direções do icosaedro
  const ico = new THREE.IcosahedronGeometry(1, 0);
  const pos = ico.attributes.position;
  const vistos = [];
  for (let i = 0; i < pos.count; i++) {
    const v = new THREE.Vector3().fromBufferAttribute(pos, i).normalize();
    if (vistos.some((w) => w.distanceTo(v) < 0.01)) continue;
    vistos.push(v);
    const p = cilindro(0.17, 0.17, 0.06, preto(), 5);
    p.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), v);
    p.position.copy(v.clone().multiplyScalar(r - 0.015)).add(new THREE.Vector3(0, r, 0));
    g.add(p);
  }
  g.rotation.set(0.3, 0.4, 0);
  return g;
}

function bicicleta() {
  const g = new THREE.Group();
  const quadro = brilhante('#ff4f8b');
  const pneu = plastico('#2b2b3a', { rugosidade: 0.6, verniz: 0.2 });
  const aro = plastico('#c0c8d4', { rugosidade: 0.2, verniz: 1 });
  const R = 0.5;
  for (const x of [-0.85, 0.85]) {
    g.add(em(malha(new THREE.TorusGeometry(R, 0.09, 16, 40), pneu), x, R + 0.02, 0));
    g.add(em(malha(new THREE.TorusGeometry(R - 0.1, 0.025, 8, 40), aro), x, R + 0.02, 0));
    for (let k = 0; k < 6; k++) { const a = (k / 6) * Math.PI; const s = cilindro(0.012, 0.012, (R - 0.1) * 2, aro, 6); s.rotation.z = a; g.add(em(s, x, R + 0.02, 0)); }
    g.add(em(cilindro(0.07, 0.07, 0.12, brilhante('#ffd23f'), 12).rotateX(Math.PI / 2), x, R + 0.02, 0));
  }
  const pedal = [0, R + 0.02, 0];
  const selim = [-0.35, 1.25, 0], guidao = [0.6, 1.4, 0];
  for (const [a, b] of [[[-0.85, R, 0], pedal], [pedal, selim], [[-0.85, R, 0], [-0.25, 1.08, 0]], [[-0.25, 1.08, 0], [0.55, 1.12, 0]], [pedal, [0.55, 1.12, 0]], [[0.85, R, 0], guidao]]) g.add(segmento(a, b, 0.055, quadro));
  const s = capsula(0.09, 0.22, plastico('#3a2a4a', { rugosidade: 0.5 })); s.rotation.z = Math.PI / 2; g.add(em(s, -0.38, 1.33, 0));
  g.add(segmento([0.6, 1.4, -0.3], [0.6, 1.4, 0.3], 0.04, aro));
  for (const l of [-1, 1]) g.add(em(capsula(0.06, 0.12, plastico('#3a2a4a')).rotateX(Math.PI / 2), 0.6, 1.4, l * 0.33));
  g.add(em(cilindro(0.12, 0.12, 0.05, aro, 16).rotateX(Math.PI / 2), 0, R + 0.02, 0.06));
  // cestinha na frente
  g.add(em(caixa(0.4, 0.3, 0.42, 0.08, plastico('#f0bf86', { rugosidade: 0.6 })), 0.85, 1.3, 0));
  g.add(em(flor('#ffd23f', 1.0), 0.85, 1.4, 0.05));
  g.rotation.y = -0.35;
  return g;
}

function trem() {
  const g = new THREE.Group();
  const verm = brilhante('#ef476f'), azul = brilhante('#4d96ff'), am = brilhante('#ffc93c'), verde = brilhante('#3dbe6b');
  g.add(em(caixa(1.9, 0.26, 0.8, 0.08, verm), 0, 0.4, 0));
  const caldeira = cilindro(0.36, 0.36, 1.0, azul, 32); caldeira.rotation.z = Math.PI / 2; g.add(em(caldeira, 0.35, 0.86, 0));
  g.add(em(cilindro(0.32, 0.32, 0.06, am, 32).rotateZ(Math.PI / 2), 0.87, 0.86, 0));
  g.add(em(caixa(0.75, 0.95, 0.82, 0.1, am), -0.5, 1.0, 0));
  g.add(em(caixa(0.95, 0.12, 0.98, 0.06, verm), -0.5, 1.53, 0));
  g.add(em(caixa(0.4, 0.32, 0.05, 0.06, plastico('#a9e1ff', { rugosidade: 0.05, verniz: 1, emissivo: '#cfeaff' })), -0.5, 1.12, 0.42));
  g.add(em(cilindro(0.14, 0.1, 0.4, verde, 20), 0.6, 1.38, 0));
  g.add(em(cilindro(0.2, 0.14, 0.12, verde, 20), 0.6, 1.62, 0));
  g.add(em(bola(0.12, am, 16), 0.15, 1.24, 0));
  const fumaca = plastico('#ffffff', { rugosidade: 0.9, emissivo: '#eef6ff' });
  g.add(em(bola(0.16, fumaca, 16), 0.72, 1.92, 0), em(bola(0.12, fumaca, 16), 0.95, 2.12, 0));
  for (const [x, r] of [[-0.6, 0.27], [0.1, 0.22], [0.65, 0.22]]) for (const z of [-0.42, 0.42]) {
    g.add(em(cilindro(r, r, 0.12, plastico('#3a2a4a', { rugosidade: 0.4 }), 24).rotateX(Math.PI / 2), x, r, z));
    g.add(em(cilindro(r * 0.45, r * 0.45, 0.14, am, 16).rotateX(Math.PI / 2), x, r, z));
  }
  const limpa = new THREE.Shape(); limpa.moveTo(0, 0); limpa.lineTo(0.3, 0); limpa.lineTo(0, 0.3); limpa.closePath();
  g.add(em(malha(new THREE.ExtrudeGeometry(limpa, { depth: 0.6, bevelEnabled: true, bevelSize: 0.03, bevelThickness: 0.03 }), verm), 0.95, 0.27, -0.3));
  g.rotation.y = -0.4;
  return g;
}

function borboleta() {
  const g = new THREE.Group();
  const asa = (cor, sx, sy) => { const b = bola(0.5, brilhante(cor, { rugosidade: 0.3, verniz: 0.7 }), 32); b.scale.set(sx, sy, 0.08); return b; };
  for (const l of [-1, 1]) {
    const cima = asa('#b57bff', 1.1, 1.2); cima.rotation.z = l * -0.35; g.add(em(cima, l * 0.52, 0.72, 0));
    const baixo = asa('#ff6fae', 0.75, 0.85); baixo.rotation.z = l * 0.4; g.add(em(baixo, l * 0.38, 0.2, 0.02));
    for (const [x, y, r, cor] of [[0.6, 0.85, 0.13, '#ffd23f'], [0.4, 0.15, 0.09, '#ffffff'], [0.75, 0.55, 0.07, '#ffffff']]) {
      const p = bola(r, brilhante(cor), 16); p.scale.set(1, 1, 0.3); g.add(em(p, l * x, y, 0.05));
    }
    g.add(segmento([l * 0.05, 1.05, 0.05], [l * 0.25, 1.45, 0.05], 0.02, preto()));
    g.add(em(bola(0.06, preto(), 12), l * 0.26, 1.47, 0.05));
  }
  g.add(em(capsula(0.1, 0.7, plastico('#5a3df2', { rugosidade: 0.3, verniz: 0.8 })), 0, 0.5, 0.06));
  const cab = bola(0.15, plastico('#5a3df2', { rugosidade: 0.3, verniz: 0.8 }), 20); g.add(em(cab, 0, 1.02, 0.08));
  for (const l of [-1, 1]) g.add(em(olho(0.04), l * 0.06, 1.05, 0.2));
  g.rotation.set(-0.25, 0.25, 0.08);
  return g;
}

function pipa() {
  const g = new THREE.Group();
  const cores = ['#ef476f', '#ffc93c', '#4d96ff', '#3dbe6b'];
  const V = [[0, 1.0], [0.62, 0.3], [0, -0.75], [-0.62, 0.3]];
  for (let i = 0; i < 4; i++) {
    const s = new THREE.Shape(); s.moveTo(0, 0.3); s.lineTo(...V[i]); s.lineTo(...V[(i + 1) % 4]); s.closePath();
    g.add(malha(new THREE.ExtrudeGeometry(s, { depth: 0.04, bevelEnabled: true, bevelSize: 0.02, bevelThickness: 0.02 }), brilhante(cores[i], { rugosidade: 0.35, verniz: 0.6 })));
  }
  const vareta = plastico('#d9975a', { rugosidade: 0.5 });
  g.add(segmento([0, 1.0, 0.07], [0, -0.75, 0.07], 0.025, vareta));
  g.add(segmento([-0.62, 0.3, 0.07], [0.62, 0.3, 0.07], 0.025, vareta));
  // rabiola com lacinhos
  let p = [0, -0.75, 0.03];
  for (let k = 1; k <= 5; k++) {
    const q = [Math.sin(k * 1.1) * 0.22, -0.75 - k * 0.3, 0.03];
    g.add(segmento(p, q, 0.012, plastico('#ffffff')));
    const cor = brilhante(cores[k % 4]);
    for (const l of [-1, 1]) { const b = bola(0.07, cor, 12); b.scale.set(1.4, 0.7, 0.5); g.add(em(b, q[0] + l * 0.08, q[1], q[2])); }
    p = q;
  }
  g.rotation.z = 0.3;
  return g;
}

function escorregador() {
  const g = new THREE.Group();
  const azul = brilhante('#4d96ff'), am = brilhante('#ffc93c'), verm = brilhante('#ef476f');
  for (const z of [-0.35, 0.35]) {
    g.add(segmento([-0.9, 0, z], [-0.75, 1.6, z], 0.06, azul));
    g.add(segmento([-0.35, 0, z], [-0.35, 1.6, z], 0.06, azul));
  }
  for (let k = 1; k <= 4; k++) g.add(segmento([-0.88 + k * 0.03, k * 0.32, -0.35], [-0.88 + k * 0.03, k * 0.32, 0.35], 0.035, verm));
  g.add(em(caixa(0.65, 0.12, 0.85, 0.05, verm), -0.55, 1.6, 0));
  for (const z of [-0.38, 0.38]) g.add(segmento([-0.8, 1.6, z], [-0.8, 2.0, z], 0.04, azul), segmento([-0.3, 1.6, z], [-0.3, 2.0, z], 0.04, azul), segmento([-0.8, 2.0, z], [-0.3, 2.0, z], 0.04, azul));
  // a rampa (curva suave), com bordas
  const rampa = new THREE.Group();
  const n = 12;
  for (let i = 0; i < n; i++) {
    const t0 = i / n, t1 = (i + 1) / n;
    const f = (t) => [-0.25 + t * 1.75, 1.6 * (1 - t) * (1 - t * 0.35) + 0.12 * t, 0];
    const a = f(t0), b = f(t1);
    const seg = caixa(Math.hypot(b[0] - a[0], b[1] - a[1]) + 0.04, 0.08, 0.7, 0.03, am);
    seg.rotation.z = Math.atan2(b[1] - a[1], b[0] - a[0]);
    rampa.add(em(seg, (a[0] + b[0]) / 2, (a[1] + b[1]) / 2, 0));
    for (const z of [-0.37, 0.37]) { const bd = caixa(Math.hypot(b[0] - a[0], b[1] - a[1]) + 0.04, 0.16, 0.06, 0.03, am); bd.rotation.z = seg.rotation.z; rampa.add(em(bd, (a[0] + b[0]) / 2, (a[1] + b[1]) / 2 + 0.07, z)); }
  }
  g.add(rampa);
  g.add(segmento([0.4, 0, -0.3], [0.4, 0.75, -0.3], 0.05, azul), segmento([0.4, 0, 0.3], [0.4, 0.75, 0.3], 0.05, azul));
  g.rotation.y = -0.35;
  return g;
}

function flores() {
  const g = new THREE.Group();
  const mont = bola(0.6, massinha('#7a4a2a'), 32); mont.scale.set(1, 0.25, 0.7); g.add(em(mont, 0, 0, 0));
  const folha = plastico('#3fae4a', { rugosidade: 0.45, verniz: 0.4 });
  // tulipas
  const tulipa = (cor, alt, x, z, r) => {
    const t = new THREE.Group();
    t.add(em(cilindro(0.03, 0.035, alt, folha, 8), 0, alt / 2, 0));
    const m = brilhante(cor, { rugosidade: 0.3, verniz: 0.7 });
    const c = bola(0.17, m, 24); c.scale.set(1, 1.25, 1); t.add(em(c, 0, alt + 0.1, 0));
    for (let k = 0; k < 3; k++) { const p = bola(0.1, m, 12); p.scale.set(0.8, 1.6, 0.5); const a = (k / 3) * Math.PI * 2; p.position.set(Math.cos(a) * 0.1, alt + 0.26, Math.sin(a) * 0.1); p.lookAt(0, alt + 0.6, 0); t.add(p); }
    const f = bola(0.12, folha, 12); f.scale.set(0.5, 1.8, 0.25); f.rotation.z = 0.5; t.add(em(f, 0.12, alt * 0.35, 0));
    t.rotation.z = r;
    return em(t, x, 0.05, z);
  };
  g.add(tulipa('#ef476f', 0.75, -0.3, 0.05, 0.12), tulipa('#ffc93c', 0.95, 0.02, -0.1, -0.04), tulipa('#ff6fae', 0.7, 0.32, 0.1, -0.15));
  g.add(em(flor('#b57bff', 1.6), -0.5, 0.0, 0.25), em(flor('#ffffff', 1.4), 0.52, 0.0, 0.25));
  return g;
}

function arvore_() {
  return arvore({ s: 1 });
}

// ---------------------------------------------------------------------------------------------
// Céu

function sol() {
  const g = new THREE.Group();
  const ouro = plastico('#ffc21a', { rugosidade: 0.25, verniz: 1, emissivo: '#ff9d00', ambiente: 0.4 });
  g.add(bola(0.8, ouro, 48));
  const raio = plastico('#ffb000', { rugosidade: 0.25, verniz: 1, emissivo: '#ff8a00', ambiente: 0.4 });
  for (let i = 0; i < 10; i++) {
    const a = (i / 10) * Math.PI * 2;
    const c = capsula(0.11, i % 2 ? 0.26 : 0.38, raio);
    c.rotation.z = a - Math.PI / 2;
    g.add(em(c, Math.cos(a) * 1.12, Math.sin(a) * 1.12, 0));
  }
  g.add(em(rostinho(0.8), 0, -0.05, 0.74));
  return g;
}

function lua() {
  const g = new THREE.Group();
  const r1 = 1, d = 0.5, r2 = 0.85;
  const xi = (r1 * r1 - r2 * r2 + d * d) / (2 * d), yi = Math.sqrt(r1 * r1 - xi * xi);
  const t1 = Math.atan2(yi, xi), t2 = Math.atan2(-yi, xi - d);
  const s = new THREE.Shape();
  s.absarc(0, 0, r1, t1, Math.PI * 2 - t1, false);
  s.absarc(d, 0, r2, t2, -t2, true);
  const geo = new THREE.ExtrudeGeometry(s, { depth: 0.3, bevelEnabled: true, bevelThickness: 0.15, bevelSize: 0.12, bevelSegments: 8, curveSegments: 48 });
  geo.center();
  const m = plastico('#ffe27a', { rugosidade: 0.3, verniz: 0.9, emissivo: '#ffcf4a', ambiente: 0.4 });
  const lua = malha(geo, m);
  g.add(lua);
  // olhinho fechado de sono e bochecha
  const fechado = new THREE.TorusGeometry(0.1, 0.025, 8, 20, Math.PI); fechado.rotateZ(Math.PI);
  g.add(em(malha(fechado, preto()), -0.42, 0.12, 0.32));
  g.add(em(bochecha(0.1), -0.5, -0.12, 0.3));
  g.add(em(sorriso(0.07, 0.018, 2.0), -0.33, -0.1, 0.32));
  // estrelinhas
  const est = plastico('#fff2b0', { rugosidade: 0.3, verniz: 0.6, emissivo: '#ffe27a' });
  g.add(em(estrela3D(0.18, 0.06, est), 0.75, 0.55, 0), em(estrela3D(0.12, 0.05, est), 0.55, -0.7, 0));
  g.rotation.z = -0.25;
  return g;
}

/** Nuvem fofinha: várias bolas brancas juntas. [forma] escolhe o desenho. */
export function nuvemFofa(forma = 0, cor = '#ffffff', brilho = '#e6f2ff') {
  const g = new THREE.Group();
  const m = plastico(cor, { rugosidade: 0.85, verniz: 0.05, emissivo: brilho, ambiente: 0.55 });
  const formas = [
    [[0, 0.1, 0.62], [-0.65, -0.08, 0.45], [0.7, -0.06, 0.5], [0.28, 0.42, 0.45], [-0.3, 0.35, 0.4], [1.15, -0.2, 0.3], [-1.05, -0.2, 0.3]],
    [[0, 0, 0.55], [-0.55, -0.1, 0.42], [0.55, -0.08, 0.45], [0.05, 0.38, 0.4]],
    [[0, 0, 0.5], [-0.6, -0.12, 0.38], [0.62, -0.1, 0.42], [-0.15, 0.35, 0.36], [0.35, 0.3, 0.32], [1.05, -0.2, 0.26]],
  ][forma];
  for (const [x, y, r] of formas) g.add(em(bola(r, m, 32), x, y, 0));
  const base = capsula(0.36, 1.6, m); base.rotation.z = Math.PI / 2; base.scale.set(1, 1, 0.8); g.add(em(base, 0, -0.22, 0));
  g.scale.set(1, 0.85, 0.6);
  return g;
}

function nuvem_() {
  const g = nuvemFofa(0);
  g.add(em(rostinho(0.55), 0, -0.06, 0.38));
  return g;
}

function arcoiris() {
  const g = new THREE.Group();
  const cores = ['#ef476f', '#ff8a3d', '#ffc93c', '#3dbe6b', '#4d96ff', '#9b6bff'];
  cores.forEach((c, i) => {
    const r = 1.5 - i * 0.17;
    g.add(malha(new THREE.TorusGeometry(r, 0.09, 16, 64, Math.PI), brilhante(c, { rugosidade: 0.3, verniz: 0.8 })));
  });
  for (const l of [-1, 1]) {
    const n = nuvemFofa(1);
    n.scale.multiplyScalar(0.62);
    g.add(em(n, l * 1.18, 0.02, 0.25));
  }
  return g;
}

function estrela() {
  const g = new THREE.Group();
  const e = estrela3D(1.0, 0.36, plastico('#ffc21a', { rugosidade: 0.18, verniz: 1, emissivo: '#ff9d00', ambiente: 0.55 }), 5, { segmentos: 8 });
  g.add(e);
  g.add(em(rostinho(0.62), 0, -0.05, 0.43));
  g.rotation.set(0.12, -0.18, 0.05);
  return g;
}

// ---------------------------------------------------------------------------------------------
// Estruturas como peças soltas (prêmio e baú)

const { estruturaDasParedes, fundoDoComodo, baseDaCasa, janela, porta, escada, chamine, telhado, cerca, pedrinha, lago } = pecas;

function gramado(r = 1.2) {
  const g = new THREE.Group();
  g.add(em(cilindro(r, r * 0.92, 0.45, massinha('#b9814b'), 48), 0, 0.22, 0));
  g.add(em(cilindro(r + 0.04, r + 0.04, 0.16, plastico('#7fd05c', { rugosidade: 0.72, verniz: 0.08 }), 48), 0, 0.5, 0));
  return g;
}

function terreno() {
  const g = gramado(1.2);
  const broto = plastico('#3fae4a', { rugosidade: 0.4, verniz: 0.5 });
  g.add(segmento([0, 0.55, 0], [0, 0.95, 0], 0.04, broto));
  for (const l of [-1, 1]) { const f = bola(0.18, broto, 16); f.scale.set(1.4, 0.45, 0.7); f.rotation.z = l * 0.4; g.add(em(f, l * 0.2, 1.0, 0)); }
  g.add(em(flor('#ff6fae', 1.4), -0.7, 0.55, 0.3), em(flor('#ffd23f', 1.2), 0.65, 0.55, 0.35), em(flor('#b57bff', 1.1), 0.5, 0.55, -0.5));
  return g;
}

/** A casa sem telhado, pequena (paredes, pisos e fundo dos cômodos nas cores de sempre). */
function paredes() {
  const g = new THREE.Group();
  g.add(baseDaCasa().children[0]);
  g.add(estruturaDasParedes());
  const cores = { quarto: '#ffe3ec', banheiro: '#d7f3ff', sala: '#fff1c9', cozinha: '#e2f7d5' };
  for (const [nome, cor] of Object.entries(cores)) {
    const f = fundoDoComodo(nome);
    f.material = plastico(cor, { rugosidade: 0.8, verniz: 0.02, ambiente: 0.45 });
    g.add(f);
  }
  g.position.y = 0.45;
  return g;
}

function telhado_() {
  const g = telhado('#ef476f');
  g.position.y = -5;
  return grupo(g);
}

function porta_() {
  return grupo(em(porta('#a0522d'), 0, 0, 0));
}

function janelas() {
  return grupo(em(janela(0.9, 0.9), 0, 0.6, 0));
}

function escada_() {
  return grupo(em(escada(), -0.6, 0.15, 0));
}

function chamine_() {
  const g = grupo(chamine());
  const f = plastico('#ffffff', { rugosidade: 0.9, emissivo: '#eef6ff' });
  g.add(em(bola(0.22, f, 16), 0.15, 2.2, 0), em(bola(0.3, f, 16), 0.45, 2.6, 0), em(bola(0.36, f, 16), 0.85, 3.05, 0));
  return g;
}

function cerca_() {
  const g = grupo(cerca(5));
  g.add(em(flor('#ff6fae', 1.6), -0.6, 0, 0.35), em(flor('#ffd23f', 1.3), 0.7, 0, 0.3));
  g.rotation.y = -0.2;
  return g;
}

function caminho() {
  const g = new THREE.Group();
  const gr = bola(1.3, plastico('#7fd05c', { rugosidade: 0.72, verniz: 0.08 }), 48); gr.scale.set(1, 0.12, 0.85); g.add(gr);
  for (const [x, z, r] of [[-0.15, -0.65, 0.3], [0.2, -0.15, 0.34], [-0.15, 0.35, 0.36], [0.15, 0.82, 0.3]]) {
    const p = pedrinha(r, r * 0.7); g.add(em(p, x, 0.16, z));
  }
  return g;
}

function lago_() {
  const g = grupo(lago(1.25, 0.8));
  const gr = bola(1.6, plastico('#7fd05c', { rugosidade: 0.72, verniz: 0.08 }), 48); gr.scale.set(1, 0.06, 0.75); g.add(em(gr, 0, -0.04, 0));
  return g;
}

// ---------------------------------------------------------------------------------------------
// Catálogo dos itens

/**
 * [id do domínio, montar, uso em dp (largura, altura), sombra de contato].
 * A sombra segue o app (CenaDaCasa.temSombraDeContato): só o que pisa no chão, sem os objetos de parede.
 */
export const ITENS = [
  ['terreno', terreno, 160, 160, true],
  ['paredes', paredes, 160, 160, true],
  ['telhado', telhado_, 160, 160, false],
  ['porta', porta_, 120, 160, false],
  ['cama', cama, 200, 200, true],
  ['janelas', janelas, 140, 140, false],
  ['sofa', sofa, 200, 200, true],
  ['sol', sol, 200, 200, false],
  ['escada', escada_, 160, 160, true],
  ['arvore', arvore_, 200, 200, true],
  ['geladeira', geladeira, 96, 200, true],
  ['ursinho', ursinho, 140, 140, false],
  ['fogao', fogao, 120, 160, true],
  ['cachorro', cachorro, 160, 160, true],
  ['tv', tv, 160, 160, true],
  ['mesa', mesa, 180, 126, true],
  ['flores', flores, 140, 140, true],
  ['banheira', banheira, 200, 200, true],
  ['chamine', chamine_, 120, 160, false],
  ['abajur', abajur, 120, 140, false],
  ['gato', gato, 140, 140, true],
  ['quadro', quadro, 140, 140, false],
  ['planta', planta, 140, 140, true],
  ['cerca', cerca_, 180, 140, true],
  ['nuvem', nuvem_, 200, 160, false],
  ['livros', livros, 140, 140, false],
  ['bola', bola_, 120, 120, true],
  ['espelho', espelho, 120, 160, false],
  ['caminho', caminho, 160, 140, false],
  ['bicicleta', bicicleta, 180, 160, true],
  ['violao', violao, 100, 180, true],
  ['bolo', bolo, 120, 120, false],
  ['lago', lago_, 200, 140, false],
  ['pato', () => grupo(patinho(1, '#ffc400').rotateY(-0.6)), 120, 120, false],
  ['trem', trem, 160, 140, true],
  ['borboleta', borboleta, 140, 140, false],
  ['pipa', pipa, 120, 180, false],
  ['escorregador', escorregador, 180, 160, true],
  ['arcoiris', arcoiris, 200, 140, false],
  ['estrela', estrela, 140, 140, false],
];

/** O sol vira lua de noite (CenaDaCasa: noite && item.id == "sol"). */
export const LUA = lua;

/** Peças e modelos reaproveitados pelas figuras das perguntas (Fase 4, figuras.js). */
export const pecasDosItens = { segmento, olho, sorriso, bochecha, rostinho, brilhante, fosco, preto, branco, luz };
export const modelosDosItens = { gato, cachorro, ursinho, bola: bola_, trem, borboleta, pipa, bolo, lua, estrela, escorregador };
/** Mais modelos reaproveitados pelo lote de figuras da Fase 9 (figurasDoLote9a.js). */
export const maisModelosDosItens = { cama, livros, violao, bicicleta, arvore: arvore_, sol, nuvem: nuvem_, arcoiris, planta };
