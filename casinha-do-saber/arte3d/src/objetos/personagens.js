// Personagens em camadas (PRD visual 3D, seção 3.2): a criança e o irmãozinho montados por partes, para
// renderizar cada camada sozinha, na mesma câmera, e o app empilhar as imagens por sobreposição direta.
//
// Cada camada é a figura inteira com papéis por parte:
//   ver      a parte aparece na imagem (material neutro claro quando o app tinge; colorida quando não);
//   recorte  a parte some, mas esconde o que está atrás dela (alfa zero): o braço que passa atrás da
//            cabeça, o rosto que afunda na pele. Só partes fixas (que existem em toda combinação) recortam;
//   sombra   a parte não aparece, mas faz sombra nas que aparecem (a cabeça faz sombra no peito);
//   capta    a parte não aparece, mas recebe a sombra das que aparecem (a franja escurece a testa).
// Assim a luz e a sombra da camada são as da figura inteira e as camadas encaixam pixel a pixel.
import { THREE, plastico, malha, caixa, bola, capsula, cilindro, em, naSuperficie, ancora } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { laco3D } from './laco3D.js';

// ---------------------------------------------------------------------------------------------
// Materiais

/**
 * Neutro claro para tingir no app por multiplicação (pele, cabelo, cor da roupa). Quase branco: no sol a
 * cor fica perto da cor escolhida na paleta; na sombra escurece junto.
 */
const NEUTRO = '#d4d4d4';
const matPele = () => plastico(NEUTRO, { rugosidade: 0.55, verniz: 0.12, ambiente: 0.5 });
const matCabelo = () => plastico(NEUTRO, { rugosidade: 0.62, verniz: 0.05, ambiente: 0.5 });
/** Mesmo cabelo em preto brilhante: vira a camada de brilho (só os reflexos, em branco translúcido). */
const matCabeloBrilho = () => plastico('#000000', { rugosidade: 0.3, verniz: 1, rugosidadeVerniz: 0.18, ambiente: 1.1 });
const matRoupa = () => plastico(NEUTRO, { rugosidade: 0.6, verniz: 0.2, ambiente: 0.5 });
/** Sobrancelhas: cinza médio, tingidas com a cor do cabelo ficam um tom mais escuras que ele. */
const matSobrancelha = () => plastico('#6e6e6e', { rugosidade: 0.7, verniz: 0, ambiente: 0.5 });

const matRecorte = new THREE.MeshBasicMaterial({
  color: 0x000000, transparent: true, opacity: 0, blending: THREE.NoBlending, depthWrite: true, toneMapped: false,
});
const matSoSombra = new THREE.MeshBasicMaterial({ colorWrite: false, depthWrite: false });
const matCapta = new THREE.ShadowMaterial({ color: new THREE.Color('#2e1830'), opacity: 0.32, depthWrite: false });

const ESCURO = '#21130e';

// ---------------------------------------------------------------------------------------------
// Utilitários

function grupo(...filhos) {
  const g = new THREE.Group();
  for (const f of filhos) if (f) g.add(f);
  return g;
}

/** Cápsula de [a] até [b] (pontos [x, y, z]), de raio [r]. */
function segmento(a, b, r, mat) {
  const va = new THREE.Vector3(...a), vb = new THREE.Vector3(...b);
  const d = vb.clone().sub(va);
  const m = capsula(r, Math.max(0.001, d.length()), mat);
  m.position.copy(va).add(vb).multiplyScalar(0.5);
  m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), d.normalize());
  return m;
}

/** Arco de toro no plano XY, centrado no alto (de [-arco/2] a [+arco/2] em volta do eixo +Y). */
function arco(raio, tubo, abertura, mat) {
  const geo = new THREE.TorusGeometry(raio, tubo, 10, 32, abertura);
  geo.rotateZ(Math.PI / 2 - abertura / 2);
  const m = malha(geo, mat, { sombra: false });
  return m;
}

/** Textura de mancha macia (bochecha). */
let texBochecha = null;
function texturaBochecha() {
  if (texBochecha) return texBochecha;
  const cv = document.createElement('canvas');
  cv.width = cv.height = 128;
  const ctx = cv.getContext('2d');
  const g = ctx.createRadialGradient(64, 64, 0, 64, 64, 64);
  g.addColorStop(0, 'rgba(255,255,255,1)');
  g.addColorStop(0.45, 'rgba(255,255,255,0.7)');
  g.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, 128, 128);
  texBochecha = new THREE.CanvasTexture(cv);
  texBochecha.colorSpace = THREE.SRGBColorSpace;
  return texBochecha;
}

/**
 * Pedaço de esfera de raio [R] centrado na direção (x, y) do rosto, com a textura de mancha: a bochecha
 * acompanha a curva da cabeça.
 */
