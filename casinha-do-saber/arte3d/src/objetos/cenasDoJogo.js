// Cenário do jogo (PRD visual 3D, Fase 4), como na maquete 2_jogo: a ilhinha de grama de cada caminho e as
// cenas das perguntas (lagoa, mesa, varal, cercadinho, tapete), em camadas na mesma câmera ('jogo' em
// cameras.js). O app empilha: céu em degradê, ilha do caminho, cena da pergunta, figuras (fig_*) nas vagas,
// crianças. Ids, vagas e ordem estão em docs/visual-3d/relatorios/fase4a.md.
//
// Mundo: a grama da ilha é o plano y = GRAMA, centro na origem, raio RAIO. As vagas (âncoras vaga_1..n) são
// os pontos onde o app põe o pé (apoio) de cada figura; as âncoras area_* marcam a borda da superfície útil
// (para quando houver mais figuras que vagas).
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em, ancora } from '../kit.js';
import { arvore } from './arvore.js';
import { arbusto } from './arbusto.js';
import { flor } from './flor.js';
import { icones } from './icones.js';
import { camadaDaCena } from './cenaDaCasa.js';

export const RAIO = 6.6;
export const GRAMA = 0.14;

/** Cores dos caminhos (dominio/Trilha: corArgb), para as flores e enfeites da ilha. */
export const CORES_DOS_CAMINHOS = {
  matematica: '#ff9f1c',
  portugues: '#2ec4b6',
  ingles: '#6c63ff',
  logica: '#ef476f',
};

const brilhante = (cor, o = {}) => plastico(cor, { rugosidade: 0.28, verniz: 0.8, ...o });
const madeira = () => plastico('#d9975a', { rugosidade: 0.5, verniz: 0.35 });
const madeiraClara = () => plastico('#efbf86', { rugosidade: 0.5, verniz: 0.35 });
const pedra = (k) => plastico(['#e3d7c2', '#d3c4aa', '#ece3d2'][k % 3], { rugosidade: 0.8, verniz: 0.05 });

/** Gerador pseudoaleatório fixo (as camadas saem iguais a cada render). */
function sorteio(semente) {
  let s = semente >>> 0;
  return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; };
}

/** Escala [obj] para ter [altura] e põe a base em y = 0. */
function naAltura(obj, altura) {
  const w = new THREE.Group();
  w.add(obj);
  const c = new THREE.Box3().setFromObject(w);
  const k = altura / (c.max.y - c.min.y);
  obj.scale.multiplyScalar(k);
  obj.position.y -= c.min.y * k;
  return w;
}

/** Cápsula de [a] até [b]. */
function segmento(a, b, r, mat) {
  const va = new THREE.Vector3(...a), vb = new THREE.Vector3(...b);
  const d = vb.clone().sub(va);
  const m = capsula(r, Math.max(0.001, d.length()), mat);
  m.position.copy(va).add(vb).multiplyScalar(0.5);
  m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), d.normalize());
  return m;
}

/** Marca as vagas e a área útil numa cena. */
function vagas(g, pontos, area) {
  pontos.forEach(([x, y, z], i) => ancora(g, 'vaga_' + (i + 1), x, y, z));
  if (area) {
    const [cx, y, cz, rx, rz] = area;
    ancora(g, 'area_esquerda', cx - rx, y, cz);
    ancora(g, 'area_direita', cx + rx, y, cz);
    ancora(g, 'area_fundo', cx, y, cz - rz);
    ancora(g, 'area_frente', cx, y, cz + rz);
  }
}

// ---------------------------------------------------------------------------------------------
// A ilha de cada caminho

/** Disco da grama (o mesmo da ilha): nas cenas, só capta a sombra e esconde o que fica abaixo dela. */
const discoDaGrama = () => em(cilindro(RAIO + 0.1, RAIO + 0.1, 0.28, plastico('#7ccf5b'), 96), 0, 0, 0);

