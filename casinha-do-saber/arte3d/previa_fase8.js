#!/usr/bin/env node
// Prévia da Fase 8 (não versionada): folha com toda a arte do manifesto_fase8.json (ou do manifesto.json depois
// de integrada): ícones dentro do botão redondo claro (ui2.css: .redondo), a casinha branca no botão verde, a mão,
// as poses dos mascotes e os enfeites, em fundos das telas.
//
//   node previa_fase8.js      grava saida/previa_fase8.png
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');

function manifesto() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto.json'), 'utf8')).imagens;
  const f8 = path.join(ARTE, 'manifesto_fase8.json');
  if (fs.existsSync(f8)) Object.assign(m, JSON.parse(fs.readFileSync(f8, 'utf8')).imagens);
  return m;
}

const url = (m, id) => 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, m[id].arquivo)).toString('base64');

async function main() {
  const m = manifesto();
  const ids = Object.keys(m);
  const novos = ['icone_avancar', 'icone_mapa', 'icone_album', 'icone_estante', 'icone_espelho', 'icone_sol', 'icone_lua', 'icone_pintar',
    'icone_tocar', 'icone_parar', 'icone_gravar', 'icone_ouvir', 'icone_repetir', 'icone_desfazer', 'icone_papel_novo', 'icone_lixeira'];
  const antigos = ['icone_fechar', 'icone_voltar', 'icone_som_ligado', 'icone_som_desligado', 'icone_ajustes', 'icone_pais', 'icone_casa', 'icone_loja'];
  const img = (id, dp) => m[id] ? `<img title="${id}" src="${url(m, id)}" style="width:${dp ?? m[id].uso.largura}px">` : `<b style="color:red">${id}?</b>`;
  const botao = (id) => `<div class="redondo">${img(id, 30)}</div>`;
  const grupo = (pref) => ids.filter((i) => i.startsWith(pref)).sort();
  const html = `<html><head><style>
    body{margin:0;font:13px sans-serif;background:#f3efff;width:1100px}
    .faixa{padding:14px 18px;display:flex;flex-wrap:wrap;gap:14px;align-items:flex-end}
    h3{margin:0;padding:10px 18px 0;color:#4a3a8a}
    .redondo{width:54px;height:54px;border-radius:50%;background:radial-gradient(circle at 35% 30%,#fff 0,#f2eeff 60%,#ddd3ff 100%);
      box-shadow:0 4px 0 #c9bdf7,0 6px 12px rgba(60,30,120,.25);border:3px solid #fff;display:flex;align-items:center;justify-content:center}
    .verde{height:72px;width:340px;border-radius:26px;background:linear-gradient(#6ee7a0,#2fc25b);box-shadow:0 6px 0 #1f9a46;display:flex;align-items:center;gap:14px;padding-left:28px;
      font:800 30px sans-serif;color:#fff;text-shadow:0 2px 0 #1f9a46}
    .noite{background:linear-gradient(#1d1b4f,#4b3a8f)}
    .premio{background:linear-gradient(#8a6cff,#b783ff,#ff9fcf)}
    .papel{background:#fff7e8}
    .cel{display:flex;flex-direction:column;align-items:center;gap:2px;color:#6a5aa8;font-size:10px}
  </style></head><body>
  <h3>Ícones da barra (Fase 2, depois os novos)</h3>
  <div class="faixa">${antigos.map(botao).join('')}</div>
  <div class="faixa">${novos.map((i) => `<div class="cel">${botao(i)}${i.replace('icone_', '')}</div>`).join('')}</div>
  <div class="faixa premio"><div class="verde">${img('icone_casa_branca', 40)}Pôr na casinha</div>${img('ui_mao_toque', 72)}${img('ui_mao_toque_apertando', 72)}</div>
  <h3>Corujinha e mascotes (todas as poses no mesmo recorte)</h3>
  <div class="faixa papel">${grupo('mascote_coruja').map((i) => img(i, 150)).join('')}</div>
  ${['cachorrinho', 'gatinho', 'dragaozinho'].map((e) => `<div class="faixa">${grupo('mascote_' + e).map((i) => img(i, 170)).join('')}</div>`).join('')}
  <h3>Enfeites</h3>
  <div class="faixa papel">${['tela_diploma_rolo', 'tela_medalha', 'tela_selo', 'tela_livro_fechado', 'tela_livro_aberto', 'tela_piano', 'tela_nota_musical'].map((i) => img(i)).join('')}</div>
  <div class="faixa">${grupo('tela_figurinha').map((i) => img(i)).join('')}</div>
  <div class="faixa noite">${['tela_lua_dormindo', 'tela_estrela_noite', 'tela_zzz'].map((i) => img(i)).join('')}${img('mascote_gatinho_dormindo', 160)}${img('mascote_coruja_dormindo', 140)}</div>
  <div class="faixa papel">${['tela_paleta', 'tela_pincel', 'tela_lapis', 'tela_balde', 'tela_borracha'].map((i) => img(i)).join('')}</div>
  </body></html>`;
  fs.mkdirSync(SAIDA, { recursive: true });
  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage({ viewport: { width: 1100, height: 800 } });
    await pagina.setContent(html);
    await pagina.waitForTimeout(300);
    await pagina.screenshot({ path: path.join(SAIDA, 'previa_fase8.png'), fullPage: true });
    console.log('saida/previa_fase8.png');
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
