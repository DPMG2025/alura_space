// Peças 3D da interface (PRD, seção 6, Fase 2): estrelas do progresso em três estados, contas
// coloridas, e os ícones da barra (fechar, voltar, som, ajustes, pais, casa, loja), todos em plástico
// brilhante com verniz e a mesma luz das maquetes.
//
// Os ícones da barra são glifos roxos em relevo, para ficar dentro dos botões redondos claros
// (ui2.css: .redondo), como o X, a engrenagem e o alto-falante das maquetes 1 e 2.
import { THREE, plastico, malha, bola, capsula, caixa, cilindro, em, ancora, ouro } from '../kit.js';
import { estrela3D } from './estrela3D.js';

/** Roxo dos glifos das maquetes (botões redondos). */
export const ROXO = '#5a3df2';
const roxo = () => plastico(ROXO, { rugosidade: 0.24, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.5 });
const brancoPlastico = () => plastico('#ffffff', { rugosidade: 0.3, verniz: 1, ambiente: 0.45 });

/** Glifo levemente girado para mostrar a espessura, com âncora no centro. */
function glifo(g, { giroY = -0.28, giroX = 0.1 } = {}) {
  const fora = new THREE.Group();
  g.rotation.set(giroX, giroY, 0);
  fora.add(g);
  ancora(fora, 'centro', 0, 0, 0);
  return fora;
}

/** Tubo de pontas redondas entre dois pontos do plano XY (traço de caneta em plástico). */
function traco([x0, y0], [x1, y1], r, mat) {
  const L = Math.hypot(x1 - x0, y1 - y0);
  const c = capsula(r, L, mat);
  c.position.set((x0 + x1) / 2, (y0 + y1) / 2, 0);
  c.rotation.z = Math.atan2(y1 - y0, x1 - x0) - Math.PI / 2;
  return c;
}

/** Arco de tubo (centro, raio, ângulo inicial e final) com as pontas arredondadas. */
function arcoDeTubo([cx, cy], R, a0, a1, r, mat) {
  const g = new THREE.Group();
  const t = malha(new THREE.TorusGeometry(R, r, 20, 48, a1 - a0), mat);
  t.rotation.z = a0;
  g.add(t);
  for (const a of [a0, a1]) g.add(em(bola(r, mat, 24), Math.cos(a) * R, Math.sin(a) * R, 0));
  return em(g, cx, cy, 0);
}

/** Polígono com cantos arredondados (raio por canto ou um raio para todos). */
function poligono(pontos, raio, alvo = new THREE.Shape()) {
  const n = pontos.length;
  const rs = Array.isArray(raio) ? raio : pontos.map(() => raio);
  const v = pontos.map((p) => new THREE.Vector2(...p));
  for (let i = 0; i < n; i++) {
    const p = v[i], a = v[(i + n - 1) % n], b = v[(i + 1) % n];
    const da = a.clone().sub(p), db = b.clone().sub(p);
    const r = Math.min(rs[i], da.length() * 0.45, db.length() * 0.45);
    const p0 = p.clone().add(da.normalize().multiplyScalar(r));
    const p1 = p.clone().add(db.normalize().multiplyScalar(r));
    if (i === 0) alvo.moveTo(p0.x, p0.y); else alvo.lineTo(p0.x, p0.y);
    alvo.quadraticCurveTo(p.x, p.y, p1.x, p1.y);
  }
  alvo.closePath();
  return alvo;
}

/** Forma plana extrudada com bisel redondo (bala de goma), centrada em z = 0. */
function extrudar(forma, prof, mat, bisel = 0.08) {
  const geo = new THREE.ExtrudeGeometry(forma, { depth: prof, bevelEnabled: true, bevelThickness: bisel, bevelSize: bisel, bevelSegments: 8, curveSegments: 24 });
  geo.translate(0, 0, -prof / 2);
  return malha(geo, mat);
}

// ---------------------------------------------------------------- estrelas

