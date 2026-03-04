# Family Parental Control App

This repository contains a single Android application that functions in two modes:
* **Parent Mode** – provides a dashboard for monitoring and controlling child devices.
* **Child Mode** – runs transparently on the child's device with explicit notifications showing that monitoring is active.

> ✅ This variant focuses on *transparent* parental controls. All stealth features have been removed to respect ethics and legality. The app openly informs the user that monitoring is taking place.

## ⚠️  CRITICAL SETUP REQUIRED: Supabase Configuration

**BEFORE BUILDING THE APP:**
1. Read [SUPABASE_SETUP.md](SUPABASE_SETUP.md) - Complete step-by-step instructions
2. Create free Supabase account at [supabase.com](https://supabase.com)
3. Get your API keys and update `SupabaseClient.java`
4. Run the SQL schema to create all tables
5. Create storage bucket for media

**Without this, the app will not communicate with the backend!**

## Features

### Parent Mode
- Master PIN setup
- Pairing child devices using generated IDs
- Dashboard listing children with real-time location
- Remote commands queue (camera capture, video, location tracking, block apps)
- App blocker UI with accessibility instructions
- View app usage statistics
- Location history and geofencing
- Call/SMS logs viewing
- Notification mirroring
- Media gallery access
- Alert management

### Child Mode
- Simple dashboard showing monitoring status (TRANSPARENT - not hidden)
- Displays all active monitoring features
- Starts background services with notifications:
  - `LocationTrackingService` – reports GPS every 30 seconds
  - `AppUsageTrackingService` – collects installed apps and usage stats
  - `NotificationListenerService` – mirrors all notifications
  - `CallSmsService` – reads call and SMS logs
  - `CameraService` – handles remote photo/video capture
  - `AudioService` – records ambient audio
  - `CommandService` – polls Supabase for parent commands
  - `ScreenCaptureService` – takes screenshots on demand

### Services Architecture
- All services run in foreground with notifications
- User can see monitoring is active
- Services communicate via Supabase backend
- Commands are queued and executed
- Data is encrypted in transit (HTTPS)

## Backend: Supabase (Free Tier)

- PostgreSQL database for all data
- REST API via PostgREST
- Storage bucket for media (photos, videos, audio)
- Real-time subscriptions (optional)
- Row Level Security for data protection

See [SUPABASE_SETUP.md](SUPABASE_SETUP.md) for complete database schema and setup instructions.

## Building

```bash
# Ensure gradlew exists
ls gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
Release APK: `app/build/outputs/apk/release/app-release.apk`

## CI/CD: GitHub Actions

Push to GitHub and CI will automatically:
1. Checkout code
2. Setup Java 17
3. Build debug and release APKs
4. Upload artifacts (download from Actions tab)

See `.github/workflows/build.yml` for workflow definition.

## Project Structure

```
contrl/
├── app/
│   ├── src/main/java/com/family/parentalcontrol/
│   │   ├── activities/          # UI screens
│   │   ├── services/            # Background services
│   │   ├── receivers/           # Broadcast receivers
│   │   ├── models/              # Data models
│   │   ├── adapters/            # RecyclerView adapters
│   │   ├── api/                 # Retrofit API interface
│   │   └── utils/               # Helper classes
│   ├── src/main/res/            # Layouts, drawables, values
│   └── AndroidManifest.xml
├── supabase/
│   └── migrations/
│       └── 001_initial_schema.sql
├── .github/
│   └── workflows/
│       └── build.yml
├── gradle/
├── build.gradle
├── settings.gradle
├── gradlew (Linux/Mac)
├── gradlew.bat (Windows)
├── SUPABASE_SETUP.md            # Complete Supabase guide
└── README.md
```

## Technology Stack

- **Language**: Java + Kotlin
- **Framework**: Android 26+ (API 26)
- **Backend**: Supabase (PostgreSQL + REST API)
- **Networking**: Retrofit 2 + OkHttp
- **Maps**: OpenStreetMap (osmdroid)
- **Location**: Google Play Services
- **Database**: Room (local) + Supabase (remote)
- **Permissions**: Runtime permissions with PermissionManager
- **Build**: Gradle 8.1
- **CI/CD**: GitHub Actions

## Permissions Required

All permissions are transparent and shown to the user:

```
Location: ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION
Device: CAMERA, RECORD_AUDIO
Data: READ_CALL_LOG, READ_SMS, READ_CONTACTS, READ_EXTERNAL_STORAGE
System: PACKAGE_USAGE_STATS, BIND_ACCESSIBILITY_SERVICE, FOREGROUND_SERVICE
```

See `AndroidManifest.xml` for complete list.

## Configuration

**File**: `app/src/main/java/com/family/parentalcontrol/utils/SupabaseClient.java`

Replace:
```java
private static final String SUPABASE_URL = "https://YOUR_PROJECT.supabase.co";
private static final String SUPABASE_KEY = "YOUR_ANON_KEY";
```

With your actual Supabase project credentials from `Settings → API`.

## Testing

1. Install APK on two devices (or emulator + device)
2. Open app on both
3. Device 1: Select "Parent Mode", set PIN
4. Device 2: Select "Child Mode", enter device info
5. Parent app should show child in list
6. Tap child → test camera/audio/location commands
7. Check Supabase dashboard for data flow

## Troubleshooting

**"HTTP 401"**: Check Supabase API key
**No data saved**: Verify database tables exist (run SQL schema)
**Location not updating**: Check location permissions granted
**Services stopping**: Enable "Battery optimization" exception for app

See [SUPABASE_SETUP.md](SUPABASE_SETUP.md) troubleshooting section.

## Security Considerations

- PIN is stored in SharedPreferences (should be encrypted in production)
- Use HTTPS for all Supabase communication
- Enable Row Level Security on Supabase tables
- Don't commit API keys to git
- Use environment variables in CI/CD pipelines
- Regularly rotate API keys
- Enable 2FA on Supabase account

## Production Deployment

1. Create separate Supabase project for production
2. Enable RLS policies on all tables
3. Use ProGuard/R8 for code obfuscation
4. Sign APK with release keystore
5. Test thoroughly before releasing
6. Monitor Supabase usage and costs
7. Set up automated backups

## Legal & Ethical

> ⚠️ This app is for transparent family monitoring only. 
> - Inform all users that monitoring is active
> - Get explicit consent before installation
> - Respect privacy laws in your jurisdiction
> - Use responsibly with proper parental guidance
> - Never use for stalking or harassment

## Next Steps & TODOs

- [ ] Implement QR code pairing flow
- [ ] Add geofencing alert logic
- [ ] Create parent dashboard UI with maps
- [ ] Build app scheduler interface
- [ ] Add call/SMS filtering logic
- [ ] Implement media upload to Supabase storage
- [ ] Create analytics and reports PDF export
- [ ] Add biometric authentication for parent access
- [ ] Implement data encryption at rest
- [ ] Add notification keyword filtering
- [ ] Create settings UI for feature configuration
- [ ] Add user guides and tutorials

## Support

For issues:
1. Check [SUPABASE_SETUP.md](SUPABASE_SETUP.md) for Supabase troubleshooting
2. Review Android logcat for errors
3. Check Supabase dashboard for data issues
4. Verify all permissions are granted
5. Ensure app is in device admin list and accessibility settings

## License

NOT FOR PUBLIC DISTRIBUTION - Personal family use only

---

**Last Updated**: March 4, 2026
**Target SDK**: Android 14 (API 34)
**Status**: ✅ Fully functional with transparent monitoring
