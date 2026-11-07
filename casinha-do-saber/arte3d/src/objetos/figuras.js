// Figuras das perguntas em 3D (PRD visual 3D, seção 5 e Fase 4): os emojis mais usados nas fases 1 a 100
// viram brinquedos de plástico com verniz. Cada modelo fica de frente (ou três quartos), com a base em y = 0;
// a câmera ('objeto' com sombra de contato, ou 'item' para o que voa ou fica pendurado) recorta em volta.
// A lista (id, emojis, lugar) fica em src/figuras.js.
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em, naSuperficie } from '../kit.js';
import { patinho } from './patinho.js';
import { estrela3D } from './estrela3D.js';
import { pecasDosItens, modelosDosItens } from './itensDaCasa.js';

const { segmento, olho, sorriso, bochecha } = pecasDosItens;
export { modelosDosItens };

/** Plástico de brinquedo com verniz (o padrão das figuras). */
const pl = (cor, o = {}) => plastico(cor, { rugosidade: 0.4, verniz: 0.55, rugosidadeVerniz: 0.2, ...o });
const brilho = (cor, o = {}) => plastico(cor, { rugosidade: 0.22, verniz: 1, rugosidadeVerniz: 0.1, ...o });
const preto = () => plastico('#1d1210', { rugosidade: 0.12, verniz: 1 });
const branco = () => plastico('#ffffff', { rugosidade: 0.3, verniz: 0.6 });

/** Perfil (raio, altura) suavizado, girado em volta do eixo y. */
function torneado(pontos, mat, seg = 48, amostras = 48) {
  const curva = new THREE.SplineCurve(pontos.map(([r, y]) => new THREE.Vector2(r, y)));
  const pts = curva.getPoints(amostras).map((p) => new THREE.Vector2(Math.max(0, p.x), p.y));
  return malha(new THREE.LatheGeometry(pts, seg), mat);
}

/** Tubo ao longo de [curva] com raio variável raio(t), de t0 a t1, com as pontas fechadas por bolinhas. */
function tubo(curva, raio, mat, { t0 = 0, t1 = 1, seg = 48, radial = 20, pontas = true } = {}) {
  const frames = curva.computeFrenetFrames(seg, false);
  const pos = [], idx = [];
  for (let i = 0; i <= seg; i++) {
    const t = t0 + ((t1 - t0) * i) / seg;
    const p = curva.getPointAt(t);
    const fi = Math.round((i / seg) * seg);
    const N = frames.normals[fi], B = frames.binormals[fi];
    const r = raio(t);
    for (let j = 0; j <= radial; j++) {
      const a = (j / radial) * Math.PI * 2;
      pos.push(p.x + r * (Math.cos(a) * N.x + Math.sin(a) * B.x), p.y + r * (Math.cos(a) * N.y + Math.sin(a) * B.y), p.z + r * (Math.cos(a) * N.z + Math.sin(a) * B.z));
    }
  }
  for (let i = 0; i < seg; i++) for (let j = 0; j < radial; j++) {
    const a = i * (radial + 1) + j, b = a + radial + 1;
    idx.push(a, b, a + 1, b, b + 1, a + 1);
  }
  const geo = new THREE.BufferGeometry();
  geo.setAttribute('position', new THREE.Float32BufferAttribute(pos, 3));
  geo.setIndex(idx);
  geo.computeVertexNormals();
  const g = new THREE.Group();
  g.add(malha(geo, mat));
  if (pontas) for (const t of [t0, t1]) { const r = raio(t); if (r > 0.005) g.add(em(bola(r, mat, 20), ...curva.getPointAt(t).toArray())); }
  return g;
}

/** Forma 2D estufada (extrusão com bisel grande): roupas, coração. */
function estufado(forma, mat, prof = 0.12, bisel = 0.1) {
  const geo = new THREE.ExtrudeGeometry(forma, { depth: prof, bevelEnabled: true, bevelThickness: bisel, bevelSize: bisel * 0.9, bevelSegments: 8, curveSegments: 32 });
  geo.translate(0, 0, -prof / 2);
  return malha(geo, mat);
}

/** Rostinho de bicho na frente de uma cabeça de raio R (olhos, bochechas, sorriso). */
function carinha(cab, R, { y = 0.04, abertura = 0.36, boca = true, bochechas = true, yBoca = -0.2 } = {}) {
  for (const l of [-1, 1]) {
    const o = olho(R * 0.16);
    naSuperficie(o, R, l * R * abertura, R * y, -R * 0.04);
    cab.add(o);
    if (bochechas) { const b = bochecha(R * 0.17); naSuperficie(b, R, l * R * 0.62, R * (y - 0.3), -R * 0.03); cab.add(b); }
  }
  if (boca) { const s = sorriso(R * 0.14, R * 0.03, 2.2); naSuperficie(s, R, 0, R * yBoca, -R * 0.01); cab.add(s); }
}

// ---------------------------------------------------------------------------------------------
// Bichinhos sentados (gato, cachorro, vaca...): corpo, barriga, patinhas, cabeça grande, focinho

/**
 * Bichinho de brinquedo sentado, de frente e um pouco virado. [orelhas](cab, m, mc) põe as orelhas e o que
 * for próprio da cabeça; [extras](g, m, mc) o que for do corpo (rabo, manchas). [focinho]: 'curto', 'longo'
 * (cavalo, vaca), 'nenhum' (elefante).
 */
function bichinho({ cor, claro = '#fff1df', orelhas, extras, focinho = 'curto', nariz = '#3a2420', corFocinho, giro = 0.3, R = 0.46 }) {
  const g = new THREE.Group();
  const m = pl(cor), mc = pl(claro);
  const mf = corFocinho ? pl(corFocinho) : mc;
  const corpo = bola(0.48, m, 40); corpo.scale.set(1, 1.0, 0.9); g.add(em(corpo, 0, 0.5, 0));
  const barriga = bola(0.3, mc, 28); barriga.scale.set(1, 1.15, 0.45); g.add(em(barriga, 0, 0.48, 0.3));
  for (const l of [-1, 1]) {
    // patinhas da frente e pés
    g.add(em(capsula(0.11, 0.22, m), l * 0.19, 0.3, 0.32));
    const pe = bola(0.15, mc, 20); pe.scale.set(1, 0.6, 1.25); g.add(em(pe, l * 0.31, 0.08, 0.3));
    const coxa = bola(0.22, m, 24); coxa.scale.set(0.9, 0.8, 1.1); g.add(em(coxa, l * 0.33, 0.2, 0.08));
  }
  const cab = new THREE.Group();
  cab.add(bola(R, m, 48));
  if (focinho === 'curto') {
    const f = bola(R * 0.42, mf, 28); f.scale.set(1.25, 0.85, 0.75); naSuperficie(f, R, 0, -R * 0.26, -R * 0.2); cab.add(f);
    const n = bola(R * 0.13, plastico(nariz, { rugosidade: 0.2, verniz: 1 }), 16); n.scale.set(1.3, 0.9, 0.9); naSuperficie(n, R, 0, -R * 0.12, R * 0.08); cab.add(n);
    const s = sorriso(R * 0.13, R * 0.028, 2.2, '#5a2a1a'); naSuperficie(s, R, 0, -R * 0.36, R * 0.07); cab.add(s);
    carinha(cab, R, { y: 0.16, abertura: 0.38, boca: false });
  } else if (focinho === 'longo') {
    const f = bola(R * 0.58, mf, 32); f.scale.set(1.2, 0.8, 0.85); naSuperficie(f, R, 0, -R * 0.38, -R * 0.12); cab.add(f);
    for (const l of [-1, 1]) { const n = bola(R * 0.07, plastico('#5a2a2a', { rugosidade: 0.3, verniz: 0.8 }), 12); naSuperficie(n, R * 1.18, l * R * 0.22, -R * 0.33, 0); cab.add(n); }
    const s = sorriso(R * 0.16, R * 0.028, 2.0, '#5a2a1a'); naSuperficie(s, R * 1.16, 0, -R * 0.55, 0); cab.add(s);
    carinha(cab, R, { y: 0.22, abertura: 0.42, boca: false });
  } else {
    carinha(cab, R, { y: 0.12, abertura: 0.38, boca: false });
  }
  if (orelhas) orelhas(cab, m, mc, R);
  g.add(em(cab, 0, 1.2, 0.04));
  if (extras) extras(g, m, mc, cab);
  g.rotation.y = giro;
  return g;
}

const orelhasDeGato = (corDentro = '#ffb3c9') => (cab, m, mc, R) => {
  for (const l of [-1, 1]) {
    const o = malha(new THREE.ConeGeometry(R * 0.32, R * 0.6, 16), m); o.rotation.z = -l * 0.38; cab.add(em(o, l * R * 0.5, R * 0.78, -R * 0.05));
    const oi = malha(new THREE.ConeGeometry(R * 0.17, R * 0.36, 12), pl(corDentro)); oi.rotation.z = -l * 0.38; cab.add(em(oi, l * R * 0.5, R * 0.74, R * 0.1));
  }
};

