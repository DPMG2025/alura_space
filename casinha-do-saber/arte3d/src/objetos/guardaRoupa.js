// Guarda-roupa em camadas (PRD visual 3D, seção 3.2, Fase 7): as 16 roupas (trajes), os 18 acessórios e os
// 4 chapéus da loja, montados em volta da MESMA figura da criança (personagens.js > partes) e renderizados na
// mesma câmera 'personagem'. Cada peça é uma camada do quadro inteiro que empilha sobre as camadas da criança.
//
// Os papéis são os da Fase 1A (ver, recorte, sombra, capta); aqui a camada junta as partes da criança com as
// peças do guarda-roupa (camadaDoGuardaRoupa). Medidas e posições vêm de MEDIDAS.crianca.
import { THREE, plastico, ouro, malha, caixa, bola, capsula, cilindro, em, naSuperficie, ancora } from '../kit.js';
import { partes, MEDIDAS, OLHO_X, OLHO_Y } from './personagens.js';
import { estrela3D } from './estrela3D.js';

const M = MEDIDAS.crianca;
const R = M.R;
const C = M.cabeca;
/** Neutro claro para o app tingir com a cor da roupa (o mesmo da roupa básica). */
const NEUTRO = '#d4d4d4';

// ---------------------------------------------------------------------------------------------
// Materiais (os mesmos parâmetros das camadas dos personagens)

/** Tecido: o mesmo material da roupa básica (massinha acetinada). */
const tecido = (cor) => plastico(cor, { rugosidade: 0.6, verniz: 0.2, ambiente: 0.5 });
/** Tecido fosco (pelúcia, lã, jeans). */
const fosco = (cor) => plastico(cor, { rugosidade: 0.78, verniz: 0.04, ambiente: 0.5 });
/** Plástico brilhante (sapatos, botões, enfeites). */
const brilho = (cor) => plastico(cor, { rugosidade: 0.22, verniz: 1 });
/** Vinil (capa de chuva, botas de borracha). */
const vinil = (cor) => plastico(cor, { rugosidade: 0.3, verniz: 0.9, ambiente: 0.55 });
const vidro = (cor, opacidade) => {
  const m = new THREE.MeshPhysicalMaterial({
    color: cor, roughness: 0.05, clearcoat: 1, clearcoatRoughness: 0.04, transparent: true, opacity: opacidade,
    envMapIntensity: 1.4, depthWrite: false, side: THREE.DoubleSide,
  });
  return m;
};

const matRecorte = new THREE.MeshBasicMaterial({
  color: 0x000000, transparent: true, opacity: 0, blending: THREE.NoBlending, depthWrite: true, toneMapped: false,
});
const matSoSombra = new THREE.MeshBasicMaterial({ colorWrite: false, depthWrite: false });
const matCapta = new THREE.ShadowMaterial({ color: new THREE.Color('#2e1830'), opacity: 0.2, depthWrite: false });

// ---------------------------------------------------------------------------------------------
// Utilitários de forma

function grupo(...filhos) {
  const g = new THREE.Group();
  for (const f of filhos) if (f) g.add(f);
  return g;
}
const V = (a) => new THREE.Vector3(...a);

/** Cápsula de [a] até [b]. */
function segmento(a, b, r, mat) {
  const va = V(a), vb = V(b);
  const d = vb.clone().sub(va);
  const m = capsula(r, Math.max(0.001, d.length()), mat);
  m.position.copy(va).add(vb).multiplyScalar(0.5);
  m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), d.normalize());
  return m;
}

/** Sólido de revolução a partir de pares [raio, y]. */
function torno(perfil, mat, seg = 72) {
  return malha(new THREE.LatheGeometry(perfil.map(([r, y]) => new THREE.Vector2(r, y)), seg), mat);
}

/** Raio do perfil [perfil] na altura y. */
function raioNoPerfil(perfil, y) {
  for (let i = 0; i < perfil.length - 1; i++) {
    const [r0, y0] = perfil[i], [r1, y1] = perfil[i + 1];
    if ((y >= y0 && y <= y1) || (y <= y0 && y >= y1)) return y1 === y0 ? r1 : r0 + ((y - y0) / (y1 - y0)) * (r1 - r0);
  }
  return 0.5;
}

/** Tubo por uma curva suave de pontos. */
function tubo(pontos, r, mat, seg = 48) {
  const curva = new THREE.CatmullRomCurve3(pontos.map(V));
  return malha(new THREE.TubeGeometry(curva, seg, r, 12, false), mat);
}

/** Anel (toro) no plano XZ (horizontal), centrado em (0, y, 0). */
function anel(raio, grossura, y, mat, { escalaZ = 1, x = 0, z = 0 } = {}) {
  const t = malha(new THREE.TorusGeometry(raio, grossura, 16, 72), mat);
  t.rotation.x = Math.PI / 2;
  t.scale.set(1, escalaZ, 1);
  t.position.set(x, y, z);
  return t;
}

const _raio = new THREE.Raycaster();
/**
 * Põe [obj] na superfície de [alvo] (um grupo de malhas), no ângulo [ang] em volta do eixo vertical (0 = frente,
 * positivo para a direita da tela) e na altura [y], olhando para fora da superfície.
 */
function sobre(alvo, ang, y, obj, afasta = 0, frente = 0) {
  alvo.updateMatrixWorld(true);
  const o = new THREE.Vector3(Math.sin(ang) * 4, y, Math.cos(ang) * 4);
  _raio.set(o, new THREE.Vector3(0, y, 0).sub(o).normalize());
  const hit = _raio.intersectObject(alvo, true)[0];
  if (!hit) { console.error('sobre: não achou a superfície em', ang, y); return obj; }
  const n = hit.face.normal.clone().transformDirection(hit.object.matrixWorld);
  // [frente] puxa a peça para olhar mais para a câmera (botões e emblemas não ficam de lado)
  if (frente) n.lerp(new THREE.Vector3(0, 0, 1), frente).normalize();
  obj.position.copy(hit.point).addScaledVector(n, afasta);
  obj.lookAt(hit.point.clone().addScaledVector(n, 3));
  return obj;
}

/**
 * Casca colada no [alvo] (afastada [folga] na direção da normal) com uma estampa desenhada num canvas.
 * A textura é presa ao mundo: u = ângulo em volta do eixo vertical (0,5 = frente), v = y / ALTURA. [desenhar]
 * recebe (ctx, L, A, px(y), pu(ang)). Transparente onde o canvas não pinta (alphaTest).
 */
const ALTURA_TEX = 2.2;
function casca(alvo, folga, desenhar, { largura = 512, mat = {} } = {}) {
  const g = new THREE.Group();
  const alvos = Array.isArray(alvo) ? alvo : [alvo];
  for (const a of alvos) a.updateMatrixWorld(true);
  const cv = document.createElement('canvas');
  cv.width = largura; cv.height = 2048;
  const ctx = cv.getContext('2d');
  const py = (y) => (1 - y / ALTURA_TEX) * cv.height;
  const pu = (ang) => (0.5 + ang / (Math.PI * 2)) * cv.width;
  desenhar(ctx, cv.width, cv.height, py, pu);
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  tex.anisotropy = 4;
  const material = new THREE.MeshPhysicalMaterial({
    map: tex, alphaTest: 0.5, roughness: 0.6, clearcoat: 0.2, clearcoatRoughness: 0.25, envMapIntensity: 0.5, ...mat,
  });
  for (const a of alvos) a.traverse((o) => {
    if (!o.isMesh) return;
    const geo = o.geometry.clone().applyMatrix4(o.matrixWorld);
    const pos = geo.attributes.position, nor = geo.attributes.normal;
    const uv = new Float32Array(pos.count * 2);
    for (let i = 0; i < pos.count; i++) {
      const x = pos.getX(i) + nor.getX(i) * folga, y = pos.getY(i) + nor.getY(i) * folga, z = pos.getZ(i) + nor.getZ(i) * folga;
      pos.setXYZ(i, x, y, z);
      uv[i * 2] = 0.5 + Math.atan2(x, z) / (Math.PI * 2);
      uv[i * 2 + 1] = y / ALTURA_TEX;
    }
    geo.setAttribute('uv', new THREE.BufferAttribute(uv, 2));
    const m = new THREE.Mesh(geo, material);
    m.receiveShadow = true;
    g.add(m);
  });
  return g;
}

/** Listras horizontais de [de] a [ate], de [largura], a cada [passo]. */
const listras = (cor, de, ate, passo, largura) => (ctx, L, A, py) => {
  ctx.fillStyle = cor;
  for (let y = de; y <= ate; y += passo) ctx.fillRect(0, py(y + largura / 2), L, py(y - largura / 2) - py(y + largura / 2));
};
/** Faixa horizontal de [y0] a [y1]. */
const faixa = (cor, y0, y1) => (ctx, L, A, py) => { ctx.fillStyle = cor; ctx.fillRect(0, py(y1), L, py(y0) - py(y1)); };

/** Estrela de 4 pontas (brilhinho), extrudada. */
function brilhinho(r, mat) {
  const s = new THREE.Shape();
  for (let i = 0; i < 8; i++) {
    const a = Math.PI / 2 + (i * Math.PI) / 4;
    const rr = i % 2 === 0 ? r : r * 0.32;
    const x = Math.cos(a) * rr, y = Math.sin(a) * rr;
    if (i === 0) s.moveTo(x, y); else s.lineTo(x, y);
  }
  s.closePath();
  const geo = new THREE.ExtrudeGeometry(s, { depth: r * 0.15, bevelEnabled: true, bevelThickness: r * 0.12, bevelSize: r * 0.08, bevelSegments: 3 });
  geo.center();
  return malha(geo, mat, { sombra: false });
}

/** Florzinha (5 pétalas e miolo) virada para +z. */
function florzinha(r, corPetala, corMiolo, { petalas = 5, achata = 0.32 } = {}) {
  const g = new THREE.Group();
  const mp = brilho(corPetala);
  for (let i = 0; i < petalas; i++) {
    const a = (i / petalas) * Math.PI * 2 + Math.PI / 2;
    const p = bola(r * 0.5, mp, 20);
    p.scale.set(1, 1.15, achata);
    p.position.set(Math.cos(a) * r * 0.55, Math.sin(a) * r * 0.55, 0);
    p.rotation.z = a - Math.PI / 2;
    p.castShadow = false;
    g.add(p);
  }
  const m = bola(r * 0.36, brilho(corMiolo), 20);
  m.scale.set(1, 1, 0.6);
  m.position.z = r * 0.1;
  m.castShadow = false;
  g.add(m);
  return g;
}

/** Laço (duas asas e o nó), virado para +z, de largura ~ 2r. */
function lacinho(r, mat) {
  const g = new THREE.Group();
  for (const lado of [-1, 1]) {
    const asa = malha(new THREE.ConeGeometry(r * 0.5, r * 0.95, 28), mat);
    asa.rotation.z = lado * Math.PI / 2;
    asa.scale.set(1, 1, 0.55);
    em(asa, lado * r * 0.45, 0, 0);
    g.add(asa);
  }
  const no = bola(r * 0.24, mat, 20);
  no.scale.set(1, 1.1, 0.8);
  g.add(no);
  return g;
}

/** Botãozinho redondo virado para +z. */
function botao(r, mat) {
  const b = bola(r, mat, 20);
  b.scale.set(1, 1, 0.45);
  b.castShadow = false;
  return b;
}

/** Contorno extrudado com bisel macio, centrado, virado para +z. */
function extrudado(forma, prof, mat, bisel = prof * 0.5) {
  const geo = new THREE.ExtrudeGeometry(forma, { depth: prof, bevelEnabled: true, bevelThickness: bisel, bevelSize: bisel, bevelSegments: 4, curveSegments: 24 });
  geo.center();
  return malha(geo, mat);
}

function retanguloRedondo(l, a, r) {
  const s = new THREE.Shape();
  const x = -l / 2, y = -a / 2;
  s.moveTo(x + r, y);
  s.lineTo(x + l - r, y); s.quadraticCurveTo(x + l, y, x + l, y + r);
  s.lineTo(x + l, y + a - r); s.quadraticCurveTo(x + l, y + a, x + l - r, y + a);
  s.lineTo(x + r, y + a); s.quadraticCurveTo(x, y + a, x, y + a - r);
  s.lineTo(x, y + r); s.quadraticCurveTo(x, y, x + r, y);
  return s;
}

