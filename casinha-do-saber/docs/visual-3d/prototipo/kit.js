import * as THREE from 'three';
import { RoundedBoxGeometry } from 'three/addons/geometries/RoundedBoxGeometry.js';
import { RoomEnvironment } from 'three/addons/environments/RoomEnvironment.js';

export { THREE };

// ---------------------------------------------------------------- palco
export function palco(canvas, { largura, altura, fov = 32, fundo = null, exposicao = 1.0 } = {}) {
  const r = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: fundo === null, preserveDrawingBuffer: true });
  r.setPixelRatio(2);
  r.setSize(largura, altura, false);
  r.shadowMap.enabled = true;
  r.shadowMap.type = THREE.PCFSoftShadowMap;
  r.toneMapping = THREE.ACESFilmicToneMapping;
  r.toneMappingExposure = exposicao;
  r.outputColorSpace = THREE.SRGBColorSpace;
  const cena = new THREE.Scene();
  if (fundo) cena.background = new THREE.Color(fundo);
  const pm = new THREE.PMREMGenerator(r);
  cena.environment = pm.fromScene(new RoomEnvironment(r), 0.04).texture;
  const cam = new THREE.PerspectiveCamera(fov, largura / altura, 0.1, 200);
  return { r, cena, cam, desenhar: () => r.render(cena, cam) };
}

export function luzes(cena, { sol = [5, 9, 6], forca = 2.2, alvo = [0, 0, 0], area = 8, ceu = '#fff6e8', chao = '#7aa860', amb = 0.9 } = {}) {
  const h = new THREE.HemisphereLight(ceu, chao, amb);
  cena.add(h);
  const d = new THREE.DirectionalLight('#fff3dc', forca);
  d.position.set(...sol);
  d.target.position.set(...alvo);
  d.castShadow = true;
  d.shadow.mapSize.set(2048, 2048);
  d.shadow.radius = 6;
  d.shadow.bias = -0.0008;
  d.shadow.normalBias = 0.05;
  const c = d.shadow.camera;
  c.left = -area; c.right = area; c.top = area; c.bottom = -area; c.near = 0.5; c.far = 60;
  cena.add(d, d.target);
  // luz de recorte, fria, por trás
  const rim = new THREE.DirectionalLight('#cfe6ff', 0.6);
  rim.position.set(-6, 5, -6);
  cena.add(rim);
  return d;
}

// ---------------------------------------------------------------- materiais
const cacheMat = new Map();
export function plastico(cor, { rugosidade = 0.38, verniz = 0.6, emissivo = null, opacidade = 1 } = {}) {
  const k = [cor, rugosidade, verniz, emissivo, opacidade].join('|');
  if (cacheMat.has(k)) return cacheMat.get(k);
  const m = new THREE.MeshPhysicalMaterial({
    color: cor, roughness: rugosidade, clearcoat: verniz, clearcoatRoughness: 0.25,
    envMapIntensity: 0.55,
  });
  if (emissivo) { m.emissive = new THREE.Color(emissivo); m.emissiveIntensity = 0.6; }
  if (opacidade < 1) { m.transparent = true; m.opacity = opacidade; m.depthWrite = false; }
  cacheMat.set(k, m);
  return m;
}
export const massinha = (cor) => plastico(cor, { rugosidade: 0.72, verniz: 0.05 });
export const pele = (cor) => plastico(cor, { rugosidade: 0.55, verniz: 0.15 });

export function malha(geo, mat, { sombra = true, recebe = true } = {}) {
  const m = new THREE.Mesh(geo, mat);
  m.castShadow = sombra;
  m.receiveShadow = recebe;
  return m;
}
export const caixa = (l, a, p, raio, mat) => malha(new RoundedBoxGeometry(l, a, p, 5, Math.min(raio, l / 2, a / 2, p / 2) * 0.999), mat);
export const bola = (r, mat, seg = 48) => malha(new THREE.SphereGeometry(r, seg, Math.round(seg / 2)), mat);
export const capsula = (r, comp, mat) => malha(new THREE.CapsuleGeometry(r, comp, 10, 24), mat);
export const cilindro = (rt, rb, a, mat, seg = 40) => malha(new THREE.CylinderGeometry(rt, rb, a, seg), mat);
export function em(obj, x, y, z, s = 1) { obj.position.set(x, y, z); if (s !== 1) obj.scale.setScalar(s); return obj; }

// Põe [obj] na superfície de uma esfera de raio [R] (centro na origem do pai), na direção (x, y), olhando para fora.
function naSuperficie(obj, R, x, y, afasta = 0) {
  const z = Math.sqrt(Math.max(0.0001, R * R - x * x - y * y));
  const n = new THREE.Vector3(x, y, z).normalize();
  obj.position.copy(n.clone().multiplyScalar(R + afasta));
  obj.lookAt(n.clone().multiplyScalar(R * 3));
  return obj;
}

