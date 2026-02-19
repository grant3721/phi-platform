-- Migration 001: Bronze Schema (Raw Data Ingestion)
-- This schema receives all data from mobile devices without validation
-- Data is immutable once inserted (append-only)

CREATE SCHEMA IF NOT EXISTS bronze;

-- Bronze: Raw patient data
CREATE TABLE IF NOT EXISTS bronze.patients (
    id TEXT PRIMARY KEY,
    phone_number TEXT NOT NULL,
    full_name TEXT NOT NULL,
    date_of_birth BIGINT NOT NULL,
    sex TEXT NOT NULL,
    barangay_id TEXT NOT NULL,
    barangay_name TEXT NOT NULL,
    philhealth_number TEXT,
    philsys_number TEXT,
    is_pregnant BOOLEAN DEFAULT FALSE,
    gestational_age_weeks INTEGER,
    expected_delivery_date BIGINT,
    messenger_opt_in BOOLEAN DEFAULT FALSE,
    messenger_user_id TEXT,
    high_risk_flag BOOLEAN DEFAULT FALSE,
    high_risk_reasons JSONB DEFAULT '[]'::jsonb,
    maternal_high_risk BOOLEAN DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    sync_status TEXT NOT NULL,
    -- Metadata
    device_id TEXT,
    bhw_id TEXT,
    ingested_at TIMESTAMP DEFAULT NOW()
);

-- Bronze: Raw scan data
CREATE TABLE IF NOT EXISTS bronze.scans (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL,
    bhw_id TEXT NOT NULL,
    scan_type TEXT NOT NULL,
    biomarkers JSONB NOT NULL, -- All 34 vital signs
    signal_quality TEXT,
    validated BOOLEAN DEFAULT FALSE,
    risk_flags JSONB DEFAULT '[]'::jsonb,
    scanned_at BIGINT NOT NULL,
    sync_status TEXT NOT NULL,
    -- Metadata
    device_id TEXT,
    ingested_at TIMESTAMP DEFAULT NOW()
);

-- Bronze: Raw survey data
CREATE TABLE IF NOT EXISTS bronze.surveys (
    id TEXT PRIMARY KEY,
    scan_id TEXT NOT NULL,
    patient_id TEXT NOT NULL,
    survey_type TEXT NOT NULL,
    responses JSONB NOT NULL,
    completed_at BIGINT NOT NULL,
    sync_status TEXT NOT NULL,
    -- Metadata
    device_id TEXT,
    bhw_id TEXT,
    ingested_at TIMESTAMP DEFAULT NOW()
);

-- Bronze: Raw referral data
CREATE TABLE IF NOT EXISTS bronze.referrals (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL,
    scan_id TEXT NOT NULL,
    tier TEXT NOT NULL,
    status TEXT NOT NULL,
    risk_flags JSONB DEFAULT '[]'::jsonb,
    notes TEXT,
    referred_by TEXT NOT NULL,
    referred_at BIGINT NOT NULL,
    due_by BIGINT NOT NULL,
    resolved_at BIGINT,
    resolved_by TEXT,
    resolution_notes TEXT,
    sync_status TEXT NOT NULL,
    -- Metadata
    device_id TEXT,
    ingested_at TIMESTAMP DEFAULT NOW()
);

-- Bronze: Sync receipts (tracking)
CREATE TABLE IF NOT EXISTS bronze.sync_receipts (
    id TEXT PRIMARY KEY,
    device_id TEXT NOT NULL,
    bhw_id TEXT NOT NULL,
    sync_timestamp BIGINT NOT NULL,
    patients_count INTEGER DEFAULT 0,
    scans_count INTEGER DEFAULT 0,
    surveys_count INTEGER DEFAULT 0,
    referrals_count INTEGER DEFAULT 0,
    accepted_counts JSONB,
    rejected_counts JSONB,
    rejected_reasons JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Comments
COMMENT ON SCHEMA bronze IS 'Raw data ingestion layer - append-only, immutable';
COMMENT ON TABLE bronze.patients IS 'Raw patient registrations from devices';
COMMENT ON TABLE bronze.scans IS 'Raw health scans from BiosenseSignal SDK';
COMMENT ON TABLE bronze.surveys IS 'Raw health survey responses';
COMMENT ON TABLE bronze.referrals IS 'Raw referrals created by BHWs';
COMMENT ON TABLE bronze.sync_receipts IS 'Sync operation tracking';
