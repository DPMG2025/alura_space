// Guarda-roupa de gatinha fofa (Fase "gatinha"): 2 trajes (`gatinha`, `lacos`) e 5 acessórios (`orelhasgato`,
// `bonegato`, `lacogrande`, `bigodes`, `mascaragata`), no mesmo formato de guardaRoupa.js (TRAJES e ACESSORIOS).
// Desenhos originais: gatinho laranja listrado, lacinhos, orelhinhas, focinho e bigodes de gato. Sem personagem de marca.
//
// Importa os utilitários de guardaRoupa.js (que por sua vez importa este arquivo para registrar as peças): os
// utilitários só são usados dentro das funções, nunca na carga do módulo, então o ciclo é inofensivo.
import { THREE, plastico, malha, bola, capsula, em, naSuperficie } from '../kit.js';
import { MEDIDAS, OLHO_X, OLHO_Y } from './personagens.js';
import {
  tecido, fosco, brilho, grupo, torno, tubo, anel, sobre, casca, calca, mangasCurtas, sapato, lacinho,
  naCabeca, raioNoPerfil, faixa,
} from './guardaRoupa.js';

const M = MEDIDAS.crianca;
const R = M.R;
const EIXO_X = new THREE.Vector3(1, 0, 0);

// ---------------------------------------------------------------------------------------------
// Formas

/** Contorno extrudado com bisel macio, virado para +z, centrado só em z (a base fica onde foi desenhada). */
function extr(forma, prof, mat, bisel = prof * 0.4) {
  const geo = new THREE.ExtrudeGeometry(forma, { depth: prof, bevelEnabled: true, bevelThickness: bisel, bevelSize: bisel, bevelSegments: 4, curveSegments: 24 });
  geo.translate(0, 0, -prof / 2);
  return malha(geo, mat);
}

/** Orelha de gato: base em y = 0 (largura [l]), ponta arredondada em y = [a]. */
function formaOrelha(l, a) {
  const s = new THREE.Shape();
  s.moveTo(-l / 2, 0);
  s.bezierCurveTo(-l * 0.56, a * 0.45, -l * 0.3, a * 0.86, -l * 0.07, a * 0.97);
  s.quadraticCurveTo(0, a * 1.04, l * 0.07, a * 0.97);
  s.bezierCurveTo(l * 0.3, a * 0.86, l * 0.5, a * 0.42, l / 2, 0);
  s.quadraticCurveTo(0, -a * 0.1, -l / 2, 0);
  return s;
}

/** Orelha de gato de [fora] com o miolo [dentro]; base em y = 0, virada para +z. */
function orelha(l, a, fora, dentro, prof = R * 0.12) {
  const g = new THREE.Group();
  g.add(extr(formaOrelha(l, a), prof, fora, prof * 0.45));
  const miolo = extr(formaOrelha(l * 0.58, a * 0.66), prof * 0.4, dentro, prof * 0.2);
  miolo.position.set(0, a * 0.1, prof * 0.5);
  g.add(miolo);
  return g;
}

const matPelo = () => fosco('#fff3ea');
const matRosaOrelha = () => plastico('#ff9fc3', { rugosidade: 0.6, verniz: 0.1, ambiente: 0.5 });

/** Patinha (almofada e quatro dedinhos) virada para +z, de raio ~ [r]. */
function patinha(r, mat) {
  const g = new THREE.Group();
  const almofada = bola(r * 0.5, mat, 24);
  almofada.scale.set(1.15, 0.9, 0.35);
  almofada.position.y = -r * 0.22;
  almofada.castShadow = false;
  g.add(almofada);
  for (const [x, y, k] of [[-0.62, 0.22, 0.2], [-0.22, 0.58, 0.24], [0.22, 0.58, 0.24], [0.62, 0.22, 0.2]]) {
    const d = bola(r * k * 1.15, mat, 16);
    d.scale.set(0.9, 1.1, 0.4);
    d.position.set(x * r * 0.85, y * r * 0.85, 0);
    d.castShadow = false;
    g.add(d);
  }
  return g;
}

