// Arte da casa de fora (PRD visual 3D, Fase 3): camadas da cena na câmera 'casa' e itens soltos na câmera
// 'casaItem'. Ids, ordem de empilhamento e uso estão em docs/visual-3d/relatorios/fase3a.md.
//
// Camadas da cena (tipo 'casa'): todas com o mesmo quadro (QUADRO_CENA, a cena do app inteira, alinhada no
// topo) e o mesmo apoio (meio da base da frente da casa). O chão e o caminho continuam EXTRA_DO_CHAO dp
// para baixo (o jardim de baixo das telas altas). [ordem]: desenhar da menor para a maior; [grupo] diz
// quando a camada aparece; [tingir] = nome do cômodo (Comodo.name em minúsculas) quando o app tinge com a
// cor do cômodo (multiplicação, como nos personagens).
import { camadasDaCena, QUADRO_CENA, EXTRA_DO_CHAO } from './objetos/cenaDaCasa.js';
import { ITENS, LUA, nuvemFofa } from './objetos/itensDaCasa.js';

/** Cores da loja (dominio/Loja.kt): coresDoTelhado e coresDaPorta, na mesma ordem (o índice é o valor guardado). */
export const CORES_DO_TELHADO = ['#ef476f', '#4d96ff', '#3dbe6b', '#9b6bff', '#ffa62b', '#8d5a3b'];
export const CORES_DA_PORTA = ['#a0522d', '#4d96ff', '#ef476f', '#3dbe6b', '#ffc93c'];

export const ORDEM_DA_CASA = {
  chao: 10,
  lago: 12,
  caminho: 13,
  cerca: 14,
  chamine: 18,
  comodo: 20,
  papel: 22,
  janelas: 24,
  porta: 26,
  paredes: 30,
  escada: 32,
  telhado: 40,
  janelaDoTelhado: 41,
  neveNoTelhado: 42,
};

const cena = { largura: QUADRO_CENA.largura, altura: QUADRO_CENA.altura };
const chao = { largura: QUADRO_CENA.largura, altura: QUADRO_CENA.altura + EXTRA_DO_CHAO };

function camada(id, grupo, montar, { uso = cena, tingir = null, ordem = ORDEM_DA_CASA[grupo] } = {}) {
  return { id, tipo: 'casa', uso, grupo, tingir, ordem, montar };
}

export const camadasDaCasa = [
  // chão: uma das duas, por baixo de tudo (antes e depois do terreno)
  camada('casa_etapa_0_vazio', 'chao', camadasDaCena.vazio, { uso: chao }),
  camada('casa_etapa_1_terreno', 'chao', camadasDaCena.terreno, { uso: chao }),
  camada('casa_etapa_2_paredes', 'paredes', camadasDaCena.paredes),
  ...CORES_DO_TELHADO.map((c, i) => camada(`casa_etapa_3_telhado_${i}`, 'telhado', camadasDaCena.telhado(c))),
  ...CORES_DA_PORTA.map((c, i) => camada(`casa_etapa_4_porta_${i}`, 'porta', camadasDaCena.porta(c))),
  // fundo dos cômodos (vêm com as paredes), tingidos com a cor de cada cômodo
  ...['quarto', 'banheiro', 'sala', 'cozinha'].map((n) => camada(`casa_comodo_${n}`, 'comodo', camadasDaCena.comodo(n), { tingir: n })),
  // papel de parede da loja (1 listras, 2 bolinhas, 3 estrelinhas; 0 é lisa, sem camada)
  ...[1, 2, 3].map((t) => camada(`casa_papel_${t}`, 'papel', camadasDaCena.papel(t))),
  // as outras estruturas (itens da ordem que a criança toca para construir)
  camada('casa_janelas', 'janelas', camadasDaCena.janelas(false)),
  camada('casa_janelas_noite', 'janelas', camadasDaCena.janelas(true)),
  camada('casa_escada', 'escada', camadasDaCena.escada),
  camada('casa_chamine', 'chamine', camadasDaCena.chamine),
  camada('casa_cerca', 'cerca', camadasDaCena.cerca),
  camada('casa_caminho', 'caminho', camadasDaCena.caminho, { uso: chao }),
  camada('casa_lago', 'lago', camadasDaCena.lago),
  // noite e clima
  camada('casa_janela_do_telhado_noite', 'janelaDoTelhado', camadasDaCena.janelaDoTelhadoNoite),
  camada('casa_neve_no_telhado', 'neveNoTelhado', camadasDaCena.neveNoTelhado),
  camada('ceu_estrelas_1', 'estrelas', camadasDaCena.estrelas(0), { ordem: 2 }),
  camada('ceu_estrelas_2', 'estrelas', camadasDaCena.estrelas(1), { ordem: 2 }),
  camada('ceu_chuva', 'clima', camadasDaCena.chuva, { ordem: 90 }),
  camada('ceu_neve', 'clima', camadasDaCena.neve, { ordem: 90 }),
];

/** Os 40 itens (item_<id do domínio>), a lua (o sol de noite) e as nuvens de enfeite do céu. */
export const itensDaCasa = [
  ...ITENS.map(([id, montar, l, a, sombra]) => ({ id: 'item_' + id, tipo: 'casaItem', uso: { largura: l, altura: a }, sombraDeContato: sombra, montar })),
  { id: 'item_sol_noite', tipo: 'casaItem', uso: { largura: 200, altura: 200 }, sombraDeContato: false, montar: LUA },
  { id: 'ceu_nuvem_1', tipo: 'item', uso: { largura: 160, altura: 100 }, montar: () => nuvemFofa(0) },
  { id: 'ceu_nuvem_2', tipo: 'item', uso: { largura: 120, altura: 80 }, montar: () => nuvemFofa(1) },
  { id: 'ceu_nuvem_3', tipo: 'item', uso: { largura: 140, altura: 90 }, montar: () => nuvemFofa(2) },
  { id: 'ceu_nuvem_chuva', tipo: 'item', uso: { largura: 160, altura: 100 }, montar: () => nuvemFofa(0, '#b8c4d8', '#5a6a88') },
];
