// Peças 3D do Prêmio e do Quase (PRD visual 3D, seção 6, Fase 5; maquete 3_premio).
//
// * Presente rosa de fita amarela, oco por dentro (para o prêmio sair dele), que abre em quadros: todos
//   os quadros têm o mesmo enquadramento (moldura invisível com a tampa em todas as posições), então o
//   app troca um pelo outro sem a caixa pular.
// * Pódio branco de faixa amarela, pilhas de moedas contáveis, faíscas, confete, coroa e troféu.
// * Quase: estrela dormindo (cinza lilás, a estrela vazia com carinha) e a semente que cresce num vaso.
//
// As estrelas cheia, vazia e brilhando e a moeda são da Fase 0 e 2 (interface.js, moeda.js): reusadas.
import { THREE, plastico, massinha, malha, caixa, bola, capsula, cilindro, em, ancora, ouro } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { moeda } from './moeda.js';
import { halo, faisca } from './interface.js';
import { texto3D } from './icones.js';

const ROSA = '#ff4f8b';
const FITA = '#ffd23f';
const rosa = () => plastico(ROSA, { rugosidade: 0.24, verniz: 1, rugosidadeVerniz: 0.12, ambiente: 0.5 });
const fita = () => plastico(FITA, { rugosidade: 0.2, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.45 });

/** Tudo que estiver em [obj] some do render, mas conta no enquadramento (moldura). */
function invisivel(obj) {
  obj.traverse((o) => { o.visible = false; });
  return obj;
}

/** Retângulo de cantos redondos (para extrudar). */
function retanguloRedondo(l, p, r, alvo = new THREE.Shape()) {
  const x = -l / 2, y = -p / 2;
  alvo.moveTo(x + r, y);
  alvo.lineTo(x + l - r, y); alvo.quadraticCurveTo(x + l, y, x + l, y + r);
  alvo.lineTo(x + l, y + p - r); alvo.quadraticCurveTo(x + l, y + p, x + l - r, y + p);
  alvo.lineTo(x + r, y + p); alvo.quadraticCurveTo(x, y + p, x, y + p - r);
  alvo.lineTo(x, y + r); alvo.quadraticCurveTo(x, y, x + r, y);
  return alvo;
}

// ---------------------------------------------------------------- presente

const LADO = 1.6; // largura da caixa
const ALTURA = 1.3;
const PAREDE = 0.16;

/** Corpo da caixa, oco: parede de cantos redondos com borda boleada em cima, e o fundo por dentro. */
function corpoDoPresente() {
  const g = new THREE.Group();
  const b = 0.06;
  const fora = retanguloRedondo(LADO - 2 * b, LADO - 2 * b, 0.16);
  fora.holes.push(new THREE.Path().copy(retanguloRedondo(LADO - 2 * PAREDE + 2 * b, LADO - 2 * PAREDE + 2 * b, 0.13)));
  const geo = new THREE.ExtrudeGeometry(fora, { depth: ALTURA - 2 * b, bevelEnabled: true, bevelThickness: b, bevelSize: b, bevelSegments: 6, curveSegments: 16 });
  geo.rotateX(-Math.PI / 2);
  geo.translate(0, b, 0);
  g.add(malha(geo, rosa()));
  // fundo e forro (um rosa mais escuro por dentro, que a luz interna esquenta)
  const forro = plastico('#c42a66', { rugosidade: 0.6, verniz: 0.2, ambiente: 0.35 });
  const vao = LADO - 2 * PAREDE;
  g.add(em(caixa(vao + 0.02, 0.1, vao + 0.02, 0.04, forro), 0, 0.1, 0));
  // forro das paredes por dentro: a boca da caixa aberta lê como oca
  for (let k = 0; k < 4; k++) {
    const placa = em(caixa(vao - 0.02, ALTURA - 0.2, 0.03, 0.012, forro), 0, ALTURA / 2 + 0.02, vao / 2 - 0.012);
    const lado = new THREE.Group();
    lado.add(placa);
    lado.rotation.y = (k * Math.PI) / 2;
    g.add(lado);
  }
  // fitas nas quatro faces, do chão até a borda, dobrando um pouquinho para dentro
  const f = fita();
  for (let k = 0; k < 4; k++) {
    const lado = new THREE.Group();
    lado.add(em(caixa(0.32, ALTURA - 0.02, 0.05, 0.02, f), 0, ALTURA / 2, LADO / 2 + 0.005));
    lado.add(em(caixa(0.32, 0.05, PAREDE + 0.04, 0.02, f), 0, ALTURA + 0.005, LADO / 2 - PAREDE / 2));
    lado.add(em(caixa(0.32, 0.22, 0.04, 0.02, f), 0, ALTURA - 0.1, LADO / 2 - PAREDE - 0.01));
    lado.rotation.y = (k * Math.PI) / 2;
    g.add(lado);
  }
  return g;
}