/** Rosto de gatinho laranja listrado, virado para +z, de raio [r] (a estampa do peito da fantasia). */
function gatinhoLaranja(r) {
  const g = new THREE.Group();
  const laranja = fosco('#ffae4d');
  const listra = fosco('#e8742a');
  const creme = fosco('#fff3ea');
  const escuro = brilho('#3d2c5e');
  const ey = 0.88, ez = 0.46;
  const zE = (x, y) => r * ez * Math.sqrt(Math.max(0.02, 1 - (x / r) ** 2 - (y / (r * ey)) ** 2));
  for (const lado of [-1, 1]) {
    const o = orelha(r * 0.62, r * 0.62, laranja, matRosaOrelha(), r * 0.16);
    o.position.set(lado * r * 0.55, r * 0.5, -r * 0.08);
    o.rotation.z = -lado * 0.5;
    g.add(o);
  }
  const cab = bola(r, laranja, 40);
  cab.scale.set(1, ey, ez);
  g.add(cab);
  // listrinhas da testa
  for (const [k, comp] of [[-1, 0.2], [0, 0.26], [1, 0.2]]) {
    const x = k * r * 0.27, y = r * 0.6;
    const l = capsula(r * 0.045, r * comp, listra);
    l.rotation.z = -k * 0.18;
    em(l, x, y, zE(x, y) - r * 0.015);
    l.castShadow = false;
    g.add(l);
  }
  // listrinhas das bochechas
  for (const lado of [-1, 1]) for (const dy of [0.0, -0.17]) {
    const x = lado * r * 0.84, y = -r * 0.04 + dy * r;
    const l = capsula(r * 0.04, r * 0.17, listra);
    l.rotation.z = lado * (Math.PI / 2 + 0.15 * (dy ? -1 : 1));
    em(l, x, y, zE(x, y) - r * 0.01);
    l.castShadow = false;
    g.add(l);
  }
  // bochechas rosadas, focinho, nariz e olhos
  for (const lado of [-1, 1]) {
    const x = lado * r * 0.58, y = -r * 0.26;
    const b = bola(r * 0.15, plastico('#ff8fb0', { rugosidade: 0.6, verniz: 0.1 }), 16);
    b.scale.set(1.2, 0.8, 0.3);
    em(b, x, y, zE(x, y));
    b.castShadow = false;
    g.add(b);
    const ox = lado * r * 0.37, oy = r * 0.1;
    const o = bola(r * 0.12, escuro, 20);
    o.scale.set(0.85, 1.15, 0.45);
    em(o, ox, oy, zE(ox, oy) + r * 0.005);
    o.castShadow = false;
    g.add(o);
    const br = bola(r * 0.04, new THREE.MeshBasicMaterial({ color: '#ffffff', toneMapped: false }), 10);
    br.castShadow = false;
    em(br, ox - lado * r * 0.03, oy + r * 0.07, zE(ox, oy) + r * 0.06);
    g.add(br);
  }
  const focinho = bola(r * 0.26, creme, 24);
  focinho.scale.set(1.15, 0.8, 0.4);
  em(focinho, 0, -r * 0.3, zE(0, -r * 0.3));
  focinho.castShadow = false;
  g.add(focinho);
  const nariz = bola(r * 0.075, plastico('#ff5f98', { rugosidade: 0.3, verniz: 0.8 }), 16);
  nariz.scale.set(1.2, 0.85, 0.6);
  em(nariz, 0, -r * 0.2, zE(0, -r * 0.2) + r * 0.05);
  nariz.castShadow = false;
  g.add(nariz);
  return g;
}

// ---------------------------------------------------------------------------------------------
// Traje: fantasia de gatinha

