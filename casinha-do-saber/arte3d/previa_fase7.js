#!/usr/bin/env node
// Prévia da Fase 7 (guarda-roupa): empilha as camadas da criança (manifesto.json) com as roupas, acessórios e
// chapéus (já no manifesto.json; manifesto_fase7.json se houver), tingindo pele, cabelo e roupa como o app, e grava em saida/ (não versionado):
//   previa_trajes.png      os 16 trajes (meninas e meninos, peles, cabelos e cores de roupa variados)
//   previa_acessorios.png  os 18 acessórios e os 4 chapéus
//   previa_combos.png      combinações com poses (tchau, torcendo, pensando): mangas e objetos na mão
//
//   node previa_fase7.js
//
// camadasComGuardaRoupa() abaixo é a referência de escolha e ocultação de camadas para o app.
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
const TRAJES = ['listras', 'pijama', 'heroi', 'abelha', 'fada', 'astronauta', 'vestido', 'macacao', 'futebol', 'bailarina', 'chuva', 'medico', 'chef', 'pirata', 'realeza', 'dinossauro'];
const ACESSORIOS = {
  ROSTO: ['oculos', 'oculosdesol', 'mascara'], PESCOCO: ['colar', 'cachecol', 'gravata'], COSTAS: ['mochila', 'asas', 'capa'],
  MAO: ['varinha', 'balao', 'ursinho'], PES: ['tenis', 'botas', 'pantufas'], CABELO: ['laco', 'tiara', 'presilha'],
};
const CHAPEUS = ['coroa', 'bone', 'cartola', 'flor'];

/** A peça a que a camada pertence: as camadas de uma peça nunca escondem umas às outras. */
function pecaDe(id) {
  if (id.startsWith('crianca_')) return 'crianca';
  const [tipo, nome] = id.split('_');
  return tipo + '_' + nome;
}

/**
 * Camadas da criança com o guarda-roupa, na ordem do manifesto.
 * vestir = { traje, chapeu, ROSTO, PESCOCO, COSTAS, MAO, PES, CABELO } (ids da Loja; chapéu pelo nome da camada).
 */
function camadasComGuardaRoupa(m, perfil, { humor = 'feliz', bracos = 'parado', piscando = false }, vestir = {}) {
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
    if (tem(`roupa_${t}_tapaolho`)) ids.push(`roupa_${t}_tapaolho`);
  }
  for (const parte of Object.keys(ACESSORIOS)) {
    const a = vestir[parte];
    if (!a) continue;
    if (parte === 'MAO') { ids.push(`acessorio_${a}_${bracos === 'torcendo' ? 'cima' : 'baixo'}`); continue; }
    ids.push(`acessorio_${a}`);
    if (tem(`acessorio_${a}_frente`)) ids.push(`acessorio_${a}_frente`);
  }
  if (vestir.chapeu) ids.push(`chapeu_${vestir.chapeu}`);
  for (const id of ids) if (!m.imagens[id]) throw new Error('camada fora do manifesto: ' + id);
  // esconde: cada camada esconde os grupos listados nas camadas das OUTRAS peças
  const escondidos = [];
  for (const id of ids) for (const gr of m.imagens[id].esconde || []) escondidos.push([pecaDe(id), gr]);
  return ids
    .filter((id) => !escondidos.some(([peca, gr]) => m.imagens[id].grupo === gr && pecaDe(id) !== peca))
    .map((id) => ({ id, ...m.imagens[id] }))
    .sort((a, b) => a.ordem - b.ordem);
}