/** Laço de duas alças (como na maquete): argolas de fita em pé, nó redondo e duas pontas. */
function laco() {
  const g = new THREE.Group();
  const f = fita();
  for (const lado of [-1, 1]) {
    const alca = malha(new THREE.TorusGeometry(0.3, 0.085, 18, 40), f);
    alca.scale.set(1.05, 0.85, 0.75);
    alca.rotation.set(0.1, lado * 0.35, lado * 0.55);
    g.add(em(alca, lado * 0.33, 0.27, 0));
    const ponta = capsula(0.07, 0.34, f);
    ponta.scale.set(1.5, 1, 0.45);
    ponta.rotation.set(0.5, 0, lado * 2.2);
    g.add(em(ponta, lado * 0.2, 0.02, 0.22));
  }
  g.add(em(bola(0.15, f, 24), 0, 0.12, 0));
  return g;
}

/** Tampa: um pouco mais larga que a caixa, fitas cruzadas e o laço. Origem no meio da tampa. */
function tampaDoPresente() {
  const g = new THREE.Group();
  g.add(caixa(LADO + 0.16, 0.36, LADO + 0.16, 0.13, rosa()));
  const f = fita();
  g.add(caixa(0.34, 0.39, LADO + 0.2, 0.06, f));
  g.add(caixa(LADO + 0.2, 0.39, 0.34, 0.06, f));
  g.add(em(laco(), 0, 0.18, 0));
  return g;
}

/** Posições da tampa nos quadros de abrir: [x, y, z, giroX, giroY, giroZ]. */
const TAMPA = [
  [0, ALTURA + 0.1, 0, 0, 0, 0], // 0 fechado
  [0.12, ALTURA + 0.55, 0.04, 0.08, 0.05, -0.2], // 1 a tampa levanta e entorta
  [0.55, ALTURA + 1.25, -0.1, 0.18, 0.12, -0.45], // 2 sobe, a luz aparece
  [1.15, ALTURA + 1.85, -0.35, 0.3, 0.2, -0.75], // 3 voando, como na maquete
  [1.45, ALTURA + 2.05, -0.5, 0.36, 0.26, -0.9], // 4 aberto, a tampa parada no alto
];
/** Força da luz que sai da caixa em cada quadro (0 a 1). */
const LUZ = [0, 0.35, 0.8, 1, 0.6];