// ---------------------------------------------------------------------------------------------
// Partes de corpo das roupas (medidas da criança: as mesmas da roupa básica)

/** Perfil do vestido básico (personagens.js). */
const PERFIL_VESTIDO = [[0.0, 0.6], [0.7, 0.62], [0.74, 0.7], [0.62, 1.05], [0.46, 1.42], [0.42, 1.62], [0.3, 1.72], [0.0, 1.74]];

/** Blusa do menino (a mesma caixa da roupa básica), de [baixo] até 1,69. */
function blusa(mat, { baixo = 0.83, largura = 0.98, fundo = 0.68 } = {}) {
  const alto = 1.69;
  return em(caixa(largura, alto - baixo, fundo, 0.3, mat), 0, (alto + baixo) / 2, 0);
}

/** Calça: cós e duas pernas até [ate] (a calça básica vai até 0,31). */
function calca(mat, { ate = 0.31, raio = 0.155, cintura = 0.8 } = {}) {
  const g = new THREE.Group();
  g.add(em(caixa(0.92, 0.34, 0.64, 0.16, mat), 0, cintura, 0));
  const alto = 0.81;
  for (const lado of [-1, 1]) {
    const perna = cilindro(raio, raio * 0.97, alto - ate, mat);
    em(perna, lado * 0.2, (alto + ate) / 2, 0);
    g.add(perna);
  }
  return g;
}

/** Meia-calça (cobre as pernas de pele) de [ate] até o quadril. */
function meiaCalca(mat, ate = 0.24) {
  const g = new THREE.Group();
  for (const lado of [-1, 1]) {
    const perna = capsula(0.137, 0.8 - ate, mat);
    em(perna, lado * 0.2, (0.8 + ate) / 2, 0);
    g.add(perna);
  }
  return g;
}

/** Manguinhas curtas nos ombros (no lugar das mangas básicas: o braço sai delas). */
function mangasCurtas(mat, r = M.manga.r) {
  const g = new THREE.Group();
  for (const lado of [-1, 1]) {
    const m = bola(r, mat, 32);
    m.scale.set(1, 0.9, 1);
    em(m, lado * M.manga.pos[0], M.manga.pos[1], M.manga.pos[2]);
    g.add(m);
  }
  return g;
}

/** Sapatos básicos (só para recorte: a calça não cobre o sapato). */
function sapatosBase() {
  const g = new THREE.Group();
  for (const lado of [-1, 1]) {
    const s = bola(1, matRecorte, 32);
    s.scale.set(0.2, 0.13, 0.28);
    em(s, lado * 0.21, 0.13, 0.06);
    g.add(s);
  }
  return g;
}

/**
 * Tronco mínimo (cilindro achatado dentro de qualquer roupa): recorta o que fica atrás do corpo
 * (a parte de trás do colar, do cachecol e das alças da mochila).
 */
function tronco() {
  const t = cilindro(0.42, 0.42, 0.8, matRecorte, 32);
  t.scale.set(1, 1, 0.72);
  em(t, 0, 1.25, 0);
  return grupo(t);
}

/** Sapato em forma de ovo (como o básico), com [cor] e [escala]. */
function sapato(lado, mat, { esc = [0.2, 0.13, 0.28], pos = [0.21, 0.13, 0.06] } = {}) {
  const s = bola(1, mat, 40);
  s.scale.set(...esc);
  em(s, lado * pos[0], pos[1], pos[2]);
  return s;
}

/** Bota: cano de [ate] e pé em ovo; [dobra] = barra de outra cor em cima. */
function botas(matCano, { ate = 0.55, raio = 0.16, matDobra = null, matPe = matCano, matSola = null } = {}) {
  const g = new THREE.Group();
  for (const lado of [-1, 1]) {
    const cano = cilindro(raio, raio * 0.95, ate - 0.12, matCano);
    em(cano, lado * 0.205, (ate + 0.12) / 2, 0);
    g.add(cano);
    g.add(sapato(lado, matPe, { esc: [0.215, 0.14, 0.3], pos: [0.21, 0.13, 0.07] }));
    if (matDobra) g.add(anel(raio * 1.02, 0.045, ate - 0.02, matDobra, { x: lado * 0.205 }));
    if (matSola) {
      const s = bola(1, matSola, 32);
      s.scale.set(0.22, 0.05, 0.31);
      em(s, lado * 0.21, 0.035, 0.07);
      g.add(s);
    }
  }
  return g;
}

// ---------------------------------------------------------------------------------------------
// Pontos dos braços para cada pose (mangas compridas e objetos na mão)

/** Ombro, cotovelo e mão de cada lado (-1 = esquerda da tela), lidos da figura montada. */
function juntas(P) {
  const filhos = P.bracos.children;
  const res = {};
  for (const [i, lado] of [[0, -1], [3, 1]]) {
    const S = V([lado * M.ombro[0], M.ombro[1], M.ombro[2]]);
    const E = filhos[i].position.clone().multiplyScalar(2).sub(S);
    const H = V(P.maos[lado]);
    res[lado] = { S, E, H };
  }
  return res;
}

/** Mangas compridas na pose da figura: cobrem o braço até o punho; a mão continua da camada dos braços. */
function mangasCompridas(P, mat, matPunho) {
  const g = new THREE.Group();
  const J = juntas(P);
  for (const lado of [-1, 1]) {
    const { S, E, H } = J[lado];
    const W = E.clone().lerp(H, 0.7);
    g.add(segmento(S.toArray(), E.toArray(), 0.155, mat));
    g.add(segmento(E.toArray(), W.toArray(), 0.15, mat));
    const ombro = bola(M.manga.r * 1.02, mat, 32);
    ombro.scale.set(1, 0.92, 1);
    em(ombro, lado * M.manga.pos[0], M.manga.pos[1], M.manga.pos[2]);
    g.add(ombro);
    // punho: anel no fim da manga, de frente para o antebraço
    const punho = malha(new THREE.TorusGeometry(0.148, 0.045, 14, 40), matPunho);
    punho.position.copy(W);
    punho.quaternion.setFromUnitVectors(new THREE.Vector3(0, 0, 1), H.clone().sub(E).normalize());
    g.add(punho);
  }
  return g;
}

/** Só as mãos (recorte: a manga não pinta por cima da mão). */
function maos(P) {
  const g = new THREE.Group();
  for (const lado of [-1, 1]) g.add(em(bola(M.raioMao, matRecorte, 24), ...P.maos[lado]));
  return g;
}

// ---------------------------------------------------------------------------------------------
// Peças de cabeça (coordenadas da cabeça; o grupo vai para C)

const naCabeca = (...filhos) => { const g = grupo(...filhos); em(g, ...C); return g; };

/** Calota (meia esfera) por cima do cabelo, inclinada para trás como a calota do cabelo. */
function cupula(raio, mat, { abre = 0.5, inclina = -0.35 } = {}) {
  const c = malha(new THREE.SphereGeometry(raio, 64, 32, 0, Math.PI * 2, 0, Math.PI * abre), mat);
  c.rotation.x = inclina;
  return c;
}

// ---------------------------------------------------------------------------------------------
// Trajes. Cada um devolve { corpo, estampa?, tras?, pes?, cabeca?, mangas?: [mat, matPunho], tingir }.
// corpo: tingido com a cor da roupa quando [tingir] (NEUTRO), senão colorido.

const AZUL_JEANS = '#3e6fb0';

function trajeListras(menina) {
  const n = tecido(NEUTRO);
  const corpo = menina ? grupo(torno(PERFIL_VESTIDO, n), mangasCurtas(n)) : grupo(blusa(n), mangasCurtas(n));
  const branco = '#ffffff';
  const estampa = grupo(casca(corpo, 0.006, listras(branco, menina ? 0.7 : 0.9, 1.66, 0.13, 0.055)));
  if (!menina) {
    const azul = tecido('#2f4f8f');
    estampa.add(calca(azul));
  } else {
    // golinha branca
    estampa.add(anel(0.3, 0.035, 1.71, brilho('#ffffff'), { escalaZ: 0.95 }));
  }
  return { corpo, estampa, tingir: true };
}

function trajeHeroi(menina) {
  const n = tecido(NEUTRO);
  const corpo = menina
    ? grupo(torno([[0, 0.78], [0.62, 0.8], [0.66, 0.86], [0.58, 1.1], [0.46, 1.42], [0.42, 1.62], [0.3, 1.72], [0, 1.74]], n), mangasCurtas(n), meiaCalca(n))
    : grupo(blusa(n), mangasCurtas(n), calca(n, { ate: 0.3 }));
  const amarelo = brilho('#ffc61a');
  const estampa = new THREE.Group();
  // emblema: disco amarelo com raio vermelho
  const emblema = new THREE.Group();
  const disco = cilindro(0.17, 0.17, 0.035, amarelo, 40);
  disco.rotation.x = Math.PI / 2;
  emblema.add(disco);
  const s = new THREE.Shape();
  s.moveTo(0.03, 0.12); s.lineTo(-0.07, -0.005); s.lineTo(0.0, -0.005); s.lineTo(-0.035, -0.12);
  s.lineTo(0.075, 0.025); s.lineTo(0.005, 0.025); s.lineTo(0.05, 0.12); s.closePath();
  const raio = extrudado(s, 0.02, brilho('#e63946'), 0.008);
  raio.position.z = 0.03;
  emblema.add(raio);
  estampa.add(sobre(corpo, 0, 1.32, emblema, 0.01, 0.5));
  // cinto amarelo com fivela
  estampa.add(casca(corpo, 0.01, faixa('#ffc61a', menina ? 1.04 : 0.95, menina ? 1.12 : 1.03), { mat: { roughness: 0.25, clearcoat: 1 } }));
  const fivela = extrudado(retanguloRedondo(0.15, 0.1, 0.03), 0.03, ouro());
  estampa.add(sobre(corpo, 0, menina ? 1.08 : 0.99, fivela, 0.025, 0.5));
  // presilhas da capa nos ombros
  for (const lado of [-1, 1]) estampa.add(em(bola(0.06, ouro(), 20), lado * 0.36, 1.66, 0.22));
  const capa = capaNasCostas(brilhoTecido('#e63946'), { comprida: 0.32 });
  const pes = botas(brilho('#e63946'), { ate: 0.5, matDobra: amarelo });
  return { corpo, estampa, tras: capa, pes, tingir: true };
}

/** Tecido acetinado (capa, veludo). */
function brilhoTecido(cor) { return plastico(cor, { rugosidade: 0.42, verniz: 0.45, ambiente: 0.55 }); }

/** Capa presa nos ombros, atrás do corpo, abrindo até [comprida] de altura. */
function capaNasCostas(mat, { comprida = 0.3, estrelas = null } = {}) {
  const g = new THREE.Group();
  const alto = 1.66, baixo = comprida;
  const geo = new THREE.CylinderGeometry(0.46, 0.86, alto - baixo, 64, 8, true, Math.PI * 0.42, Math.PI * 1.16);
  // ondinhas na barra
  const pos = geo.attributes.position;
  for (let i = 0; i < pos.count; i++) {
    const y = pos.getY(i), x = pos.getX(i), z = pos.getZ(i);
    const t = (0.5 - y / (alto - baixo)); // 0 em cima, 1 embaixo
    const a = Math.atan2(x, z);
    const onda = 1 + Math.sin(a * 7) * 0.05 * t;
    pos.setXYZ(i, x * onda, y, z * onda);
  }
  geo.computeVertexNormals();
  const m = new THREE.Mesh(geo, mat.clone());
  m.material.side = THREE.DoubleSide;
  m.castShadow = true; m.receiveShadow = true;
  m.scale.set(1, 1, 0.75);
  em(m, 0, (alto + baixo) / 2, -0.06);
  g.add(m);
  if (estrelas) {
    for (const [a, y] of [[-1.25, 0.55], [1.25, 0.55], [-1.4, 1.0], [1.4, 1.0], [-1.6, 0.4], [1.6, 0.4]]) {
      const e = estrela3D(0.075, 0.025, estrelas);
      sobre(m, a, y, e, 0.012);
      g.add(e);
    }
  }
  return g;
}

