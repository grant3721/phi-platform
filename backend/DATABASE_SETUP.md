# Database Setup Guide

This guide covers three options for setting up the PostgreSQL database for the PHI Platform.

## Option 1: Docker (Recommended for Development)

**Pros:** Fast setup, isolated, easy cleanup
**Cons:** Requires Docker installed

### Prerequisites
- Docker Desktop installed and running

### Setup Steps

1. **Start PostgreSQL with Docker Compose:**
   ```bash
   cd backend
   docker-compose up -d
   ```

2. **Verify container is running:**
   ```bash
   docker-compose ps
   ```
   Should show `phi-postgres` as `Up`

3. **View logs (optional):**
   ```bash
   docker-compose logs -f postgres
   ```

4. **Update local.settings.json:**
   ```json
   {
     "DATABASE_HOST": "localhost",
     "DATABASE_PORT": "5432",
     "DATABASE_NAME": "phi_platform",
     "DATABASE_USER": "postgres",
     "DATABASE_PASSWORD": "phi_dev_password_123"
   }
   ```

5. **Test connection:**
   ```bash
   python scripts/test_connection.py
   ```

6. **Run migrations:**
   ```bash
   python scripts/run_migrations.py
   ```

### Useful Commands

**Connect to database:**
```bash
docker exec -it phi-postgres psql -U postgres -d phi_platform
```

**Stop database:**
```bash
docker-compose down
```

**Stop and remove all data:**
```bash
docker-compose down -v
```

**Restart database:**
```bash
docker-compose restart
```

---

## Option 2: Azure Database for PostgreSQL

**Pros:** Production-ready, managed service, backups included
**Cons:** Costs money, slightly slower for local dev

### Setup Steps

1. **Create Azure Database for PostgreSQL:**
   ```bash
   # Login to Azure
   az login

   # Create resource group (if not exists)
   az group create --name phi-platform-rg --location eastasia

   # Create PostgreSQL server
   az postgres flexible-server create \
     --resource-group phi-platform-rg \
     --name phi-platform-db \
     --location eastasia \
     --admin-user phiadmin \
     --admin-password "YourStrongPassword123!" \
     --sku-name Standard_B1ms \
     --tier Burstable \
     --version 15 \
     --storage-size 32 \
     --public-access 0.0.0.0

   # Create database
   az postgres flexible-server db create \
     --resource-group phi-platform-rg \
     --server-name phi-platform-db \
     --database-name phi_platform
   ```

2. **Configure firewall (allow your IP):**
   ```bash
   # Get your public IP
   curl https://api.ipify.org

   # Add firewall rule
   az postgres flexible-server firewall-rule create \
     --resource-group phi-platform-rg \
     --name phi-platform-db \
     --rule-name AllowMyIP \
     --start-ip-address YOUR_IP \
     --end-ip-address YOUR_IP
   ```

3. **Update local.settings.json:**
   ```json
   {
     "DATABASE_HOST": "phi-platform-db.postgres.database.azure.com",
     "DATABASE_PORT": "5432",
     "DATABASE_NAME": "phi_platform",
     "DATABASE_USER": "phiadmin",
     "DATABASE_PASSWORD": "YourStrongPassword123!"
   }
   ```

4. **Test connection and run migrations:**
   ```bash
   python scripts/test_connection.py
   python scripts/run_migrations.py
   ```

### Estimated Costs
- Standard_B1ms: ~$12/month
- Storage (32GB): ~$4/month
- **Total: ~$16/month**

---

## Option 3: Local PostgreSQL Installation

**Pros:** Full control, no Docker needed
**Cons:** More setup steps, can conflict with other apps

### Windows Setup

1. **Download PostgreSQL:**
   - Go to: https://www.postgresql.org/download/windows/
   - Download PostgreSQL 15 installer

2. **Install PostgreSQL:**
   - Run installer
   - Set password for `postgres` user
   - Default port: 5432
   - Install pgAdmin (optional, useful GUI)

3. **Create database:**
   ```bash
   # Using psql
   psql -U postgres
   CREATE DATABASE phi_platform;
   \q
   ```