function ilhaBase() {
  const g = new THREE.Group();
  const verde = plastico('#7ccf5b', { rugosidade: 0.72, verniz: 0.08 });
  g.add(em(cilindro(RAIO, RAIO * 0.9, 1.1, massinha('#b8834f'), 96), 0, -0.55, 0));
  // faixa de terra mais escura embaixo, como um bolo de massinha
  g.add(em(cilindro(RAIO * 0.9, RAIO * 0.78, 0.5, massinha('#9a6a3f'), 96), 0, -1.3, 0));
  g.add(em(cilindro(RAIO + 0.05, RAIO + 0.05, 0.28, verde, 96), 0, 0, 0));
  const borda = malha(new THREE.TorusGeometry(RAIO, 0.14, 16, 128), verde);
  borda.rotation.x = Math.PI / 2;
  g.add(em(borda, 0, 0, 0));
  // tufinhos de grama mais escura espalhados
  const r = sorteio(7);
  const tufo = plastico('#5cb84a', { rugosidade: 0.7, verniz: 0.1 });
  for (let k = 0; k < 26; k++) {
    const a = r() * Math.PI * 2, d = 4.6 + r() * 1.7;
    const t = bola(0.16 + r() * 0.08, tufo, 16);
    t.scale.set(1.3, 0.45, 1);
    g.add(em(t, Math.cos(a) * d, GRAMA, Math.sin(a) * d));
  }
  return g;
}

/** Enfeite do caminho nos fundos da ilha, à direita (as crianças ficam à esquerda): o ícone 3D do caminho. */
function enfeite(caminho) {
  const g = new THREE.Group();
  if (caminho === 'matematica') {
    g.add(em(naAltura(icones.numeros(), 1.7), 0, 0, 0));
    // contas de ábaco soltas
    const cores = ['#ff5a5f', '#3d8bff', '#ffc21a', '#36c26a'];
    cores.forEach((c, i) => g.add(em(bola(0.2, brilhante(c), 24), -1.2 + i * 0.32, 0.2, 1.0 + (i % 2) * 0.2)));
  } else if (caminho === 'portugues') {
    g.add(em(naAltura(icones.letras(), 1.7), 0, 0, 0));
    // pilha de livrinhos
    const livros = new THREE.Group();
    [['#ef476f', 0.0], ['#4d96ff', 0.2], ['#ffc93c', -0.15]].forEach(([c, giro], i) => {
      const l = new THREE.Group();
      l.add(caixa(1.0, 0.22, 0.7, 0.06, brilhante(c)));
      l.add(em(caixa(0.95, 0.16, 0.66, 0.03, plastico('#fffaf0', { rugosidade: 0.6 })), 0.04, 0, 0));
      l.rotation.y = giro;
      livros.add(em(l, 0, 0.11 + i * 0.23, 0));
    });
    g.add(em(livros, -1.4, 0, 0.9));
  } else if (caminho === 'ingles') {
    g.add(em(naAltura(icones.ingles(), 1.9), 0, 0, 0));
    // bandeirinhas coloridas num varalzinho entre duas estacas
    const cores = ['#ef476f', '#ffc93c', '#4d96ff', '#3dbe6b', '#9b6bff'];
    const a = [-2.3, 1.15, 0.7], b = [-0.7, 1.15, 1.4];
    for (const p of [a, b]) g.add(segmento([p[0], 0, p[2]], p, 0.06, madeira()));
    g.add(segmento(a, b, 0.015, plastico('#ffffff')));
    for (let k = 0; k < 5; k++) {
      const t = (k + 0.5) / 5;
      const x = a[0] + (b[0] - a[0]) * t, z = a[2] + (b[2] - a[2]) * t, y = 1.15 - Math.sin(t * Math.PI) * 0.12;
      const s = new THREE.Shape(); s.moveTo(-0.15, 0); s.lineTo(0.15, 0); s.lineTo(0, -0.32); s.closePath();
      const f = malha(new THREE.ExtrudeGeometry(s, { depth: 0.02, bevelEnabled: true, bevelSize: 0.02, bevelThickness: 0.02, bevelSegments: 2 }), brilhante(cores[k]));
      f.rotation.y = -Math.atan2(b[2] - a[2], b[0] - a[0]);
      g.add(em(f, x, y, z));
    }
  } else {
    g.add(em(naAltura(icones.logica(), 1.8), 0, 0, 0));
    // pecinhas de quebra-cabeça e um cubo de brinquedo
    g.add(em(caixa(0.5, 0.5, 0.5, 0.1, brilhante('#36c26a')), -1.3, 0.25, 0.8).rotateY(0.4));
    g.add(em(caixa(0.4, 0.4, 0.4, 0.08, brilhante('#ffc21a')), -0.8, 0.2, 1.4).rotateY(-0.3));
  }
  return g;
}

