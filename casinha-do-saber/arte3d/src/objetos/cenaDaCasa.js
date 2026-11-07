// Diorama da casa de fora (PRD visual 3D, Fase 3): a ilha de grama, as etapas da construção e as outras
// estruturas (janelas, escada, chaminé, cerca, caminho, laguinho), em camadas na mesma câmera ('casa' em
// cameras.js), para o app empilhar por cima do céu em degradê e por baixo dos objetos que a criança arrasta.
//
// Mundo e cena do app: a câmera olha na horizontal, de (0, YC, D), com deslocamento de lente. O plano z = 0
// (a frente da casa) cai exatamente nas frações da cena de CenaDaCasa.kt: x da largura (0 a 1) e y da altura
// (0 a 1, altura = 1,15 x largura). Nesse plano, a cena tem [larguraMundo] x [alturaMundo] unidades e o topo
// da imagem está em y = [topo]. A casa ocupa a mesma planta do app (Planta: de 0,10 a 0,90 na largura, de
// 0,30 a 0,78 na altura, telhado de (0,04; 0,315) a (0,5; 0,075)); o que fica mais para trás (o fundo dos
// cômodos, as janelas, a porta) é posto com noPlano() para cair na fração certa da tela.
//
// Cada camada é a cena com papéis por parte, como nos personagens:
//   ver     aparece;
//   sombra  não aparece, mas faz sombra (o piso de cima escurece a parede do térreo);
//   capta   não aparece, mas recebe sombra (a casa faz sombra na grama, o telhado na parede);
//   tapa    não aparece, mas esconde o que fica atrás dela (alfa zero);
//   fundo   não aparece nem apaga, mas a sombra captada atrás dela não aparece (só profundidade).
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { arvore } from './arvore.js';
import { arbusto } from './arbusto.js';
import { flor } from './flor.js';

// ---------------------------------------------------------------------------------------------
// Mundo

export const CENA = {
  larguraMundo: 10,
  alturaMundo: 11.5,
  /** y do topo da imagem no plano z = 0 (a base da casa, fração 0,78, fica em y = 0). */
  topo: 0.78 * 11.5,
  /** Distância da câmera ao plano da frente da casa e altura dos olhos (de um pouco acima da casa). */
  D: 22,
  YC: 7,
  /** Altura da grama. */
  G: -0.45,
  /** Ponto de apoio de todas as camadas: o meio da base da frente da casa. */
  apoio: [0, -0.15, 0],
};

/** Quadro da cena, em dp (o mesmo para todas as camadas; o chão estende para baixo). */
export const QUADRO_CENA = { largura: 400, altura: 460 };
/** Quanto o chão e o caminho continuam para baixo da cena (o jardim de baixo das telas altas), em dp. */
export const EXTRA_DO_CHAO = 200;

/** Ponto do mundo na profundidade [z] que cai na fração (fx, fy) da cena. */
export function noPlano(fx, fy, z = 0) {
  const xi = (fx - 0.5) * CENA.larguraMundo, yi = CENA.topo - fy * CENA.alturaMundo;
  const s = (CENA.D - z) / CENA.D;
  return new THREE.Vector3(xi * s, CENA.YC + (yi - CENA.YC) * s, z);
}

/** Ponto da grama (altura [y]) que cai na fração (fx, fy) da cena. */
export function noChao(fx, fy, y = CENA.G) {
  const xi = (fx - 0.5) * CENA.larguraMundo, yi = CENA.topo - fy * CENA.alturaMundo;
  const dz = ((y - CENA.YC) * CENA.D) / (yi - CENA.YC);
  return new THREE.Vector3((xi * dz) / CENA.D, y, CENA.D - dz);
}

/** Escala de uma medida de tela (fração da largura) para a profundidade [z]. */
const larguraEm = (f, z) => f * CENA.larguraMundo * ((CENA.D - z) / CENA.D);
const alturaEm = (f, z) => f * CENA.alturaMundo * ((CENA.D - z) / CENA.D);

/** Gerador pseudoaleatório fixo (as camadas saem iguais a cada render). */
function sorteio(semente) {
  let s = semente >>> 0;
  return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; };
}

// ---------------------------------------------------------------------------------------------
// Medidas da casa (frações da Planta do app no plano z = 0)

const P = 2.6; // profundidade
const XL = 4.0; // meia largura por fora (0,10 a 0,90)
const ESP = 0.24; // espessura das paredes dos lados
const Y0 = CENA.apoio[1]; // piso do térreo
const Y1 = CENA.topo - 0.545 * CENA.alturaMundo; // piso de cima: a faixa do piso (de 0,506 a 0,545) contém o pé dos móveis (0,528)
const YT = CENA.topo - 0.30 * CENA.alturaMundo; // topo das paredes (TOPO)
const ZF = -P + 0.12; // face da parede do fundo
const X_DIV_CIMA = (0.52 - 0.5) * CENA.larguraMundo;
const X_DIV_BAIXO = (0.58 - 0.5) * CENA.larguraMundo;
const BEIRAL = { x: (0.04 - 0.5) * CENA.larguraMundo, y: CENA.topo - 0.315 * CENA.alturaMundo };
const CUMEEIRA = { x: 0, y: CENA.topo - 0.075 * CENA.alturaMundo };

// ---------------------------------------------------------------------------------------------
// Materiais

