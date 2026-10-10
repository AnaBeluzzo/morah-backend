package br.com.morah.pattern;

import br.com.morah.dto.ModuloCodigo;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Singleton (forma "enum", recomendada no Effective Java: a JVM garante uma única instância,
 * inclusive contra reflexão e serialização).
 * Problema que resolve: o ModuloAtivoInterceptor roda em toda requisição; guardando em memória
 * os módulos ativos de cada condomínio, ele não precisa ir ao banco toda vez.
 */
public enum CacheModulosAtivos {

    INSTANCE;

    private final Map<Long, Set<ModuloCodigo>> ativosPorCondominio = new ConcurrentHashMap<>();

    /** Devolve os módulos ativos; na primeira vez (ou após invalidar) usa o carregador para ler do banco. */
    public Set<ModuloCodigo> buscarAtivos(Long condominioId, Function<Long, Set<ModuloCodigo>> carregador) {
        return ativosPorCondominio.computeIfAbsent(condominioId, carregador);
    }

    /** Chamado quando o síndico ativa/desativa um módulo: a próxima leitura recarrega do banco. */
    public void invalidar(Long condominioId) {
        ativosPorCondominio.remove(condominioId);
    }
}
