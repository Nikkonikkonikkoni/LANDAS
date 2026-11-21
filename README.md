# LANDAS

**Location-Aware Navigation and Disaster Assistance System**  
WVSU – Pototan Campus · Iloilo, Philippines

LANDAS is a standalone native Android prototype for campus disaster preparedness. It combines an offline campus map, Dijkstra evacuation routing, emergency attendance, SOS signaling with location pinning, classroom availability, visitor tools, QR identity, alert broadcasting, and a full CDRRMO administrator console.

> Prototype notice: campus coordinates, building geometry, schedules, accounts, and hotline listings are demonstration data. They must be validated before operational use. In a real emergency, follow official responders and campus protocols.

---

## Highlights

| Module | What it does |
| --- | --- |
| Offline campus map | Custom Jetpack Compose Canvas map with buildings, walkways, assembly areas, hazards, occupancy, zoom, and pan. No network required. |
| Evacuation routing | Embedded Dijkstra router finds the nearest open assembly area, avoids hazards, respects capacity, and produces turn-by-turn guidance. |
| Attendance & accountability | One-tap “Mark safe,” manual administrator entry, offline queue badges, occupancy updates, and role-based accountability totals. |
| SOS distress signals | GPS or manual pin, identity attachment, active-signal screen, responder acknowledgement, and resolution. |
| Classroom availability | Live Available / Transition / Occupied status derived from weekly schedules plus administrator overrides. |
| Visitor tools | No-account access to map, campus guide, hotlines, QR tools, and documentation. |
| QR toolkit | On-device identity QR, official WVSU links, and an embedded ZXing scanner. |
| Alerts & announcements | Emergency templates, critical/warning/info severity, all-clear notices, local notifications, and announcement feed. |
| Scheduled drills | WorkManager publishes a queued earthquake drill and posts a notification at the chosen time. |
| Preparedness documentation | Earthquake, fire, typhoon/flooding guides plus system design, privacy, and limitation notes. |
| Profile & backup | Session details, sync queue, JSON export/restore, and sign-out. |
| Administrator console | Routes, hazards, capacities, alerts, attendance, distress response, class schedules, users, analytics, and data management. |
| Analytics | Readiness score, hourly and seven-day attendance charts, role accountability, area utilization, SOS metrics, and data health checks. |

---

## Roles

| Capability | Visitor | Student | Faculty | Administrator |
| --- | :---: | :---: | :---: | :---: |
| Campus map and evacuation routing | Yes | Yes | Yes | Yes |
| Campus guide, hotlines, documentation | Yes | Yes | Yes | Yes |
| QR identity and scanner | Yes | Yes | Yes | Yes |
| Classroom availability | Yes | Yes | Yes | Yes |
| Mark safe / attendance | — | Yes | Yes | Console view |
| SOS signal | — | Yes | Yes | Console response |
| Alerts and announcements | Yes | Yes | Yes | Publish / all-clear |
| Routes, hazards, capacities | — | — | — | Yes |
| Class schedules and room overrides | — | — | — | Yes |
| Users and passwords | — | — | — | Yes |
| Analytics and backup | — | — | — | Yes |

Visitors can browse the app without an account. Actions that require identity show a sign-in prompt instead of failing silently.

---

## Demo accounts

| Role | School ID | Password | Notes |
| --- | --- | --- | --- |
| Student | `2023-00142` | `landas123` | Ana Marie Delos Reyes |
| Faculty | `F-2019-008` | `landas123` | Prof. Jovelyn Santillan |
| Administrator | `CDRRMO-ADMIN` | `admin123` | Enable the **Administrator portal** toggle before signing in |

All other seeded students and faculty also use `landas123`.

---

## Build and run

### Requirements

- Android Studio Ladybug or newer (or command-line Android SDK)
- JDK 17 or newer (JDK 21 works)
- Android SDK Platform 35 and Build-Tools 34+
- Windows, macOS, or Linux

### Commands

```bat
gradlew.bat :app:assembleDebug
```

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install it on a device or emulator:

