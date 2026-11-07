import { THREE, palco, luzes, plastico, caixa, bola, cilindro, em, estrela3D, malha } from './kit.js';
import { FontLoader } from 'three/addons/loaders/FontLoader.js';
import { TextGeometry } from 'three/addons/geometries/TextGeometry.js';

let fonte = null;
export async function carregarFonte() {
  fonte = await new FontLoader().loadAsync('./node_modules/three/examples/fonts/helvetiker_bold.typeface.json');
  return fonte;
}

export function texto3D(t, tam, mat, prof = 0.12) {
  const geo = new TextGeometry(t, { font: fonte, size: tam, height: prof, curveSegments: 10, bevelEnabled: true, bevelThickness: prof * 0.35, bevelSize: tam * 0.05, bevelSegments: 4 });
  geo.center();
  return malha(geo, mat);
}

function bloquinho(letra, cor) {
  const g = new THREE.Group();
  g.add(caixa(1, 1, 1, 0.16, plastico(cor, { rugosidade: 0.3, verniz: 0.9 })));
  const t = texto3D(letra, 0.55, plastico('#ffffff', { rugosidade: 0.25, verniz: 1 }), 0.08);
  em(t, 0, 0, 0.52); g.add(t);
  return g;
}

export const icones = {
  numeros() {
    const g = new THREE.Group();
    g.add(em(bloquinho('1', '#ff5c5c'), -0.55, 0.5, 0));
    g.add(em(bloquinho('2', '#4d96ff'), 0.55, 0.5, 0.05));
    g.add(em(bloquinho('3', '#ffc23d'), 0, 1.5, 0.02));
    g.children[0].rotation.y = 0.25; g.children[1].rotation.y = -0.2; g.children[2].rotation.y = 0.1;
    return g;
  },
  letras() {
    const g = new THREE.Group();
    g.add(em(bloquinho('A', '#3dbe6b'), -0.55, 0.5, 0));
    g.add(em(bloquinho('B', '#ff8c1a'), 0.55, 0.5, 0.05));
    g.add(em(bloquinho('C', '#9b6bff'), 0, 1.5, 0.02));
    g.children[0].rotation.y = 0.2; g.children[1].rotation.y = -0.25; g.children[2].rotation.y = -0.1;
    return g;
  },
  ingles() {
    const g = new THREE.Group();
    const globo = bola(0.95, plastico('#3aa0ff', { rugosidade: 0.2, verniz: 1 }), 64);
    em(globo, 0, 1.0, 0); g.add(globo);
    const verde = plastico('#4cc35a', { rugosidade: 0.4, verniz: 0.6 });
    for (const [x, y, sx, sy] of [[-0.35, 0.35, 0.35, 0.3], [0.3, -0.1, 0.3, 0.45], [-0.25, -0.4, 0.25, 0.2], [0.45, 0.5, 0.2, 0.15]]) {
      const c = bola(1, verde, 24); c.scale.set(sx, sy, 0.12);
      const z = Math.sqrt(Math.max(0.01, 0.95 * 0.95 - x * x - y * y));
      c.position.set(x, 1.0 + y, z - 0.04); c.lookAt(x * 3, 1 + y * 3, z * 3); g.add(c);
    }
    const base = cilindro(0.5, 0.65, 0.18, plastico('#ffd23f', { rugosidade: 0.2, verniz: 1 })); em(base, 0, -0.05, 0); g.add(base);
    const arco = malha(new THREE.TorusGeometry(1.12, 0.06, 12, 48, Math.PI * 1.2), plastico('#ffd23f', { rugosidade: 0.2, verniz: 1 }));
    arco.rotation.z = -Math.PI * 0.6; arco.rotation.y = 0.5; em(arco, 0, 1.0, 0); g.add(arco);
    return g;
  },
  logica() {
    // brinquedo de empilhar argolas
    const g = new THREE.Group();
    g.add(em(cilindro(0.7, 0.8, 0.2, plastico('#9b6bff', { rugosidade: 0.25, verniz: 1 })), 0, 0.1, 0));
    g.add(em(cilindro(0.08, 0.08, 1.8, plastico('#ffd23f', { rugosidade: 0.2, verniz: 1 }), 16), 0, 1.0, 0));
    const cores = ['#ff5c5c', '#ff8c1a', '#ffd23f', '#3dbe6b', '#4d96ff'];
    let y = 0.32;
    cores.forEach((c, i) => {
      const r = 0.62 - i * 0.1;
      const a = malha(new THREE.TorusGeometry(r, 0.15, 18, 48), plastico(c, { rugosidade: 0.25, verniz: 1 }));
      a.rotation.x = Math.PI / 2; em(a, 0, y, 0); g.add(a);
      y += 0.29;
    });
    g.add(em(bola(0.22, plastico('#ff4f8b', { rugosidade: 0.2, verniz: 1 }), 24), 0, y + 0.02, 0));
    return g;
  },
};

// Desenha cada ícone num renderizador só e devolve as imagens.
export function renderizarIcones(nomes, tam = 150) {
  const cv = document.createElement('canvas');
  const p = palco(cv, { largura: tam, altura: tam, fov: 30 });
  const urls = {};
  for (const n of nomes) {
    p.cena.clear();
    luzes(p.cena, { sol: [3, 6, 5], forca: 2.4, area: 3 });
    const obj = icones[n]();
    p.cena.add(obj);
    const caixaObj = new THREE.Box3().setFromObject(obj);
    const centro = caixaObj.getCenter(new THREE.Vector3());
    const raio = caixaObj.getSize(new THREE.Vector3()).length() / 2;
    p.cam.position.set(centro.x + raio * 0.9, centro.y + raio * 1.0, centro.z + raio * 3.1);
    p.cam.lookAt(centro);
    p.desenhar();
    urls[n] = cv.toDataURL('image/png');
  }
  return urls;
}
