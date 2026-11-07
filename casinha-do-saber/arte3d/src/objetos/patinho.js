import { THREE, plastico, malha, bola, em, ancora } from '../kit.js';

/**
 * Patinho de borracha, de perfil com o bico para +x. Cor saturada e pouco ambiente: amarelo claro
 * desbota com o tom ACES (lição do protótipo).
 */
export function patinho(s = 1, cor = '#ffd23f') {
  const g = new THREE.Group();
  const m = new THREE.MeshPhysicalMaterial({ color: cor, roughness: 0.25, clearcoat: 1, clearcoatRoughness: 0.15, envMapIntensity: 0.3 });
  const corpo = bola(0.5, m, 40); corpo.scale.set(1.15, 0.85, 0.95); em(corpo, 0, 0.42, 0); g.add(corpo);
  const rabo = malha(new THREE.ConeGeometry(0.18, 0.35, 20), m); rabo.rotation.z = 1.1; em(rabo, -0.58, 0.62, 0); g.add(rabo);
  const cabeca = bola(0.34, m, 40); em(cabeca, 0.32, 0.95, 0); g.add(cabeca);
  const bico = bola(0.16, plastico('#ff8c1a', { rugosidade: 0.2, verniz: 1 }), 20); bico.scale.set(1.3, 0.55, 0.9); em(bico, 0.66, 0.9, 0); g.add(bico);
  for (const lado of [-1, 1]) {
    // olhos na superfície da cabeça (no protótipo ficavam por dentro e sumiam)
    const olho = bola(0.06, plastico('#1d1210', { rugosidade: 0.1, verniz: 1 }), 20); olho.scale.set(0.8, 1, 0.8); em(olho, 0.532, 1.044, lado * 0.236); g.add(olho);
    const brilho = bola(0.022, plastico('#ffffff', { emissivo: '#ffffff' }), 12); em(brilho, 0.566, 1.08, lado * 0.27); g.add(brilho);
    const asa = bola(0.24, m, 20); asa.scale.set(1.2, 0.6, 0.3); em(asa, -0.05, 0.48, lado * 0.45); asa.rotation.z = 0.3; g.add(asa);
  }
  // âncoras: centro da cabeça (chapéus) e topo da cabeça
  ancora(g, 'cabeca', 0.32, 0.95, 0);
  ancora(g, 'topoDaCabeca', 0.32, 1.29, 0);
  g.scale.setScalar(s);
  return g;
}

/** O patinho da lagoa do jogo (maquete 2_jogo), virado três quartos para a câmera. */
export function patinhoDaLagoa() {
  const g = new THREE.Group();
  const p = patinho(1, '#ffc400');
  p.rotation.y = -0.6;
  g.add(p);
  return g;
}
