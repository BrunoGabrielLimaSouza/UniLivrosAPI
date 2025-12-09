-- ============================================
-- Migration V6: Refatorar relacionamento Proposta → Troca
-- Remove dependência de Agendamento e cria relação direta
-- ============================================

-- 1. Remover relacionamento entre Troca e Agendamento
DO $$
BEGIN
    -- Drop constraint if exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_troca_agendamento'
    ) THEN
ALTER TABLE trocas DROP CONSTRAINT fk_troca_agendamento;
END IF;

    -- Drop column if exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'trocas' AND column_name = 'agendamento_id'
    ) THEN
ALTER TABLE trocas DROP COLUMN agendamento_id;
END IF;
END $$;

-- 2. Adicionar relacionamento direto entre Troca e Proposta
DO $$
BEGIN
    -- Add proposta_id column if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'trocas' AND column_name = 'proposta_id'
    ) THEN
ALTER TABLE trocas ADD COLUMN proposta_id BIGINT;
END IF;

    -- Add foreign key constraint if not exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_troca_proposta'
    ) THEN
ALTER TABLE trocas
    ADD CONSTRAINT fk_troca_proposta
        FOREIGN KEY (proposta_id) REFERENCES propostas(id) ON DELETE CASCADE;
END IF;
END $$;

-- 3. Garantir que qr_code existe em trocas
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'trocas' AND column_name = 'qr_code'
    ) THEN
ALTER TABLE trocas ADD COLUMN qr_code VARCHAR(255);
END IF;
END $$;

-- 4. Remover qr_code de propostas (se existir)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'propostas' AND column_name = 'qr_code'
    ) THEN
ALTER TABLE propostas DROP COLUMN qr_code;
END IF;
END $$;

-- 5. Remover relacionamento entre Proposta e Agendamento
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_proposta_agendamento'
    ) THEN
ALTER TABLE propostas DROP CONSTRAINT fk_proposta_agendamento;
END IF;
END $$;

-- 6. Migrar dados existentes (se houver trocas com agendamento)
-- Conectar trocas existentes às suas propostas através do agendamento
DO $$
BEGIN
    -- [CORRIGIDO] Verifica se a coluna agendamento_id *ainda existe* na tabela trocas
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'trocas' AND column_name = 'agendamento_id'
    ) THEN
        -- Atualizar trocas que têm agendamento para apontar direto para proposta
UPDATE trocas t
SET proposta_id = a.proposta_id
    FROM agendamentos a
WHERE t.agendamento_id = a.id
  AND t.proposta_id IS NULL;
END IF;
END $$;

-- 7. Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_trocas_proposta_id ON trocas(proposta_id);
CREATE INDEX IF NOT EXISTS idx_trocas_qr_code ON trocas(qr_code);
CREATE INDEX IF NOT EXISTS idx_trocas_status ON trocas(status);

-- 8. Opcional: Remover tabela agendamentos se não for mais necessária
-- ⚠️ DESCOMENTE APENAS SE TIVER CERTEZA QUE NÃO PRECISA MAIS
-- DROP TABLE IF EXISTS agendamentos CASCADE;

-- ============================================
-- Verificação final
-- ============================================
DO $$
BEGIN
    -- Verifica se a migração foi bem-sucedida
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'trocas' AND column_name = 'proposta_id'
    ) THEN
        RAISE EXCEPTION 'Migration failed: proposta_id column not found in trocas table';
END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'trocas' AND column_name = 'qr_code'
    ) THEN
        RAISE EXCEPTION 'Migration failed: qr_code column not found in trocas table';
END IF;

    RAISE NOTICE 'Migration V6 completed successfully!';
END $$;