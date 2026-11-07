// Casa por dentro (PRD visual 3D, Fase 6): móveis, enfeites, pisos e paredes na câmera 'isometrico'
// (a mesma projeção de ui/componentes/Isometrico.kt). Tudo é montado em coordenadas da PLANTA
// (dominio/CasaPorDentro.kt, PlantaDaCasa): x da planta = x do mundo, y da planta (do fundo para a frente) =
// z do mundo, altura z da planta = y do mundo, em ladrilhos e unidades de altura. A câmera achata a altura
// (unidadeZ = 0,56) e cada imagem sai recortada em volta do objeto, com o apoio no ponto de referência.
//
// MOVEIS repete PlantaDaCasa.moveis (posição de sempre, pegada, altura, z, frente). Se a planta mudar, mudar
// aqui também: a imagem só depende do TAMANHO da pegada (a câmera é ortográfica), não do lugar.
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em, ancora } from '../kit.js';
import { ITENS } from './itensDaCasa.js';
import { patinho } from './patinho.js';

/** PlantaDaCasa.moveis: id, cômodo, x0, y0, x1, y1, altura, z, frente. */
export const MOVEIS = [
  ['escada', 'sala', 0.08, 0.08, 0.95, 2.6, 2.7],
  ['sofa', 'sala', 1.05, 0.1, 2.7, 0.98, 0.88],
  ['gato', 'sala', 1.65, 0.35, 2.15, 0.85, 0.45, 0.5],
  ['livros', 'sala', 2.8, 0.12, 3.22, 0.54, 0.5],
  ['tv', 'sala', 3.7, 0.1, 4.85, 0.6, 1.3],
  ['tapete', 'sala', 1.5, 1.45, 3.9, 3.45, 0],
  ['mesa', 'cozinha', 5.6, 2.0, 6.7, 2.98, 0.72],
  ['bolo', 'cozinha', 5.9, 2.25, 6.4, 2.75, 0.45, 0.72],
  ['lixeira', 'cozinha', 5.17, 0.12, 5.62, 0.57, 0.55],
  ['fogao', 'cozinha', 5.95, 0.1, 6.8, 0.92, 0.85],
  ['geladeira', 'cozinha', 6.98, 0.1, 7.9, 0.95, 2.0],
  ['escadaDeCima', 'quarto', 0.08, 0.08, 0.95, 2.6, 0.75],
  ['quadro', 'quarto', 1.45, 0, 2.3, 0.04, 0.65, 1.5],
  ['cama', 'quarto', 1.25, 0.1, 2.5, 1.98, 0.55],
  ['ursinho', 'quarto', 1.6, 1.2, 2.15, 1.7, 0.5, 0.55],
  ['criado', 'quarto', 2.65, 0.1, 3.2, 0.65, 0.5],
  ['abajur', 'quarto', 2.7, 0.13, 3.15, 0.62, 0.55, 0.5],
  ['violao', 'quarto', 3.3, 4.3, 3.85, 4.85, 0.9],
  ['guardaroupa', 'quarto', 0.1, 3.0, 0.72, 4.85, 2.2, 0, 'X'],
  ['planta', 'banheiro', 7.3, 4.3, 7.85, 4.85, 0.7],
  ['banheira', 'banheiro', 4.15, 0.1, 5.8, 1.35, 0.6],
  ['vaso', 'banheiro', 5.92, 0.1, 6.47, 0.7, 0.7],
  ['pia', 'banheiro', 6.98, 0.1, 7.9, 0.7, 0.9],
  ['espelho', 'banheiro', 7.07, 0, 7.81, 0.04, 0.8, 1.3],
  ['patinho', 'banheiro', 4.65, 0.45, 5.1, 0.9, 0.4, 0.6],
].map(([id, comodo, x0, y0, x1, y1, altura, z = 0, frente = 'Y']) => ({ id, comodo, x0, y0, x1, y1, altura, z, frente }));

export const PLANTA = { LARGURA: 8, FUNDO: 5, ALTURA_PAREDE: 3.0, ALTURA_PAREDE_INTERNA: 0.45, ESPESSURA_PAREDE: 0.14, ESPESSURA_PISO: 0.35 };
export const meio = (andar) => (andar === 'de_cima' ? 4 : 5);
export const comodosDo = (andar) => (andar === 'de_cima' ? ['quarto', 'banheiro'] : ['sala', 'cozinha']);
export const andarDo = (c) => (c === 'quarto' || c === 'banheiro' ? 'de_cima' : 'de_baixo');
export function limitesDo(c) {
  const a = andarDo(c), m = meio(a);
  return comodosDo(a)[0] === c ? { x0: 0, y0: 0, x1: m, y1: PLANTA.FUNDO } : { x0: m, y0: 0, x1: PLANTA.LARGURA, y1: PLANTA.FUNDO };
}
/** PlantaDaCasa.portasNaParedeInterna e portaDaRua. */
export const PASSAGENS = [3, 4];
export const PORTA_DA_RUA = { y0: 3.35, y1: 4.55 };
/** Loja.coresDaPorta (o índice é o valor guardado em EstadoJogo.corPorta). */
export const CORES_DA_PORTA = ['#a0522d', '#4d96ff', '#ef476f', '#3dbe6b', '#ffc93c'];

// ---------------------------------------------------------------------------------------------
// Materiais e peças em coordenadas da planta

const brilhante = (cor, o = {}) => plastico(cor, { rugosidade: 0.28, verniz: 0.8, ...o });
const fosco = (cor) => plastico(cor, { rugosidade: 0.7, verniz: 0.08 });
const madeira = () => plastico('#d9975a', { rugosidade: 0.5, verniz: 0.35 });
const madeiraClara = () => plastico('#efbf86', { rugosidade: 0.5, verniz: 0.35 });
const metal = () => plastico('#c0c8d4', { rugosidade: 0.15, verniz: 1 });
const branco = (o = {}) => brilhante('#ffffff', { rugosidade: 0.18, verniz: 1, ...o });

const grupo = (...f) => { const g = new THREE.Group(); for (const o of f) if (o) g.add(o); return g; };

