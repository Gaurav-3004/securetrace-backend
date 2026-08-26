# SecureTrace — Complete Project (Online Version)

Is folder me poora project hai:

```
SecureTrace-Complete/
├── SecureTrace/           ← Android app (Kotlin, Android Studio project)
└── SecureTrace-Backend/   ← Server (Flask API + database)
```

App **online** hai — Android Studio se app banega, aur wo internet ke through backend server se baat karega (login, register, OTP, sab kuch server ke through hota hai).

---

## Setup — 3 steps me karna hai (order zaroori hai)

### Step 1: Permanent Database banao (Neon.tech — free, forever)

Pehle wala setup Render ke free SQLite disk pe tha, jo restart hone par **delete ho sakta tha**. Ab humne isse **PostgreSQL** (permanent) me badal diya hai.

1. **neon.tech** pe jaake free account banao (credit card nahi chahiye)
2. **New Project** banao (naam kuch bhi rakho, jaise `securetrace`)
3. Project banते hi ek **Connection String** milegi, kuch aisi dikhegi:
   ```
   postgresql://username:password@ep-xxxx.neon.tech/dbname?sslmode=require
   ```
4. **Isko copy kar lo** — agle step me chahiye hoga

Yeh database ab permanent hai — Render server restart ho, sleep ho, redeploy ho, data **kabhi delete nahi hoga**.

### Step 2: Backend deploy karo (Render)

1. `SecureTrace-Backend` folder ki files (`app.py`, `requirements.txt`, `Procfile`, `runtime.txt`) ek **GitHub repository** me daalo
2. **render.com** pe free account banao
3. Render dashboard me **New → Web Service** → apni GitHub repo select karo
4. Settings:
   - Build Command: `pip install -r requirements.txt`
   - Start Command: `gunicorn app:app`
5. **Environment Variables** add karo:
   - `DATABASE_URL` → Step 1 wali Neon connection string paste karo
   - `GMAIL_ADDRESS` → tumhara Gmail address
   - `GMAIL_APP_PASSWORD` → Google App Password (normal password nahi — Google Account → Security → 2-Step Verification ON → App Passwords se banega)
   - `JWT_SECRET` → koi bhi random lambi string (jaise `mySuperSecret12345XYZ`)
6. **Deploy** dabao — kuch minute lagenge
7. Deploy hone ke baad Render ek URL dega, jaisa: `https://securetrace-backend.onrender.com` — **isko copy kar lo**, agle step me chahiye hoga

⚠️ Free Render service **inactive rehne par so jata hai** — pehli request pe 30-50 second lag sakte hain jagne me, normal hai. Lekin ab data safe rahega chahe server so jaye ya restart ho.

### Step 3: Android app chalao

1. `SecureTrace` folder Android Studio me **Open** karo
2. Sync hone do
3. Phone connect karo, ▶️ **Run** dabao
4. App khulte hi **"🌐 Connect to Server"** screen aayega — wahi Render URL (Step 1 ka) paste karo → **Connect**
5. Ab Login/Register screen aayega, normal use karo

**Default admin login:** `admin` / `admin123`

---

## Kya-kya bana hai (summary)

- Register + Login, dono me email OTP verification (2-factor)
- User Dashboard — login count, active devices, security status, activity timeline
- Activity list me session duration bhi dikhta hai (kitni der login raha)
- **Date range filter** — 7/30/90 din ya All time ka activity data dekh sakte ho
- **Excel download** — User apna activity data, Admin sabhi users ka data — `.xlsx` file me download kar sakte hain (jitne din ka filter lagाया usi ka data export hoga)
- Security Alerts — new device, failed attempts, password change, unusual pattern
- Admin Panel — sabhi users, stats, enable/disable accounts, analytics charts
- Passwords hashed (SHA-256 + salt), sessions JWT token se secure
- Database permanent hai (PostgreSQL/Neon) — server restart/redeploy hone par bhi data safe rehta hai
- IP address ya GPS kabhi collect nahi hota — privacy-first design

## OTP Email Setup (zaroori)

OTP bhejne ke liye Render dashboard me **Environment Variables** section me yeh add karo:
- `GMAIL_ADDRESS` = jis Gmail se OTP bhejna hai
- `GMAIL_APP_PASSWORD` = Google **App Password** (normal password nahi — 2-Step Verification ON karke Security settings se generate karo)

⚠️ Yeh kabhi bhi code files me hardcode mat karna, sirf Render ke Environment Variables me daalna — safe rehta hai.

Poori technical detail (features, database, API, architecture) **`SecureTrace_Project_Report.docx`** / **`.pdf`** me hai (pehle bhej chuka hoon) — wahi file Mam ko dikhane ke liye use karo.

---

## Agar kuch atke

- Screenshot bhej dena (Android Studio ka error, ya Render ka deploy log) — dekh ke exact fix bata dunga
- Render URL "Connect" nahi ho raha → check karo URL sahi paste hua (https:// ke saath), aur backend "Live" dikha raha hai Render dashboard me
