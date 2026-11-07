// Fase 8: enfeites das demais telas, em plástico brilhante com verniz (PRD, seção 2): a mão de toque da dica,
// diplomas (rolo, medalha, selo), fundos das figurinhas do álbum, livrinhos, a noite da tela Dormir, o pianinho e
// as coisas do ateliê.
import { THREE, plastico, malha, bola, capsula, cilindro, caixa, em, ancora } from '../kit.js';
import { estrela3D } from './estrela3D.js';

const brilho = (cor, o = {}) => plastico(cor, { rugosidade: 0.2, verniz: 1, rugosidadeVerniz: 0.08, ambiente: 0.5, ...o });
const vinil = (cor, o = {}) => plastico(cor, { rugosidade: 0.42, verniz: 0.55, rugosidadeVerniz: 0.2, ambiente: 0.5, ...o });
const ouro = () => brilho('#ffc21a', { rugosidade: 0.18, emissivo: '#a86e00' });
const madeira = () => plastico('#dc9a5c', { rugosidade: 0.45, verniz: 0.5 });
const PRETO = () => plastico('#2a1d40', { rugosidade: 0.12, verniz: 1 });

/** Cápsula de [a] até [b]. */
function segmento(a, b, r, mat) {
  const va = new THREE.Vector3(...a), vb = new THREE.Vector3(...b);
  const d = vb.clone().sub(va);
  const m = capsula(r, Math.max(0.001, d.length()), mat);
  m.position.copy(va).add(vb).multiplyScalar(0.5);
  m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), d.normalize());
  return m;
}

/** Retângulo de cantos redondos (Shape) centrado. */
function retangulo(l, a, r, alvo = new THREE.Shape()) {
  const x = -l / 2, y = -a / 2;
  alvo.moveTo(x + r, y);
  alvo.lineTo(x + l - r, y); alvo.quadraticCurveTo(x + l, y, x + l, y + r);
  alvo.lineTo(x + l, y + a - r); alvo.quadraticCurveTo(x + l, y + a, x + l - r, y + a);
  alvo.lineTo(x + r, y + a); alvo.quadraticCurveTo(x, y + a, x, y + a - r);
  alvo.lineTo(x, y + r); alvo.quadraticCurveTo(x, y, x + r, y);
  return alvo;
}

function extrudar(forma, prof, mat, bisel = 0.06, seg = 6) {
  const geo = new THREE.ExtrudeGeometry(forma, { depth: prof, bevelEnabled: true, bevelThickness: bisel, bevelSize: bisel, bevelSegments: seg, curveSegments: 24 });
  geo.translate(0, 0, -prof / 2);
  return malha(geo, mat);
}

