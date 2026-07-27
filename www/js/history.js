// history.js — LocalStorage-based scan history manager with window.name backup fallback
class HistoryManager {
  constructor() {
    this.STORAGE_KEY = "heallens_history"; // Image history key for backward compatibility
    this.REPORT_KEY = "heallens_report_history"; // Report history key
    this.FAMILY_KEY = "heallens_family";
    this.activeSubTab = null; // null until user selects 'image' or 'report'
  }

  // Smart backup helpers to persist data on file:// refreshes
  _backupGet(key) {
    try {
      if (!window.name) return null;
      const backup = JSON.parse(window.name);
      return backup[key] || null;
    } catch {
      return null;
    }
  }

  _backupSet(key, value) {
    try {
      let backup = {};
      if (window.name) {
        try {
          backup = JSON.parse(window.name);
        } catch {}
      }
      if (typeof backup !== "object" || backup === null) {
        backup = {};
      }
      backup[key] = value;
      window.name = JSON.stringify(backup);
    } catch {}
  }

  // ─── Image Analysis History ───────────────────────────────────────────────
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
      history = [entry];
      try {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(history));
      } catch (err) {}
    }
    
    this._backupSet(this.STORAGE_KEY, history);
    return entry;
  }

  getAll() {
    try {
      let data = localStorage.getItem(this.STORAGE_KEY);
      if (!data) {
        const backup = this._backupGet(this.STORAGE_KEY);
        if (backup) {
          localStorage.setItem(this.STORAGE_KEY, JSON.stringify(backup));
          return backup;
        }
        return [];
      }
      const parsed = JSON.parse(data) || [];
      this._backupSet(this.STORAGE_KEY, parsed);
      return parsed;
    } catch {
      const backup = this._backupGet(this.STORAGE_KEY);
      return backup || [];
    }
  }

  getImageHistory() {
    return this.getAll();
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
    this._backupSet(this.STORAGE_KEY, history);
  }

  deleteImageById(id) {
    this.deleteById(id);
  }

  clearAll() {
    localStorage.removeItem(this.STORAGE_KEY);
    this._backupSet(this.STORAGE_KEY, null);
  }

  clearImageHistory() {
    this.clearAll();
  }

  // ─── Report Analysis History ──────────────────────────────────────────────
  addReport(reportData) {
    let history = this.getReportHistory();

    const entry = {
      id: Date.now().toString(),
      timestamp: reportData.timestamp || new Date().toISOString(),
      memberName: reportData.memberName || reportData.patientName || "Self",
      patientName: reportData.patientName || "Jane Doe",
      patientAge: reportData.patientAge || 30,
      patientGender: reportData.patientGender || "female",
      reportName: reportData.reportName || "Blood Biomarker Report",
      riskSummary: reportData.riskSummary || "Analysis Complete",
      riskLevel: reportData.riskLevel || "normal", // 'normal', 'mild', 'moderate', 'critical'
      biomarkers: reportData.biomarkers || {}
    };

    history.unshift(entry); // newest first
    if (history.length > 20) history.splice(20);

    try {
      localStorage.setItem(this.REPORT_KEY, JSON.stringify(history));
    } catch (e) {
      console.warn("Report history quota exceeded, clearing and saving latest.");
      history = [entry];
      try {
        localStorage.setItem(this.REPORT_KEY, JSON.stringify(history));
      } catch (err) {}
    }

    this._backupSet(this.REPORT_KEY, history);
    return entry;
  }

  getReportHistory() {
    try {
      let data = localStorage.getItem(this.REPORT_KEY);
      if (!data) {
        const backup = this._backupGet(this.REPORT_KEY);
        if (backup) {
          localStorage.setItem(this.REPORT_KEY, JSON.stringify(backup));
          return backup;
        }
        return [];
      }
      const parsed = JSON.parse(data) || [];
      this._backupSet(this.REPORT_KEY, parsed);
      return parsed;
    } catch {
      const backup = this._backupGet(this.REPORT_KEY);
      return backup || [];
    }
  }

  deleteReportById(id) {
    const history = this.getReportHistory().filter(h => h.id !== id);
    localStorage.setItem(this.REPORT_KEY, JSON.stringify(history));
    this._backupSet(this.REPORT_KEY, history);
  }

  clearReportHistory() {
    localStorage.removeItem(this.REPORT_KEY);
    this._backupSet(this.REPORT_KEY, null);
  }

  // Family members
  getFamilyMembers() {
    try {
      let data = localStorage.getItem(this.FAMILY_KEY);
      if (!data) {
        const backup = this._backupGet(this.FAMILY_KEY);
        if (backup) {
          localStorage.setItem(this.FAMILY_KEY, JSON.stringify(backup));
          return backup;
        }
        return [{ name: "Self", age: "", relation: "Self" }];
      }
      const parsed = JSON.parse(data) || [{ name: "Self", age: "", relation: "Self" }];
      this._backupSet(this.FAMILY_KEY, parsed);
      return parsed;
    } catch {
      const backup = this._backupGet(this.FAMILY_KEY);
      return backup || [{ name: "Self", age: "", relation: "Self" }];
    }
  }

  addFamilyMember(member) {
    const members = this.getFamilyMembers();
    member.id = Date.now().toString();
    members.push(member);
    localStorage.setItem(this.FAMILY_KEY, JSON.stringify(members));
    this._backupSet(this.FAMILY_KEY, members);
    return member;
  }

  deleteFamilyMember(id) {
    const members = this.getFamilyMembers().filter(m => m.id !== id);
    localStorage.setItem(this.FAMILY_KEY, JSON.stringify(members));
    this._backupSet(this.FAMILY_KEY, members);
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
  renderCard(entry, type = "image") {
    if (type === "report") {
      return this.renderReportCard(entry);
    }
    return this.renderImageCard(entry);
  }

  renderImageCard(entry) {
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

  renderReportCard(entry) {
    const severityClass = {
      normal: "severity-mild",
      mild: "severity-mild",
      moderate: "severity-moderate",
      critical: "severity-critical"
    };
    const severityLabel = {
      normal: window.i18n?.t("statusNormal") || "Normal",
      mild: window.i18n?.t("severe_mild") || "Mild",
      moderate: window.i18n?.t("severe_moderate") || "Moderate",
      critical: window.i18n?.t("severe_critical") || "Critical"
    };
    
    return `
      <div class="history-card history-card-report" data-id="${entry.id}">
        <div class="history-card-header">
          <div class="history-thumb">
            <div class="history-thumb-placeholder">📊</div>
          </div>
          <div class="history-info">
            <div class="history-member">
              <span class="member-icon">👤</span> ${entry.memberName || entry.patientName || 'Self'}
              <span class="history-date">${this.formatDate(entry.timestamp)}</span>
            </div>
            <div class="history-disease">${entry.reportName || 'Blood Biomarker Report'}</div>
            <div class="history-body-part">📋 ${entry.riskSummary || 'Lab Analysis Complete'}</div>
          </div>
          <div class="history-card-meta">
            <span class="severity-badge ${severityClass[entry.riskLevel] || 'severity-mild'}">
              ${severityLabel[entry.riskLevel] || entry.riskLevel || 'Normal'}
            </span>
            <span class="confidence-badge">Report AI</span>
          </div>
        </div>
        <div class="history-card-actions">
          <button class="btn-history-view" onclick="viewReportHistoryDetail('${entry.id}')">View Details</button>
          <button class="btn-history-delete" onclick="deleteReportHistory('${entry.id}')">🗑️</button>
        </div>
      </div>
    `;
  }
}

window.historyManager = new HistoryManager();
