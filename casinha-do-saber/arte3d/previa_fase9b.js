#!/usr/bin/env node
// Prévia da Fase 9B (lote 2 das figuras): grava em saida/ (não versionado)
//   previa_fase9b.png   a grade das figuras novas (192 px = 96 dp, e a 48 dp no canto) ao lado de figuras antigas
//                       das perguntas (moldura cinza), sobre o céu das telas, com o nome e o lugar de cada uma.
//   node previa_fase9b.js            (depois de CATALOGO=fase9b npm run render)
'use strict';
const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');
const novas = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto_fase9b.json'), 'utf8')).imagens;
const antigas = ['fig_gato', 'fig_bola', 'fig_maca', 'fig_tambor', 'fig_flor', 'fig_carrinho', 'fig_balao', 'fig_abelha'];
const COL = 10, W = 210, H = 240;

async function main() {
  fs.mkdirSync(SAIDA, { recursive: true });
  const ids = [...antigas.map((i) => [i, true]), ...Object.keys(novas).map((i) => [i, false])];
  const linhas = Math.ceil(ids.length / COL);
  const fundo = Buffer.from(`<svg xmlns="http://www.w3.org/2000/svg" width="${COL * W}" height="${linhas * H}">
    <defs><linearGradient id="c" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#8fd0ff"/><stop offset=".6" stop-color="#d8efff"/><stop offset="1" stop-color="#ffe7d1"/></linearGradient></defs>
    <rect width="100%" height="100%" fill="url(#c)"/></svg>`);
  const camadas = [];
  for (const [k, [id, antiga]] of ids.entries()) {
    const x = (k % COL) * W, y = Math.floor(k / COL) * H;
    const info = antiga ? null : novas[id];
    const nome = id.replace('fig_', '');
    const rot = antiga ? 'antiga' : `${info.lugar}${info.tipo === 'item' ? ' (voa)' : ''}`;
    camadas.push({ input: Buffer.from(`<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}">
      <rect x="4" y="4" width="${W - 8}" height="${H - 8}" rx="18" fill="${antiga ? '#e9e9f0' : '#ffffff'}" fill-opacity=".55" stroke="${antiga ? '#9a9ab0' : '#ffffff'}" stroke-width="3"/>
      <text x="${W / 2}" y="${H - 26}" font-family="sans-serif" font-size="15" font-weight="700" text-anchor="middle" fill="#3a2766">${nome}</text>
      <text x="${W / 2}" y="${H - 10}" font-family="sans-serif" font-size="12" text-anchor="middle" fill="#6a5a96">${rot}</text></svg>`), left: x, top: y });
    const arq = path.join(ARTE, id + '.webp');
    camadas.push({ input: await sharp(arq).toBuffer(), left: x + 9, top: y + 8 });
    camadas.push({ input: await sharp(arq).resize(48, 48, { kernel: 'lanczos3' }).png().toBuffer(), left: x + W - 54, top: y + 8 });
  }
  await sharp(fundo).composite(camadas).png().toFile(path.join(SAIDA, 'previa_fase9b.png'));
  console.log('prévia: saida/previa_fase9b.png', ids.length, 'figuras');
}
main().catch((e) => { console.error(e); process.exit(1); });
