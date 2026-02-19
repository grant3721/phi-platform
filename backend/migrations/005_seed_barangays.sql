-- Migration 005: Seed Barangays
-- Sample barangays for development (50 barangays)
-- In production, this would load all 1,209 barangays

INSERT INTO silver.barangays (id, name, municipality, province, region, population, active) VALUES
-- Metro Manila (NCR)
('BRG001', 'Barangay 1 (Poblacion)', 'Manila', 'Metro Manila', 'NCR', 5432, TRUE),
('BRG002', 'Barangay 2 (Santa Cruz)', 'Manila', 'Metro Manila', 'NCR', 6821, TRUE),
('BRG003', 'Barangay 3 (Tondo)', 'Manila', 'Metro Manila', 'NCR', 8943, TRUE),
('BRG004', 'Bagong Pag-asa', 'Quezon City', 'Metro Manila', 'NCR', 12456, TRUE),
('BRG005', 'Payatas', 'Quezon City', 'Metro Manila', 'NCR', 15678, TRUE),
('BRG006', 'Commonwealth', 'Quezon City', 'Metro Manila', 'NCR', 9234, TRUE),
('BRG007', 'Poblacion', 'Makati', 'Metro Manila', 'NCR', 7821, TRUE),
('BRG008', 'Forbes Park', 'Makati', 'Metro Manila', 'NCR', 3456, TRUE),

-- Cavite (Region IV-A - CALABARZON)
('BRG009', 'Poblacion I', 'Dasmariñas', 'Cavite', 'CALABARZON', 8934, TRUE),
('BRG010', 'Poblacion II', 'Dasmariñas', 'Cavite', 'CALABARZON', 7621, TRUE),
('BRG011', 'San Agustin I', 'Dasmariñas', 'Cavite', 'CALABARZON', 6543, TRUE),
('BRG012', 'Salitran I', 'Dasmariñas', 'Cavite', 'CALABARZON', 9876, TRUE),
('BRG013', 'Poblacion', 'Bacoor', 'Cavite', 'CALABARZON', 11234, TRUE),
('BRG014', 'Molino I', 'Bacoor', 'Cavite', 'CALABARZON', 8765, TRUE),

-- Laguna (Region IV-A - CALABARZON)
('BRG015', 'Poblacion', 'Santa Rosa', 'Laguna', 'CALABARZON', 10234, TRUE),
('BRG016', 'Balibago', 'Santa Rosa', 'Laguna', 'CALABARZON', 9876, TRUE),
('BRG017', 'Dila', 'Santa Rosa', 'Laguna', 'CALABARZON', 7654, TRUE),
('BRG018', 'Poblacion', 'Biñan', 'Laguna', 'CALABARZON', 8543, TRUE),
('BRG019', 'San Antonio', 'Biñan', 'Laguna', 'CALABARZON', 7321, TRUE),

-- Cebu (Region VII - Central Visayas)
('BRG020', 'Apas', 'Cebu City', 'Cebu', 'Central Visayas', 14567, TRUE),
('BRG021', 'Guadalupe', 'Cebu City', 'Cebu', 'Central Visayas', 16789, TRUE),
('BRG022', 'Lahug', 'Cebu City', 'Cebu', 'Central Visayas', 12345, TRUE),
('BRG023', 'Tisa', 'Cebu City', 'Cebu', 'Central Visayas', 9876, TRUE),
('BRG024', 'Poblacion', 'Mandaue', 'Cebu', 'Central Visayas', 11234, TRUE),
('BRG025', 'Centro', 'Mandaue', 'Cebu', 'Central Visayas', 8765, TRUE),

-- Davao (Region XI - Davao Region)
('BRG026', 'Poblacion', 'Davao City', 'Davao del Sur', 'Davao Region', 18765, TRUE),
('BRG027', 'Agdao', 'Davao City', 'Davao del Sur', 'Davao Region', 15432, TRUE),
('BRG028', 'Buhangin', 'Davao City', 'Davao del Sur', 'Davao Region', 14321, TRUE),
('BRG029', 'Matina', 'Davao City', 'Davao del Sur', 'Davao Region', 13456, TRUE),
('BRG030', 'Toril', 'Davao City', 'Davao del Sur', 'Davao Region', 12345, TRUE),

-- Iloilo (Region VI - Western Visayas)
('BRG031', 'City Proper', 'Iloilo City', 'Iloilo', 'Western Visayas', 15678, TRUE),
('BRG032', 'Jaro', 'Iloilo City', 'Iloilo', 'Western Visayas', 14567, TRUE),
('BRG033', 'Molo', 'Iloilo City', 'Iloilo', 'Western Visayas', 13456, TRUE),
('BRG034', 'Arevalo', 'Iloilo City', 'Iloilo', 'Western Visayas', 12345, TRUE),

