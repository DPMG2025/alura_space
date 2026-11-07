// Materiais: plástico com verniz, massinha fosca e pele acetinada (PRD, seção 2).
import * as THREE from 'three';

const cacheMat = new Map();

/**
 * Plástico brilhante. Amarelo e azul claros desbotam com o tom ACES: use cores mais saturadas e
 * menos [ambiente] (envMapIntensity), como no patinho.
 */
export function plastico(cor, { rugosidade = 0.38, verniz = 0.6, emissivo = null, opacidade = 1, ambiente = 0.55, rugosidadeVerniz = 0.25 } = {}) {
  const k = [cor, rugosidade, verniz, emissivo, opacidade, ambiente, rugosidadeVerniz].join('|');
  if (cacheMat.has(k)) return cacheMat.get(k);
  const m = new THREE.MeshPhysicalMaterial({
    color: cor, roughness: rugosidade, clearcoat: verniz, clearcoatRoughness: rugosidadeVerniz,
    envMapIntensity: ambiente,
  });
  if (emissivo) { m.emissive = new THREE.Color(emissivo); m.emissiveIntensity = 0.6; }
  if (opacidade < 1) { m.transparent = true; m.opacity = opacidade; m.depthWrite = false; }
  cacheMat.set(k, m);
  return m;
}
export const massinha = (cor) => plastico(cor, { rugosidade: 0.72, verniz: 0.05 });
export const pele = (cor) => plastico(cor, { rugosidade: 0.55, verniz: 0.15 });
/** Ouro de brinquedo (estrelas, moedas). */
export const ouro = (cor = '#ffc21a') => plastico(cor, { rugosidade: 0.18, verniz: 1 });