// ---------------------------------------------------------------- rosto
function rosto(cabeca, R, { humor = 'feliz', menina = true, corSobrancelha = '#2a1a14' } = {}) {
  const preto = plastico('#1d1210', { rugosidade: 0.15, verniz: 1 });
  const branco = plastico('#ffffff', { rugosidade: 0.1, verniz: 1, emissivo: '#ffffff' });
  for (const lado of [-1, 1]) {
    const x = lado * R * 0.36, y = -R * 0.02;
    if (humor === 'torcendo') {
      // ^ ^ olhinhos fechados de alegria
      const arco = malha(new THREE.TorusGeometry(R * 0.11, R * 0.028, 10, 24, Math.PI), preto, { sombra: false });
      naSuperficie(arco, R, x, y - R * 0.02, -R * 0.005);
      cabeca.add(arco);
    } else {
      const olho = bola(1, preto, 32);
      olho.castShadow = false;
      olho.scale.set(R * 0.125, R * 0.165, R * 0.06);
      naSuperficie(olho, R, x, y, -R * 0.02);
      cabeca.add(olho);
      const b1 = bola(R * 0.05, branco, 16); b1.castShadow = false;
      naSuperficie(b1, R, x + R * 0.04, y + R * 0.07, R * 0.035);
      const b2 = bola(R * 0.024, branco, 12); b2.castShadow = false;
      naSuperficie(b2, R, x - R * 0.045, y - R * 0.07, R * 0.03);
      cabeca.add(b1, b2);
      if (menina) {
        const cilio = malha(new THREE.CapsuleGeometry(R * 0.017, R * 0.07, 4, 8), preto, { sombra: false });
        naSuperficie(cilio, R, x + lado * R * 0.13, y + R * 0.13, R * 0.0);
        cilio.rotateZ(lado * 0.9);
        cabeca.add(cilio);
      }
    }
    // sobrancelha
    const sob = malha(new THREE.TorusGeometry(R * 0.1, R * 0.022, 8, 20, Math.PI * 0.7), plastico(corSobrancelha, { rugosidade: 0.6, verniz: 0 }), { sombra: false });
    naSuperficie(sob, R, x, y + R * (humor === 'torcendo' ? 0.3 : 0.26), -R * 0.005);
    sob.rotateZ(Math.PI * 0.15);
    cabeca.add(sob);
    // bochecha
    const boc = bola(1, plastico('#ff7a8a', { rugosidade: 0.8, verniz: 0, opacidade: 0.5 }), 24);
    boc.castShadow = false;
    boc.scale.set(R * 0.16, R * 0.1, R * 0.04);
    naSuperficie(boc, R, lado * R * 0.58, -R * 0.24, -R * 0.015);
    cabeca.add(boc);
  }
  // nariz
  const nariz = bola(R * 0.06, cabeca.userData.matPele, 16);
  nariz.scale.set(1.2, 0.8, 0.8);
  naSuperficie(nariz, R, 0, -R * 0.14, -R * 0.02);
  cabeca.add(nariz);
  // boca: meia lua escura com linguinha
  const largura = humor === 'torcendo' ? 0.24 : 0.18, fundo = humor === 'torcendo' ? 0.26 : 0.17;
  const s = new THREE.Shape();
  s.moveTo(-largura * R, 0);
  s.quadraticCurveTo(0, 0.02 * R, largura * R, 0);
  s.bezierCurveTo(largura * 0.9 * R, -fundo * R, -largura * 0.9 * R, -fundo * R, -largura * R, 0);
  const geoBoca = new THREE.ExtrudeGeometry(s, { depth: R * 0.03, bevelEnabled: true, bevelThickness: R * 0.01, bevelSize: R * 0.012, bevelSegments: 3, curveSegments: 24 });
  const boca = malha(geoBoca, plastico('#7d1f24', { rugosidade: 0.3, verniz: 0.6 }), { sombra: false });
  naSuperficie(boca, R, 0, -R * 0.3, -R * 0.03);
  cabeca.add(boca);
  const lingua = bola(1, plastico('#ff7f8c', { rugosidade: 0.4, verniz: 0.4 }), 20);
  lingua.scale.set(largura * R * 0.6, fundo * R * 0.35, R * 0.03);
  naSuperficie(lingua, R, 0, -R * 0.3 - fundo * R * 0.62, -R * 0.0);
  lingua.castShadow = false;
  cabeca.add(lingua);
  const dentes = caixa(largura * R * 1.3, R * 0.04, R * 0.02, R * 0.01, plastico('#ffffff', { rugosidade: 0.2 }));
  dentes.castShadow = false;
  naSuperficie(dentes, R, 0, -R * 0.315, R * 0.005);
  cabeca.add(dentes);
}

