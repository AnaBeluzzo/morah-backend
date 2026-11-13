"""Modelo de características do Morah: uma única fonte para o model.xml do FeatureIDE,
o diagrama PNG (notação do FeatureIDE) e os dados das justificativas do documento.

Uso: python3 gerar_modelo.py  →  model.xml, modelo-caracteristicas.png, modelo.json
"""
import json
import os
from xml.sax.saxutils import escape

from PIL import Image, ImageDraw, ImageFont

AQUI = os.path.dirname(os.path.abspath(__file__))


def f(nome, por_que, filhos=(), grupo="and", abstrata=False, obrigatoria=True):
    """Uma característica. grupo = como os FILHOS se relacionam: and | or | alt."""
    return {"nome": nome, "por_que": por_que, "filhos": list(filhos), "grupo": grupo,
            "abstrata": abstrata, "obrigatoria": obrigatoria}


def o(nome, por_que, **resto):
    """Atalho para característica opcional (filho de um grupo 'and')."""
    return f(nome, por_que, obrigatoria=False, **resto)


MODELO = f("Morah", "Raiz: o sistema de gestão condominial. Abstrata porque só agrupa a linha de produtos.", abstrata=True, filhos=[
    f("Autenticacao", "Todo condomínio precisa de login: sem ele nenhum dado é acessível.", filhos=[
        f("LoginEmailSenha", "POST /auth/login com JWT; é a única forma de entrar no app."),
        f("RefreshToken", "Renovação rotativa do access token (POST /auth/refresh); sem ela a sessão cai a cada hora."),
        o("TrocaDeContexto", "Só faz sentido para quem tem vínculo com mais de um condomínio ou perfil (POST /auth/contexto)."),
    ]),
    f("Perfis", "Agrupa os papéis de RBAC. Abstrata: o perfil em si não tem código, cada papel tem.", grupo="or", abstrata=True, filhos=[
        f("PerfilMorador", "Usa os módulos da própria unidade."),
        f("PerfilProprietario", "Tudo do morador e mais a gestão de inquilinos."),
        f("PerfilSindico", "Administra o condomínio, os módulos e o financeiro."),
        f("PerfilPortaria", "Opera visitantes, encomendas e vistorias."),
    ]),
    f("TelaInicial", "Painel por perfil (GET /home/dashboard); é a porta de entrada de todo usuário."),
    f("Notificacoes", "Todos os módulos avisam as pessoas; não pode ser desativado (regra do contrato).", filhos=[
        f("Canais", "Por onde a notificação sai. Abstrata (Strategy CanalNotificacao).", grupo="or", abstrata=True, filhos=[
            f("Push", "Notificação interna no app (CanalPush)."),
            f("Email", "Envio por e-mail (CanalEmail), simulado com log."),
        ]),
        o("MarcarComoLida", "Conforto de uso: o produto funciona sem controlar a leitura."),
    ]),
    f("Personalizacao", "Cada condomínio tem identidade própria; não pode ser desativada.", filhos=[
        f("IdentidadeVisual", "Nome, logo e cores do condomínio (PATCH /condominio/personalizacao)."),
        f("ModulosAtivaveis", "O síndico liga e desliga módulos (/condominio/modulos); é o mecanismo da variabilidade."),
    ]),
    f("Modulos", "Agrupa os seis módulos ativáveis. Abstrata: não tem código próprio.", abstrata=True, filhos=[
        o("Financeiro", "Ativável: há condomínios que usam outra administradora.", filhos=[
            f("Cobrancas", "Sem cobranças não existe módulo financeiro."),
            f("Pagamento", "Formas de quitar a cobrança. Abstrata (Template Method ProcessamentoPagamento).", grupo="or", abstrata=True, filhos=[
                f("PIX", "Copia e cola gerado pelo gateway simulado."),
                f("BaixaManual", "Síndico registra pagamento feito fora do sistema."),
                f("Webhook", "Gateway confirma o pagamento sozinho (HMAC)."),
            ]),
            f("Rateio", "Regra que divide a taxa entre as unidades. Abstrata (Strategy EstrategiaRateio).", grupo="alt", abstrata=True, filhos=[
                f("RateioIgualitario", "Mesmo valor para todas as unidades."),
                f("RateioFracaoIdeal", "Valor proporcional à fração ideal da unidade."),
            ]),
            o("DocumentosFinanceiros", "Prestação de contas publicada; dispensável num condomínio pequeno."),
            o("Inadimplencia", "Relatório gerencial do síndico; não é necessário para cobrar."),
        ]),
        o("ModuloPortaria", "Ativável: condomínio sem portaria física não usa.", filhos=[
            f("AutorizacaoVisita", "Núcleo do módulo: o morador autoriza a entrada."),
            o("ReenvioAutorizacao", "Reenviar o pedido ao morador é uma conveniência."),
            o("HistoricoAcessos", "Registro de entradas e saídas; nem todo condomínio audita."),
        ]),
        o("Avisos", "Ativável: o condomínio pode usar outro canal (ex.: mural físico).", filhos=[
            f("Mural", "Lista de avisos vigentes; sem ele não há módulo."),
            f("PublicoAlvo", "Para quem o aviso vai. Abstrata: é só a escolha do alcance.", grupo="or", abstrata=True, filhos=[
                f("AvisoCondominio", "Aviso para todas as unidades."),
                f("AvisoBloco", "Aviso para um bloco."),
                f("AvisoUnidade", "Aviso para uma unidade."),
            ]),
            o("ConfirmacaoLeitura", "Saber quem leu é útil, mas o aviso funciona sem isso."),
        ]),
        o("Reservas", "Ativável: condomínio sem área comum não usa.", filhos=[
            f("AreasComuns", "Cadastro das áreas reserváveis."),
            f("Disponibilidade", "Grade de horários; sem ela haveria reservas sobrepostas."),
            o("TaxaDeReserva", "Cobrar pelo uso da área é decisão do condomínio."),
            o("Vistoria", "Checagem de entrada e saída com registro de dano; opcional."),
        ]),
        o("MinhaUnidade", "Ativável: o síndico pode cadastrar moradores fora do app.", filhos=[
            f("DadosUnidade", "Dados da unidade do contexto ativo."),
            f("Vinculos", "Quem mora ou é dono da unidade."),
            o("SolicitacaoVinculo", "Pedido de novo morador com aprovação do síndico."),
            o("Inquilinos", "Só existe onde há locação."),
            o("Veiculos", "Cadastro de veículos (tabela database first); nem todo condomínio tem garagem."),
        ]),
        o("Encomendas", "Ativável: condomínio sem recebimento de pacotes não usa.", filhos=[
            f("Recebimento", "Portaria registra o pacote; é o núcleo do módulo."),
            f("RetiradaPorCodigo", "Código de 6 dígitos garante a entrega à pessoa certa."),
            o("AutorizacaoTerceiro", "Morador autoriza outra pessoa a retirar."),
            o("Extravio", "Abertura de ocorrência quando o pacote some."),
        ]),
    ]),
    f("Plataforma", "Decisões de infraestrutura do produto. Abstrata.", abstrata=True, filhos=[
        f("BancoDeDados", "Exatamente um banco por instalação. Abstrata.", grupo="alt", abstrata=True, filhos=[
            f("SQLite", "Arquivo local: ideal para demonstração e condomínio pequeno."),
            f("PostgreSQL", "Servidor de banco para produção com vários acessos simultâneos."),
        ]),
        f("Interface", "Como o usuário acessa a API. Abstrata.", grupo="or", abstrata=True, filhos=[
            f("AppMobileWeb", "Front React mobile-first (morah-frontend)."),
            f("SwaggerUI", "Documentação navegável da API (springdoc)."),
        ]),
        o("MultiplasInstancias", "Escalar horizontalmente; só necessário em condomínios grandes ou SaaS."),
    ]),
])

