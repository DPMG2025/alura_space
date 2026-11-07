// Fase 9C (arte, lote 3): os ícones de interface que ainda eram emoji (abas do guarda-roupa e da geladeira, linhas
// do espelho, nós e marcos da trilha, setas da escada, baú e martelinho, abas das áreas da casa e alguns botões
// soltos), no mesmo estilo dos ícones da barra (interface.js, iconesDaBarra.js): glifo roxo em relevo, levemente
// girado, com detalhes brancos. Formas gordinhas e sem traço fino: os nós da trilha e as abas aparecem a 24-40 dp.
import { THREE, plastico, malha, bola, caixa, cilindro, capsula, em } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { roxo, brancoPlastico, glifo, traco, arcoDeTubo, poligono, extrudar } from './interface.js';

/** Roxo mais claro e mais escuro, para separar duas partes do mesmo glifo sem sair da cor. */
const lilas = () => plastico('#8f7bff', { rugosidade: 0.24, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.5 });
const roxoEscuro = () => plastico('#3f25c4', { rugosidade: 0.24, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.5 });
/** Branco que acende um pouco: só para glifos inteiros em branco (selinho do "certo" sobre cor). */
const brancoVivo = () => plastico('#ffffff', { rugosidade: 0.28, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.55 });

/** Põe [obj] mais para a frente (z), sem mexer em x e y. */
function frente(obj, z) {
  obj.position.z = z;
  return obj;
}

/** Elipse plana (rx, ry) como Shape. */
function elipse(rx, ry, cx = 0, cy = 0) {
  const s = new THREE.Shape();
  s.absellipse(cx, cy, rx, ry, 0, Math.PI * 2, false);
  return s;
}

/** Disco virado para a câmera (cilindro deitado), com a face em z = 0 + a/2. */
function disco(r, a, mat, seg = 40) {
  const d = cilindro(r, r, a, mat, seg);
  d.rotation.x = Math.PI / 2;
  return d;
}

/** Bolinha branca achatada (pinta, olho, brilho), um pouco à frente. */
function pinta(x, y, z, r, mat = brancoPlastico()) {
  const b = bola(r, mat, 24);
  b.scale.set(1, 1, 0.5);
  return em(b, x, y, z);
}

/** Estrela de quatro pontas (brilho) extrudada. */
function brilho(r, prof, mat, miolo = 0.3) {
  const pts = [];
  for (let i = 0; i < 8; i++) {
    const a = Math.PI / 2 + (i * Math.PI) / 4;
    const rr = i % 2 === 0 ? r : r * miolo;
    pts.push([Math.cos(a) * rr, Math.sin(a) * rr]);
  }
  return extrudar(poligono(pts, pts.map((_, i) => (i % 2 === 0 ? r * 0.08 : r * 0.12))), prof, mat, Math.min(0.08, r * 0.1));
}

// ================================================================ guarda-roupa e espelho (Gaveta, ParteDoCorpo)

/** 👗 Roupas: vestido rodado, cinto branco e bolinhas brancas na saia. */
export function iconeRoupas() {
  const g = new THREE.Group();
  const f = poligono([
    [-0.36, 0.94], [-0.16, 0.94], [-0.08, 0.66], [0.08, 0.66], [0.16, 0.94], [0.36, 0.94],
    [0.36, 0.42], [0.22, 0.1], [0.9, -0.86], [-0.9, -0.86], [-0.22, 0.1], [-0.36, 0.42],
  ], [0.08, 0.06, 0.1, 0.1, 0.06, 0.08, 0.14, 0.04, 0.16, 0.16, 0.04, 0.14]);
  g.add(extrudar(f, 0.3, roxo(), 0.09));
  g.add(em(caixa(0.56, 0.16, 0.12, 0.06, brancoPlastico()), 0, 0.14, 0.22));
  for (const [x, y] of [[-0.42, -0.6], [0, -0.42], [0.42, -0.6], [-0.2, -0.16], [0.2, -0.16]]) g.add(pinta(x, y, 0.25, 0.09));
  return glifo(g);
}

/** 🎩 Chapéus: cartola com aba larga e faixa branca. */
export function iconeChapeus() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(caixa(1.96, 0.26, 0.9, 0.13, m), 0, -0.74, 0));
  const copa = cilindro(0.6, 0.66, 1.42, m, 48);
  g.add(em(copa, 0, 0.04, 0));
  g.add(em(cilindro(0.6, 0.6, 0.06, m, 48), 0, 0.76, 0));
  const faixa = cilindro(0.675, 0.69, 0.3, brancoPlastico(), 48);
  g.add(em(faixa, 0, -0.42, 0));
  return glifo(g, { giroX: 0.22 });
}

/** 👓 No rosto: óculos redondos, lentes azul-clarinhas com reflexo. */
export function iconeRosto() {
  const g = new THREE.Group();
  const m = roxo();
  const vidro = plastico('#d8ecff', { rugosidade: 0.06, verniz: 1, rugosidadeVerniz: 0.04, ambiente: 0.9, emissivo: '#8fbfff' });
  for (const l of [-1, 1]) {
    g.add(em(malha(new THREE.TorusGeometry(0.44, 0.15, 20, 48), m), l * 0.52, -0.06, 0));
    const lente = disco(0.36, 0.06, vidro);
    g.add(em(lente, l * 0.52, -0.06, 0));
    g.add(frente(traco([l * 0.52 - 0.2, 0.0], [l * 0.52 - 0.04, 0.18], 0.05, brancoPlastico()), 0.06));
    g.add(traco([l * 0.94, 0.12], [l * 1.02, 0.5], 0.11, m));
  }
  g.add(arcoDeTubo([0, -0.02], 0.16, 0.35, Math.PI - 0.35, 0.1, m));
  return glifo(g);
}

/** 🧣 No pescoço: cachecol enrolado com as duas pontas caídas, listras e franjas brancas. */
export function iconePescoco() {
  const g = new THREE.Group();
  const m = roxo();
  const volta = malha(new THREE.TorusGeometry(0.56, 0.24, 24, 56), m);
  volta.scale.set(1, 0.5, 1);
  volta.rotation.x = -0.25;
  g.add(em(volta, 0, 0.52, 0));
  const branco = brancoPlastico();
  for (const [x, y, alt, giro] of [[0.3, -0.18, 1.1, -0.08], [-0.16, -0.08, 0.86, 0.14]]) {
    const ponta = new THREE.Group();
    ponta.add(caixa(0.46, alt, 0.26, 0.12, m));
    for (const yy of [-alt * 0.18, -alt * 0.34]) ponta.add(em(caixa(0.48, 0.08, 0.27, 0.03, branco), 0, yy, 0.005));
    for (const xx of [-0.14, 0, 0.14]) ponta.add(em(capsula(0.04, 0.12, branco), xx, -alt / 2 - 0.08, 0));
    ponta.rotation.z = giro;
    g.add(em(ponta, x, y, x > 0 ? 0.14 : 0.06));
  }
  return glifo(g);
}