// ---------------------------------------------------------------- crianças
export function crianca({ corPele = '#b8743d', corCabelo = '#1c1412', corRoupa = '#ff5c97', menina = true, humor = 'feliz', bracos = 'tchau' } = {}) {
  const g = new THREE.Group();
  const mPele = pele(corPele);
  const mCabelo = plastico(corCabelo, { rugosidade: 0.45, verniz: 0.5 });
  const mRoupa = plastico(corRoupa, { rugosidade: 0.55, verniz: 0.25 });
  // pernas e sapatos
  for (const lado of [-1, 1]) {
    const perna = capsula(0.13, 0.42, menina ? mPele : plastico('#2f4f8f', { rugosidade: 0.7, verniz: 0 }));
    em(perna, lado * 0.2, 0.42, 0);
    g.add(perna);
    if (menina) { const meia = cilindro(0.142, 0.142, 0.22, plastico('#ffffff', { rugosidade: 0.8, verniz: 0 })); em(meia, lado * 0.2, 0.3, 0); g.add(meia); }
    const sapato = bola(1, plastico(menina ? '#d6336c' : '#e04848', { rugosidade: 0.2, verniz: 1 }));
    sapato.scale.set(0.2, 0.13, 0.28);
    em(sapato, lado * 0.21, 0.13, 0.06);
    g.add(sapato);
  }
  // corpo
  let corpo;
  if (menina) {
    const pts = [[0.0, 0.6], [0.7, 0.62], [0.74, 0.7], [0.62, 1.05], [0.46, 1.42], [0.42, 1.62], [0.3, 1.72], [0.0, 1.74]].map(([x, y]) => new THREE.Vector2(x, y));
    corpo = malha(new THREE.LatheGeometry(pts, 48), mRoupa);
    g.add(corpo);
    // bolinhas brancas no vestido
    for (const [a, y] of [[-0.5, 0.95], [0, 1.0], [0.5, 0.95], [-0.25, 1.25], [0.25, 1.25]]) {
      const raio = 0.62 - (y - 0.7) * 0.42;
      const p = bola(0.065, plastico('#ffffff', { rugosidade: 0.4 }), 16);
      p.castShadow = false;
      p.scale.set(1, 1, 0.4);
      em(p, Math.sin(a) * raio, y, Math.cos(a) * raio);
      p.lookAt(Math.sin(a) * 5, y, Math.cos(a) * 5);
      g.add(p);
    }
  } else {
    corpo = caixa(0.95, 0.85, 0.66, 0.3, mRoupa);
    em(corpo, 0, 1.25, 0);
    g.add(corpo);
    const short = caixa(0.9, 0.36, 0.62, 0.16, plastico('#2f4f8f', { rugosidade: 0.7, verniz: 0 }));
    em(short, 0, 0.78, 0);
    g.add(short);
    const estrela = estrela3D(0.17, 0.06, plastico('#ffd23f', { rugosidade: 0.25, verniz: 1 }));
    em(estrela, 0, 1.27, 0.34);
    g.add(estrela);
  }
  // braços
  const ombroY = 1.55;
  const poses = {
    tchau: [[-0.35, -0.25], [2.5, 0.2]],
    torcendo: [[-2.5, 0.2], [2.5, 0.2]],
    parado: [[-0.35, -0.2], [0.35, -0.2]],
  }[bracos];
  for (const [i, lado] of [[0, -1], [1, 1]]) {
    const ang = poses[i][0];
    const braco = new THREE.Group();
    em(braco, lado * 0.5, ombroY, 0);
    braco.rotation.z = ang;
    braco.rotation.x = poses[i][1];
    const b = capsula(0.11, 0.42, mPele);
    em(b, 0, -0.3, 0);
    braco.add(b);
    const mao = bola(0.15, mPele, 24);
    em(mao, 0, -0.56, 0);
    braco.add(mao);
    const manga = bola(0.2, mRoupa, 24);
    manga.scale.set(1, 0.9, 1);
    em(manga, 0, -0.04, 0);
    braco.add(manga);
    g.add(braco);
  }
  // cabeça
  const R = 0.78;
  const cabeca = new THREE.Group();
  em(cabeca, 0, 2.38, 0);
  cabeca.userData.matPele = mPele;
  cabeca.add(bola(R, mPele, 64));
  for (const lado of [-1, 1]) { const o = bola(R * 0.2, mPele, 20); em(o, lado * R * 0.98, -R * 0.05, 0); cabeca.add(o); }
  // cabelo: calota inclinada para trás + franja de mechas
  const calota = malha(new THREE.SphereGeometry(R * 1.07, 64, 32, 0, Math.PI * 2, 0, Math.PI * 0.53), mCabelo);
  calota.rotation.x = -0.42;
  cabeca.add(calota);
  const nuca = bola(R * 1.05, mCabelo, 48);
  em(nuca, 0, -R * 0.02, -R * 0.12);
  cabeca.add(nuca);
  for (let k = -2; k <= 2; k++) {
    const mecha = bola(R * 0.26, mCabelo, 24);
    mecha.scale.set(1.25, 0.95, 0.7);
    naSuperficie(mecha, R, k * R * 0.27, R * (0.62 - Math.abs(k) * 0.06), -R * 0.05);
    cabeca.add(mecha);
  }
  if (menina) {
    for (const lado of [-1, 1]) { const coque = bola(R * 0.38, mCabelo, 32); em(coque, lado * R * 0.95, R * 0.4, -R * 0.15); cabeca.add(coque); }
    cabeca.add(laco3D(R, '#ff4f8b', R * 0.45, R * 0.95, R * 0.25));
  } else {
    const topete = bola(R * 0.3, mCabelo, 24); topete.scale.set(0.8, 1.2, 0.8);
    em(topete, R * 0.1, R * 1.05, R * 0.15); topete.rotation.z = -0.4;
    cabeca.add(topete);
  }
  rosto(cabeca, R, { humor, menina });
  g.add(cabeca);
  g.userData.cabeca = cabeca;
  g.traverse(o => { if (o.isMesh && o.castShadow === undefined) o.castShadow = true; });
  return g;
}

