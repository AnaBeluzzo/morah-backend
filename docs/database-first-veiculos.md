# Database First: veículos da unidade

## A técnica
No **Database First** o banco de dados é a fonte da verdade. Primeiro a tabela é modelada em SQL, com
tipos, chaves e restrições. Depois o código (a entidade JPA) é gerado ou mapeado a partir dela. É o
caminho inverso do **Code First**, em que a entidade Java é escrita primeiro e o Hibernate cria a tabela.

O Morah nasceu code first (`spring.jpa.hibernate.ddl-auto: update`). O cadastro de **veículos** do
módulo Minha Unidade foi feito em database first, e as duas abordagens convivem no mesmo projeto.

| | Code First (resto do projeto) | Database First (veículos) |
|---|---|---|
| Fonte da verdade | entidade `@Entity` | `src/main/resources/schema.sql` |
| Quem cria a tabela | Hibernate (`ddl-auto: update`) | o script SQL |
| Restrições | só as que o JPA expressa | `UNIQUE`, `CHECK` e `REFERENCES` escritos à mão |
| Nomes de colunas | convenção do Hibernate | definidos no SQL; a entidade usa `@Table`/`@Column` explícitos |

## Passo a passo

### 1. Modelar a tabela no banco
`src/main/resources/schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS veiculo (
    id         INTEGER     PRIMARY KEY,
    unidade_id INTEGER     NOT NULL REFERENCES unidade (id),
    placa      VARCHAR(7)  NOT NULL UNIQUE,
    modelo     VARCHAR(60) NOT NULL,
    cor        VARCHAR(30),
    tipo       VARCHAR(10) NOT NULL CHECK (tipo IN ('CARRO', 'MOTO'))
);
```

Para mostrar no IntelliJ: abra o **Database** tool window, adicione um Data Source SQLite apontando
para `morah.db` (o IntelliJ baixa o driver) e rode o script no console. A tabela aparece na árvore.

### 2. Gerar a entidade a partir da tabela
No IntelliJ Ultimate: **View → Tool Windows → Persistence** → clique com o botão direito no módulo →
**Generate Persistence Mapping → By Database Schema**. Escolha o data source, marque a tabela
`veiculo` e o pacote `br.com.morah.model.minhaunidade`. Com o plugin JPA Buddy, o caminho é
**New → JPA Entities from DB**.

Ajustes feitos no código gerado, para seguir o padrão do projeto
(resultado em `model/minhaunidade/Veiculo.java`):
- getters e setters trocados por Lombok `@Getter @Setter @NoArgsConstructor`;
- `unidade_id` virou `@ManyToOne Unidade unidade`, com `@JoinColumn(name = "unidade_id")`;
- `tipo` virou o enum `TipoVeiculo`, com `@Enumerated(EnumType.STRING)`, que bate com o `CHECK`.

### 3. Tirar a tabela das mãos do Hibernate
Com `ddl-auto: update` o Hibernate tentaria criar e alterar `veiculo` a partir da entidade, e isso
desfaria o database first. A classe `config/FiltroSchemaDatabaseFirst` (um `SchemaFilterProvider`
do Hibernate) exclui a tabela dos filtros de create, migrate, drop e truncate. Ela é registrada no
`application.yml`:

```yaml
spring:
  jpa:
    properties:
      hibernate.hbm2ddl.schema_filter_provider: br.com.morah.config.FiltroSchemaDatabaseFirst
```

Ordem na subida da aplicação (`defer-datasource-initialization: true`):
1. o Hibernate cria e atualiza as tabelas code first (pulando `veiculo`);
2. o Spring roda o `schema.sql`, que cria `veiculo` se ela não existir;
3. o Spring roda o `data.sql`, que insere o carro de exemplo do Apto 302.

### 4. Construir o resto em cima da entidade
- `repository/minhaunidade/VeiculoRepository`
- `service/VeiculoService`: a placa é normalizada (`abc-1d23` → `ABC1D23`); placa repetida → 409, antes de
  chegar ao `UNIQUE` do banco
- `controller/VeiculoController`: `GET`, `POST`, `PATCH /{veiculoId}` e `DELETE /{veiculoId}` em
  `/unidades/minha/veiculos`, para morador e proprietário
- contrato `morah-openapi.yaml` v2.1.0: schemas `Veiculo`, `VeiculoCreateRequest`, `VeiculoUpdateRequest`
  e `TipoVeiculo`

## Como provar que a tabela veio do SQL
```bash
sqlite3 morah.db ".schema veiculo"
```
O resultado é o texto do `schema.sql` (`CREATE TABLE`, `CHECK (tipo IN ('CARRO', 'MOTO'))`). O Hibernate
geraria `create table` em minúsculas e outro formato de `check`. O teste
`VeiculoControllerTest.aTabelaVemDoSchemaSqlENaoDoHibernate` verifica isso automaticamente.

## Limite
Só `veiculo` é database first. Para levar o projeto inteiro, seria preciso passar o DDL de todas as
tabelas para o `schema.sql` (dá para exportar com `sqlite3 morah.db .schema`) e trocar o `ddl-auto` para
`validate`.
