# Parte III: Database First com Telosys

Projeto Telosys que lê o banco SQLite do Morah e gera, **sem código escrito à mão**:
- o backend REST de CRUD (entidade JPA, repositório Spring Data, DTO, service e controller por tabela);
- as telas web de CRUD (uma página por tabela e um índice).

O resultado é uma aplicação Spring Boot que roda sozinha na porta 8081.

## O que tem aqui
| Caminho | O que é |
|---|---|
| `TelosysTools/databases.yaml` | conexão JDBC com `../../morah.db` (o banco do backend) |
| `TelosysTools/lib/sqlite-jdbc-3.49.1.0.jar` | driver JDBC do SQLite (fora do Git, ver abaixo) |
| `TelosysTools/models/morah/` | **modelo criado a partir do banco** (`nm morah morah`): um `.entity` por tabela |
| `TelosysTools/templates/java-rest-springboot-jpa-basic/` | bundle oficial (github.com/telosys-templates), sem alterações |
| `TelosysTools/templates/java-rest-springboot3-sqlite/` | cópia adaptada do oficial para Spring Boot 3.5 + SQLite (ver o README dele) |
| `TelosysTools/templates/morah-telas-crud/` | bundle próprio que gera as telas HTML de CRUD |
| `pom.xml`, `src/` | **código gerado** (135 arquivos de backend e 29 de telas) |
| `telosys-cli/` | a CLI do Telosys 4.3.0 (jar, fora do Git, ver abaixo) |

## Roteiro da demonstração (≈5 min)

Pré-requisitos: JDK 21, Maven, e o banco do backend criado (`mvn spring-boot:run` no morah-backend).
Os dois `.jar` não vão para o Git (são binários); numa cópia nova, coloque-os assim:
- CLI do Telosys 4.3.0: baixe
  https://github.com/telosys-tools-bricks/telosys-cli/releases/download/4.3.0-001/telosys-cli-4.3.0-001.zip
  e extraia o `telosys-cli-4.3.0-001.jar` em `telosys-cli/`;
- driver SQLite (já baixado pelo Maven ao compilar o morah-backend):
  `cp ~/.m2/repository/org/xerial/sqlite-jdbc/3.49.1.0/sqlite-jdbc-3.49.1.0.jar TelosysTools/lib/`.

1. **Mostrar o banco** (a fonte da verdade): `sqlite3 ../../morah.db ".schema veiculo"`.
2. **Apagar o código gerado** para provar que nada é escrito à mão: `rm -rf src pom.xml target`.
3. **Abrir a CLI** nesta pasta: `java -jar telosys-cli/telosys-cli-4.3.0-001.jar` e rodar:
   ```
   h .
   cdb morah -t                      # conecta no SQLite e lista as 27 tabelas
   dm morah                          # apaga o modelo antigo (responda y)
   nm morah morah                    # recria o modelo a partir do banco: 1 entidade por tabela
   b java-rest-springboot3-sqlite
   gen * * -y                        # backend: entidade, repositório, DTO, service e controller
   b morah-telas-crud
   gen * * -y                        # telas: index.html + uma página de CRUD por tabela
   q
   ```
   O `nm` recusa um modelo que já existe; por isso o `dm` antes. O `nm` já deixa o modelo novo como atual.
4. **Copiar o banco para a demonstração** (a aplicação gerada usa a cópia, nunca o banco do backend):
   `cp ../../morah.db morah-demo.db`
5. **Subir a aplicação gerada**: `mvn spring-boot:run` e abrir http://localhost:8081.
6. Abrir **Veiculo** e mostrar: listar (vem o carro do seed), cadastrar, editar e excluir.
   A API também fica no Swagger: http://localhost:8081/swagger-ui.html.
7. Mudar o banco e gerar de novo: por exemplo, `ALTER TABLE veiculo ADD COLUMN ano INTEGER;` na cópia
   `morah-demo.db` (e no `databases.yaml` apontar para ela), repetir `dm` + `nm` + os dois `gen`, reiniciar a
   aplicação e mostrar o campo `ano` na tela.

## Limites conhecidos
- O driver do SQLite não informa as chaves estrangeiras ao Telosys, então as relações viram colunas
  `unidadeId`, `blocoId`... (sem `@ManyToOne`). Num PostgreSQL o mesmo modelo traria os links.
- O SQLite também não marca o `id` como autoincremento, então a tela pede o `id` ao cadastrar.
- `vistoria_fotos_url` não tem chave primária (é uma coleção do Hibernate) e é pulada pelos dois bundles.
- O `PATCH` gerado pelo bundle oficial é um esqueleto ("implement here the partial update"); as telas
  usam `PUT /{id}`, que grava o registro inteiro.
