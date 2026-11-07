// Fase 8: os ícones da barra que ainda eram emoji nos botões redondos (relatórios fase2b e fase5b), no mesmo
// estilo dos 8 da Fase 2 (interface.js): glifo roxo em relevo, levemente girado, com detalhes brancos, para ficar
// dentro do botão redondo claro. E a casinha branca para o botão verde "Pôr na casinha" (maquete 3_premio).
import { THREE, plastico, malha, bola, caixa, cilindro, capsula, em } from '../kit.js';
import { estrela3D } from './estrela3D.js';
import { roxo, brancoPlastico, glifo, traco, arcoDeTubo, poligono, extrudar } from './interface.js';

/** Põe [obj] mais para a frente (z), sem mexer em x e y. */
function frente(obj, z) {
  obj.position.z = z;
  return obj;
}

/** Ponta de seta (triângulo redondo extrudado) em [x, y], apontando para o ângulo [a]. */
function pontaDeSeta([x, y], a, tam, m) {
  const f = poligono([[tam, 0], [-tam * 0.62, tam * 0.78], [-tam * 0.62, -tam * 0.78]], tam * 0.22);
  const p = extrudar(f, 0.3, m, 0.09);
  p.rotation.z = a;
  return em(p, x, y, 0);
}

// ---------------------------------------------------------------- navegação

/** ➡️ ▶️ (próxima página, próxima fase): a seta do voltar, desenhada para a direita (luz certa, sem espelhar). */
export function iconeAvancar() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(traco([0.78, 0], [-0.82, 0], 0.19, m), traco([0.78, 0], [0.18, 0.62], 0.19, m), traco([0.78, 0], [0.18, -0.62], 0.19, m));
  return glifo(g);
}

/** 🗺️ Mapa dobrado em três painéis, com o caminho pontilhado branco e o X do tesouro. */
export function iconeMapa() {
  const g = new THREE.Group();
  const m = roxo();
  const claro = plastico('#7a62ff', { rugosidade: 0.24, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.5 });
  const xs = [-0.62, 0, 0.62];
  xs.forEach((x, i) => {
    const sobe = i % 2 === 0 ? 0.1 : -0.1;
    const f = poligono([[-0.31, -0.78 + sobe], [0.31, -0.78 - sobe], [0.31, 0.78 - sobe], [-0.31, 0.78 + sobe]], 0.08);
    const p = extrudar(f, 0.16, i === 1 ? claro : m, 0.06);
    p.rotation.y = i === 1 ? 0 : (i === 0 ? 0.28 : -0.28);
    g.add(em(p, x, 0, i === 1 ? -0.06 : 0.02));
  });
  const branco = brancoPlastico();
  for (const [x, y] of [[-0.74, -0.44], [-0.5, -0.2], [-0.24, -0.1], [0.04, 0.06], [0.28, 0.24]]) g.add(em(bola(0.075, branco, 16), x, y, 0.2));
  g.add(frente(traco([0.44, 0.32], [0.74, 0.62], 0.07, branco), 0.2), frente(traco([0.44, 0.62], [0.74, 0.32], 0.07, branco), 0.2));
  return glifo(g);
}

/** 📒 Álbum de figurinhas: livro de capa roxa com a estrela branca e as folhas brancas na lateral. */
export function iconeAlbum() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(caixa(1.42, 1.78, 0.42, 0.14, m), 0, 0, 0));
  g.add(em(caixa(1.22, 1.62, 0.3, 0.06, brancoPlastico()), 0.12, 0, 0)); // folhas
  g.add(em(caixa(0.26, 1.82, 0.5, 0.1, plastico('#4228c9', { rugosidade: 0.24, verniz: 1, ambiente: 0.5 })), -0.64, 0, 0)); // lombada
  const capa = em(caixa(1.3, 1.78, 0.08, 0.04, m), 0.06, 0, 0.21);
  g.add(capa);
  g.add(em(estrela3D(0.38, 0.06, brancoPlastico(), 5, { segmentos: 4 }), 0.1, 0.12, 0.3));
  for (const y of [-0.58, -0.7]) g.add(em(caixa(0.7, 0.06, 0.04, 0.02, brancoPlastico()), 0.1, y, 0.27));
  return glifo(g);
}

