# LANDAS Developer Guide

This guide explains the internals of the LANDAS prototype and the safest places to extend it.

## 1. Prerequisites

- Android Studio Ladybug or newer
- JDK 17+ (JDK 21 is tested)
- Android SDK Platform 35
- Windows: `gradlew.bat`, macOS/Linux: `./gradlew`

No backend, API keys, or Firebase project are required.

## 2. Build commands

```bat
gradlew.bat :app:compileDebugKotlin
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## 3. Package responsibilities

| Package | Responsibility |
| --- | --- |
| `ph.edu.wvsu.landas` | Application container, activity, root navigation |
| `.data` | Room entities, DAOs, database, repository, seed data, session store |
| `.routing` | Pure Kotlin Dijkstra engine and navigation step generation |
| `.ui.screens` | Compose screens for users, visitors, documentation, and administrators |
| `.ui.components` | Shared Compose primitives |
| `.ui.map` | Canvas campus map |
| `.ui.theme` | Color palette and Material theme |
| `.ui.viewmodel` | Central `LandasViewModel` |
| `.util` | Location, network, notifications, QR, backup, contacts, formatters |

## 4. Data model

### Core entities

| Entity | Key fields | Notes |
| --- | --- | --- |
| `UserEntity` | `id`, `schoolId`, `role`, `passwordHash`, `isActive` | Roles: `STUDENT`, `FACULTY`, `ADMIN`, `VISITOR` |
| `AttendanceRecord` | `userId`, `timestamp`, `evacuationArea`, `method`, `synced` | Method is `ONE_TAP` or `MANUAL` |
| `DistressSignal` | `userId`, coordinates, `nearestLocation`, `status`, `synced` | Status: `ACTIVE`, `ACKNOWLEDGED`, `RESOLVED` |
| `AlertEntity` | `type`, `severity`, `isActive`, `isRead` | Only one alert should be active at a time |
| `AnnouncementEntity` | `title`, `body`, `category` | General notices |
| `CampusNodeEntity` | position, `kind`, `isEvacuationArea`, `capacity`, `occupancy`, `hazardBlocked` | Graph vertices |
| `CampusEdgeEntity` | `fromNodeId`, `toNodeId`, `distanceMeters`, `hazardBlocked`, `accessible` | Undirected graph edges |
| `ClassroomEntity` | building, capacity, `statusOverride` | Overrides: `AVAILABLE`, `OCCUPIED`, or `null` |
| `ScheduleSlotEntity` | `roomId`, `dayOfWeek`, minutes, subject, instructor, section | `dayOfWeek` uses ISO 1–7 (Monday–Sunday) |

### Session

`SessionStore` persists a `StoredSession` through DataStore. A visitor session has `isVisitor = true` and no linked user. On boot, the view model re-validates non-visitor sessions against the database and signs out deactivated accounts.

## 5. Adding campus data

### Option A: through the administrator UI

1. Sign in as an administrator.
2. Open **Routes & hazards** → **Locations**.
3. Tap empty map ground or **Add location**.
4. Enter name, short label, type, coordinates (world units `1000 × 760`), optional capacity, and description.
5. Open **Paths** → **Add path** and connect locations. Distance is estimated from map coordinates.
6. Block hazards with the switches. The student map updates immediately.

### Option B: through seed data

Edit `SeedData.seedCampusNetwork()` / `seedClassrooms()` / `seedSchedules()`.

Important: seeding only runs when the relevant table is empty. After changing seed data, clear app storage (or uninstall the app) so the new seed is applied.

### Coordinate system

- World: `1000 × 760` logical units.
- `CampusMap` scales the world to the available canvas and supports pinch zoom/pan.
- `SeedData.distance` converts map distance to metres with a `0.35×` factor, with a 4 m minimum.
- Manual pins map back to approximate latitude/longitude using `CampusLocation` constants.

## 6. Routing

`DijkstraRouter` is pure Kotlin and has no Android dependencies.

```kotlin
val outcome = DijkstraRouter.routeToNearestEvacuation(
    nodes = nodes,
    edges = edges,
    startNodeId = "n_gate",
    avoidHazards = true,
    respectCapacity = true
)
```

Behavior:

- Blocked nodes and edges are removed while building the graph.
- Blocked areas are excluded as destinations.
- Missing start/destination returns `RouteOutcome.Failure` with a user-facing reason.
- If every open area is at capacity, the nearest open area is returned and `capacityFallback = true` appears in the result notes.
- `NavigationStep`s include direction and distance; the map screen renders the path overlay.

To add a new routing strategy (for example, accessible-only or smoke-aware routes), add a new function to the router and keep the graph-building rules explicit.

## 7. Authentication

`LandasRepository.authenticate()`:

1. Looks up the school ID.
2. Rejects inactive accounts.
3. Validates the salted hash via `PasswordHasher`.
4. Requires the administrator portal toggle for `ADMIN` accounts.

`PasswordHasher` is a demonstration implementation (salted SHA-256). Do not ship it. Replace it with a server-side KDF and token-based sessions.

## 8. View model and screen wiring

`LandasViewModel` exposes:

- Session and current user
- Login, mark-safe, SOS, and sync state
- Room-backed `StateFlow`s for users, alerts, announcements, nodes, edges, classrooms, schedules, attendance, and distress signals
- Actions for all regular and administrator mutations
- A `messages` shared flow for snackbars

Add a new screen by:

1. Creating a composable in `ui/screens`.
2. Adding a `Screen` enum value and a branch in `MainActivity.LandasNavigation`.
3. Passing repository state and an action lambda from the view model.
4. Keeping the screen stateless where practical; local UI state belongs in `remember`.

Saveable screen state is provided by `rememberSaveableStateHolder`, so `LazyColumn` scroll positions survive back-stack navigation.

## 9. Backup and restore

`BackupManager` serializes `SnapshotData` to JSON and restores it through `LandasRepository.restoreSnapshot()`.

Included tables:

- Users, attendance, distress signals, alerts, announcements
- Campus nodes, campus edges
- Classrooms, schedule slots

Restore upserts rows and does not delete rows missing from the backup. If you need a full destructive restore, add a repository transaction that clears tables first.

## 10. Notifications

`NotificationHelper` creates two channels:

- `landas_emergency_alerts` — high importance, for emergency alerts and drills
- `landas_general` — default importance, for announcements

Alerts published from the admin console trigger a local notification on the same device. Android 13+ requires `POST_NOTIFICATIONS`, requested by `MainActivity`.

`DrillScheduler` optionally queues one WorkManager job (`DrillWorker`) that publishes a drill alert and posts a notification at the chosen time. It stores the queued timestamp in SharedPreferences so the administrator screen can display and cancel it. WorkManager execution is inexact and depends on battery optimization; it is not a real-time dispatch mechanism.

## 11. Testing

Unit tests:

```bat
gradlew.bat :app:testDebugUnitTest
```

The router tests live in `app/src/test/java/ph/edu/wvsu/landas/routing/DijkstraRouterTest.kt`.

Manual test scenarios are in `docs/TEST_PLAN.md`.

## 12. Production hardening checklist

- [ ] Replace demo credentials and seeded personal data.
- [ ] Add server-side authentication, token refresh, and account provisioning.
- [ ] Encrypt the Room database and exported backups.
- [ ] Add authenticated transport for alerts, attendance, and SOS.
- [ ] Integrate FCM or another push provider for real-time alerts.
- [ ] Replace prototype coordinates with surveyed campus data.
- [ ] Add an audit log for administrator actions.
- [ ] Add UI/instrumentation tests for critical flows.
- [ ] Add accessibility review (TalkBack, font scaling, contrast).
- [ ] Implement scheduled drills and delivery receipts.
- [ ] Add a privacy impact assessment and retention policy.
