// Figuras do lote 2 (Fase 9B): as coisas das áreas novas da casa (CasaQueCresce), dos livrinhos, da história, do
// ateliê e da loja que ainda eram emoji. Mesmo estilo das figuras das perguntas (figuras.js): brinquedo de plástico
// com verniz, cores vivas, formas gordinhas, base em y = 0, de frente ou em três quartos. A lista (id, emojis,
// lugar) fica em src/catalogo_fase9b.js.
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em, naSuperficie } from '../kit.js';
import { pecasDosItens, modelosDosItens } from './itensDaCasa.js';
import { modelosDasFiguras as F, pecasDasFiguras as P } from './figuras.js';

const { segmento, olho, sorriso, bochecha } = pecasDosItens;
const { pl, brilho, preto, branco, torneado, tubo, estufado, bichinho, bigodes, rabo } = P;

const V = (x, y, z) => new THREE.Vector3(x, y, z);
const curva = (pts) => new THREE.CatmullRomCurve3(pts.map((p) => V(...p)));
const metal = (cor = '#d9dee8') => plastico(cor, { rugosidade: 0.18, verniz: 1, ambiente: 0.9 });
const ouro = (cor = '#ffc21a') => plastico(cor, { rugosidade: 0.18, verniz: 1 });
const vidro = (cor = '#bfe8ff', opacidade = 0.42) => plastico(cor, { rugosidade: 0.04, verniz: 1, opacidade, ambiente: 0.9 });
const madeira = (cor = '#d9975a') => plastico(cor, { rugosidade: 0.5, verniz: 0.35 });
const luzinha = (cor) => plastico(cor, { rugosidade: 0.1, emissivo: cor });
const grupo = (...f) => { const g = new THREE.Group(); for (const o of f) if (o) g.add(o); return g; };
const rot = (o, x = 0, y = 0, z = 0) => { o.rotation.set(x, y, z); return o; };
const esc = (o, x, y = x, z = x) => { o.scale.set(x, y, z); return o; };
/** Anel (toro) deitado no plano xz. */
const anel = (r, t, mat, seg = 48) => rot(malha(new THREE.TorusGeometry(r, t, 14, seg), mat), Math.PI / 2);
/** Brilhinho de quatro pontas (as faíscas dos emojis). */
function faisca(s, cor = '#ffffff') {
  const m = luzinha(cor);
  const g = new THREE.Group();
  for (const r of [0, Math.PI / 2]) { const b = bola(s * 0.22, m, 12); b.scale.set(1, 4, 0.6); b.rotation.z = r; g.add(b); }
  return g;
}
/** Forma de [pontos] 2D (x, y) fechada, estufada. */
function placa(pontos, mat, prof, bisel) {
  const s = new THREE.Shape();
  pontos.forEach(([x, y], i) => (i ? s.lineTo(x, y) : s.moveTo(x, y)));
  s.closePath();
  return estufado(s, mat, prof, bisel);
}

// ---------------------------------------------------------------------------------------------
// Quarto novo

/** 🏮 Lanterninha de papel vermelha, pendurada. */
function lanternaDePapel() {
  const g = new THREE.Group();
  const verm = brilho('#ff3b3b', { emissivo: '#ff4a2a' });
  const corpo = bola(0.5, verm, 48); corpo.scale.set(1, 0.9, 1); g.add(em(corpo, 0, 0.9, 0));
  const gomo = plastico('#c7202f', { rugosidade: 0.35, verniz: 0.6 });
  for (let k = 0; k < 6; k++) {
    const t = malha(new THREE.TorusGeometry(0.5, 0.012, 8, 64), gomo);
    t.scale.set(1, 0.9, 1); t.rotation.y = (k / 6) * Math.PI; g.add(em(t, 0, 0.9, 0));
  }
  for (const y of [0.47, 1.33]) g.add(em(cilindro(0.22, 0.22, 0.12, ouro(), 32), 0, y, 0));
  g.add(segmento([0, 1.39, 0], [0, 1.7, 0], 0.02, plastico('#7a3a1a')));
  g.add(em(anel(0.07, 0.02, ouro()), 0, 1.72, 0));
  const borla = plastico('#ffc21a', { rugosidade: 0.6, verniz: 0.2 });
  g.add(em(bola(0.06, ouro(), 12), 0, 0.38, 0));
  for (let k = 0; k < 7; k++) { const a = (k / 7) * Math.PI * 2; g.add(segmento([0, 0.36, 0], [Math.cos(a) * 0.06, 0.08, Math.sin(a) * 0.06], 0.022, borla)); }
  g.rotation.z = 0.08;
  return g;
}

/** 💖 Almofada de coração com brilhinhos. */
function almofadaCoracao() {
  const s = new THREE.Shape();
  s.moveTo(0, -0.55);
  s.bezierCurveTo(-0.15, -0.38, -0.62, -0.12, -0.62, 0.22);
  s.bezierCurveTo(-0.62, 0.52, -0.34, 0.66, -0.16, 0.6);
  s.bezierCurveTo(-0.06, 0.56, 0, 0.46, 0, 0.4);
  s.bezierCurveTo(0, 0.46, 0.06, 0.56, 0.16, 0.6);
  s.bezierCurveTo(0.34, 0.66, 0.62, 0.52, 0.62, 0.22);
  s.bezierCurveTo(0.62, -0.12, 0.15, -0.38, 0, -0.55);
  const g = new THREE.Group();
  const c = estufado(s, plastico('#ff6fb5', { rugosidade: 0.45, verniz: 0.35 }), 0.22, 0.2);
  g.add(em(c, 0, 0.6, 0));
  const c2 = estufado(s, plastico('#ffb3d9', { rugosidade: 0.4, verniz: 0.5 }), 0.05, 0.06);
  c2.scale.setScalar(0.55); g.add(em(c2, 0, 0.66, 0.27));
  g.add(em(faisca(0.28), 0.5, 1.18, 0.15), em(faisca(0.18), -0.62, 1.0, 0.1), em(faisca(0.16), 0.66, 0.35, 0.2));
  g.rotation.set(-0.12, -0.3, 0.06);
  return g;
}

