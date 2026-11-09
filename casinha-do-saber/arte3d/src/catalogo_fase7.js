// Catálogo da Fase 7 (guarda-roupa): roupas, acessórios e chapéus em camadas na câmera 'personagem', o mesmo
// quadro da criança (170 x 260 dp). Renderizado à parte com `CATALOGO=fase7 npm run render`: modo incremental,
// manifesto separado em app/src/main/assets/arte/manifesto_fase7.json (o integrador junta ao manifesto.json).
//
// Campos como em camadas.js (grupo, tingir, ordem, personagem) e mais:
//   esconde  grupos das camadas da criança que a peça substitui (o app não desenha esses grupos).
// A ordem de empilhamento e as regras de uso estão em docs/visual-3d/relatorios/fase7a.md.
import { TRAJES, ACESSORIOS, CHAPEUS, tapaOlho, camadaDoGuardaRoupa, mangasCompridas } from './objetos/guardaRoupa.js';
import { QUADRO_CRIANCA } from './camadas.js';

/** Ordem (de trás para a frente) das camadas do guarda-roupa, intercaladas com as da criança (camadas.js > ORDEM). */
export const ORDEM_FASE7 = {
  roupaTras: 14, // capa do herói, asas da fada e da abelha, rabo do dinossauro, manto real
  costas: 16, // mochila, asas de borboleta, capa mágica
  pes: 35, // sapatos próprios do traje (grupo 'pes': o mesmo dos sapatos básicos)
  pesAcessorio: 36, // tênis, botas, pantufas
  roupa: 40, // corpo do traje (substitui a roupa básica)
  estampa: 45, // estampa colorida dos trajes tingidos
  costasFrente: 52, // alças da mochila, fecho da capa (na frente do peito, atrás do queixo por recorte)
  pescoco: 53, // colar, cachecol, gravata
  tapaOlho: 66, // tapa-olho do pirata (sem acessório no rosto)
  rosto: 67, // óculos, óculos de sol, máscara
  cabelo: 75, // laço, tiara, presilha
  trajeCabeca: 76, // anteninhas, capacete, chapéu de chuva, touca de chef, lenço de pirata, capuz de dino
  chapeu: 77, // coroa, boné, cartola, flor
  mao: 78, // varinha, balão, ursinho (embaixo da camada dos braços: a mão "segura")
  mangas: 82, // mangas compridas por pose (em cima dos braços; a mão continua visível)
};

const POSES = [['parado', 'parado', 1], ['tchau1', 'tchau', 1], ['tchau2', 'tchau', 2], ['tchau3', 'tchau', 3], ['tchau4', 'tchau', 4], ['torcendo', 'torcendo', 1], ['pensando', 'pensando', 1]];

const L = [];
function add(id, grupo, tingir, montar, extra = {}) {
  L.push({ id, tipo: 'personagem', uso: QUADRO_CRIANCA, grupo, tingir, ordem: ORDEM_FASE7[grupo], personagem: 'crianca', montar, ...extra });
}
const fig = { menina: true, estilo: 'liso' };

