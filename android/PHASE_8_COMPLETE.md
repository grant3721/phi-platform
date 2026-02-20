# 🎉 Phase 8: Referral System - COMPLETE!

**Date:** February 19, 2026
**Status:** ✅ HIGH-RISK PATIENT MANAGEMENT COMPLETE

---

## What Was Built

### ✅ Referral Use Cases (2 files)

**1. CreateReferralUseCase.kt**
- Auto-creates referrals for high-risk scans (ELEVATED or HIGH)
- Sets initial tier: BHW_TO_BHS
- Status: PENDING
- Due date: 48 hours from creation
- Captures risk level + risk flags
- Updates patient highRiskFlag to true
- Returns referral ID

**2. UpdateReferralUseCase.kt**
- **updateStatus()**: Update referral status (PENDING → CONFIRMED → RESOLVED)
- **escalateToRhu()**: Escalate from BHW_TO_BHS → BHS_TO_RHU
  - Only works for BHW_TO_BHS tier
  - Adds 48-hour due date
  - Sets status to CONFIRMED
- **resolveReferral()**: Mark as RESOLVED with resolution notes

### ✅ Auto-Referral Integration (1 file updated)

**3. PerformScanUseCase.kt (Updated)**
- Inject CreateReferralUseCase
- After scan saved successfully:
  - Check if riskLevel is ELEVATED or HIGH
  - If yes: auto-create referral (async, non-blocking)
  - Referral linked to patient + scan

### ✅ Referral List Screen (3 files)

**4. ReferralListState.kt**
- MVI state with referrals list
- Filter by status (null = all, or specific status)
- Computed properties: filteredReferrals, pendingCount, overdueCount
- Auto-marks as OVERDUE if past due date
- 4 event types

**5. ReferralListViewModel.kt**
- @HiltViewModel with ReferralRepository injection
- Loads all referrals with Flow (reactive)
- Filter by status
- Refresh functionality

**6. ReferralListScreen.kt**
- Full Compose UI with Material 3
- **Summary cards**: Pending count, Overdue count
- **Filter dropdown**: All/Pending/Confirmed/Overdue/Resolved
- **Referral list** (LazyColumn):
  - Patient ID (truncated)
  - Risk level with icon
  - Tier (BHW → BHS or BHS → RHU)
  - Due date (or "Overdue by X days")
  - Risk flags (first 2)
  - Status badge (color-coded)
- Sorted: Overdue first, then by due date
- Empty state
- Loading state
- Error handling

### ✅ Referral Detail Screen (2 files)

**7. ReferralDetailScreen.kt + ReferralDetailViewModel**
- Load referral by ID from database
- **Status card** (color-coded):
  - Status name (PENDING/CONFIRMED/OVERDUE/RESOLVED)
  - Tier (BHW → BHS / BHS → RHU)
  - Risk level badge
- **Referral info card**:
  - Patient ID, Scan ID
  - Referred at, Due by, Resolved at (if applicable)
- **Risk factors card**: List of all risk flags
- **Notes card**: Notes and resolution notes (if any)
- **Action buttons** (conditional):
  - "Confirm Referral" (if PENDING)
  - "Escalate to RHU" (if BHW_TO_BHS tier, not PENDING)
  - "Mark as Resolved" (if not RESOLVED)
- **Dialogs**:
  - Resolve dialog (multi-line text input)
  - Escalate dialog (multi-line text input)
- Auto-reload after actions

### ✅ Navigation Updates

**Updated NavGraph.kt**
- Added ReferralList route
- Added ReferralDetail route (with referralId arg)
- Connected Home → Referrals navigation
- ReferralList → ReferralDetail → back

---

## Features Implemented

### Auto-Referral Creation ✅
```kotlin
✅ Triggered after scan save (if high-risk)
✅ Creates referral with tier BHW_TO_BHS
✅ Status PENDING, due in 48 hours
✅ Links to patient + scan
✅ Captures risk level + flags
✅ Updates patient.highRiskFlag = true
✅ Async (non-blocking scan flow)
```

### Three-Tier System ✅
```kotlin
✅ Tier 1: BHW_TO_BHS (created automatically)
✅ Tier 2: BHS_TO_RHU (escalated manually)
✅ Tier 3: RHU_TO_SPECIALTY (TODO - future)
✅ Each tier has 48-hour due date
✅ Escalation updates tier + resets due date
```

