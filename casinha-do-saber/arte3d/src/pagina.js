// Lado do navegador do pipeline: monta um objeto do catálogo, enquadra pela câmera do tipo,
// renderiza com fundo transparente e devolve o PNG e os pontos (apoio e âncoras) em pixels.
// Quem chama é o render.js, pelo Playwright: window.renderizar(id, escala, superamostra).
import { THREE, palco, luzes } from './kit.js';
import { tiposDeCamera, filtroFinal } from './cameras.js';
// ?catalogo=fase4 carrega src/catalogo_fase4.js (catálogos de fases paralelas, juntados depois ao principal)
const nomeDoCatalogo = new URLSearchParams(location.search).get('catalogo');
if (nomeDoCatalogo && !/^[a-z0-9_]+$/.test(nomeDoCatalogo)) throw new Error('catálogo inválido: ' + nomeDoCatalogo);
const { catalogo } = await import(nomeDoCatalogo ? `./catalogo_${nomeDoCatalogo}.js` : './catalogo.js');
import { carregarFonte } from './objetos/icones.js';
import { CENA } from './objetos/cenaDaCasa.js';

/** Fundo preto vira transparente e os reflexos viram branco translúcido (camadas de brilho do cabelo). */
function soOBrilho(ctx, L, A) {
  const img = ctx.getImageData(0, 0, L, A);
  const d = img.data;
  for (let i = 0; i < d.length; i += 4) {
    const lum = Math.max(d[i], d[i + 1], d[i + 2]);
    const a = Math.max(0, Math.min(1, (lum - 40) / 190)) * d[i + 3];
    d[i] = d[i + 1] = d[i + 2] = 255;
    d[i + 3] = Math.round(a * 0.85);
  }
  ctx.putImageData(img, 0, 0);
}

/** Lados da imagem em que algo encosta (alfa > 0 na primeira ou na última linha ou coluna). */
function bordasTocadas(ctx, L, A) {
  const d = ctx.getImageData(0, 0, L, A).data;
  const alfa = (x, y) => d[(y * L + x) * 4 + 3];
  const lados = [];
  const linha = (y) => { for (let x = 0; x < L; x++) if (alfa(x, y) > 8) return true; return false; };
  const coluna = (x) => { for (let y = 0; y < A; y++) if (alfa(x, y) > 8) return true; return false; };
  if (linha(0)) lados.push('cima');
  if (linha(A - 1)) lados.push('baixo');
  if (coluna(0)) lados.push('esquerda');
  if (coluna(L - 1)) lados.push('direita');
  return lados;
}

/** Mancha macia embaixo do objeto: a sombra de contato que "assenta" a peça em qualquer fundo do app. */
function sombraDeContato(caixa) {
  const tam = caixa.getSize(new THREE.Vector3());
  const c = caixa.getCenter(new THREE.Vector3());
  const cv = document.createElement('canvas');
  cv.width = cv.height = 256;
  const ctx = cv.getContext('2d');
  const g = ctx.createRadialGradient(128, 128, 0, 128, 128, 128);
  g.addColorStop(0, 'rgba(46,24,74,0.46)');
  g.addColorStop(0.5, 'rgba(46,24,74,0.22)');
  g.addColorStop(1, 'rgba(46,24,74,0)');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, 256, 256);
  const tex = new THREE.CanvasTexture(cv);
  tex.colorSpace = THREE.SRGBColorSpace;
  const rx = tam.x * 0.62, rz = Math.max(tam.z, tam.x * 0.5) * 0.62;
  const plano = new THREE.Mesh(
    new THREE.PlaneGeometry(rx * 2, rz * 2),
    new THREE.MeshBasicMaterial({ map: tex, transparent: true, depthWrite: false, toneMapped: false }),
  );
  plano.rotation.x = -Math.PI / 2;
  plano.position.set(c.x, caixa.min.y + 0.003, c.z);
  plano.renderOrder = -1;
  // pontos da elipse, para o enquadramento incluir a mancha (os cantos do plano são transparentes)
  const pontos = [];
  for (let k = 0; k < 48; k++) {
    const a = (k / 48) * Math.PI * 2;
    pontos.push(new THREE.Vector3(c.x + Math.cos(a) * rx, caixa.min.y, c.z + Math.sin(a) * rz));
  }
  return { plano, pontos };
}

/** Todos os vértices do objeto, em coordenadas do mundo. */
function verticesDoMundo(obj) {
  const pts = [];
  const v = new THREE.Vector3();
  obj.updateMatrixWorld(true);
  obj.traverse((o) => {
    if (!o.isMesh) return;
    const pos = o.geometry.attributes.position;
    for (let i = 0; i < pos.count; i++) pts.push(v.fromBufferAttribute(pos, i).applyMatrix4(o.matrixWorld).clone());
  });
  return pts;
}