/** 🥿 Sapatilha azul com lacinho. */
function sapatilha() {
  const g = new THREE.Group();
  const azul = brilho('#4d8dff');
  const sola = bola(0.33, plastico('#ff8fb0', { rugosidade: 0.5, verniz: 0.3 }), 40); sola.scale.set(0.95, 0.14, 1.9);
  g.add(em(sola, 0, 0.05, 0));
  const corpo = bola(0.3, azul, 48); corpo.scale.set(1, 0.7, 2.0); g.add(em(corpo, 0, 0.12, 0));
  const forro = bola(0.24, plastico('#ffd2e6', { rugosidade: 0.7, verniz: 0.1 }), 32); forro.scale.set(0.9, 0.3, 1.5); g.add(em(forro, 0, 0.32, -0.2));
  const borda = malha(new THREE.TorusGeometry(0.25, 0.035, 10, 40), azul); borda.scale.set(0.9, 1.5, 1); borda.rotation.x = Math.PI / 2; g.add(em(borda, 0, 0.33, -0.2));
  const rosa = brilho('#ff5c9a');
  for (const l of [-1, 1]) { const a = bola(0.09, rosa, 16); a.scale.set(1.3, 0.75, 0.6); a.rotation.z = l * 0.3; g.add(em(a, l * 0.1, 0.33, 0.12)); }
  g.add(em(bola(0.05, rosa, 12), 0, 0.33, 0.13));
  g.rotation.y = -0.75;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Área do pet

/** 🥏 Disco (frisbee) inclinado. */
function frisbee() {
  const g = new THREE.Group();
  const cor = brilho('#2fc6a8');
  const d = bola(0.6, cor, 64); d.scale.set(1, 0.2, 1); g.add(d);
  g.add(anel(0.6, 0.07, cor, 64));
  for (const r of [0.22, 0.4]) { const a = anel(r, 0.022, brilho('#ffffff')); a.position.y = 0.12 * Math.sqrt(1 - (r / 0.6) ** 2) - 0.005; g.add(a); }
  g.add(em(esc(bola(0.1, brilho('#ffc93c'), 24), 1, 0.3, 1), 0, 0.12, 0));
  g.rotation.set(0.55, 0, 0.15);
  return grupo(g);
}

/** 🐩 Poodle de pelo fofinho. */
function poodle() {
  const fofo = pl('#f5f2fa', { rugosidade: 0.7, verniz: 0.15 });
  return bichinho({
    cor: '#f3eef8', claro: '#fffaff', nariz: '#2a1a24', giro: 0.35,
    orelhas: (cab, m, mc, R) => {
      for (const [x, y, r] of [[0, 0.95, 0.28], [-0.22, 0.86, 0.22], [0.22, 0.86, 0.22], [-0.1, 1.05, 0.2], [0.12, 1.04, 0.2]]) cab.add(em(bola(R * r * 1.3, fofo, 20), x * R * 1.4, y * R, -R * 0.05));
      for (const l of [-1, 1]) for (const y of [0.15, -0.15, -0.42]) cab.add(em(bola(R * 0.22, fofo, 16), l * R * 0.95, y * R, -R * 0.05));
      const laco = brilho('#ff5c9a');
      for (const l of [-1, 1]) { const b = bola(R * 0.13, laco, 12); b.scale.set(1.4, 0.8, 0.6); b.rotation.z = l * 0.4; cab.add(em(b, R * 0.32 + l * R * 0.14, R * 0.86, R * 0.42)); }
    },
    extras: (g) => {
      g.add(em(bola(0.17, fofo, 20), -0.45, 0.62, -0.38));
      g.add(segmento([-0.3, 0.3, -0.3], [-0.42, 0.55, -0.36], 0.05, pl('#f3eef8')));
      for (const l of [-1, 1]) g.add(em(bola(0.13, fofo, 16), l * 0.31, 0.12, 0.3));
      const col = malha(new THREE.TorusGeometry(0.3, 0.045, 10, 32), brilho('#ff5c9a')); col.rotation.x = Math.PI / 2 - 0.25; g.add(em(col, 0, 0.88, 0.02));
    },
  });
}

/** 🧶 Novelo de lã com o fio solto e as agulhas. */
function novelo() {
  const g = new THREE.Group();
  const la = plastico('#ff4f7b', { rugosidade: 0.75, verniz: 0.08 });
  const fio = plastico('#ff6f93', { rugosidade: 0.7, verniz: 0.1 });
  g.add(em(bola(0.46, la, 40), 0, 0.46, 0));
  const voltas = [[0, 0, 0], [0.6, 0, 0.3], [-0.5, 0.4, 0], [1.2, 0.8, 0], [0.3, 1.5, 0.6], [-1.0, 0.2, 1.1], [0.9, -0.6, 1.5], [1.5, 0.3, -0.4]];
  for (const [x, y, z] of voltas) {
    const t = malha(new THREE.TorusGeometry(0.465, 0.022, 8, 64), fio); t.rotation.set(x, y, z); g.add(em(t, 0, 0.46, 0));
  }
  g.add(tubo(curva([[0.3, 0.2, 0.32], [0.55, 0.03, 0.45], [0.85, 0.03, 0.3], [1.0, 0.03, 0.55]]), () => 0.025, fio, { seg: 30, radial: 8 }));
  for (const l of [-1, 1]) {
    g.add(segmento([l * 0.12, 0.6, -0.1], [l * 0.42 - 0.05, 1.3, -0.28], 0.025, metal('#c4cad8')));
    g.add(em(bola(0.06, brilho('#ffc93c'), 12), l * 0.42 - 0.05, 1.32, -0.28));
  }
  g.rotation.y = -0.3;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Flores, livrinhos

/** Haste com folhas e uma flor (cabeça) por cima. */
function hasteComFlor(cab, alt = 1.0, folhas = '#3fae4a') {
  const g = new THREE.Group();
  const verde = pl(folhas);
  g.add(tubo(curva([[0, 0, 0], [0.05, alt * 0.45, 0], [0, alt, 0.05]]), () => 0.045, verde, { seg: 16, radial: 8 }));
  for (const l of [-1, 1]) { const f = bola(0.2, verde, 16); f.scale.set(1.3, 0.25, 0.6); f.rotation.z = l * 0.5; g.add(em(f, l * 0.22, alt * 0.3 + (l > 0 ? 0.14 : 0), 0)); }
  g.add(em(cab, 0, alt + 0.05, 0.05));
  return g;
}

/** Cabeça de flor de [n] pétalas redondas, virada para a frente. */
function cabecaDeFlor({ n = 8, cor, miolo, r = 0.2, largura = 0.55, raioMiolo = 0.14, camadas = 1 }) {
  const cab = new THREE.Group();
  const petala = pl(cor, { rugosidade: 0.35, verniz: 0.6 });
  for (let c = 0; c < camadas; c++) for (let k = 0; k < n; k++) {
    const a = ((k + c * 0.5) / n) * Math.PI * 2;
    const p = bola(r, petala, 20); p.scale.set(largura, 1.15, 0.28);
    const d = r * 1.05 * (1 - c * 0.25);
    p.position.set(Math.cos(a) * d, Math.sin(a) * d, -0.02 + c * 0.04); p.rotation.z = a - Math.PI / 2;
    cab.add(p);
  }
  const m = bola(raioMiolo, pl(miolo, { rugosidade: 0.6, verniz: 0.2 }), 24); m.scale.set(1, 1, 0.6); cab.add(em(m, 0, 0, 0.05));
  cab.rotation.x = -0.3;
  return cab;
}

/** 🌼 Margarida amarela. */
const margarida = () => hasteComFlor(cabecaDeFlor({ n: 12, cor: '#ffd23f', miolo: '#ff8a1c', r: 0.19, largura: 0.42, raioMiolo: 0.14 }));

/** 🌺 Hibisco vermelho com o pistilo comprido. */
function hibisco() {
  const cab = new THREE.Group();
  const petala = pl('#ff3b6b', { rugosidade: 0.35, verniz: 0.6 });
  for (let k = 0; k < 5; k++) {
    const a = (k / 5) * Math.PI * 2 + Math.PI / 2;
    const p = bola(0.26, petala, 24); p.scale.set(0.95, 1.05, 0.3);
    p.position.set(Math.cos(a) * 0.24, Math.sin(a) * 0.24, 0); p.rotation.set(0, 0, a - Math.PI / 2);
    cab.add(p);
  }
  cab.add(em(esc(bola(0.12, pl('#b5123f'), 16), 1, 1, 0.5), 0, 0, 0.06));
  cab.add(tubo(curva([[0, 0, 0.05], [0.05, 0.12, 0.2], [0.1, 0.3, 0.32]]), () => 0.022, pl('#ff8fb0'), { seg: 12, radial: 8 }));
  for (const [x, y, z] of [[0.1, 0.32, 0.33], [0.06, 0.3, 0.36], [0.13, 0.28, 0.3], [0.09, 0.36, 0.3]]) cab.add(em(bola(0.035, pl('#ffd23f'), 8), x, y, z));
  cab.rotation.x = -0.25;
  return hasteComFlor(cab, 0.95, '#2f9e44');
}

/** 🪷 Flor de lótus numa folha boiando. */
function lotus() {
  const g = new THREE.Group();
  const folha = new THREE.Shape();
  folha.absarc(0, 0, 0.75, 0.25, Math.PI * 2 - 0.25, false); folha.lineTo(0, 0); folha.closePath();
  const f = estufado(folha, pl('#3fbf5a'), 0.03, 0.03); f.rotation.x = -Math.PI / 2; f.rotation.z = -0.9; g.add(em(f, 0, 0.04, 0));
  const petalaM = (cor) => pl(cor, { rugosidade: 0.35, verniz: 0.6 });
  const camada = (n, r, alt, inclina, cor, giro) => {
    for (let k = 0; k < n; k++) {
      const a = (k / n) * Math.PI * 2 + giro;
      const p = new THREE.Group();
      const b = bola(0.2, petalaM(cor), 20); b.scale.set(0.55, 1.2, 0.3); b.position.y = 0.2; p.add(b);
      p.position.set(Math.cos(a) * r, alt, Math.sin(a) * r);
      p.rotation.order = 'YXZ'; p.rotation.y = -a + Math.PI / 2; p.rotation.x = inclina;
      g.add(p);
    }
  };
  camada(8, 0.18, 0.1, 1.0, '#ffb3d1', 0);
  camada(7, 0.12, 0.14, 0.55, '#ff8cc0', 0.3);
  camada(5, 0.05, 0.18, 0.2, '#ff6fae', 0.1);
  g.add(em(esc(bola(0.09, pl('#ffd23f'), 16), 1, 0.5, 1), 0, 0.36, 0));
  return g;
}

/** 🌲 Pinheiro de três andares. */
function pinheiro() {
  const g = new THREE.Group();
  g.add(em(cilindro(0.12, 0.15, 0.35, madeira('#a0643a'), 20), 0, 0.17, 0));
  const verdes = ['#2e9e4f', '#36b25a', '#45c468'];
  [[0.62, 0.3, 0.6], [0.5, 0.72, 0.55], [0.36, 1.1, 0.5]].forEach(([r, y, h], i) => {
    g.add(em(torneado([[0, h + 0.04], [r * 0.25, h * 0.8], [r * 0.7, h * 0.3], [r, 0.05], [r * 0.9, 0], [0, 0.02]], pl(verdes[i]), 40, 32), 0, y, 0));
  });
  g.add(em(bola(0.06, pl('#45c468'), 12), 0, 1.66, 0));
  return g;
}

/** Uma casinha de brinquedo. */
function casinha(cor, telhado, l = 0.7, a = 0.55) {
  const g = new THREE.Group();
  g.add(em(caixa(l, a, 0.6, 0.06, pl(cor)), 0, a / 2, 0));
  const t = placa([[-l / 2 - 0.08, 0], [l / 2 + 0.08, 0], [0, 0.42]], brilho(telhado), 0.55, 0.05);
  g.add(em(t, 0, a, 0));
  g.add(em(caixa(0.16, 0.26, 0.04, 0.04, madeira('#b86b3a')), -l * 0.18, 0.13, 0.3));
  g.add(em(bola(0.015, ouro(), 8), -l * 0.18 + 0.05, 0.13, 0.33));
  const jan = caixa(0.15, 0.15, 0.04, 0.03, plastico('#cfeeff', { rugosidade: 0.1, verniz: 1, emissivo: '#bfe6ff' }));
  g.add(em(jan, l * 0.2, a * 0.6, 0.3));
  g.add(em(caixa(0.18, 0.025, 0.05, 0.01, branco()), l * 0.2, a * 0.6, 0.31), em(caixa(0.025, 0.18, 0.05, 0.01, branco()), l * 0.2, a * 0.6, 0.31));
  return g;
}

/** 🏘 Casinhas da rua. */
function casinhas() {
  const g = new THREE.Group();
  g.add(em(casinha('#ffe7b8', '#ef476f', 0.72, 0.6), -0.45, 0, -0.25));
  g.add(em(casinha('#d8ecff', '#4d96ff', 0.6, 0.48), 0.45, 0, -0.35));
  g.add(em(casinha('#ffd6e3', '#36c26a', 0.62, 0.42), 0.05, 0, 0.45));
  const chao = esc(bola(1.05, massinha('#8fd16a'), 40), 1.1, 0.06, 0.8); g.add(em(chao, 0, 0, 0));
  g.rotation.y = -0.25;
  return g;
}

/** Ninho de gravetos (🪹 vazio, 🪺 com ovinhos). */
function ninho(ovos) {
  return () => {
    const g = new THREE.Group();
    const palha = plastico('#b9783e', { rugosidade: 0.7, verniz: 0.1 });
    const palhaClara = plastico('#d9a05e', { rugosidade: 0.7, verniz: 0.1 });
    const base = bola(0.5, palha, 40); base.scale.set(1, 0.45, 1); g.add(em(base, 0, 0.18, 0));
    const ninhoAnel = malha(new THREE.TorusGeometry(0.48, 0.14, 16, 48), palha); ninhoAnel.rotation.x = Math.PI / 2; g.add(em(ninhoAnel, 0, 0.3, 0));
    const dentro = bola(0.4, plastico('#7a4a22', { rugosidade: 0.8, verniz: 0 }), 32); dentro.scale.set(1, 0.3, 1); g.add(em(dentro, 0, 0.33, 0));
    for (let k = 0; k < 22; k++) {
      const a = (k / 22) * Math.PI * 2, b = a + 0.9 + (k % 3) * 0.2;
      const y = 0.22 + (k % 4) * 0.05;
      g.add(segmento([Math.cos(a) * 0.55, y, Math.sin(a) * 0.55], [Math.cos(b) * 0.55, y + 0.06, Math.sin(b) * 0.55], 0.022, k % 2 ? palhaClara : palha));
    }
    if (ovos) {
      const ovo = pl('#7fd3f0', { rugosidade: 0.3, verniz: 0.7 });
      for (const [x, z, r] of [[-0.14, 0.02, 0.4], [0.14, -0.04, -0.3], [0.0, 0.16, 0.1]]) { const o = bola(0.14, ovo, 24); o.scale.set(1, 1.3, 1); o.rotation.z = r; g.add(em(o, x, 0.45, z)); }
    }
    return g;
  };
}

/** Carinha redonda amarela (😄 😊 🤗 😟). */
function carinha(tipo) {
  return () => {
    const g = new THREE.Group();
    const R = 0.55;
    const cab = new THREE.Group();
    cab.add(bola(R, plastico('#ffc93c', { rugosidade: 0.28, verniz: 0.85, ambiente: 0.4 }), 64));
    const escuro = plastico('#5a2a14', { rugosidade: 0.25, verniz: 0.8 });
    const olhoFechado = (l) => { const s = sorriso(R * 0.13, R * 0.028, 2.4, '#5a2a14'); s.rotation.z = Math.PI; naSuperficie(s, R, l * R * 0.34, R * 0.2, -R * 0.005); return s; };
    if (tipo === 'rindo' || tipo === 'feliz' || tipo === 'abraco') {
      for (const l of [-1, 1]) cab.add(olhoFechado(l));
    } else {
      for (const l of [-1, 1]) { const o = olho(R * 0.14); naSuperficie(o, R, l * R * 0.3, R * 0.16, -R * 0.04); cab.add(o); }
      for (const l of [-1, 1]) { const s = capsula(R * 0.03, R * 0.18, escuro); s.rotation.z = Math.PI / 2 + l * 0.35; naSuperficie(s, R, l * R * 0.3, R * 0.44, -R * 0.01); s.rotateZ(Math.PI / 2 - l * 0.35); cab.add(s); }
    }
    for (const l of [-1, 1]) { const b = bochecha(R * 0.17); naSuperficie(b, R, l * R * 0.58, -R * 0.12, -R * 0.03); cab.add(b); }
    if (tipo === 'rindo' || tipo === 'abraco') {
      const s = new THREE.Shape(); s.moveTo(-0.24, 0); s.lineTo(0.24, 0); s.absarc(0, 0, 0.24, 0, -Math.PI, true);
      const boca = estufado(s, escuro, 0.02, 0.025); boca.scale.set(R * 1.6, R * 1.5, 1);
      const b = grupo(boca); naSuperficie(b, R, 0, -R * 0.2, -R * 0.06); cab.add(b);
      const dentes = caixa(0.3 * R * 1.6, 0.06, 0.05, 0.02, branco()); const d = grupo(dentes); naSuperficie(d, R, 0, -R * 0.24, -R * 0.03); cab.add(d);
      const lingua = esc(bola(R * 0.15, pl('#ff6f7f'), 16), 1.4, 0.7, 0.4); const ll = grupo(lingua); naSuperficie(ll, R, 0, -R * 0.47, -R * 0.06); cab.add(ll);
    } else if (tipo === 'feliz') {
      const s = sorriso(R * 0.24, R * 0.035, 2.4, '#5a2a14'); naSuperficie(s, R, 0, -R * 0.24, -R * 0.01); cab.add(s);
    } else {
      const s = sorriso(R * 0.16, R * 0.035, 2.0, '#5a2a14'); s.rotation.z = Math.PI; const gs = grupo(s); naSuperficie(gs, R, 0, -R * 0.42, -R * 0.01); cab.add(gs);
      const gota = esc(bola(R * 0.09, vidro('#8fd8ff', 0.75), 12), 0.8, 1.2, 0.5); naSuperficie(gota, R, R * 0.66, R * 0.42, 0); cab.add(gota);
    }
    g.add(em(cab, 0, R, 0));
    if (tipo === 'abraco') {
      const mao = plastico('#ffb52e', { rugosidade: 0.3, verniz: 0.8 });
      for (const l of [-1, 1]) {
        const m = bola(R * 0.3, mao, 24); m.scale.set(1, 0.75, 0.6); m.rotation.z = l * 0.6; g.add(em(m, l * R * 0.72, R * 0.42, R * 0.72));
        for (let k = 0; k < 3; k++) { const d = capsula(R * 0.06, R * 0.14, mao); d.rotation.z = l * (0.9 + k * 0.25); g.add(em(d, l * (R * 0.52 - k * 0.04), R * (0.58 - k * 0.08), R * 0.8)); }
      }
    }
    g.rotation.y = tipo === 'preocupada' ? 0.15 : -0.12;
    return g;
  };
}

/** 🎊 Bola de confete abrindo, com fitas e papeizinhos. */
function bolaDeConfete() {
  const g = new THREE.Group();
  const metade = (cor) => malha(new THREE.SphereGeometry(0.42, 40, 20, 0, Math.PI * 2, 0, Math.PI / 2), brilho(cor, { rugosidade: 0.15 }));
  const de1 = metade('#ffc21a'); de1.material.side = THREE.DoubleSide; de1.rotation.z = 0.55; g.add(em(de1, -0.1, 0.45, 0));
  const de2 = metade('#ffc21a'); de2.rotation.set(Math.PI, 0, 0.25); g.add(em(de2, 0.05, 0.4, 0));
  g.add(em(bola(0.38, luzinha('#ffe9a8'), 24), -0.02, 0.43, 0));
  const cores = ['#ff3b4f', '#3d8bff', '#36c26a', '#9a62ff', '#ff8a1c', '#ff5c9a'];
  const fitas = [[[0, 0.5, 0], [0.4, 0.95, 0.1], [0.2, 1.3, 0.2], [0.55, 1.55, 0.1]], [[0, 0.5, 0], [-0.35, 0.9, 0.2], [-0.15, 1.25, 0.1], [-0.5, 1.5, 0]], [[0, 0.5, 0], [0.05, 1.0, 0.25], [0.3, 1.25, 0.2], [0.1, 1.6, 0.3]]];
  fitas.forEach((f, i) => g.add(tubo(curva(f), () => 0.03, brilho(cores[i * 2]), { seg: 30, radial: 8 })));
  for (let k = 0; k < 18; k++) {
    const a = k * 2.4, r = 0.25 + (k % 5) * 0.12;
    const p = caixa(0.09, 0.05, 0.015, 0.008, brilho(cores[k % cores.length]));
    p.rotation.set(k, k * 0.7, k * 1.3);
    g.add(em(p, Math.cos(a) * r, 0.9 + (k % 6) * 0.15, Math.sin(a) * r * 0.4 + 0.15));
  }
  return g;
}

/** Bolinha de cor (como as da Lógica): 🟤, ⚪. */
function bolinha(cor, o = {}) {
  return () => grupo(em(bola(0.5, brilho(cor, { rugosidade: 0.18, ...o }), 64), 0, 0.5, 0));
}

/** 🟫 Quadrado marrom: bloquinho de terra. */
function quadradoMarrom() {
  const g = new THREE.Group();
  g.add(em(caixa(0.9, 0.9, 0.9, 0.14, brilho('#9a5a32', { rugosidade: 0.25 })), 0, 0.45, 0));
  g.rotation.y = 0.5;
  return g;
}

/** 🔖 Marcador de página com fitinha. */
function marcador() {
  const g = new THREE.Group();
  const m = placa([[-0.3, 0.65], [0.3, 0.65], [0.3, -0.6], [0, -0.38], [-0.3, -0.6]], brilho('#ff4f7b'), 0.04, 0.05);
  g.add(em(m, 0, 0.65, 0));
  g.add(em(esc(estrelaPlana(0.13, brilho('#ffc93c')), 1), 0, 0.85, 0.06));
  g.add(em(caixa(0.6, 0.06, 0.06, 0.02, plastico('#ffffff', { rugosidade: 0.4 })), 0, 1.14, 0.03));
  g.rotation.set(0, -0.3, 0.12);
  return g;
}

/** Estrelinha de cinco pontas estufada. */
function estrelaPlana(r, mat, prof = 0.03) {
  const pts = [];
  for (let k = 0; k < 10; k++) { const a = Math.PI / 2 + (k * Math.PI) / 5; const rr = k % 2 ? r * 0.45 : r; pts.push([Math.cos(a) * rr, Math.sin(a) * rr]); }
  return placa(pts, mat, prof, r * 0.18);
}

/** 🌄 Montanhas com o sol nascendo. */
function montanhaSol() {
  const g = new THREE.Group();
  const sol = bola(0.36, plastico('#ffb52e', { rugosidade: 0.2, verniz: 0.8, emissivo: '#ff9a1c' }), 32);
  g.add(em(sol, 0.15, 0.85, -0.55));
  for (let k = 0; k < 8; k++) { const a = Math.PI * (0.05 + k / 7 * 0.9); g.add(segmento([0.15 + Math.cos(a) * 0.48, 0.85 + Math.sin(a) * 0.48, -0.6], [0.15 + Math.cos(a) * 0.66, 0.85 + Math.sin(a) * 0.66, -0.6], 0.035, luzinha('#ffc93c'))); }
  const morro = (r, h, cor, neve) => {
    const m = new THREE.Group();
    m.add(torneado([[0, h], [r * 0.18, h * 0.95], [r * 0.6, h * 0.45], [r, 0.02], [0, 0]], pl(cor), 40, 32));
    if (neve) m.add(torneado([[0, h + 0.015], [r * 0.18, h * 0.96], [r * 0.27, h * 0.8], [r * 0.12, h * 0.82], [0, h * 0.8]], pl('#ffffff'), 40, 16));
    return m;
  };
  g.add(em(morro(0.65, 1.1, '#7b6cf0', true), -0.35, 0, -0.2));
  g.add(em(morro(0.55, 0.8, '#5aa0f0', true), 0.5, 0, -0.1));
  g.add(em(morro(0.5, 0.45, '#4cc46a', false), 0.05, 0, 0.35));
  const chao = esc(bola(1.0, massinha('#8fd16a'), 40), 1.15, 0.05, 0.7); g.add(chao);
  return g;
}

/** 🪵 Tronco cortado com folhinha. */
function tronco() {
  const g = new THREE.Group();
  const casca = plastico('#8a5530', { rugosidade: 0.55, verniz: 0.3 });
  const t = cilindro(0.3, 0.32, 1.3, casca, 40); t.rotation.z = Math.PI / 2; g.add(em(t, 0, 0.32, 0));
  for (const l of [-1, 1]) {
    const tampa = cilindro(0.29, 0.29, 0.03, plastico('#f2c98f', { rugosidade: 0.45, verniz: 0.4 }), 40); tampa.rotation.z = Math.PI / 2; g.add(em(tampa, l * 0.66, 0.32, 0));
    for (const r of [0.08, 0.16, 0.23]) { const a = malha(new THREE.TorusGeometry(r, 0.008, 6, 40), plastico('#c98f55')); a.rotation.y = Math.PI / 2; g.add(em(a, l * 0.68, 0.32, 0)); }
  }
  for (let k = 0; k < 6; k++) { const a = (k / 6) * Math.PI * 2; const r = capsula(0.025, 1.05, plastico('#6e3f20', { rugosidade: 0.6 })); r.rotation.z = Math.PI / 2; g.add(em(r, 0, 0.32 + Math.sin(a) * 0.31, Math.cos(a) * 0.31)); }
  const galho = cilindro(0.06, 0.08, 0.25, casca, 16); galho.rotation.z = -0.5; g.add(em(galho, 0.15, 0.68, 0.05));
  const folha = esc(bola(0.12, pl('#45c468'), 16), 1.4, 0.3, 0.7); folha.rotation.z = 0.4; g.add(em(folha, 0.28, 0.8, 0.05));
  g.rotation.y = -0.45;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Ateliê e loja

/** 🧽 Esponja com o lado verde de esfregar. */
function esponja() {
  const g = new THREE.Group();
  g.add(em(caixa(1.0, 0.42, 0.62, 0.12, plastico('#ffd23f', { rugosidade: 0.6, verniz: 0.2 })), 0, 0.21, 0));
  g.add(em(caixa(1.0, 0.14, 0.62, 0.06, plastico('#36b25a', { rugosidade: 0.7, verniz: 0.1 })), 0, 0.48, 0));
  const furo = plastico('#d9a51a', { rugosidade: 0.8, verniz: 0 });
  const pts = [[-0.3, 0.12], [0.05, 0.25], [0.32, 0.1], [-0.12, 0.3], [0.25, 0.32], [-0.38, 0.3], [0.0, 0.08]];
  for (const [x, y] of pts) g.add(em(esc(bola(0.045, furo, 12), 1, 1, 0.4), x, y, 0.315));
  for (const [x, z] of [[-0.48, 0.1], [-0.495, -0.12]]) g.add(em(esc(bola(0.04, furo, 12), 0.4, 1, 1), x, 0.22, z));
  g.add(em(faisca(0.22), 0.45, 0.75, 0.2), em(faisca(0.14), -0.42, 0.72, 0.1));
  for (const [x, r] of [[0.18, 0.1], [-0.1, 0.07]]) g.add(em(bola(r, vidro('#e0f4ff', 0.55), 20), x, 0.62 + r, 0.05));
  g.rotation.y = -0.4;
  return g;
}

/** 📿 Colar de contas coloridas (pendurado: âncora 'topo'). */
function colarDeContas() {
  const g = new THREE.Group();
  const cores = ['#ff3b4f', '#ff8a1c', '#ffc21a', '#36c26a', '#3d8bff', '#9a62ff'];
  const n = 22;
  for (let k = 0; k < n; k++) {
    const t = (k / n) * Math.PI * 2;
    const x = Math.sin(t) * 0.5, y = 1.0 - (1 - Math.cos(t)) * 0.55 - (1 - Math.cos(t)) ** 2 * 0.06;
    g.add(em(bola(0.075, brilho(cores[k % cores.length], { rugosidade: 0.15 }), 20), x, y, Math.cos(t) * 0.05));
  }
  g.add(tubo(curva([[0, -0.2, 0.05], [0, -0.32, 0.06]]), () => 0.03, ouro(), { seg: 4, radial: 8 }));
  g.add(em(bola(0.1, brilho('#ff5c9a', { rugosidade: 0.12 }), 20), 0, -0.4, 0.06));
  const borla = plastico('#ff5c9a', { rugosidade: 0.6, verniz: 0.2 });
  for (let k = 0; k < 6; k++) { const a = (k / 6) * Math.PI * 2; g.add(segmento([0, -0.48, 0.06], [Math.cos(a) * 0.05, -0.72, 0.06 + Math.sin(a) * 0.05], 0.02, borla)); }
  return g;
}

// ---------------------------------------------------------------------------------------------
// Piscina

/** 🛟 Boia salva-vidas listrada. */
function boia() {
  const g = new THREE.Group();
  for (let k = 0; k < 4; k++) {
    const t = malha(new THREE.TorusGeometry(0.5, 0.2, 24, 32, Math.PI / 2), brilho(k % 2 ? '#ffffff' : '#ff3b4f'));
    t.rotation.z = (k * Math.PI) / 2; g.add(t);
  }
  const corda = plastico('#ffe7b0', { rugosidade: 0.6 });
  for (let k = 0; k < 4; k++) {
    const a = (k / 4) * Math.PI * 2 + Math.PI / 4;
    const t = malha(new THREE.TorusGeometry(0.2, 0.022, 8, 24), corda); t.rotation.z = a; t.rotation.y = Math.PI / 2;
    t.position.set(Math.cos(a) * 0.5, Math.sin(a) * 0.5, 0); t.lookAt(0, 0, 0); t.rotateY(Math.PI / 2);
    g.add(t);
  }
  const w = grupo(g); g.rotation.x = -1.05; g.position.y = 0.2;
  return w;
}

/** ⛱ Guarda-sol fincado na areia. */
function guardaSol() {
  const g = new THREE.Group();
  const cores = ['#ff3b4f', '#ffffff', '#3d8bff', '#ffffff', '#ffc21a', '#ffffff', '#36c26a', '#ffffff'];
  const topo = new THREE.Group();
  cores.forEach((c, k) => {
    const s = malha(new THREE.SphereGeometry(0.85, 12, 16, (k / 8) * Math.PI * 2, Math.PI / 4, 0, Math.PI * 0.42), brilho(c, { rugosidade: 0.3 }));
    s.material.side = THREE.DoubleSide; s.scale.y = 0.6; topo.add(s);
  });
  for (let k = 0; k < 8; k++) { const a = (k / 8) * Math.PI * 2; topo.add(em(bola(0.04, brilho(cores[k]), 10), Math.sin(a) * 0.82, 0.6 * 0.85 * Math.cos(Math.PI * 0.42) - 0.02, Math.cos(a) * 0.82)); }
  topo.add(em(bola(0.06, ouro(), 12), 0, 0.52, 0));
  const haste = grupo(topo); topo.position.y = 1.1;
  haste.add(em(cilindro(0.035, 0.035, 1.6, metal('#f2f2f2'), 12), 0, 0.8, 0));
  haste.rotation.z = -0.22;
  g.add(em(haste, 0.1, 0, 0));
  const areia = esc(bola(0.55, massinha('#f6d58f'), 32), 1, 0.25, 0.8); g.add(em(areia, 0.1, 0, 0));
  g.add(em(esc(bola(0.12, brilho('#ff8fb0'), 16), 1, 0.6, 0.4), 0.42, 0.06, 0.25));
  return g;
}

/** 🤿 Máscara de mergulho com o snorkel. */
function mascaraDeMergulho() {
  const g = new THREE.Group();
  const azul = brilho('#2fa8ff');
  const aro = new THREE.Shape();
  aro.moveTo(-0.5, 0.25); aro.quadraticCurveTo(-0.55, -0.3, -0.15, -0.28); aro.quadraticCurveTo(0, -0.1, 0.15, -0.28);
  aro.quadraticCurveTo(0.55, -0.3, 0.5, 0.25); aro.quadraticCurveTo(0, 0.38, -0.5, 0.25);
  g.add(em(estufado(aro, azul, 0.18, 0.06), 0, 0.6, 0));
  const lente = estufado(aro, vidro('#d6f2ff', 0.55), 0.02, 0.02); lente.scale.set(0.82, 0.78, 1); g.add(em(lente, 0, 0.62, 0.14));
  g.add(em(esc(bola(0.09, luzinha('#ffffff'), 12), 1.6, 0.7, 0.3), -0.22, 0.75, 0.17));
  const tira = malha(new THREE.TorusGeometry(0.42, 0.04, 8, 40, Math.PI), plastico('#2b2b3a', { rugosidade: 0.5 }));
  tira.rotation.set(0, 0, 0); tira.rotation.x = -Math.PI / 2; tira.scale.set(1.2, 0.7, 1); g.add(em(tira, 0, 0.62, -0.05));
  g.add(tubo(curva([[0.62, 0.05, 0.02], [0.66, 0.5, 0], [0.62, 1.1, -0.05], [0.55, 1.35, -0.05]]), () => 0.065, brilho('#ffc21a'), { seg: 30, radial: 14 }));
  g.add(tubo(curva([[0.62, 0.05, 0.02], [0.45, -0.02, 0.1], [0.3, 0.05, 0.15]]), () => 0.065, brilho('#ffc21a'), { seg: 16, radial: 14 }));
  g.add(em(esc(bola(0.09, brilho('#ff8a1c'), 16), 1, 0.7, 1), 0.28, 0.06, 0.16));
  g.rotation.y = -0.25;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Parquinho

/** 🫧 Bolhas de sabão. */
function bolhas() {
  const g = new THREE.Group();
  const bolha = (r, x, y, z, cor) => {
    const b = new THREE.Group();
    b.add(bola(r, plastico(cor, { rugosidade: 0.03, verniz: 1, opacidade: 0.6, ambiente: 1.2, emissivo: cor }), 40));
    b.add(em(esc(bola(r * 0.22, luzinha('#ffffff'), 12), 1.3, 0.8, 0.4), -r * 0.4, r * 0.45, r * 0.75));
    b.add(em(bola(r * 0.08, luzinha('#ffffff'), 8), r * 0.45, -r * 0.4, r * 0.8));
    const aro = malha(new THREE.TorusGeometry(r * 0.98, r * 0.03, 8, 48), plastico('#ffffff', { opacidade: 0.5, rugosidade: 0.1 })); b.add(em(aro, 0, 0, 0));
    return em(b, x, y, z);
  };
  g.add(bolha(0.42, 0, 0.5, 0, '#9fe3ff'), bolha(0.26, 0.55, 1.0, 0.05, '#ffb3e6'), bolha(0.18, -0.5, 1.0, 0.1, '#c7b3ff'), bolha(0.12, 0.2, 1.3, 0.1, '#b3ffd9'));
  return g;
}

/** Roda de brinquedo deitada no eixo z. */
function roda(r, l, cor = '#2b2b3a', aro = '#ffc93c') {
  const g = new THREE.Group();
  const p = cilindro(r, r, l, plastico(cor, { rugosidade: 0.5, verniz: 0.3 }), 32); p.rotation.x = Math.PI / 2; g.add(p);
  const c = cilindro(r * 0.5, r * 0.5, l * 1.1, brilho(aro), 20); c.rotation.x = Math.PI / 2; g.add(c);
  return g;
}

/** 🛴 Patinete. */
function patinete() {
  const g = new THREE.Group();
  const azul = brilho('#3d8bff'), verm = brilho('#ff3b4f');
  g.add(em(caixa(1.2, 0.08, 0.32, 0.04, azul), 0, 0.2, 0));
  g.add(em(caixa(1.0, 0.02, 0.26, 0.01, plastico('#2b2b3a', { rugosidade: 0.8 })), -0.02, 0.245, 0));
  for (const x of [-0.6, 0.62]) g.add(em(roda(0.13, 0.1, '#2b2b3a', '#ffc93c'), x, 0.13, 0));
  g.add(segmento([0.6, 0.15, 0], [0.48, 1.35, 0], 0.045, metal()));
  g.add(segmento([0.62, 0.13, 0], [0.62, 0.3, 0], 0.03, metal()));
  g.add(em(caixa(0.12, 0.12, 0.1, 0.03, verm), 0.55, 0.62, 0));
  const guidao = capsula(0.035, 0.6, metal()); guidao.rotation.x = Math.PI / 2; g.add(em(guidao, 0.48, 1.35, 0));
  for (const z of [-0.36, 0.36]) { const p = capsula(0.06, 0.12, verm); p.rotation.x = Math.PI / 2; g.add(em(p, 0.48, 1.35, z)); }
  g.add(em(caixa(0.2, 0.06, 0.3, 0.02, verm), -0.62, 0.28, 0));
  g.rotation.y = -0.6;
  return g;
}

/** 🛹 Skate de bico levantado. */
function skate() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.moveTo(-0.95, 0.22); s.quadraticCurveTo(-0.8, 0.02, -0.6, 0.0); s.lineTo(0.6, 0.0); s.quadraticCurveTo(0.8, 0.02, 0.95, 0.22);
  s.lineTo(0.92, 0.27); s.quadraticCurveTo(0.78, 0.08, 0.6, 0.06); s.lineTo(-0.6, 0.06); s.quadraticCurveTo(-0.78, 0.08, -0.92, 0.27); s.closePath();
  const geo = new THREE.ExtrudeGeometry(s, { depth: 0.4, bevelEnabled: true, bevelThickness: 0.03, bevelSize: 0.025, bevelSegments: 4, curveSegments: 24 });
  geo.translate(0, 0, -0.2);
  const tabua = malha(geo, brilho('#9a62ff'));
  g.add(em(tabua, 0, 0.22, 0));
  g.add(em(caixa(1.15, 0.012, 0.38, 0.005, plastico('#2b2b3a', { rugosidade: 0.9 })), 0, 0.29, 0));
  g.add(em(rot(estrelaPlana(0.12, brilho('#ffc93c'), 0.01), -Math.PI / 2), 0, 0.3, 0));
  for (const x of [-0.5, 0.5]) {
    g.add(em(caixa(0.12, 0.06, 0.3, 0.02, metal()), x, 0.18, 0));
    for (const z of [-0.2, 0.2]) { const r = cilindro(0.075, 0.075, 0.09, brilho('#36c26a'), 24); r.rotation.x = Math.PI / 2; g.add(em(r, x, 0.085, z)); }
  }
  g.rotation.set(0.15, -0.5, 0);
  return g;
}

/** 🛝 Escorregador (o mesmo da casa). */
const escorregador = () => grupo(modelosDosItens.escorregador());

// ---------------------------------------------------------------------------------------------
// Horta, brinquedos, casa na árvore

/** 🫑 Pimentão verde de gomos. */
function pimentao() {
  const g = new THREE.Group();
  const geo = new THREE.SphereGeometry(0.5, 64, 48);
  const p = geo.attributes.position;
  for (let i = 0; i < p.count; i++) {
    const x = p.getX(i), y = p.getY(i), z = p.getZ(i);
    const a = Math.atan2(z, x);
    const h = y / 0.5;
    const gomo = 1 + 0.16 * Math.cos(4 * a) * (1 - h * h * 0.5);
    const base = h < -0.55 ? 0.85 + 0.15 * (1 + h) / 0.45 : 1;
    p.setXYZ(i, x * gomo * base, y * 1.25 - (h < 0 ? 0.04 * Math.cos(4 * a) * h * h : 0), z * gomo * base);
  }
  geo.computeVertexNormals();
  g.add(em(malha(geo, brilho('#2fae4a', { rugosidade: 0.15 })), 0, 0.62, 0));
  g.add(em(cilindro(0.16, 0.2, 0.06, pl('#1f7a32'), 24), 0, 1.22, 0));
  g.add(tubo(curva([[0, 1.22, 0], [0.02, 1.37, 0], [0.12, 1.45, 0.02]]), () => 0.05, pl('#2f8a3a'), { seg: 12, radial: 10 }));
  g.rotation.set(0.1, 0.3, 0.06);
  return g;
}

/** 🏎 Carro de corrida com aerofólio. */
function carroDeCorrida() {
  const g = new THREE.Group();
  const verm = brilho('#ff3b4f'), branco_ = brilho('#ffffff');
  const corpo = capsula(0.24, 1.3, verm); corpo.rotation.z = Math.PI / 2; corpo.scale.set(0.75, 1, 1.25); g.add(em(corpo, 0, 0.3, 0));
  const listra = capsula(0.06, 1.35, branco_); listra.rotation.z = Math.PI / 2; g.add(em(listra, 0.02, 0.48, 0));
  const bico = caixa(0.25, 0.06, 0.9, 0.03, verm); g.add(em(bico, 0.92, 0.16, 0));
  g.add(em(caixa(0.16, 0.06, 0.92, 0.03, verm), -0.82, 0.62, 0));
  for (const z of [-0.3, 0.3]) g.add(em(caixa(0.12, 0.28, 0.04, 0.02, verm), -0.78, 0.48, z));
  const cap = bola(0.17, brilho('#ffc21a'), 24); g.add(em(cap, -0.1, 0.56, 0));
  const visor = esc(bola(0.12, plastico('#2b2b3a', { rugosidade: 0.05, verniz: 1 }), 16), 0.7, 0.6, 1.1); g.add(em(visor, 0.02, 0.58, 0));
  for (const [x, r, l] of [[-0.55, 0.26, 0.24], [0.6, 0.2, 0.18]]) for (const z of [-0.45, 0.45]) g.add(em(roda(r, l, '#2b2b3a', '#d9dee8'), x, r, z));
  const n = cilindro(0.12, 0.12, 0.02, branco_, 24); n.rotation.x = Math.PI / 2; g.add(em(n, 0.25, 0.32, 0.31));
  g.rotation.y = -0.6;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Jardim de flores

/** ⛲ Fonte de dois andares com esguicho. */
function fonte() {
  const g = new THREE.Group();
  const pedra = pl('#e9e4f5', { rugosidade: 0.45, verniz: 0.4 });
  const agua = plastico('#4fc3ff', { rugosidade: 0.05, verniz: 1, opacidade: 0.75, emissivo: '#2a9fe0' });
  g.add(torneado([[0, 0], [0.8, 0], [0.85, 0.08], [0.82, 0.3], [0.72, 0.32], [0.7, 0.12], [0, 0.12]], pedra, 48, 24));
  g.add(em(cilindro(0.71, 0.71, 0.04, agua, 48), 0, 0.25, 0));
  g.add(em(cilindro(0.1, 0.14, 0.6, pedra, 24), 0, 0.5, 0));
  g.add(em(torneado([[0, 0], [0.38, 0.02], [0.45, 0.12], [0.42, 0.16], [0.35, 0.08], [0, 0.07]], pedra, 40, 16), 0, 0.78, 0));
  g.add(em(cilindro(0.37, 0.37, 0.03, agua, 40), 0, 0.88, 0));
  g.add(em(cilindro(0.04, 0.05, 0.22, pedra, 12), 0, 1.0, 0));
  const jato = plastico('#7fd6ff', { rugosidade: 0.05, verniz: 1, opacidade: 0.8, emissivo: '#4fb8ff' });
  for (let k = 0; k < 6; k++) {
    const a = (k / 6) * Math.PI * 2;
    g.add(tubo(curva([[0, 1.1, 0], [Math.cos(a) * 0.15, 1.3, Math.sin(a) * 0.15], [Math.cos(a) * 0.32, 1.15, Math.sin(a) * 0.32], [Math.cos(a) * 0.4, 0.92, Math.sin(a) * 0.4]]), (t) => 0.035 * (1 - t * 0.4), jato, { seg: 20, radial: 8 }));
  }
  for (let k = 0; k < 10; k++) { const a = k * 1.9; g.add(em(bola(0.035, jato, 8), Math.cos(a) * 0.55, 0.45 + (k % 3) * 0.12, Math.sin(a) * 0.55)); }
  g.add(em(bola(0.08, jato, 12), 0, 1.22, 0));
  return g;
}

// ---------------------------------------------------------------------------------------------
// Estúdio de música

/** 🎷 Saxofone dourado. */
function saxofone() {
  const g = new THREE.Group();
  const o = ouro('#ffc23a');
  const c = curva([[0.05, 1.7, 0], [0.0, 1.45, 0], [-0.02, 0.9, 0], [0.0, 0.35, 0], [0.18, 0.12, 0], [0.42, 0.25, 0], [0.5, 0.6, 0]]);
  g.add(tubo(c, (t) => 0.06 + t * 0.1, o, { seg: 60, radial: 20, pontas: false }));
  const sino = torneado([[0.16, 0], [0.2, 0.15], [0.32, 0.3], [0.33, 0.34], [0.3, 0.33]], o, 40, 24);
  sino.material = o; sino.material.side = THREE.DoubleSide; g.add(em(sino, 0.5, 0.55, 0));
  g.add(em(cilindro(0.3, 0.3, 0.02, plastico('#7a4a00', { rugosidade: 0.4 }), 40), 0.5, 0.86, 0));
  g.add(segmento([0.05, 1.7, 0], [0.32, 1.85, 0], 0.035, o));
  g.add(segmento([0.32, 1.85, 0], [0.45, 1.85, 0], 0.03, preto()));
  for (let k = 0; k < 6; k++) { const t = 0.3 + k * 0.08; const p = c.getPointAt(t); const k_ = cilindro(0.045, 0.045, 0.03, plastico('#fff5d6', { rugosidade: 0.2, verniz: 1 }), 16); k_.rotation.x = Math.PI / 2; g.add(em(k_, p.x, p.y, 0.06 + t * 0.1)); }
  g.rotation.set(0, -0.4, -0.12);
  return g;
}

/** 🪕 Banjo. */
function banjo() {
  const g = new THREE.Group();
  const corpo = cilindro(0.42, 0.42, 0.14, madeira('#c97c3a'), 48); corpo.rotation.x = Math.PI / 2; g.add(em(corpo, 0, 0.45, 0));
  const pele = cilindro(0.37, 0.37, 0.15, plastico('#fff8ec', { rugosidade: 0.55, verniz: 0.2 }), 48); pele.rotation.x = Math.PI / 2; g.add(em(pele, 0, 0.45, 0.005));
  const aro = malha(new THREE.TorusGeometry(0.4, 0.03, 10, 48), metal()); g.add(em(aro, 0, 0.45, 0.08));
  g.add(em(caixa(0.14, 1.15, 0.06, 0.02, madeira('#8a4f22')), 0, 1.3, 0.04));
  g.add(em(caixa(0.2, 0.28, 0.07, 0.04, madeira('#6e3a18')), 0, 1.98, 0.04));
  for (const y of [1.9, 2.04]) for (const l of [-1, 1]) { const p = cilindro(0.025, 0.025, 0.1, branco(), 10); p.rotation.z = Math.PI / 2; g.add(em(p, l * 0.14, y, 0.04)); }
  for (const x of [-0.045, -0.015, 0.015, 0.045]) g.add(segmento([x, 0.2, 0.09], [x, 1.86, 0.08], 0.006, metal('#f4f4f4')));
  g.add(em(caixa(0.18, 0.04, 0.05, 0.01, plastico('#3a2a1a')), 0, 0.4, 0.1));
  g.add(em(caixa(0.12, 0.03, 0.04, 0.01, branco()), 0, 0.3, 0.1));
  g.rotation.set(0, -0.3, -0.35);
  return g;
}

/** 🪘 Atabaque (tambor comprido de madeira). */
function atabaque() {
  const g = new THREE.Group();
  g.add(torneado([[0.22, 0], [0.3, 0.3], [0.38, 0.75], [0.4, 1.0], [0.38, 1.2], [0, 1.2]], madeira('#b8692f'), 48, 32));
  g.add(em(cilindro(0.39, 0.39, 0.03, plastico('#fff3dc', { rugosidade: 0.55, verniz: 0.2 }), 48), 0, 1.21, 0));
  g.add(em(anel(0.4, 0.035, brilho('#ef3b4f')), 0, 1.16, 0));
  g.add(em(anel(0.33, 0.035, brilho('#ef3b4f')), 0, 0.32, 0));
  const cord = plastico('#fff6e4', { rugosidade: 0.5 });
  for (let k = 0; k < 10; k++) {
    const a1 = (k / 10) * Math.PI * 2, a2 = ((k + 0.5) / 10) * Math.PI * 2, a3 = ((k + 1) / 10) * Math.PI * 2;
    g.add(segmento([Math.cos(a1) * 0.42, 1.14, Math.sin(a1) * 0.42], [Math.cos(a2) * 0.35, 0.34, Math.sin(a2) * 0.35], 0.014, cord));
    g.add(segmento([Math.cos(a2) * 0.35, 0.34, Math.sin(a2) * 0.35], [Math.cos(a3) * 0.42, 1.14, Math.sin(a3) * 0.42], 0.014, cord));
  }
  for (let k = 0; k < 5; k++) { const a = (k / 5) * Math.PI * 2; g.add(em(caixa(0.05, 0.1, 0.05, 0.02, ouro()), Math.cos(a) * 0.38, 0.7, Math.sin(a) * 0.38)); }
  g.rotation.x = 0.12;
  return g;
}

/** 📻 Rádio antigo com antena. */
function radio() {
  const g = new THREE.Group();
  g.add(em(caixa(1.2, 0.72, 0.4, 0.12, brilho('#ff8a5c')), 0, 0.36, 0));
  const grade = cilindro(0.24, 0.24, 0.03, plastico('#4a3a5a', { rugosidade: 0.5 }), 40); grade.rotation.x = Math.PI / 2; g.add(em(grade, -0.28, 0.36, 0.2));
  for (const r of [0.08, 0.16]) g.add(em(malha(new THREE.TorusGeometry(r, 0.015, 8, 32), plastico('#8a7a9a')), -0.28, 0.36, 0.22));
  g.add(em(caixa(0.4, 0.16, 0.03, 0.03, plastico('#fff5d6', { emissivo: '#ffe9a8', rugosidade: 0.2 })), 0.28, 0.46, 0.2));
  for (const x of [0.2, 0.3, 0.4]) g.add(em(caixa(0.01, 0.1, 0.035, 0.002, plastico('#7a4a2a')), x, 0.46, 0.2));
  for (const x of [0.18, 0.38]) { const b = cilindro(0.06, 0.06, 0.06, brilho('#ffc21a'), 20); b.rotation.x = Math.PI / 2; g.add(em(b, x, 0.22, 0.22)); }
  g.add(segmento([0.45, 0.7, -0.05], [0.75, 1.35, -0.1], 0.02, metal()));
  g.add(em(bola(0.04, metal(), 12), 0.75, 1.36, -0.1));
  const alca = malha(new THREE.TorusGeometry(0.3, 0.04, 10, 32, Math.PI), plastico('#4a3a5a', { rugosidade: 0.4, verniz: 0.5 })); g.add(em(alca, -0.05, 0.72, 0));
  for (const l of [-1, 1]) g.add(em(caixa(0.14, 0.06, 0.3, 0.03, plastico('#4a3a5a')), l * 0.45, 0.03, 0));
  g.rotation.y = -0.35;
  return g;
}

// ---------------------------------------------------------------------------------------------
// Varanda gourmet

/** 🍢 Espetinho (triângulo, bolinha e quadrado num palito). */
function espetinho() {
  const g = new THREE.Group();
  g.add(segmento([0, 0, 0], [0, 1.6, 0], 0.03, madeira('#e8b27a')));
  g.add(em(placa([[-0.28, -0.2], [0.28, -0.2], [0, 0.26]], plastico('#a58b78', { rugosidade: 0.45, verniz: 0.4 }), 0.12, 0.06), 0, 1.15, 0));
  const disco = cilindro(0.22, 0.22, 0.16, plastico('#fff4f6', { rugosidade: 0.45, verniz: 0.4 }), 40); disco.rotation.x = Math.PI / 2; g.add(em(disco, 0, 0.78, 0));
  const espiral = malha(new THREE.TorusGeometry(0.1, 0.03, 8, 30), pl('#ff6f9a')); g.add(em(espiral, 0, 0.78, 0.09));
  g.add(em(caixa(0.36, 0.36, 0.16, 0.06, plastico('#f2c47a', { rugosidade: 0.45, verniz: 0.45 })), 0, 0.38, 0));
  g.rotation.set(0, -0.35, 0.08);
  return g;
}

/** 🥞 Panquecas com manteiga e mel. */
function panquecas() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.75, 0.0], [0.82, 0.07], [0.7, 0.06], [0, 0.04]], branco(), 48, 16));
  const massa = plastico('#e7a14a', { rugosidade: 0.45, verniz: 0.35 });
  for (let k = 0; k < 4; k++) g.add(em(torneado([[0, 0.12], [0.5, 0.12], [0.55, 0.06], [0.5, 0], [0, 0]], massa, 48, 16), 0.0 + (k % 2) * 0.02, 0.06 + k * 0.12, 0));
  const mel = plastico('#b9661a', { rugosidade: 0.05, verniz: 1, opacidade: 0.92 });
  g.add(em(esc(cilindro(0.42, 0.44, 0.03, mel, 40), 1, 1, 1), 0, 0.55, 0));
  for (const [a, h] of [[0.4, 0.22], [1.5, 0.3], [2.7, 0.18], [4.0, 0.26], [5.2, 0.2]]) g.add(em(capsula(0.045, h, mel), Math.cos(a) * 0.5, 0.52 - h / 2, Math.sin(a) * 0.5));
  g.add(em(caixa(0.2, 0.12, 0.2, 0.04, plastico('#ffe680', { rugosidade: 0.3, verniz: 0.6 })), 0.02, 0.62, 0.02));
  g.rotation.set(0.1, 0.4, 0);
  return g;
}

