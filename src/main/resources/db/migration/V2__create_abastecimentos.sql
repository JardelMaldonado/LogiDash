CREATE SEQUENCE IF NOT EXISTS abastecimentos_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS abastecimentos (
    id                  INTEGER NOT NULL DEFAULT nextval('abastecimentos_seq'),
    identificador       BIGINT NOT NULL,
    data                TIMESTAMP NOT NULL,
    placa               VARCHAR(255) NOT NULL,
    nome_motorista      VARCHAR(255) NOT NULL,
    razao_social_posto  VARCHAR(255) NOT NULL,
    posto_interno       BOOLEAN NOT NULL,
    CONSTRAINT pk_abastecimentos PRIMARY KEY (id),
    CONSTRAINT uk_abastecimento_identificador UNIQUE (identificador)
    );

ALTER SEQUENCE abastecimentos_seq OWNED BY abastecimentos.id;