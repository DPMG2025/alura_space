// Palco: renderizador, ambiente e luzes. Evoluído de docs/visual-3d/prototipo/kit.js.
import * as THREE from 'three';
import { RoomEnvironment } from 'three/addons/environments/RoomEnvironment.js';

export { THREE };

/**
 * Renderizador com tom ACES, sombras macias e ambiente RoomEnvironment.
 * [fundo] nulo = fundo transparente (o caso do pipeline). [pixelRatio] 1 no pipeline: o tamanho do
 * canvas já é o tamanho final da imagem.
 */
export function palco(canvas, { largura, altura, fov = 32, fundo = null, exposicao = 1.0, pixelRatio = 2 } = {}) {
  const r = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: fundo === null, preserveDrawingBuffer: true, premultipliedAlpha: true });
  r.setPixelRatio(pixelRatio);
  r.setSize(largura, altura, false);
  r.setClearColor(0x000000, 0);
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

/** Sol quente do alto à direita, luz de céu e recorte frio por trás (PRD, seção 2). */
export function luzes(cena, { sol = [5, 9, 6], forca = 2.2, alvo = [0, 0, 0], area = 8, ceu = '#fff6e8', chao = '#7aa860', amb = 0.9, recorte = 0.6 } = {}) {
  const h = new THREE.HemisphereLight(ceu, chao, amb);
  cena.add(h);
  const d = new THREE.DirectionalLight('#fff3dc', forca);
  d.position.set(...sol);
  d.target.position.set(...alvo);
  d.castShadow = true;
  d.shadow.mapSize.set(2048, 2048);
  d.shadow.radius = 6;
  // Sem isso o cabelo fica com listras de sombra (lição do protótipo).
  d.shadow.bias = -0.0008;
  d.shadow.normalBias = 0.05;
  const c = d.shadow.camera;
  c.left = -area; c.right = area; c.top = area; c.bottom = -area; c.near = 0.5; c.far = 60;
  cena.add(d, d.target);
  const rim = new THREE.DirectionalLight('#cfe6ff', recorte);
  rim.position.set(-6, 5, -6);
  cena.add(rim);
  return d;
}
