// app.js — Main app controller: routing, tab navigation, all page rendering
document.addEventListener("DOMContentLoaded", () => {
  // Init i18n first
  window.i18n?.applyTranslations();

  // Init scanner
  window.scanner?.init();

  // Setup navigation
  setupNavigation();

  // Setup language switcher
  setupLanguageSwitcher();

  // Render default tab
  showTab("scanner");

  // Setup chatbot
  setupChatbot();

  // Render SOS contacts
  renderSOSContacts();

  // Render profile
  renderProfile();

  // Setup Ayurveda body map
  setupAyurvedaMap();

  // Init Appointment Manager
  window.appointmentManager?.init();
});

// Global helpers for HTML onclick handlers
window.addSymptomChip = function(sym) {
  const input = document.getElementById('symptom-input');
  if (!input) return;
  const val = input.value.trim();
  if (!val.toLowerCase().includes(sym.toLowerCase())) {
    input.value = val ? val + ", " + sym : sym;
  }
};

window.closeSymptomModal = function(addSym) {
  document.getElementById('symptom-prompt-modal').style.display = 'none';
  if (addSym) {
    const section = document.getElementById('tab-scanner');
    if (section) section.scrollIntoView({ behavior: 'smooth' });
    document.getElementById('symptom-input')?.focus();
  } else {
    if (window.scanner) {
      window.scanner.forceAnalysis = true;
      document.getElementById('analyze-btn')?.click();
    }
  }
};


// ─── Tab Navigation ─────────────────────────────────────────────────────────
function setupNavigation() {
  document.querySelectorAll(".nav-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const tab = btn.getAttribute("data-tab");

      if (tab === "history") {
        const historyGroup = document.getElementById("nav-group-history");
        const isHistoryActive = btn.classList.contains("active");
        const isOpen = historyGroup?.classList.contains("open");

        if (isHistoryActive && isOpen) {
          // Second click on Clinical History -> Close / collapse sub-menu & reset history view
          historyGroup.classList.remove("open");
          if (window.historyManager) window.historyManager.activeSubTab = null;
          renderHistoryTab();
          return;
        } else {
          // Opening history
          if (historyGroup) historyGroup.classList.add("open");
          showTab("history");
        }
      } else {
        document.getElementById("nav-group-history")?.classList.remove("open");
        showTab(tab);
      }

      document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
    });
  });

  // Mobile hamburger
  document.getElementById("hamburger-btn")?.addEventListener("click", () => {
    document.getElementById("sidebar").classList.toggle("mobile-open");
  });

  document.getElementById("sidebar-overlay")?.addEventListener("click", () => {
    document.getElementById("sidebar").classList.remove("mobile-open");
  });
}

function showTab(tabName) {
  document.querySelectorAll(".tab-content").forEach(s => s.style.display = "none");
  const target = document.getElementById(`tab-${tabName}`);
  if (target) target.style.display = "block";

  // Update Topbar Title
  const navBtn = document.querySelector(`.nav-btn[data-tab="${tabName}"]`);
  const label = navBtn?.querySelector(".nav-label")?.innerText;
  const topbarTitle = document.getElementById("topbar-title");
  if (topbarTitle && label) topbarTitle.innerText = label;

  // Toggle sidebar sub-menu for history
  const historyGroup = document.getElementById("nav-group-history");
  if (historyGroup) {
    if (tabName === "history") {
      historyGroup.classList.add("open");
    } else {
      historyGroup.classList.remove("open");
    }
  }

  if (tabName === "history") renderHistoryTab();
  if (tabName === "sos") renderSOSContacts();
  if (tabName === "profile") renderProfile();
  if (tabName === "remedies") setupAyurvedaMap();
  if (tabName === "lab-analyzer") window.labAnalyzer?.resetOrRender();

  // Close sidebar on mobile
  document.getElementById("sidebar")?.classList.remove("mobile-open");
}

// ─── Language Switcher ───────────────────────────────────────────────────────
function setupLanguageSwitcher() {
  document.querySelectorAll(".lang-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const lang = btn.getAttribute("data-lang");
      window.i18n?.setLanguage(lang);
      document.querySelectorAll(".lang-btn").forEach(b => b.classList.remove("active"));
      btn.classList.add("active");

      // Update current result if any
      window.scanner?.reRenderCurrentResult();

      // Update voice engine lang
      if (window.voiceEngine?.recognition) {
        window.voiceEngine.recognition.lang = window.i18n.getVoiceLang();
      }
    });
  });

  // Set active lang btn based on current lang
  const currentLang = window.i18n?.currentLang || "en";
  document.querySelector(`.lang-btn[data-lang="${currentLang}"]`)?.classList.add("active");
}