export function irmaozinho({ corPele = '#b8743d', corCabelo = '#1c1412', humor = 'feliz', bracos = 'parado' } = {}) {
  const g = new THREE.Group();
  const mPele = pele(corPele);
  const mCabelo = plastico(corCabelo, { rugosidade: 0.45, verniz: 0.5 });
  const blusa = plastico('#ffd23f', { rugosidade: 0.55, verniz: 0.2 });
  const jardineira = plastico('#22b8a8', { rugosidade: 0.6, verniz: 0.15 });
  for (const lado of [-1, 1]) {
    const perna = capsula(0.11, 0.18, mPele); em(perna, lado * 0.16, 0.3, 0); g.add(perna);
    const sapato = bola(1, plastico('#e63946', { rugosidade: 0.2, verniz: 1 })); sapato.scale.set(0.16, 0.11, 0.22); em(sapato, lado * 0.17, 0.11, 0.05); g.add(sapato);
  }
  const corpo = caixa(0.78, 0.72, 0.6, 0.28, blusa); em(corpo, 0, 0.82, 0); g.add(corpo);
  const calca = caixa(0.8, 0.42, 0.62, 0.2, jardineira); em(calca, 0, 0.6, 0); g.add(calca);
  const peito = caixa(0.46, 0.34, 0.08, 0.06, jardineira); em(peito, 0, 0.9, 0.29); g.add(peito);
  for (const lado of [-1, 1]) {
    const alca = caixa(0.09, 0.42, 0.06, 0.03, jardineira); em(alca, lado * 0.2, 1.0, 0.3); alca.rotation.z = lado * 0.15; g.add(alca);
    const botao = bola(0.04, plastico('#fff3b0', { rugosidade: 0.2, verniz: 1 }), 12); em(botao, lado * 0.17, 1.02, 0.35); g.add(botao);
  }
  const est = estrela3D(0.1, 0.04, plastico('#fff3b0', { rugosidade: 0.3, verniz: 1 })); em(est, 0, 0.88, 0.34); g.add(est);
  const angs = bracos === 'torcendo' ? [2.6, -2.6] : [0.45, -0.45];
  for (const [i, lado] of [[0, -1], [1, 1]]) {
    const braco = new THREE.Group(); em(braco, lado * 0.42, 1.06, 0);
    braco.rotation.z = -angs[i];
    const b = capsula(0.1, 0.24, mPele); em(b, 0, -0.2, 0); braco.add(b);
    const mao = bola(0.13, mPele, 20); em(mao, 0, -0.38, 0); braco.add(mao);
    const manga = bola(0.16, blusa, 20); em(manga, 0, -0.03, 0); braco.add(manga);
    g.add(braco);
  }
  const R = 0.68;
  const cabeca = new THREE.Group(); em(cabeca, 0, 1.72, 0);
  cabeca.userData.matPele = mPele;
  cabeca.add(bola(R, mPele, 64));
  for (const lado of [-1, 1]) { const o = bola(R * 0.2, mPele, 20); em(o, lado * R * 0.98, -R * 0.05, 0); cabeca.add(o); }
  const calota = malha(new THREE.SphereGeometry(R * 1.04, 64, 32, 0, Math.PI * 2, 0, Math.PI * 0.42), mCabelo);
  calota.rotation.x = -0.5; cabeca.add(calota);
  // cachinho no alto
  const cacho = malha(new THREE.TorusGeometry(R * 0.16, R * 0.07, 12, 24, Math.PI * 1.6), mCabelo);
  em(cacho, R * 0.05, R * 1.08, R * 0.1); cacho.rotation.y = 0.4; cabeca.add(cacho);
  rosto(cabeca, R, { humor, menina: false });
  g.add(cabeca);
  return g;
}

