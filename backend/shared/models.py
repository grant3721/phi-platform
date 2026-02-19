"""
Pydantic models for request/response validation
"""
from pydantic import BaseModel, Field, field_validator
from typing import Optional, List, Dict, Any
from datetime import datetime
from enum import Enum


# Enums
class SyncStatus(str, Enum):
    PENDING = "PENDING"
    SYNCED = "SYNCED"
    FAILED = "FAILED"


class ReferralStatus(str, Enum):
    PENDING = "PENDING"
    CONFIRMED = "CONFIRMED"
    IN_PROGRESS = "IN_PROGRESS"
    RESOLVED = "RESOLVED"
    ESCALATED = "ESCALATED"
    OVERDUE = "OVERDUE"
    CANCELLED = "CANCELLED"


class ReferralTier(str, Enum):
    BHW_TO_BHS = "BHW_TO_BHS"
    BHS_TO_RHU = "BHS_TO_RHU"
    RHU_TO_HOSPITAL = "RHU_TO_HOSPITAL"


# Auth Models
class OTPRequestModel(BaseModel):
    phone_number: str = Field(..., pattern=r"^\+?639\d{9}$")

    @field_validator("phone_number")
    @classmethod
    def normalize_phone(cls, v: str) -> str:
        """Normalize to +639XXXXXXXXX format"""
        v = v.replace(" ", "").replace("-", "")
        if v.startswith("09"):
            v = "+63" + v[1:]
        elif v.startswith("9") and len(v) == 10:
            v = "+63" + v
        return v


class OTPVerifyModel(BaseModel):
    phone_number: str
    otp_code: str = Field(..., min_length=6, max_length=6)


class AuthResponseModel(BaseModel):
    token: str
    user_id: str
    role: str
    expires_in: int  # seconds


# Patient Models
class PatientUploadModel(BaseModel):
    id: str
    phone_number: str
    full_name: str
    date_of_birth: int  # timestamp
    sex: str
    barangay_id: str
    barangay_name: str
    philhealth_number: Optional[str] = None
    philsys_number: Optional[str] = None
    is_pregnant: bool = False
    gestational_age_weeks: Optional[int] = None
    expected_delivery_date: Optional[int] = None
    messenger_opt_in: bool = False
    messenger_user_id: Optional[str] = None
    high_risk_flag: bool = False
    high_risk_reasons: List[str] = []
    maternal_high_risk: bool = False
    created_at: int
    updated_at: int
    sync_status: str


# Scan Models
class ScanUploadModel(BaseModel):
    id: str
    patient_id: str
    bhw_id: str
    scan_type: str
    biomarkers: Dict[str, Any]  # JSON with all 34 biomarkers
    signal_quality: Optional[str] = None
    validated: bool
    risk_flags: List[str]
    scanned_at: int
    sync_status: str


# Survey Models
class SurveyUploadModel(BaseModel):
    id: str
    scan_id: str
    patient_id: str
    survey_type: str
    responses: Dict[str, Any]  # JSON responses
    completed_at: int
    sync_status: str


# Referral Models
class ReferralUploadModel(BaseModel):
    id: str
    patient_id: str
    scan_id: str
    tier: str
    status: str
    risk_flags: List[str]
    notes: Optional[str] = None
    referred_by: str
    referred_at: int
    due_by: int
    resolved_at: Optional[int] = None
    resolved_by: Optional[str] = None
    resolution_notes: Optional[str] = None
    sync_status: str


# Sync Models
class SyncUploadRequest(BaseModel):
    device_id: str
    bhw_id: str
    sync_timestamp: int
    patients: List[PatientUploadModel] = []
    scans: List[ScanUploadModel] = []
    surveys: List[SurveyUploadModel] = []
    referrals: List[ReferralUploadModel] = []


class SyncUploadResponse(BaseModel):
    success: bool
    sync_receipt_id: str
    accepted_counts: Dict[str, int]  # {"patients": 5, "scans": 3, ...}
    rejected_counts: Dict[str, int]
    rejected_reasons: List[Dict[str, Any]]  # [{"id": "...", "type": "...", "reason": "..."}]
    server_timestamp: int


class SyncDownloadRequest(BaseModel):
    device_id: str
    bhw_id: str
    last_sync_timestamp: int


class SyncDownloadResponse(BaseModel):
    config_thresholds: Optional[Dict[str, Any]] = None
    referral_updates: List[Dict[str, Any]] = []
    patient_updates: List[Dict[str, Any]] = []
    server_timestamp: int


# Config Models
class ConfigThresholdModel(BaseModel):
    id: str
    threshold_type: str
    parameter_name: str
    low_threshold: Optional[float] = None
    high_threshold: Optional[float] = None
    unit: Optional[str] = None
    description: Optional[str] = None
    active: bool
    updated_at: int