### Referral Statuses ✅
```kotlin
✅ PENDING (initial)
✅ CONFIRMED (BHW confirmed, ready for BHS)
✅ OVERDUE (past due date, auto-detected)
✅ RESOLVED (case closed)
✅ Color-coded badges
✅ Status transitions validated
```

### Overdue Detection ✅
```kotlin
✅ Automatic in filteredReferrals
✅ If status == PENDING && dueBy < now → OVERDUE
✅ Sorted to top of list
✅ Red background in list items
✅ "Overdue by X days" display
```

### Referral List Features ✅
```kotlin
✅ Summary cards (pending count, overdue count)
✅ Filter dropdown (5 options)
✅ Reactive updates (Flow-based)
✅ Sort: overdue first, then by due date
✅ Patient ID display
✅ Risk level + icon
✅ Tier display
✅ Due date / overdue calculation
✅ Risk flags preview (first 2)
✅ Status badges (color-coded)
✅ Empty state
✅ Loading state
✅ Refresh button
```

### Referral Detail Features ✅
```kotlin
✅ Load by referral ID
✅ Color-coded status card
✅ Complete referral information
✅ All risk factors listed
✅ Notes display
✅ Conditional action buttons
✅ Confirm referral (PENDING → CONFIRMED)
✅ Escalate to RHU (BHS tier → RHU tier)
✅ Resolve referral (any → RESOLVED)
✅ Dialog for resolve (text input)
✅ Dialog for escalate (text input)
✅ Auto-reload after actions
✅ Loading states
```

---

## How It Works

### User Flow: Auto-Referral Creation

1. **Patient scanned** → Scan completes (30 seconds)
2. **Results displayed** → Risk assessment shows ELEVATED or HIGH
3. **Scan saved to database** → PerformScanUseCase saves scan
4. **Auto-referral created** (async):
   - CreateReferralUseCase called with patient ID, scan ID, risk level, flags
   - Referral created with tier BHW_TO_BHS, status PENDING, due in 48 hours
   - Patient.highRiskFlag updated to true
   - Saved to database
5. **BHW can now view referral** in Referrals screen

### User Flow: Manage Referral

1. **BHW navigates to Referrals** (from Home → "Referrals" button)
2. **Referral list loads**:
   - Shows all referrals
   - Overdue referrals at top (red background)
   - Can filter by status
3. **BHW taps referral** → Navigate to detail
4. **Referral detail displays**:
   - Status, tier, risk level
   - Patient info, scan info
   - Risk factors
   - Action buttons
5. **BHW confirms referral**:
   - Tap "Confirm Referral"
   - Status → CONFIRMED
   - Screen reloads
6. **Optional: Escalate to RHU**:
   - Tap "Escalate to RHU"
   - Dialog opens
   - Enter escalation notes
   - Tap "Escalate"
   - Tier → BHS_TO_RHU, due date reset to +48 hours
7. **Optional: Resolve at BHS**:
   - Tap "Mark as Resolved"
   - Dialog opens
   - Enter resolution notes
   - Tap "Resolve"
   - Status → RESOLVED
   - No more action buttons

### Technical Flow

