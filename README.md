# 🩺 HealLens AI

> **AI-Powered Healthcare Assistant for Medical Image Analysis, Report Analysis, and Cloud-Based Patient Records**

HealLens AI is a full-stack healthcare web application that leverages Artificial Intelligence to assist users in analyzing medical images and laboratory reports. It provides secure authentication, cloud synchronization, clinical history management, and emergency contact support through a modern responsive interface.

---

# 🚀 Features

## 🤖 AI-Powered Analysis
- 🫁 Chest X-ray Analysis
- 🦴 Bone X-ray Analysis
- 🩺 Skin Disease Analysis
- 📄 Medical Report Analysis
- 📊 AI Confidence Score
- 💡 Personalized Recommendations

## 🔐 Authentication
- Email & Password Login
- Google Sign-In
- Email Verification
- Forgot Password
- Password Reset
- Secure Session Management

## ☁️ Cloud Features
- Supabase Cloud Database
- Clinical History Synchronization
- Cross-Device Sync
- Emergency Contacts Cloud Storage
- Multi-user Data Isolation (Row Level Security)

## 📚 Patient Management
- Clinical History
- Report History
- Emergency SOS Contacts
- User Profile Management

## 🌍 Additional Features
- Responsive Design
- Multilingual Support
- Android Support (Capacitor)
- Modern Glassmorphism UI

---

# 🛠️ Tech Stack

## Frontend
- HTML5
- CSS3
- JavaScript (ES6)
- TensorFlow.js

## Backend
- Node.js
- Express.js

## Database
- Supabase (PostgreSQL)

## AI
- TensorFlow.js
- CNN (Convolutional Neural Network)

## Mobile
- Capacitor
- Android Studio

## Deployment
- Vercel

---

# 🏗️ System Architecture

```text
                 User
                   │
                   ▼
            HealLens Frontend
         (HTML • CSS • JavaScript)
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
 TensorFlow.js AI       Supabase Auth
        │                     │
        ▼                     ▼
 Image/Report AI      Authentication
        │                     │
        └──────────┬──────────┘
                   ▼
            Supabase Database
                   │
         Clinical Records
         Emergency Contacts
         User Profiles
```

---

# 📂 Project Structure

```
HealLens/
│
├── backend/
│   ├── controllers/
│   ├── routes/
│   ├── services/
│   └── server.js
│
├── android/
├── js/
├── style/
├── www/
├── model/
└── README.md
```

---

# ✨ Current Status

| Module | Status |
|---------|--------|
| Authentication | ✅ Completed |
| Google Login | ✅ Completed |
| Forgot Password | ✅ Completed |
| Email Verification | ✅ Completed |
| AI Image Analysis | ✅ Completed |
| Medical Report Analysis | ✅ Completed |
| Clinical History | ✅ Completed |
| Emergency Contacts | ✅ Completed |
| Supabase Cloud Sync | ✅ Completed |
| Row Level Security (RLS) | ✅ Completed |
| Responsive UI | ✅ Completed |
| Deployment | ✅ Completed |

---

# 🔒 Security

- Secure Authentication
- Supabase Row Level Security (RLS)
- User-specific Clinical Records
- User-specific Emergency Contacts
- Cloud Session Management

---

# 📸 Screenshots

> *(Add screenshots here after deployment)*

- Login Page
- Dashboard
- AI Scanner
- Medical Report Analyzer
- Clinical History
- Emergency Contacts

---

# 🌐 Live Demo

**Vercel Deployment**

https://heallenss.vercel.app

---

# 👨‍💻 Author

**Manikanda Sabari**

AI & Data Science Engineer

---

# 📄 License

This project is developed for educational and research purposes.
