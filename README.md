# AlwaysScreen Clock 🕒

AlwaysScreen Clock is a minimalist, open-source Android "Always-On" display app. It transforms your phone into a beautiful analog desk clock while ensuring the screen never sleeps and automatically reopens when you are inactive.

## 🌟 Features

- **Always-On Display:** Prevents the screen from sleeping using `FLAG_KEEP_SCREEN_ON`.
- **Minimalist Analog Clock:** A clean, high-contrast design with smooth animations.
- **Smart Inactivity Reopen:** Automatically brings the clock back to the front after a user-defined idle period (15s, 30s, 1m).
- **Global Touch Detection:** Uses an Accessibility Service to detect user activity across all apps (e.g., while using Telegram or WhatsApp), ensuring the clock only appears when you are truly idle.
- **Slide to Unlock:** A secure, intuitive slider to minimize the app and return to normal phone usage.
- **Immersive Mode:** Completely hides system bars for a distraction-free experience.
- **Boot Support:** Optionally starts automatically when the device boots up.

## 🛠 Permissions Explained

To function as a kiosk-style display, the app requires:
1. **Display over other apps:** Allows the app to reappear automatically.
2. **Accessibility Service:** Required to detect user interaction (taps/scrolls) globally so the clock doesn't interrupt you while using other apps.
3. **Foreground Service:** Keeps the inactivity monitor running reliably in the background.

## 🚀 How to Use

1. **Install** the APK.
2. **Launch** AlwaysScreen Clock.
3. **Grant Permissions:** Follow the on-screen prompts to enable "Display over other apps" and the "AlwaysScreen Touch Detector" in Accessibility settings.
4. **Set Timeout:** Tap the invisible area in the top-right corner to change the inactivity duration.
5. **Relax:** Place your phone on a stand. The clock will appear automatically whenever you stop using the device.

## 📂 Project Structure

- `MainActivity.kt`: Handles the UI, slider logic, and settings.
- `AnalogClockView.kt`: Custom View for the high-performance clock rendering.
- `InactivityService.kt`: Background monitor for idle time management.
- `GlobalTouchService.kt`: Accessibility service for global touch detection.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