/** 📚 Estante: três livros em pé, de alturas diferentes, com faixas brancas. */
export function iconeEstante() {
  const g = new THREE.Group();
  const m = roxo();
  const branco = brancoPlastico();
  const livros = [[-0.6, 0.5, 1.56, 0], [-0.04, 0.5, 1.8, 0], [0.56, 0.48, 1.4, -0.22]];
  for (const [x, l, a, giro] of livros) {
    const b = new THREE.Group();
    b.add(caixa(l, a, 0.5, 0.1, m));
    for (const y of [a * 0.32, -a * 0.32]) b.add(em(caixa(l * 0.82, 0.08, 0.06, 0.03, branco), 0, y, 0.25));
    b.rotation.z = giro;
    g.add(em(b, x + (giro ? 0.08 : 0), -0.9 + a / 2 - (giro ? 0.05 : 0), 0));
  }
  return glifo(g);
}

// ---------------------------------------------------------------- casa

/** 🪞 Espelho de mão: moldura oval roxa, vidro azul claro com dois reflexos e o cabo. */
export function iconeEspelho() {
  const g = new THREE.Group();
  const m = roxo();
  const aro = malha(new THREE.TorusGeometry(0.62, 0.15, 24, 64), m);
  aro.scale.set(0.86, 1, 1);
  g.add(em(aro, 0, 0.3, 0));
  const vidro = bola(0.6, plastico('#cfe9ff', { rugosidade: 0.06, verniz: 1, rugosidadeVerniz: 0.04, ambiente: 0.9, emissivo: '#7fb5ff' }), 48);
  vidro.scale.set(0.8, 0.94, 0.12);
  g.add(em(vidro, 0, 0.3, 0));
  const branco = plastico('#ffffff', { rugosidade: 0.1, verniz: 1, emissivo: '#ffffff' });
  g.add(frente(traco([-0.26, 0.24], [0.02, 0.62], 0.06, branco), 0.08));
  g.add(frente(traco([-0.08, 0.06], [0.08, 0.28], 0.045, branco), 0.08));
  g.add(traco([0, -0.36], [0, -0.92], 0.16, m));
  g.add(em(bola(0.2, m, 24), 0, -0.98, 0));
  return glifo(g);
}

/** ☀️ Virar dia: sol redondo com oito raios curtos. */
export function iconeSol() {
  const g = new THREE.Group();
  const m = roxo();
  const disco = cilindro(0.48, 0.48, 0.32, m, 48);
  disco.rotation.x = Math.PI / 2;
  g.add(disco);
  const borda = malha(new THREE.TorusGeometry(0.48, 0.16, 16, 48), m);
  g.add(borda);
  for (let i = 0; i < 8; i++) {
    const a = (i / 8) * Math.PI * 2 + Math.PI / 8;
    g.add(traco([Math.cos(a) * 0.84, Math.sin(a) * 0.84], [Math.cos(a) * 1.0, Math.sin(a) * 1.0], 0.12, m));
  }
  return glifo(g);
}

/** Forma da lua crescente (a mesma de itensDaCasa.js > lua). */
function formaDaLua(r1 = 1, d = 0.5, r2 = 0.85) {
  const xi = (r1 * r1 - r2 * r2 + d * d) / (2 * d), yi = Math.sqrt(r1 * r1 - xi * xi);
  const t1 = Math.atan2(yi, xi), t2 = Math.atan2(-yi, xi - d);
  const s = new THREE.Shape();
  s.absarc(0, 0, r1, t1, Math.PI * 2 - t1, false);
  s.absarc(d, 0, r2, t2, -t2, true);
  return s;
}

/** 🌙 Virar noite: lua crescente roxa com uma estrelinha branca. */
export function iconeLua() {
  const g = new THREE.Group();
  const lua = extrudar(formaDaLua(0.92, 0.48, 0.76), 0.3, roxo(), 0.1);
  lua.rotation.z = -0.35;
  g.add(em(lua, -0.12, 0, 0));
  g.add(em(estrela3D(0.26, 0.08, brancoPlastico(), 5, { segmentos: 4 }), 0.5, 0.26, 0.05));
  g.add(em(estrela3D(0.15, 0.06, brancoPlastico(), 5, { segmentos: 4 }), 0.68, -0.32, 0.05));
  return glifo(g);
}