/** Rabinho listrado: corrente de bolas que afina (cor alternando em faixas), saindo de trás e subindo pela direita. */
function rabinhoDeGato() {
  const g = new THREE.Group();
  const laranja = fosco('#ffae4d');
  const listra = fosco('#e8742a');
  const pontos = [[0.1, 0.84, -0.46], [0.55, 0.74, -0.5], [0.93, 0.86, -0.44], [1.15, 1.16, -0.36], [1.12, 1.5, -0.32], [0.92, 1.72, -0.34]];
  const curva = new THREE.CatmullRomCurve3(pontos.map((p) => new THREE.Vector3(...p)));
  const n = 44;
  for (let i = 0; i <= n; i++) {
    const t = i / n;
    const p = curva.getPoint(t);
    const r = 0.135 - t * 0.04;
    const faixaEscura = Math.floor(i / 3) % 2 === 1 && t < 0.86;
    const b = bola(r, t > 0.9 ? fosco('#fff3ea') : faixaEscura ? listra : laranja, 28);
    em(b, p.x, p.y, p.z);
    g.add(b);
  }
  return g;
}

function trajeGatinha() {
  const rosa = tecido('#ff9ec4');
  const claro = fosco('#fff3ea');
  const perfil = [[0, 0.56], [0.56, 0.58], [0.68, 0.74], [0.7, 1.0], [0.62, 1.32], [0.47, 1.58], [0.3, 1.72], [0, 1.74]];
  const barriga = torno(perfil, rosa);
  const corpo = grupo(barriga, mangasCurtas(rosa), calca(rosa, { ate: 0.26, raio: 0.165, cintura: 0.7 }));
  // barra de pelúcia clara na boca do macacão
  corpo.add(anel(0.575, 0.05, 0.6, claro));

  const estampa = new THREE.Group();
  // gatinho laranja listrado no peito
  estampa.add(sobre(barriga, 0, 1.16, gatinhoLaranja(0.2), 0.012, 0.6));
  // coleirinha rosa-choque com sininho dourado
  const rc = raioNoPerfil(perfil, 1.54) + 0.014;
  estampa.add(anel(rc, 0.04, 1.54, brilho('#e63977'), { escalaZ: 1.0 }));
  const sino = new THREE.Group();
  sino.add(bola(0.075, plastico('#ffc21a', { rugosidade: 0.18, verniz: 1 }), 24));
  const fenda = capsula(0.008, 0.07, brilho('#8a5a00'));
  fenda.rotation.z = Math.PI / 2;
  em(fenda, 0, -0.01, 0.07);
  sino.add(fenda);
  em(sino, 0, 1.47, rc + 0.05);
  estampa.add(sino);
  // patinhas brancas na barriga e na barra
  for (const [a, y, r] of [[-0.62, 0.78, 0.075], [0.55, 0.88, 0.07], [-0.3, 0.68, 0.062], [0.95, 0.72, 0.06], [-1.0, 0.95, 0.065]]) {
    estampa.add(sobre(barriga, a, y, patinha(r, brilho('#ffffff')), 0.01, 0.2));
  }

  const tras = rabinhoDeGato();
  // patinhas: pantufas de pelúcia clara com dedinhos rosa
  const pes = new THREE.Group();
  for (const lado of [-1, 1]) {
    pes.add(sapato(lado, claro, { esc: [0.225, 0.145, 0.3], pos: [0.21, 0.14, 0.07] }));
    for (const k of [-1, 0, 1]) {
      const x = lado * 0.21 + k * 0.085;
      const d = bola(0.034, brilho('#ff6fae'), 14);
      d.scale.set(1, 0.9, 0.7);
      em(d, x, 0.2 - (k === 0 ? 0 : 0.018), 0.37 - Math.abs(k) * 0.025);
      pes.add(d);
    }
    const almofada = bola(0.06, brilho('#ff6fae'), 16);
    almofada.scale.set(1.2, 0.8, 0.7);
    em(almofada, lado * 0.21, 0.19, 0.27);
    pes.add(almofada);
  }
  return { corpo, estampa, tras, pes, tingir: false };
}

