# java-rest-springboot3-sqlite

Cópia adaptada do bundle oficial `java-rest-springboot-jpa-basic` (github.com/telosys-templates).
Gera um backend REST de CRUD (entidade JPA, repositório Spring Data, DTO, service e controller por tabela).

Adaptações para rodar com as mesmas versões do morah-backend:
- Spring Boot 2.7 → 3.5.16 e Java 17 → 21;
- `javax.persistence` → `jakarta.persistence` (no template da entidade);
- driver PostgreSQL → SQLite (`sqlite-jdbc` + `hibernate-community-dialects`);
- ModelMapper → `BeanUtils.copyProperties` do Spring (no `GenericService`);
- springdoc 1.6 → `springdoc-openapi-starter-webmvc-ui` 2.8.17 e plugin `spring-boot-maven-plugin`.
- tabela sem chave primária é pulada (`#cancel`) em vez de interromper com erro.
- `PagingAndSortingRepository` → `JpaRepository` (no Spring Data 3 ele não herda mais o `CrudRepository`).