/** Caixa de cantos redondos de (x0, y0, z0) a (x1, y1, z1) da planta. */
function bloco(x0, y0, x1, y1, z0, z1, mat, raio = 0.04) {
  const l = x1 - x0, p = y1 - y0, a = z1 - z0;
  return em(caixa(l, a, p, Math.min(raio, l / 2, p / 2, a / 2), mat), (x0 + x1) / 2, (z0 + z1) / 2, (y0 + y1) / 2);
}
/** Cilindro em pé com o centro da base em (x, y, z) da planta. */
const pino = (x, y, z, r, a, mat, r2 = r, seg = 24) => em(cilindro(r, r2, a, mat, seg), x, z + a / 2, y);
/** Ponto (x, y, z) da planta no mundo. */
const P = (x, y, z) => new THREE.Vector3(x, z, y);
function marca(g, nome, x, y, z) { ancora(g, nome, x, z, y); }
function achatar(o, x, y, z) { o.scale.set(x, y, z); return o; }

/** Modelo da casa de fora (itensDaCasa.js) sem o giro de três quartos (aqui a câmera já é isométrica). */
const DE_FORA = Object.fromEntries(ITENS.map(([id, f]) => [id, f]));
function deFora(id) {
  const o = DE_FORA[id]();
  o.rotation.set(0, 0, 0);
  return o;
}

/**
 * Encaixa um modelo (de frente para +z do mundo, em qualquer escala) na pegada do móvel [m].
 * [giro] gira em y antes; 'esticar' ajusta x e z à pegada e y a [altura] (ou à média de x e z);
 * 'uniforme' mantém as proporções (cabe na pegada e em [altura]).
 */
function encaixar(obj, m, { modo = 'esticar', altura = null, giro = 0, folga = 0 } = {}) {
  const giroG = new THREE.Group();
  obj.rotation.y += giro;
  giroG.add(obj);
  giroG.updateMatrixWorld(true);
  const cx = new THREE.Box3().setFromObject(giroG);
  const t = cx.getSize(new THREE.Vector3());
  const L = m.x1 - m.x0 - 2 * folga, F = m.y1 - m.y0 - 2 * folga;
  let sx = L / t.x, sz = F / t.z, sy;
  if (modo === 'uniforme') {
    const s = Math.min(sx, sz, altura ? altura / t.y : Infinity);
    sx = sz = sy = s;
  } else {
    sy = altura ? altura / t.y : (sx + sz) / 2;
  }
  const c = cx.getCenter(new THREE.Vector3());
  const g = new THREE.Group();
  giroG.position.set(-c.x, -cx.min.y, -c.z);
  const esc = new THREE.Group();
  esc.scale.set(sx, sy, sz);
  esc.add(giroG);
  g.add(esc);
  g.position.set((m.x0 + m.x1) / 2, m.z, (m.y0 + m.y1) / 2);
  return g;
}

// ---------------------------------------------------------------------------------------------
// Móveis que só existem dentro de casa (sem prêmio): escadas, lixeira, criado-mudo, guarda-roupa,
// vaso, pia, tapete. E versões próprias da geladeira e da banheira (abrir a porta; o patinho é à parte).

function escadaQueSobe(m) {
  const g = new THREE.Group();
  const n = 7, d = (m.y1 - m.y0) / n, hs = m.altura / n;
  const a = madeira(), b = plastico('#e6a86c', { rugosidade: 0.5, verniz: 0.35 });
  // os degraus sobem para o fundo (y0); cada um é um bloco do chão até a sua altura
  for (let i = 0; i < n; i++) {
    g.add(bloco(m.x0, m.y0, m.x1, m.y1 - i * d, i * hs, (i + 1) * hs - 0.015, i % 2 ? b : a, 0.035));
    // pisada clarinha
    g.add(bloco(m.x0 + 0.04, m.y1 - (i + 1) * d + 0.02, m.x1 - 0.04, m.y1 - i * d - 0.02, (i + 1) * hs - 0.02, (i + 1) * hs + 0.005, madeiraClara(), 0.02));
  }
  // corrimão do lado de dentro (x1), com balaústres e bolinhas
  const cor = brilhante('#ff8fab');
  const ya = m.y1 - d * 0.5, za = hs + 0.55, yb = m.y0 + 0.3, zb = m.altura + 0.2;
  const p0 = P(m.x1 - 0.06, ya, za), p1 = P(m.x1 - 0.06, yb, zb);
  // balaústres: de cada degrau (um sim, um não) até o corrimão
  for (let i = 0; i < n; i += 2) {
    const y = m.y1 - i * d - d * 0.5, zDegrau = (i + 1) * hs;
    const zCorr = za + ((ya - y) / (ya - yb)) * (zb - za);
    g.add(pino(m.x1 - 0.06, y, zDegrau, 0.025, zCorr - zDegrau, madeiraClara(), 0.025, 12));
  }
  g.add(segmento(p0, p1, 0.045, cor));
  g.add(em(bola(0.07, cor, 16), p0.x, p0.y + 0.02, p0.z));
  return g;
}

function escadaQueDesce(m) {
  // o vão no piso, com os degraus descendo para o fundo e o corrimão do lado de dentro do quarto
  const g = new THREE.Group();
  const n = 5, d = (m.y1 - m.y0) / n;
  g.add(bloco(m.x0, m.y0, m.x1, m.y1, -0.06, 0.006, fosco('#5c3d28'), 0.02));
  for (let k = 0; k < n; k++) {
    const tom = new THREE.Color('#c98b5b').multiplyScalar(0.62 + k * 0.08);
    g.add(bloco(m.x0 + 0.06, m.y0 + k * d + d * 0.4, m.x1 - 0.06, m.y0 + (k + 1) * d - 0.02, -0.05, 0.012, plastico('#' + tom.getHexString(), { rugosidade: 0.55, verniz: 0.3 }), 0.015));
  }
  for (let y = m.y0 + 0.12; y < m.y1 - 0.05; y += 0.5) g.add(pino(m.x1 - 0.035, y, 0, 0.022, m.altura - 0.06, madeiraClara(), 0.022, 12));
  const cor = brilhante('#ff8fab');
  g.add(bloco(m.x1 - 0.09, m.y0, m.x1 + 0.01, m.y1, m.altura - 0.08, m.altura, cor, 0.04));
  // a beirada do vão
  g.add(bloco(m.x0 - 0.02, m.y1 - 0.04, m.x1 + 0.02, m.y1 + 0.02, -0.02, 0.03, madeira(), 0.015));
  return g;
}

function segmento(va, vb, r, mat) {
  const d = vb.clone().sub(va);
  const s = capsula(r, Math.max(0.001, d.length()), mat);
  s.position.copy(va).add(vb).multiplyScalar(0.5);
  s.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), d.normalize());
  return s;
}