// ---------------------------------------------------------------------------------------------
// Traje: vestido de lacinhos

function trajeLacos() {
  const rosaClaro = tecido('#ffb5d0');
  const branco = fosco('#ffffff');
  const perfil = [[0, 0.5], [0.7, 0.52], [0.82, 0.6], [0.82, 0.72], [0.67, 1.02], [0.48, 1.38], [0.43, 1.6], [0.3, 1.72], [0, 1.74]];
  const saia = torno(perfil, rosaClaro);
  const corpo = grupo(saia, mangasCurtas(rosaClaro, M.manga.r * 1.1));
  // barra de arquinhos brancos
  for (let i = 0; i < 26; i++) {
    const a = (i / 26) * Math.PI * 2;
    corpo.add(em(bola(0.08, branco, 16), Math.sin(a) * 0.8, 0.54, Math.cos(a) * 0.8));
  }
  // golinha redonda branca
  corpo.add(anel(0.45, 0.05, 1.56, branco, { escalaZ: 1.0 }));

  const estampa = new THREE.Group();
  const rosa = brilho('#ff5fa2');
  const escuro = brilho('#e63977');
  const claro = brilho('#ffffff');
  // faixa da cintura com um laço
  estampa.add(casca([saia], 0.008, faixa('#ff8fb8', 1.2, 1.3), { mat: { roughness: 0.3, clearcoat: 0.9 } }));
  estampa.add(sobre(saia, 0, 1.25, lacinho(0.17, escuro), 0.04, 0.5));
  // lacinhos espalhados pela saia
  const lugares = [[-0.7, 1.02, 0.14, rosa], [0.6, 1.05, 0.14, rosa], [-0.05, 0.88, 0.15, escuro], [0.95, 0.82, 0.13, claro],
    [-1.05, 0.82, 0.13, claro], [0.35, 0.72, 0.13, rosa], [-0.4, 0.72, 0.13, rosa], [1.55, 0.78, 0.12, escuro], [-1.5, 0.78, 0.12, escuro],
    [0.75, 1.3, 0.1, claro], [-0.8, 1.3, 0.1, claro]];
  for (const [a, y, r, mat] of lugares) estampa.add(sobre(saia, a, y, lacinho(r * 1.25, mat), 0.03, 0.25));
  // lacinho no pescoço e nas mangas
  estampa.add(sobre(saia, 0, 1.52, lacinho(0.12, rosa), 0.05, 0.6));
  for (const lado of [-1, 1]) {
    const l = lacinho(0.1, escuro);
    l.rotation.z = lado * 0.2;
    em(l, lado * 0.66, 1.58, 0.2);
    estampa.add(l);
  }
  return { corpo, estampa, tingir: false };
}

// ---------------------------------------------------------------------------------------------
// Acessórios: cabelo

/** Orelhinhas de gato em arco (tiara) rosa. */
function orelhasDeGato() {
  const g = new THREE.Group();
  const rosa = brilho('#ff8fb8');
  const arco = malha(new THREE.TorusGeometry(R * 1.08, R * 0.05, 12, 96, Math.PI * 0.95), rosa);
  arco.geometry.rotateZ(Math.PI / 2 - Math.PI * 0.475);
  arco.rotation.x = -0.12;
  g.add(arco);
  for (const lado of [-1, 1]) {
    const a = 0.66;
    const p = new THREE.Vector3(lado * Math.sin(a) * R * 1.09, Math.cos(a) * R * 1.09, 0).applyAxisAngle(EIXO_X, -0.12);
    const o = orelha(R * 0.5, R * 0.56, matPelo(), matRosaOrelha(), R * 0.13);
    o.position.copy(p).add(new THREE.Vector3(0, -R * 0.06, R * 0.02));
    o.rotation.set(-0.12, 0, -lado * 0.5);
    g.add(o);
  }
  // lacinho na base da orelha da direita
  const l = lacinho(R * 0.2, brilho('#ff5fa2'));
  em(l, R * 0.2, R * 1.04, R * 0.2);
  l.rotation.set(-0.5, 0, 0.1);
  g.add(l);
  return naCabeca(g);
}