/** 🎒 Nas costas: mochila com alça, bolso da frente e zíper branco. */
export function iconeCostas() {
  const g = new THREE.Group();
  const m = roxo();
  // corpo de topo arredondado (arco), como mochila de criança
  const corpo = new THREE.Shape();
  corpo.moveTo(-0.62, -0.78);
  corpo.quadraticCurveTo(-0.62, -0.94, -0.46, -0.94);
  corpo.lineTo(0.46, -0.94);
  corpo.quadraticCurveTo(0.62, -0.94, 0.62, -0.78);
  corpo.lineTo(0.62, 0.16);
  corpo.absarc(0, 0.16, 0.62, 0, Math.PI, false);
  corpo.closePath();
  g.add(extrudar(corpo, 0.5, m, 0.12));
  g.add(arcoDeTubo([0, 0.74], 0.24, 0.15, Math.PI - 0.15, 0.1, roxoEscuro()));
  // alças nas laterais
  for (const l of [-1, 1]) g.add(em(capsula(0.11, 0.9, roxoEscuro()), l * 0.74, -0.14, -0.06));
  // bolso da frente com a tampa branca
  g.add(em(caixa(0.92, 0.62, 0.24, 0.2, lilas()), 0, -0.52, 0.3));
  const branco = brancoPlastico();
  const tampa = new THREE.Shape();
  tampa.moveTo(-0.46, -0.2);
  tampa.lineTo(0.46, -0.2);
  tampa.lineTo(0.46, -0.34);
  tampa.quadraticCurveTo(0, -0.52, -0.46, -0.34);
  tampa.closePath();
  g.add(em(extrudar(tampa, 0.06, branco, 0.04), 0, -0.04, 0.42));
  g.add(pinta(0, -0.42, 0.5, 0.07, roxoEscuro()));
  g.add(frente(arcoDeTubo([0, 0.16], 0.4, Math.PI * 0.62, Math.PI * 0.88, 0.05, branco), 0.36));
  return glifo(g);
}

/** 🎈 Na mão: balão com nó e barbante. */
export function iconeMao() {
  const g = new THREE.Group();
  const m = roxo();
  const b = bola(0.64, m, 48);
  b.scale.set(0.9, 1.06, 0.62);
  g.add(em(b, 0.02, 0.28, 0));
  const no = cilindro(0.04, 0.14, 0.16, m, 24);
  g.add(em(no, 0.02, -0.46, 0));
  g.add(traco([0.02, -0.54], [-0.14, -0.74], 0.05, m), traco([-0.14, -0.74], [0.06, -0.96], 0.05, m));
  const reflexo = bola(0.13, plastico('#ffffff', { rugosidade: 0.1, verniz: 1, emissivo: '#ffffff' }), 24);
  reflexo.scale.set(0.8, 1.3, 0.4);
  g.add(em(reflexo, -0.26, 0.56, 0.36));
  return glifo(g);
}

/** 👟 Nos pés: tênis de lado, sola branca e cadarços brancos. */
export function iconePes() {
  const g = new THREE.Group();
  const f = poligono([[-0.98, -0.34], [-0.98, 0.42], [-0.62, 0.56], [-0.28, 0.46], [0.04, -0.02], [0.66, -0.12], [0.98, -0.22], [0.98, -0.34]],
    [0.06, 0.14, 0.12, 0.1, 0.14, 0.14, 0.16, 0.06]);
  g.add(extrudar(f, 0.5, roxo(), 0.1));
  const branco = brancoPlastico();
  g.add(em(caixa(2.16, 0.28, 0.72, 0.13, branco), 0, -0.5, 0));
  for (const [x, y] of [[-0.24, 0.26], [-0.06, 0.08], [0.14, -0.04]]) g.add(frente(traco([x - 0.14, y - 0.12], [x + 0.12, y + 0.12], 0.055, branco), 0.36));
  g.add(pinta(-0.72, 0.08, 0.36, 0.1));
  return glifo(g);
}

/** 🎀 No cabelo: laço de duas alças, nó e duas fitinhas. */
export function iconeCabelo() {
  const g = new THREE.Group();
  const m = roxo();
  for (const l of [-1, 1]) {
    const alca = extrudar(poligono([[0, 0.04], [l * 0.98, 0.62], [l * 0.98, -0.5]], 0.3), 0.36, m, 0.1);
    g.add(alca);
    const fita = extrudar(poligono([[l * 0.1, -0.1], [l * 0.46, -0.96], [l * 0.24, -0.82], [l * 0.06, -0.96]], 0.08), 0.18, m, 0.07);
    g.add(em(fita, 0, 0, -0.06));
    g.add(frente(traco([l * 0.3, 0.12], [l * 0.62, 0.3], 0.05, brancoPlastico()), 0.3));
  }
  g.add(em(caixa(0.4, 0.46, 0.56, 0.16, lilas()), 0, 0, 0.04));
  return glifo(g);
}

// ================================================================ geladeira (PrateleiraDaGeladeira)

/** 🍎 Frutas e legumes: maçã com cabinho e folha clara. */
export function iconeFrutas() {
  const g = new THREE.Group();
  const m = roxo();
  for (const l of [-1, 1]) {
    const b = bola(0.58, m, 48);
    b.scale.set(1, 1.02, 0.66);
    g.add(em(b, l * 0.26, -0.12, 0));
  }
  g.add(traco([0, 0.36], [0.08, 0.76], 0.07, roxoEscuro()));
  const folha = extrudar(elipse(0.3, 0.14), 0.06, lilas(), 0.05);
  folha.rotation.z = 0.5;
  g.add(em(folha, 0.36, 0.7, 0));
  g.add(pinta(-0.42, 0.14, 0.38, 0.11));
  return glifo(g);
}

/** 🥛 Leite, queijo e ovos: caixinha de leite com a gota branca na frente. */
export function iconeLeite() {
  const g = new THREE.Group();
  const m = roxo();
  const f = poligono([[-0.6, -0.92], [0.6, -0.92], [0.6, 0.34], [0.34, 0.7], [-0.34, 0.7], [-0.6, 0.34]], [0.14, 0.14, 0.06, 0.06, 0.06, 0.06]);
  g.add(extrudar(f, 0.56, m, 0.1));
  g.add(em(caixa(0.76, 0.22, 0.4, 0.08, m), 0, 0.82, 0));
  const gota = new THREE.Shape();
  gota.moveTo(0, 0.4);
  gota.bezierCurveTo(0.08, 0.24, 0.3, 0.02, 0.3, -0.14);
  gota.absarc(0, -0.14, 0.3, 0, Math.PI, true);
  gota.bezierCurveTo(-0.3, 0.02, -0.08, 0.24, 0, 0.4);
  g.add(em(extrudar(gota, 0.05, brancoPlastico(), 0.04), 0, -0.24, 0.36));
  return glifo(g);
}

/** 🧊 Congelador: floco de neve gordinho. */
export function iconeCongelador() {
  const g = new THREE.Group();
  const m = roxo();
  for (let i = 0; i < 3; i++) {
    const a = Math.PI / 2 + (i * Math.PI) / 3;
    const [c, s] = [Math.cos(a), Math.sin(a)];
    g.add(traco([-c * 0.92, -s * 0.92], [c * 0.92, s * 0.92], 0.13, m));
    for (const lado of [1, -1]) {
      const [px, py] = [c * 0.56 * lado, s * 0.56 * lado];
      for (const d of [0.62, -0.62]) {
        const b = a + (lado > 0 ? 0 : Math.PI) + d;
        g.add(traco([px, py], [px + Math.cos(b) * 0.34, py + Math.sin(b) * 0.34], 0.1, m));
      }
    }
  }
  g.add(frente(disco(0.2, 0.3, m), 0));
  g.add(pinta(0, 0, 0.17, 0.1));
  return glifo(g);
}