/** A ilha do caminho: grama, terra, árvores, arbustos, flores na cor do caminho e o enfeite do caminho. */
export function ilhaDoCaminho(caminho) {
  const cor = CORES_DOS_CAMINHOS[caminho];
  const g = ilhaBase();
  for (const [x, z, s] of [[-4.6, -3.4, 1.15], [4.5, -3.0, 0.95], [-1.9, -5.2, 0.85], [5.6, 0.3, 0.7]]) {
    g.add(em(arvore({ s }), x, GRAMA, z));
  }
  g.add(em(arbusto('#4fae3e', 1.2), -5.5, GRAMA, 1.4));
  g.add(em(arbusto('#5bb94a', 1.0), 5.2, GRAMA, 2.6));
  g.add(em(arbusto('#55b444', 0.9), 1.0, GRAMA, -5.6));
  const coresDasFlores = [cor, '#ffd23f', '#ffffff', cor, '#ff8fc0'];
  [[-4.6, 3.8], [-3.2, 5.0], [4.2, 3.9], [3.0, 5.4], [-5.9, -1.0], [6.0, -1.6], [-1.4, 5.9], [1.3, 6.0], [5.3, 1.6], [-5.4, 2.6]]
    .forEach(([x, z], i) => g.add(em(flor(coresDasFlores[i % 5], 1.6), x, GRAMA, z)));
  g.add(em(enfeite(caminho), 2.0, GRAMA, -4.4));
  // onde as crianças ficam (fundos, à esquerda), o centro e vagas na grama e no céu (figuras soltas)
  ancora(g, 'personagens', -3.2, GRAMA, -2.6);
  ancora(g, 'centro', 0, GRAMA, 0);
  vagas(g, [[-2.0, GRAMA, -0.6], [0, GRAMA, -1.0], [2.0, GRAMA, -0.6], [-1.6, GRAMA, 1.6], [0.4, GRAMA, 1.8], [2.4, GRAMA, 1.4]], [0, GRAMA, 0.4, 3.6, 3.2]);
  for (let k = 0; k < 4; k++) ancora(g, 'ceu_' + (k + 1), -2.7 + k * 1.8, 2.6 + (k % 2) * 0.6, -1.2);
  return g;
}

// ---------------------------------------------------------------------------------------------
// Cenas das perguntas

/** Lagoa com pedras em volta, vitórias-régias e taboas (maquete 2_jogo). As vagas ficam na água. */
function lagoa() {
  const g = new THREE.Group();
  const R = 3.75, yAgua = 0.24;
  // fundo e água: a água é um disco liso brilhante, logo acima da grama
  g.add(em(cilindro(R + 0.15, R + 0.15, 0.5, plastico('#e6dcc8', { rugosidade: 0.85, verniz: 0 }), 96), 0, -0.04, 0));
  const agua = cilindro(R - 0.1, R - 0.1, 0.1, new THREE.MeshPhysicalMaterial({ color: '#1488ee', roughness: 0.08, clearcoat: 1, clearcoatRoughness: 0.05, envMapIntensity: 1.0 }), 96);
  agua.receiveShadow = true;
  g.add(em(agua, 0, yAgua - 0.05, 0));
  // brilho de água mais clara perto da borda
  const anel = malha(new THREE.RingGeometry(R - 0.75, R - 0.1, 96), new THREE.MeshPhysicalMaterial({ color: '#8fd0ff', roughness: 0.1, clearcoat: 1, transparent: true, opacity: 0.35, envMapIntensity: 0.8 }), { sombra: false });
  anel.rotation.x = -Math.PI / 2;
  g.add(em(anel, 0, yAgua + 0.005, 0));
  // pedras da borda (achatadas, de tamanhos variados)
  const r = sorteio(3);
  const n = 24;
  for (let k = 0; k < n; k++) {
    const a = (k / n) * Math.PI * 2 + r() * 0.05;
    const s = 0.46 + r() * 0.16;
    const p = bola(s, pedra(k), 28);
    p.scale.set(1.05, 0.5, 0.92);
    p.rotation.y = a;
    g.add(em(p, Math.cos(a) * (R + 0.2), 0.2, Math.sin(a) * (R + 0.2)));
  }
  // vitórias-régias (com o cortezinho) e uma florzinha rosa
  const folha = plastico('#3fae4a', { rugosidade: 0.45, verniz: 0.5 });
  for (const [x, z, rr, giro] of [[-2.5, 1.1, 0.6, 0.4], [2.6, -1.2, 0.5, 2.0], [0.4, -2.7, 0.42, 4.0], [2.2, 2.0, 0.38, 1.0]]) {
    const geo = new THREE.CylinderGeometry(rr, rr, 0.05, 40, 1, false, 0.25, Math.PI * 2 - 0.5);
    const f = malha(geo, folha);
    f.rotation.y = giro;
    g.add(em(f, x, yAgua + 0.03, z));
    if (rr > 0.55) g.add(em(flor('#ff7fb0', 1.3), x + 0.1, yAgua - 0.14, z));
  }
  // taboas nos fundos, à direita
  const caule = plastico('#4a9d3a', { rugosidade: 0.6, verniz: 0.1 });
  const taboa = massinha('#8a5a34');
  for (const [x, z, h] of [[3.2, -2.2, 1.5], [3.55, -1.8, 1.2], [2.9, -2.6, 1.0]]) {
    g.add(em(cilindro(0.035, 0.04, h, caule, 8), x, h / 2, z));
    g.add(em(capsula(0.09, 0.32, taboa), x, h + 0.05, z));
    const fl = capsula(0.04, 0.6, caule); fl.rotation.z = 0.5; g.add(em(fl, x - 0.15, h * 0.45, z + 0.1));
  }
  vagas(g, [[-1.5, yAgua, -0.7], [0.4, yAgua, -1.5], [1.7, yAgua, 0.0], [-0.7, yAgua, 1.0], [0.9, yAgua, 1.6], [-2.4, yAgua, -0.1]], [0, yAgua, 0, 2.9, 2.6]);
  return g;
}