export function laco3D(R, cor, x, y, z) {
  const g = new THREE.Group();
  const m = plastico(cor, { rugosidade: 0.25, verniz: 1 });
  for (const lado of [-1, 1]) {
    const asa = malha(new THREE.ConeGeometry(R * 0.2, R * 0.38, 24), m);
    asa.rotation.z = lado * Math.PI / 2;
    em(asa, lado * R * 0.17, 0, 0);
    g.add(asa);
  }
  g.add(bola(R * 0.09, m, 16));
  em(g, x, y, z);
  g.rotation.z = -0.3;
  return g;
}

export function estrela3D(r, prof, mat, pontas = 5) {
  const s = new THREE.Shape();
  for (let i = 0; i < pontas * 2; i++) {
    const a = -Math.PI / 2 + i * Math.PI / pontas;
    const rr = i % 2 === 0 ? r : r * 0.48;
    const x = Math.cos(a) * rr, y = -Math.sin(a) * rr;
    if (i === 0) s.moveTo(x, y); else s.lineTo(x, y);
  }
  s.closePath();
  const geo = new THREE.ExtrudeGeometry(s, { depth: prof, bevelEnabled: true, bevelThickness: prof * 0.6, bevelSize: r * 0.12, bevelSegments: 4 });
  geo.center();
  return malha(geo, mat);
}

// ---------------------------------------------------------------- natureza
export function arvore({ copa = '#5cbf4a', s = 1 } = {}) {
  const g = new THREE.Group();
  const tronco = cilindro(0.14, 0.22, 1.2, massinha('#9a6a44')); em(tronco, 0, 0.6, 0); g.add(tronco);
  const m = plastico(copa, { rugosidade: 0.6, verniz: 0.1 });
  for (const [x, y, z, r] of [[0, 1.6, 0, 0.75], [-0.5, 1.35, 0.1, 0.5], [0.5, 1.4, 0.05, 0.55], [0.1, 2.05, -0.05, 0.5], [0.05, 1.45, 0.45, 0.45]]) {
    g.add(em(bola(r, m, 32), x, y, z));
  }
  for (const [x, y, z] of [[0.35, 1.7, 0.55], [-0.45, 1.5, 0.5], [0.6, 1.25, 0.4]]) g.add(em(bola(0.09, plastico('#ff4d4d', { rugosidade: 0.2, verniz: 1 }), 16), x, y, z));
  g.scale.setScalar(s);
  return g;
}
export function arbusto(cor = '#4fae3e', s = 1) {
  const g = new THREE.Group();
  const m = plastico(cor, { rugosidade: 0.6, verniz: 0.1 });
  for (const [x, y, z, r] of [[0, 0.3, 0, 0.42], [-0.38, 0.22, 0.05, 0.3], [0.38, 0.22, 0, 0.32]]) g.add(em(bola(r, m, 24), x, y, z));
  g.scale.setScalar(s);
  return g;
}
export function flor(cor, s = 1) {
  const g = new THREE.Group();
  const caule = cilindro(0.02, 0.02, 0.3, massinha('#3f9a3a'), 8); em(caule, 0, 0.15, 0); g.add(caule);
  const m = plastico(cor, { rugosidade: 0.35, verniz: 0.6 });
  for (let k = 0; k < 5; k++) {
    const a = k * Math.PI * 2 / 5;
    const p = bola(0.07, m, 12); p.scale.set(1, 0.5, 1);
    em(p, Math.cos(a) * 0.08, 0.32, Math.sin(a) * 0.08); g.add(p);
  }
  g.add(em(bola(0.055, plastico('#ffe066', { rugosidade: 0.3 }), 12), 0, 0.34, 0));
  g.scale.setScalar(s);
  return g;
}
export function nuvem(s = 1) {
  const g = new THREE.Group();
  const m = plastico('#ffffff', { rugosidade: 0.85, verniz: 0, emissivo: '#dfefff' });
  for (const [x, y, r] of [[0, 0, 0.7], [-0.75, -0.15, 0.5], [0.75, -0.12, 0.55], [0.3, 0.3, 0.5]]) {
    const b = bola(r, m, 24); b.castShadow = false; b.receiveShadow = false; g.add(em(b, x, y, 0));
  }
  g.scale.set(s, s * 0.8, s * 0.6);
  return g;
}
export function patinho(s = 1, cor = '#ffd23f') {
  const g = new THREE.Group();
  const m = new THREE.MeshPhysicalMaterial({ color: cor, roughness: 0.25, clearcoat: 1, clearcoatRoughness: 0.15, envMapIntensity: 0.3 });
  const corpo = bola(0.5, m, 40); corpo.scale.set(1.15, 0.85, 0.95); em(corpo, 0, 0.42, 0); g.add(corpo);
  const rabo = malha(new THREE.ConeGeometry(0.18, 0.35, 20), m); rabo.rotation.z = 1.1; em(rabo, -0.58, 0.62, 0); g.add(rabo);
  const cabeca = bola(0.34, m, 40); em(cabeca, 0.32, 0.95, 0); g.add(cabeca);
  const bico = bola(0.16, plastico('#ff8c1a', { rugosidade: 0.2, verniz: 1 }), 20); bico.scale.set(1.3, 0.55, 0.9); em(bico, 0.66, 0.9, 0); g.add(bico);
  for (const lado of [-1, 1]) {
    const olho = bola(0.055, plastico('#1d1210', { rugosidade: 0.1, verniz: 1 }), 12); em(olho, 0.5, 1.03, lado * 0.2); g.add(olho);
    const brilho = bola(0.02, plastico('#ffffff', { emissivo: '#ffffff' }), 8); em(brilho, 0.54, 1.06, lado * 0.22); g.add(brilho);
    const asa = bola(0.24, m, 20); asa.scale.set(1.2, 0.6, 0.3); em(asa, -0.05, 0.48, lado * 0.45); asa.rotation.z = 0.3; g.add(asa);
  }
  g.scale.setScalar(s);
  return g;
}

