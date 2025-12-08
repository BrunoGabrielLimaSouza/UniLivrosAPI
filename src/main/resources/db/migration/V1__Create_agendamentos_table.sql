-- Create agendamentos table with refactored schema if it doesn't exist
-- OR ensure the table exists with at least the basic structure
DO $$
BEGIN
    -- Create table if it doesn't exist (with old schema for backward compatibility during migration)
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agendamentos') THEN
        CREATE TABLE agendamentos (
            id BIGSERIAL PRIMARY KEY,
            status VARCHAR(50) NOT NULL DEFAULT 'AGENDADO',
            proposta_id BIGINT,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP
        );
    END IF;
    
    -- Ensure required columns exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'agendamentos' AND column_name = 'status') THEN
        ALTER TABLE agendamentos ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'AGENDADO';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'agendamentos' AND column_name = 'proposta_id') THEN
        ALTER TABLE agendamentos ADD COLUMN proposta_id BIGINT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'agendamentos' AND column_name = 'created_at') THEN
        ALTER TABLE agendamentos ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'agendamentos' AND column_name = 'updated_at') THEN
        ALTER TABLE agendamentos ADD COLUMN updated_at TIMESTAMP;
    END IF;
END $$;

-- Create indexes for faster lookups (if they don't exist)
CREATE INDEX IF NOT EXISTS idx_agendamentos_proposta_id ON agendamentos(proposta_id);
CREATE INDEX IF NOT EXISTS idx_agendamentos_status ON agendamentos(status);
