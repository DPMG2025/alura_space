import { THREE, malha, ouro, ancora } from '../kit.js';

/** Estrela de cinco pontas, extrudada, com bisel macio. Peça usada dentro de outros modelos (moeda, roupa, confete). */
export function estrela3D(r, prof, mat, pontas = 5, { segmentos = 4 } = {}) {
  const s = new THREE.Shape();
  for (let i = 0; i < pontas * 2; i++) {
    const a = -Math.PI / 2 + i * Math.PI / pontas;
    const rr = i % 2 === 0 ? r : r * 0.48;
    const x = Math.cos(a) * rr, y = -Math.sin(a) * rr;
    if (i === 0) s.moveTo(x, y); else s.lineTo(x, y);
  }
  s.closePath();
  const geo = new THREE.ExtrudeGeometry(s, { depth: prof, bevelEnabled: true, bevelThickness: prof * 0.6, bevelSize: r * 0.12, bevelSegments: segmentos });
  geo.center();
  return malha(geo, mat);
}

/** A estrela dourada do prêmio (maquete 3_premio), sozinha, levemente inclinada para mostrar o volume. */
export function estrelaDourada() {
  const g = new THREE.Group();
  const e = estrela3D(1.0, 0.35, ouro(), 5, { segmentos: 8 });
  e.rotation.set(0.15, -0.22, 0);
  g.add(e);
  ancora(g, 'centro', 0, 0, 0);
  return g;
}
