# Aura Finance — 100% Offline Android App (Privacy-First SMS Reader)

A native Android application with an embedded modern finance dashboard that **automatically reads bank and payment SMS alerts locally on the device**, requiring **zero internet connection**.

---

## ✦ 100% Offline Guarantee

In [`app/src/main/AndroidManifest.xml`](file:///a:/AI_Project_work/money_tracking/android/app/src/main/AndroidManifest.xml):
- `android.permission.INTERNET` is **completely omitted**.
- The Android operating system physically prevents the app from making any external network or cloud requests.
- All transaction parsing runs locally on your phone's processor using deterministic regex logic.
- Your personal banking data never leaves your device.

---

## ✦ Android Architecture

```
android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Declares RECEIVE_SMS & READ_SMS (Zero internet permission)
│   │   ├── java/com/aura/finance/
│   │   │   ├── MainActivity.kt          # Manages Android WebView and JavaScript bridge
│   │   │   ├── SmsReceiver.kt           # Real-time BroadcastReceiver for incoming bank SMS
│   │   │   ├── SmsScanner.kt            # Offline scanner for existing past SMS inbox
│   │   │   ├── SmsParser.kt             # High-accuracy regex engine for banking formats
│   │   │   └── WebAppInterface.kt       # Native-to-JavaScript two-way bridge
│   │   └── assets/                      # Self-contained offline dashboard (index.html, styles, icons)
```

### Supported Bank SMS Formats (Out of the Box)
- **UPI Apps**: Google Pay, PhonePe, Paytm, BHIM (`Sent Rs.420 to Swiggy UPI...`)
- **Indian Banks**: HDFC, SBI, ICICI, Axis, Kotak, PNB, Bank of Baroda
- **International Cards**: Chase, Citi, Wells Fargo, Amex (`Spent USD 45.00 at Target...`)

---

## ✦ How to Build and Run Manually

### Option 1: 1-Click Batch Execution (Fastest)
Double-click [`run_app.bat`](file:///A:/AI_Project_work/money_tracking/run_app.bat) in the project root directory:
```powershell
A:\AI_Project_work\money_tracking\run_app.bat
```
This script automatically:
1. Syncs `index.html` web dashboard assets to the Android app.
2. Compiles the Android debug APK using Gradle.
3. Installs the APK onto your connected simulator or Android device via `adb`.
4. Grants SMS permissions (`READ_SMS`, `RECEIVE_SMS`) and launches `MainActivity`.

---

### Option 2: Android Studio Run (▶) Button
1. Open **Android Studio** and click **File -> Open...** -> select `A:\AI_Project_work\money_tracking\android`.
2. Select your connected phone or simulator from the top device dropdown.
3. Click the green **Run (▶)** button (using the pre-configured `app` configuration in `.idea/runConfigurations/app.xml`).

---

## ✦ Testing Right Now Without Android Studio

You can test the exact same offline SMS regex engine directly in the dashboard right now:
1. Start the local server with [`start_server.bat`](file:///a:/AI_Project_work\money_tracking\start_server.bat) or open [`index.html`](file:///a:/AI_Project_work\money_tracking\index.html).
2. Click the green **"SMS Scanner"** button in the top navigation bar.
3. Click any of the pre-loaded bank sample buttons (HDFC, SBI, ICICI, Google Pay, Chase).
4. Watch it parse the transaction and immediately update the live **Credit/Debit/Total Balance** bar graph!