/** Textura xadrez da toalha de piquenique. */
function texturaXadrez(cor = '#ef476f') {
  const cv = document.createElement('canvas');
  cv.width = cv.height = 512;
  const c = cv.getContext('2d');
  c.fillStyle = '#ffffff'; c.fillRect(0, 0, 512, 512);
  const n = 12, t = 512 / n;
  c.globalAlpha = 0.55; c.fillStyle = cor;
  for (let i = 0; i < n; i += 2) { c.fillRect(i * t, 0, t, 512); c.fillRect(0, i * t, 512, t); }
  c.globalAlpha = 1;
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  tex.anisotropy = 8;
  return tex;
}

/** Mesa redonda com toalha xadrez (comidas). As vagas ficam no tampo. */
function mesa() {
  const g = new THREE.Group();
  const R = 3.3, yTampo = 1.15;
  const xadrez = new THREE.MeshPhysicalMaterial({ map: texturaXadrez(), roughness: 0.6, clearcoat: 0.15, envMapIntensity: 0.5 });
  // pernas e o pé de madeira
  for (const [x, z] of [[-2.2, 1.6], [2.2, 1.6], [-2.0, -1.8], [2.0, -1.8]]) g.add(em(capsula(0.16, yTampo - 0.3, madeira()), x, (yTampo - 0.1) / 2, z));
  // tampo com a toalha e a barra caída de babado
  g.add(em(cilindro(R, R, 0.12, xadrez, 96), 0, yTampo, 0));
  const barra = new THREE.CylinderGeometry(R + 0.06, R + 0.22, 0.42, 96, 1, true);
  const pos = barra.attributes.position;
  for (let i = 0; i < pos.count; i++) {
    if (pos.getY(i) < 0) {
      const a = Math.atan2(pos.getZ(i), pos.getX(i));
      const k = 1 + 0.035 * Math.cos(a * 18);
      pos.setX(i, pos.getX(i) * k); pos.setZ(i, pos.getZ(i) * k);
      pos.setY(i, pos.getY(i) - 0.05 * (1 + Math.cos(a * 18)));
    }
  }
  barra.computeVertexNormals();
  const matBarra = new THREE.MeshPhysicalMaterial({ map: texturaXadrez(), roughness: 0.6, clearcoat: 0.1, side: THREE.DoubleSide, envMapIntensity: 0.5 });
  g.add(em(malha(barra, matBarra), 0, yTampo - 0.24, 0));
  // pratinho e jarra de suco nos fundos
  g.add(em(cilindro(0.5, 0.42, 0.06, plastico('#ffffff', { rugosidade: 0.2, verniz: 0.9 }), 40), -2.1, yTampo + 0.09, -1.5));
  const jarra = new THREE.Group();
  jarra.add(em(cilindro(0.3, 0.34, 0.8, plastico('#bfe6ff', { rugosidade: 0.05, verniz: 1, opacidade: 0.55 }), 32), 0, 0.4, 0));
  jarra.add(em(cilindro(0.27, 0.31, 0.6, brilhante('#ffa62b'), 32), 0, 0.3, 0));
  const alca = malha(new THREE.TorusGeometry(0.2, 0.05, 10, 24, Math.PI), plastico('#bfe6ff', { rugosidade: 0.05, verniz: 1, opacidade: 0.7 }));
  alca.rotation.z = -Math.PI / 2;
  jarra.add(em(alca, 0.32, 0.45, 0));
  g.add(em(jarra, 2.3, yTampo + 0.06, -1.4));
  vagas(g, [[-1.4, yTampo + 0.06, -0.5], [0.2, yTampo + 0.06, -1.0], [1.6, yTampo + 0.06, -0.3], [-0.8, yTampo + 0.06, 1.1], [0.9, yTampo + 0.06, 1.2], [-2.3, yTampo + 0.06, 0.6]], [0, yTampo + 0.06, 0, 2.6, 2.4]);
  return g;
}

