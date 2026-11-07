// Fase 8: a Corujinha Professora (narradora da história, ui/componentes/Corujinha.kt) e os mascotes da casa
// (cachorrinho, gatinho e dragãozinho, ui/componentes/MascoteDesenhado.kt) em 3D, de vinil brilhante, com poses.
//
// Todas as poses de um personagem têm a mesma moldura invisível (o recorte, a luz e o apoio são os mesmos), então
// o app troca uma pela outra sem o desenho pular. A sombra de contato sai da base (galho ou pés), que não muda.
// O estágio de crescimento do mascote é escala no app (EstagioMascote.escala), como no desenho antigo.
import { THREE, plastico, malha, bola, capsula, cilindro, caixa, em, ancora, naSuperficie } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { zzz } from './enfeites.js';

export const POSES_DA_CORUJA = ['parada', 'falando', 'feliz', 'dormindo'];
export const POSES_DO_MASCOTE = ['parado', 'falando', 'feliz', 'dormindo'];
export const ESPECIES = ['cachorrinho', 'gatinho', 'dragaozinho'];

const vinil = (cor, o = {}) => plastico(cor, { rugosidade: 0.4, verniz: 0.6, rugosidadeVerniz: 0.18, ambiente: 0.34, ...o });
const brilho = (cor, o = {}) => plastico(cor, { rugosidade: 0.2, verniz: 1, rugosidadeVerniz: 0.08, ambiente: 0.5, ...o });
const PRETO = () => plastico('#2a1d40', { rugosidade: 0.12, verniz: 1 });
const LUZ = () => plastico('#ffffff', { rugosidade: 0.1, verniz: 1, emissivo: '#ffffff' });

/** Caixa invisível que fixa o enquadramento (conta no recorte, não aparece). */
function moldura(x0, x1, y0, y1, z0, z1) {
  const m = new THREE.Mesh(new THREE.BoxGeometry(x1 - x0, y1 - y0, z1 - z0), new THREE.MeshBasicMaterial({ visible: false }));
  m.position.set((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2);
  m.castShadow = false;
  return m;
}

/** Cápsula de [a] até [b] (pontos 3D). */
function segmento(a, b, r, mat) {
  const va = new THREE.Vector3(...a), vb = new THREE.Vector3(...b);
  const d = vb.clone().sub(va);
  const m = capsula(r, Math.max(0.001, d.length()), mat);
  m.position.copy(va).add(vb).multiplyScalar(0.5);
  m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), d.normalize());
  return m;
}

/** Tubo macio ao longo de pontos (rabos), afinando até [rFim]. */
function tubo(pontos, r0, rFim, mat, seg = 48) {
  const curva = new THREE.CatmullRomCurve3(pontos.map((p) => new THREE.Vector3(...p)));
  const geo = new THREE.TubeGeometry(curva, seg, 1, 16, false);
  // afina o tubo ao longo do comprimento
  const pos = geo.attributes.position;
  const v = new THREE.Vector3();
  const anel = 17;
  for (let i = 0; i <= seg; i++) {
    const t = i / seg;
    const c = curva.getPointAt(t);
    const r = r0 + (rFim - r0) * t;
    for (let j = 0; j < anel; j++) {
      const k = i * anel + j;
      v.fromBufferAttribute(pos, k).sub(c).multiplyScalar(r).add(c);
      pos.setXYZ(k, v.x, v.y, v.z);
    }
  }
  geo.computeVertexNormals();
  const g = new THREE.Group();
  g.add(malha(geo, mat));
  g.add(em(bola(r0, mat, 20), ...pontos[0]));
  g.add(em(bola(rFim, mat, 16), ...pontos[pontos.length - 1]));
  return g;
}

/** Olho grande de desenho: preto com dois brilhos, na superfície da cabeça de raio [R]. */
function olhoAberto(R, x, y, tam) {
  const g = new THREE.Group();
  const o = bola(1, PRETO(), 32);
  o.scale.set(tam * 0.82, tam, tam * 0.45);
  o.castShadow = false;
  g.add(o);
  g.add(em(bola(tam * 0.36, LUZ(), 16), tam * 0.28, tam * 0.38, tam * 0.3));
  g.add(em(bola(tam * 0.16, LUZ(), 12), -tam * 0.3, -tam * 0.4, tam * 0.3));
  naSuperficie(g, R, x, y, -tam * 0.12);
  return g;
}

