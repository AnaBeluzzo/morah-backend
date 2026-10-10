# Morah — Backend

Backend do **Morah**, sistema de gestão de condomínios desenvolvido como projeto acadêmico
do 6º período de Engenharia de Software.

O contrato da API está em [`morah-openapi.yaml`](morah-openapi.yaml) ("Morah API — Gestão
Condominial", v2.0.0) e é a fonte da verdade: endpoints, campos, códigos HTTP e perfis seguem
exatamente o que está nele.

## Autores

- _(preencher com os nomes dos alunos)_

## Stack

- Java 21, Spring Boot 3.5, Maven
- Spring Web, Spring Data JPA e Bean Validation
- Spring Security + `spring-boot-starter-oauth2-resource-server` (só o suporte a JWT do Nimbus:
  `NimbusJwtEncoder`/`NimbusJwtDecoder`, HS256 com segredo próprio — sem Authorization Server)
- BCrypt para senhas; HMAC-SHA256 (`javax.crypto.Mac`) para o webhook
- SQLite em arquivo (`morah.db`) com `sqlite-jdbc` + `hibernate-community-dialects`
- Lombok (só `@Getter`, `@Setter` e `@NoArgsConstructor` nas entidades)
- springdoc-openapi (Swagger UI) para testar as APIs no navegador
- JUnit 5, MockMvc e `spring-security-test`

## Como rodar

Pré-requisitos: JDK 21 ou mais recente e Maven 3.9+.

```bash
mvn spring-boot:run
```

A API sobe em **http://localhost:8080/v1** e cria o arquivo `morah.db` na raiz do projeto, com
os dados de demonstração do `src/main/resources/data.sql` (só são inseridos se ainda não existirem).
Para recomeçar do zero, pare a aplicação e apague o `morah.db`.

Se no Windows o Tomcat falhar com `Unable to establish loopback connection` (problema do JDK com a
pasta temporária usada pelos sockets locais), aponte essa pasta para outro diretório:

```bash
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=C:/Users/Public"
```

Swagger UI: **http://localhost:8080/v1/swagger-ui.html** — faça o login em `POST /auth/login`,
copie o `accessToken` e cole no botão **Authorize**.

## Como testar

```bash
mvn test
```

Os testes de integração usam um banco separado (`target/test.db`, recriado a cada `mvn clean test`),
fazem login de verdade com os usuários do seed e rodam cada teste numa transação desfeita ao final.

## Usuários de demonstração

Todos com a senha **`demo123`** (no banco fica só o hash BCrypt).

| E-mail | Pessoa | Perfil | Unidade |
|---|---|---|---|
| `helena.prado@demo.com.br` | Helena Prado | `sindico` | — |
| `rafael.nunes@demo.com.br` | Rafael Nunes | `proprietario` | Bloco A · Apto 302 (id 1) |
| `camila.duarte@demo.com.br` | Camila Duarte | `morador` | Bloco B · Apto 502 (id 2) |
| `portaria@demo.com.br` | Portaria Central | `portaria` | — |

O seed também tem: a taxa da competência 2026-09 com uma cobrança por unidade, um aviso para o
condomínio, o "Salão de festas", uma encomenda aguardando retirada e uma autorização de visita
pendente no Apto 302, a personalização do condomínio e os seis módulos ativos.

## Exemplos com curl

Login (guarde o `accessToken` e o `refreshToken` da resposta):

```bash
curl -X POST http://localhost:8080/v1/auth/login -H "Content-Type: application/json" -d "{\"email\":\"rafael.nunes@demo.com.br\",\"senha\":\"demo123\"}"
```

Chamada autenticada com Bearer:

```bash
curl http://localhost:8080/v1/home/dashboard -H "Authorization: Bearer SEU_ACCESS_TOKEN"
```

Renovar o token (o refresh token usado deixa de valer):

```bash
curl -X POST http://localhost:8080/v1/auth/refresh -H "Content-Type: application/json" -d "{\"refreshToken\":\"SEU_REFRESH_TOKEN\"}"
```

Logout (a partir daqui o access token responde 401):

```bash
curl -X POST http://localhost:8080/v1/auth/logout -H "Authorization: Bearer SEU_ACCESS_TOKEN"
```

## Estrutura

Pacotes em `br.com.morah`:

| Pacote | Conteúdo |
|---|---|
| `controller` | Controllers REST (um por área do contrato) |
| `service` | Regras de negócio |
| `repository` | Interfaces Spring Data JPA |
| `model` | Entidades JPA |
| `dto` | Um `record` por schema de objeto do contrato e um `enum` por schema de enum |
| `exception` | `ApiException` e o `GlobalExceptionHandler` (ProblemDetail, RFC 9457) |
| `config` | `SecurityConfig`, `WebConfig`, `ModuloAtivoInterceptor`, `OpenApiConfig` |
| `security` | `TokenService` (gera o JWT) e `UsuarioLogado` (claims do token) |
| `pattern` | Os design patterns (ver abaixo) |

## Endpoints → classe e método

| Endpoint | Classe | Método |
|---|---|---|
| POST /auth/login | AuthController | login |
| GET /auth/me | AuthController | buscarMe |
| POST /auth/contexto | AuthController | selecionarContexto |
| POST /auth/refresh | AuthController | renovarToken |
| POST /auth/logout | AuthController | logout |
| GET /home/dashboard | DashboardController | buscarDashboard |
| GET /financeiro/cobrancas | FinanceiroController | buscarCobrancas |
| GET /financeiro/cobrancas/{cobrancaId} | FinanceiroController | buscarCobranca |
| GET /financeiro/cobrancas/{cobrancaId}/pix | FinanceiroController | gerarPix |
| POST /financeiro/cobrancas/{cobrancaId}/baixa-manual | FinanceiroController | baixarCobrancaManualmente |
| POST /financeiro/webhooks/pagamentos | FinanceiroController | receberWebhookPagamento |
| GET /financeiro/documentos | FinanceiroController | buscarDocumentosFinanceiros |
| POST /financeiro/documentos | FinanceiroController | criarDocumentoFinanceiro |
| POST /financeiro/taxas | FinanceiroController | criarTaxa |
| POST /financeiro/taxas/{taxaId}/gerar-cobrancas | FinanceiroController | gerarCobrancasEmLote |
| GET /financeiro/inadimplencia | FinanceiroController | buscarResumoInadimplencia |
| POST /portaria/visitantes | PortariaController | registrarVisitante |
| GET /portaria/autorizacoes | PortariaController | buscarAutorizacoes |
| GET /portaria/autorizacoes/{autorizacaoId} | PortariaController | buscarAutorizacao |
| PATCH /portaria/autorizacoes/{autorizacaoId}/decisao | PortariaController | decidirAutorizacao |
| POST /portaria/autorizacoes/{autorizacaoId}/reenviar | PortariaController | reenviarAutorizacao |
| GET /portaria/acessos | PortariaController | buscarAcessos |
| POST /portaria/acessos | PortariaController | registrarAcesso |
| GET /avisos | AvisoController | buscarAvisos |
| POST /avisos | AvisoController | criarAviso |
| GET /avisos/{avisoId} | AvisoController | buscarAviso |
| PATCH /avisos/{avisoId} | AvisoController | alterarAviso |
| DELETE /avisos/{avisoId} | AvisoController | deletarAviso |
| GET /areas-comuns | ReservaController | buscarAreasComuns |
| GET /areas-comuns/{areaComumId}/disponibilidade | ReservaController | buscarDisponibilidade |
| GET /reservas | ReservaController | buscarReservas |
| POST /reservas | ReservaController | criarReserva |
| GET /reservas/{reservaId} | ReservaController | buscarReserva |
| DELETE /reservas/{reservaId} | ReservaController | deletarReserva |
| POST /reservas/{reservaId}/vistoria | ReservaController | registrarVistoria |
| GET /unidades/minha | UnidadeController | buscarMinhaUnidade |
| GET /unidades/minha/vinculos | UnidadeController | buscarVinculos |
| POST /unidades/minha/vinculos/solicitacoes | UnidadeController | solicitarVinculo |
| POST /unidades/minha/inquilinos | UnidadeController | criarInquilino |
| DELETE /unidades/minha/inquilinos/{vinculoId} | UnidadeController | deletarInquilino |
| GET /unidades/{unidadeId} | UnidadeController | buscarUnidade |
| GET /unidades/{unidadeId}/vinculos/solicitacoes | UnidadeController | buscarSolicitacoesVinculo |
| PATCH /unidades/{unidadeId}/vinculos/solicitacoes/{solicitacaoId} | UnidadeController | decidirSolicitacaoVinculo |
| GET /encomendas | EncomendaController | buscarEncomendas |
| POST /encomendas | EncomendaController | criarEncomenda |
| GET /encomendas/{encomendaId} | EncomendaController | buscarEncomenda |
| POST /encomendas/{encomendaId}/autorizacoes-retirada | EncomendaController | autorizarRetiradaTerceiro |
| POST /encomendas/{encomendaId}/retirada | EncomendaController | confirmarRetirada |
| POST /encomendas/{encomendaId}/ocorrencias | EncomendaController | abrirOcorrencia |
| GET /notificacoes | NotificacaoController | buscarNotificacoes |
| PATCH /notificacoes/{notificacaoId}/lida | NotificacaoController | marcarNotificacaoLida |
| GET /condominio/personalizacao | CondominioController | buscarPersonalizacao |
| PATCH /condominio/personalizacao | CondominioController | alterarPersonalizacao |
| GET /condominio/modulos | CondominioController | buscarModulos |
| PATCH /condominio/modulos/{modulo} | CondominioController | alterarModulo |

Cada controller chama o service de mesmo nome de área (`AvisoController` → `AvisoService` etc.;
`CondominioController` → `CondominioService`; `DashboardController` → montadores do pacote `pattern`).

## Autenticação e permissões

- `POST /auth/login` confere a senha com BCrypt e cria uma `Sessao` (id UUID, contexto ativo,
  refresh token UUID válido por 7 dias). O contexto inicial é o primeiro `PessoaPerfil` da pessoa.
- O access token é um JWT HS256 de 1 hora com as claims `sub`, `sid`, `perfil`, `condominio_id`
  e `unidade_id` (ausente para síndico e portaria).
- O `JwtDecoder` valida assinatura e expiração e, com um validador próprio, se a sessão `sid`
  ainda existe: por isso o logout invalida o token na hora.
- A claim `perfil` vira o papel `ROLE_<PERFIL>`; `proprietario` também recebe `ROLE_MORADOR`.
- Cada método de controller tem `@PreAuthorize` com exatamente os perfis do contrato.
- Condomínio, unidade e perfil vêm sempre do token (`UsuarioLogado`). Recurso de outra unidade → 403;
  de outro condomínio → 404.
- Módulo desativado → 403 em todas as rotas dele (`ModuloAtivoInterceptor`), inclusive para o síndico.

## Design patterns

São 8 usos: 3 Template Method, 3 Strategy e 2 Singleton. Todos estão em `br.com.morah.pattern`.

| Pattern | Onde | Problema que resolve |
|---|---|---|
| Template Method | `BaseCrudController<T>` (usado por `AvisoController`, `ReservaController`, `EncomendaController`) | Fluxo fixo por requisição: usuário logado → validar payload → executar → verificar escopo → responder. Os controllers só implementam os ganchos `validar()` e `verificarEscopo()`. O fluxo roda numa transação: se o escopo for negado, o que foi executado é desfeito. |
| Template Method | `NotificacaoTemplate<E>` (subclasses `NotificacaoAviso`, `NotificacaoVisitante`, `NotificacaoEncomenda`, `NotificacaoReserva`, `NotificacaoFinanceiro`, `NotificacaoConvite`) | Toda notificação segue montar payload → escolher canal → enviar → registrar no banco. Cada módulo só escreve o texto. |
| Template Method | `ProcessamentoPagamentoTemplate<E>` (`BaixaManualProcessador`, `WebhookPagamentoProcessador`) | Quitar uma cobrança: validar origem → localizar → verificar → marcar como pago → notificar. Só a validação da origem muda (síndico do condomínio × assinatura HMAC); o webhook `falhou` para antes de marcar como pago. |
| Strategy | `EstrategiaRateio` (`RateioIgualitario`, `RateioFracaoIdeal`) | Valor de cada unidade na geração em lote, escolhido pelo `tipoRateio` da taxa. |
| Strategy | `CanalNotificacao` (`CanalPush`, `CanalEmail`) | Forma de envio escolhida no passo "escolher canal" do `NotificacaoTemplate` (financeiro e convite por e-mail; o resto por push). Envio simulado com log. |
| Strategy | `MontadorDashboard` (`DashboardMoradorMontador`, `DashboardSindicoMontador`, `DashboardPortariaMontador`) | Tela inicial diferente por perfil, escolhida pelo perfil ativo no `DashboardController`. |
| Singleton (GoF, idiom holder) | `GeradorCodigo` | Uma única instância de `SecureRandom` (cara de criar) para os códigos de 6 dígitos das encomendas e das autorizações de terceiros. |
| Singleton (enum) | `CacheModulosAtivos.INSTANCE` | Mapa em memória dos módulos ativos por condomínio, para o interceptor não ir ao banco a cada requisição. Invalidado em `alterarModulo`. |

O `TokenService` é um `@Service` comum do Spring e não é tratado como pattern.

## Decisões tomadas por simplicidade

- **Datas no SQLite**: gravadas como texto `yyyy-MM-dd HH:mm:ss.SSS` (parâmetros `date_class=TEXT`
  e `date_string_format` na URL do JDBC), para o `data.sql` ficar legível. Instantes ficam em UTC.
- **Horários "do dia"** (reservas de 08:00 às 22:00, filtro `data` de acessos e reservas, encomendas
  recebidas hoje) usam o fuso do servidor.
- **Status calculados na leitura**, sem job agendado: cobrança `atrasado`, autorização de visita
  `expirada` (30 min) e autorização de retirada `expirada`. No banco ficam só os status "reais".
- **`logoUrl` nulo na personalização**: o PATCH recebe o corpo como `JsonNode` só nesse endpoint, para
  diferenciar "campo ausente" (mantém a logo) de `"logoUrl": null` (remove). O `JsonNode` é convertido
  para `PersonalizacaoUpdateRequest` e validado com o `Validator`. Testamos `Optional<String>` no
  record, mas o Jackson entrega `Optional.empty()` também quando o campo não vem, o que apagaria a logo.
- **Campos aceitos mas não gravados** (porque nenhum schema de resposta os expõe): meio, valor, data
  e observação da baixa manual; `fotoUrl` do registro de visitante e do acesso; `retiradoPorNome` e
  `retiradoPorDocumento` da retirada; `observacao` e `justificativa` das solicitações e decisões.
- **Webhook**: cobrança inexistente, já paga ou cancelada responde 400 (o contrato do webhook só
  prevê 202, 400 e 401). O processamento é síncrono.
- **Convite de inquilino**: o `TipoNotificacao` do contrato não tem "convite", então essa notificação
  é gravada sem `tipo` (o campo é omitido no JSON). O inquilino nasce sem senha; o envio do convite
  por e-mail é simulado com log.
- **Cadastro de inquilino** com e-mail já usado por outra pessoa → 400.
- **Encerrar inquilino** apaga o `PessoaPerfil` de morador daquela unidade e todas as sessões da pessoa.
- **Destinatários das notificações**: "moradores da unidade" = pessoas com perfil naquela unidade;
  aviso para o condomínio notifica todas as pessoas com perfil no condomínio, menos o autor.
- **Escopo no `BaseCrudController`**: para o cancelamento de reserva, a checagem de status (409)
  acontece antes da checagem de unidade (403).
- **Dashboard**: `unidade` também some quando o módulo Minha Unidade está desativado;
  `ocorrenciasAbertas` não pertence a um módulo só e é sempre exibido.
- **Módulos**: um condomínio sem linha em `modulo_condominio` para algum módulo o considera ativo.
- **Ordenação** das listas é fixa (a mais útil para cada tela), já que `sort` não é suportado.
- **Swagger UI** (springdoc) foi adicionado a pedido, só para testar as APIs no navegador; a
  documentação gerada vem do código, e o contrato oficial continua sendo o `morah-openapi.yaml`.