// --- Trajes -------------------------------------------------------------------------------------
for (const [nome, def] of Object.entries(TRAJES)) {
  const variantes = def.genero ? [['_menina', true], ['_menino', false]] : [['', true]];
  const amostra = def.montar(true);
  for (const [sufixo, menina] of variantes) {
    const peca = () => def.montar(menina);
    const esconde = ['roupa', 'estampa'];
    if (amostra.pes) esconde.push('pes');
    add(`roupa_${nome}${sufixo}`, 'roupa', amostra.tingir ? 'roupa' : null,
      () => camadaDoGuardaRoupa(fig, () => ({ traje: peca().corpo }), { ver: ['traje'], recorte: ['sapatosBase'], sombra: ['cabeca'] }),
      { esconde });
    if (amostra.estampa) {
      add(`roupa_${nome}${sufixo}_estampa`, 'estampa', null, () => camadaDoGuardaRoupa(fig, () => {
        const t = peca();
        return { traje: t.corpo, estampa: t.estampa };
      }, { ver: ['estampa'], recorte: ['traje', 'sapatosBase'], sombra: ['cabeca'] }));
    }
  }
  // peças sem diferença de gênero
  const unica = (campo) => () => def.montar(true)[campo];
  if (amostra.tras) {
    add(`roupa_${nome}_tras`, 'roupaTras', null, () => camadaDoGuardaRoupa(fig, () => ({ tras: unica('tras')() }), { ver: ['tras'], sombra: ['cabeca', 'tronco'] }));
  }
  if (amostra.pes) {
    add(`roupa_${nome}_pes`, 'pes', null, () => camadaDoGuardaRoupa(fig, () => ({ pesTraje: unica('pes')(), traje: unica('corpo')() }), { ver: ['pesTraje'], sombra: ['traje'] }));
  }
  if (amostra.cabeca) {
    add(`roupa_${nome}_cabeca`, 'trajeCabeca', null, () => camadaDoGuardaRoupa(fig, () => ({ chapeu: unica('cabeca')() }), { ver: ['chapeu'], capta: ['cabeca', 'calota'] }), { esconde: ['laco'] });
  }
  if (amostra.mangas) {
    for (const [pose, nomePose, quadro] of POSES) {
      add(`roupa_${nome}_mangas_${pose}`, 'mangas', null, () => camadaDoGuardaRoupa({ ...fig, pose: nomePose, quadro }, (P) => {
        const [mat, punho] = def.montar(true).mangas;
        return { mangasLongas: mangasCompridas(P, mat, punho) };
      }, { ver: ['mangasLongas'], recorte: ['cabeca', 'maos'] }));
    }
  }
}
add('roupa_pirata_tapaolho', 'tapaOlho', null, () => camadaDoGuardaRoupa(fig, () => ({ tapa: tapaOlho() }), { ver: ['tapa'], capta: ['cabeca'] }));

// --- Acessórios ---------------------------------------------------------------------------------
for (const [nome, def] of Object.entries(ACESSORIOS)) {
  const id = `acessorio_${nome}`;
  switch (def.parte) {
    case 'ROSTO':
      add(id, 'rosto', null, () => camadaDoGuardaRoupa(fig, () => ({ peca: def.montar() }), { ver: ['peca'], recorte: ['cabeca'] }), { esconde: ['tapaOlho'], ...(def.ordem ? { ordem: def.ordem } : {}) });
      break;
    case 'PESCOCO':
      add(id, 'pescoco', null, () => camadaDoGuardaRoupa(fig, () => ({ peca: def.montar() }), { ver: ['peca'], recorte: ['cabeca', 'tronco'], sombra: ['cabeca'] }));
      break;
    case 'COSTAS':
      add(id, 'costas', null, () => camadaDoGuardaRoupa(fig, () => ({ peca: def.montar() }), { ver: ['peca'], sombra: ['cabeca', 'tronco'] }));
      if (def.frente) {
        add(`${id}_frente`, 'costasFrente', null, () => camadaDoGuardaRoupa(fig, () => ({ peca: def.frente() }), { ver: ['peca'], recorte: ['cabeca', 'tronco'], sombra: ['cabeca'] }));
      }
      break;
    case 'MAO':
      // a mão esquerda (da tela) só tem duas posições: embaixo (parado, tchau, pensando) e em cima (torcendo)
      for (const [variante, pose] of [['baixo', 'parado'], ['cima', 'torcendo']]) {
        add(`${id}_${variante}`, 'mao', null, () => camadaDoGuardaRoupa({ ...fig, pose }, (P) => ({ peca: def.montarNaMao(P, variante === 'cima') }), { ver: ['peca'], ancoras: ['maoEsquerda'] }));
      }
      break;
    case 'PES':
      add(id, 'pesAcessorio', null, () => camadaDoGuardaRoupa(fig, () => ({ peca: def.montar() }), { ver: ['peca'] }), { esconde: ['pes'] });
      break;
    case 'CABELO':
      add(id, 'cabelo', null, () => camadaDoGuardaRoupa(fig, () => ({ peca: def.montar() }), { ver: ['peca'], capta: ['cabeca', 'calota'] }), { esconde: ['laco'] });
      break;
    default:
      throw new Error('parte desconhecida: ' + def.parte);
  }
}

// --- Chapéus ------------------------------------------------------------------------------------
for (const [nome, def] of Object.entries(CHAPEUS)) {
  add(`chapeu_${nome}`, 'chapeu', null, () => camadaDoGuardaRoupa(fig, () => ({ chapeu: def.montar() }), {
    ver: ['chapeu'], capta: ['cabeca', 'calota'], ancoras: ['cabeca', 'topoDaCabeca'],
  }), { esconde: ['laco', 'trajeCabeca'] });
}

export const catalogo = L;