function lixeira(m) {
  const g = new THREE.Group();
  const cx = (m.x0 + m.x1) / 2, cy = (m.y0 + m.y1) / 2, r = (m.x1 - m.x0) / 2 - 0.02;
  const verde = brilhante('#3dbe6b');
  g.add(pino(cx, cy, 0, r * 0.92, 0.46, verde, r * 0.8, 32));
  g.add(pino(cx, cy, 0.46, r, 0.07, brilhante('#2ea35a'), r, 32));
  g.add(em(bola(0.05, brilhante('#ffd23f'), 12), cx, 0.55, cy));
  // pedal
  g.add(bloco(cx - 0.07, m.y1 - 0.06, cx + 0.07, m.y1 + 0.02, 0.02, 0.06, metal(), 0.015));
  return g;
}

function criadoMudo(m) {
  const g = new THREE.Group();
  g.add(bloco(m.x0, m.y0, m.x1, m.y1, 0.04, 0.5, madeira(), 0.05));
  g.add(bloco(m.x0 - 0.02, m.y0 - 0.02, m.x1 + 0.02, m.y1 + 0.02, 0.46, 0.52, madeiraClara(), 0.03));
  // gaveta e puxador
  g.add(bloco(m.x0 + 0.06, m.y1 - 0.02, m.x1 - 0.06, m.y1 + 0.01, 0.24, 0.42, madeiraClara(), 0.02));
  g.add(em(bola(0.035, brilhante('#ff8fab'), 12), (m.x0 + m.x1) / 2, 0.33, m.y1 + 0.02));
  for (const [x, y] of [[m.x0 + 0.06, m.y0 + 0.06], [m.x1 - 0.06, m.y0 + 0.06], [m.x0 + 0.06, m.y1 - 0.06], [m.x1 - 0.06, m.y1 - 0.06]]) g.add(pino(x, y, 0, 0.035, 0.06, madeira(), 0.03, 12));
  return g;
}

/** Guarda-roupa encostado na parede x = 0, de frente para +x. [aberto]: portas abertas e o varão vazio. */
function guardaRoupa(m, aberto) {
  const g = new THREE.Group();
  const corpo = madeira(), porta = plastico('#e8a76c', { rugosidade: 0.45, verniz: 0.45 });
  const ym = (m.y0 + m.y1) / 2;
  g.add(bloco(m.x0, m.y0, m.x1, m.y1, 0.06, 2.2, corpo, 0.06));
  g.add(bloco(m.x0 - 0.02, m.y0 - 0.04, m.x1 + 0.05, m.y1 + 0.04, 2.16, 2.3, plastico('#b9784a', { rugosidade: 0.5, verniz: 0.35 }), 0.05));
  for (const y of [m.y0 + 0.08, m.y1 - 0.08]) for (const x of [m.x0 + 0.08, m.x1 - 0.08]) g.add(pino(x, y, 0, 0.04, 0.08, plastico('#8d5a3b'), 0.04, 12));
  if (!aberto) {
    for (const [a, b] of [[m.y0 + 0.06, ym - 0.015], [ym + 0.015, m.y1 - 0.06]]) {
      g.add(bloco(m.x1 - 0.02, a, m.x1 + 0.03, b, 0.1, 2.1, porta, 0.03));
      // almofada da porta (um coração em relevo)
      g.add(bloco(m.x1 + 0.02, a + 0.12, m.x1 + 0.04, b - 0.12, 1.25, 1.95, plastico('#f2bd88', { rugosidade: 0.45, verniz: 0.45 }), 0.02));
      g.add(bloco(m.x1 + 0.02, a + 0.12, m.x1 + 0.04, b - 0.12, 0.25, 1.1, plastico('#f2bd88', { rugosidade: 0.45, verniz: 0.45 }), 0.02));
    }
    for (const y of [ym - 0.1, ym + 0.1]) g.add(em(bola(0.045, brilhante('#ff8fab'), 12), m.x1 + 0.06, 1.18, y));
  } else {
    // vão escuro, varão e duas portas abertas para a frente (+x)
    g.add(bloco(m.x1 - 0.03, m.y0 + 0.06, m.x1 + 0.005, m.y1 - 0.06, 0.1, 2.1, fosco('#5a3a22'), 0.01));
    g.add(segmento(P(m.x1 + 0.0, m.y0 + 0.1, 1.8), P(m.x1 + 0.0, m.y1 - 0.1, 1.8), 0.025, metal()));
    const meia = (m.y1 - m.y0) / 2 - 0.05;
    for (const [y, l] of [[m.y0 + 0.04, -1], [m.y1 - 0.04, 1]]) {
      g.add(bloco(m.x1, y - (l < 0 ? 0 : 0.05), m.x1 + meia, y + (l < 0 ? 0.05 : 0), 0.1, 2.1, porta, 0.025));
      g.add(em(bola(0.04, brilhante('#ff8fab'), 12), m.x1 + meia - 0.1, 1.18, y - l * 0.06));
    }
    // lugares dos cabides (o app pendura as roupas que a criança tem)
    for (let i = 0, a = m.y0 + 0.24; a + 0.1 < m.y1 - 0.1; i++, a += 0.26) marca(g, 'cabide_' + i, m.x1 - 0.02, a, 1.78);
  }
  return g;
}

function vasoSanitario(m) {
  const g = new THREE.Group();
  const cx = (m.x0 + m.x1) / 2, b = branco();
  // caixa acoplada no fundo, bacia redonda na frente, tampa azul clarinho
  g.add(bloco(m.x0 + 0.02, m.y0, m.x1 - 0.02, m.y0 + 0.2, 0.3, 0.7, b, 0.06));
  g.add(bloco(m.x0, m.y0 - 0.01, m.x1, m.y0 + 0.22, 0.68, 0.74, brilhante('#bfe6ff'), 0.04));
  g.add(em(bola(0.045, metal(), 12), m.x1 - 0.08, 0.63, m.y0 + 0.21));
  const bacia = pino(cx, m.y0 + 0.4, 0, 0.2, 0.36, b, 0.15, 32); bacia.scale.set(1, 1, 1.25); g.add(bacia);
  const assento = em(malha(new THREE.TorusGeometry(0.2, 0.045, 12, 32), brilhante('#bfe6ff')), cx, 0.38, m.y0 + 0.4);
  assento.rotation.x = Math.PI / 2; assento.scale.set(1, 1.25, 1); g.add(assento);
  g.add(achatar(em(bola(0.17, plastico('#7fd0ff', { rugosidade: 0.05, verniz: 1 }), 24), cx, 0.33, m.y0 + 0.4), 1, 0.08, 1.2));
  return g;
}