const bigodes = (cab, R) => {
  for (const l of [-1, 1]) for (const k of [-1, 1]) {
    const b = cilindro(0.008, 0.008, R * 0.55, plastico('#ffffff'), 6);
    b.rotation.z = Math.PI / 2 + k * 0.15 * l;
    cab.add(em(b, l * R * 0.62, -R * 0.22 + k * R * 0.07, R * 0.72));
  }
};

const rabo = (pontos, r, m) => tubo(new THREE.CatmullRomCurve3(pontos.map((p) => new THREE.Vector3(...p))), (t) => r * (1 - t * 0.3), m, { seg: 24, radial: 12 });

const gato = () => bichinho({
  cor: '#ffa94d', claro: '#fff1df',
  orelhas: (cab, m, mc, R) => { orelhasDeGato()(cab, m, mc, R); bigodes(cab, R); for (const x of [-0.12, 0, 0.12]) { const lis = capsula(0.025, 0.08, pl('#e8782a')); lis.rotation.z = x * 2; naSuperficie(lis, R, x * R * 2, R * 0.62, 0); cab.add(lis); } },
  extras: (g, m) => g.add(rabo([[-0.3, 0.15, -0.3], [-0.65, 0.35, -0.2], [-0.7, 0.75, -0.1], [-0.55, 0.95, 0]], 0.08, m)),
});

const tigre = () => bichinho({
  cor: '#ff9a2e', claro: '#fff4e4',
  orelhas: (cab, m, mc, R) => {
    orelhasDeGato('#ffe0c4')(cab, m, mc, R); bigodes(cab, R);
    const listra = pl('#3a2420', { rugosidade: 0.4 });
    for (const [x, y, rz] of [[0, 0.68, 0], [-0.22, 0.6, 0.35], [0.22, 0.6, -0.35]]) { const l = capsula(0.035, 0.12, listra); l.rotation.z = rz; naSuperficie(l, R, x * R * 2, y * R, 0); cab.add(l); }
    for (const l of [-1, 1]) for (const y of [0.1, -0.08]) { const s = capsula(0.03, 0.1, listra); s.rotation.z = Math.PI / 2 + l * 0.2; naSuperficie(s, R, l * R * 0.85, y * R, -0.005); cab.add(s); }
  },
  extras: (g, m) => {
    const listra = pl('#3a2420', { rugosidade: 0.4 });
    for (const l of [-1, 1]) for (const y of [0.4, 0.62]) { const s = capsula(0.035, 0.16, listra); s.rotation.z = Math.PI / 2 + l * 0.3; g.add(em(s, l * 0.43, y, 0.12)); }
    g.add(rabo([[-0.3, 0.15, -0.3], [-0.65, 0.35, -0.2], [-0.72, 0.7, -0.1]], 0.08, m));
  },
});

const cachorro = () => bichinho({
  cor: '#e9c38f', claro: '#fff4e0', nariz: '#2a1a14',
  orelhas: (cab, m, mc, R) => {
    const esc = pl('#a8673a');
    for (const l of [-1, 1]) { const o = bola(R * 0.42, esc, 24); o.scale.set(0.55, 1.15, 0.35); o.rotation.z = l * 0.3; cab.add(em(o, l * R * 0.92, -R * 0.08, 0)); }
    const mancha = bola(R * 0.26, esc, 16); mancha.scale.set(1.3, 1, 0.4); naSuperficie(mancha, R, R * 0.38, R * 0.5, -R * 0.03); cab.add(mancha);
    const lingua = bola(R * 0.12, pl('#ff7f8c'), 12); lingua.scale.set(1, 1.3, 0.5); naSuperficie(lingua, R, R * 0.02, -R * 0.52, R * 0.02); cab.add(lingua);
  },
  extras: (g, m) => {
    const col = malha(new THREE.TorusGeometry(0.3, 0.05, 10, 32), brilho('#ef476f')); col.rotation.x = Math.PI / 2 - 0.25; g.add(em(col, 0, 0.88, 0.02));
    g.add(em(bola(0.07, brilho('#ffc93c'), 12), 0, 0.78, 0.33));
    g.add(rabo([[-0.35, 0.25, -0.3], [-0.6, 0.5, -0.3], [-0.68, 0.75, -0.25]], 0.07, m));
  },
});

const coelho = () => bichinho({
  cor: '#f4f1f6', claro: '#ffffff', nariz: '#ff7fa0', corFocinho: '#ffffff',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = capsula(R * 0.2, R * 0.9, m); o.scale.set(1, 1, 0.6); o.rotation.z = -l * 0.15; cab.add(em(o, l * R * 0.32, R * 1.25, -R * 0.1));
      const oi = capsula(R * 0.1, R * 0.7, pl('#ffb3c9')); oi.scale.set(1, 1, 0.5); oi.rotation.z = -l * 0.15; cab.add(em(oi, l * R * 0.33, R * 1.25, R * 0.0));
    }
    bigodes(cab, R);
  },
  extras: (g) => g.add(em(bola(0.17, pl('#ffffff', { rugosidade: 0.8, verniz: 0.1 }), 20), -0.1, 0.25, -0.45)),
});

const rato = () => bichinho({
  cor: '#b9bfd0', claro: '#eef0f6', nariz: '#ff7fa0',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = cilindro(R * 0.45, R * 0.45, R * 0.12, m, 32); o.rotation.x = Math.PI / 2; o.rotation.z = l * 0.2; cab.add(em(o, l * R * 0.72, R * 0.66, -R * 0.15));
      const oi = cilindro(R * 0.3, R * 0.3, R * 0.06, pl('#ffb3c9'), 32); oi.rotation.x = Math.PI / 2; cab.add(em(oi, l * R * 0.72, R * 0.66, -R * 0.07));
    }
    bigodes(cab, R);
  },
  extras: (g) => g.add(rabo([[-0.3, 0.1, -0.35], [-0.7, 0.1, -0.2], [-0.85, 0.4, 0], [-0.7, 0.6, 0.1]], 0.035, pl('#ffb3c9'))),
});

const porco = () => bichinho({
  cor: '#ffadc6', claro: '#ffd3e0', focinho: 'nenhum',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) { const o = malha(new THREE.ConeGeometry(R * 0.26, R * 0.42, 3), m); o.rotation.set(0.5, 0, -l * 0.6); cab.add(em(o, l * R * 0.58, R * 0.72, R * 0.1)); }
    const cil = cilindro(R * 0.34, R * 0.36, R * 0.24, pl('#ff8fb0'), 32); cil.rotation.x = Math.PI / 2;
    const f = new THREE.Group(); f.add(cil); naSuperficie(f, R, 0, -R * 0.2, -R * 0.02); cab.add(f);
    for (const l of [-1, 1]) { const n = bola(R * 0.07, plastico('#c2496e', { rugosidade: 0.3 }), 12); n.scale.set(0.8, 1.2, 0.5); naSuperficie(n, R * 1.13, l * R * 0.13, -R * 0.21, 0); cab.add(n); }
    const s = sorriso(R * 0.16, R * 0.03, 2.0, '#9a2a4a'); naSuperficie(s, R, 0, -R * 0.6, 0); cab.add(s);
  },
  extras: (g, m) => g.add(tubo(new THREE.CatmullRomCurve3([[-0.4, 0.4, -0.3], [-0.6, 0.45, -0.3], [-0.6, 0.6, -0.2], [-0.5, 0.55, -0.2], [-0.55, 0.45, -0.25]].map((p) => new THREE.Vector3(...p))), () => 0.035, m, { seg: 24, radial: 10 })),
});

const vaca = () => bichinho({
  cor: '#ffffff', claro: '#ffffff', focinho: 'longo', corFocinho: '#ffb3c9',
  orelhas: (cab, m, mc, R) => {
    const malhada = pl('#2b2230');
    for (const l of [-1, 1]) {
      const o = bola(R * 0.3, m, 20); o.scale.set(1.3, 0.55, 0.45); o.rotation.z = l * -0.3; cab.add(em(o, l * R * 1.05, R * 0.32, 0));
      const c = malha(new THREE.ConeGeometry(R * 0.11, R * 0.38, 16), pl('#f5e6c8')); c.rotation.z = -l * 0.5; cab.add(em(c, l * R * 0.48, R * 0.95, 0));
    }
    const m1 = bola(R * 0.3, malhada, 20); m1.scale.set(1.2, 1, 0.3); naSuperficie(m1, R, -R * 0.45, R * 0.45, -R * 0.06); cab.add(m1);
  },
  extras: (g) => {
    const malhada = pl('#2b2230');
    for (const [x, y, z, s] of [[0.36, 0.62, 0.28, 0.18], [-0.38, 0.4, 0.25, 0.14], [-0.2, 0.75, -0.35, 0.2]]) { const mm = bola(s, malhada, 20); mm.scale.set(1.2, 1, 0.35); mm.lookAt(x * 3, y, z * 3); g.add(em(mm, x, y, z)); }
    const sino = cilindro(0.06, 0.1, 0.12, brilho('#ffc21a'), 16); g.add(em(sino, 0, 0.82, 0.42));
  },
});

