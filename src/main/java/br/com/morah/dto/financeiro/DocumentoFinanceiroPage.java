package br.com.morah.dto.financeiro;

import br.com.morah.dto.comum.PageMetadata;

import java.util.List;

public record DocumentoFinanceiroPage(List<DocumentoFinanceiro> content, PageMetadata page) {
}
