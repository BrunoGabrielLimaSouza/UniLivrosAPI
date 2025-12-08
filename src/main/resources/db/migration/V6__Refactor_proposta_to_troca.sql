-- Remover relacionamento entre Troca e Agendamento
ALTER TABLE trocas DROP CONSTRAINT IF EXISTS fk_troca_agendamento;
ALTER TABLE trocas DROP COLUMN IF EXISTS agendamento_id;

-- Adicionar relacionamento direto entre Troca e Proposta
ALTER TABLE trocas ADD COLUMN IF NOT EXISTS proposta_id BIGINT;
ALTER TABLE trocas ADD CONSTRAINT fk_troca_proposta FOREIGN KEY (proposta_id) REFERENCES propostas(id);

-- Garantir que qr_code existe em trocas
ALTER TABLE trocas ADD COLUMN IF NOT EXISTS qr_code VARCHAR(255);

-- Remover qr_code de propostas (se existir)
ALTER TABLE propostas DROP COLUMN IF EXISTS qr_code;

-- Remover relacionamento entre Proposta e Agendamento
ALTER TABLE propostas DROP CONSTRAINT IF EXISTS fk_proposta_agendamento;

-- Opcional: Remover tabela agendamentos se não for mais necessária
-- DROP TABLE IF EXISTS agendamentos CASCADE;