const cavalo = () => bichinho({
  cor: '#b5713f', claro: '#f2d2b0', focinho: 'longo', corFocinho: '#e8bfa0',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) { const o = malha(new THREE.ConeGeometry(R * 0.18, R * 0.45, 12), m); o.rotation.z = -l * 0.25; cab.add(em(o, l * R * 0.42, R * 0.9, -R * 0.1)); }
    const crina = pl('#5a3420', { rugosidade: 0.5 });
    for (let k = 0; k < 6; k++) { const a = 0.15 + k * 0.32; const b = bola(R * 0.2, crina, 16); cab.add(em(b, 0, Math.cos(a) * R * 1.0, -Math.sin(a) * R * 0.95 + R * 0.1)); }
    const t = bola(R * 0.22, crina, 16); t.scale.set(1.3, 0.9, 1); cab.add(em(t, R * 0.12, R * 0.82, R * 0.35));
    const mancha = bola(R * 0.16, pl('#ffffff'), 16); mancha.scale.set(0.7, 1.3, 0.3); naSuperficie(mancha, R, 0, R * 0.45, -R * 0.02); cab.add(mancha);
  },
  extras: (g) => g.add(rabo([[-0.35, 0.5, -0.35], [-0.65, 0.45, -0.3], [-0.75, 0.2, -0.2]], 0.1, pl('#5a3420', { rugosidade: 0.5 }))),
});

const leao = () => bichinho({
  cor: '#ffc04d', claro: '#fff1d2',
  orelhas: (cab, m, mc, R) => {
    const juba = pl('#d9792b', { rugosidade: 0.5 });
    for (let k = 0; k < 14; k++) { const a = (k / 14) * Math.PI * 2; cab.add(em(bola(R * 0.33, juba, 20), Math.cos(a) * R * 0.95, Math.sin(a) * R * 0.95, -R * 0.25)); }
    for (const l of [-1, 1]) { const o = bola(R * 0.2, m, 16); o.scale.set(1, 1, 0.6); cab.add(em(o, l * R * 0.6, R * 0.68, R * 0.1)); }
    bigodes(cab, R);
  },
  extras: (g, m) => {
    g.add(rabo([[-0.35, 0.2, -0.35], [-0.7, 0.35, -0.25], [-0.75, 0.65, -0.1]], 0.05, m));
    g.add(em(bola(0.1, pl('#d9792b'), 16), -0.75, 0.72, -0.08));
  },
});

const macaco = () => bichinho({
  cor: '#8a5a3a', claro: '#f1cfa6', corFocinho: '#f1cfa6',
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = bola(R * 0.32, m, 20); o.scale.set(1, 1, 0.5); cab.add(em(o, l * R * 1.0, R * 0.05, 0));
      const oi = bola(R * 0.2, mc, 16); oi.scale.set(1, 1, 0.4); cab.add(em(oi, l * R * 1.02, R * 0.05, R * 0.08));
    }
    // máscara clara do rosto (coração)
    for (const l of [-1, 1]) { const mm = bola(R * 0.42, mc, 24); mm.scale.set(1, 1.05, 0.4); naSuperficie(mm, R, l * R * 0.28, R * 0.1, -R * 0.2); cab.add(mm); }
    const t = bola(R * 0.12, m, 12); cab.add(em(t, 0, R * 1.0, R * 0.1));
  },
  extras: (g, m) => g.add(rabo([[-0.35, 0.2, -0.35], [-0.7, 0.3, -0.3], [-0.8, 0.7, -0.2], [-0.6, 0.9, -0.15], [-0.5, 0.75, -0.15]], 0.05, m)),
});

const ovelha = () => {
  // corpo de bolinhas de lã, rosto escuro
  const g = new THREE.Group();
  const la = pl('#ffffff', { rugosidade: 0.75, verniz: 0.15 });
  const rosto = pl('#4a3a44');
  for (const [x, y, z, r] of [[0, 0.55, 0, 0.42], [-0.35, 0.5, 0.05, 0.3], [0.35, 0.5, 0.05, 0.3], [0, 0.85, -0.05, 0.3], [-0.25, 0.8, 0.1, 0.25], [0.25, 0.8, 0.1, 0.25], [0, 0.35, 0.25, 0.3], [-0.3, 0.3, -0.1, 0.25], [0.3, 0.3, -0.1, 0.25]]) g.add(em(bola(r, la, 24), x, y, z));
  for (const l of [-1, 1]) {
    g.add(em(capsula(0.08, 0.2, rosto), l * 0.2, 0.14, 0.18));
    g.add(em(capsula(0.08, 0.2, rosto), l * 0.22, 0.14, -0.2));
  }
  const R = 0.34;
  const cab = new THREE.Group();
  const c = bola(R, rosto, 40); c.scale.set(0.95, 1.1, 0.95); cab.add(c);
  carinha(cab, R, { y: 0.05, abertura: 0.38, bochechas: true, yBoca: -0.38 });
  for (const l of [-1, 1]) { const o = bola(R * 0.35, rosto, 16); o.scale.set(1.4, 0.5, 0.5); o.rotation.z = l * -0.3; cab.add(em(o, l * R * 1.1, R * 0.2, 0)); }
  for (const [x, y, r] of [[0, 0.95, 0.38], [-0.32, 0.8, 0.3], [0.32, 0.8, 0.3]]) cab.add(em(bola(R * r, la, 16), x * R, y * R, 0.1 * R));
  g.add(em(cab, 0, 1.1, 0.25));
  g.rotation.y = 0.3;
  return g;
};

const elefante = () => bichinho({
  cor: '#9fb4d6', claro: '#c9d6ec', focinho: 'nenhum', R: 0.5,
  orelhas: (cab, m, mc, R) => {
    for (const l of [-1, 1]) {
      const o = bola(R * 0.65, m, 28); o.scale.set(0.9, 1.05, 0.22); o.rotation.y = l * 0.4; cab.add(em(o, l * R * 1.05, R * 0.05, -R * 0.15));
      const oi = bola(R * 0.48, pl('#ffc0cf'), 24); oi.scale.set(0.9, 1.05, 0.15); oi.rotation.y = l * 0.4; cab.add(em(oi, l * R * 1.02, R * 0.05, -R * 0.07));
    }
    // tromba curvada para cima
    const pts = [[0, -R * 0.2, R * 0.8], [0, -R * 0.65, R * 1.0], [0, -R * 0.9, R * 1.2], [0, -R * 0.75, R * 1.45]].map((p) => new THREE.Vector3(...p));
    cab.add(tubo(new THREE.CatmullRomCurve3(pts), (t) => R * (0.26 - t * 0.1), m, { seg: 30, radial: 18 }));
    const s = sorriso(R * 0.12, R * 0.025, 2.0, '#5a2a3a'); naSuperficie(s, R, R * 0.28, -R * 0.38, 0); cab.add(s);
  },
  extras: (g, m) => g.add(rabo([[-0.35, 0.3, -0.35], [-0.6, 0.3, -0.3], [-0.7, 0.15, -0.25]], 0.04, m)),
});

// ---------------------------------------------------------------------------------------------
// Bichos da água, aves e insetos

function peixe() {
  const g = new THREE.Group();
  const m = pl('#ff8a2a'), mc = pl('#ffffff');
  const corpo = bola(0.6, m, 48); corpo.scale.set(1.25, 0.95, 0.62); g.add(em(corpo, 0, 0.62, 0));
  for (const x of [-0.18, 0.18]) { const f = bola(0.6, mc, 40); f.scale.set(0.09, 0.9, 0.6); g.add(em(f, x, 0.62, 0)); }
  const cauda = new THREE.Shape(); cauda.moveTo(0, 0); cauda.quadraticCurveTo(-0.5, 0.45, -0.55, 0.5); cauda.quadraticCurveTo(-0.4, 0, -0.55, -0.5); cauda.quadraticCurveTo(-0.5, -0.45, 0, 0);
  g.add(em(estufado(cauda, m, 0.05, 0.06), -0.62, 0.62, 0));
  const dorsal = bola(0.3, m, 24); dorsal.scale.set(1.2, 0.7, 0.18); g.add(em(dorsal, -0.08, 1.12, 0));
  const lateral = bola(0.18, m, 20); lateral.scale.set(1.2, 0.6, 0.3); lateral.rotation.z = -0.5; g.add(em(lateral, 0.05, 0.45, 0.36));
  const o = olho(0.1); naSuperficie(o, 0.6, 0, 0, 0); o.position.set(0.42, 0.75, 0.3); o.rotation.y = 0.5; g.add(o);
  const b = bochecha(0.08); b.position.set(0.48, 0.55, 0.28); b.rotation.y = 0.6; g.add(b);
  const boca = malha(new THREE.TorusGeometry(0.06, 0.03, 10, 20), pl('#ff5a7a')); boca.rotation.y = Math.PI / 2; g.add(em(boca, 0.75, 0.6, 0.05));
  g.rotation.y = -0.45;
  return g;
}