const mat = {
  parede: () => plastico('#fff4e0', { rugosidade: 0.62, verniz: 0.18 }),
  piso: () => plastico('#e3a96a', { rugosidade: 0.5, verniz: 0.35 }),
  base: () => plastico('#ebbd84', { rugosidade: 0.55, verniz: 0.3 }),
  madeira: () => plastico('#d9975a', { rugosidade: 0.5, verniz: 0.35 }),
  madeiraClara: () => plastico('#f0bf86', { rugosidade: 0.5, verniz: 0.35 }),
  /** Fundo dos cômodos: neutro claro, o app tinge com a cor de cada cômodo (Comodo.cores). */
  neutro: () => plastico('#efefef', { rugosidade: 0.8, verniz: 0.02, ambiente: 0.45 }),
  grama: () => plastico('#5cc23c', { rugosidade: 0.75, verniz: 0.06, ambiente: 0.45 }),
  gramaEscura: () => plastico('#47a834', { rugosidade: 0.75, verniz: 0.05, ambiente: 0.45 }),
  terra: () => massinha('#b9814b'),
  branco: () => plastico('#ffffff', { rugosidade: 0.3, verniz: 0.6 }),
  vidro: () => plastico('#8fd3ff', { rugosidade: 0.06, verniz: 1, emissivo: '#bfe6ff', ambiente: 0.8 }),
  vidroNoite: () => plastico('#ffd77a', { rugosidade: 0.2, verniz: 0.6, emissivo: '#ffcf5a', ambiente: 0.3 }),
  tijolo: () => massinha('#c95a3e'),
  pedra: () => plastico('#d8cdb8', { rugosidade: 0.65, verniz: 0.15 }),
};

const matTapa = new THREE.MeshBasicMaterial({
  color: 0x000000, transparent: true, opacity: 0, blending: THREE.NoBlending, depthWrite: true, toneMapped: false,
});
const matSoSombra = new THREE.MeshBasicMaterial({ colorWrite: false, depthWrite: false });
/** Só profundidade: o que capta sombra esconde a sombra que cairia atrás dele (sem escurecer duas vezes). */
const matSoFundo = new THREE.MeshBasicMaterial({ colorWrite: false, depthWrite: true });
const matCapta = () => new THREE.ShadowMaterial({ color: new THREE.Color('#2e1840'), opacity: 0.26, depthWrite: false });

function comPapel(obj, papel) {
  obj.traverse((o) => {
    if (!o.isMesh) return;
    if (papel === 'sombra') { o.material = matSoSombra; o.castShadow = true; o.receiveShadow = false; }
    if (papel === 'capta') { o.material = matCapta(); o.castShadow = false; o.receiveShadow = true; o.renderOrder = 2; }
    if (papel === 'fundo') { o.material = matSoFundo; o.castShadow = false; o.receiveShadow = false; o.renderOrder = -1; }
    if (papel === 'tapa') { o.material = matTapa; o.castShadow = false; o.receiveShadow = false; o.renderOrder = 1; }
  });
  return obj;
}

/** Monta uma camada a partir de listas de objetos (já posicionados no mundo). */
export function camadaDaCena({ ver = [], sombra = [], capta = [], tapa = [], fundo = [] }) {
  const g = new THREE.Group();
  for (const o of ver) g.add(o);
  for (const o of sombra) g.add(comPapel(o, 'sombra'));
  for (const o of capta) { g.add(comPapel(o.clone(true), 'fundo')); g.add(comPapel(o, 'capta')); }
  for (const o of tapa) g.add(comPapel(o, 'tapa'));
  for (const o of fundo) g.add(comPapel(o, 'fundo'));
  return g;
}

const grupo = (...filhos) => { const g = new THREE.Group(); for (const f of filhos) if (f) g.add(f); return g; };

// ---------------------------------------------------------------------------------------------
// A ilha

/** Disco de grama enorme: o horizonte curvo atrás da casa e o chão que desce até o jardim de baixo. */
function discoDeGrama() {
  const g = new THREE.Group();
  const R = 17, cz = -5;
  const topo = cilindro(R, R, 1.2, mat.grama(), 128);
  em(topo, 0, CENA.G - 0.6, cz);
  g.add(topo);
  const borda = malha(new THREE.TorusGeometry(R, 0.3, 16, 160), mat.grama());
  borda.rotation.x = Math.PI / 2;
  em(borda, 0, CENA.G - 0.3, cz);
  g.add(borda);
  // morrinhos no fundo (o horizonte ondulado da maquete)
  for (const [x, z, r, a] of [[-10.5, -14, 5.2, 0.42], [-3.5, -19, 5.5, 0.3], [6, -18, 6, 0.36], [12.5, -12, 4.6, 0.45], [-15, -6, 4, 0.4]]) {
    const m = bola(r, mat.grama(), 64);
    m.scale.set(1, a, 0.8);
    em(m, x, CENA.G - 0.1, z);
    g.add(m);
  }
  return g;
}

