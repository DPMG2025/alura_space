#!/usr/bin/env node
// Prévia da Fase 9C (não versionada): os ícones novos ao lado de 6 ícones antigos da barra, em 32 dp e 48 dp
// (tela 2x, como um celular xhdpi), dentro do botão redondo claro, numa pílula de aba e sobre fundos coloridos
// (selinho do "certo" e setas da escada). Mais uma folha ampliada (96 dp) para olhar os detalhes.
//
//   node previa_fase9c.js      grava saida/previa_fase9c.png e saida/previa_fase9c_grande.png
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');

function manifesto() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto.json'), 'utf8')).imagens;
  Object.assign(m, JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto_fase9c.json'), 'utf8')).imagens);
  return m;
}

const url = (m, id) => 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, m[id].arquivo)).toString('base64');

async function main() {
  const m = manifesto();
  const novos = Object.keys(JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto_fase9c.json'), 'utf8')).imagens);
  const ordem = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto_fase9c.json'), 'utf8')).imagens;
  void ordem;
  const antigos = ['icone_fechar', 'icone_casa', 'icone_loja', 'icone_pais', 'icone_mapa', 'icone_espelho'];
  const img = (id, dp) => `<img title="${id}" src="${url(m, id)}" style="width:${dp}px;height:${dp}px">`;
  const grupos = [
    ['Guarda-roupa, geladeira e espelho', ['roupas', 'chapeus', 'rosto', 'pescoco', 'costas', 'mao', 'pes', 'cabelo', 'frutas', 'leite', 'congelador',
      'pele', 'cor_do_cabelo', 'penteado', 'roupa', 'escrever'].map((n) => 'icone_' + n)],
    ['Trilha (nós e marcos)', novos.filter((i) => i.startsWith('icone_no_') || ['icone_certo', 'icone_alfinete'].includes(i))],
    ['Casa e botões soltos', ['icone_subir', 'icone_descer', 'icone_bau', 'icone_martelo', 'icone_marcador', 'icone_bussola', 'icone_vassoura', 'icone_dica', 'icone_broto']],
    ['Abas das áreas', novos.filter((i) => i.startsWith('icone_area_'))],
  ];
  const linha = (ids, dp, cls) => `<div class="faixa">${antigos.map((i) => `<div class="${cls} velho">${img(i, dp)}</div>`).join('')}<span class="sep"></span>${ids.map((i) => `<div class="${cls}">${img(i, dp)}</div>`).join('')}</div>`;
  const html = `<html><head><style>
    body{margin:0;font:12px sans-serif;background:#f3efff;width:1180px}
    h3{margin:0;padding:10px 14px 2px;color:#4a3a8a}
    .faixa{padding:6px 14px;display:flex;flex-wrap:wrap;gap:8px;align-items:center}
    .sep{width:2px;height:40px;background:#c9bdf7;margin:0 6px}
    .r48{width:56px;height:56px}.r32{width:40px;height:40px}
    .r48,.r32{border-radius:50%;background:radial-gradient(circle at 35% 30%,#fff 0,#f2eeff 60%,#ddd3ff 100%);
      box-shadow:0 3px 0 #c9bdf7;border:2px solid #fff;display:flex;align-items:center;justify-content:center}
    .velho{outline:2px dashed #b9a8ff}
    .aba{height:44px;padding:0 10px;border-radius:22px;background:#fff;box-shadow:0 4px 0 #d9b184;display:flex;align-items:center}
    .aba.sel{background:#ffc21a;box-shadow:0 4px 0 #e0a800}
    .cor{display:flex;gap:10px;padding:10px 14px;align-items:center}
    .selo{width:26px;height:26px;border-radius:50%;border:2px solid #fff;display:flex;align-items:center;justify-content:center}
    .cena{width:120px;height:90px;border-radius:12px;display:flex;align-items:center;justify-content:center;background:linear-gradient(#f6d7a8,#b07a4a)}
  </style></head><body>
  ${grupos.map(([t, ids]) => `<h3>${t}: 48 dp e 32 dp (tracejados = antigos)</h3>${linha(ids, 30, 'r48')}${linha(ids, 22, 'r32')}`).join('')}
  <h3>Abas (pílula 44 dp, ícone 32 dp) e áreas</h3>
  <div class="faixa">${['icone_roupas', 'icone_chapeus', 'icone_rosto', 'icone_pescoco', 'icone_costas', 'icone_mao', 'icone_pes', 'icone_cabelo'].map((i, k) => `<div class="aba${k === 2 ? ' sel' : ''}">${img(i, 32)}</div>`).join('')}</div>
  <div class="faixa">${novos.filter((i) => i.startsWith('icone_area_')).map((i, k) => `<div class="aba${k === 0 ? ' sel' : ''}">${img(i, 30)}</div>`).join('')}</div>
  <h3>Sobre cor: selinho do certo (26 dp), setas na escada, nó fechado</h3>
  <div class="cor">${['#3a86ff', '#ff4a55', '#2fc25b', '#9a5cff', '#ff8a1c'].map((c) => `<div class="selo" style="background:${c}">${img('icone_certo_branco', 20)}</div>`).join('')}
    <div class="cena">${img('icone_subir', 44)}</div><div class="cena" style="background:linear-gradient(#5a4a9a,#2e2563)">${img('icone_descer', 44)}</div>
    <div class="cena" style="background:#e9e2ff">${img('icone_no_bloqueado', 26)}${img('icone_pais', 26)}</div></div>
  </body></html>`;
  const grande = `<html><body style="margin:0;background:#f3efff;width:1180px;display:flex;flex-wrap:wrap;gap:6px;padding:8px;font:11px sans-serif">
    ${[...antigos, ...novos].map((i) => `<div style="display:flex;flex-direction:column;align-items:center;width:104px">${img(i, 96)}<span>${i.replace('icone_', '')}</span></div>`).join('')}</body></html>`;
  fs.mkdirSync(SAIDA, { recursive: true });
  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage({ viewport: { width: 1180, height: 800 }, deviceScaleFactor: 2 });
    await pagina.setContent(html);
    await pagina.waitForTimeout(300);
    await pagina.screenshot({ path: path.join(SAIDA, 'previa_fase9c.png'), fullPage: true });
    const p2 = await navegador.newPage({ viewport: { width: 1200, height: 800 }, deviceScaleFactor: 1 });
    await p2.setContent(grande);
    await p2.waitForTimeout(300);
    await p2.screenshot({ path: path.join(SAIDA, 'previa_fase9c_grande.png'), fullPage: true });
    console.log('saida/previa_fase9c.png saida/previa_fase9c_grande.png');
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
