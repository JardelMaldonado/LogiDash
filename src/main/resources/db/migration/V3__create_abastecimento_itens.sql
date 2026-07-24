CREATE SEQUENCE IF NOT EXISTS abastecimento_itens_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS abastecimento_itens (
    id                INTEGER NOT NULL DEFAULT nextval('abastecimento_itens_seq'),
    abastecimento_id  INTEGER NOT NULL,
    tipo_combustivel  VARCHAR(255) NOT NULL,
    quantidade        NUMERIC(10,4) NOT NULL,
    valor_unitario    NUMERIC(10,4),
    valor_total       NUMERIC(10,4),
    CONSTRAINT pk_abastecimento_itens PRIMARY KEY (id),
    CONSTRAINT fk_abastecimento_itens_abastecimento
    FOREIGN KEY (abastecimento_id) REFERENCES abastecimentos (id)
    );

ALTER SEQUENCE abastecimento_itens_seq OWNED BY abastecimento_itens.id;