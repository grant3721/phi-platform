-- Migration 004: Indexes for Query Optimization

-- ========================================
-- BRONZE SCHEMA INDEXES
-- ========================================

-- Bronze patients
CREATE INDEX IF NOT EXISTS idx_bronze_patients_phone ON bronze.patients(phone_number);
CREATE INDEX IF NOT EXISTS idx_bronze_patients_bhw ON bronze.patients(bhw_id);
CREATE INDEX IF NOT EXISTS idx_bronze_patients_device ON bronze.patients(device_id);
CREATE INDEX IF NOT EXISTS idx_bronze_patients_ingested ON bronze.patients(ingested_at);

-- Bronze scans
CREATE INDEX IF NOT EXISTS idx_bronze_scans_patient ON bronze.scans(patient_id);
CREATE INDEX IF NOT EXISTS idx_bronze_scans_bhw ON bronze.scans(bhw_id);
CREATE INDEX IF NOT EXISTS idx_bronze_scans_device ON bronze.scans(device_id);
CREATE INDEX IF NOT EXISTS idx_bronze_scans_scanned_at ON bronze.scans(scanned_at);
CREATE INDEX IF NOT EXISTS idx_bronze_scans_ingested ON bronze.scans(ingested_at);

-- Bronze surveys
CREATE INDEX IF NOT EXISTS idx_bronze_surveys_scan ON bronze.surveys(scan_id);
CREATE INDEX IF NOT EXISTS idx_bronze_surveys_patient ON bronze.surveys(patient_id);
CREATE INDEX IF NOT EXISTS idx_bronze_surveys_type ON bronze.surveys(survey_type);

-- Bronze referrals
CREATE INDEX IF NOT EXISTS idx_bronze_referrals_patient ON bronze.referrals(patient_id);
CREATE INDEX IF NOT EXISTS idx_bronze_referrals_scan ON bronze.referrals(scan_id);
CREATE INDEX IF NOT EXISTS idx_bronze_referrals_status ON bronze.referrals(status);
CREATE INDEX IF NOT EXISTS idx_bronze_referrals_tier ON bronze.referrals(tier);

-- Bronze sync receipts
CREATE INDEX IF NOT EXISTS idx_bronze_sync_device ON bronze.sync_receipts(device_id);
CREATE INDEX IF NOT EXISTS idx_bronze_sync_bhw ON bronze.sync_receipts(bhw_id);
CREATE INDEX IF NOT EXISTS idx_bronze_sync_timestamp ON bronze.sync_receipts(sync_timestamp);

-- ========================================
-- SILVER SCHEMA INDEXES
-- ========================================

-- Silver patients
CREATE INDEX IF NOT EXISTS idx_silver_patients_phone ON silver.patients(phone_number);
CREATE INDEX IF NOT EXISTS idx_silver_patients_barangay ON silver.patients(barangay_id);
CREATE INDEX IF NOT EXISTS idx_silver_patients_high_risk ON silver.patients(high_risk_flag) WHERE high_risk_flag = TRUE;
CREATE INDEX IF NOT EXISTS idx_silver_patients_pregnant ON silver.patients(is_pregnant) WHERE is_pregnant = TRUE;
CREATE INDEX IF NOT EXISTS idx_silver_patients_last_scan ON silver.patients(last_scan_at);

-- Silver scans
CREATE INDEX IF NOT EXISTS idx_silver_scans_patient ON silver.scans(patient_id);
CREATE INDEX IF NOT EXISTS idx_silver_scans_bhw ON silver.scans(bhw_id);
CREATE INDEX IF NOT EXISTS idx_silver_scans_scanned_at ON silver.scans(scanned_at);
CREATE INDEX IF NOT EXISTS idx_silver_scans_validated ON silver.scans(validated_at);
CREATE INDEX IF NOT EXISTS idx_silver_scans_risk_score ON silver.scans(risk_score) WHERE risk_score > 7.0;

-- GIN indexes for JSON columns (risk_flags)
CREATE INDEX IF NOT EXISTS idx_silver_scans_risk_flags_gin ON silver.scans USING GIN(risk_flags);

-- Silver surveys
CREATE INDEX IF NOT EXISTS idx_silver_surveys_scan ON silver.surveys(scan_id);
CREATE INDEX IF NOT EXISTS idx_silver_surveys_patient ON silver.surveys(patient_id);
CREATE INDEX IF NOT EXISTS idx_silver_surveys_type ON silver.surveys(survey_type);
CREATE INDEX IF NOT EXISTS idx_silver_surveys_completed ON silver.surveys(completed_at);