function trajeAbelha() {
  const amarelo = tecido('#ffc21a');
  const preto = tecido('#2b2b33');
  const perfil = [[0, 0.62], [0.55, 0.64], [0.7, 0.8], [0.75, 1.0], [0.68, 1.3], [0.5, 1.56], [0.36, 1.7], [0, 1.74]];
  const barriga = torno(perfil, amarelo);
  const corpo = grupo(barriga, mangasCurtas(preto), meiaCalca(preto, 0.26));
  corpo.add(casca([barriga], 0.006, listras('#2b2b33', 0.82, 1.42, 0.27, 0.12)));
  // ferrãozinho de trás não aparece; golinha preta
  corpo.add(anel(0.3, 0.04, 1.71, preto, { escalaZ: 0.95 }));
  // asinhas transparentes
  const tras = new THREE.Group();
  const matAsa = vidro('#dff3ff', 0.55);
  for (const lado of [-1, 1]) {
    for (const [dy, esc, giro] of [[0.16, 0.42, 0.5], [-0.14, 0.3, 0.95]]) {
      const asa = bola(1, matAsa, 32);
      asa.scale.set(esc, esc * 0.62, 0.03);
      asa.position.set(lado * (0.42 + esc * 0.7), 1.38 + dy, -0.42);
      asa.rotation.set(0, lado * 0.35, lado * giro);
      asa.castShadow = false;
      tras.add(asa);
      const borda = malha(new THREE.TorusGeometry(1, 0.035, 8, 48), brilho('#ffffff'));
      borda.scale.set(esc, esc * 0.62, 1);
      borda.position.copy(asa.position);
      borda.rotation.copy(asa.rotation);
      tras.add(borda);
    }
  }
  // anteninhas
  const cabeca = antenas();
  return { corpo, tras, cabeca, tingir: false };
}

function antenas() {
  const preto = brilho('#2b2b33');
  const g = new THREE.Group();
  const tiara = malha(new THREE.TorusGeometry(R * 1.08, R * 0.05, 12, 64, Math.PI * 0.9), preto);
  tiara.geometry.rotateZ(Math.PI / 2 - Math.PI * 0.45);
  tiara.rotation.x = -0.32;
  g.add(tiara);
  for (const lado of [-1, 1]) {
    const base = [lado * R * 0.42, R * 0.92, R * 0.18];
    const ponta = [lado * R * 0.78, R * 1.62, R * 0.28];
    g.add(tubo([base, [lado * R * 0.5, R * 1.3, R * 0.25], ponta], R * 0.04, preto, 24));
    g.add(em(bola(R * 0.13, brilho('#ffc21a'), 24), ...ponta));
  }
  return naCabeca(g);
}

function trajeFada() {
  const n = tecido(NEUTRO);
  const perfil = [[0, 0.8], [0.62, 0.82], [0.66, 0.88], [0.58, 1.1], [0.46, 1.42], [0.42, 1.62], [0.3, 1.72], [0, 1.74]];
  const corpo = grupo(torno(perfil, n), mangasCurtas(n));
  // barra de pétalas
  for (let i = 0; i < 11; i++) {
    const a = (i / 11) * Math.PI * 2;
    const p = bola(1, n, 24);
    p.scale.set(0.19, 0.28, 0.07);
    const r = 0.62;
    p.position.set(Math.sin(a) * r, 0.74, Math.cos(a) * r);
    p.lookAt(Math.sin(a) * 3, 0.74 - 0.9, Math.cos(a) * 3);
    corpo.add(p);
  }
  const estampa = new THREE.Group();
  const branco = brilho('#ffffff');
  for (const [a, y, r] of [[-0.5, 1.0, 0.07], [0.35, 1.18, 0.06], [-0.15, 1.36, 0.05], [0.6, 0.95, 0.055], [0.05, 0.96, 0.05], [-0.75, 1.25, 0.05]]) {
    estampa.add(sobre(corpo, a, y, brilhinho(r, branco), 0.008));
  }
  const gema = bola(0.06, brilho('#ff6fae'), 24);
  gema.scale.set(1, 1, 0.6);
  estampa.add(sobre(corpo, 0, 1.56, gema, 0.01));
  // asas de fada: duas grandes em cima, duas menores embaixo, transparentes com borda prateada
  const tras = new THREE.Group();
  const matAsa = vidro('#e6d2ff', 0.6);
  const matAsa2 = vidro('#ffd2ef', 0.6);
  for (const lado of [-1, 1]) {
    for (const [dy, esc, giro, mat] of [[0.28, 0.5, 0.75, matAsa], [-0.12, 0.36, 2.1, matAsa2]]) {
      const s = new THREE.Shape();
      s.moveTo(0, 0);
      s.bezierCurveTo(0.2, 0.25, 0.9, 0.35, 1.0, 0.0);
      s.bezierCurveTo(1.05, -0.3, 0.3, -0.25, 0, 0);
      const geo = new THREE.ShapeGeometry(s, 24);
      const asa = new THREE.Mesh(geo, mat);
      asa.scale.set(lado * esc * 1.25, esc * 1.25, 1);
      asa.position.set(lado * 0.2, 1.36 + dy * 0.6, -0.36);
      asa.rotation.set(0, lado * 0.3, lado * (giro - 0.75) * 0.5);
      tras.add(asa);
      const pts = s.getPoints(40).map((p) => new THREE.Vector3(p.x, p.y, 0));
      const borda = malha(new THREE.TubeGeometry(new THREE.CatmullRomCurve3(pts, true), 80, 0.02, 8, true), plastico('#f4f0ff', { rugosidade: 0.2, verniz: 1 }));
      borda.scale.copy(asa.scale);
      borda.position.copy(asa.position);
      borda.rotation.copy(asa.rotation);
      tras.add(borda);
    }
  }
  return { corpo, estampa, tras, tingir: true };
}

function trajeAstronauta() {
  const branco = tecido('#f2f4f8');
  const cinza = tecido('#9aa3b5');
  const corpo = grupo(blusa(branco, { largura: 1.02, fundo: 0.72 }), mangasCurtas(branco, M.manga.r * 1.04));
  const pernas = calca(branco, { ate: 0.36, raio: 0.17 });
  corpo.add(pernas);
  for (const lado of [-1, 1]) corpo.add(anel(0.172, 0.04, 0.55, cinza, { x: lado * 0.2 }));
  // cinto e gola
  corpo.add(casca([corpo.children[0]], 0.012, faixa('#9aa3b5', 0.9, 0.99)));
  corpo.add(anel(0.4, 0.075, 1.66, cinza, { escalaZ: 0.9 }));
  // painel do peito
  const painel = new THREE.Group();
  painel.add(extrudado(retanguloRedondo(0.32, 0.2, 0.05), 0.03, brilho('#4d96ff')));
  for (const [x, cor] of [[-0.09, '#ef476f'], [0, '#ffd23f'], [0.09, '#3dbe6b']]) painel.add(em(botao(0.03, brilho(cor)), x, -0.02, 0.035));
  painel.add(em(caixa(0.2, 0.03, 0.02, 0.01, brilho('#dff3ff')), 0, 0.05, 0.03));
  corpo.add(sobre(corpo.children[0], 0, 1.3, painel, 0.02, 1));
  const bandeira = extrudado(retanguloRedondo(0.11, 0.075, 0.015), 0.01, brilho('#e63946'));
  corpo.add(sobre(corpo.children[0], -0.85, 1.42, bandeira, 0.012));
  const pes = botas(cinza, { ate: 0.4, raio: 0.19, matPe: tecido('#c9cfdb'), matSola: brilho('#6b7385') });
  const cabeca = capacete();
  return { corpo, pes, cabeca, mangas: [branco, cinza], tingir: false };
}

function capacete() {
  const g = new THREE.Group();
  const raio = R * 1.32;
  const bolha = new THREE.Mesh(new THREE.SphereGeometry(raio, 64, 40, 0, Math.PI * 2, 0, Math.PI * 0.78), vidro('#cfeaff', 0.16));
  bolha.position.set(0, R * 0.04, R * 0.02);
  g.add(bolha);
  // reflexo: faixa clara do lado da luz
  const reflexo = malha(new THREE.TorusGeometry(raio * 0.86, raio * 0.035, 8, 40, Math.PI * 0.32), new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: 0.75, toneMapped: false }), { sombra: false });
  reflexo.rotation.set(0.35, -0.5, 0.55);
  reflexo.position.set(0, R * 0.04, raio * 0.35);
  g.add(reflexo);
  const reflexo2 = bola(raio * 0.05, new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: 0.8, toneMapped: false }), 16);
  reflexo2.castShadow = false;
  naSuperficie(reflexo2, raio, raio * 0.45, raio * 0.55, 0);
  g.add(reflexo2);
  // aro de baixo
  const aro = malha(new THREE.TorusGeometry(raio * Math.sin(Math.PI * 0.78), R * 0.08, 14, 64), tecido('#c9cfdb'));
  aro.rotation.x = Math.PI / 2;
  aro.position.y = R * 0.04 + raio * Math.cos(Math.PI * 0.78);
  g.add(aro);
  return naCabeca(g);
}

function trajeVestido() {
  const n = tecido(NEUTRO);
  const perfil = [[0, 0.56], [0.68, 0.57], [0.78, 0.64], [0.79, 0.7], [0.64, 1.02], [0.47, 1.4], [0.42, 1.62], [0.3, 1.72], [0, 1.74]];
  const saia = torno(perfil, n);
  const corpo = grupo(saia, mangasCurtas(n, M.manga.r * 1.08));
  // babado na barra
  for (let i = 0; i < 26; i++) {
    const a = (i / 26) * Math.PI * 2;
    corpo.add(em(bola(0.075, n, 16), Math.sin(a) * 0.76, 0.6, Math.cos(a) * 0.76));
  }
  const estampa = new THREE.Group();
  // faixa branca na cintura com lacinho
  estampa.add(casca([saia], 0.008, faixa('#ffffff', 1.22, 1.31), { mat: { roughness: 0.3, clearcoat: 0.9 } }));
  estampa.add(sobre(saia, 0.2, 1.265, lacinho(0.12, brilho('#ffffff')), 0.04, 0.4));
  for (const [a, y] of [[0, 1.48], [-0.55, 1.05], [0.5, 1.12], [-0.05, 0.92], [0.95, 0.85], [-1.0, 0.8], [0.3, 0.72], [-0.4, 0.7], [0.7, 1.45], [-0.75, 1.42]]) {
    estampa.add(sobre(saia, a, y, florzinha(0.1, '#ffffff', '#ffc21a'), 0.012, 0.2));
  }
  return { corpo, estampa, tingir: true };
}

function trajeMacacao() {
  const n = tecido(NEUTRO);
  const corpo = grupo(blusa(n), mangasCurtas(n));
  const jeans = fosco(AZUL_JEANS);
  const escuro = tecido('#2f5a94');
  const estampa = new THREE.Group();
  estampa.add(calca(jeans, { ate: 0.36, raio: 0.165 }));
  for (const lado of [-1, 1]) estampa.add(anel(0.17, 0.05, 0.37, escuro, { x: lado * 0.2 }));
  // peitilho, alças e botões
  const peito = caixa(0.56, 0.5, 0.08, 0.06, jeans);
  em(peito, 0, 1.16, 0.33);
  estampa.add(peito);
  estampa.add(em(caixa(0.26, 0.16, 0.04, 0.03, escuro), 0, 1.17, 0.38));
  for (const lado of [-1, 1]) {
    estampa.add(segmento([lado * 0.22, 1.38, 0.36], [lado * 0.27, 1.62, 0.3], 0.05, jeans));
    estampa.add(segmento([lado * 0.27, 1.62, 0.3], [lado * 0.3, 1.72, 0.05], 0.05, jeans));
    estampa.add(em(botao(0.045, brilho('#ffc21a')), lado * 0.21, 1.36, 0.385));
  }
  return { corpo, estampa, tingir: true };
}

