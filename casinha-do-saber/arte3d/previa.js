#!/usr/bin/env node
// Prévia das camadas dos personagens (PRD visual 3D, seção 3.2): empilha as camadas de
// app/src/main/assets/arte/ na ordem do manifesto, tinge pele, cabelo e roupa por multiplicação num
// canvas (como o app fará com ColorFilter Modulate) e grava saida/previa_personagens.png (não versionado).
//
//   npm run previa
//
// camadasDe() abaixo é a referência de como o app escolhe as camadas para um perfil, pose e humor.
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');

// Paletas do app (dominio/EstadoJogo.kt, Aparencia)
const PELES = ['#FFDBB4', '#F1C27D', '#C68642', '#8D5524'];
const CABELOS = ['#3B2314', '#1C1C1C', '#E6BE8A', '#B55239'];
const ROUPAS = ['#4D96FF', '#FF6FAE', '#3DBE6B', '#FFA62B', '#9B6BFF', '#FF5E5B'];
const ESTILOS = { menina: ['liso', 'cacheado', 'trancas'], menino: ['liso', 'cacheado', 'espetado'] };

/** Camadas da criança para um perfil, na ordem do manifesto. */
function camadasDaCrianca(m, perfil, { humor = 'feliz', bracos = 'parado', piscando = false }) {
  const g = perfil.menina ? 'menina' : 'menino';
  const estilo = ESTILOS[g][perfil.estilo];
  const olhos = piscando ? 'fechado' : { feliz: 'aberto', torcendo: 'alegre', pensando: 'pensando', surpreso: 'surpreso' }[humor];
  const ids = [
    'crianca_sombra', `crianca_cabelo_tras_${g}_${estilo}`, 'crianca_corpo', `crianca_pes_${g}`,
    `crianca_roupa_${g}`, `crianca_roupa_${g}_estampa`, 'crianca_cabeca', `crianca_boca_${humor}`,
    `crianca_olhos_${olhos}${perfil.menina ? '_cilios' : ''}`, `crianca_sobrancelhas_${humor}`,
    `crianca_cabelo_frente_${g}_${estilo}`, `crianca_cabelo_brilho_${g}_${estilo}`, `crianca_bracos_${bracos}`,
  ];
  if (perfil.menina && estilo === 'trancas') ids.push('crianca_enfeite_trancas');
  if (perfil.menina && estilo !== 'trancas') ids.push('crianca_laco');
  return ordenar(m, ids);
}

/** Camadas do irmãozinho (mesma pele e cabelo da criança; gênero do irmão). */
function camadasDoIrmao(m, irmaMenina, { humor = 'feliz', bracos = 'parado', piscando = false }) {
  const g = irmaMenina ? 'menina' : 'menino';
  const olhos = piscando ? 'fechado' : { feliz: 'aberto', torcendo: 'alegre', pensando: 'pensando', surpreso: 'surpreso' }[humor];
  const ids = [
    'irmao_sombra', 'irmao_corpo', `irmao_pes_${g}`, `irmao_roupa_${g}`, 'irmao_cabeca', `irmao_boca_${humor}`,
    `irmao_olhos_${olhos}${irmaMenina ? '_cilios' : ''}`, `irmao_sobrancelhas_${humor}`, `irmao_cabelo_${g}`,
    `irmao_cabelo_brilho_${g}`, `irmao_bracos_${bracos}`,
  ];
  if (irmaMenina) ids.push('irmao_laco');
  return ordenar(m, ids);
}

function ordenar(m, ids) {
  for (const id of ids) if (!m.imagens[id]) throw new Error('camada fora do manifesto: ' + id);
  return ids.map((id) => ({ id, ...m.imagens[id] })).sort((a, b) => a.ordem - b.ordem);
}

const COMBOS = [
  {
    titulo: 'menina, pele escura, cabelo preto, coques; tchau',
    perfil: { menina: true, pele: 3, cabelo: 1, estilo: 0, roupa: 1 }, crianca: { humor: 'feliz', bracos: 'tchau2' },
    irmao: { menina: false, humor: 'feliz', bracos: 'parado' },
  },
  {
    titulo: 'menino, pele clara, loiro, espetadinho; torcendo',
    perfil: { menina: false, pele: 0, cabelo: 2, estilo: 2, roupa: 0 }, crianca: { humor: 'torcendo', bracos: 'torcendo' },
    irmao: { menina: false, humor: 'torcendo', bracos: 'torcendo' },
  },
  {
    titulo: 'menina, pele média, ruiva, tranças; pensando',
    perfil: { menina: true, pele: 1, cabelo: 3, estilo: 2, roupa: 4 }, crianca: { humor: 'pensando', bracos: 'pensando' },
    irmao: { menina: true, humor: 'surpreso', bracos: 'tchau3' },
  },
  {
    titulo: 'menino, pele morena, castanho, cacheado; surpreso',
    perfil: { menina: false, pele: 2, cabelo: 0, estilo: 1, roupa: 2 }, crianca: { humor: 'surpreso', bracos: 'parado' },
    irmao: { menina: true, humor: 'feliz', bracos: 'tchau1', piscando: true },
  },
];