/** Olho fechado em arco: [feliz] = ^ (alegria), senão ‿ (sono). */
function olhoFechado(R, x, y, tam, feliz) {
  const geo = new THREE.TorusGeometry(tam * 0.8, tam * 0.2, 10, 24, Math.PI);
  if (!feliz) geo.rotateZ(Math.PI);
  const m = malha(geo, PRETO(), { sombra: false });
  naSuperficie(m, R, x, y, -tam * 0.05);
  return m;
}

/** Bochecha rosada. */
function bochecha(R, x, y, tam) {
  const b = bola(1, plastico('#ff7a9a', { rugosidade: 0.8, verniz: 0, opacidade: 0.55 }), 20);
  b.scale.set(tam, tam * 0.62, tam * 0.25);
  b.castShadow = false;
  naSuperficie(b, R, x, y, -tam * 0.12);
  return b;
}

/** Boca: sorriso fechado (arco) ou aberta (meia lua escura com linguinha). */
function boca(R, y, tam, aberta) {
  const g = new THREE.Group();
  if (!aberta) {
    const geo = new THREE.TorusGeometry(tam, tam * 0.2, 10, 28, 2.1);
    geo.rotateZ(-Math.PI / 2 - 1.05);
    g.add(malha(geo, plastico('#5a2230', { rugosidade: 0.3, verniz: 0.6 }), { sombra: false }));
    naSuperficie(g, R, 0, y + tam * 0.6, -tam * 0.05);
    return g;
  }
  const s = new THREE.Shape();
  const L = tam * 1.15, F = tam * 1.25;
  s.moveTo(-L, 0);
  s.quadraticCurveTo(0, tam * 0.12, L, 0);
  s.bezierCurveTo(L * 0.9, -F, -L * 0.9, -F, -L, 0);
  const geo = new THREE.ExtrudeGeometry(s, { depth: tam * 0.15, bevelEnabled: true, bevelThickness: tam * 0.06, bevelSize: tam * 0.06, bevelSegments: 3, curveSegments: 24 });
  g.add(malha(geo, plastico('#7d1f34', { rugosidade: 0.3, verniz: 0.6 }), { sombra: false }));
  const lingua = bola(1, plastico('#ff7f8c', { rugosidade: 0.4, verniz: 0.4 }), 20);
  lingua.scale.set(L * 0.55, F * 0.28, tam * 0.12);
  g.add(em(lingua, 0, -F * 0.55, tam * 0.16));
  naSuperficie(g, R, 0, y, -tam * 0.12);
  return g;
}

// =====================================================================================================
// Corujinha Professora

const PENAS = '#b8743a';
const PENAS_ESCURAS = '#8a5a36';
const PEITO = '#f6e2bf';
const LARANJA = '#ff9f2e';
const ROXO_OCULOS = '#6c63ff';
const CAPELO = '#3d2c5e';

/** Asa da coruja, presa no ombro: [ang] gira a partir de "caída" (0) para cima (positivo = para fora). */
function asa(lado, ang) {
  const g = new THREE.Group();
  const m = vinil(PENAS_ESCURAS);
  const a = bola(0.42, m, 32);
  a.scale.set(0.42, 1, 0.62);
  g.add(em(a, 0, -0.36, 0));
  // pontinhas das penas
  for (const k of [-1, 0, 1]) {
    const p = bola(0.12, m, 16);
    p.scale.set(0.8, 1.2, 0.7);
    g.add(em(p, k * 0.07, -0.74 + Math.abs(k) * 0.05, 0));
  }
  g.rotation.z = lado * ang;
  return em(g, lado * 0.62, 1.32, 0.02);
}