// ================================================================ espelho (linhas do perfil)

/** 🧑 Pele: rostinho redondo com olhos e sorriso brancos. */
export function iconePele() {
  const g = new THREE.Group();
  const m = roxo();
  const cabeca = bola(0.86, m, 48);
  cabeca.scale.set(1, 0.96, 0.5);
  g.add(cabeca);
  for (const l of [-1, 1]) {
    g.add(em(malha(new THREE.SphereGeometry(0.2, 24, 12), m), l * 0.88, 0, 0));
    const olho = bola(0.13, brancoPlastico(), 24);
    olho.scale.set(0.85, 1.15, 0.5);
    g.add(em(olho, l * 0.3, 0.16, 0.4));
  }
  const sorriso = arcoDeTubo([0, -0.08], 0.34, Math.PI + 0.55, Math.PI * 2 - 0.55, 0.07, brancoPlastico());
  g.add(frente(sorriso, 0.4));
  return glifo(g);
}

/** 💇 Cor do cabelo: cabeça de cabelo grande (franja) e a gota de tinta branca ao lado. */
export function iconeCorDoCabelo() {
  const g = new THREE.Group();
  const rosto = bola(0.6, lilas(), 48);
  rosto.scale.set(1, 1, 0.5);
  g.add(em(rosto, -0.12, -0.24, 0));
  // touca de cabelo: arco por cima da cabeça e a franja em três ondinhas
  const cabelo = new THREE.Shape();
  cabelo.absarc(-0.12, -0.1, 0.84, Math.PI + 0.35, -0.35, true);
  cabelo.lineTo(0.42, 0.04);
  cabelo.quadraticCurveTo(0.28, -0.14, 0.1, 0.06);
  cabelo.quadraticCurveTo(-0.07, -0.16, -0.24, 0.06);
  cabelo.quadraticCurveTo(-0.41, -0.16, -0.6, 0.04);
  cabelo.quadraticCurveTo(-0.72, -0.12, -0.91, -0.39);
  cabelo.closePath();
  g.add(em(extrudar(cabelo, 0.34, roxo(), 0.1), 0, 0, 0.06));
  g.add(frente(arcoDeTubo([-0.12, -0.1], 0.6, Math.PI * 0.58, Math.PI * 0.8, 0.05, brancoPlastico()), 0.3));
  for (const x of [-0.34, 0.1]) {
    const olho = bola(0.08, roxoEscuro(), 16);
    olho.scale.set(0.9, 1.2, 0.5);
    g.add(em(olho, x, -0.32, 0.3));
  }
  const gota = new THREE.Shape();
  gota.moveTo(0, 0.36);
  gota.bezierCurveTo(0.06, 0.22, 0.24, 0.02, 0.24, -0.1);
  gota.absarc(0, -0.1, 0.24, 0, Math.PI, true);
  gota.bezierCurveTo(-0.24, 0.02, -0.06, 0.22, 0, 0.36);
  g.add(em(extrudar(gota, 0.16, brancoPlastico(), 0.06), 0.8, -0.56, 0.2));
  return glifo(g);
}

/** ✂️ Penteado: tesoura aberta, argolas embaixo e parafuso branco. */
export function iconePenteado() {
  const g = new THREE.Group();
  const m = roxo();
  for (const l of [-1, 1]) {
    const lamina = extrudar(poligono([[0, 0], [l * 0.16, 0.08], [l * 0.56, 0.98], [l * 0.46, 1.02], [-l * 0.06, 0.12]], [0.04, 0.06, 0.08, 0.06, 0.04]), 0.14, m, 0.06);
    g.add(em(lamina, 0, -0.1, l * 0.05));
    g.add(traco([0, -0.1], [l * 0.36, -0.52], 0.1, m));
    g.add(em(malha(new THREE.TorusGeometry(0.24, 0.1, 18, 40), m), l * 0.46, -0.7, l * 0.05));
  }
  g.add(em(disco(0.1, 0.36, brancoPlastico(), 24), 0, -0.1, 0));
  return glifo(g);
}

/** 👕 Roupa: camiseta com gola e a estrela branca no peito. */
export function iconeRoupa() {
  const g = new THREE.Group();
  const f = poligono([
    [-0.3, 0.86], [-0.96, 0.52], [-0.72, 0.04], [-0.5, 0.14], [-0.5, -0.9], [0.5, -0.9], [0.5, 0.14], [0.72, 0.04],
    [0.96, 0.52], [0.3, 0.86], [0, 0.6],
  ], [0.1, 0.14, 0.08, 0.04, 0.12, 0.12, 0.04, 0.08, 0.14, 0.1, 0.18]);
  g.add(extrudar(f, 0.28, roxo(), 0.09));
  g.add(em(estrela3D(0.3, 0.05, brancoPlastico(), 5, { segmentos: 4 }), 0, -0.2, 0.24));
  return glifo(g);
}

/** ✏️ Escrever o nome: lápis deitado em diagonal, ponta de madeira branca. */
export function iconeEscrever() {
  const g = new THREE.Group();
  const m = roxo();
  const lapis = new THREE.Group();
  lapis.add(em(cilindro(0.26, 0.26, 1.3, m, 6), 0, 0.2, 0));
  lapis.add(em(cilindro(0.27, 0.27, 0.2, brancoPlastico(), 24), 0, 0.92, 0));
  lapis.add(em(capsula(0.24, 0.12, lilas()), 0, 1.1, 0));
  lapis.add(em(cilindro(0.26, 0.08, 0.42, plastico('#fff3e0', { rugosidade: 0.5, verniz: 0.4, ambiente: 0.5 }), 24), 0, -0.66, 0));
  lapis.add(em(cilindro(0.08, 0.01, 0.16, roxoEscuro(), 16), 0, -0.94, 0));
  lapis.rotation.z = -Math.PI / 4;
  g.add(em(lapis, 0.02, 0.02, 0));
  g.scale.setScalar(0.92);
  return glifo(g);
}

// ================================================================ trilha: tipos de nó e marcos

/** Fase comum: estrela roxa gordinha com brilho branco. */
export function iconeNoFase() {
  const g = new THREE.Group();
  g.add(estrela3D(1.0, 0.3, roxo(), 5, { segmentos: 6 }));
  g.add(frente(traco([-0.3, 0.22], [-0.1, 0.4], 0.06, brancoPlastico()), 0.26));
  return glifo(g);
}

/** 👑 Chefão do mundo: coroa de três pontas com bolinhas brancas e faixa branca. */
export function iconeNoChefao() {
  const g = new THREE.Group();
  const m = roxo();
  const f = poligono([[-0.96, -0.66], [0.96, -0.66], [0.96, 0.5], [0.48, 0.02], [0, 0.66], [-0.48, 0.02], [-0.96, 0.5]],
    [0.12, 0.12, 0.08, 0.1, 0.08, 0.1, 0.08]);
  g.add(extrudar(f, 0.3, m, 0.09));
  const branco = brancoPlastico();
  for (const [x, y] of [[-0.94, 0.6], [0, 0.78], [0.94, 0.6]]) g.add(em(bola(0.16, branco, 24), x, y, 0));
  g.add(em(caixa(1.86, 0.2, 0.08, 0.06, branco), 0, -0.42, 0.22));
  for (const x of [-0.5, 0, 0.5]) g.add(pinta(x, -0.1, 0.24, 0.09));
  return glifo(g);
}

