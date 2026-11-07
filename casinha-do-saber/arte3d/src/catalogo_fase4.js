// Catálogo da Fase 4 (jogo), feito em paralelo às fases 5 e 7: `CATALOGO=fase4 npm run render` gera só estas
// imagens e o manifesto à parte app/src/main/assets/arte/manifesto_fase4.json. Quem juntar as fases espalha
// `catalogoDaFase4` no src/catalogo.js (e apaga este arquivo e o manifesto à parte).
//
// Camadas do cenário (câmera 'jogo', todas com o mesmo quadro de 400 x 520 dp e o mesmo apoio = centro da
// ilha): jogo_<caminho>_ilha (caminho = dominio/Trilha.name em minúsculas) e jogo_comum_<cena> (as cenas das
// perguntas, as mesmas nos quatro caminhos). Figuras: fig_<nome> (src/figuras.js).
import { ilhaDoCaminho, camadaDaCenaDoJogo, CORES_DOS_CAMINHOS } from './objetos/cenasDoJogo.js';
import { figuras } from './figuras.js';

const QUADRO_DO_JOGO = { largura: 400, altura: 520 };

/** Ordem de empilhamento (da menor para a maior); as figuras e as crianças vão por cima, em ordem de y. */
export const ORDEM_DO_JOGO = { ilha: 10, cena: 20 };

const camadasDoJogo = [
  ...Object.keys(CORES_DOS_CAMINHOS).map((c) => ({
    id: `jogo_${c}_ilha`, tipo: 'jogo', uso: QUADRO_DO_JOGO, grupo: 'ilha', tingir: null, ordem: ORDEM_DO_JOGO.ilha, montar: () => ilhaDoCaminho(c),
  })),
  ...['lagoa', 'mesa', 'varal', 'cercado', 'tapete'].map((n) => ({
    id: `jogo_comum_${n}`, tipo: 'jogo', uso: QUADRO_DO_JOGO, grupo: 'cena', tingir: null, ordem: ORDEM_DO_JOGO.cena, montar: camadaDaCenaDoJogo(n),
  })),
];

export const catalogoDaFase4 = [...camadasDoJogo, ...figuras];
export const catalogo = catalogoDaFase4;
