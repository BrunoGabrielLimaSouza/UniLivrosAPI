-- Add suggestion fields to propostas table
-- These fields moved from agendamentos to propostas as per refactoring
ALTER TABLE propostas 
ADD COLUMN IF NOT EXISTS data_hora_sugerida TIMESTAMP;

ALTER TABLE propostas 
ADD COLUMN IF NOT EXISTS local_sugerido VARCHAR(255);

ALTER TABLE propostas 
ADD COLUMN IF NOT EXISTS observacoes TEXT;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_propostas_data_hora_sugerida ON propostas(data_hora_sugerida);