/** 🏆 Troféu (marco vencido, último do mundo): taça com alças, pé e estrela branca. */
export function iconeNoTrofeu() {
  const g = new THREE.Group();
  const m = roxo();
  const taca = poligono([[-0.66, 0.9], [0.66, 0.9], [0.56, 0.18], [0.16, -0.18], [-0.16, -0.18], [-0.56, 0.18]], [0.08, 0.08, 0.3, 0.06, 0.06, 0.3]);
  g.add(extrudar(taca, 0.36, m, 0.1));
  for (const l of [-1, 1]) g.add(arcoDeTubo([l * 0.62, 0.48], 0.26, l > 0 ? -Math.PI / 2 : Math.PI / 2, l > 0 ? Math.PI / 2 : Math.PI * 1.5, 0.09, m));
  g.add(em(caixa(0.26, 0.4, 0.28, 0.08, m), 0, -0.36, 0));
  g.add(em(caixa(0.9, 0.26, 0.44, 0.1, m), 0, -0.64, 0));
  g.add(em(caixa(1.12, 0.2, 0.5, 0.08, roxoEscuro()), 0, -0.86, 0));
  g.add(em(estrela3D(0.26, 0.05, brancoPlastico(), 5, { segmentos: 4 }), 0, 0.46, 0.27));
  return glifo(g);
}

/** 🏋️ Treinar: halteres. */
export function iconeNoTreino() {
  const g = new THREE.Group();
  const m = roxo();
  const barra = cilindro(0.11, 0.11, 1.5, brancoPlastico(), 24);
  barra.rotation.z = Math.PI / 2;
  g.add(barra);
  for (const l of [-1, 1]) {
    g.add(em(caixa(0.3, 1.2, 0.62, 0.14, m), l * 0.44, 0, 0));
    g.add(em(caixa(0.24, 0.84, 0.48, 0.11, m), l * 0.76, 0, 0));
  }
  const giro = new THREE.Group();
  giro.add(g);
  giro.rotation.z = 0.32;
  return glifo(giro);
}

/** Revisão (Relembrar): duas setas em círculo em volta de uma estrelinha branca. */
export function iconeNoRevisao() {
  const g = new THREE.Group();
  const m = roxo();
  const R = 0.72;
  g.add(arcoDeTubo([0, 0], R, 0.4, Math.PI - 0.3, 0.17, m));
  g.add(arcoDeTubo([0, 0], R, Math.PI + 0.4, Math.PI * 2 - 0.3, 0.17, m));
  for (const a of [Math.PI - 0.3, Math.PI * 2 - 0.3]) {
    const f = poligono([[0.38, 0], [-0.24, 0.32], [-0.24, -0.32]], 0.08);
    const p = extrudar(f, 0.3, m, 0.09);
    p.rotation.z = a + Math.PI / 2 + 0.12;
    g.add(em(p, Math.cos(a) * R, Math.sin(a) * R, 0));
  }
  g.add(em(estrela3D(0.34, 0.12, brancoPlastico(), 5, { segmentos: 4 }), 0, 0, 0.04));
  return glifo(g);
}

/** ⚡ Desafio: raio gordinho. */
export function iconeNoDesafio() {
  const g = new THREE.Group();
  const f = poligono([[0.26, 0.98], [-0.66, -0.06], [-0.08, -0.06], [-0.3, -0.98], [0.66, 0.1], [0.08, 0.1]], [0.08, 0.1, 0.04, 0.08, 0.1, 0.04]);
  g.add(extrudar(f, 0.34, roxo(), 0.09));
  g.add(frente(traco([0.08, 0.66], [-0.22, 0.26], 0.05, brancoPlastico()), 0.26));
  return glifo(g);
}

/** ✨ Novidade: brilho grande de quatro pontas e dois pequenos. */
export function iconeNoNovidade() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(brilho(0.82, 0.3, m, 0.34), -0.14, -0.08, 0));
  g.add(em(brilho(0.36, 0.22, m, 0.36), 0.66, 0.6, 0));
  g.add(em(brilho(0.26, 0.2, lilas(), 0.38), 0.7, -0.62, 0));
  g.add(pinta(-0.14, -0.08, 0.24, 0.12));
  return glifo(g);
}

/** 🔒 Fechada: cadeado mais gordo que o dos pais (lê a 24 dp), com buraco da chave branco grande. */
export function iconeNoBloqueado() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(caixa(1.52, 1.2, 0.56, 0.26, m), 0, -0.34, 0));
  const alca = new THREE.Group();
  alca.add(malha(new THREE.TorusGeometry(0.44, 0.17, 20, 48, Math.PI), m));
  for (const x of [-0.44, 0.44]) alca.add(em(cilindro(0.17, 0.17, 0.36, m, 24), x, -0.18, 0));
  g.add(em(alca, 0, 0.42, 0));
  const branco = brancoPlastico();
  g.add(em(disco(0.19, 0.08, branco, 32), 0, -0.22, 0.28));
  g.add(em(caixa(0.18, 0.42, 0.08, 0.06, branco), 0, -0.46, 0.28));
  return glifo(g);
}

/** 🚩 Marco do caminho: bandeirinha ondulada no mastro com bolinha branca. */
export function iconeNoMarco() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(traco([-0.54, -0.96], [-0.54, 0.78], 0.1, roxoEscuro()));
  g.add(em(bola(0.15, brancoPlastico(), 24), -0.54, 0.88, 0));
  const pano = new THREE.Shape();
  pano.moveTo(-0.46, 0.78);
  pano.bezierCurveTo(-0.1, 0.96, 0.2, 0.6, 0.86, 0.74);
  pano.lineTo(0.86, 0.02);
  pano.bezierCurveTo(0.2, -0.12, -0.1, 0.24, -0.46, 0.06);
  pano.closePath();
  g.add(em(extrudar(pano, 0.18, m, 0.08), 0, 0, 0));
  g.add(pinta(0.2, 0.42, 0.16, 0.1));
  return glifo(g);
}

function certinho(mat) {
  const g = new THREE.Group();
  g.add(traco([-0.66, 0.02], [-0.2, -0.46], 0.2, mat), traco([-0.2, -0.46], [0.7, 0.54], 0.2, mat));
  return g;
}

/** ✔ Certo (roxo, para fundo claro). */
export function iconeCerto() {
  return glifo(certinho(roxo()));
}

/** ✔ Certo branco: selinho da fase vencida e "usando"/"é seu" sobre fundo colorido. */
export function iconeCertoBranco() {
  return glifo(certinho(brancoVivo()));
}

/** 📍 Você está aqui (mapa dos mundos): alfinete de mapa com furo branco. */
export function iconeAlfinete() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  const r = 0.62, cy = 0.3;
  const a = Math.asin(0.56); // onde as retas encostam no círculo
  s.moveTo(0, -0.98);
  s.lineTo(-Math.cos(a) * r, cy - Math.sin(a) * r);
  s.absarc(0, cy, r, Math.PI + a, -a, true);
  s.lineTo(0, -0.98);
  const furo = new THREE.Path();
  furo.absarc(0, cy, 0.24, 0, Math.PI * 2, true);
  s.holes.push(furo);
  g.add(extrudar(s, 0.4, roxo(), 0.1));
  g.add(em(disco(0.25, 0.3, brancoPlastico()), 0, cy, -0.02));
  return glifo(g);
}

// ================================================================ casa: escada, baú e obra