function enquadrar(cam, cfg, caixa, pontos, largura, altura, pxPorDp) {
  if (cfg.enquadrar === 'cena') {
    // Câmera olhando na horizontal, deslocamento de lente: o plano z = 0 vira a cena do app, com
    // [CENA.larguraMundo] unidades na largura da imagem e o topo da imagem em y = CENA.topo.
    const k = largura / CENA.larguraMundo; // pixels por unidade no plano z = 0
    const cheia = 2 * CENA.D * Math.tan(THREE.MathUtils.degToRad(cfg.fov) / 2) * k;
    cam.fov = cfg.fov;
    cam.aspect = 1;
    cam.near = 1;
    cam.far = 120;
    cam.position.set(0, CENA.YC, CENA.D);
    cam.lookAt(0, CENA.YC, 0);
    cam.updateMatrixWorld();
    cam.setViewOffset(cheia, cheia, cheia / 2 - (CENA.larguraMundo / 2) * k, cheia / 2 - (CENA.topo - CENA.YC) * k, largura, altura);
    cam.updateProjectionMatrix();
    return;
  }
  if (cfg.enquadrar === 'fixo') {
    cam.position.set(...cfg.posicao);
    cam.lookAt(new THREE.Vector3(...cfg.alvo));
    cam.updateMatrixWorld();
    if (cfg.quadro) {
      // recorte do quadro inteiro: centrado na horizontal e alinhado embaixo (mesma escala para todos)
      const cheiaL = Math.round(cfg.quadro.largura * pxPorDp), cheiaA = Math.round(cfg.quadro.altura * pxPorDp);
      cam.aspect = cheiaL / cheiaA;
      // [recorte]: canto de cima à esquerda do recorte dentro do quadro, em dp (cenas do jogo)
      const x0 = cfg.recorte ? cfg.recorte.x * pxPorDp : (cheiaL - largura) / 2;
      const y0 = cfg.recorte ? cfg.recorte.y * pxPorDp : cheiaA - altura;
      cam.setViewOffset(cheiaL, cheiaA, x0, y0, largura, altura);
    }
    cam.updateProjectionMatrix();
    return;
  }
  const centro = caixa.getCenter(new THREE.Vector3());
  const raio = caixa.getSize(new THREE.Vector3()).length() / 2;
  const dir = new THREE.Vector3(...cfg.direcao).normalize();
  const dist = (raio / Math.sin(THREE.MathUtils.degToRad(cfg.fov) / 2)) * 1.1;
  cam.position.copy(centro).addScaledVector(dir, dist);
  cam.near = Math.max(0.01, dist - raio * 2);
  cam.far = dist + raio * 2;
  cam.lookAt(centro);
  cam.clearViewOffset();
  cam.updateMatrixWorld();
  cam.updateProjectionMatrix();
  let x0 = Infinity, x1 = -Infinity, y0 = Infinity, y1 = -Infinity;
  const p = new THREE.Vector3();
  for (const q of pontos) {
    p.copy(q).project(cam);
    x0 = Math.min(x0, p.x); x1 = Math.max(x1, p.x);
    y0 = Math.min(y0, p.y); y1 = Math.max(y1, p.y);
  }
  // Recorte que mantém a proporção da imagem: meia largura igual à meia altura, em NDC.
  const cx = (x0 + x1) / 2, cy = (y0 + y1) / 2;
  const e = Math.max((x1 - x0) / 2, (y1 - y0) / 2) * cfg.margem;
  const cheiaL = largura / e, cheiaA = altura / e;
  cam.setViewOffset(cheiaL, cheiaA, ((cx - e + 1) / 2) * cheiaL, ((1 - (cy + e)) / 2) * cheiaA, largura, altura);
  cam.updateProjectionMatrix();
}

function paraPixel(cam, ponto, largura, altura) {
  const p = ponto.clone().project(cam);
  return { x: ((p.x + 1) / 2) * largura, y: ((1 - p.y) / 2) * altura };
}