function bochecha(R, x, y, larg, alt, cor, forca) {
  const z = Math.sqrt(Math.max(0.0001, R * R - x * x - y * y));
  const theta = Math.acos(y / R);
  const phi = Math.atan2(z, -x);
  const dPhi = larg / (R * Math.sin(theta)), dTheta = alt / R;
  const geo = new THREE.SphereGeometry(R * 1.004, 24, 16, phi - dPhi, dPhi * 2, theta - dTheta, dTheta * 2);
  const mat = new THREE.MeshBasicMaterial({ color: cor, map: texturaBochecha(), transparent: true, opacity: forca, depthWrite: false });
  const m = new THREE.Mesh(geo, mat);
  m.renderOrder = 5;
  return m;
}

/** Cone que sai da superfície (espetadinho). */
function ponta(r, alt, mat) {
  const geo = new THREE.ConeGeometry(r, alt, 24);
  geo.translate(0, alt / 2, 0);
  geo.rotateX(Math.PI / 2);
  return malha(geo, mat);
}

// ---------------------------------------------------------------------------------------------
// Rosto (coordenadas da cabeça; R = raio da cabeça)

/** Altura dos olhos e distância do meio, em frações de R (o app conta com isso para óculos e máscaras). */
export const OLHO_X = 0.35, OLHO_Y = -0.04;

function texturaOlho() {
  const cv = document.createElement('canvas');
  cv.width = 8; cv.height = 128;
  const ctx = cv.getContext('2d');
  // v = 0 embaixo, 1 em cima (a textura é lida de baixo para cima na esfera)
  const g = ctx.createLinearGradient(0, 128, 0, 0);
  g.addColorStop(0.0, '#9a5a32');
  g.addColorStop(0.3, '#6a3a22');
  g.addColorStop(0.5, '#2a1810');
  g.addColorStop(1.0, '#1a0e0a');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, 8, 128);
  const t = new THREE.CanvasTexture(cv);
  t.colorSpace = THREE.SRGBColorSpace;
  return t;
}
let texOlho = null;

/**
 * Olhos. [estado]: aberto, pensando (olhando para cima), surpreso (maiores), alegre (^ ^, fechadinhos de
 * alegria) e fechado (piscando). [cilios] põe cílios no canto de fora.
 */
export function olhos(R, estado, cilios) {
  const g = new THREE.Group();
  texOlho = texOlho || texturaOlho();
  const escuro = plastico(ESCURO, { rugosidade: 0.25, verniz: 0.6 });
  const matOlho = new THREE.MeshPhysicalMaterial({ map: texOlho, roughness: 0.12, clearcoat: 1, clearcoatRoughness: 0.05, envMapIntensity: 1.0 });
  const brilho = new THREE.MeshBasicMaterial({ color: '#ffffff', toneMapped: false });
  for (const lado of [-1, 1]) {
    const x = lado * R * OLHO_X, y = R * OLHO_Y;
    if (estado === 'alegre' || estado === 'fechado') {
      // ^ ^ (alegre) ou um arquinho para baixo (piscando)
      const a = arco(R * 0.12, R * 0.034, Math.PI * 0.9, escuro);
      naSuperficie(a, R, x, y + (estado === 'alegre' ? -R * 0.02 : R * 0.04), -R * 0.012);
      if (estado === 'fechado') a.rotateZ(Math.PI);
      a.scale.set(1, estado === 'alegre' ? 1.0 : 0.7, 1);
      g.add(a);
      if (cilios) {
        for (const k of [0, 1]) {
          const c = segmento([0, 0, 0], [lado * R * 0.07, R * 0.05, 0], R * 0.016, escuro);
          c.castShadow = false;
          const base = estado === 'alegre' ? [x + lado * R * 0.12, y - R * 0.06 + k * R * 0.06] : [x + lado * R * 0.12, y + R * 0.02 - k * R * 0.055];
          const suporte = new THREE.Group();
          naSuperficie(suporte, R, base[0], base[1], -R * 0.006);
          suporte.add(c);
          c.rotation.z = estado === 'alegre' ? lado * (0.2 - k * 0.5) : -lado * (0.2 + k * 0.55);
          g.add(suporte);
        }
      }
      continue;
    }
    const surpreso = estado === 'surpreso';
    const pensando = estado === 'pensando';
    const lx = R * (surpreso ? 0.165 : 0.155), ly = R * (surpreso ? 0.215 : 0.2);
    const yo = y + (pensando ? R * 0.015 : 0);
    const olho = new THREE.Mesh(new THREE.SphereGeometry(1, 40, 24), matOlho);
    olho.scale.set(lx, ly, R * 0.08);
    naSuperficie(olho, R, x, yo, -R * 0.035);
    g.add(olho);
    // brilho grande (em cima, do lado da luz) e brilho pequeno (embaixo, do outro lado); pensando: olhar para cima
    const dx = pensando ? R * 0.03 : 0, dy = pensando ? R * 0.05 : 0;
    const b1 = new THREE.Mesh(new THREE.SphereGeometry(1, 20, 12), brilho);
    b1.scale.set(R * (surpreso ? 0.05 : 0.058), R * (surpreso ? 0.05 : 0.062), R * 0.02);
    naSuperficie(b1, R, x + R * 0.045 + dx, yo + R * 0.07 + dy, R * 0.035);
    const b2 = new THREE.Mesh(new THREE.SphereGeometry(1, 16, 10), brilho);
    b2.scale.set(R * 0.027, R * 0.027, R * 0.012);
    naSuperficie(b2, R, x - R * 0.05 + dx * 0.5, yo - R * 0.075 + dy * 0.6, R * 0.03);
    g.add(b1, b2);
    // pálpebra: um traço escuro que contorna o alto do olho
    const pal = arco(R * 0.15, R * 0.022, Math.PI * 0.62, escuro);
    pal.scale.set(lx / (R * 0.15), ly / (R * 0.15) * 0.98, 1);
    naSuperficie(pal, R, x, yo, R * 0.012);
    g.add(pal);
    if (cilios) {
      for (const k of [0, 1]) {
        const suporte = new THREE.Group();
        naSuperficie(suporte, R, x + lado * lx * (0.86 - k * 0.22), yo + ly * (0.42 + k * 0.36), R * 0.008);
        const c = segmento([0, 0, 0], [lado * R * 0.075, R * 0.04, 0], R * 0.017, escuro);
        c.castShadow = false;
        c.rotation.z = lado * (0.15 + k * 0.35);
        suporte.add(c);
        g.add(suporte);
      }
    }
  }
  return g;
}

