// Lista do que o pipeline renderiza. Cada entrada vira <id>.webp em app/src/main/assets/arte/
// e uma linha no manifesto.json.
//
//   id     nome do arquivo e chave no manifesto (minúsculas, sem acento)
//   tipo   câmera e luz (ver cameras.js)
//   uso    maior tamanho em que a imagem aparece no app, em dp; o render sai no dobro (ESCALA em render.js)
//   montar devolve o THREE.Object3D; âncoras são marcadas no modelo com ancora() (kit/formas.js)
//
// Camadas de personagem (camadas.js) têm ainda: grupo, tingir ('pele' | 'cabelo' | 'roupa' | null),
// ordem (de trás para a frente) e personagem ('crianca' | 'irmao'), que vão para o manifesto.
import { estrelaDourada } from './objetos/estrela3D.js';
import { moedaDoPlacar } from './objetos/moeda.js';
import { patinhoDaLagoa } from './objetos/patinho.js';
import { camadasDosPersonagens } from './camadas.js';
import { icones } from './objetos/icones.js';
import * as ui from './objetos/interface.js';
import { camadasDaCasa, itensDaCasa } from './casa.js';

// Fase 2: ícones dos caminhos (ids pelos nomes de dominio/Trilha) e peças da interface.
const icone = (id, montar, uso = 48) => ({ id, tipo: 'item', uso: { largura: uso, altura: uso }, montar });
const pecasDaInterface = [
  { id: 'icone_trilha_matematica', tipo: 'icone', uso: { largura: 96, altura: 96 }, montar: icones.numeros },
  { id: 'icone_trilha_portugues', tipo: 'icone', uso: { largura: 96, altura: 96 }, montar: icones.letras },
  { id: 'icone_trilha_ingles', tipo: 'icone', uso: { largura: 96, altura: 96 }, montar: icones.ingles },
  { id: 'icone_trilha_logica', tipo: 'icone', uso: { largura: 96, altura: 96 }, montar: icones.logica },
  icone('estrela_cheia', ui.estrelaCheia),
  icone('estrela_vazia', ui.estrelaVazia),
  icone('estrela_brilhando', ui.estrelaBrilhando, 72),
  ...Object.entries(ui.CORES_DAS_CONTAS).map(([nome, cor]) => icone('conta_' + nome, ui.conta(cor), 32)),
  icone('conta_vazia', ui.contaVazia, 32),
  icone('icone_fechar', ui.iconeFechar),
  icone('icone_voltar', ui.iconeVoltar),
  icone('icone_som_ligado', ui.iconeSomLigado),
  icone('icone_som_desligado', ui.iconeSomDesligado),
  icone('icone_ajustes', ui.iconeAjustes),
  icone('icone_pais', ui.iconePais),
  icone('icone_casa', ui.iconeCasa),
  icone('icone_loja', ui.iconeLoja),
];

export const catalogo = [
  { id: 'estrela3d', tipo: 'item', uso: { largura: 120, altura: 120 }, montar: estrelaDourada },
  { id: 'moeda', tipo: 'item', uso: { largura: 64, altura: 64 }, montar: moedaDoPlacar },
  { id: 'patinho', tipo: 'objeto', uso: { largura: 120, altura: 120 }, montar: patinhoDaLagoa },
  ...pecasDaInterface,
  ...camadasDosPersonagens,
  // Fase 3: a casa de fora (casa.js)
  ...camadasDaCasa,
  ...itensDaCasa,
];