// ─── History Tab ─────────────────────────────────────────────────────────────
function openHistoryWithSubTab(subtab) {
  showTab("history");
  document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
  document.querySelector('.nav-btn[data-tab="history"]')?.classList.add("active");
  document.getElementById("nav-group-history")?.classList.add("open");
  switchHistorySubTab(subtab);
}

function switchHistorySubTab(subtab) {
  if (window.historyManager) {
    window.historyManager.activeSubTab = subtab;
  }
  renderHistoryTab();
}

function renderHistoryTab() {
  const container = document.getElementById("history-list");
  if (!container) return;

  const currentSubTab = window.historyManager?.activeSubTab || null;

  // Update top tab buttons active state
  const imgBtn = document.getElementById("history-tab-image");
  const repBtn = document.getElementById("history-tab-report");
  if (imgBtn) imgBtn.classList.toggle("active", currentSubTab === "image");
  if (repBtn) repBtn.classList.toggle("active", currentSubTab === "report");

  // Update sidebar sub-buttons active state
  document.querySelectorAll(".sidebar-sub-btn").forEach(btn => {
    const sub = btn.getAttribute("data-history-subtab");
    btn.classList.toggle("active", sub === currentSubTab);
  });

  const filterMember = document.getElementById("history-filter-member")?.value || "all";
  const filterSeverity = document.getElementById("history-filter-severity")?.value || "all";

  if (!currentSubTab) {
    // Landing prompt view when no subtab is selected yet
    container.innerHTML = `
      <div class="history-landing-select" style="text-align: center; padding: 40px 20px;">
        <div style="font-size: 3rem; margin-bottom: 12px;">📋</div>
        <h3 style="font-size: 1.3rem; margin-bottom: 8px; color: var(--text-heading);">Select History Record Type</h3>
        <p style="color: var(--text-secondary); margin-bottom: 24px; font-size: 0.92rem;">Select an option below or from the sidebar to view health records.</p>
        <div style="display: flex; gap: 20px; justify-content: center; flex-wrap: wrap;">
          <button class="history-select-card" onclick="openHistoryWithSubTab('image')" style="flex: 1; min-width: 220px; max-width: 320px; padding: 24px; border-radius: var(--radius-lg); background: var(--bg-glass); border: 1.5px solid var(--border-color); cursor: pointer; transition: all var(--transition-base); text-align: center;">
            <div style="font-size: 2.5rem; margin-bottom: 12px;">📷</div>
            <div style="font-weight: 700; font-size: 1.1rem; color: var(--text-primary); margin-bottom: 6px;">Image Analysis</div>
            <div style="font-size: 0.82rem; color: var(--text-secondary);">View past X-ray & skin diagnostic scan records</div>
          </button>
          <button class="history-select-card" onclick="openHistoryWithSubTab('report')" style="flex: 1; min-width: 220px; max-width: 320px; padding: 24px; border-radius: var(--radius-lg); background: var(--bg-glass); border: 1.5px solid var(--border-color); cursor: pointer; transition: all var(--transition-base); text-align: center;">
            <div style="font-size: 2.5rem; margin-bottom: 12px;">📊</div>
            <div style="font-weight: 700; font-size: 1.1rem; color: var(--text-primary); margin-bottom: 6px;">Report Analysis</div>
            <div style="font-size: 0.82rem; color: var(--text-secondary);">View blood report analysis logs</div>
          </button>
        </div>
      </div>`;
    return;
  }

  if (currentSubTab === "image") {
    let history = window.historyManager?.getImageHistory() || [];
    if (filterMember !== "all") history = history.filter(h => h.memberName === filterMember);
    if (filterSeverity !== "all") history = history.filter(h => h.severity === filterSeverity);

    if (history.length === 0) {
      container.innerHTML = `
        <div class="empty-history">
          <div class="empty-icon">📷</div>
          <p>${window.i18n?.t("noImageHistory") || "No image analysis history yet."}</p>
        </div>`;
      return;
    }

    container.innerHTML = history.map(entry => window.historyManager.renderCard(entry, "image")).join("");
  } else if (currentSubTab === "report") {
    let history = window.historyManager?.getReportHistory() || [];
    if (filterMember !== "all") history = history.filter(h => (h.memberName === filterMember || h.patientName === filterMember));
    if (filterSeverity !== "all") history = history.filter(h => h.riskLevel === filterSeverity);

    if (history.length === 0) {
      container.innerHTML = `
        <div class="empty-history">
          <div class="empty-icon">📊</div>
          <p>${window.i18n?.t("noReportHistory") || "No report analysis history yet."}</p>
        </div>`;
      return;
    }

    container.innerHTML = history.map(entry => window.historyManager.renderCard(entry, "report")).join("");
  }

  // Update member filter options
  updateMemberFilterOptions();
}