/** Varal: duas estacas, a corda com pregadores e o cesto de roupa. As vagas ficam na corda (onde o app
 *  prende a âncora 'topo' das roupas). */
function varal() {
  const g = new THREE.Group();
  const z = 0.2, xa = 3.3, h = 2.9;
  for (const s of [-1, 1]) {
    g.add(em(cilindro(0.13, 0.16, h, madeira(), 20), s * xa, h / 2, z));
    g.add(em(caixa(0.9, 0.18, 0.18, 0.06, madeira()), s * xa, h - 0.2, z));
    g.add(em(bola(0.17, brilhante('#ef476f'), 20), s * xa, h + 0.05, z));
    // base de pedrinhas
    g.add(em(cilindro(0.35, 0.45, 0.2, pedra(0), 24), s * xa, 0.12, z));
  }
  // corda com barriga (catenária aproximada por parábola)
  const yCorda = h - 0.3, barriga = 0.35;
  const pts = [];
  for (let i = 0; i <= 40; i++) {
    const t = i / 40, x = -xa + 2 * xa * t;
    pts.push(new THREE.Vector3(x, yCorda - barriga * 4 * t * (1 - t), z));
  }
  const corda = new THREE.CatmullRomCurve3(pts);
  g.add(malha(new THREE.TubeGeometry(corda, 80, 0.035, 8), plastico('#ffffff', { rugosidade: 0.5, verniz: 0.2 })));
  // pregadores coloridos nas vagas
  const cores = ['#ff5a5f', '#ffc21a', '#3d8bff', '#36c26a', '#9a62ff'];
  const posVagas = [];
  for (let k = 0; k < 5; k++) {
    const t = 0.14 + k * 0.18;
    const p = corda.getPoint(t);
    posVagas.push([p.x, p.y, p.z + 0.02]);
    for (const dx of [-0.32, 0.32]) {
      const q = corda.getPoint(t + dx / (2 * xa));
      const pr = caixa(0.09, 0.32, 0.12, 0.04, brilhante(cores[k]));
      g.add(em(pr, q.x, q.y - 0.02, q.z + 0.06));
    }
  }
  // cesto de roupa (vime) na frente, à esquerda
  const cesto = new THREE.Group();
  const vime = plastico('#e2b36f', { rugosidade: 0.6, verniz: 0.2 });
  cesto.add(em(cilindro(0.75, 0.6, 0.7, vime, 40), 0, 0.35, 0));
  for (let k = 0; k < 4; k++) cesto.add(em(malha(new THREE.TorusGeometry(0.62 + k * 0.04, 0.03, 8, 40), plastico('#c9934f', { rugosidade: 0.6 })).rotateX(Math.PI / 2), 0, 0.12 + k * 0.17, 0));
  cesto.add(em(malha(new THREE.TorusGeometry(0.76, 0.07, 10, 40), plastico('#c9934f', { rugosidade: 0.6 })).rotateX(Math.PI / 2), 0, 0.7, 0));
  // roupas dobradas aparecendo
  cesto.add(em(caixa(0.8, 0.22, 0.6, 0.1, plastico('#8fd3ff', { rugosidade: 0.7 })), -0.05, 0.72, 0.0).rotateZ(0.1));
  cesto.add(em(caixa(0.6, 0.2, 0.5, 0.1, plastico('#ffb3c9', { rugosidade: 0.7 })), 0.1, 0.86, 0.05).rotateZ(-0.15));
  g.add(em(cesto, 3.3, GRAMA, 3.2));
  vagas(g, posVagas, null);
  // as roupas penduradas descem até cerca de 1,6 abaixo da corda
  return g;
}