/** Tufinhos de grama e florzinhas espalhados longe dos lugares dos objetos do app. */
function tufos(semente, { flores = true, longeDaCasa = true } = {}) {
  const g = new THREE.Group();
  const r = sorteio(semente);
  const cores = ['#ff6fae', '#ffd23f', '#b57bff', '#ff8a5c', '#ffffff'];
  for (let i = 0; i < 70; i++) {
    const x = (r() - 0.5) * 26, z = -16 + r() * 22;
    if (longeDaCasa && Math.abs(x) < 5.6 && z > -4.2) continue; // casa, caminho e objetos da frente
    if (z > 2.5 && Math.abs(x) < 9) continue;
    const t = bola(0.12 + r() * 0.1, mat.gramaEscura(), 16);
    t.scale.set(1.6, 0.45, 1);
    em(t, x, CENA.G + 0.02, z);
    g.add(t);
    if (flores && i % 3 === 0) g.add(em(flor(cores[i % cores.length], 1.3 + r() * 0.5), x + 0.2, CENA.G, z + 0.1));
  }
  return g;
}

/** Árvores e arbustos dos lados da casa (como na maquete 1), longe do lugar da árvore do app (0,07; 0,95). */
function arvoresDosLados() {
  const g = new THREE.Group();
  const a1 = noChao(-0.03, 0.66); g.add(em(arvore({ s: 1.9 }), a1.x, CENA.G, a1.z));
  const a2 = noChao(1.04, 0.64); g.add(em(arvore({ copa: '#4fb84a', s: 2.1 }), a2.x, CENA.G, a2.z));
  const a3 = noChao(0.94, 0.56); g.add(em(arvore({ copa: '#66c552', s: 1.4 }), a3.x, CENA.G, a3.z));
  for (const [fx, fy, s, cor] of [[0.02, 0.775, 1.5, '#4fae3e'], [0.985, 0.77, 1.6, '#45a338'], [0.1, 0.6, 1.2, '#5cb948'], [0.91, 0.62, 1.1, '#4fae3e']]) {
    const p = noChao(fx, fy);
    g.add(em(arbusto(cor, s), p.x, CENA.G, p.z));
  }
  return g;
}

/** Base de madeira clara onde a casa assenta (o terreno pronto para construir). */
function baseDaCasa() {
  const g = new THREE.Group();
  g.add(em(caixa(2 * XL + 0.5, 0.46, P + 0.5, 0.16, mat.base()), 0, Y0 - 0.23, -P / 2 + 0.05));
  // degrauzinho na frente, para o caminho chegar na casa
  g.add(em(caixa(1.1, 0.24, 0.5, 0.1, mat.base()), noPlano(0.305, 0.8, 0.45).x, CENA.G + 0.1, 0.45));
  return g;
}

/** Terra batida do lote vazio, a placa (o app escreve "Minha casa vai ficar aqui!") e os tijolinhos. */
function loteVazio() {
  const g = new THREE.Group();
  const lote = caixa(2 * XL + 0.6, 0.2, P + 1.4, 0.1, mat.terra());
  em(lote, 0, CENA.G + 0.02, -P / 2 + 0.3);
  g.add(lote);
  // placa: tábua em (0,36 a 0,64; 0,62 a 0,71), poste até a grama em 0,82
  const pe = noChao(0.501, 0.82);
  const zP = pe.z;
  const topoT = noPlano(0.5, 0.62, zP), baseT = noPlano(0.5, 0.71, zP);
  const larg = larguraEm(0.28, zP), alt = topoT.y - baseT.y;
  g.add(em(caixa(0.14, topoT.y - CENA.G, 0.14, 0.05, mat.madeira()), 0, (topoT.y + CENA.G) / 2 - 0.1, zP - 0.12));
  g.add(em(caixa(larg + 0.12, alt + 0.12, 0.16, 0.1, mat.madeira()), pe.x, (topoT.y + baseT.y) / 2, zP));
  g.add(em(caixa(larg - 0.06, alt - 0.06, 0.08, 0.06, plastico('#f6dcaa', { rugosidade: 0.6, verniz: 0.1 })), pe.x, (topoT.y + baseT.y) / 2, zP + 0.07));
  // tijolinhos esperando a obra, em (0,70 a 0,86; 0,90)
  const t = noChao(0.78, 0.905);
  const tij = (x, y, z, r = 0) => { const b = caixa(0.62, 0.3, 0.34, 0.06, mat.tijolo()); b.rotation.y = r; return em(b, x, y, z); };
  g.add(tij(t.x - 0.7, CENA.G + 0.15, t.z, 0.05), tij(t.x, CENA.G + 0.15, t.z + 0.05, -0.04), tij(t.x + 0.7, CENA.G + 0.15, t.z, 0.08));
  g.add(tij(t.x - 0.35, CENA.G + 0.45, t.z + 0.02, 0.1), tij(t.x + 0.35, CENA.G + 0.45, t.z, -0.06));
  g.add(tij(t.x, CENA.G + 0.75, t.z + 0.02, 0.02));
  return g;
}

// ---------------------------------------------------------------------------------------------
// A casa