# Restrições cruzadas (cross-tree constraints). Operadores: imp, conj (And), disj (Or), not.
V = lambda nome: ("var", nome)
RESTRICOES = [
    (("imp", V("PerfilProprietario"), V("PerfilMorador")),
     "Proprietário é cumulativo a morador: tem tudo o que o morador tem (regra de RBAC do contrato)."),
    (("imp", V("Inquilinos"), ("conj", V("PerfilProprietario"), V("Email"))),
     "Só o proprietário cadastra inquilino, e o convite de acesso vai por e-mail."),
    (("imp", V("ModuloPortaria"), V("PerfilPortaria")),
     "Sem o perfil portaria ninguém registraria visitantes."),
    (("imp", V("Encomendas"), V("PerfilPortaria")),
     "Quem recebe e entrega os pacotes é a portaria."),
    (("imp", V("Vistoria"), V("PerfilPortaria")),
     "A vistoria da área comum é registrada pela portaria."),
    (("imp", V("Webhook"), V("PIX")),
     "O webhook do gateway confirma pagamentos PIX; sem PIX ele não tem o que receber."),
    (("imp", ("disj", V("Inadimplencia"), V("SolicitacaoVinculo")), V("PerfilSindico")),
     "Relatório de inadimplência e aprovação de vínculos são tarefas do síndico."),
    (("imp", V("ModulosAtivaveis"), V("PerfilSindico")),
     "Só o síndico liga e desliga módulos; como ModulosAtivaveis é obrigatória, todo produto tem síndico."),
    (("imp", V("TaxaDeReserva"), V("Financeiro")),
     "Cobrar a reserva gera cobrança, que só existe com o módulo financeiro."),
    (("imp", V("MultiplasInstancias"), ("not", V("SQLite"))),
     "SQLite é um arquivo local com um escritor por vez: não serve para várias instâncias."),
    (("imp", V("AutorizacaoTerceiro"), ("conj", V("PerfilMorador"), V("PerfilPortaria"))),
     "O morador autoriza o terceiro e a portaria confere o código na retirada."),
    (("imp", V("TelaInicial"), ("disj", V("Financeiro"), V("ModuloPortaria"), V("Avisos"),
                                V("Reservas"), V("MinhaUnidade"), V("Encomendas"))),
     "O painel mostra dados dos módulos: sem nenhum módulo ativo ele ficaria vazio."),
    (("imp", V("Veiculos"), ("disj", V("PerfilMorador"), V("PerfilProprietario"))),
     "Veículos são cadastrados pelas pessoas da unidade (morador ou proprietário)."),
]