/** Seta grossa (subir/descer) com contorno branco, para ficar legível sobre a cena da casa. */
function setaDaEscada(sobe) {
  const g = new THREE.Group();
  const d = sobe ? 1 : -1;
  const pts = [[0, 0.96], [0.88, 0.06], [0.38, 0.06], [0.38, -0.9], [-0.38, -0.9], [-0.38, 0.06], [-0.88, 0.06]].map(([x, y]) => [x, y * d]);
  const raios = [0.16, 0.1, 0.04, 0.12, 0.12, 0.04, 0.1];
  const f = () => (sobe ? poligono(pts, raios) : poligono(pts.slice().reverse(), raios.slice().reverse()));
  g.add(extrudar(f(), 0.36, roxo(), 0.09));
  // contorno: a mesma forma, fina e atrás, com o bisel largo (o bisel cresce a silhueta)
  g.add(em(extrudar(f(), 0.08, brancoVivo(), 0.17), 0, 0, -0.16));
  g.add(frente(traco([-0.14, sobe ? 0.6 : -0.62], [-0.46, sobe ? 0.28 : -0.3], 0.055, brancoPlastico()), 0.28));
  return glifo(g);
}

/** ⬆️ Subir a escada. */
export const iconeSubir = () => setaDaEscada(true);
/** ⬇️ Descer a escada. */
export const iconeDescer = () => setaDaEscada(false);

/** Baú de presentes: arca com tampa abaulada, cintas e fechadura brancas. */
export function iconeBau() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(caixa(1.86, 1.0, 0.86, 0.14, m), 0, -0.4, 0));
  const tampa = cilindro(0.52, 0.52, 1.86, lilas(), 48);
  tampa.rotation.z = Math.PI / 2;
  tampa.scale.set(1, 1, 0.84);
  const corte = new THREE.Group();
  corte.add(tampa);
  g.add(em(corte, 0, 0.12, 0));
  const branco = brancoPlastico();
  for (const x of [-0.56, 0.56]) {
    g.add(em(caixa(0.18, 1.04, 0.9, 0.05, branco), x, -0.4, 0));
    const cinta = malha(new THREE.TorusGeometry(0.53, 0.06, 12, 40, Math.PI), branco);
    cinta.rotation.y = Math.PI / 2;
    cinta.scale.set(1, 1, 0.84);
    g.add(em(cinta, x, 0.12, 0));
  }
  g.add(em(caixa(0.38, 0.44, 0.12, 0.08, branco), 0, -0.04, 0.45));
  g.add(em(disco(0.06, 0.06, roxoEscuro(), 16), 0, -0.02, 0.52));
  g.scale.setScalar(0.96);
  return glifo(g, { giroX: 0.2 });
}

/** 🔨 Construir (obra no baú): martelinho em diagonal. */
export function iconeMartelo() {
  const g = new THREE.Group();
  const m = roxo();
  const martelo = new THREE.Group();
  martelo.add(em(caixa(0.32, 1.5, 0.32, 0.14, m), 0, -0.3, 0));
  martelo.add(em(caixa(0.36, 0.14, 0.36, 0.05, brancoPlastico()), 0, -0.68, 0));
  martelo.add(em(caixa(1.28, 0.5, 0.52, 0.16, roxoEscuro()), 0.12, 0.62, 0));
  martelo.add(em(caixa(0.18, 0.56, 0.56, 0.07, m), 0.74, 0.62, 0));
  martelo.rotation.z = 0.62;
  g.add(em(martelo, 0.04, -0.04, 0));
  return glifo(g);
}

// ================================================================ botões soltos

/** 🔖 Marcador do livrinho: fitinha com o corte em V e estrela branca. */
export function iconeMarcador() {
  const g = new THREE.Group();
  const f = poligono([[-0.56, 0.96], [0.56, 0.96], [0.56, -0.96], [0, -0.48], [-0.56, -0.96]], [0.14, 0.14, 0.06, 0.08, 0.06]);
  g.add(extrudar(f, 0.24, roxo(), 0.09));
  g.add(em(estrela3D(0.28, 0.05, brancoPlastico(), 5, { segmentos: 4 }), 0, 0.3, 0.2));
  return glifo(g);
}

/** 🧭 Descobrir o nível: bússola com agulha roxa e branca. */
export function iconeBussola() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(malha(new THREE.TorusGeometry(0.78, 0.18, 24, 64), m));
  g.add(disco(0.74, 0.14, plastico('#ece6ff', { rugosidade: 0.3, verniz: 1, ambiente: 0.5 }), 48));
  g.add(em(bola(0.13, m, 16), 0, 0.98, 0));
  const agulha = new THREE.Group();
  agulha.add(extrudar(poligono([[0, 0.62], [0.2, 0], [-0.2, 0]], 0.04), 0.1, roxoEscuro(), 0.05));
  agulha.add(extrudar(poligono([[0, -0.62], [-0.2, 0], [0.2, 0]], 0.04), 0.1, brancoPlastico(), 0.05));
  agulha.rotation.z = -0.62;
  g.add(em(agulha, 0, 0, 0.14));
  g.add(em(disco(0.08, 0.3, m, 16), 0, 0, 0.1));
  return glifo(g);
}

/** 🧹 Limpar o desenho: vassoura com cabo e cerdas listradas. */
export function iconeVassoura() {
  const g = new THREE.Group();
  const m = roxo();
  const v = new THREE.Group();
  v.add(traco([0, 0.02], [0, 1.12], 0.1, roxoEscuro()));
  v.add(em(caixa(0.6, 0.18, 0.3, 0.07, brancoPlastico()), 0, -0.02, 0));
  v.add(extrudar(poligono([[-0.3, -0.08], [0.3, -0.08], [0.58, -0.96], [-0.58, -0.96]], [0.06, 0.06, 0.1, 0.1]), 0.26, m, 0.08));
  for (const x of [-0.22, 0, 0.22]) v.add(frente(traco([x * 0.9, -0.3], [x * 1.4, -0.82], 0.04, brancoPlastico()), 0.2));
  v.rotation.z = -0.5;
  g.add(em(v, 0.1, -0.02, 0));
  return glifo(g);
}

/** 💡 Dica: lâmpada com rosca branca e raiozinhos. */
export function iconeDica() {
  const g = new THREE.Group();
  const m = roxo();
  const vidro = bola(0.6, m, 48);
  vidro.scale.set(1, 1, 0.66);
  g.add(em(vidro, 0, 0.26, 0));
  g.add(em(cilindro(0.34, 0.28, 0.36, m, 32), 0, -0.36, 0));
  const branco = brancoPlastico();
  for (const y of [-0.58, -0.74]) g.add(em(cilindro(0.29, 0.29, 0.1, branco, 32), 0, y, 0));
  g.add(em(bola(0.14, m, 16), 0, -0.84, 0));
  g.add(frente(arcoDeTubo([0, 0.26], 0.38, Math.PI * 0.55, Math.PI * 0.9, 0.06, branco), 0.36));
  for (const a of [Math.PI * 0.2, Math.PI * 0.5, Math.PI * 0.8]) g.add(traco([Math.cos(a) * 0.78, 0.26 + Math.sin(a) * 0.78], [Math.cos(a) * 0.98, 0.26 + Math.sin(a) * 0.98], 0.07, m));
  g.scale.setScalar(0.94);
  return glifo(g);
}

