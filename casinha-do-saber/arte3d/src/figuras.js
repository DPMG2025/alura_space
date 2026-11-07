// Figuras das perguntas (Fase 4): os emojis mais usados nas fases 1 a 100 dos quatro caminhos, contados no
// gerador de fases do app (GeradorDeFases, 12 sorteios por fase; ver docs/visual-3d/relatorios/fase4a.md).
//
// id: fig_<nome do vocabulário do app> (Vocabulario.kt: Objeto.singular ou Palavra.texto, em minúsculas e sem
// acento; as bolinhas de cor da Lógica levam a cor). No manifesto, cada figura leva também:
//   emojis  os emojis que ela substitui (o app troca o emoji pela imagem quando acha o emoji aqui);
//   lugar   a cena em que ela fica melhor (lagoa, cercado, mesa, varal, tapete, grama, ceu).
// Tipo 'objeto' (pisa no chão, com sombra de contato) ou 'item' (voa ou fica pendurado, sem sombra).
// Pendurados (roupas, no varal): a âncora 'topo' é o ponto que o app prende na vaga da corda.
import { THREE, ancora } from './kit.js';
import { modelosDasFiguras as M } from './objetos/figuras.js';

/** [nome, emojis, lugar, voa] */
export const LISTA_DE_FIGURAS = [
  // bichos
  ['gato', ['🐱', '🐈'], 'grama'],
  ['peixe', ['🐟'], 'lagoa'],
  ['sapo', ['🐸'], 'lagoa'],
  ['cachorro', ['🐶', '🐕'], 'grama'],
  ['pato', ['🦆'], 'lagoa'],
  ['cavalo', ['🐴'], 'cercado'],
  ['vaca', ['🐮', '🐄'], 'cercado'],
  ['porco', ['🐷'], 'cercado'],
  ['tartaruga', ['🐢'], 'lagoa'],
  ['borboleta', ['🦋'], 'ceu', true],
  ['leao', ['🦁'], 'grama'],
  ['pintinho', ['🐥'], 'cercado'],
  ['macaco', ['🐒'], 'grama'],
  ['elefante', ['🐘'], 'grama'],
  ['coelho', ['🐰', '🐇'], 'grama'],
  ['galo', ['🐓'], 'cercado'],
  ['passarinho', ['🐦'], 'grama'],
  ['ovelha', ['🐑'], 'cercado'],
  ['galinha', ['🐔'], 'cercado'],
  ['pinguim', ['🐧'], 'lagoa'],
  ['joaninha', ['🐞'], 'grama'],
  ['foca', ['🦭'], 'lagoa'],
  ['tigre', ['🐯'], 'grama'],
  ['abelha', ['🐝'], 'ceu', true],
  ['rato', ['🐭'], 'grama'],
  // comidas
  ['maca', ['🍎'], 'mesa'],
  ['banana', ['🍌'], 'mesa'],
  ['morango', ['🍓'], 'mesa'],
  ['uva', ['🍇'], 'mesa'],
  ['cenoura', ['🥕'], 'mesa'],
  ['bolo', ['🎂'], 'mesa'],
  ['cereja', ['🍒'], 'mesa'],
  ['pao', ['🍞'], 'mesa'],
  ['ovo', ['🥚'], 'mesa'],
  ['bolinho', ['🧁'], 'mesa'],
  // brinquedos
  ['bola', ['⚽'], 'tapete'],
  ['balao', ['🎈'], 'ceu', true],
  ['carrinho', ['🚗'], 'tapete'],
  ['ursinho', ['🧸'], 'tapete'],
  ['trem', ['🚂'], 'tapete'],
  ['aviao', ['✈️'], 'ceu', true],
  ['pipa', ['🪁'], 'ceu', true],
  ['tambor', ['🥁'], 'tapete'],
  // roupas
  ['luva', ['🧤'], 'varal', true],
  ['cachecol', ['🧣'], 'varal', true],
  ['camiseta', ['👕'], 'varal', true],
  ['bone', ['🧢'], 'varal', true],
  ['meia', ['🧦'], 'varal', true],
  // céu e natureza
  ['estrela', ['⭐', '🌟'], 'ceu', true],
  ['coracao', ['❤️'], 'ceu', true],
  ['lua', ['🌙'], 'ceu', true],
  ['flor', ['🌸'], 'grama'],
  // bolinhas de cor (sequências e padrões da Lógica)
  ['bolinha_vermelha', ['🔴'], 'tapete'],
  ['bolinha_azul', ['🔵'], 'tapete'],
  ['bolinha_verde', ['🟢'], 'tapete'],
  ['bolinha_amarela', ['🟡'], 'tapete'],
  ['bolinha_roxa', ['🟣'], 'tapete'],
  ['bolinha_laranja', ['🟠'], 'tapete'],
];

/** Tamanho de uso das figuras (dp): o maior em que aparecem nas cenas e nas respostas. */
export const USO_DA_FIGURA = 96;

/** Põe o modelo num grupo limpo e marca as âncoras 'topo' (meio de cima) e 'centro'. */
export function comAncoras(montar) {
  return () => {
    const w = new THREE.Group();
    w.add(montar());
    w.updateMatrixWorld(true);
    const c = new THREE.Box3().setFromObject(w);
    const m = c.getCenter(new THREE.Vector3());
    ancora(w, 'topo', m.x, c.max.y, m.z);
    ancora(w, 'centro', m.x, m.y, m.z);
    return w;
  };
}

export const figuras = LISTA_DE_FIGURAS.map(([nome, emojis, lugar, voa]) => {
  if (!M[nome]) throw new Error('figura sem modelo: ' + nome);
  return {
    id: 'fig_' + nome,
    tipo: voa ? 'item' : 'objeto',
    uso: { largura: USO_DA_FIGURA, altura: USO_DA_FIGURA },
    montar: comAncoras(M[nome]),
    dados: { emojis, lugar },
  };
});