async function main() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto.json'), 'utf8'));
  const usados = new Set();
  const cenas = COMBOS.map((c) => {
    const cores = { pele: PELES[c.perfil.pele], cabelo: CABELOS[c.perfil.cabelo], roupa: ROUPAS[c.perfil.roupa] };
    const crianca = camadasDaCrianca(m, c.perfil, c.crianca);
    const irmao = camadasDoIrmao(m, c.irmao.menina, c.irmao);
    for (const l of [...crianca, ...irmao]) usados.add(l.arquivo);
    return { titulo: c.titulo, cores, crianca, irmao };
  });
  // extra: menina e menino com os outros estilos, para conferir todos os cabelos
  const extras = [];
  for (const [menina, estilo, cabelo, pele, roupa] of [[true, 1, 0, 2, 3], [false, 0, 1, 1, 5], [true, 0, 2, 0, 2], [false, 1, 3, 3, 4]]) {
    const perfil = { menina, pele, cabelo, estilo, roupa };
    const crianca = camadasDaCrianca(m, perfil, { humor: 'feliz', bracos: 'parado' });
    for (const l of crianca) usados.add(l.arquivo);
    extras.push({ cores: { pele: PELES[pele], cabelo: CABELOS[cabelo], roupa: ROUPAS[roupa] }, crianca });
  }
  const dados = {};
  for (const f of usados) dados[f] = 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, f)).toString('base64');

  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage();
    await pagina.setContent('<html><body style="margin:0"></body></html>');
    const png = await pagina.evaluate(async ({ cenas, extras, dados }) => {
      const imgs = {};
      await Promise.all(Object.entries(dados).map(([f, url]) => new Promise((ok, erro) => {
        const im = new Image(); im.onload = () => { imgs[f] = im; ok(); }; im.onerror = erro; im.src = url;
      })));
      /** Empilha as camadas num canvas do tamanho da camada, tingindo as marcadas. */
      function empilhar(camadas, cores) {
        const cv = document.createElement('canvas');
        cv.width = camadas[0].largura; cv.height = camadas[0].altura;
        const ctx = cv.getContext('2d');
        for (const l of camadas) {
          const im = imgs[l.arquivo];
          if (!l.tingir) { ctx.drawImage(im, 0, 0); continue; }
          const t = document.createElement('canvas');
          t.width = cv.width; t.height = cv.height;
          const tc = t.getContext('2d');
          tc.drawImage(im, 0, 0);
          tc.globalCompositeOperation = 'multiply';
          tc.fillStyle = cores[l.tingir];
          tc.fillRect(0, 0, t.width, t.height);
          tc.globalCompositeOperation = 'destination-in';
          tc.drawImage(im, 0, 0);
          ctx.drawImage(t, 0, 0);
        }
        return cv;
      }
      const celL = 560, celA = 600;
      const W = celL * 2, H = celA * 2 + 560;
      const cv = document.createElement('canvas');
      cv.width = W; cv.height = H;
      const ctx = cv.getContext('2d');
      const ceu = ctx.createLinearGradient(0, 0, 0, H);
      ceu.addColorStop(0, '#8fd0ff'); ceu.addColorStop(0.6, '#ffe2cf'); ceu.addColorStop(1, '#ffc9d6');
      ctx.fillStyle = ceu; ctx.fillRect(0, 0, W, H);
      ctx.font = '600 20px sans-serif';
      cenas.forEach((c, i) => {
        const x0 = (i % 2) * celL, y0 = Math.floor(i / 2) * celA;
        ctx.fillStyle = 'rgba(122,190,96,0.55)';
        ctx.beginPath(); ctx.ellipse(x0 + celL / 2, y0 + celA - 70, celL * 0.42, 46, 0, 0, Math.PI * 2); ctx.fill();
        const a = empilhar(c.crianca, c.cores), b = empilhar(c.irmao, c.cores);
        const apA = c.crianca[0].apoio, apB = c.irmao[0].apoio;
        const chao = y0 + celA - 70;
        ctx.drawImage(a, x0 + 200 - apA.x, chao - apA.y);
        ctx.drawImage(b, x0 + 400 - apB.x, chao - apB.y);
        ctx.fillStyle = '#3b2a5a'; ctx.fillText(c.titulo, x0 + 16, y0 + 30);
      });
      extras.forEach((c, i) => {
        const a = empilhar(c.crianca, c.cores);
        const ap = c.crianca[0].apoio;
        ctx.drawImage(a, i * (W / 4) + W / 8 - ap.x, celA * 2 + 520 - ap.y);
      });
      return cv.toDataURL('image/png');
    }, { cenas, extras, dados });
    fs.mkdirSync(SAIDA, { recursive: true });
    const destino = path.join(SAIDA, 'previa_personagens.png');
    fs.writeFileSync(destino, Buffer.from(png.split(',')[1], 'base64'));
    console.log('prévia:', path.relative(process.cwd(), destino));
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