/** Sobrancelhas por humor: feliz, torcendo (sobem um pouco), surpreso (sobem bem) e pensando (uma só). */
export function sobrancelhas(R, humor) {
  const g = new THREE.Group();
  const m = matSobrancelha();
  for (const lado of [-1, 1]) {
    const sobe = { feliz: 0, torcendo: 0.035, surpreso: 0.06, pensando: lado < 0 ? 0.06 : -0.01 }[humor];
    const s = arco(R * 0.13, R * 0.038, Math.PI * 0.55, m);
    // o arco fica 0,12 R acima do ponto de apoio: o alto da sobrancelha fica em OLHO_Y + 0,3 R
    naSuperficie(s, R, lado * R * (OLHO_X + 0.01), R * (OLHO_Y + 0.185 + sobe), -R * 0.012);
    // pensando: a sobrancelha de baixo inclina para dentro
    const giro = humor === 'pensando' && lado > 0 ? 0.25 : humor === 'surpreso' ? 0 : -lado * 0.06;
    s.rotateZ(giro);
    s.scale.set(1, humor === 'surpreso' ? 1.15 : 0.9, 1);
    g.add(s);
  }
  return g;
}

/** Boca por humor (com as bochechas): feliz, torcendo (bocão de "eba!"), surpreso ("o") e pensando. */
export function boca(R, humor) {
  const g = new THREE.Group();
  const matBoca = plastico('#7a1c22', { rugosidade: 0.35, verniz: 0.5 });
  const matLingua = plastico('#ff7d8a', { rugosidade: 0.45, verniz: 0.3 });
  const matDente = plastico('#ffffff', { rugosidade: 0.25, verniz: 0.4 });
  const yBoca = -R * 0.33;
  if (humor === 'feliz' || humor === 'torcendo') {
    const larg = R * (humor === 'torcendo' ? 0.24 : 0.19), fundo = R * (humor === 'torcendo' ? 0.28 : 0.2);
    const s = new THREE.Shape();
    s.moveTo(-larg, 0);
    s.quadraticCurveTo(0, R * 0.03, larg, 0);
    s.bezierCurveTo(larg * 0.95, -fundo * 1.25, -larg * 0.95, -fundo * 1.25, -larg, 0);
    const geo = new THREE.ExtrudeGeometry(s, { depth: R * 0.03, bevelEnabled: true, bevelThickness: R * 0.012, bevelSize: R * 0.014, bevelSegments: 3, curveSegments: 28 });
    const b = malha(geo, matBoca, { sombra: false });
    naSuperficie(b, R, 0, yBoca, -R * 0.035);
    g.add(b);
    // linguinha e dentinhos de cima, um pouco à frente do fundo da boca
    const l = bola(1, matLingua, 24);
    l.castShadow = false;
    l.scale.set(larg * 0.62, fundo * 0.36, R * 0.03);
    naSuperficie(l, R, 0, yBoca - fundo * 0.66, -R * 0.0);
    g.add(l);
    const d = caixa(larg * 1.1, R * 0.038, R * 0.03, R * 0.012, matDente);
    d.castShadow = false;
    naSuperficie(d, R, 0, yBoca - R * 0.03, -R * 0.006);
    g.add(d);
  } else if (humor === 'surpreso') {
    const s = new THREE.Shape();
    s.absellipse(0, 0, R * 0.085, R * 0.11, 0, Math.PI * 2, false, 0);
    const geo = new THREE.ExtrudeGeometry(s, { depth: R * 0.03, bevelEnabled: true, bevelThickness: R * 0.012, bevelSize: R * 0.014, bevelSegments: 3, curveSegments: 28 });
    const b = malha(geo, matBoca, { sombra: false });
    naSuperficie(b, R, 0, yBoca - R * 0.05, -R * 0.035);
    g.add(b);
    const l = bola(1, matLingua, 20);
    l.castShadow = false;
    l.scale.set(R * 0.055, R * 0.035, R * 0.025);
    naSuperficie(l, R, 0, yBoca - R * 0.12, R * 0.0);
    g.add(l);
  } else {
    // pensando: boquinha fechada, puxada para um lado
    const a = arco(R * 0.1, R * 0.03, Math.PI * 0.55, matBoca);
    naSuperficie(a, R, R * 0.04, yBoca - R * 0.02, -R * 0.01);
    a.rotateZ(Math.PI + 0.35);
    g.add(a);
  }
  // bochechas rosadas (mais fortes torcendo)
  const forca = humor === 'torcendo' ? 0.78 : 0.6;
  for (const lado of [-1, 1]) g.add(bochecha(R, lado * R * 0.56, -R * 0.24, R * 0.2, R * 0.13, '#ff5a78', forca));
  return g;
}