function updateMemberFilterOptions() {
  const select = document.getElementById("history-filter-member");
  if (!select) return;
  const members = window.historyManager?.getFamilyMembers() || [];
  const existing = Array.from(select.options).map(o => o.value);
  members.forEach(m => {
    if (!existing.includes(m.name)) {
      const opt = document.createElement("option");
      opt.value = m.name;
      opt.textContent = m.name;
      select.appendChild(opt);
    }
  });
}

function viewHistoryDetail(id) {
  const history = window.historyManager?.getImageHistory() || [];
  const entry = history.find(h => h.id === id);
  if (!entry) return;

  // Temporarily show as scan result
  showTab("scanner");
  document.querySelector('.nav-btn[data-tab="scanner"]')?.click();
  if (window.scanner) {
    window.scanner.currentResult = entry;
    window.scanner.showResult(entry);
  }
}

function viewReportHistoryDetail(id) {
  const history = window.historyManager?.getReportHistory() || [];
  const entry = history.find(h => h.id === id);
  if (!entry) return;

  if (window.labAnalyzer) {
    window.labAnalyzer.patientName = entry.patientName || entry.memberName || "Jane Doe";
    window.labAnalyzer.patientAge = entry.patientAge || 30;
    window.labAnalyzer.patientGender = entry.patientGender || "female";
    if (entry.biomarkers && Object.keys(entry.biomarkers).length > 0) {
      window.labAnalyzer.currentValues = JSON.parse(JSON.stringify(entry.biomarkers));
    }

    const nameInput = document.getElementById("lab-patient-name");
    const ageInput = document.getElementById("lab-patient-age");
    const genderInput = document.getElementById("lab-patient-gender");

    if (nameInput) nameInput.value = window.labAnalyzer.patientName;
    if (ageInput) ageInput.value = window.labAnalyzer.patientAge;
    if (genderInput) genderInput.value = window.labAnalyzer.patientGender;

    window.labAnalyzer.reRenderCurrentResult();

    const resultCard = document.getElementById("lab-result-card");
    if (resultCard) {
      resultCard.style.display = "block";
      resultCard.scrollIntoView({ behavior: "smooth" });
    }
  }

  showTab("lab-analyzer");
  document.querySelector('.nav-btn[data-tab="lab-analyzer"]')?.click();
}

function deleteHistory(id) {
  if (confirm("Delete this scan record?")) {
    window.historyManager?.deleteImageById(id);
    renderHistoryTab();
  }
}

function deleteReportHistory(id) {
  if (confirm("Delete this report analysis record?")) {
    window.historyManager?.deleteReportById(id);
    renderHistoryTab();
  }
}

function clearAllHistory() {
  const subtab = window.historyManager?.activeSubTab || "image";
  const typeLabel = subtab === "image" ? "image scan history" : "report analysis history";
  if (confirm(`Clear all ${typeLabel}? This cannot be undone.`)) {
    if (subtab === "image") {
      window.historyManager?.clearImageHistory();
    } else {
      window.historyManager?.clearReportHistory();
    }
    renderHistoryTab();
  }
}