// ---------------------------------------------------------------------------------------------
// Biblioteca

/** Livro fechado em pé (📗 📘 📙). */
function livro(cor, enfeite) {
  return () => {
    const g = new THREE.Group();
    const capa = brilho(cor);
    g.add(em(caixa(0.9, 1.2, 0.08, 0.04, capa), 0, 0.6, 0.15));
    g.add(em(caixa(0.9, 1.2, 0.08, 0.04, capa), 0, 0.6, -0.15));
    g.add(em(caixa(0.12, 1.2, 0.38, 0.05, capa), -0.42, 0.6, 0));
    g.add(em(caixa(0.84, 1.12, 0.24, 0.02, plastico('#fff8e8', { rugosidade: 0.6, verniz: 0.1 })), 0.02, 0.6, 0));
    for (let k = 0; k < 5; k++) g.add(em(caixa(0.005, 1.08, 0.2, 0.002, plastico('#eadfca')), 0.445, 0.6, -0.08 + k * 0.04));
    for (const y of [0.25, 0.95]) g.add(em(caixa(0.13, 0.05, 0.39, 0.02, ouro()), -0.42, y, 0));
    g.add(em(caixa(0.5, 0.3, 0.02, 0.05, plastico('#fff8e8', { rugosidade: 0.5 })), 0.02, 0.85, 0.195));
    const e = enfeite === 'estrela' ? estrelaPlana(0.14, ouro()) : enfeite === 'coracao' ? em(esc(bola(0.1, brilho('#ff5c9a'), 16), 1, 1, 0.4), 0, 0, 0) : em(esc(bola(0.12, ouro(), 20), 1, 1, 0.3), 0, 0, 0);
    g.add(em(grupo(e), 0.02, 0.45, 0.2));
    g.rotation.set(0, -0.5, 0.06);
    return g;
  };
}