window.renderizar = (id, escala, superamostra) => {
  const item = catalogo.find((c) => c.id === id);
  if (!item) throw new Error('objeto fora do catálogo: ' + id);
  const cfg = tiposDeCamera[item.tipo];
  if (!cfg) throw new Error('tipo de câmera desconhecido: ' + item.tipo);
  // tamanho final (o que vai para o app) e tamanho do render (superamostrado, o Node reduz depois)
  const largura = Math.round(item.uso.largura * escala), altura = Math.round(item.uso.altura * escala);
  const L = largura * superamostra, A = altura * superamostra;

  const canvas = document.createElement('canvas');
  const p = palco(canvas, { largura: L, altura: A, fov: cfg.fov, pixelRatio: 1 });
  const obj = item.montar();
  p.cena.add(obj);
  obj.updateMatrixWorld(true);
  const caixa = new THREE.Box3().setFromObject(obj);
  const centro = caixa.getCenter(new THREE.Vector3());
  const raio = caixa.getSize(new THREE.Vector3()).length() / 2;

  const pontos = verticesDoMundo(obj);
  if (item.sombraDeContato ?? cfg.sombraDeContato) {
    // userData.base: a parte que encosta no chão, quando a caixa do objeto inteiro é maior que ela
    // (quadros de animação com moldura invisível, tampa voando)
    const s = sombraDeContato(obj.userData.base ? new THREE.Box3().setFromObject(obj.userData.base) : caixa);
    p.cena.add(s.plano);
    pontos.push(...s.pontos);
  }
  // câmera fixa: luz fixa também (o centro não pode depender do que está em cada camada)
  const centroLuz = cfg.centro ? new THREE.Vector3(...cfg.centro) : centro;
  const sol = cfg.luzes.sol.map((v, i) => v + centroLuz.getComponent(i));
  const area = cfg.centro ? cfg.luzes.area : Math.max(cfg.luzes.area, raio * 1.6);
  luzes(p.cena, { ...cfg.luzes, sol, alvo: centroLuz.toArray(), area });
  enquadrar(p.cam, cfg, caixa, pontos, L, A, escala * superamostra);
  p.desenhar();

  // ajuste de cor final do protótipo, num canvas 2D
  const saida = document.createElement('canvas');
  saida.width = L; saida.height = A;
  const ctx = saida.getContext('2d');
  ctx.filter = filtroFinal;
  ctx.drawImage(canvas, 0, 0);
  if (item.posProcesso === 'brilho') soOBrilho(ctx, L, A);
  // [cortaNaBorda]: cenários que de propósito passam da borda (a ilha do jogo é mais larga que a tela)
  const bordas = cfg.enquadrar === 'fixo' && !cfg.cortaNaBorda && !item.cortaNaBorda ? bordasTocadas(ctx, L, A) : [];
  const png = saida.toDataURL('image/png');

  // pontos no tamanho final
  const reduz = (q) => ({ x: Math.round(q.x / superamostra), y: Math.round(q.y / superamostra) });
  // câmera fixa: o apoio é a origem (os pés da figura), igual em todas as camadas
  // câmera da casa: o apoio é o meio da base da casa (o mesmo pixel em todas as camadas da cena)
  const pontoDeApoio = cfg.enquadrar === 'fixo' ? new THREE.Vector3(0, 0, 0)
    : cfg.enquadrar === 'cena' ? new THREE.Vector3(...CENA.apoio)
      : new THREE.Vector3(centro.x, caixa.min.y, centro.z);
  // Peças finas e giradas (glifos da barra) podem projetar o meio da base da caixa um pouco fora do
  // recorte; o apoio fica preso à imagem (o manifesto exige pontos dentro dela).
  const apoioBruto = reduz(paraPixel(p.cam, pontoDeApoio, L, A));
  const apoio = { x: Math.min(largura - 1, Math.max(0, apoioBruto.x)), y: Math.min(altura - 1, Math.max(0, apoioBruto.y)) };
  const ancoras = {};
  obj.traverse((o) => {
    if (!o.userData.ancora) return;
    // presa à imagem, como o apoio (o 'topo' das figuras pode projetar 1 px fora do recorte)
    const q = reduz(paraPixel(p.cam, o.getWorldPosition(new THREE.Vector3()), L, A));
    ancoras[o.userData.ancora] = { x: Math.min(largura - 1, Math.max(0, q.x)), y: Math.min(altura - 1, Math.max(0, q.y)) };
  });

  p.r.dispose();
  p.r.forceContextLoss();
  const extra = item.grupo === undefined ? {} : { grupo: item.grupo, tingir: item.tingir, ordem: item.ordem, personagem: item.personagem };
  if (item.esconde) extra.esconde = item.esconde;
  // [dados]: campos extras da entrada que vão como estão para o manifesto (figuras: emojis e lugar)
  if (item.dados) extra.dados = item.dados;
  return { png, largura, altura, tipo: item.tipo, uso: item.uso, apoio, ancoras, bordas, ...extra };
};

window.catalogo = () => catalogo.map((c) => c.id);
await document.fonts.ready;
await carregarFonte(); // números e letras em 3D dos ícones dos caminhos
window.PRONTO = true;
