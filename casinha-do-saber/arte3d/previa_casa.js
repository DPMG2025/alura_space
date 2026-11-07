#!/usr/bin/env node
// Prévia da casa de fora (PRD visual 3D, Fase 3): empilha as camadas da cena e os itens de
// app/src/main/assets/arte/ como o app vai empilhar (CenaDaCasa: posições do domínio, tamanho em fração da
// largura, pé no apoio) e grava em saida/ (não versionado):
//   previa_casa.png         a casa completa de dia, com os 40 itens e as crianças (comparar com a maquete 1)
//   previa_casa_etapas.png  vazio, terreno, paredes, telhado, porta
//   previa_casa_noite.png   noite, chuva, neve e as variações da loja (telhado, porta, papel de parede)
//   previa_casa_itens.png   os itens soltos
//
//   npm run previa:casa
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');

/** dominio/ItensDaCasa.ordem: id, x, y (pé), tamanho (fração da largura). Estruturas não têm posição. */
const ITENS = [
  ['cama', 0.27, 0.528, 0.16], ['sofa', 0.44, 0.775, 0.13], ['sol', 0.12, 0.16, 0.13], ['arvore', 0.07, 0.95, 0.17],
  ['geladeira', 0.85, 0.775, 0.075], ['ursinho', 0.31, 0.495, 0.06], ['fogao', 0.76, 0.775, 0.075], ['cachorro', 0.20, 0.95, 0.07],
  ['tv', 0.54, 0.775, 0.075], ['mesa', 0.655, 0.775, 0.09], ['flores', 0.60, 0.99, 0.06], ['banheira', 0.72, 0.528, 0.14],
  ['abajur', 0.36, 0.375, 0.055], ['gato', 0.37, 0.775, 0.06], ['quadro', 0.19, 0.43, 0.07], ['planta', 0.47, 0.528, 0.06],
  ['nuvem', 0.80, 0.15, 0.13], ['livros', 0.52, 0.665, 0.06], ['bola', 0.665, 0.99, 0.055], ['espelho', 0.60, 0.45, 0.06],
  ['bicicleta', 0.60, 0.92, 0.09], ['violao', 0.14, 0.528, 0.06], ['bolo', 0.655, 0.735, 0.05], ['pato', 0.80, 0.93, 0.06],
  ['trem', 0.31, 0.995, 0.07], ['borboleta', 0.20, 0.86, 0.06], ['pipa', 0.95, 0.27, 0.07], ['escorregador', 0.92, 0.86, 0.08],
  ['arcoiris', 0.27, 0.12, 0.12], ['estrela', 0.50, 0.095, 0.07],
];
/** Bichos desenhados ficam maiores (CenaDaCasa: escalaDoBicho), lá fora 1,9 e dentro 1,4. */
const ESCALA_DO_BICHO = { cachorro: 1.9, gato: 1.4 };
const CORES_DOS_COMODOS = { quarto: '#FFE3EC', banheiro: '#D7F3FF', sala: '#FFF1C9', cozinha: '#E2F7D5' };
const CORES_DOS_COMODOS_2 = { quarto: '#E8E0FF', banheiro: '#FFF0F5', sala: '#DDF0FF', cozinha: '#FFE6D5' };