/** 🎨 Pintar as paredes: paleta roxa com o furo do dedão e quatro bolinhas de tinta coloridas. */
export function iconePintar() {
  const g = new THREE.Group();
  const s = new THREE.Shape();
  // feijão: elipse grande com uma mordida embaixo à direita
  s.absellipse(0, 0, 1.0, 0.8, 0, Math.PI * 2, false);
  const furo = new THREE.Path();
  furo.absarc(0.42, -0.3, 0.16, 0, Math.PI * 2, true);
  s.holes.push(furo);
  const p = extrudar(s, 0.22, roxo(), 0.08);
  g.add(p);
  const tinta = (cor) => plastico(cor, { rugosidade: 0.12, verniz: 1, rugosidadeVerniz: 0.06, ambiente: 0.5 });
  for (const [x, y, cor] of [[-0.55, 0.12, '#ff4a55'], [-0.22, 0.46, '#ffc21a'], [0.22, 0.44, '#2fc25b'], [0.56, 0.18, '#3a86ff'], [-0.38, -0.36, '#ffffff']]) {
    const b = bola(0.19, tinta(cor), 32);
    b.scale.set(1, 1, 0.55);
    g.add(em(b, x, y, 0.2));
  }
  return glifo(g);
}

// ---------------------------------------------------------------- som e gravação

/** ▶️ Ouvir / tocar: triângulo de cantos redondos. */
export function iconeTocar() {
  const g = new THREE.Group();
  g.add(em(extrudar(poligono([[0.86, 0], [-0.6, 0.86], [-0.6, -0.86]], 0.2), 0.32, roxo(), 0.1), 0.06, 0, 0));
  return glifo(g);
}

/** ⏹️ Parar: quadrado de cantos redondos. */
export function iconeParar() {
  const g = new THREE.Group();
  g.add(caixa(1.36, 1.36, 0.48, 0.2, roxo()));
  return glifo(g);
}

/** ⏺️ Gravar: microfone roxo de cabeça gradeada branca, no suporte em U. */
export function iconeGravar() {
  const g = new THREE.Group();
  const m = roxo();
  const cabeca = capsula(0.36, 0.56, m);
  g.add(em(cabeca, 0, 0.28, 0));
  const branco = brancoPlastico();
  for (const y of [0.16, 0.36, 0.56]) {
    const faixa = malha(new THREE.TorusGeometry(0.36, 0.035, 10, 40), branco);
    faixa.rotation.x = Math.PI / 2;
    g.add(em(faixa, 0, y, 0));
  }
  g.add(arcoDeTubo([0, 0.06], 0.6, Math.PI * 1.02, Math.PI * 1.98, 0.1, m));
  g.add(traco([0, -0.56], [0, -0.82], 0.1, m));
  g.add(traco([-0.36, -0.88], [0.36, -0.88], 0.11, m));
  return glifo(g);
}

/** 👂 Ouvir a música: fone de ouvido (arco e duas conchas, almofadas brancas). */
export function iconeOuvir() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(arcoDeTubo([0, -0.04], 0.78, 0.12, Math.PI - 0.12, 0.14, m));
  const branco = brancoPlastico();
  for (const l of [-1, 1]) {
    const concha = caixa(0.38, 0.72, 0.42, 0.17, m);
    g.add(em(concha, l * 0.74, -0.34, 0));
    const almofada = caixa(0.14, 0.6, 0.36, 0.07, branco);
    g.add(em(almofada, l * 0.52, -0.34, 0));
  }
  return glifo(g);
}