/** Boné rosa com orelhinhas e pala curtinha (os olhos aparecem). */
function boneDeGato() {
  const g = new THREE.Group();
  const rosa = tecido('#ff8fb8');
  const claro = brilho('#ffd1e3');
  const copa = new THREE.Group();
  copa.add(malha(new THREE.SphereGeometry(R * 1.1, 64, 32, 0, Math.PI * 2, 0, Math.PI * 0.5), rosa));
  const pala = new THREE.Shape();
  pala.absellipse(0, 0, R * 0.78, R * 0.66, 0, Math.PI, false, 0);
  const geo = new THREE.ExtrudeGeometry(pala, { depth: R * 0.04, bevelEnabled: true, bevelThickness: R * 0.02, bevelSize: R * 0.02, bevelSegments: 3, curveSegments: 32 });
  geo.rotateX(Math.PI / 2);
  const p = malha(geo, brilho('#ff6fae'));
  p.position.set(0, R * 0.05, R * 0.74);
  p.rotation.x = -0.2;
  copa.add(p);
  // barrinha clara na borda
  const borda = malha(new THREE.TorusGeometry(R * 1.1, R * 0.035, 10, 72), claro);
  borda.rotation.x = Math.PI / 2;
  copa.add(borda);
  copa.add(em(bola(R * 0.1, claro, 16), 0, R * 1.1, 0));
  // orelhinhas
  for (const lado of [-1, 1]) {
    const a = 0.72;
    const o = orelha(R * 0.5, R * 0.52, rosa, matPelo(), R * 0.13);
    em(o, lado * Math.sin(a) * R * 1.02, Math.cos(a) * R * 1.02 - R * 0.04, 0);
    o.rotation.z = -lado * (a * 0.8);
    copa.add(o);
  }
  // patinha branca na frente
  const pat = patinha(R * 0.19, brilho('#ffffff'));
  naSuperficie(pat, R * 1.1, 0, R * 0.5, R * 0.005);
  copa.add(pat);
  copa.rotation.x = -0.42;
  copa.position.y = R * 0.16;
  g.add(copa);
  return naCabeca(g);
}

