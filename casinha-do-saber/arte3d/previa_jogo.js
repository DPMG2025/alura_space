#!/usr/bin/env node
// Prévia do jogo (PRD visual 3D, Fase 4): empilha as camadas do cenário, as figuras nas vagas e as crianças como
// o app vai empilhar e grava em saida/ (não versionado):
//   previa_jogo.png          a tela da maquete 2_jogo (lagoa com patinhos), ao lado da maquete
//   previa_jogo_cenas.png    as quatro ilhas com as cinco cenas e figuras nos seus lugares
//   previa_figuras.png       a grade das figuras (tamanho de uso e pequenas), com o emoji de cada uma
//
//   npm run previa:jogo     (depois de CATALOGO=fase4 npm run render)
//
// Como o app empilha uma camada do cenário: a camada tem 400 x 520 dp e é o recorte de y = 100 dp da tela de
// 400 dp de largura; na tela de L dp, desenhar com largura L em y = 100 x L / 400. Uma figura vai com o
// apoio (o pé) na vaga, ou, no varal, com a âncora 'topo' na vaga.
'use strict';
const fs = require('fs');
const path = require('path');
const http = require('http');
const { chromium } = require('playwright');

const RAIZ = __dirname;
const ARTE = path.resolve(RAIZ, '..', 'app', 'src', 'main', 'assets', 'arte');
const MAQUETE = path.resolve(RAIZ, '..', 'docs', 'visual-3d', 'maquetes', '2_jogo.png');
const SAIDA = path.join(RAIZ, 'saida');
const TIPOS = { '.html': 'text/html', '.js': 'text/javascript', '.json': 'application/json', '.css': 'text/css', '.woff2': 'font/woff2', '.webp': 'image/webp', '.png': 'image/png' };

function servir(paginas) {
  const srv = http.createServer((req, res) => {
    const url = decodeURIComponent(new URL(req.url, 'http://x').pathname);
    if (paginas[url]) { res.writeHead(200, { 'Content-Type': 'text/html' }); res.end(paginas[url]); return; }
    let arquivo;
    if (url.startsWith('/arte/')) arquivo = path.join(ARTE, path.basename(url));
    else if (url === '/maquete.png') arquivo = MAQUETE;
    else arquivo = path.normalize(path.join(RAIZ, url));
    if (!fs.existsSync(arquivo) || fs.statSync(arquivo).isDirectory()) { res.writeHead(404); res.end(); return; }
    res.writeHead(200, { 'Content-Type': TIPOS[path.extname(arquivo)] || 'application/octet-stream' });
    fs.createReadStream(arquivo).pipe(res);
  });
  return new Promise((ok) => srv.listen(0, '127.0.0.1', () => ok(srv)));
}

