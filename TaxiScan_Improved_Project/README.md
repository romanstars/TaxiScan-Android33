# TaxiAnalytic 2.2.1

A clean Android Studio rebuild based on the calculator behavior documented from the TaxiScan 1.7.6 APK. The original APK did not contain the original Kotlin/Java source, so this project is a rebuild rather than recovered source.

## Design refresh and rebrand

The dashboard and permissions hub use a graphite background with green and yellow accents, rounded cards, and clearer visual grouping. This refresh changes presentation only: trip calculations, settings, permission actions, local history, and offer-scanning behavior keep their existing handlers and data.

## Features

- Ukrainian dark dashboard and local trip calculator.
- Net earnings after platform commission, fuel, and per-kilometre vehicle wear.
- Editable vehicle costs, commission, pickup and passenger-trip distance.
- Local trip history and today’s net earnings and distance.
- Connection and permissions screen for Bolt and Uklon.
- Local offer filter for minimum fare, fare per kilometre, and maximum distance; the overlay marks offers to review but never accepts or rejects them.
- Optional sound for newly detected offers, configurable overlay placement, size and transparency.
- Trip timers, local event log, JSON settings backup/restore, app launch shortcut, and trip-time estimate using a manual traffic buffer.
- Optional notification parsing limited to Bolt and Uklon.

## Privacy and limits

Offer text is processed on the device and is not saved in logs or sent over the network. Accessibility reads window content only after the user enables the service in Android settings. The service is scoped to Bolt and Uklon and does not perform taps, gestures, or order acceptance. Location permission is optional and this version does not read or save coordinates. The overlay cannot receive taps and never blocks controls in the driver app.

The offer parser uses fare and kilometre labels it can find in visible app text. The filter is a local hint, not an automatic action. Bolt/Uklon layouts and wording can vary by app version and language, so verify the detected values in the driver app. The trip-time estimate is local math; no live traffic API is configured. Auto-click scenarios are disabled.

## Open and build

Open this folder in Android Studio. Use JDK 17, Gradle 8.13, Android Gradle Plugin 8.13.2, and Android SDK Platform 36.

The new-install package ID is `com.romanstars.taxianalytic`. It installs as a separate app beside older TaxiScan builds; Android keeps the old app and its local data, but this new app does not import that data. Version code is 35 and version name is 2.2.1. The provided APK is debug-signed for testing and is not a Play Store release build.

To connect the apps, open TaxiAnalytic → **Підключення платформ**, then enable Accessibility and overlay access in Android settings. Notification access is optional. Battery and autostart controls differ by device maker and may need manual adjustment.