/** Laço enorme de lado, rosa de bolinhas brancas. */
function lacoEnorme() {
  const g = new THREE.Group();
  const rosa = brilho('#ff5fa2');
  const escuro = brilho('#e63977');
  const prof = R * 0.16, bisel = R * 0.06;
  const s = R * 0.8;
  // asa: forma de "laçarote", com a ponta de dentro afinada no nó
  const asa = new THREE.Shape();
  asa.moveTo(0, 0.05 * s);
  asa.bezierCurveTo(0.22 * s, 0.46 * s, 0.82 * s, 0.7 * s, 1.0 * s, 0.34 * s);
  asa.bezierCurveTo(1.08 * s, 0.1 * s, 1.08 * s, -0.1 * s, 1.0 * s, -0.34 * s);
  asa.bezierCurveTo(0.82 * s, -0.7 * s, 0.22 * s, -0.46 * s, 0, -0.05 * s);
  const bolinhas = [[0.4, 0.2], [0.62, 0.0], [0.4, -0.2], [0.8, 0.22], [0.8, -0.22], [0.22, 0.0]];
  for (const lado of [-1, 1]) {
    const w = new THREE.Group();
    w.add(extr(asa, prof, rosa, bisel));
    for (const [x, y] of bolinhas) {
      const d = bola(s * 0.06, brilho('#ffffff'), 14);
      d.scale.set(1, 1, 0.35);
      em(d, x * s, y * s, prof / 2 + bisel * 0.95);
      d.castShadow = false;
      w.add(d);
    }
    // dobrinha escura perto do nó
    const dobra = bola(s * 0.2, escuro, 20);
    dobra.scale.set(1, 1.4, 0.35);
    em(dobra, s * 0.16, 0, prof / 2 + bisel * 0.55);
    dobra.castShadow = false;
    w.add(dobra);
    w.scale.x = lado;
    w.rotation.z = lado * 0.16;
    g.add(w);
    // pontas da fita
    const ponta = new THREE.Shape();
    ponta.moveTo(-0.13 * s, 0);
    ponta.lineTo(0.13 * s, 0);
    ponta.lineTo(0.2 * s, -0.62 * s);
    ponta.lineTo(0, -0.5 * s);
    ponta.lineTo(-0.2 * s, -0.62 * s);
    ponta.closePath();
    const f = extr(ponta, prof * 0.5, rosa, bisel * 0.6);
    f.position.set(lado * s * 0.12, -s * 0.06, -prof * 0.1);
    f.rotation.z = lado * 0.32;
    g.add(f);
  }
  const no = bola(s * 0.2, escuro, 24);
  no.scale.set(1, 1.15, 0.85);
  em(no, 0, 0, prof * 0.2);
  g.add(no);
  g.position.set(-R * 0.56, R * 0.9, R * 0.3);
  g.rotation.set(-0.22, -0.28, 0.32);
  return naCabeca(g);
}

// ---------------------------------------------------------------------------------------------
// Acessórios: rosto

/** Focinho rosa e três bigodes de cada lado (sem cobrir os olhos). */
function bigodesDeGato() {
  const g = new THREE.Group();
  const fio = plastico('#4a2f5e', { rugosidade: 0.4, verniz: 0.5 });
  const ponto = (x, y, folga) => {
    const z = Math.sqrt(Math.max(0, R * R - x * x - y * y));
    return [x, y, Math.max(z + folga, R * 0.16)];
  };
  for (const lado of [-1, 1]) {
    for (const k of [-1, 0, 1]) {
      const ys = -R * 0.27 + k * R * 0.055, ye = -R * 0.27 + k * R * 0.19 - R * 0.01;
      const pts = [
        ponto(lado * R * 0.44, ys, R * 0.035),
        ponto(lado * R * 0.68, (ys * 2 + ye) / 3 + R * 0.01, R * 0.04),
        ponto(lado * R * 0.92, (ys + ye * 2) / 3 + R * 0.012, R * 0.04),
        ponto(lado * R * 1.2, ye, R * 0.04),
      ];
      g.add(tubo(pts, R * 0.02, fio, 40));
      g.add(em(bola(R * 0.02, fio, 10), ...pts[3]));
    }
    // pintinhas de onde nascem os bigodes
    for (const [dx, dy] of [[0.5, -0.2], [0.55, -0.26], [0.5, -0.32]]) {
      const p = bola(R * 0.016, fio, 8);
      em(p, ...ponto(lado * R * dx, R * dy, R * 0.0));
      p.castShadow = false;
      g.add(p);
    }
  }
  // narizinho rosa em coração arredondado
  const w = R * 0.085, h = R * 0.07;
  const s = new THREE.Shape();
  s.moveTo(-w, h * 0.45);
  s.quadraticCurveTo(0, h * 0.95, w, h * 0.45);
  s.quadraticCurveTo(w * 0.85, -h * 0.15, 0, -h);
  s.quadraticCurveTo(-w * 0.85, -h * 0.15, -w, h * 0.45);
  const nariz = extr(s, R * 0.025, plastico('#ff6fa8', { rugosidade: 0.25, verniz: 0.9 }), R * 0.02);
  naSuperficie(nariz, R, 0, -R * 0.17, R * 0.004);
  g.add(nariz);
  return naCabeca(g);
}

