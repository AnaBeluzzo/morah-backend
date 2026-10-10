-- Dados mínimos de demonstração. INSERT OR IGNORE com ids fixos: só insere se ainda não existir.
-- Datas: texto 'yyyy-MM-dd HH:mm:ss.SSS'; instantes em UTC.
-- Todos os logins usam a senha demo123 (gravada só como hash BCrypt).

-- ===== Login: condomínio, blocos, unidades, pessoas e perfis
INSERT OR IGNORE INTO condominio (id, nome) VALUES (1, 'Condomínio Demo');

INSERT OR IGNORE INTO bloco (id, condominio_id, nome) VALUES (1, 1, 'Bloco A');
INSERT OR IGNORE INTO bloco (id, condominio_id, nome) VALUES (2, 1, 'Bloco B');

INSERT OR IGNORE INTO unidade (id, bloco_id, identificacao, tipo, fracao_ideal, status) VALUES (1, 1, 'Apto 302', 'apartamento', 0.5, 'ocupada');
INSERT OR IGNORE INTO unidade (id, bloco_id, identificacao, tipo, fracao_ideal, status) VALUES (2, 2, 'Apto 502', 'apartamento', 0.5, 'ocupada');

INSERT OR IGNORE INTO pessoa (id, nome, email, senha_hash) VALUES (1, 'Helena Prado', 'helena.prado@demo.com.br', '$2a$10$6J6a131OcqNr.V/16Ir0/Oxr78o32WHH1sRjEIyeaC6fm9mzVG9mW');
INSERT OR IGNORE INTO pessoa (id, nome, email, senha_hash) VALUES (2, 'Rafael Nunes', 'rafael.nunes@demo.com.br', '$2a$10$6J6a131OcqNr.V/16Ir0/Oxr78o32WHH1sRjEIyeaC6fm9mzVG9mW');
INSERT OR IGNORE INTO pessoa (id, nome, email, senha_hash) VALUES (3, 'Camila Duarte', 'camila.duarte@demo.com.br', '$2a$10$6J6a131OcqNr.V/16Ir0/Oxr78o32WHH1sRjEIyeaC6fm9mzVG9mW');
INSERT OR IGNORE INTO pessoa (id, nome, email, senha_hash) VALUES (4, 'Portaria Central', 'portaria@demo.com.br', '$2a$10$6J6a131OcqNr.V/16Ir0/Oxr78o32WHH1sRjEIyeaC6fm9mzVG9mW');

INSERT OR IGNORE INTO pessoa_perfil (id, pessoa_id, perfil, condominio_id, unidade_id) VALUES (1, 1, 'SINDICO', 1, NULL);
INSERT OR IGNORE INTO pessoa_perfil (id, pessoa_id, perfil, condominio_id, unidade_id) VALUES (2, 2, 'PROPRIETARIO', 1, 1);
INSERT OR IGNORE INTO pessoa_perfil (id, pessoa_id, perfil, condominio_id, unidade_id) VALUES (3, 3, 'MORADOR', 1, 2);
INSERT OR IGNORE INTO pessoa_perfil (id, pessoa_id, perfil, condominio_id, unidade_id) VALUES (4, 4, 'PORTARIA', 1, NULL);

-- ===== Avisos: um aviso para todo o condomínio
INSERT OR IGNORE INTO aviso (id, condominio_id, titulo, conteudo, categoria, prioridade, publico_tipo, autor_id, publicado_em, expira_em)
VALUES (1, 1, 'Bem-vindos ao Morah', 'Este é o novo canal de comunicação do condomínio.', 'geral', 'NORMAL', 'CONDOMINIO', 1, '2026-10-01 12:00:00.000', NULL);

-- ===== Financeiro: taxa da competência 2026-09 e uma cobrança por unidade
INSERT OR IGNORE INTO taxa (id, condominio_id, descricao, competencia, valor, tipo_rateio, vencimento_dia)
VALUES (1, 1, 'Taxa condominial', '2026-09', 1000.00, 'IGUALITARIO', 10);

INSERT OR IGNORE INTO cobranca (id, unidade_id, taxa_id, competencia, vencimento, valor_original, multa, juros, status)
VALUES (1, 1, 1, '2026-09', '2026-09-10 00:00:00.000', 500.00, 0, 0, 'PENDENTE');
INSERT OR IGNORE INTO cobranca (id, unidade_id, taxa_id, competencia, vencimento, valor_original, multa, juros, status)
VALUES (2, 2, 1, '2026-09', '2026-09-10 00:00:00.000', 500.00, 0, 0, 'PENDENTE');
