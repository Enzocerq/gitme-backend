-- Script SQL para criar as novas tabelas do ETL de seeding
-- Executar no Supabase/PostgreSQL

-- Tabela: github_users (dimensão de contribuidores observados)
CREATE TABLE IF NOT EXISTS public.github_users (
    login VARCHAR(255) PRIMARY KEY NOT NULL,
    github_id BIGINT UNIQUE,
    avatar_url TEXT,
    html_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_github_users_github_id ON public.github_users(github_id);
CREATE INDEX IF NOT EXISTS idx_github_users_login ON public.github_users(login);

-- Tabela: reviews (avaliações de PRs)
CREATE TABLE IF NOT EXISTS public.reviews (
    id BIGINT PRIMARY KEY NOT NULL,
    pull_request_id BIGINT NOT NULL,
    author_login VARCHAR(255),
    state VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_pull_request
        FOREIGN KEY (pull_request_id)
        REFERENCES public.pull_requests(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reviews_pull_request_id ON public.reviews(pull_request_id);
CREATE INDEX IF NOT EXISTS idx_reviews_author_login ON public.reviews(author_login);
CREATE INDEX IF NOT EXISTS idx_reviews_state ON public.reviews(state);

-- Commit para persistência
COMMIT;

-- Migração: ampliar colunas title de VARCHAR(255) para TEXT
-- Necessário pois títulos de issues/PRs podem ultrapassar 255 caracteres
ALTER TABLE public.issues ALTER COLUMN title TYPE TEXT;
ALTER TABLE public.pull_requests ALTER COLUMN title TYPE TEXT;

-- ================================================================
-- Tabela: pull_request_commits (join PR ↔ Commit para Cycle Time)
-- Executar ANTES do próximo seed com axios/axios
-- ================================================================
CREATE TABLE IF NOT EXISTS public.pull_request_commits (
    pr_id     BIGINT       NOT NULL,
    commit_sha VARCHAR(255) NOT NULL,
    PRIMARY KEY (pr_id, commit_sha),
    CONSTRAINT fk_prc_pull_request
        FOREIGN KEY (pr_id)
        REFERENCES public.pull_requests(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_prc_commit
        FOREIGN KEY (commit_sha)
        REFERENCES public.commits(sha)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_prc_pr_id     ON public.pull_request_commits(pr_id);
CREATE INDEX IF NOT EXISTS idx_prc_commit_sha ON public.pull_request_commits(commit_sha);

-- Índices de performance para queries de métricas (período + repo)
CREATE INDEX IF NOT EXISTS idx_commits_repo_date    ON public.commits(repo_id, commit_date);
CREATE INDEX IF NOT EXISTS idx_commits_author_date  ON public.commits(author_login, commit_date);
CREATE INDEX IF NOT EXISTS idx_prs_repo_created     ON public.pull_requests(repo_id, created_at);
CREATE INDEX IF NOT EXISTS idx_prs_repo_merged      ON public.pull_requests(repo_id, merged_at);
CREATE INDEX IF NOT EXISTS idx_issues_repo_closed   ON public.issues(repo_id, closed_at);
CREATE INDEX IF NOT EXISTS idx_reviews_submitted    ON public.reviews(submitted_at);