/** Cercadinho da fazenda: cerca de madeira nos fundos, fardo de feno e cocho. As vagas
 *  ficam na grama. */
function cercado() {
  const g = new THREE.Group();
  const R = 4.3;
  const tabua = plastico('#c98b52', { rugosidade: 0.55, verniz: 0.3 });
  const a0 = Math.PI * 1.08, a1 = Math.PI * 1.92, nPostes = 9;
  const ponto = (a, r = R) => [Math.cos(a) * r, Math.sin(a) * r];
  for (let k = 0; k < nPostes; k++) {
    const a = a0 + ((a1 - a0) * k) / (nPostes - 1);
    const [x, z] = ponto(a);
    g.add(em(caixa(0.26, 1.35, 0.26, 0.07, tabua), x, 0.68, z));
    g.add(em(bola(0.15, tabua, 16), x, 1.38, z));
  }
  for (const y of [0.55, 1.05]) {
    for (let k = 0; k < nPostes - 1; k++) {
      const aa = a0 + ((a1 - a0) * k) / (nPostes - 1), ab = a0 + ((a1 - a0) * (k + 1)) / (nPostes - 1);
      const [x1, z1] = ponto(aa), [x2, z2] = ponto(ab);
      g.add(segmento([x1, y, z1], [x2, y, z2], 0.08, tabua));
    }
  }
  // fardo de feno (à esquerda, para dentro) e cocho (à direita)
  const feno = new THREE.Group();
  const palha = plastico('#f2c94c', { rugosidade: 0.75, verniz: 0.05 });
  feno.add(caixa(1.2, 0.7, 0.75, 0.15, palha));
  for (const x of [-0.3, 0.3]) feno.add(em(caixa(0.06, 0.74, 0.79, 0.03, plastico('#c0392b', { rugosidade: 0.6 })), x, 0, 0));
  feno.rotation.y = 0.4;
  g.add(em(feno, -3.3, GRAMA + 0.35, -0.6));
  const cocho = new THREE.Group();
  cocho.add(em(caixa(1.3, 0.4, 0.6, 0.1, tabua), 0, 0.35, 0));
  cocho.add(em(caixa(1.15, 0.1, 0.45, 0.04, plastico('#5aa9e6', { rugosidade: 0.1, verniz: 1 })), 0, 0.52, 0));
  for (const x of [-0.5, 0.5]) cocho.add(em(caixa(0.12, 0.3, 0.5, 0.04, tabua), x, 0.1, 0));
  cocho.rotation.y = -0.5;
  g.add(em(cocho, 3.3, GRAMA, -0.4));
  const feno2 = feno.clone(); feno2.rotation.y = -0.3; g.add(em(feno2, -4.2, GRAMA + 0.35, 0.6));
  vagas(g, [[-1.6, GRAMA, -1.2], [0.3, GRAMA, -1.8], [2.0, GRAMA, -1.0], [-1.0, GRAMA, 1.0], [1.0, GRAMA, 1.2], [-2.6, GRAMA, 0.6]], [0, GRAMA, 0, 3.2, 2.8]);
  return g;
}

