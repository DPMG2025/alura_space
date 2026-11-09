#!/usr/bin/env node
// Prévia dos 7 itens de gatinha fofa na criança (empilha as camadas como o app, com tingimento de pele, cabelo e roupa).
//
//   node previa_gatinha.js [itens|penteados|poses|zoom]
//
//   itens      saida/gatinha_previa.png    os 7 itens lado a lado (cada um numa criança)
//   penteados  saida/gatinha_penteados.png acessórios de cabeça e rosto em todos os penteados (meninas e meninos)
//   poses      saida/gatinha_poses.png     os dois trajes em todas as poses de braços, menina e menino
//   zoom       saida/gatinha_zoom.png      cabeças ampliadas
//
// Usa manifesto.json + manifesto_fase7.json (se existir), como previa_fase7.js.
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const ARTE = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'arte');
const SAIDA = path.join(__dirname, 'saida');

const PELES = ['#FFDBB4', '#F1C27D', '#C68642', '#8D5524'];
const CABELOS = ['#3B2314', '#1C1C1C', '#E6BE8A', '#B55239'];
const ROUPAS = ['#4D96FF', '#FF6FAE', '#3DBE6B', '#FFA62B', '#9B6BFF', '#FF5E5B'];
const ESTILOS = { menina: ['liso', 'cacheado', 'trancas'], menino: ['liso', 'cacheado', 'espetado'] };
const PARTES = { orelhasgato: 'CABELO', bonegato: 'CABELO', lacogrande: 'CABELO', bigodes: 'ROSTO', mascaragata: 'ROSTO' };

function pecaDe(id) {
  if (id.startsWith('crianca_')) return 'crianca';
  const [tipo, nome] = id.split('_');
  return tipo + '_' + nome;
}

function camadas(m, perfil, { humor = 'feliz', bracos = 'parado', piscando = false }, vestir = {}) {
  const g = perfil.menina ? 'menina' : 'menino';
  const estilo = ESTILOS[g][perfil.estilo];
  const olhos = piscando ? 'fechado' : { feliz: 'aberto', torcendo: 'alegre', pensando: 'pensando', surpreso: 'surpreso' }[humor];
  const ids = [
    'crianca_sombra', `crianca_cabelo_tras_${g}_${estilo}`, `crianca_cabelo_brilho_tras_${g}_${estilo}`, 'crianca_corpo', `crianca_pes_${g}`,
    `crianca_roupa_${g}`, `crianca_roupa_${g}_estampa`, 'crianca_cabeca', `crianca_boca_${humor}`,
    `crianca_olhos_${olhos}${perfil.menina ? '_cilios' : ''}`, `crianca_sobrancelhas_${humor}`,
    `crianca_cabelo_frente_${g}_${estilo}`, `crianca_cabelo_brilho_${g}_${estilo}`, `crianca_bracos_${bracos}`,
  ];
  if (perfil.menina && estilo === 'trancas') ids.push('crianca_enfeite_trancas');
  if (perfil.menina && estilo !== 'trancas') ids.push('crianca_laco');
  const tem = (id) => !!m.imagens[id];
  const t = vestir.traje;
  if (t) {
    const corpo = tem(`roupa_${t}`) ? `roupa_${t}` : `roupa_${t}_${g}`;
    ids.push(corpo);
    for (const id of [`${corpo}_estampa`, `roupa_${t}_tras`, `roupa_${t}_pes`, `roupa_${t}_mangas_${bracos}`]) if (tem(id)) ids.push(id);
    if (!vestir.chapeu && tem(`roupa_${t}_cabeca`)) ids.push(`roupa_${t}_cabeca`);
  }
  for (const a of vestir.acessorios || []) {
    ids.push(`acessorio_${a}`);
    if (tem(`acessorio_${a}_frente`)) ids.push(`acessorio_${a}_frente`);
  }
  if (vestir.chapeu) ids.push(`chapeu_${vestir.chapeu}`);
  for (const id of ids) if (!m.imagens[id]) throw new Error('camada fora do manifesto: ' + id);
  const escondidos = [];
  for (const id of ids) for (const gr of m.imagens[id].esconde || []) escondidos.push([pecaDe(id), gr]);
  return ids
    .filter((id) => !escondidos.some(([peca, gr]) => m.imagens[id].grupo === gr && pecaDe(id) !== peca))
    .map((id) => ({ id, ...m.imagens[id] }))
    .sort((a, b) => a.ordem - b.ordem);
}