function sapo() {
  const g = new THREE.Group();
  const m = pl('#4cc94a'), mc = pl('#c9f2a0');
  const corpo = bola(0.6, m, 48); corpo.scale.set(1.15, 0.78, 1); g.add(em(corpo, 0, 0.5, 0));
  const barriga = bola(0.4, mc, 32); barriga.scale.set(1.1, 0.8, 0.5); g.add(em(barriga, 0, 0.4, 0.38));
  for (const l of [-1, 1]) {
    const ov = bola(0.24, m, 32); g.add(em(ov, l * 0.3, 0.98, 0.12));
    const br = bola(0.17, branco(), 24); br.scale.set(1, 1, 0.6); g.add(em(br, l * 0.3, 1.0, 0.3));
    g.add(em(olho(0.1), l * 0.3, 1.0, 0.38));
    const coxa = bola(0.3, m, 24); coxa.scale.set(0.8, 0.6, 1.2); g.add(em(coxa, l * 0.6, 0.25, -0.05));
    const pe = bola(0.16, m, 16); pe.scale.set(1.3, 0.4, 1.2); g.add(em(pe, l * 0.55, 0.06, 0.4));
    g.add(em(bochecha(0.12), l * 0.45, 0.55, 0.45));
  }
  const s = sorriso(0.3, 0.03, 2.4, '#2a6a2a'); g.add(em(s, 0, 0.72, 0.52));
  g.rotation.y = 0.25;
  return g;
}

function tartaruga() {
  const g = new THREE.Group();
  const casco = pl('#2fae5f'), pele = pl('#a6e07a');
  const c = malha(new THREE.SphereGeometry(0.75, 48, 24, 0, Math.PI * 2, 0, Math.PI / 2), casco); c.scale.set(1, 0.78, 1); g.add(em(c, 0, 0.18, 0));
  const aba = malha(new THREE.TorusGeometry(0.74, 0.08, 12, 48), pl('#ffd23f')); aba.rotation.x = Math.PI / 2; g.add(em(aba, 0, 0.18, 0));
  // placas do casco
  const placa = pl('#5cd17f');
  const R = 0.75;
  for (const [x, y] of [[0, 0.68], [-0.38, 0.4], [0.38, 0.4], [0, 0.25], [-0.62, 0.12], [0.62, 0.12]]) {
    const p = cilindro(0.16, 0.16, 0.04, placa, 6); p.rotation.x = Math.PI / 2;
    const z = Math.sqrt(Math.max(0.01, R * R - x * x - y * y));
    const n = new THREE.Vector3(x, y * 0.78, z).normalize();
    p.position.set(n.x * R, n.y * R * 0.78 + 0.18, n.z * R);
    p.lookAt(p.position.clone().add(n));
    p.rotateX(Math.PI / 2);
    g.add(p);
  }
  const R2 = 0.3;
  const cab = new THREE.Group(); cab.add(bola(R2, pele, 32));
  carinha(cab, R2, { y: 0.1, abertura: 0.38, yBoca: -0.3 });
  g.add(em(cab, 0, 0.42, 0.85));
  for (const [x, z] of [[-0.5, 0.45], [0.5, 0.45], [-0.5, -0.45], [0.5, -0.45]]) { const p = bola(0.16, pele, 16); p.scale.set(1, 0.7, 1.2); g.add(em(p, x, 0.1, z)); }
  g.rotation.y = 0.35;
  return g;
}

function pintinho() {
  const g = new THREE.Group();
  const m = new THREE.MeshPhysicalMaterial({ color: '#ffc800', roughness: 0.3, clearcoat: 0.8, clearcoatRoughness: 0.2, envMapIntensity: 0.3 });
  const corpo = bola(0.55, m, 48); corpo.scale.set(1, 0.95, 0.95); g.add(em(corpo, 0, 0.55, 0));
  const cab = bola(0.4, m, 40); g.add(em(cab, 0, 1.12, 0.05));
  for (const [x, rz] of [[-0.08, 0.4], [0.05, -0.1], [0.15, -0.5]]) { const p = capsula(0.05, 0.16, m); p.rotation.z = rz; g.add(em(p, x, 1.55, 0)); }
  const laranja = pl('#ff8c1a');
  const bico = malha(new THREE.ConeGeometry(0.1, 0.2, 16), laranja); bico.rotation.x = Math.PI / 2; g.add(em(bico, 0, 1.06, 0.5));
  for (const l of [-1, 1]) {
    g.add(em(olho(0.07), l * 0.15, 1.18, 0.36));
    g.add(em(bochecha(0.08), l * 0.26, 1.02, 0.33));
    const asa = bola(0.24, m, 20); asa.scale.set(0.5, 0.9, 0.7); asa.rotation.z = l * 0.4; g.add(em(asa, l * 0.52, 0.6, 0));
    const pe = bola(0.12, laranja, 16); pe.scale.set(1, 0.4, 1.4); g.add(em(pe, l * 0.2, 0.04, 0.25));
  }
  g.rotation.y = 0.2;
  return g;
}

/** Ave de quintal (galo e galinha). */
function ave({ cor, crista = 3, cauda = ['#2fae5f', '#3d8bff', '#ff8a1c'], barbela = true }) {
  const g = new THREE.Group();
  const m = pl(cor);
  const corpo = bola(0.5, m, 40); corpo.scale.set(1.1, 0.95, 0.9); g.add(em(corpo, 0, 0.62, 0));
  cauda.forEach((c, i) => {
    const t = malha(new THREE.TorusGeometry(0.32 - i * 0.02, 0.07, 12, 24, Math.PI * 0.9), pl(c));
    t.rotation.set(0, Math.PI / 2, 0); t.rotation.z = 0.2 + i * 0.35;
    g.add(em(t, -0.45, 0.9, -0.1 + (i - 1) * 0.12));
  });
  const cab = new THREE.Group();
  const R = 0.32;
  cab.add(bola(R, m, 32));
  carinha(cab, R, { y: 0.12, abertura: 0.4, boca: false });
  const vermelho = pl('#ff3b3b');
  for (let k = 0; k < crista; k++) cab.add(em(bola(0.1, vermelho, 16), (k - (crista - 1) / 2) * 0.12, R + 0.04 - Math.abs(k - (crista - 1) / 2) * 0.04, 0));
  const bico = malha(new THREE.ConeGeometry(0.08, 0.2, 16), pl('#ffc21a')); bico.rotation.x = Math.PI / 2; cab.add(em(bico, 0, -0.03, R + 0.06));
  if (barbela) { const b = bola(0.07, vermelho, 12); b.scale.set(1, 1.5, 0.8); cab.add(em(b, 0, -0.2, R - 0.02)); }
  g.add(em(cab, 0.12, 1.2, 0.12));
  for (const l of [-1, 1]) {
    const asa = bola(0.26, m, 20); asa.scale.set(1.1, 0.7, 0.35); asa.rotation.z = -0.3; g.add(em(asa, -0.05, 0.65, l * 0.42));
    g.add(em(cilindro(0.04, 0.04, 0.22, pl('#ffb000'), 10), l * 0.15, 0.13, 0.05));
    const pe = bola(0.1, pl('#ffb000'), 12); pe.scale.set(1.2, 0.4, 1.4); g.add(em(pe, l * 0.15, 0.03, 0.12));
  }
  g.rotation.y = 0.45;
  return g;
}
const galo = () => ave({ cor: '#ffffff', crista: 3 });
const galinha = () => ave({ cor: '#d98a4a', crista: 2, cauda: ['#a8592a', '#c9773a'], barbela: true });

function passarinho() {
  const g = new THREE.Group();
  const m = pl('#3d9bff'), mc = pl('#fff4d6');
  const corpo = bola(0.48, m, 40); corpo.scale.set(1, 0.95, 0.95); g.add(em(corpo, 0, 0.55, 0));
  const barriga = bola(0.32, mc, 28); barriga.scale.set(1, 1.1, 0.5); g.add(em(barriga, 0, 0.5, 0.28));
  const R = 0.36;
  const cab = new THREE.Group(); cab.add(bola(R, m, 32));
  carinha(cab, R, { y: 0.12, abertura: 0.38, boca: false });
  const bico = malha(new THREE.ConeGeometry(0.08, 0.2, 16), pl('#ff9a1a')); bico.rotation.x = Math.PI / 2; cab.add(em(bico, 0, -0.05, R + 0.06));
  const top = capsula(0.05, 0.16, m); top.rotation.z = 0.4; cab.add(em(top, -0.05, R + 0.06, 0));
  g.add(em(cab, 0, 1.08, 0.05));
  for (const l of [-1, 1]) {
    const asa = bola(0.28, pl('#2a7ae0'), 20); asa.scale.set(0.35, 0.8, 1.0); asa.rotation.x = 0.3; g.add(em(asa, l * 0.45, 0.6, -0.05));
    g.add(em(cilindro(0.03, 0.03, 0.18, pl('#ff9a1a'), 8), l * 0.14, 0.1, 0.05));
  }
  const cauda = bola(0.25, m, 20); cauda.scale.set(0.6, 0.25, 1.2); cauda.rotation.x = -0.6; g.add(em(cauda, 0, 0.4, -0.5));
  g.rotation.y = 0.3;
  return g;
}

