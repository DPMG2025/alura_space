# Fase 9B (arte, lote 2): lista os emojis das coisas das áreas novas (CasaQueCresce), dos livrinhos, da
# história, do ateliê e da loja que ainda não têm modelo 3D. Pula os que já têm figura (`emojis` no
# manifesto.json), os que viram item_<tipo> da casa (ArteDasAreas) e os do vocabulário das perguntas (outro lote).
# Uso (de casinha-do-saber/): python3 arte3d/emojis_fase9b.py [saida.json]
import re, json, sys, glob, os, collections

R = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
D = R + '/app/src/main/java/br/com/casinhadosaber/'


def ler(p):
    return open(p, encoding='utf-8').read()


EMO = re.compile(
    r'(?:[\U0001F000-\U0001FAFF☀-➿⬀-⯿⌀-⏿←-⇿〰〽■-◿⤴⤵]'
    r'(?:️|[\U0001F3FB-\U0001F3FF])?(?:‍[\U0001F000-\U0001FAFF☀-➿⬀-⯿](?:️)?)*)')


def norm(e):
    return e.replace('️', '').strip()


def emojis(txt):
    return [norm(m) for m in EMO.findall(txt) if norm(m)]


# Vocabulário das perguntas (fica com o outro agente).
voc = set()
for p in glob.glob(D + 'dominio/fases/*.kt') + [D + 'dominio/Vocabulario.kt', D + 'dominio/VocabularioDosMundos.kt',
                                                 D + 'dominio/GeradorDeFases.kt', D + 'dominio/AtividadesNovas.kt']:
    voc |= set(emojis(ler(p)))
cob = ler(R + '/docs/visual-3d/relatorios/cobertura-emojis.md')
voc |= set(emojis(cob.split('## Todos os sem modelo')[1]))

# Figuras já feitas (manifesto principal e manifestos à parte de outros lotes, se houver).
temFig = {}
for mf in glob.glob(R + '/app/src/main/assets/arte/manifesto*.json'):
    if mf.endswith('manifesto_fase9b.json'):
        continue
    for k, v in json.load(open(mf))['imagens'].items():
        if k.startswith('fig_'):
            for e in v.get('emojis', []):
                temFig[norm(e)] = k
temFig['🪙'] = 'moeda'
man = json.load(open(R + '/app/src/main/assets/arte/manifesto.json'))['imagens']

# Peças da casa (mesmo tipo e mesmo emoji = item_<tipo>, ArteDasAreas.para).
ic = ler(D + 'dominio/ItensDaCasa.kt')
casa = {m.group(1): norm(m.group(2)) for m in re.finditer(
    r'(?:objeto|movel|estrutura)\("(\w+)", "[^"]*", "[^"]*", "[^"]*", "([^"]*)"', ic)}
estrut = set(re.findall(r'estrutura\("(\w+)"', ic))

cq = ler(D + 'dominio/CasaQueCresce.kt')
areas = []
for m in re.finditer(r'AreaDaCasa\(\s*"(\w+)", "([^"]*)", "[^"]*", "[^"]*", "([^"]*)", TipoDeArea\.(\w+), (\d+),\s*tipos\((.*?)\),\s*\)',
                     cq, re.S):
    tipos = [l.split('|') for l in re.findall(r'"([^"]*\|[^"]*)"', m.group(6))]
    areas.append(dict(id=m.group(1), nome=m.group(2), icone=norm(m.group(3)), tam=int(m.group(5)), tipos=tipos))
assert len(areas) == 20, len(areas)

cont = collections.Counter()
origem = collections.defaultdict(list)
prio = {}
nomes = collections.defaultdict(list)
for ai, a in enumerate(areas):
    for j in range(a['tam'] - 1):
        t = a['tipos'][j % len(a['tipos'])]
        e, tipo = norm(t[4]), t[0]
        if tipo in ('cachorro', 'gato'):
            continue
        if tipo in casa and tipo not in estrut and casa[tipo] == e and ('item_' + tipo) in man:
            continue
        cont[e] += 1
        prio.setdefault(e, ai)
        if a['id'] not in origem[e]:
            origem[e].append(a['id'])
        if t[1] not in nomes[e]:
            nomes[e].append(t[1])
    e = a['icone']
    cont[e] += 1
    prio.setdefault(e, ai)
    origem[e].append(a['id'] + '(obra)')

outros = {'livrinhos': ['dominio/Livrinhos.kt', 'ui/telas/TelaLivrinhos.kt', 'ui/telas/CenaDoLivrinho.kt'],
          'historia': ['dominio/Historia.kt', 'ui/telas/CenaDaHistoria.kt'],
          'atelie': ['ui/telas/TelaAtelie.kt'], 'loja': ['dominio/Loja.kt']}
peso = {'livrinhos': 1.5, 'historia': 1.6, 'atelie': 1.7, 'loja': 2.5}
for k, fs in outros.items():
    for f in fs:
        if not os.path.exists(D + f):
            continue
        for e in emojis(ler(D + f)):
            cont[e] += 1
            prio[e] = min(prio.get(e, 99), peso[k])
            if k not in origem[e]:
                origem[e].append(k)

alvos, pulFig, pulVoc = [], [], []
for e, c in cont.items():
    if e in temFig:
        pulFig.append(e)
    elif e in voc:
        pulVoc.append(e)
    else:
        alvos.append(dict(emoji=e, n=c, prio=prio[e], origem=origem[e], nomes=nomes[e]))
alvos.sort(key=lambda a: (a['prio'], -a['n']))
if len(sys.argv) > 1:
    json.dump(dict(alvos=alvos, comFigura=pulFig, doVocabulario=pulVoc), open(sys.argv[1], 'w'), ensure_ascii=False, indent=1)
print(len(cont), 'emojis;', len(alvos), 'alvos;', len(pulFig), 'com figura;', len(pulVoc), 'do vocabulário')
for a in alvos:
    print(a['prio'], a['emoji'], a['n'], ' '.join(a['origem'][:4]), '/', ', '.join(a['nomes'][:2]))
