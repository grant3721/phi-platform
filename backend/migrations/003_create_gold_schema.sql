-- Migration 003: Gold Schema (Analytics & Aggregations)
-- This schema contains pre-computed aggregations for dashboards and reporting
-- Updated by triggers or scheduled jobs

CREATE SCHEMA IF NOT EXISTS gold;

-- Gold: Barangay health statistics
CREATE TABLE IF NOT EXISTS gold.barangay_stats (
    barangay_id TEXT PRIMARY KEY REFERENCES silver.barangays(id),
    barangay_name TEXT NOT NULL,
    municipality TEXT NOT NULL,
    province TEXT NOT NULL,

    -- Population metrics
    total_patients INTEGER DEFAULT 0,
    active_patients_30d INTEGER DEFAULT 0, -- Scanned in last 30 days

    -- Scan metrics
    total_scans INTEGER DEFAULT 0,
    scans_last_7d INTEGER DEFAULT 0,
    scans_last_30d INTEGER DEFAULT 0,
    avg_scans_per_patient FLOAT DEFAULT 0,

    -- Risk metrics
    high_risk_patients INTEGER DEFAULT 0,
    high_risk_percentage FLOAT DEFAULT 0,
    maternal_high_risk INTEGER DEFAULT 0,

    -- Referral metrics
    total_referrals INTEGER DEFAULT 0,
    pending_referrals INTEGER DEFAULT 0,
    overdue_referrals INTEGER DEFAULT 0,

    -- Health indicators (averages)
    avg_systolic_bp FLOAT,
    avg_diastolic_bp FLOAT,
    avg_heart_rate FLOAT,
    avg_spo2 FLOAT,
    avg_respiratory_rate FLOAT,

    -- Last update
    last_updated TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (barangay_id) REFERENCES silver.barangays(id)
);

-- Gold: Municipality health statistics
CREATE TABLE IF NOT EXISTS gold.municipality_stats (
    id SERIAL PRIMARY KEY,
    municipality TEXT NOT NULL,
    province TEXT NOT NULL,

    -- Population metrics
    total_barangays INTEGER DEFAULT 0,
    total_patients INTEGER DEFAULT 0,
    active_patients_30d INTEGER DEFAULT 0,

    -- Coverage metrics
    barangays_with_scans INTEGER DEFAULT 0,
    coverage_percentage FLOAT DEFAULT 0,

    -- Scan metrics
    total_scans INTEGER DEFAULT 0,
    scans_last_7d INTEGER DEFAULT 0,
    scans_last_30d INTEGER DEFAULT 0,

    -- Risk metrics
    high_risk_patients INTEGER DEFAULT 0,
    high_risk_percentage FLOAT DEFAULT 0,

    -- Referral metrics
    total_referrals INTEGER DEFAULT 0,
    pending_referrals INTEGER DEFAULT 0,
    overdue_referrals INTEGER DEFAULT 0,

    -- Health indicators (averages)
    avg_systolic_bp FLOAT,
    avg_diastolic_bp FLOAT,
    avg_heart_rate FLOAT,
    avg_spo2 FLOAT,

    -- Last update
    last_updated TIMESTAMP DEFAULT NOW(),

    UNIQUE(municipality, province)
);

-- Gold: Province health statistics
CREATE TABLE IF NOT EXISTS gold.province_stats (
    province TEXT PRIMARY KEY,
    region TEXT NOT NULL,

    -- Population metrics
    total_municipalities INTEGER DEFAULT 0,
    total_barangays INTEGER DEFAULT 0,
    total_patients INTEGER DEFAULT 0,
    active_patients_30d INTEGER DEFAULT 0,

    -- Scan metrics
    total_scans INTEGER DEFAULT 0,
    scans_last_7d INTEGER DEFAULT 0,
    scans_last_30d INTEGER DEFAULT 0,

    -- Risk metrics
    high_risk_patients INTEGER DEFAULT 0,
    high_risk_percentage FLOAT DEFAULT 0,

    -- Referral metrics
    total_referrals INTEGER DEFAULT 0,
    pending_referrals INTEGER DEFAULT 0,
    overdue_referrals INTEGER DEFAULT 0,

    -- Health indicators (averages)
    avg_systolic_bp FLOAT,
    avg_diastolic_bp FLOAT,
    avg_heart_rate FLOAT,
    avg_spo2 FLOAT,

    -- Last update
    last_updated TIMESTAMP DEFAULT NOW()
);