SIMBOLO = {"imp": "⇒", "conj": "∧", "disj": "∨"}


def texto(expr, raiz=True):
    tipo = expr[0]
    if tipo == "var":
        return expr[1]
    if tipo == "not":
        return "¬" + texto(expr[1], False)
    corpo = f" {SIMBOLO[tipo]} ".join(texto(e, False) for e in expr[1:])
    return corpo if raiz else f"({corpo})"


# ------------------------------------------------------------------ model.xml do FeatureIDE

def xml_feature(n, nivel, grupo_do_pai="and"):
    pad = "\t" * nivel
    # em grupos Or/Alternative o FeatureIDE não usa "mandatory" nos membros
    obrigatoria = n["obrigatoria"] and grupo_do_pai == "and"
    attrs = (' abstract="true"' if n["abstrata"] else "") + (' mandatory="true"' if obrigatoria else "")
    desc = f'{pad}\t<description>{escape(n["por_que"])}</description>\n'
    if not n["filhos"]:
        return f'{pad}<feature{attrs} name="{n["nome"]}">\n{desc}{pad}</feature>\n'
    filhos = "".join(xml_feature(c, nivel + 1, n["grupo"]) for c in n["filhos"])
    return f'{pad}<{n["grupo"]}{attrs} name="{n["nome"]}">\n{desc}{filhos}{pad}</{n["grupo"]}>\n'


def xml_expr(expr, nivel):
    pad = "\t" * nivel
    if expr[0] == "var":
        return f"{pad}<var>{expr[1]}</var>\n"
    filhos = "".join(xml_expr(e, nivel + 1) for e in expr[1:])
    return f"{pad}<{expr[0]}>\n{filhos}{pad}</{expr[0]}>\n"


def gerar_xml():
    regras = "".join(f"\t\t<rule>\n\t\t\t<description>{escape(por_que)}</description>\n{xml_expr(e, 3)}\t\t</rule>\n"
                     for e, por_que in RESTRICOES)
    return ('<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n<featureModel>\n\t<properties/>\n'
            f'\t<struct>\n{xml_feature(MODELO, 2)}\t</struct>\n\t<constraints>\n{regras}\t</constraints>\n</featureModel>\n')


# ------------------------------------------------------------------ diagrama (notação do FeatureIDE, da esquerda para a direita)