// ---------------------------------------------------------------------------------------------
// Cabelo (coordenadas da cabeça)

function calota(R, m, { abre = 0.53, inclina = -0.42, escala = 1.06 } = {}) {
  const c = malha(new THREE.SphereGeometry(R * escala, 64, 32, 0, Math.PI * 2, 0, Math.PI * abre), m);
  c.rotation.x = inclina;
  return c;
}

function mechas(R, m, { curta = false } = {}) {
  const g = new THREE.Group();
  for (let k = -2; k <= 2; k++) {
    const mecha = bola(R * 0.25, m, 24);
    mecha.scale.set(1.25, 0.95, 0.65);
    naSuperficie(mecha, R, k * R * 0.27, R * ((curta ? 0.6 : 0.52) - Math.abs(k) * 0.07), -R * 0.06);
    g.add(mecha);
  }
  return g;
}

/** Trancinha na linha da testa (a franja trançada da maquete). */
function tiaraTrancada(R, m) {
  const g = new THREE.Group();
  const n = 9;
  for (let i = 0; i < n; i++) {
    const t = -1 + (2 * i) / (n - 1);
    const conta = bola(R * 0.15, m, 20);
    conta.scale.set(1.35, 0.85, 0.7);
    naSuperficie(conta, R * 1.02, t * R * 0.8, R * (0.52 - 0.24 * t * t), -R * 0.05);
    conta.rotateZ((i % 2 ? 0.55 : -0.55) - t * 0.5);
    g.add(conta);
  }
  return g;
}

function cachos(R, m, { de, ate, passo, raio, distancia, z, achata = 0.85 }) {
  const g = new THREE.Group();
  for (let a = de; a <= ate + 0.001; a += passo) {
    const rad = THREE.MathUtils.degToRad(a);
    const c = bola(R * raio, m, 24);
    c.scale.set(1, 1, achata);
    em(c, Math.cos(rad) * R * distancia, Math.sin(rad) * R * distancia, R * z);
    g.add(c);
  }
  return g;
}

/**
 * Cabelo por gênero e estilo, separado em [tras] (atrás de tudo: coques, tranças, nuca, cachos de trás)
 * e [frente] (na frente da cabeça: calota, franja, topete, pontas, cachos da testa). [base] é a calota
 * comum (o laço se apoia nela).
 */