function joaninha() {
  const g = new THREE.Group();
  const verm = brilho('#ff2d3a'), pt = preto();
  const casco = malha(new THREE.SphereGeometry(0.6, 48, 24, 0, Math.PI * 2, 0, Math.PI / 2), verm); casco.scale.set(1, 0.85, 1.15); g.add(em(casco, 0, 0.1, 0));
  // risca do meio das asas: meio anel por cima do casco
  const risca = malha(new THREE.TorusGeometry(0.6, 0.022, 8, 48, Math.PI), pt); risca.rotation.y = Math.PI / 2;
  const meio = new THREE.Group(); meio.add(risca); meio.scale.set(1, 0.86, 1.16); g.add(em(meio, 0, 0.1, 0));
  for (const [x, z] of [[0.3, 0.3], [-0.3, 0.3], [0.35, -0.25], [-0.35, -0.25], [0.18, -0.6], [-0.18, -0.6]]) {
    const R = 0.6;
    const y = Math.sqrt(Math.max(0.01, 1 - (x / R) ** 2 - (z / (R * 1.15)) ** 2)) * R * 0.85;
    const p = cilindro(0.11, 0.11, 0.04, pt, 24);
    const n = new THREE.Vector3(x / R, y / (R * 0.85), z / (R * 1.15)).normalize();
    p.position.set(x, y + 0.1, z);
    p.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), n);
    g.add(p);
  }
  const cab = new THREE.Group();
  const R = 0.3;
  const c = bola(R, pt, 32); cab.add(c);
  for (const l of [-1, 1]) {
    const br = bola(R * 0.26, branco(), 16); br.scale.set(1, 1.1, 0.5); naSuperficie(br, R, l * R * 0.38, R * 0.1, -R * 0.08); cab.add(br);
    const o = bola(R * 0.13, pt, 12); naSuperficie(o, R, l * R * 0.38, R * 0.08, R * 0.04); cab.add(o);
    cab.add(segmento([l * 0.08, R * 0.8, 0], [l * 0.25, R * 1.6, 0.05], 0.018, pt));
    cab.add(em(bola(0.05, pt, 12), l * 0.25, R * 1.6, 0.05));
  }
  const s = sorriso(R * 0.18, R * 0.04, 2.2, '#ffffff'); naSuperficie(s, R, 0, -R * 0.35, R * 0.02); cab.add(s);
  g.add(em(cab, 0, 0.3, 0.7));
  for (const l of [-1, 1]) for (const z of [-0.3, 0, 0.3]) g.add(em(capsula(0.03, 0.12, pt).rotateZ(l * 1.2), l * 0.62, 0.08, z));
  g.rotation.y = 0.4;
  g.rotation.x = 0.15;
  return g;
}

function abelha() {
  const g = new THREE.Group();
  const am = pl('#ffc81a'), pt = pl('#2a2030');
  const corpo = capsula(0.42, 0.55, am); corpo.rotation.z = Math.PI / 2; g.add(em(corpo, 0, 0.6, 0));
  for (const x of [-0.12, 0.2]) { const f = cilindro(0.43, 0.43, 0.14, pt, 40); f.rotation.z = Math.PI / 2; g.add(em(f, x, 0.6, 0)); }
  const ferrao = malha(new THREE.ConeGeometry(0.08, 0.22, 12), pt); ferrao.rotation.z = Math.PI / 2; g.add(em(ferrao, -0.78, 0.58, 0));
  const asaM = plastico('#e8f6ff', { rugosidade: 0.1, verniz: 1, opacidade: 0.75, emissivo: '#cfeaff' });
  for (const l of [-1, 1]) { const a = bola(0.32, asaM, 24); a.scale.set(0.7, 1, 0.15); a.rotation.set(l * 0.4, 0, 0.3); g.add(em(a, -0.05, 1.1, l * 0.22)); }
  const R = 0.36;
  const cab = new THREE.Group(); cab.add(bola(R, am, 32));
  carinha(cab, R, { y: 0.08, abertura: 0.38, yBoca: -0.3 });
  for (const l of [-1, 1]) { cab.add(segmento([l * 0.1, R * 0.85, 0], [l * 0.22, R * 1.5, 0.08], 0.018, pt)); cab.add(em(bola(0.05, pt, 12), l * 0.22, R * 1.5, 0.08)); }
  cab.rotation.y = Math.PI / 2 - 0.25;
  g.add(em(cab, 0.62, 0.68, 0));
  g.rotation.y = -0.9;
  return g;
}

function foca() {
  const g = new THREE.Group();
  const m = pl('#8aa3c4'), mc = pl('#c9d8ec');
  const corpo = torneado([[0, 0], [0.42, 0.06], [0.5, 0.35], [0.42, 0.75], [0.3, 1.0], [0, 1.08]], m);
  g.add(corpo);
  const barriga = bola(0.3, mc, 28); barriga.scale.set(1, 1.3, 0.5); g.add(em(barriga, 0, 0.4, 0.3));
  const R = 0.36;
  const cab = new THREE.Group(); cab.add(bola(R, m, 32));
  carinha(cab, R, { y: 0.18, abertura: 0.36, boca: false });
  for (const l of [-1, 1]) { const f = bola(R * 0.3, mc, 16); f.scale.set(1, 0.8, 0.8); naSuperficie(f, R, l * R * 0.16, -R * 0.22, -R * 0.1); cab.add(f); }
  const n = bola(R * 0.14, preto(), 12); naSuperficie(n, R, 0, -R * 0.08, R * 0.06); cab.add(n);
  bigodes(cab, R * 0.9);
  g.add(em(cab, 0, 1.22, 0.05));
  for (const l of [-1, 1]) { const n2 = bola(0.2, m, 16); n2.scale.set(0.5, 1, 0.3); n2.rotation.z = l * 0.6; g.add(em(n2, l * 0.45, 0.45, 0.15)); }
  for (const l of [-1, 1]) { const c = bola(0.18, m, 16); c.scale.set(1.2, 0.3, 0.8); c.rotation.y = l * 0.6; g.add(em(c, l * 0.25, 0.05, -0.4)); }
  // bola listrada no nariz
  const b = new THREE.Group();
  ['#ef476f', '#ffffff', '#3d8bff', '#ffffff', '#ffc93c', '#ffffff'].forEach((c, i) => b.add(malha(new THREE.SphereGeometry(0.22, 32, 16, (i * Math.PI) / 3, Math.PI / 3), brilho(c))));
  b.rotation.z = 0.4;
  g.add(em(b, 0, 1.76, 0.18));
  g.rotation.y = 0.25;
  return g;
}

function pinguim() {
  const g = new THREE.Group();
  const m = pl('#2b3550'), mc = pl('#ffffff');
  g.add(torneado([[0, 0], [0.48, 0.05], [0.56, 0.45], [0.48, 0.95], [0.4, 1.25], [0.2, 1.42], [0, 1.45]], m));
  const barriga = torneado([[0, 0.05], [0.36, 0.1], [0.42, 0.45], [0.34, 0.85], [0.2, 1.0], [0, 1.02]], mc, 32);
  barriga.scale.set(1, 1, 0.5); g.add(em(barriga, 0, 0, 0.3));
  const rosto = bola(0.3, mc, 28); rosto.scale.set(1.1, 0.8, 0.5); g.add(em(rosto, 0, 1.08, 0.3));
  for (const l of [-1, 1]) {
    g.add(em(olho(0.07), l * 0.13, 1.12, 0.43));
    g.add(em(bochecha(0.07), l * 0.24, 0.98, 0.4));
    const asa = bola(0.3, m, 20); asa.scale.set(0.25, 0.9, 0.6); asa.rotation.z = l * 0.25; g.add(em(asa, l * 0.55, 0.62, 0));
    const pe = bola(0.13, pl('#ff9a1a'), 16); pe.scale.set(1.1, 0.35, 1.5); g.add(em(pe, l * 0.2, 0.04, 0.3));
  }
  const bico = malha(new THREE.ConeGeometry(0.09, 0.2, 16), pl('#ff9a1a')); bico.rotation.x = Math.PI / 2; g.add(em(bico, 0, 1.0, 0.55));
  g.rotation.y = 0.25;
  return g;
}

const pato = () => { const g = new THREE.Group(); const p = patinho(1, '#ffc400'); p.rotation.y = -0.6; g.add(p); return g; };

// ---------------------------------------------------------------------------------------------
// Comidas

function maca() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0.1], [0.28, 0.0], [0.55, 0.12], [0.66, 0.4], [0.64, 0.66], [0.5, 0.88], [0.3, 0.96], [0.12, 0.88], [0, 0.82]], brilho('#ee2436'), 64));
  g.add(em(cilindro(0.035, 0.05, 0.3, massinha('#7a4a2a'), 10), 0.02, 0.98, 0).rotateZ(-0.2));
  const folha = bola(0.2, pl('#3fbf4a'), 20); folha.scale.set(1.2, 0.2, 0.55); folha.rotation.set(0, 0.4, 0.4); g.add(em(folha, 0.22, 1.05, 0.02));
  return g;
}

