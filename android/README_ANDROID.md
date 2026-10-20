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

## ✦ How to Build and Run on Your Android Phone

### Step 1: Open in Android Studio
1. Launch **Android Studio** on your computer.
2. Click **File -> Open...** and select the folder:
   ```
   A:\AI_Project_work\money_tracking\android
   ```
3. Allow Gradle to sync dependencies (takes ~1 minute on first open).

### Step 2: Connect Your Android Phone
1. On your phone, enable **Developer Options**:
   - Go to **Settings -> About Phone -> Tap "Build Number" 7 times**.
2. Go to **Settings -> Developer Options -> Enable "USB Debugging"**.
3. Connect your phone to your PC via USB cable.

### Step 3: Run the App
1. In Android Studio, select your phone from the device dropdown at the top.
2. Click the green **Run (▶)** button.
3. On your phone screen, tap **"Allow"** when prompted for SMS permissions.
4. That's it! 
   - All past banking SMS in your inbox will be scanned and plotted onto the bar graph.
   - Any new payment or bank SMS will instantly update the Credit/Debit graph in real time!

---

## ✦ Testing Right Now Without Android Studio

You can test the exact same offline SMS regex engine directly in the dashboard right now:
1. Start the local server with [`start_server.bat`](file:///a:/AI_Project_work\money_tracking\start_server.bat) or open [`index.html`](file:///a:/AI_Project_work\money_tracking\index.html).
2. Click the green **"SMS Scanner"** button in the top navigation bar.
3. Click any of the pre-loaded bank sample buttons (HDFC, SBI, ICICI, Google Pay, Chase).
4. Watch it parse the transaction and immediately update the live **Credit/Debit/Total Balance** bar graph!
