# Complete Supabase Setup Guide

## Step 1: Create Supabase Account (FREE)

1. Go to [supabase.com](https://supabase.com)
2. Click "Sign Up" (no credit card required)
3. Create account with email/GitHub
4. Create new organization

## Step 2: Create Project

1. Click "New Project"
2. Enter project name: **family-guard**
3. Set database password (save this!)
4. Choose region closest to you
5. Click "Create New Project" (wait 5-10 minutes for setup)

## Step 3: Get API Keys

After project is created:
1. Go to **Settings → API** (left sidebar)
2. Copy your **Project URL** - looks like: `https://xyzabc123.supabase.co`
3. Copy **anon public key** - paste into `SupabaseClient.java`
4. Copy **service_role key** (keep secret - don't share or commit)

## Step 4: Create Database Schema

1. Go to **SQL Editor** (left sidebar)
2. Click "New Query"
3. Copy all SQL from `supabase/migrations/001_initial_schema.sql` file
4. Paste into editor
5. Click "Run" (execute all tables)

## Step 5: Create Storage Bucket

1. Go to **Storage** (left sidebar)
2. Click "Create New Bucket"
3. Name it: **media**
4. Uncheck "Public bucket" (private)
5. Click "Create Bucket"
6. Go to bucket → Policies → Create new policy
7. Template: "Allow all users to update their own data"
8. Click "Review" → "Save policy"

## Step 6: Enable Notification Parameters (Optional)

Go to **Database → Extensions** and enable:
- uuid-ossp (if not already enabled)

## Step 7: Update Android Code

File: `app/src/main/java/com/family/parentalcontrol/utils/SupabaseClient.java`

Replace these lines:
```java
private static final String SUPABASE_URL = "https://YOUR_PROJECT.supabase.co";
private static final String SUPABASE_KEY = "YOUR_ANON_KEY";
```

With your actual values:
```java
private static final String SUPABASE_URL = "https://xyzabc123.supabase.co";
private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
```

## Supabase Table Structure

### profiles table
- `id` (UUID, primary key, references auth.users)
- `device_mode` ('parent' or 'child')
- `device_name` (TEXT)
- `master_pin` (TEXT - encrypted)
- `created_at` (TIMESTAMP)

### relationships table
- `parent_id` (UUID, foreign key)
- `child_id` (UUID, foreign key)
- `child_name` (TEXT)
- `child_age` (INTEGER)
- `paired_at` (TIMESTAMP)
- `is_active` (BOOLEAN)

### locations table
- `child_id` (UUID, foreign key)
- `latitude` (DOUBLE)
- `longitude` (DOUBLE)
- `accuracy` (FLOAT)
- `battery_level` (INTEGER)
- `address` (TEXT)
- `timestamp` (TIMESTAMP)

### app_usage table
- `child_id` (UUID, foreign key)
- `package_name` (TEXT)
- `app_name` (TEXT)
- `usage_duration` (INTEGER - seconds)
- `category` (TEXT)
- `timestamp` (TIMESTAMP)

### commands table
- `parent_id` (UUID, foreign key)
- `child_id` (UUID, foreign key)
- `command` (TEXT - e.g., 'capture_photo_front')
- `parameters` (JSONB)
- `status` ('pending' or 'executed')
- `created_at` (TIMESTAMP)
- `executed_at` (TIMESTAMP)

### blocked_apps table
- `parent_id` (UUID, foreign key)
- `child_id` (UUID, foreign key)
- `package_name` (TEXT)
- `app_name` (TEXT)
- `blocked_at` (TIMESTAMP)

### geofences table
- `parent_id` (UUID, foreign key)
- `child_id` (UUID, foreign key)
- `name` (TEXT - "Home", "School")
- `latitude` (DOUBLE)
- `longitude` (DOUBLE)
- `radius` (FLOAT - meters)
- `is_active` (BOOLEAN)

### notifications_log table
- `child_id` (UUID, foreign key)
- `app_package` (TEXT)
- `title` (TEXT)
- `text` (TEXT)
- `sent_at` (TIMESTAMP)

### media table
- `child_id` (UUID, foreign key)
- `media_type` ('photo', 'video', 'audio', 'screenshot')
- `storage_path` (TEXT - path in storage bucket)
- `timestamp` (TIMESTAMP)

### alerts table
- `child_id` (UUID, foreign key)
- `alert_type` (TEXT - 'geofence_enter', 'geofence_exit', 'app_blocked')
- `message` (TEXT)
- `is_read` (BOOLEAN)
- `created_at` (TIMESTAMP)

## Testing Connection

After setting up:
1. Build and install app on phone
2. Open app
3. Select "Parent Mode"
4. If no errors in console logs, Supabase is connected!
5. Check Supabase dashboard → **Table Editor** to see data coming in

## Troubleshooting

**"401 Unauthorized" errors:**
- Check API key is correct
- Check URL ends with ".supabase.co"
- Regenerate key in Supabase if needed

**"Network error" / "Connection refused":**
- Check internet connection
- Make sure Supabase project is active (not paused)
- Review firewall/proxy settings

**Missing tables:**
- Rerun SQL schema from `001_initial_schema.sql`
- Check for SQL syntax errors in execution output

**Storage bucket issues:**
- Ensure bucket is created and named "media"
- Check policies allow uploads from your app

## Security Notes

- Never commit`SUPABASE_KEY` to version control
- Use environment variables in CI/CD
- Enable RLS (Row Level Security) policies on all tables
- Enable 2FA on your Supabase account
- Keep service role key secret (server-side only)

## Production Setup

When deploying:
1. Create separate Supabase project for production
2. Enable custom domain (optional)
3. Set up automated backups
4. Enable audit logging
5. Review and enable RLS policies
6. Use read-only replicas for large datasets