-- Silver referrals
CREATE INDEX IF NOT EXISTS idx_silver_referrals_patient ON silver.referrals(patient_id);
CREATE INDEX IF NOT EXISTS idx_silver_referrals_scan ON silver.referrals(scan_id);
CREATE INDEX IF NOT EXISTS idx_silver_referrals_status ON silver.referrals(status);
CREATE INDEX IF NOT EXISTS idx_silver_referrals_tier ON silver.referrals(tier);
CREATE INDEX IF NOT EXISTS idx_silver_referrals_referred_by ON silver.referrals(referred_by);
CREATE INDEX IF NOT EXISTS idx_silver_referrals_due_by ON silver.referrals(due_by);
CREATE INDEX IF NOT EXISTS idx_silver_referrals_pending ON silver.referrals(status, due_by) WHERE status IN ('PENDING', 'CONFIRMED');

-- Silver incentives
CREATE INDEX IF NOT EXISTS idx_silver_incentives_bhw ON silver.bhw_incentives(bhw_id);
CREATE INDEX IF NOT EXISTS idx_silver_incentives_scan ON silver.bhw_incentives(scan_id);
CREATE INDEX IF NOT EXISTS idx_silver_incentives_status ON silver.bhw_incentives(status);
CREATE INDEX IF NOT EXISTS idx_silver_incentives_earned_at ON silver.bhw_incentives(earned_at);
CREATE INDEX IF NOT EXISTS idx_silver_incentives_bhw_date ON silver.bhw_incentives(bhw_id, earned_at);

-- Silver config thresholds
CREATE INDEX IF NOT EXISTS idx_silver_thresholds_type ON silver.config_thresholds(threshold_type);
CREATE INDEX IF NOT EXISTS idx_silver_thresholds_active ON silver.config_thresholds(active) WHERE active = TRUE;

-- Silver barangays
CREATE INDEX IF NOT EXISTS idx_silver_barangays_municipality ON silver.barangays(municipality);
CREATE INDEX IF NOT EXISTS idx_silver_barangays_province ON silver.barangays(province);
CREATE INDEX IF NOT EXISTS idx_silver_barangays_region ON silver.barangays(region);

-- ========================================
-- GOLD SCHEMA INDEXES
-- ========================================

-- Gold barangay stats
CREATE INDEX IF NOT EXISTS idx_gold_barangay_municipality ON gold.barangay_stats(municipality);
CREATE INDEX IF NOT EXISTS idx_gold_barangay_province ON gold.barangay_stats(province);
CREATE INDEX IF NOT EXISTS idx_gold_barangay_high_risk ON gold.barangay_stats(high_risk_percentage DESC);
CREATE INDEX IF NOT EXISTS idx_gold_barangay_updated ON gold.barangay_stats(last_updated);

-- Gold municipality stats
CREATE INDEX IF NOT EXISTS idx_gold_municipality_province ON gold.municipality_stats(province);
CREATE INDEX IF NOT EXISTS idx_gold_municipality_coverage ON gold.municipality_stats(coverage_percentage DESC);

-- Gold BHW performance
CREATE INDEX IF NOT EXISTS idx_gold_bhw_barangay ON gold.bhw_performance(barangay_id);
CREATE INDEX IF NOT EXISTS idx_gold_bhw_activity ON gold.bhw_performance(last_activity_at);
CREATE INDEX IF NOT EXISTS idx_gold_bhw_earnings ON gold.bhw_performance(total_earnings DESC);

-- Gold outbreak signals
CREATE INDEX IF NOT EXISTS idx_gold_outbreak_barangay ON gold.outbreak_signals(barangay_id);
CREATE INDEX IF NOT EXISTS idx_gold_outbreak_status ON gold.outbreak_signals(status);
CREATE INDEX IF NOT EXISTS idx_gold_outbreak_severity ON gold.outbreak_signals(severity);
CREATE INDEX IF NOT EXISTS idx_gold_outbreak_detected ON gold.outbreak_signals(detected_at);
CREATE INDEX IF NOT EXISTS idx_gold_outbreak_active ON gold.outbreak_signals(status, severity) WHERE status = 'ACTIVE';

-- Gold daily summary
CREATE INDEX IF NOT EXISTS idx_gold_daily_date ON gold.daily_summary(summary_date DESC);

-- ========================================
-- COMMENTS
-- ========================================
-- Comments on indexes removed for Azure compatibility