function moldura(x0, x1, y0, y1, z0, z1) {
  const m = new THREE.Mesh(new THREE.BoxGeometry(x1 - x0, y1 - y0, z1 - z0), new THREE.MeshBasicMaterial({ visible: false }));
  m.position.set((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2);
  return m;
}

// ---------------------------------------------------------------- Zzz

/** Letra Z feita de tubos (sem fonte), no plano XY, de altura [a]. */
function letraZ(a, mat) {
  const g = new THREE.Group();
  const r = a * 0.13, l = a * 0.8;
  g.add(segmento([-l / 2, a / 2, 0], [l / 2, a / 2, 0], r, mat));
  g.add(segmento([l / 2, a / 2, 0], [-l / 2, -a / 2, 0], r, mat));
  g.add(segmento([-l / 2, -a / 2, 0], [l / 2, -a / 2, 0], r, mat));
  return g;
}

/** Três Z de sono, do menor (embaixo) ao maior (em cima), em lilás quase branco. [tam] = altura do conjunto. */
export function zzz(tam = 1) {
  const g = new THREE.Group();
  const m = brilho('#efeaff', { emissivo: '#8f7bff' });
  for (const [x, y, s] of [[-0.32, -0.32, 0.26], [0.0, 0.02, 0.34], [0.34, 0.4, 0.44]]) {
    const z = letraZ(s * tam, m);
    z.rotation.z = 0.18;
    g.add(em(z, x * tam, y * tam, 0));
  }
  return g;
}

/** tela_zzz: os Zzz soltos (dormir, mascote dormindo). */
export function zzzSolto() {
  const g = zzz(1.6);
  g.rotation.y = -0.2;
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

// ---------------------------------------------------------------- mão de toque

/**
 * Mão de desenho (luva branca de punho roxo) apontando para cima: a dica "toque aqui" (MaoGuia, no lugar do 👆).
 * [apertando]: o mesmo quadro com a mão mais baixa e o dedo dobradinho, e o anel do toque embaixo da ponta.
 * Âncora 'ponta' na ponta do dedo (o app alinha a ponta com o alvo).
 */
export function maoDeToque(apertando) {
  return () => {
    const g = new THREE.Group();
    const mao = new THREE.Group();
    const luva = plastico('#ffffff', { rugosidade: 0.3, verniz: 0.9, rugosidadeVerniz: 0.15, ambiente: 0.4 });
    const punho = brilho('#7a5cff');
    // palma gordinha
    mao.add(caixa(1.0, 0.92, 0.58, 0.28, luva));
    // indicador para cima
    const base = [-0.24, 0.3, 0.04];
    const ponta = [-0.24, 1.02, 0.04];
    mao.add(segmento(base, ponta, 0.19, luva));
    // três dedos dobrados na frente da palma (os nós em fila)
    for (const [i, x] of [[0, 0.0], [1, 0.24], [2, 0.44]]) {
      const d = capsula(0.15 - i * 0.012, 0.16, luva);
      d.rotation.x = Math.PI / 2;
      mao.add(em(d, x, 0.3 - i * 0.05, 0.26));
    }
    // dedão cruzando a frente
    mao.add(segmento([-0.44, -0.16, 0.2], [-0.06, 0.04, 0.42], 0.15, luva));
    // costurinhas lilás nas costas da mão
    const risco = plastico('#c9bdf7', { rugosidade: 0.5, verniz: 0.3 });
    for (const x of [0.06, 0.26]) mao.add(segmento([x, -0.3, 0.3], [x, -0.04, 0.3], 0.02, risco));
    // punho roxo
    const p = malha(new THREE.TorusGeometry(0.4, 0.15, 20, 40), punho);
    p.rotation.x = Math.PI / 2;
    mao.add(em(p, 0, -0.56, 0));
    ancora(mao, 'ponta', ponta[0], ponta[1] + 0.19, ponta[2]);
    mao.rotation.set(0.1, -0.25, 0.1);
    mao.position.y = apertando ? 0.1 : 0;
    g.add(mao);
    if (apertando) {
      // anel do toque em volta da ponta do dedo
      mao.updateMatrixWorld(true);
      const q = new THREE.Vector3(ponta[0], ponta[1] + 0.16, ponta[2]).applyMatrix4(mao.matrixWorld);
      const anel = malha(new THREE.TorusGeometry(0.36, 0.05, 12, 48), plastico('#b9a8ff', { rugosidade: 0.2, verniz: 1, emissivo: '#8f7bff', opacidade: 0.9 }), { sombra: false });
      anel.rotation.x = -1.1;
      g.add(em(anel, q.x, q.y, q.z));
    }
    g.add(moldura(-0.7, 0.7, -0.78, 1.52, -0.45, 0.7));
    return g;
  };
}

// ---------------------------------------------------------------- diplomas

/** Diploma enrolado (papel creme) com laço vermelho e selo dourado, deitado na diagonal. */
export function diplomaEnrolado() {
  const g = new THREE.Group();
  const papel = vinil('#fff4dc', { rugosidade: 0.55, verniz: 0.25 });
  const rolo = new THREE.Group();
  const c = cilindro(0.26, 0.26, 2.0, papel, 48);
  c.rotation.z = Math.PI / 2;
  rolo.add(c);
  // bordas enroladas: anéis nas pontas e a espiral escura de dentro
  const dentro = plastico('#e6cfa2', { rugosidade: 0.7, verniz: 0.1 });
  for (const l of [-1, 1]) {
    const a = malha(new THREE.TorusGeometry(0.22, 0.05, 12, 40), papel);
    a.rotation.y = Math.PI / 2;
    rolo.add(em(a, l * 1.0, 0, 0));
    const f = cilindro(0.17, 0.17, 0.02, dentro, 32);
    f.rotation.z = Math.PI / 2;
    rolo.add(em(f, l * 1.005, 0, 0));
  }
  // laço
  const fita = brilho('#ef3b5c');
  const volta = malha(new THREE.TorusGeometry(0.28, 0.06, 12, 40), fita);
  volta.rotation.y = Math.PI / 2;
  rolo.add(volta);
  for (const l of [-1, 1]) {
    const alca = malha(new THREE.TorusGeometry(0.17, 0.06, 12, 32), fita);
    alca.scale.set(1.2, 0.75, 1);
    alca.rotation.set(-0.5, 0, l * 0.5);
    rolo.add(em(alca, l * 0.2, 0.36, 0.12));
    rolo.add(segmento([0, 0.28, 0.12], [l * 0.22, -0.2, 0.38], 0.05, fita));
  }
  rolo.add(em(bola(0.09, fita, 20), 0, 0.3, 0.14));
  // selo dourado pendurado no laço
  const s = seloPeca(0.22);
  s.rotation.x = -0.3;
  rolo.add(em(s, 0.02, -0.12, 0.42));
  rolo.position.y = 0.26;
  rolo.rotation.y = 0.35;
  g.add(rolo);
  return g;
}

/** Disco de selo com borda ondulada e estrela no meio (peça). */
function seloPeca(r) {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  const n = 16;
  for (let i = 0; i <= n * 8; i++) {
    const a = (i / (n * 8)) * Math.PI * 2;
    const rr = r * (1 + 0.08 * Math.cos(a * n));
    if (i === 0) s.moveTo(Math.cos(a) * rr, Math.sin(a) * rr); else s.lineTo(Math.cos(a) * rr, Math.sin(a) * rr);
  }
  g.add(extrudar(s, r * 0.18, ouro(), r * 0.08, 4));
  const aro = malha(new THREE.TorusGeometry(r * 0.72, r * 0.06, 10, 40), brilho('#ffdc5c', { emissivo: '#a86e00' }));
  g.add(em(aro, 0, 0, r * 0.14));
  g.add(em(estrela3D(r * 0.48, r * 0.08, brilho('#fff3b0', { emissivo: '#c99a00' }), 5, { segmentos: 4 }), 0, 0, r * 0.16));
  return g;
}

/** Selo dourado com duas fitas (tela de diplomas, diploma grande). */
export function selo() {
  const g = new THREE.Group();
  const fita = brilho('#ef3b5c');
  for (const l of [-1, 1]) {
    const f = extrudar(new THREE.Shape([new THREE.Vector2(-0.16, 0.2), new THREE.Vector2(0.16, 0.2), new THREE.Vector2(0.16, -0.62), new THREE.Vector2(0, -0.5), new THREE.Vector2(-0.16, -0.62)]), 0.04, fita, 0.03, 3);
    f.rotation.z = l * 0.38;
    g.add(em(f, l * 0.2, -0.28, -0.08));
  }
  g.add(seloPeca(0.62));
  g.rotation.set(0.1, -0.22, 0);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

/** Medalha de ouro com fita em V (vermelha e azul). Âncora 'topo' no alto da fita. */
export function medalha() {
  const g = new THREE.Group();
  const fitas = [['#3a86ff', -1], ['#ef3b5c', 1]];
  for (const [cor, l] of fitas) {
    const f = caixa(0.36, 1.1, 0.06, 0.03, brilho(cor));
    f.rotation.z = l * 0.36;
    g.add(em(f, l * 0.2, 0.62, -0.06 + (l > 0 ? 0.02 : 0)));
    g.add(em(caixa(0.08, 1.0, 0.07, 0.03, brilho('#ffffff')), l * 0.2, 0.62, -0.04 + (l > 0 ? 0.02 : 0)).rotateZ(l * 0.36));
  }
  const argola = malha(new THREE.TorusGeometry(0.1, 0.035, 10, 24), ouro());
  g.add(em(argola, 0, 0.08, 0));
  const d = cilindro(0.5, 0.5, 0.14, ouro(), 64);
  d.rotation.x = Math.PI / 2;
  g.add(em(d, 0, -0.5, 0));
  const aro = malha(new THREE.TorusGeometry(0.5, 0.07, 14, 64), brilho('#ffd84a', { emissivo: '#a86e00' }));
  g.add(em(aro, 0, -0.5, 0));
  g.add(em(estrela3D(0.3, 0.06, brilho('#fff0a0', { emissivo: '#c99a00' }), 5, { segmentos: 5 }), 0, -0.5, 0.1));
  g.rotation.set(0.05, -0.25, 0.04);
  ancora(g, 'topo', 0, 1.18, 0);
  return g;
}

// ---------------------------------------------------------------- figurinhas do álbum

/** Cores dos quatro caminhos (dominio/Modelos.kt > Trilha.corArgb). */
export const CORES_DAS_TRILHAS = { matematica: '#ff9f1c', portugues: '#2ec4b6', ingles: '#6c63ff', logica: '#ef476f' };

/**
 * Fundo de figurinha: cartão de plástico com a borda grossa na cor do caminho e a janelinha branca no meio,
 * onde o app desenha o emoji da figurinha (âncora 'centro'). [cor] 'dourada' = figurinha do chefão.
 */
export function figurinha(cor) {
  return () => {
    const g = new THREE.Group();
    const dourada = cor === 'dourada';
    const borda = dourada ? brilho('#ffc21a', { emissivo: '#a86e00', rugosidade: 0.16 }) : brilho(cor);
    const L = 1.6, A = 1.92;
    g.add(extrudar(retangulo(L, A, 0.26), 0.14, borda, 0.07));
    const janela = vinil('#fffdf8', { rugosidade: 0.35, verniz: 0.8 });
    g.add(em(extrudar(retangulo(L - 0.34, A - 0.34, 0.16), 0.04, janela, 0.04, 4), 0, 0, 0.1));
    // brilho de verniz no canto de cima
    const reflexo = plastico('#ffffff', { rugosidade: 0.1, verniz: 1, emissivo: '#ffffff', opacidade: 0.55 });
    const r = capsula(0.035, 0.34, reflexo);
    r.rotation.z = -0.8;
    g.add(em(r, -0.48, 0.66, 0.17));
    if (dourada) {
      const est = brilho('#fff3b0', { emissivo: '#c99a00' });
      for (const [x, y] of [[-L / 2 + 0.1, A / 2 - 0.1], [L / 2 - 0.1, A / 2 - 0.1], [-L / 2 + 0.1, -A / 2 + 0.1], [L / 2 - 0.1, -A / 2 + 0.1]]) {
        g.add(em(estrela3D(0.15, 0.05, est, 5, { segmentos: 3 }), x, y, 0.13));
      }
    }
    g.rotation.set(-0.06, 0.14, 0);
    ancora(g, 'centro', 0, 0, 0.12);
    return g;
  };
}

/** Lugar de figurinha que falta: cartão lilás clarinho, afundado, com a borda pontilhada. */
export function figurinhaVazia() {
  const g = new THREE.Group();
  const L = 1.6, A = 1.92;
  g.add(extrudar(retangulo(L, A, 0.26), 0.08, vinil('#e7e1f7', { rugosidade: 0.5, verniz: 0.4 }), 0.05));
  g.add(em(extrudar(retangulo(L - 0.34, A - 0.34, 0.16), 0.02, vinil('#d8cff0', { rugosidade: 0.6, verniz: 0.2 }), 0.02, 3), 0, 0, 0.04));
  // pontinhos na volta da janelinha
  const ponto = vinil('#b9acdf');
  const l = L - 0.18, a = A - 0.18;
  const passo = 0.18;
  for (let x = -l / 2; x <= l / 2 + 0.001; x += passo) for (const y of [-a / 2, a / 2]) g.add(em(bola(0.03, ponto, 10), x, y, 0.08));
  for (let y = -a / 2 + passo; y < a / 2 - 0.05; y += passo) for (const x of [-l / 2, l / 2]) g.add(em(bola(0.03, ponto, 10), x, y, 0.08));
  g.rotation.set(-0.06, 0.14, 0);
  ancora(g, 'centro', 0, 0, 0.06);
  return g;
}

// ---------------------------------------------------------------- livrinhos

/** Livrinho fechado, em pé e um pouco virado: capa azul com estrela, folhas creme e lombada. */
export function livroFechado() {
  const g = new THREE.Group();
  const capa = brilho('#4d96ff');
  const folhas = vinil('#fff6e2', { rugosidade: 0.6, verniz: 0.1 });
  const l = 1.2, a = 1.55, p = 0.36;
  g.add(em(caixa(l - 0.08, a - 0.1, p - 0.08, 0.03, folhas), 0.04, a / 2, 0));
  for (const z of [-p / 2, p / 2]) g.add(em(caixa(l, a, 0.07, 0.035, capa), 0, a / 2, z));
  g.add(em(cilindro(p / 2 + 0.02, p / 2 + 0.02, a, capa, 32), -l / 2, a / 2, 0));
  // riscos das folhas
  const risco = plastico('#ecdcb8', { rugosidade: 0.8, verniz: 0 });
  for (let k = 1; k < 4; k++) g.add(em(caixa(0.012, a - 0.16, 0.012, 0.005, risco), l / 2 - 0.02, a / 2, -p / 2 + (k * p) / 4));
  // capa: estrela e faixas
  g.add(em(estrela3D(0.28, 0.05, brilho('#ffd23f', { emissivo: '#a87a00' }), 5, { segmentos: 4 }), 0.05, a * 0.58, p / 2 + 0.06));
  const faixa = brilho('#ffffff');
  for (const y of [a * 0.24, a * 0.18]) g.add(em(caixa(0.6, 0.05, 0.03, 0.02, faixa), 0.05, y, p / 2 + 0.04));
  g.rotation.y = -0.45;
  return g;
}

/** Perfil das folhas de um lado do livro aberto (x de 0 na lombada a 1), extrudado no comprimento do livro. */
function folhasDoLado(lado, mat) {
  const s = new THREE.Shape();
  s.moveTo(0, 0.02);
  s.quadraticCurveTo(0.12, 0.26, 0.48, 0.24);
  s.quadraticCurveTo(0.86, 0.22, 1.0, 0.18);
  s.lineTo(1.0, 0.04);
  s.lineTo(0.04, 0.0);
  s.closePath();
  const geo = new THREE.ExtrudeGeometry(s, { depth: 1.3, bevelEnabled: true, bevelThickness: 0.02, bevelSize: 0.02, bevelSegments: 3, curveSegments: 24 });
  geo.translate(0, 0, -0.65);
  const m = malha(geo, mat);
  m.scale.x = lado;
  return m;
}

/** Livrinho aberto (livrinhos e história): folhas curvas, linhas de texto e fita marcadora. */
export function livroAberto() {
  const g = new THREE.Group();
  const capa = brilho('#ff6fae');
  const folhas = vinil('#fffaf0', { rugosidade: 0.5, verniz: 0.2 });
  for (const l of [-1, 1]) {
    const c = caixa(1.12, 0.06, 1.42, 0.03, capa);
    c.rotation.z = -l * 0.06;
    g.add(em(c, l * 0.56, 0.02, 0));
    g.add(em(folhasDoLado(l, folhas), 0, 0.06, 0));
    // linhas de texto azuis, seguindo a folha
    const linha = plastico('#9cc3ff', { rugosidade: 0.6, verniz: 0.1 });
    for (let k = 0; k < 4; k++) {
      const z = -0.42 + k * 0.24;
      for (const [x0, x1] of [[0.3, 0.86]]) {
        const comp = (x1 - x0) * (k === 3 ? 0.6 : 1);
        const x = x0 + comp / 2;
        const y = 0.06 + (x < 0.48 ? 0.02 + 0.22 * (x / 0.48) : 0.24 - 0.06 * ((x - 0.48) / 0.52)) + 0.035;
        const b = caixa(comp, 0.012, 0.05, 0.006, linha);
        b.rotation.z = l * (x < 0.48 ? -0.25 : 0.1);
        g.add(em(b, l * x, y, z));
      }
    }
  }
  // fita marcadora
  const fita = brilho('#ffd23f');
  g.add(segmento([0.0, 0.1, 0.55], [0.06, 0.02, 0.85], 0.03, fita));
  g.rotation.set(0.1, 0, 0);
  return g;
}

// ---------------------------------------------------------------- dormir

/** Forma da lua crescente. */
function formaDaLua(r1 = 1, d = 0.5, r2 = 0.85) {
  const xi = (r1 * r1 - r2 * r2 + d * d) / (2 * d), yi = Math.sqrt(r1 * r1 - xi * xi);
  const t1 = Math.atan2(yi, xi), t2 = Math.atan2(-yi, xi - d);
  const s = new THREE.Shape();
  s.absarc(0, 0, r1, t1, Math.PI * 2 - t1, false);
  s.absarc(d, 0, r2, t2, -t2, true);
  return s;
}

/** Lua crescente dormindo, de touca azul com pompom, para a tela Dormir. */
export function luaDormindo() {
  const g = new THREE.Group();
  const s = formaDaLua(1, 0.52, 0.84);
  const geo = new THREE.ExtrudeGeometry(s, { depth: 0.34, bevelEnabled: true, bevelThickness: 0.16, bevelSize: 0.13, bevelSegments: 8, curveSegments: 64 });
  geo.translate(0, 0, -0.17);
  const lua = malha(geo, plastico('#ffd84a', { rugosidade: 0.28, verniz: 0.9, emissivo: '#ffb31a', ambiente: 0.45 }));
  g.add(lua);
  // carinha no lado de dentro da lua (voltada para a direita)
  const preto = PRETO();
  const olho = new THREE.TorusGeometry(0.1, 0.026, 8, 20, Math.PI);
  olho.rotateZ(Math.PI);
  g.add(em(malha(olho, preto), -0.5, 0.14, 0.34));
  const boc = bola(0.1, plastico('#ff7a9a', { rugosidade: 0.8, verniz: 0, opacidade: 0.6 }), 16);
  boc.scale.set(1, 0.6, 0.3);
  g.add(em(boc, -0.6, -0.12, 0.32));
  const sorriso = new THREE.TorusGeometry(0.07, 0.02, 8, 20, 2.0);
  sorriso.rotateZ(-Math.PI / 2 - 1.0);
  g.add(em(malha(sorriso, plastico('#7d1f34', { rugosidade: 0.3, verniz: 0.6 })), -0.42, -0.08, 0.34));
  // touca na ponta de cima
  const touca = new THREE.Group();
  const azul = vinil('#5b7bff', { rugosidade: 0.5, verniz: 0.3 });
  const cone = malha(new THREE.ConeGeometry(0.3, 0.9, 32), azul);
  cone.rotation.z = -0.9;
  touca.add(em(cone, 0.28, 0.2, 0));
  const aba = malha(new THREE.TorusGeometry(0.28, 0.09, 14, 40), vinil('#ffffff', { rugosidade: 0.85, verniz: 0 }));
  aba.rotation.set(Math.PI / 2, 0.9, 0);
  touca.add(aba);
  touca.add(em(bola(0.13, vinil('#ffffff', { rugosidade: 0.9, verniz: 0 }), 20), 0.68, 0.48, 0));
  touca.rotation.z = 0.2;
  g.add(em(touca, 0.18, 0.86, 0));
  g.rotation.set(0.05, -0.15, -0.2);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

/** Estrelinha da noite: amarela clarinha, redonda e brilhante. */
export function estrelaDaNoite() {
  const g = new THREE.Group();
  const e = estrela3D(1, 0.36, plastico('#ffe680', { rugosidade: 0.2, verniz: 1, emissivo: '#ffc21a', ambiente: 0.5 }), 5, { segmentos: 10 });
  e.rotation.set(0.12, -0.25, 0.1);
  g.add(e);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

// ---------------------------------------------------------------- piano

const CORES_DAS_NOTAS = ['#ef476f', '#ff9f1c', '#ffd166', '#3dbe6b', '#2ec4b6', '#4d96ff', '#9b6bff', '#ff6fae'];

/** Pianinho de brinquedo roxo com as oito teclas coloridas (cores de dominio/Criativo.kt > Nota). */
export function pianinho() {
  const g = new THREE.Group();
  const corpo = brilho('#6c4bff');
  const l = 2.6;
  g.add(em(caixa(l, 0.9, 0.7, 0.16, corpo), 0, 0.75, -0.3)); // corpo de trás
  g.add(em(caixa(l, 0.32, 1.0, 0.12, corpo), 0, 0.36, 0.0)); // base das teclas
  for (const x of [-l / 2 + 0.16, l / 2 - 0.16]) {
    g.add(em(caixa(0.22, 0.46, 1.1, 0.08, corpo), x, 0.62, 0.05));
    g.add(em(cilindro(0.08, 0.1, 0.22, brilho('#4a2fd0'), 20), x, 0.1, 0.36));
    g.add(em(cilindro(0.08, 0.1, 0.22, brilho('#4a2fd0'), 20), x, 0.1, -0.5));
  }
  const vao = l - 0.44, n = 8, tl = vao / n;
  CORES_DAS_NOTAS.forEach((cor, i) => {
    const x = -vao / 2 + tl / 2 + i * tl;
    g.add(em(caixa(tl - 0.04, 0.16, 0.62, 0.06, brilho(cor)), x, 0.58, 0.18));
  });
  for (let i = 1; i < n; i++) {
    if (i === 3 || i === 7) continue;
    const x = -vao / 2 + i * tl;
    g.add(em(caixa(tl * 0.48, 0.12, 0.34, 0.04, brilho('#2a1d40')), x, 0.7, 0.0));
  }
  // estante de partitura com notinha
  g.add(em(caixa(1.0, 0.5, 0.06, 0.04, vinil('#fff6e2')), 0, 1.36, -0.4));
  const nota = notaPeca(0.2, brilho('#ef476f'));
  g.add(em(nota, 0, 1.36, -0.34));
  g.rotation.y = -0.2;
  return g;
}

/** Colcheia (peça): cabeça inclinada, haste e bandeirinha. */
function notaPeca(tam, mat) {
  const g = new THREE.Group();
  const cab = bola(tam * 0.5, mat, 24);
  cab.scale.set(1.3, 0.95, 0.7);
  cab.rotation.z = 0.4;
  g.add(em(cab, -tam * 0.16, -tam * 0.8, 0));
  g.add(segmento([tam * 0.42, -tam * 0.75, 0], [tam * 0.42, tam * 0.9, 0], tam * 0.1, mat));
  const b = segmento([tam * 0.42, tam * 0.9, 0], [tam * 0.95, tam * 0.3, 0], tam * 0.1, mat);
  g.add(b);
  return g;
}

/** Notinha musical solta (sobe das teclas do piano). */
export function notaMusical() {
  const g = new THREE.Group();
  const n = notaPeca(1, brilho('#9b6bff'));
  n.rotation.set(0, -0.3, 0.08);
  g.add(n);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

// ---------------------------------------------------------------- ateliê

const CORES_DAS_TINTAS = ['#ef3b5c', '#ff9f1c', '#ffd23f', '#3dbe6b', '#3a86ff', '#9b6bff'];

/** Paleta de madeira com seis bolinhas de tinta e o furo do dedão, com o pincel deitado em cima. */
export function paleta() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  s.absellipse(0, 0, 1.2, 0.92, 0, Math.PI * 2, false);
  const furo = new THREE.Path();
  furo.absarc(0.5, -0.38, 0.17, 0, Math.PI * 2, true);
  s.holes.push(furo);
  const tabua = extrudar(s, 0.1, madeira(), 0.05);
  tabua.rotation.x = -Math.PI / 2;
  g.add(em(tabua, 0, 0.1, 0));
  CORES_DAS_TINTAS.forEach((cor, i) => {
    const a = Math.PI * 0.95 - i * 0.42;
    const b = bola(0.2, brilho(cor, { rugosidade: 0.12 }), 32);
    b.scale.set(1, 0.42, 1);
    g.add(em(b, Math.cos(a) * 0.78, 0.2, -Math.sin(a) * 0.6));
  });
  const p = pincelPeca('#3a86ff');
  p.rotation.set(Math.PI / 2, 0, 0.5);
  g.add(em(p, -0.1, 0.32, 0.2));
  // em pé, inclinada para a câmera (de cima ela some)
  g.rotation.set(1.0, -0.15, 0.12);
  ancora(g, 'centro', 0, 0.1, 0);
  return g;
}

/** Pincel (peça): cabo vermelho, virola prateada e ponta molhada de [tinta], ao longo de y. */
function pincelPeca(tinta) {
  const g = new THREE.Group();
  g.add(em(capsula(0.07, 1.0, brilho('#ef476f')), 0, -0.45, 0));
  g.add(em(cilindro(0.09, 0.08, 0.26, plastico('#d6dbe8', { rugosidade: 0.18, verniz: 1, ambiente: 0.9 }), 24), 0, 0.2, 0));
  const pelo = malha(new THREE.ConeGeometry(0.1, 0.4, 24), brilho(tinta, { rugosidade: 0.15 }));
  pelo.scale.set(1, 1, 0.8);
  g.add(em(pelo, 0, 0.5, 0));
  g.add(em(bola(0.1, brilho(tinta, { rugosidade: 0.15 }), 20), 0, 0.33, 0));
  return g;
}

/** Pincel solto, inclinado, com a ponta azul. */
export function pincel() {
  const g = new THREE.Group();
  const p = pincelPeca('#3a86ff');
  p.rotation.set(0.1, 0.2, -0.75);
  g.add(p);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

/** Lápis de cor amarelo (ferramenta Lápis do ateliê). */
export function lapis() {
  const g = new THREE.Group();
  const corpo = malha(new THREE.CylinderGeometry(0.16, 0.16, 1.2, 6), brilho('#ffc21a'));
  g.add(corpo);
  const madeiraP = malha(new THREE.ConeGeometry(0.16, 0.34, 6), plastico('#f3c28e', { rugosidade: 0.6, verniz: 0.1 }));
  g.add(em(madeiraP, 0, 0.77, 0));
  g.add(em(malha(new THREE.ConeGeometry(0.065, 0.14, 12), brilho('#3a86ff')), 0, 0.9, 0));
  g.add(em(cilindro(0.165, 0.165, 0.14, plastico('#d6dbe8', { rugosidade: 0.18, verniz: 1, ambiente: 0.9 }), 24), 0, -0.66, 0));
  g.add(em(capsula(0.15, 0.1, brilho('#ff8fb8')), 0, -0.8, 0));
  g.rotation.set(0.1, 0.3, -0.7);
  ancora(g, 'ponta', 0, 0.97, 0);
  return g;
}

/** Baldinho de tinta azul com tinta rosa escorrendo e a alça (ferramenta Balde). */
export function balde() {
  const g = new THREE.Group();
  const azul = brilho('#3a86ff');
  const corpo = cilindro(0.62, 0.48, 1.0, azul, 48);
  g.add(em(corpo, 0, 0.5, 0));
  const borda = malha(new THREE.TorusGeometry(0.62, 0.06, 12, 48), azul);
  borda.rotation.x = Math.PI / 2;
  g.add(em(borda, 0, 1.0, 0));
  const tinta = brilho('#ff4f9a', { rugosidade: 0.1 });
  const sup = cilindro(0.58, 0.58, 0.06, tinta, 48);
  g.add(em(sup, 0, 0.96, 0));
  for (const [a, comp] of [[0.3, 0.36], [0.9, 0.22], [-0.4, 0.28]]) {
    const x = Math.sin(a) * 0.62, z = Math.cos(a) * 0.62;
    g.add(segmento([x, 1.0, z], [x * 0.96, 1.0 - comp, z * 0.96], 0.07, tinta));
  }
  const alca = malha(new THREE.TorusGeometry(0.6, 0.04, 10, 40, Math.PI), plastico('#d6dbe8', { rugosidade: 0.2, verniz: 1, ambiente: 0.9 }));
  alca.rotation.y = 0.3;
  g.add(em(alca, 0, 1.0, 0));
  g.rotation.set(0.15, -0.3, 0);
  ancora(g, 'centro', 0, 0.5, 0);
  return g;
}

/** Borracha de duas cores (rosa e azul), com cantinho gasto (ferramenta Borracha). */
export function borracha() {
  const g = new THREE.Group();
  g.add(em(caixa(0.8, 0.5, 0.5, 0.14, vinil('#ff8fb8', { rugosidade: 0.6, verniz: 0.25 })), -0.36, 0, 0));
  g.add(em(caixa(0.7, 0.5, 0.5, 0.14, vinil('#5b9dff', { rugosidade: 0.6, verniz: 0.25 })), 0.38, 0, 0));
  g.rotation.set(0.25, -0.5, 0.35);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}
