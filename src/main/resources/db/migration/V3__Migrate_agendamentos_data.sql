-- Make old columns nullable before data migration to avoid constraint violations
DO $$
BEGIN
    -- Make data_hora nullable if it exists and is NOT NULL
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'data_hora'
        AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE agendamentos ALTER COLUMN data_hora DROP NOT NULL;
    END IF;

    -- Make local nullable if it exists and is NOT NULL
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'local'
        AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE agendamentos ALTER COLUMN local DROP NOT NULL;
    END IF;
END $$;

-- Migrate data from old agendamentos columns to propostas
-- Only run if old columns exist in agendamentos table
DO $$
BEGIN
    -- Check if old columns exist and migrate data
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'data_hora'
    ) THEN
        -- Migrate data_hora to propostas.data_hora_sugerida
        UPDATE propostas p
        SET data_hora_sugerida = a.data_hora
        FROM agendamentos a
        WHERE a.proposta_id = p.id
        AND a.data_hora IS NOT NULL
        AND p.data_hora_sugerida IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'local'
    ) THEN
        -- Migrate local to propostas.local_sugerido
        UPDATE propostas p
        SET local_sugerido = a.local
        FROM agendamentos a
        WHERE a.proposta_id = p.id
        AND a.local IS NOT NULL
        AND p.local_sugerido IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'observacoes'
    ) THEN
        -- Migrate observacoes to propostas.observacoes
        UPDATE propostas p
        SET observacoes = a.observacoes
        FROM agendamentos a
        WHERE a.proposta_id = p.id
        AND a.observacoes IS NOT NULL
        AND p.observacoes IS NULL;
    END IF;
END $$;