/** Paredes dos lados, pisos, teto e as divisórias dos cômodos (sem o fundo dos cômodos, que o app tinge). */
export function estruturaDasParedes() {
  const g = new THREE.Group();
  const h = YT - Y0;
  for (const lado of [-1, 1]) g.add(em(caixa(ESP, h, P, 0.09, mat.parede()), lado * (XL - ESP / 2), Y0 + h / 2, -P / 2));
  g.add(em(caixa(2 * XL - 0.08, 0.26, P - 0.02, 0.07, mat.piso()), 0, Y1 - 0.13, -P / 2));
  g.add(em(caixa(2 * XL + 0.04, 0.28, P + 0.04, 0.1, mat.parede()), 0, YT - 0.14, -P / 2));
  g.add(em(caixa(0.16, YT - 0.28 - Y1, P - 0.1, 0.05, mat.parede()), X_DIV_CIMA, (Y1 + YT - 0.28) / 2, -P / 2 - 0.05));
  g.add(em(caixa(0.16, Y1 - 0.26 - Y0, P - 0.1, 0.05, mat.parede()), X_DIV_BAIXO, (Y0 + Y1 - 0.26) / 2, -P / 2 - 0.05));
  // rodapés de madeira no fundo
  for (const y of [Y0, Y1]) g.add(em(caixa(2 * XL - 0.2, 0.12, 0.08, 0.03, mat.madeiraClara()), 0, y + 0.06, ZF + 0.04));
  return g;
}

/** Os quatro fundos de cômodo: [x0, x1, y0, y1] no mundo. */
export const COMODOS = {
  quarto: [-XL + 0.05, X_DIV_CIMA, Y1, YT - 0.28],
  banheiro: [X_DIV_CIMA, XL - 0.05, Y1, YT - 0.28],
  sala: [-XL + 0.05, X_DIV_BAIXO, Y0, Y1 - 0.26],
  cozinha: [X_DIV_BAIXO, XL - 0.05, Y0, Y1 - 0.26],
};

function fundoDoComodo(nome) {
  const [x0, x1, y0, y1] = COMODOS[nome];
  return em(caixa(x1 - x0 + 0.1, y1 - y0 + 0.1, 0.16, 0.02, mat.neutro()), (x0 + x1) / 2, (y0 + y1) / 2, ZF - 0.08);
}

/** Desenho do papel de parede (1 listras, 2 bolinhas, 3 estrelinhas) em relevo, no fundo de todos os cômodos. */
function papelDeParede(tipo) {
  const g = new THREE.Group();
  const claro = plastico('#ffffff', { rugosidade: 0.5, verniz: 0.3, opacidade: 0.55 });
  const dourado = plastico('#ffcf3f', { rugosidade: 0.3, verniz: 0.8 });
  for (const [x0, x1, y0, y1] of Object.values(COMODOS)) {
    if (tipo === 1) {
      for (let x = x0 + 0.18; x < x1 - 0.08; x += 0.42) g.add(em(caixa(0.16, y1 - y0 - 0.02, 0.03, 0.01, claro), x, (y0 + y1) / 2, ZF + 0.01));
    } else if (tipo === 2) {
      let linha = 0;
      for (let y = y0 + 0.3; y < y1 - 0.1; y += 0.42, linha++) {
        for (let x = x0 + (linha % 2 ? 0.42 : 0.21); x < x1 - 0.1; x += 0.42) {
          const b = bola(0.075, claro, 16); b.scale.set(1, 1, 0.35); g.add(em(b, x, y, ZF + 0.01));
        }
      }
    } else {
      let linha = 0;
      for (let y = y0 + 0.45; y < y1 - 0.15; y += 0.62, linha++) {
        for (let x = x0 + (linha % 2 ? 0.75 : 0.35); x < x1 - 0.15; x += 0.8) {
          const e = estrela3D(0.12, 0.03, dourado); e.rotation.z = (linha + x) * 0.3; g.add(em(e, x, y, ZF + 0.03));
        }
      }
    }
  }
  return g;
}

/** Janela com moldura branca, vidro, cruz e venezianas cor de rosa, de frente para +z. [w] e [h] do vidro. */
export function janela(w, h, noite = false) {
  const g = new THREE.Group();
  const branco = mat.branco();
  g.add(em(caixa(w + 0.2, h + 0.2, 0.1, 0.06, branco), 0, 0, 0));
  g.add(em(caixa(w, h, 0.05, 0.04, noite ? mat.vidroNoite() : mat.vidro()), 0, 0, 0.04));
  g.add(em(caixa(0.06, h, 0.08, 0.02, branco), 0, 0, 0.07));
  g.add(em(caixa(w, 0.06, 0.08, 0.02, branco), 0, 0, 0.07));
  const rosa = plastico('#ff8fab', { rugosidade: 0.4, verniz: 0.5 });
  for (const lado of [-1, 1]) {
    g.add(em(caixa(w * 0.32, h + 0.18, 0.07, 0.05, rosa), lado * (w / 2 + 0.1 + w * 0.17), 0, 0.0));
  }
  g.add(em(caixa(w + 0.42, 0.1, 0.24, 0.04, branco), 0, -h / 2 - 0.12, 0.08));
  if (!noite) {
    // reflexo do céu
    const r = plastico('#ffffff', { rugosidade: 0.1, emissivo: '#ffffff', opacidade: 0.7 });
    const a = caixa(0.05, h * 0.3, 0.01, 0.02, r); a.rotation.z = -0.6; g.add(em(a, -w * 0.28, h * 0.22, 0.075));
  }
  return g;
}

/** Janela redonda (banheiro e frontão). */
export function janelaRedonda(r, noite = false) {
  const g = new THREE.Group();
  const aro = malha(new THREE.TorusGeometry(r, r * 0.22, 16, 40), mat.branco());
  g.add(aro);
  const vidro = malha(new THREE.CircleGeometry(r * 0.98, 40), noite ? mat.vidroNoite() : mat.vidro());
  g.add(em(vidro, 0, 0, -0.01));
  const cruz = caixa(r * 2, 0.05, 0.05, 0.02, mat.branco());
  g.add(em(cruz, 0, 0, 0.02));
  g.add(em(caixa(0.05, r * 2, 0.05, 0.02, mat.branco()), 0, 0, 0.02));
  return g;
}

