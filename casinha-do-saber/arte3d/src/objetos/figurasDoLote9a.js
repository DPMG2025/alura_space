// Figuras das perguntas, Fase 9 lote 1 (PRD visual 3D, seção 5): mais emojis do vocabulário das perguntas viram
// brinquedos de plástico com verniz, no mesmo molde das figuras da Fase 4 (objetos/figuras.js): de frente ou
// três quartos, base em y = 0, cores chapadas e saturadas, olhinhos brilhantes e bochechas nos bichos.
// A lista (id, emojis, lugar) fica em src/catalogo_fase9a.js.
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em, naSuperficie } from '../kit.js';
import { pecasDosItens, maisModelosDosItens as I } from './itensDaCasa.js';
import { pecasDasFiguras } from './figuras.js';
import { estrela3D } from './estrela3D.js';
import { livroFechado, livroAberto, lapis as lapisDoPremio, paleta as paletaDoPremio, pianinho, medalha as medalhaDoPremio } from './enfeites.js';
import { trofeu as trofeuDoPremio } from './premio.js';
import { presente as presenteDaCasa } from './presente.js';

const { segmento, olho, sorriso, bochecha, luz } = pecasDosItens;
const { pl, brilho, preto, branco, torneado, tubo, estufado, carinha, bichinho, orelhasDeGato, bigodes, rabo, coracao: coracaoVermelho } = pecasDasFiguras;

const V = (x, y, z) => new THREE.Vector3(x, y, z);
const curva = (pts) => new THREE.CatmullRomCurve3(pts.map((p) => V(...p)));
const grupo = (...f) => { const g = new THREE.Group(); for (const o of f) if (o) g.add(o); return g; };
const fosco = (cor, o = {}) => pl(cor, { rugosidade: 0.62, verniz: 0.18, ...o });
const vidro = () => plastico('#a9e1ff', { rugosidade: 0.05, verniz: 1, emissivo: '#cfeaff', ambiente: 0.6 });
const metal = (cor = '#d6dbe4') => plastico(cor, { rugosidade: 0.2, verniz: 1 });
const roda = (r, l, cor = '#2b2b3a') => {
  const g = new THREE.Group();
  const p = cilindro(r, r, l, plastico(cor, { rugosidade: 0.5, verniz: 0.3 }), 32); p.rotation.x = Math.PI / 2; g.add(p);
  const c = cilindro(r * 0.45, r * 0.45, l + 0.02, brilho('#ffd23f'), 20); c.rotation.x = Math.PI / 2; g.add(c);
  return g;
};
/** Embrulha [g] num grupo (para girar sem perder a base em y = 0). */
const girado = (g, x = 0, y = 0, z = 0) => { g.rotation.set(x, y, z); return grupo(g); };
/** Forma 2D a partir de pontos (polígono). */
const poli = (pts) => { const s = new THREE.Shape(); s.moveTo(...pts[0]); for (const p of pts.slice(1)) s.lineTo(...p); s.closePath(); return s; };
/** Olho grande de desenho (branco com pupila), virado para +z. */
function olhao(r) {
  const g = new THREE.Group();
  const b = bola(r, branco(), 24); b.scale.set(1, 1.1, 0.6); g.add(b);
  g.add(em(olho(r * 0.55), 0, -r * 0.05, r * 0.42));
  return g;
}

// =============================================================================================
// Bichos do mar e da água (lagoa)

function polvo() {
  const g = new THREE.Group();
  const m = pl('#ff6fa8'), mc = pl('#ffc2dc');
  const cab = torneado([[0, 0.45], [0.42, 0.5], [0.58, 0.85], [0.55, 1.2], [0.36, 1.48], [0, 1.55]], m, 48);
  g.add(cab);
  for (let k = 0; k < 8; k++) {
    const a = (k / 8) * Math.PI * 2 + 0.2;
    const c = Math.cos(a), s = Math.sin(a);
    const pts = [[c * 0.3, 0.55, s * 0.3], [c * 0.55, 0.22, s * 0.55], [c * 0.82, 0.08, s * 0.82], [c * 0.98, 0.18, s * 0.98], [c * 0.9, 0.3, s * 0.9]];
    g.add(tubo(curva(pts), (t) => 0.14 * (1 - t * 0.7), m, { seg: 30, radial: 14 }));
    for (const t of [0.35, 0.55, 0.72]) { const p = curva(pts).getPointAt(t); g.add(em(bola(0.035, mc, 10), p.x, p.y - 0.08 * (1 - t), p.z)); }
  }
  const R = 0.56;
  const rosto = new THREE.Group();
  carinha(rosto, R, { y: 0.0, abertura: 0.34, yBoca: -0.28 });
  g.add(em(rosto, 0, 0.92, 0.02));
  for (const [x, y, r] of [[-0.3, 1.32, 0.07], [0.25, 1.4, 0.05], [0.4, 1.18, 0.06]]) { const p = bola(r, mc, 12); naSuperficie(p, 0.56, x, y - 0.95, -0.02); p.position.y += 0.95; g.add(p); }
  g.rotation.y = 0.2;
  return g;
}

function caranguejo() {
  const g = new THREE.Group();
  const m = pl('#ff4a3a'), mc = pl('#ff8a6a');
  const corpo = bola(0.6, m, 48); corpo.scale.set(1.25, 0.6, 0.9); g.add(em(corpo, 0, 0.42, 0));
  for (const l of [-1, 1]) {
    g.add(segmento([l * 0.18, 0.62, 0.3], [l * 0.22, 0.95, 0.35], 0.045, m));
    const o = olhao(0.1); g.add(em(o, l * 0.22, 1.0, 0.38));
    g.add(em(bochecha(0.08), l * 0.42, 0.42, 0.5));
    // pernas
    for (const k of [0, 1, 2]) {
      const z = -0.3 + k * 0.25;
      g.add(segmento([l * 0.62, 0.35, z], [l * 0.95, 0.38, z * 1.2], 0.05, m), segmento([l * 0.95, 0.38, z * 1.2], [l * 1.05, 0.04, z * 1.3], 0.045, m));
    }
    // braço e pinça
    g.add(segmento([l * 0.55, 0.5, 0.3], [l * 0.85, 0.75, 0.5], 0.07, m));
    const p = new THREE.Group();
    const a = bola(0.2, mc, 24); a.scale.set(0.9, 1.15, 0.7); p.add(a);
    const b = bola(0.13, mc, 20); b.scale.set(0.7, 1.1, 0.6); p.add(em(b, l * 0.14, 0.15, 0));
    p.rotation.z = -l * 0.35;
    g.add(em(p, l * 0.9, 0.95, 0.52));
  }
  const s = sorriso(0.14, 0.025, 2.3); g.add(em(s, 0, 0.45, 0.72));
  g.rotation.x = 0.05;
  return g;
}

/** Corpo fusiforme de bicho do mar (golfinho, tubarão), deitado ao longo de x. */
function fuso(cor, comp, alto, largo, mat) {
  const c = torneado([[0, -comp * 0.5], [alto * 0.5, -comp * 0.42], [alto * 0.95, -comp * 0.15], [alto, comp * 0.05], [alto * 0.75, comp * 0.3], [alto * 0.3, comp * 0.48], [0, comp * 0.5]], mat || pl(cor), 40);
  c.rotation.z = -Math.PI / 2;
  c.scale.set(1, 1, largo / alto);
  return grupo(c);
}

function golfinho() {
  const g = new THREE.Group();
  const m = pl('#4aa8ff'), mc = pl('#dff1ff');
  const pose = new THREE.Group(); pose.add(fuso('#4aa8ff', 1.7, 0.42, 0.4, m));
  const bar = bola(0.36, mc, 28); bar.scale.set(1.9, 0.6, 0.85); pose.add(em(bar, 0.12, -0.16, 0));
  const bico = capsula(0.11, 0.2, mc); bico.rotation.z = Math.PI / 2; pose.add(em(bico, 0.92, -0.08, 0));
  const dors = malha(new THREE.ConeGeometry(0.17, 0.42, 16), m); dors.rotation.z = -0.5; pose.add(em(dors, -0.05, 0.45, 0));
  const cauda = new THREE.Shape(); cauda.moveTo(0, 0); cauda.quadraticCurveTo(-0.15, 0.3, -0.05, 0.42); cauda.quadraticCurveTo(-0.25, 0.2, -0.3, 0); cauda.quadraticCurveTo(-0.25, -0.2, -0.05, -0.42); cauda.quadraticCurveTo(-0.15, -0.3, 0, 0);
  const ca = estufado(cauda, m, 0.04, 0.05); ca.rotation.x = Math.PI / 2; pose.add(em(ca, -0.85, 0, 0));
  for (const l of [-1, 1]) {
    const n = bola(0.16, m, 16); n.scale.set(1.3, 0.25, 0.6); n.rotation.set(l * 0.6, 0, -0.5); pose.add(em(n, 0.25, -0.22, l * 0.36));
    const o = olho(0.08); o.rotation.y = l * 1.1; pose.add(em(o, 0.62, 0.12, l * 0.27));
    const b = bochecha(0.06); b.rotation.y = l * 1.2; pose.add(em(b, 0.7, -0.05, l * 0.25));
  }
  const s = sorriso(0.1, 0.02, 2.2); s.rotation.y = Math.PI / 2; pose.add(em(s, 0.86, -0.14, 0));
  pose.rotation.z = 0.3;
  g.add(pose);
  g.rotation.y = -0.7;
  return assentar(g, 0.02);
}

function tubarao() {
  const g = new THREE.Group();
  const m = pl('#7d96b5'), mc = pl('#f2f6fb');
  const pose = new THREE.Group();
  pose.add(fuso('#7d96b5', 1.8, 0.48, 0.45, m));
  const bar = bola(0.4, mc, 28); bar.scale.set(1.9, 0.55, 0.9); pose.add(em(bar, 0.15, -0.2, 0));
  const dors = malha(new THREE.ConeGeometry(0.22, 0.55, 16), m); dors.rotation.z = -0.45; pose.add(em(dors, -0.05, 0.55, 0));
  const cauda = poli([[0, 0], [-0.32, 0.5], [-0.18, 0.02], [-0.3, -0.35]]);
  const ca = estufado(cauda, m, 0.05, 0.05); pose.add(em(ca, -0.82, 0, 0));
  for (const l of [-1, 1]) {
    const n = malha(new THREE.ConeGeometry(0.13, 0.4, 12), m); n.rotation.set(l * 1.2, 0, -2.2); pose.add(em(n, 0.2, -0.3, l * 0.38));
    const o = olho(0.075); o.rotation.y = l * 1.1; pose.add(em(o, 0.58, 0.15, l * 0.31));
  }
  // sorriso com dentinhos
  const boca = malha(new THREE.TorusGeometry(0.17, 0.035, 10, 24, Math.PI), pl('#7d1f24')); boca.rotation.set(0, Math.PI / 2, Math.PI); boca.scale.z = 1.6;
  pose.add(em(boca, 0.66, -0.06, 0));
  for (const z of [-0.12, -0.04, 0.04, 0.12]) { const d = malha(new THREE.ConeGeometry(0.025, 0.06, 6), branco()); d.rotation.x = Math.PI; pose.add(em(d, 0.69 - Math.abs(z) * 0.4, -0.1, z * 1.6)); }
  pose.rotation.z = 0.12;
  g.add(em(pose, 0, 0.7, 0));
  g.rotation.y = -0.5;
  return g;
}

function baleia() {
  const g = new THREE.Group();
  const m = pl('#3d7bff'), mc = pl('#cfe3ff');
  const corpo = bola(0.75, m, 48); corpo.scale.set(1.25, 0.8, 0.95); g.add(em(corpo, 0, 0.62, 0));
  const bar = bola(0.6, mc, 32); bar.scale.set(1.3, 0.5, 0.9); g.add(em(bar, 0.1, 0.38, 0.1));
  for (let k = -2; k <= 2; k++) g.add(em(caixa(0.05, 0.02, 0.6, 0.01, pl('#a9c8ff')), 0.15 + k * 0.14, 0.24, 0.12));
  const cauda = new THREE.Group();
  for (const l of [-1, 1]) { const f = bola(0.26, m, 20); f.scale.set(1.4, 0.3, 0.6); f.rotation.set(0, l * 0.5, l * 0.5); cauda.add(em(f, l * 0.2, 0.15, 0)); }
  cauda.add(em(capsula(0.16, 0.3, m), 0, -0.1, 0));
  cauda.rotation.z = 0.6;
  g.add(em(cauda, -1.0, 0.95, 0));
  for (const l of [-1, 1]) {
    const o = olho(0.09); o.rotation.y = l * 0.9; g.add(em(o, 0.55, 0.75, l * 0.55));
    const b = bochecha(0.08); b.rotation.y = l * 0.9; g.add(em(b, 0.68, 0.55, l * 0.52));
    const n = bola(0.2, m, 16); n.scale.set(1.2, 0.3, 0.6); n.rotation.set(l * 0.6, 0, -0.4); g.add(em(n, 0.2, 0.3, l * 0.7));
  }
  const s = sorriso(0.22, 0.03, 2.0); s.rotation.y = Math.PI / 2; g.add(em(s, 0.92, 0.55, 0));
  // esguicho
  const agua = plastico('#7fd3ff', { rugosidade: 0.1, verniz: 1, emissivo: '#bfeaff' });
  g.add(tubo(curva([[0.25, 1.15, 0], [0.25, 1.45, 0]]), () => 0.05, agua, { seg: 8, radial: 10 }));
  for (const l of [-1, 1]) g.add(tubo(curva([[0.25, 1.45, 0], [0.25 + l * 0.15, 1.6, 0], [0.25 + l * 0.32, 1.48, 0]]), (t) => 0.05 * (1 - t * 0.4), agua, { seg: 16, radial: 10 }));
  g.rotation.y = -0.6;
  return g;
}

function concha() {
  const g = new THREE.Group();
  const m = pl('#ffb38a'), mc = pl('#ffd9c2');
  const leque = new THREE.Group();
  const n = 9, comp = 0.82;
  for (let k = 0; k < n; k++) {
    const a = (-0.5 + k / (n - 1)) * Math.PI * 0.8;
    const r = capsula(0.1, comp, k % 2 ? mc : m);
    r.scale.set(1, 1, 0.7);
    r.position.set(Math.sin(a) * comp * 0.55, Math.cos(a) * comp * 0.55, 0.02);
    r.rotation.z = -a;
    leque.add(r);
  }
  const s = new THREE.Shape(); s.moveTo(0, 0); s.absarc(0, 0, 1.0, Math.PI * 0.1, Math.PI * 0.9, false); s.closePath();
  leque.add(em(estufado(s, m, 0.06, 0.05), 0, 0, -0.06));
  for (const l of [-1, 1]) { const o = bola(0.15, m, 16); o.scale.set(1.2, 0.8, 0.5); leque.add(em(o, l * 0.15, 0.02, 0)); }
  leque.add(em(bola(0.06, plastico('#ffffff', { rugosidade: 0.1, verniz: 1 }), 12), 0, 0.12, 0.12));
  leque.rotation.x = -0.35;
  g.add(leque);
  g.rotation.y = -0.15;
  return assentar(g);
}

function camarao() {
  const g = new THREE.Group();
  const m = pl('#ff8a5a'), mc = pl('#ffc6a6');
  // corpo em arco: segmentos ao longo de um meio anel
  const n = 7;
  for (let k = 0; k < n; k++) {
    const t = k / (n - 1);
    const a = Math.PI * 0.15 + t * Math.PI * 1.05;
    const r = 0.26 - t * 0.14;
    const s = bola(r, k % 2 ? mc : m, 24); s.scale.set(1, 1, 0.9);
    g.add(em(s, Math.cos(a) * 0.45, 0.55 + Math.sin(a) * 0.45, 0));
  }
  const cab = bola(0.28, m, 32); cab.scale.set(1.15, 1, 0.9); g.add(em(cab, 0.42, 0.72, 0));
  for (const l of [-1, 1]) { const o = olho(0.06); g.add(em(o, 0.55, 0.85, l * 0.15)); o.rotation.y = 0.6 * l + 0.8; }
  g.add(em(bochecha(0.06), 0.66, 0.68, 0.14));
  for (const l of [-1, 1]) g.add(tubo(curva([[0.6, 0.9, l * 0.08], [0.8, 1.25, l * 0.15], [0.55, 1.5, l * 0.25]]), () => 0.018, m, { seg: 20, radial: 6 }));
  // cauda em leque
  for (const a of [-0.5, 0, 0.5]) { const f = bola(0.12, m, 12); f.scale.set(0.5, 1.4, 0.3); f.rotation.z = a + 0.6; g.add(em(f, -0.35 + a * 0.1, 0.2, 0)); }
  for (let k = 0; k < 4; k++) g.add(segmento([0.3 - k * 0.15, 0.4, 0.1], [0.32 - k * 0.15, 0.12, 0.15], 0.025, mc));
  g.rotation.y = -0.4;
  return girado(g);
}

function lula() {
  const g = new THREE.Group();
  const m = pl('#c48cff'), mc = pl('#e6ccff');
  const manto = torneado([[0, 0.55], [0.35, 0.6], [0.4, 0.9], [0.33, 1.3], [0.15, 1.65], [0, 1.75]], m, 40);
  g.add(manto);
  for (const l of [-1, 1]) { const b = bola(0.25, m, 16); b.scale.set(0.9, 0.6, 0.25); b.rotation.z = l * 0.6; g.add(em(b, l * 0.25, 1.6, 0)); }
  const R = 0.38;
  const rosto = new THREE.Group(); carinha(rosto, R, { y: 0.05, abertura: 0.36, yBoca: -0.32 }); g.add(em(rosto, 0, 0.85, 0.0));
  for (let k = 0; k < 8; k++) {
    const a = (k / 8) * Math.PI * 2;
    const c = Math.cos(a), s = Math.sin(a);
    const comp = k % 4 === 0 ? 0.62 : 0.45;
    g.add(tubo(curva([[c * 0.22, 0.6, s * 0.22], [c * 0.3, 0.35, s * 0.3], [c * 0.32, 0.6 - comp, s * 0.32 + 0.0]].map(([x, y, z]) => [x, Math.max(y, 0.07), z])), (t) => 0.07 * (1 - t * 0.6), k % 2 ? mc : m, { seg: 20, radial: 10 }));
  }
  g.rotation.y = 0.2;
  return g;
}

function jacare() {
  const g = new THREE.Group();
  const m = pl('#3fae5a'), mc = pl('#c8eaa0');
  const corpo = bola(0.45, m, 40); corpo.scale.set(1.45, 0.75, 0.9); g.add(em(corpo, -0.15, 0.32, 0));
  g.add(tubo(curva([[-0.7, 0.3, 0], [-1.0, 0.42, -0.05], [-1.12, 0.78, -0.1], [-0.98, 0.98, -0.1]]), (t) => 0.22 * (1 - t * 0.8), m, { seg: 24, radial: 16 }));
  for (let k = 0; k < 6; k++) { const c = malha(new THREE.ConeGeometry(0.05, 0.12, 4), pl('#2a8a45')); g.add(em(c, -0.5 + k * 0.17, 0.6, 0)); }
  const cab = new THREE.Group();
  const top = bola(0.3, m, 32); top.scale.set(1.9, 0.5, 0.95); cab.add(em(top, 0.3, 0.06, 0));
  const bai = bola(0.28, mc, 32); bai.scale.set(1.9, 0.35, 0.9); cab.add(em(bai, 0.3, -0.12, 0));
  for (const l of [-1, 1]) {
    const ov = bola(0.13, m, 20); cab.add(em(ov, 0.05, 0.2, l * 0.16));
    const o = olho(0.07); o.rotation.y = l * 0.4; cab.add(em(o, 0.13, 0.24, l * 0.18));
    for (const x of [0.25, 0.45, 0.65]) { const d = malha(new THREE.ConeGeometry(0.025, 0.07, 6), branco()); d.rotation.x = Math.PI; cab.add(em(d, x, -0.04, l * 0.22)); }
    cab.add(em(bola(0.025, preto(), 8), 0.84, 0.12, l * 0.08));
  }
  cab.scale.setScalar(1.35);
  cab.rotation.z = 0.3;
  g.add(em(cab, 0.5, 0.48, 0));
  for (const [x, z] of [[0.2, 0.35], [0.2, -0.35], [-0.5, 0.35], [-0.5, -0.35]]) { const p = bola(0.12, m, 16); p.scale.set(1.3, 0.6, 1); g.add(em(p, x, 0.07, z)); }
  g.rotation.y = -0.62;
  return g;
}

function peixinho() {
  // peixe tropical (🐠): amarelo com faixas azuis, de lado
  const g = new THREE.Group();
  const m = pl('#ffcf2a'), az = pl('#2e7bff');
  const corpo = bola(0.6, m, 48); corpo.scale.set(1.0, 1.0, 0.45); g.add(em(corpo, 0, 0.7, 0));
  for (const x of [-0.25, 0.2]) { const f = bola(0.6, az, 40); f.scale.set(0.09, 0.95, 0.47); g.add(em(f, x, 0.7, 0)); }
  const cauda = new THREE.Shape(); cauda.moveTo(0, 0); cauda.lineTo(-0.45, 0.42); cauda.quadraticCurveTo(-0.32, 0, -0.45, -0.42); cauda.closePath();
  g.add(em(estufado(cauda, az, 0.05, 0.06), -0.52, 0.7, 0));
  const dorsal = new THREE.Shape(); dorsal.moveTo(-0.4, 0); dorsal.quadraticCurveTo(-0.1, 0.5, 0.3, 0.05); dorsal.closePath();
  g.add(em(estufado(dorsal, az, 0.03, 0.04), 0, 1.15, 0));
  const ventral = new THREE.Shape(); ventral.moveTo(-0.3, 0); ventral.quadraticCurveTo(-0.1, -0.35, 0.15, 0); ventral.closePath();
  g.add(em(estufado(ventral, az, 0.03, 0.04), 0, 0.25, 0));
  const o = olho(0.1); o.rotation.y = 0.3; g.add(em(o, 0.33, 0.85, 0.24));
  g.add(em(bochecha(0.07), 0.42, 0.62, 0.2));
  const boca = malha(new THREE.TorusGeometry(0.05, 0.025, 10, 20), pl('#ff5a7a')); boca.rotation.y = Math.PI / 2; g.add(em(boca, 0.6, 0.62, 0.03));
  g.rotation.y = -0.4;
  return g;
}

function baiacu() {
  const g = new THREE.Group();
  const m = pl('#ffd36a'), mc = pl('#fff3d6');
  g.add(em(bola(0.62, m, 48), 0, 0.7, 0));
  const bar = bola(0.5, mc, 32); bar.scale.set(1, 0.8, 0.7); g.add(em(bar, 0, 0.55, 0.22));
  const esp = pl('#e8a23a');
  for (let i = 0; i < 46; i++) {
    const y = 1 - (i / 45) * 2, r = Math.sqrt(1 - y * y), a = i * 2.4;
    const n = V(Math.cos(a) * r, y, Math.sin(a) * r);
    if (n.z > 0.55 && Math.abs(n.y) < 0.6) continue; // sem espinho no rosto
    const c = malha(new THREE.ConeGeometry(0.045, 0.16, 8), esp);
    c.position.copy(n.clone().multiplyScalar(0.66)).add(V(0, 0.7, 0));
    c.quaternion.setFromUnitVectors(V(0, 1, 0), n);
    g.add(c);
  }
  const R = 0.62;
  const rosto = new THREE.Group(); carinha(rosto, R, { y: 0.12, abertura: 0.32, boca: false }); g.add(em(rosto, 0, 0.7, 0));
  const boca = malha(new THREE.TorusGeometry(0.07, 0.035, 10, 20), pl('#ff5a7a')); g.add(em(boca, 0, 0.5, 0.6));
  for (const l of [-1, 1]) { const n = bola(0.14, pl('#ffb33a'), 16); n.scale.set(0.3, 1, 0.7); g.add(em(n, l * 0.64, 0.65, 0.1)); }
  const cauda = bola(0.2, pl('#ffb33a'), 16); cauda.scale.set(0.3, 1, 1); g.add(em(cauda, 0, 0.75, -0.7));
  g.rotation.y = 0.35;
  return g;
}

