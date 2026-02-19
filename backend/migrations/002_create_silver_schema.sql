-- Migration 002: Silver Schema (Validated Data)
-- This schema contains validated, deduplicated, and quality-checked data
-- Data here has passed business rules and is ready for analytics

CREATE SCHEMA IF NOT EXISTS silver;

-- Silver: Validated patients (deduplicated by phone)
CREATE TABLE IF NOT EXISTS silver.patients (
    id TEXT PRIMARY KEY,
    phone_number TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    date_of_birth BIGINT NOT NULL,
    sex TEXT NOT NULL CHECK (sex IN ('MALE', 'FEMALE')),
    barangay_id TEXT NOT NULL,
    barangay_name TEXT NOT NULL,
    philhealth_number TEXT UNIQUE,
    philsys_number TEXT UNIQUE,
    is_pregnant BOOLEAN DEFAULT FALSE,
    gestational_age_weeks INTEGER CHECK (gestational_age_weeks BETWEEN 0 AND 42),
    expected_delivery_date BIGINT,
    messenger_opt_in BOOLEAN DEFAULT FALSE,
    messenger_user_id TEXT,
    high_risk_flag BOOLEAN DEFAULT FALSE,
    high_risk_reasons JSONB DEFAULT '[]'::jsonb,
    maternal_high_risk BOOLEAN DEFAULT FALSE,
    -- Audit
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    last_scan_at BIGINT,
    total_scans INTEGER DEFAULT 0,
    validated_at TIMESTAMP DEFAULT NOW(),
    bronze_source_id TEXT
);

-- Silver: Validated scans
CREATE TABLE IF NOT EXISTS silver.scans (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES silver.patients(id) ON DELETE CASCADE,
    bhw_id TEXT NOT NULL,
    scan_type TEXT NOT NULL,
    -- Extracted biomarkers (flattened from JSON)
    systolic_bp FLOAT,
    diastolic_bp FLOAT,
    heart_rate_bpm FLOAT,
    respiratory_rate FLOAT,
    spo2 FLOAT,
    hemoglobin FLOAT,
    hba1c FLOAT,
    blood_glucose FLOAT,
    cholesterol_total FLOAT,
    cholesterol_ldl FLOAT,
    cholesterol_hdl FLOAT,
    triglycerides FLOAT,
    heart_rate_variability FLOAT,
    sdnn FLOAT,
    rmssd FLOAT,
    stress_index FLOAT,
    -- Additional biomarkers stored as JSON
    biomarkers_full JSONB NOT NULL,
    signal_quality TEXT CHECK (signal_quality IN ('EXCELLENT', 'GOOD', 'FAIR', 'POOR')),
    validated BOOLEAN DEFAULT TRUE,
    risk_flags JSONB DEFAULT '[]'::jsonb,
    risk_score FLOAT,
    scanned_at BIGINT NOT NULL,
    -- Audit
    validated_at TIMESTAMP DEFAULT NOW(),
    bronze_source_id TEXT,
    UNIQUE(patient_id, scanned_at)
);

-- Silver: Validated surveys
CREATE TABLE IF NOT EXISTS silver.surveys (
    id TEXT PRIMARY KEY,
    scan_id TEXT NOT NULL REFERENCES silver.scans(id) ON DELETE CASCADE,
    patient_id TEXT NOT NULL REFERENCES silver.patients(id) ON DELETE CASCADE,
    survey_type TEXT NOT NULL CHECK (survey_type IN ('NCD', 'MATERNAL', 'INFECTIOUS', 'MENTAL_HEALTH')),
    responses JSONB NOT NULL,
    completed_at BIGINT NOT NULL,
    -- Audit
    validated_at TIMESTAMP DEFAULT NOW(),
    bronze_source_id TEXT,
    UNIQUE(scan_id, survey_type)
);

-- Silver: Validated referrals
CREATE TABLE IF NOT EXISTS silver.referrals (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES silver.patients(id) ON DELETE CASCADE,
    scan_id TEXT NOT NULL REFERENCES silver.scans(id) ON DELETE CASCADE,
    tier TEXT NOT NULL CHECK (tier IN ('BHW_TO_BHS', 'BHS_TO_RHU', 'RHU_TO_HOSPITAL')),
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'RESOLVED', 'ESCALATED', 'OVERDUE', 'CANCELLED')),
    risk_flags JSONB DEFAULT '[]'::jsonb,
    notes TEXT,
    referred_by TEXT NOT NULL,
    referred_at BIGINT NOT NULL,
    due_by BIGINT NOT NULL,
    resolved_at BIGINT,
    resolved_by TEXT,
    resolution_notes TEXT,
    -- Audit
    validated_at TIMESTAMP DEFAULT NOW(),
    bronze_source_id TEXT
);

-- Silver: BHW incentive tracking
CREATE TABLE IF NOT EXISTS silver.bhw_incentives (
    id TEXT PRIMARY KEY,
    bhw_id TEXT NOT NULL,
    scan_id TEXT NOT NULL REFERENCES silver.scans(id),
    amount FLOAT NOT NULL CHECK (amount >= 0),
    status TEXT NOT NULL CHECK (status IN ('EARNED', 'REJECTED', 'PAID')),
    rejection_reason TEXT,
    earned_at BIGINT NOT NULL,
    paid_at BIGINT,
    -- Audit
    validated_at TIMESTAMP DEFAULT NOW()
);

-- Silver: Config thresholds
CREATE TABLE IF NOT EXISTS silver.config_thresholds (
    id TEXT PRIMARY KEY,
    threshold_type TEXT NOT NULL,
    parameter_name TEXT NOT NULL,
    low_threshold FLOAT,
    high_threshold FLOAT,
    unit TEXT,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    updated_at BIGINT NOT NULL,
    UNIQUE(threshold_type, parameter_name)
);

-- Silver: Barangays (reference data)
CREATE TABLE IF NOT EXISTS silver.barangays (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    municipality TEXT NOT NULL,
    province TEXT NOT NULL,
    region TEXT NOT NULL,
    population INTEGER,
    coordinates POINT,
    active BOOLEAN DEFAULT TRUE
);

-- Comments
COMMENT ON SCHEMA silver IS 'Validated and cleaned data layer';
COMMENT ON TABLE silver.patients IS 'Deduplicated and validated patient records';
COMMENT ON TABLE silver.scans IS 'Quality-checked health scans with extracted biomarkers';
COMMENT ON TABLE silver.surveys IS 'Validated survey responses';
COMMENT ON TABLE silver.referrals IS 'Active referral tracking';
COMMENT ON TABLE silver.bhw_incentives IS 'BHW incentive payments';
COMMENT ON TABLE silver.config_thresholds IS 'Clinical thresholds for risk assessment';
COMMENT ON TABLE silver.barangays IS 'Philippine barangay reference data';
