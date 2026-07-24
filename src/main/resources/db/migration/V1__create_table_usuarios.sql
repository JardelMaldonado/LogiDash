CREATE SEQUENCE IF NOT EXISTS usuarios_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS usuarios (
    id      BIGINT NOT NULL DEFAULT nextval('usuarios_seq'),
    nome    VARCHAR(255) NOT NULL,
    email   VARCHAR(255) NOT NULL,
    senha   VARCHAR(255) NOT NULL,
    role    VARCHAR(50) NOT NULL,
    ativo   BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
    );

ALTER SEQUENCE usuarios_seq OWNED BY usuarios.id;