/** Facho de luz quente que sai da caixa aberta (cone aberto com degradê). */
function facho(forca) {
  const cv = document.createElement('canvas');
  cv.width = 8; cv.height = 256;
  const ctx = cv.getContext('2d');
  const gr = ctx.createLinearGradient(0, 256, 0, 0);
  gr.addColorStop(0, `rgba(255,246,200,${0.95 * forca})`);
  gr.addColorStop(0.35, `rgba(255,236,160,${0.5 * forca})`);
  gr.addColorStop(1, 'rgba(255,230,150,0)');
  ctx.fillStyle = gr; ctx.fillRect(0, 0, 8, 256);
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  const altura = 2.6;
  const geo = new THREE.CylinderGeometry(1.25, (LADO - 2 * PAREDE) / 2 * 1.2, altura, 48, 1, true);
  const m = new THREE.Mesh(geo, new THREE.MeshBasicMaterial({ map: tex, transparent: true, depthWrite: false, side: THREE.DoubleSide, toneMapped: false }));
  m.position.y = ALTURA + altura / 2 - 0.15;
  m.renderOrder = 2;
  return m;
}

/**
 * O presente num quadro de abrir (0 = fechado, 4 = aberto). Com [moldura], ganha a tampa invisível em
 * todas as posições e o facho no máximo: todos os quadros saem com o mesmo recorte.
 */
function presente(quadro, { moldura = true, comTampa = true, luz = true } = {}) {
  const fora = new THREE.Group();
  const g = new THREE.Group();
  g.rotation.y = -0.35; // como na maquete: um canto um pouco virado para a câmera
  fora.add(g);
  const corpo = corpoDoPresente();
  g.add(corpo);
  fora.userData.base = corpo; // a sombra de contato é a da caixa, não a da tampa voando
  const pos = (t, [x, y, z, rx, ry, rz]) => { t.position.set(x, y, z); t.rotation.set(rx, ry, rz); return t; };
  if (comTampa) g.add(pos(tampaDoPresente(), TAMPA[quadro]));
  const forca = luz ? LUZ[quadro] : 0;
  if (forca > 0) {
    g.add(facho(forca));
    const h = halo(1.5 * (0.6 + forca * 0.5), '#fff2b0');
    h.material.opacity = Math.min(1, forca * 1.1);
    h.lookAt(0, 0.42, 1); // virado para a câmera do tipo objeto
    g.add(em(h, 0, ALTURA + 0.25, 0));
    const l = new THREE.PointLight('#ffd98a', 9 * forca, 5, 1.6);
    l.position.set(0, ALTURA - 0.35, 0);
    g.add(l);
    // faíscas saindo da caixa
    if (forca >= 0.6) {
      const branco = plastico('#fffbe6', { rugosidade: 0.15, verniz: 1, emissivo: '#fff2b0', ambiente: 0.6 });
      const pontos = quadro === 3
        ? [[-0.75, 2.35, 0.3, 0.2], [0.55, 2.75, 0.2, 0.15], [-0.25, 3.05, 0.1, 0.13], [0.85, 1.95, 0.6, 0.12], [-1.05, 1.75, 0.5, 0.11]]
        : [[-0.6, 2.2, 0.3, 0.16], [0.45, 2.55, 0.2, 0.13], [-0.1, 2.85, 0.1, 0.1]];
      for (const [x, y, z, r] of pontos) {
        const fa = faisca(r, branco);
        fa.lookAt(0, 0.42, 1);
        g.add(em(fa, x, y, z));
      }
    }
  }
  if (moldura) {
    for (const t of TAMPA) g.add(invisivel(pos(tampaDoPresente(), t)));
    g.add(invisivel(facho(1)));
    g.add(invisivel(em(caixa(2.6, 0.1, 0.1, 0.02, rosa()), 0, 3.3, 0)));
  }
  ancora(fora, 'boca', 0, ALTURA, 0);
  ancora(fora, 'centro', 0, ALTURA / 2, 0);
  const t = TAMPA[quadro];
  if (comTampa) {
    const pt = new THREE.Vector3(t[0], t[1], t[2]).applyAxisAngle(new THREE.Vector3(0, 1, 0), g.rotation.y);
    ancora(fora, 'tampa', pt.x, pt.y, pt.z);
  }
  return fora;
}

