// Catálogo da Fase 8 (demais telas), só a arte: ícones da barra que ainda eram emoji, a casinha branca do botão
// verde, a mão de toque, a Corujinha e os mascotes em poses, e os enfeites das telas (diplomas, álbum, livrinhos,
// dormir, piano, ateliê). Renderizar à parte:   CATALOGO=fase8 npm run render
// Grava as imagens em app/src/main/assets/arte/ e as entradas em manifesto_fase8.json; o integrador junta este
// catálogo ao catalogo.js (...catalogoFase8) e o manifesto ao manifesto.json. Detalhes em
// docs/visual-3d/relatorios/fase8a.md.
import * as b from './objetos/iconesDaBarra.js';
import * as m from './objetos/mascotes.js';
import * as e from './objetos/enfeites.js';

const quadrado = (lado) => ({ largura: lado, altura: lado });
// ícones da barra: mesmo tipo e uso dos 8 da Fase 2 (catalogo.js > icone)
const icone = (id, montar, uso = 48) => ({ id, tipo: 'item', uso: quadrado(uso), montar });

const iconesDaBarra = [
  icone('icone_avancar', b.iconeAvancar),
  icone('icone_mapa', b.iconeMapa),
  icone('icone_album', b.iconeAlbum),
  icone('icone_estante', b.iconeEstante),
  icone('icone_espelho', b.iconeEspelho),
  icone('icone_sol', b.iconeSol),
  icone('icone_lua', b.iconeLua),
  icone('icone_pintar', b.iconePintar),
  icone('icone_tocar', b.iconeTocar),
  icone('icone_parar', b.iconeParar),
  icone('icone_gravar', b.iconeGravar),
  icone('icone_ouvir', b.iconeOuvir),
  icone('icone_repetir', b.iconeRepetir),
  icone('icone_desfazer', b.iconeDesfazer),
  icone('icone_papel_novo', b.iconePapelNovo),
  icone('icone_lixeira', b.iconeLixeira),
  icone('icone_casa_branca', b.iconeCasaBranca),
];

const interfaceGeral = [
  // mão da dica "toque aqui" (MaoGuia): dedo para cima; a ponta do dedo é a âncora 'ponta'
  { id: 'ui_mao_toque', tipo: 'item', uso: quadrado(72), montar: e.maoDeToque(false) },
  { id: 'ui_mao_toque_apertando', tipo: 'item', uso: quadrado(72), montar: e.maoDeToque(true) },
];

// Poses: todas com a mesma moldura invisível (mesmo recorte e mesmo apoio): o app troca uma pela outra sem pular.
const mascotes = [
  ...m.POSES_DA_CORUJA.map((pose) => ({ id: `mascote_coruja_${pose}`, tipo: 'objeto', uso: { largura: 180, altura: 200 }, montar: m.corujinha(pose) })),
  ...m.ESPECIES.flatMap((especie) => m.POSES_DO_MASCOTE.map((pose) => (
    { id: `mascote_${especie}_${pose}`, tipo: 'objeto', uso: quadrado(200), montar: m.mascote(especie, pose) }))),
];

const enfeites = [
  // diplomas
  { id: 'tela_diploma_rolo', tipo: 'objeto', uso: quadrado(120), montar: e.diplomaEnrolado },
  { id: 'tela_medalha', tipo: 'item', uso: { largura: 80, altura: 110 }, montar: e.medalha },
  { id: 'tela_selo', tipo: 'item', uso: quadrado(80), montar: e.selo },
  // álbum: fundos das figurinhas (o emoji da figurinha vai por cima, no meio do cartão)
  ...Object.entries(e.CORES_DAS_TRILHAS).map(([trilha, cor]) => (
    { id: `tela_figurinha_${trilha}`, tipo: 'item', uso: { largura: 80, altura: 96 }, montar: e.figurinha(cor) })),
  { id: 'tela_figurinha_dourada', tipo: 'item', uso: { largura: 80, altura: 96 }, montar: e.figurinha('dourada') },
  { id: 'tela_figurinha_vazia', tipo: 'item', uso: { largura: 80, altura: 96 }, montar: e.figurinhaVazia },
  // livrinhos e história
  { id: 'tela_livro_fechado', tipo: 'objeto', uso: quadrado(120), montar: e.livroFechado },
  { id: 'tela_livro_aberto', tipo: 'objeto', uso: { largura: 160, altura: 120 }, montar: e.livroAberto },
  // dormir
  { id: 'tela_lua_dormindo', tipo: 'item', uso: quadrado(160), montar: e.luaDormindo },
  { id: 'tela_estrela_noite', tipo: 'item', uso: quadrado(40), montar: e.estrelaDaNoite },
  { id: 'tela_zzz', tipo: 'item', uso: quadrado(64), montar: e.zzzSolto },
  // piano
  { id: 'tela_piano', tipo: 'objeto', uso: { largura: 200, altura: 140 }, montar: e.pianinho },
  { id: 'tela_nota_musical', tipo: 'item', uso: quadrado(40), montar: e.notaMusical },
  // ateliê
  { id: 'tela_paleta', tipo: 'item', uso: quadrado(120), montar: e.paleta },
  { id: 'tela_pincel', tipo: 'item', uso: quadrado(96), montar: e.pincel },
  { id: 'tela_lapis', tipo: 'item', uso: quadrado(64), montar: e.lapis },
  { id: 'tela_balde', tipo: 'item', uso: quadrado(64), montar: e.balde },
  { id: 'tela_borracha', tipo: 'item', uso: quadrado(64), montar: e.borracha },
];

export const catalogoFase8 = [...iconesDaBarra, ...interfaceGeral, ...mascotes, ...enfeites];
export const catalogo = catalogoFase8;
