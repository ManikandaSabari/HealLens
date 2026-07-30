# HealLens AI — Software Quality Assurance Test Plan

**Document Version:** 1.0.0  
**Project Name:** HealLens AI  
**Project Type:** AI-Based Healthcare Web Application  
**Author:** QA Engineering Team / Project Evaluation Panel  
**Date:** July 30, 2026  
**Status:** Approved for Execution  

---

## 1. Executive Summary & Objective

**HealLens AI** is an advanced AI-powered web and mobile medical diagnostic application. It provides real-time image analysis (Chest X-Rays, Skin Lesions, Bone X-Rays using TensorFlow.js CNN models and rule-based fallbacks), medical biomarker report parsing, cloud-synchronized clinical history, and cloud-synchronized 3-contact emergency SOS features via Supabase.

The primary objective of this **Quality Assurance (QA) Test Plan** is to establish a rigorous, industry-standard testing methodology covering manual test case execution, cross-browser validation, UI/UX performance, and an automated Selenium WebDriver TestNG Page Object Model (POM) testing framework.

---

## 2. Technology Stack & Target Architecture

| Component Layer | Technology / Framework |
| :--- | :--- |
| **Frontend UI** | HTML5, CSS3 (Vanilla Glassmorphism), Modern JavaScript (ES6+) |
| **Backend & APIs** | Node.js + Express.js API Services |
| **Database & Cloud Storage** | Supabase PostgreSQL Database, Supabase Storage Buckets |
| **Authentication Engine** | Supabase Auth (JWT, OAuth 2.0 Google Integration, Email Verification) |
| **AI Processing Engine** | TensorFlow.js (CNN Deep Learning Models + Heuristic Rule-Based Fallback) |
| **Mobile Runtime** | Capacitor Android Wrapper |
| **Test Automation Stack** | Java 17, Selenium WebDriver 4.x, TestNG 7.x, Maven, ExtentReports 5.x |
| **Continuous Integration (CI)** | GitHub Actions Workflow (`maven-test.yml`) |

---

## 3. Scope of Testing

### 3.1 In-Scope Functional Modules
1. **Authentication & Identity Management**:
   - User Signup with Email & Password validation.
   - Email verification link workflows.
   - User Login with JWT session persistence.
   - Forgot Password email generation (`resetPasswordForEmail`).
   - Reset Password modal & password update (`updateUser`).
   - Google OAuth 2.0 single sign-on integration.
   - Session logout & token clearance.
   - Duplicate user registration & invalid credential validations.

2. **Main Navigation & Dashboard**:
   - Dashboard page layout & responsive grid rendering.
   - Dynamic user profile header synchronization.
   - Module entry points (Scanner, Lab Analyzer, History, SOS, Profile).
   - Sidebar overlay navigation & mobile hamburger toggle.

3. **AI Image Analysis Engine**:
   - Upload & drag-and-drop validation for X-Ray (Chest), Skin Lesion, and Bone fracture images.
   - Real-time client-side TensorFlow.js CNN inference.
   - Classification output: Condition name, confidence score (%), severity level (Low, Moderate, High, Severe), and medical remedies.
   - Automatic & manual cloud database persistence (`clinical_records`).

4. **Medical Report Biomarker Analysis**:
   - Lab report document/image upload.
   - Automatic biomarker extraction (Hemoglobin, Glucose, Cholesterol, WBC, RBC, Platelets, etc.).
   - Risk summary classification & clinical advice generation.
   - Persistence to Supabase cloud database.

5. **Clinical History & Cloud Synchronization**:
   - Historical image scan & biomarker report listing.
   - Category filtering (All, Image Scans, Biomarker Reports) and member filtering.
   - Single record deletion & full history clearing.
   - Multi-device / cross-browser cloud synchronization via Supabase realtime subscriptions.

6. **Emergency SOS System**:
   - Emergency contact creation (Name, Relationship, Phone Number).
   - Strict **3-Contact Maximum Limit** validation.
   - Inline contact editing and deletion.
   - Real-time cloud sync to Supabase database.
   - One-click SOS dispatch (SMS, Direct Phone Call, Geolocation broadcast).

7. **User Profile & Settings**:
   - User profile information rendering (Full Name, Email, Registration Date).
   - Dynamic theme toggling & interface customization.