function banana() {
  const g = new THREE.Group();
  const am = pl('#ffd21a', { rugosidade: 0.35 });
  const ponta = massinha('#6b4a2a');
  for (const [dz, rot] of [[-0.12, -0.12], [0.12, 0.12]]) {
    const pts = [[-0.75, 0.75], [-0.55, 0.3], [-0.1, 0.08], [0.4, 0.15], [0.8, 0.5]].map(([x, y]) => new THREE.Vector3(x, y, 0));
    const c = new THREE.CatmullRomCurve3(pts);
    const b = tubo(c, (t) => 0.06 + 0.17 * Math.sin(Math.min(1, t * 1.1) * Math.PI) ** 0.7, am, { seg: 48, radial: 6 });
    b.rotation.y = rot;
    g.add(em(b, 0, 0.1, dz));
    const p = c.getPointAt(1);
    const pp = bola(0.06, ponta, 10); g.add(em(pp, p.x, p.y + 0.1, p.z + dz));
  }
  g.add(em(cilindro(0.06, 0.08, 0.2, pl('#9ac43a'), 10), -0.78, 0.92, 0).rotateZ(0.5));
  g.rotation.set(0, -0.2, -0.1);
  return g;
}

function morango() {
  const g = new THREE.Group();
  const perfil = [[0, 0], [0.18, 0.08], [0.38, 0.3], [0.5, 0.58], [0.47, 0.8], [0.3, 0.92], [0, 0.95]];
  g.add(torneado(perfil, brilho('#ff2d4a'), 48));
  const curva = new THREE.SplineCurve(perfil.map(([r, y]) => new THREE.Vector2(r, y)));
  const sem = pl('#ffe066', { rugosidade: 0.3 });
  for (let i = 0; i < 6; i++) for (let k = 0; k < 9; k++) {
    const p = curva.getPoint(0.15 + i * 0.12);
    const a = (k / 9) * Math.PI * 2 + i * 0.35;
    const s = bola(0.028, sem, 8); s.scale.set(0.7, 1.2, 0.5);
    s.position.set(Math.cos(a) * (p.x + 0.005), p.y, Math.sin(a) * (p.x + 0.005));
    s.lookAt(Math.cos(a) * 3, p.y, Math.sin(a) * 3);
    g.add(s);
  }
  const verde = pl('#3fbf4a');
  for (let k = 0; k < 6; k++) { const a = (k / 6) * Math.PI * 2; const f = bola(0.16, verde, 16); f.scale.set(1.3, 0.25, 0.55); f.rotation.y = -a; f.rotation.z = -0.3; g.add(em(f, Math.cos(a) * 0.2, 0.93, Math.sin(a) * 0.2)); }
  g.add(em(cilindro(0.03, 0.04, 0.2, verde, 8), 0, 1.03, 0));
  g.rotation.set(0.15, 0, -0.18);
  return g;
}

function uva() {
  const g = new THREE.Group();
  const m = brilho('#8e44d8');
  const camadas = [[4, 1.15], [4, 0.93], [3, 0.72], [3, 0.52], [2, 0.33], [1, 0.15]];
  camadas.forEach(([n, y], i) => {
    for (let k = 0; k < n; k++) {
      const x = (k - (n - 1) / 2) * 0.3;
      g.add(em(bola(0.17, m, 24), x, y, (i % 2) * 0.05 + (k % 2) * 0.08));
    }
  });
  for (const [x, y] of [[-0.15, 1.0], [0.15, 0.8], [0, 0.6]]) g.add(em(bola(0.16, m, 20), x, y, -0.18));
  g.add(segmento([0, 1.25, 0], [0.08, 1.55, 0], 0.04, massinha('#7a5a2a')));
  const folha = bola(0.3, pl('#3fbf4a'), 20); folha.scale.set(1.1, 0.18, 0.7); folha.rotation.set(0.3, 0, 0.3); g.add(em(folha, 0.3, 1.45, 0));
  g.rotation.y = 0.2;
  return g;
}

function cereja() {
  const g = new THREE.Group();
  const m = brilho('#e8192c');
  const cab = massinha('#5a8a2a');
  for (const [x, z] of [[-0.32, 0.05], [0.3, -0.05]]) {
    const c = bola(0.32, m, 40); c.scale.set(1, 0.92, 1); g.add(em(c, x, 0.32, z));
    g.add(tubo(new THREE.CatmullRomCurve3([[x, 0.6, z], [x * 0.7, 0.95, z], [0.02, 1.35, 0]].map((p) => new THREE.Vector3(...p))), () => 0.03, cab, { seg: 20, radial: 8 }));
  }
  const folha = bola(0.25, pl('#3fbf4a'), 20); folha.scale.set(1.2, 0.18, 0.6); folha.rotation.set(0, 0.3, -0.4); g.add(em(folha, 0.25, 1.38, 0));
  return g;
}

function cenoura() {
  const g = new THREE.Group();
  const lar = pl('#ff8a1a', { rugosidade: 0.45 });
  const c = torneado([[0, 0], [0.08, 0.15], [0.18, 0.55], [0.26, 1.0], [0.27, 1.15], [0.18, 1.25], [0, 1.27]], lar, 40);
  g.add(c);
  for (const y of [0.35, 0.6, 0.85]) {
    const r = 0.08 + (y / 1.15) * 0.17;
    const anel = malha(new THREE.TorusGeometry(r, 0.012, 6, 32, Math.PI * 1.2), pl('#e86f0a'));
    anel.rotation.x = Math.PI / 2; anel.rotation.z = y * 4;
    g.add(em(anel, 0, y, 0));
  }
  const verde = pl('#3fbf4a');
  for (const [rz, rx, h] of [[0.4, 0.1, 0.55], [0, -0.1, 0.65], [-0.4, 0.1, 0.55], [0.15, 0.4, 0.45]]) {
    const f = capsula(0.06, h, verde); f.rotation.set(rx, 0, rz);
    g.add(em(f, Math.sin(-rz) * h * 0.5, 1.25 + h * 0.45, Math.sin(rx) * h * 0.5));
  }
  const w = new THREE.Group(); w.add(g); g.rotation.z = -0.6; g.position.y = 0.3;
  return w;
}

function pao() {
  const g = new THREE.Group();
  const m = pl('#e3a04f', { rugosidade: 0.5, verniz: 0.35 });
  const corpo = capsula(0.45, 0.9, m); corpo.rotation.z = Math.PI / 2; corpo.scale.set(0.8, 1, 0.9); g.add(em(corpo, 0, 0.38, 0));
  const claro = pl('#f6d39a', { rugosidade: 0.5 });
  for (const x of [-0.42, 0, 0.42]) { const c = capsula(0.06, 0.4, claro); c.rotation.set(Math.PI / 2, 0, 0.7); c.scale.set(1, 1, 0.6); g.add(em(c, x, 0.73, 0.02)); }
  g.rotation.y = -0.35;
  return g;
}

function ovo() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.32, 0.06], [0.44, 0.35], [0.38, 0.75], [0.2, 0.98], [0, 1.03]], pl('#fff6e6', { rugosidade: 0.3, verniz: 0.7 }), 56));
  g.rotation.z = 0.12;
  return g;
}

function bolinho() {
  const g = new THREE.Group();
  const geo = new THREE.CylinderGeometry(0.48, 0.36, 0.55, 48, 1, false);
  const pos = geo.attributes.position;
  for (let i = 0; i < pos.count; i++) {
    const a = Math.atan2(pos.getZ(i), pos.getX(i));
    const k = 1 + 0.05 * Math.cos(a * 16);
    pos.setX(i, pos.getX(i) * k); pos.setZ(i, pos.getZ(i) * k);
  }
  geo.computeVertexNormals();
  g.add(em(malha(geo, brilho('#4dc3ff')), 0, 0.275, 0));
  const cob = pl('#ff9ccf', { rugosidade: 0.35, verniz: 0.6 });
  for (const [r, t, y] of [[0.42, 0.17, 0.62], [0.32, 0.15, 0.85], [0.2, 0.13, 1.04]]) { const anel = malha(new THREE.TorusGeometry(r, t, 16, 40), cob); anel.rotation.x = Math.PI / 2; g.add(em(anel, 0, y, 0)); }
  const base = bola(0.42, cob, 32); base.scale.set(1, 0.45, 1); g.add(em(base, 0, 0.64, 0));
  g.add(em(bola(0.13, brilho('#e8192c'), 20), 0, 1.22, 0));
  g.add(segmento([0, 1.3, 0], [0.08, 1.45, 0], 0.015, massinha('#5a8a2a')));
  const cores = ['#ffffff', '#ffd23f', '#3d8bff', '#36c26a', '#9a62ff'];
  for (let k = 0; k < 16; k++) {
    const a = k * 2.4, y = 0.7 + (k % 3) * 0.14, r = 0.47 - (k % 3) * 0.1;
    const s = capsula(0.018, 0.06, pl(cores[k % 5])); s.rotation.set(a, a * 0.5, 0);
    g.add(em(s, Math.cos(a) * r, y + 0.07, Math.sin(a) * r));
  }
  return g;
}

