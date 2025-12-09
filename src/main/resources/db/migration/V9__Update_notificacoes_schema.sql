-- 1. Renomear data_criacao para created_at (se data_criacao existir)
-- Isso alinha com o @Column(name = "created_at") da sua entidade
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notificacoes' AND column_name = 'data_criacao') THEN
ALTER TABLE notificacoes RENAME COLUMN data_criacao TO created_at;
END IF;
END $$;

-- 2. Garantir que created_at existe (caso a tabela tenha sido criada sem data_criacao)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notificacoes' AND column_name = 'created_at') THEN
ALTER TABLE notificacoes ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();
END IF;
END $$;

-- 3. Adicionar colunas novas exigidas pela entidade Notificacao.java

-- Adicionar Título
ALTER TABLE notificacoes ADD COLUMN IF NOT EXISTS titulo VARCHAR(255);

-- Adicionar Tipo (Enum no Java, String no Banco)
ALTER TABLE notificacoes ADD COLUMN IF NOT EXISTS tipo VARCHAR(50);

-- Adicionar IDs de relacionamento (podem ser nulos)
ALTER TABLE notificacoes ADD COLUMN IF NOT EXISTS proposta_id BIGINT;
ALTER TABLE notificacoes ADD COLUMN IF NOT EXISTS troca_id BIGINT;

-- 4. Adicionar Foreign Keys para manter integridade (Opcional, mas recomendado)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notificacao_proposta') THEN
ALTER TABLE notificacoes
    ADD CONSTRAINT fk_notificacao_proposta
        FOREIGN KEY (proposta_id) REFERENCES propostas(id) ON DELETE CASCADE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notificacao_troca') THEN
ALTER TABLE notificacoes
    ADD CONSTRAINT fk_notificacao_troca
        FOREIGN KEY (troca_id) REFERENCES trocas(id) ON DELETE CASCADE;
END IF;
END $$;