// ---------------------------------------------------------------- casinha (casa de bonecas aberta na frente)
export function casinha() {
  const g = new THREE.Group();
  const parede = plastico('#fff1d6', { rugosidade: 0.7, verniz: 0.05 });
  const madeira = plastico('#d99a5b', { rugosidade: 0.5, verniz: 0.4 });
  const telhado = plastico('#ef476f', { rugosidade: 0.35, verniz: 0.7 });
  const L = 4.2, A = 3.2, P = 2.4;
  // piso e andares
  g.add(em(caixa(L + 0.3, 0.22, P + 0.3, 0.08, madeira), 0, 0.11, 0));
  g.add(em(caixa(L, 0.16, P, 0.06, madeira), 0, A / 2 + 0.1, 0));
  // paredes do fundo (cores por cômodo) e laterais
  const cores = [['#ffd6e6', -L / 4, A * 0.75 + 0.1], ['#d6ecff', L / 4, A * 0.75 + 0.1], ['#fff1c9', -L / 4, A * 0.25 + 0.2], ['#dff5dc', L / 4, A * 0.25 + 0.2]];
  for (const [c, x, y] of cores) g.add(em(caixa(L / 2 - 0.02, A / 2 - 0.1, 0.16, 0.05, plastico(c, { rugosidade: 0.75, verniz: 0 })), x, y, -P / 2 + 0.08));
  for (const lado of [-1, 1]) g.add(em(caixa(0.18, A + 0.1, P, 0.06, parede), lado * (L / 2 - 0.09), A / 2 + 0.15, 0));
  g.add(em(caixa(0.12, A, P * 0.98, 0.04, parede), 0, A / 2 + 0.15, -0.02));
  // telhado
  for (const lado of [-1, 1]) {
    const t = caixa(L * 0.68, 0.22, P + 0.6, 0.1, telhado);
    t.rotation.z = -lado * 0.62;
    em(t, lado * L * 0.27, A + 0.95, 0);
    g.add(t);
  }
  const frontao = new THREE.Shape(); frontao.moveTo(-L / 2, 0); frontao.lineTo(0, 1.45); frontao.lineTo(L / 2, 0); frontao.closePath();
  const fr = malha(new THREE.ExtrudeGeometry(frontao, { depth: 0.12, bevelEnabled: true, bevelSize: 0.03, bevelThickness: 0.03 }), parede);
  em(fr, 0, A + 0.2, -P / 2 + 0.05); g.add(fr);
  const chamine = caixa(0.45, 1.0, 0.45, 0.06, plastico('#b5523b', { rugosidade: 0.7, verniz: 0 })); em(chamine, 1.1, A + 1.5, -0.3); g.add(chamine);
  const janelaRedonda = malha(new THREE.TorusGeometry(0.28, 0.07, 16, 32), plastico('#ffffff', { rugosidade: 0.3 }));
  em(janelaRedonda, 0, A + 0.85, -P / 2 + 0.22); g.add(janelaRedonda);
  g.add(em(malha(new THREE.CircleGeometry(0.27, 32), plastico('#9fd8ff', { emissivo: '#bfe6ff', rugosidade: 0.1 })), 0, A + 0.85, -P / 2 + 0.2));
  // janelas no fundo
  for (const [x, y] of [[-L / 4, A * 0.78], [L / 4 + 0.4, A * 0.28 + 0.15], [-0.6, A * 0.28 + 0.15]]) {
    g.add(em(caixa(0.8, 0.75, 0.08, 0.05, plastico('#ffffff', { rugosidade: 0.3 })), x, y, -P / 2 + 0.2));
    g.add(em(caixa(0.62, 0.58, 0.04, 0.03, plastico('#a8dcff', { emissivo: '#cfeaff', rugosidade: 0.05 })), x, y, -P / 2 + 0.24));
    g.add(em(caixa(0.05, 0.6, 0.06, 0.02, plastico('#ffffff')), x, y, -P / 2 + 0.26));
  }
  // porta
  const porta = caixa(0.75, 1.25, 0.1, 0.12, plastico('#9b5b34', { rugosidade: 0.45, verniz: 0.5 })); em(porta, -L / 2 + 0.75, 0.85, -P / 2 + 0.22); g.add(porta);
  g.add(em(bola(0.06, plastico('#ffd23f', { rugosidade: 0.15, verniz: 1 }), 12), -L / 2 + 1.0, 0.8, -P / 2 + 0.3));
  // móveis: cama (quarto), banheira (banheiro), sofá (sala), geladeira + fogão (cozinha)
  const yAndar = A / 2 + 0.18;
  const cama = new THREE.Group();
  cama.add(em(caixa(1.3, 0.3, 0.8, 0.1, plastico('#4d96ff', { rugosidade: 0.6, verniz: 0.1 })), 0, 0.3, 0));
  cama.add(em(caixa(1.3, 0.12, 0.8, 0.06, madeira), 0, 0.12, 0));
  cama.add(em(caixa(0.12, 0.7, 0.8, 0.05, madeira), -0.65, 0.35, 0));
  cama.add(em(caixa(0.36, 0.16, 0.6, 0.08, plastico('#ffffff', { rugosidade: 0.8, verniz: 0 })), -0.42, 0.5, 0));
  em(cama, -L / 4, yAndar, -0.3); g.add(cama);
  const urso = new THREE.Group(); const mu = massinha('#b07a4f');
  urso.add(em(bola(0.17, mu, 20), 0, 0.17, 0)); urso.add(em(bola(0.14, mu, 20), 0, 0.42, 0));
  for (const l of [-1, 1]) urso.add(em(bola(0.06, mu, 12), l * 0.1, 0.53, 0));
  em(urso, -L / 4 + 0.3, yAndar + 0.4, -0.2); g.add(urso);
  const banheira = new THREE.Group();
  banheira.add(em(caixa(1.2, 0.5, 0.7, 0.22, plastico('#ffffff', { rugosidade: 0.15, verniz: 1 })), 0, 0.3, 0));
  banheira.add(em(caixa(1.0, 0.06, 0.5, 0.03, plastico('#7fd0ff', { rugosidade: 0.05, verniz: 1, emissivo: '#bfe9ff' })), 0, 0.52, 0));
  for (let k = 0; k < 6; k++) banheira.add(em(bola(0.1 + (k % 3) * 0.03, plastico('#ffffff', { rugosidade: 0.9, verniz: 0, opacidade: 0.9 }), 12), -0.35 + k * 0.14, 0.6, (k % 2) * 0.12 - 0.05));
  banheira.add(em(patinho(0.25), 0.25, 0.5, 0.05));
  em(banheira, L / 4, yAndar, -0.2); g.add(banheira);
  const sofa = new THREE.Group(); const ms = plastico('#22b8a8', { rugosidade: 0.7, verniz: 0.05 });
  sofa.add(em(caixa(1.3, 0.3, 0.6, 0.12, ms), 0, 0.28, 0));
  sofa.add(em(caixa(1.3, 0.55, 0.18, 0.09, ms), 0, 0.5, -0.24));
  for (const l of [-1, 1]) sofa.add(em(caixa(0.18, 0.42, 0.6, 0.08, ms), l * 0.65, 0.35, 0));
  sofa.add(em(caixa(0.32, 0.28, 0.12, 0.08, plastico('#ffd23f', { rugosidade: 0.7 })), -0.35, 0.55, -0.1));
  em(sofa, -0.35, 0.22, -0.4); g.add(sofa);
  const gel = caixa(0.6, 1.3, 0.55, 0.12, plastico('#f4f7fb', { rugosidade: 0.15, verniz: 1 })); em(gel, L / 2 - 0.55, 0.87, -0.55); g.add(gel);
  g.add(em(caixa(0.05, 0.3, 0.06, 0.02, plastico('#c0c8d4')), L / 2 - 0.35, 1.1, -0.26));
  const fogao = caixa(0.6, 0.7, 0.55, 0.08, plastico('#ffffff', { rugosidade: 0.2, verniz: 1 })); em(fogao, L / 2 - 1.25, 0.57, -0.55); g.add(fogao);
  g.add(em(caixa(0.4, 0.3, 0.04, 0.03, plastico('#2b2b3a', { rugosidade: 0.1, verniz: 1 })), L / 2 - 1.25, 0.5, -0.26));
  const bolo = new THREE.Group();
  bolo.add(em(cilindro(0.2, 0.2, 0.18, plastico('#ffb3c9', { rugosidade: 0.6 })), 0, 0.09, 0));
  bolo.add(em(cilindro(0.13, 0.13, 0.14, plastico('#ffffff', { rugosidade: 0.6 })), 0, 0.25, 0));
  bolo.add(em(bola(0.04, plastico('#ff3355', { rugosidade: 0.2, verniz: 1 }), 12), 0, 0.36, 0));
  em(bolo, L / 2 - 1.25, 0.92, -0.55); g.add(bolo);
  // luzinha quente dentro
  const luz = new THREE.PointLight('#ffd9a0', 1.2, 5, 2); luz.position.set(0, A * 0.45, 1.3); g.add(luz);
  return g;
}

