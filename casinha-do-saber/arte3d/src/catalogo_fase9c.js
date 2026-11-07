// Catálogo da Fase 9C (arte, lote 3): ícones de interface que ainda eram emoji, no estilo dos ícones da barra
// (glifo roxo em relevo, câmera 'item', 48 dp, âncora 'centro'). Renderizar à parte:
//   CATALOGO=fase9c npm run render
// Grava as imagens em app/src/main/assets/arte/ e as entradas em manifesto_fase9c.json; o integrador junta este
// catálogo ao catalogo.js (...catalogoFase9c) e o manifesto ao manifesto.json. Detalhes e o mapa emoji -> id em
// docs/visual-3d/relatorios/fase9c-arte-lote3.md.
import * as i from './objetos/iconesDoLote3.js';

const icone = (id, montar, uso = 48) => ({ id, tipo: 'item', uso: { largura: uso, altura: uso }, montar });

/** Abas das áreas da casa que cresce: id da área (AreaDaCasa.id) e o modelo. */
const AREAS = [
  ['quarto2', i.iconeAreaQuarto], ['pet', i.iconeAreaPet], ['piscina', i.iconeAreaPiscina], ['parquinho', i.iconeAreaParquinho],
  ['horta', i.iconeAreaHorta], ['brinquedos', i.iconeAreaBrinquedos], ['arvore', i.iconeAreaArvore], ['flores', i.iconeAreaFlores],
  ['estudio', i.iconeAreaEstudio], ['gourmet', i.iconeAreaGourmet], ['biblioteca', i.iconeAreaBiblioteca],
  ['observatorio', i.iconeAreaObservatorio], ['fazendinha', i.iconeAreaFazendinha], ['quadra', i.iconeAreaQuadra],
  ['aquario', i.iconeAreaAquario], ['cinema', i.iconeAreaCinema], ['lago', i.iconeAreaLago], ['laboratorio', i.iconeAreaLaboratorio],
  ['estufa', i.iconeAreaEstufa], ['festa', i.iconeAreaFesta],
];

export const catalogoFase9c = [
  // guarda-roupa e espelho: abas das gavetas (Gaveta / ParteDoCorpo)
  icone('icone_roupas', i.iconeRoupas),
  icone('icone_chapeus', i.iconeChapeus),
  icone('icone_rosto', i.iconeRosto),
  icone('icone_pescoco', i.iconePescoco),
  icone('icone_costas', i.iconeCostas),
  icone('icone_mao', i.iconeMao),
  icone('icone_pes', i.iconePes),
  icone('icone_cabelo', i.iconeCabelo),
  // geladeira: abas das prateleiras (PrateleiraDaGeladeira)
  icone('icone_frutas', i.iconeFrutas),
  icone('icone_leite', i.iconeLeite),
  icone('icone_congelador', i.iconeCongelador),
  // espelho (TelaPerfil): linhas
  icone('icone_pele', i.iconePele),
  icone('icone_cor_do_cabelo', i.iconeCorDoCabelo),
  icone('icone_penteado', i.iconePenteado),
  icone('icone_roupa', i.iconeRoupa),
  icone('icone_escrever', i.iconeEscrever),
  // trilha: tipos de nó e marcos
  icone('icone_no_fase', i.iconeNoFase),
  icone('icone_no_chefao', i.iconeNoChefao),
  icone('icone_no_trofeu', i.iconeNoTrofeu),
  icone('icone_no_treino', i.iconeNoTreino),
  icone('icone_no_revisao', i.iconeNoRevisao),
  icone('icone_no_desafio', i.iconeNoDesafio),
  icone('icone_no_novidade', i.iconeNoNovidade),
  icone('icone_no_bloqueado', i.iconeNoBloqueado),
  icone('icone_no_marco', i.iconeNoMarco),
  icone('icone_certo', i.iconeCerto),
  icone('icone_certo_branco', i.iconeCertoBranco),
  icone('icone_alfinete', i.iconeAlfinete),
  // casa: escada, baú e obra
  icone('icone_subir', i.iconeSubir),
  icone('icone_descer', i.iconeDescer),
  icone('icone_bau', i.iconeBau),
  icone('icone_martelo', i.iconeMartelo),
  // botões soltos
  icone('icone_marcador', i.iconeMarcador),
  icone('icone_bussola', i.iconeBussola),
  icone('icone_vassoura', i.iconeVassoura),
  icone('icone_dica', i.iconeDica),
  icone('icone_broto', i.iconeBroto),
  ...AREAS.map(([area, f]) => icone('icone_area_' + area, f)),
];
export const catalogo = catalogoFase9c;