function capelo() {
  const g = new THREE.Group();
  const escuro = brilho(CAPELO);
  g.add(em(cilindro(0.4, 0.44, 0.24, escuro, 40), 0, 0, 0));
  const tampa = caixa(1.18, 0.08, 1.18, 0.03, brilho('#4a3a6e'));
  tampa.rotation.y = Math.PI / 4;
  g.add(em(tampa, 0, 0.15, 0));
  const ouroM = brilho('#ffc93c', { emissivo: '#b07a00' });
  g.add(em(bola(0.07, ouroM, 16), 0, 0.21, 0));
  // cordão do botão até a ponta da frente direita, e o pingente pendurado
  const ponta = [0.0, 0.2, 0.8];
  g.add(segmento([0, 0.21, 0], [0.55, 0.2, 0.55], 0.025, ouroM));
  g.add(segmento([0.55, 0.2, 0.55], [0.6, -0.22, 0.6], 0.025, ouroM));
  const pingente = malha(new THREE.ConeGeometry(0.07, 0.22, 16), ouroM);
  g.add(em(pingente, 0.6, -0.3, 0.6));
  void ponta;
  return g;
}

/** A Corujinha Professora pousada no galho, com óculos, capelo e livrinho. [pose]: POSES_DA_CORUJA. */
export function corujinha(pose) {
  return () => {
    const g = new THREE.Group();
    const corpo = new THREE.Group();
    g.add(corpo);
    const penas = vinil(PENAS);
    const peito = vinil(PEITO, { rugosidade: 0.55, verniz: 0.3 });

    // galho com folhinhas (a base: a sombra de contato sai dele)
    const galho = new THREE.Group();
    const casca = vinil('#8a5a35', { rugosidade: 0.6, verniz: 0.2 });
    const tronco = capsula(0.13, 2.1, casca);
    tronco.rotation.z = Math.PI / 2;
    galho.add(em(tronco, 0, 0.16, 0));
    const folha = vinil('#5fc95a', { rugosidade: 0.35, verniz: 0.7 });
    for (const [x, y, z, rz] of [[-1.12, 0.3, 0.05, 0.6], [-0.98, 0.1, 0.18, -0.3], [1.12, 0.3, 0.0, -0.6], [1.0, 0.36, 0.16, -1.2]]) {
      const f = bola(0.18, folha, 20);
      f.scale.set(1.5, 0.75, 0.35);
      f.rotation.z = rz;
      galho.add(em(f, x, y, z));
    }
    g.add(galho);
    g.userData.base = galho;

    // pezinhos agarrando o galho
    const pe = vinil(LARANJA, { rugosidade: 0.3, verniz: 0.8 });
    for (const x of [-0.24, 0.24]) {
      for (const k of [-1, 0, 1]) g.add(segmento([x + k * 0.07, 0.36, 0.16], [x + k * 0.1, 0.22, 0.24], 0.055, pe));
    }

    // corpo e peito com peninhas em V
    const b = bola(0.72, penas, 48);
    b.scale.set(1, 1.08, 0.92);
    corpo.add(em(b, 0, 0.98, 0));
    const pe2 = bola(0.52, peito, 40);
    pe2.scale.set(1, 1.12, 0.5);
    corpo.add(em(pe2, 0, 0.9, 0.42));
    const vMat = vinil('#e2bf8c', { rugosidade: 0.6, verniz: 0.2 });
    for (const [x, y] of [[-0.16, 1.1], [0.16, 1.1], [0, 0.9], [-0.24, 0.84], [0.24, 0.84], [-0.12, 0.66], [0.12, 0.66]]) {
      const v = malha(new THREE.TorusGeometry(0.07, 0.02, 8, 16, Math.PI * 0.8), vMat, { sombra: false });
      v.rotation.z = Math.PI + Math.PI * 0.1;
      const z = 0.42 + 0.26 * Math.sqrt(Math.max(0, 1 - (x / 0.52) ** 2 - ((y - 0.9) / 0.58) ** 2));
      corpo.add(em(v, x, y, z));
    }

    // asas: a da esquerda segura o livrinho; a da direita gesticula ao falar e se ergue na alegria
    const angDireita = { parada: 0.18, falando: 1.25, feliz: 2.35, dormindo: 0.1 }[pose];
    const angEsquerda = { parada: 0.18, falando: 0.18, feliz: 0.55, dormindo: 0.1 }[pose];
    corpo.add(asa(1, angDireita), asa(-1, angEsquerda));
    const livro = new THREE.Group();
    livro.add(caixa(0.5, 0.36, 0.13, 0.04, brilho('#4d96ff')));
    livro.add(em(caixa(0.44, 0.31, 0.12, 0.02, vinil('#ffffff', { rugosidade: 0.5 })), 0.03, 0, 0.0));
    livro.add(em(estrela3D(0.08, 0.02, brilho('#ffd23f'), 5), 0, 0, 0.08));
    livro.rotation.set(0.15, 0.6, 0.35);
    corpo.add(em(livro, -0.7, 0.66 + angEsquerda * 0.1, 0.36));

    // cabeça (mais larga que o corpo), tufos, discos dos olhos, óculos, bico
    const R = 0.74;
    const cab = new THREE.Group();
    const cabeca = bola(R, penas, 56);
    cab.add(cabeca);
    cab.scale.set(1.12, 0.92, 0.94);
    for (const lado of [-1, 1]) {
      const tufo = malha(new THREE.ConeGeometry(0.2, 0.46, 24), penas);
      tufo.rotation.z = -lado * 0.5;
      cab.add(em(tufo, lado * 0.52, 0.64, -0.06));
    }
    const disco = vinil(PEITO, { rugosidade: 0.5, verniz: 0.35 });
    const oculos = brilho(ROXO_OCULOS);
    for (const lado of [-1, 1]) {
      const x = lado * 0.3, y = 0.0;
      const d = bola(1, disco, 32);
      d.scale.set(0.3, 0.3, 0.1);
      naSuperficie(d, R, x, y, -0.04);
      cab.add(d);
      if (pose === 'feliz' || pose === 'dormindo') cab.add(olhoFechado(R + 0.05, x, y - 0.01, 0.1, pose === 'feliz'));
      else cab.add(olhoAberto(R + 0.05, x, y, 0.15));
      const aro = malha(new THREE.TorusGeometry(0.26, 0.035, 12, 40), oculos);
      naSuperficie(aro, R, x, y, 0.08);
      cab.add(aro);
      cab.add(bochecha(R, lado * 0.5, -0.24, 0.1));
    }
    cab.add(em(segmento([-0.06, 0.03, 0.82], [0.06, 0.03, 0.82], 0.03, oculos), 0, 0, 0));
    // bico: em cima fixo; embaixo abre ao falar e na alegria
    const bicoM = brilho(LARANJA);
    const cima = malha(new THREE.ConeGeometry(0.15, 0.34, 28), bicoM);
    cima.scale.set(1, 1, 0.8);
    cima.rotation.x = Math.PI + 0.5;
    cab.add(em(cima, 0, -0.17, 0.8));
    const aberto = pose === 'falando' || pose === 'feliz';
    if (aberto) {
      cab.add(em(bola(0.09, plastico('#7d1f34', { rugosidade: 0.4 }), 16), 0, -0.34, 0.76));
      const baixo = malha(new THREE.ConeGeometry(0.1, 0.16, 20), bicoM);
      baixo.rotation.x = Math.PI - 0.7;
      cab.add(em(baixo, 0, -0.44, 0.78));
    }
    // capelo
    const cap = capelo();
    cap.rotation.set(-0.08, 0.15, pose === 'dormindo' ? 0.0 : -0.06);
    cab.add(em(cap, 0, 0.78, -0.02));
    ancora(cab, 'cabeca', 0, 0, 0);
    ancora(cab, 'bico', 0, -0.3, 0.8);

    cab.rotation.z = pose === 'dormindo' ? 0.2 : pose === 'feliz' ? -0.08 : 0;
    cab.rotation.x = pose === 'dormindo' ? 0.12 : 0;
    corpo.add(em(cab, 0, 2.0, 0.04));

    if (pose === 'dormindo') g.add(em(zzz(0.62), 1.05, 2.7, 0.2));

    g.add(moldura(-1.45, 1.45, 0, 3.35, -0.8, 1.0));
    g.rotation.y = -0.12;
    return g;
  };
}