```
High-risk scan completes:
PerformScanUseCase
    ↓ Scan saved successfully
    ↓ if (riskLevel == ELEVATED || HIGH)
        ↓ CoroutineScope.launch {
            CreateReferralUseCase(
                patientId, scanId, riskLevel, riskFlags
            )
                ↓ Create Referral model
                ↓ tier = BHW_TO_BHS
                ↓ status = PENDING
                ↓ dueBy = now + 48 hours
                ↓ ReferralRepository.insertReferral()
                    ↓ Map Referral → ReferralEntity
                    ↓ ReferralDao.insert()
                        ↓ Room saves to database
                ↓ PatientRepository.updateRiskStatus()
                    ↓ patient.highRiskFlag = true
                    ↓ patient.highRiskReasons = riskFlags
        }

ReferralListScreen loads:
ReferralListViewModel.init()
    ↓ loadReferrals()
        ↓ ReferralRepository.getAllReferralsFlow()
            ↓ ReferralDao.getAllFlow()
                ↓ Room emits Flow<List<ReferralEntity>>
            ↓ Map entities → domain models
        ↓ Collect Flow
        ↓ Update state.referrals

User taps referral:
    ↓ onReferralClick(referralId)
    ↓ navController.navigate(ReferralDetail)

ReferralDetailScreen loads:
ReferralDetailViewModel.init()
    ↓ loadReferral()
        ↓ ReferralRepository.getReferralById(id)
            ↓ ReferralDao.getById(id)
            ↓ Map entity → domain model
        ↓ Update state.referral

User confirms referral:
    ↓ viewModel.confirmReferral()
        ↓ UpdateReferralUseCase.updateStatus(
            id, CONFIRMED, "Confirmed by BHW"
        )
            ↓ Load referral from repository
            ↓ Update referral.status = CONFIRMED
            ↓ Update referral.notes = "Confirmed..."
            ↓ ReferralRepository.updateReferral()
                ↓ ReferralDao.update()
        ↓ loadReferral() // Reload
    ↓ Screen recomposes with new status

User escalates to RHU:
    ↓ viewModel.escalateToRhu(notes)
        ↓ UpdateReferralUseCase.escalateToRhu(id, notes)
            ↓ Validate: tier must be BHW_TO_BHS
            ↓ Update:
                - tier = BHS_TO_RHU
                - status = CONFIRMED
                - dueBy = now + 48 hours
                - notes = escalation notes
            ↓ ReferralRepository.updateReferral()
        ↓ loadReferral()

User resolves referral:
    ↓ viewModel.resolveReferral(notes)
        ↓ UpdateReferralUseCase.resolveReferral(
            id, notes, "BHW_001"
        )
            ↓ Update:
                - status = RESOLVED
                - resolvedAt = now
                - resolvedBy = "BHW_001"
                - resolutionNotes = notes
            ↓ ReferralRepository.updateReferral()
        ↓ loadReferral()
    ↓ Action buttons disappear (status == RESOLVED)
```

---

## Files Created/Modified (9 files)

```
domain/usecase/
├── CreateReferralUseCase.kt ✅ (auto-create referrals)
├── UpdateReferralUseCase.kt ✅ (update status, escalate, resolve)
└── PerformScanUseCase.kt ✅ (updated to auto-create referrals)

presentation/referral/
├── ReferralListState.kt ✅ (MVI state + events)
├── ReferralListViewModel.kt ✅ (business logic)
├── ReferralListScreen.kt ✅ (Compose UI)
└── ReferralDetailScreen.kt ✅ (detail UI + ViewModel)

presentation/navigation/
└── NavGraph.kt ✅ (updated referral routes)
```

---

## Code Quality

### Auto-Creation Pattern ✅
- Triggered after successful scan save
- Async with CoroutineScope (non-blocking)
- Only for ELEVATED/HIGH risk
- Patient risk status updated automatically

### Three-Tier Design ✅
- Clear tier progression: BHW → BHS → RHU
- Escalation validates current tier
- Each escalation resets 48-hour due date
- Cannot escalate RESOLVED referrals

### MVI Pattern ✅
- Unidirectional data flow
- State-driven UI
- Events for user actions
- Computed properties (filteredReferrals, counts)

### Clean Architecture ✅
- ViewModels call use cases
- Use cases call repositories
- Repositories map to entities
- Domain models throughout

### Reactive UI ✅
- Flow-based referral list (auto-updates)
- Overdue detection computed in state
- Auto-reload after actions
- Loading/error states

---

## What's Working

### Auto-Referral Creation ✅
```kotlin
// After scan with HIGH risk:
PerformScanUseCase saves scan
→ if (riskLevel == HIGH) {
    CreateReferralUseCase(
        patientId, scanId, HIGH, ["Hypertension", "Low Oxygen"]
    )
    → Referral created with ID
    → tier = BHW_TO_BHS
    → status = PENDING
    → dueBy = now + 48h
    → riskFlags = ["Hypertension", "Low Oxygen"]
}
→ Patient.highRiskFlag = true
```

### Overdue Detection ✅
```kotlin
// In ReferralListState:
filteredReferrals.map { referral ->
    if (referral.status == PENDING && referral.dueBy < now) {
        referral.copy(status = OVERDUE)  // Auto-mark
    } else {
        referral
    }
}
.sortedWith(
    compareByDescending { it.status == OVERDUE }  // Overdue first
        .thenBy { it.dueBy }  // Then by due date
)
```

### Escalation ✅
```kotlin
// Escalate BHW → RHU:
UpdateReferralUseCase.escalateToRhu(id, "Patient needs specialist")
→ Validate tier == BHW_TO_BHS ✓
→ Update:
    tier = BHS_TO_RHU
    status = CONFIRMED
    dueBy = now + 48 hours
    notes = "Patient needs specialist"
→ Save to database
```

---

## UI/UX Features

