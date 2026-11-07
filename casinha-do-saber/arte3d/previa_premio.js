#!/usr/bin/env node
// Prévia da Fase 5 (não versionada): monta a tela do Prêmio como a maquete 3_premio só com as imagens de
// app/src/main/assets/arte/ (presente abrindo, clarão, pódio, estrelas, moeda, confete, faíscas e os irmãos
// em camadas, tingidos como em previa.js), ao lado da maquete, e uma tela do Quase com as peças gentis.
//
//   node previa_premio.js      grava saida/previa_premio.png
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');
const MAQUETE = path.resolve(__dirname, '..', 'docs', 'visual-3d', 'maquetes', '3_premio.png');

function manifesto() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto.json'), 'utf8')).imagens;
  const f5 = path.join(ARTE, 'manifesto_fase5.json');
  if (fs.existsSync(f5)) Object.assign(m, JSON.parse(fs.readFileSync(f5, 'utf8')).imagens);
  return m;
}

// Camadas como em previa.js (camadasDaCrianca / camadasDoIrmao), para a pose de comemorar.
const crianca = ['crianca_sombra', 'crianca_cabelo_tras_menina_liso', 'crianca_cabelo_brilho_tras_menina_liso', 'crianca_corpo', 'crianca_pes_menina',
  'crianca_roupa_menina', 'crianca_roupa_menina_estampa', 'crianca_cabeca', 'crianca_boca_torcendo', 'crianca_olhos_alegre_cilios',
  'crianca_sobrancelhas_torcendo', 'crianca_cabelo_frente_menina_liso', 'crianca_cabelo_brilho_menina_liso', 'crianca_bracos_torcendo', 'crianca_laco'];
const irmao = ['irmao_sombra', 'irmao_corpo', 'irmao_pes_menino', 'irmao_roupa_menino', 'irmao_cabeca', 'irmao_boca_torcendo', 'irmao_olhos_alegre',
  'irmao_sobrancelhas_torcendo', 'irmao_cabelo_menino', 'irmao_cabelo_brilho_menino', 'irmao_bracos_torcendo'];
const CORES = { pele: '#C68642', cabelo: '#1C1C1C', roupa: '#FF6FAE' };

