// Gerado por Telosys embedded generator 4.3.0: script genérico de CRUD (listar, criar, editar, excluir)

const NUMERICOS = ['byte', 'short', 'int', 'long', 'float', 'double', 'decimal'];

function montarCrud(config) {
  const raiz = document.getElementById('crud');
  const chave = config.campos.find((campo) => campo.chave);
  let editando = null;

  raiz.innerHTML = `
    <form id="formulario"><fieldset><legend>Novo registro</legend><div class="campos"></div>
      <button type="submit">Salvar</button> <button type="button" id="cancelar">Cancelar</button>
      <p id="mensagem" role="status"></p></fieldset></form>
    <div class="tabela"><table><thead><tr></tr></thead><tbody></tbody></table></div>`;
  const form = raiz.querySelector('#formulario');
  const mensagem = raiz.querySelector('#mensagem');

  config.campos.forEach((campo) => {
    const rotulo = document.createElement('label');
    rotulo.textContent = campo.nome + (campo.obrigatorio || campo.chave ? ' *' : '');
    const entrada = document.createElement('input');
    entrada.name = campo.nome;
    entrada.id = `campo-${campo.nome}`;
    rotulo.htmlFor = entrada.id;
    entrada.type = NUMERICOS.includes(campo.tipo) ? 'number' : campo.tipo === 'boolean' ? 'checkbox' : 'text';
    if (entrada.type === 'number' && campo.tipo !== 'int' && campo.tipo !== 'long') entrada.step = 'any';
    if (campo.max) entrada.maxLength = campo.max;
    entrada.required = campo.obrigatorio || campo.chave;
    rotulo.appendChild(entrada);
    form.querySelector('.campos').appendChild(rotulo);
    raiz.querySelector('thead tr').insertAdjacentHTML('beforeend', `<th>${campo.nome}</th>`);
  });
  raiz.querySelector('thead tr').insertAdjacentHTML('beforeend', '<th>Ações</th>');

  function lerFormulario() {
    const dados = {};
    config.campos.forEach((campo) => {
      const entrada = form.elements[campo.nome];
      if (entrada.type === 'checkbox') dados[campo.nome] = entrada.checked;
      else if (entrada.value === '') dados[campo.nome] = null;
      else dados[campo.nome] = entrada.type === 'number' ? Number(entrada.value) : entrada.value;
    });
    return dados;
  }

  function preencher(registro) {
    editando = registro;
    form.querySelector('legend').textContent = registro ? `Editando ${chave.nome} = ${registro[chave.nome]}` : 'Novo registro';
    config.campos.forEach((campo) => {
      const entrada = form.elements[campo.nome];
      const valor = registro ? registro[campo.nome] : null;
      if (entrada.type === 'checkbox') entrada.checked = Boolean(valor);
      else entrada.value = valor ?? '';
      entrada.readOnly = Boolean(registro) && campo.chave;
    });
  }

  async function chamar(metodo, caminho, corpo) {
    const resposta = await fetch(config.recurso + caminho, {
      method: metodo,
      headers: corpo ? { 'Content-Type': 'application/json' } : {},
      body: corpo ? JSON.stringify(corpo) : undefined,
    });
    if (!resposta.ok) throw new Error(`${metodo} ${config.recurso}${caminho} → ${resposta.status}`);
    const texto = await resposta.text();
    return texto ? JSON.parse(texto) : null;
  }

  async function listar() {
    const corpo = raiz.querySelector('tbody');
    corpo.innerHTML = '';
    for (const registro of await chamar('GET', '')) {
      const linha = document.createElement('tr');
      config.campos.forEach((campo) => {
        const celula = document.createElement('td');
        celula.textContent = registro[campo.nome] ?? '';
        linha.appendChild(celula);
      });
      const acoes = document.createElement('td');
      const editar = Object.assign(document.createElement('button'), { textContent: 'Editar', onclick: () => preencher(registro) });
      const excluir = Object.assign(document.createElement('button'), {
        textContent: 'Excluir',
        onclick: async () => {
          if (!confirm(`Excluir ${config.titulo} ${registro[chave.nome]}?`)) return;
          await executar(() => chamar('DELETE', `/${registro[chave.nome]}`), 'Registro excluído');
        },
      });
      acoes.append(editar, ' ', excluir);
      linha.appendChild(acoes);
      corpo.appendChild(linha);
    }
  }

  async function executar(acao, sucesso) {
    try {
      await acao();
      mensagem.textContent = sucesso;
      preencher(null);
      await listar();
    } catch (erro) {
      mensagem.textContent = `Erro: ${erro.message}`;
    }
  }

  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    const dados = lerFormulario();
    // novo: POST (409 se a chave já existe); edição: PUT na chave do registro
    executar(
      () => (editando ? chamar('PUT', `/${editando[chave.nome]}`, dados) : chamar('POST', '', dados)),
      editando ? 'Registro atualizado' : 'Registro criado',
    );
  });
  raiz.querySelector('#cancelar').addEventListener('click', () => preencher(null));
  listar().catch((erro) => { mensagem.textContent = `Erro: ${erro.message}`; });
}