function onda() {
  const g = new THREE.Group();
  const az = pl('#2f8cff'), cl = pl('#7fd3ff'), esp = pl('#ffffff', { rugosidade: 0.5, verniz: 0.3 });
  const s = new THREE.Shape();
  s.moveTo(-0.9, 0); s.lineTo(0.95, 0); s.lineTo(0.95, 0.45);
  s.bezierCurveTo(0.95, 1.1, 0.45, 1.42, -0.05, 1.28);
  s.bezierCurveTo(-0.48, 1.15, -0.5, 0.72, -0.18, 0.66);
  s.bezierCurveTo(0.02, 0.62, 0.12, 0.78, 0.04, 0.9);
  s.bezierCurveTo(0.32, 0.82, 0.28, 0.42, -0.1, 0.4);
  s.bezierCurveTo(-0.5, 0.38, -0.72, 0.32, -0.9, 0.28);
  s.closePath();
  g.add(estufado(s, az, 0.3, 0.06));
  const s2 = new THREE.Shape(); s2.moveTo(-0.9, 0); s2.lineTo(0.95, 0); s2.lineTo(0.95, 0.22); s2.bezierCurveTo(0.4, 0.32, -0.3, 0.12, -0.9, 0.2); s2.closePath();
  g.add(em(estufado(s2, cl, 0.05, 0.05), 0, 0, 0.2));
  const crista = new THREE.CubicBezierCurve(new THREE.Vector2(0.95, 0.45), new THREE.Vector2(0.95, 1.1), new THREE.Vector2(0.45, 1.42), new THREE.Vector2(-0.05, 1.28));
  for (let k = 2; k <= 9; k++) { const p = crista.getPoint(k / 10); g.add(em(bola(0.07 + (k % 2) * 0.025, esp, 14), p.x, p.y, 0.2)); }
  for (const [x, y] of [[-0.25, 1.15], [-0.4, 0.95]]) g.add(em(bola(0.06, esp, 12), x, y, 0.2));
  g.rotation.y = -0.15;
  return g;
}

function barco() {
  // veleiro (⛵)
  const g = new THREE.Group();
  const casco = new THREE.Shape(); casco.moveTo(-0.85, 0.4); casco.lineTo(0.95, 0.4); casco.quadraticCurveTo(0.7, 0.0, 0.45, 0.0); casco.lineTo(-0.5, 0.0); casco.quadraticCurveTo(-0.75, 0.05, -0.85, 0.4);
  const c = estufado(casco, brilho('#ef3b4f'), 0.5, 0.08); g.add(c);
  g.add(em(caixa(1.7, 0.08, 0.62, 0.03, pl('#d9975a')), 0.05, 0.44, 0));
  g.add(em(caixa(1.82, 0.08, 0.66, 0.04, brilho('#ffffff')), 0.05, 0.3, 0));
  g.add(em(cilindro(0.04, 0.04, 1.5, pl('#8a5a3a'), 12), 0, 1.15, 0));
  const vela = poli([[0.05, 0.1], [0.05, 1.3], [0.85, 0.12]]);
  g.add(em(estufado(vela, pl('#ffffff', { rugosidade: 0.5 }), 0.03, 0.04), 0, 0.5, 0));
  const vela2 = poli([[-0.05, 0.2], [-0.05, 1.15], [-0.6, 0.2]]);
  g.add(em(estufado(vela2, pl('#ffd23f', { rugosidade: 0.5 }), 0.03, 0.04), 0, 0.5, 0));
  const band = poli([[0, 0], [0.3, 0.09], [0, 0.18]]); g.add(em(estufado(band, pl('#3d8bff'), 0.02, 0.02), 0.02, 1.82, 0));
  for (const x of [-0.45, -0.05, 0.4]) { const v = cilindro(0.08, 0.08, 0.05, vidro(), 20); v.rotation.x = Math.PI / 2; g.add(em(v, x, 0.2, 0.3)); }
  g.rotation.y = -0.45;
  return g;
}

function navio() {
  const g = new THREE.Group();
  const casco = new THREE.Shape(); casco.moveTo(-1.0, 0.45); casco.lineTo(1.15, 0.45); casco.quadraticCurveTo(0.9, 0.0, 0.7, 0.0); casco.lineTo(-0.85, 0.0); casco.quadraticCurveTo(-1.0, 0.1, -1.0, 0.45);
  g.add(estufado(casco, brilho('#2e5fd8'), 0.65, 0.08));
  g.add(em(caixa(2.05, 0.1, 0.82, 0.04, brilho('#ef3b4f')), 0.07, 0.1, 0));
  g.add(em(caixa(1.2, 0.4, 0.6, 0.08, brilho('#ffffff')), -0.1, 0.72, 0));
  g.add(em(caixa(0.7, 0.32, 0.5, 0.08, brilho('#ffffff')), -0.15, 1.06, 0));
  for (const x of [-0.55, -0.3, -0.05, 0.2, 0.4]) { const v = cilindro(0.065, 0.065, 0.04, vidro(), 20); v.rotation.x = Math.PI / 2; g.add(em(v, x, 0.72, 0.31)); }
  for (const x of [-0.6, -0.2, 0.2, 0.6]) { const v = cilindro(0.06, 0.06, 0.04, vidro(), 20); v.rotation.x = Math.PI / 2; g.add(em(v, x, 0.25, 0.42)); }
  for (const x of [-0.35, 0.05]) {
    g.add(em(cilindro(0.13, 0.15, 0.45, brilho('#ffc21a'), 24), x, 1.42, 0));
    g.add(em(cilindro(0.135, 0.135, 0.1, preto(), 24), x, 1.62, 0));
  }
  for (const [x, y, r] of [[0.15, 1.85, 0.1], [0.3, 1.98, 0.13], [0.5, 2.05, 0.15]]) g.add(em(bola(r, pl('#ffffff', { rugosidade: 0.8, verniz: 0.05 }), 16), x, y, 0));
  g.rotation.y = -0.45;
  return g;
}

function canoa() {
  const g = new THREE.Group();
  const m = brilho('#ffa133'), mc = pl('#ffd29a');
  const c = bola(0.5, m, 48); c.scale.set(2.1, 0.55, 0.8);
  const metade = new THREE.Group(); metade.add(c);
  // casco: meia esfera esticada (só a metade de baixo)
  const geo = new THREE.SphereGeometry(0.5, 48, 24, 0, Math.PI * 2, Math.PI / 2, Math.PI / 2);
  const casco = malha(geo, m); casco.scale.set(2.1, 0.75, 0.8); casco.material.side = THREE.DoubleSide; g.add(em(casco, 0, 0.38, 0));
  const borda = malha(new THREE.TorusGeometry(0.5, 0.04, 10, 48), mc); borda.rotation.x = Math.PI / 2; borda.scale.set(2.1, 0.8, 1); g.add(em(borda, 0, 0.38, 0));
  g.add(em(caixa(0.08, 0.06, 0.7, 0.02, pl('#b5713f')), 0.3, 0.33, 0), em(caixa(0.08, 0.06, 0.7, 0.02, pl('#b5713f')), -0.35, 0.33, 0));
  g.add(segmento([-0.6, 0.15, 0.55], [0.7, 0.9, -0.1], 0.035, pl('#b5713f')));
  const pa = bola(0.14, pl('#d9975a'), 16); pa.scale.set(1, 1.8, 0.25); pa.rotation.z = 1.0; g.add(em(pa, -0.62, 0.15, 0.56));
  g.rotation.y = -0.65;
  g.rotation.x = 0.25;
  return g;
}

// =============================================================================================
// Bichos da floresta, da savana e do quintal

function papagaio() {
  const g = new THREE.Group();
  const verm = pl('#ff3b3b'), am = pl('#ffd21a'), az = pl('#2e7bff'), verde = pl('#36c26a');
  const corpo = bola(0.42, verm, 40); corpo.scale.set(1, 1.3, 0.95); g.add(em(corpo, 0, 0.75, 0));
  const R = 0.36;
  const cab = new THREE.Group(); cab.add(bola(R, verm, 32));
  const mascara = bola(R * 0.55, branco(), 20); mascara.scale.set(1, 1, 0.4);
  for (const l of [-1, 1]) { const mm = mascara.clone(); naSuperficie(mm, R, l * R * 0.38, R * 0.08, -R * 0.12); cab.add(mm); }
  carinha(cab, R, { y: 0.1, abertura: 0.38, boca: false, bochechas: false });
  const bico = new THREE.Group();
  const b1 = bola(0.14, plastico('#f2e6d0', { rugosidade: 0.3, verniz: 0.8 }), 20); b1.scale.set(0.9, 1.1, 1); bico.add(b1);
  const b2 = malha(new THREE.ConeGeometry(0.08, 0.16, 12), plastico('#3a2a2a', { rugosidade: 0.3 })); b2.rotation.x = Math.PI; bico.add(em(b2, 0, -0.14, 0.04));
  cab.add(em(bico, 0, -0.08, R * 0.95));
  g.add(em(cab, 0, 1.38, 0.05));
  for (const l of [-1, 1]) {
    const asa = new THREE.Group();
    for (const [c, y, s] of [[verm, 0.1, 1], [am, -0.12, 0.9], [verde, -0.3, 0.8], [az, -0.48, 0.7]]) { const p = bola(0.2 * s, c, 16); p.scale.set(0.35, 1.3, 1); asa.add(em(p, 0, y, -0.05 * y)); }
    asa.rotation.z = l * 0.2;
    g.add(em(asa, l * 0.42, 0.8, -0.02));
    g.add(em(bola(0.07, pl('#8a8a9a'), 12), l * 0.14, 0.12, 0.2));
  }
  for (const [c, x, comp] of [[az, -0.08, 0.7], [verm, 0.08, 0.6]]) { const p = capsula(0.07, comp, c); p.rotation.x = -0.35; g.add(em(p, x, 0.2, -0.35)); }
  g.add(em(cilindro(0.06, 0.06, 0.9, massinha('#8a5a3a'), 12).rotateZ(Math.PI / 2), 0, 0.06, 0.15));
  g.rotation.y = 0.3;
  return g;
}

function cobra() {
  const g = new THREE.Group();
  const m = pl('#4cc94a'), mc = pl('#ffe066');
  // corpo enrolado em espiral subindo até a cabeça
  const pts = [];
  for (let i = 0; i <= 40; i++) {
    const t = i / 40;
    const a = t * Math.PI * 3.2;
    const r = 0.62 - t * 0.38;
    pts.push([Math.cos(a) * r, 0.16 + t * 0.55 + (t > 0.85 ? (t - 0.85) * 3 : 0), Math.sin(a) * r]);
  }
  const c = curva(pts);
  g.add(tubo(c, (t) => 0.17 - t * 0.03 + (t < 0.03 ? -0.08 * (1 - t / 0.03) : 0), m, { seg: 120, radial: 20 }));
  for (let k = 1; k < 12; k++) { const p = c.getPointAt(k / 12); const s = bola(0.07, mc, 12); s.scale.set(1, 0.5, 1); g.add(em(s, p.x, p.y + 0.15, p.z)); }
  const fim = c.getPointAt(1);
  const R = 0.28;
  const cab = new THREE.Group(); const cb = bola(R, m, 32); cb.scale.set(1.15, 0.9, 1.1); cab.add(cb);
  carinha(cab, R, { y: 0.15, abertura: 0.38, yBoca: -0.25 });
  const lingua = tubo(curva([[0, -R * 0.4, R * 0.9], [0, -R * 0.55, R * 1.3], [0.05, -R * 0.5, R * 1.5]]), () => 0.015, pl('#ff3b5a'), { seg: 10, radial: 6 });
  cab.add(lingua);
  g.add(em(cab, fim.x, fim.y + 0.25, fim.z + 0.1));
  g.rotation.y = 0.3;
  return g;
}

function zebra() {
  return bichinho({
    cor: '#ffffff', claro: '#ffffff', focinho: 'longo', corFocinho: '#3a3540',
    orelhas: (cab, m, mc, R) => {
      const pt = pl('#2b2230');
      for (const l of [-1, 1]) { const o = malha(new THREE.ConeGeometry(R * 0.2, R * 0.5, 12), m); o.rotation.z = -l * 0.3; cab.add(em(o, l * R * 0.45, R * 0.9, -R * 0.1)); }
      for (let k = 0; k < 6; k++) { const a = 0.1 + k * 0.32; cab.add(em(bola(R * 0.16, pt, 12), 0, Math.cos(a) * R * 1.0, -Math.sin(a) * R * 0.95 + R * 0.1)); }
      for (const [x, y, rz] of [[0, 0.62, 0], [-0.2, 0.5, 0.4], [0.2, 0.5, -0.4]]) { const l = capsula(0.03, 0.12, pt); l.rotation.z = rz; naSuperficie(l, R, x * R * 2, y * R, 0); cab.add(l); }
      for (const l of [-1, 1]) for (const y of [0.2, -0.05]) { const s = capsula(0.028, 0.12, pt); s.rotation.z = Math.PI / 2 + l * 0.25; naSuperficie(s, R, l * R * 0.85, y * R, -0.005); cab.add(s); }
    },
    extras: (g) => {
      const pt = pl('#2b2230');
      for (const l of [-1, 1]) for (const y of [0.32, 0.52, 0.72]) { const s = capsula(0.035, 0.2, pt); s.rotation.z = Math.PI / 2 + l * 0.35; g.add(em(s, l * 0.43, y, 0.1)); }
      for (const l of [-1, 1]) for (const y of [0.15, 0.27]) { const s = capsula(0.025, 0.12, pt); s.rotation.z = Math.PI / 2 + l * 0.1; g.add(em(s, l * 0.36, y, 0.18)); }
      g.add(rabo([[-0.35, 0.4, -0.35], [-0.6, 0.35, -0.3], [-0.7, 0.15, -0.25]], 0.04, pl('#ffffff')));
      g.add(em(bola(0.07, pt, 12), -0.72, 0.1, -0.25));
    },
  });
}

function girafa() {
  const g = new THREE.Group();
  const m = pl('#ffc94a'), mancha = pl('#c9772a'), mc = pl('#fff1d2');
  const corpo = bola(0.45, m, 40); corpo.scale.set(1, 0.9, 0.85); g.add(em(corpo, 0, 0.5, 0));
  for (const l of [-1, 1]) {
    g.add(em(capsula(0.1, 0.25, m), l * 0.18, 0.25, 0.3));
    const pe = bola(0.13, plastico('#7a4a2a', { rugosidade: 0.4, verniz: 0.6 }), 16); pe.scale.set(1, 0.6, 1.2); g.add(em(pe, l * 0.28, 0.07, 0.3));
    const coxa = bola(0.2, m, 20); coxa.scale.set(0.9, 0.8, 1.1); g.add(em(coxa, l * 0.32, 0.2, 0.05));
  }
  const pesc = tubo(curva([[0, 0.7, 0], [0, 1.2, 0.02], [0, 1.65, 0.05]]), (t) => 0.2 - t * 0.06, m, { seg: 20, radial: 20 });
  g.add(pesc);
  for (const [x, y, z, s] of [[0.36, 0.6, 0.25, 0.12], [-0.38, 0.45, 0.22, 0.11], [0.1, 0.78, 0.33, 0.1], [-0.15, 0.3, 0.38, 0.09], [0.12, 1.15, 0.17, 0.07], [-0.1, 1.4, 0.14, 0.065], [0.08, 1.6, 0.12, 0.06]]) {
    const mm = cilindro(s, s, 0.03, mancha, 6); mm.rotation.x = Math.PI / 2; mm.lookAt(V(x * 4, y, z * 4)); mm.rotateX(Math.PI / 2); g.add(em(mm, x, y, z));
  }
  const R = 0.36;
  const cab = new THREE.Group(); cab.add(bola(R, m, 40));
  const f = bola(R * 0.55, mc, 28); f.scale.set(1.15, 0.8, 0.8); naSuperficie(f, R, 0, -R * 0.38, -R * 0.12); cab.add(f);
  for (const l of [-1, 1]) { const n = bola(R * 0.06, plastico('#5a2a2a'), 10); naSuperficie(n, R * 1.18, l * R * 0.18, -R * 0.33, 0); cab.add(n); }
  const s = sorriso(R * 0.15, R * 0.03, 2.0, '#5a2a1a'); naSuperficie(s, R * 1.14, 0, -R * 0.55, 0); cab.add(s);
  carinha(cab, R, { y: 0.22, abertura: 0.42, boca: false });
  for (const l of [-1, 1]) {
    cab.add(segmento([l * R * 0.25, R * 0.8, -R * 0.1], [l * R * 0.32, R * 1.35, -R * 0.1], 0.035, m));
    cab.add(em(bola(0.07, plastico('#7a4a2a', { rugosidade: 0.4, verniz: 0.6 }), 12), l * R * 0.32, R * 1.38, -R * 0.1));
    const o = bola(R * 0.25, m, 16); o.scale.set(1.4, 0.5, 0.5); o.rotation.z = l * -0.4; cab.add(em(o, l * R * 0.98, R * 0.4, -R * 0.1));
  }
  g.add(em(cab, 0, 1.92, 0.12));
  g.add(rabo([[-0.1, 0.5, -0.38], [-0.2, 0.3, -0.5], [-0.22, 0.12, -0.5]], 0.03, m));
  g.rotation.y = 0.3;
  return g;
}

function coruja() {
  const g = new THREE.Group();
  const m = pl('#9a6a4a'), mc = pl('#f1d6b0'), esc = pl('#6e4a32');
  g.add(torneado([[0, 0], [0.45, 0.05], [0.55, 0.45], [0.5, 0.85], [0.42, 1.05], [0, 1.1]], m, 48));
  const bar = bola(0.38, mc, 28); bar.scale.set(1, 1.15, 0.45); g.add(em(bar, 0, 0.45, 0.3));
  for (const [x, y] of [[-0.12, 0.55], [0.12, 0.55], [0, 0.4], [-0.15, 0.3], [0.15, 0.3]]) { const v = malha(new THREE.TorusGeometry(0.05, 0.015, 6, 12, Math.PI), esc); v.rotation.z = Math.PI; g.add(em(v, x, y, 0.47)); }
  const R = 0.48;
  const cab = new THREE.Group(); const cb = bola(R, m, 40); cb.scale.set(1.1, 0.9, 0.95); cab.add(cb);
  for (const l of [-1, 1]) {
    const disco = cilindro(R * 0.42, R * 0.42, 0.05, mc, 32); disco.rotation.x = Math.PI / 2; cab.add(em(disco, l * R * 0.42, R * 0.02, R * 0.78));
    const o = olhao(R * 0.26); cab.add(em(o, l * R * 0.42, R * 0.02, R * 0.83));
    const t = malha(new THREE.ConeGeometry(R * 0.2, R * 0.5, 12), m); t.rotation.z = -l * 0.35; cab.add(em(t, l * R * 0.72, R * 0.78, 0));
    cab.add(em(bochecha(R * 0.12), l * R * 0.7, -R * 0.35, R * 0.62));
  }
  const bico = malha(new THREE.ConeGeometry(R * 0.12, R * 0.3, 12), pl('#ffb000')); bico.rotation.x = Math.PI; cab.add(em(bico, 0, -R * 0.25, R * 0.9));
  g.add(em(cab, 0, 1.38, 0.02));
  for (const l of [-1, 1]) {
    const asa = bola(0.3, esc, 20); asa.scale.set(0.3, 0.9, 0.7); asa.rotation.z = l * 0.15; g.add(em(asa, l * 0.52, 0.55, 0));
    for (const k of [-1, 0, 1]) g.add(em(bola(0.05, pl('#ffb000'), 10), l * 0.18 + k * 0.06, 0.03, 0.38));
  }
  g.add(em(cilindro(0.07, 0.07, 1.2, massinha('#8a5a3a'), 12).rotateZ(Math.PI / 2), 0, 0.0, 0.32));
  g.rotation.y = 0.2;
  return grupo(em(g, 0, 0.07, 0));
}

const gorila = () => bichinho({
  cor: '#4a4552', claro: '#9a909e', corFocinho: '#b6abb8', giro: 0.25, R: 0.48,
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) { const o = bola(R * 0.22, mc, 16); o.scale.set(0.6, 1, 0.6); cab.add(em(o, l * R * 0.98, 0, 0)); }
    const testa = bola(R * 0.75, m, 24); testa.scale.set(1.2, 0.55, 0.8); cab.add(em(testa, 0, R * 0.42, R * 0.25));
    const rosto = bola(R * 0.6, mc, 24); rosto.scale.set(1.1, 0.95, 0.5); naSuperficie(rosto, R, 0, -R * 0.05, -R * 0.35); cab.add(rosto);
  },
  extras: (g, m) => {
    for (const l of [-1, 1]) { const b = capsula(0.14, 0.45, m); b.rotation.z = l * 0.25; g.add(em(b, l * 0.52, 0.45, 0.15)); g.add(em(bola(0.15, pl('#9a909e'), 16), l * 0.58, 0.14, 0.2)); }
  },
});

const urso = () => bichinho({
  cor: '#a8693a', claro: '#f0cf9e', corFocinho: '#f0cf9e', nariz: '#2a1a14',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = bola(R * 0.3, m, 20); o.scale.set(1, 1, 0.6); cab.add(em(o, l * R * 0.68, R * 0.72, -R * 0.05));
      const oi = bola(R * 0.17, mc, 16); oi.scale.set(1, 1, 0.4); cab.add(em(oi, l * R * 0.68, R * 0.72, R * 0.1));
    }
  },
  extras: (g) => g.add(em(bola(0.12, pl('#a8693a'), 16), 0, 0.2, -0.45)),
});

const ursoPolar = () => bichinho({
  cor: '#f6f8fc', claro: '#ffffff', corFocinho: '#ffffff', nariz: '#2a1a14',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = bola(R * 0.26, m, 20); o.scale.set(1, 1, 0.6); cab.add(em(o, l * R * 0.66, R * 0.72, -R * 0.05));
      const oi = bola(R * 0.14, pl('#d9e2f2'), 16); oi.scale.set(1, 1, 0.4); cab.add(em(oi, l * R * 0.66, R * 0.72, R * 0.08));
    }
    const ch = new THREE.Group();
    ch.add(em(cilindro(R * 0.55, R * 0.62, R * 0.18, fosco('#3d8bff'), 32), 0, 0, 0));
    const copa = bola(R * 0.55, fosco('#3d8bff'), 24); copa.scale.set(1, 0.7, 1); ch.add(em(copa, 0, 0.05, 0));
    ch.add(em(bola(R * 0.18, fosco('#ffffff'), 16), 0, R * 0.45, 0));
    ch.rotation.z = 0.25;
    cab.add(em(ch, R * 0.1, R * 0.82, 0));
  },
});

