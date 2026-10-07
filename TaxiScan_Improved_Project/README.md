# TaxiScan 2.0.0 — clean rebuild starter

This is a clean Android Studio implementation based on the calculator behavior documented from the TaxiScan 1.7.6 APK. The APK did not contain the original Kotlin/Java source, so this project is a rebuild, not recovered original code.

## Included
- Ukrainian dark dashboard.
- Net earnings after platform commission, fuel, and per-kilometre vehicle wear.
- Separate pickup and passenger-trip distance.
- Saved local cost settings (defaults: 15% commission, 8 L/100 km, 95 UAH/L, 1.50 UAH/km wear).
- Local trip history and today’s net earnings and distance.
- Input validation; no network, location, overlay, or account permissions.

## Open and build
Open this folder in Android Studio. Use JDK 17, Gradle 8.13, Android Gradle Plugin 8.13.2, and Android SDK Platform 36.

The project keeps the package ID from the recovered APK (`com.example.myno.activity`) and raises versionCode to 32. To update an existing installation or publish an update, signing must use the original authorized upload key. This workspace does not have that key.

The current environment has Java 17 but no Android SDK installed, so APK compilation and device testing could not be run here.
