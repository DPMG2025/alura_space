const { chromium } = require('playwright');
(async () => {
  const [,, ...pares] = process.argv;
  const browser = await chromium.launch({ args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--ignore-gpu-blocklist'] });
  const page = await browser.newPage({ viewport: { width: 393, height: 852 }, deviceScaleFactor: 2 });
  page.on('console', m => { if (m.type() === 'error' || m.type()==='warning') console.log('console:', m.text()); });
  page.on('pageerror', e => console.log('pageerror:', e.message));
  for (let i = 0; i < pares.length; i += 2) {
    await page.goto('http://localhost:8765/' + pares[i]);
    await page.waitForFunction(() => window.PRONTO === true, null, { timeout: 120000 });
    await page.screenshot({ path: pares[i + 1] });
    console.log('ok', pares[i + 1]);
  }
  await browser.close();
})();
