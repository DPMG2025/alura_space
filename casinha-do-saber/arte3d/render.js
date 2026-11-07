#!/usr/bin/env node
// Render em lote da arte 3D (PRD visual 3D, seção 3.1).
//
//   npm run render              renderiza o catálogo inteiro e apaga imagens órfãs
//   npm run render -- moeda     renderiza só os ids dados (o resto do manifesto fica como está)
//   CATALOGO=fase6 npm run render   catálogo separado (src/catalogo_fase6.js) e manifesto separado
//                               (manifesto_fase6.json, na mesma pasta); nunca apaga imagens de outros manifestos
//
// Sobe um servidor local com esta pasta, abre pagina/render.html no Chromium headless (WebGL por
// SwiftShader), pede cada objeto do catálogo (src/catalogo.js) e grava, em
// ../app/src/main/assets/arte/, <id>.webp (com alfa) e manifesto.json. Prévias em PNG vão para saida/.
'use strict';
const fs = require('fs');
const path = require('path');
const http = require('http');
const { chromium } = require('playwright');
const sharp = require('sharp');

const RAIZ = __dirname;
const DESTINO = path.resolve(RAIZ, '..', 'app', 'src', 'main', 'assets', 'arte');
const PREVIA = path.join(RAIZ, 'saida');
/** Pixels por dp na imagem: o dobro do tamanho de uso (PRD, seção 7). */
const ESCALA = 2;
/** O render sai maior e é reduzido com Lanczos: bordas mais lisas que o antialias do SwiftShader. */
const SUPERAMOSTRA = 2;
const QUALIDADE_WEBP = 85;
/** Catálogo separado por fase (ex.: fase6), para fases em paralelo não mexerem no mesmo manifesto. */
const CATALOGO = process.env.CATALOGO || '';
if (CATALOGO && !/^[a-z0-9_]+$/.test(CATALOGO)) throw new Error('CATALOGO inválido: ' + CATALOGO);

const TIPOS = { '.html': 'text/html', '.js': 'text/javascript', '.json': 'application/json', '.css': 'text/css', '.woff2': 'font/woff2' };

function servir() {
  const srv = http.createServer((req, res) => {
    const url = decodeURIComponent(new URL(req.url, 'http://x').pathname);
    const arquivo = path.normalize(path.join(RAIZ, url));
    if (!arquivo.startsWith(RAIZ + path.sep) || !fs.existsSync(arquivo) || fs.statSync(arquivo).isDirectory()) {
      res.writeHead(404); res.end(); return;
    }
    res.writeHead(200, { 'Content-Type': TIPOS[path.extname(arquivo)] || 'application/octet-stream' });
    fs.createReadStream(arquivo).pipe(res);
  });
  return new Promise((ok) => srv.listen(0, '127.0.0.1', () => ok(srv)));
}

function ordenar(obj) {
  if (Array.isArray(obj) || obj === null || typeof obj !== 'object') return obj;
  return Object.fromEntries(Object.keys(obj).sort().map((k) => [k, ordenar(obj[k])]));
}