async function main() {
  const m = manifesto();
  const usar = new Set([...crianca, ...irmao, 'estrela3d', 'moeda', 'patinho', 'icone_casa',
    ...Object.keys(m).filter((k) => k.startsWith('premio_') || k.startsWith('quase_'))]);
  const dados = {};
  for (const id of usar) {
    if (!m[id]) throw new Error('fora do manifesto: ' + id);
    dados[id] = { ...m[id], url: 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, m[id].arquivo)).toString('base64') };
  }
  const fonte = (f) => 'data:font/woff2;base64,' + fs.readFileSync(path.join(__dirname, 'fonts', f)).toString('base64');
  const maquete = 'data:image/png;base64,' + fs.readFileSync(MAQUETE).toString('base64');

  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage();
    await pagina.setContent('<html><body style="margin:0"></body></html>');
    const png = await pagina.evaluate(async ({ dados, crianca, irmao, CORES, baloo, fredoka, maquete }) => {
      for (const [nome, url, peso] of [['Baloo 2', baloo, 800], ['Fredoka', fredoka, 700]]) {
        const ff = new FontFace(nome, `url(${url})`, { weight: String(peso) });
        await ff.load(); document.fonts.add(ff);
      }
      const img = {};
      const carregar = (id, url) => new Promise((ok, erro) => { const im = new Image(); im.onload = () => { img[id] = im; ok(); }; im.onerror = erro; im.src = url; });
      await Promise.all(Object.entries(dados).map(([id, d]) => carregar(id, d.url)));
      await carregar('__maquete', maquete);

      const E = 2; // px por dp (as imagens já estão no dobro)
      const W = 393 * E, H = 852 * E;
      function empilhar(ids) {
        const cv = document.createElement('canvas');
        cv.width = dados[ids[0]].largura; cv.height = dados[ids[0]].altura;
        const ctx = cv.getContext('2d');
        for (const id of ids.slice().sort((a, b) => dados[a].ordem - dados[b].ordem)) {
          const d = dados[id], im = img[id];
          if (!d.tingir) { ctx.drawImage(im, 0, 0); continue; }
          const t = document.createElement('canvas'); t.width = cv.width; t.height = cv.height;
          const tc = t.getContext('2d');
          tc.drawImage(im, 0, 0);
          tc.globalCompositeOperation = 'multiply'; tc.fillStyle = CORES[d.tingir]; tc.fillRect(0, 0, t.width, t.height);
          tc.globalCompositeOperation = 'destination-in'; tc.drawImage(im, 0, 0);
          ctx.drawImage(t, 0, 0);
        }
        return cv;
      }
      /** Desenha [id] com o ponto [ref] ('apoio', 'centro' ou âncora) em (x, y) dp, na escala [s]. */
      function por(ctx, id, x, y, { s = 1, giro = 0, ref = 'centro', alfa = 1, fonte = null } = {}) {
        const d = dados[id] || fonte.d, im = fonte ? fonte.im : img[id];
        const p = ref === 'apoio' ? d.apoio : ref === 'centro' ? (d.ancoras.centro || { x: d.largura / 2, y: d.altura / 2 }) : d.ancoras[ref];
        ctx.save(); ctx.globalAlpha = alfa; ctx.translate(x * E, y * E); ctx.rotate(giro); ctx.scale(s, s);
        ctx.drawImage(im, -p.x, -p.y); ctx.restore();
      }
      function fundo(ctx, cx, cy) {
        const g = ctx.createLinearGradient(0, 0, 0, H);
        g.addColorStop(0, '#8a6cff'); g.addColorStop(0.4, '#b783ff'); g.addColorStop(0.85, '#ff9fcf'); g.addColorStop(1, '#ffc2dc');
        ctx.fillStyle = g; ctx.fillRect(0, 0, W, H);
        ctx.save(); ctx.translate(cx * E, cy * E);
        for (let i = 0; i < 16; i++) {
          ctx.rotate(Math.PI * 2 / 16);
          ctx.fillStyle = 'rgba(255,255,255,0.16)';
          ctx.beginPath(); ctx.moveTo(0, 0); ctx.arc(0, 0, H, 0, Math.PI * 2 * 9 / 360); ctx.closePath(); ctx.fill();
        }
        ctx.restore();
        const r = ctx.createRadialGradient(cx * E, cy * E, 0, cx * E, cy * E, 0.46 * Math.hypot(W, H) / 1.4);
        r.addColorStop(0, 'rgba(255,247,200,0.95)'); r.addColorStop(0.2, 'rgba(255,214,120,0.55)'); r.addColorStop(1, 'rgba(255,190,220,0)');
        ctx.fillStyle = r; ctx.fillRect(0, 0, W, H);
      }
      function titulo(ctx, t, y, tam) {
        ctx.font = `800 ${tam * E}px "Baloo 2"`; ctx.textAlign = 'center'; ctx.lineJoin = 'round';
        ctx.fillStyle = '#5b2fd6'; ctx.fillText(t, W / 2, (y + 4) * E);
        ctx.lineWidth = 7 * E; ctx.strokeStyle = '#6a3fe0'; ctx.strokeText(t, W / 2, y * E);
        ctx.fillStyle = '#ffffff'; ctx.fillText(t, W / 2, y * E);
      }
      function pilula(ctx, x, y, l, a) {
        ctx.fillStyle = 'rgba(80,40,160,0.25)'; ctx.beginPath(); ctx.roundRect((x - l / 2) * E, (y - a / 2 + 5) * E, l * E, a * E, a * E / 2); ctx.fill();
        ctx.fillStyle = '#ffffff'; ctx.beginPath(); ctx.roundRect((x - l / 2) * E, (y - a / 2) * E, l * E, a * E, a * E / 2); ctx.fill();
      }
      function botao(ctx, texto) {
        const x = 40, y = 852 - 34 - 82, l = 313, a = 82;
        ctx.fillStyle = '#1c9552'; ctx.beginPath(); ctx.roundRect(x * E, (y + 8) * E, l * E, a * E, 30 * E); ctx.fill();
        const g = ctx.createLinearGradient(0, y * E, 0, (y + a) * E); g.addColorStop(0, '#5be38f'); g.addColorStop(1, '#2fc46c');
        ctx.fillStyle = g; ctx.beginPath(); ctx.roundRect(x * E, y * E, l * E, a * E, 30 * E); ctx.fill();
        ctx.lineWidth = 4 * E; ctx.strokeStyle = '#ffffff'; ctx.stroke();
        ctx.font = `800 ${30 * E}px "Baloo 2"`; ctx.textAlign = 'center'; ctx.lineJoin = 'round';
        ctx.lineWidth = 5 * E; ctx.strokeStyle = '#1c7a44'; ctx.strokeText(texto, (x + l / 2) * E, (y + a / 2 + 11) * E);
        ctx.fillStyle = '#fff'; ctx.fillText(texto, (x + l / 2) * E, (y + a / 2 + 11) * E);
      }
      let semente = 11;
      const rnd = () => { semente = (semente * 16807) % 2147483647; return (semente - 1) / 2147483646; };
      const confetes = Object.keys(dados).filter((k) => k.startsWith('premio_confete_'));
      function confete(ctx, n, y0, y1, evitar = () => false) {
        for (let i = 0; i < n; i++) {
          const x = rnd() * 393, y = y0 + rnd() * (y1 - y0);
          const id = confetes[Math.floor(rnd() * confetes.length)];
          const s = 0.45 + rnd() * 0.45, giro = rnd() * 6.28;
          if (evitar(x, y)) continue;
          por(ctx, id, x, y, { s, giro });
        }
      }

      // ---------------- prêmio
      const cv = document.createElement('canvas'); cv.width = W; cv.height = H;
      const ctx = cv.getContext('2d');
      fundo(ctx, 196, 420);
      confete(ctx, 60, 20, 640, (x, y) => (y > 330 && y < 600 && x > 90 && x < 300));
      titulo(ctx, 'Parabéns, Ana!', 92, 46);
      por(ctx, 'estrela3d', 104, 168, { s: 0.74, giro: -0.28 });
      por(ctx, 'estrela3d', 289, 168, { s: 0.74, giro: 0.28 });
      por(ctx, 'estrela3d', 196, 146, { s: 0.92 });
      pilula(ctx, 196, 244, 120, 50);
      por(ctx, 'moeda', 162, 244, { s: 0.48 });
      ctx.font = `800 ${28 * E}px "Baloo 2"`; ctx.textAlign = 'left'; ctx.fillStyle = '#3b2a8a'; ctx.fillText('+5', 186 * E, 254 * E);
      // presente aberto com o clarão, e o prêmio saindo
      por(ctx, 'premio_clarao', 196, 420, { s: 1.2, alfa: 0.9 });
      por(ctx, 'premio_presente_abrindo_3', 196, 470, { s: 1.3, ref: 'boca' });
      por(ctx, 'patinho', 180, 330, { s: 0.85, giro: -0.12 });
      for (const [id, x, y, s] of [['premio_faisca_branca', 120, 330, 0.7], ['premio_faisca_dourada', 268, 300, 0.6], ['premio_faisca_branca', 250, 420, 0.5], ['premio_faisca_dourada', 110, 440, 0.55]]) por(ctx, id, x, y, { s });
      // pódio e os irmãos comemorando
      const podio = dados.premio_podio;
      const px = 196, py = 676, ps = 1.0;
      por(ctx, 'premio_podio', px, py, { s: ps, ref: 'centro' });
      const ancora = (n) => ({ x: px + (podio.ancoras[n].x - podio.ancoras.centro.x) * ps / E, y: py + (podio.ancoras[n].y - podio.ancoras.centro.y) * ps / E });
      const a = empilhar(crianca), b = empilhar(irmao);
      const pe = ancora('esquerda'), pd = ancora('direita');
      por(ctx, null, pe.x - 6, pe.y, { ref: 'apoio', s: 0.8, fonte: { im: a, d: dados.crianca_sombra } });
      por(ctx, null, pd.x + 6, pd.y, { ref: 'apoio', s: 0.8, fonte: { im: b, d: dados.irmao_sombra } });
      por(ctx, 'premio_confete_estrela_verde', 290, 650, { s: 0.7 });
      por(ctx, 'premio_confete_estrela_rosa', 200, 640, { s: 0.6 });
      confete(ctx, 8, 560, 720, (x) => x > 70 && x < 330);
      botao(ctx, 'Pôr na casinha');

      // ---------------- quase
      const cq = document.createElement('canvas'); cq.width = W; cq.height = H;
      const q = cq.getContext('2d');
      const gq = q.createLinearGradient(0, 0, 0, H); gq.addColorStop(0, '#7fb7ff'); gq.addColorStop(0.55, '#cfe3ff'); gq.addColorStop(1, '#ffe6ef');
      q.fillStyle = gq; q.fillRect(0, 0, W, H);
      titulo(q, 'Quase!', 96, 52);
      por(q, 'quase_estrela_dormindo', 196, 210, { s: 0.85 });
      // a semente que cresce: os quatro estágios (o app mostra um por vez, crescendo)
      [1, 2, 3, 4].forEach((e, i) => por(q, `quase_semente_${e}`, 62 + i * 90, 420, { ref: 'apoio', s: 0.62 }));
      por(q, 'premio_presente_fechado', 196, 590, { ref: 'apoio', s: 0.8 });
      [1, 2, 3, 5].forEach((n, i) => por(q, `premio_moedas_${n}`, 70 + i * 84, 680, { ref: 'apoio', s: 0.8 }));
      por(q, 'premio_coroa', 300, 520, { s: 0.6, giro: 0.15 });
      por(q, 'premio_trofeu', 90, 590, { ref: 'apoio', s: 0.55 });
      botao(q, 'De novo');

      // ---------------- lado a lado: maquete | prévia | quase
      const out = document.createElement('canvas'); out.width = W * 3 + 40; out.height = H;
      const o = out.getContext('2d');
      o.fillStyle = '#222'; o.fillRect(0, 0, out.width, out.height);
      o.drawImage(img.__maquete, 0, 0, W, H);
      o.drawImage(cv, W + 20, 0);
      o.drawImage(cq, 2 * W + 40, 0);
      return out.toDataURL('image/png');
    }, { dados, crianca, irmao, CORES, baloo: fonte('Baloo2-800-latin.woff2'), fredoka: fonte('Fredoka-700-latin.woff2'), maquete });
    fs.mkdirSync(SAIDA, { recursive: true });
    const destino = path.join(SAIDA, 'previa_premio.png');
    fs.writeFileSync(destino, Buffer.from(png.split(',')[1], 'base64'));
    console.log('prévia:', path.relative(process.cwd(), destino));
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