const onca = () => bichinho({
  cor: '#ffb43a', claro: '#fff4e0', nariz: '#3a2420',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) { const o = bola(R * 0.24, m, 16); o.scale.set(1, 1, 0.5); cab.add(em(o, l * R * 0.66, R * 0.7, 0)); const oi = bola(R * 0.13, pl('#3a2420'), 12); oi.scale.set(1, 1, 0.4); cab.add(em(oi, l * R * 0.66, R * 0.7, R * 0.08)); }
    bigodes(cab, R);
    const pt = pl('#3a2420');
    for (const [x, y] of [[-0.35, 0.55], [0.3, 0.6], [0, 0.78], [-0.6, 0.2], [0.62, 0.25]]) { const p = malha(new THREE.TorusGeometry(R * 0.07, R * 0.025, 6, 12), pt); naSuperficie(p, R, x * R, y * R, 0); cab.add(p); }
  },
  extras: (g, m) => {
    const pt = pl('#3a2420');
    for (const [x, y, z] of [[0.38, 0.6, 0.22], [0.42, 0.35, 0.18], [-0.4, 0.5, 0.2], [-0.3, 0.75, 0.25], [0.3, 0.82, 0.2], [-0.45, 0.28, 0.1]]) { const p = malha(new THREE.TorusGeometry(0.05, 0.02, 6, 12), pt); p.lookAt(V(x * 3, y, z * 3)); g.add(em(p, x, y, z)); }
    g.add(rabo([[-0.3, 0.15, -0.3], [-0.65, 0.35, -0.2], [-0.7, 0.75, -0.1], [-0.55, 0.95, 0]], 0.07, m));
  },
});

function preguica() {
  // bicho-preguiça pendurado num galho
  const g = new THREE.Group();
  const m = pl('#a88a6a'), mc = pl('#f0e0c4'), esc = pl('#5a4030');
  g.add(em(cilindro(0.08, 0.08, 1.6, massinha('#8a5a3a'), 16).rotateZ(Math.PI / 2), 0, 1.55, 0));
  for (const [x, z] of [[-0.6, 0.05], [0.65, 0.0]]) { const f = bola(0.14, pl('#4fbf4a'), 16); f.scale.set(1.3, 0.4, 0.7); g.add(em(f, x, 1.68, z)); }
  const corpo = bola(0.42, m, 40); corpo.scale.set(1, 1.15, 0.85); g.add(em(corpo, 0, 0.85, 0));
  for (const l of [-1, 1]) {
    g.add(segmento([l * 0.3, 1.0, 0.05], [l * 0.35, 1.52, 0.05], 0.09, m));
    g.add(segmento([l * 0.25, 0.6, 0.05], [l * 0.42, 1.48, -0.05], 0.09, m));
    for (const k of [-1, 1]) g.add(segmento([l * 0.35 + k * 0.03, 1.55, 0.06], [l * 0.35 + k * 0.05, 1.66, 0.12], 0.02, pl('#3a2a20')));
  }
  const R = 0.34;
  const cab = new THREE.Group(); cab.add(bola(R, m, 32));
  const rosto = bola(R * 0.78, mc, 28); rosto.scale.set(1.1, 0.85, 0.5); naSuperficie(rosto, R, 0, 0, -R * 0.45); cab.add(rosto);
  for (const l of [-1, 1]) { const mk = bola(R * 0.22, esc, 16); mk.scale.set(1.4, 0.7, 0.4); mk.rotation.z = l * 0.4; naSuperficie(mk, R, l * R * 0.36, R * 0.05, -R * 0.06); cab.add(mk); }
  for (const l of [-1, 1]) { const o = olho(R * 0.13); naSuperficie(o, R, l * R * 0.36, R * 0.07, R * 0.04); cab.add(o); }
  const n = bola(R * 0.12, preto(), 12); naSuperficie(n, R, 0, -R * 0.15, R * 0.04); cab.add(n);
  const s = sorriso(R * 0.16, R * 0.03, 2.2, '#5a2a1a'); naSuperficie(s, R, 0, -R * 0.38, 0.0); cab.add(s);
  g.add(em(cab, 0, 1.15, 0.28));
  g.rotation.y = 0.2;
  return g;
}

function lagartixa() {
  const g = new THREE.Group();
  const m = pl('#7ad13a'), mc = pl('#d8f5a8');
  const corpo = bola(0.32, m, 32); corpo.scale.set(1.7, 0.5, 0.85); g.add(em(corpo, 0, 0.18, 0));
  g.add(tubo(curva([[-0.5, 0.15, 0], [-0.85, 0.25, 0], [-0.95, 0.6, 0], [-0.75, 0.78, 0], [-0.62, 0.62, 0]]), (t) => 0.13 * (1 - t * 0.85), m, { seg: 30, radial: 12 }));
  for (const l of [-1, 1]) for (const x of [0.3, -0.3]) {
    g.add(segmento([x, 0.15, l * 0.2], [x + 0.08, 0.06, l * 0.45], 0.05, m));
    for (const k of [-1, 0, 1]) g.add(em(bola(0.035, mc, 8), x + 0.08 + k * 0.05, 0.03, l * 0.5));
  }
  for (let k = 0; k < 4; k++) g.add(em(bola(0.045, pl('#ff8a1c'), 10), 0.25 - k * 0.2, 0.33, 0));
  const R = 0.25;
  const cab = new THREE.Group(); const c = bola(R, m, 32); c.scale.set(1.25, 0.8, 1.0); cab.add(c);
  for (const l of [-1, 1]) { const o = olhao(0.08); o.rotation.y = l * 0.6; cab.add(em(o, 0.02, 0.15, l * 0.15)); }
  const s = sorriso(0.12, 0.018, 2.4); s.rotation.y = Math.PI / 2; cab.add(em(s, 0.27, -0.03, 0));
  cab.rotation.y = -0.6;
  cab.scale.setScalar(1.45);
  cab.rotation.z = 0.25;
  g.add(em(cab, 0.66, 0.3, 0.12));
  g.rotation.y = -0.62;
  return g;
}

function caracol() {
  const g = new THREE.Group();
  const m = pl('#ffd08a'), casca = brilho('#ff8a4a'), cc = brilho('#ffc04d');
  const corpo = capsula(0.17, 1.0, m); corpo.rotation.z = Math.PI / 2; corpo.scale.set(1, 1, 1.2); g.add(em(corpo, 0, 0.17, 0));
  const pesc = tubo(curva([[0.45, 0.2, 0], [0.62, 0.45, 0], [0.62, 0.65, 0]]), () => 0.15, m, { seg: 16, radial: 16 });
  g.add(pesc);
  const R = 0.2; const cab = new THREE.Group(); cab.add(bola(R, m, 28)); carinha(cab, R, { y: 0.1, abertura: 0.4, yBoca: -0.35 });
  for (const l of [-1, 1]) { cab.add(segmento([l * 0.06, R * 0.8, 0], [l * 0.12, R * 1.9, 0.02], 0.022, m)); cab.add(em(bola(0.045, m, 10), l * 0.12, R * 1.95, 0.02)); }
  g.add(em(cab, 0.62, 0.7, 0.04));
  // casca em espiral: anéis de toro diminuindo
  const casc = new THREE.Group();
  const geo = [];
  for (let i = 0; i <= 80; i++) { const t = i / 80; const a = t * Math.PI * 4.2; const r = 0.4 * (1 - t * 0.85); geo.push([Math.cos(a) * r, Math.sin(a) * r, 0.05 * t]); }
  casc.add(tubo(curva(geo), (t) => 0.2 * (1 - t * 0.8), casca, { seg: 160, radial: 18 }));
  const miolo = bola(0.38, cc, 32); miolo.scale.set(1, 1, 0.55); casc.add(em(miolo, 0, 0, -0.05));
  g.add(em(casc, -0.08, 0.68, 0));
  g.rotation.y = -0.4;
  return g;
}

function formiga() {
  const g = new THREE.Group();
  const m = brilho('#d83a2a');
  const abd = bola(0.32, m, 32); abd.scale.set(1.2, 0.95, 1); g.add(em(abd, -0.45, 0.45, 0));
  const tor = bola(0.2, m, 24); g.add(em(tor, 0, 0.45, 0));
  const R = 0.28;
  const cab = new THREE.Group(); cab.add(bola(R, m, 32)); carinha(cab, R, { y: 0.1, abertura: 0.38, yBoca: -0.32 });
  for (const l of [-1, 1]) { cab.add(tubo(curva([[l * 0.08, R * 0.8, 0], [l * 0.18, R * 1.6, 0.0], [l * 0.32, R * 1.85, 0.1]]), () => 0.018, m, { seg: 12, radial: 6 })); cab.add(em(bola(0.04, m, 10), l * 0.32, R * 1.85, 0.1)); }
  cab.rotation.y = 1.0;
  g.add(em(cab, 0.38, 0.62, 0));
  for (const l of [-1, 1]) for (const x of [-0.12, 0, 0.12]) {
    g.add(segmento([x, 0.4, l * 0.12], [x * 2.2, 0.35, l * 0.42], 0.025, m), segmento([x * 2.2, 0.35, l * 0.42], [x * 2.5, 0.02, l * 0.5], 0.025, m));
  }
  g.rotation.y = -0.5;
  return g;
}

function camelo() {
  const g = new THREE.Group();
  const m = pl('#e0a95a'), mc = pl('#f5d7a8');
  const corpo = bola(0.45, m, 40); corpo.scale.set(1.45, 0.75, 0.8); g.add(em(corpo, 0, 0.85, 0));
  for (const x of [-0.25, 0.25]) { const c = bola(0.25, m, 24); c.scale.set(1, 1.2, 0.9); g.add(em(c, x, 1.18, 0)); }
  for (const x of [-0.42, 0.42]) for (const z of [-0.2, 0.2]) {
    g.add(em(capsula(0.08, 0.5, m), x, 0.38, z));
    const pe = bola(0.1, mc, 12); pe.scale.set(1.1, 0.5, 1.2); g.add(em(pe, x, 0.05, z + 0.03));
  }
  g.add(tubo(curva([[0.55, 0.9, 0], [0.8, 0.95, 0], [0.88, 1.35, 0]]), (t) => 0.13 - t * 0.02, m, { seg: 16, radial: 16 }));
  const R = 0.26;
  const cab = new THREE.Group(); const c = bola(R, m, 32); c.scale.set(1.3, 0.95, 1); cab.add(c);
  const f = bola(R * 0.6, mc, 20); f.scale.set(1, 0.8, 1); cab.add(em(f, R * 0.85, -R * 0.2, 0));
  for (const l of [-1, 1]) { const o = olho(0.06); o.rotation.y = l * 0.4 + 0.5; cab.add(em(o, R * 0.35, R * 0.35, l * R * 0.65)); const or = bola(0.07, m, 10); or.scale.set(0.6, 1, 0.6); cab.add(em(or, -R * 0.4, R * 0.8, l * R * 0.6)); }
  const s = sorriso(0.07, 0.015, 2.2, '#5a2a1a'); s.rotation.y = Math.PI / 2; cab.add(em(s, R * 1.3, -R * 0.35, 0));
  g.add(em(cab, 0.95, 1.5, 0));
  g.add(rabo([[-0.62, 0.9, 0], [-0.75, 0.7, 0], [-0.78, 0.5, 0]], 0.035, m));
  g.rotation.y = -0.6;
  return g;
}

const panda = () => bichinho({
  cor: '#ffffff', claro: '#ffffff', corFocinho: '#ffffff', nariz: '#1d1210',
  orelhas: (cab, m, mc, R) => {
    const pt = pl('#2b2230');
    for (const l of [-1, 1]) {
      const o = bola(R * 0.28, pt, 20); o.scale.set(1, 1, 0.6); cab.add(em(o, l * R * 0.68, R * 0.72, -R * 0.05));
      const mk = bola(R * 0.22, pt, 16); mk.scale.set(1.0, 1.35, 0.4); mk.rotation.z = l * 0.5; naSuperficie(mk, R, l * R * 0.38, R * 0.12, -R * 0.06); cab.add(mk);
    }
  },
  extras: (g) => {
    const pt = pl('#2b2230');
    const faixa = malha(new THREE.TorusGeometry(0.42, 0.14, 12, 32), pt); faixa.rotation.x = Math.PI / 2; faixa.scale.set(1, 1, 0.9); g.add(em(faixa, 0, 0.82, 0));
    for (const l of [-1, 1]) { g.add(em(capsula(0.115, 0.22, pt), l * 0.19, 0.3, 0.32)); const pe = bola(0.155, pt, 20); pe.scale.set(1, 0.6, 1.25); g.add(em(pe, l * 0.31, 0.08, 0.3)); const cx = bola(0.225, pt, 24); cx.scale.set(0.9, 0.8, 1.1); g.add(em(cx, l * 0.33, 0.2, 0.08)); }
    const bambu = pl('#7ad13a');
    g.add(em(cilindro(0.04, 0.04, 0.7, bambu, 12).rotateZ(0.5), 0.35, 0.55, 0.42));
  },
});

function dinossauro() {
  const g = new THREE.Group();
  const m = pl('#36c26a'), mc = pl('#c8f2a0'), pl2 = pl('#ffc21a');
  const corpo = bola(0.5, m, 40); corpo.scale.set(1.3, 0.85, 0.85); g.add(em(corpo, 0, 0.62, 0));
  const bar = bola(0.36, mc, 28); bar.scale.set(1.4, 0.7, 0.5); g.add(em(bar, 0.05, 0.5, 0.3));
  g.add(tubo(curva([[0.45, 0.8, 0], [0.7, 1.2, 0], [0.75, 1.65, 0.0]]), (t) => 0.2 - t * 0.06, m, { seg: 20, radial: 16 }));
  g.add(tubo(curva([[-0.5, 0.65, 0], [-0.95, 0.5, 0.1], [-1.25, 0.35, 0.3]]), (t) => 0.22 * (1 - t * 0.85), m, { seg: 20, radial: 16 }));
  for (const x of [-0.35, 0.35]) for (const z of [-0.25, 0.25]) { g.add(em(capsula(0.12, 0.3, m), x, 0.25, z)); const pe = bola(0.14, m, 16); pe.scale.set(1.1, 0.55, 1.2); g.add(em(pe, x + 0.03, 0.07, z)); }
  for (let k = 0; k < 6; k++) { const t = k / 5; const p = malha(new THREE.ConeGeometry(0.07, 0.15, 4), pl2); g.add(em(p, -0.6 + t * 1.1, 0.95 + Math.sin(t * Math.PI) * 0.15 + (t > 0.8 ? 0.15 : 0), 0)); }
  const R = 0.28;
  const cab = new THREE.Group(); const c = bola(R, m, 32); c.scale.set(1.3, 0.95, 1); cab.add(c);
  for (const l of [-1, 1]) { const o = olho(0.065); o.rotation.y = 0.4 + l * 0.6; cab.add(em(o, R * 0.35, R * 0.35, l * R * 0.62)); const b = bochecha(0.06); b.rotation.y = 0.5 + l * 0.8; cab.add(em(b, R * 0.7, -R * 0.15, l * R * 0.6)); }
  const s = sorriso(0.11, 0.018, 2.4); s.rotation.y = Math.PI / 2 - 0.0; cab.add(em(s, R * 1.25, -R * 0.2, 0));
  g.add(em(cab, 0.85, 1.75, 0));
  g.rotation.y = -0.6;
  return g;
}

function peru() {
  const g = new THREE.Group();
  const m = pl('#8a5a3a');
  const leque = ['#ef3b3b', '#ff8a1c', '#ffc21a', '#36c26a', '#3d8bff', '#ffc21a', '#ff8a1c', '#ef3b3b'];
  leque.forEach((c, i) => {
    const a = -Math.PI * 0.42 + (i / (leque.length - 1)) * Math.PI * 0.84;
    const p = bola(0.24, pl(c), 24); p.scale.set(0.55, 1.45, 0.18);
    p.rotation.z = -a;
    g.add(em(p, Math.sin(a) * 0.62, 0.75 + Math.cos(a) * 0.62, -0.3));
  });
  const corpo = bola(0.5, m, 40); corpo.scale.set(1, 1.0, 0.9); g.add(em(corpo, 0, 0.58, 0));
  const bar = bola(0.33, pl('#c08a5a'), 28); bar.scale.set(1, 1.1, 0.45); g.add(em(bar, 0, 0.52, 0.3));
  const R = 0.28;
  const cab = new THREE.Group(); cab.add(bola(R, pl('#c8a0e0'), 32));
  carinha(cab, R, { y: 0.15, abertura: 0.4, boca: false });
  const bico = malha(new THREE.ConeGeometry(0.07, 0.17, 12), pl('#ffc21a')); bico.rotation.x = Math.PI / 2; cab.add(em(bico, 0, -0.02, R + 0.05));
  const barb = capsula(0.05, 0.16, pl('#ff3b3b')); cab.add(em(barb, 0.06, -0.15, R + 0.02));
  g.add(em(cab, 0, 1.12, 0.12));
  for (const l of [-1, 1]) {
    const asa = bola(0.26, pl('#6e4630'), 20); asa.scale.set(0.35, 0.8, 0.9); g.add(em(asa, l * 0.48, 0.58, 0));
    g.add(em(cilindro(0.04, 0.04, 0.2, pl('#ffb000'), 10), l * 0.16, 0.1, 0.05));
    const pe = bola(0.1, pl('#ffb000'), 12); pe.scale.set(1.2, 0.4, 1.4); g.add(em(pe, l * 0.16, 0.03, 0.12));
  }
  g.rotation.y = 0.2;
  return g;
}

const cabra = () => bichinho({
  cor: '#f2ece2', claro: '#ffffff', focinho: 'longo', corFocinho: '#ffe0d0',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = bola(R * 0.3, m, 20); o.scale.set(1.4, 0.5, 0.45); o.rotation.z = l * -0.5; cab.add(em(o, l * R * 1.0, R * 0.25, 0));
      cab.add(tubo(curva([[l * R * 0.3, R * 0.8, -R * 0.1], [l * R * 0.45, R * 1.25, -R * 0.35], [l * R * 0.6, R * 1.2, -R * 0.7]]), (t) => R * 0.12 * (1 - t * 0.6), pl('#b8a088'), { seg: 16, radial: 10 }));
    }
    const barba = malha(new THREE.ConeGeometry(R * 0.15, R * 0.45, 12), pl('#d9cfc0')); barba.rotation.x = Math.PI; naSuperficie(barba, R, 0, -R * 0.75, R * 0.1); barba.rotation.set(Math.PI + 0.4, 0, 0); cab.add(barba);
  },
  extras: (g) => {
    g.add(em(bola(0.1, pl('#ffffff'), 12), -0.15, 0.6, -0.45));
    const sino = cilindro(0.06, 0.1, 0.12, brilho('#ffc21a'), 16); g.add(em(sino, 0, 0.82, 0.42));
  },
});

const boi = () => bichinho({
  cor: '#9a5a32', claro: '#f1d6b0', focinho: 'longo', corFocinho: '#e8b8a0',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = bola(R * 0.3, m, 20); o.scale.set(1.3, 0.55, 0.45); o.rotation.z = l * -0.3; cab.add(em(o, l * R * 1.05, R * 0.3, 0));
      cab.add(tubo(curva([[l * R * 0.4, R * 0.75, 0], [l * R * 0.95, R * 0.95, 0], [l * R * 1.15, R * 1.3, 0.05]]), (t) => R * 0.12 * (1 - t * 0.7), pl('#f5e6c8'), { seg: 16, radial: 10 }));
    }
    const t = bola(R * 0.22, pl('#ffffff'), 16); t.scale.set(1, 1, 0.3); naSuperficie(t, R, 0, R * 0.5, -R * 0.03); cab.add(t);
  },
  extras: (g, m) => g.add(rabo([[-0.35, 0.5, -0.35], [-0.6, 0.4, -0.3], [-0.7, 0.15, -0.25]], 0.04, m)),
});

// =============================================================================================
// Natureza, tempo e lugares

function arvore() { const g = I.arvore(); return g; }

function bonecoDeNeve() {
  const g = new THREE.Group();
  const neve = pl('#ffffff', { rugosidade: 0.55, verniz: 0.3 });
  g.add(em(bola(0.5, neve, 40), 0, 0.46, 0));
  g.add(em(bola(0.37, neve, 40), 0, 1.1, 0));
  const R = 0.3;
  const cab = new THREE.Group(); cab.add(bola(R, neve, 40));
  carinha(cab, R, { y: 0.15, abertura: 0.38, boca: false });
  for (let k = -2; k <= 2; k++) { const b = bola(0.025, preto(), 8); naSuperficie(b, R, k * R * 0.14, -R * 0.4 + Math.abs(k) * R * 0.06, 0); cab.add(b); }
  const nariz = malha(new THREE.ConeGeometry(0.06, 0.28, 16), pl('#ff8a1a')); nariz.rotation.x = Math.PI / 2; cab.add(em(nariz, 0, -R * 0.05, R + 0.12));
  const cart = new THREE.Group();
  cart.add(em(cilindro(0.32, 0.32, 0.04, preto(), 32), 0, 0, 0));
  cart.add(em(cilindro(0.21, 0.21, 0.32, preto(), 32), 0, 0.17, 0));
  cart.add(em(cilindro(0.215, 0.215, 0.07, brilho('#ef3b4f'), 32), 0, 0.06, 0));
  cart.rotation.z = 0.15;
  cab.add(em(cart, 0.02, R * 0.85, 0));
  g.add(em(cab, 0, 1.65, 0.02));
  const cach = malha(new THREE.TorusGeometry(0.27, 0.07, 12, 32), fosco('#3d8bff')); cach.rotation.x = Math.PI / 2; g.add(em(cach, 0, 1.4, 0));
  const ponta = caixa(0.15, 0.35, 0.06, 0.03, fosco('#3d8bff')); ponta.rotation.z = 0.2; g.add(em(ponta, 0.15, 1.22, 0.26));
  for (const y of [1.15, 0.95, 0.6]) g.add(em(bola(0.05, preto(), 12), 0, y, y > 0.8 ? 0.36 : 0.5));
  for (const l of [-1, 1]) g.add(tubo(curva([[l * 0.32, 1.15, 0], [l * 0.6, 1.3, 0], [l * 0.75, 1.5, 0]]), () => 0.025, massinha('#8a5a3a'), { seg: 12, radial: 6 }));
  g.rotation.y = 0.2;
  return g;
}

function floco() {
  const g = new THREE.Group();
  const m = plastico('#5ab4ff', { rugosidade: 0.12, verniz: 1, emissivo: '#8fd0ff', ambiente: 0.6 });
  for (let k = 0; k < 6; k++) {
    const a = (k / 6) * Math.PI * 2;
    const b = new THREE.Group();
    b.add(segmento([0, 0, 0], [0, 0.75, 0], 0.065, m));
    for (const [y, c] of [[0.35, 0.18], [0.58, 0.13]]) for (const l of [-1, 1]) b.add(segmento([0, y, 0], [l * c, y + c, 0], 0.05, m));
    b.add(em(bola(0.08, m, 12), 0, 0.78, 0));
    b.rotation.z = a;
    g.add(b);
  }
  g.add(em(cilindro(0.18, 0.18, 0.12, m, 6).rotateX(Math.PI / 2), 0, 0, 0));
  g.add(em(bola(0.07, plastico('#ffffff', { emissivo: '#ffffff' }), 12), 0, 0, 0.08));
  g.rotation.set(0.15, -0.3, 0);
  return g;
}