async function main() {
  const pedidos = process.argv.slice(2);
  fs.mkdirSync(DESTINO, { recursive: true });
  fs.mkdirSync(PREVIA, { recursive: true });
  const srv = await servir();
  const porta = srv.address().port;
  const navegador = await chromium.launch({ args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--ignore-gpu-blocklist'] });
  let falhou = false;
  try {
    const pagina = await navegador.newPage();
    pagina.on('console', (m) => { if (m.type() === 'error' || m.type() === 'warning') console.log('console:', m.text()); });
    pagina.on('pageerror', (e) => { falhou = true; console.log('erro na página:', e.message); });
    await pagina.goto(`http://127.0.0.1:${porta}/pagina/render.html${CATALOGO ? '?catalogo=' + CATALOGO : ''}`);
    await pagina.waitForFunction(() => window.PRONTO === true, null, { timeout: 120000 });
    const todos = await pagina.evaluate(() => window.catalogo());
    const ids = pedidos.length ? pedidos : todos;
    const desconhecidos = ids.filter((i) => !todos.includes(i));
    if (desconhecidos.length) throw new Error('fora do catálogo: ' + desconhecidos.join(', '));

    const arqManifesto = path.join(DESTINO, CATALOGO ? `manifesto_${CATALOGO}.json` : 'manifesto.json');
    const anterior = (pedidos.length || CATALOGO) && fs.existsSync(arqManifesto) ? JSON.parse(fs.readFileSync(arqManifesto, 'utf8')).imagens : {};
    const imagens = { ...anterior };

    for (const id of ids) {
      const t0 = Date.now();
      const r = await pagina.evaluate(([i, e, s]) => window.renderizar(i, e, s), [id, ESCALA, SUPERAMOSTRA]);
      const png = Buffer.from(r.png.split(',')[1], 'base64');
      const reduzido = await sharp(png).resize(r.largura, r.altura, { kernel: 'lanczos3' }).png().toBuffer();
      fs.writeFileSync(path.join(PREVIA, id + '.png'), reduzido);
      const webp = await sharp(reduzido).webp({ quality: QUALIDADE_WEBP, alphaQuality: 100, effort: 6, smartSubsample: true }).toBuffer();
      const arquivo = id + '.webp';
      fs.writeFileSync(path.join(DESTINO, arquivo), webp);
      imagens[id] = { arquivo, tipo: r.tipo, largura: r.largura, altura: r.altura, uso: r.uso, apoio: r.apoio, ancoras: r.ancoras };
      if (r.iso !== undefined) imagens[id].iso = r.iso;
      if (r.grupo !== undefined) Object.assign(imagens[id], { grupo: r.grupo, tingir: r.tingir, ordem: r.ordem, personagem: r.personagem });
      // camadas de câmera fixa não podem encostar na borda: seria parte da figura cortada
      if (r.bordas.length) { falhou = true; console.log(`CORTADO ${id}: encosta em ${r.bordas.join(', ')}`); }
      console.log(`ok ${arquivo} ${r.largura}x${r.altura} ${(webp.length / 1024).toFixed(1)} KB (${Date.now() - t0} ms)`);
    }

    // Sem lista explícita, o manifesto é exatamente o catálogo: remove o que saiu dele.
    if (!pedidos.length && CATALOGO) {
      // catálogo separado: só some o que saiu DESTE catálogo (as imagens dos outros manifestos ficam)
      for (const id of Object.keys(imagens)) {
        if (todos.includes(id)) continue;
        const f = path.join(DESTINO, imagens[id].arquivo);
        if (fs.existsSync(f)) { fs.unlinkSync(f); console.log('removido órfão', imagens[id].arquivo); }
        delete imagens[id];
      }
    } else if (!pedidos.length) {
      for (const id of Object.keys(imagens)) if (!todos.includes(id)) delete imagens[id];
      const validos = new Set(Object.values(imagens).map((i) => i.arquivo).concat('manifesto.json'));
      // imagens dos manifestos separados (manifesto_<fase>.json) também são válidas
      for (const f of fs.readdirSync(DESTINO)) {
        if (!/^manifesto_[a-z0-9_]+\.json$/.test(f)) continue;
        validos.add(f);
        for (const i of Object.values(JSON.parse(fs.readFileSync(path.join(DESTINO, f), 'utf8')).imagens)) validos.add(i.arquivo);
      }
      for (const f of fs.readdirSync(DESTINO)) {
        if (!validos.has(f)) { fs.unlinkSync(path.join(DESTINO, f)); console.log('removido órfão', f); }
      }
    }
    const manifesto = { versao: 1, escala: ESCALA, imagens: ordenar(imagens) };
    fs.writeFileSync(arqManifesto, JSON.stringify(manifesto, null, 2) + '\n');
    console.log('manifesto:', Object.keys(imagens).length, 'imagens em', path.relative(process.cwd(), DESTINO));
  } finally {
    await navegador.close();
    srv.close();
  }
  if (falhou) process.exit(1);
}

main().catch((e) => { console.error(e); process.exit(1); });
