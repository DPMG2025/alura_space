// Catálogo da Fase 6 (casa por dentro), separado do catalogo.js principal enquanto outras fases andam em
// paralelo: CATALOGO=fase6 npm run render grava as imagens em app/src/main/assets/arte/ e o manifesto em
// manifesto_fase6.json. Tudo na câmera 'isometrico' (cameras.js), ids com o prefixo dentro_. Ver
// docs/visual-3d/relatorios/fase6a.md.
import { entradasDosMoveis, entradasDosComodos } from './objetos/casaPorDentro.js';

export const catalogo = [...entradasDosMoveis(), ...entradasDosComodos()];