function trajeFutebol() {
  const amarelo = tecido('#ffd23f');
  const verde = brilho('#22a85a');
  const camisa = blusa(amarelo);
  const corpo = grupo(camisa, mangasCurtas(amarelo));
  corpo.add(casca([camisa], 0.008, (ctx, L, A, py, pu) => {
    ctx.fillStyle = '#22a85a';
    for (const a of [-Math.PI / 2, Math.PI / 2]) ctx.fillRect(pu(a) - L * 0.025, 0, L * 0.05, A);
  }));
  for (const lado of [-1, 1]) corpo.add(anel(0.17, 0.04, 1.42, verde, { x: lado * 0.62 }));
  // gola em V
  const gola = new THREE.Shape();
  gola.moveTo(-0.17, 0.08); gola.lineTo(0, -0.12); gola.lineTo(0.17, 0.08); gola.lineTo(0.1, 0.08); gola.lineTo(0, -0.03); gola.lineTo(-0.1, 0.08); gola.closePath();
  corpo.add(sobre(camisa, 0, 1.58, extrudado(gola, 0.015, verde, 0.01), 0.015, 0.6));
  // bolinha de futebol no peito
  const b = new THREE.Group();
  b.add(bola(0.1, brilho('#ffffff'), 32));
  for (const [x, y] of [[0, 0], [0.065, 0.05], [-0.065, 0.05], [0.04, -0.07], [-0.04, -0.07]]) {
    const p = bola(0.032, brilho('#22222a'), 12);
    p.scale.set(1, 1, 0.5);
    naSuperficie(p, 0.1, x, y, -0.006);
    b.add(p);
  }
  corpo.add(sobre(camisa, 0.3, 1.24, b, 0.04, 0.4));
  // calção azul
  corpo.add(calca(tecido('#2b59c3'), { ate: 0.6, raio: 0.172 }));
  const pes = new THREE.Group();
  const meia = tecido('#ffffff');
  for (const lado of [-1, 1]) {
    pes.add(em(cilindro(0.145, 0.14, 0.4, meia), lado * 0.2, 0.32, 0));
    pes.add(anel(0.148, 0.03, 0.46, verde, { x: lado * 0.2 }));
    pes.add(sapato(lado, brilho('#25252d')));
    for (const k of [0, 1]) {
      const risca = caixa(0.03, 0.04, 0.2, 0.012, brilho('#ffffff'));
      risca.position.set(lado * (0.21 + 0.185), 0.14, 0.02 + k * 0.08);
      pes.add(risca);
    }
  }
  return { corpo, pes, tingir: false };
}

function trajeBailarina() {
  const rosa = tecido('#ffb3d1');
  const perfil = [[0, 0.82], [0.42, 0.82], [0.48, 0.9], [0.46, 1.2], [0.44, 1.5], [0.42, 1.62], [0.3, 1.72], [0, 1.74]];
  const corpete = torno(perfil, rosa);
  const corpo = grupo(corpete, meiaCalca(tecido('#ffd6e6'), 0.2));
  // manguinhas bufantes de tule
  corpo.add(mangasCurtas(tecido('#ffc7dd'), M.manga.r * 1.06));
  // saia de tule: camadas onduladas
  const camadas = [[0.98, 1.0, '#ffd1e3', 0.86], [0.92, 0.96, '#ffe3ee', 0.92], [0.84, 0.92, '#ffffff', 0.95]];
  camadas.forEach(([raio, y, cor], k) => {
    const geo = new THREE.LatheGeometry([new THREE.Vector2(0.42, y + 0.05), new THREE.Vector2(raio * 0.7, y - 0.02), new THREE.Vector2(raio, y - 0.12)], 96);
    const pos = geo.attributes.position;
    for (let i = 0; i < pos.count; i++) {
      const x = pos.getX(i), z = pos.getZ(i), yy = pos.getY(i);
      const r = Math.hypot(x, z);
      const a = Math.atan2(x, z);
      pos.setY(i, yy + Math.sin(a * 11 + k) * 0.035 * (r - 0.42) / 0.5);
    }
    geo.computeVertexNormals();
    const mat = plastico(cor, { rugosidade: 0.5, verniz: 0.3, ambiente: 0.6 }).clone();
    mat.side = THREE.DoubleSide;
    const m = new THREE.Mesh(geo, mat);
    m.castShadow = true; m.receiveShadow = true;
    m.position.y = -0.04 * k;
    corpo.add(m);
  });
  corpo.add(anel(0.47, 0.035, 0.98, brilho('#ff8fb8')));
  for (const [a, y] of [[0, 1.42], [-0.45, 1.2], [0.4, 1.28], [0.1, 1.08]]) corpo.add(sobre(corpete, a, y, brilhinho(0.05, brilho('#ffffff')), 0.006));
  const pes = new THREE.Group();
  const cetim = brilho('#ff8fb8');
  for (const lado of [-1, 1]) {
    pes.add(sapato(lado, cetim, { esc: [0.17, 0.11, 0.26], pos: [0.21, 0.11, 0.07] }));
    // fitinhas cruzadas no tornozelo
    pes.add(anel(0.142, 0.022, 0.27, cetim, { x: lado * 0.2 }));
    pes.add(anel(0.14, 0.022, 0.36, cetim, { x: lado * 0.2 }));
  }
  return { corpo, pes, tingir: false };
}

function trajeChuva() {
  const amarelo = vinil('#ffcf24');
  const perfil = [[0, 0.52], [0.66, 0.52], [0.71, 0.57], [0.62, 1.0], [0.5, 1.4], [0.45, 1.6], [0.32, 1.72], [0, 1.74]];
  const capa = torno(perfil, amarelo);
  const corpo = grupo(capa, mangasCurtas(amarelo));
  corpo.add(casca([capa], 0.004, (ctx, L, A, py, pu) => { ctx.fillStyle = '#e8a90c'; ctx.fillRect(pu(0) - 2, py(1.7), 4, py(0.52) - py(1.7)); }));
  for (const y of [1.45, 1.2, 0.95]) {
    const t = capsula(0.03, 0.1, brilho('#8d5a3b'));
    t.rotation.z = Math.PI / 2;
    corpo.add(sobre(capa, 0, y, grupo(t), 0.035, 0.3));
  }
  for (const lado of [-1, 1]) {
    const bolso = caixa(0.2, 0.13, 0.04, 0.03, vinil('#f2b705'));
    corpo.add(sobre(capa, lado * 0.62, 0.86, bolso, 0.015));
  }
  const pes = botas(vinil('#ffcf24'), { ate: 0.52, raio: 0.165, matSola: brilho('#c98a00') });
  const cabeca = chapeuDeChuva();
  return { corpo, pes, cabeca, mangas: [amarelo, vinil('#f2b705')], tingir: false };
}

function chapeuDeChuva() {
  const amarelo = vinil('#ffcf24');
  const g = new THREE.Group();
  g.add(cupula(R * 1.12, amarelo, { abre: 0.5, inclina: -0.3 }));
  // aba larga, caindo mais atrás (chapéu de pescador)
  const aba = new THREE.LatheGeometry([new THREE.Vector2(R * 1.0, 0.02), new THREE.Vector2(R * 1.22, -0.03), new THREE.Vector2(R * 1.45, -0.13)], 80);
  const mat = amarelo.clone(); mat.side = THREE.DoubleSide;
  const a = new THREE.Mesh(aba, mat);
  a.castShadow = true; a.receiveShadow = true;
  a.rotation.x = -0.3;
  a.position.y = R * 0.0;
  g.add(a);
  g.add(em(bola(R * 0.1, vinil('#f2b705'), 20), 0, R * 1.08, -R * 0.33));
  g.position.y = R * 0.34;
  return naCabeca(g);
}

function trajeMedico() {
  const branco = tecido('#f7f9fc');
  const perfil = [[0, 0.62], [0.62, 0.62], [0.67, 0.68], [0.58, 1.05], [0.47, 1.4], [0.43, 1.62], [0.3, 1.72], [0, 1.74]];
  const jaleco = torno(perfil, branco);
  const corpo = grupo(jaleco, mangasCurtas(branco));
  const verdeAgua = tecido('#5fb3c4');
  corpo.add(calca(verdeAgua, { ate: 0.3, raio: 0.15 }));
  // abertura, gola em V da roupa de baixo e lapelas
  corpo.add(casca([jaleco], 0.004, (ctx, L, A, py, pu) => {
    ctx.fillStyle = '#c3ccd8';
    ctx.fillRect(pu(0) - 2, py(1.45), 4, py(0.62) - py(1.45));
  }));
  const v = new THREE.Shape();
  v.moveTo(-0.13, 0.08); v.lineTo(0, -0.12); v.lineTo(0.13, 0.08); v.closePath();
  corpo.add(sobre(jaleco, 0, 1.58, extrudado(v, 0.01, verdeAgua, 0.006), 0.008, 0.6));
  for (const lado of [-1, 1]) {
    const lap = new THREE.Shape();
    lap.moveTo(0, 0.12); lap.lineTo(lado * 0.12, 0.1); lap.lineTo(lado * 0.02, -0.18); lap.closePath();
    corpo.add(sobre(jaleco, lado * 0.2, 1.5, extrudado(lap, 0.012, branco, 0.008), 0.02, 0.6));
  }
  for (const y of [1.25, 1.05, 0.85]) corpo.add(sobre(jaleco, 0.05, y, botao(0.028, brilho('#c3ccd8')), 0.01, 0.5));
  // bolso com cruz vermelha
  const bolso = new THREE.Group();
  bolso.add(extrudado(retanguloRedondo(0.2, 0.15, 0.03), 0.012, branco, 0.008));
  bolso.add(em(caixa(0.025, 0.08, 0.02, 0.008, brilho('#e63946')), 0, 0, 0.018));
  bolso.add(em(caixa(0.08, 0.025, 0.02, 0.008, brilho('#e63946')), 0, 0, 0.018));
  corpo.add(sobre(jaleco, -0.45, 1.25, bolso, 0.01, 0.3));
  // estetoscópio
  const cinza = brilho('#4a5568');
  corpo.add(tubo([[-0.24, 1.7, 0.18], [-0.3, 1.58, 0.38], [-0.24, 1.4, 0.5], [-0.08, 1.22, 0.55]], 0.025, cinza));
  corpo.add(tubo([[0.24, 1.7, 0.18], [0.29, 1.56, 0.39], [0.22, 1.42, 0.5]], 0.025, cinza));
  const disco = cilindro(0.065, 0.065, 0.035, ouro('#c9d1dc'), 32);
  disco.rotation.x = Math.PI / 2 - 0.3;
  corpo.add(em(disco, -0.06, 1.18, 0.57));
  return { corpo, mangas: [branco, branco], tingir: false };
}

function trajeChef() {
  const branco = tecido('#f7f9fc');
  const jaqueta = blusa(branco, { baixo: 0.78, largura: 1.0, fundo: 0.7 });
  const corpo = grupo(jaqueta, mangasCurtas(branco));
  for (const x of [-0.13, 0.13]) for (const y of [1.42, 1.22, 1.02]) {
    corpo.add(sobre(jaqueta, Math.atan2(x, 0.35), y, botao(0.032, brilho('#5a6575')), 0.012, 0.8));
  }
  // lenço vermelho no pescoço
  const vermelho = brilho('#e63946');
  corpo.add(em(lacinho(0.13, vermelho), 0, 1.62, 0.36));
  const ponta = new THREE.Shape();
  ponta.moveTo(-0.1, 0.06); ponta.lineTo(0.1, 0.06); ponta.lineTo(0, -0.12); ponta.closePath();
  corpo.add(sobre(jaqueta, 0, 1.52, extrudado(ponta, 0.015, vermelho, 0.01), 0.03, 0.6));
  // calça xadrez
  const cinza = tecido('#3a3a44');
  const pernas = calca(cinza, { ate: 0.3, raio: 0.155 });
  corpo.add(pernas);
  corpo.add(casca(pernas, 0.004, (ctx, L, A, py) => {
    ctx.fillStyle = '#c9ced8';
    for (let x = 0; x < L; x += 26) ctx.fillRect(x, 0, 5, A);
    for (let y = 0.3; y < 1.0; y += 0.07) ctx.fillRect(0, py(y), L, 5);
  }));
  const cabeca = touca();
  return { corpo, mangas: [branco, tecido('#e8ecf2')], cabeca, tingir: false };
}

