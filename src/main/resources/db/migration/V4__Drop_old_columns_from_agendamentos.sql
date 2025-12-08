-- Drop obsolete columns from agendamentos table
-- These columns have been moved to propostas table
DO $$
BEGIN
    -- Drop data_hora column if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'data_hora'
    ) THEN
        ALTER TABLE agendamentos DROP COLUMN data_hora;
    END IF;

    -- Drop local column if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'local'
    ) THEN
        ALTER TABLE agendamentos DROP COLUMN local;
    END IF;

    -- Drop observacoes column if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'observacoes'
    ) THEN
        ALTER TABLE agendamentos DROP COLUMN observacoes;
    END IF;

    -- Drop usuario_id column if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'usuario_id'
    ) THEN
        ALTER TABLE agendamentos DROP COLUMN usuario_id;
    END IF;
END $$;