/** 🌱 Novo perfil / quem vai jogar: broto de duas folhas num montinho de terra. */
export function iconeBroto() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(traco([0, -0.56], [0, 0.18], 0.1, m));
  for (const l of [-1, 1]) {
    const folha = extrudar(elipse(0.46, 0.24), 0.18, l > 0 ? m : lilas(), 0.07);
    folha.rotation.z = l * 0.5;
    g.add(em(folha, l * 0.42, 0.36 + (l > 0 ? 0.12 : 0), 0));
    g.add(frente(traco([l * 0.16, 0.28 + (l > 0 ? 0.12 : 0)], [l * 0.62, 0.5 + (l > 0 ? 0.12 : 0)], 0.035, brancoPlastico()), 0.14));
  }
  const terra = bola(0.66, roxoEscuro(), 40);
  terra.scale.set(1.2, 0.42, 0.62);
  g.add(em(terra, 0, -0.66, 0));
  return glifo(g);
}

// ================================================================ abas das áreas da casa (AreaDaCasa.icone)

/** 🛏️ Quarto novo: cama de lado com travesseiro branco e cobertor claro. */
export function iconeAreaQuarto() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(caixa(0.26, 1.36, 0.6, 0.12, m), -0.86, 0.0, 0));
  g.add(em(caixa(0.22, 0.8, 0.6, 0.1, m), 0.88, -0.28, 0));
  g.add(em(caixa(1.74, 0.42, 0.62, 0.14, m), 0, -0.4, 0));
  g.add(em(caixa(0.5, 0.3, 0.5, 0.14, brancoPlastico()), -0.48, -0.04, 0.02));
  g.add(em(caixa(1.06, 0.3, 0.66, 0.14, lilas()), 0.24, -0.1, 0.02));
  for (const x of [-0.82, 0.82]) g.add(em(caixa(0.18, 0.24, 0.5, 0.06, roxoEscuro()), x, -0.74, 0));
  return glifo(g);
}

/** 🐾 Área do pet: patinha (almofada grande e quatro dedinhos). */
export function iconeAreaPet() {
  const g = new THREE.Group();
  const m = roxo();
  const palma = bola(0.5, m, 48);
  palma.scale.set(1.18, 0.96, 0.6);
  g.add(em(palma, 0, -0.38, 0));
  for (const [x, y, r] of [[-0.66, 0.16, 0.25], [-0.24, 0.56, 0.27], [0.24, 0.56, 0.27], [0.66, 0.16, 0.25]]) {
    const d = bola(r, m, 32);
    d.scale.set(0.9, 1.1, 0.6);
    g.add(em(d, x, y, 0));
  }
  g.add(pinta(-0.2, -0.24, 0.28, 0.1));
  return glifo(g);
}

/** 🏊 Piscina: boia listrada (roxa e branca). */
export function iconeAreaPiscina() {
  const g = new THREE.Group();
  const R = 0.62, r = 0.3;
  for (let i = 0; i < 8; i++) {
    const seg = malha(new THREE.TorusGeometry(R, r, 24, 16, Math.PI / 4), i % 2 === 0 ? roxo() : brancoPlastico());
    seg.rotation.z = (i * Math.PI) / 4 + Math.PI / 8;
    g.add(seg);
  }
  return glifo(g, { giroX: 0.35 });
}

/** 🛝 Parquinho: escorregador com escada. */
export function iconeAreaParquinho() {
  const g = new THREE.Group();
  const m = roxo();
  for (const x of [-0.86, -0.46]) g.add(traco([x, -0.92], [x, 0.64], 0.08, roxoEscuro()));
  for (const y of [-0.5, -0.14, 0.22]) g.add(traco([-0.86, y], [-0.46, y], 0.06, brancoPlastico()));
  g.add(em(caixa(0.6, 0.16, 0.4, 0.06, m), -0.56, 0.62, 0));
  const rampa = new THREE.Shape();
  rampa.moveTo(-0.36, 0.7);
  rampa.bezierCurveTo(0.1, 0.62, 0.3, -0.62, 0.98, -0.72);
  rampa.lineTo(0.98, -0.98);
  rampa.bezierCurveTo(0.12, -0.92, -0.02, 0.36, -0.36, 0.42);
  rampa.closePath();
  g.add(extrudar(rampa, 0.42, m, 0.09));
  return glifo(g);
}

/** 🥕 Horta: cenoura com rama clara e risquinhos brancos. */
export function iconeAreaHorta() {
  const g = new THREE.Group();
  const corpo = poligono([[-0.42, 0.42], [0.42, 0.42], [0.04, -0.98]], [0.26, 0.26, 0.08]);
  const c = extrudar(corpo, 0.4, roxo(), 0.1);
  c.rotation.z = -0.32;
  g.add(em(c, 0, -0.06, 0));
  const rama = lilas();
  for (const [x, y] of [[-0.22, 0.98], [0.14, 1.0], [0.42, 0.82]]) g.add(traco([0.08, 0.42], [x, y], 0.11, rama));
  for (const [x0, y0] of [[-0.18, 0.04], [0.02, -0.3]]) g.add(frente(traco([x0, y0], [x0 + 0.22, y0 + 0.06], 0.04, brancoPlastico()), 0.3));
  return glifo(g);
}

/** 🧸 Sala de brinquedos: cabeça do ursinho com focinho branco. */
export function iconeAreaBrinquedos() {
  const g = new THREE.Group();
  const m = roxo();
  const cabeca = bola(0.72, m, 48);
  cabeca.scale.set(1, 0.94, 0.56);
  g.add(em(cabeca, 0, -0.1, 0));
  for (const l of [-1, 1]) {
    const orelha = bola(0.3, m, 32);
    orelha.scale.set(1, 1, 0.5);
    g.add(em(orelha, l * 0.6, 0.56, -0.04));
    g.add(pinta(l * 0.6, 0.56, 0.1, 0.15, lilas()));
    g.add(pinta(l * 0.28, 0.08, 0.36, 0.09));
  }
  const focinho = bola(0.3, brancoPlastico(), 32);
  focinho.scale.set(1.15, 0.85, 0.5);
  g.add(em(focinho, 0, -0.3, 0.32));
  g.add(pinta(0, -0.22, 0.46, 0.1, roxoEscuro()));
  return glifo(g);
}

/** 🌳 Casa na árvore: copa redonda, tronco e a casinha branca na copa. */
export function iconeAreaArvore() {
  const g = new THREE.Group();
  const m = roxo();
  for (const [x, y, r] of [[-0.42, 0.18, 0.5], [0.42, 0.18, 0.5], [0, 0.5, 0.56], [0, 0.04, 0.5]]) {
    const b = bola(r, m, 40);
    b.scale.set(1, 1, 0.56);
    g.add(em(b, x, y, 0));
  }
  g.add(em(caixa(0.34, 0.8, 0.3, 0.1, roxoEscuro()), 0, -0.6, -0.04));
  const casa = poligono([[-0.26, -0.24], [0.26, -0.24], [0.26, 0.04], [0.36, 0.04], [0, 0.32], [-0.36, 0.04], [-0.26, 0.04]], 0.04);
  g.add(em(extrudar(casa, 0.08, brancoPlastico(), 0.04), 0.04, 0.08, 0.32));
  g.add(em(caixa(0.14, 0.18, 0.06, 0.03, roxoEscuro()), 0.04, -0.07, 0.4));
  return glifo(g);
}

