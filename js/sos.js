// sos.js — Cloud-First Emergency SOS Feature with Supabase database & local cache fallback

class SOSManager {
  constructor() {
    this.STORAGE_KEY = "heallens_sos_contacts";
    this.isActivated = false;
    this.remoteContacts = null;
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

  _getAllRawContacts() {
    try {
      let data = localStorage.getItem(this.STORAGE_KEY);
      if (!data) return [];
      return JSON.parse(data) || [];
    } catch {
      return [];
    }
  }

  _saveAllRawContacts(contacts) {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(contacts));
    } catch (e) {}
    this._backupSet(this.STORAGE_KEY, contacts);
  }

  async fetchRemoteContacts() {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();

    if (client && currentUserId) {
      try {
        const { data, error } = await client
          .from("emergency_contacts")
          .select("*")
          .eq("user_id", currentUserId)
          .order("created_at", { ascending: false });

        if (!error && Array.isArray(data)) {
          const formatted = data.map(c => ({
            id: c.id ? c.id.toString() : Date.now().toString(),
            name: c.Contact_Name || c.name || c.patient_name || "Emergency Contact",
            phone: c.Phone_Number || c.phone || "",
            relation: c.Relationship || c.relation || "Contact",
            userId: c.user_id || currentUserId,
            user_id: c.user_id || currentUserId
          }));
          this.remoteContacts = formatted;
          this._saveAllRawContacts(formatted);
          return formatted;
        }
      } catch (err) {
        console.warn("⚠️ Direct Supabase contacts query failed:", err.message);
      }
    }

    return null;
  }

  getContacts() {
    const currentUserId = (window.currentUser && window.currentUser.id) ? window.currentUser.id : null;
    if (this.remoteContacts && Array.isArray(this.remoteContacts)) {
      if (currentUserId) {
        return this.remoteContacts.filter(c => (!c.userId && !c.user_id) || c.userId === currentUserId || c.user_id === currentUserId);
      }
      return this.remoteContacts;
    }
    const allContacts = this._getAllRawContacts();
    if (currentUserId) {
      return allContacts.filter(c => (!c.userId && !c.user_id) || c.userId === currentUserId || c.user_id === currentUserId);
    }
    return allContacts;
  }

  async addContact(contact) {
    const currentUserId = await this._getUserId();
    const userContacts = this.getContacts();
    if (userContacts.length >= 3) {
      alert("Maximum 3 emergency contacts allowed.");
      return false;
    }

    contact.id = Date.now().toString();
    contact.userId = currentUserId;
    contact.user_id = currentUserId;

    // 1. Primary: Save directly to Supabase Cloud Database
    const client = this._getSupabaseClient();
    if (client && currentUserId) {
      try {
        const dbPayload = {
          user_id: currentUserId,
          Patient_Name: (window.currentUser && window.currentUser.full_name) || "Self",
          Contact_Name: contact.name,
          Relationship: contact.relation || "Contact",
          Phone_Number: contact.phone
        };
        const { data, error } = await client.from("emergency_contacts").insert([dbPayload]).select();
        if (data && data[0]) {
          contact.id = data[0].id.toString();
        }
      } catch (err) {
        console.warn("⚠️ Direct Supabase addContact insert failed:", err.message);
      }
    }



    await this.fetchRemoteContacts();
    return contact;
  }

  async updateContact(id, updatedFields) {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();

    if (client && currentUserId) {
      try {
        const dbPayload = {};
        if (updatedFields.name) dbPayload.Contact_Name = updatedFields.name;
        if (updatedFields.relation) dbPayload.Relationship = updatedFields.relation;
        if (updatedFields.phone) dbPayload.Phone_Number = updatedFields.phone;

        await client.from("emergency_contacts").update(dbPayload).eq("id", id).eq("user_id", currentUserId);
      } catch (err) {
        console.warn("⚠️ Direct Supabase updateContact failed:", err.message);
      }
    }



    await this.fetchRemoteContacts();
  }

  async deleteContact(id) {
    const currentUserId = await this._getUserId();
    const client = this._getSupabaseClient();

    if (client && currentUserId) {
      try {
        await client.from("emergency_contacts").delete().eq("id", id).eq("user_id", currentUserId);
      } catch (err) {
        console.warn("⚠️ Direct Supabase deleteContact failed:", err.message);
      }
    }



    await this.fetchRemoteContacts();
  }

  // Trigger SOS alert
  async triggerSOS(reason) {
    const contacts = this.getContacts();
    this.isActivated = true;

    this.showSOSOverlay(reason);

    if (contacts.length > 0) {
      const firstContact = contacts[0];
      setTimeout(() => {
        if (confirm(`Call ${firstContact.name} (${firstContact.phone}) now?`)) {
          window.location.href = `tel:${firstContact.phone}`;
        }
      }, 1500);
    }

    const emergencyMsg = this.buildEmergencyMessage(reason, contacts);
    try {
      await navigator.clipboard.writeText(emergencyMsg);
    } catch (e) {}

    const historyEntry = {
      timestamp: new Date().toISOString(),
      reason,
      contacts: contacts.map(c => c.name)
    };
    try {
      const log = JSON.parse(localStorage.getItem("heallens_sos_log") || "[]");
      log.unshift(historyEntry);
      localStorage.setItem("heallens_sos_log", JSON.stringify(log.slice(0, 10)));
    } catch (e) {}
  }

  buildEmergencyMessage(reason, contacts) {
    const now = new Date().toLocaleString("en-IN");
    return `🚨 HEALLENS EMERGENCY ALERT 🚨
Time: ${now}
Condition: ${reason || "Medical emergency detected by HealLens AI"}
Please provide immediate assistance.

Emergency Contacts:
${contacts.map((c, i) => `${i + 1}. ${c.name} (${c.relation}): ${c.phone}`).join('\n')}

Emergency Numbers: Ambulance: 108 | Emergency: 112 | Health: 104`;
  }

  showSOSOverlay(reason) {
    document.getElementById("sos-overlay")?.remove();

    const overlay = document.createElement("div");
    overlay.id = "sos-overlay";
    overlay.className = "sos-overlay";
    overlay.innerHTML = `
      <div class="sos-overlay-content">
        <div class="sos-pulse-ring"></div>
        <div class="sos-icon">🚨</div>
        <h2 class="sos-overlay-title">SOS ACTIVATED</h2>
        <p class="sos-overlay-msg">${reason || "Emergency alert triggered"}</p>
        <div class="sos-emergency-numbers">
          <a href="tel:108" class="sos-call-btn">📞 Ambulance: 108</a>
          <a href="tel:112" class="sos-call-btn">📞 Emergency: 112</a>
          <a href="tel:104" class="sos-call-btn">📞 Health: 104</a>
        </div>
        <button class="sos-dismiss-btn" onclick="document.getElementById('sos-overlay').remove()">
          Dismiss Alert
        </button>
      </div>
    `;
    document.body.appendChild(overlay);

    setTimeout(() => {
      overlay.remove();
      this.isActivated = false;
    }, 30000);
  }

  autoTriggerIfCritical(scanResult) {
    if (scanResult.emergencyThreshold || scanResult.severity === "critical") {
      const contacts = this.getContacts();
      if (contacts.length > 0) {
        setTimeout(() => {
          const confirmSOS = confirm(
            `⚠️ Critical condition detected: ${scanResult.diseaseName}\n\nDo you want to trigger an SOS alert to your emergency contacts?`
          );
          if (confirmSOS) {
            this.triggerSOS(`Critical AI detection: ${scanResult.diseaseName}`);
          }
        }, 1000);
      }
    }
  }

  renderContactCard(contact) {
    return `
      <div class="sos-contact-card" data-id="${contact.id}">
        <div class="sos-contact-avatar">${(contact.name || "E").charAt(0).toUpperCase()}</div>
        <div class="sos-contact-info">
          <div class="sos-contact-name">${contact.name}</div>
          <div class="sos-contact-relation">${contact.relation}</div>
          <div class="sos-contact-phone">📞 ${contact.phone}</div>
        </div>
        <div class="sos-contact-actions">
          <a href="tel:${contact.phone}" class="sos-call-icon" title="Call">📞</a>
          <button class="sos-delete-btn" onclick="sosManager.deleteContact('${contact.id}').then(() => renderSOSContacts())">🗑️</button>
        </div>
      </div>
    `;
  }
}

window.sosManager = new SOSManager();
