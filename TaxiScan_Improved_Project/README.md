# TaxiScan 2.1.0

A clean Android Studio rebuild based on the calculator behavior documented from the TaxiScan 1.7.6 APK. The original APK did not contain the original Kotlin/Java source, so this project is a rebuild rather than recovered source.

## Features

- Ukrainian dark dashboard and local trip calculator.
- Net earnings after platform commission, fuel, and per-kilometre vehicle wear.
- Separate pickup and passenger-trip distance.
- Saved local cost settings (defaults: 15% commission, 8 L/100 km, 95 UAH/L, 1.50 UAH/km wear).
- Local trip history and today’s net earnings and distance.
- A connection screen for Android permissions, following the supplied setup-screen design.
- Optional offer parsing for Bolt and Uklon: visible text is read only from those apps, then the fare and kilometre total are shown in a non-interactive overlay.
- Optional notification parsing, also limited to Bolt and Uklon.

## Privacy and limits

Offer text is processed on the device and is not stored or sent over the network. Accessibility reads window content only after the user enables the service in Android settings. The service is scoped to Bolt and Uklon and does not perform taps, gestures, or order acceptance. Location permission is optional and this version does not read or save coordinates. The overlay cannot receive taps and never blocks controls in the driver app.

The parser uses currency and kilometre labels visible in app text. App layouts and wording can vary by version and language, so the displayed total should be checked against the driver app.

## Open and build

Open this folder in Android Studio. Use JDK 17, Gradle 8.13, Android Gradle Plugin 8.13.2, and Android SDK Platform 36.

The application ID is retained from the recovered APK (`com.example.myno.activity`). Version code is 33 and version name is 2.1.0. Updating an existing installation or publishing requires the original authorized upload key, which is not included here. The provided APK is debug-signed for testing and is not a Play Store release build.

To connect the apps, open TaxiScan → **Підключити Bolt / Uklon**, then enable Accessibility and overlay access in Android settings. Notification access is optional. Battery and autostart controls differ by device maker and may need manual adjustment.