/** Halo de luz quente (janelas de noite). */
function halo(raio, cor = 'rgba(255,214,120,') {
  const cv = document.createElement('canvas');
  cv.width = cv.height = 128;
  const ctx = cv.getContext('2d');
  const gr = ctx.createRadialGradient(64, 64, 0, 64, 64, 64);
  gr.addColorStop(0, cor + '0.55)');
  gr.addColorStop(0.45, cor + '0.22)');
  gr.addColorStop(1, cor + '0)');
  ctx.fillStyle = gr;
  ctx.fillRect(0, 0, 128, 128);
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  const m = new THREE.Mesh(new THREE.PlaneGeometry(raio * 2, raio * 2), new THREE.MeshBasicMaterial({ map: tex, transparent: true, depthWrite: false, toneMapped: false }));
  m.renderOrder = 3;
  return m;
}

/** As janelas do app (CenaDaCasa: janelas), no fundo dos cômodos. */
function janelasDaCasa(noite) {
  const g = new THREE.Group();
  for (const [fx, fy, fw, fh] of [[0.39, 0.35, 0.08, 0.08], [0.45, 0.585, 0.08, 0.075], [0.66, 0.585, 0.08, 0.075]]) {
    const c = noPlano(fx + fw / 2, fy + fh / 2, ZF);
    const j = janela(larguraEm(fw, ZF), alturaEm(fh, ZF), noite);
    g.add(em(j, c.x, c.y, ZF + 0.05));
    if (noite) g.add(em(halo(larguraEm(fw, ZF) * 1.3), c.x, c.y, ZF + 0.3));
  }
  const c = noPlano(0.82, 0.39, ZF);
  g.add(em(janelaRedonda(larguraEm(0.035, ZF)), c.x, c.y, ZF + 0.06));
  if (noite) {
    g.children[g.children.length - 1].traverse((o) => { if (o.isMesh && o.geometry.type === 'CircleGeometry') o.material = mat.vidroNoite(); });
    g.add(em(halo(larguraEm(0.035, ZF) * 2), c.x, c.y, ZF + 0.3));
  }
  return g;
}

/** Porta com topo arredondado, batente branco, macaneta dourada e janelinha. [cor] da Loja.coresDaPorta. */
export function porta(cor, l = 1.0, a = 1.75) {
  const g = new THREE.Group();
  const forma = (L, A, r) => {
    const s = new THREE.Shape();
    s.moveTo(-L / 2, 0); s.lineTo(L / 2, 0); s.lineTo(L / 2, A - r);
    s.absarc(0, A - r, L / 2, 0, Math.PI, false);
    s.lineTo(-L / 2, 0);
    return s;
  };
  const geoBatente = new THREE.ExtrudeGeometry(forma(l + 0.26, a + 0.13, (l + 0.26) / 2), { depth: 0.08, bevelEnabled: true, bevelThickness: 0.04, bevelSize: 0.04, bevelSegments: 4, curveSegments: 24 });
  g.add(malha(geoBatente, mat.branco()));
  const geoPorta = new THREE.ExtrudeGeometry(forma(l, a, l / 2), { depth: 0.08, bevelEnabled: true, bevelThickness: 0.04, bevelSize: 0.035, bevelSegments: 4, curveSegments: 24 });
  const corPorta = plastico(cor, { rugosidade: 0.38, verniz: 0.6 });
  g.add(em(malha(geoPorta, corPorta), 0, 0, 0.1));
  // almofadas da porta (relevo)
  const escuro = plastico(new THREE.Color(cor).multiplyScalar(0.82).getStyle(), { rugosidade: 0.4, verniz: 0.5 });
  g.add(em(caixa(l * 0.62, a * 0.24, 0.06, 0.05, escuro), 0, a * 0.22, 0.24));
  const jan = janelaRedonda(l * 0.2);
  g.add(em(jan, 0, a * 0.7, 0.24));
  g.add(em(bola(0.075, plastico('#ffc93c', { rugosidade: 0.15, verniz: 1 }), 16), l * 0.32, a * 0.47, 0.28));
  return g;
}

function portaDaCasa(cor) {
  const c = noPlano(0.318, 0.68, ZF);
  return em(porta(cor), c.x, Y0, ZF + 0.02);
}

/** Escada de madeira no canto esquerdo da sala, subindo até o piso de cima. */
export function escada(degraus = 7, larguraBase = 1.25, prof = 1.05) {
  const g = new THREE.Group();
  const alt = (Y1 - Y0) / degraus;
  for (let i = 0; i < degraus; i++) {
    const L = larguraBase - i * 0.14;
    g.add(em(caixa(L, alt + 0.02, prof, 0.05, mat.madeira()), L / 2, alt * (i + 0.5), 0));
    g.add(em(caixa(L - 0.04, 0.07, prof + 0.04, 0.03, mat.madeiraClara()), L / 2, alt * (i + 1) - 0.02, 0.01));
  }
  return g;
}

function escadaDaCasa() {
  return em(escada(), -XL + ESP, Y0, ZF + 0.55);
}

