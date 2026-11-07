// Catálogo da Fase 5 (Prêmio e Quase), à parte do catalogo.js para não conflitar com as fases em
// paralelo. Renderizar:   CATALOGO=fase5 npm run render
// Grava as imagens em app/src/main/assets/arte/ e as entradas em manifesto_fase5.json; o integrador
// junta este catálogo ao catalogo.js (...catalogoFase5) e o manifesto ao manifesto.json.
//
// Reusados, sem duplicar: estrela3d, estrela_cheia, estrela_vazia, estrela_brilhando e moeda.
import * as p from './objetos/premio.js';

const quadrado = (lado) => ({ largura: lado, altura: lado });

const presente = [
  { id: 'premio_presente_fechado', tipo: 'objeto', uso: quadrado(180), montar: p.presenteFechado },
  // quadros de abrir: mesmo recorte e mesma luz, trocar um pelo outro sem a caixa pular
  ...[0, 1, 2, 3, 4].map((q) => ({ id: `premio_presente_abrindo_${q}`, tipo: 'objeto', uso: { largura: 220, altura: 240 }, montar: p.presenteAbrindo(q) })),
  { id: 'premio_caixa_aberta', tipo: 'objeto', uso: quadrado(180), montar: p.caixaAberta },
  { id: 'premio_tampa', tipo: 'item', uso: quadrado(120), montar: p.tampaSolta },
  { id: 'premio_clarao', tipo: 'item', uso: quadrado(220), montar: p.clarao },
];

const festa = [
  { id: 'premio_podio', tipo: 'objeto', uso: { largura: 340, altura: 120 }, montar: p.podio },
  ...[1, 2, 3, 5].map((n) => ({ id: `premio_moedas_${n}`, tipo: 'objeto', uso: quadrado(72), montar: p.pilhaDeMoedas(n) })),
  { id: 'premio_faisca_branca', tipo: 'item', uso: quadrado(32), montar: p.faiscaSolta('#fffbe6', '#fff2b0') },
  { id: 'premio_faisca_dourada', tipo: 'item', uso: quadrado(32), montar: p.faiscaSolta('#ffcf1f', '#ffb300') },
  ...Object.keys(p.FORMAS_DO_CONFETE).flatMap((forma) => Object.entries(p.CORES_DO_CONFETE).map(([cor, hex]) => (
    { id: `premio_confete_${forma}_${cor}`, tipo: 'item', uso: quadrado(24), montar: p.confeteSolto(forma, hex) }))),
  { id: 'premio_coroa', tipo: 'item', uso: quadrado(96), montar: p.coroa },
  { id: 'premio_trofeu', tipo: 'objeto', uso: quadrado(120), montar: p.trofeu },
];

const quase = [
  { id: 'quase_estrela_dormindo', tipo: 'item', uso: quadrado(120), montar: p.estrelaDormindo },
  ...[1, 2, 3, 4].map((e) => ({ id: `quase_semente_${e}`, tipo: 'objeto', uso: { largura: 100, altura: 140 }, montar: p.sementeQueCresce(e) })),
];

export const catalogoFase5 = [...presente, ...festa, ...quase];
export const catalogo = catalogoFase5;