function pia(m) {
  const g = new THREE.Group();
  const cx = (m.x0 + m.x1) / 2;
  g.add(bloco(m.x0 + 0.03, m.y0, m.x1 - 0.03, m.y1 - 0.02, 0.06, 0.75, branco(), 0.06));
  // portinhas do gabinete
  for (const [a, b] of [[m.x0 + 0.1, cx - 0.02], [cx + 0.02, m.x1 - 0.1]]) {
    g.add(bloco(a, m.y1 - 0.03, b, m.y1 + 0.0, 0.14, 0.66, brilhante('#d7f3ff'), 0.03));
    g.add(em(bola(0.03, brilhante('#ff8fab'), 10), (a + b) / 2 + (a < cx ? 0.12 : -0.12), 0.5, m.y1 + 0.01));
  }
  g.add(bloco(m.x0 - 0.03, m.y0, m.x1 + 0.03, m.y1 + 0.04, 0.75, 0.83, brilhante('#9fd6ee'), 0.03));
  // cuba (um buraco raso azul) e torneira
  g.add(bloco(cx - 0.3, m.y0 + 0.16, cx + 0.3, m.y1 - 0.08, 0.81, 0.835, plastico('#6fbfe0', { rugosidade: 0.1, verniz: 1 }), 0.06));
  g.add(pino(cx, m.y0 + 0.09, 0.83, 0.035, 0.22, metal(), 0.04, 16));
  g.add(segmento(P(cx, m.y0 + 0.09, 1.04), P(cx, m.y0 + 0.28, 1.0), 0.03, metal()));
  for (const l of [-1, 1]) g.add(em(bola(0.04, brilhante(l < 0 ? '#ef476f' : '#4d96ff'), 12), cx + l * 0.13, 0.88, m.y0 + 0.08));
  // sabonete cor-de-rosa
  g.add(bloco(m.x1 - 0.2, m.y0 + 0.1, m.x1 - 0.06, m.y0 + 0.2, 0.83, 0.88, brilhante('#ff8fab'), 0.025));
  marca(g, 'bico', cx, m.y0 + 0.3, 1.0);
  marca(g, 'bacia', cx, m.y0 + 0.36, 0.84);
  return g;
}

function tapete(m) {
  const g = new THREE.Group();
  const camadas = [['#f2d9a0', 0], ['#fff0c9', 0.15], ['#f7c9a8', 0.45]];
  camadas.forEach(([cor, d], i) => g.add(bloco(m.x0 + d, m.y0 + d, m.x1 - d, m.y1 - d, 0, 0.02 + i * 0.008, fosco(cor), 0.12)));
  // bolinhas na borda
  for (let x = m.x0 + 0.25; x < m.x1 - 0.1; x += 0.4) for (const y of [m.y0 + 0.07, m.y1 - 0.07]) g.add(achatar(em(bola(0.03, fosco('#ff8fab'), 10), x, 0.02, y), 1, 0.4, 1));
  return g;
}

/** Geladeira de frente (+y). Aberta: a porta abre para a esquerda (dobradiça em x0), ver abaixo. */
function geladeira(m, aberta) {
  const g = new THREE.Group();
  const corpo = brilhante('#f4f8fc', { rugosidade: 0.18, verniz: 1 });
  const { x0, y0, x1, y1 } = m;
  const pe = 0.06, topo = 2.0;
  for (const x of [x0 + 0.12, x1 - 0.12]) g.add(pino(x, y1 - 0.15, 0, 0.05, pe, plastico('#9aa6b4'), 0.05, 12));
  if (!aberta) {
    g.add(bloco(x0, y0, x1, y1 - 0.06, pe, topo, corpo, 0.1));
    // duas portas (congelador em cima), puxadores e ímãs
    g.add(bloco(x0 + 0.02, y1 - 0.08, x1 - 0.02, y1, 1.34, topo - 0.02, corpo, 0.08));
    g.add(bloco(x0 + 0.02, y1 - 0.08, x1 - 0.02, y1, pe + 0.02, 1.3, corpo, 0.08));
    for (const [z0, z1] of [[1.45, 1.8], [0.75, 1.2]]) g.add(segmento(P(x0 + 0.13, y1 + 0.02, z0), P(x0 + 0.13, y1 + 0.02, z1), 0.03, metal()));
    const c = new THREE.Shape(); c.moveTo(0, -0.1); c.bezierCurveTo(-0.16, 0.02, -0.08, 0.14, 0, 0.06); c.bezierCurveTo(0.08, 0.14, 0.16, 0.02, 0, -0.1);
    g.add(em(malha(new THREE.ExtrudeGeometry(c, { depth: 0.03, bevelEnabled: true, bevelSize: 0.012, bevelThickness: 0.012 }), brilhante('#ff4f8b')), x1 - 0.3, 1.66, y1));
    const est = estrelinha(0.08, brilhante('#ffc21a'));
    g.add(em(est, x1 - 0.32, 0.95, y1 + 0.01));
  } else {
    // caixa oca: fundo, laterais, teto e chão; prateleiras brancas e luz azul clarinha por dentro
    const dentro = plastico('#a9dcf5', { rugosidade: 0.4, verniz: 0, ambiente: 0.3 });
    g.add(bloco(x0, y0, x1, y0 + 0.08, pe, topo, corpo, 0.06));
    for (const [a, b] of [[x0, x0 + 0.07], [x1 - 0.07, x1]]) g.add(bloco(a, y0, b, y1 - 0.02, pe, topo, corpo, 0.035));
    g.add(bloco(x0, y0, x1, y1 - 0.02, topo - 0.08, topo, corpo, 0.04));
    g.add(bloco(x0, y0, x1, y1 - 0.02, pe, pe + 0.08, corpo, 0.03));
    g.add(bloco(x0 + 0.07, y0 + 0.08, x1 - 0.07, y0 + 0.11, pe + 0.08, topo - 0.08, dentro, 0.01));
    for (const [a, b] of [[x0 + 0.07, x0 + 0.08], [x1 - 0.08, x1 - 0.07]]) g.add(bloco(a, y0 + 0.08, b, y1 - 0.04, pe + 0.08, topo - 0.08, dentro, 0.003));
    for (const z of [0.6, 1.1, 1.5]) g.add(bloco(x0 + 0.07, y0 + 0.08, x1 - 0.07, y1 - 0.05, z, z + 0.03, plastico('#ffffff', { rugosidade: 0.2, verniz: 0.8, emissivo: '#ffffff' }), 0.01));
    // a porta aberta: placa saindo da dobradiça da ESQUERDA (x0, y1) para a frente. Com a dobradiça em x1
    // (como no desenho do app), a porta ficaria na frente da geladeira para esta câmera e taparia tudo.
    const L = x1 - x0 - 0.02;
    g.add(bloco(x0 - 0.06, y1, x0 + 0.02, y1 + L, pe + 0.02, topo - 0.02, corpo, 0.06));
    g.add(bloco(x0 + 0.015, y1 + 0.06, x0 + 0.045, y1 + L - 0.06, pe + 0.1, topo - 0.1, dentro, 0.02));
    for (const z of [0.5, 1.2]) g.add(bloco(x0 + 0.02, y1 + 0.08, x0 + 0.14, y1 + L - 0.1, z, z + 0.07, brilhante('#b9d7e8'), 0.02));
    g.add(segmento(P(x0 - 0.08, y1 + L - 0.12, 0.75), P(x0 - 0.08, y1 + L - 0.12, 1.2), 0.03, metal()));
    for (let i = 0; i < 9; i++) marca(g, 'comida_' + i, x0 + 0.18 + (i % 3) * (x1 - x0 - 0.36) / 2, y1 - 0.2, 0.12 + Math.floor(i / 3) * 0.5);
  }
  return g;
}