function sol() {
  const g = new THREE.Group();
  const am = brilho('#ffc21a', { emissivo: '#ffb000' }), lar = brilho('#ff9a1a');
  const R = 0.55;
  const cab = new THREE.Group(); cab.add(bola(R, am, 48));
  carinha(cab, R, { y: 0.12, abertura: 0.34, yBoca: -0.25 });
  g.add(cab);
  for (let k = 0; k < 10; k++) {
    const a = (k / 10) * Math.PI * 2;
    const r = malha(new THREE.ConeGeometry(0.13, 0.32, 16), lar);
    r.position.set(Math.cos(a) * 0.78, Math.sin(a) * 0.78, -0.1);
    r.rotation.z = a - Math.PI / 2;
    g.add(r);
  }
  g.rotation.y = -0.2;
  return g;
}

function chuva() {
  const g = new THREE.Group();
  const n = I.nuvem(); n.updateMatrixWorld(true);
  const caixaN = new THREE.Box3().setFromObject(n); const t = caixaN.getSize(V());
  const k = 1.4 / t.x; n.scale.setScalar(k);
  n.position.set(-caixaN.getCenter(V()).x * k, 0.75 - caixaN.min.y * k, 0);
  g.add(n);
  const gota = plastico('#3d9bff', { rugosidade: 0.1, verniz: 1, emissivo: '#7fc4ff', ambiente: 0.6 });
  for (const [x, y] of [[-0.45, 0.45], [-0.12, 0.25], [0.2, 0.5], [0.48, 0.28], [-0.3, 0.05], [0.32, 0.02]]) {
    const d = new THREE.Group();
    d.add(bola(0.07, gota, 16));
    const c = malha(new THREE.ConeGeometry(0.068, 0.14, 16), gota); c.position.y = 0.08; d.add(c);
    g.add(em(d, x, y + 0.08, 0.1));
  }
  return g;
}

function nuvem() {
  const g = new THREE.Group();
  const n = I.nuvem(); g.add(n);
  return g;
}

function palmeira() {
  const g = new THREE.Group();
  const tronco = pl('#c8874a', { rugosidade: 0.55 });
  const c = curva([[0, 0, 0], [0.12, 0.6, 0], [0.05, 1.2, 0], [-0.12, 1.65, 0]]);
  for (let k = 0; k < 9; k++) { const p = c.getPointAt(k / 9); const r = 0.15 - k * 0.006; const a = cilindro(r * 0.85, r, 0.2, tronco, 16); g.add(em(a, p.x, p.y + 0.1, p.z)); }
  const folha = pl('#36b04a', { rugosidade: 0.45 });
  for (let k = 0; k < 7; k++) {
    const a = (k / 7) * Math.PI * 2;
    const ca = Math.cos(a), sa = Math.sin(a);
    const f = tubo(curva([[-0.12, 1.8, 0], [-0.12 + ca * 0.45, 1.98, sa * 0.45], [-0.12 + ca * 0.85, 1.6, sa * 0.85]]), (t) => 0.13 * Math.sin(Math.max(0.05, t) * Math.PI) ** 0.6 + 0.01, folha, { seg: 20, radial: 10 });
    g.add(f);
  }
  for (const [x, z] of [[-0.05, 0.12], [-0.2, 0.08], [-0.1, -0.05]]) g.add(em(bola(0.1, pl('#8a5a2a'), 16), x, 1.68, z));
  const ilha = bola(0.5, pl('#ffe0a0', { rugosidade: 0.7, verniz: 0.1 }), 32); ilha.scale.set(1.2, 0.25, 1); g.add(em(ilha, 0, 0.0, 0));
  g.rotation.y = 0.2;
  return g;
}

function cacto() {
  const g = new THREE.Group();
  const m = pl('#3fae4a'), vaso = brilho('#e8763a');
  g.add(em(cilindro(0.35, 0.27, 0.42, vaso, 32), 0, 0.21, 0));
  g.add(em(malha(new THREE.TorusGeometry(0.35, 0.05, 10, 32), vaso).rotateX(Math.PI / 2), 0, 0.42, 0));
  g.add(em(cilindro(0.33, 0.33, 0.02, massinha('#7a4a2a'), 32), 0, 0.41, 0));
  g.add(em(capsula(0.2, 0.75, m), 0, 0.95, 0));
  for (const l of [-1, 1]) g.add(tubo(curva([[0, 0.85 + (l > 0 ? 0.15 : 0), 0], [l * 0.32, 0.85 + (l > 0 ? 0.15 : 0), 0], [l * 0.35, 1.2 + (l > 0 ? 0.15 : 0), 0]]), () => 0.11, m, { seg: 16, radial: 16 }));
  const esp = plastico('#ffffff');
  for (let k = 0; k < 14; k++) { const a = k * 2.3, y = 0.7 + (k % 7) * 0.12; const e = cilindro(0.008, 0.008, 0.07, esp, 4); e.rotation.z = Math.PI / 2; e.rotation.y = -a; g.add(em(e, Math.cos(a) * 0.21, y, Math.sin(a) * 0.21)); }
  const flor = new THREE.Group();
  for (let k = 0; k < 5; k++) { const a = (k / 5) * Math.PI * 2; const p = bola(0.07, pl('#ff6fa8'), 12); p.scale.set(1, 0.5, 1); flor.add(em(p, Math.cos(a) * 0.07, 0, Math.sin(a) * 0.07)); }
  flor.add(em(bola(0.04, pl('#ffe066'), 8), 0, 0.03, 0));
  g.add(em(flor, 0, 1.55, 0));
  const R = 0.2; const rosto = new THREE.Group(); carinha(rosto, R, { y: 0.0, abertura: 0.4, yBoca: -0.35 }); g.add(em(rosto, 0, 1.0, 0));
  g.rotation.y = 0.15;
  return g;
}

function girassol() {
  const g = new THREE.Group();
  const verde = pl('#3fae4a');
  g.add(tubo(curva([[0, 0, 0], [0.05, 0.6, 0], [0, 1.15, 0.05]]), () => 0.05, verde, { seg: 16, radial: 8 }));
  for (const l of [-1, 1]) { const f = bola(0.22, verde, 16); f.scale.set(1.3, 0.22, 0.6); f.rotation.z = l * 0.5; g.add(em(f, l * 0.24, 0.45 + (l > 0 ? 0.18 : 0), 0)); }
  const cab = new THREE.Group();
  const pet = pl('#ffc81a', { rugosidade: 0.35 });
  for (let k = 0; k < 14; k++) { const a = (k / 14) * Math.PI * 2; const p = bola(0.15, pet, 16); p.scale.set(0.45, 1.1, 0.18); p.rotation.z = a - Math.PI / 2; cab.add(em(p, Math.cos(a) * 0.36, Math.sin(a) * 0.36, -0.02)); }
  const miolo = bola(0.26, pl('#7a4a22', { rugosidade: 0.6 }), 32); miolo.scale.set(1, 1, 0.45); cab.add(miolo);
  const R = 0.26; const rosto = new THREE.Group(); carinha(rosto, R * 0.45, { y: 0.2, abertura: 0.8, yBoca: -0.5 }); rosto.scale.set(2.2, 2.2, 1); cab.add(em(rosto, 0, 0, 0.08));
  cab.rotation.x = -0.2;
  g.add(em(cab, 0, 1.35, 0.05));
  return g;
}

function broto() {
  const g = new THREE.Group();
  const terra = bola(0.45, massinha('#8a5a3a'), 32); terra.scale.set(1, 0.35, 1); g.add(em(terra, 0, 0.08, 0));
  const verde = pl('#5ccf4a');
  g.add(tubo(curva([[0, 0.15, 0], [0.04, 0.45, 0], [0, 0.75, 0]]), () => 0.045, verde, { seg: 12, radial: 8 }));
  for (const l of [-1, 1]) { const f = bola(0.24, verde, 20); f.scale.set(1.2, 0.3, 0.7); f.rotation.z = l * 0.45; g.add(em(f, l * 0.22, 0.85, 0)); }
  return g;
}

function arcoIris() { return I.arcoiris(); }

function raio() {
  const s = poli([[0.15, 0.95], [-0.35, 0.0], [0.0, 0.0], [-0.2, -0.95], [0.4, 0.15], [0.05, 0.15], [0.3, 0.95]]);
  const g = new THREE.Group();
  g.add(estufado(s, brilho('#ffd21a', { emissivo: '#ffb000' }), 0.14, 0.08));
  g.rotation.set(0.05, -0.25, -0.05);
  return g;
}

function rosa() {
  const g = new THREE.Group();
  const verde = pl('#3fae4a');
  g.add(tubo(curva([[0, 0, 0], [0.05, 0.5, 0], [0, 1.0, 0.03]]), () => 0.04, verde, { seg: 16, radial: 8 }));
  for (const l of [-1, 1]) { const f = bola(0.17, verde, 16); f.scale.set(1.3, 0.25, 0.6); f.rotation.z = l * 0.5; g.add(em(f, l * 0.18, 0.4 + (l > 0 ? 0.15 : 0), 0)); }
  const verm = pl('#ef2d4a', { rugosidade: 0.3, verniz: 0.7 });
  const flor = new THREE.Group();
  flor.add(em(bola(0.16, verm, 20), 0, 0.12, 0));
  for (let k = 0; k < 5; k++) { const a = (k / 5) * Math.PI * 2; const p = bola(0.17, verm, 20); p.scale.set(0.8, 1.0, 0.35); p.rotation.y = -a; p.rotation.x = 0.25; flor.add(em(p, Math.cos(a) * 0.15, 0.08, Math.sin(a) * 0.15)); }
  for (let k = 0; k < 3; k++) { const a = (k / 3) * Math.PI * 2 + 0.5; const p = bola(0.12, verm, 16); p.scale.set(0.8, 1.0, 0.35); p.rotation.y = -a; flor.add(em(p, Math.cos(a) * 0.07, 0.2, Math.sin(a) * 0.07)); }
  for (let k = 0; k < 4; k++) { const a = (k / 4) * Math.PI * 2; const s = bola(0.1, verde, 12); s.scale.set(1, 0.3, 0.5); s.rotation.y = -a; flor.add(em(s, Math.cos(a) * 0.12, -0.08, Math.sin(a) * 0.12)); }
  g.add(em(flor, 0, 1.02, 0));
  return g;
}

function cogumelo() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.22, 0.0], [0.2, 0.25], [0.18, 0.5], [0, 0.52]], pl('#fff3e0'), 32));
  const chapeu = malha(new THREE.SphereGeometry(0.55, 48, 24, 0, Math.PI * 2, 0, Math.PI / 2), brilho('#ef2d3a')); chapeu.scale.set(1, 0.7, 1); g.add(em(chapeu, 0, 0.45, 0));
  g.add(em(cilindro(0.55, 0.5, 0.06, pl('#ffe8d0'), 48), 0, 0.44, 0));
  for (const [x, y, z, r] of [[0, 0.83, 0.1, 0.1], [-0.3, 0.68, 0.3, 0.08], [0.3, 0.65, 0.32, 0.09], [0.15, 0.6, -0.4, 0.08], [-0.4, 0.62, -0.1, 0.07], [0.42, 0.62, -0.05, 0.06]]) {
    const p = bola(r, branco(), 16); p.scale.set(1, 0.4, 1); p.lookAt(V(x * 3, (y - 0.45) * 3 + 0.45, z * 3)); g.add(em(p, x, y, z));
  }
  const R = 0.2; const rosto = new THREE.Group(); carinha(rosto, R, { y: 0.0, abertura: 0.42, yBoca: -0.35 }); g.add(em(rosto, 0, 0.26, 0.02));
  return g;
}

function trevo() {
  const g = new THREE.Group();
  const verde = pl('#36c26a'), claro = pl('#7ad86a');
  g.add(tubo(curva([[0, 0, 0], [0.08, 0.45, 0], [0, 0.85, 0]]), () => 0.045, verde, { seg: 12, radial: 8 }));
  const s = new THREE.Shape();
  s.moveTo(0, -0.55);
  s.bezierCurveTo(-0.15, -0.38, -0.62, -0.12, -0.62, 0.22);
  s.bezierCurveTo(-0.62, 0.52, -0.34, 0.66, -0.16, 0.6);
  s.bezierCurveTo(-0.06, 0.56, 0, 0.46, 0, 0.4);
  s.bezierCurveTo(0, 0.46, 0.06, 0.56, 0.16, 0.6);
  s.bezierCurveTo(0.34, 0.66, 0.62, 0.52, 0.62, 0.22);
  s.bezierCurveTo(0.62, -0.12, 0.15, -0.38, 0, -0.55);
  for (let k = 0; k < 4; k++) {
    const a = (k / 4) * Math.PI * 2 + Math.PI / 4;
    const f = estufado(s, verde, 0.06, 0.06);
    f.scale.setScalar(0.42);
    f.rotation.z = a - Math.PI / 2;
    g.add(em(f, Math.cos(a) * 0.26, 1.0 + Math.sin(a) * 0.26, 0));
    const veio = capsula(0.018, 0.16, claro); veio.rotation.z = a - Math.PI / 2; g.add(em(veio, Math.cos(a) * 0.14, 1.0 + Math.sin(a) * 0.14, 0.05));
  }
  g.add(em(bola(0.06, verde, 12), 0, 1.0, 0.02));
  g.rotation.y = -0.2;
  return g;
}

function fogo() {
  const g = new THREE.Group();
  const lenha = massinha('#8a5a3a'), ponta = massinha('#e8c08a');
  for (const a of [0.55, -0.55]) {
    const t = new THREE.Group();
    const c = cilindro(0.12, 0.12, 1.1, lenha, 20); c.rotation.z = Math.PI / 2; t.add(c);
    for (const l of [-1, 1]) { const p = cilindro(0.115, 0.115, 0.02, ponta, 20); p.rotation.z = Math.PI / 2; t.add(em(p, l * 0.555, 0, 0)); }
    t.rotation.y = a;
    g.add(em(t, 0, 0.12, 0));
  }
  const lingua = (cor, esc, x, z, rz) => {
    const c = torneado([[0, 0], [0.36, 0.06], [0.4, 0.3], [0.27, 0.62], [0.1, 0.92], [0, 1.08]], plastico(cor, { rugosidade: 0.2, verniz: 0.8, emissivo: cor }), 40);
    c.scale.setScalar(esc); c.rotation.z = rz;
    return em(c, x, 0.18, z);
  };
  g.add(lingua('#ff4a1a', 1, 0, 0, 0), lingua('#ff6a1a', 0.62, -0.3, 0.05, 0.35), lingua('#ff6a1a', 0.55, 0.3, 0.05, -0.35));
  g.add(lingua('#ff9a1a', 0.68, 0, 0.14, 0), lingua('#ffd21a', 0.4, 0, 0.3, 0));
  return g;
}

function pedra() {
  const g = new THREE.Group();
  const geo = new THREE.IcosahedronGeometry(0.5, 2);
  const pos = geo.attributes.position;
  for (let i = 0; i < pos.count; i++) {
    const v = V(pos.getX(i), pos.getY(i), pos.getZ(i));
    const k = 1 + 0.08 * Math.sin(v.x * 7) * Math.cos(v.z * 6);
    pos.setXYZ(i, v.x * k * 1.3, v.y * k * 0.8, v.z * k);
  }
  geo.computeVertexNormals();
  g.add(em(malha(geo, pl('#9aa3b5', { rugosidade: 0.55, verniz: 0.3 })), 0, 0.38, 0));
  const m2 = bola(0.2, pl('#b8c0cf', { rugosidade: 0.55 }), 20); m2.scale.set(1.2, 0.7, 1); g.add(em(m2, 0.55, 0.12, 0.25));
  const musgo = bola(0.14, pl('#6fcf5a', { rugosidade: 0.7 }), 12); musgo.scale.set(1.4, 0.45, 1); g.add(em(musgo, -0.25, 0.74, 0.05));
  return g;
}

function montanha() {
  const g = new THREE.Group();
  const m = pl('#8a7ad8'), neve = pl('#ffffff');
  const c1 = malha(new THREE.ConeGeometry(0.75, 1.4, 6), m); g.add(em(c1, 0, 0.7, 0));
  const n1 = malha(new THREE.ConeGeometry(0.29, 0.55, 6), neve); g.add(em(n1, 0, 1.135, 0));
  const c2 = malha(new THREE.ConeGeometry(0.55, 0.95, 6), pl('#6f62c4')); g.add(em(c2, 0.6, 0.475, 0.2));
  const n2 = malha(new THREE.ConeGeometry(0.18, 0.31, 6), neve); g.add(em(n2, 0.6, 0.795, 0.2));
  const c3 = malha(new THREE.ConeGeometry(0.45, 0.75, 6), pl('#a090e8')); g.add(em(c3, -0.6, 0.375, 0.25));
  g.rotation.y = 0.3;
  return g;
}

function vulcao() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.85, 0.0], [0.7, 0.3], [0.42, 0.85], [0.28, 1.05], [0.22, 1.0], [0, 0.95]], pl('#9a6a4a', { rugosidade: 0.55 }), 48));
  const lava = plastico('#ff5a1a', { rugosidade: 0.2, verniz: 0.8, emissivo: '#ff7a1a' });
  g.add(em(cilindro(0.24, 0.24, 0.05, lava, 32), 0, 1.0, 0));
  for (const [a, l] of [[0.3, 0.55], [-0.5, 0.4], [1.0, 0.35]]) g.add(tubo(curva([[Math.sin(a) * 0.22, 1.02, Math.cos(a) * 0.22], [Math.sin(a) * 0.45, 0.85 - l * 0.3, Math.cos(a) * 0.45], [Math.sin(a) * 0.62, 0.95 - l * 1.1, Math.cos(a) * 0.62]]), (t) => 0.08 * (1 - t * 0.4), lava, { seg: 16, radial: 10 }));
  for (const [x, y, r] of [[0, 1.3, 0.2], [0.18, 1.5, 0.16], [-0.12, 1.62, 0.14]]) g.add(em(bola(r, pl('#c9c4d6', { rugosidade: 0.8, verniz: 0.05 }), 16), x, y, 0));
  g.rotation.y = 0.2;
  return g;
}

function osso() {
  const g = new THREE.Group();
  const m = pl('#fff6e2', { rugosidade: 0.35, verniz: 0.6 });
  g.add(em(capsula(0.16, 0.9, m).rotateZ(Math.PI / 2), 0, 0, 0));
  for (const l of [-1, 1]) for (const z of [-1, 1]) g.add(em(bola(0.2, m, 24), l * 0.58, z * 0.13, 0));
  g.rotation.set(0.3, -0.2, 0.35);
  return assentar(g);
}

function casa() {
  const g = new THREE.Group();
  g.add(em(caixa(1.2, 0.85, 0.95, 0.08, pl('#fff1d6')), 0, 0.43, 0));
  const telh = new THREE.Shape(); telh.moveTo(-0.75, 0); telh.lineTo(0.75, 0); telh.lineTo(0, 0.65); telh.closePath();
  const t = estufado(telh, brilho('#ef3b4f'), 1.0, 0.07); g.add(em(t, 0, 0.88, 0));
  const front = new THREE.Shape(); front.moveTo(-0.58, 0); front.lineTo(0.58, 0); front.lineTo(0, 0.5); front.closePath();
  g.add(em(estufado(front, pl('#fff1d6'), 0.9, 0.02), 0, 0.84, 0));
  g.add(em(caixa(0.32, 0.5, 0.06, 0.06, brilho('#3d8bff')), -0.2, 0.26, 0.48));
  g.add(em(bola(0.03, brilho('#ffd23f'), 8), -0.1, 0.27, 0.52));
  g.add(em(caixa(0.32, 0.3, 0.05, 0.04, brilho('#ffffff')), 0.28, 0.5, 0.48));
  g.add(em(caixa(0.26, 0.24, 0.06, 0.03, vidro()), 0.28, 0.5, 0.49));
  g.add(em(caixa(0.02, 0.24, 0.07, 0.01, brilho('#ffffff')), 0.28, 0.5, 0.5), em(caixa(0.26, 0.02, 0.07, 0.01, brilho('#ffffff')), 0.28, 0.5, 0.5));
  const jr = cilindro(0.1, 0.1, 0.05, vidro(), 24); jr.rotation.x = Math.PI / 2; g.add(em(jr, 0, 1.12, 0.47));
  g.add(em(caixa(0.18, 0.4, 0.18, 0.04, brilho('#c45a3a')), 0.4, 1.35, -0.2));
  g.rotation.y = -0.4;
  return g;
}

function escola() {
  const g = new THREE.Group();
  const par = pl('#ffd9a0'), tel = brilho('#ef3b4f');
  g.add(em(caixa(2.0, 0.8, 0.8, 0.06, par), 0, 0.4, 0));
  g.add(em(caixa(0.7, 1.25, 0.9, 0.06, par), 0, 0.62, 0.05));
  const tri = new THREE.Shape(); tri.moveTo(-0.45, 0); tri.lineTo(0.45, 0); tri.lineTo(0, 0.38); tri.closePath();
  g.add(em(estufado(tri, tel, 0.9, 0.05), 0, 1.25, 0.05));
  g.add(em(caixa(2.1, 0.1, 0.9, 0.04, tel), 0, 0.84, 0));
  g.add(em(cilindro(0.18, 0.18, 0.05, branco(), 32).rotateX(Math.PI / 2), 0, 1.0, 0.52));
  g.add(em(caixa(0.02, 0.12, 0.02, 0.005, preto()), 0, 1.04, 0.56), em(caixa(0.09, 0.02, 0.02, 0.005, preto()), 0.04, 1.0, 0.56));
  g.add(em(caixa(0.3, 0.42, 0.06, 0.06, brilho('#3d8bff')), 0, 0.21, 0.5));
  for (const x of [-0.75, -0.5, 0.5, 0.75]) g.add(em(caixa(0.18, 0.22, 0.06, 0.03, vidro()), x, 0.48, 0.4));
  g.add(em(cilindro(0.02, 0.02, 0.6, metal(), 8), 0, 1.65, 0.05));
  g.add(em(estufado(poli([[0, 0], [0.32, 0.1], [0, 0.2]]), brilho('#36c26a'), 0.02, 0.02), 0.02, 1.75, 0.05));
  g.rotation.y = -0.3;
  return g;
}

function circo() {
  const g = new THREE.Group();
  const n = 12;
  for (let k = 0; k < n; k++) {
    const a0 = (k / n) * Math.PI * 2;
    const p = malha(new THREE.CylinderGeometry(0.8, 0.8, 0.8, 8, 1, true, a0, (Math.PI * 2) / n), pl(k % 2 ? '#ffffff' : '#ef3b4f'));
    p.material.side = THREE.DoubleSide;
    g.add(em(p, 0, 0.4, 0));
    const t = malha(new THREE.ConeGeometry(0.88, 0.75, 8, 1, true, a0, (Math.PI * 2) / n), pl(k % 2 ? '#ffffff' : '#ef3b4f'));
    t.material.side = THREE.DoubleSide;
    g.add(em(t, 0, 1.17, 0));
  }
  for (let k = 0; k < n; k++) { const a = (k / n) * Math.PI * 2 + Math.PI / n; g.add(em(bola(0.1, pl(k % 2 ? '#ffd23f' : '#3d8bff'), 12), Math.sin(a) * 0.86, 0.8, Math.cos(a) * 0.86)); }
  g.add(em(cilindro(0.025, 0.025, 0.4, metal(), 8), 0, 1.7, 0));
  g.add(em(estufado(poli([[0, 0], [0.3, 0.09], [0, 0.18]]), brilho('#ffd23f'), 0.02, 0.02), 0.02, 1.78, 0));
  const porta = new THREE.Shape(); porta.moveTo(-0.22, 0); porta.lineTo(0.22, 0); porta.lineTo(0.15, 0.5); porta.quadraticCurveTo(0, 0.6, -0.15, 0.5); porta.closePath();
  g.add(em(estufado(porta, pl('#5a2a7a'), 0.02, 0.02), 0, 0.0, 0.8));
  return girado(g, 0, 0.13);
}