/** Estrela do progresso cheia: dourada, como as das maquetes. */
export function estrelaCheia() {
  const g = new THREE.Group();
  const e = estrela3D(1.0, 0.32, ouro(), 5, { segmentos: 8 });
  e.rotation.set(0.12, -0.2, 0);
  g.add(e);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

/** Estrela ainda não ganha: mesma forma, em plástico cinza lilás fosco, um pouco menos alta. */
export function estrelaVazia() {
  const g = new THREE.Group();
  const e = estrela3D(1.0, 0.24, plastico('#c9c4d6', { rugosidade: 0.42, verniz: 0.7, rugosidadeVerniz: 0.3, ambiente: 0.5 }), 5, { segmentos: 8 });
  e.rotation.set(0.12, -0.2, 0);
  g.add(e);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

/** Halo macio (textura radial) para o brilho atrás da estrela. */
function halo(raio, cor) {
  const cv = document.createElement('canvas');
  cv.width = cv.height = 256;
  const ctx = cv.getContext('2d');
  const gr = ctx.createRadialGradient(128, 128, 0, 128, 128, 128);
  gr.addColorStop(0, cor + 'ff'); gr.addColorStop(0.35, cor + 'aa'); gr.addColorStop(0.7, cor + '33'); gr.addColorStop(1, cor + '00');
  ctx.fillStyle = gr; ctx.fillRect(0, 0, 256, 256);
  // raios suaves
  ctx.globalCompositeOperation = 'lighter';
  ctx.translate(128, 128);
  for (let i = 0; i < 10; i++) {
    ctx.rotate(Math.PI / 5);
    const lg = ctx.createLinearGradient(0, 0, 0, -122);
    lg.addColorStop(0, cor + '66'); lg.addColorStop(1, cor + '00');
    ctx.fillStyle = lg;
    ctx.beginPath(); ctx.moveTo(-9, 0); ctx.lineTo(0, -122); ctx.lineTo(9, 0); ctx.fill();
  }
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  const m = new THREE.Mesh(new THREE.PlaneGeometry(raio * 2, raio * 2), new THREE.MeshBasicMaterial({ map: tex, transparent: true, depthWrite: false, toneMapped: false }));
  m.renderOrder = -1;
  return m;
}

/** Faísca de quatro pontas (plástico branco brilhante). */
function faisca(r, mat) {
  const s = new THREE.Shape();
  for (let i = 0; i < 8; i++) {
    const a = Math.PI / 2 + (i * Math.PI) / 4;
    const rr = i % 2 === 0 ? r : r * 0.3;
    if (i === 0) s.moveTo(Math.cos(a) * rr, Math.sin(a) * rr); else s.lineTo(Math.cos(a) * rr, Math.sin(a) * rr);
  }
  s.closePath();
  const geo = new THREE.ExtrudeGeometry(s, { depth: r * 0.12, bevelEnabled: true, bevelThickness: r * 0.08, bevelSize: r * 0.06, bevelSegments: 4 });
  geo.center();
  return malha(geo, mat, { sombra: false });
}

/** Estrela recém ganha: dourada mais clara, com halo quente e faíscas em volta. */
export function estrelaBrilhando() {
  const g = new THREE.Group();
  const e = estrela3D(1.0, 0.32, plastico('#ffcf1f', { rugosidade: 0.15, verniz: 1, rugosidadeVerniz: 0.08, emissivo: '#ffb300', ambiente: 0.6 }), 5, { segmentos: 8 });
  e.rotation.set(0.12, -0.2, 0);
  g.add(e);
  g.add(em(halo(1.5, '#ffd75e'), 0, 0, -0.45));
  const branco = plastico('#fffbe6', { rugosidade: 0.15, verniz: 1, emissivo: '#fff2b0', ambiente: 0.6 });
  for (const [x, y, r] of [[0.98, 0.88, 0.24], [-1.08, 0.6, 0.17], [-0.88, -0.9, 0.2], [1.1, -0.5, 0.14]]) g.add(em(faisca(r, branco), x, y, 0.2));
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

// ---------------------------------------------------------------- contas do progresso

/** Cores das contas (as seis cores dos blocos de resposta e das argolas). */
export const CORES_DAS_CONTAS = {
  vermelha: '#ff4a55', laranja: '#ff8a1c', amarela: '#ffc21a', verde: '#2fc25b', azul: '#3a86ff', roxa: '#9a5cff',
};

/** Conta de plástico brilhante (bolinha levemente achatada, como na maquete 2_jogo). */
export function conta(cor) {
  return () => {
    const g = new THREE.Group();
    const b = bola(0.5, plastico(cor, { rugosidade: 0.18, verniz: 1, rugosidadeVerniz: 0.08, ambiente: 0.5 }), 64);
    b.scale.set(1, 1, 0.9);
    g.add(b);
    ancora(g, 'centro', 0, 0, 0);
    return g;
  };
}

/** Conta ainda não ganha: perolada clara. */
export const contaVazia = () => {
  const g = new THREE.Group();
  const b = bola(0.5, plastico('#ece6fb', { rugosidade: 0.3, verniz: 1, rugosidadeVerniz: 0.15, ambiente: 0.45 }), 64);
  b.scale.set(1, 1, 0.9);
  g.add(b);
  ancora(g, 'centro', 0, 0, 0);
  return g;
};

// ---------------------------------------------------------------- ícones da barra

export function iconeFechar() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(traco([-0.62, -0.62], [0.62, 0.62], 0.2, m), traco([-0.62, 0.62], [0.62, -0.62], 0.2, m));
  return glifo(g);
}

export function iconeVoltar() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(traco([-0.78, 0], [0.82, 0], 0.19, m), traco([-0.78, 0], [-0.18, 0.62], 0.19, m), traco([-0.78, 0], [-0.18, -0.62], 0.19, m));
  return glifo(g);
}

function altoFalante(m) {
  const f = poligono([[-0.95, -0.32], [-0.5, -0.32], [-0.02, -0.74], [-0.02, 0.74], [-0.5, 0.32], [-0.95, 0.32]], 0.14);
  return extrudar(f, 0.3, m, 0.09);
}

export function iconeSomLigado() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(altoFalante(m));
  g.add(arcoDeTubo([0.02, 0], 0.45, -0.75, 0.75, 0.12, m));
  g.add(arcoDeTubo([0.02, 0], 0.82, -0.8, 0.8, 0.12, m));
  return glifo(g);
}

