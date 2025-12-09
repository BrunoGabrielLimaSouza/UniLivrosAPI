CREATE TABLE notificacoes (
                              id BIGSERIAL PRIMARY KEY,
                              mensagem VARCHAR(255),
                              lida BOOLEAN NOT NULL DEFAULT FALSE,
                              data_criacao TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
                              usuario_id BIGINT NOT NULL,

                              CONSTRAINT fk_notificacao_usuario
                                  FOREIGN KEY (usuario_id)
                                      REFERENCES usuarios(id)
                                      ON DELETE CASCADE
);

-- Opcional: Índice para melhorar a performance da contagem de não lidas
CREATE INDEX idx_notificacoes_usuario_lida ON notificacoes(usuario_id, lida);