// =============================================================================================
// Comidas (mesa)

function leite() {
  const g = new THREE.Group();
  const copo = plastico('#e8f6ff', { rugosidade: 0.05, verniz: 1, opacidade: 0.45, ambiente: 0.6 });
  g.add(torneado([[0, 0], [0.32, 0.0], [0.34, 0.05], [0.4, 1.0], [0.37, 1.02], [0, 1.0]], copo, 48));
  g.add(torneado([[0, 0.04], [0.31, 0.04], [0.36, 0.82], [0, 0.82]], pl('#ffffff', { rugosidade: 0.3, verniz: 0.8 }), 48));
  const R = 0.36; const rosto = new THREE.Group(); carinha(rosto, R, { y: 0.0, abertura: 0.38, yBoca: -0.3 }); g.add(em(rosto, 0, 0.5, 0.03));
  return g;
}

function pipoca() {
  const g = new THREE.Group();
  const n = 12;
  for (let k = 0; k < n; k++) {
    const a0 = (k / n) * Math.PI * 2;
    const p = malha(new THREE.CylinderGeometry(0.45, 0.32, 0.85, 8, 1, true, a0, (Math.PI * 2) / n), pl(k % 2 ? '#ffffff' : '#ef3b4f'));
    p.material.side = THREE.DoubleSide;
    g.add(em(p, 0, 0.42, 0));
  }
  g.add(em(cilindro(0.32, 0.32, 0.02, pl('#ffffff'), 24), 0, 0.01, 0));
  const mi = pl('#fff4d0', { rugosidade: 0.6, verniz: 0.15 }), am = pl('#ffe08a', { rugosidade: 0.6, verniz: 0.15 });
  for (let k = 0; k < 34; k++) {
    const a = k * 2.4, r = 0.35 * Math.sqrt((k % 11) / 11 + 0.05), y = 0.82 + (k > 22 ? 0.18 : k > 12 ? 0.1 : 0.02) + Math.sin(k) * 0.03;
    const p = new THREE.Group();
    for (const [dx, dy, dz] of [[0, 0, 0], [0.04, 0.03, 0.02], [-0.03, 0.04, -0.02]]) p.add(em(bola(0.065, k % 3 ? mi : am, 10), dx, dy, dz));
    g.add(em(p, Math.cos(a) * r, y, Math.sin(a) * r));
  }
  g.rotation.x = 0.1;
  return g;
}

function citrico(cor, corClara, s = [1, 1, 1], ponta = true) {
  return () => {
    const g = new THREE.Group();
    const c = bola(0.5, brilho(cor, { rugosidade: 0.35 }), 48); c.scale.set(...s); g.add(em(c, 0, 0.5 * s[1], 0));
    if (ponta) for (const l of [-1, 1]) { const p = bola(0.1, brilho(cor), 16); p.scale.set(1.3, 0.8, 0.8); g.add(em(p, l * 0.5 * s[0], 0.5 * s[1], 0)); }
    const folha = bola(0.18, pl('#3fbf4a'), 16); folha.scale.set(1.3, 0.22, 0.55); folha.rotation.set(0, 0.4, 0.4);
    g.add(em(folha, 0.15, 0.98 * s[1], 0));
    g.add(em(cilindro(0.025, 0.03, 0.1, massinha('#6b4a2a'), 8), 0, 0.98 * s[1], 0));
    return g;
  };
}
const limao = () => { const g = citrico('#ffe02a', '#fff4a0', [1.3, 0.85, 0.85])(); g.rotation.y = -0.3; return grupo(g); };
const laranja = citrico('#ff9a1a', '#ffd08a', [1, 0.95, 1], false);

function gelo() {
  const g = new THREE.Group();
  const m = plastico('#bfe9ff', { rugosidade: 0.05, verniz: 1, opacidade: 0.82, emissivo: '#dff5ff', ambiente: 0.7 });
  g.add(em(caixa(0.8, 0.7, 0.75, 0.16, m), 0, 0.35, 0));
  g.add(em(caixa(0.55, 0.5, 0.5, 0.12, m), 0.55, 0.25, -0.35));
  g.add(em(caixa(0.1, 0.3, 0.02, 0.03, plastico('#ffffff', { emissivo: '#ffffff' })), -0.2, 0.45, 0.38));
  const R = 0.3; const rosto = new THREE.Group(); carinha(rosto, R, { y: 0, abertura: 0.42, yBoca: -0.32 }); g.add(em(rosto, 0.05, 0.36, 0.13));
  g.rotation.y = 0.35;
  return g;
}

function pera() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0.02], [0.3, 0.0], [0.5, 0.2], [0.5, 0.45], [0.32, 0.72], [0.22, 0.95], [0.18, 1.12], [0.08, 1.2], [0, 1.18]], brilho('#b8d83a', { rugosidade: 0.35 }), 64));
  g.add(em(cilindro(0.03, 0.04, 0.22, massinha('#6b4a2a'), 10), 0.03, 1.27, 0).rotateZ(-0.25));
  const folha = bola(0.17, pl('#3fbf4a'), 16); folha.scale.set(1.3, 0.22, 0.55); folha.rotation.set(0, 0.4, 0.4); g.add(em(folha, 0.18, 1.3, 0));
  const b = bola(0.18, pl('#ffb070', { rugosidade: 0.4, opacidade: 0.5 }), 16); b.scale.set(1, 0.8, 0.3); g.add(em(b, 0.28, 0.42, 0.38));
  g.rotation.z = 0.1;
  return grupo(g);
}

function queijo() {
  const g = new THREE.Group();
  const forma = poli([[0, 0], [1.2, 0], [0.0, 0.9]]);
  const geo = new THREE.ExtrudeGeometry(forma, { depth: 0.5, bevelEnabled: true, bevelThickness: 0.04, bevelSize: 0.04, bevelSegments: 4 });
  geo.rotateX(-Math.PI / 2);
  const q = malha(geo, pl('#ffd23f', { rugosidade: 0.4 }));
  g.add(em(q, -0.45, 0.04, 0.35));
  const furo = pl('#f2b81a', { rugosidade: 0.5 });
  for (const [x, y, z, r] of [[-0.1, 0.28, 0.4, 0.08], [0.2, 0.18, 0.4, 0.06], [-0.3, 0.12, 0.4, 0.05], [0.4, 0.35, 0.0, 0.07]]) { const f = bola(r, furo, 12); f.scale.set(1, 1, 0.3); g.add(em(f, x, y, z)); }
  for (const [x, z, r] of [[-0.2, 0.0, 0.07], [0.0, -0.3, 0.06]]) { const f = bola(r, furo, 12); f.scale.set(1, 0.3, 1); g.add(em(f, x, 0.56, z)); }
  g.rotation.y = 0.5;
  return g;
}

function bala() {
  const g = new THREE.Group();
  const m = brilho('#ff5aa0');
  const c = bola(0.35, m, 40); c.scale.set(1.25, 1, 1); g.add(c);
  for (let k = 0; k < 3; k++) { const f = malha(new THREE.TorusGeometry(0.32, 0.04, 8, 32), brilho('#ffffff')); f.rotation.y = Math.PI / 2; f.position.x = -0.2 + k * 0.2; f.scale.set(1, 1.05, 1.05); g.add(f); }
  for (const l of [-1, 1]) {
    const p = malha(new THREE.ConeGeometry(0.28, 0.4, 16), m); p.rotation.z = l * Math.PI / 2; g.add(em(p, l * 0.6, 0, 0));
  }
  g.rotation.set(0.2, -0.3, 0.15);
  return grupo(em(g, 0, 0.4, 0));
}

function melancia() {
  // fatia de melancia
  const g = new THREE.Group();
  const fatia = (r, cor, prof) => {
    const s = new THREE.Shape(); s.moveTo(-r, 0); s.absarc(0, 0, r, Math.PI, Math.PI * 2, false); s.closePath();
    const m = estufado(s, pl(cor, { rugosidade: 0.35 }), prof, 0.05);
    return m;
  };
  const f = new THREE.Group();
  f.add(fatia(0.8, '#2fae5f', 0.34));
  f.add(em(fatia(0.72, '#f2ffe0', 0.37), 0, 0, 0));
  f.add(em(fatia(0.66, '#ff3b5a', 0.4), 0, 0, 0));
  for (const [x, y] of [[-0.35, -0.22], [0, -0.38], [0.35, -0.22], [-0.15, -0.15], [0.17, -0.12], [0, -0.55], [-0.4, -0.42], [0.42, -0.42]]) { const s = bola(0.04, preto(), 10); s.scale.set(0.7, 1.2, 0.5); for (const z of [0.26, -0.26]) f.add(em(s.clone(), x, y, z)); }
  f.rotation.z = Math.PI;
  g.add(em(f, 0, 0.0, 0));
  g.rotation.set(0.15, -0.3, 0);
  return grupo(em(g, 0, 0.05, 0));
}

function sorvete() {
  const g = new THREE.Group();
  const casq = pl('#e8a85a', { rugosidade: 0.5 });
  const c = malha(new THREE.ConeGeometry(0.33, 0.9, 32), casq); c.rotation.x = Math.PI; g.add(em(c, 0, 0.45, 0));
  for (let k = 0; k < 5; k++) { const l = malha(new THREE.TorusGeometry(0.1 + k * 0.05, 0.012, 6, 24), pl('#c8803a')); l.rotation.x = Math.PI / 2; g.add(em(l, 0, 0.18 + k * 0.15, 0)); }
  const cr = pl('#fff2e0', { rugosidade: 0.35, verniz: 0.7 }), ro = pl('#ffa8c8', { rugosidade: 0.35, verniz: 0.7 });
  for (const [y, r, m] of [[0.95, 0.36, cr], [1.22, 0.3, ro], [1.45, 0.2, cr]]) { const b = bola(r, m, 32); b.scale.set(1, 0.75, 1); g.add(em(b, 0, y, 0)); }
  g.add(em(bola(0.1, brilho('#e8192c'), 16), 0, 1.65, 0));
  return g;
}

function tomate() {
  const g = new THREE.Group();
  const c = bola(0.5, brilho('#ef2d2a'), 48); c.scale.set(1.12, 0.85, 1.12); g.add(em(c, 0, 0.43, 0));
  const verde = pl('#3fbf4a');
  for (let k = 0; k < 5; k++) { const a = (k / 5) * Math.PI * 2; const f = bola(0.13, verde, 12); f.scale.set(1.4, 0.2, 0.45); f.rotation.y = -a; g.add(em(f, Math.cos(a) * 0.14, 0.85, Math.sin(a) * 0.14)); }
  g.add(em(cilindro(0.03, 0.04, 0.15, verde, 8), 0, 0.92, 0));
  const R = 0.5; const rosto = new THREE.Group(); carinha(rosto, R, { y: 0.05, abertura: 0.36, yBoca: -0.25 }); rosto.scale.set(1.12, 0.85, 1.12); g.add(em(rosto, 0, 0.43, 0));
  return g;
}

function abacaxi() {
  const g = new THREE.Group();
  const c = bola(0.45, pl('#ffb81a', { rugosidade: 0.45 }), 48); c.scale.set(1, 1.35, 1); g.add(em(c, 0, 0.6, 0));
  const esc = pl('#d9861a');
  for (let i = 0; i < 6; i++) for (let k = 0; k < 10; k++) {
    const a = (k / 10) * Math.PI * 2 + (i % 2) * 0.31;
    const yy = -0.75 + i * 0.3;
    const r = Math.sqrt(Math.max(0, 1 - yy * yy));
    const p = bola(0.04, esc, 8);
    const n = V(Math.cos(a) * r, yy, Math.sin(a) * r);
    p.position.set(n.x * 0.45, 0.6 + n.y * 0.45 * 1.35, n.z * 0.45);
    g.add(p);
  }
  const verde = pl('#3fae4a');
  for (let k = 0; k < 9; k++) {
    const a = (k / 9) * Math.PI * 2, h = 0.35 + (k % 3) * 0.12;
    const f = malha(new THREE.ConeGeometry(0.08, h, 6), verde);
    f.rotation.set(Math.sin(a) * 0.5, 0, -Math.cos(a) * 0.5);
    g.add(em(f, Math.cos(a) * 0.08, 1.2 + h * 0.4, Math.sin(a) * 0.08));
  }
  g.add(em(malha(new THREE.ConeGeometry(0.08, 0.55, 6), verde), 0, 1.45, 0));
  return g;
}

function pizza() {
  const g = new THREE.Group();
  const s = new THREE.Shape(); s.moveTo(0, 0); s.lineTo(-0.5, 1.1); s.quadraticCurveTo(0, 1.25, 0.5, 1.1); s.closePath();
  const fat = estufado(s, pl('#ffcf4a', { rugosidade: 0.45 }), 0.06, 0.04); fat.rotation.x = -Math.PI / 2; g.add(em(fat, 0, 0.06, 0.55));
  g.add(tubo(curva([[-0.52, 0.12, -0.55], [0, 0.12, -0.68], [0.52, 0.12, -0.55]]), () => 0.1, pl('#d98a3a', { rugosidade: 0.5 }), { seg: 20, radial: 12 }));
  for (const [x, z] of [[0, 0.1], [-0.18, -0.25], [0.2, -0.3], [0.02, -0.45]]) g.add(em(cilindro(0.09, 0.09, 0.04, brilho('#e8392a'), 20), x, 0.13, z));
  for (const [x, z] of [[-0.08, -0.1], [0.12, -0.12], [-0.25, -0.45]]) g.add(em(cilindro(0.04, 0.04, 0.04, pl('#36a04a'), 10), x, 0.13, z));
  g.rotation.set(0.85, 0.3, 0);
  return assentar(g);
}

function macaVerde() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0.1], [0.28, 0.0], [0.55, 0.12], [0.66, 0.4], [0.64, 0.66], [0.5, 0.88], [0.3, 0.96], [0.12, 0.88], [0, 0.82]], brilho('#7ad13a'), 64));
  g.add(em(cilindro(0.035, 0.05, 0.3, massinha('#7a4a2a'), 10), 0.02, 0.98, 0).rotateZ(-0.2));
  const folha = bola(0.2, pl('#2f9a3a'), 20); folha.scale.set(1.2, 0.2, 0.55); folha.rotation.set(0, 0.4, 0.4); g.add(em(folha, 0.22, 1.05, 0.02));
  return g;
}

function biscoito() {
  const g = new THREE.Group();
  const m = pl('#d9984a', { rugosidade: 0.55, verniz: 0.2 });
  const b = cilindro(0.6, 0.6, 0.18, m, 48); b.rotation.x = Math.PI / 2; g.add(b);
  const t = malha(new THREE.TorusGeometry(0.6, 0.09, 12, 48), m); g.add(t);
  for (const [x, y] of [[0, 0], [0.3, 0.2], [-0.3, 0.25], [-0.25, -0.3], [0.3, -0.25], [0.05, 0.45], [0.02, -0.45], [-0.45, 0]]) { const c = bola(0.075, pl('#5a3420', { rugosidade: 0.4 }), 12); c.scale.set(1, 1, 0.5); g.add(em(c, x, y, 0.1)); }
  g.rotation.set(-0.35, 0.25, 0);
  return assentar(g);
}

function mel() {
  const g = new THREE.Group();
  const pote = plastico('#ffb41a', { rugosidade: 0.12, verniz: 1, opacidade: 0.92, emissivo: '#ff9a00', ambiente: 0.6 });
  g.add(torneado([[0, 0], [0.4, 0.0], [0.5, 0.25], [0.5, 0.55], [0.4, 0.75], [0.3, 0.8], [0, 0.8]], pote, 48));
  g.add(em(cilindro(0.35, 0.35, 0.14, fosco('#ef3b4f'), 32), 0, 0.86, 0));
  const pano = cilindro(0.42, 0.33, 0.08, pl('#ffffff', { rugosidade: 0.6 }), 32); g.add(em(pano, 0, 0.82, 0));
  const etq = new THREE.Shape(); etq.absarc(0, 0, 0.2, 0, Math.PI * 2, false);
  const e = estufado(etq, pl('#fff4d0'), 0.02, 0.02); g.add(em(e, 0, 0.42, 0.5));
  const ab = new THREE.Group();
  ab.add(em(capsula(0.08, 0.1, pl('#ffc81a')).rotateZ(Math.PI / 2), 0, 0, 0));
  ab.add(em(cilindro(0.082, 0.082, 0.04, preto(), 16).rotateZ(Math.PI / 2), 0.02, 0, 0));
  for (const l of [-1, 1]) { const a = bola(0.06, plastico('#e8f6ff', { opacidade: 0.8, verniz: 1 }), 12); a.scale.set(0.7, 1, 0.2); ab.add(em(a, 0, 0.1, l * 0.04)); }
  g.add(em(ab, 0, 0.42, 0.53));
  g.add(segmento([0.15, 0.85, 0.1], [0.45, 1.25, 0.15], 0.03, pl('#d9975a')));
  const pontaDaColher = bola(0.08, pl('#d9975a'), 12); pontaDaColher.scale.set(1, 1.3, 1); g.add(em(pontaDaColher, 0.47, 1.28, 0.15));
  return g;
}

function alface() {
  const g = new THREE.Group();
  const verde = pl('#7ad13a', { rugosidade: 0.4 }), esc = pl('#3fae4a', { rugosidade: 0.4 });
  g.add(em(bola(0.42, pl('#c8f2a0', { rugosidade: 0.4 }), 32), 0, 0.5, 0));
  for (let k = 0; k < 7; k++) {
    const a = (k / 7) * Math.PI * 2;
    const f = bola(0.38, k % 2 ? verde : esc, 24); f.scale.set(1, 1.1, 0.3);
    const piv = new THREE.Group(); piv.add(em(f, 0, 0.32, 0)); piv.rotation.x = 0.4;
    const h = new THREE.Group(); h.add(em(piv, 0, 0, 0.36)); h.rotation.y = a;
    g.add(em(h, 0, 0.05, 0));
  }
  for (const a of [0.3, 2.4, 4.4]) { const f = bola(0.22, verde, 16); f.scale.set(1, 0.9, 0.3); f.rotation.set(0.6, a, 0); g.add(em(f, Math.sin(a) * 0.12, 0.85, Math.cos(a) * 0.12)); }
  return assentar(g);
}

function pepino() {
  const g = new THREE.Group();
  const c = capsula(0.22, 1.0, brilho('#3fa040', { rugosidade: 0.35 })); c.rotation.z = Math.PI / 2; c.scale.set(1, 1, 1);
  g.add(em(c, 0, 0.22, 0));
  for (let k = 0; k < 10; k++) { const a = k * 1.9; g.add(em(bola(0.025, pl('#8ad86a'), 8), -0.5 + k * 0.11, 0.22 + Math.sin(a) * 0.2, Math.cos(a) * 0.2)); }
  const fat = cilindro(0.22, 0.22, 0.08, pl('#3fa040'), 32); fat.rotation.x = 1.3;
  const dentro = cilindro(0.19, 0.19, 0.09, pl('#c8f2a0'), 32); dentro.rotation.x = 1.3;
  g.add(em(fat, 0.7, 0.22, 0.45), em(dentro, 0.7, 0.225, 0.455));
  for (let k = 0; k < 5; k++) { const a = (k / 5) * Math.PI * 2; const s = bola(0.025, pl('#fff4d0'), 8); s.scale.set(0.6, 1, 0.5); g.add(em(s, 0.7 + Math.cos(a) * 0.08, 0.24 + Math.sin(a) * 0.08 * 0.3, 0.5 + 0.03)); }
  g.rotation.y = -0.35;
  return g;
}

function ovoFrito() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.7, 0.62, 0.12, preto(), 48), 0, 0.06, 0));
  g.add(em(cilindro(0.6, 0.6, 0.02, plastico('#3a3a4a', { rugosidade: 0.3, verniz: 0.8 }), 48), 0, 0.12, 0));
  g.add(em(caixa(0.55, 0.08, 0.14, 0.04, preto()), 0.92, 0.1, 0));
  const clara = bola(0.45, pl('#ffffff', { rugosidade: 0.3, verniz: 0.8 }), 32); clara.scale.set(1.1, 0.12, 0.95); g.add(em(clara, -0.05, 0.14, 0));
  const gema = bola(0.18, brilho('#ffb81a'), 32); gema.scale.set(1, 0.65, 1); g.add(em(gema, 0, 0.2, 0.05));
  g.rotation.set(0.6, -0.4, 0);
  return assentar(g);
}

function copo() {
  // copo de suco com canudo (🥤)
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.3, 0], [0.4, 0.95], [0, 0.95]], brilho('#ef3b4f'), 48));
  for (const y of [0.3, 0.6]) g.add(em(cilindro(0.33 + y * 0.1, 0.32 + y * 0.1, 0.1, brilho('#ffffff'), 48), 0, y, 0));
  g.add(em(cilindro(0.43, 0.43, 0.14, brilho('#ffffff'), 48), 0, 1.0, 0));
  const tampa = bola(0.43, brilho('#ffffff'), 48); tampa.scale.set(1, 0.25, 1); g.add(em(tampa, 0, 1.06, 0));
  g.add(segmento([0.05, 1.0, 0], [0.12, 1.55, 0], 0.04, brilho('#3d8bff')), segmento([0.12, 1.55, 0], [0.35, 1.65, 0], 0.04, brilho('#3d8bff')));
  return g;
}

function coco() {
  const g = new THREE.Group();
  const casca = pl('#8a5a3a', { rugosidade: 0.6 });
  const c = bola(0.5, casca, 48); c.scale.set(1, 0.95, 1); g.add(em(c, 0, 0.48, 0));
  const meio = new THREE.Group();
  const m = malha(new THREE.SphereGeometry(0.42, 48, 24, 0, Math.PI * 2, Math.PI / 2, Math.PI / 2), casca); m.material.side = THREE.DoubleSide; meio.add(m);
  meio.add(em(cilindro(0.4, 0.4, 0.02, pl('#ffffff'), 48), 0, 0, 0));
  meio.add(em(malha(new THREE.TorusGeometry(0.4, 0.03, 8, 48), pl('#fff6e8')).rotateX(Math.PI / 2), 0, 0, 0));
  meio.rotation.x = 0.9;
  g.add(em(meio, 0.65, 0.4, 0.35));
  for (const [x, y] of [[-0.12, 0.85], [0.1, 0.85], [0, 0.95]]) g.add(em(bola(0.05, pl('#5a3420'), 8), x, y, 0.2));
  g.rotation.y = -0.2;
  return g;
}