const CSS = `
@import url('/fonts/fonts.css');
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Baloo 2', 'Fredoka', sans-serif; color: #3a2766; background: #fff; }
.tela { position: relative; width: 393px; height: 852px; overflow: hidden; background: linear-gradient(180deg, #6cc3ff 0%, #a6dcff 35%, #ffe7d1 75%, #ffd0e0 100%); }
.tela canvas { position: absolute; left: 0; top: 0; width: 393px; height: 852px; }
.redondo { position: absolute; width: 58px; height: 58px; border-radius: 50%; background: radial-gradient(circle at 35% 28%, #ffffff 0 18%, #fdfbff 30%, #e9e2ff 100%);
  box-shadow: 0 6px 0 #b9a8ec, 0 10px 16px rgba(60, 30, 120, .28), inset 0 -4px 8px rgba(120, 90, 200, .18); border: 3px solid #fff; display: grid; place-items: center; }
.redondo img { width: 34px; height: 34px; }
.contas { position: absolute; left: 50%; transform: translateX(-50%); top: 24px; height: 40px; padding: 0 12px; border-radius: 20px; display: flex; gap: 6px; align-items: center; background: rgba(255,255,255,.75); box-shadow: 0 4px 0 rgba(120,100,200,.25); }
.contas img { width: 26px; height: 26px; }
.contas img.vez { width: 32px; height: 32px; }
.balao { position: absolute; background: linear-gradient(180deg, #ffffff 0%, #f6f2ff 100%); border-radius: 28px; padding: 14px 18px;
  box-shadow: 0 6px 0 #d7ccf7, 0 12px 22px rgba(60, 30, 120, .22); font-weight: 700; font-size: 25px; line-height: 1.15; color: #3a2766; }
.respostas { position: absolute; left: 16px; right: 16px; bottom: 30px; display: grid; grid-template-columns: 1fr 1fr; gap: 22px 16px; }
.bloco { position: relative; border-radius: 28px; height: 104px; display: grid; place-items: center; font-size: 66px; font-weight: 800; line-height: 1; color: #fff;
  background: radial-gradient(120% 70% at 30% 0%, rgba(255,255,255,.55) 0 30%, rgba(255,255,255,0) 55%), linear-gradient(180deg, var(--c) 0%, var(--c2, var(--c)) 100%);
  box-shadow: 0 8px 0 var(--b), 0 14px 22px rgba(40, 20, 90, .3), inset 0 -6px 10px rgba(0,0,0,.12), inset 0 3px 0 rgba(255,255,255,.45); border: 3px solid rgba(255,255,255,.75);
  text-shadow: 0 2px 0 rgba(0,0,0,.28), 2px 0 0 rgba(0,0,0,.28), -2px 0 0 rgba(0,0,0,.28), 0 -2px 0 rgba(0,0,0,.28), 0 4px 0 rgba(0,0,0,.22); }
.bloco span { transform: translateY(4px); }
.lado { display: flex; gap: 24px; padding: 16px; background: #fff; width: max-content; }
.rotulo { font: 600 15px Fredoka, sans-serif; text-align: center; margin-top: 6px; }
`;

const RESPOSTAS = [['#ff7a6b', '#f2524a', '#c4342e'], ['#5fb2ff', '#3b8ff0', '#2366c4'], ['#57d989', '#2fbf68', '#1d9150'], ['#ffc94d', '#ffad1f', '#d08100']];