4. **Update local.settings.json:**
   ```json
   {
     "DATABASE_HOST": "localhost",
     "DATABASE_PORT": "5432",
     "DATABASE_NAME": "phi_platform",
     "DATABASE_USER": "postgres",
     "DATABASE_PASSWORD": "your_password"
   }
   ```

5. **Test and migrate:**
   ```bash
   python scripts/test_connection.py
   python scripts/run_migrations.py
   ```

### macOS Setup

1. **Install PostgreSQL with Homebrew:**
   ```bash
   brew install postgresql@15
   brew services start postgresql@15
   ```

2. **Create database:**
   ```bash
   createdb phi_platform
   ```

3. **Continue with step 4 above**

### Linux Setup

1. **Install PostgreSQL:**
   ```bash
   sudo apt update
   sudo apt install postgresql postgresql-contrib
   sudo systemctl start postgresql
   ```

2. **Create database:**
   ```bash
   sudo -u postgres psql
   CREATE DATABASE phi_platform;
   \q
   ```

3. **Continue with step 4 above**

---

## Verifying Setup

After setup with any option, verify everything works:

### 1. Test Connection
```bash
cd backend
python scripts/test_connection.py
```

**Expected output:**
```
========================================================
PHI Platform - Database Connection Test
========================================================

✓ Connection successful!

PostgreSQL Version:
PostgreSQL 15.x ...

Checking schemas...
✓ Found schemas:
  - bronze: 5 tables
  - gold: 6 tables
  - public: 2 tables
  - silver: 7 tables

Testing query performance...
✓ Query latency: 2.34ms

========================================================
✓ All tests passed - Database ready!
========================================================
```

### 2. Verify Seed Data

Connect to database:
```bash
# Docker
docker exec -it phi-postgres psql -U postgres -d phi_platform

# Local
psql -U postgres -d phi_platform
```

Check seed data:
```sql
-- Count barangays (should be 50)
SELECT COUNT(*) FROM silver.barangays;

-- Count thresholds (should be 13)
SELECT COUNT(*) FROM silver.config_thresholds;

-- List barangays
SELECT id, name, municipality, province FROM silver.barangays LIMIT 10;

-- List thresholds
SELECT id, parameter_name, low_threshold, high_threshold, unit
FROM silver.config_thresholds
WHERE active = TRUE;
```

### 3. Check Table Structure

```sql
-- List all schemas
\dn

-- List tables in silver schema
\dt silver.*

-- Describe patients table
\d silver.patients
```

---

## Troubleshooting

### Connection Refused
- **Docker**: Ensure container is running: `docker-compose ps`
- **Local**: Check PostgreSQL service: `pg_ctl status`
- **Azure**: Verify firewall rules allow your IP

### Authentication Failed
- Check password in `local.settings.json`
- For Docker, password is: `phi_dev_password_123`

### Database Does Not Exist
```bash
# Docker
docker exec -it phi-postgres psql -U postgres -c "CREATE DATABASE phi_platform;"

# Local
createdb -U postgres phi_platform
```

### Migrations Failed
- Check error message in output
- Verify you have the correct PostgreSQL version (15+)
- Try running migrations one at a time:
  ```bash
  psql -U postgres -d phi_platform -f migrations/001_create_bronze_schema.sql
  ```

---

## Next Steps

After database setup:

1. **Generate dummy data** (50,000 records)
2. **Test Azure Functions locally**
3. **Build sync endpoints**
4. **Connect Android app**

---

## Quick Reference

| Task | Docker | Azure | Local |
|------|--------|-------|-------|
| Start | `docker-compose up -d` | N/A | `pg_ctl start` |
| Stop | `docker-compose down` | N/A | `pg_ctl stop` |
| Connect | `docker exec -it phi-postgres psql -U postgres -d phi_platform` | `psql -h <host> -U phiadmin -d phi_platform` | `psql -U postgres -d phi_platform` |
| Reset | `docker-compose down -v && docker-compose up -d` | Drop/recreate database | `dropdb phi_platform && createdb phi_platform` |