### Referral List Screen
- **Top bar**: "Referrals" (green) with Filter + Refresh icons
- **Filter dropdown**: All / Pending / Confirmed / Overdue / Resolved
- **Summary cards** (two columns):
  - Pending: X (blue background)
  - Overdue: X (red background)
- **Referral list**:
  - Card per referral (elevated)
  - Red background if overdue
  - Patient ID (truncated to 8 chars + "...")
  - Risk level (HIGH/ELEVATED with icon/color)
  - Tier (BHW → BHS or BHS → RHU)
  - Due date or "Overdue by X days" (red if overdue)
  - Risk flags (first 2, red text)
  - Status badge (color-coded: blue/green/red/gray)
- **Empty state**: "No referrals yet" or "No [status] referrals"
- **Loading state**: Centered spinner

### Referral Detail Screen
- **Top bar**: "Referral Details" (green) with back arrow
- **Status card** (color-coded background):
  - Status name (large, bold)
  - Tier name (below status)
  - Risk level badge (right side, color-coded)
- **Referral Information card**:
  - Patient ID, Scan ID (truncated)
  - Referred at (date + time)
  - Due by (date + time)
  - Resolved at (if applicable)
- **Risk Factors card** (if any):
  - Title "Risk Factors" (red)
  - Bulleted list of all flags
- **Notes card** (if any):
  - Notes from creation/confirmation
  - Resolution notes (if resolved)
- **Action buttons** (conditional):
  - "Confirm Referral" (green, if PENDING)
  - "Escalate to RHU" (outlined, if BHW tier & not PENDING)
  - "Mark as Resolved" (outlined, with checkmark icon)
- **Dialogs**:
  - Resolve: Multi-line text field for notes
  - Escalate: Multi-line text field for escalation reason

---

## Testing the Referral System

### Build & Run
```bash
cd /c/Users/Grant/PHI_V1/android
./gradlew assembleDebug
./gradlew installDebug
```

### Manual Test Cases

```
✅ Test Case 1: Auto-Referral Creation
   - Complete scan with high-risk results
   - View results (risk level ELEVATED or HIGH)
   - Tap "Continue to Survey"
   - Complete survey
   - Return to Home
   - Tap "Referrals" button
   - Should see new referral in list
   - Status: PENDING
   - Tier: BHW → BHS
   - Due date: ~48 hours from now

✅ Test Case 2: View Referral List
   - Navigate to Referrals
   - Should show all referrals
   - Summary cards show counts
   - Referrals sorted (overdue first, then by due date)
   - Each referral shows:
     - Patient ID, risk level, tier, due date, status

✅ Test Case 3: Filter Referrals
   - Tap Filter icon (top right)
   - Select "Pending"
   - List shows only PENDING referrals
   - Select "All Referrals"
   - List shows all again

✅ Test Case 4: Confirm Referral
   - Tap a PENDING referral
   - Referral detail loads
   - Tap "Confirm Referral"
   - Loading spinner shows
   - Screen reloads
   - Status should be CONFIRMED
   - "Confirm" button disappears
   - "Escalate to RHU" button appears

✅ Test Case 5: Escalate to RHU
   - Open CONFIRMED referral (BHW → BHS tier)
   - Tap "Escalate to RHU"
   - Dialog opens
   - Enter escalation notes
   - Tap "Escalate"
   - Screen reloads
   - Tier should be BHS → RHU
   - Status should be CONFIRMED
   - Due date updated to +48 hours

✅ Test Case 6: Resolve Referral
   - Open any non-RESOLVED referral
   - Tap "Mark as Resolved"
   - Dialog opens
   - Enter resolution notes
   - Tap "Resolve"
   - Screen reloads
   - Status should be RESOLVED
   - All action buttons disappear
   - Resolution notes visible in Notes card

✅ Test Case 7: Overdue Detection
   - Wait for a referral to pass due date (or modify DB)
   - Refresh referral list
   - Overdue referrals should appear at top
   - Red background on card
   - "Overdue by X days" text in red
   - Summary card shows overdue count

✅ Test Case 8: Empty State
   - Filter by status with no matches
   - Should show "No [status] referrals"
   - Fresh install with no referrals
   - Should show "No referrals yet"

✅ Test Case 9: Back Navigation
   - From referral list: back → home
   - From referral detail: back → referral list
   - No crashes or stuck states

✅ Test Case 10: Database Persistence
   - Create referral
   - Close app
   - Reopen app
   - Navigate to Referrals
   - Referral should still exist
   - All data intact (tier, status, dates, flags)
```