function touca() {
  const branco = tecido('#ffffff');
  const g = new THREE.Group();
  const faixa = cilindro(R * 0.82, R * 0.86, R * 0.4, branco, 64);
  em(faixa, 0, R * 0.92, -R * 0.08);
  faixa.rotation.x = -0.25;
  g.add(faixa);
  const fofo = new THREE.Group();
  for (const [x, y, z, r] of [[0, 1.4, -0.05, 0.52], [-0.44, 1.28, 0.05, 0.4], [0.44, 1.28, 0.05, 0.4], [0, 1.26, 0.33, 0.42], [-0.28, 1.36, -0.33, 0.4], [0.28, 1.36, -0.33, 0.4]]) {
    fofo.add(em(bola(R * r, branco, 40), x * R, y * R, z * R));
  }
  fofo.rotation.x = -0.25;
  g.add(fofo);
  return naCabeca(g);
}

function trajePirata() {
  const creme = tecido('#f7f2e8');
  const camisa = blusa(creme);
  const corpo = grupo(camisa, mangasCurtas(creme));
  corpo.add(casca([camisa], 0.006, listras('#e63946', 0.95, 1.65, 0.12, 0.05)));
  for (const lado of [-1, 1]) {
    const mangas = mangasCurtas(creme);
    corpo.add(casca([mangas.children[lado < 0 ? 0 : 1]], 0.006, listras('#e63946', 1.4, 1.7, 0.12, 0.05)));
  }
  // colete preto aberto na frente
  corpo.add(casca([camisa], 0.022, (ctx, L, A, py, pu) => {
    ctx.fillStyle = '#26262e';
    const meio = pu(0), abre = L * 0.085;
    ctx.fillRect(0, py(1.7), meio - abre, py(0.86) - py(1.7));
    ctx.fillRect(meio + abre, py(1.7), L - meio - abre, py(0.86) - py(1.7));
  }, { mat: { roughness: 0.7, clearcoat: 0.1 } }));
  // cinto marrom e fivela dourada
  const calcaMarrom = calca(tecido('#4a3426'), { ate: 0.4, raio: 0.16 });
  corpo.add(calcaMarrom);
  corpo.add(casca([calcaMarrom.children[0]], 0.02, faixa('#6b3e22', 0.84, 0.95), { mat: { roughness: 0.4, clearcoat: 0.6 } }));
  const fivela = extrudado(retanguloRedondo(0.15, 0.12, 0.03), 0.03, ouro());
  corpo.add(sobre(calcaMarrom.children[0], 0, 0.895, fivela, 0.04, 0.8));
  const pes = botas(brilho('#25252d'), { ate: 0.55, raio: 0.165, matDobra: brilho('#6b3e22') });
  const cabeca = lencoPirata();
  return { corpo, pes, cabeca, tingir: false };
}

function lencoPirata() {
  const vermelho = tecido('#e63946');
  const g = new THREE.Group();
  const c = cupula(R * 1.1, vermelho, { abre: 0.47, inclina: -0.32 });
  g.add(c);
  for (const [x, y] of [[-0.45, 0.88], [0.05, 1.0], [0.5, 0.86], [-0.2, 0.7], [0.3, 0.68], [-0.65, 0.55], [0.72, 0.5]]) {
    const p = bola(R * 0.07, brilho('#ffffff'), 12);
    p.scale.set(1, 1, 0.4);
    naSuperficie(p, R * 1.1, x * R, y * R, 0);
    g.add(p);
  }
  // nó e pontas do lado direito
  const no = bola(R * 0.15, vermelho, 20);
  em(no, R * 0.95, R * 0.35, -R * 0.2);
  g.add(no);
  for (const [dx, dy, giro] of [[0.25, -0.18, -0.6], [0.12, -0.3, -0.2]]) {
    const ponta = malha(new THREE.ConeGeometry(R * 0.12, R * 0.4, 20), vermelho);
    ponta.scale.set(1, 1, 0.45);
    ponta.rotation.z = giro + Math.PI;
    em(ponta, R * (0.95 + dx), R * (0.35 + dy), -R * 0.2);
    g.add(ponta);
  }
  return naCabeca(g);
}

/** Tapa-olho do pirata (só quando não há acessório no rosto). */
function tapaOlho() {
  const g = new THREE.Group();
  const preto = brilho('#1e1e24');
  const tapa = bola(1, preto, 32);
  tapa.scale.set(R * 0.22, R * 0.25, R * 0.06);
  naSuperficie(tapa, R, -R * OLHO_X, R * OLHO_Y, R * 0.04);
  g.add(tapa);
  const fita = malha(new THREE.TorusGeometry(R * 1.012, R * 0.028, 10, 96), preto);
  fita.rotation.set(Math.PI / 2 - 0.12, 0, 0.42);
  fita.position.y = R * 0.1;
  g.add(fita);
  return naCabeca(g);
}

function trajeRealeza(menina) {
  const roxo = brilhoTecido('#7b3fc4');
  const ouroM = ouro();
  const branco = fosco('#ffffff');
  const corpo = new THREE.Group();
  let base;
  if (menina) {
    const perfil = [[0, 0.28], [0.82, 0.28], [0.86, 0.34], [0.72, 0.78], [0.52, 1.28], [0.45, 1.5], [0.42, 1.62], [0.3, 1.72], [0, 1.74]];
    base = torno(perfil, roxo);
    corpo.add(base);
    corpo.add(anel(0.5, 0.035, 1.28, ouroM));
    // barra de arminho
    corpo.add(anel(0.84, 0.065, 0.32, branco));
    corpo.add(casca([base], 0.006, (ctx, L, A, py, pu) => {
      ctx.fillStyle = '#ffc93c';
      ctx.fillRect(pu(0) - L * 0.012, py(1.28), L * 0.024, py(0.3) - py(1.28));
    }, { mat: { roughness: 0.2, clearcoat: 1 } }));
    for (const y of [1.15, 0.95, 0.75, 0.55]) corpo.add(sobre(base, 0, y, bola(0.035, brilho('#ff6fae'), 16), 0.02));
  } else {
    base = blusa(roxo, { baixo: 0.7 });
    corpo.add(base);
    corpo.add(meiaCalca(fosco('#f5f0ff'), 0.24));
    corpo.add(casca([base], 0.012, faixa('#ffc93c', 0.98, 1.06), { mat: { roughness: 0.2, clearcoat: 1 } }));
    // barra de arminho embaixo da túnica
    const barra = caixa(1.02, 0.1, 0.72, 0.05, branco);
    em(barra, 0, 0.72, 0);
    corpo.add(barra);
    for (const y of [1.45, 1.25]) corpo.add(sobre(base, 0, y, botao(0.04, ouroM), 0.01, 0.6));
  }
  // pintinhas pretas do arminho
  corpo.add(mangasCurtas(roxo, M.manga.r * 1.06));
  for (const lado of [-1, 1]) corpo.add(anel(0.15, 0.035, 1.37, ouroM, { x: lado * 0.62 }));
  // gola de arminho nos ombros
  const gola = anel(0.42, 0.1, 1.66, branco, { escalaZ: 0.85 });
  corpo.add(gola);
  for (let i = -3; i <= 3; i++) {
    const a = i * 0.42;
    const p = bola(0.025, brilho('#22222a'), 10);
    p.scale.set(1, 1.6, 0.6);
    corpo.add(sobre(gola, a, 1.66 + 0.06, p, 0.0));
  }
  const tras = capaNasCostas(brilhoTecido('#c81d3a'), { comprida: 0.2 });
  const pes = new THREE.Group();
  for (const lado of [-1, 1]) {
    pes.add(sapato(lado, ouro('#ffc21a')));
    pes.add(em(bola(0.04, brilho('#e63946'), 12), lado * 0.21, 0.22, 0.25));
  }
  return { corpo, tras, pes, tingir: false };
}

function trajeDinossauro() {
  const verde = tecido('#5cc46b');
  const claro = tecido('#b8e986');
  const perfil = [[0, 0.55], [0.6, 0.57], [0.72, 0.74], [0.75, 1.0], [0.66, 1.34], [0.48, 1.6], [0.32, 1.72], [0, 1.74]];
  const barriga = torno(perfil, verde);
  const corpo = grupo(barriga, mangasCurtas(verde), calca(verde, { ate: 0.26, raio: 0.165, cintura: 0.7 }));
  const placa = bola(1, claro, 40);
  placa.scale.set(0.45, 0.52, 0.12);
  corpo.add(sobre(barriga, 0, 1.05, placa, -0.04));
  for (const y of [0.82, 0.98, 1.14, 1.3]) {
    const r = raioNoPerfil(perfil, y);
    const l = malha(new THREE.TorusGeometry(r + 0.012, 0.012, 8, 64, 0.9), tecido('#8fd26a'));
    l.geometry.rotateZ(Math.PI / 2 - 0.45);
    l.rotation.x = Math.PI / 2;
    l.rotation.z = Math.PI;
    l.position.y = y;
    corpo.add(l);
  }
  // rabo com espinhos
  const tras = new THREE.Group();
  const laranja = brilho('#ff9f1c');
  const pontos = [[0.15, 0.8, -0.45], [0.5, 0.62, -0.55], [0.85, 0.42, -0.45], [1.1, 0.22, -0.25], [1.25, 0.12, -0.05]];
  const curva = new THREE.CatmullRomCurve3(pontos.map(V));
  for (let i = 0; i <= 16; i++) {
    const t = i / 16;
    const p = curva.getPoint(t);
    tras.add(em(bola(0.3 * (1 - t * 0.78), verde, 28), p.x, p.y, p.z));
    if (i % 4 === 2) {
      const e = malha(new THREE.ConeGeometry(0.08 * (1 - t * 0.5), 0.2 * (1 - t * 0.5), 16), laranja);
      e.position.set(p.x, p.y + 0.3 * (1 - t * 0.78) - 0.02, p.z);
      e.rotation.z = -0.5;
      tras.add(e);
    }
  }
  const pes = new THREE.Group();
  for (const lado of [-1, 1]) {
    pes.add(sapato(lado, verde, { esc: [0.24, 0.15, 0.32], pos: [0.21, 0.14, 0.07] }));
    for (const k of [-1, 0, 1]) {
      const unha = malha(new THREE.ConeGeometry(0.035, 0.09, 12), brilho('#ffffff'));
      unha.rotation.x = Math.PI / 2;
      em(unha, lado * 0.21 + k * 0.09, 0.09, 0.37 - Math.abs(k) * 0.03);
      pes.add(unha);
    }
  }
  const cabeca = capuzDeDino();
  return { corpo, tras, pes, cabeca, mangas: [verde, tecido('#4fb25e')], tingir: false };
}

function capuzDeDino() {
  const verde = tecido('#5cc46b');
  const g = new THREE.Group();
  g.add(cupula(R * 1.12, verde, { abre: 0.5, inclina: -0.42 }));
  const laranja = brilho('#ff9f1c');
  // espinhos no meio da cabeça
  for (const [ang, tam] of [[-0.25, 0.75], [0.15, 1.0], [0.55, 0.95], [0.95, 0.8], [1.3, 0.6]]) {
    const e = malha(new THREE.ConeGeometry(R * 0.14 * tam, R * 0.34 * tam, 20), laranja);
    const a = ang - 0.42;
    e.position.set(0, Math.cos(a) * R * 1.12, -Math.sin(a) * R * 1.12);
    e.rotation.x = -a;
    e.scale.set(0.55, 1, 1);
    g.add(e);
  }
  // olhinhos e dentinhos do capuz
  for (const lado of [-1, 1]) {
    const o = new THREE.Group();
    o.add(bola(R * 0.14, brilho('#ffffff'), 24));
    o.add(em(bola(R * 0.07, brilho('#22222a'), 16), 0, 0, R * 0.1));
    naSuperficie(o, R * 1.12, lado * R * 0.36, R * 0.82, 0);
    g.add(o);
  }
  // borda da frente do capuz: um anel na boca da cúpula
  const borda = malha(new THREE.TorusGeometry(R * 1.12 * Math.sin(Math.PI * 0.5), R * 0.06, 12, 72), tecido('#4fb25e'));
  borda.rotation.x = Math.PI / 2 - 0.42;
  g.add(borda);
  for (let k = -2; k <= 2; k++) {
    const d = malha(new THREE.ConeGeometry(R * 0.06, R * 0.14, 12), brilho('#ffffff'));
    d.rotation.x = Math.PI;
    const a = k * 0.32;
    const raio = R * 1.12;
    const p = new THREE.Vector3(Math.sin(a) * raio, 0, Math.cos(a) * raio).applyAxisAngle(new THREE.Vector3(1, 0, 0), -0.42);
    d.position.copy(p).add(new THREE.Vector3(0, -R * 0.08, 0));
    g.add(d);
  }
  return naCabeca(g);
}

