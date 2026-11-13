package br.com.morah.config;

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

import java.util.Set;

/**
 * Database First: as tabelas daqui nascem no schema.sql e o Hibernate não cria, altera, apaga nem trunca.
 * As demais seguem code first (ddl-auto: update). A validação continua valendo para todas.
 */
public class FiltroSchemaDatabaseFirst implements SchemaFilterProvider {

    private static final Set<String> TABELAS_DATABASE_FIRST = Set.of("veiculo");

    private static final SchemaFilter SEM_TABELAS_DATABASE_FIRST = new SchemaFilter() {
        @Override
        public boolean includeNamespace(Namespace namespace) {
            return true;
        }

        @Override
        public boolean includeTable(Table tabela) {
            return !TABELAS_DATABASE_FIRST.contains(tabela.getName());
        }

        @Override
        public boolean includeSequence(Sequence sequencia) {
            return true;
        }
    };

    @Override
    public SchemaFilter getCreateFilter() {
        return SEM_TABELAS_DATABASE_FIRST;
    }

    @Override
    public SchemaFilter getDropFilter() {
        return SEM_TABELAS_DATABASE_FIRST;
    }

    @Override
    public SchemaFilter getTruncatorFilter() {
        return SEM_TABELAS_DATABASE_FIRST;
    }

    @Override
    public SchemaFilter getMigrateFilter() {
        return SEM_TABELAS_DATABASE_FIRST;
    }

    @Override
    public SchemaFilter getValidateFilter() {
        return SchemaFilter.ALL;
    }
}
