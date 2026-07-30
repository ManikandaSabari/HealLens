// history.js — Cloud-First scan and report history manager with Supabase database & local cache fallback

class HistoryManager {
  constructor() {
    this.STORAGE_KEY = "heallens_history"; // Image history key
    this.REPORT_KEY = "heallens_report_history"; // Report history key
    this.FAMILY_KEY = "heallens_family";
    this.activeSubTab = null; // null until user selects 'image' or 'report'
    this.remoteRecords = null;
  }

  _getSupabaseClient() {
    if (typeof window !== "undefined" && window.supabaseClient) {
      return window.supabaseClient;
    }
    if (typeof window !== "undefined" && window.supabase && typeof window.supabase.createClient === 'function') {
      const url = window.SUPABASE_URL || "https://sqitixlmrrksqdqqboyp.supabase.co";
      const key = window.SUPABASE_ANON_KEY || "sb_publishable_9KGmq4RT3sRuoPGfy5gsHg_l4lShf3O";
      window.supabaseClient = window.supabase.createClient(url, key);
      return window.supabaseClient;
    }
    return null;
  }

  async _getUserId() {
    if (typeof window !== "undefined" && window.currentUser && window.currentUser.id) {
      return window.currentUser.id;
    }
    const client = this._getSupabaseClient();
    if (client) {
      try {
        const { data: { session } } = await client.auth.getSession();
        if (session && session.user) {
          if (typeof window !== "undefined" && !window.currentUser) {
            window.currentUser = session.user;
          }
          return session.user.id;
        }
      } catch (e) {}
    }
    return null;
  }