function trajePijama() {
  const azul = tecido('#3b4c8c');
  const camisa = blusa(azul);
  const corpo = grupo(camisa, mangasCurtas(azul));
  const pernas = calca(azul, { ate: 0.27, raio: 0.16 });
  corpo.add(pernas);
  const amarelo = brilho('#ffd23f');
  for (const [a, y, r] of [[-0.55, 1.45, 0.07], [0.45, 1.3, 0.08], [-0.2, 1.12, 0.06], [0.75, 1.0, 0.06], [-0.85, 1.0, 0.06], [0.15, 1.5, 0.05]]) {
    const e = estrela3D(r, 0.02, amarelo);
    corpo.add(sobre(camisa, a, y, e, 0.012));
  }
  for (const [a, y] of [[-0.3, 0.55], [0.35, 0.45], [-0.5, 0.36], [0.2, 0.7], [-0.15, 0.42]]) {
    const lado = a < 0 ? -1 : 1;
    const perna = pernas.children[lado < 0 ? 1 : 2];
    perna.updateMatrixWorld(true);
    const e = estrela3D(0.055, 0.018, amarelo);
    // na frente da perna
    e.position.set(lado * 0.2 + a * 0.1, y, 0.165);
    corpo.add(e);
  }
  for (const y of [1.48, 1.28, 1.08]) corpo.add(sobre(camisa, 0, y, botao(0.03, brilho('#ffffff')), 0.01, 0.6));
  corpo.add(anel(0.3, 0.04, 1.7, tecido('#ffffff'), { escalaZ: 0.95 }));
  return { corpo, mangas: [azul, amarelo], tingir: false };
}

export const TRAJES = {
  listras: { montar: trajeListras, genero: true },
  pijama: { montar: trajePijama },
  heroi: { montar: trajeHeroi, genero: true },
  abelha: { montar: trajeAbelha },
  fada: { montar: trajeFada },
  astronauta: { montar: trajeAstronauta },
  vestido: { montar: trajeVestido },
  macacao: { montar: trajeMacacao },
  futebol: { montar: trajeFutebol },
  bailarina: { montar: trajeBailarina },
  chuva: { montar: trajeChuva },
  medico: { montar: trajeMedico },
  chef: { montar: trajeChef },
  pirata: { montar: trajePirata },
  realeza: { montar: trajeRealeza, genero: true },
  dinossauro: { montar: trajeDinossauro },
};
export { tapaOlho, mangasCompridas };

// ---------------------------------------------------------------------------------------------
// Acessórios

// ROSTO -----------------------------------------------------------------------------------------

/** Centro de cada lente: em frente ao olho, um pouco afastado, virado quase de frente. */
function lugarDaLente(lado, afasta) {
  const x = lado * R * OLHO_X, y = R * OLHO_Y;
  const z = Math.sqrt(R * R - x * x - y * y);
  const n = new THREE.Vector3(x, y, z).normalize();
  const p = n.clone().multiplyScalar(R + afasta);
  const olha = n.clone().lerp(new THREE.Vector3(0, 0, 1), 0.55).normalize();
  return { p, olha };
}

function hastes(lado, de, mat, r) {
  // da borda de fora da lente até a orelha
  return segmento(de.toArray(), [lado * R * 0.96, R * 0.0, -R * 0.12], r, mat);
}

function oculos() {
  const g = new THREE.Group();
  const aro = brilho('#ff3b58');
  const lente = vidro('#e9f6ff', 0.22);
  const bordas = [];
  for (const lado of [-1, 1]) {
    const { p, olha } = lugarDaLente(lado, R * 0.16);
    const l = new THREE.Group();
    l.add(malha(new THREE.TorusGeometry(R * 0.22, R * 0.045, 16, 48), aro));
    const vid = new THREE.Mesh(new THREE.CircleGeometry(R * 0.22, 40), lente);
    l.add(vid);
    const reflexo = malha(new THREE.TorusGeometry(R * 0.15, R * 0.022, 8, 24, Math.PI * 0.35), new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: 0.85, toneMapped: false }), { sombra: false });
    reflexo.rotation.z = Math.PI * 0.55;
    reflexo.position.z = 0.005;
    l.add(reflexo);
    l.position.copy(p);
    l.lookAt(p.clone().add(olha));
    g.add(l);
    const borda = new THREE.Vector3(lado * R * 0.22, 0, 0).applyQuaternion(l.quaternion).add(p);
    bordas.push(borda);
    g.add(hastes(lado, borda, aro, R * 0.035));
  }
  // ponte
  const a = lugarDaLente(-1, R * 0.16).p, b = lugarDaLente(1, R * 0.16).p;
  const ia = a.clone().add(new THREE.Vector3(R * 0.21, R * 0.03, 0)), ib = b.clone().add(new THREE.Vector3(-R * 0.21, R * 0.03, 0));
  g.add(tubo([ia.toArray(), [0, ia.y + R * 0.05, ia.z + R * 0.03], ib.toArray()], R * 0.035, aro, 16));
  return naCabeca(g);
}

function oculosDeSol() {
  const g = new THREE.Group();
  const preto = brilho('#1d2333');
  const lenteMat = plastico('#151a28', { rugosidade: 0.08, verniz: 1, rugosidadeVerniz: 0.05, ambiente: 1.2 });
  for (const lado of [-1, 1]) {
    const { p, olha } = lugarDaLente(lado, R * 0.16);
    const l = new THREE.Group();
    l.add(extrudado(retanguloRedondo(R * 0.52, R * 0.38, R * 0.15), R * 0.05, lenteMat, R * 0.03));
    const reflexo = capsula(R * 0.025, R * 0.16, new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: 0.7, toneMapped: false }));
    reflexo.castShadow = false;
    reflexo.rotation.z = -0.9;
    reflexo.position.set(-R * 0.08, R * 0.06, R * 0.06);
    l.add(reflexo);
    l.position.copy(p);
    l.lookAt(p.clone().add(olha));
    g.add(l);
    const borda = new THREE.Vector3(lado * R * 0.26, R * 0.08, 0).applyQuaternion(l.quaternion).add(p);
    g.add(hastes(lado, borda, preto, R * 0.035));
  }
  const a = lugarDaLente(-1, R * 0.16).p, b = lugarDaLente(1, R * 0.16).p;
  g.add(segmento([a.x + R * 0.24, a.y + R * 0.08, a.z], [b.x - R * 0.24, b.y + R * 0.08, b.z], R * 0.04, preto));
  return naCabeca(g);
}

/** Máscara de herói: faixa colada no rosto, com furos para os olhos (casca de esfera com textura recortada). */
function mascara() {
  const g = new THREE.Group();
  const raio = R * 1.035;
  const phi0 = 0.42, dPhi = Math.PI - 0.84, th0 = 1.05, dTh = 1.05;
  const cv = document.createElement('canvas');
  cv.width = 1024; cv.height = 512;
  const ctx = cv.getContext('2d');
  const img = ctx.createImageData(cv.width, cv.height);
  for (let j = 0; j < cv.height; j++) {
    const th = th0 + (j / cv.height) * dTh;
    for (let i = 0; i < cv.width; i++) {
      const ph = phi0 + (i / cv.width) * dPhi;
      const x = -Math.cos(ph) * Math.sin(th), y = Math.cos(th);
      const ax = Math.abs(x);
      const yc = -0.03 + 0.1 * ax * ax;
      const h = 0.3 - 0.12 * ax * ax;
      let dentro = ax < 0.86 && Math.abs(y - yc) < h;
      for (const lado of [-1, 1]) {
        const dx = (x - lado * OLHO_X) / 0.19, dy = (y - (OLHO_Y - 0.01)) / 0.235;
        if (dx * dx + dy * dy < 1) dentro = false;
      }
      const k = (j * cv.width + i) * 4;
      img.data[k] = 0x30; img.data[k + 1] = 0x52; img.data[k + 2] = 0xd8; img.data[k + 3] = dentro ? 255 : 0;
    }
  }
  ctx.putImageData(img, 0, 0);
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  const mat = new THREE.MeshPhysicalMaterial({ map: tex, alphaTest: 0.5, roughness: 0.4, clearcoat: 0.6, clearcoatRoughness: 0.2, envMapIntensity: 0.6, side: THREE.DoubleSide });
  const m = new THREE.Mesh(new THREE.SphereGeometry(raio, 96, 48, phi0, dPhi, th0, dTh), mat);
  m.castShadow = true;
  g.add(m);
  // segunda casca um pouco por dentro: dá espessura à borda
  const m2 = new THREE.Mesh(new THREE.SphereGeometry(raio - R * 0.02, 96, 48, phi0, dPhi, th0, dTh), mat.clone());
  m2.material.color = new THREE.Color('#9aa8e8');
  g.add(m2);
  // fitinhas amarradas atrás (aparecem dos lados)
  for (const lado of [-1, 1]) {
    for (const [dy, giro] of [[0.05, -0.5], [-0.12, -1.0]]) {
      const f = capsula(R * 0.05, R * 0.3, brilho('#3052d8'));
      f.scale.set(1, 1, 0.4);
      f.rotation.z = lado * giro;
      em(f, lado * R * 1.02, R * dy, -R * 0.15);
      g.add(f);
    }
  }
  return naCabeca(g);
}

// PESCOÇO -------------------------------------------------------------------------------------

function colar() {
  const g = new THREE.Group();
  const cores = ['#ff4f8b', '#ffd23f', '#4d96ff', '#3dbe6b'];
  let i = 0;
  for (let a = -1.6; a <= 1.6001; a += 0.16) {
    const y = 1.66 - 0.17 * Math.cos(a / 1.6 * Math.PI / 2) ** 2;
    const b = bola(0.062, brilho(cores[i++ % cores.length]), 24);
    em(b, Math.sin(a) * 0.46, y, Math.cos(a) * 0.47);
    g.add(b);
  }
  return g;
}

function cachecol() {
  const verde = fosco('#3dbe6b');
  const g = new THREE.Group();
  const volta = anel(0.42, 0.13, 1.66, verde, { escalaZ: 1.08 });
  g.add(volta);
  const ponta = caixa(0.22, 0.56, 0.09, 0.04, verde);
  ponta.rotation.z = 0.12;
  em(ponta, 0.24, 1.3, 0.47);
  g.add(ponta);
  for (let k = 0; k < 5; k++) {
    const f = capsula(0.014, 0.06, verde);
    f.rotation.z = 0.12;
    em(f, 0.14 + k * 0.048 + 0.03, 0.99 + k * 0.006, 0.47);
    g.add(f);
  }
  g.add(casca([volta, ponta], 0.004, (ctx, L, A, py) => {
    ctx.fillStyle = '#ffffff';
    for (const y of [1.05, 1.16, 1.27]) ctx.fillRect(0, py(y + 0.03), L, py(y - 0.03) - py(y + 0.03));
    ctx.fillStyle = '#ffd23f';
    ctx.fillRect(0, py(1.69), L, py(1.63) - py(1.69));
  }, { mat: { roughness: 0.78, clearcoat: 0.04 } }));
  return g;
}