// ---------------------------------------------------------------------------------------------
// Observatório

/** 🌕 Lua cheia com crateras. */
function luaCheia() {
  const g = new THREE.Group();
  const R = 0.55;
  g.add(em(bola(R, plastico('#ffe680', { rugosidade: 0.3, verniz: 0.7, emissivo: '#ffcf40' }), 64), 0, R, 0));
  const cr = plastico('#f2c94c', { rugosidade: 0.5, verniz: 0.3 });
  for (const [x, y, r] of [[-0.2, 0.2, 0.12], [0.18, -0.05, 0.09], [0.05, 0.32, 0.06], [-0.25, -0.2, 0.08], [0.3, 0.25, 0.05], [0.0, -0.3, 0.07]]) {
    const c = esc(bola(r, cr, 20), 1, 1, 0.3); naSuperficie(c, R, x, y, -r * 0.1); g.add(em(grupo(c), 0, R, 0));
  }
  return g;
}

// ---------------------------------------------------------------------------------------------
// Quadra de esportes

/** 🏈 Bola de rúgbi. */
function bolaDeRugbi() {
  const g = new THREE.Group();
  const b = bola(0.4, brilho('#a8572a', { rugosidade: 0.3 }), 48); b.scale.set(1.55, 1, 1); g.add(b);
  for (const l of [-1, 1]) { const f = malha(new THREE.TorusGeometry(0.33, 0.025, 8, 40), branco()); f.rotation.y = Math.PI / 2; g.add(em(f, l * 0.36, 0, 0)); }
  g.add(em(caixa(0.36, 0.03, 0.04, 0.01, branco()), 0, 0.395, 0));
  for (let k = 0; k < 5; k++) g.add(em(caixa(0.025, 0.03, 0.14, 0.01, branco()), -0.14 + k * 0.07, 0.4, 0));
  const w = grupo(g); g.rotation.set(0.5, 0.3, 0.35); g.position.y = 0.45;
  return w;
}

