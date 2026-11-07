// Catálogo da Fase 9B (arte, lote 2): figuras `fig_*` para os emojis das coisas das áreas novas da casa
// (CasaQueCresce), dos livrinhos, da história, do ateliê e da loja que ainda não tinham modelo 3D.
// Lista gerada por `python3 arte3d/emojis_fase9b.py` (pula os emojis que já têm figura, os que viram item_<tipo>
// da casa e os do vocabulário das perguntas, que são de outro lote). Ordem: as áreas que a criança alcança antes.
//
//   CATALOGO=fase9b npm run render [-- ids]   grava as imagens e app/src/main/assets/arte/manifesto_fase9b.json
//   node previa_fase9b.js                     grade das novas ao lado de figuras antigas, em saida/
//
// Mesmo formato das figuras da Fase 4 (src/figuras.js): 96 dp, `emojis` e `lugar` no manifesto, âncoras 'topo'
// e 'centro', apoio = o pé. Tipo 'objeto' (no chão, sombra de contato) ou 'item' (voa ou fica pendurado).
// Para o integrador: espalhar `catalogoDaFase9b` no src/catalogo.js e rodar o render completo.
import { THREE, ancora } from './kit.js';
import { modelosDaFase9b as M } from './objetos/figurasFase9b.js';
import { USO_DA_FIGURA } from './figuras.js';

/** [nome, emojis, lugar, voa] */
export const LISTA_DA_FASE9B = [
  // quarto novo
  ['lanterna_de_papel', ['🏮'], 'ceu', true],
  ['almofada_coracao', ['💖'], 'tapete'],
  ['sapatilha', ['🥿'], 'tapete'],
  // área do pet
  ['frisbee', ['🥏'], 'grama'],
  ['poodle', ['🐩'], 'grama'],
  ['novelo', ['🧶'], 'tapete'],
  // livrinhos, história, ateliê e loja
  ['margarida', ['🌼'], 'grama'],
  ['lotus', ['🪷'], 'lagoa'],
  ['carinha_rindo', ['😄'], 'ceu', true],
  ['bola_de_confete', ['🎊'], 'ceu', true],
  ['bolinha_marrom', ['🟤'], 'tapete'],
  ['pinheiro', ['🌲'], 'grama'],
  ['casinhas', ['🏘️'], 'grama'],
  ['ninho_vazio', ['🪹'], 'grama'],
  ['carinha_abraco', ['🤗'], 'ceu', true],
  ['carinha_feliz', ['😊'], 'ceu', true],
  ['marcador', ['🔖'], 'ceu', true],
  ['montanha_sol', ['🌄'], 'grama'],
  ['quadrado_marrom', ['🟫'], 'tapete'],
  ['tronco', ['🪵'], 'grama'],
  ['carinha_preocupada', ['😟'], 'ceu', true],
  ['esponja', ['🧽'], 'tapete'],
  ['colar_de_contas', ['📿'], 'varal', true],
  ['bolinha_branca', ['⚪'], 'tapete'],
  // piscina
  ['boia', ['🛟'], 'lagoa'],
  ['guarda_sol', ['⛱️'], 'grama'],
  ['mascara_de_mergulho', ['🤿'], 'lagoa'],
  // parquinho
  ['bolhas', ['🫧'], 'ceu', true],
  ['patinete', ['🛴'], 'grama'],
  ['skate', ['🛹'], 'grama'],
  ['escorregador', ['🛝'], 'grama'],
  // horta, sala de brinquedos, casa na árvore, jardim de flores
  ['pimentao', ['🫑'], 'mesa'],
  ['carro_de_corrida', ['🏎️'], 'tapete'],
  ['ninho', ['🪺'], 'grama'],
  ['hibisco', ['🌺'], 'grama'],
  ['fonte', ['⛲'], 'grama'],
  // estúdio de música e arte
  ['saxofone', ['🎷'], 'tapete'],
  ['banjo', ['🪕'], 'tapete'],
  ['atabaque', ['🪘'], 'tapete'],
  ['radio', ['📻'], 'tapete'],
  // varanda gourmet
  ['espetinho', ['🍢'], 'mesa'],
  ['panquecas', ['🥞'], 'mesa'],
  // biblioteca
  ['livro_verde', ['📗'], 'tapete'],
  ['livro_azul', ['📘'], 'tapete'],
  ['livro_laranja', ['📙'], 'tapete'],
  // observatório
  ['lua_cheia', ['🌕'], 'ceu', true],
  // fazendinha (os mesmos bichos das perguntas, para os emojis de corpo inteiro)
  ['porco_da_fazenda', ['🐖'], 'cercado'],
  ['pintinho_da_fazenda', ['🐤'], 'cercado'],
  ['cavalo_da_fazenda', ['🐎'], 'cercado'],
  // quadra de esportes
  ['bola_de_rugbi', ['🏈'], 'grama'],
  ['bola_de_beisebol', ['⚾'], 'grama'],
  ['pingue_pongue', ['🏓'], 'tapete'],
  ['peteca', ['🏸'], 'grama'],
  ['gol', ['🥅'], 'grama'],
  ['patins_de_rodinha', ['🛼'], 'tapete'],
  ['taco_de_hoquei', ['🏒'], 'grama'],
  ['cronometro', ['⏱️'], 'ceu', true],
  // sala do aquário
  ['coral', ['🪸'], 'lagoa'],
  ['lagosta', ['🦞'], 'lagoa'],
  // cinema
  ['camera_de_cinema', ['🎥'], 'tapete'],
  ['projetor', ['📽️'], 'tapete'],
  ['rolo_de_filme', ['🎞️'], 'ceu', true],
  // lago dos peixes
  ['castor', ['🦫'], 'lagoa'],
  ['lontra', ['🦦'], 'lagoa'],
  // laboratório
  ['microscopio', ['🔬'], 'mesa'],
  ['frasco', ['⚗️'], 'mesa'],
  ['placa_de_petri', ['🧫'], 'mesa'],
  ['dna', ['🧬'], 'ceu', true],
  ['termometro', ['🌡️'], 'mesa'],
  ['esquadro', ['📐'], 'mesa'],
  // salão de festas
  ['pinata', ['🪅'], 'ceu', true],
  ['globo_de_luz', ['🪩'], 'ceu', true],
  ['estrelinha_de_festa', ['🎇'], 'ceu', true],
  ['chapeu_de_festa', ['🥳'], 'tapete'],
];

/** Põe o modelo num grupo limpo e marca as âncoras 'topo' (meio de cima) e 'centro' (como src/figuras.js). */
function comAncoras(montar) {
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

export const catalogoDaFase9b = LISTA_DA_FASE9B.map(([nome, emojis, lugar, voa]) => {
  if (!M[nome]) throw new Error('figura sem modelo: ' + nome);
  return {
    id: 'fig_' + nome,
    tipo: voa ? 'item' : 'objeto',
    uso: { largura: USO_DA_FIGURA, altura: USO_DA_FIGURA },
    montar: comAncoras(M[nome]),
    dados: { emojis, lugar },
  };
});
export const catalogo = catalogoDaFase9b;