ESCALA = 2
LINHA, COLUNA, ALTURA, MARGEM = 30 * ESCALA, 200 * ESCALA, 22 * ESCALA, 20 * ESCALA
COR_CONCRETA, COR_ABSTRATA, COR_BORDA, COR_LINHA = "#BFD7F5", "#F2F6FB", "#3A6EA5", "#555555"
FONTE = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial.ttf", 12 * ESCALA)
FONTE_B = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial Bold.ttf", 13 * ESCALA)
# a Arial comum não tem ⇒ ∧ ∨ ¬
FONTE_U = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial Unicode.ttf", 12 * ESCALA)


def posicionar(n, nivel=0, prox=None):
    prox = prox if prox is not None else [0]
    n["x"] = MARGEM + nivel * COLUNA
    if n["filhos"]:
        for c in n["filhos"]:
            posicionar(c, nivel + 1, prox)
        n["y"] = (n["filhos"][0]["y"] + n["filhos"][-1]["y"]) / 2
    else:
        n["y"] = MARGEM + 40 * ESCALA + prox[0] * LINHA
        prox[0] += 1
    return prox[0]


def largura_caixa(d, nome):
    return d.textlength(nome, font=FONTE) + 14 * ESCALA


def desenhar(d, n):
    w = largura_caixa(d, n["nome"])
    if n["filhos"]:
        px, py = n["x"] + w, n["y"]
        pontos = []
        for c in n["filhos"]:
            cx, cy = c["x"], c["y"]
            d.line([(px, py), (px + 30 * ESCALA, py), (cx - 20 * ESCALA, cy), (cx - 6 * ESCALA, cy)], fill=COR_LINHA, width=ESCALA)
            pontos.append((px + 30 * ESCALA, py, cx - 20 * ESCALA, cy))
            if n["grupo"] == "and":
                r = 5 * ESCALA
                caixa = [cx - 6 * ESCALA - 2 * r, cy - r, cx - 6 * ESCALA, cy + r]
                d.ellipse(caixa, fill=COR_LINHA if c["obrigatoria"] else "white", outline=COR_LINHA, width=ESCALA)
            desenhar(d, c)
        if n["grupo"] in ("or", "alt"):
            # arco ligando as linhas dos filhos perto do pai: cheio = Or, vazio = Alternative
            ax = px + 30 * ESCALA
            r = 26 * ESCALA
            ys = [p[3] for p in pontos]
            ang = lambda y: __import__("math").degrees(__import__("math").atan2(y - py, (pontos[0][2] - ax))) if pontos[0][2] != ax else 0
            a1, a2 = min(ang(y) for y in ys), max(ang(y) for y in ys)
            caixa = [ax - r, py - r, ax + r, py + r]
            if n["grupo"] == "or":
                d.pieslice(caixa, a1, a2, fill=COR_LINHA)
            else:
                d.pieslice(caixa, a1, a2, fill="white", outline=COR_LINHA, width=2 * ESCALA)
    cor = COR_ABSTRATA if n["abstrata"] else COR_CONCRETA
    d.rounded_rectangle([n["x"], n["y"] - ALTURA / 2, n["x"] + w, n["y"] + ALTURA / 2], radius=4 * ESCALA,
                        fill=cor, outline=COR_BORDA, width=ESCALA)
    d.text((n["x"] + 7 * ESCALA, n["y"]), n["nome"], font=FONTE, fill="#111111", anchor="lm")


def legenda(d, x, y):
    itens = [("caixa", COR_CONCRETA, "Concreta (Concrete)"), ("caixa", COR_ABSTRATA, "Abstrata (Abstract)"),
             ("cheio", None, "Obrigatória (Mandatory)"), ("vazio", None, "Opcional (Optional)"),
             ("or", None, "Grupo Or (pelo menos um)"), ("alt", None, "Grupo Alternative (exatamente um)")]
    d.text((x, y), "Legenda", font=FONTE_B, fill="#111111")
    for i, (tipo, cor, rotulo) in enumerate(itens):
        yy = y + (24 + i * 22) * ESCALA
        r = 6 * ESCALA
        if tipo == "caixa":
            d.rounded_rectangle([x, yy - 8 * ESCALA, x + 30 * ESCALA, yy + 8 * ESCALA], radius=3 * ESCALA, fill=cor, outline=COR_BORDA, width=ESCALA)
        elif tipo in ("cheio", "vazio"):
            d.ellipse([x + 9 * ESCALA, yy - r, x + 9 * ESCALA + 2 * r, yy + r], fill=COR_LINHA if tipo == "cheio" else "white", outline=COR_LINHA, width=ESCALA)
        else:
            caixa = [x, yy - 14 * ESCALA, x + 30 * ESCALA, yy + 14 * ESCALA]
            d.pieslice([x - 6 * ESCALA, yy - 16 * ESCALA, x + 26 * ESCALA, yy + 16 * ESCALA], -25, 25,
                       fill=COR_LINHA if tipo == "or" else "white", outline=COR_LINHA, width=2 * ESCALA)
        d.text((x + 40 * ESCALA, yy), rotulo, font=FONTE, fill="#111111", anchor="lm")


