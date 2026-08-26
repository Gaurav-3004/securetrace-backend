# SecureTrace — Cybersecurity Login Monitoring App

**"Monitor. Detect. Protect."**

Ek complete native Android app (Kotlin + SQLite), premium dark-blue cybersecurity theme ke saath.

## ⚠️ Important: Architecture Note

Aapne bola tha "pichle wale jaisa" — isliye maine ise **native Android Studio project (Kotlin + on-device SQLite)** ki tarah banaya hai, jaisa humara pehla LoginMonitor app tha. Iska matlab:

- ✅ Koi Flask/MySQL server setup nahi chahiye
- ✅ Koi Flutter installation nahi chahiye
- ✅ Sab kuch offline, phone ke andar hi chalta hai
- ✅ Same Android Studio workflow jo humne pehle stabilize kiya tha

Agar future me real multi-device backend (Flask + MySQL + JWT) chahiye ho jisme data server pe centrally store ho, wo ek alag, bahut bada project hoga — bata dena, alag se plan kar denge.

## Default Login

```
Admin — Username: admin | Password: admin123
```
Normal users khud Register screen se account bana sakte hain.

## Features Implemented

**Authentication**
- Register, Login, Forgot/Reset Password, Logout
- Passwords kabhi plain text me store nahi hote — **salted SHA-256 hashing**

**Privacy-first Design**
- ❌ IP address — collect nahi hota
- ❌ GPS / precise location — collect nahi hota
- ✅ Sirf device name, model, OS version, app version, login timestamp store hota hai

**User Dashboard**
- Total login count, last login time, active device count
- Live security status indicator (🟢 Protected / 🟡 Attention / 🔴 Action Needed)
- Recent activity timeline

**Security Monitoring**
- New device login detection → alert
- Failed login tracking → 5 consecutive fails = account auto-locked
- Unusual pattern detection (success after multiple fails) → alert
- Password change → alert
- Local push notifications for all alerts

**Admin Panel** (separate Admin Login screen)
- Total users, total logins, failed login stats, alert summary
- User management — search, enable/disable accounts
- Analytics — 7-day login trend chart, device distribution chart, success rate

## Default Admin

```
Username: admin
Password: admin123
```

## Screens (12 total)

Splash → Login → Register → Forgot Password → Dashboard → Activity History → Security Alerts → Profile → Settings → Admin Login → Admin Dashboard → User Management → Analytics

## 🆕 OTP Email Verification (NEW)

Register aur Login dono ab **6-digit OTP verification** maangte hain — password sahi hone ke baad bhi, ek code Gmail se bheja jayega aur wahi enter karna hoga tabhi account create/login hoga.

### Ek-baar ka setup (zaroori)

App poori tarah offline hai, isliye email bhejne ke liye ek **Gmail account configure karna hoga** (aapka apna, ya koi bhi Gmail jo aap OTP-sending ke liye use karna chahein):

1. App kholo → Login screen ke sabse niche **"⚙️ Setup OTP Email"** link dabao
2. Jis Gmail se OTP bhejna hai uska address daalo
3. **App Password** daalo (yeh normal Gmail password nahi hai!):
   - Google Account → Security → **2-Step Verification ON karo** (agar off hai)
   - Phir Security me hi **"App Passwords"** section me jaake ek naya password generate karo (koi bhi naam de sakte ho, jaise "SecureTrace")
   - Wahan se mila 16-character code yahan paste karo
4. **Save & Send Test Email** dabao — agar sab sahi hai, ek test email khud ko mil jayega

Iske baad se **har naya register/login OTP maangega**, jo usi Gmail account se bheja jayega jisko user ne apne registration me diya tha.

### Security note
- App Password is device pe plain SharedPreferences me store hota hai — kisi important/personal Gmail ka password iske liye use mat karo, ek dedicated Gmail bana lo agar possible ho
- APK kisi aur ko share mat karo agar usme yeh config already save ho — warna wo bhi tumhare Gmail se emails bhej sakega
- Agar setup nahi kiya gaya ho, to app **automatically bina-OTP** login/register allow kar deta hai (taaki app kabhi completely lock na ho) — setup hote hi OTP zaroori ho jata hai



Same steps jo humne pehle follow kiye the:

1. Zip extract karo
2. Android Studio → Open → `SecureTrace` folder select karo
3. Gradle sync hone do (AGP 8.4.1 + Gradle 8.6 — pehle wala stable combo)
4. Phone connect karo (USB debugging ON) ya emulator use karo
5. ▶️ Run

## Project Structure

```
SecureTrace/
├── app/src/main/java/com/securetrace/app/
│   ├── MainActivity.kt              → Splash + router
│   ├── LoginActivity.kt / RegisterActivity.kt / ForgotPasswordActivity.kt
│   ├── DashboardActivity.kt         → User home
│   ├── ActivityHistoryActivity.kt / SecurityAlertsActivity.kt
│   ├── ProfileActivity.kt / SettingsActivity.kt
│   ├── AdminLoginActivity.kt / AdminDashboardActivity.kt
│   ├── UserManagementActivity.kt / AnalyticsActivity.kt
│   ├── db/DatabaseHelper.kt         → Saara security logic yahin hai
│   ├── model/                       → User, LoginActivityLog, SecurityAlert
│   ├── adapter/                     → RecyclerView adapters
│   └── util/                        → PasswordUtil, SessionManager, DeviceUtil, NotificationHelper
└── res/layout, values, drawable     → Glassmorphism dark theme UI
```
