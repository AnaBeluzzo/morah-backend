package br.com.morah.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final ModuloAtivoInterceptor moduloAtivoInterceptor;

    public WebConfig(ObjectMapper objectMapper, ModuloAtivoInterceptor moduloAtivoInterceptor) {
        this.objectMapper = objectMapper;
        this.moduloAtivoInterceptor = moduloAtivoInterceptor;
    }

    /**
     * Enums em query/path (?status=aguardando_retirada, /modulos/minha_unidade) usam
     * o mesmo valor do JSON (@JsonValue), e não o nome da constante Java.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new ConverterFactory<String, Enum<?>>() {
            @Override
            public <E extends Enum<?>> Converter<String, E> getConverter(Class<E> tipo) {
                return valor -> objectMapper.convertValue(valor, tipo);
            }
        });
    }

    /** Toda requisição passa pela checagem de módulo ativo antes de chegar ao controller. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(moduloAtivoInterceptor);
    }
}
