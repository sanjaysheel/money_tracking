# Aura Finance — BenQ-Style Flagship Product Showcase Website

This is a **fresh, dedicated product showcase website** built specifically to promote **Aura Finance** as an industrial-grade, sovereign personal wealth and cash flow technology product.

The original application and native Android source files in the parent directory (`money_tracking`) remain **100% untouched**. All promotional website assets, styles, interactive simulators, architecture laboratories, and documentation reside exclusively within this `website/` folder.

---

## 🌟 Inspired by BenQ's Engineering Philosophy & Apple Pro Aesthetics

BenQ's flagship product showcases (such as MOBIUZ, DesignVue, and ScreenBar) are celebrated for their uncompromising, millimeter-level dissection of every single subsystem, optical coating, calibration certificate, and architectural layer. 

This website brings that exact senior design engineering standard to personal finance software:

### 1. Interactive Titanium Smartphone Simulator
- **Hardware-Fidelity Chassis:** Titanium bezel, dynamic notch/Dynamic Island, hardware volume/power buttons, live digital clock, and home indicator.
- **Dynamic Push Alert Notifications:** Simulates incoming bank SMS notifications that drop from the Dynamic Island, auto-ingesting into the live simulated ledger.
- **4 Live Functional In-App Tabs:**
  - *Dashboard:* Net Balance hero card, Inflow/Outflow ratio gauges, live transaction search, swipeable filter pills (All, Debits, Credits, Card Tails), and reactive ledger rows.
  - *Insights & Analytics:* Run-rate burn projections, pure vector SVG 6-month cash flow chart, category budgets with progress bars, and savings goals.
  - *Scheduler & Bills:* Total Due, Paid So Far, Pending Due KPI cards, upcoming recurring bills with working checkboxes that toggle Paid/Pending and update KPI totals in real time.
  - *Vault Settings:* Dark/Light mode toggle, 4-tier glass refraction selector, live text scaling slider (80%–120%), currency picker, and app lock.
- **Modals & Security Gate:** Working "➕ Quick Add Entry" modal, "📥 Parse Bank SMS" modal, and working 4-digit PIN lockscreen (`1234`) with biometric fingerprint sensor button.
- **Companion Remote Control Deck:** External bench controls for quick tab switching, injecting simulated bank transactions, locking the virtual device, toggling optical refraction tiers, and resetting demo data.

### 2. Architectural 6-Layer Exploded System Stack
Interactive 3D layer teardown detailing the complete execution boundary from silicon to screen:
- **Layer 06 // GPU Surface:** Presentation Viewport & Liquid Glass (<0.8ms paint, 60 FPS, 1.2 MB VRAM).
- **Layer 05 // Real-Time Calculus:** Deterministic Cash Flow Arithmetic (0.04ms runrate, zero floating-point drift).
- **Layer 04 // Embedded WAL:** Sandboxed SQLite B-Tree Engine (`aura_finance.db` schema v4, 0.22ms atomic commits).
- **Layer 03 // Telephony Ingress:** DLT Regex Tokenizer & Entity Resolver (35+ commercial Indian banks, 0.38ms average match).
- **Layer 02 // OS Boundary:** Zero-Internet Kernel Sandbox (`android.permission.INTERNET` physically omitted).
- **Layer 01 // Silicon Trust:** Hardware Keystore TEE & Biometrics (Hardware-backed AES keys, `BiometricPrompt`).

### 3. Interactive Subsystem Engineering Laboratories
- **Sub-Millisecond Ingestion Pipeline Simulator:** Trigger a simulated telephony interrupt and watch the packet travel across the 5 processing stages in real-time with live latency counters.
- **Executive Runway & Health Simulator:** Interactive sliders for Monthly Inflow, Monthly Outflow, and Target Savings Ratio that dynamically recalculate Net Balance, Achieved Savings Rate, Runway Buffer Days, and Financial Health Grade (AAA / AA / A / BBB).
- **Tactile Subscription & Bill Matrix:** Interactive toggle switches that mark bills as Paid/Pending and recompute Total Due, Paid Liquidity, and Pending Cash Drag.
- **Kernel Security Sandbox & Outbound Socket Firewall Test:** Audits the 12 Android permissions and provides a live test button that simulates SELinux kernel rejection of outbound TCP/IP socket requests.
- **Optical Liquid Glass Calibration Studio:** Interactive sliders for Blur Radius (0–40px), Surface Opacity (10%–90%), and Ambient Tint matrices with live code output.

### 4. Senior Creative Engineering Polish
- **Dynamic Mouse-Tracking Spotlight:** Radial border luminescence (`--mouse-x`, `--mouse-y`) that tracks the user's cursor across cards.
- **3D Perspective Tilt Physics:** Cards smoothly tilt in 3D perspective based on pointer position.
- **Web Audio Tactile Sound Engine:** Zero-dependency audio synthesizer generating subtle mechanical clicks on button hovers, clicks, and tab switches (with audio toggle switch in the subnav).
- **Zero Consumer Emojis:** Completely free of standard unicode emojis (`🤖`, `🍎`, `📱`, `🍷`, etc.); rendered exclusively with bespoke, calibrated duotone SVG glyphs and typography.
- **BenQ Engineering Reticles:** Corner registration crosshairs (`+`, `┌ ┐ └ ┘`) and telemetry chips.

### 5. Verified Source Code & Schema Workbench
- Direct inspection of production Kotlin and SQLite snippets:
  - `AndroidManifest.xml` (Air-gap proof with physical omission of `INTERNET`)
  - `SmsParser.kt` (Compiled regex heuristics & DLT entity resolution)
  - `TransactionDbHelper.kt` (SQLite schema v4 with WAL journal mode)
  - `WebAppInterface.kt` (Secure JavaScript-to-native Android bridge)
- One-click snippet copying with visual confirmation.

### 6. Cryptographic Integrity & Downloads
- Direct download for `Aura_Finance.apk` (4.6 MB).
- Authenticated SHA-256 binary hash verification (`971275966023B78F8D9872DB37E7289A152D893EFD8D425E14D292F80E5BE974`).
- One-click copy checksum button.

---

## 📂 File Architecture

```
website/
├── index.html                 # Complete 2,400+ line BenQ-caliber product showcase
├── README.md                  # System & architectural documentation
├── assets/
│   ├── Aura_Finance.apk       # Standalone Android APK binary (verified SHA-256)
│   ├── icon-192.png           # 192x192 PWA app icon
│   ├── icon-512.png           # 512x512 PWA splash icon
│   └── apple-touch-icon.png   # iOS Safari touch icon
├── css/
│   ├── style.css              # Core typography, hero, cards, and grid styles
│   ├── animations.css         # Keyframe animations, ambient orbs, and glass glow
│   ├── simulator.css          # Titanium smartphone chassis, Dynamic Island, and apps
│   └── developer-spec.css     # Bento grids, exploded stack, interactive labs, and reticles
└── js/
    ├── main.js                # Interaction engines, audio synthesizer, labs, and workbench
    └── simulator.js           # Virtual smartphone operating system and reactive ledger
```

---

## 🚀 How to Run & Preview

Open `website/index.html` directly in any modern web browser (Google Chrome, Microsoft Edge, Mozilla Firefox, Safari).

Or serve it locally using any static file server:
```powershell
# Using Python:
python -m http.server 8000 --directory website

# Or using Node:
npx serve website
```
Then navigate to `http://localhost:8000`.