export function iconeSomDesligado() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(altoFalante(m));
  g.add(traco([0.3, -0.34], [0.94, 0.3], 0.13, m), traco([0.3, 0.3], [0.94, -0.34], 0.13, m));
  return glifo(g);
}

export function iconeAjustes() {
  // engrenagem de 8 dentes com furo no meio
  const dentes = 8, R = 1.0, r = 0.78;
  const pts = [];
  for (let i = 0; i < dentes; i++) {
    const a = (i / dentes) * Math.PI * 2;
    const meio = Math.PI / dentes;
    for (const [da, rr] of [[-meio * 0.92, r], [-meio * 0.5, R], [meio * 0.5, R], [meio * 0.92, r]]) {
      pts.push([Math.cos(a + da) * rr, Math.sin(a + da) * rr]);
    }
  }
  const f = poligono(pts, pts.map((_, i) => (i % 4 === 0 || i % 4 === 3 ? 0.04 : 0.06)));
  const furo = new THREE.Path();
  furo.absarc(0, 0, 0.3, 0, Math.PI * 2, true);
  f.holes.push(furo);
  const g = new THREE.Group();
  const e = extrudar(f, 0.3, roxo(), 0.09);
  e.rotation.z = Math.PI / dentes / 2;
  g.add(e);
  return glifo(g);
}

export function iconePais() {
  // cadeado: área dos pais
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(caixa(1.36, 1.08, 0.5, 0.22, m), 0, -0.36, 0));
  const alca = new THREE.Group();
  alca.add(malha(new THREE.TorusGeometry(0.42, 0.13, 20, 48, Math.PI), m));
  for (const x of [-0.42, 0.42]) alca.add(em(cilindro(0.13, 0.13, 0.32, m, 24), x, -0.16, 0));
  g.add(em(alca, 0, 0.36, 0));
  const branco = brancoPlastico();
  g.add(em(cilindro(0.14, 0.14, 0.08, branco, 32), 0, -0.26, 0.25));
  g.children[g.children.length - 1].rotation.x = Math.PI / 2;
  g.add(em(caixa(0.12, 0.3, 0.08, 0.04, branco), 0, -0.46, 0.25));
  return glifo(g);
}

export function iconeCasa() {
  const f = poligono([[-0.66, -0.86], [0.66, -0.86], [0.66, 0.06], [0.98, 0.06], [0, 0.94], [-0.98, 0.06], [-0.66, 0.06]], [0.14, 0.14, 0.04, 0.12, 0.14, 0.12, 0.04]);
  const g = new THREE.Group();
  const m = roxo();
  g.add(extrudar(f, 0.3, m, 0.09));
  // chaminé
  g.add(em(caixa(0.24, 0.42, 0.34, 0.07, m), 0.52, 0.5, -0.02));
  // porta branca em arco
  const porta = new THREE.Shape();
  porta.moveTo(-0.2, -0.86); porta.lineTo(0.2, -0.86); porta.lineTo(0.2, -0.32); porta.absarc(0, -0.32, 0.2, 0, Math.PI, false); porta.closePath();
  g.add(em(extrudar(porta, 0.04, brancoPlastico(), 0.04), 0, 0.02, 0.21));
  return glifo(g);
}

export function iconeLoja() {
  // sacola de compras com estrela na frente
  const g = new THREE.Group();
  const m = roxo();
  const corpo = extrudar(poligono([[-0.74, -0.9], [0.74, -0.9], [0.64, 0.42], [-0.64, 0.42]], 0.14), 0.38, m, 0.1);
  g.add(corpo);
  g.add(arcoDeTubo([0, 0.42], 0.34, 0, Math.PI, 0.1, m));
  const e = estrela3D(0.3, 0.05, brancoPlastico(), 5, { segmentos: 5 });
  g.add(em(e, 0, -0.26, 0.31));
  return glifo(g);
}
