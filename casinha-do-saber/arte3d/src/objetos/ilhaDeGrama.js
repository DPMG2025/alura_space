import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';

export function ilhaDeGrama(raio = 7) {
  const g = new THREE.Group();
  const terra = cilindro(raio, raio * 0.92, 0.9, massinha('#b8834f'), 64); em(terra, 0, -0.45, 0); g.add(terra);
  const grama = cilindro(raio + 0.05, raio + 0.05, 0.28, plastico('#7ccf5b', { rugosidade: 0.7, verniz: 0.1 }), 64); em(grama, 0, 0.0, 0); g.add(grama);
  const borda = malha(new THREE.TorusGeometry(raio, 0.14, 16, 96), plastico('#7ccf5b', { rugosidade: 0.7, verniz: 0.1 })); borda.rotation.x = Math.PI / 2; em(borda, 0, 0.0, 0); g.add(borda);
  return g;
}