export const presenteFechado = () => presente(0, { moldura: false });
export const presenteAbrindo = (q) => () => presente(q);
export const caixaAberta = () => presente(4, { moldura: false, comTampa: false, luz: false });
export function tampaSolta() {
  const g = new THREE.Group();
  const t = tampaDoPresente();
  t.rotation.set(0.3, -0.15, -0.35);
  g.add(t);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

/** Luz macia com raios (o clarão atrás do presente aberto, na maquete). */
export function clarao() {
  const g = new THREE.Group();
  g.add(halo(1.6, '#fff2b0'));
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

// ---------------------------------------------------------------- pódio

/** Pódio redondo branco com faixa amarela (maquete 3). Topo em y = 0; âncoras onde os irmãos ficam. */
export function podio() {
  const g = new THREE.Group();
  const branco = plastico('#ffffff', { rugosidade: 0.3, verniz: 1, rugosidadeVerniz: 0.15, ambiente: 0.45 });
  const R = 3.0;
  // perfil torneado: topo levemente boleado, saia que alarga até a faixa
  const perfil = [
    [0, -0.48], [R + 0.08, -0.48], [R + 0.04, -0.4], [R, -0.12], [R - 0.03, -0.03], [R - 0.12, 0], [0, 0],
  ].map(([x, y]) => new THREE.Vector2(x, y));
  g.add(malha(new THREE.LatheGeometry(perfil, 96), branco));
  const faixa = cilindro(R + 0.17, R + 0.2, 0.2, fita(), 96);
  g.add(em(faixa, 0, -0.55, 0));
  const pe = cilindro(R + 0.12, R + 0.02, 0.18, branco, 96);
  g.add(em(pe, 0, -0.74, 0));
  // friso dourado no topo
  const friso = malha(new THREE.TorusGeometry(R - 0.06, 0.045, 12, 120), ouro('#ffc933'));
  friso.rotation.x = Math.PI / 2;
  g.add(em(friso, 0, -0.04, 0));
  ancora(g, 'centro', 0, 0, 0.4);
  ancora(g, 'esquerda', -2.25, 0, 0.8);
  ancora(g, 'direita', 2.25, 0, 0.8);
  ancora(g, 'fundo', 0, 0, -1.4);
  // palco oval (mais largo que fundo), como o pódio visto quase de frente na maquete
  g.scale.set(1, 1, 0.62);
  return g;
}

// ---------------------------------------------------------------- moedas

/**
 * Pilha de [n] moedas (1, 2, 3, 5) que dá para contar: todas em pé, de frente, mostrando a estrela,
 * empilhadas em pirâmide (5 = 3 embaixo e 2 em cima; 3 = 2 e 1), cada uma um pouco desencontrada.
 */
const FILEIRAS = { 1: [1], 2: [2], 3: [2, 1], 4: [3, 1], 5: [3, 2] };
export function pilhaDeMoedas(n) {
  return () => {
    const g = new THREE.Group();
    const passo = 0.84; // as moedas se encostam (diâmetro ~1,03): sobrepõem um pouquinho
    let k = 0;
    FILEIRAS[n].forEach((qtd, fila) => {
      for (let i = 0; i < qtd; i++, k++) {
        const m = moeda(1);
        const x = (i - (qtd - 1) / 2) * passo;
        // fila de cima um pouco à frente (encostada nas de baixo) e cada moeda com um giro próprio
        m.rotation.set(-0.1 - fila * 0.06, -0.3 + ((k * 37) % 5) * 0.06, ((k * 53) % 7 - 3) * 0.03);
        g.add(em(m, x, 0.52 + fila * 0.8, fila * 0.16 + (i % 2) * 0.05));
      }
    });
    const alto = FILEIRAS[n].length;
    ancora(g, 'centro', 0, 0.52 + (alto - 1) * 0.4, 0);
    return g;
  };
}

// ---------------------------------------------------------------- faíscas e confete

export function faiscaSolta(cor, emissivo) {
  return () => {
    const g = new THREE.Group();
    const f = faisca(1, plastico(cor, { rugosidade: 0.15, verniz: 1, emissivo, ambiente: 0.6 }));
    f.rotation.set(0.2, -0.3, 0.1);
    g.add(f);
    ancora(g, 'centro', 0, 0, 0);
    return g;
  };
}

/** Cores das festas do app (ui/componentes/Confete.kt, CoresDaFesta), um tom mais forte para o tom ACES. */
export const CORES_DO_CONFETE = {
  vermelho: '#ff4f4f', amarelo: '#ffc21a', verde: '#2fc25b', azul: '#3a86ff', roxo: '#a466ff', rosa: '#ff6fa6',
};

/** Papel de confete levemente curvado. */
function papel(mat) {
  const geo = new THREE.CylinderGeometry(1.1, 1.1, 0.34, 24, 1, true, -0.38, 0.76);
  const m = malha(geo, mat, { sombra: false });
  m.material = mat.clone();
  m.material.side = THREE.DoubleSide;
  const g = new THREE.Group();
  em(m, 0, 0, -1.1);
  g.add(m);
  return g;
}

/** Serpentina: tubinho em espiral. */
function serpentina(mat) {
  const pts = [];
  for (let i = 0; i <= 80; i++) {
    const t = i / 80;
    const a = t * Math.PI * 4.2;
    pts.push(new THREE.Vector3(Math.cos(a) * 0.22, (t - 0.5) * 0.95, Math.sin(a) * 0.22));
  }
  return malha(new THREE.TubeGeometry(new THREE.CatmullRomCurve3(pts), 120, 0.06, 12), mat, { sombra: false });
}

export const FORMAS_DO_CONFETE = {
  papel: (mat) => { const p = papel(mat); p.rotation.set(0.5, 0.6, 0.7); return p; },
  estrela: (mat) => { const e = estrela3D(0.42, 0.12, mat, 5, { segmentos: 5 }); e.rotation.set(0.3, -0.45, 0.25); return e; },
  bolinha: (mat) => {
    // disquinho de borda redonda (pastilha), meio de lado
    const g = new THREE.Group();
    g.add(cilindro(0.34, 0.34, 0.1, mat, 40));
    const borda = malha(new THREE.TorusGeometry(0.34, 0.05, 12, 48), mat, { sombra: false });
    borda.rotation.x = Math.PI / 2;
    g.add(borda);
    g.rotation.set(1.0, 0.3, 0.4);
    return g;
  },
  serpentina: (mat) => { const s = serpentina(mat); s.rotation.set(0.3, 0, 0.55); return s; },
};

export function confeteSolto(forma, cor) {
  return () => {
    const g = new THREE.Group();
    g.add(FORMAS_DO_CONFETE[forma](plastico(cor, { rugosidade: 0.3, verniz: 0.9, rugosidadeVerniz: 0.15, ambiente: 0.5 })));
    ancora(g, 'centro', 0, 0, 0);
    return g;
  };
}

// ---------------------------------------------------------------- campeão

/** Coroa dourada de cinco pontas com bolinhas e pedras coloridas. */
export function coroa() {
  const g = new THREE.Group();
  const o = ouro('#ffc21a');
  const R = 1.0;
  // aro com espessura (perfil torneado fechado)
  const perfil = [[R - 0.12, 0], [R + 0.04, 0], [R + 0.08, 0.05], [R + 0.08, 0.5], [R + 0.03, 0.55], [R - 0.12, 0.55], [R - 0.12, 0]]
    .map(([x, y]) => new THREE.Vector2(x, y));
  g.add(malha(new THREE.LatheGeometry(perfil, 96), o));
  // friso embaixo
  const friso = malha(new THREE.TorusGeometry(R + 0.06, 0.07, 14, 96), ouro('#ffb81a'));
  friso.rotation.x = Math.PI / 2;
  g.add(em(friso, 0, 0.04, 0));
  const pedras = ['#ff3d6e', '#3a86ff', '#2fc25b', '#a466ff', '#ff8a1c'];
  for (let k = 0; k < 5; k++) {
    const a = (k / 5) * Math.PI * 2;
    // ponta: triângulo de cantos redondos extrudado, em pé sobre o aro, virado para fora
    const tri = new THREE.Shape();
    tri.moveTo(-0.34, 0); tri.lineTo(0.34, 0); tri.quadraticCurveTo(0.12, 0.4, 0.02, 0.78); tri.quadraticCurveTo(0, 0.82, -0.02, 0.78); tri.quadraticCurveTo(-0.12, 0.4, -0.34, 0);
    const geo = new THREE.ExtrudeGeometry(tri, { depth: 0.08, bevelEnabled: true, bevelThickness: 0.04, bevelSize: 0.04, bevelSegments: 5, curveSegments: 16 });
    geo.translate(0, 0, -0.04);
    const pivo = new THREE.Group();
    pivo.rotation.y = a;
    pivo.add(em(malha(geo, o), 0, 0.46, R - 0.03));
    g.add(pivo);
    const bolinha = bola(0.11, ouro('#ffd84a'), 24);
    bolinha.position.set(Math.sin(a) * (R - 0.03), 1.3, Math.cos(a) * (R - 0.03));
    g.add(bolinha);
    const pedra = bola(0.14, plastico(pedras[k], { rugosidade: 0.08, verniz: 1, rugosidadeVerniz: 0.05, ambiente: 0.7 }), 24);
    pedra.scale.set(1, 1.15, 0.55);
    pedra.position.set(Math.sin(a) * (R + 0.1), 0.28, Math.cos(a) * (R + 0.1));
    pedra.lookAt(Math.sin(a) * 5, 0.28, Math.cos(a) * 5);
    g.add(pedra);
  }
  g.rotation.set(0.2, 0.2, -0.08);
  const fora = new THREE.Group();
  fora.add(g);
  ancora(fora, 'centro', 0, 0.6, 0);
  return fora;
}

/** Troféu de campeão: taça dourada com alças, estrela na frente e base de bloquinhos roxos. */
export function trofeu() {
  const g = new THREE.Group();
  const o = ouro('#ffc21a');
  const roxo = plastico('#7a4dff', { rugosidade: 0.25, verniz: 1, rugosidadeVerniz: 0.12, ambiente: 0.5 });
  g.add(em(caixa(1.5, 0.36, 1.1, 0.12, roxo), 0, 0.18, 0));
  g.add(em(caixa(1.1, 0.3, 0.8, 0.1, roxo), 0, 0.51, 0));
  // pé e taça torneados
  const perfil = [
    [0, 0.66], [0.42, 0.66], [0.4, 0.74], [0.16, 0.86], [0.12, 1.15], [0.2, 1.3], [0.5, 1.45], [0.78, 1.75],
    [0.9, 2.15], [0.92, 2.45], [0.97, 2.5], [0.96, 2.56], [0.86, 2.55], [0.82, 2.4], [0.7, 2.0], [0, 1.9],
  ].map(([x, y]) => new THREE.Vector2(x, y));
  g.add(malha(new THREE.LatheGeometry(perfil, 80), o));
  for (const lado of [-1, 1]) {
    const alca = malha(new THREE.TorusGeometry(0.34, 0.075, 16, 40, Math.PI * 1.25), o);
    alca.rotation.z = lado === 1 ? -Math.PI * 0.625 : Math.PI * 0.375;
    g.add(em(alca, lado * 0.92, 2.0, 0));
  }
  const estrela = estrela3D(0.36, 0.1, plastico('#ff4f6e', { rugosidade: 0.15, verniz: 1, ambiente: 0.6 }), 5, { segmentos: 5 });
  estrela.rotation.x = -0.2;
  g.add(em(estrela, 0, 2.02, 0.84));
  g.rotation.y = -0.25;
  const fora = new THREE.Group();
  fora.add(g);
  ancora(fora, 'centro', 0, 1.5, 0);
  return fora;
}

// ---------------------------------------------------------------- Quase

/** Estrela dormindo: a estrela vazia (cinza lilás) de olhinhos fechados, bochechas e um "z z". */
export function estrelaDormindo() {
  const g = new THREE.Group();
  const corpo = new THREE.Group();
  const e = estrela3D(1.0, 0.3, plastico('#c9c4d6', { rugosidade: 0.42, verniz: 0.7, rugosidadeVerniz: 0.3, ambiente: 0.5 }), 5, { segmentos: 8 });
  corpo.add(e);
  const frente = 0.33;
  const escuro = plastico('#5b5270', { rugosidade: 0.3, verniz: 0.8 });
  for (const lado of [-1, 1]) {
    // olho fechado: arco virado para baixo (pálpebra), com cílios curtinhos
    const olho = malha(new THREE.TorusGeometry(0.13, 0.032, 10, 24, Math.PI), escuro, { sombra: false });
    olho.rotation.z = Math.PI;
    corpo.add(em(olho, lado * 0.24, 0.04, frente));
    const boc = bola(1, plastico('#ff9aae', { rugosidade: 0.8, verniz: 0, opacidade: 0.75 }), 24);
    boc.scale.set(0.11, 0.07, 0.03);
    corpo.add(em(boc, lado * 0.34, -0.15, frente - 0.01));
  }
  // boquinha tranquila
  const boca = malha(new THREE.TorusGeometry(0.06, 0.022, 8, 20, Math.PI), escuro, { sombra: false });
  boca.rotation.z = Math.PI;
  corpo.add(em(boca, 0, -0.16, frente));
  corpo.rotation.set(0.1, -0.2, 0.18); // cabecinha tombada de sono
  g.add(corpo);
  // z z em lilás
  const lilas = plastico('#9a7cff', { rugosidade: 0.25, verniz: 1, ambiente: 0.5 });
  const z1 = texto3D('z', 0.42, lilas, 0.08); z1.rotation.z = 0.2; g.add(em(z1, 0.95, 0.95, 0.1));
  const z2 = texto3D('z', 0.28, lilas, 0.06); z2.rotation.z = 0.25; g.add(em(z2, 1.28, 1.35, 0.1));
  ancora(g, 'centro', 0, 0, 0);
  return g;
}

/** Vasinho de barro com terra, de onde a semente cresce. Topo da terra em y = 0.9. */
function vaso() {
  const g = new THREE.Group();
  const barro = plastico('#e8784a', { rugosidade: 0.45, verniz: 0.6, ambiente: 0.45 });
  const perfil = [[0, 0], [0.5, 0], [0.56, 0.04], [0.68, 0.72], [0.66, 0.74], [0, 0.74]].map(([x, y]) => new THREE.Vector2(x, y));
  g.add(malha(new THREE.LatheGeometry(perfil, 64), barro));
  const borda = malha(new THREE.TorusGeometry(0.72, 0.1, 16, 64), barro);
  borda.rotation.x = Math.PI / 2;
  borda.scale.set(1, 1, 1.6);
  g.add(em(borda, 0, 0.78, 0));
  const terra = cilindro(0.66, 0.66, 0.1, massinha('#7a4a2b'), 48);
  g.add(em(terra, 0, 0.84, 0));
  // grãozinhos de terra
  const t2 = massinha('#8a5634');
  for (const [x, z] of [[0.3, 0.2], [-0.35, 0.1], [0.1, -0.35], [-0.15, 0.38]]) g.add(em(bola(0.06, t2, 12), x, 0.9, z));
  return g;
}

function folha(tam, mat, giro) {
  const s = new THREE.Shape();
  s.moveTo(0, 0);
  s.quadraticCurveTo(tam * 0.55, tam * 0.35, 0, tam);
  s.quadraticCurveTo(-tam * 0.55, tam * 0.35, 0, 0);
  const geo = new THREE.ExtrudeGeometry(s, { depth: tam * 0.04, bevelEnabled: true, bevelThickness: tam * 0.05, bevelSize: tam * 0.05, bevelSegments: 4, curveSegments: 16 });
  geo.translate(0, 0, -tam * 0.02);
  const f = malha(geo, mat);
  const g = new THREE.Group();
  g.add(f);
  g.rotation.set(0, 0, giro);
  return g;
}

/** Semente que cresce, em 4 estágios: 1 semente, 2 broto, 3 plantinha com botão, 4 florzinha. */
export function sementeQueCresce(estagio) {
  return () => {
    const fora = new THREE.Group();
    const v = vaso();
    fora.add(v);
    fora.userData.base = v;
    const verde = plastico('#3dbe5b', { rugosidade: 0.35, verniz: 0.8, ambiente: 0.5 });
    const verdeClaro = plastico('#6fdc6a', { rugosidade: 0.35, verniz: 0.8, ambiente: 0.5 });
    const topo = 0.9;
    if (estagio === 1) {
      const semente = bola(0.16, plastico('#c9873e', { rugosidade: 0.4, verniz: 0.7 }), 32);
      semente.scale.set(1, 1.35, 0.9);
      semente.rotation.z = -0.4;
      fora.add(em(semente, 0.02, topo + 0.08, 0.1));
    } else {
      const h = { 2: 0.35, 3: 0.85, 4: 1.15 }[estagio];
      const caule = capsula(0.05, h, verde);
      caule.rotation.z = 0.06;
      fora.add(em(caule, 0, topo + h / 2, 0));
      const pares = { 2: [[h, 0.32]], 3: [[h * 0.45, 0.42], [h, 0.36]], 4: [[h * 0.35, 0.46], [h * 0.75, 0.38]] }[estagio];
      pares.forEach(([y, tam], i) => {
        for (const lado of [-1, 1]) {
          const f = folha(tam, i % 2 ? verdeClaro : verde, -lado * 1.05);
          f.rotation.y = (i % 2 ? 0.5 : -0.3);
          fora.add(em(f, lado * 0.02, topo + y, 0));
        }
      });
      if (estagio === 3) {
        const botao = bola(0.15, plastico('#ff7fae', { rugosidade: 0.3, verniz: 0.9 }), 24);
        botao.scale.set(0.85, 1.25, 0.85);
        fora.add(em(botao, 0.02, topo + h + 0.12, 0));
      }
      if (estagio === 4) {
        const flor = new THREE.Group();
        const petala = plastico('#ff6fa6', { rugosidade: 0.3, verniz: 0.9, ambiente: 0.5 });
        for (let k = 0; k < 6; k++) {
          const a = (k / 6) * Math.PI * 2;
          const p = bola(0.17, petala, 24);
          p.scale.set(1.15, 0.75, 0.45);
          p.position.set(Math.cos(a) * 0.2, Math.sin(a) * 0.2, 0);
          p.rotation.z = a;
          flor.add(p);
        }
        flor.add(em(bola(0.13, plastico('#ffc21a', { rugosidade: 0.25, verniz: 1 }), 24), 0, 0, 0.06));
        flor.rotation.x = -0.25;
        fora.add(em(flor, 0.03, topo + h + 0.18, 0.05));
      }
    }
    // moldura: a planta maior, invisível, para os quatro estágios saírem no mesmo recorte
    fora.add(invisivel(em(caixa(1.0, 0.1, 0.1, 0.02, verde), 0, topo + 1.6, 0)));
    ancora(fora, 'topoDaTerra', 0, topo, 0);
    return fora;
  };
}