function gravata() {
  const g = new THREE.Group();
  const l = lacinho(0.17, brilho('#e63946'));
  for (const [x, y] of [[-0.11, 0.03], [-0.13, -0.04], [0.11, 0.03], [0.13, -0.04]]) {
    const p = bola(0.018, brilho('#ffffff'), 10);
    p.scale.set(1, 1, 0.4);
    em(p, x, y, 0.06);
    l.add(p);
  }
  em(l, 0, 1.6, 0.42);
  l.rotation.x = -0.2;
  g.add(l);
  return g;
}

// COSTAS --------------------------------------------------------------------------------------

function mochilaTras() {
  const laranja = fosco('#ff7a3d');
  const g = new THREE.Group();
  g.add(em(caixa(1.24, 0.92, 0.42, 0.18, laranja), 0, 1.25, -0.52));
  g.add(em(caixa(0.9, 0.18, 0.44, 0.08, fosco('#d95a22')), 0, 1.66, -0.52));
  const alca = malha(new THREE.TorusGeometry(0.12, 0.035, 10, 32, Math.PI), brilho('#d95a22'));
  em(alca, 0, 1.74, -0.52);
  g.add(alca);
  for (const lado of [-1, 1]) g.add(em(caixa(0.12, 0.5, 0.3, 0.05, fosco('#ffb35c')), lado * 0.66, 1.15, -0.5));
  return g;
}
function mochilaFrente() {
  const g = new THREE.Group();
  const alca = fosco('#d95a22');
  for (const lado of [-1, 1]) {
    g.add(tubo([[lado * 0.3, 1.66, -0.25], [lado * 0.32, 1.72, 0.05], [lado * 0.3, 1.58, 0.36], [lado * 0.29, 1.3, 0.42], [lado * 0.28, 1.1, 0.44]], 0.045, alca, 32));
    const clip = caixa(0.1, 0.07, 0.05, 0.02, brilho('#ffd23f'));
    em(clip, lado * 0.28, 1.2, 0.47);
    g.add(clip);
  }
  return g;
}

function asasDeBorboleta() {
  const g = new THREE.Group();
  const laranja = plastico('#ff9f1c', { rugosidade: 0.35, verniz: 0.7 });
  const amarelo = plastico('#ffd23f', { rugosidade: 0.35, verniz: 0.7 });
  const borda = brilho('#7a3b12');
  for (const lado of [-1, 1]) {
    for (const [dy, esc, giro, mat, ponto] of [[0.22, 0.62, 0.55, laranja, '#ffffff'], [-0.22, 0.44, -0.55, amarelo, '#4d96ff']]) {
      const s = new THREE.Shape();
      s.moveTo(0, 0);
      s.bezierCurveTo(0.1, 0.6, 0.9, 0.75, 1.0, 0.25);
      s.bezierCurveTo(1.05, -0.15, 0.4, -0.25, 0, 0);
      const asa = extrudado(s, 0.04, mat, 0.02);
      asa.geometry.translate(0.5, -0.17, 0);
      const pivo = new THREE.Group();
      pivo.add(asa);
      asa.position.x = 0.5;
      // contorno
      const pts = s.getPoints(48).map((p) => new THREE.Vector3(p.x, p.y - 0.17 + 0.17, 0));
      const cont = malha(new THREE.TubeGeometry(new THREE.CatmullRomCurve3(pts, true), 96, 0.035, 8, true), borda);
      cont.position.set(0, 0, 0);
      pivo.add(cont);
      const p = bola(0.1, brilho(ponto), 20);
      p.scale.set(1, 1, 0.4);
      em(p, 0.6, 0.22, 0.04);
      pivo.add(p);
      pivo.scale.set(lado * esc * 1.1, esc * 1.1, 1);
      pivo.position.set(lado * 0.12, 1.32 + dy * 0.8, -0.42);
      pivo.rotation.set(0, -lado * 0.35, lado * giro * 0.6);
      g.add(pivo);
    }
  }
  // corpinho da borboleta no meio das costas
  g.add(em(capsula(0.06, 0.4, brilho('#7a3b12')), 0, 1.3, -0.44));
  return g;
}

function capaMagicaTras() {
  return capaNasCostas(brilhoTecido('#7b4dff'), { comprida: 0.24, estrelas: brilho('#ffd23f') });
}
function capaMagicaFrente() {
  const g = new THREE.Group();
  const e = estrela3D(0.11, 0.04, ouro());
  em(e, 0, 1.6, 0.42);
  g.add(e);
  for (const lado of [-1, 1]) g.add(tubo([[lado * 0.36, 1.66, 0.12], [lado * 0.2, 1.64, 0.38], [lado * 0.06, 1.6, 0.43]], 0.03, brilho('#ffd23f'), 16));
  return g;
}

// MÃO (objeto na mão esquerda da tela; a mão da camada dos braços fica por cima e "segura") ---------

/** Mão esquerda (da tela) na pose: 'baixo' (parado, tchau, pensando) ou 'cima' (torcendo). */
function maoNaPose(P) { return V(P.maos[-1]); }

function varinha(P, cima) {
  const H = maoNaPose(P);
  const dir = V(cima ? [-0.18, 1, 0.2] : [-0.42, 1, 0.38]).normalize();
  const g = new THREE.Group();
  const de = H.clone().addScaledVector(dir, -0.14), ate = H.clone().addScaledVector(dir, 0.62);
  g.add(segmento(de.toArray(), ate.toArray(), 0.032, brilho('#6b3e2a')));
  const e = estrela3D(0.17, 0.06, brilho('#ffd23f'));
  e.position.copy(ate).addScaledVector(dir, 0.12);
  e.rotation.set(-0.15, 0.25, 0.2);
  g.add(e);
  const branco = new THREE.MeshBasicMaterial({ color: '#fff7c2', toneMapped: false });
  for (const [dx, dy, r] of [[0.22, 0.12, 0.05], [-0.2, 0.2, 0.04], [0.08, 0.3, 0.035]]) {
    const b = brilhinho(r, branco);
    b.position.copy(e.position).add(new THREE.Vector3(dx, dy, 0.05));
    g.add(b);
  }
  return g;
}

function balao(P, cima) {
  const H = maoNaPose(P);
  const g = new THREE.Group();
  const centro = V(cima ? [-1.02, 3.42, 0.12] : [-1.04, 3.2, 0.1]);
  const b = bola(1, plastico('#ff3d5e', { rugosidade: 0.12, verniz: 1, rugosidadeVerniz: 0.08, ambiente: 0.8 }), 48);
  b.scale.set(0.3, 0.36, 0.3);
  b.position.copy(centro);
  g.add(b);
  const noh = malha(new THREE.ConeGeometry(0.06, 0.08, 16), brilho('#d92a4a'));
  noh.position.copy(centro).add(V([0, -0.38, 0]));
  g.add(noh);
  const baixo = centro.clone().add(V([0, -0.42, 0]));
  const meio = baixo.clone().lerp(H, 0.5).add(V([0.12, 0, 0.05]));
  g.add(tubo([H.toArray(), meio.toArray(), baixo.toArray()], 0.009, brilho('#8a8a95'), 32));
  return g;
}

function ursinho(P, cima) {
  const H = maoNaPose(P);
  const g = new THREE.Group();
  const marrom = fosco('#b07a4f');
  const claro = fosco('#e6c29a');
  const escuro = brilho('#2b2b33');
  const corpo = bola(1, marrom, 32);
  corpo.scale.set(0.19, 0.22, 0.16);
  g.add(corpo);
  g.add(em(bola(0.1, claro, 24), 0, -0.02, 0.1)).children.at(-1).scale.set(1, 1.1, 0.5);
  const cab = new THREE.Group();
  cab.add(bola(0.17, marrom, 32));
  for (const lado of [-1, 1]) {
    cab.add(em(bola(0.065, marrom, 16), lado * 0.12, 0.12, -0.02));
    cab.add(em(bola(0.035, claro, 12), lado * 0.12, 0.12, 0.03));
    cab.add(em(bola(0.022, escuro, 12), lado * 0.06, 0.03, 0.15));
  }
  const focinho = bola(0.07, claro, 20);
  focinho.scale.set(1, 0.8, 0.7);
  cab.add(em(focinho, 0, -0.04, 0.14));
  cab.add(em(bola(0.025, escuro, 12), 0, -0.02, 0.2));
  em(cab, 0, 0.3, 0.02);
  g.add(cab);
  for (const lado of [-1, 1]) {
    g.add(em(bola(0.07, marrom, 16), lado * 0.18, 0.06, 0.06));
    g.add(em(bola(0.08, marrom, 16), lado * 0.1, -0.2, 0.08));
  }
  if (cima) {
    em(g, H.x - 0.05, H.y + 0.24, H.z + 0.12);
    g.rotation.z = 0.15;
  } else {
    em(g, H.x - 0.16, H.y + 0.02, H.z + 0.22);
    g.rotation.set(0, 0.35, -0.15);
  }
  return grupo(g);
}

// PÉS -----------------------------------------------------------------------------------------

function tenis() {
  const g = new THREE.Group();
  for (const lado of [-1, 1]) {
    g.add(em(cilindro(0.142, 0.142, 0.12, fosco('#ffffff')), lado * 0.2, 0.28, 0));
    g.add(sapato(lado, brilho('#ff4f8b'), { esc: [0.205, 0.135, 0.29], pos: [0.21, 0.14, 0.07] }));
    const sola = bola(1, brilho('#ffffff'), 32);
    sola.scale.set(0.22, 0.06, 0.31);
    em(sola, lado * 0.21, 0.05, 0.07);
    g.add(sola);
    const bico = bola(1, brilho('#ffffff'), 24);
    bico.scale.set(0.15, 0.08, 0.1);
    em(bico, lado * 0.21, 0.1, 0.3);
    g.add(bico);
    for (const k of [0, 1, 2]) {
      const c = capsula(0.018, 0.12, brilho('#4d96ff'));
      c.rotation.z = Math.PI / 2;
      em(c, lado * 0.21, 0.24 - k * 0.025, 0.08 + k * 0.06);
      g.add(c);
    }
  }
  return g;
}

function botasMarrons() {
  return botas(plastico('#8d5a3b', { rugosidade: 0.35, verniz: 0.7 }), { ate: 0.6, raio: 0.165, matDobra: plastico('#6b3e22', { rugosidade: 0.4, verniz: 0.6 }), matSola: brilho('#4a2a14') });
}

function pantufas() {
  const g = new THREE.Group();
  const fofo = fosco('#fff6fa');
  const rosa = fosco('#ffb3c7');
  for (const lado of [-1, 1]) {
    const p = bola(1, fofo, 40);
    p.scale.set(0.24, 0.16, 0.32);
    em(p, lado * 0.21, 0.14, 0.08);
    g.add(p);
    for (const o of [-1, 1]) {
      const orelha = bola(1, fofo, 24);
      orelha.scale.set(0.05, 0.14, 0.035);
      orelha.rotation.z = -o * 0.25;
      em(orelha, lado * 0.21 + o * 0.07, 0.36, 0.2);
      g.add(orelha);
      const dentro = bola(1, rosa, 16);
      dentro.scale.set(0.028, 0.1, 0.02);
      dentro.rotation.z = -o * 0.25;
      em(dentro, lado * 0.21 + o * 0.07, 0.36, 0.228);
      g.add(dentro);
      g.add(em(bola(0.022, brilho('#2b2b33'), 12), lado * 0.21 + o * 0.055, 0.22, 0.34));
    }
    g.add(em(bola(0.026, brilho('#ff6fae'), 12), lado * 0.21, 0.18, 0.39));
  }
  return g;
}

// CABELO --------------------------------------------------------------------------------------

function lacoGrande() {
  const g = new THREE.Group();
  const vermelho = brilho('#e63946');
  for (const lado of [-1, 1]) {
    const asa = bola(1, vermelho, 32);
    asa.scale.set(R * 0.3, R * 0.2, R * 0.12);
    asa.rotation.z = lado * 0.35;
    em(asa, lado * R * 0.28, R * 0.03, 0);
    g.add(asa);
    for (const [dx, dy] of [[0.25, 0.08], [0.38, -0.04], [0.18, -0.06]]) {
      const p = bola(R * 0.03, brilho('#ffffff'), 12);
      p.scale.set(1, 1, 0.4);
      em(p, lado * R * dx, R * dy, R * 0.115);
      g.add(p);
    }
    const fita = capsula(R * 0.06, R * 0.22, vermelho);
    fita.scale.set(1, 1, 0.45);
    fita.rotation.z = lado * 0.4;
    em(fita, lado * R * 0.1, -R * 0.2, 0.0);
    g.add(fita);
  }
  g.add(bola(R * 0.1, vermelho, 20));
  em(g, R * 0.48, R * 0.92, R * 0.3);
  g.rotation.set(-0.2, 0.3, -0.3);
  return naCabeca(g);
}

