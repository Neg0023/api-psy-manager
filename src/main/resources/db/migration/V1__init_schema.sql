-- =====================================================================
-- V1__init_schema.sql
-- Schema inicial do Psy Manager (single-tenant: um único psicólogo Admin)
-- Banco: PostgreSQL (Neon)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Pacientes
--   - CPF é OPCIONAL (NULL permitido). No PostgreSQL a constraint UNIQUE
--     trata múltiplos NULL como distintos, então vários pacientes podem
--     ficar sem CPF; a unicidade só vale entre CPFs informados.
--   - CPF armazenado como 11 dígitos (sem máscara).
-- ---------------------------------------------------------------------
CREATE TABLE patients (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name   VARCHAR(150) NOT NULL,
    cpf         VARCHAR(11),
    birth_date  DATE         NOT NULL,
    phone       VARCHAR(20)  NOT NULL,
    email       VARCHAR(150) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    profession  VARCHAR(100),
    address     VARCHAR(255),
    notes       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_patients_cpf        UNIQUE (cpf),
    CONSTRAINT chk_patients_status    CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_patients_cpf_format CHECK (cpf IS NULL OR cpf ~ '^[0-9]{11}$')
);

-- ---------------------------------------------------------------------
-- Sessões / Agenda
--   - Profissional único => a verificação de choque de horário é global
--     e fica na camada de service (retorna 409). O índice abaixo apoia
--     essa consulta de sobreposição.
-- ---------------------------------------------------------------------
CREATE TABLE appointments (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    patient_id  BIGINT       NOT NULL,
    start_time  TIMESTAMPTZ  NOT NULL,
    end_time    TIMESTAMPTZ  NOT NULL,
    status      VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    notes       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT chk_appointments_status CHECK (
        status IN ('SCHEDULED', 'CONFIRMED', 'DONE',
                   'CANCELED_BY_PATIENT', 'CANCELED_BY_PROFESSIONAL')
    ),
    CONSTRAINT chk_appointments_time CHECK (end_time > start_time)
);

CREATE INDEX idx_appointments_time    ON appointments (start_time, end_time);
CREATE INDEX idx_appointments_patient ON appointments (patient_id);

-- ---------------------------------------------------------------------
-- Lançamentos financeiros (fluxo de caixa)
--   - amount sempre positivo; o sinal é dado pelo "type" (RECEITA/DESPESA).
--   - patient_id é opcional (aplicável a receitas vinculadas a sessões).
-- ---------------------------------------------------------------------
CREATE TABLE financial_entries (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description     VARCHAR(255)  NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    competence_date DATE          NOT NULL,
    type            VARCHAR(10)   NOT NULL,
    status          VARCHAR(10)   NOT NULL DEFAULT 'PENDENTE',
    patient_id      BIGINT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_financial_entries_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT chk_financial_entries_type   CHECK (type IN ('RECEITA', 'DESPESA')),
    CONSTRAINT chk_financial_entries_status CHECK (status IN ('PAGO', 'PENDENTE')),
    CONSTRAINT chk_financial_entries_amount CHECK (amount > 0)
);

CREATE INDEX idx_financial_entries_competence ON financial_entries (competence_date, type);
CREATE INDEX idx_financial_entries_patient    ON financial_entries (patient_id);

-- ---------------------------------------------------------------------
-- Formulários de anamnese gerados via Google Forms API
--   - Rastreabilidade: form_id + URL pública vinculados (opcionalmente)
--     a um paciente.
-- ---------------------------------------------------------------------
CREATE TABLE anamnesis_forms (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    patient_id     BIGINT,
    google_form_id VARCHAR(255) NOT NULL,
    share_url      VARCHAR(512) NOT NULL,
    title          VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_anamnesis_forms_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE INDEX idx_anamnesis_forms_patient ON anamnesis_forms (patient_id);

-- ---------------------------------------------------------------------
-- Token OAuth do Google para a Forms API (single-tenant => 1 linha)
--   - Guarda o refresh_token do Admin (CIFRADO em repouso pela aplicação)
--     para a aplicação criar formulários em nome da conta Google do Admin.
-- ---------------------------------------------------------------------
CREATE TABLE google_oauth_token (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_email       VARCHAR(150) NOT NULL,
    refresh_token_enc TEXT         NOT NULL,
    scope             VARCHAR(512),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_google_oauth_admin UNIQUE (admin_email)
);