// =====================================================================================================
// Mascotes da casa

const PELAGENS = {
  cachorrinho: { corpo: '#f0a258', barriga: '#fff0d6', orelha: '#9a4f22', detalhe: '#d47f3c' },
  gatinho: { corpo: '#ff9a2e', barriga: '#fff2dc', orelha: '#ff8fb0', detalhe: '#d9661a' },
  dragaozinho: { corpo: '#4fcb55', barriga: '#ffdf6a', orelha: '#fff1cf', detalhe: '#25b09c' },
};

/** Braço preso no ombro: [ang] 0 = caído; positivo abre para fora e sobe. */
function braco(lado, ang, mat) {
  const g = new THREE.Group();
  g.add(em(capsula(0.13, 0.26, mat), 0, -0.2, 0));
  g.rotation.z = lado * ang;
  return em(g, lado * 0.4, 0.92, 0.06);
}

/** Cachorrinho, gatinho ou dragãozinho de pé, de frente, cabeça grande. [pose]: POSES_DO_MASCOTE. */
export function mascote(especie, pose) {
  return () => {
    const c = PELAGENS[especie];
    const g = new THREE.Group();
    const corpoM = vinil(c.corpo);
    const barrigaM = vinil(c.barriga, { rugosidade: 0.5, verniz: 0.35 });
    const dragao = especie === 'dragaozinho';
    const gato = especie === 'gatinho';

    // pés (a base)
    const pes = new THREE.Group();
    for (const l of [-1, 1]) {
      const p = bola(0.2, gato ? barrigaM : corpoM, 28);
      p.scale.set(1, 0.6, 1.3);
      pes.add(em(p, l * 0.24, 0.12, 0.1));
    }
    g.add(pes);
    g.userData.base = pes;

    // corpo e barriga
    const corpo = bola(0.5, corpoM, 48);
    corpo.scale.set(1, 1.04, 0.9);
    g.add(em(corpo, 0, 0.62, 0));
    const barriga = bola(0.36, barrigaM, 36);
    barriga.scale.set(1, 1.1, 0.45);
    g.add(em(barriga, 0, 0.58, 0.31));
    if (dragao) {
      const lista = vinil('#e5c35a', { rugosidade: 0.5, verniz: 0.3 });
      for (const y of [0.42, 0.58, 0.74]) {
        const w = 0.26 * Math.sqrt(1 - ((y - 0.58) / 0.42) ** 2);
        g.add(segmento([-w, y, 0.47], [w, y, 0.47], 0.018, lista));
      }
    }
    if (gato) {
      const listra = vinil(c.detalhe);
      for (const l of [-1, 1]) for (const y of [0.55, 0.75]) {
        const s = segmento([l * 0.47, y, 0.08], [l * 0.36, y - 0.02, 0.3], 0.03, listra);
        g.add(s);
      }
    }

    // rabo e asinhas (atrás)
    if (especie === 'cachorrinho') {
      const ang = pose === 'feliz' ? 0.25 : 0;
      const r = tubo([[0.3, 0.55, -0.32], [0.6, 0.62, -0.42], [0.72 + ang * 0.3, 0.92, -0.38]], 0.08, 0.06, corpoM);
      g.add(r);
    } else if (gato) {
      const r = tubo([[0.32, 0.4, -0.3], [0.78, 0.36, -0.3], [0.88, 0.7, -0.25], [0.76, 1.02, -0.18]], 0.075, 0.07, corpoM);
      g.add(r);
      g.add(em(bola(0.085, barrigaM, 16), 0.75, 1.07, -0.17));
    } else {
      const r = tubo([[0.3, 0.32, -0.3], [0.72, 0.24, -0.25], [0.98, 0.42, -0.15]], 0.15, 0.05, corpoM);
      g.add(r);
      const coracao = vinil('#ff8fb8', { rugosidade: 0.3, verniz: 0.8 });
      const h = new THREE.Shape();
      h.moveTo(0, -0.12);
      h.bezierCurveTo(-0.16, -0.02, -0.14, 0.12, -0.06, 0.12);
      h.bezierCurveTo(-0.02, 0.12, 0, 0.08, 0, 0.06);
      h.bezierCurveTo(0, 0.08, 0.02, 0.12, 0.06, 0.12);
      h.bezierCurveTo(0.14, 0.12, 0.16, -0.02, 0, -0.12);
      const geo = new THREE.ExtrudeGeometry(h, { depth: 0.04, bevelEnabled: true, bevelThickness: 0.03, bevelSize: 0.025, bevelSegments: 4, curveSegments: 16 });
      geo.center();
      const ch = malha(geo, coracao);
      ch.rotation.set(0, -0.4, -0.5);
      g.add(em(ch, 1.04, 0.52, -0.12));
      // asinhas de morcego, atrás dos ombros
      const asaM = vinil(c.detalhe, { rugosidade: 0.35, verniz: 0.7 });
      const abre = pose === 'feliz' ? 0.25 : 0;
      for (const l of [-1, 1]) {
        const s = new THREE.Shape();
        s.moveTo(0, 0);
        s.lineTo(0.62, 0.42);
        s.quadraticCurveTo(0.56, 0.18, 0.62, 0.0);
        s.quadraticCurveTo(0.46, 0.02, 0.42, -0.14);
        s.quadraticCurveTo(0.3, 0.0, 0.2, -0.18);
        s.quadraticCurveTo(0.14, -0.04, 0, 0);
        const geo2 = new THREE.ExtrudeGeometry(s, { depth: 0.03, bevelEnabled: true, bevelThickness: 0.025, bevelSize: 0.025, bevelSegments: 3, curveSegments: 12 });
        const w = malha(geo2, asaM);
        w.scale.set(l, 1, 1);
        w.rotation.set(0, l * 0.5, l * abre);
        g.add(em(w, l * 0.18, 0.84, -0.32));
      }
    }

    // braços
    const angD = { parado: 0.3, falando: 2.5, feliz: 2.55, dormindo: 0.15 }[pose];
    const angE = { parado: 0.3, falando: 0.3, feliz: 2.55, dormindo: 0.15 }[pose];
    g.add(braco(1, angD, corpoM), braco(-1, angE, corpoM));

    // coleira com plaquinha (não no dragão)
    if (!dragao) {
      const col = malha(new THREE.TorusGeometry(0.33, 0.055, 12, 40), brilho('#e63946'));
      col.rotation.x = Math.PI / 2 - 0.12;
      g.add(em(col, 0, 1.04, 0.02));
      g.add(em(bola(0.075, brilho('#ffc93c', { emissivo: '#a87000' }), 20), 0, 0.96, 0.36));
    }

    // cabeça
    const R = 0.62;
    const cab = new THREE.Group();
    const cabeca = bola(R, corpoM, 56);
    cab.add(cabeca);
    const fechado = pose === 'feliz' || pose === 'dormindo';
    for (const l of [-1, 1]) {
      if (fechado) cab.add(olhoFechado(R, l * 0.23, 0.02, 0.085, pose === 'feliz'));
      else cab.add(olhoAberto(R, l * 0.23, 0.04, 0.13));
      cab.add(bochecha(R, l * 0.4, -0.18, 0.09));
    }
    // focinho
    const foc = bola(1, especie === 'dragaozinho' ? vinil('#9be596') : barrigaM, 32);
    const fr = especie === 'dragaozinho' ? [0.3, 0.19, 0.16] : gato ? [0.21, 0.14, 0.12] : [0.25, 0.18, 0.16];
    foc.scale.set(...fr);
    naSuperficie(foc, R, 0, -0.17, -0.07);
    cab.add(foc);
    if (dragao) {
      for (const l of [-1, 1]) {
        const n = bola(0.025, PRETO(), 10);
        naSuperficie(n, R + 0.08, l * 0.08, -0.1, 0);
        cab.add(n);
      }
    } else {
      const nariz = bola(1, gato ? brilho('#ff6f91') : PRETO(), 20);
      nariz.scale.set(gato ? 0.05 : 0.075, gato ? 0.04 : 0.055, 0.05);
      naSuperficie(nariz, R + 0.1, 0, -0.1, 0);
      cab.add(nariz);
    }
    const aberta = pose === 'falando' || pose === 'feliz';
    const bocaG = boca(R + 0.07, -0.28, aberta ? 0.075 : 0.06, aberta);
    cab.add(bocaG);

    // detalhes de cada espécie
    if (especie === 'cachorrinho') {
      const ore = vinil(c.orelha);
      for (const l of [-1, 1]) {
        const o = bola(0.24, ore, 28);
        o.scale.set(0.55, 1.15, 0.38);
        o.rotation.z = l * (pose === 'feliz' ? 0.7 : 0.3);
        cab.add(em(o, l * 0.6, -0.04, 0.0));
      }
      const mancha = bola(1, vinil(c.detalhe), 24);
      mancha.scale.set(0.17, 0.15, 0.06);
      naSuperficie(mancha, R, 0.23, 0.08, -0.03);
      cab.add(mancha);
    } else if (gato) {
      const dentro = vinil(c.orelha, { rugosidade: 0.5 });
      for (const l of [-1, 1]) {
        const o = malha(new THREE.ConeGeometry(0.2, 0.38, 32), corpoM);
        o.scale.set(1, 1, 0.55);
        o.rotation.z = -l * 0.35;
        cab.add(em(o, l * 0.36, 0.5, 0));
        const oi = malha(new THREE.ConeGeometry(0.11, 0.24, 24), dentro);
        oi.scale.set(1, 1, 0.4);
        oi.rotation.z = -l * 0.35;
        cab.add(em(oi, l * 0.355, 0.49, 0.07));
        for (const k of [-1, 1]) {
          const bigode = segmento([l * 0.22, -0.16 + k * 0.03, 0.6], [l * 0.52, -0.12 + k * 0.08, 0.44], 0.009, plastico('#ffffff', { rugosidade: 0.3 }));
          cab.add(bigode);
        }
      }
      const listra = vinil(c.detalhe);
      for (const k of [-1, 0, 1]) {
        const s = capsula(0.03, 0.12, listra);
        naSuperficie(s, R, k * 0.13, 0.4 - Math.abs(k) * 0.04, -0.02);
        s.rotateZ(-k * 0.25);
        cab.add(s);
      }
    } else {
      const chifreM = vinil(c.orelha, { rugosidade: 0.35, verniz: 0.7 });
      for (const l of [-1, 1]) {
        const ch = tubo([[l * 0.26, 0.48, 0], [l * 0.38, 0.68, -0.02], [l * 0.48, 0.78, -0.04]], 0.08, 0.035, chifreM, 24);
        cab.add(ch);
      }
      const crista = vinil(c.detalhe, { rugosidade: 0.35, verniz: 0.7 });
      for (const [y, z, s] of [[0.6, -0.12, 1], [0.48, -0.38, 0.85], [0.26, -0.56, 0.7]]) {
        const p = malha(new THREE.ConeGeometry(0.08 * s, 0.2 * s, 20), crista);
        p.position.set(0, y, z);
        p.lookAt(0, y * 3, z * 3);
        p.rotateX(Math.PI / 2);
        cab.add(p);
      }
    }
    ancora(cab, 'cabeca', 0, 0, 0);
    ancora(cab, 'topoDaCabeca', 0, R, 0);
    cab.rotation.z = pose === 'dormindo' ? 0.16 : pose === 'feliz' ? -0.06 : 0;
    cab.rotation.x = pose === 'dormindo' ? 0.1 : 0;
    g.add(em(cab, 0, 1.6, 0.02));

    if (pose === 'dormindo') g.add(em(zzz(0.46), 0.78, 2.28, 0.2));

    g.add(moldura(-1.12, 1.12, 0, 2.6, -0.7, 0.85));
    g.rotation.y = -0.18;
    return g;
  };
}