export function cabelo(R, menina, estilo, m) {
  const tras = new THREE.Group(), frente = new THREE.Group();
  const base = calota(R, m, menina ? {} : { abre: 0.5, inclina: -0.5, escala: 1.05 });
  frente.add(base);
  // nuca: atrás da cabeça e um pouco acima, para aparecer só dos lados de cima (não embaixo do queixo)
  // (o alto dela fica por baixo da calota, que tem raio 1,05 a 1,06 R)
  const nuca = bola(R * (menina ? 1.0 : 0.98), m, 48);
  em(nuca, 0, R * 0.04, -R * (menina ? 0.16 : 0.2));
  tras.add(nuca);
  let pontasDasTrancas = [];
  if (estilo === 'liso') {
    if (menina) {
      for (const lado of [-1, 1]) { const coque = bola(R * 0.38, m, 32); em(coque, lado * R * 0.95, R * 0.42, -R * 0.12); tras.add(coque); }
      frente.add(tiaraTrancada(R, m));
    } else {
      frente.add(mechas(R, m, { curta: true }));
      const topete = bola(R * 0.3, m, 24);
      topete.scale.set(0.8, 1.15, 0.8);
      em(topete, R * 0.12, R * 1.0, R * 0.2);
      topete.rotation.z = -0.5;
      frente.add(topete);
    }
  } else if (estilo === 'cacheado') {
    if (menina) {
      tras.add(cachos(R, m, { de: -55, ate: 235, passo: 20, raio: 0.3, distancia: 1.06, z: -0.28 }));
      tras.add(cachos(R, m, { de: -40, ate: 220, passo: 26, raio: 0.26, distancia: 0.98, z: -0.52 }));
    } else {
      tras.add(cachos(R, m, { de: -5, ate: 185, passo: 21, raio: 0.24, distancia: 1.0, z: -0.3 }));
    }
    // cachinhos na testa e no alto da cabeça
    const n = menina ? 7 : 6;
    for (let i = 0; i < n; i++) {
      const t = -1 + (2 * i) / (n - 1);
      const c = bola(R * (menina ? 0.21 : 0.19), m, 24);
      naSuperficie(c, R * 1.02, t * R * 0.72, R * (0.55 - 0.2 * t * t), -R * 0.03);
      frente.add(c);
    }
    for (const [x, y] of [[-0.42, 0.86], [0.0, 0.92], [0.42, 0.86], [-0.2, 0.98], [0.22, 0.98]]) {
      const c = bola(R * 0.22, m, 24);
      naSuperficie(c, R * 1.04, x * R, y * R, -R * 0.06);
      frente.add(c);
    }
  } else if (estilo === 'trancas') {
    frente.add(mechas(R, m));
    for (const lado of [-1, 1]) {
      for (let k = 0; k < 4; k++) {
        const conta = bola(R * (0.22 - k * 0.012), m, 24);
        conta.scale.set(0.95, 1.15, 0.9);
        em(conta, lado * R * (0.98 + k * 0.05), -R * (0.05 + k * 0.36), -R * 0.1);
        tras.add(conta);
      }
      pontasDasTrancas.push([lado * R * 1.105, -R * 0.95, -R * 0.1]);
    }
  } else if (estilo === 'espetado') {
    frente.add(mechas(R, m, { curta: true }));
    const dirs = [[-0.62, 0.62], [-0.32, 0.86], [0.0, 0.95], [0.32, 0.86], [0.62, 0.62], [-0.16, 0.66], [0.16, 0.66]];
    for (const [x, y] of dirs) {
      const p = ponta(R * 0.2, R * 0.36, m);
      naSuperficie(p, R * 0.98, x * R, y * R, 0);
      frente.add(p);
    }
  }
  return { tras, frente, base, pontasDasTrancas };
}

// ---------------------------------------------------------------------------------------------
// Medidas das figuras (unidades do mundo; o chão é y = 0 e os pés ficam em x = 0, z = 0)

export const MEDIDAS = {
  crianca: {
    R: 0.8, cabeca: [0, 2.42, 0], ombro: [0.5, 1.52, 0], manga: { r: 0.2, pos: [0.53, 1.52, 0] },
    raioBraco: 0.11, raioMao: 0.15,
  },
  irmao: {
    R: 0.68, cabeca: [0, 1.74, 0], ombro: [0.4, 1.04, 0], manga: { r: 0.16, pos: [0.42, 1.05, 0] },
    raioBraco: 0.095, raioMao: 0.13,
  },
};

/**
 * Poses dos braços: para cada lado (-1 = esquerda da tela, 1 = direita), cotovelo e mão relativos ao ombro.
 * O tchau tem 4 quadros (o antebraço balança); torcendo = os dois punhos para cima; pensando = mão no queixo.
 */
function poseDosBracos(pose, quadro, k) {
  // k: escala da figura (1 = criança)
  const baixo = { cotovelo: [0.15, -0.3, 0.03], mao: [0.24, -0.6, 0.08] };
  const p = (v) => v.map((c) => c * k);
  const espelha = (b) => ({ cotovelo: [-b.cotovelo[0], b.cotovelo[1], b.cotovelo[2]], mao: [-b.mao[0], b.mao[1], b.mao[2]] });
  let dir = baixo, esq = espelha(baixo);
  if (pose === 'torcendo') {
    const cima = { cotovelo: [0.3, 0.16, 0.08], mao: [0.4, 0.46, 0.16] };
    dir = cima; esq = espelha(cima);
  } else if (pose === 'tchau') {
    const ang = [-0.42, -0.14, 0.14, 0.42][quadro - 1];
    const cot = [0.33, 0.1, 0.06];
    dir = { cotovelo: cot, mao: [cot[0] + Math.sin(ang) * 0.31, cot[1] + Math.cos(ang) * 0.31, cot[2] + 0.06] };
  } else if (pose === 'pensando') {
    dir = { cotovelo: [0.06, -0.3, 0.3], mao: [-0.2, 0.2, 0.52] };
  }
  const conv = (b) => ({ cotovelo: p(b.cotovelo), mao: p(b.mao) });
  return { [-1]: conv(esq), [1]: conv(dir) };
}

// ---------------------------------------------------------------------------------------------
// Montagem por partes

/**
 * A figura inteira, separada em partes (cada uma é um grupo nas coordenadas do mundo).
 * quem: 'crianca' | 'irmao'; menina; estilo (só criança); pose e quadro dos braços; olhos, cilios,
 * boca e sobrancelhas (humor).
 */
