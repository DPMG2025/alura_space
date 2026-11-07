// Camadas da criança e do irmãozinho (PRD visual 3D, seção 3.2). Todas saem na câmera 'personagem'
// (cameras.js): a criança ocupa o quadro inteiro; o irmão usa um recorte menor do mesmo quadro (centrado
// e alinhado embaixo), na mesma escala, então os dois ficam do tamanho certo um ao lado do outro.
//
// Cada entrada: id, grupo, tingir ('pele' | 'cabelo' | 'roupa' | null), ordem (de trás para a frente:
// o app desenha a menor primeiro) e montar. A ordem e os grupos estão descritos em
// docs/visual-3d/relatorios/fase1a.md.
import { camada, sombraNoChao } from './objetos/personagens.js';

/** Quadro da criança, em dp (o mesmo de cameras.js > personagem.quadro). */
export const QUADRO_CRIANCA = { largura: 170, altura: 260 };
/** Recorte do irmãozinho dentro do mesmo quadro (centrado e alinhado embaixo), em dp. */
export const QUADRO_IRMAO = { largura: 130, altura: 196 };

export const ORDEM = {
  sombra: 10,
  cabeloTras: 20,
  enfeiteCabelo: 25,
  corpo: 30,
  pes: 35,
  roupa: 40,
  estampa: 45,
  cabeca: 50,
  boca: 60,
  olhos: 62,
  sobrancelhas: 64,
  cabeloFrente: 70,
  cabeloBrilho: 72,
  laco: 74,
  bracos: 80,
};

const HUMORES = ['feliz', 'torcendo', 'pensando', 'surpreso'];
const OLHOS = ['aberto', 'alegre', 'fechado', 'pensando', 'surpreso'];
const POSES = [['parado', 'parado', 1], ['tchau1', 'tchau', 1], ['tchau2', 'tchau', 2], ['tchau3', 'tchau', 3], ['tchau4', 'tchau', 4], ['torcendo', 'torcendo', 1], ['pensando', 'pensando', 1]];
/** Estilos de cabelo da criança, na ordem de Perfil.estiloCabelo (0, 1, 2). */
export const ESTILOS = { menina: ['liso', 'cacheado', 'trancas'], menino: ['liso', 'cacheado', 'espetado'] };

function entradas(quem) {
  const pre = quem === 'crianca' ? 'crianca' : 'irmao';
  const uso = quem === 'crianca' ? QUADRO_CRIANCA : QUADRO_IRMAO;
  const base = { quem };
  const L = [];
  const add = (id, grupo, tingir, montar) => L.push({ id: `${pre}_${id}`, tipo: 'personagem', uso, grupo, tingir, ordem: ORDEM[grupo], personagem: quem, montar });

  add('sombra', 'sombra', null, () => (quem === 'crianca' ? sombraNoChao(0.78, 0.5) : sombraNoChao(0.6, 0.4)));
  add('corpo', 'corpo', 'pele', () => camada(base, { ver: ['corpo'], sombra: ['roupa'] }));
  for (const g of ['menina', 'menino']) {
    const o = { ...base, menina: g === 'menina' };
    add(`pes_${g}`, 'pes', null, () => camada(o, { ver: ['pes'], sombra: ['roupa'] }));
    if (quem === 'crianca') {
      add(`roupa_${g}`, 'roupa', 'roupa', () => camada(o, { ver: ['roupa'], sombra: ['cabeca'] }));
      add(`roupa_${g}_estampa`, 'estampa', null, () => camada(o, { ver: ['estampa'], recorte: ['roupa', 'pes'], sombra: ['cabeca'] }));
    } else {
      add(`roupa_${g}`, 'roupa', null, () => camada(o, { ver: ['roupa'], sombra: ['cabeca'] }));
    }
  }
  add('cabeca', 'cabeca', 'pele', () => camada(base, { ver: ['cabeca'], ancoras: ['cabeca', 'topoDaCabeca', 'olhoEsquerdo', 'olhoDireito', 'boca'] }));
  for (const h of HUMORES) {
    add(`boca_${h}`, 'boca', null, () => camada({ ...base, boca: h }, { ver: ['boca'], recorte: ['cabeca'] }));
    add(`sobrancelhas_${h}`, 'sobrancelhas', 'cabelo', () => camada({ ...base, sobrancelhas: h }, { ver: ['sobrancelhas'], recorte: ['cabeca'] }));
  }
  for (const e of OLHOS) {
    for (const cilios of [false, true]) {
      add(`olhos_${e}${cilios ? '_cilios' : ''}`, 'olhos', null, () => camada({ ...base, olhos: e, cilios }, { ver: ['olhos'], recorte: ['cabeca'] }));
    }
  }
  for (const [nome, pose, quadro] of POSES) {
    add(`bracos_${nome}`, 'bracos', 'pele', () => camada({ ...base, pose, quadro }, { ver: ['bracos'], recorte: ['cabeca', 'mangas'], ancoras: ['maoEsquerda', 'maoDireita'] }));
  }
  if (quem === 'crianca') {
    for (const g of ['menina', 'menino']) {
      ESTILOS[g].forEach((estilo) => {
        const o = { ...base, menina: g === 'menina', estilo };
        add(`cabelo_tras_${g}_${estilo}`, 'cabeloTras', 'cabelo', () => camada(o, { ver: ['cabeloTras'], sombra: ['cabeloFrente'] }));
        add(`cabelo_frente_${g}_${estilo}`, 'cabeloFrente', 'cabelo', () => camada(o, { ver: ['cabeloFrente'], capta: ['cabeca'] }));
        add(`cabelo_brilho_${g}_${estilo}`, 'cabeloBrilho', null, () => camada(o, { ver: ['cabeloFrente'], recorte: ['cabeca'], brilho: true }));
      });
    }
    add('enfeite_trancas', 'enfeiteCabelo', null, () => camada({ ...base, estilo: 'trancas' }, { ver: ['enfeite'], recorte: ['cabeloTras'] }));
    add('laco', 'laco', null, () => camada({ ...base, estilo: 'liso' }, { ver: ['laco'], recorte: ['calota', 'cabeca'] }));
  } else {
    for (const g of ['menina', 'menino']) {
      const o = { ...base, menina: g === 'menina' };
      add(`cabelo_${g}`, 'cabeloFrente', 'cabelo', () => camada(o, { ver: ['cabeloFrente'], capta: ['cabeca'] }));
      add(`cabelo_brilho_${g}`, 'cabeloBrilho', null, () => camada(o, { ver: ['cabeloFrente'], recorte: ['cabeca'], brilho: true }));
    }
    add('laco', 'laco', null, () => camada({ ...base, menina: true }, { ver: ['laco'], recorte: ['calota', 'cabeca'] }));
  }
  // camadas de brilho: o render tira o fundo preto e deixa só os reflexos em branco translúcido
  for (const e of L) if (e.grupo === 'cabeloBrilho') e.posProcesso = 'brilho';
  return L;
}

export const camadasDosPersonagens = [...entradas('crianca'), ...entradas('irmao')];