// ---------------------------------------------------------------------------------------------
// Brinquedos

function balao() {
  const g = new THREE.Group();
  const b = torneado([[0, 0], [0.12, 0.04], [0.42, 0.3], [0.58, 0.7], [0.56, 1.0], [0.38, 1.25], [0, 1.32]], brilho('#ff3b4f', { rugosidade: 0.12 }), 56);
  g.add(em(b, 0, 0.95, 0));
  g.add(em(malha(new THREE.ConeGeometry(0.08, 0.12, 16), brilho('#e2283c')), 0, 0.92, 0).rotateX(Math.PI));
  g.add(tubo(new THREE.CatmullRomCurve3([[0, 0.88, 0], [0.08, 0.6, 0], [-0.06, 0.3, 0], [0.05, 0, 0]].map((p) => new THREE.Vector3(...p))), () => 0.012, plastico('#ffffff', { rugosidade: 0.5 }), { seg: 24, radial: 6 }));
  g.rotation.z = -0.1;
  return g;
}

function carrinho() {
  const g = new THREE.Group();
  const verm = brilho('#ff3b4f');
  const vidro = plastico('#a9e1ff', { rugosidade: 0.05, verniz: 1, emissivo: '#cfeaff', ambiente: 0.6 });
  g.add(em(caixa(1.7, 0.5, 0.95, 0.22, verm), 0, 0.5, 0));
  g.add(em(caixa(0.95, 0.5, 0.85, 0.22, verm), -0.12, 0.92, 0));
  for (const l of [-1, 1]) {
    g.add(em(caixa(0.36, 0.3, 0.04, 0.06, vidro), -0.3, 0.93, l * 0.43));
    g.add(em(caixa(0.3, 0.3, 0.04, 0.06, vidro), 0.12, 0.93, l * 0.43));
  }
  g.add(em(caixa(0.04, 0.3, 0.65, 0.06, vidro), 0.35, 0.93, 0));
  for (const x of [-0.55, 0.55]) for (const z of [-0.46, 0.46]) {
    const r = cilindro(0.26, 0.26, 0.18, plastico('#2b2b3a', { rugosidade: 0.5, verniz: 0.3 }), 32); r.rotation.x = Math.PI / 2; g.add(em(r, x, 0.27, z));
    const c = cilindro(0.12, 0.12, 0.2, brilho('#ffd23f'), 20); c.rotation.x = Math.PI / 2; g.add(em(c, x, 0.27, z));
  }
  for (const z of [-0.3, 0.3]) g.add(em(bola(0.09, plastico('#fff3b0', { emissivo: '#ffe066', rugosidade: 0.1 }), 16), 0.85, 0.55, z));
  g.add(em(caixa(0.1, 0.12, 0.9, 0.05, plastico('#d6dbe4', { rugosidade: 0.2, verniz: 1 })), 0.88, 0.35, 0));
  g.rotation.y = -0.6;
  return g;
}

function aviao() {
  const g = new THREE.Group();
  const am = brilho('#ffc21a'), verm = brilho('#ff3b4f'), azul = brilho('#3d8bff');
  const corpo = capsula(0.3, 1.3, am); corpo.rotation.z = Math.PI / 2; g.add(em(corpo, 0, 0.6, 0));
  g.add(em(caixa(0.6, 0.08, 2.2, 0.04, verm), 0.15, 0.55, 0));
  g.add(em(caixa(0.32, 0.06, 0.9, 0.03, verm), -0.82, 0.68, 0));
  const leme = caixa(0.32, 0.45, 0.06, 0.03, azul); g.add(em(leme, -0.85, 0.92, 0));
  g.add(em(bola(0.13, plastico('#ffffff', { rugosidade: 0.2 }), 16), 1.0, 0.6, 0));
  for (const a of [0, Math.PI / 2]) { const h = caixa(0.03, 0.75, 0.1, 0.03, plastico('#3a2a4a')); h.rotation.x = a + 0.4; g.add(em(h, 1.06, 0.6, 0)); }
  const cab = bola(0.22, plastico('#a9e1ff', { rugosidade: 0.05, verniz: 1, emissivo: '#cfeaff' }), 24); cab.scale.set(1.3, 0.8, 0.8); g.add(em(cab, 0.25, 0.85, 0));
  for (const x of [-0.25, -0.5]) { const j = bola(0.07, plastico('#a9e1ff', { rugosidade: 0.05, verniz: 1, emissivo: '#cfeaff' }), 12); j.scale.set(1, 1, 0.4); g.add(em(j, x, 0.68, 0.27)); }
  for (const z of [-0.3, 0.3]) { g.add(em(cilindro(0.025, 0.025, 0.3, plastico('#3a2a4a'), 8), 0.3, 0.3, z)); const r = cilindro(0.1, 0.1, 0.06, plastico('#2b2b3a'), 16); r.rotation.x = Math.PI / 2; g.add(em(r, 0.3, 0.12, z)); }
  g.rotation.set(0.15, -0.6, 0.12);
  return g;
}

function tambor() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.55, 0.55, 0.62, brilho('#ef3b4f'), 48), 0, 0.36, 0));
  const pele = plastico('#fffaf0', { rugosidade: 0.5, verniz: 0.3 });
  for (const y of [0.05, 0.67]) {
    g.add(em(malha(new THREE.TorusGeometry(0.56, 0.06, 12, 48), brilho('#ffc21a')).rotateX(Math.PI / 2), 0, y, 0));
  }
  g.add(em(cilindro(0.54, 0.54, 0.02, pele, 48), 0, 0.68, 0));
  for (let k = 0; k < 8; k++) {
    const a1 = (k / 8) * Math.PI * 2, a2 = ((k + 0.5) / 8) * Math.PI * 2;
    const p1 = [Math.cos(a1) * 0.57, 0.62, Math.sin(a1) * 0.57], p2 = [Math.cos(a2) * 0.57, 0.1, Math.sin(a2) * 0.57];
    const p3 = [Math.cos(a2 + Math.PI / 8) * 0.57, 0.62, Math.sin(a2 + Math.PI / 8) * 0.57];
    g.add(segmento(p1, p2, 0.018, plastico('#ffffff')), segmento(p2, p3, 0.018, plastico('#ffffff')));
  }
  for (const l of [-1, 1]) {
    const v = segmento([l * 0.1, 0.72, 0.1], [l * 0.65, 1.0, -0.2], 0.035, plastico('#d9975a', { rugosidade: 0.5 }));
    g.add(v, em(bola(0.08, brilho('#ffffff'), 12), l * 0.1, 0.74, 0.1));
  }
  g.rotation.x = 0.15;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Roupas (penduradas no varal: âncora 'topo')

function luva() {
  const g = new THREE.Group();
  const forma = new THREE.Shape();
  forma.moveTo(-0.3, 0);
  forma.lineTo(-0.32, 0.55);
  forma.absarc(0, 0.6, 0.32, Math.PI, 0, true);
  forma.lineTo(0.3, 0);
  forma.closePath();
  for (const [x, cor, rz] of [[-0.3, '#36c26a', 0.15], [0.32, '#2fae5f', -0.12]]) {
    const m = new THREE.Group();
    const mat = pl(cor, { rugosidade: 0.6, verniz: 0.2 });
    m.add(em(estufado(forma, mat, 0.12, 0.12), 0, 0.25, 0));
    const ded = capsula(0.11, 0.22, mat); ded.rotation.z = 0.7; m.add(em(ded, -0.36, 0.5, 0));
    const punho = caixa(0.72, 0.26, 0.36, 0.12, pl('#ffffff', { rugosidade: 0.7, verniz: 0.1 }));
    m.add(em(punho, 0, 0.13, 0));
    for (let k = -2; k <= 2; k++) m.add(em(caixa(0.03, 0.2, 0.37, 0.015, pl('#e6e6ee', { rugosidade: 0.7 })), k * 0.13, 0.13, 0));
    m.rotation.z = rz;
    if (x > 0) m.scale.x = -1;
    g.add(em(m, x, 0, x > 0 ? -0.12 : 0));
  }
  g.add(tubo(new THREE.CatmullRomCurve3([[-0.3, 0.1, 0.1], [0, -0.1, 0.25], [0.32, 0.1, 0.05]].map((p) => new THREE.Vector3(...p))), () => 0.02, pl('#ffffff'), { seg: 20, radial: 6 }));
  g.position.y = 0.15;
  const w = new THREE.Group(); w.add(g); return w;
}

function cachecol() {
  const g = new THREE.Group();
  const verm = pl('#ef3b4f', { rugosidade: 0.6, verniz: 0.2 }), bco = pl('#ffffff', { rugosidade: 0.6, verniz: 0.2 });
  const volta = malha(new THREE.TorusGeometry(0.42, 0.16, 20, 48), verm); volta.scale.set(1, 0.45, 1); g.add(em(volta, 0, 1.25, 0));
  for (const [x, rz, comp] of [[-0.18, 0.12, 1.0], [0.2, -0.08, 0.85]]) {
    const ponta = new THREE.Group();
    const n = 5;
    for (let k = 0; k < n; k++) ponta.add(em(caixa(0.34, comp / n + 0.02, 0.12, 0.05, k % 2 ? bco : verm), 0, -k * (comp / n), 0));
    for (let f = -2; f <= 2; f++) ponta.add(em(capsula(0.025, 0.12, verm), f * 0.07, -comp + 0.05, 0));
    ponta.rotation.z = rz;
    g.add(em(ponta, x, 1.08, 0.38 - Math.abs(x) * 0.3));
  }
  g.rotation.y = 0.2;
  return g;
}

