# LANDAS Test Plan

Use this matrix to evaluate the prototype on an emulator or physical Android device.

## 0. Build and install

- [ ] `gradlew.bat :app:testDebugUnitTest` passes.
- [ ] `gradlew.bat :app:assembleDebug` produces `app/build/outputs/apk/debug/app-debug.apk`.
- [ ] Installing on a clean device shows the splash screen, then the sign-in screen.
- [ ] Android 13+ shows the notification permission prompt.

## 1. Visitor flow

- [ ] Tap **Continue as visitor**.
- [ ] Home shows campus visitor identity and no crash.
- [ ] Evacuation map opens, supports pan/zoom, and computes a route.
- [ ] Campus guide lists buildings and assembly areas; search works.
- [ ] Emergency hotline rows open the dialer / copy numbers.
- [ ] QR screen generates an identity QR (visitor mode) and opens the scanner.
- [ ] Tapping **Marked Safe** or **Distress Alarm** shows the sign-in prompt instead of failing.
- [ ] Profile → **Exit visitor mode** returns to sign-in.

## 2. Authentication

- [ ] Student `2023-00142` / `landas123` signs in.
- [ ] Wrong password shows an inline error.
- [ ] Administrator credentials without the portal toggle are rejected.
- [ ] Administrator credentials with the portal toggle sign in and open CDRRMO Control.
- [ ] Deactivated user cannot sign in.
- [ ] Session survives app restart.
- [ ] Sign-out clears the session and returns to sign-in.

## 3. Evacuation routing

- [ ] From the main gate, a route reaches an open assembly area.
- [ ] Turn-by-turn steps show a start, intermediate turns, and an arrival instruction.
- [ ] Distance and estimated minutes are plausible.
- [ ] Select a different destination and confirm the path changes.
- [ ] Select the same location as start and destination; a friendly error appears.
- [ ] As administrator, block the nearest assembly area; student routing selects the other area.
- [ ] Block every area; routing returns “No open evacuation area …”.
- [ ] Block one path; routing detours or reports no safe path.
- [ ] Set the nearest area to full occupancy; routing chooses the next area.
- [ ] Set all open areas full; routing still recommends the nearest with a capacity note.

## 4. Mark safe and attendance

- [ ] Student marks safe at an area; confirmation shows area and time.
- [ ] Existing check-in is detected and displayed.
- [ ] Offline (airplane mode) check-in shows **Queued**.
- [ ] Administrator sees the record in Attendance.
- [ ] Manual attendance adds an unmarked user.
- [ ] Occupancy increases on check-in and manual entry.
- [ ] **Clear today’s attendance** removes only today’s rows and restores occupancy.
- [ ] Previous-day historical rows remain after clearing today.
- [ ] Accountability totals in dashboard/analytics exclude visitors and duplicates.

## 5. SOS

- [ ] Open SOS and grant or deny location permission.
- [ ] GPS acquired or campus center fallback is shown clearly.
- [ ] Manual pin dialog updates nearest landmark and coordinates.
- [ ] Send with a message; active-signal screen appears with identity and time.
- [ ] Check-in queue state shows Queued/Synced correctly.
- [ ] Student cancels their own signal; status becomes resolved.
- [ ] Administrator sees the signal, acknowledges it, and the student sees acknowledged status.
- [ ] Administrator resolves it; it leaves the open list.
- [ ] Dial 911 action opens the dialer.

## 6. Classroom availability

- [ ] Room status matches the current timetable.
- [ ] A class in session shows Occupied.
- [ ] A class ending within 15 minutes shows Transition.
- [ ] Override **Force open** changes status to Available and marks the override.
- [ ] Override **Force occupied** changes status to Occupied.
- [ ] Clear to **Auto** restores computed status.
- [ ] Search and building filters return expected rooms.

## 7. Alerts

- [ ] Visitor/student alert screen lists seeded alerts.
- [ ] Active emergency banner appears on the dashboard.
- [ ] Publish a Critical earthquake alert; banner and local notification appear.
- [ ] Publishing a second alert deactivates the first.
- [ ] Publish an Info announcement; it appears in the announcement list.
- [ ] Send all-clear; active banner disappears and history records `ALL_CLEAR`.
- [ ] Schedule a drill for one minute ahead; a queued time appears and can be cancelled.
- [ ] Let a scheduled drill fire; a notification and active drill alert appear.
- [ ] Deny notification permission and confirm the drill alert still publishes without crashing.
- [ ] Mark-all-read clears the unread badge.

## 8. QR

- [ ] Identity QR contains the correct school ID, name, and role.
- [ ] QR screen shows official WVSU links.
- [ ] Scanner launches, requests camera permission, and parses a LANDAS QR.
- [ ] Cancelling the scanner returns to the QR screen without crashing.

## 9. Administrator console

- [ ] Dashboard metrics match attendance, SOS, and route data.
- [ ] Module cards open every admin screen.
- [ ] Route editor add/edit/delete updates the graph.
- [ ] Adding a path updates the edge list and routing.
- [ ] Blocking a node and edge updates both admin and student maps.
- [ ] Class-schedule add/delete updates student availability.
- [ ] User activation/deactivation works; self-deactivation is prevented.
- [ ] Password reset message shows the correct default.
- [ ] Analytics charts render and totals add up.
- [ ] Data health checks reflect the current database.

## 10. Backup and restore

- [ ] Export creates a JSON file through the system file picker.
- [ ] Exported JSON contains users, attendance, distress, alerts, announcements, nodes, edges, classrooms, and schedules.
- [ ] Restore asks for confirmation and reports a summary.
- [ ] Restoring after adding a room/class/user brings back the backup state.
- [ ] Restoring invalid JSON shows an error and does not crash.

## 11. Offline behavior

- [ ] Enable airplane mode; map, routing, classrooms, guides, hotlines, QR, and admin screens still work.
- [ ] New attendance and SOS records show Queued.
- [ ] Returning online triggers automatic sync (or manual sync) and clears queued badges.
- [ ] Online/Offline pills update after connectivity changes.

## 12. UI and resilience

- [ ] Rotate is locked to portrait; layout remains usable.
- [ ] Small phone (360 dp wide) has no clipped primary actions.
- [ ] Long names and building labels ellipsize instead of overlapping.
- [ ] Back button returns through the screen stack; home back exits.
- [ ] Snackbar messages appear above the navigation bar.
- [ ] No crash when data lists are empty (fresh database, deletes).
- [ ] Session timeout not applicable; app resumes to the same screen state.

## 13. Regression after schema or seed changes

- [ ] Uninstall/reinstall to re-seed.
- [ ] Verify demo accounts and classroom slots exist.
- [ ] Verify at least two evacuation areas exist and are routable.
- [ ] Run router unit tests.