function exportHistoryPDF() {
  const subtab = window.historyManager?.activeSubTab || "image";
  
  if (subtab === "image") {
    const history = window.historyManager?.getImageHistory() || [];
    const printContent = `
      <html><head><title>HealLens Health Report — Image Scans</title>
      <style>
        body { font-family: Arial, sans-serif; padding: 20px; }
        h1 { color: #00D4FF; } h2 { color: #7C3AED; border-bottom: 1px solid #ccc; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th, td { padding: 8px 12px; border: 1px solid #ddd; text-align: left; }
        th { background: #f0f0f0; }
        .mild { color: green; } .moderate { color: orange; } .critical { color: red; }
      </style></head><body>
      <h1>🏥 HealLens Image Scan Report</h1>
      <p>Generated: ${new Date().toLocaleString("en-IN")}</p>
      <table>
        <tr><th>Date</th><th>Member</th><th>Condition</th><th>Body Part</th><th>Severity</th><th>AI %</th></tr>
        ${history.map(h => `
          <tr>
            <td>${window.historyManager.formatDate(h.timestamp)}</td>
            <td>${h.memberName}</td>
            <td>${h.diseaseName}</td>
            <td>${h.bodyPartLabel}</td>
            <td class="${h.severity}">${h.severity}</td>
            <td>${h.confidence}%</td>
          </tr>
        `).join("")}
      </table>
      </body></html>
    `;
    const win = window.open("", "_blank");
    win.document.documentElement.innerHTML = printContent;
    win.document.close();
    win.print();
  } else {
    const history = window.historyManager?.getReportHistory() || [];
    const printContent = `
      <html><head><title>HealLens Health Report — Lab Reports</title>
      <style>
        body { font-family: Arial, sans-serif; padding: 20px; }
        h1 { color: #00D4FF; } h2 { color: #7C3AED; border-bottom: 1px solid #ccc; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th, td { padding: 8px 12px; border: 1px solid #ddd; text-align: left; }
        th { background: #f0f0f0; }
        .normal, .mild { color: green; } .moderate { color: orange; } .critical { color: red; }
      </style></head><body>
      <h1>🏥 HealLens Lab Report Analysis Log</h1>
      <p>Generated: ${new Date().toLocaleString("en-IN")}</p>
      <table>
        <tr><th>Date</th><th>Patient</th><th>Report Name</th><th>Risk Profile Summary</th><th>Risk Level</th></tr>
        ${history.map(h => `
          <tr>
            <td>${window.historyManager.formatDate(h.timestamp)}</td>
            <td>${h.patientName || h.memberName}</td>
            <td>${h.reportName}</td>
            <td>${h.riskSummary}</td>
            <td class="${h.riskLevel}">${h.riskLevel}</td>
          </tr>
        `).join("")}
      </table>
      </body></html>
    `;
    const win = window.open("", "_blank");
    win.document.documentElement.innerHTML = printContent;
    win.document.close();
    win.print();
  }
}

window.switchHistorySubTab = switchHistorySubTab;
window.openHistoryWithSubTab = openHistoryWithSubTab;
window.viewReportHistoryDetail = viewReportHistoryDetail;
window.deleteReportHistory = deleteReportHistory;

// ─── Chatbot ─────────────────────────────────────────────────────────────────
function setupChatbot() {
  const form = document.getElementById("chat-form");
  const input = document.getElementById("chat-input");
  const messages = document.getElementById("chat-messages");

  if (!messages) return;

  // Show welcome message
  appendChatMessage("bot", window.i18n?.t("chatWelcome") || "Hello! How can I help you?");

  const handleSend = () => {
    const text = input?.value?.trim();
    if (!text) return;
    sendChatMessage(text);
    if (input) input.value = "";
  };

  if (form) {
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      handleSend();
    });
  }

  // Also listen for send button click specifically
  document.getElementById("chat-send-btn")?.addEventListener("click", handleSend);

  // Allow Enter key in input
  input?.addEventListener("keypress", (e) => {
    if (e.key === "Enter") handleSend();
  });


  // Chatbot voice input
  document.getElementById("chat-mic-btn")?.addEventListener("click", () => {
    window.voiceEngine?.toggleListening((transcript) => {
      if (input) input.value = transcript;
      sendChatMessage(transcript);
      input.value = "";
    });
  });
}

function sendChatMessage(text) {
  appendChatMessage("user", text);
  window.healthChatbot?.addToHistory("user", text);

  // Show typing indicator
  const typingId = "typing-" + Date.now();
  appendChatMessage("bot typing", "...", typingId);

  // Simulate AI thinking delay
  setTimeout(() => {
    const response = window.healthChatbot?.getResponse(text) || "I'm here to help with your health questions.";
    document.getElementById(typingId)?.remove();
    appendChatMessage("bot", response);
    window.healthChatbot?.addToHistory("bot", response);

    // TTS for bot response
    window.voiceEngine?.speak(response, window.i18n?.getVoiceLang());
  }, 800 + Math.random() * 700);
}