/** ⚾ Bola de beisebol com a costura. */
function bolaDeBeisebol() {
  const g = new THREE.Group();
  const R = 0.5;
  g.add(bola(R, plastico('#fbfaf6', { rugosidade: 0.35, verniz: 0.5 }), 64));
  const pts = [];
  for (let i = 0; i <= 80; i++) {
    const t = (i / 80) * Math.PI * 2;
    const v = V(Math.cos(t) * 0.75 + Math.cos(3 * t) * 0.25, Math.sin(t) * 0.75 - Math.sin(3 * t) * 0.25, Math.sin(2 * t) * 0.6).normalize().multiplyScalar(R * 1.005);
    pts.push(v);
  }
  const c = new THREE.CatmullRomCurve3(pts, true);
  const verm = plastico('#e8343f', { rugosidade: 0.4 });
  g.add(tubo(c, () => 0.012, verm, { seg: 160, radial: 6, pontas: false }));
  for (let k = 0; k < 48; k++) {
    const t = k / 48; const p = c.getPointAt(t); const tg = c.getTangentAt(t); const n = p.clone().normalize();
    const lado = tg.clone().cross(n).normalize().multiplyScalar(0.04);
    for (const s of [-1, 1]) g.add(segmento(p.clone().add(lado.clone().multiplyScalar(s)).add(tg.clone().multiplyScalar(-0.015)).toArray(), p.clone().add(tg.clone().multiplyScalar(0.015)).toArray(), 0.008, verm));
  }
  const w = grupo(g); g.rotation.set(0.4, 0.6, 0.2); g.position.y = R;
  return w;
}

