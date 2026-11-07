// Ícones 3D dos quatro caminhos (PRD, seção 2): blocos 1 2 3 (Números), blocos A B C (Letras),
// globo (Inglês) e argolas de empilhar (Lógica). Como na maquete 1_inicio.
import { THREE, plastico, caixa, bola, cilindro, em, malha, ancora } from '../kit.js';
import { FontLoader } from 'three/addons/loaders/FontLoader.js';
import { TextGeometry } from 'three/addons/geometries/TextGeometry.js';

// Baloo 2 ExtraBold (a fonte dos títulos do app), convertida para o formato da three só com os
// caracteres usados (fonts/baloo2_800.typeface.json, gerada da woff2 com fontTools; OFL).
let fonte = null;
export async function carregarFonte() {
  fonte = await new FontLoader().loadAsync('/fonts/baloo2_800.typeface.json');
  return fonte;
}

/** Texto em relevo, centrado na origem. Na three 0.160 a espessura é [height] (não [depth]). */
export function texto3D(t, tam, mat, prof = 0.12) {
  if (!fonte) throw new Error('fonte 3D não carregada (carregarFonte)');
  const geo = new TextGeometry(t, { font: fonte, size: tam, height: prof, curveSegments: 12, bevelEnabled: true, bevelThickness: prof * 0.45, bevelSize: tam * 0.045, bevelSegments: 5 });
  geo.center();
  return malha(geo, mat);
}

const brilhante = (cor) => plastico(cor, { rugosidade: 0.22, verniz: 1, rugosidadeVerniz: 0.12 });

/** Bloco de brinquedo com a letra em relevo branco na frente e, menor, no lado direito. */
function bloquinho(letra, cor) {
  const g = new THREE.Group();
  g.add(caixa(1, 1, 1, 0.17, plastico(cor, { rugosidade: 0.28, verniz: 1, rugosidadeVerniz: 0.15, ambiente: 0.5 })));
  const branco = plastico('#ffffff', { rugosidade: 0.3, verniz: 1, ambiente: 0.5, emissivo: '#4a4a4a' });
  const t = texto3D(letra, 0.62, branco, 0.06);
  em(t, 0, -0.02, 0.5); g.add(t);
  return g;
}

/** Pilha de três blocos: dois embaixo e um em cima, cada um levemente girado. */
function pilha([a, b, c]) {
  const g = new THREE.Group();
  const b1 = em(bloquinho(...a), -0.54, 0.5, 0.06); b1.rotation.y = 0.22;
  const b2 = em(bloquinho(...b), 0.56, 0.5, -0.02); b2.rotation.y = -0.18;
  const b3 = em(bloquinho(...c), 0.02, 1.5, 0.04); b3.rotation.y = 0.12; b3.rotation.z = -0.05;
  g.add(b1, b2, b3);
  ancora(g, 'centro', 0, 1.0, 0);
  return g;
}

export const icones = {
  numeros: () => pilha([['1', '#ff5a5f'], ['2', '#3d8bff'], ['3', '#ffc21a']]),
  letras: () => pilha([['A', '#36c26a'], ['B', '#ff8a1c'], ['C', '#9a62ff']]),
  ingles() {
    const g = new THREE.Group();
    const R = 0.92;
    const terra = new THREE.Group();
    terra.add(bola(R, plastico('#2f8fff', { rugosidade: 0.18, verniz: 1, rugosidadeVerniz: 0.1, ambiente: 0.45 }), 64));
    // continentes: lentes de massinha verde coladas na superfície
    const verde = plastico('#3fc25a', { rugosidade: 0.35, verniz: 0.8, ambiente: 0.45 });
    const manchas = [[-0.38, 0.32, 0.36, 0.3], [0.22, -0.12, 0.3, 0.42], [-0.32, -0.42, 0.24, 0.18], [0.5, 0.48, 0.2, 0.15], [-0.6, -0.05, 0.14, 0.2], [0.15, 0.68, 0.24, 0.1]];
    for (const [x, y, sx, sy] of manchas) {
      const c = bola(1, verde, 32); c.scale.set(sx, sy, 0.1);
      const z = Math.sqrt(Math.max(0.01, R * R - x * x - y * y));
      c.position.set(x, y, z - 0.035); c.lookAt(x * 3, y * 3, z * 3); terra.add(c);
    }
    terra.rotation.y = 0.35;
    // globo, meridiano e eixo inclinados juntos, como num globo de mesa
    const inclinado = new THREE.Group();
    inclinado.add(terra);
    const dourado = brilhante('#ffc21a');
    const Ra = R + 0.12;
    const arco = malha(new THREE.TorusGeometry(Ra, 0.06, 14, 64, Math.PI), dourado);
    arco.rotation.set(0, -0.75, -Math.PI / 2); inclinado.add(arco);
    for (const s of [1, -1]) inclinado.add(em(bola(0.085, dourado, 20), 0, s * Ra, 0));
    inclinado.rotation.z = -0.36;
    em(inclinado, 0, 1.3, 0); g.add(inclinado);
    // haste da base até o polo de baixo
    const polo = new THREE.Vector3(0, -Ra, 0).applyAxisAngle(new THREE.Vector3(0, 0, 1), -0.36).add(new THREE.Vector3(0, 1.3, 0));
    const haste = cilindro(0.075, 0.1, Math.hypot(polo.x, polo.y - 0.05), dourado, 20);
    haste.position.set(polo.x / 2, (polo.y + 0.05) / 2, 0); haste.rotation.z = -Math.atan2(polo.x, polo.y - 0.05); g.add(haste);
    g.add(em(cilindro(0.48, 0.6, 0.16, dourado, 48), 0, 0.02, 0));
    ancora(g, 'centro', 0, 1.0, 0);
    return g;
  },
  logica() {
    // brinquedo de empilhar argolas: base roxa, pino, cinco argolas do arco-íris e a bolinha no topo
    const g = new THREE.Group();
    g.add(em(cilindro(0.74, 0.82, 0.22, brilhante('#9a62ff'), 48), 0, 0.11, 0));
    g.add(em(cilindro(0.09, 0.09, 1.7, brilhante('#ffd23f'), 20), 0, 1.0, 0));
    const cores = ['#ff4f6d', '#ff8a1c', '#ffc21a', '#36c26a', '#3d8bff'];
    let y = 0.36;
    cores.forEach((c, i) => {
      const r = 0.6 - i * 0.085, t = 0.16 - i * 0.012;
      const a = malha(new THREE.TorusGeometry(r, t, 24, 64), brilhante(c));
      a.rotation.x = Math.PI / 2; em(a, 0, y, 0); g.add(a);
      y += t * 2 - 0.005;
    });
    g.add(em(bola(0.21, brilhante('#ff4f9a'), 32), 0, y + 0.1, 0));
    ancora(g, 'centro', 0, 0.9, 0);
    return g;
  },
};