function estrelinha(r, mat) {
  const s = new THREE.Shape();
  for (let i = 0; i < 10; i++) {
    const a = Math.PI / 2 + (i * Math.PI) / 5, rr = i % 2 ? r * 0.45 : r;
    if (i === 0) s.moveTo(Math.cos(a) * rr, Math.sin(a) * rr); else s.lineTo(Math.cos(a) * rr, Math.sin(a) * rr);
  }
  s.closePath();
  return malha(new THREE.ExtrudeGeometry(s, { depth: 0.02, bevelEnabled: true, bevelSize: 0.01, bevelThickness: 0.01 }), mat);
}

/** Banheira de pezinhos, sem o patinho (ele é outro móvel); a água fica em z = 0,6 (onde o patinho boia). */
function banheira(m) {
  const g = new THREE.Group();
  const { x0, y0, x1, y1 } = m;
  const b = branco({ rugosidade: 0.12 });
  // fundo e as quatro bordas (oca: a água aparece por cima), com a beirada arredondada
  const r = 0.13;
  g.add(bloco(x0, y0 + 0.02, x1, y1, 0.12, 0.5, b, 0.2));
  g.add(bloco(x0, y0 + 0.02, x1, y0 + 0.02 + r, 0.3, 0.68, b, 0.065));
  g.add(bloco(x0, y1 - r, x1, y1, 0.3, 0.68, b, 0.065));
  g.add(bloco(x0, y0 + 0.02, x0 + r, y1, 0.3, 0.68, b, 0.065));
  g.add(bloco(x1 - r, y0 + 0.02, x1, y1, 0.3, 0.68, b, 0.065));
  g.add(bloco(x0 + r - 0.01, y0 + 0.02 + r - 0.01, x1 - r + 0.01, y1 - r + 0.01, 0.45, 0.6, plastico('#22a0ee', { rugosidade: 0.2, verniz: 0.1, ambiente: 0.1 }), 0.02));
  for (const [x, y] of [[x0 + 0.18, y0 + 0.2], [x1 - 0.18, y0 + 0.2], [x0 + 0.18, y1 - 0.18], [x1 - 0.18, y1 - 0.18]]) g.add(em(bola(0.1, brilhante('#ffc93c', { rugosidade: 0.15 }), 16), x, 0.09, y));
  // espuma num canto (longe do patinho, que fica no meio)
  const espuma = plastico('#ffffff', { rugosidade: 0.9, verniz: 0, emissivo: '#eef6ff' });
  for (const [x, y, r] of [[x0 + 0.3, y0 + 0.35, 0.12], [x0 + 0.45, y0 + 0.28, 0.09], [x0 + 0.28, y0 + 0.55, 0.08], [x1 - 0.3, y1 - 0.32, 0.1], [x1 - 0.45, y1 - 0.28, 0.07]]) g.add(em(bola(r, espuma, 16), x, 0.62, y));
  // torneira no fundo, à direita
  g.add(pino(x1 - 0.32, y0 + 0.07, 0.6, 0.035, 0.28, metal(), 0.035, 16));
  g.add(segmento(P(x1 - 0.32, y0 + 0.07, 0.86), P(x1 - 0.32, y0 + 0.26, 0.82), 0.03, metal()));
  marca(g, 'agua', (x0 + x1) / 2, (y0 + y1) / 2, 0.6);
  return g;
}

// ---------------------------------------------------------------------------------------------
// Peças que vêm da casa de fora, encaixadas na pegada da planta

function pendurarNaParede(obj, m, { largura, comMoldura = 0.06 }) {
  // quadro e espelho: de frente para +y da planta, encostados em y = 0, com a moldura do app em volta
  const box = new THREE.Box3().setFromObject(obj);
  const t = box.getSize(new THREE.Vector3());
  const s = (largura + 2 * comMoldura) / t.x;
  const c = box.getCenter(new THREE.Vector3());
  const g = new THREE.Group();
  const inner = new THREE.Group();
  obj.position.set(-c.x, -box.min.y, -box.min.z);
  inner.add(obj);
  inner.scale.setScalar(s);
  g.add(inner);
  g.position.set((m.x0 + m.x1) / 2, m.z - comMoldura, 0.002);
  return g;
}