export function ilhaDeGrama(raio = 7) {
  const g = new THREE.Group();
  const terra = cilindro(raio, raio * 0.92, 0.9, massinha('#b8834f'), 64); em(terra, 0, -0.45, 0); g.add(terra);
  const grama = cilindro(raio + 0.05, raio + 0.05, 0.28, plastico('#7ccf5b', { rugosidade: 0.7, verniz: 0.1 }), 64); em(grama, 0, 0.0, 0); g.add(grama);
  const borda = malha(new THREE.TorusGeometry(raio, 0.14, 16, 96), plastico('#7ccf5b', { rugosidade: 0.7, verniz: 0.1 })); borda.rotation.x = Math.PI / 2; em(borda, 0, 0.0, 0); g.add(borda);
  return g;
}

// Moeda dourada
export function moeda(s = 1) {
  const g = new THREE.Group();
  const ouro = plastico('#ffc933', { rugosidade: 0.2, verniz: 1 });
  const c = cilindro(0.5, 0.5, 0.14, ouro, 48); c.rotation.x = Math.PI / 2; g.add(c);
  const e = estrela3D(0.25, 0.04, plastico('#ffe58a', { rugosidade: 0.15, verniz: 1 })); em(e, 0, 0, 0.08); g.add(e);
  g.scale.setScalar(s);
  return g;
}