function appendChatMessage(role, text, id) {
  const messages = document.getElementById("chat-messages");
  if (!messages) return;

  const div = document.createElement("div");
  div.className = `chat-msg chat-msg-${role.split(" ")[0]}`;
  if (id) div.id = id;

  const time = new Date().toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit" });

  if (role.includes("typing")) {
    div.innerHTML = `<div class="chat-bubble"><span class="typing-dots"><span>.</span><span>.</span><span>.</span></span></div>`;
  } else {
    div.innerHTML = `
      <div class="chat-avatar">${role === "user" ? "👤" : "🤖"}</div>
      <div class="chat-bubble-wrap">
        <div class="chat-bubble">${text}</div>
        <div class="chat-time">${time}</div>
      </div>
    `;
  }

  messages.appendChild(div);
  messages.scrollTop = messages.scrollHeight;
}

// ─── SOS ─────────────────────────────────────────────────────────────────────
function renderSOSContacts() {
  const container = document.getElementById("sos-contacts-list");
  if (!container) return;

  const contacts = window.sosManager?.getContacts() || [];
  if (contacts.length === 0) {
    container.innerHTML = `<div class="empty-contacts"><p>No emergency contacts added yet.</p></div>`;
    return;
  }
  container.innerHTML = contacts.map(c => window.sosManager.renderContactCard(c)).join("");
}

function submitSOSContact() {
  const name = document.getElementById("sos-contact-name")?.value?.trim();
  const phone = document.getElementById("sos-contact-phone")?.value?.trim();
  const relation = document.getElementById("sos-contact-relation")?.value?.trim();

  if (!name || !phone) {
    alert("Please enter contact name and phone number.");
    return;
  }

  window.sosManager?.addContact({ name, phone, relation: relation || "Contact" });
  renderSOSContacts();

  // Reset form
  ["sos-contact-name", "sos-contact-phone", "sos-contact-relation"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });

  document.getElementById("sos-add-form")?.classList.remove("open");
}

function triggerManualSOS() {
  const reason = "Manual SOS triggered by user via HealLens app";
  window.sosManager?.triggerSOS(reason);
}

// ─── Profile ──────────────────────────────────────────────────────────────────
function renderProfile() {
  const container = document.getElementById("family-members-list");
  if (!container) return;

  const members = window.historyManager?.getFamilyMembers() || [];
  container.innerHTML = members.map(m => `
    <div class="family-card">
      <div class="family-avatar">${m.name.charAt(0).toUpperCase()}</div>
      <div class="family-info">
        <div class="family-name">${m.name}</div>
        <div class="family-meta">${m.relation}${m.age ? " · " + m.age + " yrs" : ""}</div>
      </div>
      ${m.id ? `<button class="family-delete-btn" onclick="deleteFamilyMember('${m.id}')">🗑️</button>` : ""}
    </div>
  `).join("");
}

function submitFamilyMember() {
  const name = document.getElementById("member-name")?.value?.trim();
  const age = document.getElementById("member-age")?.value?.trim();
  const relation = document.getElementById("member-relation")?.value?.trim();

  if (!name) { alert("Please enter member name."); return; }

  window.historyManager?.addFamilyMember({ name, age, relation: relation || "Family" });
  renderProfile();
  updateScannerMemberSelect();

  ["member-name", "member-age", "member-relation"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });
  document.getElementById("add-member-form")?.classList.remove("open");
}

function deleteFamilyMember(id) {
  if (confirm("Remove this family member?")) {
    window.historyManager?.deleteFamilyMember(id);
    renderProfile();
    updateScannerMemberSelect();
  }
}

function updateScannerMemberSelect() {
  const select = document.getElementById("scanner-member-select");
  if (!select) return;
  const members = window.historyManager?.getFamilyMembers() || [];
  select.innerHTML = members.map(m => `<option value="${m.name}">${m.name}</option>`).join("");
  select.addEventListener("change", () => {
    if (window.scanner) window.scanner.selectedMember = select.value;
  });
}

