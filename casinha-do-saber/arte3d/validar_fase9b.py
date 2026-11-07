# Fase 9B: confere o manifesto_fase9b.json (arquivo existe, largura e altura = cabeçalho do WebP = uso x 2,
# no máximo 400 KB, id e emojis que não estão no manifesto.json, apoio e âncoras dentro da imagem).
# Uso (de casinha-do-saber/): python3 arte3d/validar_fase9b.py
import json, os, struct, sys

A = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'app/src/main/assets/arte')
m = json.load(open(os.path.join(A, 'manifesto_fase9b.json')))['imagens']
prin = json.load(open(os.path.join(A, 'manifesto.json')))['imagens']


def dim(p):
    b = open(p, 'rb').read(40)
    c = b[12:16]
    if c == b'VP8X':
        return 1 + int.from_bytes(b[24:27], 'little'), 1 + int.from_bytes(b[27:30], 'little')
    if c == b'VP8L':
        v = int.from_bytes(b[21:25], 'little')
        return (v & 0x3fff) + 1, ((v >> 14) & 0x3fff) + 1
    return struct.unpack('<H', b[26:28])[0] & 0x3fff, struct.unpack('<H', b[28:30])[0] & 0x3fff


n = lambda e: e.replace('️', '')
emPrin = {n(e) for v in prin.values() for e in v.get('emojis', [])}
erros, tot, mx = [], 0, 0
for k, v in m.items():
    p = os.path.join(A, v['arquivo'])
    if not os.path.exists(p):
        erros.append(k + ': sem arquivo'); continue
    s = os.path.getsize(p); tot += s; mx = max(mx, s)
    w, h = dim(p)
    if (w, h) != (v['largura'], v['altura']) or (w, h) != (v['uso']['largura'] * 2, v['uso']['altura'] * 2):
        erros.append(f'{k}: tamanho {w}x{h}')
    if s > 400 * 1024: erros.append(f'{k}: {s} bytes')
    if k in prin: erros.append(k + ': já está no manifesto.json')
    for a in [v['apoio']] + list(v['ancoras'].values()):
        if not (0 <= a['x'] < w and 0 <= a['y'] < h): erros.append(k + ': ponto fora')
    if not v.get('emojis') or not v.get('lugar'): erros.append(k + ': sem emojis/lugar')
    if any(n(e) in emPrin for e in v['emojis']): erros.append(k + ': emoji já tem figura')
print(len(m), 'imagens;', len(erros), 'erros; total', round(tot / 1024, 1), 'KB; maior', round(mx / 1024, 1), 'KB')
for e in erros: print(' ', e)
sys.exit(1 if erros else 0)