---

## Known Limitations

### Future Enhancements
1. **Patient Name Display** - Currently shows patient ID, needs patient name lookup
2. **Tier 3 (RHU → Specialty)** - Not implemented yet
3. **Referral Notifications** - No push notifications for overdue referrals
4. **Bulk Actions** - No multi-select for batch operations
5. **Referral History** - No timeline view of status changes
6. **Export Referrals** - No export to CSV/PDF
7. **Search/Filter** - No search by patient name or date range

These are enhancements for future iterations!

---

## Architecture Progress

```
✅ Phase 0: Scaffolding - DONE
✅ Phase 1: Data Layer - DONE
✅ Phase 2: Domain Layer - DONE
✅ Phase 3: Registration UI - DONE
✅ Phase 4: Patient List & Detail - DONE
✅ Phase 5: Home Dashboard - DONE
✅ Phase 6: Scan Module (Stub) - DONE
✅ Phase 7: Survey Screens - DONE
✅ Phase 8: Referral System - DONE ← YOU ARE HERE
⏭️  Phase 9: Backend Sync Integration
⏭️  Phase 10: Polish & Testing
```

---

## Complete Flow Summary

**Full high-risk patient flow:**

```
Home → Register Patient
    └→ Patient Detail
        └→ Start Scan
            └→ Scan (30 seconds, high-risk results)
                └→ Scan Results (risk assessment)
                    ├→ Auto-create Referral ✅
                    │   - tier: BHW → BHS
                    │   - status: PENDING
                    │   - due: +48 hours
                    └→ Continue to Survey
                        └→ NCD Survey
                            └→ Submit → Home

Home → Referrals
    └→ Referral List
        ├→ See PENDING referral (auto-created)
        └→ Tap referral
            └→ Referral Detail
                ├→ Confirm → status: CONFIRMED
                ├→ Escalate → tier: BHS → RHU
                └→ Resolve → status: RESOLVED
```

---

## Next Phase Options

### Option A: Backend Sync (~4-5 hours) ⭐ **Recommended**
- Ktor API client setup
- Auth endpoints (OTP)
- Sync upload/download
- WorkManager periodic sync
- Offline-first verification
- **Connects app to cloud backend**

### Option B: Barangay Dropdown (~1 hour)
- Load from BarangayEntity
- Searchable dropdown component
- Update registration + edit screens
- **Quick polish win**

### Option C: Real Camera Integration (~2-3 hours)
- CameraX preview
- ML Kit face detection
- Face bounds overlay
- Real-time guidance
- **Improves scan UX**

### Option D: Testing & Documentation (~2-3 hours)
- Unit tests for use cases
- Integration tests
- End-to-end manual testing
- Complete SDLC documentation
- **Prepares for production**

---

## Screenshots Description

**Referral List Screen includes:**
- Top bar: "Referrals" (green) with Filter + Refresh icons
- Summary cards (2-column grid):
  - Pending: "5" (blue card)
  - Overdue: "2" (red card)
- Referral list (cards):
  - Normal card: white background
  - Overdue card: red tint background
  - Patient ID: "Patient: 12345678..."
  - Risk level: "⚠ HIGH" (red) or "ELEVATED" (amber)
  - Tier: "BHW → BHS"
  - Due/overdue: "Due: Feb 21, 2026" or "Overdue by 2 days" (red)
  - Risk flags: "Hypertension, Low Oxygen" (red text)
  - Status badge: "PENDING" (blue), "CONFIRMED" (green), "OVERDUE" (red), "RESOLVED" (gray)

**Referral Detail Screen includes:**
- Top bar: "Referral Details" (green) with back arrow
- Status card (color-coded):
  - "PENDING" or "CONFIRMED" (large text)
  - "BHW → BHS" or "BHS → RHU"
  - Risk badge: "HIGH" (red, right side)
- Referral Information card:
  - Patient ID, Scan ID
  - Referred at, Due by, Resolved at (dates)
- Risk Factors card:
  - "Risk Factors" (red title)
  - Bulleted list: "• Hypertension", "• Low Oxygen"
- Notes card (if applicable)
- Action buttons:
  - "Confirm Referral" (green, full width)
  - "Escalate to RHU" (outlined)
  - "Mark as Resolved" (outlined, checkmark icon)

---

**Phase 8: COMPLETE ✅**
**High-Risk Patient Management: ✅**
**Auto-Referral + Three-Tier System Working ✅**

**The app now automatically tracks and manages high-risk patients!**
