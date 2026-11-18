# M Notes — Android (Kotlin + Jetpack Compose)

Native Android rewrite of **M Notes** (`MOAMENMEKKIN0/M-Notes-App`) built with Kotlin, Jetpack Compose, Material Design 3, Firebase Authentication (Google Sign-In via Credential Manager), and Cloud Firestore.

## Implemented Features
- **Bilingual RTL/LTR Experience**: Instant switching between Arabic (default RTL) and English (LTR) using bundled `Cairo`, `Tajawal`, `IBM Plex Sans Arabic`, and `Almarai` font weights.
- **Two-Page Swipeable Workspace**: `Tasks & Schedule` and `Requests` pages paired with 5 customizable Top Bar Switcher styles (`T1`, `T11`, `T13`, `T18`, `T20`).
- **Customizable Hero Banner**: 7 banner layouts (`B1`, `B2`, `B3`, `B4`, `B5`, `B7`, `B8`), editable title/subtitle, small icon badge, custom photo, 3 image sizes, and 4 photo frame shapes.
- **Smart Tasks & Recurring Scheduling**: Live search filtering, `NONE` / `DAILY` / `WEEKLY` / `CYCLE` rotation steps, weekday selection, due-time alert scheduling, "Simulate Tomorrow" testing for future tasks, and 5 card/photo shapes (`ring`, `capsule`, `ios`, `blob`, `strip`).
- **Due-Time Alert Overlays**: 6 alert presentations (`A1`–`A6`) with green completion transition, snooze dismiss, and looping ringtone audio.
- **Audio Engine & Quick Ringtone Cutter**: UI sound synthesis (`AudioTrack`), haptic vibration, 8 procedural Gym Notification Tracks (`G1`–`G8`), and device audio picking/trimming to WAV (`MediaExtractor` + `MediaCodec`).
- **Interactive Photo Cropper & Library**: Zero-permission system Photo Picker (`PickVisualMedia`), pan/zoom/90°-rotate cropper, and local photo library management.
- **Cloud Sync & JSON Backup**: Per-user Cloud Firestore sync (`users/{uid}/tasks`, `requests`, preferences, and `backups/latest`), plus manual backup, restore, and Storage Access Framework JSON export/import.
- **7 Curated Themes**: `Mint Clean`, `Sky Blue`, `Ocean Teal`, `Classic Mono`, `Arctic Ice`, `Sage Leaf`, and `Nordic Gray`.