def gerar_png(caminho):
    folhas = posicionar(MODELO)
    altura_arvore = MARGEM + 40 * ESCALA + folhas * LINHA
    linhas_restricoes = len(RESTRICOES) + 2
    altura = int(altura_arvore + linhas_restricoes * 22 * ESCALA + MARGEM * 2)
    img = Image.new("RGB", (6 * COLUNA + MARGEM * 2, altura), "white")
    d = ImageDraw.Draw(img)
    d.text((MARGEM, MARGEM), "Modelo de características — Morah (gestão condominial)", font=FONTE_B, fill="#111111")
    desenhar(d, MODELO)
    legenda(d, MARGEM + 4 * COLUNA + 20 * ESCALA, MARGEM + 40 * ESCALA)
    y = altura_arvore + 10 * ESCALA
    d.text((MARGEM, y), f"Restrições ({len(RESTRICOES)})", font=FONTE_B, fill="#111111")
    for i, (expr, _) in enumerate(RESTRICOES, 1):
        d.text((MARGEM, y + (i + 0.4) * 22 * ESCALA), f"C{i}: {texto(expr)}", font=FONTE_U, fill="#111111")
    img.save(caminho)
    return img.size


# ------------------------------------------------------------------ dados para o documento

def achatar(n, pai=None, saida=None):
    saida = saida if saida is not None else []
    if pai is None:
        relacao = "Raiz"
    elif pai["grupo"] == "or":
        relacao = "Or Group"
    elif pai["grupo"] == "alt":
        relacao = "Alternative Group"
    else:
        relacao = "Mandatory" if n["obrigatoria"] else "Optional"
    saida.append({"nome": n["nome"], "pai": pai["nome"] if pai else "", "tipo": "Abstract" if n["abstrata"] else "Concrete",
                  "relacao": relacao, "grupoDosFilhos": {"and": "And", "or": "Or", "alt": "Alternative"}[n["grupo"]] if n["filhos"] else "",
                  "por_que": n["por_que"]})
    for c in n["filhos"]:
        achatar(c, n, saida)
    return saida


def checar():
    """Toda variável das restrições é uma característica do modelo, e os nomes são únicos."""
    nomes = [x["nome"] for x in achatar(MODELO)]
    assert len(nomes) == len(set(nomes)), "nomes repetidos"

    def vars_(e):
        return [e[1]] if e[0] == "var" else [v for s in e[1:] for v in vars_(s)]

    for e, _ in RESTRICOES:
        for v in vars_(e):
            assert v in nomes, f"restrição usa característica inexistente: {v}"
    operadores = {op for e, _ in RESTRICOES for op in str(e).split("'") if op in ("imp", "conj", "disj", "not")}
    assert operadores == {"imp", "conj", "disj", "not"}, operadores
    assert len(RESTRICOES) >= 10
    return nomes


if __name__ == "__main__":
    nomes = checar()
    with open(os.path.join(AQUI, "model.xml"), "w", encoding="utf-8") as arq:
        arq.write(gerar_xml())
    tamanho = gerar_png(os.path.join(AQUI, "modelo-caracteristicas.png"))
    tabela = achatar(MODELO)
    with open(os.path.join(AQUI, "modelo.json"), "w", encoding="utf-8") as arq:
        json.dump({"caracteristicas": tabela,
                   "restricoes": [{"id": f"C{i}", "expr": texto(e), "por_que": p} for i, (e, p) in enumerate(RESTRICOES, 1)]},
                  arq, ensure_ascii=False, indent=2)
    contagem = {k: sum(1 for x in tabela if x[campo] == k) for campo, ks in
                (("tipo", ["Abstract", "Concrete"]), ("relacao", ["Mandatory", "Optional", "Or Group", "Alternative Group"])) for k in ks}
    print(f"{len(nomes)} características, {len(RESTRICOES)} restrições, PNG {tamanho}, {contagem}")
