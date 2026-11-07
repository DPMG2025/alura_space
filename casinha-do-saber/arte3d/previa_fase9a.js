#!/usr/bin/env node
// Prévia da Fase 9, arte em lote 1 (não versionada): as figuras novas do manifesto_fase9a.json em grade, no
// tamanho de uso (96 dp) e a 48 dp, com o emoji ao lado, e na primeira fila 6 figuras da Fase 4 para comparar
// o estilo. Uma folha por grupo de 40 figuras.
//
//   CATALOGO=fase9a npm run render && node previa_fase9a.js     grava saida/previa_fase9a_<n>.png
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');
const ANTIGAS = ['fig_gato', 'fig_peixe', 'fig_maca', 'fig_carrinho', 'fig_camiseta', 'fig_sapo'];

const ler = (f) => JSON.parse(fs.readFileSync(path.join(ARTE, f), 'utf8')).imagens;
const url = (m, id) => 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, m[id].arquivo)).toString('base64');

async function main() {
  const antigo = ler('manifesto.json');
  const novo = ler('manifesto_fase9a.json');
  const m = { ...antigo, ...novo };
  // node previa_fase9a.js [ids]: só as figuras dadas (fig_ opcional), numa folha previa_fase9a_sel.png
  const pedidos = process.argv.slice(2).map((i) => (i.startsWith('fig_') ? i : 'fig_' + i));
  const ids = pedidos.length ? pedidos : Object.keys(novo);
  const cel = (id, velha) => `<div class="cel${velha ? ' velha' : ''}"><img src="${url(m, id)}" style="width:96px"><img src="${url(m, id)}" style="width:48px">
    <div class="rot">${(m[id].emojis || []).join(' ')} ${id.replace('fig_', '')}<br><i>${m[id].lugar}</i></div></div>`;
  fs.mkdirSync(SAIDA, { recursive: true });
  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage({ viewport: { width: 1240, height: 800 } });
    for (let k = 0; k * 40 < ids.length; k++) {
      const grupo = ids.slice(k * 40, k * 40 + 40);
      const html = `<html><head><style>
        body{margin:0;font:12px sans-serif;background:linear-gradient(#bfe6ff,#fff1e0);width:1240px}
        .grade{display:grid;grid-template-columns:repeat(8,150px);gap:6px;padding:12px}
        .cel{display:flex;flex-direction:column;align-items:center;background:rgba(255,255,255,.45);border-radius:14px;padding:4px}
        .cel img:nth-child(2){margin-top:-4px}
        .velha{background:rgba(255,220,120,.45)}
        .rot{text-align:center;color:#3a2766;font-size:12px}
      </style></head><body><div class="grade">${ANTIGAS.map((i) => cel(i, true)).join('')}${grupo.map((i) => cel(i)).join('')}</div></body></html>`;
      await pagina.setContent(html);
      await pagina.waitForTimeout(200);
      const arq = path.join(SAIDA, pedidos.length ? `previa_fase9a_sel${k ? k + 1 : ''}.png` : `previa_fase9a_${k + 1}.png`);
      await pagina.screenshot({ path: arq, fullPage: true });
      console.log(path.relative(process.cwd(), arq));
    }
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
