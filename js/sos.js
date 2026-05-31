// sos.js — Emergency SOS Feature
class SOSManager {
  constructor() {
    this.STORAGE_KEY = "heallens_sos_contacts";
    this.isActivated = false;
  }

  getContacts() {
    try {
      return JSON.parse(localStorage.getItem(this.STORAGE_KEY)) || [];
    } catch { return []; }
  }

  addContact(contact) {
    const contacts = this.getContacts();
    if (contacts.length >= 3) {
      alert("Maximum 3 emergency contacts allowed.");
      return false;
    }
    contact.id = Date.now().toString();
    contacts.push(contact);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(contacts));
    return contact;
  }

  deleteContact(id) {
    const contacts = this.getContacts().filter(c => c.id !== id);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(contacts));
  }

  // Trigger SOS alert
  async triggerSOS(reason) {
    const contacts = this.getContacts();
    this.isActivated = true;

    // Show SOS overlay
    this.showSOSOverlay(reason);

    // Attempt to call first contact
    if (contacts.length > 0) {
      const firstContact = contacts[0];
      setTimeout(() => {
        if (confirm(`Call ${firstContact.name} (${firstContact.phone}) now?`)) {
          window.location.href = `tel:${firstContact.phone}`;
        }
      }, 1500);
    }

    // Copy emergency info to clipboard
    const emergencyMsg = this.buildEmergencyMessage(reason, contacts);
    try {
      await navigator.clipboard.writeText(emergencyMsg);
    } catch (e) {
      console.warn("Could not copy to clipboard");
    }

    // Log to history
    const historyEntry = {
      timestamp: new Date().toISOString(),
      reason,
      contacts: contacts.map(c => c.name)
    };
    const log = JSON.parse(localStorage.getItem("heallens_sos_log") || "[]");
    log.unshift(historyEntry);
    localStorage.setItem("heallens_sos_log", JSON.stringify(log.slice(0, 10)));
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
    // Remove existing overlay if any
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

    // Auto dismiss after 30 seconds
    setTimeout(() => {
      overlay.remove();
      this.isActivated = false;
    }, 30000);
  }

  // Auto-trigger if scan result is critical
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
        <div class="sos-contact-avatar">${contact.name.charAt(0).toUpperCase()}</div>
        <div class="sos-contact-info">
          <div class="sos-contact-name">${contact.name}</div>
          <div class="sos-contact-relation">${contact.relation}</div>
          <div class="sos-contact-phone">📞 ${contact.phone}</div>
        </div>
        <div class="sos-contact-actions">
          <a href="tel:${contact.phone}" class="sos-call-icon" title="Call">📞</a>
          <button class="sos-delete-btn" onclick="sosManager.deleteContact('${contact.id}'); renderSOSContacts()">🗑️</button>
        </div>
      </div>
    `;
  }
}

window.sosManager = new SOSManager();
