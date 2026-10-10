package br.com.morah.dto.dashboard;

/** Os três formatos possíveis da tela inicial; o campo "perfil" diz qual deles veio. */
public sealed interface Dashboard permits DashboardMorador, DashboardSindico, DashboardPortaria {
}