/** 🏓 Raquete de pingue-pongue e bolinha. */
function pinguePongue() {
  const g = new THREE.Group();
  const r = new THREE.Group();
  const face = cilindro(0.42, 0.42, 0.05, brilho('#ef3b4f', { rugosidade: 0.4 }), 48); face.rotation.x = Math.PI / 2; r.add(em(face, 0, 0.75, 0));
  const tras = cilindro(0.42, 0.42, 0.02, plastico('#2b2b3a', { rugosidade: 0.5 }), 48); tras.rotation.x = Math.PI / 2; r.add(em(tras, 0, 0.75, -0.035));
  const borda = malha(new THREE.TorusGeometry(0.42, 0.03, 8, 48), madeira('#f2c98f')); r.add(em(borda, 0, 0.75, -0.01));
  r.add(em(caixa(0.14, 0.45, 0.1, 0.05, madeira('#d9975a')), 0, 0.22, -0.01));
  r.rotation.z = 0.35;
  g.add(em(r, -0.1, 0, 0));
  g.add(em(bola(0.12, brilho('#ffffff', { rugosidade: 0.25 }), 24), 0.45, 0.12, 0.25));
  g.rotation.y = -0.25;
  return g;
}

/** 🏸 Raquete e peteca. */
function peteca() {
  const g = new THREE.Group();
  const r = new THREE.Group();
  const aro = malha(new THREE.TorusGeometry(0.3, 0.03, 10, 48), brilho('#3d8bff')); aro.scale.set(0.8, 1, 1); r.add(em(aro, 0, 1.0, 0));
  const corda = plastico('#ffffff', { rugosidade: 0.4 });
  for (let k = -4; k <= 4; k++) { const x = k * 0.05; const h = Math.sqrt(Math.max(0, 1 - (x / 0.24) ** 2)) * 0.3; r.add(segmento([x, 1.0 - h, 0], [x, 1.0 + h, 0], 0.005, corda)); }
  for (let k = -5; k <= 5; k++) { const y = k * 0.05; const w = Math.sqrt(Math.max(0, 1 - (y / 0.3) ** 2)) * 0.24; r.add(segmento([-w, 1.0 + y, 0], [w, 1.0 + y, 0], 0.005, corda)); }
  r.add(segmento([0, 0.7, 0], [0, 0.25, 0], 0.025, metal()));
  r.add(em(capsula(0.05, 0.28, brilho('#ffc21a')), 0, 0.14, 0));
  r.rotation.z = 0.3;
  r.scale.setScalar(1.3);
  g.add(em(r, -0.3, 0, 0));
  const p = new THREE.Group();
  p.add(em(esc(bola(0.12, brilho('#ff3b4f'), 20), 1, 0.9, 1), 0, 0.1, 0));
  const pena = torneado([[0.1, 0.18], [0.16, 0.32], [0.24, 0.52], [0.23, 0.53], [0.15, 0.33], [0.09, 0.19]], plastico('#ffffff', { rugosidade: 0.5, verniz: 0.2 }), 32, 12);
  pena.material.side = THREE.DoubleSide; p.add(pena);
  p.rotation.z = -0.5;
  g.add(em(p, 0.45, 0.05, 0.15));
  g.rotation.y = -0.2;
  return g;
}

/** 🥅 Gol com rede. */
function gol() {
  const g = new THREE.Group();
  const trave = brilho('#ffffff');
  const L = 0.9, A = 0.75, F = 0.5;
  g.add(segmento([-L, 0, 0], [-L, A, 0], 0.05, trave), segmento([L, 0, 0], [L, A, 0], 0.05, trave), segmento([-L, A, 0], [L, A, 0], 0.05, trave));
  const rede = plastico('#7f8aa5', { rugosidade: 0.6 });
  for (const l of [-1, 1]) g.add(segmento([l * L, A, 0], [l * L, 0, -F], 0.02, rede), segmento([l * L, 0, 0], [l * L, 0, -F], 0.02, rede));
  g.add(segmento([-L, 0, -F], [L, 0, -F], 0.02, rede));
  for (let k = 1; k < 12; k++) { const x = -L + (k * 2 * L) / 12; g.add(segmento([x, A, 0], [x, 0, -F], 0.008, rede)); }
  for (let k = 1; k < 6; k++) { const t = k / 6; g.add(segmento([-L, A * (1 - t), -F * t], [L, A * (1 - t), -F * t], 0.008, rede)); }
  for (const l of [-1, 1]) for (let k = 1; k < 5; k++) { const t = k / 5; g.add(segmento([l * L, A * (1 - t), 0], [l * L, 0, -F * t], 0.006, rede)); }
  const b = grupo(modelosDosItens.bola()); b.scale.setScalar(0.3); g.add(em(b, 0.4, 0, 0.4));
  g.rotation.y = -0.35;
  return g;
}

/** 🛼 Patins de rodinha. */
function patinsDeRodinha() {
  const g = new THREE.Group();
  const rosa = brilho('#ff6fae');
  g.add(em(caixa(0.4, 0.62, 0.48, 0.14, rosa), 0, 0.52, -0.12));
  const bico = bola(0.24, rosa, 32); bico.scale.set(0.85, 0.7, 1.3); g.add(em(bico, 0, 0.36, 0.2));
  g.add(em(caixa(0.42, 0.06, 0.86, 0.03, plastico('#ffffff', { rugosidade: 0.4 })), 0, 0.23, 0.03));
  g.add(em(caixa(0.42, 0.1, 0.05, 0.03, plastico('#ffffff')), 0, 0.84, -0.12));
  for (let k = 0; k < 4; k++) g.add(em(caixa(0.3, 0.025, 0.04, 0.01, branco()), 0, 0.42 + k * 0.1, 0.13 - k * 0.0));
  g.add(em(caixa(0.3, 0.06, 0.6, 0.03, metal()), 0, 0.17, 0.03));
  for (const z of [-0.26, 0.32]) for (const x of [-0.18, 0.18]) { const r = cilindro(0.1, 0.1, 0.09, brilho('#ffc21a'), 24); r.rotation.z = Math.PI / 2; g.add(em(r, x, 0.1, z)); }
  g.add(em(esc(bola(0.07, plastico('#ff3b4f', { rugosidade: 0.5 }), 12), 1, 0.8, 1), 0, 0.09, 0.48));
  g.rotation.y = -0.8;
  return g;
}

/** 🏒 Taco de hóquei e disco. */
function tacoDeHoquei() {
  const g = new THREE.Group();
  const t = new THREE.Group();
  t.add(segmento([0, 0.15, 0], [-0.2, 1.6, 0], 0.045, madeira('#e0a060')));
  t.add(em(rot(caixa(0.6, 0.16, 0.07, 0.04, madeira('#e0a060')), 0, 0, 0.12), 0.25, 0.12, 0));
  for (const y of [1.25, 1.35, 1.45]) t.add(em(rot(cilindro(0.052, 0.052, 0.06, plastico('#2b2b3a', { rugosidade: 0.5 }), 16), 0, 0, 0.14), -0.2 + (1.6 - y) * 0.14 - 0.0, y, 0));
  t.add(em(rot(caixa(0.4, 0.09, 0.075, 0.02, plastico('#ffffff', { rugosidade: 0.5 })), 0, 0, 0.12), 0.3, 0.12, 0));
  g.add(t);
  g.add(em(cilindro(0.16, 0.16, 0.08, plastico('#2b2b3a', { rugosidade: 0.3, verniz: 0.8 }), 32), 0.65, 0.04, 0.25));
  g.rotation.y = -0.35;
  return g;
}

/** ⏱ Cronômetro. */
function cronometro() {
  const g = new THREE.Group();
  const corpo = cilindro(0.5, 0.5, 0.22, brilho('#ef3b4f'), 48); corpo.rotation.x = Math.PI / 2; g.add(corpo);
  const vid = cilindro(0.42, 0.42, 0.24, plastico('#ffffff', { rugosidade: 0.3, verniz: 0.8 }), 48); vid.rotation.x = Math.PI / 2; g.add(em(vid, 0, 0, 0.005));
  for (let k = 0; k < 12; k++) { const a = (k / 12) * Math.PI * 2; const m = caixa(0.025, k % 3 ? 0.05 : 0.09, 0.02, 0.008, plastico('#3a2766')); m.rotation.z = -a; g.add(em(m, Math.sin(a) * 0.34, Math.cos(a) * 0.34, 0.125)); }
  const pont = caixa(0.03, 0.3, 0.02, 0.01, brilho('#ef3b4f')); pont.rotation.z = -0.8; g.add(em(pont, Math.sin(0.8) * 0.13, Math.cos(0.8) * 0.13, 0.135));
  g.add(em(bola(0.04, brilho('#3a2766'), 12), 0, 0, 0.14));
  g.add(em(cilindro(0.08, 0.08, 0.12, metal(), 16), 0, 0.55, 0), em(cilindro(0.12, 0.12, 0.06, metal(), 16), 0, 0.63, 0));
  const lat = cilindro(0.05, 0.05, 0.1, metal(), 12); lat.rotation.z = -0.7; g.add(em(lat, 0.36, 0.4, 0));
  const w = grupo(g); g.rotation.set(-0.1, -0.3, 0); g.position.y = 0.5;
  return w;
}

// ---------------------------------------------------------------------------------------------
// Sala do aquário

/** 🪸 Coral de galhos numa pedra. */
function coral() {
  const g = new THREE.Group();
  const pedra = esc(bola(0.42, pl('#b9aed6', { rugosidade: 0.6, verniz: 0.2 }), 32), 1.2, 0.45, 1); g.add(pedra);
  const galho = (pts, r, cor) => g.add(tubo(curva(pts), (t) => r * (1 - t * 0.35), pl(cor), { seg: 20, radial: 12 }));
  const cores = ['#ff6f7f', '#ff8a5c', '#ff6fb5'];
  galho([[0, 0.1, 0], [0.05, 0.6, 0], [-0.05, 1.1, 0.05]], 0.09, cores[0]);
  galho([[0.02, 0.5, 0], [0.3, 0.75, 0.05], [0.4, 1.05, 0]], 0.065, cores[0]);
  galho([[0.0, 0.75, 0.02], [-0.3, 0.95, 0.05], [-0.38, 1.25, 0.05]], 0.06, cores[0]);
  galho([[0.3, 0.1, 0.1], [0.5, 0.4, 0.15], [0.62, 0.7, 0.1]], 0.07, cores[1]);
  galho([[-0.3, 0.1, 0.05], [-0.55, 0.35, 0.1], [-0.62, 0.62, 0.05]], 0.07, cores[2]);
  g.add(em(bola(0.06, pl('#ffc93c'), 12), 0.1, 0.4, 0.12), em(bola(0.05, pl('#ffc93c'), 12), -0.2, 0.7, 0.08));
  return g;
}

/** 🦞 Lagosta vermelha de frente, com as pinças. */
function lagosta() {
  const g = new THREE.Group();
  const v = brilho('#ef3b3b'), vc = brilho('#ff6a5a');
  const corpo = bola(0.32, v, 40); corpo.scale.set(1, 0.85, 1.2); g.add(em(corpo, 0, 0.3, 0));
  for (let k = 0; k < 3; k++) { const s = bola(0.25 - k * 0.04, vc, 28); s.scale.set(1, 0.6, 0.8); g.add(em(s, 0, 0.2 - k * 0.02, -0.35 - k * 0.18)); }
  for (const l of [-1, 0, 1]) { const f = bola(0.12, v, 16); f.scale.set(0.7, 0.25, 1.1); f.rotation.y = l * 0.4; g.add(em(f, l * 0.1, 0.1, -0.9)); }
  for (const l of [-1, 1]) {
    const haste = segmento([l * 0.1, 0.5, 0.25], [l * 0.13, 0.66, 0.3], 0.03, v); g.add(haste);
    g.add(em(olho(0.07), l * 0.13, 0.7, 0.33));
    g.add(tubo(curva([[l * 0.08, 0.42, 0.33], [l * 0.35, 0.9, 0.4], [l * 0.6, 1.2, 0.2]]), () => 0.012, v, { seg: 20, radial: 6 }));
    g.add(segmento([l * 0.25, 0.3, 0.2], [l * 0.5, 0.35, 0.35], 0.05, v));
    const pinca = new THREE.Group();
    const a = bola(0.17, v, 24); a.scale.set(0.8, 1.3, 0.7); pinca.add(em(a, 0, 0.12, 0));
    const b = bola(0.09, vc, 16); b.scale.set(0.7, 1.4, 0.6); b.rotation.z = -l * 0.45; pinca.add(em(b, l * 0.1, 0.32, 0));
    const c = bola(0.08, vc, 16); c.scale.set(0.7, 1.4, 0.6); c.rotation.z = l * 0.35; pinca.add(em(c, -l * 0.06, 0.33, 0));
    pinca.rotation.z = -l * 0.35;
    g.add(em(pinca, l * 0.58, 0.3, 0.38));
    for (let k = 0; k < 3; k++) g.add(segmento([l * 0.22, 0.2, 0.05 - k * 0.15], [l * 0.42, 0.02, 0.1 - k * 0.18], 0.025, vc));
  }
  for (const l of [-1, 1]) { const b = bochecha(0.06); g.add(em(b, l * 0.2, 0.32, 0.38)); }
  const s = sorriso(0.07, 0.015, 2.2, '#7d1f24'); g.add(em(s, 0, 0.3, 0.39));
  g.rotation.set(0.15, 0.25, 0);
  return g;
}