/** Chaminé de tijolo atrás do telhado. */
export function chamine(alt = 1.9) {
  const g = new THREE.Group();
  g.add(em(caixa(0.66, alt, 0.66, 0.08, mat.tijolo()), 0, alt / 2, 0));
  g.add(em(caixa(0.86, 0.24, 0.86, 0.08, massinha('#a8432e')), 0, alt - 0.02, 0));
  // tijolinhos em relevo
  const claro = massinha('#d8704f');
  for (const [x, y] of [[-0.14, 0.45], [0.16, 0.8], [-0.1, 1.15], [0.12, 1.45]]) g.add(em(caixa(0.26, 0.12, 0.04, 0.03, claro), x, y, 0.33));
  return g;
}

function chamineDaCasa() {
  const z = -1.25;
  const topo = noPlano(0.715, 0.112, z);
  return em(chamine(), topo.x, topo.y - 1.9, z);
}

/** Telhado: duas placas de plástico brilhante, cumeeira redonda e frontão com a janela redonda. */
export function telhado(cor, { comFrontao = true, noiteNaJanela = false } = {}) {
  const g = new THREE.Group();
  const m = plastico(cor, { rugosidade: 0.3, verniz: 0.85, rugosidadeVerniz: 0.18 });
  const escura = plastico(new THREE.Color(cor).multiplyScalar(0.84).getStyle(), { rugosidade: 0.3, verniz: 0.85 });
  const t = 0.36; // espessura
  const dx = CUMEEIRA.x - BEIRAL.x, dy = CUMEEIRA.y - BEIRAL.y;
  const comp = Math.hypot(dx, dy) + 0.3;
  const ang = Math.atan2(dy, dx);
  const prof = P + 1.0;
  for (const lado of [-1, 1]) {
    // o topo da placa passa pela linha do beiral até a cumeeira (a mesma do triângulo do app)
    const nx = lado * Math.sin(ang), ny = Math.cos(ang);
    const placa = caixa(comp, t, prof, 0.16, m);
    placa.rotation.z = -lado * ang;
    em(placa, lado * dx / 2 - nx * t / 2, (BEIRAL.y + CUMEEIRA.y) / 2 - ny * t / 2, -P / 2 + 0.2);
    g.add(placa);
    // friso mais escuro na borda do beiral
    const friso = cilindro(0.2, 0.2, prof + 0.02, escura, 24);
    friso.rotation.x = Math.PI / 2;
    em(friso, lado * (dx - 0.05) - nx * 0.18, BEIRAL.y - 0.16, -P / 2 + 0.2);
    g.add(friso);
  }
  const cumeeira = cilindro(0.24, 0.24, prof + 0.06, escura, 32);
  cumeeira.rotation.x = Math.PI / 2;
  em(cumeeira, 0, CUMEEIRA.y - 0.16, -P / 2 + 0.2);
  g.add(cumeeira);
  if (comFrontao) {
    const yb = YT - 0.1, larg = XL + 0.02, yt = CUMEEIRA.y - 0.32;
    const s = new THREE.Shape();
    s.moveTo(-larg, yb); s.lineTo(0, yt); s.lineTo(larg, yb); s.closePath();
    const fr = malha(new THREE.ExtrudeGeometry(s, { depth: 0.16, bevelEnabled: true, bevelSize: 0.05, bevelThickness: 0.05, bevelSegments: 3 }), mat.parede());
    em(fr, 0, 0, 0.0);
    g.add(fr);
    const cj = noPlano(0.5, 0.215, 0.3);
    g.add(em(janelaRedonda(larguraEm(0.032, 0.3), noiteNaJanela), cj.x, cj.y, 0.27));
  }
  return g;
}

/** Neve em cima do telhado (clima 2): uma camada branca macia e gotinhas penduradas no beiral. */
function neveNoTelhado() {
  const g = new THREE.Group();
  const branco = plastico('#ffffff', { rugosidade: 0.85, verniz: 0.05, ambiente: 0.6 });
  const dx = CUMEEIRA.x - BEIRAL.x, dy = CUMEEIRA.y - BEIRAL.y;
  const ang = Math.atan2(dy, dx);
  const comp = Math.hypot(dx, dy) - 0.2;
  const zFrente = -P / 2 + 0.2 + (P + 1.0) / 2;
  for (const lado of [-1, 1]) {
    const nx = lado * Math.sin(ang), ny = Math.cos(ang);
    const placa = caixa(comp, 0.22, P + 0.8, 0.11, branco);
    placa.rotation.z = -lado * ang;
    em(placa, lado * dx / 2 + nx * 0.09, (BEIRAL.y + CUMEEIRA.y) / 2 + ny * 0.09, -P / 2 + 0.2);
    g.add(placa);
    // bolotas de neve na borda da frente e no beiral
    const passos = 9;
    for (let k = 0; k <= passos; k++) {
      const f = k / passos;
      const x = lado * dx * (1 - f) + nx * 0.16, y = BEIRAL.y + dy * f + ny * 0.16;
      const b = bola(0.17 + (k % 2) * 0.05, branco, 20);
      b.scale.set(1.2, 0.9, 1);
      g.add(em(b, x, y, zFrente - 0.12));
    }
    for (let k = 0; k < 4; k++) {
      const b = bola(0.15, branco, 16);
      b.scale.set(1, 1.4, 1);
      g.add(em(b, lado * (dx - 0.1) - nx * 0.1, BEIRAL.y - 0.15, zFrente - 0.4 - k * 0.9));
    }
  }
  return g;
}