async function main() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto.json'), 'utf8')).imagens;
  const dados = {};
  for (const [id, info] of Object.entries(m)) {
    if (/^(casa_|item_|ceu_|crianca_|irmao_)/.test(id)) dados[id] = 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, info.arquivo)).toString('base64');
  }
  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage();
    await pagina.setContent('<html><body style="margin:0"></body></html>');
    const saidas = await pagina.evaluate(async ({ m, dados, ITENS, ESCALA_DO_BICHO, CORES_DOS_COMODOS, CORES_DOS_COMODOS_2 }) => {
      const imgs = {};
      await Promise.all(Object.entries(dados).map(([id, url]) => new Promise((ok, erro) => {
        const im = new Image(); im.onload = () => { imgs[id] = im; ok(); }; im.onerror = erro; im.src = url;
      })));
      const W = 800, H = 920; // a cena (400 x 460 dp no dobro)
      const tingida = (id, cor) => {
        const im = imgs[id];
        const t = document.createElement('canvas'); t.width = im.width; t.height = im.height;
        const c = t.getContext('2d');
        c.drawImage(im, 0, 0); c.globalCompositeOperation = 'multiply'; c.fillStyle = cor; c.fillRect(0, 0, t.width, t.height);
        c.globalCompositeOperation = 'destination-in'; c.drawImage(im, 0, 0);
        return t;
      };
      function ceu(ctx, noite, alt) {
        const g = ctx.createLinearGradient(0, 0, 0, alt);
        if (noite) { g.addColorStop(0, '#0e1638'); g.addColorStop(0.5, '#22306a'); g.addColorStop(1, '#3b4c8c'); } else { g.addColorStop(0, '#7cc4fa'); g.addColorStop(0.45, '#b8e2ff'); g.addColorStop(0.75, '#ffe6d6'); g.addColorStop(1, '#ffd0dc'); }
        ctx.fillStyle = g; ctx.fillRect(0, 0, ctx.canvas.width, alt);
      }
      function item(ctx, id, x, y, tam, alvoW = W, alvoH = H) {
        const info = m['item_' + id], im = imgs['item_' + id];
        if (!info) return;
        const w = tam * alvoW * 1.08;
        const k = w / info.largura;
        ctx.drawImage(im, x * alvoW - info.apoio.x * k, y * alvoH - info.apoio.y * k, info.largura * k, info.altura * k);
      }
      function pilha(ids, perfil) {
        const camadas = ids.map((id) => ({ id, ...m[id] })).sort((a, b) => a.ordem - b.ordem);
        const cv = document.createElement('canvas'); cv.width = camadas[0].largura; cv.height = camadas[0].altura;
        const ctx = cv.getContext('2d');
        for (const l of camadas) ctx.drawImage(l.tingir ? tingida(l.id, perfil[l.tingir]) : imgs[l.id], 0, 0);
        return { cv, apoio: camadas[0].apoio };
      }
      function criancas(ctx, x, y) {
        const perfil = { pele: '#8D5524', cabelo: '#1C1C1C', roupa: '#FF6FAE' };
        const a = pilha(['crianca_sombra', 'crianca_cabelo_tras_menina_liso', 'crianca_cabelo_brilho_tras_menina_liso', 'crianca_corpo', 'crianca_pes_menina', 'crianca_roupa_menina', 'crianca_roupa_menina_estampa', 'crianca_cabeca', 'crianca_boca_feliz', 'crianca_olhos_aberto_cilios', 'crianca_sobrancelhas_feliz', 'crianca_cabelo_frente_menina_liso', 'crianca_cabelo_brilho_menina_liso', 'crianca_bracos_tchau2', 'crianca_laco'], perfil);
        const b = pilha(['irmao_sombra', 'irmao_corpo', 'irmao_pes_menino', 'irmao_roupa_menino', 'irmao_cabeca', 'irmao_boca_feliz', 'irmao_olhos_aberto', 'irmao_sobrancelhas_feliz', 'irmao_cabelo_menino', 'irmao_cabelo_brilho_menino', 'irmao_bracos_parado'], perfil);
        // CenaDaCasa: a criança tem 0,13 da largura (o quadro 170 x 260 dp); o irmão, na mesma escala
        const k = (0.13 * W) / a.cv.width;
        ctx.drawImage(b.cv, (x + 0.1) * W - b.apoio.x * k, (y - 0.004) * H - b.apoio.y * k, b.cv.width * k, b.cv.height * k);
        ctx.drawImage(a.cv, x * W - a.apoio.x * k, y * H - a.apoio.y * k, a.cv.width * k, a.cv.height * k);
      }
      /** A cena (opções: etapa 0..4+, noite, clima, telhado, porta, papel, itens, criancas, cores dos cômodos). */
      function cena(o) {
        const alt = o.jardim ? 1320 : H;
        const cv = document.createElement('canvas'); cv.width = W; cv.height = alt;
        const ctx = cv.getContext('2d');
        ceu(ctx, o.noite, alt);
        const d = (id) => { if (imgs[id]) ctx.drawImage(imgs[id], 0, 0); };
        if (o.noite) { d('ceu_estrelas_1'); d('ceu_estrelas_2'); }
        if (!o.noite && o.itens) {
          ctx.globalAlpha = 0.9;
          const n1 = imgs.ceu_nuvem_1, n2 = imgs.ceu_nuvem_2;
          ctx.drawImage(n2, W * 0.62, H * 0.2, n2.width * 0.8, n2.height * 0.8);
          ctx.drawImage(n1, W * 0.02, H * 0.27, n1.width * 0.6, n1.height * 0.6);
          ctx.globalAlpha = 1;
        }
        if (o.clima === 1) { const n = imgs.ceu_nuvem_chuva; ctx.drawImage(n, W * 0.55, H * 0.05, n.width * 1.2, n.height * 1.2); ctx.drawImage(n, W * 0.05, H * 0.1, n.width, n.height); }
        // itens do céu atrás da casa
        if (o.itens) for (const [id, x, y, t] of ITENS) if (['sol', 'nuvem', 'arcoiris', 'estrela', 'pipa'].includes(id) && !(o.noite && ['arcoiris', 'pipa'].includes(id))) item(ctx, o.noite && id === 'sol' ? 'sol_noite' : id, x, y, t);
        d(o.etapa >= 1 ? 'casa_etapa_1_terreno' : 'casa_etapa_0_vazio');
        if (o.itens) { d('casa_lago'); d('casa_caminho'); d('casa_cerca'); }
        if (o.etapa >= 3 && o.itens) d('casa_chamine');
        if (o.etapa >= 2) {
          const cores = o.cores || CORES_DOS_COMODOS;
          for (const n of ['quarto', 'banheiro', 'sala', 'cozinha']) ctx.drawImage(tingida('casa_comodo_' + n, cores[n]), 0, 0);
          if (o.papel) d('casa_papel_' + o.papel);
          if (o.itens) d(o.noite ? 'casa_janelas_noite' : 'casa_janelas');
          if (o.etapa >= 4) d('casa_etapa_4_porta_' + (o.porta || 0));
          d('casa_etapa_2_paredes');
          if (o.itens) d('casa_escada');
          if (o.noite) { ctx.fillStyle = 'rgba(16,26,64,0.25)'; ctx.fillRect(0, 0, W, alt); }
        }
        if (o.etapa >= 3) { d('casa_etapa_3_telhado_' + (o.telhado || 0)); if (o.noite) d('casa_janela_do_telhado_noite'); if (o.clima === 2) d('casa_neve_no_telhado'); }
        if (o.itens) for (const [id, x, y, t] of ITENS) {
          if (['sol', 'nuvem', 'arcoiris', 'estrela', 'pipa'].includes(id)) continue;
          if (o.noite && id === 'borboleta') continue;
          item(ctx, id, x, y, t * (ESCALA_DO_BICHO[id] || 1));
        }
        if (o.criancas) criancas(ctx, 0.47, 0.99);
        if (o.clima === 1) d('ceu_chuva');
        if (o.clima === 2) d('ceu_neve');
        if (o.noite && !o.etapa) { ctx.fillStyle = 'rgba(16,26,64,0.3)'; ctx.fillRect(0, 0, W, alt); }
        return cv;
      }
      const png = (cv) => cv.toDataURL('image/png');
      const r = {};
      r.previa_casa = png(cena({ etapa: 4, itens: true, criancas: true, jardim: true }));
      // etapas
      {
        const cv = document.createElement('canvas'); cv.width = W * 5 / 2; cv.height = H / 2;
        const ctx = cv.getContext('2d');
        [0, 1, 2, 3, 4].forEach((e, i) => ctx.drawImage(cena({ etapa: e }), i * W / 2, 0, W / 2, H / 2));
        r.previa_casa_etapas = png(cv);
      }
      {
        const cv = document.createElement('canvas'); cv.width = W * 1.5; cv.height = H;
        const ctx = cv.getContext('2d');
        const vs = [
          cena({ etapa: 4, itens: true, noite: true }), cena({ etapa: 4, itens: true, clima: 1, telhado: 1, porta: 1, papel: 1 }),
          cena({ etapa: 4, itens: true, clima: 2, telhado: 2, porta: 2, papel: 2, cores: CORES_DOS_COMODOS_2 }), cena({ etapa: 4, telhado: 3, porta: 3, papel: 3 }),
          cena({ etapa: 4, telhado: 4, porta: 4 }), cena({ etapa: 4, telhado: 5, noite: true }),
        ];
        vs.forEach((c, i) => ctx.drawImage(c, (i % 3) * W / 2, Math.floor(i / 3) * H / 2, W / 2, H / 2));
        r.previa_casa_noite = png(cv);
      }
      {
        const ids = Object.keys(m).filter((k) => k.startsWith('item_') || /^ceu_nuvem/.test(k)).sort();
        const lado = 220, cols = 8;
        const cv = document.createElement('canvas'); cv.width = lado * cols; cv.height = lado * Math.ceil(ids.length / cols);
        const ctx = cv.getContext('2d');
        ceu(ctx, false, cv.height);
        ctx.font = '600 15px sans-serif'; ctx.fillStyle = '#3b2a5a';
        ids.forEach((id, i) => {
          const im = imgs[id]; const k = Math.min((lado - 30) / im.width, (lado - 30) / im.height);
          const x = (i % cols) * lado, y = Math.floor(i / cols) * lado;
          ctx.drawImage(im, x + (lado - im.width * k) / 2, y + 4 + (lado - 30 - im.height * k) / 2, im.width * k, im.height * k);
          ctx.fillText(id.replace('item_', ''), x + 8, y + lado - 8);
        });
        r.previa_casa_itens = png(cv);
      }
      return r;
    }, { m, dados, ITENS, ESCALA_DO_BICHO, CORES_DOS_COMODOS, CORES_DOS_COMODOS_2 });
    fs.mkdirSync(SAIDA, { recursive: true });
    for (const [nome, url] of Object.entries(saidas)) {
      fs.writeFileSync(path.join(SAIDA, nome + '.png'), Buffer.from(url.split(',')[1], 'base64'));
      console.log('prévia:', path.join('saida', nome + '.png'));
    }
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
