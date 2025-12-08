-- Add proper constraints to agendamentos table after refactoring
DO $$
BEGIN
    -- Make proposta_id NOT NULL if it isn't already
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendamentos' 
        AND column_name = 'proposta_id'
        AND is_nullable = 'YES'
    ) THEN
        -- Delete orphaned agendamentos without proposta_id (invalid data)
        -- These are considered invalid since agendamentos must always be linked to a proposta
        DELETE FROM agendamentos WHERE proposta_id IS NULL;
        -- Then make it NOT NULL
        ALTER TABLE agendamentos ALTER COLUMN proposta_id SET NOT NULL;
    END IF;

    -- Add unique constraint on proposta_id if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_agendamentos_proposta_id'
    ) THEN
        ALTER TABLE agendamentos ADD CONSTRAINT uk_agendamentos_proposta_id UNIQUE (proposta_id);
    END IF;

    -- Add foreign key constraint if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_agendamentos_proposta'
    ) THEN
        ALTER TABLE agendamentos 
        ADD CONSTRAINT fk_agendamentos_proposta 
        FOREIGN KEY (proposta_id) 
        REFERENCES propostas(id) 
        ON DELETE CASCADE;
    END IF;
END $$;