-- Gold: BHW performance metrics
CREATE TABLE IF NOT EXISTS gold.bhw_performance (
    bhw_id TEXT PRIMARY KEY,
    bhw_name TEXT,
    barangay_id TEXT REFERENCES silver.barangays(id),

    -- Activity metrics
    total_scans INTEGER DEFAULT 0,
    scans_last_7d INTEGER DEFAULT 0,
    scans_last_30d INTEGER DEFAULT 0,
    active_patients INTEGER DEFAULT 0,

    -- Quality metrics
    avg_signal_quality FLOAT, -- 1-4 scale
    scan_rejection_rate FLOAT,

    -- Incentive metrics
    total_earnings FLOAT DEFAULT 0,
    earnings_last_30d FLOAT DEFAULT 0,
    avg_daily_earnings FLOAT DEFAULT 0,
    days_at_cap INTEGER DEFAULT 0, -- Days hit ₱150 cap

    -- Referral metrics
    total_referrals_created INTEGER DEFAULT 0,
    high_risk_detections INTEGER DEFAULT 0,

    -- Last update
    last_activity_at TIMESTAMP,
    last_updated TIMESTAMP DEFAULT NOW()
);

-- Gold: Outbreak signals (for AI analysis)
CREATE TABLE IF NOT EXISTS gold.outbreak_signals (
    id SERIAL PRIMARY KEY,
    barangay_id TEXT REFERENCES silver.barangays(id),
    municipality TEXT,
    province TEXT,

    -- Signal details
    signal_type TEXT NOT NULL, -- 'RESPIRATORY', 'FEVER', 'GASTROINTESTINAL', etc.
    severity TEXT NOT NULL CHECK (severity IN ('LOW', 'MODERATE', 'HIGH', 'CRITICAL')),

    -- Metrics that triggered signal
    affected_count INTEGER,
    baseline_count INTEGER,
    deviation_percentage FLOAT,

    -- Biomarker averages
    avg_respiratory_rate FLOAT,
    avg_spo2 FLOAT,
    avg_heart_rate FLOAT,

    -- Time period
    signal_start_date DATE,
    signal_end_date DATE,
    days_duration INTEGER,

    -- AI Analysis
    ai_analysis_completed BOOLEAN DEFAULT FALSE,
    ai_recommended_actions TEXT,
    ai_confidence_score FLOAT,
    ai_analyzed_at TIMESTAMP,

    -- Status
    status TEXT DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'RESOLVED', 'FALSE_POSITIVE')),
    resolved_at TIMESTAMP,
    resolution_notes TEXT,

    -- Audit
    detected_at TIMESTAMP DEFAULT NOW(),
    last_updated TIMESTAMP DEFAULT NOW()
);

-- Gold: Daily summary (for quick dashboards)
CREATE TABLE IF NOT EXISTS gold.daily_summary (
    summary_date DATE PRIMARY KEY,

    -- Scan metrics
    total_scans INTEGER DEFAULT 0,
    unique_patients_scanned INTEGER DEFAULT 0,
    avg_scans_per_patient FLOAT DEFAULT 0,

    -- Risk metrics
    high_risk_detections INTEGER DEFAULT 0,
    maternal_high_risk INTEGER DEFAULT 0,

    -- Referral metrics
    new_referrals INTEGER DEFAULT 0,
    resolved_referrals INTEGER DEFAULT 0,
    overdue_referrals INTEGER DEFAULT 0,

    -- BHW metrics
    active_bhws INTEGER DEFAULT 0,
    total_earnings_distributed FLOAT DEFAULT 0,

    -- Health indicators (daily averages)
    avg_systolic_bp FLOAT,
    avg_diastolic_bp FLOAT,
    avg_heart_rate FLOAT,
    avg_spo2 FLOAT,

    -- Last update
    last_updated TIMESTAMP DEFAULT NOW()
);

-- Comments
COMMENT ON SCHEMA gold IS 'Analytics and aggregations layer for dashboards';
COMMENT ON TABLE gold.barangay_stats IS 'Pre-computed barangay health statistics';
COMMENT ON TABLE gold.municipality_stats IS 'Municipality-level health rollups';
COMMENT ON TABLE gold.province_stats IS 'Province-level health overview';
COMMENT ON TABLE gold.bhw_performance IS 'BHW activity and performance tracking';
COMMENT ON TABLE gold.outbreak_signals IS 'Early warning signals for disease outbreaks';
COMMENT ON TABLE gold.daily_summary IS 'Daily system-wide metrics';