function pirulito() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.035, 0.035, 0.9, branco(), 12), 0, 0.45, 0));
  const disco = new THREE.Group();
  disco.add(em(cilindro(0.5, 0.5, 0.16, brilho('#ff5aa0'), 48).rotateX(Math.PI / 2), 0, 0, 0));
  disco.add(em(malha(new THREE.TorusGeometry(0.5, 0.08, 12, 48), brilho('#ff5aa0')), 0, 0, 0));
  const pts = [];
  for (let i = 0; i <= 120; i++) { const t = i / 120; const a = t * Math.PI * 6; const r = 0.04 + t * 0.4; pts.push([Math.cos(a) * r, Math.sin(a) * r, 0]); }
  for (const z of [0.085, -0.085]) disco.add(em(tubo(curva(pts), () => 0.04, brilho('#ffffff'), { seg: 300, radial: 8 }), 0, 0, z));
  g.add(em(disco, 0, 1.4, 0));
  g.rotation.set(0, -0.25, 0.1);
  return grupo(g);
}

/** Calçado de brinquedo (tênis, sapato, bota, patins), de lado e um pouco virado: sola, bico, calcanhar. */

function batata() {
  const g = new THREE.Group();
  const geo = new THREE.SphereGeometry(0.5, 48, 24);
  const pos = geo.attributes.position;
  for (let i = 0; i < pos.count; i++) { const x = pos.getX(i), y = pos.getY(i), z = pos.getZ(i); const k = 1 + 0.06 * Math.sin(x * 9 + z * 4) * Math.cos(y * 7); pos.setXYZ(i, x * k * 1.35, y * k * 0.8, z * k * 0.95); }
  geo.computeVertexNormals();
  g.add(em(malha(geo, pl('#c8955a', { rugosidade: 0.6, verniz: 0.2 })), 0, 0.4, 0));
  for (const [x, y, z] of [[0.3, 0.6, 0.35], [-0.35, 0.5, 0.35], [0.1, 0.3, 0.45], [-0.5, 0.65, 0.05]]) { const p = bola(0.03, pl('#8a5a3a'), 8); g.add(em(p, x, y, z)); }
  g.rotation.y = -0.2;
  return g;
}

function brocolis() {
  const g = new THREE.Group();
  const talo = pl('#9ad86a');
  g.add(em(cilindro(0.14, 0.2, 0.55, talo, 20), 0, 0.27, 0));
  for (const l of [-1, 1]) g.add(segmento([0, 0.4, 0], [l * 0.25, 0.65, 0.05], 0.08, talo));
  const verde = pl('#2f9a3a', { rugosidade: 0.6 });
  for (const [x, y, z, r] of [[0, 0.85, 0, 0.3], [-0.3, 0.72, 0.05, 0.24], [0.3, 0.74, 0.05, 0.25], [-0.12, 0.8, 0.25, 0.22], [0.15, 0.78, 0.25, 0.22], [0, 0.72, -0.25, 0.22], [0.1, 1.02, 0.1, 0.18], [-0.15, 1.0, -0.05, 0.18]]) g.add(em(bola(r, verde, 20), x, y, z));
  for (let k = 0; k < 22; k++) { const a = k * 2.4, y = 0.85 + Math.sin(k) * 0.15; g.add(em(bola(0.06, pl('#3fae4a', { rugosidade: 0.6 }), 8), Math.cos(a) * 0.35, y, Math.sin(a) * 0.3 + 0.08)); }
  return g;
}

function milho() {
  const g = new THREE.Group();
  const am = pl('#ffd21a', { rugosidade: 0.3, verniz: 0.8 });
  const sab = new THREE.Group();
  for (let i = 0; i < 9; i++) for (let k = 0; k < 10; k++) {
    const a = (k / 10) * Math.PI * 2 + (i % 2) * 0.31;
    const y = -0.55 + i * 0.14;
    const r = 0.22 * (1 - Math.abs(y) * 0.45);
    const p = bola(0.075, am, 10); p.scale.set(1, 0.8, 0.8);
    sab.add(em(p, Math.cos(a) * r, y, Math.sin(a) * r));
  }
  sab.add(em(capsula(0.2, 0.9, pl('#ffc21a')), 0, 0, 0));
  const pal = pl('#7ad13a', { rugosidade: 0.45 });
  for (const l of [-1, 1, 0]) { const f = bola(0.3, pal, 24); f.scale.set(0.5, 1.4, 0.18); f.rotation.z = l * 0.35; f.rotation.y = l === 0 ? Math.PI / 2 : 0; sab.add(em(f, l * 0.22, -0.35, l === 0 ? -0.18 : 0.05)); }
  sab.rotation.z = -0.5;
  g.add(em(sab, 0, 0.55, 0));
  return g;
}

function pessego() {
  const g = new THREE.Group();
  const c = bola(0.5, pl('#ffa86a', { rugosidade: 0.55, verniz: 0.2 }), 48); g.add(em(c, 0, 0.5, 0));
  const r = bola(0.36, pl('#ff6a6a', { rugosidade: 0.55, verniz: 0.2 }), 32); r.scale.set(1, 1, 0.5); g.add(em(r, 0.12, 0.45, 0.28));
  const vinco = malha(new THREE.TorusGeometry(0.5, 0.025, 8, 48, Math.PI), pl('#ff8a5a')); vinco.rotation.y = Math.PI / 2 + 0.4; vinco.rotation.z = Math.PI / 2; g.add(em(vinco, 0, 0.5, 0));
  const folha = bola(0.2, pl('#3fbf4a'), 16); folha.scale.set(1.3, 0.22, 0.55); folha.rotation.set(0, 0.4, 0.5); g.add(em(folha, 0.18, 1.02, 0));
  g.add(em(cilindro(0.025, 0.03, 0.12, massinha('#6b4a2a'), 8), 0, 1.0, 0));
  return g;
}

function vela() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.42, 0.45, 0.1, brilho('#ffc21a'), 32), 0, 0.05, 0));
  g.add(em(malha(new THREE.TorusGeometry(0.1, 0.03, 8, 20, Math.PI), brilho('#ffc21a')).rotateY(Math.PI / 2), 0.47, 0.13, 0));
  g.add(em(cilindro(0.2, 0.2, 0.8, pl('#fff2e0', { rugosidade: 0.4 }), 32), 0, 0.5, 0));
  for (const a of [0.4, 2.5]) g.add(tubo(curva([[Math.cos(a) * 0.2, 0.9, Math.sin(a) * 0.2], [Math.cos(a) * 0.21, 0.78, Math.sin(a) * 0.21], [Math.cos(a) * 0.21, 0.66, Math.sin(a) * 0.21]]), (t) => 0.04 * (1 - t * 0.3), pl('#fff2e0'), { seg: 8, radial: 8 }));
  g.add(em(cilindro(0.015, 0.015, 0.1, preto(), 6), 0, 0.95, 0));
  const ch = torneado([[0, 0], [0.08, 0.06], [0.07, 0.15], [0.03, 0.25], [0, 0.3]], plastico('#ffb000', { emissivo: '#ffb000', rugosidade: 0.2 }), 24);
  g.add(em(ch, 0, 0.98, 0));
  g.add(em(torneado([[0, 0], [0.04, 0.04], [0.03, 0.1], [0, 0.14]], plastico('#fff4a0', { emissivo: '#fff4a0' }), 16), 0, 1.0, 0.02));
  return g;
}

// =============================================================================================
// Roupas e calçados (varal: âncora 'topo')

const tecido = (cor) => pl(cor, { rugosidade: 0.55, verniz: 0.25 });

function tenis() {
  const g = calcado({ cor: '#3d8bff', sola: '#ffffff', cadarco: '#ffffff' });
  const listra = capsula(0.035, 0.45, brilho('#ffffff')); listra.rotation.z = Math.PI / 2 - 0.35; g.add(em(listra, 0.0, 0.33, 0.29));
  g.add(em(bola(0.06, brilho('#ffd23f'), 12).rotateY(0), -0.42, 0.42, 0.29));
  return grupo(g);
}

function chinelo() {
  const g = new THREE.Group();
  for (const [x, cor] of [[-0.28, '#ffc21a'], [0.28, '#ffc21a']]) {
    const s = new THREE.Shape(); s.absellipse(0, 0, 0.24, 0.55, 0, Math.PI * 2, false);
    const sola = estufado(s, pl(cor, { rugosidade: 0.45 }), 0.06, 0.04); sola.rotation.x = -Math.PI / 2;
    const p = new THREE.Group(); p.add(em(sola, 0, 0.06, 0));
    const tira = pl('#3d8bff', { rugosidade: 0.4 });
    for (const l of [-1, 1]) p.add(tubo(curva([[0, 0.1, -0.3], [l * 0.12, 0.2, -0.1], [l * 0.22, 0.1, 0.1]]), () => 0.035, tira, { seg: 16, radial: 8 }));
    p.add(em(bola(0.04, tira, 10), 0, 0.12, -0.3));
    p.rotation.y = x < 0 ? 0.15 : -0.1;
    g.add(em(p, x, 0, x < 0 ? 0 : 0.1));
  }
  const w = new THREE.Group(); g.rotation.x = 0.0; w.add(g); w.rotation.set(0.6, 0, 0);
  return grupo(em(w, 0, 0.3, 0));
}

function calca() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.moveTo(-0.5, 1.2); s.lineTo(0.5, 1.2); s.lineTo(0.58, 0); s.lineTo(0.12, 0); s.lineTo(0, 0.75); s.lineTo(-0.12, 0); s.lineTo(-0.58, 0); s.closePath();
  g.add(estufado(s, tecido('#3a6fd0'), 0.18, 0.08));
  g.add(em(caixa(1.06, 0.14, 0.34, 0.06, tecido('#2a58b0')), 0, 1.17, 0));
  g.add(em(bola(0.04, brilho('#ffc21a'), 10), 0, 1.17, 0.18));
  for (const l of [-1, 1]) { g.add(em(caixa(0.48, 0.1, 0.32, 0.04, tecido('#2a58b0')), l * 0.35, 0.05, 0)); const b = caixa(0.2, 0.18, 0.02, 0.03, tecido('#2a58b0')); g.add(em(b, l * 0.3, 0.92, 0.17)); }
  for (const l of [-1, 1]) g.add(em(caixa(0.012, 0.8, 0.02, 0.005, pl('#ffc21a')), l * 0.32, 0.5, 0.18));
  g.rotation.y = 0.15;
  return g;
}

function cartola() {
  const g = new THREE.Group();
  const pt = plastico('#2b2438', { rugosidade: 0.25, verniz: 1 });
  g.add(em(cilindro(0.75, 0.75, 0.06, pt, 48), 0, 0.03, 0));
  const aba = malha(new THREE.TorusGeometry(0.72, 0.05, 10, 48), pt); aba.rotation.x = Math.PI / 2; g.add(em(aba, 0, 0.05, 0));
  g.add(em(cilindro(0.45, 0.42, 0.9, pt, 48), 0, 0.5, 0));
  g.add(em(cilindro(0.435, 0.428, 0.18, brilho('#ef3b4f'), 48), 0, 0.17, 0));
  g.add(em(estrela3D(0.1, 0.04, brilho('#ffd23f')), 0.25, 0.17, 0.35).rotateY(0.6));
  g.rotation.set(0.12, 0.3, 0);
  return g;
}

function bota() {
  return grupo(calcado({ cor: '#b8662e', sola: '#5a3420', cano: 0.5, borda: '#fff1d6', cadarco: '#ffd23f', giro: -0.5 }));
}

function bermuda() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.moveTo(-0.55, 0.75); s.lineTo(0.55, 0.75); s.lineTo(0.65, 0); s.lineTo(0.08, 0); s.lineTo(0, 0.38); s.lineTo(-0.08, 0); s.lineTo(-0.65, 0); s.closePath();
  g.add(estufado(s, tecido('#ff8a1c'), 0.18, 0.08));
  g.add(em(caixa(1.15, 0.12, 0.34, 0.06, tecido('#ffffff')), 0, 0.73, 0));
  for (const x of [-0.4, 0.4]) for (const y of [0.25, 0.5]) { const f = bola(0.07, tecido('#ffd23f'), 16); f.scale.set(1, 1, 0.3); g.add(em(f, x + (y > 0.3 ? 0.1 : -0.05), y, 0.18)); }
  g.add(tubo(curva([[-0.05, 0.72, 0.18], [-0.08, 0.55, 0.2]]), () => 0.015, branco(), { seg: 4, radial: 6 }), tubo(curva([[0.05, 0.72, 0.18], [0.08, 0.55, 0.2]]), () => 0.015, branco(), { seg: 4, radial: 6 }));
  g.rotation.y = 0.15;
  return g;
}

function casaco() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.moveTo(-0.5, 0); s.lineTo(0.5, 0); s.lineTo(0.5, 0.95); s.lineTo(0.62, 0.6); s.lineTo(0.9, 0.0); s.lineTo(1.05, 0.08); s.lineTo(0.78, 1.1); s.lineTo(0.38, 1.28); s.quadraticCurveTo(0, 1.1, -0.38, 1.28); s.lineTo(-0.78, 1.1); s.lineTo(-1.05, 0.08); s.lineTo(-0.9, 0.0); s.lineTo(-0.62, 0.6); s.lineTo(-0.5, 0.95); s.closePath();
  g.add(estufado(s, tecido('#ef3b4f'), 0.16, 0.1));
  g.add(em(caixa(0.05, 1.15, 0.05, 0.02, metal('#c9cfdb')), 0, 0.6, 0.2));
  const capuz = bola(0.3, tecido('#ffffff'), 24); capuz.scale.set(1.3, 0.5, 0.6); g.add(em(capuz, 0, 1.28, -0.05));
  for (const l of [-1, 1]) { g.add(em(caixa(0.28, 0.12, 0.3, 0.05, tecido('#ffffff')), l * 0.98, 0.07, 0).rotateZ(l * 0.35)); g.add(em(caixa(0.22, 0.2, 0.03, 0.04, tecido('#c92a3a')), l * 0.25, 0.32, 0.19)); }
  g.add(em(caixa(1.02, 0.12, 0.3, 0.05, tecido('#ffffff')), 0, 0.05, 0));
  g.rotation.y = 0.12;
  return g;
}

function vestido() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.moveTo(-0.7, 0); s.quadraticCurveTo(0, -0.08, 0.7, 0); s.lineTo(0.28, 0.75); s.lineTo(0.38, 1.22); s.lineTo(0.2, 1.25); s.quadraticCurveTo(0, 1.05, -0.2, 1.25); s.lineTo(-0.38, 1.22); s.lineTo(-0.28, 0.75); s.closePath();
  g.add(estufado(s, tecido('#ff6fb0'), 0.14, 0.08));
  g.add(em(caixa(0.62, 0.1, 0.3, 0.04, tecido('#ffd23f')), 0, 0.76, 0));
  for (const [x, y] of [[-0.35, 0.2], [0.0, 0.35], [0.35, 0.18], [-0.15, 0.55], [0.18, 0.58]]) { const p = bola(0.06, tecido('#ffffff'), 12); p.scale.set(1, 1, 0.3); g.add(em(p, x, y, 0.15)); }
  const laco = new THREE.Group();
  for (const l of [-1, 1]) { const a = bola(0.1, tecido('#ffd23f'), 12); a.scale.set(1.2, 0.8, 0.4); laco.add(em(a, l * 0.1, 0, 0)); }
  laco.add(em(bola(0.05, tecido('#ffd23f'), 10), 0, 0, 0.03));
  g.add(em(laco, 0.0, 0.77, 0.17));
  g.rotation.y = 0.12;
  return g;
}

function maio() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.moveTo(-0.15, 0); s.lineTo(0.15, 0); s.quadraticCurveTo(0.25, 0.25, 0.48, 0.32); s.lineTo(0.42, 0.85); s.lineTo(0.3, 1.25); s.lineTo(0.2, 1.25); s.quadraticCurveTo(0.12, 0.95, 0, 0.92); s.quadraticCurveTo(-0.12, 0.95, -0.2, 1.25); s.lineTo(-0.3, 1.25); s.lineTo(-0.42, 0.85); s.lineTo(-0.48, 0.32); s.quadraticCurveTo(-0.25, 0.25, -0.15, 0); s.closePath();
  g.add(estufado(s, tecido('#2ec4b6'), 0.12, 0.07));
  for (const [x, y] of [[-0.2, 0.45], [0.15, 0.62], [0.25, 0.35], [-0.1, 0.75], [0, 0.3]]) { const p = bola(0.055, tecido('#ffd23f'), 12); p.scale.set(1, 1, 0.3); g.add(em(p, x, y, 0.13)); }
  g.rotation.y = 0.12;
  return g;
}

function oculos() {
  const g = new THREE.Group();
  const arm = brilho('#ef3b6a');
  const lente = plastico('#2b2438', { rugosidade: 0.05, verniz: 1, emissivo: '#3a2a6a', ambiente: 0.6 });
  for (const l of [-1, 1]) {
    const aro = malha(new THREE.TorusGeometry(0.3, 0.05, 12, 40), arm); aro.scale.set(1.15, 0.95, 1); g.add(em(aro, l * 0.38, 0, 0));
    const len = cilindro(0.3, 0.3, 0.03, lente, 40); len.rotation.x = Math.PI / 2; len.scale.set(1.15, 1, 0.95); g.add(em(len, l * 0.38, 0, 0));
    g.add(em(caixa(0.08, 0.1, 0.03, 0.02, plastico('#ffffff', { emissivo: '#ffffff' })), l * 0.38 - 0.12, 0.1, 0.03).rotateZ(0.6));
    g.add(segmento([l * 0.72, 0.05, 0], [l * 0.78, 0.05, -0.7], 0.035, arm));
  }
  g.add(tubo(curva([[-0.06, 0.05, 0], [0, 0.12, 0], [0.06, 0.05, 0]]), () => 0.04, arm, { seg: 10, radial: 8 }));
  g.rotation.set(0.15, -0.35, 0);
  return grupo(em(g, 0, 0.4, 0));
}

function sapato() {
  return grupo(calcado({ cor: '#8a4220', sola: '#2b1a14', salto: true, brilhoso: true, cadarco: '#3a1a0a' }));
}

function patins() {
  const g = new THREE.Group();
  const b = calcado({ cor: '#ffffff', sola: '#ff6fa8', cano: 0.4, borda: '#ff6fa8', cadarco: '#ff6fa8', giro: 0 });
  g.add(em(b, 0, 0.2, 0));
  g.add(tubo(curva([[-0.6, 0.04, 0], [0.45, 0.04, 0], [0.68, 0.1, 0], [0.7, 0.22, 0]]), () => 0.03, metal(), { seg: 20, radial: 8 }));
  for (const x of [-0.35, 0.35]) g.add(em(caixa(0.06, 0.2, 0.06, 0.02, metal()), x, 0.13, 0));
  g.rotation.y = -0.55;
  return grupo(g);
}

function sapatilha() {
  const g = new THREE.Group();
  const m = pl('#ffa8c8', { rugosidade: 0.3, verniz: 0.8 });
  for (const [x, rz] of [[-0.28, 0], [0.28, 0.1]]) {
    const p = new THREE.Group();
    const c = bola(0.25, m, 32); c.scale.set(0.8, 0.6, 1.6); p.add(em(c, 0, 0.15, 0));
    const ab = bola(0.17, plastico('#c85a8a', { rugosidade: 0.6 }), 16); ab.scale.set(0.8, 0.3, 1.2); p.add(em(ab, 0, 0.28, -0.12));
    const laco = new THREE.Group(); for (const l of [-1, 1]) { const a = bola(0.07, m, 12); a.scale.set(1.2, 0.8, 0.5); laco.add(em(a, l * 0.07, 0, 0)); } p.add(em(laco, 0, 0.28, 0.14));
    p.add(tubo(curva([[0, 0.28, -0.1], [-0.12, 0.45, -0.2], [0.05, 0.6, -0.15]]), () => 0.025, m, { seg: 16, radial: 8 }));
    p.rotation.y = rz - 0.3;
    g.add(em(p, x, 0, 0));
  }
  return g;
}

// =============================================================================================
// Partes do corpo (de massinha cor de pele, fofas)

const corDePele = (o = {}) => pl('#f2b48a', { rugosidade: 0.5, verniz: 0.3, ...o });

function dente() {
  const g = new THREE.Group();
  const m = pl('#ffffff', { rugosidade: 0.15, verniz: 1 });
  const c = bola(0.45, m, 48); c.scale.set(1.15, 0.85, 0.85); g.add(em(c, 0, 0.75, 0));
  for (const l of [-1, 1]) { const r = capsula(0.17, 0.35, m); r.rotation.z = l * 0.18; g.add(em(r, l * 0.24, 0.35, 0)); }
  const R = 0.45; const rosto = new THREE.Group(); carinha(rosto, R, { y: 0.05, abertura: 0.36, yBoca: -0.3 }); rosto.scale.set(1.15, 0.85, 0.85); g.add(em(rosto, 0, 0.75, 0));
  g.add(em(estrela3D(0.1, 0.03, plastico('#bfe6ff', { emissivo: '#dff3ff' })), 0.42, 1.08, 0.3));
  return g;
}

function mao() {
  const g = new THREE.Group();
  const m = corDePele();
  const palma = caixa(0.7, 0.75, 0.26, 0.13, m); g.add(em(palma, 0, 0.55, 0));
  for (const [x, h] of [[-0.25, 0.45], [-0.08, 0.55], [0.09, 0.52], [0.25, 0.4]]) { const d = capsula(0.1, h, m); d.rotation.z = -x * 0.5; g.add(em(d, x * 1.1, 0.92 + h / 2, 0)); }
  const pol = capsula(0.09, 0.32, m); pol.rotation.z = 0.9; g.add(em(pol, -0.45, 0.62, 0.05));
  g.add(em(capsula(0.24, 0.25, m), 0, 0.12, 0));
  g.add(em(cilindro(0.26, 0.26, 0.14, fosco('#3d8bff'), 24), 0, 0.15, 0));
  g.rotation.y = 0.2;
  return g;
}

function pe() {
  const g = new THREE.Group();
  const m = corDePele();
  const sola = bola(0.4, m, 40); sola.scale.set(0.85, 0.42, 1.45); g.add(em(sola, 0, 0.18, 0));
  g.add(em(capsula(0.2, 0.35, m), 0, 0.42, -0.35));
  const dedos = [[-0.22, 0.13, 0.6], [-0.08, 0.1, 0.65], [0.05, 0.09, 0.63], [0.17, 0.08, 0.58], [0.27, 0.07, 0.5]];
  dedos.forEach(([x, r, z]) => { const d = bola(r, m, 16); d.scale.set(1, 0.9, 1.1); g.add(em(d, x, 0.2 + r * 0.3, z)); });
  g.rotation.y = 0.4;
  return g;
}

function boca() {
  const g = new THREE.Group();
  const m = brilho('#ff4a6a');
  const cima = new THREE.Group();
  for (const l of [-1, 1]) { const c = capsula(0.14, 0.38, m); c.rotation.z = Math.PI / 2 - l * 0.35; cima.add(em(c, l * 0.22, 0.08, 0)); }
  g.add(em(cima, 0, 0.62, 0));
  const baixo = capsula(0.17, 0.5, m); baixo.rotation.z = Math.PI / 2; baixo.scale.set(1, 1, 0.9); g.add(em(baixo, 0, 0.38, 0));
  g.add(em(caixa(0.7, 0.07, 0.18, 0.03, plastico('#7d1f24')), 0, 0.5, 0.05));
  for (const x of [-0.1, 0.1]) g.add(em(caixa(0.16, 0.08, 0.04, 0.02, branco()), x, 0.53, 0.14));
  g.add(em(bola(0.04, plastico('#ffffff', { emissivo: '#ffffff' }), 8), 0.2, 0.68, 0.14));
  g.rotation.y = -0.2;
  return g;
}