/** 🌷 Jardim de flores: tulipa de três pétalas, cabinho e folha. */
export function iconeAreaFlores() {
  const g = new THREE.Group();
  const m = roxo();
  const flor = poligono([[-0.56, 0.86], [-0.24, 0.6], [0, 0.94], [0.24, 0.6], [0.56, 0.86], [0.5, 0.2], [0, -0.06], [-0.5, 0.2]],
    [0.1, 0.06, 0.1, 0.06, 0.1, 0.3, 0.3, 0.3]);
  g.add(em(extrudar(flor, 0.38, m, 0.1), 0, 0, 0));
  g.add(traco([0, -0.02], [0, -0.96], 0.09, roxoEscuro()));
  const folha = extrudar(elipse(0.4, 0.16), 0.12, lilas(), 0.06);
  folha.rotation.z = 0.7;
  g.add(em(folha, 0.32, -0.56, 0));
  g.add(frente(traco([-0.28, 0.5], [-0.2, 0.24], 0.05, brancoPlastico()), 0.3));
  return glifo(g);
}

/** 🎨 Estúdio de música e arte: duas notas musicais ligadas. */
export function iconeAreaEstudio() {
  const g = new THREE.Group();
  const m = roxo();
  for (const [x, y] of [[-0.5, -0.62], [0.52, -0.42]]) {
    const cab = bola(0.3, m, 32);
    cab.scale.set(1.2, 0.9, 0.6);
    cab.rotation.z = 0.35;
    g.add(em(cab, x, y, 0));
    g.add(traco([x + 0.28, y + 0.06], [x + 0.28, y + 1.24], 0.08, m));
  }
  const viga = extrudar(poligono([[-0.18, 0.5], [0.88, 0.76], [0.88, 1.0], [-0.18, 0.74]], 0.06), 0.18, m, 0.06);
  g.add(em(viga, -0.04, -0.06, 0));
  g.add(pinta(-0.6, -0.52, 0.18, 0.07));
  g.add(pinta(0.42, -0.32, 0.18, 0.07));
  g.scale.setScalar(0.94);
  return glifo(g);
}

/** 🍢 Varanda gourmet: espetinho com pedaços roxos e brancos. */
export function iconeAreaGourmet() {
  const g = new THREE.Group();
  const e = new THREE.Group();
  e.add(traco([0, -1.08], [0, 1.0], 0.06, roxoEscuro()));
  e.add(em(bola(0.3, roxo(), 32), 0, 0.62, 0));
  e.add(em(caixa(0.5, 0.4, 0.44, 0.14, brancoPlastico()), 0, 0.14, 0));
  e.add(em(bola(0.3, lilas(), 32), 0, -0.34, 0));
  e.add(em(caixa(0.46, 0.34, 0.42, 0.13, roxo()), 0, -0.76, 0));
  e.rotation.z = -0.62;
  g.add(e);
  return glifo(g);
}

/** 📚 Biblioteca: livro aberto, páginas brancas com linhas roxas. */
export function iconeAreaBiblioteca() {
  const g = new THREE.Group();
  const m = roxo();
  const capa = poligono([[-1.04, -0.74], [0, -0.94], [1.04, -0.74], [1.04, 0.66], [0, 0.46], [-1.04, 0.66]], 0.1);
  g.add(em(extrudar(capa, 0.26, m, 0.09), 0, 0, -0.14));
  g.add(em(caixa(0.16, 1.4, 0.3, 0.07, roxoEscuro()), 0, -0.2, 0.0));
  for (const l of [-1, 1]) {
    const pagina = poligono(l < 0
      ? [[-0.9, -0.42], [-0.04, -0.58], [-0.04, 0.6], [-0.9, 0.76]]
      : [[0.04, -0.58], [0.9, -0.42], [0.9, 0.76], [0.04, 0.6]], 0.05);
    const p = extrudar(pagina, 0.06, brancoPlastico(), 0.04);
    p.rotation.y = l * -0.24;
    g.add(em(p, l * 0.02, 0, 0.06));
    for (const y of [0.34, 0.12, -0.1]) g.add(frente(traco([l * 0.2, y - 0.02], [l * 0.7, y + 0.06], 0.04, lilas()), 0.16 + 0.0));
  }
  return glifo(g);
}

/** 🔭 Observatório: luneta no tripé e uma estrelinha branca. */
export function iconeAreaObservatorio() {
  const g = new THREE.Group();
  const m = roxo();
  const tubo = new THREE.Group();
  tubo.add(em(cilindro(0.22, 0.3, 1.3, m, 32), 0, 0, 0));
  tubo.add(em(cilindro(0.36, 0.36, 0.2, roxoEscuro(), 32), 0, 0.68, 0));
  tubo.add(em(cilindro(0.15, 0.15, 0.34, m, 24), 0, -0.78, 0));
  tubo.add(em(cilindro(0.31, 0.31, 0.12, brancoPlastico(), 32), 0, 0.2, 0));
  tubo.rotation.z = -1.0;
  g.add(em(tubo, -0.06, 0.24, 0));
  for (const [x, z] of [[-0.6, 0], [0.5, 0], [-0.04, -0.3]]) g.add(em(traco([-0.04, 0.12], [x, -0.96], 0.07, roxoEscuro()), 0, 0, z));
  g.add(em(estrela3D(0.2, 0.06, brancoPlastico(), 5, { segmentos: 3 }), 0.72, 0.84, 0));
  return glifo(g);
}

/** 🐄 Fazendinha: celeiro de telhado quebrado com porta em X branca. */
export function iconeAreaFazendinha() {
  const g = new THREE.Group();
  const m = roxo();
  const f = poligono([[-0.86, -0.9], [0.86, -0.9], [0.86, 0.22], [0.56, 0.7], [0, 0.96], [-0.56, 0.7], [-0.86, 0.22]], [0.12, 0.12, 0.06, 0.08, 0.12, 0.08, 0.06]);
  g.add(extrudar(f, 0.34, m, 0.1));
  const branco = brancoPlastico();
  const z = 0.27;
  g.add(em(caixa(0.86, 0.86, 0.06, 0.04, branco), 0, -0.46, z - 0.03));
  g.add(em(caixa(0.7, 0.7, 0.08, 0.04, roxoEscuro()), 0, -0.46, z - 0.01));
  g.add(frente(traco([-0.3, -0.76], [0.3, -0.16], 0.06, branco), z + 0.04), frente(traco([-0.3, -0.16], [0.3, -0.76], 0.06, branco), z + 0.04));
  g.add(em(caixa(0.36, 0.26, 0.06, 0.04, branco), 0, 0.36, z - 0.02));
  return glifo(g);
}

/** ⚽ Quadra de esportes: bola roxa com gomos brancos. */
export function iconeAreaQuadra() {
  const g = new THREE.Group();
  const R = 0.9;
  g.add(bola(R, roxo(), 64));
  const branco = brancoPlastico();
  const gomo = (r) => {
    const pts = [];
    for (let i = 0; i < 5; i++) { const a = Math.PI / 2 + (i * 2 * Math.PI) / 5; pts.push([Math.cos(a) * r, Math.sin(a) * r]); }
    return extrudar(poligono(pts, r * 0.2), 0.04, branco, 0.03);
  };
  g.add(em(gomo(0.3), 0, 0, R - 0.02));
  for (let i = 0; i < 5; i++) {
    const a = Math.PI / 2 + Math.PI / 5 + (i * 2 * Math.PI) / 5;
    g.add(naSuperficieRasa(gomo(0.24), R - 0.02, Math.cos(a) * 0.7, Math.sin(a) * 0.7));
  }
  return glifo(g);
}