const P = (menina, pele, cabelo, estilo, roupa) => ({ menina, pele, cabelo, estilo, roupa });
const FOLHAS = {
  trajes: TRAJES.map((t, i) => ({
    titulo: t,
    perfil: P(i % 2 === 0, i % 4, (i * 3) % 4, Math.floor(i / 2) % 3, i % 6),
    pose: {}, vestir: { traje: t },
  })),
  acessorios: [
    ...Object.entries(ACESSORIOS).flatMap(([parte, lista], k) => lista.map((a, i) => ({
      titulo: a,
      perfil: P((i + k) % 2 === 1, (i + k) % 4, (i + 2 * k) % 4, (i + k) % 3, (i + k) % 6),
      pose: {}, vestir: { [parte]: a },
    }))),
    ...CHAPEUS.map((c, i) => ({ titulo: 'chapeu ' + c, perfil: P(i % 2 === 0, (i + 1) % 4, i % 4, i % 3, (i + 2) % 6), pose: {}, vestir: { chapeu: c } })),
  ],
  combos: [
    { titulo: 'astronauta, tchau', perfil: P(true, 3, 1, 1, 0), pose: { bracos: 'tchau2' }, vestir: { traje: 'astronauta', MAO: 'balao', PES: 'tenis' } },
    { titulo: 'dino torcendo, balão', perfil: P(false, 0, 2, 2, 1), pose: { humor: 'torcendo', bracos: 'torcendo' }, vestir: { traje: 'dinossauro', MAO: 'balao' } },
    { titulo: 'pirata, cartola, pensando', perfil: P(false, 2, 0, 0, 2), pose: { humor: 'pensando', bracos: 'pensando' }, vestir: { traje: 'pirata', chapeu: 'cartola', PESCOCO: 'gravata' } },
    { titulo: 'realeza, coroa, varinha', perfil: P(true, 1, 3, 2, 4), pose: { bracos: 'tchau3' }, vestir: { traje: 'realeza', chapeu: 'coroa', MAO: 'varinha', COSTAS: 'capa' } },
    { titulo: 'heroi, máscara, torcendo', perfil: P(false, 3, 1, 1, 5), pose: { humor: 'torcendo', bracos: 'torcendo' }, vestir: { traje: 'heroi', ROSTO: 'mascara', MAO: 'varinha' } },
    { titulo: 'chef pensando', perfil: P(true, 0, 0, 0, 3), pose: { humor: 'pensando', bracos: 'pensando' }, vestir: { traje: 'chef', ROSTO: 'oculos', PES: 'pantufas' } },
    { titulo: 'listras, boné, mochila', perfil: P(false, 1, 0, 2, 0), pose: { bracos: 'tchau1' }, vestir: { traje: 'listras', chapeu: 'bone', COSTAS: 'mochila', PES: 'tenis', MAO: 'ursinho' } },
    { titulo: 'fada, flor, colar', perfil: P(true, 2, 2, 1, 4), pose: { humor: 'surpreso' }, vestir: { traje: 'fada', chapeu: 'flor', PESCOCO: 'colar', PES: 'botas' } },
    { titulo: 'medico tchau', perfil: P(true, 3, 0, 2, 1), pose: { bracos: 'tchau4' }, vestir: { traje: 'medico', ROSTO: 'oculos' } },
    { titulo: 'pijama torcendo, ursinho', perfil: P(false, 2, 3, 0, 2), pose: { humor: 'torcendo', bracos: 'torcendo' }, vestir: { traje: 'pijama', MAO: 'ursinho', PES: 'pantufas' } },
    { titulo: 'chuva, mochila', perfil: P(true, 1, 1, 0, 5), pose: { bracos: 'tchau2' }, vestir: { traje: 'chuva', COSTAS: 'mochila' } },
    { titulo: 'sem traje: asas, tiara, sol', perfil: P(true, 0, 2, 1, 1), pose: { humor: 'feliz', bracos: 'parado' }, vestir: { COSTAS: 'asas', CABELO: 'tiara', ROSTO: 'oculosdesol', PESCOCO: 'cachecol' } },
  ],
  // conferência do rosto: cada linha uma peça em menino e menina, pele clara e escura, cabelo espetado e cacheado
  rostos: [
    ['boné', { chapeu: 'bone' }], ['coroa', { chapeu: 'coroa' }], ['cartola', { chapeu: 'cartola' }], ['flor', { chapeu: 'flor' }],
    ['máscara', { ROSTO: 'mascara' }], ['fada', { traje: 'fada' }],
  ].flatMap(([nome, vestir]) => [
    P(false, 0, 2, 2, 0), P(false, 3, 1, 1, 2), P(true, 0, 3, 1, 4), P(true, 3, 0, 1, 1), P(true, 2, 1, 2, 5), P(false, 1, 0, 0, 3),
  ].map((perfil, i) => ({ titulo: nome, perfil, pose: i % 3 === 2 ? { humor: 'torcendo', bracos: 'torcendo' } : {}, vestir }))),
};