8. **UI/UX & Cross-Browser Validation**:
   - Glassmorphism UI layout, responsive breakpoints (Desktop, Tablet, Mobile).
   - Dynamic Toast notifications & modal dialogs.
   - Strict input validation & error boundary behavior.

### 3.2 Out-of-Scope Items
- Third-party cellular network carrier delivery delays for SOS SMS.
- Supabase infrastructure hardware failover testing.

---

## 4. Test Strategy & Methodology

```
+-----------------------------------------------------------------------------------+
|                                  HEALLENS AI QA SUITE                             |
+------------------------------------------+----------------------------------------+
                                           |
    +--------------------------------------+-----------------------------------+
    |                                      |                                   |
    v                                      v                                   v
[Manual Testing Suite]        [Selenium Automation Suite]          [CI/CD & Reporting]
- 400+ Test Cases             - Page Object Model (POM)            - TestNG XML Runner
- Smoke / Sanity              - Selenium 4 WebDriver               - ExtentReports HTML
- Exploratory & Regression    - Cross-Browser Execution            - GitHub Actions Pipeline
- Edge Cases & UI             - Screenshot Capture on Fail         - Multi-Sheet Excel Report
```

### 4.1 Test Levels
1. **Unit & Component Testing**: Testing isolated JS functions, helper utilities, and regex parsers.
2. **Integration Testing**: Validating interaction between Frontend UI components and Supabase Auth / PostgreSQL endpoints.
3. **System & End-to-End (E2E) Testing**: Complete user journey validation from Signup to AI Diagnosis to Emergency SOS.
4. **Regression Testing**: Ensuring bug fixes do not regress existing authentication or sync workflows.

---

## 5. Test Environment & Configurations

| Parameter | Configuration / Specification |
| :--- | :--- |
| **Staging Web URL** | `http://localhost:5500/login.html` / `https://heallens.app` |
| **Supported Browsers** | Google Chrome v126+, Mozilla Firefox v127+, Microsoft Edge v126+, Safari 17+ |
| **Supported OS** | Windows 11, macOS Sonoma, Android 14, iOS 17 |
| **Screen Resolutions** | Desktop (1920x1080, 1440x900), Tablet (768x1024), Mobile (375x812) |
| **Java JDK** | Java SE Development Kit 17 LTS |
| **Build Tool** | Apache Maven 3.9+ |

---

## 6. Entry & Exit Criteria

### 6.1 Entry Criteria
- Code build successfully compiled and deployed to testing environment.
- Supabase database schema & Auth configuration active.
- Test plan and test cases reviewed and approved by evaluation panel.
- Selenium WebDriver automation framework dependencies loaded.

### 6.2 Exit Criteria
- 100% of planned test cases executed.
- Overall test pass rate **>= 98%**.
- Zero **Critical** or **High** severity open bugs in core modules.
- ExtentReports HTML execution report and Excel test report generated.

---

## 7. Defect Management & Severity Matrix

Defects identified during testing are logged using the standard QA Defect Template and categorized as follows:

| Severity | Criteria | Resolution SLA |
| :--- | :--- | :--- |
| **Critical (S1)** | System crash, data loss, login complete block, Supabase sync failure. | Immediate (< 4 hrs) |
| **High (S2)** | AI classification error, Emergency SOS add/delete failure, reset password fail. | Within 24 hrs |
| **Medium (S3)** | UI alignment issue, missing toast message, minor validation text mismatch. | Within 48 hrs |
| **Low (S4)** | Cosmetic typo, minor color discrepancy, subtle transition delay. | Next Sprint |

---

## 8. Risk Assessment & Mitigation Plan

| Identified Risk | Risk Impact | Mitigation Strategy |
| :--- | :--- | :--- |
| Supabase Auth email rate limits during automated testing | High | Implement 60s sleep/cooldown between reset password execution tests; mock Auth tokens where applicable. |
| Large TensorFlow.js model load latency on low-bandwidth connections | Medium | Pre-load TF.js models asynchronously during page load; implement loader spinners with timeouts. |
| Dynamic DOM elements causing StaleElementReferenceException in Selenium | Medium | Implement Explicit Waits (`WebDriverWait`) using `ExpectedConditions` in `WaitUtils.java`. |

---

**Approval Sign-off:**  
- **Lead QA Engineer:** *Approved*  
- **Project Advisor / Evaluator:** *Approved*  