// ---------------------------------------------------------------------------------------------
// Fora da casa

/** Cerca branca de ponta, ao longo de x, com [n] tábuas, de pé em y = 0. */
export function cerca(n = 5, passo = 0.42, alt = 0.8) {
  const g = new THREE.Group();
  const branco = plastico('#ffffff', { rugosidade: 0.45, verniz: 0.4 });
  const s = new THREE.Shape();
  const w = 0.24;
  s.moveTo(-w / 2, 0); s.lineTo(w / 2, 0); s.lineTo(w / 2, alt - 0.12); s.lineTo(0, alt); s.lineTo(-w / 2, alt - 0.12); s.closePath();
  const geo = new THREE.ExtrudeGeometry(s, { depth: 0.07, bevelEnabled: true, bevelSize: 0.03, bevelThickness: 0.03, bevelSegments: 3 });
  for (let i = 0; i < n; i++) g.add(em(malha(geo, branco), (i - (n - 1) / 2) * passo, 0, 0));
  for (const y of [alt * 0.3, alt * 0.66]) g.add(em(caixa(n * passo, 0.1, 0.06, 0.03, branco), 0, y, -0.06));
  return g;
}

function cercaDaCasa() {
  const g = new THREE.Group();
  const z = -1.2;
  for (const [fx0, fx1] of [[-0.08, 0.098], [0.902, 1.08]]) {
    const a = noPlano(fx0, 0.5, z), b = noPlano(fx1, 0.5, z);
    const n = Math.round((b.x - a.x) / 0.42);
    g.add(em(cerca(n), (a.x + b.x) / 2, CENA.G, z));
  }
  return g;
}

/** Pedrinha achatada do caminho. */
function pedrinha(rx, rz) {
  const p = bola(1, mat.pedra(), 32);
  p.scale.set(rx, 0.07, rz);
  return p;
}

function caminhoDaCasa() {
  const g = new THREE.Group();
  // as quatro pedras do app (x, y, raio em fração da largura) e mais pedras pelo jardim de baixo
  const pedras = [[0.305, 0.815, 0.032], [0.29, 0.86, 0.036], [0.318, 0.91, 0.04], [0.292, 0.97, 0.044]];
  for (let y = 1.035, k = 0; y < 1.42; y += 0.07, k++) pedras.push([k % 2 ? 0.315 : 0.29, y, 0.046]);
  for (const [fx, fy, r] of pedras) {
    const c = noChao(fx, fy, CENA.G + 0.05);
    const rx = larguraEm(r * 0.85, c.z);
    g.add(em(pedrinha(rx, rx * 0.62), c.x, CENA.G + 0.03, c.z));
  }
  return g;
}

/** Laguinho: água azul com brilho, borda de pedrinhas e uma folha de vitória-régia. */
export function lago(rx, rz) {
  const g = new THREE.Group();
  const agua = cilindro(1, 1, 0.12, plastico('#3fa9e6', { rugosidade: 0.05, verniz: 1, rugosidadeVerniz: 0.05, ambiente: 0.9 }), 64);
  agua.scale.set(rx, 1, rz);
  em(agua, 0, 0.04, 0);
  g.add(agua);
  const claro = cilindro(1, 1, 0.02, plastico('#9ad9ff', { rugosidade: 0.1, verniz: 1, emissivo: '#bfe9ff', opacidade: 0.75 }), 48);
  claro.scale.set(rx * 0.4, 1, rz * 0.25);
  em(claro, -rx * 0.15, 0.115, -rz * 0.2);
  g.add(claro);
  const n = 22;
  for (let i = 0; i < n; i++) {
    const a = (i / n) * Math.PI * 2;
    const p = bola(0.2 + (i % 3) * 0.04, mat.pedra(), 20);
    p.scale.set(1.3, 0.6, 1);
    em(p, Math.cos(a) * (rx + 0.08), 0.06, Math.sin(a) * (rz + 0.08));
    g.add(p);
  }
  const folha = cilindro(0.32, 0.32, 0.04, plastico('#4fb84a', { rugosidade: 0.4, verniz: 0.5 }), 24);
  em(folha, rx * 0.45, 0.12, rz * 0.25);
  g.add(folha);
  g.add(em(flor('#ff8fc4', 0.9), rx * 0.45, 0.06, rz * 0.25));
  return g;
}

function lagoDaCasa() {
  const c = noChao(0.825, 0.93);
  const e = noChao(0.68, 0.93), d = noChao(0.97, 0.93);
  const f = noChao(0.825, 0.875), t = noChao(0.825, 0.985);
  return em(lago((d.x - e.x) / 2, (t.z - f.z) / 2), c.x, CENA.G, (f.z + t.z) / 2);
}

// ---------------------------------------------------------------------------------------------
// Céu (no plano da cena)

/** Estrelinhas da noite (as posições do app), em dois grupos para piscarem alternadas. */
function estrelasDaNoite(par) {
  const g = new THREE.Group();
  const m = plastico('#fff2b0', { rugosidade: 0.3, verniz: 0.6, emissivo: '#ffe27a', ambiente: 0.4 });
  for (let i = 0; i < 28; i++) {
    if (i % 2 !== par) continue;
    const fx = (i * 0.137 + 0.05) % 1, fy = (i * 0.071 + 0.02) % 0.62;
    const p = noPlano(fx, fy, 0);
    const r = 0.1 + (i % 3) * 0.05;
    const e = estrela3D(r, r * 0.25, m);
    e.rotation.z = i * 0.7;
    g.add(em(e, p.x, p.y, 0));
  }
  return g;
}

