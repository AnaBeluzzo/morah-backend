-- Tabelas DATABASE FIRST: o banco é a fonte da verdade e a entidade JPA é mapeada a partir daqui.
-- O Hibernate não cria nem altera estas tabelas (ver config/FiltroSchemaDatabaseFirst).
-- Roda a cada subida, depois do Hibernate e antes do data.sql; IF NOT EXISTS mantém os dados.

-- ===== Minha Unidade: veículos dos moradores
CREATE TABLE IF NOT EXISTS veiculo (
    id         INTEGER     PRIMARY KEY,
    unidade_id INTEGER     NOT NULL REFERENCES unidade (id),
    placa      VARCHAR(7)  NOT NULL UNIQUE,
    modelo     VARCHAR(60) NOT NULL,
    cor        VARCHAR(30),
    tipo       VARCHAR(10) NOT NULL CHECK (tipo IN ('CARRO', 'MOTO'))
);
