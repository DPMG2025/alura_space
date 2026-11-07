import { THREE, plastico, massinha, pele, malha, caixa, bola, capsula, cilindro, em, naSuperficie, rosto, ancora } from '../kit.js';
import { estrela3D } from './estrela3D.js';

export function confete(cena, n, caixaArea, seed = 7) {
  let x = seed;
  const rnd = () => { x = (x * 16807) % 2147483647; return (x - 1) / 2147483646; };
  const cores = ['#ff4f8b', '#ffd23f', '#4d96ff', '#3dbe6b', '#9b6bff', '#ff8c1a'];
  for (let i = 0; i < n; i++) {
    const mat = plastico(cores[i % cores.length], { rugosidade: 0.3, verniz: 0.8 });
    const p = i % 3 === 0 ? estrela3D(0.12, 0.03, mat) : caixa(0.22, 0.1, 0.02, 0.01, mat);
    p.castShadow = false;
    p.position.set(caixaArea[0] + rnd() * (caixaArea[1] - caixaArea[0]), caixaArea[2] + rnd() * (caixaArea[3] - caixaArea[2]), caixaArea[4] + rnd() * (caixaArea[5] - caixaArea[4]));
    p.rotation.set(rnd() * 6, rnd() * 6, rnd() * 6);
    cena.add(p);
  }
}
