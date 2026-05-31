// history.js — LocalStorage-based scan history manager
class HistoryManager {
  constructor() {
    this.STORAGE_KEY = "heallens_history";
    this.FAMILY_KEY = "heallens_family";
  }

  addScan(result, memberName, imageSrc) {
    let history = this.getAll();
    
    // Prevent localStorage 5MB quota error by not saving massive base64 images
    let safeImageSrc = imageSrc;
    if (imageSrc && imageSrc.length > 50000) {
      safeImageSrc = null; // Image too large, don't store in history
    }

    const entry = {
      id: Date.now().toString(),
      timestamp: result.timestamp || new Date().toISOString(),
      memberName: memberName || "Self",
      imageSrc: safeImageSrc,
      bodyPart: result.bodyPart,
      bodyPartLabel: result.bodyPartLabel,
      diseaseName: result.diseaseName,
      severity: result.severity,
      confidence: result.confidence,
      description: result.description,
      remedies: result.remedies,
      ayurveda: result.ayurveda,
      doctorType: result.doctorType,
      emergencyThreshold: result.emergencyThreshold
    };
    
    history.unshift(entry); // newest first
    if (history.length > 20) history.splice(20); // Keep max 20
    
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(history));
    } catch (e) {
      console.warn("Storage quota exceeded, clearing history and trying again.");
      // Fallback: if localStorage is full, clear it and store only the newest item
      history = [entry];
      try {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(history));
      } catch (err) {}
    }
    
    return entry;
  }

  getAll() {
    try {
      return JSON.parse(localStorage.getItem(this.STORAGE_KEY)) || [];
    } catch { return []; }
  }

  getByMember(memberName) {
    return this.getAll().filter(h => h.memberName === memberName);
  }

  getBySeverity(severity) {
    return this.getAll().filter(h => h.severity === severity);
  }

  deleteById(id) {
    const history = this.getAll().filter(h => h.id !== id);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(history));
  }

  clearAll() {
    localStorage.removeItem(this.STORAGE_KEY);
  }

  // Family members
  getFamilyMembers() {
    try {
      return JSON.parse(localStorage.getItem(this.FAMILY_KEY)) || [{ name: "Self", age: "", relation: "Self" }];
    } catch { return [{ name: "Self", age: "", relation: "Self" }]; }
  }

  addFamilyMember(member) {
    const members = this.getFamilyMembers();
    member.id = Date.now().toString();
    members.push(member);
    localStorage.setItem(this.FAMILY_KEY, JSON.stringify(members));
    return member;
  }

  deleteFamilyMember(id) {
    const members = this.getFamilyMembers().filter(m => m.id !== id);
    localStorage.setItem(this.FAMILY_KEY, JSON.stringify(members));
  }

  // Format date for display
  formatDate(isoString) {
    try {
      const d = new Date(isoString);
      return d.toLocaleDateString("en-IN", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit"
      });
    } catch { return isoString; }
  }

  // Render history card HTML
  renderCard(entry) {
    const lang = window.i18n?.currentLang || "en";
    const severityClass = { mild: "severity-mild", moderate: "severity-moderate", critical: "severity-critical" };
    const severityLabel = {
      mild: window.i18n?.t("severe_mild") || "Mild",
      moderate: window.i18n?.t("severe_moderate") || "Moderate",
      critical: window.i18n?.t("severe_critical") || "Critical"
    };
    const bodyIcon = { chest: "🫁", bone: "🦴", skin: "🧴" };

    return `
      <div class="history-card" data-id="${entry.id}">
        <div class="history-card-header">
          <div class="history-thumb">
            ${entry.imageSrc
              ? `<img src="${entry.imageSrc}" alt="scan" />`
              : `<div class="history-thumb-placeholder">${bodyIcon[entry.bodyPart] || "🔬"}</div>`
            }
          </div>
          <div class="history-info">
            <div class="history-member">
              <span class="member-icon">👤</span> ${entry.memberName}
              <span class="history-date">${this.formatDate(entry.timestamp)}</span>
            </div>
            <div class="history-disease">${entry.diseaseName}</div>
            <div class="history-body-part">${bodyIcon[entry.bodyPart] || ""} ${entry.bodyPartLabel}</div>
          </div>
          <div class="history-card-meta">
            <span class="severity-badge ${severityClass[entry.severity] || ''}">
              ${severityLabel[entry.severity] || entry.severity}
            </span>
            <span class="confidence-badge">${entry.confidence}% AI</span>
          </div>
        </div>
        <div class="history-card-actions">
          <button class="btn-history-view" onclick="viewHistoryDetail('${entry.id}')">View Details</button>
          <button class="btn-history-delete" onclick="deleteHistory('${entry.id}')">🗑️</button>
        </div>
      </div>
    `;
  }
}

window.historyManager = new HistoryManager();