const MONTAR = {
  escada: (m) => escadaQueSobe(m),
  escadaDeCima: (m) => escadaQueDesce(m),
  sofa: (m) => encaixar(deFora('sofa'), m, { altura: 0.95 }),
  gato: (m) => encaixar(deFora('gato'), m, { modo: 'uniforme', altura: 0.62, giro: Math.PI / 4 }),
  livros: (m) => encaixar(deFora('livros'), m, { modo: 'uniforme', altura: 0.5, giro: Math.PI / 5 }),
  tv: (m) => encaixar(deFora('tv'), m, { altura: 1.3 }),
  tapete: (m) => tapete(m),
  mesa: (m) => encaixar(deFora('mesa'), m, { altura: 0.72 }),
  bolo: (m) => encaixar(deFora('bolo'), m, { modo: 'uniforme', altura: 0.5 }),
  lixeira: (m) => lixeira(m),
  fogao: (m) => encaixar(deFora('fogao'), m, { altura: 1.25 }),
  geladeira: (m) => geladeira(m, false),
  geladeira_aberta: (m) => geladeira(m, true),
  quadro: (m) => pendurarNaParede(deFora('quadro'), m, { largura: m.x1 - m.x0 }),
  cama: (m) => encaixar(deFora('cama'), m, { altura: 1.05, giro: -Math.PI / 2 }),
  ursinho: (m) => encaixar(deFora('ursinho'), m, { modo: 'uniforme', altura: 0.62, giro: Math.PI / 4 }),
  criado: (m) => criadoMudo(m),
  abajur: (m) => encaixar(deFora('abajur'), m, { modo: 'uniforme', altura: 0.62 }),
  violao: (m) => encaixar(deFora('violao'), m, { modo: 'uniforme', altura: 0.95, giro: Math.PI / 4 }),
  guardaroupa: (m) => guardaRoupa(m, false),
  guardaroupa_aberto: (m) => guardaRoupa(m, true),
  planta: (m) => encaixar(deFora('planta'), m, { modo: 'uniforme', altura: 0.78 }),
  banheira: (m) => banheira(m),
  vaso: (m) => vasoSanitario(m),
  pia: (m) => pia(m),
  espelho: (m) => pendurarNaParede(deFora('espelho'), m, { largura: m.x1 - m.x0, comMoldura: 0 }),
  patinho: (m) => encaixar(grupo(patinho(1, '#ffc400')), m, { modo: 'uniforme', altura: 0.42, giro: -2.3 }),
};

/** Sombra de contato: só no que pisa no chão (ou em cima de outro móvel) e não é plano nem de parede. */
const SEM_SOMBRA = new Set(['tapete', 'quadro', 'espelho', 'escadaDeCima', 'patinho']);

/**
 * Entradas do catálogo dos móveis: dentro_<id> (e as variantes _aberta / _aberto). Referência (o apoio) =
 * centro da base da pegada, (centroX, centroY, z) da planta, o mesmo ponto que o app usa de âncora
 * (Projecao.ponto(m.centroX, m.centroY, m.z)). Móveis de parede: centro da base na parede (y = 0).
 */
export function entradasDosMoveis() {
  const variantes = { geladeira: ['', '_aberta'], guardaroupa: ['', '_aberto'] };
  const lista = [];
  for (const m of MOVEIS) {
    for (const v of variantes[m.id] || ['']) {
      const chave = m.id + v;
      const id = 'dentro_' + chave.replace('escadaDeCima', 'escada_de_cima');
      const naParede = m.id === 'quadro' || m.id === 'espelho';
      lista.push({
        id,
        tipo: 'isometrico',
        referencia: { x: (m.x0 + m.x1) / 2, y: naParede ? 0 : (m.y0 + m.y1) / 2, z: m.z },
        sombra: SEM_SOMBRA.has(m.id) ? null : { x0: m.x0, y0: m.y0, x1: m.x1, y1: m.y1, z: m.z, folga: m.z > 0 ? 0.02 : 0.08, forca: m.z > 0 ? 0.22 : 0.34 },
        iso: {
          movel: m.id, comodo: m.comodo, frente: m.frente, estado: v ? v.slice(1) : 'normal',
          pegada: { x0: m.x0, y0: m.y0, x1: m.x1, y1: m.y1, z: m.z, altura: m.altura },
        },
        montar: () => MONTAR[chave](m),
      });
    }
  }
  return lista;
}

// ---------------------------------------------------------------------------------------------
// Cascas dos cômodos: piso, paredes, laje e acabamento; parede interna, janelas, porta e lâmpada.
// Referência de todas as peças do andar: o canto (0, 0, 0) do chão (Projecao.ponto(0, 0)).

/** Tábuas de madeira (quarto e sala) ou lajotas (banheiro e cozinha), como pisoDo() no app. */
function piso(c) {
  const g = new THREE.Group();
  const l = limitesDo(c);
  const madeiraNoPiso = c === 'quarto' || c === 'sala';
  const base = { quarto: '#e9b47c', sala: '#e2a768', banheiro: '#bfeafc', cozinha: '#fddcaa' }[c];
  const rejunte = { quarto: '#9a6232', sala: '#94592c', banheiro: '#8fbdd2', cozinha: '#c9a376' }[c];
  g.add(bloco(l.x0, l.y0, l.x1, l.y1, -0.06, -0.02, fosco(rejunte), 0.01));
  if (madeiraNoPiso) {
    const tons = [base, '#' + new THREE.Color(base).offsetHSL(0, 0.02, -0.025).getHexString(), '#' + new THREE.Color(base).offsetHSL(0, -0.02, 0.02).getHexString()];
    let fileira = 0;
    for (let y = l.y0; y < l.y1 - 0.01; y += 0.5, fileira++) {
      let x = l.x0;
      let corte = l.x0 + (fileira % 2 === 0 ? 1.3 : 0.6);
      let k = 0;
      while (x < l.x1 - 0.01) {
        const fim = Math.min(corte, l.x1);
        g.add(bloco(x + 0.012, y + 0.012, fim - 0.012, Math.min(y + 0.5, l.y1) - 0.012, -0.05, 0, plastico(tons[(fileira + k) % 3], { rugosidade: 0.55, verniz: 0, ambiente: 0.12 }), 0.02));
        x = fim; corte = fim + 1.6; k++;
      }
    }
  } else {
    let k = 0;
    for (let x = l.x0; x < l.x1 - 0.01; x++) for (let y = l.y0; y < l.y1 - 0.01; y++, k++) {
      const cor = (x + y) % 2 === 0 ? base : '#' + new THREE.Color(base).offsetHSL(0, 0, -0.015).getHexString();
      g.add(bloco(x + 0.018, y + 0.018, Math.min(x + 1, l.x1) - 0.018, Math.min(y + 1, l.y1) - 0.018, -0.05, 0, plastico(cor, { rugosidade: 0.4, verniz: 0, ambiente: 0.15 }), 0.03));
    }
  }
  return g;
}

/**
 * Paredes do fundo do cômodo (y = 0 e, no cômodo da esquerda, x = 0) em branco, para o app TINGIR com a cor
 * do cômodo (multiplicar por EstadoJogo.corDo, como os fundos de cômodo da casa de fora). Rodapé e azulejos
 * (banheiro até meia parede, cozinha numa faixa) em relevo.
 */