const P = (menina, pele, cabelo, estilo, roupa) => ({ menina, pele, cabelo, estilo, roupa });
const ITENS = [
  { titulo: 'gatinha (traje)', perfil: P(true, 1, 0, 0, 1), pose: {}, vestir: { traje: 'gatinha' } },
  { titulo: 'lacos (traje)', perfil: P(true, 0, 3, 2, 3), pose: {}, vestir: { traje: 'lacos' } },
  { titulo: 'orelhasgato', perfil: P(true, 2, 1, 1, 4), pose: {}, vestir: { acessorios: ['orelhasgato'] } },
  { titulo: 'bonegato', perfil: P(false, 1, 0, 0, 0), pose: {}, vestir: { acessorios: ['bonegato'] } },
  { titulo: 'lacogrande', perfil: P(true, 0, 2, 0, 5), pose: {}, vestir: { acessorios: ['lacogrande'] } },
  { titulo: 'bigodes', perfil: P(false, 3, 1, 1, 2), pose: {}, vestir: { acessorios: ['bigodes'] } },
  { titulo: 'mascaragata', perfil: P(true, 2, 0, 0, 1), pose: {}, vestir: { acessorios: ['mascaragata'] } },
];
const FOLHAS = {
  itens: { cenas: ITENS, col: 7 },
  penteados: {
    col: 6,
    cenas: Object.keys(PARTES).flatMap((a) => [
      P(true, 0, 0, 0, 1), P(true, 1, 3, 1, 4), P(true, 3, 1, 2, 3), P(false, 0, 2, 0, 0), P(false, 2, 0, 1, 2), P(false, 3, 3, 2, 5),
    ].map((perfil, i) => ({ titulo: `${a} ${perfil.menina ? 'menina' : 'menino'} ${ESTILOS[perfil.menina ? 'menina' : 'menino'][perfil.estilo]}`, perfil, pose: {}, vestir: { acessorios: [a] }, corte: [40, 20, 260, 250] }))),
  },
  poses: {
    col: 7,
    cenas: ['gatinha', 'lacos'].flatMap((t, k) => [
      ['parado', 'parado', 'feliz'], ['tchau1', 'tchau', 'feliz'], ['tchau2', 'tchau', 'feliz'], ['tchau3', 'tchau', 'feliz'], ['tchau4', 'tchau', 'feliz'], ['torcendo', 'torcendo', 'torcendo'], ['pensando', 'pensando', 'pensando'],
    ].map(([nome, , humor], i) => ({
      titulo: `${t} ${nome}`, perfil: P(i % 2 === k, i % 4, (i + k) % 4, i % 3, (i + 2) % 6), pose: { bracos: nome, humor },
      vestir: { traje: t, acessorios: k === 0 ? ['orelhasgato', 'bigodes'] : ['lacogrande'] },
    }))),
  },
  zoom: {
    col: 5, escala: 2.2,
    cenas: [['orelhasgato', true], ['bonegato', false], ['lacogrande', true], ['bigodes', false], ['mascaragata', true]].map(([a, menina], i) => ({
      titulo: a, perfil: P(menina, (i + 1) % 4, i % 4, i % 3, i), pose: {}, vestir: { acessorios: [a] }, corte: [40, 20, 260, 260],
    })),
  },
};
FOLHAS.corpo = {
  col: 4, escala: 1.6,
  cenas: [['gatinha', true, 1], ['gatinha', false, 2], ['lacos', true, 3], ['lacos', false, 0]].map(([t, menina, c]) => ({
    titulo: t + (menina ? ' menina' : ' menino'), perfil: P(menina, c, c, c % 3, c), pose: {}, vestir: { traje: t }, corte: [10, 190, 320, 320],
  })),
};
const NOMES = { corpo: 'gatinha_corpo', itens: 'gatinha_previa', penteados: 'gatinha_penteados', poses: 'gatinha_poses', zoom: 'gatinha_zoom' };