/** Textura do tapete de brincar: anéis coloridos. */
function texturaDoTapete() {
  const cv = document.createElement('canvas');
  cv.width = cv.height = 512;
  const c = cv.getContext('2d');
  const cores = ['#ff8fa3', '#ffd166', '#8fe3a8', '#8fc8ff', '#c7a6ff', '#fff4e0'];
  for (let i = 0; i < cores.length; i++) {
    c.fillStyle = cores[i];
    c.beginPath(); c.arc(256, 256, 256 - i * 42, 0, Math.PI * 2); c.fill();
  }
  // bolinhas brancas no anel de fora
  c.fillStyle = 'rgba(255,255,255,0.75)';
  for (let k = 0; k < 28; k++) { const a = (k / 28) * Math.PI * 2; c.beginPath(); c.arc(256 + Math.cos(a) * 235, 256 + Math.sin(a) * 235, 7, 0, Math.PI * 2); c.fill(); }
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  tex.anisotropy = 8;
  return tex;
}

/** Tapete de brincar redondo com o baú de brinquedos nos fundos (brinquedos, bolinhas, roupas soltas). */
function tapete() {
  const g = new THREE.Group();
  const R = 3.7;
  const mat = new THREE.MeshPhysicalMaterial({ map: texturaDoTapete(), roughness: 0.85, clearcoat: 0.05, envMapIntensity: 0.4 });
  g.add(em(cilindro(R, R, 0.1, [plastico('#ff8fa3', { rugosidade: 0.85 }), mat, mat], 96), 0, GRAMA + 0.02, 0));
  // o lado do tapete usa o rosa do anel de fora (o material 0 da CylinderGeometry é a lateral)
  const borda = malha(new THREE.TorusGeometry(R, 0.06, 10, 128), plastico('#ff6f91', { rugosidade: 0.6 }));
  borda.rotation.x = Math.PI / 2;
  g.add(em(borda, 0, GRAMA + 0.06, 0));
  // baú de brinquedos aberto nos fundos, à direita, com brinquedos aparecendo
  const bau = new THREE.Group();
  bau.add(em(caixa(1.6, 0.9, 1.0, 0.14, brilhante('#4d96ff')), 0, 0.45, 0));
  bau.add(em(caixa(1.66, 0.12, 1.06, 0.05, brilhante('#ffc93c')), 0, 0.9, 0));
  const tampa = caixa(1.6, 0.14, 1.0, 0.07, brilhante('#ef476f'));
  tampa.rotation.x = -1.2;
  bau.add(em(tampa, 0, 1.35, -0.75));
  bau.add(em(bola(0.3, brilhante('#ffc21a'), 24), -0.35, 1.0, 0.05));
  bau.add(em(caixa(0.4, 0.4, 0.4, 0.08, brilhante('#36c26a')), 0.35, 1.0, 0.0).rotateY(0.5));
  for (const s of [-1, 1]) bau.add(em(estrelinha(), s * 0.55, 0.5, 0.52));
  bau.rotation.y = -0.5;
  g.add(em(bau, 3.6, GRAMA, 3.6));
  vagas(g, [[-1.5, GRAMA + 0.08, -0.8], [0.3, GRAMA + 0.08, -1.4], [1.8, GRAMA + 0.08, -0.4], [-0.9, GRAMA + 0.08, 1.1], [1.0, GRAMA + 0.08, 1.2], [-2.5, GRAMA + 0.08, 0.3]], [0, GRAMA + 0.08, 0, 3.0, 2.8]);
  return g;
}

function estrelinha() {
  const s = new THREE.Shape();
  for (let k = 0; k < 10; k++) {
    const a = Math.PI / 2 + (k * Math.PI) / 5, r = k % 2 ? 0.07 : 0.16;
    if (k === 0) s.moveTo(Math.cos(a) * r, Math.sin(a) * r); else s.lineTo(Math.cos(a) * r, Math.sin(a) * r);
  }
  s.closePath();
  return malha(new THREE.ExtrudeGeometry(s, { depth: 0.03, bevelEnabled: true, bevelSize: 0.02, bevelThickness: 0.02, bevelSegments: 2 }), brilhante('#ffffff'));
}

/** As cenas das perguntas, prontas para a câmera 'jogo': a cena mais o disco da grama que só capta a sombra
 *  dela e esconde o que fica abaixo da grama. */
export const cenasDoJogo = {
  lagoa, mesa, varal, cercado, tapete,
};

export function camadaDaCenaDoJogo(nome) {
  return () => {
    const cena = cenasDoJogo[nome]();
    const g = camadaDaCena({ ver: [cena], capta: [discoDaGrama()] });
    // as âncoras da cena precisam estar no grupo de cima
    return g;
  };
}