// ---------------------------------------------------------------------------------------------
// Cinema

/** Rolo de filme (disco com furos), de frente no eixo z. */
function rolo(r, cor = '#3a3a4a') {
  const g = new THREE.Group();
  const d = cilindro(r, r, 0.08, brilho(cor), 40); d.rotation.x = Math.PI / 2; g.add(d);
  for (let k = 0; k < 5; k++) { const a = (k / 5) * Math.PI * 2; const f = cilindro(r * 0.22, r * 0.22, 0.09, plastico('#c9cfdb', { rugosidade: 0.3 }), 16); f.rotation.x = Math.PI / 2; g.add(em(f, Math.cos(a) * r * 0.55, Math.sin(a) * r * 0.55, 0)); }
  const c = cilindro(r * 0.12, r * 0.12, 0.12, metal(), 12); c.rotation.x = Math.PI / 2; g.add(c);
  return g;
}

/** 🎥 Câmera de cinema com dois rolos. */
function cameraDeCinema() {
  const g = new THREE.Group();
  g.add(em(caixa(1.0, 0.55, 0.42, 0.1, brilho('#3a3a52')), 0, 0.5, 0));
  const lente = cilindro(0.16, 0.2, 0.35, brilho('#2b2b3a'), 32); lente.rotation.z = Math.PI / 2; g.add(em(lente, 0.65, 0.5, 0));
  const vid = cilindro(0.15, 0.15, 0.02, plastico('#9fd8ff', { rugosidade: 0.05, verniz: 1, emissivo: '#5fb8ff' }), 32); vid.rotation.z = Math.PI / 2; g.add(em(vid, 0.83, 0.5, 0));
  g.add(em(rolo(0.3, '#ef3b4f'), -0.25, 1.05, 0), em(rolo(0.24, '#ef3b4f'), 0.3, 1.0, 0));
  g.add(em(caixa(0.2, 0.12, 0.1, 0.03, brilho('#ffc21a')), -0.2, 0.5, 0.22));
  g.add(em(bola(0.04, luzinha('#ff3b3b'), 10), 0.3, 0.66, 0.21));
  for (const [x, z] of [[-0.25, 0.15], [0.2, 0.15], [0, -0.2]]) g.add(segmento([0, 0.22, 0], [x, 0, z], 0.03, metal()));
  g.rotation.y = -0.45;
  return g;
}

/** 📽 Projetor de filme com a luz acesa. */
function projetor() {
  const g = new THREE.Group();
  g.add(em(caixa(0.9, 0.42, 0.5, 0.12, brilho('#7c8aa8')), 0, 0.3, 0));
  const lente = cilindro(0.14, 0.14, 0.25, brilho('#3a3a52'), 32); lente.rotation.z = Math.PI / 2; g.add(em(lente, 0.55, 0.32, 0));
  const luz = cilindro(0.12, 0.12, 0.02, luzinha('#fff3b0'), 32); luz.rotation.z = Math.PI / 2; g.add(em(luz, 0.68, 0.32, 0));
  const facho = cilindro(0.13, 0.4, 0.6, plastico('#fff3b0', { opacidade: 0.25, emissivo: '#ffe680', rugosidade: 0.2 }), 32); facho.rotation.z = Math.PI / 2; g.add(em(facho, 0.99, 0.32, 0));
  const r1 = rolo(0.3, '#3a3a52'); r1.rotation.y = Math.PI / 2; g.add(em(r1, -0.15, 0.85, 0));
  const r2 = rolo(0.24, '#3a3a52'); r2.rotation.y = Math.PI / 2; g.add(em(r2, 0.28, 0.8, 0));
  for (const x of [-0.3, 0.3]) for (const z of [-0.18, 0.18]) g.add(em(cilindro(0.04, 0.05, 0.1, plastico('#2b2b3a'), 12), x, 0.05, z));
  g.add(segmento([-0.15, 0.85, 0], [0.28, 0.8, 0], 0.012, plastico('#2b2b3a')));
  g.rotation.y = -0.55;
  return g;
}

/** 🎞 Fita de filme enrolando. */
function roloDeFilme() {
  const g = new THREE.Group();
  const c = curva([[-0.6, 1.0, 0], [-0.1, 1.2, 0.1], [0.4, 0.95, 0.15], [0.55, 0.5, 0.1], [0.2, 0.3, 0.2], [-0.3, 0.45, 0.15], [-0.55, 0.15, 0.05]]);
  const fita = plastico('#3a3a52', { rugosidade: 0.3, verniz: 0.8 });
  const n = 26;
  for (let k = 0; k < n; k++) {
    const t = k / n, t1 = (k + 1) / n;
    const a = c.getPointAt(t), b = c.getPointAt(t1);
    const q = new THREE.Group();
    q.add(caixa(a.distanceTo(b) * 1.05, 0.34, 0.03, 0.01, fita));
    q.add(em(caixa(a.distanceTo(b) * 0.7, 0.2, 0.034, 0.01, plastico(['#9fd8ff', '#ffe680', '#ffb3d1'][k % 3], { rugosidade: 0.2, verniz: 0.8, emissivo: ['#5fb8ff', '#ffcf40', '#ff7fb0'][k % 3] })), 0, 0, 0));
    for (const y of [-0.135, 0.135]) q.add(em(caixa(0.03, 0.035, 0.036, 0.005, plastico('#ffffff')), 0, y, 0));
    q.position.copy(a).add(b).multiplyScalar(0.5);
    q.rotation.z = Math.atan2(b.y - a.y, b.x - a.x);
    q.rotation.y = -Math.atan2(b.z - a.z, Math.hypot(b.x - a.x, b.y - a.y));
    g.add(q);
  }
  return g;
}

// ---------------------------------------------------------------------------------------------
// Lago

/** 🦫 Castor com os dentões e o rabo achatado. */
function castor() {
  return bichinho({
    cor: '#a8673a', claro: '#e9c9a0', nariz: '#2a1a14', corFocinho: '#e9c9a0', giro: 0.35,
    orelhas: (cab, m, mc, R) => {
      for (const l of [-1, 1]) { const o = bola(R * 0.2, m, 16); o.scale.set(1, 1, 0.6); cab.add(em(o, l * R * 0.66, R * 0.66, -R * 0.1)); }
      const d = caixa(R * 0.28, R * 0.24, R * 0.06, R * 0.04, branco()); const gd = grupo(d); naSuperficie(gd, R, 0, -R * 0.5, R * 0.18); cab.add(gd);
      const linha = caixa(R * 0.012, R * 0.22, R * 0.07, 0.002, plastico('#d9d2c8')); const gl = grupo(linha); naSuperficie(gl, R, 0, -R * 0.5, R * 0.19); cab.add(gl);
      bigodes(cab, R);
    },
    extras: (g) => {
      const r = new THREE.Group();
      const cauda = bola(0.3, plastico('#5a3a2a', { rugosidade: 0.45, verniz: 0.4 }), 32); cauda.scale.set(0.75, 0.14, 1.25); r.add(cauda);
      for (let k = -2; k <= 2; k++) r.add(em(caixa(0.02, 0.05, 0.6, 0.01, plastico('#3e2618')), k * 0.08, 0.035, 0));
      r.rotation.x = -0.3;
      g.add(em(r, -0.05, 0.08, -0.65));
    },
  });
}

/** 🦦 Lontra segurando uma conchinha. */
function lontra() {
  return bichinho({
    cor: '#8a5a3a', claro: '#f2dcc0', nariz: '#2a1a14', corFocinho: '#f2dcc0', giro: -0.3,
    orelhas: (cab, m, mc, R) => {
      for (const l of [-1, 1]) { const o = bola(R * 0.16, m, 16); o.scale.set(1, 0.8, 0.6); cab.add(em(o, l * R * 0.78, R * 0.5, -R * 0.1)); }
      const rosto = bola(R * 0.7, mc, 32); rosto.scale.set(1.15, 0.8, 0.5); naSuperficie(rosto, R, 0, -R * 0.12, -R * 0.42); cab.add(rosto);
      bigodes(cab, R);
    },
    extras: (g, m) => {
      g.add(rabo([[0.3, 0.12, -0.3], [0.6, 0.06, -0.3], [0.85, 0.1, -0.15]], 0.09, m));
      const concha = new THREE.Group();
      const c = bola(0.14, pl('#ffb3c9'), 24); c.scale.set(1, 0.85, 0.45); concha.add(c);
      for (let k = -2; k <= 2; k++) { const s = capsula(0.012, 0.18, pl('#ff8fb0')); s.rotation.z = k * 0.3; concha.add(em(s, Math.sin(k * 0.3) * 0.04, 0, 0.06)); }
      g.add(em(concha, 0, 0.62, 0.42));
    },
  });
}

// ---------------------------------------------------------------------------------------------
// Laboratório

/** 🔬 Microscópio de brinquedo. */
function microscopio() {
  const g = new THREE.Group();
  const azul = brilho('#3d8bff'), bco = brilho('#ffffff');
  g.add(em(caixa(0.8, 0.14, 0.6, 0.06, azul), 0, 0.07, 0));
  g.add(tubo(curva([[-0.25, 0.1, -0.1], [-0.35, 0.6, -0.15], [-0.15, 1.05, -0.05]]), () => 0.09, bco, { seg: 24, radial: 16 }));
  g.add(em(caixa(0.5, 0.05, 0.4, 0.02, plastico('#2b2b3a', { rugosidade: 0.4 })), 0.08, 0.5, 0.05));
  g.add(em(caixa(0.3, 0.012, 0.12, 0.004, vidro('#d6f2ff', 0.7)), 0.08, 0.53, 0.05));
  const t = new THREE.Group();
  t.add(em(cilindro(0.11, 0.11, 0.6, bco, 32), 0, 0.3, 0));
  t.add(em(cilindro(0.08, 0.08, 0.18, preto(), 24), 0, 0.68, 0));
  t.add(em(cilindro(0.06, 0.04, 0.16, metal(), 20), 0, -0.06, 0));
  t.add(em(anel(0.12, 0.025, azul), 0, 0.3, 0));
  t.rotation.z = 0.45;
  g.add(em(t, 0.15, 0.62, 0.05));
  for (const l of [-1, 1]) { const b = cilindro(0.08, 0.08, 0.06, brilho('#ffc21a'), 20); b.rotation.x = Math.PI / 2; g.add(em(b, -0.3, 0.55, l * 0.13)); }
  g.add(em(esc(bola(0.07, luzinha('#fff3b0'), 12), 1, 0.4, 1), 0.08, 0.17, 0.05));
  g.rotation.y = -0.5;
  return g;
}

/** ⚗ Frasco redondo com líquido e o bico de lado. */
function frasco() {
  const g = new THREE.Group();
  const vid = vidro('#e6f6ff', 0.38);
  g.add(torneado([[0.15, 0], [0.4, 0.12], [0.5, 0.42], [0.4, 0.7], [0.12, 0.85], [0.11, 1.25], [0.13, 1.28]], vid, 48, 32));
  g.add(torneado([[0, 0.03], [0.15, 0.03], [0.36, 0.14], [0.45, 0.4], [0.44, 0.48], [0, 0.48]], plastico('#9a62ff', { rugosidade: 0.1, verniz: 1, emissivo: '#7a3aff', opacidade: 0.85 }), 48, 24));
  for (const [x, y, r] of [[0.08, 0.6, 0.06], [-0.12, 0.72, 0.045], [0.02, 0.95, 0.04], [-0.03, 1.4, 0.05], [0.06, 1.55, 0.035]]) g.add(em(bola(r, vidro('#d9c4ff', 0.6), 12), x, y, 0.05));
  g.add(em(cilindro(0.11, 0.1, 0.12, madeira('#c98f55'), 20), 0, 1.3, 0));
  g.add(tubo(curva([[0.3, 0.68, 0], [0.55, 0.85, 0], [0.8, 0.82, 0], [0.9, 0.7, 0]]), () => 0.045, vid, { seg: 20, radial: 10 }));
  g.add(em(faisca(0.2, '#ffffff'), 0.38, 1.25, 0.1));
  g.rotation.y = -0.4;
  return g;
}