async function main() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto.json'), 'utf8'));
  const f7 = path.join(ARTE, 'manifesto_fase7.json');
  if (fs.existsSync(f7)) Object.assign(m.imagens, JSON.parse(fs.readFileSync(f7, 'utf8')).imagens);
  const pedidas = process.argv.slice(2);
  const folhas = {};
  const usados = new Set();
  for (const [nome, def] of Object.entries(FOLHAS)) {
    if (pedidas.length ? !pedidas.includes(nome) : (nome === 'zoom' || nome === 'corpo')) continue;
    folhas[nome] = {
      col: def.col, escala: def.escala || 1,
      cenas: def.cenas.map((c) => {
        const cs = camadas(m, c.perfil, c.pose, c.vestir);
        for (const l of cs) usados.add(l.arquivo);
        return { titulo: c.titulo, corte: c.corte, cores: { pele: PELES[c.perfil.pele], cabelo: CABELOS[c.perfil.cabelo], roupa: ROUPAS[c.perfil.roupa] }, camadas: cs };
      }),
    };
  }
  const dados = {};
  for (const f of usados) dados[f] = 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, f)).toString('base64');
  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage();
    await pagina.setContent('<html><body style="margin:0"></body></html>');
    for (const [nome, folha] of Object.entries(folhas)) {
      const png = await pagina.evaluate(async ({ folha, dados }) => {
        const imgs = {};
        await Promise.all(Object.entries(dados).map(([f, url]) => new Promise((ok, erro) => {
          const im = new Image(); im.onload = () => { imgs[f] = im; ok(); }; im.onerror = erro; im.src = url;
        })));
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
        const { cenas, col: c0, escala } = folha;
        const col = Math.min(cenas.length, c0), lin = Math.ceil(cenas.length / col);
        const celL = Math.round((cenas[0].corte ? cenas[0].corte[2] : 340) * escala) + 20, celA = Math.round((cenas[0].corte ? cenas[0].corte[3] : 520) * escala) + 50;
        const W = col * celL, H = lin * celA;
        const cv = document.createElement('canvas');
        cv.width = W; cv.height = H;
        const ctx = cv.getContext('2d');
        const ceu = ctx.createLinearGradient(0, 0, 0, H);
        ceu.addColorStop(0, '#8fd0ff'); ceu.addColorStop(0.6, '#ffe2cf'); ceu.addColorStop(1, '#ffc9d6');
        ctx.fillStyle = ceu; ctx.fillRect(0, 0, W, H);
        ctx.font = '600 18px sans-serif';
        cenas.forEach((c, i) => {
          const x0 = (i % col) * celL, y0 = Math.floor(i / col) * celA;
          const a = empilhar(c.camadas, c.cores);
          const [sx, sy, sw, sh] = c.corte || [0, 0, a.width, a.height];
          ctx.drawImage(a, sx, sy, sw, sh, x0 + 10, y0 + 40, sw * escala, sh * escala);
          ctx.fillStyle = '#3b2a5a'; ctx.fillText(c.titulo, x0 + 12, y0 + 26);
        });
        return cv.toDataURL('image/png');
      }, { folha, dados });
      fs.mkdirSync(SAIDA, { recursive: true });
      const destino = path.join(SAIDA, NOMES[nome] + '.png');
      fs.writeFileSync(destino, Buffer.from(png.split(',')[1], 'base64'));
      console.log('prévia:', path.relative(process.cwd(), destino));
    }
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