export function partes({ quem = 'crianca', menina = true, estilo = 'liso', pose = 'parado', quadro = 1, olhos: estadoOlhos = 'aberto', cilios = false, boca: humorBoca = 'feliz', sobrancelhas: humorSob = 'feliz' } = {}) {
  const M = MEDIDAS[quem];
  const R = M.R;
  const irmao = quem === 'irmao';
  const k = irmao ? 0.72 : 1; // escala do corpo do irmão em relação à criança
  const P = {};
  const mPele = matPele(), mRoupa = matRoupa();

  // pernas (pele)
  P.corpo = grupo();
  for (const lado of [-1, 1]) {
    const perna = irmao ? capsula(0.11, 0.2, mPele) : capsula(0.13, 0.44, mPele);
    em(perna, lado * (irmao ? 0.16 : 0.2), irmao ? 0.3 : 0.44, 0);
    P.corpo.add(perna);
  }

  // sapatos (e meias da menina): colorido
  P.pes = grupo();
  for (const lado of [-1, 1]) {
    const corSapato = irmao ? (menina ? '#ff4f8b' : '#e63946') : (menina ? '#e8417c' : '#e04848');
    const sapato = bola(1, plastico(corSapato, { rugosidade: 0.2, verniz: 1 }), 40);
    if (irmao) { sapato.scale.set(0.16, 0.11, 0.22); em(sapato, lado * 0.17, 0.11, 0.05); }
    else { sapato.scale.set(0.2, 0.13, 0.28); em(sapato, lado * 0.21, 0.13, 0.06); }
    P.pes.add(sapato);
    if (!irmao && menina) {
      const meia = cilindro(0.142, 0.142, 0.22, plastico('#ffffff', { rugosidade: 0.8, verniz: 0 }));
      em(meia, lado * 0.2, 0.3, 0);
      P.pes.add(meia);
    }
  }

  // roupa (tingida na criança) e estampa (colorida); no irmão a jardineira inteira é colorida
  P.roupa = grupo();
  P.estampa = grupo();
  P.mangas = grupo();
  if (!irmao) {
    if (menina) {
      const perfil = [[0.0, 0.6], [0.7, 0.62], [0.74, 0.7], [0.62, 1.05], [0.46, 1.42], [0.42, 1.62], [0.3, 1.72], [0.0, 1.74]];
      const pts = perfil.map(([x, y]) => new THREE.Vector2(x, y));
      P.roupa.add(malha(new THREE.LatheGeometry(pts, 64), mRoupa));
      // raio do vestido na altura y (entre os pontos do perfil)
      const raioDoVestido = (y) => {
        for (let i = 2; i < 5; i++) {
          const [r0, y0] = perfil[i], [r1, y1] = perfil[i + 1];
          if (y >= y0 && y <= y1) return r0 + ((y - y0) / (y1 - y0)) * (r1 - r0);
        }
        return 0.6;
      };
      for (const [a, y] of [[-0.5, 0.86], [0, 0.92], [0.5, 0.86], [-0.27, 1.2], [0.27, 1.2], [-0.95, 1.02], [0.95, 1.02], [0, 1.42]]) {
        const raio = raioDoVestido(y) + 0.012;
        const p = bola(0.06, plastico('#ffffff', { rugosidade: 0.4, verniz: 0.3 }), 16);
        p.castShadow = false;
        p.scale.set(1, 1, 0.35);
        em(p, Math.sin(a) * raio, y, Math.cos(a) * raio);
        p.lookAt(Math.sin(a) * 5, y, Math.cos(a) * 5);
        P.estampa.add(p);
      }
    } else {
      P.roupa.add(em(caixa(0.98, 0.86, 0.68, 0.3, mRoupa), 0, 1.26, 0));
      const azul = plastico('#2f4f8f', { rugosidade: 0.7, verniz: 0.05 });
      P.estampa.add(em(caixa(0.92, 0.34, 0.64, 0.16, azul), 0, 0.8, 0));
      for (const lado of [-1, 1]) {
        const calca = cilindro(0.155, 0.15, 0.5, azul);
        em(calca, lado * 0.2, 0.56, 0);
        P.estampa.add(calca);
      }
      const estrela = estrela3D(0.17, 0.06, plastico('#ffd23f', { rugosidade: 0.25, verniz: 1 }));
      em(estrela, 0, 1.28, 0.35);
      P.estampa.add(estrela);
    }
    for (const lado of [-1, 1]) {
      const manga = bola(M.manga.r, mRoupa, 32);
      manga.scale.set(1, 0.9, 1);
      em(manga, lado * M.manga.pos[0], M.manga.pos[1], M.manga.pos[2]);
      P.mangas.add(manga);
    }
    P.roupa.add(P.mangas.clone());
  } else {
    const blusa = plastico(menina ? '#c9a7ff' : '#ffd23f', { rugosidade: 0.55, verniz: 0.2 });
    const jard = plastico(menina ? '#ff7a8a' : '#22b8a8', { rugosidade: 0.6, verniz: 0.15 });
    const clarinho = plastico(menina ? '#fff0f3' : '#fff3b0', { rugosidade: 0.25, verniz: 1 });
    P.roupa.add(em(caixa(0.8, 0.72, 0.6, 0.28, blusa), 0, 0.82, 0));
    P.roupa.add(em(caixa(0.82, 0.42, 0.62, 0.2, jard), 0, 0.6, 0));
    P.roupa.add(em(caixa(0.46, 0.34, 0.08, 0.06, jard), 0, 0.9, 0.29));
    for (const lado of [-1, 1]) {
      const alca = caixa(0.09, 0.42, 0.06, 0.03, jard);
      em(alca, lado * 0.2, 1.0, 0.3);
      alca.rotation.z = lado * 0.15;
      P.roupa.add(alca);
      P.roupa.add(em(bola(0.04, clarinho, 12), lado * 0.17, 1.02, 0.35));
    }
    if (menina) {
      const s = new THREE.Shape();
      s.moveTo(0, -0.08);
      s.bezierCurveTo(-0.13, 0.0, -0.08, 0.1, 0, 0.04);
      s.bezierCurveTo(0.08, 0.1, 0.13, 0.0, 0, -0.08);
      const geo = new THREE.ExtrudeGeometry(s, { depth: 0.03, bevelEnabled: true, bevelThickness: 0.015, bevelSize: 0.015, bevelSegments: 3 });
      geo.center();
      P.roupa.add(em(malha(geo, clarinho), 0, 0.89, 0.345));
    } else {
      P.roupa.add(em(estrela3D(0.1, 0.04, clarinho), 0, 0.88, 0.345));
    }
    for (const lado of [-1, 1]) {
      const manga = bola(M.manga.r, blusa, 32);
      em(manga, lado * M.manga.pos[0], M.manga.pos[1], M.manga.pos[2]);
      P.mangas.add(manga);
    }
    P.roupa.add(P.mangas.clone());
  }

  // braços (pele), da pose pedida
  P.bracos = grupo();
  P.maos = {};
  const pose_ = poseDosBracos(pose, quadro, k);
  for (const lado of [-1, 1]) {
    const S = [lado * M.ombro[0], M.ombro[1], M.ombro[2]];
    const b = pose_[lado];
    const E = [S[0] + b.cotovelo[0], S[1] + b.cotovelo[1], S[2] + b.cotovelo[2]];
    const H = [S[0] + b.mao[0], S[1] + b.mao[1], S[2] + b.mao[2]];
    P.bracos.add(segmento(S, E, M.raioBraco, mPele));
    P.bracos.add(segmento(E, H, M.raioBraco * 0.97, mPele));
    P.bracos.add(em(bola(M.raioMao, mPele, 32), ...H));
    P.maos[lado] = H;
  }

  // cabeça (pele): esfera, orelhas e narizinho
  const C = M.cabeca;
  const naCabeca = (...filhos) => { const g = grupo(...filhos); em(g, ...C); return g; };
  const esfera = bola(R, mPele, 72);
  const cab = [esfera];
  for (const lado of [-1, 1]) {
    const o = bola(R * 0.21, mPele, 24);
    o.scale.set(0.8, 1, 0.75);
    em(o, lado * R * 0.97, -R * 0.06, -R * 0.02);
    cab.push(o);
  }
  const nariz = bola(R * 0.07, mPele, 20);
  nariz.scale.set(1.2, 0.85, 0.8);
  naSuperficie(nariz, R, 0, -R * 0.17, -R * 0.03);
  cab.push(nariz);
  P.cabeca = naCabeca(...cab);
  P.esfera = naCabeca(esfera.clone());

  // rosto
  P.olhos = naCabeca(olhos(R, estadoOlhos, cilios));
  P.boca = naCabeca(boca(R, humorBoca));
  P.sobrancelhas = naCabeca(sobrancelhas(R, humorSob));

  // cabelo
  const mCab = matCabelo();
  if (!irmao) {
    const c = cabelo(R, menina, estilo, mCab);
    P.cabeloTras = naCabeca(c.tras);
    P.cabeloFrente = naCabeca(c.frente);
    P.calota = naCabeca(c.base.clone());
    P.enfeite = grupo();
    for (const pt of c.pontasDasTrancas) {
      const elastico = bola(R * 0.13, plastico('#ff4f8b', { rugosidade: 0.25, verniz: 1 }), 24);
      elastico.scale.set(1.6, 0.65, 1.6);
      em(elastico, pt[0], pt[1], pt[2]);
      P.enfeite.add(elastico);
    }
    P.enfeite = naCabeca(P.enfeite);
    P.laco = naCabeca(laco3D(R, '#ff4f8b', R * 0.48, R * 0.92, R * 0.28));
  } else {
    const frente = new THREE.Group();
    const base = calota(R, mCab, { abre: 0.42, inclina: -0.5, escala: 1.04 });
    frente.add(base);
    for (let i = -2; i <= 2; i++) {
      const mecha = bola(R * 0.2, mCab, 20);
      mecha.scale.set(1.3, 0.9, 0.6);
      naSuperficie(mecha, R, i * R * 0.25, R * (0.76 - Math.abs(i) * 0.05), -R * 0.07);
      frente.add(mecha);
    }
    if (menina) {
      // palmeirinha no alto da cabeça
      const tufo = bola(R * 0.26, mCab, 24); tufo.scale.set(1.2, 0.8, 1); em(tufo, R * 0.05, R * 1.12, 0);
      const tufo2 = bola(R * 0.2, mCab, 24); tufo2.scale.set(1.1, 0.8, 1); em(tufo2, R * 0.28, R * 1.22, R * 0.02);
      const tufo3 = bola(R * 0.18, mCab, 24); tufo3.scale.set(1.1, 0.8, 1); em(tufo3, -R * 0.18, R * 1.22, -R * 0.02);
      frente.add(tufo, tufo2, tufo3);
    } else {
      // cachinho no alto
      const geo = new THREE.TorusGeometry(R * 0.12, R * 0.07, 14, 28, Math.PI * 1.45);
      const cacho = malha(geo, mCab);
      em(cacho, R * 0.04, R * 1.1, R * 0.12);
      cacho.rotation.set(-0.2, 0.15, -0.9);
      frente.add(cacho);
    }
    P.cabeloTras = grupo();
    P.cabeloFrente = naCabeca(frente);
    P.calota = naCabeca(base.clone());
    P.enfeite = grupo();
    P.laco = naCabeca(laco3D(R * 0.8, '#ff4f8b', R * 0.05, R * 1.02, R * 0.3));
  }

  // pontos úteis (coordenadas do mundo)
  const V = (x, y, z) => new THREE.Vector3(x, y, z);
  const naCab = (x, y) => V(C[0] + x, C[1] + y, C[2] + Math.sqrt(Math.max(0, R * R - x * x - y * y)));
  P.pontos = {
    cabeca: V(...C),
    topoDaCabeca: V(C[0], C[1] + R * 1.06, C[2]),
    olhoEsquerdo: naCab(-R * OLHO_X, R * OLHO_Y),
    olhoDireito: naCab(R * OLHO_X, R * OLHO_Y),
    boca: naCab(0, -R * 0.36),
    maoEsquerda: V(...P.maos[-1]),
    maoDireita: V(...P.maos[1]),
  };
  return P;
}

