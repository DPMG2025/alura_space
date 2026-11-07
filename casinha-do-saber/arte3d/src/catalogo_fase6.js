// Catálogo da Fase 6 (casa por dentro). Já faz parte do src/catalogo.js principal (o `npm run render` completo
// gera tudo no manifesto.json); CATALOGO=fase6 npm run render ainda renderiza só estas imagens, de modo
// incremental, com o manifesto à parte manifesto_fase6.json para revisão (o próximo render completo o apaga).
// Tudo na câmera 'isometrico' (cameras.js), ids com o prefixo dentro_. Ver docs/visual-3d/relatorios/fase6a.md.
import { entradasDosMoveis, entradasDosComodos } from './objetos/casaPorDentro.js';

export const catalogo = [...entradasDosMoveis(), ...entradasDosComodos()];
