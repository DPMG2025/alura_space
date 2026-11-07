// Rosto dos personagens: olhos com dois brilhos, sobrancelhas, bochechas, boca com linguinha.
import * as THREE from 'three';
import { plastico } from './materiais.js';
import { malha, bola, caixa, naSuperficie } from './formas.js';

export function rosto(cabeca, R, { humor = 'feliz', menina = true, corSobrancelha = '#2a1a14' } = {}) {
  const preto = plastico('#1d1210', { rugosidade: 0.15, verniz: 1 });
  const branco = plastico('#ffffff', { rugosidade: 0.1, verniz: 1, emissivo: '#ffffff' });
  for (const lado of [-1, 1]) {
    const x = lado * R * 0.36, y = -R * 0.02;
    if (humor === 'torcendo') {
      // ^ ^ olhinhos fechados de alegria
      const arco = malha(new THREE.TorusGeometry(R * 0.11, R * 0.028, 10, 24, Math.PI), preto, { sombra: false });
      naSuperficie(arco, R, x, y - R * 0.02, -R * 0.005);
      cabeca.add(arco);
    } else {
      const olho = bola(1, preto, 32);
      olho.castShadow = false;
      olho.scale.set(R * 0.125, R * 0.165, R * 0.06);
      naSuperficie(olho, R, x, y, -R * 0.02);
      cabeca.add(olho);
      const b1 = bola(R * 0.05, branco, 16); b1.castShadow = false;
      naSuperficie(b1, R, x + R * 0.04, y + R * 0.07, R * 0.035);
      const b2 = bola(R * 0.024, branco, 12); b2.castShadow = false;
      naSuperficie(b2, R, x - R * 0.045, y - R * 0.07, R * 0.03);
      cabeca.add(b1, b2);
      if (menina) {
        const cilio = malha(new THREE.CapsuleGeometry(R * 0.017, R * 0.07, 4, 8), preto, { sombra: false });
        naSuperficie(cilio, R, x + lado * R * 0.13, y + R * 0.13, R * 0.0);
        cilio.rotateZ(lado * 0.9);
        cabeca.add(cilio);
      }
    }
    // sobrancelha
    const sob = malha(new THREE.TorusGeometry(R * 0.1, R * 0.022, 8, 20, Math.PI * 0.7), plastico(corSobrancelha, { rugosidade: 0.6, verniz: 0 }), { sombra: false });
    naSuperficie(sob, R, x, y + R * (humor === 'torcendo' ? 0.3 : 0.26), -R * 0.005);
    sob.rotateZ(Math.PI * 0.15);
    cabeca.add(sob);
    // bochecha
    const boc = bola(1, plastico('#ff7a8a', { rugosidade: 0.8, verniz: 0, opacidade: 0.5 }), 24);
    boc.castShadow = false;
    boc.scale.set(R * 0.16, R * 0.1, R * 0.04);
    naSuperficie(boc, R, lado * R * 0.58, -R * 0.24, -R * 0.015);
    cabeca.add(boc);
  }
  // nariz
  const nariz = bola(R * 0.06, cabeca.userData.matPele, 16);
  nariz.scale.set(1.2, 0.8, 0.8);
  naSuperficie(nariz, R, 0, -R * 0.14, -R * 0.02);
  cabeca.add(nariz);
  // boca: meia lua escura com linguinha
  const largura = humor === 'torcendo' ? 0.24 : 0.18, fundo = humor === 'torcendo' ? 0.26 : 0.17;
  const s = new THREE.Shape();
  s.moveTo(-largura * R, 0);
  s.quadraticCurveTo(0, 0.02 * R, largura * R, 0);
  s.bezierCurveTo(largura * 0.9 * R, -fundo * R, -largura * 0.9 * R, -fundo * R, -largura * R, 0);
  const geoBoca = new THREE.ExtrudeGeometry(s, { depth: R * 0.03, bevelEnabled: true, bevelThickness: R * 0.01, bevelSize: R * 0.012, bevelSegments: 3, curveSegments: 24 });
  const boca = malha(geoBoca, plastico('#7d1f24', { rugosidade: 0.3, verniz: 0.6 }), { sombra: false });
  naSuperficie(boca, R, 0, -R * 0.3, -R * 0.03);
  cabeca.add(boca);
  const lingua = bola(1, plastico('#ff7f8c', { rugosidade: 0.4, verniz: 0.4 }), 20);
  lingua.scale.set(largura * R * 0.6, fundo * R * 0.35, R * 0.03);
  naSuperficie(lingua, R, 0, -R * 0.3 - fundo * R * 0.62, -R * 0.0);
  lingua.castShadow = false;
  cabeca.add(lingua);
  const dentes = caixa(largura * R * 1.3, R * 0.04, R * 0.02, R * 0.01, plastico('#ffffff', { rugosidade: 0.2 }));
  dentes.castShadow = false;
  naSuperficie(dentes, R, 0, -R * 0.315, R * 0.005);
  cabeca.add(dentes);
}