/** 🔁 Começar de novo: duas setas em círculo. */
export function iconeRepetir() {
  const g = new THREE.Group();
  const m = roxo();
  const R = 0.7;
  g.add(arcoDeTubo([0, 0], R, 0.35, Math.PI - 0.25, 0.17, m));
  g.add(arcoDeTubo([0, 0], R, Math.PI + 0.35, Math.PI * 2 - 0.25, 0.17, m));
  for (const a of [Math.PI - 0.25, Math.PI * 2 - 0.25]) {
    g.add(pontaDeSeta([Math.cos(a) * R, Math.sin(a) * R], a + Math.PI / 2 + 0.12, 0.34, m));
  }
  return glifo(g);
}

/** ↩️ Desfazer: gancho que volta para a esquerda. */
export function iconeDesfazer() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(arcoDeTubo([0.22, -0.02], 0.56, -Math.PI / 2, Math.PI / 2, 0.17, m));
  g.add(traco([0.22, 0.54], [-0.42, 0.54], 0.17, m));
  g.add(traco([0.22, -0.58], [-0.3, -0.58], 0.17, m));
  g.add(pontaDeSeta([-0.52, 0.54], Math.PI, 0.38, m));
  return glifo(g);
}

/** 📄 Papel novo: folha com a ponta dobrada, linhas brancas e um + branco. */
export function iconePapelNovo() {
  const g = new THREE.Group();
  const m = roxo();
  const f = poligono([[-0.7, -0.92], [0.7, -0.92], [0.7, 0.5], [0.28, 0.92], [-0.7, 0.92]], [0.12, 0.12, 0.02, 0.02, 0.12]);
  g.add(extrudar(f, 0.24, m, 0.08));
  const dobra = extrudar(poligono([[0.28, 0.5], [0.7, 0.5], [0.28, 0.92]], 0.05), 0.06, plastico('#8f7bff', { rugosidade: 0.24, verniz: 1, ambiente: 0.5 }), 0.04);
  g.add(em(dobra, 0, 0, 0.2));
  const branco = brancoPlastico();
  for (const [y, l] of [[0.4, 0.52], [0.14, 0.86], [-0.12, 0.86], [-0.38, 0.4]]) g.add(em(caixa(l, 0.1, 0.06, 0.04, branco), -0.42 + l / 2, y, 0.2));
  g.add(frente(traco([0.16, -0.6], [0.5, -0.6], 0.075, branco), 0.21), frente(traco([0.33, -0.43], [0.33, -0.77], 0.075, branco), 0.21));
  return glifo(g);
}

/** 🗑️ Apagar: lixeira com tampa, alça e listras brancas. */
export function iconeLixeira() {
  const g = new THREE.Group();
  const m = roxo();
  g.add(em(extrudar(poligono([[-0.62, 0.42], [0.62, 0.42], [0.5, -0.92], [-0.5, -0.92]], 0.14), 0.42, m, 0.1), 0, 0, 0));
  g.add(em(caixa(1.62, 0.24, 0.6, 0.1, m), 0, 0.6, 0));
  g.add(em(arcoDeTubo([0, 0.72], 0.2, 0, Math.PI, 0.08, m), 0, 0, 0));
  const branco = brancoPlastico();
  for (const x of [-0.26, 0, 0.26]) g.add(frente(traco([x * 1.02, 0.2], [x * 0.92, -0.68], 0.055, branco), 0.29));
  return glifo(g);
}

// ---------------------------------------------------------------- botão verde

/** Casinha branca (glifo) para o botão verde "Pôr na casinha" (maquete 3_premio): a porta é um vão que desce até o
 *  chão, então o verde do botão aparece por ela. */
export function iconeCasaBranca() {
  const branco = plastico('#ffffff', { rugosidade: 0.28, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.55 });
  const corpo = poligono(
    [[-0.66, -0.86], [-0.22, -0.86], [-0.22, -0.26], [0.22, -0.26], [0.22, -0.86], [0.66, -0.86], [0.66, 0.06], [0.98, 0.06], [0, 0.94], [-0.98, 0.06], [-0.66, 0.06]],
    [0.12, 0.05, 0.06, 0.06, 0.05, 0.12, 0.04, 0.12, 0.14, 0.12, 0.04]);
  const g = new THREE.Group();
  g.add(extrudar(corpo, 0.3, branco, 0.09));
  return glifo(g);
}