```bat
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Tests

```bat
gradlew.bat :app:testDebugUnitTest
```

The unit tests cover the pure Kotlin Dijkstra router: nearest evacuation selection, hazard avoidance, blocked destinations, capacity fallback, and route reconstruction.

### Continuous integration

`.github/workflows/android.yml` runs the unit tests and assembles a debug APK on every push to `main` and on pull requests. The APK is uploaded as the `landas-debug-apk` workflow artifact. No repository secrets are required.

The workflow targets Ubuntu runners with the preinstalled Android SDK and JDK 21.

### Configuration

- Application ID: `ph.edu.wvsu.landas`
- Minimum SDK: 26 (Android 8.0)
- Target / compile SDK: 35
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Persistence: Room + DataStore

The app does not require a backend, API key, Firebase project, or Google Maps key.

---

## Architecture

```
app/src/main/java/ph/edu/wvsu/landas/
├── LandasApplication.kt          Application container (database, repository, helpers)
├── MainActivity.kt               Root Compose navigation, snackbar host, notification permission
├── data/
│   ├── Entities.kt               Room entities, roles, status constants
│   ├── Daos.kt                   Room DAOs and queries
│   ├── LandasDatabase.kt         Database singleton
│   ├── LandasRepository.kt       Data API, authentication, alerts, backup, admin mutations
│   ├── SeedData.kt               WVSU-PC demo accounts, campus graph, rooms, schedules, activity
│   └── SessionStore.kt           DataStore session persistence
├── routing/
│   └── DijkstraRouter.kt         Pure Kotlin routing engine and turn-by-turn steps
├── ui/
│   ├── components/               Shared cards, buttons, pills, avatars, scaffold
│   ├── map/CampusMap.kt          Interactive Canvas map
│   ├── screens/                  All user, visitor, documentation, and admin screens
│   ├── theme/                    Color palette and Material 3 theme
│   └── viewmodel/LandasViewModel.kt
└── util/
    ├── BackupManager.kt          JSON export / restore
    ├── ClassroomAvailability.kt  Schedule-based room status
    ├── DrillScheduler.kt         WorkManager-backed scheduled drill delivery
    ├── EmergencyContacts.kt      Hotline directory
    ├── Formatters.kt             Date, time, relative time, initials
    ├── LocationHelper.kt         Fused Location Provider wrapper
    ├── NetworkMonitor.kt         Connectivity state
    ├── NotificationHelper.kt     Local emergency/general notifications
    └── QrUtils.kt                QR generation and payload parsing
```

### State flow

1. `LandasViewModel` boots the repository, seeds the database on first run, and restores the DataStore session.
2. Room `Flow`s become Compose state via `collectAsStateWithLifecycle`.
3. Screens call view-model actions; the view model updates the repository and emits snackbar messages.
4. `MainActivity` owns a lightweight screen stack with saveable state per screen. Administrators land in the CDRRMO console and can return to the campus dashboard.
5. Session sign-out returns to the login screen; a visitor session can exit visitor mode from Profile.

### Routing model

- The campus is an undirected weighted graph of `CampusNodeEntity` and `CampusEdgeEntity`.
- Edge weight is walking distance in metres.
- Before traversal, hazard-blocked nodes and edges are removed.
- Dijkstra computes shortest distances from the start node.
- The nearest open assembly area is selected; if all open areas are at capacity, the nearest area is still returned with a capacity-fallback note.
- Turn-by-turn steps are generated from the node sequence, segment bearings, and turn deltas.

### Offline-first behavior

Everything is stored locally. Attendance and SOS records carry a `synced` flag and appear as **Queued** when created offline. The sync routine marks queued rows as delivered locally. **There is no backend server in this prototype**; the queue exists to demonstrate offline-first behaviour and a future integration point.

---

## Emergency data disclaimer

- Campus building positions, sizes, walkways, capacity values, and occupancy numbers are fabricated prototype data.
- Hotlines are based on public listings and are marked for field re-verification with WVSU-PC, CDRRMO/MDRRMO Pototan, PNP, fire, and health offices.
- GPS routing uses the device location projected onto the prototype map; it is not a survey-grade campus coordinate system.
- Local notifications require Android 13+ notification permission.
- Backup files contain personal data. Store them securely.

---

## Suggested evaluation walkthrough

1. Launch the app and continue as a visitor. Explore the map, campus guide, hotline screen, and QR tools.
2. Sign out, then sign in as the student. Mark yourself safe, send an SOS with a manual pin, and check the classroom screen.
3. Sign out and sign in as the administrator (enable the portal toggle). Open **Distress response**, acknowledge and resolve the student’s signal, then check **Attendance** and **Analytics**.
4. In **Routes & hazards**, block a walkway and confirm the student map chooses a different route. Adjust an assembly area’s occupancy.
5. Publish a drill alert, inspect the student alert banner, then send an all-clear.
6. Add a class slot in **Class schedules**, set a manual room override, and verify the status on the student classroom screen.
7. In **Alerts & notices**, schedule a drill for one minute ahead and confirm the notification and active drill banner appear.
8. Export a backup from **Backup & data**, then restore it and confirm the data remains consistent.

See [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md) for the full scenario matrix.

---

## Limitations and next steps

- No live backend, push messaging, or dispatch integration. Notification delivery is local to the device.
- Passwords use a salted SHA-256 demo hash, not a production password KDF. Use Argon2/bcrypt and server-side authentication in production.
- No audit log, role delegation, or multi-campus tenancy yet.
- No automated UI tests; the routing engine has unit tests.
- Scheduled drills use WorkManager on a single device, so delivery is inexact and does not reach other phones.
- Map geometry is hand-authored and should be replaced with surveyed coordinates or a GeoJSON import.
- A production deployment needs encrypted transport, encryption at rest, consent management, and a data-retention policy.

---

## License and attribution

Academic prototype for WVSU – Pototan Campus. Third-party libraries retain their respective licenses: AndroidX, Jetpack Compose, Room, ZXing, and Google Play Services Location.
