-- Create agendamentos table with refactored schema
-- This table now only stores status and relationship with proposta
CREATE TABLE IF NOT EXISTS agendamentos (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    proposta_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_agendamentos_proposta 
        FOREIGN KEY (proposta_id) 
        REFERENCES propostas(id) 
        ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_agendamentos_proposta_id ON agendamentos(proposta_id);
CREATE INDEX idx_agendamentos_status ON agendamentos(status);