/** Código da página: carrega o manifesto, empilha camadas, figuras e crianças em canvas. */
const SCRIPT = String.raw`
const m4 = await (await fetch('/arte/manifesto_fase4.json')).json();
const m1 = await (await fetch('/arte/manifesto.json')).json();
const M = { ...m1.imagens, ...m4.imagens };
const cache = {};
const img = (id) => cache[id] || (cache[id] = new Promise((ok, erro) => { const i = new Image(); i.onload = () => ok(i); i.onerror = () => erro(new Error(id)); i.src = '/arte/' + M[id].arquivo; }));
async function tingida(id, cor) {
  const im = await img(id);
  const t = document.createElement('canvas'); t.width = im.width; t.height = im.height;
  const c = t.getContext('2d');
  c.drawImage(im, 0, 0); c.globalCompositeOperation = 'multiply'; c.fillStyle = cor; c.fillRect(0, 0, t.width, t.height);
  c.globalCompositeOperation = 'destination-in'; c.drawImage(im, 0, 0);
  return t;
}
async function pilha(ids, perfil) {
  const cs = ids.map((id) => ({ id, ...M[id] })).sort((a, b) => a.ordem - b.ordem);
  const cv = document.createElement('canvas'); cv.width = cs[0].largura; cv.height = cs[0].altura;
  const ctx = cv.getContext('2d');
  for (const l of cs) ctx.drawImage(l.tingir ? await tingida(l.id, perfil[l.tingir]) : await img(l.id), 0, 0);
  return { cv, apoio: cs[0].apoio };
}
const PERFIL = { pele: '#8D5524', cabelo: '#1C1C1C', roupa: '#FF6FAE' };
const ANA = ['crianca_sombra', 'crianca_cabelo_tras_menina_liso', 'crianca_cabelo_brilho_tras_menina_liso', 'crianca_corpo', 'crianca_pes_menina', 'crianca_roupa_menina', 'crianca_roupa_menina_estampa', 'crianca_cabeca', 'crianca_boca_feliz', 'crianca_olhos_aberto_cilios', 'crianca_sobrancelhas_feliz', 'crianca_cabelo_frente_menina_liso', 'crianca_cabelo_brilho_menina_liso', 'crianca_bracos_tchau2', 'crianca_laco'];
const THEO = ['irmao_sombra', 'irmao_corpo', 'irmao_pes_menino', 'irmao_roupa_menino', 'irmao_cabeca', 'irmao_boca_feliz', 'irmao_olhos_aberto', 'irmao_sobrancelhas_feliz', 'irmao_cabelo_menino', 'irmao_cabelo_brilho_menino', 'irmao_bracos_parado'];

/**
 * Desenha a cena num canvas de L dp de largura (escala px por dp [px]): [ilha], [cena], [figuras] =
 * [[id, vaga ou {x,y} em px da camada, tamanho em dp, espelhar]], crianças na âncora 'personagens'.
 */
async function cena(ctx, { L = 393, px = 2, y0 = 0, ilha, cena: nomeDaCena, figuras = [], criancas = true }) {
  const k = (L * px) / M[ilha].largura; // camada -> canvas
  const topo = y0 + 100 * (L / 400) * px;
  const noCanvas = (p) => ({ x: p.x * k, y: topo + p.y * k });
  ctx.drawImage(await img(ilha), 0, topo, M[ilha].largura * k, M[ilha].altura * k);
  const camadaDaCena = nomeDaCena ? 'jogo_comum_' + nomeDaCena : null;
  if (camadaDaCena) ctx.drawImage(await img(camadaDaCena), 0, topo, M[camadaDaCena].largura * k, M[camadaDaCena].altura * k);
  const ancoras = { ...M[ilha].ancoras, ...(camadaDaCena ? M[camadaDaCena].ancoras : {}) };
  const itens = [];
  for (const [id, onde, tam, espelhar] of figuras) {
    const p = noCanvas(typeof onde === 'string' ? ancoras[onde] : onde);
    const f = M[id];
    const s = (tam * px) / f.uso.largura / 2 * 1; // uso em dp, imagem no dobro
    const ref = nomeDaCena === 'varal' ? f.ancoras.topo : f.apoio;
    itens.push({ y: p.y, desenhar: async () => {
      const im = await img(id);
      ctx.save();
      ctx.translate(p.x, p.y);
      if (espelhar) ctx.scale(-1, 1);
      ctx.drawImage(im, -ref.x * s, -ref.y * s, f.largura * s, f.altura * s);
      ctx.restore();
    } });
  }
  if (criancas) {
    const p = noCanvas(ancoras.personagens);
    const a = await pilha(ANA, PERFIL), b = await pilha(THEO, PERFIL);
    const kk = (138 * px) / a.cv.height * (L / 393) * 1.25;
    itens.push({ y: p.y - 1, desenhar: async () => {
      ctx.drawImage(b.cv, p.x + 52 * px - b.apoio.x * kk, p.y + 6 * px - b.apoio.y * kk, b.cv.width * kk, b.cv.height * kk);
      ctx.drawImage(a.cv, p.x - a.apoio.x * kk, p.y - a.apoio.y * kk, a.cv.width * kk, a.cv.height * kk);
    } });
  }
  itens.sort((u, v) => u.y - v.y);
  for (const it of itens) await it.desenhar();
}
window.cena = cena;
window.M = M;
window.img = img;
window.PRONTO = true;
`;

