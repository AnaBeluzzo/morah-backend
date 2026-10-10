package br.com.morah.dto;

import java.util.List;

public record DocumentoFinanceiroPage(List<DocumentoFinanceiro> content, PageMetadata page) {
}