function paredesDoComodo(c) {
  const g = new THREE.Group();
  const l = limitesDo(c);
  const h = PLANTA.ALTURA_PAREDE, e = PLANTA.ESPESSURA_PAREDE;
  const parede = plastico('#ffffff', { rugosidade: 0.75, verniz: 0.05 });
  const rodape = plastico('#dcdcdc', { rugosidade: 0.5, verniz: 0.3 });
  const esquerda = l.x0 === 0;
  g.add(bloco(l.x0 - (esquerda ? e : 0), -e, l.x1, 0, 0, h, parede, 0.005));
  g.add(bloco(l.x0, -0.01, l.x1, 0.025, 0, 0.16, rodape, 0.012));
  if (esquerda) {
    g.add(bloco(-e, 0, 0, l.y1, 0, h, parede, 0.005));
    g.add(bloco(-0.01, 0, 0.025, l.y1, 0, 0.16, rodape, 0.012));
  }
  const faixa = c === 'banheiro' ? [0.16, 1.25] : c === 'cozinha' ? [0.9, 1.55] : null;
  if (faixa) {
    const az = plastico('#f2f2f2', { rugosidade: 0.15, verniz: 1 });
    const t = 0.35, tz = 0.3;
    for (let x = l.x0; x < l.x1 - 0.01; x += t) {
      for (let z = faixa[0]; z < faixa[1] - 0.01; z += tz) {
        g.add(bloco(x + 0.012, 0, Math.min(x + t, l.x1) - 0.012, 0.03, z + 0.012, Math.min(z + tz, faixa[1]) - 0.012, az, 0.012));
      }
    }
  }
  return g;
}

/** Espessura do piso (a casa parece uma maquete) por baixo de tudo do andar. Não se tinge. */
function laje() {
  const g = new THREE.Group();
  const { LARGURA: L, FUNDO: F, ESPESSURA_PISO: s, ESPESSURA_PAREDE: e } = PLANTA;
  g.add(bloco(-e, -e, L, F, -s, -0.04, plastico('#d6a574', { rugosidade: 0.5, verniz: 0.35 }), 0.06));
  g.add(bloco(-e - 0.02, F - 0.06, L + 0.02, F + 0.02, -s - 0.02, -s + 0.07, plastico('#c98f5c', { rugosidade: 0.5, verniz: 0.35 }), 0.03));
  g.add(bloco(L - 0.06, -e - 0.02, L + 0.02, F + 0.02, -s - 0.02, -s + 0.07, plastico('#b98a5e', { rugosidade: 0.5, verniz: 0.35 }), 0.03));
  return g;
}

/** Acabamento no alto das paredes, pontas da frente e a divisória de madeira entre os cômodos. Não se tinge. */
function acabamento(andar) {
  const g = new THREE.Group();
  const { LARGURA: L, FUNDO: F, ALTURA_PAREDE: h, ESPESSURA_PAREDE: e } = PLANTA;
  const cor = plastico('#fdf3e4', { rugosidade: 0.4, verniz: 0.5 });
  g.add(bloco(-e - 0.03, -e - 0.03, L + 0.03, 0.03, h - 0.02, h + 0.08, cor, 0.04));
  g.add(bloco(-e - 0.03, -e - 0.03, 0.03, F + 0.03, h - 0.02, h + 0.08, cor, 0.04));
  // colunas das pontas (como o corte da casa de bonecas)
  g.add(bloco(-e - 0.03, F - 0.1, 0.03, F + 0.03, -0.02, h, plastico('#e8d6be', { rugosidade: 0.4, verniz: 0.5 }), 0.03));
  g.add(bloco(L - 0.1, -e - 0.03, L + 0.03, 0.03, -0.02, h, plastico('#d9c3a5', { rugosidade: 0.4, verniz: 0.5 }), 0.03));
  // divisória de madeira no alto da parede, no meio
  const m = meio(andar);
  g.add(bloco(m - 0.06, 0, m + 0.06, 0.04, 0, h, plastico('#e9d8c4', { rugosidade: 0.45, verniz: 0.4 }), 0.02));
  return g;
}

/** Um pedaço (um ladrilho) da parede interna baixinha, PlantaDaCasa.paredesInternas. */
function paredeInterna() {
  const g = new THREE.Group();
  g.add(bloco(-0.06, 0, 0.06, 1, 0, PLANTA.ALTURA_PAREDE_INTERNA, plastico('#f3e6d3', { rugosidade: 0.55, verniz: 0.25 }), 0.03));
  g.add(bloco(-0.075, -0.0, 0.075, 1, PLANTA.ALTURA_PAREDE_INTERNA - 0.04, PLANTA.ALTURA_PAREDE_INTERNA + 0.02, plastico('#fdf3e4', { rugosidade: 0.4, verniz: 0.5 }), 0.025));
  return g;
}

/** Janela de 1 ladrilho na parede do fundo (y = 0), de z 1,32 a 2,58, com cortininhas. Base no meio, em z = 1,32. */
function janela(noite = false) {
  const g = new THREE.Group();
  const a = -0.5, b = 0.5;
  const moldura = branco();
  const vidro = noite ? plastico('#ffd77a', { rugosidade: 0.1, verniz: 1, emissivo: '#ffcf5a' }) : plastico('#bde7ff', { rugosidade: 0.05, verniz: 1, emissivo: '#9fd8ff', ambiente: 1.0 });
  g.add(bloco(a - 0.08, 0, b + 0.08, 0.05, 1.32, 2.58, moldura, 0.04));
  g.add(bloco(a, 0.04, b, 0.06, 1.4, 2.5, vidro, 0.01));
  g.add(bloco(-0.03, 0.04, 0.03, 0.08, 1.4, 2.5, moldura, 0.01));
  g.add(bloco(a, 0.04, b, 0.08, 1.92, 1.98, moldura, 0.01));
  g.add(bloco(a - 0.12, 0.0, b + 0.12, 0.1, 1.24, 1.32, moldura, 0.03));
  // cortininhas cor-de-rosa franzidas e o varão
  const rosa = plastico('#ff8fb1', { rugosidade: 0.7, verniz: 0.05 });
  for (const [x0, x1] of [[a - 0.12, a + 0.08], [b - 0.08, b + 0.12]]) {
    for (let k = 0; k < 3; k++) {
      const x = x0 + (k + 0.5) * (x1 - x0) / 3;
      g.add(pino(x, 0.1, 1.3, 0.04, 1.32, rosa, 0.045, 12));
    }
  }
  g.add(segmento(P(a - 0.2, 0.08, 2.66), P(b + 0.2, 0.08, 2.66), 0.025, madeira()));
  for (const x of [a - 0.2, b + 0.2]) g.add(em(bola(0.045, brilhante('#ff8fab'), 12), x, 2.66, 0.08));
  return g;
}