// ─── Ayurveda Body Map ────────────────────────────────────────────────────────
function setupAyurvedaMap() {
  const bodyParts = {
    head: {
      label: "Head & Brain",
      medicines: [
        { name: "Brahmi (Bacopa)", use: "Memory, anxiety, brain health", dosage: "500mg twice daily" },
        { name: "Shankhpushpi", use: "Mental clarity and stress relief", dosage: "1 tsp powder with milk" },
        { name: "Jatamansi", use: "Headache and insomnia", dosage: "250mg capsule twice daily" }
      ]
    },
    chest: {
      label: "Chest & Lungs",
      medicines: [
        { name: "Vasaka (Adhatoda)", use: "Cough, asthma, bronchitis", dosage: "Syrup 2 tsp 3 times daily" },
        { name: "Sitopaladi Churna", use: "Respiratory infections", dosage: "3g with honey twice daily" },
        { name: "Tulsi (Holy Basil)", use: "Immunity and respiratory health", dosage: "Tea made from 10 fresh leaves" }
      ]
    },
    stomach: {
      label: "Stomach & Digestion",
      medicines: [
        { name: "Triphala", use: "Digestion and detox", dosage: "1 tsp with warm water at night" },
        { name: "Hingvasthak Churna", use: "Bloating and gas", dosage: "3g before meals with ghee" },
        { name: "Avipattikar Churna", use: "Acidity and indigestion", dosage: "3g twice daily with water" }
      ]
    },
    joints: {
      label: "Joints & Bones",
      medicines: [
        { name: "Shallaki (Boswellia)", use: "Joint pain and arthritis", dosage: "400mg capsule twice daily" },
        { name: "Guggulu", use: "Anti-inflammatory for joints", dosage: "500mg tablet twice daily" },
        { name: "Laksha Guggulu", use: "Bone fractures and healing", dosage: "2 tablets with warm milk twice daily" }
      ]
    },
    skin: {
      label: "Skin & Blood",
      medicines: [
        { name: "Neem (Azadirachta)", use: "Skin infections, acne, blood purification", dosage: "Capsule 500mg twice daily" },
        { name: "Manjishtha", use: "Skin glow and blood detox", dosage: "1g powder with honey twice daily" },
        { name: "Khadirarishta", use: "Chronic skin disorders", dosage: "20ml with equal water twice daily" }
      ]
    },
    immunity: {
      label: "Immunity & Vitality",
      medicines: [
        { name: "Ashwagandha", use: "Stress, immunity, energy", dosage: "500mg capsule twice daily or 1 tsp powder with warm milk" },
        { name: "Chyawanprash", use: "Overall immunity booster", dosage: "1-2 tsp with warm milk every morning" },
        { name: "Giloy (Guduchi)", use: "Antiviral, liver, immunity", dosage: "Juice 20ml twice daily" }
      ]
    }
  };

  // Render body map tabs
  const tabsContainer = document.getElementById("body-map-tabs");
  const infoContainer = document.getElementById("body-map-info");

  if (!tabsContainer || !infoContainer) return;

  const icons = { head: "🧠", chest: "🫁", stomach: "🫃", joints: "🦴", skin: "🧴", immunity: "🛡️" };

  tabsContainer.innerHTML = Object.keys(bodyParts).map(part => `
    <button class="body-map-tab" data-part="${part}" onclick="showBodyPart('${part}')">
      ${icons[part]} ${bodyParts[part].label}
    </button>
  `).join("");

  window._bodyPartsData = bodyParts;
  showBodyPart("chest");
}

function showBodyPart(part) {
  const data = window._bodyPartsData?.[part];
  if (!data) return;

  document.querySelectorAll(".body-map-tab").forEach(b => b.classList.remove("active"));
  document.querySelector(`.body-map-tab[data-part="${part}"]`)?.classList.add("active");

  const info = document.getElementById("body-map-info");
  if (!info) return;

  info.innerHTML = `
    <h3 class="body-map-title">${data.label} — Ayurvedic Medicines</h3>
    <div class="ayurveda-map-grid">
      ${data.medicines.map(m => `
        <div class="ayurveda-map-card">
          <div class="ayurveda-map-name">🌿 ${m.name}</div>
          <div class="ayurveda-map-use"><strong>Use:</strong> ${m.use}</div>
          <div class="ayurveda-map-dosage"><strong>Dosage:</strong> ${m.dosage}</div>
        </div>
      `).join("")}
    </div>
  `;
}

// ─── History filter bindings ──────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("history-filter-member")?.addEventListener("change", renderHistoryTab);
  document.getElementById("history-filter-severity")?.addEventListener("change", renderHistoryTab);
  updateScannerMemberSelect();
});
