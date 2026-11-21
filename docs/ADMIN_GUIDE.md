# LANDAS Administrator Guide

This guide is for the CDRRMO coordinator or campus safety administrator using the LANDAS prototype.

> The prototype stores everything on one device. Student phones do not receive data from the administrator console. Use it to evaluate workflows, not to run a real emergency.

## 1. Signing in

1. Open LANDAS.
2. Enable **Administrator portal** on the sign-in screen.
3. Sign in with `CDRRMO-ADMIN` / `admin123`.
4. The CDRRMO Control dashboard opens. Press back once to reach the standard campus dashboard.

## 2. Reading the dashboard

The dashboard shows:

- People marked safe vs. active students and faculty
- Active SOS count
- Blocked route locations and paths
- Assembly-area capacity bars
- Today’s recent activity
- Offline sync queue

An active emergency alert appears as a red banner; tap it to open Alerts & notices.

## 3. Routes & hazards

### Edit a location

1. Tap a building on the map, or use the location list.
2. Change the name, short label, type, coordinates, size, capacity, or description.
3. Save. Student maps update immediately.

### Add a location

- Tap empty ground on the map, or tap **Add location**.
- Coordinates are in map-world units (1000 × 760).

### Block a hazard

- Toggle **Blocked from routing** on a location, or **Blocked** on a path.
- Blocked locations are excluded as destinations and intermediates.
- Blocked paths are excluded only as connections.
- Red X markers appear on the map.

### Manage assembly areas

- Enable **Evacuation assembly area** and set capacity.
- Adjust occupancy with the − and + controls.
- A blocked area is excluded from route searches. If every open area is full, the router still returns the nearest area with a capacity warning.

### Add a walking path

1. Open **Paths** → **Add path**.
2. Choose the start and end locations.
3. Confirm. Distance is estimated from map coordinates.

Deleting a location also deletes every path connected to it.

## 4. Alerts & notices

### Publish an emergency alert

1. Choose a template: Earthquake, Fire, Typhoon, or Drill.
2. Set severity: Critical, Warning, or Info.
3. Edit the headline and instructions.
4. Review and publish.

Publishing deactivates the previous alert and shows the new alert across dashboards. A local notification fires on this device.

### Send all-clear

With an active alert, tap **Stand down · send all-clear**. This deactivates the banner and records an all-clear notice in alert history.

### Publish an announcement

Open **General announcements** → **New**, enter a title and message, choose a category, and publish.

### Schedule a drill

1. Open **Alerts & notices** → **Scheduled drill**.
2. Choose **In 1 min**, **In 5 min**, **In 15 min**, or **Tomorrow 9 AM**.
3. Tap **Schedule drill**. The queued time appears on the card and can be cancelled.

When the drill fires, LANDAS publishes an active drill alert, adds it to alert history, and posts a high-priority notification. WorkManager timing is inexact: battery optimization can delay delivery by several minutes. The drill only affects this device.

## 5. Attendance & accountability

- View today’s check-ins, historical totals, and queued offline records.
- Search by name, school ID, or assembly area.
- Filter by students, faculty, or queued records.
- **Add** manually records a person at an assembly area. People already marked today are disabled in the picker.
- **Clear today’s attendance** removes only today’s records and releases their occupancy. Historical days remain.

## 6. Distress response

- **Active** signals show the sender, message, map pin, coordinates, nearest landmark, nearest assembly area, and time.
- **Acknowledge** tells the sender a responder has seen the request.
- **Resolve** closes the request after responders reach the pin.
- The signal map plots active, acknowledged, and resolved pins.

Because there is no backend, signals only exist on this device. In production, signals would arrive from student devices through authenticated transport.

## 7. Class schedules

- Each room displays its computed status: Available, Transition, or Occupied.
- **Auto** uses the weekly timetable.
- **Force open** and **Force occupied** override the timetable until cleared.
- **Add class** creates a slot with day, start/end time, subject, instructor, and section.
- Deleting a slot immediately changes computed availability.

Status rules:

- Occupied: a class is in session.
- Transition: a class ended within 15 minutes, or the next class starts within 15 minutes.
- Available: no class in session and no transition window.

## 8. Users & access

- Search and filter by role or inactive status.
- Toggle an account to deactivate or reactivate it. Deactivated accounts cannot sign in; history remains.
- You cannot deactivate your own account.
- **Reset password** restores the demo default: `landas123` for students/faculty, `admin123` for administrators.

## 9. Analytics

- **Readiness score** combines route health, today’s accountability, assembly-area headroom, sync health, and open SOS load.
- **Hourly check-ins** shows the current day’s distribution.
- **Seven-day trend** shows historical attendance.
- **Accountability by role** counts each person once.
- **Assembly area utilization** shows occupancy against capacity.
- **SOS response** shows active/acknowledged/resolved counts and resolution rate.
- **Data coverage** and **readiness checks** identify missing or inconsistent data.

The score is a prototype indicator and not an official certification.

## 10. Backup & data

### Export

1. Open **Backup & data** → **Export backup**.
2. Choose a file location.
3. Store it securely; it contains personal and location data.

### Restore

1. Tap **Restore backup** and pick a JSON file.
2. Confirm.
3. Rows are upserted by ID. A backup can reintroduce old attendance or alerts.

### Sync queue

- Queued attendance and SOS records are marked as delivered by the local sync routine.
- No network call is made. The queue demonstrates future backend behavior.

## 11. Prototype credentials

| Role | ID | Password |
| --- | --- | --- |
| Student | `2023-00142` | `landas123` |
| Faculty | `F-2019-008` | `landas123` |
| Administrator | `CDRRMO-ADMIN` | `admin123` |

## 12. Operational limitations

- Single-device data only; no multi-device synchronization.
- No live push alerts to other phones.
- No real dispatch integration.
- Demo map geometry, schedules, and hotlines require validation.
- Local notifications need Android 13+ notification permission.
- In a real emergency, always follow official responders and campus protocols.