/** 🧫 Placa de cultura com as bolinhas coloridas. */
function placaDePetri() {
  const g = new THREE.Group();
  g.add(torneado([[0, 0], [0.62, 0], [0.64, 0.02], [0.64, 0.16], [0.61, 0.16], [0.6, 0.03], [0, 0.03]], vidro('#e6f6ff', 0.45), 64, 16));
  g.add(em(cilindro(0.59, 0.59, 0.08, plastico('#b6e86a', { rugosidade: 0.25, verniz: 0.8, opacidade: 0.9 }), 64), 0, 0.07, 0));
  const cores = ['#ff6fae', '#ffc21a', '#9a62ff', '#3d8bff', '#ff8a1c'];
  for (let k = 0; k < 12; k++) { const a = k * 2.3, r = 0.12 + (k % 4) * 0.11; const b = bola(0.05 + (k % 3) * 0.02, pl(cores[k % 5]), 14); b.scale.y = 0.5; g.add(em(b, Math.cos(a) * r, 0.12, Math.sin(a) * r)); }
  const w = grupo(g); g.rotation.x = 0.55;
  return w;
}

/** 🧬 Fita de DNA (dupla hélice). */
function dna() {
  const g = new THREE.Group();
  const h = (fase) => { const pts = []; for (let i = 0; i <= 40; i++) { const t = i / 40; const a = t * Math.PI * 3 + fase; pts.push(V(Math.cos(a) * 0.32, t * 1.6, Math.sin(a) * 0.32)); } return new THREE.CatmullRomCurve3(pts); };
  g.add(tubo(h(0), () => 0.06, brilho('#3d8bff'), { seg: 80, radial: 14 }));
  g.add(tubo(h(Math.PI), () => 0.06, brilho('#ff5c9a'), { seg: 80, radial: 14 }));
  const cores = ['#ffc21a', '#36c26a', '#9a62ff', '#ff8a1c'];
  for (let k = 1; k < 12; k++) {
    const t = k / 12; const a = t * Math.PI * 3;
    const p1 = [Math.cos(a) * 0.32, t * 1.6, Math.sin(a) * 0.32], p2 = [Math.cos(a + Math.PI) * 0.32, t * 1.6, Math.sin(a + Math.PI) * 0.32];
    const m = [(p1[0] + p2[0]) / 2, t * 1.6, (p1[2] + p2[2]) / 2];
    g.add(segmento(p1, m, 0.03, pl(cores[k % 4])), segmento(m, p2, 0.03, pl(cores[(k + 1) % 4])));
  }
  g.rotation.set(0.1, 0, 0.12);
  return g;
}

/** 🌡 Termômetro com o líquido vermelho. */
function termometro() {
  const g = new THREE.Group();
  g.add(em(capsula(0.11, 1.1, vidro('#eef8ff', 0.5)), 0, 0.78, 0));
  g.add(em(bola(0.2, brilho('#ff3b4f'), 32), 0, 0.2, 0));
  g.add(em(capsula(0.05, 0.6, brilho('#ff3b4f')), 0, 0.6, 0));
  for (let k = 0; k < 8; k++) g.add(em(caixa(k % 2 ? 0.06 : 0.1, 0.015, 0.02, 0.005, plastico('#3a2766')), 0.11, 0.45 + k * 0.1, 0.06));
  g.add(em(caixa(0.36, 1.25, 0.06, 0.08, brilho('#ffffff')), 0, 0.82, -0.12));
  g.rotation.set(0, -0.25, 0.05);
  return g;
}

/** 📐 Esquadro transparente. */
function esquadro() {
  const g = new THREE.Group();
  const s = new THREE.Shape(); s.moveTo(0, 0); s.lineTo(1.2, 0); s.lineTo(0, 1.2); s.closePath();
  const h = new THREE.Path(); h.moveTo(0.22, 0.2); h.lineTo(0.68, 0.2); h.lineTo(0.22, 0.66); h.closePath(); s.holes.push(h);
  const geo = new THREE.ExtrudeGeometry(s, { depth: 0.06, bevelEnabled: true, bevelThickness: 0.02, bevelSize: 0.02, bevelSegments: 3 });
  g.add(malha(geo, plastico('#36c2a8', { rugosidade: 0.1, verniz: 1, opacidade: 0.75 })));
  for (let k = 1; k < 11; k++) g.add(em(caixa(0.012, k % 5 ? 0.06 : 0.11, 0.01, 0.003, plastico('#1d5a50')), k * 0.1, (k % 5 ? 0.06 : 0.11) / 2 + 0.01, 0.09));
  for (let k = 1; k < 11; k++) g.add(em(caixa(k % 5 ? 0.06 : 0.11, 0.012, 0.01, 0.003, plastico('#1d5a50')), (k % 5 ? 0.06 : 0.11) / 2 + 0.01, k * 0.1, 0.09));
  g.rotation.set(-0.1, -0.35, 0);
  g.position.set(-0.5, 0.02, 0);
  return grupo(g);
}

// ---------------------------------------------------------------------------------------------
// Salão de festas

/** 🪅 Piñata de burrinho listrado (pendurada). */
function pinata() {
  const g = new THREE.Group();
  const cores = ['#ff5c9a', '#ffc21a', '#36c2a8', '#9a62ff', '#ff8a1c'];
  const listrado = (l, a, p, x, y, z, n) => { for (let k = 0; k < n; k++) g.add(em(caixa(l, a / n * 1.05, p, Math.min(0.05, a / n / 2), pl(cores[k % 5], { rugosidade: 0.55, verniz: 0.2 })), x, y - a / 2 + (k + 0.5) * a / n, z)); };
  listrado(0.95, 0.5, 0.42, 0, 0.75, 0, 5);
  for (const x of [-0.32, 0.32]) for (const z of [-0.12, 0.12]) listrado(0.12, 0.42, 0.12, x, 0.29, z, 3);
  listrado(0.2, 0.5, 0.22, 0.42, 1.15, 0, 4);
  listrado(0.42, 0.24, 0.26, 0.58, 1.42, 0, 3);
  for (const z of [-0.08, 0.08]) { const o = capsula(0.05, 0.16, pl('#ff5c9a')); o.rotation.z = -0.3; g.add(em(o, 0.52, 1.66, z)); }
  for (const z of [-0.135, 0.135]) g.add(em(olho(0.04), 0.66, 1.46, z));
  for (let k = 0; k < 5; k++) g.add(segmento([-0.47, 0.85, 0], [-0.62, 0.5 - k * 0.04, -0.08 + k * 0.04], 0.025, pl(cores[k])));
  g.add(segmento([0.05, 1.0, 0], [0.05, 1.7, 0], 0.012, plastico('#f2f2f2')));
  g.rotation.y = 0.45;
  return g;
}

/** 🪩 Globo de luz espelhado. */
function globoDeLuz() {
  const g = new THREE.Group();
  const geo = new THREE.SphereGeometry(0.55, 18, 12);
  const m = new THREE.MeshPhysicalMaterial({ color: '#dfe6f5', metalness: 0.9, roughness: 0.18, clearcoat: 1, flatShading: true, envMapIntensity: 1.2 });
  g.add(em(malha(geo, m), 0, 0.55, 0));
  const cores = ['#ff5c9a', '#3d8bff', '#ffc21a', '#36c26a', '#9a62ff'];
  for (let k = 0; k < 10; k++) { const a = k * 1.3, y = -0.35 + (k % 5) * 0.16; const q = caixa(0.08, 0.08, 0.02, 0.01, luzinha(cores[k % 5])); const gq = grupo(q); naSuperficie(gq, 0.55, Math.cos(a) * 0.35, y, 0.0); g.add(em(gq, 0, 0.55, 0)); }
  g.add(em(cilindro(0.06, 0.06, 0.08, metal(), 12), 0, 1.13, 0));
  g.add(segmento([0, 1.15, 0], [0, 1.5, 0], 0.012, metal()));
  g.add(em(faisca(0.25), 0.55, 1.0, 0.2), em(faisca(0.18), -0.6, 0.35, 0.2));
  return g;
}

/** 🎇 Estrelinha de festa (faíscas saindo de um ponto). */
function estrelinhaDeFesta() {
  const g = new THREE.Group();
  const c = V(0, 0.9, 0);
  const cores = ['#ffc21a', '#ff8a1c', '#ff5c9a', '#ffe680'];
  for (let k = 0; k < 16; k++) {
    const a = (k / 16) * Math.PI * 2;
    const r = k % 2 ? 0.5 : 0.7;
    const p = V(Math.cos(a) * r, 0.9 + Math.sin(a) * r, (k % 3 - 1) * 0.15);
    g.add(segmento(c.toArray(), p.toArray(), 0.02, luzinha(cores[k % 4])));
    g.add(em(bola(0.045, luzinha(cores[(k + 1) % 4]), 10), p.x, p.y, p.z));
  }
  g.add(em(bola(0.1, luzinha('#fff6c9'), 16), 0, 0.9, 0));
  g.add(segmento([0, 0.85, 0], [0.05, 0, 0.05], 0.025, metal('#9aa3b5')));
  for (const [x, y] of [[0.55, 1.55], [-0.6, 1.4], [0.7, 0.4]]) g.add(em(faisca(0.2, '#fff3b0'), x, y, 0.1));
  return g;
}

/** 🥳 Chapéu de festa de bolinhas com pompom. */
function chapeuDeFesta() {
  const g = new THREE.Group();
  const R = 0.42, H = 1.05;
  g.add(torneado([[0, 0.02], [R * 0.98, 0.02], [R, 0.06], [R * 0.5, H * 0.52], [0.04, H], [0, H]], brilho('#9a62ff'), 48, 32));
  const cores = ['#ffc21a', '#ff5c9a', '#36c2a8'];
  for (let k = 0; k < 16; k++) {
    const t = 0.12 + (k % 4) * 0.2; const a = k * 1.7;
    const r = R * (1 - t) * 1.0 + 0.012;
    g.add(em(bola(0.04 + (1 - t) * 0.02, pl(cores[k % 3]), 12), Math.cos(a) * r, 0.06 + t * (H - 0.06), Math.sin(a) * r));
  }
  g.add(em(anel(R, 0.05, brilho('#ffc21a')), 0, 0.05, 0));
  const pom = plastico('#ff5c9a', { rugosidade: 0.75, verniz: 0.1 });
  for (const [x, y, z] of [[0, 0, 0], [0.07, 0.05, 0], [-0.07, 0.04, 0.02], [0, 0.08, -0.05], [0.02, 0.03, 0.08], [-0.03, -0.04, -0.06]]) g.add(em(bola(0.08, pom, 14), x, H + 0.05 + y, z));
  for (const l of [-1, 1]) g.add(em(faisca(0.18, '#ffe680'), l * 0.55, 0.8 + l * 0.2, 0.1));
  g.rotation.set(0.1, 0, -0.12);
  return g;
}

// ---------------------------------------------------------------------------------------------

export const modelosDaFase9b = {
  lanterna_de_papel: lanternaDePapel, almofada_coracao: almofadaCoracao, sapatilha,
  frisbee, poodle, novelo,
  margarida, lotus, carinha_rindo: carinha('rindo'), bola_de_confete: bolaDeConfete,
  bolinha_marrom: bolinha('#9a5a32'), pinheiro, casinhas, ninho_vazio: ninho(false),
  carinha_abraco: carinha('abraco'), carinha_feliz: carinha('feliz'), marcador, montanha_sol: montanhaSol,
  quadrado_marrom: quadradoMarrom, tronco, carinha_preocupada: carinha('preocupada'),
  esponja, colar_de_contas: colarDeContas, bolinha_branca: bolinha('#f3f1f7', { ambiente: 0.35 }),
  boia, guarda_sol: guardaSol, mascara_de_mergulho: mascaraDeMergulho,
  bolhas, patinete, skate, escorregador,
  pimentao, carro_de_corrida: carroDeCorrida, ninho: ninho(true), hibisco, fonte,
  saxofone, banjo, atabaque, radio, espetinho, panquecas,
  livro_verde: livro('#36b25a', 'estrela'), livro_azul: livro('#3d8bff', 'bola'), livro_laranja: livro('#ff8a1c', 'coracao'),
  lua_cheia: luaCheia,
  porco_da_fazenda: F.porco, pintinho_da_fazenda: F.pintinho, cavalo_da_fazenda: F.cavalo,
  bola_de_rugbi: bolaDeRugbi, bola_de_beisebol: bolaDeBeisebol, pingue_pongue: pinguePongue, peteca, gol,
  patins_de_rodinha: patinsDeRodinha, taco_de_hoquei: tacoDeHoquei, cronometro,
  coral, lagosta, camera_de_cinema: cameraDeCinema, projetor, rolo_de_filme: roloDeFilme,
  castor, lontra,
  microscopio, frasco, placa_de_petri: placaDePetri, dna, termometro, esquadro,
  pinata, globo_de_luz: globoDeLuz, estrelinha_de_festa: estrelinhaDeFesta, chapeu_de_festa: chapeuDeFesta,
};