/** Máscara de gatinha: faixa rosa colada nos olhos (furos em amêndoa) e orelhinhas. */
function mascaraDeGata() {
  const g = new THREE.Group();
  const raio = R * 1.035;
  const phi0 = 0.42, dPhi = Math.PI - 0.84, th0 = 1.05, dTh = 1.05;
  const cv = document.createElement('canvas');
  cv.width = 1024; cv.height = 512;
  const ctx = cv.getContext('2d');
  const img = ctx.createImageData(cv.width, cv.height);
  const tilt = 0.32, ct = Math.cos(tilt), st = Math.sin(tilt);
  for (let j = 0; j < cv.height; j++) {
    const th = th0 + (j / cv.height) * dTh;
    for (let i = 0; i < cv.width; i++) {
      const ph = phi0 + (i / cv.width) * dPhi;
      const x = -Math.cos(ph) * Math.sin(th), y = Math.cos(th);
      const ax = Math.abs(x);
      // contorno: elipse "quadrada" com as pontas de fora para cima (olho de gato)
      const yc = 0.02 + 0.07 * ax;
      let dentro = (ax / 0.9) ** 3 + (Math.abs(y - yc) / 0.4) ** 3 < 1;
      // furos em amêndoa, inclinados para cima nas pontas de fora
      const ex = ax - OLHO_X, ey = y - (OLHO_Y - 0.005);
      const u = ex * ct + ey * st, v = -ex * st + ey * ct;
      if ((u / 0.27) ** 2 + (v / 0.2) ** 2 < 1) dentro = false;
      const k = (j * cv.width + i) * 4;
      img.data[k] = 0xff; img.data[k + 1] = 0x7a; img.data[k + 2] = 0xae; img.data[k + 3] = dentro ? 255 : 0;
    }
  }
  ctx.putImageData(img, 0, 0);
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  const mat = new THREE.MeshPhysicalMaterial({ map: tex, alphaTest: 0.5, roughness: 0.4, clearcoat: 0.6, clearcoatRoughness: 0.2, envMapIntensity: 0.6, side: THREE.DoubleSide });
  const m = new THREE.Mesh(new THREE.SphereGeometry(raio, 96, 48, phi0, dPhi, th0, dTh), mat);
  m.castShadow = true;
  g.add(m);
  const m2 = new THREE.Mesh(new THREE.SphereGeometry(raio - R * 0.02, 96, 48, phi0, dPhi, th0, dTh), mat.clone());
  m2.material.color = new THREE.Color('#ffb3d1');
  g.add(m2);
  // orelhinhas nos cantos de cima
  for (const lado of [-1, 1]) {
    const o = orelha(R * 0.5, R * 0.56, brilho('#ff7aae'), matPelo(), R * 0.12);
    const n = new THREE.Vector3(lado * 0.66, 0.5, 0).setZ(Math.sqrt(1 - 0.66 ** 2 - 0.25)).normalize();
    o.position.copy(n.multiplyScalar(R * 1.0));
    o.rotation.set(-0.5, lado * 0.1, -lado * 0.62);
    g.add(o);
  }
  return naCabeca(g);
}

export const TRAJES_GATINHA = {
  gatinha: { montar: trajeGatinha },
  lacos: { montar: trajeLacos },
};

export const ACESSORIOS_GATINHA = {
  orelhasgato: { parte: 'CABELO', montar: orelhasDeGato },
  bonegato: { parte: 'CABELO', montar: boneDeGato },
  lacogrande: { parte: 'CABELO', montar: lacoEnorme },
  bigodes: { parte: 'ROSTO', montar: bigodesDeGato },
  // ordem 71 (acima do cabelo da frente, 70): as orelhinhas da máscara não somem atrás do cabelo
  mascaragata: { parte: 'ROSTO', montar: mascaraDeGata, ordem: 71 },
};