function naSuperficieRasa(obj, R, x, y) {
  const z = Math.sqrt(Math.max(0.0001, R * R - x * x - y * y));
  const n = new THREE.Vector3(x, y, z).normalize();
  obj.position.copy(n.clone().multiplyScalar(R));
  obj.lookAt(n.clone().multiplyScalar(R * 3));
  return obj;
}

/** 🐠 Sala do aquário: peixinho com rabo, listra e olho brancos. */
export function iconeAreaAquario() {
  const g = new THREE.Group();
  const m = roxo();
  const corpo = bola(0.62, m, 48);
  corpo.scale.set(1.15, 0.86, 0.5);
  g.add(em(corpo, -0.16, 0, 0));
  g.add(em(extrudar(poligono([[0.44, 0], [1.0, 0.5], [1.0, -0.5]], 0.12), 0.18, m, 0.07), 0, 0, 0));
  g.add(em(extrudar(poligono([[-0.4, 0.4], [0.16, 0.44], [-0.04, 0.78]], 0.08), 0.1, lilas(), 0.05), 0, 0, 0));
  g.add(em(caixa(0.14, 0.86, 0.1, 0.06, brancoPlastico()), 0.08, 0, 0.18));
  g.add(pinta(-0.48, 0.12, 0.28, 0.12));
  g.add(pinta(-0.46, 0.12, 0.33, 0.05, roxoEscuro()));
  return glifo(g);
}

/** 🎬 Cinema em casa: claquete com a tampa aberta e listras brancas. */
export function iconeAreaCinema() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(caixa(1.76, 1.14, 0.36, 0.12, m), 0, -0.36, 0));
  const branco = brancoPlastico();
  const barra = (y, giro) => {
    const b = new THREE.Group();
    b.add(em(caixa(1.76, 0.3, 0.3, 0.08, roxoEscuro()), 0, 0, 0));
    for (const x of [-0.56, -0.08, 0.4]) {
      const listra = extrudar(poligono([[x, -0.15], [x + 0.22, -0.15], [x + 0.36, 0.15], [x + 0.14, 0.15]], 0.02), 0.04, branco, 0.02);
      b.add(em(listra, 0, 0, 0.15));
    }
    b.rotation.z = giro;
    return em(b, 0, y, 0);
  };
  g.add(barra(0.4, 0));
  const tampa = barra(0, 0.34);
  tampa.position.set(0.04, 0.84, 0);
  const pivo = new THREE.Group();
  pivo.add(tampa);
  g.add(pivo);
  g.add(em(caixa(1.2, 0.08, 0.06, 0.03, branco), 0, -0.36, 0.2));
  g.add(em(caixa(0.8, 0.08, 0.06, 0.03, branco), -0.2, -0.62, 0.2));
  g.scale.setScalar(0.92);
  return glifo(g);
}

/** 🎣 Lago dos peixes: vara de pescar com a linha e o peixinho fisgado. */
export function iconeAreaLago() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(traco([-0.92, -0.92], [0.44, 0.92], 0.12, roxoEscuro()));
  g.add(em(disco(0.24, 0.26, m, 24), -0.54, -0.4, 0.08));
  g.add(em(disco(0.08, 0.3, brancoPlastico(), 16), -0.54, -0.4, 0.12));
  g.add(traco([0.44, 0.92], [0.5, 0.18], 0.055, roxoEscuro()));
  const peixe = new THREE.Group();
  const corpo = bola(0.42, m, 32);
  corpo.scale.set(0.78, 1.12, 0.5);
  peixe.add(corpo);
  peixe.add(em(extrudar(poligono([[0, -0.36], [0.34, -0.8], [-0.34, -0.8]], 0.08), 0.14, m, 0.05), 0, 0, 0));
  peixe.add(pinta(0.1, 0.16, 0.2, 0.08));
  peixe.rotation.z = 0.2;
  g.add(em(peixe, 0.54, -0.2, 0));
  return glifo(g);
}

/** 🔬 Laboratório: frasco de ciências com líquido claro e bolhas brancas. */
export function iconeAreaLaboratorio() {
  const g = new THREE.Group();
  const m = roxo();
  const frasco = poligono([[-0.22, 0.82], [0.22, 0.82], [0.22, 0.2], [0.8, -0.82], [-0.8, -0.82], [-0.22, 0.2]], [0.06, 0.06, 0.1, 0.18, 0.18, 0.1]);
  g.add(extrudar(frasco, 0.4, m, 0.1));
  g.add(em(caixa(0.66, 0.18, 0.5, 0.08, roxoEscuro()), 0, 0.88, 0));
  const liquido = poligono([[-0.42, -0.2], [0.42, -0.2], [0.68, -0.72], [-0.68, -0.72]], 0.08);
  g.add(em(extrudar(liquido, 0.06, lilas(), 0.04), 0, 0, 0.27));
  for (const [x, y, r] of [[-0.16, -0.42, 0.09], [0.22, -0.52, 0.07], [0.04, 0.1, 0.07], [-0.02, 0.4, 0.05]]) g.add(pinta(x, y, 0.32, r));
  return glifo(g);
}

/** 🦋 Estufa de borboletas: borboleta de quatro asas com pintas brancas. */
export function iconeAreaEstufa() {
  const g = new THREE.Group();
  const m = roxo();
  for (const l of [-1, 1]) {
    const cima = extrudar(elipse(0.5, 0.4), 0.16, m, 0.07);
    cima.rotation.z = l * 0.5;
    g.add(em(cima, l * 0.5, 0.3, 0));
    const baixo = extrudar(elipse(0.34, 0.28), 0.14, lilas(), 0.06);
    baixo.rotation.z = -l * 0.5;
    g.add(em(baixo, l * 0.4, -0.42, 0.02));
    g.add(pinta(l * 0.6, 0.38, 0.17, 0.12));
    g.add(traco([l * 0.04, 0.48], [l * 0.28, 0.9], 0.04, roxoEscuro()));
    g.add(em(bola(0.07, roxoEscuro(), 16), l * 0.29, 0.92, 0));
  }
  g.add(em(capsula(0.13, 0.84, roxoEscuro()), 0, -0.02, 0.1));
  return glifo(g);
}

/** 🎉 Salão de festas: chapeuzinho de festa listrado, pompom e confetes. */
export function iconeAreaFesta() {
  const g = new THREE.Group();
  const m = roxo();
  const cone = poligono([[0.04, 0.82], [0.66, -0.86], [-0.6, -0.86]], [0.06, 0.12, 0.12]);
  g.add(extrudar(cone, 0.4, m, 0.1));
  const branco = brancoPlastico();
  for (const [y, w] of [[0.24, 0.24], [-0.22, 0.42], [-0.62, 0.58]]) g.add(frente(traco([-w + 0.02, y - 0.1], [w + 0.04, y + 0.06], 0.06, branco), 0.27));
  g.add(em(bola(0.2, branco, 24), 0.04, 0.92, 0));
  for (const [x, y, c] of [[-0.78, 0.4, m], [0.74, 0.5, lilas()], [-0.72, -0.2, lilas()], [0.86, -0.1, m]]) g.add(em(caixa(0.16, 0.1, 0.08, 0.03, c), x, y, 0));
  return glifo(g);
}