  // Backup helpers for local storage
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
        try { backup = JSON.parse(window.name); } catch {}
      }
      if (typeof backup !== "object" || backup === null) backup = {};
      backup[key] = value;
      window.name = JSON.stringify(backup);
    } catch {}
  }

  _getRawHistory() {
    try {
      let data = localStorage.getItem(this.STORAGE_KEY);
      if (!data) return [];
      return JSON.parse(data) || [];
    } catch {
      return [];
    }
  }

  _getRawReports() {
    try {
      let data = localStorage.getItem(this.REPORT_KEY);
      if (!data) return [];
      return JSON.parse(data) || [];
    } catch {
      return [];
    }
  }

  // ─── Image Analysis History ───────────────────────────────────────────────
  async addScan(result, memberName, imageSrc) {
    const currentUserId = await this._getUserId();

    let safeImageSrc = imageSrc;
    if (imageSrc && imageSrc.length > 50000) {
      safeImageSrc = null;
    }

    const entry = {
      id: Date.now().toString(),
      userId: currentUserId,
      user_id: currentUserId,
      timestamp: result.timestamp || new Date().toISOString(),
      memberName: memberName || result.patientName || "Self",
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

    // 1. Primary: Save directly to Supabase Cloud Database
    const client = this._getSupabaseClient();
    if (client && currentUserId) {
      try {
        const dbPayload = {
          user_id: currentUserId,
          Name: memberName || result.patientName || "Self",
          Age: result.patientAge ? Number(result.patientAge) : 30,
          Gender: result.patientGender || "Unknown",
          analysis_type: "Image",
          Category: result.bodyPartLabel || result.bodyPart || "General",
          Prediction: result.diseaseName || "Scan Completed",
          Confidence: result.confidence ? String(result.confidence) : "90",
          Severity: result.severity || "mild",
          Symptoms: result.description || "",
          Recommendations: Array.isArray(result.remedies) ? result.remedies.join("; ") : (result.remedies || "")
        };
        const { data } = await client.from("clinical_records").insert([dbPayload]).select();
        if (data && data[0]) {
          entry.id = data[0].id.toString();
        }
      } catch (err) {
        console.warn("⚠️ Direct Supabase addScan insert failed:", err.message);
      }
    }

    // 2. Update local cache
    let rawHistory = this._getRawHistory().filter(item => !currentUserId || item.userId === currentUserId || item.user_id === currentUserId);
    rawHistory.unshift(entry);
    if (rawHistory.length > 50) rawHistory.splice(50);
    try { localStorage.setItem(this.STORAGE_KEY, JSON.stringify(rawHistory)); } catch (e) {}

    await this.fetchRemoteHistory();
    return entry;
  }

  async fetchRemoteHistory() {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();

    if (client && currentUserId) {
      try {
        const { data, error } = await client
          .from("clinical_records")
          .select("*")
          .eq("user_id", currentUserId)
          .order("created_at", { ascending: false });

        if (!error && Array.isArray(data)) {
          this.remoteRecords = data;
          try {
            localStorage.setItem(this.STORAGE_KEY + "_" + currentUserId, JSON.stringify(data));
          } catch (e) {}
          return data;
        }
      } catch (err) {
        console.warn("⚠️ Direct Supabase fetch query error:", err.message);
      }
    }

    return null;
  }

  getAll() {
    const currentUserId = (window.currentUser && window.currentUser.id) ? window.currentUser.id : null;
    let parsed = this._getRawHistory();
    if (currentUserId) {
      return parsed.filter(item => (!item.userId && !item.user_id) || item.userId === currentUserId || item.user_id === currentUserId);
    }
    return parsed;
  }

  getImageHistory() {
    const currentUserId = (window.currentUser && window.currentUser.id) ? window.currentUser.id : null;

    if (this.remoteRecords && Array.isArray(this.remoteRecords)) {
      let imageRecords = this.remoteRecords.filter(r => 
        (r.analysis_type === "Image" || r.analysis_type === "Image Analysis") &&
        (!currentUserId || r.user_id === currentUserId || r.userId === currentUserId)
      );
      return imageRecords.map(r => ({
        id: r.id ? r.id.toString() : Date.now().toString(),
        userId: r.user_id || r.userId || currentUserId,
        user_id: r.user_id || r.userId || currentUserId,
        timestamp: r.created_at || new Date().toISOString(),
        memberName: r.Name || r.patient_name || "Self",
        patientName: r.Name || r.patient_name || "Self",
        patientAge: r.Age || r.age || 30,
        patientGender: r.Gender || r.gender || "Unknown",
        bodyPartLabel: r.Category || r.category || "General",
        bodyPart: r.Category || r.category || "General",
        diseaseName: r.Prediction || r.prediction || "Scan Completed",
        severity: r.Severity || r.severity || "mild",
        confidence: r.Confidence || r.confidence ? String(r.Confidence || r.confidence) : "90",
        description: r.Symptoms || r.symptoms || "",
        remedies: r.Recommendations || r.recommendation ? [r.Recommendations || r.recommendation] : []
      }));
    }
    return this.getAll();
  }

  async deleteById(id) {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();

    if (client && currentUserId) {
      try {
        await client.from("clinical_records").delete().eq("id", id).eq("user_id", currentUserId);
      } catch (e) {}
    }

    const rawHistory = this._getRawHistory().filter(h => h.id !== id);
    try { localStorage.setItem(this.STORAGE_KEY, JSON.stringify(rawHistory)); } catch (e) {}
    await this.fetchRemoteHistory();
  }

  deleteImageById(id) {
    this.deleteById(id);
  }

  async clearAll() {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();

    if (client && currentUserId) {
      try {
        await client.from("clinical_records").delete().eq("user_id", currentUserId).eq("analysis_type", "Image");
      } catch (e) {}
    }

    try { localStorage.removeItem(this.STORAGE_KEY); } catch (e) {}
    await this.fetchRemoteHistory();
  }

  clearImageHistory() {
    this.clearAll();
  }

  // ─── Report Analysis History ──────────────────────────────────────────────
  async addReport(reportData) {
    const currentUserId = await this._getUserId();

    const entry = {
      id: Date.now().toString(),
      userId: currentUserId,
      user_id: currentUserId,
      timestamp: reportData.timestamp || new Date().toISOString(),
      memberName: reportData.memberName || reportData.patientName || "Self",
      patientName: reportData.patientName || "Jane Doe",
      patientAge: reportData.patientAge || 30,
      patientGender: reportData.patientGender || "female",
      reportName: reportData.reportName || "Blood Biomarker Report",
      riskSummary: reportData.riskSummary || "Analysis Complete",
      riskLevel: reportData.riskLevel || "normal",
      biomarkers: reportData.biomarkers || {}
    };

    const client = this._getSupabaseClient();
    if (client && currentUserId) {
      try {
        const dbPayload = {
          user_id: currentUserId,
          Name: reportData.patientName || reportData.memberName || "Self",
          Age: reportData.patientAge ? Number(reportData.patientAge) : 30,
          Gender: reportData.patientGender || "Unknown",
          analysis_type: "Report",
          Category: reportData.reportName || "Blood Biomarker Report",
          Prediction: reportData.riskSummary || "Report Analysis Complete",
          Confidence: "100",
          Severity: reportData.riskLevel || "normal",
          Symptoms: "",
          Recommendations: ""
        };
        const { data } = await client.from("clinical_records").insert([dbPayload]).select();
        if (data && data[0]) {
          entry.id = data[0].id.toString();
        }
      } catch (e) {}
    }



    let rawHistory = this._getRawReports().filter(item => !currentUserId || item.userId === currentUserId || item.user_id === currentUserId);
    rawHistory.unshift(entry);
    if (rawHistory.length > 50) rawHistory.splice(50);
    try { localStorage.setItem(this.REPORT_KEY, JSON.stringify(rawHistory)); } catch (e) {}

    await this.fetchRemoteHistory();
    return entry;
  }

  getReportHistory() {
    const currentUserId = (window.currentUser && window.currentUser.id) ? window.currentUser.id : null;

    if (this.remoteRecords && Array.isArray(this.remoteRecords)) {
      let reportRecords = this.remoteRecords.filter(r => 
        (r.analysis_type === "Report" || r.analysis_type === "Report Analysis") &&
        (!currentUserId || r.user_id === currentUserId || r.userId === currentUserId)
      );
      return reportRecords.map(r => ({
        id: r.id ? r.id.toString() : Date.now().toString(),
        userId: r.user_id || r.userId || currentUserId,
        user_id: r.user_id || r.userId || currentUserId,
        timestamp: r.created_at || new Date().toISOString(),
        memberName: r.Name || r.patient_name || "Self",
        patientName: r.Name || r.patient_name || "Jane Doe",
        patientAge: r.Age || r.age || 30,
        patientGender: r.Gender || r.gender || "female",
        reportName: r.Category || r.category || "Blood Biomarker Report",
        riskSummary: r.Prediction || r.prediction || "Analysis Complete",
        riskLevel: r.Severity || r.severity || "normal"
      }));
    }
    const rawReports = this._getRawReports();
    if (currentUserId) {
      return rawReports.filter(item => (!item.userId && !item.user_id) || item.userId === currentUserId || item.user_id === currentUserId);
    }
    return rawReports;
  }

  async deleteReportById(id) {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();
    if (client && currentUserId) {
      try {
        await client.from("clinical_records").delete().eq("id", id).eq("user_id", currentUserId);
      } catch (e) {}
    }
    const rawHistory = this._getRawReports().filter(h => h.id !== id);
    try { localStorage.setItem(this.REPORT_KEY, JSON.stringify(rawHistory)); } catch (e) {}
    await this.fetchRemoteHistory();
  }

  async clearReportHistory() {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();
    if (client && currentUserId) {
      try {
        await client.from("clinical_records").delete().eq("user_id", currentUserId).eq("analysis_type", "Report");
      } catch (e) {}
    }
    try { localStorage.removeItem(this.REPORT_KEY); } catch (e) {}
    await this.fetchRemoteHistory();
  }

  // Family members
  getFamilyMembers() {
    try {
      let data = localStorage.getItem(this.FAMILY_KEY);
      if (!data) return [{ name: "Self", age: "", relation: "Self" }];
      return JSON.parse(data) || [{ name: "Self", age: "", relation: "Self" }];
    } catch {
      return [{ name: "Self", age: "", relation: "Self" }];
    }
  }

  addFamilyMember(member) {
    const members = this.getFamilyMembers();
    member.id = Date.now().toString();
    members.push(member);
    try { localStorage.setItem(this.FAMILY_KEY, JSON.stringify(members)); } catch (e) {}
    return member;
  }

  deleteFamilyMember(id) {
    const members = this.getFamilyMembers().filter(m => m.id !== id);
    try { localStorage.setItem(this.FAMILY_KEY, JSON.stringify(members)); } catch (e) {}
  }

  // Helpers
  getByMember(memberName) {
    return this.getAll().filter(h => h.memberName === memberName);
  }

  getBySeverity(severity) {
    return this.getAll().filter(h => h.severity === severity);
  }

  formatDate(isoString) {
    try {
      const d = new Date(isoString);
      return d.toLocaleDateString("en-IN", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit"
      });
    } catch { return isoString; }
  }

  renderCard(entry, type = "image") {
    if (type === "report") return this.renderReportCard(entry);
    return this.renderImageCard(entry);
  }

  renderImageCard(entry) {
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
