package br.com.morah.pattern.singleton;

import java.security.SecureRandom;

/**
 * Singleton (GoF clássico, idiom "holder").
 * Problema que resolve: SecureRandom é caro de criar; com uma única instância
 * todos os códigos de 6 dígitos (retirada de encomenda e autorização de terceiro)
 * saem do mesmo gerador, sem recriá-lo a cada requisição.
 */
public final class GeradorCodigo {

    private final SecureRandom aleatorio = new SecureRandom();

    // construtor privado: ninguém de fora consegue dar "new"
    private GeradorCodigo() {
    }

    // a classe Holder só é carregada no primeiro getInstance(): criação preguiçosa e thread-safe pela JVM
    private static class Holder {
        private static final GeradorCodigo INSTANCIA = new GeradorCodigo();
    }

    public static GeradorCodigo getInstance() {
        return Holder.INSTANCIA;
    }

    /** Código numérico de 6 dígitos, com zeros à esquerda (ex.: "004217"). */
    public String gerarCodigo() {
        return String.format("%06d", aleatorio.nextInt(1_000_000));
    }
}