async function main() {
  const m = JSON.parse(fs.readFileSync(path.join(ARTE, 'manifesto.json'), 'utf8'));
  // depois da integração tudo está no manifesto.json; o manifesto à parte só existe num render CATALOGO=fase7
  const f7 = path.join(ARTE, 'manifesto_fase7.json');
  if (fs.existsSync(f7)) Object.assign(m.imagens, JSON.parse(fs.readFileSync(f7, 'utf8')).imagens);
  const pedidas = process.argv.slice(2);
  const folhas = {};
  const usados = new Set();
  for (const [nome, cenas] of Object.entries(FOLHAS)) {
    if (pedidas.length && !pedidas.includes(nome)) continue;
    folhas[nome] = cenas.map((c) => {
      const camadas = camadasComGuardaRoupa(m, c.perfil, c.pose, c.vestir);
      for (const l of camadas) usados.add(l.arquivo);
      return { titulo: c.titulo, cores: { pele: PELES[c.perfil.pele], cabelo: CABELOS[c.perfil.cabelo], roupa: ROUPAS[c.perfil.roupa] }, camadas };
    });
  }
  const dados = {};
  for (const f of usados) dados[f] = 'data:image/webp;base64,' + fs.readFileSync(path.join(ARTE, f)).toString('base64');

  const navegador = await chromium.launch();
  try {
    const pagina = await navegador.newPage();
    await pagina.setContent('<html><body style="margin:0"></body></html>');
    for (const [nome, cenas] of Object.entries(folhas)) {
      const png = await pagina.evaluate(async ({ cenas, dados }) => {
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
        const col = Math.min(cenas.length, 6), lin = Math.ceil(cenas.length / col);
        const celL = 360, celA = 580;
        const W = col * celL, H = lin * celA;
        const cv = document.createElement('canvas');
        cv.width = W; cv.height = H;
        const ctx = cv.getContext('2d');
        const ceu = ctx.createLinearGradient(0, 0, 0, H);
        ceu.addColorStop(0, '#8fd0ff'); ceu.addColorStop(0.6, '#ffe2cf'); ceu.addColorStop(1, '#ffc9d6');
        ctx.fillStyle = ceu; ctx.fillRect(0, 0, W, H);
        ctx.font = '600 20px sans-serif';
        cenas.forEach((c, i) => {
          const x0 = (i % col) * celL, y0 = Math.floor(i / col) * celA;
          const a = empilhar(c.camadas, c.cores);
          ctx.drawImage(a, x0 + 10, y0 + 40);
          ctx.fillStyle = '#3b2a5a'; ctx.fillText(c.titulo, x0 + 12, y0 + 28);
        });
        return cv.toDataURL('image/png');
      }, { cenas, dados });
      fs.mkdirSync(SAIDA, { recursive: true });
      const destino = path.join(SAIDA, `previa_${nome}.png`);
      fs.writeFileSync(destino, Buffer.from(png.split(',')[1], 'base64'));
      console.log('prévia:', path.relative(process.cwd(), destino));
    }
  } finally {
    await navegador.close();
  }
}

main().catch((e) => { console.error(e); process.exit(1); });