function tiara() {
  const g = new THREE.Group();
  const dourado = ouro();
  const arco = malha(new THREE.TorusGeometry(R * 1.08, R * 0.05, 12, 96, Math.PI * 0.95), dourado);
  arco.geometry.rotateZ(Math.PI / 2 - Math.PI * 0.475);
  arco.rotation.x = -0.12;
  g.add(arco);
  const topo = new THREE.Vector3(0, R * 1.08, 0).applyAxisAngle(new THREE.Vector3(1, 0, 0), -0.12);
  const pico = malha(new THREE.ConeGeometry(R * 0.12, R * 0.3, 4), dourado);
  pico.position.copy(topo).add(new THREE.Vector3(0, R * 0.12, R * 0.02));
  pico.rotation.y = Math.PI / 4;
  g.add(pico);
  const gema = malha(new THREE.OctahedronGeometry(R * 0.11), plastico('#ff4f8b', { rugosidade: 0.05, verniz: 1, ambiente: 1.0 }));
  gema.position.copy(topo).add(new THREE.Vector3(0, R * 0.04, R * 0.1));
  gema.scale.set(1, 1.2, 0.7);
  g.add(gema);
  for (const lado of [-1, 1]) {
    for (const [a, cor, r] of [[0.42, '#4dc3ff', 0.07], [0.78, '#ffffff', 0.05]]) {
      const p = new THREE.Vector3(lado * Math.sin(a) * R * 1.1, Math.cos(a) * R * 1.1, 0).applyAxisAngle(new THREE.Vector3(1, 0, 0), -0.12);
      const gm = malha(new THREE.OctahedronGeometry(R * r), plastico(cor, { rugosidade: 0.05, verniz: 1, ambiente: 1.0 }));
      gm.position.copy(p).add(new THREE.Vector3(0, 0, R * 0.06));
      g.add(gm);
    }
  }
  return naCabeca(g);
}

function presilha() {
  const g = new THREE.Group();
  const e = estrela3D(R * 0.22, R * 0.07, brilho('#ffd23f'));
  naSuperficie(e, R * 1.08, R * 0.62, R * 0.62, R * 0.02);
  g.add(e);
  return naCabeca(g);
}

// ---------------------------------------------------------------------------------------------
// Chapéus (no alto da cabeça; o app troca o chapéu emoji por estas camadas)

function coroa() {
  const g = new THREE.Group();
  const dourado = ouro();
  const aro = new THREE.LatheGeometry([[0.38, 0], [0.47, 0], [0.5, 0.04], [0.5, 0.22], [0.47, 0.26], [0.42, 0.26], [0.38, 0.22]].map(([r, y]) => new THREE.Vector2(r, y)), 72);
  const m = dourado.clone(); m.side = THREE.DoubleSide;
  g.add(malha(aro, m));
  // veludo vermelho por dentro
  const veludo = bola(0.42, plastico('#c81d3a', { rugosidade: 0.7, verniz: 0.1 }), 40);
  veludo.scale.set(1, 0.55, 1);
  veludo.position.y = 0.12;
  g.add(veludo);
  for (let i = 0; i < 6; i++) {
    const a = (i / 6) * Math.PI * 2;
    const ponta = malha(new THREE.ConeGeometry(0.11, 0.3, 4), dourado);
    ponta.scale.set(1, 1, 0.45);
    ponta.position.set(Math.sin(a) * 0.46, 0.38, Math.cos(a) * 0.46);
    ponta.rotation.y = a + Math.PI / 4;
    g.add(ponta);
    g.add(em(bola(0.05, dourado, 16), Math.sin(a) * 0.46, 0.55, Math.cos(a) * 0.46));
    const cor = i % 2 ? '#4d96ff' : '#e63946';
    const gm = bola(0.055, plastico(cor, { rugosidade: 0.06, verniz: 1, ambiente: 1.0 }), 20);
    gm.scale.set(1, 1.2, 0.6);
    const p = new THREE.Vector3(Math.sin(a) * 0.505, 0.13, Math.cos(a) * 0.505);
    gm.position.copy(p);
    gm.lookAt(p.clone().multiplyScalar(2).setY(0.13));
    g.add(gm);
  }
  em(g, 0.04, R * 0.82, -0.04);
  g.rotation.set(-0.18, 0, -0.14);
  return naCabeca(g);
}

function bone() {
  const g = new THREE.Group();
  const azul = tecido('#2f80ff');
  const amarelo = brilho('#ffc21a');
  const copa = new THREE.Group();
  copa.add(malha(new THREE.SphereGeometry(R * 1.1, 64, 32, 0, Math.PI * 2, 0, Math.PI * 0.5), azul));
  const pala = new THREE.Shape();
  pala.absellipse(0, 0, R * 0.78, R * 0.62, 0, Math.PI, false, 0);
  const geo = new THREE.ExtrudeGeometry(pala, { depth: R * 0.04, bevelEnabled: true, bevelThickness: R * 0.02, bevelSize: R * 0.02, bevelSegments: 3, curveSegments: 32 });
  geo.rotateX(Math.PI / 2);
  const p = malha(geo, amarelo);
  p.position.set(0, R * 0.02, R * 0.72);
  p.rotation.x = 0.8;
  copa.add(p);
  copa.add(em(bola(R * 0.1, amarelo, 16), 0, R * 1.1, 0));
  // faixas da copa
  for (let i = 0; i < 6; i++) {
    const a = (i / 6) * Math.PI * 2;
    const linha = malha(new THREE.TorusGeometry(R * 1.105, R * 0.012, 6, 48, Math.PI / 2), tecido('#256be0'));
    linha.rotation.y = a + Math.PI / 2;
    copa.add(linha);
  }
  const e = estrela3D(R * 0.17, R * 0.05, brilho('#ffffff'));
  naSuperficie(e, R * 1.1, 0, R * 0.42, R * 0.01);
  copa.add(e);
  copa.rotation.x = -0.35;
  copa.position.y = R * 0.06;
  g.add(copa);
  return naCabeca(g);
}

function cartola() {
  const g = new THREE.Group();
  const preto = plastico('#2b2b3a', { rugosidade: 0.4, verniz: 0.55, ambiente: 0.6 });
  const copa = malha(new THREE.LatheGeometry([[0, 0.02], [0.42, 0.02], [0.425, 0.3], [0.45, 0.6], [0.44, 0.63], [0.4, 0.645], [0, 0.645]].map(([r, y]) => new THREE.Vector2(r, y)), 64), preto);
  g.add(copa);
  const aba = malha(new THREE.LatheGeometry([[0.3, 0.0], [0.62, -0.01], [0.72, 0.03], [0.74, 0.06], [0.7, 0.06], [0.6, 0.03], [0.3, 0.04]].map(([r, y]) => new THREE.Vector2(r, y)), 72), preto);
  aba.material = preto.clone(); aba.material.side = THREE.DoubleSide;
  g.add(aba);
  const fita = cilindro(0.44, 0.432, 0.12, brilho('#e63946'), 64);
  fita.position.y = 0.11;
  g.add(fita);
  const flor = florzinha(0.09, '#ffffff', '#ffd23f');
  flor.position.set(0.24, 0.11, 0.38);
  flor.lookAt(0.24 * 3, 0.11, 0.38 * 3);
  g.add(flor);
  em(g, 0.05, R * 0.86, -0.05);
  g.rotation.set(-0.16, 0, -0.12);
  return naCabeca(g);
}

function florNoCabelo() {
  const g = new THREE.Group();
  const f = florzinha(R * 0.36, '#ff6fae', '#ffd23f', { achata: 0.4 });
  const folhas = new THREE.Group();
  for (const lado of [-1, 1]) {
    const folha = bola(1, plastico('#3dbe6b', { rugosidade: 0.35, verniz: 0.7 }), 20);
    folha.scale.set(R * 0.16, R * 0.07, R * 0.03);
    folha.position.set(lado * R * 0.3, -R * 0.2, -R * 0.03);
    folha.rotation.z = lado * -0.6;
    folhas.add(folha);
  }
  const miolo = florzinha(R * 0.2, '#ffb3d1', '#ffd23f', { achata: 0.4 });
  miolo.position.z = R * 0.05;
  miolo.rotation.z = 0.6;
  const conj = grupo(folhas, f, miolo);
  naSuperficie(conj, R * 1.06, -R * 0.6, R * 0.66, R * 0.05);
  g.add(conj);
  return naCabeca(g);
}

export const ACESSORIOS = {
  oculos: { parte: 'ROSTO', montar: oculos },
  oculosdesol: { parte: 'ROSTO', montar: oculosDeSol },
  mascara: { parte: 'ROSTO', montar: mascara },
  colar: { parte: 'PESCOCO', montar: colar },
  cachecol: { parte: 'PESCOCO', montar: cachecol },
  gravata: { parte: 'PESCOCO', montar: gravata },
  mochila: { parte: 'COSTAS', montar: mochilaTras, frente: mochilaFrente },
  asas: { parte: 'COSTAS', montar: asasDeBorboleta },
  capa: { parte: 'COSTAS', montar: capaMagicaTras, frente: capaMagicaFrente },
  varinha: { parte: 'MAO', montarNaMao: varinha },
  balao: { parte: 'MAO', montarNaMao: balao },
  ursinho: { parte: 'MAO', montarNaMao: ursinho },
  tenis: { parte: 'PES', montar: tenis },
  botas: { parte: 'PES', montar: botasMarrons },
  pantufas: { parte: 'PES', montar: pantufas },
  laco: { parte: 'CABELO', montar: lacoGrande },
  tiara: { parte: 'CABELO', montar: tiara },
  presilha: { parte: 'CABELO', montar: presilha },
};

/** Chapéus da loja: id da camada -> emoji do Produto (Loja.kt, Categoria.CHAPEU). */
export const CHAPEUS = {
  coroa: { emoji: '👑', montar: coroa },
  bone: { emoji: '🧢', montar: bone },
  cartola: { emoji: '🎩', montar: cartola },
  flor: { emoji: '🌸', montar: florNoCabelo },
};

// ---------------------------------------------------------------------------------------------
// Camada: a figura da criança (partes) + as peças do guarda-roupa, com os papéis da Fase 1A

function comoRecorte(obj, { fazSombra = true } = {}) {
  obj.traverse((o) => { if (o.isMesh) { o.material = matRecorte; o.castShadow = fazSombra; o.receiveShadow = false; o.renderOrder = 1; } });
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

/**
 * [opcoes] da figura (partes()); [pecas](P) devolve as peças extras por nome. Papéis como em
 * personagens.js > camada(): ver, recorte, sombra, capta e âncoras (nomes de P.pontos).
 * Partes auxiliares sempre disponíveis: tronco, sapatosBase, maos.
 */
export function camadaDoGuardaRoupa(opcoes, pecas, { ver = [], recorte = [], sombra = [], capta = [], ancoras = [] }) {
  const P = partes({ quem: 'crianca', ...opcoes });
  Object.assign(P, { tronco: tronco(), sapatosBase: sapatosBase(), maos: maos(P) }, pecas(P));
  const g = new THREE.Group();
  for (const nome of ver) g.add(P[nome]);
  for (const nome of recorte) g.add(comoRecorte(P[nome].clone(true)));
  for (const nome of sombra) g.add(comoSombra(P[nome].clone(true)));
  for (const nome of capta) {
    g.add(comoRecorte(P[nome].clone(true), { fazSombra: false }));
    g.add(comoCaptador(P[nome].clone(true)));
  }
  for (const nome of ancoras) { const p = P.pontos[nome]; ancora(g, nome, p.x, p.y, p.z); }
  return g;
}
