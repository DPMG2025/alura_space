// Lado do navegador do pipeline: monta um objeto do catálogo, enquadra pela câmera do tipo,
// renderiza com fundo transparente e devolve o PNG e os pontos (apoio e âncoras) em pixels.
// Quem chama é o render.js, pelo Playwright: window.renderizar(id, escala, superamostra).
import { THREE, palco, luzes } from './kit.js';
import { tiposDeCamera, filtroFinal } from './cameras.js';
import { catalogo } from './catalogo.js';

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

function enquadrar(cam, cfg, caixa, pontos, largura, altura) {
  if (cfg.enquadrar === 'fixo') {
    cam.position.set(...cfg.posicao);
    cam.lookAt(new THREE.Vector3(...cfg.alvo));
    cam.updateMatrixWorld();
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
  if (cfg.sombraDeContato) {
    const s = sombraDeContato(caixa);
    p.cena.add(s.plano);
    pontos.push(...s.pontos);
  }
  const sol = cfg.luzes.sol.map((v, i) => v + centro.getComponent(i));
  luzes(p.cena, { ...cfg.luzes, sol, alvo: centro.toArray(), area: Math.max(cfg.luzes.area, raio * 1.6) });
  enquadrar(p.cam, cfg, caixa, pontos, L, A);
  p.desenhar();

  // ajuste de cor final do protótipo, num canvas 2D
  const saida = document.createElement('canvas');
  saida.width = L; saida.height = A;
  const ctx = saida.getContext('2d');
  ctx.filter = filtroFinal;
  ctx.drawImage(canvas, 0, 0);
  const png = saida.toDataURL('image/png');

  // pontos no tamanho final
  const reduz = (q) => ({ x: Math.round(q.x / superamostra), y: Math.round(q.y / superamostra) });
  const apoio = reduz(paraPixel(p.cam, new THREE.Vector3(centro.x, caixa.min.y, centro.z), L, A));
  const ancoras = {};
  obj.traverse((o) => {
    if (o.userData.ancora) ancoras[o.userData.ancora] = reduz(paraPixel(p.cam, o.getWorldPosition(new THREE.Vector3()), L, A));
  });

  p.r.dispose();
  p.r.forceContextLoss();
  return { png, largura, altura, tipo: item.tipo, uso: item.uso, apoio, ancoras };
};

window.catalogo = () => catalogo.map((c) => c.id);
await document.fonts.ready;
window.PRONTO = true;