function olhoGrande() {
  const g = new THREE.Group();
  const branc = pl('#ffffff', { rugosidade: 0.15, verniz: 1 });
  const b = bola(0.55, branc, 48); b.scale.set(1.25, 0.9, 0.7); g.add(em(b, 0, 0.6, 0));
  const iris = cilindro(0.28, 0.28, 0.06, brilho('#4aa8ff'), 40); iris.rotation.x = Math.PI / 2; g.add(em(iris, 0, 0.6, 0.37));
  const pup = cilindro(0.14, 0.14, 0.07, preto(), 32); pup.rotation.x = Math.PI / 2; g.add(em(pup, 0, 0.6, 0.39));
  g.add(em(bola(0.06, plastico('#ffffff', { emissivo: '#ffffff' }), 12), 0.08, 0.68, 0.42));
  const cil = pl('#2a1a14');
  for (let k = 0; k < 5; k++) { const a = Math.PI * (0.25 + k * 0.125); g.add(segmento([Math.cos(a) * 0.62, 0.6 + Math.sin(a) * 0.46, 0.15], [Math.cos(a) * 0.8, 0.6 + Math.sin(a) * 0.62, 0.2], 0.025, cil)); }
  const pal = malha(new THREE.TorusGeometry(0.66, 0.035, 8, 40, Math.PI), cil); pal.scale.set(1.05, 0.72, 1); g.add(em(pal, 0, 0.6, 0.18));
  g.rotation.y = 0.1;
  return g;
}

function orelha() {
  const g = new THREE.Group();
  const m = corDePele();
  const s = new THREE.Shape(); s.moveTo(0, 0); s.bezierCurveTo(-0.3, 0.05, -0.1, 0.55, -0.25, 0.8); s.bezierCurveTo(-0.35, 1.2, 0.0, 1.45, 0.3, 1.3); s.bezierCurveTo(0.55, 1.15, 0.5, 0.75, 0.3, 0.45); s.bezierCurveTo(0.15, 0.2, 0.25, 0.0, 0, 0);
  g.add(estufado(s, m, 0.18, 0.1));
  const dentro = new THREE.Shape(); dentro.moveTo(0.05, 0.25); dentro.bezierCurveTo(-0.15, 0.6, -0.12, 1.1, 0.12, 1.15); dentro.bezierCurveTo(0.35, 1.15, 0.35, 0.8, 0.18, 0.6); dentro.closePath();
  g.add(em(estufado(dentro, pl('#e8957a', { rugosidade: 0.5 }), 0.04, 0.05), 0, 0, 0.15));
  g.rotation.y = -0.3;
  return grupo(em(g, 0, 0.1, 0));
}

function nariz() {
  const g = new THREE.Group();
  const m = corDePele();
  const s = new THREE.Shape();
  s.moveTo(-0.1, 1.2);
  s.bezierCurveTo(0.05, 0.95, 0.25, 0.6, 0.45, 0.35);
  s.bezierCurveTo(0.58, 0.18, 0.45, 0.0, 0.25, 0.02);
  s.bezierCurveTo(0.15, 0.03, 0.05, 0.08, -0.15, 0.05);
  s.lineTo(-0.25, 1.2);
  s.closePath();
  g.add(estufado(s, m, 0.32, 0.14));
  const asa = bola(0.25, m, 24); asa.scale.set(1.15, 0.85, 0.6); g.add(em(asa, 0.12, 0.17, 0.22));
  const furo = bola(0.07, plastico('#8a4a3a', { rugosidade: 0.5 }), 12); furo.scale.set(1.4, 0.7, 0.4); g.add(em(furo, 0.25, 0.08, 0.3));
  g.rotation.y = -0.15;
  return assentar(g);
}

function perna() {
  const g = new THREE.Group();
  const m = corDePele();
  g.add(tubo(curva([[0, 1.5, 0], [0.05, 1.0, 0.05], [0, 0.5, -0.05], [0, 0.3, 0]]), (t) => 0.2 - t * 0.06, m, { seg: 24, radial: 20 }));
  g.add(em(bola(0.16, m, 16), 0.03, 0.95, 0.12));
  const ten = new THREE.Group();
  const s = new THREE.Shape(); s.moveTo(-0.22, 0); s.lineTo(0.4, 0); s.quadraticCurveTo(0.6, 0, 0.55, 0.15); s.quadraticCurveTo(0.45, 0.28, 0.15, 0.3); s.lineTo(0.15, 0.4); s.lineTo(-0.2, 0.4); s.closePath();
  ten.add(estufado(s, brilho('#ef3b4f'), 0.3, 0.06));
  ten.add(em(caixa(0.86, 0.08, 0.4, 0.03, branco()), 0.17, 0.03, 0));
  ten.rotation.y = -Math.PI / 2;
  g.add(em(ten, 0, 0.0, -0.05));
  g.add(em(cilindro(0.17, 0.17, 0.2, fosco('#ffffff'), 20), 0, 0.42, 0));
  g.add(em(cilindro(0.175, 0.175, 0.05, fosco('#3d8bff'), 20), 0, 0.48, 0));
  g.add(em(cilindro(0.23, 0.21, 0.3, fosco('#3d8bff'), 24), 0, 1.45, 0));
  g.rotation.y = 0.5;
  return g;
}

// =============================================================================================
// Formas e cores da Lógica

function quadrado(cor) {
  return () => grupo(em(caixa(0.9, 0.9, 0.9, 0.16, brilho(cor, { rugosidade: 0.18 })), 0, 0.45, 0));
}

function triangulo() {
  const g = new THREE.Group();
  const s = poli([[-0.55, 0], [0.55, 0], [0, 0.95]]);
  const t = estufado(s, brilho('#ff3b4f', { rugosidade: 0.18 }), 0.3, 0.12); g.add(em(t, 0, 0.12, 0));
  g.rotation.y = -0.25;
  return g;
}

function losango() {
  const g = new THREE.Group();
  const s = poli([[0, -0.6], [0.45, 0], [0, 0.6], [-0.45, 0]]);
  g.add(em(estufado(s, brilho('#3d8bff', { rugosidade: 0.18 }), 0.25, 0.12), 0, 0.72, 0));
  g.rotation.y = -0.25;
  return g;
}

function coracaoDe(cor) {
  return () => {
    const g = coracaoVermelho();
    g.traverse((o) => { if (o.isMesh) o.material = brilho(cor, { rugosidade: 0.15 }); });
    return g;
  };
}

// =============================================================================================
// Objetos da escola, da casa e brinquedos (tapete)

function lapis() {
  // o lápis da Fase 8 traz a âncora 'ponta' (para a tela de pintar); a figura só leva 'topo' e 'centro'
  const g = lapisDoPremio();
  const extras = [];
  g.traverse((o) => { if (o.userData.ancora) extras.push(o); });
  for (const o of extras) o.parent.remove(o);
  return g;
}

function dado() {
  const g = new THREE.Group();
  g.add(em(caixa(0.9, 0.9, 0.9, 0.16, brilho('#ffffff', { rugosidade: 0.2 })), 0, 0.45, 0));
  const p = brilho('#ef3b4f');
  const ponto = (x, y, z, eixo) => { const b = bola(0.075, p, 16); b.scale[eixo] = 0.3; g.add(em(b, x, y, z)); };
  for (const [x, y] of [[-0.22, 0.22], [0.22, 0.22], [0, 0], [-0.22, -0.22], [0.22, -0.22]]) ponto(x, 0.45 + y, 0.45, 'z');
  for (const [x, z] of [[-0.22, -0.22], [0, 0], [0.22, 0.22]]) ponto(x, 0.9, z, 'y');
  for (const [y, z] of [[0.22, -0.22], [-0.22, 0.22], [0.22, 0.22], [-0.22, -0.22]]) ponto(0.45, 0.45 + y, z, 'x');
  g.rotation.y = -0.45;
  return g;
}

function tesoura() {
  const g = new THREE.Group();
  const lam = metal('#d6dbe4');
  for (const l of [-1, 1]) {
    const p = new THREE.Group();
    const s = poli([[-0.04, 0], [0.04, 0], [0.025, 0.85], [0, 0.95], [-0.04, 0.85]]);
    p.add(em(estufado(s, lam, 0.02, 0.02), 0, 0, l * 0.025));
    const aro = malha(new THREE.TorusGeometry(0.17, 0.06, 12, 32), brilho(l > 0 ? '#ef3b4f' : '#ef3b4f')); aro.scale.set(0.9, 1.2, 1); p.add(em(aro, 0, -0.25, l * 0.025));
    p.rotation.z = l * 0.3;
    g.add(p);
  }
  g.add(em(cilindro(0.05, 0.05, 0.12, metal('#9aa3b5'), 16).rotateX(Math.PI / 2), 0, 0, 0));
  g.rotation.set(0.25, -0.3, -0.3);
  return grupo(em(g, 0, 0.5, 0));
}

function livros() { return I.livros(); }
function livro() { return livroFechado(); }
function livroAbertoFig() {
  const g = new THREE.Group();
  const capa = brilho('#ef3b4f'), papel = pl('#ffffff', { rugosidade: 0.5, verniz: 0.3 }), linha = pl('#9aa3c8');
  for (const l of [-1, 1]) {
    const c = caixa(0.8, 0.05, 1.0, 0.03, capa); c.rotation.z = l * -0.18; g.add(em(c, l * 0.39, 0.08, 0));
    const p = caixa(0.72, 0.12, 0.9, 0.05, papel); p.rotation.z = l * -0.14; g.add(em(p, l * 0.37, 0.16, 0));
    for (let k = 0; k < 4; k++) { const t = caixa(0.42, 0.012, 0.035, 0.01, linha); t.rotation.z = l * -0.14; g.add(em(t, l * 0.38, 0.23 + 0.05, -0.25 + k * 0.16)); }
  }
  g.add(em(estrela3D(0.1, 0.03, brilho('#ffd23f')), 0.38, 0.33, 0.3).rotateX(-Math.PI / 2));
  g.rotation.set(0.75, 0, 0);
  return assentar(g);
}

function mochila() {
  const g = new THREE.Group();
  const m = pl('#ef3b4f', { rugosidade: 0.45, verniz: 0.5 });
  g.add(em(caixa(0.9, 1.1, 0.45, 0.22, m), 0, 0.55, 0));
  const tampa = bola(0.45, m, 32); tampa.scale.set(1, 0.5, 0.52); g.add(em(tampa, 0, 1.0, 0.02));
  g.add(em(caixa(0.62, 0.45, 0.16, 0.1, pl('#ffd23f')), 0, 0.38, 0.25));
  g.add(em(caixa(0.6, 0.04, 0.17, 0.02, pl('#e8b41a')), 0, 0.52, 0.27));
  g.add(em(caixa(0.06, 0.08, 0.04, 0.02, metal()), 0.2, 0.52, 0.34));
  g.add(em(caixa(0.86, 0.22, 0.05, 0.05, pl('#c92a3a')), 0, 0.92, 0.22));
  g.add(em(malha(new THREE.TorusGeometry(0.12, 0.035, 8, 20, Math.PI), pl('#c92a3a')), 0, 1.22, 0));
  g.add(em(estrela3D(0.1, 0.03, brilho('#ffffff')), -0.25, 0.75, 0.24));
  for (const l of [-1, 1]) g.add(em(caixa(0.12, 0.5, 0.1, 0.04, pl('#c92a3a')), l * 0.48, 0.45, 0.0));
  g.rotation.y = 0.3;
  return g;
}

function mala() {
  const g = new THREE.Group();
  const m = brilho('#3d8bff');
  g.add(em(caixa(1.15, 0.9, 0.42, 0.14, m), 0, 0.55, 0));
  for (const x of [-0.3, 0.3]) g.add(em(caixa(0.08, 0.92, 0.44, 0.03, pl('#2a6fe0')), x, 0.55, 0));
  g.add(em(malha(new THREE.TorusGeometry(0.15, 0.04, 8, 20, Math.PI), preto()), 0, 1.0, 0));
  for (const x of [-0.45, 0.45]) g.add(em(roda(0.07, 0.06), x, 0.07, 0.12), em(roda(0.07, 0.06), x, 0.07, -0.12));
  for (const [x, y, c] of [[0.0, 0.7, '#ffd23f'], [-0.15, 0.4, '#ef3b4f'], [0.15, 0.45, '#36c26a']]) { const e = cilindro(0.1, 0.1, 0.02, pl(c), 24); e.rotation.x = Math.PI / 2; g.add(em(e, x, y, 0.22)); }
  g.rotation.y = -0.35;
  return g;
}

function giz() {
  const g = new THREE.Group();
  [['#ef3b4f', -0.3, 0.1], ['#3d8bff', 0, 0], ['#36c26a', 0.3, -0.1]].forEach(([c, x, rz]) => {
    const p = new THREE.Group();
    const m = pl(c, { rugosidade: 0.55, verniz: 0.2 });
    p.add(em(cilindro(0.1, 0.1, 0.85, m, 20), 0, 0.43, 0));
    p.add(em(malha(new THREE.ConeGeometry(0.1, 0.22, 20), m), 0, 0.96, 0));
    p.add(em(cilindro(0.105, 0.105, 0.55, pl('#ffffff', { rugosidade: 0.6 }), 20), 0, 0.35, 0));
    p.add(em(cilindro(0.106, 0.106, 0.04, m, 20), 0, 0.5, 0), em(cilindro(0.106, 0.106, 0.04, m, 20), 0, 0.2, 0));
    p.rotation.z = rz;
    g.add(em(p, x, 0, 0));
  });
  g.rotation.y = 0.2;
  return g;
}

function regua() {
  const g = new THREE.Group();
  g.add(em(caixa(1.8, 0.36, 0.08, 0.04, brilho('#ffd23f')), 0, 0, 0));
  for (let k = 0; k <= 16; k++) g.add(em(caixa(0.02, k % 4 === 0 ? 0.15 : 0.08, 0.02, 0.005, preto()), -0.8 + k * 0.1, 0.18 - (k % 4 === 0 ? 0.075 : 0.04), 0.045));
  g.rotation.set(-0.15, -0.25, 0.45);
  return assentar(g);
}

function trompete() {
  const g = new THREE.Group();
  const o = plastico('#ffc21a', { rugosidade: 0.15, verniz: 1 });
  const sino = torneado([[0, 0], [0.08, 0], [0.09, 0.35], [0.16, 0.55], [0.34, 0.72], [0.48, 0.8], [0, 0.8]], o, 40);
  sino.rotation.z = -Math.PI / 2; g.add(em(sino, -0.1, 0, 0));
  g.add(tubo(curva([[-0.8, 0, 0], [-0.3, 0.0, 0], [-0.2, 0.18, 0], [-0.55, 0.26, 0], [-0.75, 0.18, 0]]), () => 0.06, o, { seg: 30, radial: 12 }));
  g.add(em(cilindro(0.08, 0.05, 0.15, o, 16).rotateZ(Math.PI / 2), -0.88, 0, 0));
  for (const x of [-0.52, -0.4, -0.28]) { g.add(em(cilindro(0.04, 0.04, 0.28, o, 12), x, 0.14, 0)); g.add(em(cilindro(0.06, 0.06, 0.05, branco(), 12), x, 0.3, 0)); }
  g.rotation.set(0, -0.35, 0.12);
  return assentar(g, 0.05);
}

function computador() {
  const g = new THREE.Group();
  const cinza = metal('#c9cfdb');
  g.add(em(caixa(1.3, 0.06, 0.85, 0.04, cinza), 0, 0.03, 0.25));
  g.add(em(caixa(1.1, 0.02, 0.45, 0.01, plastico('#3a3a4a', { rugosidade: 0.4 })), 0, 0.065, 0.18));
  g.add(em(caixa(0.36, 0.02, 0.18, 0.01, plastico('#b0b8c8', { rugosidade: 0.3 })), 0, 0.065, 0.55));
  const tela = new THREE.Group();
  tela.add(caixa(1.3, 0.85, 0.05, 0.04, cinza));
  tela.add(em(caixa(1.18, 0.73, 0.02, 0.02, plastico('#4aa8ff', { emissivo: '#5ab4ff', rugosidade: 0.2 })), 0, 0, 0.03));
  tela.add(em(estrela3D(0.12, 0.03, brilho('#ffd23f', { emissivo: '#ffb000' })), -0.3, 0.1, 0.05));
  tela.add(em(caixa(0.4, 0.06, 0.02, 0.02, plastico('#ffffff', { emissivo: '#ffffff' })), 0.15, 0.15, 0.05), em(caixa(0.3, 0.06, 0.02, 0.02, plastico('#ffffff', { emissivo: '#ffffff' })), 0.1, 0.0, 0.05));
  tela.rotation.x = -0.25;
  g.add(em(tela, 0, 0.45, -0.22));
  g.rotation.y = -0.3;
  return g;
}

function chave() {
  const g = new THREE.Group();
  const o = plastico('#ffc21a', { rugosidade: 0.15, verniz: 1 });
  const aro = malha(new THREE.TorusGeometry(0.25, 0.08, 16, 40), o); g.add(em(aro, -0.55, 0, 0));
  g.add(em(capsula(0.06, 0.85, o).rotateZ(Math.PI / 2), 0.1, 0, 0));
  for (const x of [0.35, 0.5]) g.add(em(caixa(0.08, 0.2, 0.08, 0.02, o), x, -0.1, 0));
  g.add(em(estrela3D(0.1, 0.04, brilho('#ef3b4f')), -0.55, 0, 0.08));
  g.rotation.set(0.25, -0.3, 0.45);
  return grupo(em(g, 0, 0.5, 0));
}

function paleta() { return paletaDoPremio(); }

function escova() {
  const g = new THREE.Group();
  g.add(em(caixa(1.3, 0.14, 0.18, 0.07, brilho('#3dbfb0')), 0, 0, 0));
  g.add(em(caixa(0.4, 0.05, 0.19, 0.02, brilho('#ffffff')), -0.25, 0.07, 0));
  for (let i = 0; i < 5; i++) for (const z of [-0.045, 0.045]) g.add(em(cilindro(0.03, 0.03, 0.22, pl(i % 2 ? '#ffffff' : '#7fd3ff'), 8), 0.42 + i * 0.055, 0.16, z));
  g.add(tubo(curva([[0.4, 0.3, 0], [0.5, 0.36, 0.02], [0.6, 0.31, -0.02], [0.67, 0.35, 0]]), () => 0.055, pl('#ffffff'), { seg: 16, radial: 10 }));
  g.rotation.set(0.45, -0.3, 0.55);
  return assentar(g);
}

function cadeira() {
  const g = new THREE.Group();
  const m = pl('#d9975a', { rugosidade: 0.45 });
  g.add(em(caixa(0.8, 0.1, 0.75, 0.04, m), 0, 0.6, 0));
  g.add(em(caixa(0.7, 0.08, 0.66, 0.04, fosco('#ef3b4f')), 0, 0.68, 0.02));
  for (const x of [-0.33, 0.33]) for (const z of [-0.3, 0.3]) g.add(em(caixa(0.08, 0.6, 0.08, 0.03, m), x, 0.3, z));
  for (const x of [-0.33, 0.33]) g.add(em(caixa(0.08, 0.7, 0.08, 0.03, m), x, 1.0, -0.33));
  for (const y of [0.9, 1.2]) g.add(em(caixa(0.75, 0.12, 0.06, 0.03, m), 0, y, -0.33));
  g.rotation.y = -0.45;
  return g;
}

function microfone() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.09, 0.06, 0.8, plastico('#3a3a4a', { rugosidade: 0.25, verniz: 1 }), 24), 0, 0.4, 0));
  g.add(em(cilindro(0.11, 0.11, 0.1, brilho('#ef3b6a'), 24), 0, 0.82, 0));
  const cab = bola(0.24, metal('#c9cfdb'), 32); g.add(em(cab, 0, 1.0, 0));
  for (let k = 0; k < 3; k++) { const a = malha(new THREE.TorusGeometry(0.24, 0.01, 6, 32), metal('#9aa3b5')); a.rotation.y = (k / 3) * Math.PI; g.add(em(a, 0, 1.0, 0)); }
  g.add(em(cilindro(0.04, 0.04, 0.02, brilho('#ffd23f'), 12).rotateX(Math.PI / 2), 0, 0.55, 0.09));
  g.rotation.set(0, 0, -0.35);
  return grupo(em(g, 0.1, 0.1, 0));
}

function quebraCabeca() {
  const g = new THREE.Group();
  const peca = (cor) => {
    const s = new THREE.Shape(); const L = 0.4;
    s.moveTo(-L, -L); s.lineTo(-0.1, -L); s.absarc(0, -L, 0.1, Math.PI, 0, true); s.lineTo(L, -L); s.lineTo(L, -0.1); s.absarc(L, 0, 0.1, -Math.PI / 2, Math.PI / 2, false); s.lineTo(L, L); s.lineTo(-L, L); s.closePath();
    return estufado(s, brilho(cor, { rugosidade: 0.2 }), 0.12, 0.05);
  };
  const a = peca('#3d8bff'); a.rotation.x = -Math.PI / 2 + 0.1; g.add(em(a, -0.42, 0.12, 0.1));
  const b = peca('#ffc21a'); b.rotation.set(-0.3, 0.3, 0.2); g.add(em(b, 0.42, 0.45, -0.05));
  g.rotation.y = -0.2;
  return g;
}

function tijolo() {
  const g = new THREE.Group();
  const m = pl('#d8613a', { rugosidade: 0.6, verniz: 0.15 });
  for (const [x, y, z] of [[-0.45, 0.17, 0], [0.45, 0.17, 0], [0, 0.52, 0]]) g.add(em(caixa(0.85, 0.32, 0.45, 0.06, m), x, y, z));
  const arg = pl('#f2e6d0', { rugosidade: 0.7 });
  g.add(em(caixa(1.75, 0.03, 0.42, 0.01, arg), 0, 0.345, 0), em(caixa(0.03, 0.3, 0.42, 0.01, arg), 0, 0.17, 0));
  g.rotation.y = -0.35;
  return g;
}

function robo() {
  const g = new THREE.Group();
  const m = metal('#b8c4da'), az = brilho('#3d8bff');
  g.add(em(caixa(0.75, 0.7, 0.5, 0.12, m), 0, 0.65, 0));
  g.add(em(caixa(0.4, 0.3, 0.05, 0.05, az), 0, 0.68, 0.25));
  for (const [x, c] of [[-0.1, '#ef3b4f'], [0.1, '#36c26a']]) g.add(em(bola(0.05, luz(c), 12), x, 0.68, 0.29));
  g.add(em(caixa(0.65, 0.5, 0.5, 0.14, m), 0, 1.3, 0));
  g.add(em(caixa(0.5, 0.3, 0.05, 0.08, plastico('#2b3550', { rugosidade: 0.2, verniz: 1 })), 0, 1.32, 0.25));
  for (const l of [-1, 1]) {
    g.add(em(bola(0.07, luz('#7fd3ff'), 16), l * 0.12, 1.35, 0.29));
    g.add(em(cilindro(0.08, 0.08, 0.1, az, 16).rotateZ(Math.PI / 2), l * 0.36, 1.3, 0));
    g.add(segmento([l * 0.42, 0.85, 0], [l * 0.55, 0.45, 0.1], 0.07, m));
    const p = malha(new THREE.TorusGeometry(0.08, 0.03, 8, 16, Math.PI * 1.4), az); p.rotation.z = -Math.PI / 2 - 0.7; g.add(em(p, l * 0.56, 0.36, 0.1));
    g.add(em(caixa(0.22, 0.3, 0.26, 0.06, m), l * 0.2, 0.15, 0));
    g.add(em(caixa(0.26, 0.08, 0.34, 0.03, az), l * 0.2, 0.04, 0.03));
  }
  const s = sorriso(0.08, 0.02, 2.4, '#7fd3ff'); g.add(em(s, 0, 1.25, 0.28));
  g.add(em(cilindro(0.02, 0.02, 0.2, m, 8), 0, 1.65, 0), em(bola(0.06, luz('#ef3b4f'), 12), 0, 1.78, 0));
  g.rotation.y = 0.25;
  return g;
}