// ---------------------------------------------------------------------------------------------
// Papéis das partes numa camada

function comoRecorte(obj, { fazSombra = true } = {}) {
  obj.traverse((o) => {
    if (!o.isMesh) return;
    o.material = matRecorte;
    o.castShadow = fazSombra;
    o.receiveShadow = false;
    o.renderOrder = 1;
  });
  return obj;
}
function comoSombra(obj) {
  obj.traverse((o) => { if (o.isMesh) { o.material = matSoSombra; o.castShadow = true; o.receiveShadow = false; } });
  return obj;
}
function comoCaptador(obj) {
  obj.traverse((o) => { if (o.isMesh) { o.material = matCapta; o.castShadow = false; o.receiveShadow = true; o.renderOrder = 2; } });
  return obj;
}
function comoBrilho(obj) {
  const m = matCabeloBrilho();
  obj.traverse((o) => { if (o.isMesh) o.material = m; });
  return obj;
}

/**
 * Monta uma camada: [ver] são as partes que aparecem; [recorte] escondem o que fica atrás delas;
 * [sombra] só fazem sombra; [capta] só recebem sombra. [ancoras] são nomes de P.pontos.
 */
export function camada(opcoes, { ver = [], recorte = [], sombra = [], capta = [], ancoras = [], brilho = false }) {
  const P = partes(opcoes);
  const g = new THREE.Group();
  for (const nome of ver) { const o = P[nome]; if (brilho) comoBrilho(o); g.add(o); }
  for (const nome of recorte) g.add(comoRecorte(P[nome].clone(true)));
  for (const nome of sombra) g.add(comoSombra(P[nome].clone(true)));
  for (const nome of capta) {
    // capta = recorte (o que está atrás some) + captador (a sombra aparece por cima)
    g.add(comoRecorte(P[nome].clone(true), { fazSombra: false }));
    g.add(comoCaptador(P[nome].clone(true)));
  }
  for (const nome of ancoras) { const p = P.pontos[nome]; ancora(g, nome, p.x, p.y, p.z); }
  return g;
}

/** Sombra de contato macia embaixo dos pés (camada própria, a primeira da pilha). */
export function sombraNoChao(raioX, raioZ) {
  const cv = document.createElement('canvas');
  cv.width = cv.height = 256;
  const ctx = cv.getContext('2d');
  const gr = ctx.createRadialGradient(128, 128, 0, 128, 128, 128);
  gr.addColorStop(0, 'rgba(46,24,74,0.5)');
  gr.addColorStop(0.5, 'rgba(46,24,74,0.24)');
  gr.addColorStop(1, 'rgba(46,24,74,0)');
  ctx.fillStyle = gr;
  ctx.fillRect(0, 0, 256, 256);
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  const plano = new THREE.Mesh(
    new THREE.PlaneGeometry(raioX * 2, raioZ * 2),
    new THREE.MeshBasicMaterial({ map: tex, transparent: true, depthWrite: false, toneMapped: false }),
  );
  plano.rotation.x = -Math.PI / 2;
  plano.position.set(0, 0.003, 0.02);
  const g = new THREE.Group();
  g.add(plano);
  return g;
}