/** Janela redonda do banheiro (em cima da banheira), centro em z = 2,05, raio 0,5. Base no meio, em z = 1,55. */
function janelaRedonda(noite = false) {
  const g = new THREE.Group();
  const aro = em(malha(new THREE.TorusGeometry(0.45, 0.06, 16, 48), branco()), 0, 2.05, 0.04);
  g.add(aro);
  const vidro = em(malha(new THREE.CircleGeometry(0.42, 48), noite ? plastico('#ffd77a', { rugosidade: 0.1, verniz: 1, emissivo: '#ffcf5a' }) : plastico('#bde7ff', { rugosidade: 0.05, verniz: 1, emissivo: '#9fd8ff' })), 0, 2.05, 0.02);
  g.add(vidro);
  g.add(bloco(-0.025, 0.01, 0.025, 0.05, 1.62, 2.48, branco(), 0.01));
  g.add(bloco(-0.42, 0.01, 0.42, 0.05, 2.025, 2.075, branco(), 0.01));
  return g;
}

/** Porta da rua na parede x = 0 da sala (de frente para +x), com a cor da loja. Base no meio, em x = 0. */
function porta(cor) {
  const g = new THREE.Group();
  const { y0, y1 } = PORTA_DA_RUA;
  const ym = (y0 + y1) / 2, a = y0 - ym, b = y1 - ym;
  g.add(bloco(0, a - 0.1, 0.05, b + 0.1, 0, 2.3, branco(), 0.04));
  g.add(bloco(0.02, a, 0.08, b, 0.02, 2.17, brilhante(cor, { rugosidade: 0.35 }), 0.05));
  // almofadas, janelinha redonda e maçaneta dourada
  for (const [z0, z1] of [[0.2, 0.95], [1.05, 1.45]]) g.add(bloco(0.07, a + 0.15, 0.1, b - 0.15, z0, z1, brilhante(cor, { rugosidade: 0.35 }), 0.03));
  const jan = em(malha(new THREE.TorusGeometry(0.17, 0.035, 12, 32), brilhante('#ffe9a8')), 0.09, 1.75, 0);
  jan.rotation.y = Math.PI / 2; g.add(jan);
  const vid = em(malha(new THREE.CircleGeometry(0.15, 32), plastico('#bde7ff', { rugosidade: 0.05, verniz: 1, emissivo: '#9fd8ff' })), 0.085, 1.75, 0);
  vid.rotation.y = Math.PI / 2; g.add(vid);
  g.add(em(bola(0.055, brilhante('#ffd166', { rugosidade: 0.15, verniz: 1 }), 16), 0.12, 1.0, b - 0.18));
  // capacho
  g.add(bloco(0.02, a, 0.45, b, 0, 0.025, fosco('#ff8fab'), 0.03));
  return g;
}

/** Lâmpada pendurada (fio, cúpula, lâmpada); base no meio da lâmpada. Acesa: a lâmpada brilha. */
function lampada(acesa) {
  const g = new THREE.Group();
  g.add(segmento(P(0, 0, 0.1), P(0, 0, 0.88), 0.012, plastico('#6b5b4b')));
  const cupula = em(cilindro(0.07, 0.2, 0.17, brilhante('#ff8fab'), 32), 0, 0.14, 0);
  g.add(cupula);
  g.add(em(bola(0.075, acesa ? plastico('#fff3b0', { rugosidade: 0.1, emissivo: '#fff3b0' }) : plastico('#b0b4c0', { rugosidade: 0.2, verniz: 0.8 }), 20), 0, 0.03, 0));
  marca(g, 'luz', 0, 0, 0);
  return g;
}

const casca = (id, montar, referencia, iso, sombra = null, luzes = null) => ({ id, tipo: 'isometrico', referencia, iso, sombra, montar, ...(luzes ? { luzes } : {}) });
const ORIGEM = { x: 0, y: 0, z: 0 };

export function entradasDosComodos() {
  const lista = [];
  for (const andar of ['de_baixo', 'de_cima']) {
    lista.push(casca(`dentro_laje_${andar}`, laje, ORIGEM, { peca: 'laje', andar, ordem: 0 }));
    for (const c of comodosDo(andar)) {
      // piso sem verniz e com sol mais fraco: visto de 30 graus, o reflexo do verniz e o sol estouravam o piso
      lista.push(casca(`dentro_piso_${c}`, () => piso(c), ORIGEM, { peca: 'piso', andar, comodo: c, ordem: 1 }, null, { forca: 1.2 }));
      lista.push(casca(`dentro_parede_${c}`, () => paredesDoComodo(c), ORIGEM, { peca: 'parede', andar, comodo: c, tingir: c, ordem: 2 }));
    }
    lista.push(casca(`dentro_acabamento_${andar}`, () => acabamento(andar), ORIGEM, { peca: 'acabamento', andar, ordem: 3 }));
  }
  // peças que se repetem: referência no meio da base
  lista.push(casca('dentro_parede_interna', paredeInterna, { x: 0, y: 0.5, z: 0 }, { peca: 'paredeInterna', pegada: { x0: -0.06, y0: 0, x1: 0.06, y1: 1, z: 0, altura: PLANTA.ALTURA_PAREDE_INTERNA } }));
  lista.push(casca('dentro_janela', () => janela(false), { x: 0, y: 0, z: 1.32 }, { peca: 'janela', ordem: 2.5 }));
  lista.push(casca('dentro_janela_noite', () => janela(true), { x: 0, y: 0, z: 1.32 }, { peca: 'janela', ordem: 2.5 }));
  lista.push(casca('dentro_janela_redonda', () => janelaRedonda(false), { x: 0, y: 0, z: 1.55 }, { peca: 'janela', ordem: 2.5 }));
  lista.push(casca('dentro_janela_redonda_noite', () => janelaRedonda(true), { x: 0, y: 0, z: 1.55 }, { peca: 'janela', ordem: 2.5 }));
  CORES_DA_PORTA.forEach((cor, i) => lista.push(casca(`dentro_porta_${i}`, () => porta(cor), { x: 0, y: 0, z: 0 }, { peca: 'porta', ordem: 2.5 })));
  lista.push(casca('dentro_lampada_acesa', () => lampada(true), ORIGEM, { peca: 'lampada' }));
  lista.push(casca('dentro_lampada_apagada', () => lampada(false), ORIGEM, { peca: 'lampada' }));
  return lista;
}