function paginaDaTela(pergunta, respostas) {
  return `<!doctype html><html><head><meta charset="utf-8"><style>${CSS}</style></head><body>
<div class="lado">
  <img src="/maquete.png" style="width:393px;height:852px;border-radius:12px">
  <div class="tela" id="tela">
    <canvas id="c" width="786" height="1704"></canvas>
    <div class="redondo" style="left:16px;top:16px"><img src="/arte/icone_fechar.webp"></div>
    <div class="contas"><img src="/arte/conta_verde.webp"><img src="/arte/conta_verde.webp"><img class="vez" src="/arte/conta_amarela.webp"><img src="/arte/conta_vazia.webp"><img src="/arte/conta_vazia.webp"></div>
    <div class="redondo" style="right:16px;top:16px"><img src="/arte/icone_som_ligado.webp"></div>
    <div class="balao" style="left:150px;right:14px;top:110px">${pergunta}
      <svg style="position:absolute;left:-18px;top:34px" width="24" height="30" viewBox="0 0 24 30"><path d="M24 0 L0 16 L24 28 Z" fill="#fbf9ff"/></svg></div>
    <div class="respostas">${respostas.map((r, i) => `<div class="bloco" style="--c:${RESPOSTAS[i][0]};--c2:${RESPOSTAS[i][1]};--b:${RESPOSTAS[i][2]}"><span>${r}</span></div>`).join('')}</div>
  </div>
</div>
<script type="module">${SCRIPT}</script></body></html>`;
}

const paginaVazia = `<!doctype html><html><head><meta charset="utf-8"><style>${CSS}</style></head><body><div id="raiz"></div><script type="module">${SCRIPT}</script></body></html>`;

