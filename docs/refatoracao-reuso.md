# Refatoração: compactação de código por reuso

Técnica aplicada: **Extract Method / DRY** (*Don't Repeat Yourself*): achar trechos repetidos,
extrair cada um para um único lugar e trocar as cópias por uma chamada. O comportamento não muda;
os 103 testes de integração e de padrões passam antes e depois.

## Roteiro para o vídeo

### 1. Abertura (≈30 s)
- O que é DRY: cada regra deve existir em **um** lugar só.
- Problema: copiar e colar faz uma alteração exigir N edições, e é fácil esquecer uma.
- Mostrar `mvn test` verde **antes** de mexer (rede de segurança).

### 2. Como as repetições foram encontradas (≈1 min)
Buscas no terminal que mostram as cópias:

```bash
grep -rn "PageMetadata.de(pagina)" src/main/java
```

```bash
grep -rn "naoEncontrado(\"Unidade" src/main/java
```

```bash
grep -rn "atStartOfDay" src/main/java
```

E leitura dos services: `new Ocorrencia()` e `new VinculoUnidade()` montados campo a campo em dois lugares.

### 3. As cinco extrações (≈1 min cada)

| # | Repetição | Cópias | Para onde foi |
|---|---|---|---|
| 1 | Montar a página do contrato (`stream().map(Dto::de).toList()` + `PageMetadata.de`) | 7 | `PageMetadata.paginar(...)` |
| 2 | Buscar unidade do condomínio ou 404 | 3 | `UnidadeRepository.buscarDoCondominio(...)` (método `default`) |
| 3 | Criar ocorrência aberta | 2 | `Ocorrencia.aberta(...)` (fábrica estática) |
| 4 | Criar vínculo de unidade | 2 | `UnidadeService.salvarVinculo(...)` (método privado) |
| 5 | Início do dia no fuso do servidor | 3 | reuso do `ReservaService.horario(data, 0)` que **já existia** |

Para cada uma, mostrar o "antes → depois" no diff (`git diff`). Exemplo da nº 1:

```java
// antes (repetido em 7 métodos)
return new EncomendaPage(pagina.getContent().stream().map(Encomenda::de).toList(),
        PageMetadata.de(pagina));

// depois
return PageMetadata.paginar(pagina, Encomenda::de, EncomendaPage::new);
```

Pontos para comentar:
- **nº 1** usa generics + referência a construtor (`EncomendaPage::new`) para servir qualquer tipo de página.
- **nº 2** usa método `default` em interface do Spring Data: o Spring não tenta gerar consulta para ele.
- **nº 3** coloca a regra "nasce aberta, com data de agora" dentro da própria entidade.
- **nº 5** é o reuso mais barato: o helper já estava no projeto, só não era usado.

### 4. Validação (≈30 s)
- `mvn clean test` → 103 testes, 0 falhas.
- `git diff --stat` → 10 arquivos.

### 5. Resultado e limites (≈30 s)
- Services: 1430 → 1408 linhas; 17 blocos repetidos viraram 5 helpers.
- O total do projeto fica praticamente igual (os helpers têm Javadoc), então o ganho não é só
  "menos linhas": cada regra mudou para **um** lugar. Mudar o formato da paginação agora é editar
  1 método, não 7.
- O que ficou de fora de propósito: construtores longos de injeção (o Lombok `@RequiredArgsConstructor`
  resolveria, mas o projeto restringe o Lombok às entidades) e os `@RequestParam page/size` dos controllers
  (anotações de validação, quase não há o que extrair).
