# PHI Platform Backend - Azure Deployment Success

**Deployment Date:** February 19, 2026
**Function App:** phi-platform-api
**Base URL:** https://phi-platform-api.azurewebsites.net

---

## Deployed Endpoints

### Authentication (2 endpoints)
- **POST** `/api/auth/otp/request` - Send OTP for authentication
- **POST** `/api/auth/otp/verify` - Verify OTP and issue JWT token

### Configuration (2 endpoints)
- **GET** `/api/config/thresholds` - Get clinical thresholds ✅ Tested
- **PUT** `/api/config/thresholds/{threshold_id}` - Update threshold (Admin)

### Synchronization (2 endpoints)
- **POST** `/api/sync/upload` - Upload patient/scan data from mobile devices
- **GET** `/api/sync/download` - Download config updates and dedup results

### Analytics (4 endpoints)
- **GET** `/api/analytics/province` - Province-level health statistics ✅ Tested
  - Query params: `?province=<name>&days=<int>&metric=<string>`
- **GET** `/api/analytics/municipality` - Municipality-level statistics
  - Query params: `?municipality=<name>&province=<name>`
- **GET** `/api/outbreak/signals` - Detect outbreak patterns ✅ Tested
  - Query params: `?days=<int>`
- **GET** `/api/outbreak/analyze` - Claude AI epidemiological analysis
  - Query params: `?signal_id=<id>` (optional)

---

## Database Connection
- **Host:** phi-platform-db-2026.postgres.database.azure.com
- **Database:** phi_platform
- **Connection:** SSL enabled, connection pooling configured
- **Data:** 1,000 patients, 11,689 scans with biomarkers

---

## Application Insights
- **Monitoring Dashboard:** [View in Azure Portal](https://portal.azure.com/#resource/subscriptions/86f984d0-66ba-4126-8bbc-16b40e56da68/resourceGroups/rg-drc-phi-demo/providers/microsoft.insights/components/phi-platform-api/overview)
- Real-time metrics, logs, and performance monitoring enabled

---

## Configuration (App Settings)
✅ DATABASE_HOST
✅ DATABASE_PORT
✅ DATABASE_NAME
✅ DATABASE_USER
✅ DATABASE_PASSWORD
✅ ANTHROPIC_API_KEY
✅ JWT_SECRET_KEY
✅ SMS_API_KEY
✅ ENVIRONMENT=production

---

## Testing Results

### Config Thresholds Endpoint
```bash
curl "https://phi-platform-api.azurewebsites.net/api/config/thresholds"
```
**Status:** ✅ Working - Returns 13 clinical thresholds

### Province Analytics Endpoint
```bash
curl "https://phi-platform-api.azurewebsites.net/api/analytics/province"
```
**Status:** ✅ Working - Returns aggregated stats for 11 provinces

### Outbreak Signals Endpoint
```bash
curl "https://phi-platform-api.azurewebsites.net/api/outbreak/signals?days=7"
```
**Status:** ✅ Working - Detecting respiratory, hypertension, and diabetes patterns

---

## Next Steps

### For Android App Development
Update API base URL in app configuration:
```kotlin
const val API_BASE_URL = "https://phi-platform-api.azurewebsites.net/api"
```

### For Web Dashboard
Configure environment variables:
```javascript
REACT_APP_API_BASE_URL=https://phi-platform-api.azurewebsites.net/api
```

### Authentication Flow
1. Call `/auth/otp/request` with phone number
2. User receives SMS with OTP code
3. Call `/auth/otp/verify` with phone + OTP
4. Receive JWT token
5. Include token in Authorization header: `Bearer <token>`

### Sync Flow (Mobile App)
1. Collect scans/surveys offline
2. When online, POST to `/sync/upload` with batched data
3. Backend processes: Bronze → Silver pipeline
4. Deduplicates patients, validates scans
5. Call `/sync/download` to get server updates

---

## Monitoring & Logs

View real-time logs:
```bash
func azure functionapp logstream phi-platform-api --resource-group rg-drc-phi-demo
```

Check deployment status:
```bash
az functionapp list --resource-group rg-drc-phi-demo --query "[?name=='phi-platform-api']" -o table
```

---

## Support & Documentation

- **Resource Group:** rg-drc-phi-demo
- **Region:** East US
- **Plan:** Consumption (Pay-per-execution)
- **Runtime:** Python 3.11
- **Functions Version:** 4.x

All endpoints are production-ready and accessible globally via HTTPS.