export function presente(s = 1, aberto = false) {
  const g = new THREE.Group();
  const caixaM = plastico('#ff4f8b', { rugosidade: 0.25, verniz: 1 });
  const fita = plastico('#ffd23f', { rugosidade: 0.2, verniz: 1 });
  g.add(em(caixa(1.6, 1.3, 1.6, 0.14, caixaM), 0, 0.65, 0));
  g.add(em(caixa(0.3, 1.32, 1.62, 0.05, fita), 0, 0.65, 0));
  g.add(em(caixa(1.62, 1.32, 0.3, 0.05, fita), 0, 0.65, 0));
  const tampa = new THREE.Group();
  tampa.add(em(caixa(1.78, 0.36, 1.78, 0.12, caixaM), 0, 0, 0));
  tampa.add(em(caixa(0.32, 0.38, 1.8, 0.05, fita), 0, 0, 0));
  tampa.add(em(caixa(1.8, 0.38, 0.32, 0.05, fita), 0, 0, 0));
  for (const lado of [-1, 1]) {
    const laco = malha(new THREE.TorusGeometry(0.28, 0.1, 16, 32), fita);
    laco.rotation.set(0, 0, lado * 0.6);
    laco.scale.set(1, 0.8, 0.6);
    em(laco, lado * 0.3, 0.42, 0); tampa.add(laco);
  }
  tampa.add(em(bola(0.13, fita, 16), 0, 0.3, 0));
  if (aberto) { em(tampa, 1.25, 2.15, -0.5); tampa.rotation.set(0.3, 0.2, -0.75); } else em(tampa, 0, 1.4, 0);
  g.add(tampa);
  g.userData.tampa = tampa;
  g.scale.setScalar(s);
  return g;
}

export function confete(cena, n, caixaArea, seed = 7) {
  let x = seed;
  const rnd = () => { x = (x * 16807) % 2147483647; return (x - 1) / 2147483646; };
  const cores = ['#ff4f8b', '#ffd23f', '#4d96ff', '#3dbe6b', '#9b6bff', '#ff8c1a'];
  for (let i = 0; i < n; i++) {
    const mat = plastico(cores[i % cores.length], { rugosidade: 0.3, verniz: 0.8 });
    const p = i % 3 === 0 ? estrela3D(0.12, 0.03, mat) : caixa(0.22, 0.1, 0.02, 0.01, mat);
    p.castShadow = false;
    p.position.set(caixaArea[0] + rnd() * (caixaArea[1] - caixaArea[0]), caixaArea[2] + rnd() * (caixaArea[3] - caixaArea[2]), caixaArea[4] + rnd() * (caixaArea[5] - caixaArea[4]));
    p.rotation.set(rnd() * 6, rnd() * 6, rnd() * 6);
    cena.add(p);
  }
}
