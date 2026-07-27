# HealLens Backend Service

A modular, scalable Node.js + Express backend foundation for the **HealLens** healthcare application.

---

## 📁 Architecture Overview

```
backend/
├── server.js           # Main Express application entry point
├── package.json        # Dependencies & scripts
├── .env                # Environment variables configuration
├── .gitignore          # Excluded files for git version control
│
├── config/             # Database and third-party configurations (e.g. Supabase)
├── routes/             # Express API route endpoints
├── controllers/        # Request handlers & business logic
├── middleware/         # Custom middleware (authentication, error handling, validation)
├── services/           # Service layer & data access logic
├── utils/              # Helper utilities & custom error classes
└── README.md           # Backend documentation
```

---

## 🚀 Getting Started

### 1. Installation

Navigate to the `backend/` directory and install the required dependencies:

```bash
cd backend
npm install
```

### 2. Running locally

Start the development server with live reload (`nodemon`):

```bash
npm run dev
```

Or start the production server:

```bash
npm start
```

### 3. Server Endpoints

| Method | Endpoint | Description | Expected Output |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Health check endpoint | `HealLens Backend is Running 🚀` |

---

## 🌐 Environment Variables (`.env`)

- `PORT`: Port on which the server listens (Default: `5000`)