-- Pangasinan (Region I - Ilocos Region)
('BRG035', 'Poblacion', 'Dagupan', 'Pangasinan', 'Ilocos Region', 11234, TRUE),
('BRG036', 'Lucao', 'Dagupan', 'Pangasinan', 'Ilocos Region', 9876, TRUE),
('BRG037', 'Pantal', 'Dagupan', 'Pangasinan', 'Ilocos Region', 8765, TRUE),

-- Pampanga (Region III - Central Luzon)
('BRG038', 'Poblacion', 'Angeles', 'Pampanga', 'Central Luzon', 16789, TRUE),
('BRG039', 'Balibago', 'Angeles', 'Pampanga', 'Central Luzon', 15678, TRUE),
('BRG040', 'Cutcut', 'Angeles', 'Pampanga', 'Central Luzon', 12345, TRUE),

-- Albay (Region V - Bicol)
('BRG041', 'Poblacion', 'Legazpi', 'Albay', 'Bicol', 14567, TRUE),
('BRG042', 'Em's Barrio', 'Legazpi', 'Albay', 'Bicol', 11234, TRUE),
('BRG043', 'Cabangan', 'Legazpi', 'Albay', 'Bicol', 9876, TRUE),

-- Zamboanga (Region IX - Zamboanga Peninsula)
('BRG044', 'Poblacion', 'Zamboanga City', 'Zamboanga del Sur', 'Zamboanga Peninsula', 17654, TRUE),
('BRG045', 'Tetuan', 'Zamboanga City', 'Zamboanga del Sur', 'Zamboanga Peninsula', 15432, TRUE),
('BRG046', 'San Roque', 'Zamboanga City', 'Zamboanga del Sur', 'Zamboanga Peninsula', 13456, TRUE),

-- Cagayan de Oro (Region X - Northern Mindanao)
('BRG047', 'Poblacion', 'Cagayan de Oro', 'Misamis Oriental', 'Northern Mindanao', 16789, TRUE),
('BRG048', 'Carmen', 'Cagayan de Oro', 'Misamis Oriental', 'Northern Mindanao', 14567, TRUE),
('BRG049', 'Macabalan', 'Cagayan de Oro', 'Misamis Oriental', 'Northern Mindanao', 12345, TRUE),
('BRG050', 'Kauswagan', 'Cagayan de Oro', 'Misamis Oriental', 'Northern Mindanao', 11234, TRUE)

ON CONFLICT (id) DO NOTHING;

-- Insert default config thresholds
INSERT INTO silver.config_thresholds (id, threshold_type, parameter_name, low_threshold, high_threshold, unit, description, active, updated_at) VALUES
('THR001', 'VITAL_SIGN', 'systolic_bp', 90.0, 140.0, 'mmHg', 'Systolic blood pressure', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR002', 'VITAL_SIGN', 'diastolic_bp', 60.0, 90.0, 'mmHg', 'Diastolic blood pressure', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR003', 'VITAL_SIGN', 'heart_rate', 60.0, 100.0, 'bpm', 'Resting heart rate', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR004', 'VITAL_SIGN', 'spo2', 95.0, 100.0, '%', 'Blood oxygen saturation', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR005', 'VITAL_SIGN', 'respiratory_rate', 12.0, 20.0, '/min', 'Respiratory rate', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR006', 'BLOODLESS_TEST', 'hemoglobin', 12.0, 16.0, 'g/dL', 'Hemoglobin level', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR007', 'BLOODLESS_TEST', 'hba1c', 4.0, 5.7, '%', 'Glycated hemoglobin', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR008', 'BLOODLESS_TEST', 'blood_glucose', 70.0, 100.0, 'mg/dL', 'Fasting blood glucose', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR009', 'RISK_SCORE', 'diabetes_risk', NULL, 7.0, 'score', 'Diabetes risk score', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR010', 'RISK_SCORE', 'cvd_risk', NULL, 7.0, 'score', 'Cardiovascular disease risk', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR011', 'RISK_SCORE', 'stroke_risk', NULL, 7.0, 'score', 'Stroke risk score', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR012', 'HRV', 'sdnn', 50.0, NULL, 'ms', 'Standard deviation of NN intervals', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000),
('THR013', 'STRESS', 'stress_index', NULL, 7.0, 'score', 'Overall stress index', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000)

ON CONFLICT (id) DO NOTHING;

-- Create summary comment
COMMENT ON TABLE silver.barangays IS 'Seeded with 50 sample barangays for development. Production will have all 1,209 Philippine barangays.';
COMMENT ON TABLE silver.config_thresholds IS 'Default clinical thresholds for risk assessment. Can be updated via admin API.';
