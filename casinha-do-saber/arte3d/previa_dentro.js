#!/usr/bin/env node
// Prévia da casa por dentro (PRD visual 3D, Fase 6): monta os dois andares com as imagens dentro_* de
// app/src/main/assets/arte/ (manifesto_fase6.json) exatamente como o app deve montar: a Projecao de
// ui/componentes/Isometrico.kt com ladrilho = 64 dp x 2 = 128 px (escala 1:1 com as imagens), cada imagem com
// o apoio em Projecao.ponto(referência) e os móveis na ordem do pintor de PlantaDaCasa.ordenarParaDesenho.
// Grava em saida/ (não versionado):
//   previa_dentro.png        a casa por dentro, dois andares empilhados, todos os móveis no lugar de sempre
//   previa_dentro_grade.png  a mesma, com a grade do piso e as caixas de oclusão (pegada x altura) por cima
//   previa_dentro_sala.png   a sala e a cozinha de perto (o andar de baixo), com a grade
//   previa_dentro_pecas.png  variantes: geladeira aberta, guarda-roupa aberto, portas, janelas de noite, lâmpadas
//
//   npm run previa:dentro
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');

async function main() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto_fase6.json'), 'utf8')).imagens;
  const dados = {};
  for (const [id, info] of Object.entries(m)) dados[id] = 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, info.arquivo)).toString('base64');
  fs.mkdirSync(SAIDA, { recursive: true });
  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage();
    await pagina.setContent('<html><body style="margin:0"></body></html>');
    const saidas = await pagina.evaluate(async ({ m, dados }) => {
      const imgs = {};
      await Promise.all(Object.entries(dados).map(([id, url]) => new Promise((ok, erro) => {
        const im = new Image(); im.onload = () => { imgs[id] = im; ok(); }; im.onerror = erro; im.src = url;
      })));
      // ---- PlantaDaCasa
      const L = 8, F = 5, H = 3.0;
      const meio = (a) => (a === 'de_cima' ? 4 : 5);
      const comodos = (a) => (a === 'de_cima' ? ['quarto', 'banheiro'] : ['sala', 'cozinha']);
      const corDo = { quarto: '#FFE3EC', banheiro: '#D7F3FF', sala: '#FFF1C9', cozinha: '#E2F7D5' };
      const andarDo = (c) => (c === 'quarto' || c === 'banheiro' ? 'de_cima' : 'de_baixo');
      const moveis = Object.entries(m).filter(([id, i]) => i.iso.movel && i.iso.estado === 'normal').map(([id, i]) => ({ id, ...i.iso.pegada, movel: i.iso.movel, andar: andarDo(i.iso.comodo) }));
      const naParede = new Set(['quadro', 'espelho']);
      const noChao = new Set(['tapete']);
      // ---- Projecao (Isometrico.kt)
      const proj = (tw, ox, oy) => ({
        tw, x: (gx, gy) => ox + (gx - gy) * tw / 2, y: (gx, gy, z = 0) => oy + (gx + gy) * tw / 4 - z * tw * 0.56,
      });
      function sprite(ctx, p, id, rx, ry, rz, tingir) {
        const info = m[id]; const im = imgs[id];
        const k = p.tw / (info.iso.ladrilho * 2);
        const x = p.x(rx, ry) - info.apoio.x * k, y = p.y(rx, ry, rz) - info.apoio.y * k;
        if (!tingir) { ctx.drawImage(im, x, y, info.largura * k, info.altura * k); return; }
        const t = document.createElement('canvas'); t.width = im.width; t.height = im.height;
        const c = t.getContext('2d');
        c.drawImage(im, 0, 0); c.globalCompositeOperation = 'multiply'; c.fillStyle = tingir; c.fillRect(0, 0, t.width, t.height);
        c.globalCompositeOperation = 'destination-in'; c.drawImage(im, 0, 0);
        ctx.drawImage(t, x, y, info.largura * k, info.altura * k);
      }
      // ---- ordem do pintor (PlantaDaCasa.atras / ordenarParaDesenho)
      function atras(a, b) {
        const e = 0.001;
        const sob = a.x0 - a.y1 < b.x1 - b.y0 - e && b.x0 - b.y1 < a.x1 - a.y0 - e;
        if (!sob) return false;
        if (a.x1 <= b.x0 + e || a.y1 <= b.y0 + e) return true;
        if (b.x1 <= a.x0 + e || b.y1 <= a.y0 + e) return false;
        return a.z1 <= b.z0 + e;
      }
      function ordenar(cx) {
        const n = cx.length, antes = cx.map(() => []), grau = new Array(n).fill(0);
        for (let i = 0; i < n; i++) for (let j = 0; j < n; j++) if (i !== j && atras(cx[i], cx[j])) { antes[i].push(j); grau[j]++; }
        const chave = (i) => (cx[i].x0 + cx[i].x1 + cx[i].y0 + cx[i].y1) / 2 + cx[i].z0 * 0.01;
        const feito = new Array(n).fill(false), ordem = [];
        for (let k = 0; k < n; k++) {
          let melhor = -1;
          for (let i = 0; i < n; i++) if (!feito[i] && grau[i] === 0 && (melhor < 0 || chave(i) < chave(melhor))) melhor = i;
          if (melhor < 0) for (let i = 0; i < n; i++) if (!feito[i] && (melhor < 0 || chave(i) < chave(melhor))) melhor = i;
          feito[melhor] = true; ordem.push(melhor);
          for (const j of antes[melhor]) grau[j]--;
        }
        return ordem.map((i) => cx[i]);
      }
      function grade(ctx, p) {
        ctx.save(); ctx.strokeStyle = 'rgba(40,20,80,0.35)'; ctx.lineWidth = 1;
        for (let x = 0; x <= L; x++) { ctx.beginPath(); ctx.moveTo(p.x(x, 0), p.y(x, 0)); ctx.lineTo(p.x(x, F), p.y(x, F)); ctx.stroke(); }
        for (let y = 0; y <= F; y++) { ctx.beginPath(); ctx.moveTo(p.x(0, y), p.y(0, y)); ctx.lineTo(p.x(L, y), p.y(L, y)); ctx.stroke(); }
        ctx.restore();
      }
      function caixas(ctx, p, lista) {
        ctx.save(); ctx.strokeStyle = 'rgba(230,30,90,0.85)'; ctx.lineWidth = 1.5;
        for (const c of lista) {
          const q = [[c.x0, c.y0], [c.x1, c.y0], [c.x1, c.y1], [c.x0, c.y1]];
          for (const z of [c.z0, c.z1]) { ctx.beginPath(); q.forEach(([x, y], i) => (i ? ctx.lineTo(p.x(x, y), p.y(x, y, z)) : ctx.moveTo(p.x(x, y), p.y(x, y, z)))); ctx.closePath(); ctx.stroke(); }
          for (const [x, y] of q) { ctx.beginPath(); ctx.moveTo(p.x(x, y), p.y(x, y, c.z0)); ctx.lineTo(p.x(x, y), p.y(x, y, c.z1)); ctx.stroke(); }
        }
        ctx.restore();
      }
      function andar(ctx, p, a, { comGrade = false, noite = false } = {}) {
        sprite(ctx, p, `dentro_laje_${a}`, 0, 0, 0);
        for (const c of comodos(a)) sprite(ctx, p, `dentro_piso_${c}`, 0, 0, 0);
        for (const c of comodos(a)) sprite(ctx, p, `dentro_parede_${c}`, 0, 0, 0, corDo[c]);
        const sufixo = noite ? '_noite' : '';
        if (a === 'de_cima') { sprite(ctx, p, 'dentro_janela' + sufixo, 3.22, 0, 1.32); sprite(ctx, p, 'dentro_janela_redonda' + sufixo, 4.975, 0, 1.55); }
        else { sprite(ctx, p, 'dentro_janela' + sufixo, 2.1, 0, 1.32); sprite(ctx, p, 'dentro_janela' + sufixo, 6.05, 0, 1.32); sprite(ctx, p, 'dentro_porta_0', 0, 3.95, 0); }
        sprite(ctx, p, `dentro_acabamento_${a}`, 0, 0, 0);
        const doAndar = moveis.filter((mv) => mv.andar === a);
        for (const mv of doAndar.filter((mv) => noChao.has(mv.movel))) sprite(ctx, p, mv.id, (mv.x0 + mv.x1) / 2, (mv.y0 + mv.y1) / 2, mv.z);
        if (comGrade) grade(ctx, p);
        for (const mv of doAndar.filter((mv) => naParede.has(mv.movel))) sprite(ctx, p, mv.id, (mv.x0 + mv.x1) / 2, 0, mv.z);
        const soltas = doAndar.filter((mv) => !noChao.has(mv.movel) && !naParede.has(mv.movel))
          .map((mv) => ({ ...mv, z0: mv.z, z1: mv.z + mv.altura }));
        for (const y of [0, 1, 2]) soltas.push({ id: 'dentro_parede_interna', x0: meio(a) - 0.06, x1: meio(a) + 0.06, y0: y, y1: y + 1, z0: 0, z1: 0.45, interna: true });
        const ordem = ordenar(soltas);
        for (const s of ordem) sprite(ctx, p, s.id, (s.x0 + s.x1) / 2, (s.y0 + s.y1) / 2, s.z0);
        for (const c of comodos(a)) {
          const lim = c === comodos(a)[0] ? [0, meio(a)] : [meio(a), L];
          const cx = c === 'banheiro' ? 7.6 : (lim[0] + lim[1]) / 2, cy = c === 'banheiro' ? 2.0 : 2.5;
          sprite(ctx, p, noite ? 'dentro_lampada_acesa' : 'dentro_lampada_apagada', cx, cy, c === 'banheiro' ? 2.85 : 2.62);
        }
        if (comGrade) caixas(ctx, p, ordem);
      }
      function ceu(ctx, W, Hc, noite) {
        const g = ctx.createLinearGradient(0, 0, 0, Hc);
        if (noite) { g.addColorStop(0, '#0e1638'); g.addColorStop(1, '#3b4c8c'); } else { g.addColorStop(0, '#7cc4fa'); g.addColorStop(0.5, '#b8e2ff'); g.addColorStop(0.8, '#ffe6d6'); g.addColorStop(1, '#ffd0dc'); }
        ctx.fillStyle = g; ctx.fillRect(0, 0, W, Hc);
      }
      // enquadramento (PlantaDaCasa.enquadramento) com ladrilho de 128 px
      const tw = 128, acima = 0.14 * 2 / 4 + H * 0.56, abaixo = (L + F) / 4 + 0.35 * 0.56 + 0.3, entre = F / 2 + 0.35 * 0.56 + H * 0.56 + 0.4;
      const largura = (L + F + 0.28) / 2;
      function casa({ comGrade = false, noite = false, so = null } = {}) {
        const andares = so ? [so] : ['de_cima', 'de_baixo'];
        const W = Math.ceil(largura * tw + 64), Hc = Math.ceil((acima + abaixo + (andares.length - 1) * entre) * tw + 48);
        const cv = document.createElement('canvas'); cv.width = W; cv.height = Hc;
        const ctx = cv.getContext('2d');
        ceu(ctx, W, Hc, noite);
        const ox = W / 2 - (L - F) * tw / 4;
        andares.forEach((a, i) => andar(ctx, proj(tw, ox, 24 + tw * (acima + i * entre)), a, { comGrade, noite }));
        return cv.toDataURL('image/png');
      }
      function pecas() {
        const ids = ['dentro_geladeira', 'dentro_geladeira_aberta', 'dentro_guardaroupa', 'dentro_guardaroupa_aberto', 'dentro_escada', 'dentro_escada_de_cima',
          'dentro_porta_0', 'dentro_porta_1', 'dentro_porta_2', 'dentro_porta_3', 'dentro_porta_4', 'dentro_janela', 'dentro_janela_noite', 'dentro_janela_redonda',
          'dentro_janela_redonda_noite', 'dentro_lampada_acesa', 'dentro_lampada_apagada', 'dentro_parede_interna', 'dentro_patinho', 'dentro_bolo', 'dentro_gato', 'dentro_ursinho', 'dentro_abajur', 'dentro_livros'];
        const W = 1600, cel = 200, col = 8;
        const cv = document.createElement('canvas'); cv.width = W; cv.height = Math.ceil(ids.length / col) * 330;
        const ctx = cv.getContext('2d');
        ctx.fillStyle = '#e9e4f5'; ctx.fillRect(0, 0, cv.width, cv.height);
        ids.forEach((id, i) => {
          const info = m[id]; const im = imgs[id];
          const k = Math.min(1, 180 / info.largura, 280 / info.altura);
          const x = (i % col) * cel + 10, y = Math.floor(i / col) * 330 + 10;
          ctx.drawImage(im, x, y, info.largura * k, info.altura * k);
          ctx.fillStyle = '#e6194b'; ctx.beginPath(); ctx.arc(x + info.apoio.x * k, y + info.apoio.y * k, 4, 0, 7); ctx.fill();
          ctx.fillStyle = '#333'; ctx.font = '13px sans-serif'; ctx.fillText(id.replace('dentro_', ''), x, y + 300);
        });
        return cv.toDataURL('image/png');
      }
      return {
        previa_dentro: casa(),
        previa_dentro_grade: casa({ comGrade: true }),
        previa_dentro_noite: casa({ noite: true }),
        previa_dentro_sala: casa({ comGrade: true, so: 'de_baixo' }),
        previa_dentro_pecas: pecas(),
      };
    }, { m, dados });
    for (const [nome, url] of Object.entries(saidas)) {
      fs.writeFileSync(path.join(SAIDA, nome + '.png'), Buffer.from(url.split(',')[1], 'base64'));
      console.log('ok', path.join('saida', nome + '.png'));
    }
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
