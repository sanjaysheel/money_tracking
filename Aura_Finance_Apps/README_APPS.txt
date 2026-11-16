========================================================================
AURA FINANCE - UNIFIED APPS DISTRIBUTION HUB (ANDROID & iOS)
========================================================================

Welcome to the official app distribution folder for Aura Finance.
This folder contains everything needed to install and run the app directly
on both Android devices and Apple iPhones/iPads.

------------------------------------------------------------------------
📁 Android/
------------------------------------------------------------------------
- Aura_Finance.apk
  -> Fully compiled, aligned, and signed native Android APK.
  -> Ready to install on any Android phone (Android 8.0 to Android 15+).
  -> Includes automatic Bank SMS transaction scanner, local SQLite database,
     biometric fingerprint auth, and moving liquid glass effects.
  -> Refer to: INSTALL_GUIDE_ANDROID.txt

------------------------------------------------------------------------
📁 iOS/
------------------------------------------------------------------------
Because Apple iOS prevents direct APK-style downloads outside of macOS,
two turnkey methods are provided:

1. Direct iPhone Home-Screen Installation (Zero Mac / Zero App Store required):
   -> Double-click "Start_iOS_Server.bat" on this PC.
   -> Open Safari on your iPhone, enter the displayed Wi-Fi address.
   -> Tap Share -> "Add to Home Screen".
   -> Aura Finance installs directly as a standalone native app with its custom icon!

2. Xcode Swift Project (For Mac / Xcode users):
   -> Open "Xcode_Project_Template/AuraFinance" in Xcode on Mac to compile a native .ipa.

- Refer to: INSTALL_GUIDE_IOS.txt for detailed step-by-step guides.
========================================================================