function ioio() {
  const g = new THREE.Group();
  for (const z of [-0.12, 0.12]) { const d = bola(0.45, brilho('#ef3b4f'), 40); d.scale.set(1, 1, 0.3); g.add(em(d, 0, 0, z)); }
  g.add(em(cilindro(0.12, 0.12, 0.2, brilho('#ffffff'), 20).rotateX(Math.PI / 2), 0, 0, 0));
  g.add(em(estrela3D(0.18, 0.04, brilho('#ffd23f')), 0, 0, 0.26));
  g.add(tubo(curva([[0, 0.1, 0], [0.05, 0.35, 0], [0.0, 0.6, 0]]), () => 0.015, pl('#ffffff'), { seg: 16, radial: 6 }));
  g.add(em(malha(new THREE.TorusGeometry(0.06, 0.02, 6, 16), pl('#ffffff')), 0, 0.66, 0));
  g.rotation.y = -0.35;
  return grupo(em(g, 0, 0.48, 0));
}

function guardaChuva() {
  const g = new THREE.Group();
  const n = 8;
  for (let k = 0; k < n; k++) {
    const a0 = (k / n) * Math.PI * 2;
    const p = malha(new THREE.SphereGeometry(0.8, 8, 16, a0, (Math.PI * 2) / n, 0, Math.PI / 2.3), brilho(k % 2 ? '#ffffff' : '#9a62ff'));
    p.material.side = THREE.DoubleSide;
    p.scale.set(1, 0.6, 1);
    g.add(p);
  }
  g.add(em(bola(0.05, brilho('#ffd23f'), 12), 0, 0.5, 0));
  g.add(em(cilindro(0.025, 0.025, 1.1, metal('#9aa3b5'), 8), 0, -0.05, 0));
  g.add(tubo(curva([[0, -0.6, 0], [0, -0.7, 0], [0.1, -0.78, 0], [0.18, -0.7, 0]]), () => 0.045, brilho('#9a62ff'), { seg: 16, radial: 10 }));
  g.rotation.set(0.1, -0.2, 0.25);
  return grupo(em(g, 0, 0.85, 0));
}

function violao() { return I.violao(); }

function fone() {
  const g = new THREE.Group();
  const m = brilho('#ef3b6a');
  g.add(tubo(curva([[-0.5, 0.1, 0], [-0.48, 0.55, 0], [0, 0.82, 0], [0.48, 0.55, 0], [0.5, 0.1, 0]]), () => 0.07, m, { seg: 40, radial: 14 }));
  for (const l of [-1, 1]) {
    const c = cilindro(0.26, 0.26, 0.2, m, 32); c.rotation.z = Math.PI / 2; g.add(em(c, l * 0.55, 0.0, 0));
    const a = cilindro(0.22, 0.22, 0.08, fosco('#2b2438'), 32); a.rotation.z = Math.PI / 2; g.add(em(a, l * 0.43, 0.0, 0));
  }
  g.rotation.y = -0.45;
  return grupo(em(g, 0, 0.3, 0));
}

function cama() { return I.cama(); }
function trofeu() { return trofeuDoPremio(); }
function medalha() { return medalhaDoPremio(); }

function caneta() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.08, 0.08, 1.1, brilho('#3d8bff'), 20), 0, 0, 0));
  g.add(em(cilindro(0.085, 0.085, 0.35, brilho('#2a6fe0'), 20), 0, 0.42, 0));
  g.add(em(caixa(0.03, 0.3, 0.04, 0.01, metal()), 0.09, 0.45, 0));
  g.add(em(malha(new THREE.ConeGeometry(0.08, 0.2, 20), metal('#c9cfdb')), 0, -0.65, 0).rotateX(Math.PI));
  g.add(em(bola(0.02, preto(), 8), 0, -0.76, 0));
  g.rotation.set(0.1, 0, -0.6);
  return grupo(em(g, 0, 0.55, 0));
}

function videogame() {
  const g = new THREE.Group();
  const m = brilho('#6c63ff');
  g.add(em(caixa(0.8, 0.35, 0.18, 0.15, m), 0, 0, 0));
  for (const l of [-1, 1]) { const p = capsula(0.17, 0.2, m); p.rotation.z = l * 0.5; g.add(em(p, l * 0.42, -0.12, 0)); }
  g.add(em(caixa(0.22, 0.07, 0.06, 0.02, plastico('#2b2438')), -0.3, 0.02, 0.1), em(caixa(0.07, 0.22, 0.06, 0.02, plastico('#2b2438')), -0.3, 0.02, 0.1));
  for (const [x, y, c] of [[0.32, 0.08, '#ef3b4f'], [0.42, 0.0, '#ffd23f'], [0.32, -0.08, '#36c26a'], [0.22, 0.0, '#3d8bff']]) g.add(em(bola(0.045, brilho(c), 12), x, y, 0.1));
  g.rotation.set(-0.5, 0.25, 0);
  return grupo(em(g, 0, 0.35, 0));
}

function piano() { return pianinho(); }

function lampada() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.12, 0.0], [0.16, 0.05], [0.16, 0.3], [0, 0.3]], metal('#c9cfdb'), 32));
  for (const y of [0.1, 0.18, 0.26]) g.add(em(malha(new THREE.TorusGeometry(0.165, 0.015, 6, 24), metal('#9aa3b5')).rotateX(Math.PI / 2), 0, y, 0));
  g.add(torneado([[0.15, 0.3], [0.2, 0.45], [0.42, 0.75], [0.44, 1.0], [0.3, 1.2], [0, 1.25]], plastico('#ffd84a', { rugosidade: 0.05, verniz: 1, emissivo: '#ffc21a', ambiente: 0.7 }), 48));
  g.add(tubo(curva([[-0.06, 0.4, 0], [-0.08, 0.7, 0], [0, 0.78, 0], [0.08, 0.7, 0], [0.06, 0.4, 0]]), () => 0.018, plastico('#ff7a1a', { emissivo: '#ff7a1a' }), { seg: 20, radial: 6 }));
  g.add(em(bola(0.07, plastico('#ffffff', { emissivo: '#ffffff' }), 12), -0.22, 0.95, 0.3));
  g.rotation.z = 0.1;
  return grupo(g);
}

function cesto() {
  const g = new THREE.Group();
  const m = pl('#d9a05a', { rugosidade: 0.55 });
  g.add(em(cilindro(0.6, 0.45, 0.6, m, 40), 0, 0.3, 0));
  for (const y of [0.12, 0.27, 0.42]) g.add(em(malha(new THREE.TorusGeometry(0.47 + y * 0.22, 0.04, 8, 40), pl('#c08848')).rotateX(Math.PI / 2), 0, y, 0));
  g.add(em(malha(new THREE.TorusGeometry(0.61, 0.06, 10, 40), pl('#c08848')).rotateX(Math.PI / 2), 0, 0.6, 0));
  g.add(tubo(curva([[-0.55, 0.6, 0], [-0.45, 1.2, 0], [0.45, 1.2, 0], [0.55, 0.6, 0]]), () => 0.05, pl('#c08848'), { seg: 30, radial: 10 }));
  const pano = bola(0.5, fosco('#ef3b4f'), 24); pano.scale.set(1.1, 0.25, 1.1); g.add(em(pano, 0, 0.62, 0));
  for (const [x, z] of [[-0.18, 0.15], [0.15, 0.1], [0, -0.15]]) g.add(em(bola(0.15, brilho('#ee2436'), 16), x, 0.72, z));
  g.rotation.y = 0.2;
  return g;
}

function presente() { return presenteDaCasa(1); }

function esqui() {
  const g = new THREE.Group();
  for (const z of [-0.2, 0.2]) {
    const e = new THREE.Group();
    e.add(em(caixa(1.8, 0.06, 0.22, 0.025, brilho('#ef3b4f')), 0, 0, 0));
    const bico = tubo(curva([[0.86, 0.0, 0], [1.0, 0.06, 0], [1.05, 0.2, 0]]), () => 0.035, brilho('#ef3b4f'), { seg: 10, radial: 8 });
    bico.scale.set(1, 1, 3.0); e.add(bico);
    e.add(em(caixa(0.32, 0.12, 0.24, 0.04, metal()), -0.05, 0.07, 0));
    g.add(em(e, 0, 0.05, z));
  }
  for (const l of [-1, 1]) {
    const x = -0.5 + l * 0.12;
    g.add(em(cilindro(0.03, 0.03, 1.35, metal('#9aa3b5'), 8), x, 0.68, -0.45 + l * 0.05));
    g.add(em(malha(new THREE.TorusGeometry(0.09, 0.02, 6, 16), preto()).rotateX(Math.PI / 2), x, 0.12, -0.45 + l * 0.05));
    g.add(em(capsula(0.045, 0.18, brilho('#3d8bff')), x, 1.38, -0.45 + l * 0.05));
  }
  g.rotation.y = -0.45;
  return g;
}

function treno() {
  const g = new THREE.Group();
  const m = brilho('#ef3b4f'), mad = pl('#d9975a');
  for (const z of [-0.32, 0.32]) {
    g.add(tubo(curva([[-0.75, 0.05, z], [0.6, 0.05, z], [0.85, 0.12, z], [0.88, 0.35, z], [0.72, 0.42, z]]), () => 0.04, metal('#ffc21a'), { seg: 24, radial: 8 }));
    for (const x of [-0.5, 0.0, 0.45]) g.add(em(cilindro(0.025, 0.025, 0.25, metal('#ffc21a'), 8), x, 0.17, z));
  }
  for (let k = 0; k < 5; k++) g.add(em(caixa(0.22, 0.06, 0.8, 0.02, mad), -0.6 + k * 0.27, 0.32, 0));
  g.add(em(caixa(1.4, 0.08, 0.06, 0.02, m), -0.05, 0.37, 0.38), em(caixa(1.4, 0.08, 0.06, 0.02, m), -0.05, 0.37, -0.38));
  g.rotation.y = -0.45;
  return g;
}

// =============================================================================================
// Transportes

function caminhao({ cab = '#ef3b4f', carga = '#ffffff', bombeiro = false } = {}) {
  return () => {
    const g = new THREE.Group();
    const mc = brilho(cab);
    g.add(em(caixa(0.65, 0.65, 0.85, 0.15, mc), 0.62, 0.62, 0));
    g.add(em(caixa(0.05, 0.3, 0.6, 0.04, vidro()), 0.95, 0.75, 0));
    for (const l of [-1, 1]) g.add(em(caixa(0.3, 0.26, 0.04, 0.04, vidro()), 0.66, 0.75, l * 0.43));
    if (bombeiro) {
      g.add(em(caixa(1.2, 0.5, 0.85, 0.1, mc), -0.35, 0.55, 0));
      g.add(em(caixa(1.4, 0.06, 0.2, 0.02, metal('#e0e4ec')), -0.2, 0.92, 0));
      for (let k = 0; k < 7; k++) g.add(em(caixa(0.03, 0.06, 0.3, 0.01, metal('#e0e4ec')), -0.8 + k * 0.2, 0.92, 0));
      g.add(em(caixa(1.15, 0.08, 0.86, 0.03, brilho('#ffffff')), -0.35, 0.45, 0));
      g.add(em(bola(0.08, luz('#4aa8ff'), 12), 0.62, 1.0, 0.15), em(bola(0.08, luz('#ff4a4a'), 12), 0.62, 1.0, -0.15));
      const mang = malha(new THREE.TorusGeometry(0.15, 0.06, 10, 24), brilho('#ffd23f')); mang.rotation.y = Math.PI / 2; g.add(em(mang, -0.35, 0.55, 0.45));
    } else {
      g.add(em(caixa(1.2, 0.75, 0.9, 0.08, brilho(carga)), -0.35, 0.68, 0));
      g.add(em(estrela3D(0.18, 0.04, brilho('#ffd23f')), -0.35, 0.68, 0.46));
    }
    g.add(em(caixa(1.95, 0.14, 0.8, 0.05, plastico('#3a3a4a', { rugosidade: 0.4 })), 0.0, 0.28, 0));
    for (const x of [-0.6, 0.6]) for (const z of [-0.42, 0.42]) g.add(em(roda(0.22, 0.16), x, 0.22, z));
    for (const z of [-0.28, 0.28]) g.add(em(bola(0.07, plastico('#fff3b0', { emissivo: '#ffe066', rugosidade: 0.1 }), 12), 0.95, 0.45, z));
    g.rotation.y = -0.6;
    return g;
  };
}

function onibus() {
  const g = new THREE.Group();
  const m = brilho('#ffc21a');
  g.add(em(caixa(2.0, 0.85, 0.85, 0.18, m), 0, 0.68, 0));
  for (const l of [-1, 1]) for (let k = 0; k < 4; k++) g.add(em(caixa(0.32, 0.28, 0.04, 0.06, vidro()), -0.7 + k * 0.42, 0.85, l * 0.43));
  g.add(em(caixa(0.04, 0.32, 0.68, 0.06, vidro()), 1.0, 0.85, 0));
  g.add(em(caixa(2.02, 0.08, 0.87, 0.03, brilho('#2b2438')), 0, 0.55, 0));
  g.add(em(caixa(0.3, 0.5, 0.04, 0.05, vidro()), 0.75, 0.62, 0.43));
  for (const x of [-0.6, 0.6]) for (const z of [-0.42, 0.42]) g.add(em(roda(0.2, 0.16), x, 0.22, z));
  for (const z of [-0.28, 0.28]) g.add(em(bola(0.07, plastico('#fff3b0', { emissivo: '#ffe066', rugosidade: 0.1 }), 12), 1.0, 0.45, z));
  g.rotation.y = -0.55;
  return g;
}

function bicicleta() { return I.bicicleta(); }

function foguete() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.3, 0.05], [0.4, 0.4], [0.42, 0.85], [0.35, 1.2], [0.2, 1.48], [0, 1.6]], brilho('#ffffff'), 48));
  g.add(torneado([[0.25, 1.32], [0.21, 1.46], [0.12, 1.55], [0, 1.61]].map(([r, y]) => [r + 0.005, y]).concat([[0, 1.61]]), brilho('#ef3b4f'), 48));
  g.add(em(cilindro(0.42, 0.42, 0.12, brilho('#ef3b4f'), 48), 0, 0.5, 0));
  const jan = cilindro(0.16, 0.16, 0.06, vidro(), 32); jan.rotation.x = Math.PI / 2; g.add(em(jan, 0, 0.95, 0.4));
  g.add(em(malha(new THREE.TorusGeometry(0.16, 0.04, 10, 32), brilho('#3d8bff')), 0, 0.95, 0.42));
  for (let k = 0; k < 3; k++) {
    const a = (k / 3) * Math.PI * 2 + Math.PI / 6;
    const s = poli([[0, 0], [0.35, -0.15], [0.35, 0.05], [0, 0.45]]);
    const a1 = estufado(s, brilho('#ef3b4f'), 0.06, 0.03);
    const p = new THREE.Group(); p.add(em(a1, 0.3, 0, 0)); p.rotation.y = -a;
    g.add(em(p, 0, 0.12, 0));
  }
  const fogo = torneado([[0, 0], [0.18, -0.05], [0.12, -0.25], [0, -0.45]], plastico('#ff9a1a', { emissivo: '#ff7a1a', rugosidade: 0.2 }), 24);
  g.add(em(fogo, 0, 0.02, 0));
  g.rotation.set(0, 0.2, -0.35);
  return grupo(g);
}

function helicoptero() {
  const g = new THREE.Group();
  const m = brilho('#36c26a');
  const c = bola(0.5, m, 40); c.scale.set(1.2, 0.9, 0.9); g.add(em(c, 0, 0.7, 0));
  const v = bola(0.38, vidro(), 32); v.scale.set(0.9, 0.85, 0.85); g.add(em(v, 0.32, 0.78, 0));
  g.add(tubo(curva([[-0.5, 0.75, 0], [-1.0, 0.85, 0], [-1.25, 0.95, 0]]), (t) => 0.13 * (1 - t * 0.5), m, { seg: 16, radial: 12 }));
  g.add(em(caixa(0.12, 0.35, 0.05, 0.03, brilho('#ffd23f')), -1.25, 1.05, 0.05));
  g.add(em(cilindro(0.05, 0.05, 0.2, metal('#9aa3b5'), 12), 0, 1.22, 0));
  for (const a of [0.3, 0.3 + Math.PI / 2]) { const p = caixa(1.9, 0.03, 0.12, 0.015, plastico('#3a3a4a', { rugosidade: 0.3 })); p.rotation.y = a; g.add(em(p, 0, 1.33, 0)); }
  for (const z of [-0.32, 0.32]) {
    g.add(em(capsula(0.035, 1.0, metal('#9aa3b5')).rotateZ(Math.PI / 2), 0.05, 0.08, z));
    for (const x of [-0.25, 0.3]) g.add(segmento([x, 0.1, z], [x * 0.8, 0.4, z * 0.6], 0.03, metal('#9aa3b5')));
  }
  g.rotation.y = -0.55;
  return g;
}

function trator() {
  const g = new THREE.Group();
  const m = brilho('#36b04a');
  g.add(em(caixa(0.9, 0.45, 0.6, 0.1, m), 0.35, 0.6, 0));
  g.add(em(caixa(0.55, 0.75, 0.75, 0.08, m), -0.3, 0.85, 0));
  g.add(em(caixa(0.62, 0.06, 0.82, 0.03, brilho('#ffc21a')), -0.3, 1.24, 0));
  for (const l of [-1, 1]) g.add(em(caixa(0.4, 0.35, 0.04, 0.04, vidro()), -0.3, 0.95, l * 0.38));
  g.add(em(cilindro(0.05, 0.05, 0.35, preto(), 12), 0.55, 0.98, 0.15));
  for (const z of [-0.42, 0.42]) { g.add(em(roda(0.42, 0.24), -0.35, 0.42, z)); g.add(em(roda(0.22, 0.16), 0.6, 0.22, z * 0.9)); }
  g.rotation.y = -0.6;
  return g;
}

function assentar(g, folga = 0) {
  const w = grupo(g);
  w.updateMatrixWorld(true);
  const b = new THREE.Box3().setFromObject(w);
  g.position.y += folga - b.min.y;
  return w;
}

function calcado({ cor, sola = '#ffffff', cano = 0, borda = null, cadarco = null, salto = false, brilhoso = false, giro = -0.55 }) {
  const g = new THREE.Group();
  const m = brilhoso ? plastico(cor, { rugosidade: 0.18, verniz: 1 }) : pl(cor);
  const ms = pl(sola, { rugosidade: 0.5 });
  g.add(em(caixa(1.3, 0.14, 0.6, 0.06, ms), 0.05, 0.07, 0));
  const bico = bola(0.32, m, 40); bico.scale.set(1.3, 0.75, 0.92); g.add(em(bico, 0.3, 0.27, 0));
  g.add(em(caixa(0.72, 0.56, 0.56, 0.22, m), -0.22, 0.4, 0));
  if (salto) g.add(em(caixa(0.3, 0.14, 0.5, 0.04, ms), -0.38, 0.0, 0));
  if (cano) {
    g.add(em(cilindro(0.27, 0.28, cano, m, 32), -0.3, 0.55 + cano / 2, 0));
    if (borda) g.add(em(malha(new THREE.TorusGeometry(0.27, 0.07, 12, 32), pl(borda, { rugosidade: 0.7, verniz: 0.1 })).rotateX(Math.PI / 2), -0.3, 0.55 + cano, 0));
  } else {
    const ab = bola(0.2, plastico('#3a2a2a', { rugosidade: 0.7 }), 20); ab.scale.set(1.3, 0.25, 0.9); g.add(em(ab, -0.3, 0.68, 0));
  }
  if (cadarco) {
    const mc = pl(cadarco, { rugosidade: 0.5 });
    for (let k = 0; k < 3; k++) { const c = caixa(0.06, 0.035, 0.36, 0.015, mc); c.rotation.z = -0.7; g.add(em(c, 0.14 - k * 0.12, 0.5 + k * 0.07, 0)); }
  }
  g.rotation.y = giro;
  return g;
}

// =============================================================================================

export const modelosDoLote9a = {
  // mar e água
  polvo, caranguejo, golfinho, tubarao, baleia, concha, camarao, lula, jacare, peixinho, baiacu, onda, barco, navio, canoa,
  // bichos
  papagaio, cobra, zebra, girafa, coruja, gorila, urso, urso_polar: ursoPolar, onca, preguica, lagartixa, caracol, formiga, camelo, panda, dinossauro,
  peru, cabra, boi,
  // natureza e lugares
  arvore, boneco_de_neve: bonecoDeNeve, floco_de_neve: floco, sol, chuva, nuvem, palmeira, cacto, girassol, broto, arco_iris: arcoIris, raio, rosa,
  cogumelo, trevo, fogo, pedra, montanha, vulcao, osso, casa, escola, circo,
  // comidas
  leite, pipoca, limao, gelo, pera, queijo, bala, melancia, sorvete, tomate, abacaxi, pizza, maca_verde: macaVerde, laranja, biscoito, mel, alface,
  pepino, ovo_frito: ovoFrito, copo, coco, pirulito, batata, brocolis, milho, pessego, vela,
  // roupas e calçados
  tenis, chinelo, calca, cartola, bota, bermuda, casaco, vestido, maio, oculos, sapato, patins, sapatilha,
  // corpo
  dente, mao, pe, boca, olho: olhoGrande, orelha, nariz, perna,
  // formas e cores
  quadrado_azul: quadrado('#3d8bff'), quadrado_vermelho: quadrado('#ff3b4f'), quadrado_verde: quadrado('#36c26a'),
  quadrado_amarelo: quadrado('#ffc21a'), quadrado_roxo: quadrado('#9a62ff'), quadrado_laranja: quadrado('#ff8a1c'),
  triangulo, losango,
  coracao_amarelo: coracaoDe('#ffc21a'), coracao_azul: coracaoDe('#3d8bff'), coracao_roxo: coracaoDe('#9a62ff'),
  coracao_laranja: coracaoDe('#ff8a1c'), coracao_verde: coracaoDe('#36c26a'),
  // escola, casa e brinquedos
  lapis, dado, tesoura, livros, livro, livro_aberto: livroAbertoFig, mochila, mala, giz_de_cera: giz, regua, trompete, computador, chave, paleta,
  escova_de_dente: escova, cadeira, microfone, quebra_cabeca: quebraCabeca, tijolo, robo, ioio, guarda_chuva: guardaChuva, violao, fone, cama,
  trofeu, medalha, caneta, videogame, piano, lampada, cesto, presente, esqui, treno,
  // transportes
  caminhao_de_bombeiro: caminhao({ bombeiro: true }), caminhao: caminhao({ cab: '#3d8bff', carga: '#ffffff' }), onibus, bicicleta, foguete, helicoptero, trator,
};