function camiseta() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.moveTo(-0.45, 0);
  s.lineTo(0.45, 0);
  s.lineTo(0.45, 0.72);
  s.lineTo(0.62, 0.62);
  s.lineTo(0.82, 0.92);
  s.lineTo(0.42, 1.18);
  s.quadraticCurveTo(0, 0.95, -0.42, 1.18);
  s.lineTo(-0.82, 0.92);
  s.lineTo(-0.62, 0.62);
  s.lineTo(-0.45, 0.72);
  s.closePath();
  g.add(estufado(s, pl('#3d8bff', { rugosidade: 0.55, verniz: 0.25 }), 0.1, 0.09));
  const gola = malha(new THREE.TorusGeometry(0.22, 0.05, 10, 32, Math.PI), pl('#ffffff', { rugosidade: 0.5 }));
  gola.rotation.z = Math.PI; gola.scale.set(1.0, 0.55, 1);
  g.add(em(gola, 0, 1.12, 0.16));
  g.add(em(estrela3D(0.2, 0.05, brilho('#ffd23f')), 0, 0.55, 0.17));
  for (const l of [-1, 1]) g.add(em(caixa(0.3, 0.06, 0.24, 0.03, pl('#2a6fe0')), l * 0.72, 0.77, 0).rotateZ(-l * 0.98));
  g.rotation.y = 0.15;
  return g;
}

function bone() {
  const g = new THREE.Group();
  const azul = pl('#3d8bff', { rugosidade: 0.45, verniz: 0.4 });
  const copa = malha(new THREE.SphereGeometry(0.55, 48, 24, 0, Math.PI * 2, 0, Math.PI / 2), azul); copa.scale.set(1, 0.85, 1.05); g.add(em(copa, 0, 0.05, 0));
  g.add(em(malha(new THREE.TorusGeometry(0.55, 0.05, 10, 48), pl('#2a6fe0')).rotateX(Math.PI / 2), 0, 0.06, 0));
  const aba = new THREE.Shape(); aba.absarc(0, 0, 0.5, Math.PI * 0.05, Math.PI * 0.95, false); aba.quadraticCurveTo(0, 0.25, 0.5 * Math.cos(Math.PI * 0.05), 0.5 * Math.sin(Math.PI * 0.05));
  const a = estufado(aba, pl('#ff3b4f', { rugosidade: 0.4, verniz: 0.5 }), 0.03, 0.03);
  a.rotation.x = Math.PI / 2 - 0.12; g.add(em(a, 0, 0.08, 0.3));
  g.add(em(bola(0.07, pl('#ff3b4f'), 12), 0, 0.52, 0));
  g.add(em(estrela3D(0.13, 0.04, brilho('#ffd23f')), 0, 0.3, 0.5).rotateX(-0.45));
  g.rotation.set(0.1, 0.5, 0);
  return g;
}

function meia() {
  // cano de pé com listras, pé deitado, calcanhar e ponta amarelos
  const g = new THREE.Group();
  const rosa = pl('#ff5a7a', { rugosidade: 0.6, verniz: 0.2 }), bco = pl('#ffffff', { rugosidade: 0.6, verniz: 0.2 }), am = pl('#ffc21a', { rugosidade: 0.6, verniz: 0.2 });
  g.add(em(cilindro(0.22, 0.22, 0.9, rosa, 32), 0, 0.75, 0));
  for (const y of [0.55, 0.8, 1.05]) g.add(em(cilindro(0.225, 0.225, 0.1, bco, 32), 0, y, 0));
  g.add(em(cilindro(0.24, 0.24, 0.16, bco, 32), 0, 1.22, 0));
  g.add(em(malha(new THREE.TorusGeometry(0.22, 0.04, 10, 32), bco).rotateX(Math.PI / 2), 0, 1.3, 0));
  const pe = capsula(0.22, 0.5, rosa); pe.rotation.z = Math.PI / 2; g.add(em(pe, 0.3, 0.24, 0));
  g.add(em(bola(0.225, am, 32), 0, 0.3, 0));
  const ponta = bola(0.225, am, 32); ponta.scale.set(0.8, 1, 1); g.add(em(ponta, 0.58, 0.24, 0));
  g.rotation.y = -0.4;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Céu, natureza e formas

function coracao() {
  const s = new THREE.Shape();
  s.moveTo(0, -0.55);
  s.bezierCurveTo(-0.15, -0.38, -0.62, -0.12, -0.62, 0.22);
  s.bezierCurveTo(-0.62, 0.52, -0.34, 0.66, -0.16, 0.6);
  s.bezierCurveTo(-0.06, 0.56, 0, 0.46, 0, 0.4);
  s.bezierCurveTo(0, 0.46, 0.06, 0.56, 0.16, 0.6);
  s.bezierCurveTo(0.34, 0.66, 0.62, 0.52, 0.62, 0.22);
  s.bezierCurveTo(0.62, -0.12, 0.15, -0.38, 0, -0.55);
  const g = new THREE.Group();
  g.add(estufado(s, brilho('#ff2d55', { rugosidade: 0.15 }), 0.18, 0.16));
  g.rotation.set(0.05, -0.25, 0.1);
  return g;
}

function florRosa() {
  const g = new THREE.Group();
  const verde = pl('#3fae4a');
  g.add(tubo(new THREE.CatmullRomCurve3([[0, 0, 0], [0.05, 0.4, 0], [0, 0.8, 0.05]].map((p) => new THREE.Vector3(...p))), () => 0.04, verde, { seg: 16, radial: 8 }));
  for (const l of [-1, 1]) { const f = bola(0.18, verde, 16); f.scale.set(1.3, 0.25, 0.6); f.rotation.z = l * 0.5; g.add(em(f, l * 0.2, 0.3 + (l > 0 ? 0.15 : 0), 0)); }
  const cab = new THREE.Group();
  const petala = pl('#ff8fc0', { rugosidade: 0.35, verniz: 0.6 });
  for (let k = 0; k < 5; k++) {
    const a = (k / 5) * Math.PI * 2 + Math.PI / 2;
    const p = new THREE.Group();
    for (const l of [-1, 1]) { const b = bola(0.2, petala, 24); b.scale.set(0.85, 1.1, 0.3); p.add(em(b, l * 0.07, 0.25, 0)); }
    p.rotation.z = a - Math.PI / 2;
    cab.add(p);
  }
  cab.add(em(bola(0.13, pl('#ffe066'), 20), 0, 0, 0.05));
  for (let k = 0; k < 6; k++) { const a = (k / 6) * Math.PI * 2; cab.add(em(bola(0.03, pl('#ff5a8a'), 8), Math.cos(a) * 0.17, Math.sin(a) * 0.17, 0.08)); }
  cab.rotation.x = -0.35;
  g.add(em(cab, 0, 1.05, 0.05));
  return g;
}

function bolinha(cor) {
  return () => {
    const g = new THREE.Group();
    g.add(em(bola(0.5, brilho(cor, { rugosidade: 0.18 }), 64), 0, 0.5, 0));
    return g;
  };
}

// ---------------------------------------------------------------------------------------------

export const modelosDasFiguras = {
  gato, tigre, cachorro, coelho, rato, porco, vaca, cavalo, leao, macaco, ovelha, elefante,
  peixe, sapo, tartaruga, pintinho, galo, galinha, passarinho, joaninha, abelha, foca, pinguim, pato,
  borboleta: modelosDosItens.borboleta,
  maca, banana, morango, uva, cereja, cenoura, pao, ovo, bolo: modelosDosItens.bolo, bolinho,
  bola: modelosDosItens.bola, balao, carrinho, trem: modelosDosItens.trem, aviao, ursinho: modelosDosItens.ursinho, pipa: modelosDosItens.pipa, tambor,
  luva, cachecol, camiseta, bone, meia,
  estrela: modelosDosItens.estrela, coracao, lua: modelosDosItens.lua, flor: florRosa,
  bolinha_vermelha: bolinha('#ff3b4f'), bolinha_azul: bolinha('#3d8bff'), bolinha_verde: bolinha('#36c26a'),
  bolinha_amarela: bolinha('#ffc21a'), bolinha_roxa: bolinha('#9a62ff'), bolinha_laranja: bolinha('#ff8a1c'),
};

/** Peças e moldes reaproveitados pelos lotes seguintes de figuras (Fase 9: figurasDoLote9a.js). */
export const pecasDasFiguras = { pl, brilho, preto, branco, torneado, tubo, estufado, carinha, bichinho, orelhasDeGato, bigodes, rabo, coracao, ave };
