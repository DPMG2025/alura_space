// Formas básicas e utilitários de posição.
import * as THREE from 'three';
import { RoundedBoxGeometry } from 'three/addons/geometries/RoundedBoxGeometry.js';

export function malha(geo, mat, { sombra = true, recebe = true } = {}) {
  const m = new THREE.Mesh(geo, mat);
  m.castShadow = sombra;
  m.receiveShadow = recebe;
  return m;
}
export const caixa = (l, a, p, raio, mat) => malha(new RoundedBoxGeometry(l, a, p, 5, Math.min(raio, l / 2, a / 2, p / 2) * 0.999), mat);
export const bola = (r, mat, seg = 48) => malha(new THREE.SphereGeometry(r, seg, Math.round(seg / 2)), mat);
export const capsula = (r, comp, mat) => malha(new THREE.CapsuleGeometry(r, comp, 10, 24), mat);
export const cilindro = (rt, rb, a, mat, seg = 40) => malha(new THREE.CylinderGeometry(rt, rb, a, seg), mat);
export function em(obj, x, y, z, s = 1) { obj.position.set(x, y, z); if (s !== 1) obj.scale.setScalar(s); return obj; }

/** Põe [obj] na superfície de uma esfera de raio [R] (centro na origem do pai), na direção (x, y), olhando para fora. */
export function naSuperficie(obj, R, x, y, afasta = 0) {
  const z = Math.sqrt(Math.max(0.0001, R * R - x * x - y * y));
  const n = new THREE.Vector3(x, y, z).normalize();
  obj.position.copy(n.clone().multiplyScalar(R + afasta));
  obj.lookAt(n.clone().multiplyScalar(R * 3));
  return obj;
}

/**
 * Marca uma âncora (ponto útil para o app: centro da cabeça para chapéus, mãos para acessórios).
 * O render projeta cada âncora na imagem e grava no manifesto, em pixels.
 */
export function ancora(pai, nome, x, y, z) {
  const o = new THREE.Object3D();
  o.name = 'ancora:' + nome;
  o.position.set(x, y, z);
  o.userData.ancora = nome;
  pai.add(o);
  return o;
}
