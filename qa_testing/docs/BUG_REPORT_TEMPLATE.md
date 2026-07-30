# HealLens AI — Defect Report Template & Defect Log

**Project Name:** HealLens AI  
**Document Version:** 1.0.0  
**Status:** QA Standard Template  

---

## 1. Industry Standard Bug Report Template

```markdown
### Bug ID: BUG-HEALLENS-XXX

- **Module:** [Authentication | Dashboard | Image Analysis | Medical Report | Clinical History | Emergency SOS | Profile | UI]
- **Summary:** [Short descriptive title of the defect]
- **Severity:** [Critical | High | Medium | Low]
- **Priority:** [P1 - Urgent | P2 - High | P3 - Medium | P4 - Low]
- **Browser / Environment:** [Chrome 126 / Windows 11 / Staging URL: http://localhost:5500/login.html]
- **User Account Type:** [Verified User | New User | Guest]

#### Preconditions:
1. User logged in to HealLens AI.
2. Active internet connection for Supabase database access.

#### Steps to Reproduce:
1. Navigate to [Target Module].
2. Perform [Action].
3. Click on [Element].

#### Expected Result:
[Detailed description of expected application behavior]

#### Actual Result:
[Detailed description of actual failing behavior]

#### Attachments / Screenshots:
- Screenshot: `bug_xxx_screenshot.png`
- Browser Console Log: `console_error.log`

#### Defect Lifecycle Status:
- **Current Status:** [New | Open | In Progress | Resolved | Verified Closed]
- **Assigned To:** [Developer / Engineering Lead]
- **Resolution:** [Fixed | Cannot Reproduce | Duplicate | As Designed | Deferred]
```

---

## 2. Logged Sample Defect Reports for HealLens AI

### Defect 1: BUG-HEALLENS-001 (Resolved)
- **Module:** Authentication (Forgot Password)
- **Summary:** Rapid duplicate clicks on "Forgot Password" link trigger Supabase 429 rate limit error
- **Severity:** High
- **Priority:** P2 - High
- **Browser:** Google Chrome v126 / Windows 11
- **Steps to Reproduce:**
  1. Open `login.html`.
  2. Click "Forgot Password?".
  3. Input valid registered email `admin@heallens.com`.
  4. Rapidly click OK twice.
- **Expected Result:** Application should prevent duplicate parallel submissions and inform user of cooldown gracefully.
- **Actual Result:** Second request hit Supabase `resetPasswordForEmail()` within 60s, producing raw error alert "email rate limit exceeded".
- **Resolution:** Fixed. In-flight lock guard `window.isResettingPassword` implemented to prevent duplicate executions and display user-friendly cooldown guidance.
- **Status:** Verified Closed ✅

---

### Defect 2: BUG-HEALLENS-002 (Resolved)
- **Module:** Emergency SOS
- **Summary:** Adding 4th emergency contact failed silently without feedback
- **Severity:** Medium
- **Priority:** P3 - Medium
- **Browser:** Mozilla Firefox v127 / macOS
- **Steps to Reproduce:**
  1. Add 3 emergency contacts in Emergency SOS tab.
  2. Fill in details for a 4th contact.
  3. Click "Add Contact".
- **Expected Result:** Validation warning message "Maximum 3 emergency contacts allowed" should be displayed.
- **Actual Result:** Form submitted but contact was not added to list, with no alert feedback.
- **Resolution:** Fixed. Added explicit length check `userContacts.length >= 3` returning alert feedback before API invocation.
- **Status:** Verified Closed ✅

---

### Defect 3: BUG-HEALLENS-003 (Resolved)
- **Module:** Clinical History
- **Summary:** Localhost 5000 API fallback calls producing console warnings when server unavailable
- **Severity:** Low
- **Priority:** P4 - Low
- **Browser:** Microsoft Edge v126 / Windows 11
- **Steps to Reproduce:**
  1. Open Clinical History tab with Supabase Cloud DB connected.
  2. Inspect Browser Console.
- **Expected Result:** Application should sync exclusively with Supabase database without calling non-existent localhost endpoints.
- **Actual Result:** Secondary fallback fetch to `http://localhost:5000` logged minor connection warnings.
- **Resolution:** Fixed. Obsolete `http://localhost:5000` fallback calls removed; application operates 100% cloud-native via Supabase SDK.
- **Status:** Verified Closed ✅