async function main() {
  const srv = await servir({ '/tela.html': paginaDaTela('Quantos patinhos tem na lagoa?', [3, 5, 4, 6]), '/vazia.html': paginaVazia });
  const porta = srv.address().port;
  const navegador = await chromium.launch();
  fs.mkdirSync(SAIDA, { recursive: true });
  try {
    // 1. a tela da maquete 2
    const p = await navegador.newPage({ viewport: { width: 900, height: 900 } });
    p.on('pageerror', (e) => console.log('erro:', e.message));
    await p.goto(`http://127.0.0.1:${porta}/tela.html`);
    await p.waitForFunction(() => window.PRONTO === true);
    await p.evaluate(async () => {
      const ctx = document.getElementById('c').getContext('2d');
      await window.cena(ctx, { ilha: 'jogo_matematica_ilha', cena: 'lagoa', figuras: [['fig_pato', 'vaga_1', 96], ['fig_pato', 'vaga_2', 96, true], ['fig_pato', 'vaga_3', 96, true], ['fig_pato', 'vaga_4', 96]] });
      await document.fonts.ready;
    });
    await p.locator('.lado').screenshot({ path: path.join(SAIDA, 'previa_jogo.png') });
    console.log('prévia: saida/previa_jogo.png');

    // 2. as cenas
    const q = await navegador.newPage({ viewport: { width: 1600, height: 900 } });
    q.on('pageerror', (e) => console.log('erro:', e.message));
    await q.goto(`http://127.0.0.1:${porta}/vazia.html`);
    await q.waitForFunction(() => window.PRONTO === true);
    const cenas = await q.evaluate(async () => {
      const quadros = [
        ['jogo_matematica_ilha', 'lagoa', [['fig_peixe', 'vaga_1'], ['fig_sapo', 'vaga_2'], ['fig_tartaruga', 'vaga_3'], ['fig_pato', 'vaga_4'], ['fig_foca', 'vaga_5'], ['fig_pinguim', 'vaga_6']]],
        ['jogo_portugues_ilha', 'mesa', [['fig_maca', 'vaga_1'], ['fig_banana', 'vaga_2'], ['fig_morango', 'vaga_3'], ['fig_uva', 'vaga_4'], ['fig_bolo', 'vaga_5'], ['fig_bolinho', 'vaga_6']]],
        ['jogo_ingles_ilha', 'varal', [['fig_camiseta', 'vaga_1'], ['fig_meia', 'vaga_2'], ['fig_cachecol', 'vaga_3'], ['fig_luva', 'vaga_4'], ['fig_bone', 'vaga_5']]],
        ['jogo_logica_ilha', 'tapete', [['fig_bolinha_vermelha', 'vaga_1'], ['fig_carrinho', 'vaga_2'], ['fig_bolinha_azul', 'vaga_3'], ['fig_ursinho', 'vaga_4'], ['fig_tambor', 'vaga_5'], ['fig_bola', 'vaga_6']]],
        ['jogo_matematica_ilha', 'cercado', [['fig_vaca', 'vaga_1'], ['fig_cavalo', 'vaga_2'], ['fig_porco', 'vaga_3'], ['fig_galinha', 'vaga_4'], ['fig_ovelha', 'vaga_5'], ['fig_pintinho', 'vaga_6']]],
        ['jogo_ingles_ilha', null, [['fig_gato', 'vaga_1'], ['fig_cachorro', 'vaga_2'], ['fig_leao', 'vaga_3'], ['fig_elefante', 'vaga_4'], ['fig_macaco', 'vaga_5'], ['fig_coelho', 'vaga_6'], ['fig_balao', 'ceu_1'], ['fig_borboleta', 'ceu_2'], ['fig_estrela', 'ceu_3'], ['fig_pipa', 'ceu_4']]],
      ];
      const L = 393, px = 1.5, alt = 600;
      const cv = document.createElement('canvas'); cv.width = 3 * L * px; cv.height = 2 * alt * px;
      const ctx = cv.getContext('2d');
      for (let i = 0; i < quadros.length; i++) {
        const [ilha, nome, figs] = quadros[i];
        const sub = document.createElement('canvas'); sub.width = L * px; sub.height = alt * px;
        const c = sub.getContext('2d');
        const g = c.createLinearGradient(0, 0, 0, sub.height);
        g.addColorStop(0, '#6cc3ff'); g.addColorStop(0.5, '#a6dcff'); g.addColorStop(1, '#ffe7d1');
        c.fillStyle = g; c.fillRect(0, 0, sub.width, sub.height);
        await window.cena(c, { L, px, y0: -40 * px, ilha, cena: nome, figuras: figs.map(([id, v]) => [id, v, 72]) });
        ctx.drawImage(sub, (i % 3) * L * px, Math.floor(i / 3) * alt * px);
      }
      return cv.toDataURL('image/png');
    });
    fs.writeFileSync(path.join(SAIDA, 'previa_jogo_cenas.png'), Buffer.from(cenas.split(',')[1], 'base64'));
    console.log('prévia: saida/previa_jogo_cenas.png');

    // 3. a grade das figuras
    const grade = await q.evaluate(async () => {
      const ids = Object.keys(window.M).filter((k) => k.startsWith('fig_'));
      const lado = 200, cols = 10, rot = 34;
      const cv = document.createElement('canvas'); cv.width = lado * cols; cv.height = (lado + rot) * Math.ceil(ids.length / cols);
      const ctx = cv.getContext('2d');
      const g = ctx.createLinearGradient(0, 0, 0, cv.height); g.addColorStop(0, '#d9efff'); g.addColorStop(1, '#ffeadf');
      ctx.fillStyle = g; ctx.fillRect(0, 0, cv.width, cv.height);
      for (let i = 0; i < ids.length; i++) {
        const id = ids[i], f = window.M[id], im = await window.img(id);
        const x = (i % cols) * lado, y = Math.floor(i / cols) * (lado + rot);
        ctx.drawImage(im, x + 4, y + 4, 192 * 0.82, 192 * 0.82);
        ctx.drawImage(im, x + 150, y + 108, 48, 48); // 48 dp na tela de 1x
        ctx.font = '600 15px Fredoka, sans-serif'; ctx.fillStyle = '#3b2a5a';
        ctx.fillText(id.replace('fig_', '') + '  ' + f.emojis.join(' '), x + 6, y + lado + 14);
        ctx.font = '12px sans-serif'; ctx.fillStyle = '#7a6a9a';
        ctx.fillText(f.lugar + (f.tipo === 'item' ? ' · voa' : ''), x + 6, y + lado + 30);
      }
      return cv.toDataURL('image/png');
    });
    fs.writeFileSync(path.join(SAIDA, 'previa_figuras.png'), Buffer.from(grade.split(',')[1], 'base64'));
    console.log('prévia: saida/previa_figuras.png');
  } finally {
    await navegador.close();
    srv.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