/** Pingos de chuva (clima 1) que se repetem na vertical (a imagem é emendável em cima e embaixo). */
function pingos() {
  const g = new THREE.Group();
  const m = plastico('#4f8fe6', { rugosidade: 0.12, verniz: 1, ambiente: 0.8, opacidade: 0.9 });
  for (let i = 0; i < 55; i++) {
    const fx = (i * 0.173) % 1, fy = (i * 0.071 + 0.013) % 1;
    for (const d of [-1, 0, 1]) {
      const p = noPlano(fx, fy + d, 0);
      const gota = new THREE.Group();
      gota.add(em(bola(0.1, m, 20), 0, 0, 0));
      const c = malha(new THREE.ConeGeometry(0.098, 0.26, 20), m);
      gota.add(em(c, 0, 0.13, 0));
      gota.rotation.z = -0.22;
      g.add(em(gota, p.x, p.y, 0));
    }
  }
  return g;
}

/** Flocos de neve (clima 2): bolinhas fofas, emendáveis na vertical. */
function flocos() {
  const g = new THREE.Group();
  const m = plastico('#ffffff', { rugosidade: 0.85, verniz: 0.05, emissivo: '#eef6ff', ambiente: 0.6 });
  for (let i = 0; i < 36; i++) {
    const fx = (i * 0.173 + 0.01) % 1, fy = (i * 0.071 + 0.02) % 1;
    for (const d of [-1, 0, 1]) {
      const p = noPlano(fx, fy + d, 0);
      g.add(em(bola(0.07 + (i % 3) * 0.035, m, 20), p.x, p.y, 0));
    }
  }
  return g;
}

// ---------------------------------------------------------------------------------------------
// Camadas da cena (montadores para o catálogo)

export const camadasDaCena = {
  vazio: () => camadaDaCena({ ver: [discoDeGrama(), tufos(11, { flores: false }), arvoresDosLados(), loteVazio()] }),
  terreno: () => camadaDaCena({ ver: [discoDeGrama(), tufos(11), arvoresDosLados(), baseDaCasa()] }),
  comodo: (nome) => () => camadaDaCena({ ver: [fundoDoComodo(nome)] }),
  papel: (tipo) => () => camadaDaCena({ ver: [papelDeParede(tipo)] }),
  janelas: (noite) => () => camadaDaCena({ ver: [janelasDaCasa(noite)], tapa: [estruturaDasParedes()] }),
  porta: (cor) => () => camadaDaCena({ ver: [portaDaCasa(cor)], tapa: [estruturaDasParedes()] }),
  paredes: () => camadaDaCena({
    ver: [estruturaDasParedes()],
    capta: [baseDaCasa(), chaoQueCapta()],
    fundo: Object.keys(COMODOS).map(fundoDoComodo),
  }),
  escada: () => camadaDaCena({ ver: [escadaDaCasa()], sombra: [estruturaDasParedes()], capta: [] }),
  telhado: (cor) => () => camadaDaCena({ ver: [telhado(cor)], capta: [estruturaDasParedes(), chaoQueCapta()], fundo: Object.keys(COMODOS).map(fundoDoComodo) }),
  janelaDoTelhadoNoite: () => {
    const cj = noPlano(0.5, 0.215, 0.3);
    const j = janelaRedonda(larguraEm(0.032, 0.3), true);
    return camadaDaCena({ ver: [em(j, cj.x, cj.y, 0.27), em(halo(1.0), cj.x, cj.y, 0.6)] });
  },
  neveNoTelhado: () => camadaDaCena({ ver: [neveNoTelhado()], sombra: [telhado('#ffffff')] }),
  chamine: () => camadaDaCena({ ver: [chamineDaCasa()], tapa: [telhado('#ffffff')] }),
  cerca: () => camadaDaCena({ ver: [cercaDaCasa()], capta: [chaoQueCapta()] }),
  caminho: () => camadaDaCena({ ver: [caminhoDaCasa()] }),
  lago: () => camadaDaCena({ ver: [lagoDaCasa()] }),
  estrelas: (par) => () => camadaDaCena({ ver: [estrelasDaNoite(par)] }),
  chuva: () => camadaDaCena({ ver: [pingos()] }),
  neve: () => camadaDaCena({ ver: [flocos()] }),
};

/** Plano de grama que só recebe sombra (a sombra da casa e da cerca na grama). */
function chaoQueCapta() {
  // só perto da casa: longe dela a sombra cortaria no limite da câmera de sombra
  const p = new THREE.Mesh(new THREE.CircleGeometry(7, 64), new THREE.MeshBasicMaterial());
  p.rotation.x = -Math.PI / 2;
  p.scale.set(1, 0.7, 1);
  return em(p, 0, CENA.G + 0.005, -1.5);
}

// Peças soltas para os ícones das estruturas (itens terreno, paredes, telhado...): ver itensDaCasa.js.
export const pecas = { estruturaDasParedes, fundoDoComodo, baseDaCasa, janela, janelaRedonda, porta, escada, chamine, telhado, cerca, pedrinha, lago, grupo, mat };
