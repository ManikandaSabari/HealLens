// appointment.js - HealLens Doctor Appointment Booking Manager

class AppointmentManager {
  constructor() {
    this.selectedDoctor = null;
    this.specialty = "";
    this.doctorsDb = {
      "Pulmonologist": [
        { name: "Dr. Arvind Swamy", exp: "15 yrs exp", rating: "4.9⭐", slot: "10:00 AM - 01:00 PM", hospital: "Apollo Respiratory Clinic" },
        { name: "Dr. Sarah Mathew", exp: "10 yrs exp", rating: "4.8⭐", slot: "02:00 PM - 05:00 PM", hospital: "Metro Chest Center" }
      ],
      "Orthopedic Surgeon": [
        { name: "Dr. Rajesh Kumar", exp: "18 yrs exp", rating: "4.9⭐", slot: "09:30 AM - 12:30 PM", hospital: "Fortis Bone & Joint Hospital" },
        { name: "Dr. Amanda Ross", exp: "12 yrs exp", rating: "4.7⭐", slot: "03:00 PM - 06:00 PM", hospital: "Orthocare Specialty Clinic" }
      ],
      "Rheumatologist": [
        { name: "Dr. Priya Sharma", exp: "14 yrs exp", rating: "4.8⭐", slot: "11:00 AM - 02:00 PM", hospital: "Care Arthritis Institute" },
        { name: "Dr. Katherine Lee", exp: "9 yrs exp", rating: "4.6⭐", slot: "04:00 PM - 07:00 PM", hospital: "Global Joint & Immunology Care" }
      ],
      "Dermatologist": [
        { name: "Dr. Divya Patel", exp: "11 yrs exp", rating: "4.8⭐", slot: "10:30 AM - 01:30 PM", hospital: "DermaGlow Skin Hospital" },
        { name: "Dr. Michael Chang", exp: "15 yrs exp", rating: "4.9⭐", slot: "02:30 PM - 05:30 PM", hospital: "Advanced Skin Clinic" }
      ],
      "General Physician": [
        { name: "Dr. K. Raghavan", exp: "20 yrs exp", rating: "4.9⭐", slot: "08:30 AM - 11:30 AM", hospital: "City General Hospital" },
        { name: "Dr. Jessica Taylor", exp: "8 yrs exp", rating: "4.7⭐", slot: "01:00 PM - 04:00 PM", hospital: "Care First Family Clinic" }
      ]
    };
  }

  init() {
    // Bind click event on Book Appointment button in Scanner Result page
    const bookBtn = document.querySelector(".btn-visit-doctor");
    if (bookBtn) {
      // Remove any existing event listeners by replacing the element
      const newBookBtn = bookBtn.cloneNode(true);
      bookBtn.parentNode.replaceChild(newBookBtn, bookBtn);
      
      newBookBtn.addEventListener("click", (e) => {
        e.preventDefault();
        const specialtyText = document.getElementById("res-doctor")?.innerText || "General Physician";
        this.openModal(specialtyText);
      });
    }
  }

  openModal(specialty) {
    this.specialty = specialty;
    this.selectedDoctor = null;

    // Set Specialty Details in UI
    const specialtyNameEl = document.getElementById("booking-specialty-name");
    const specialtyIconEl = document.getElementById("booking-specialty-icon");
    if (specialtyNameEl) specialtyNameEl.innerText = specialty;
    
    // Choose icon based on specialty
    const icons = { "Pulmonologist": "🫁", "Orthopedic Surgeon": "🦴", "Rheumatologist": "🩺", "Dermatologist": "🧴", "General Physician": "👨‍⚕️" };
    if (specialtyIconEl) specialtyIconEl.innerText = icons[specialty] || "👨‍⚕️";

    // Set default tomorrow date
    const dateInput = document.getElementById("booking-date");
    if (dateInput) {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const yyyy = tomorrow.getFullYear();
      const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
      const dd = String(tomorrow.getDate()).padStart(2, '0');
      dateInput.value = `${yyyy}-${mm}-${dd}`;
      dateInput.min = `${yyyy}-${mm}-${dd}`; // restrict past dates
    }

    // Load Patient selection options from Family Members
    const patientSelect = document.getElementById("booking-patient");
    if (patientSelect) {
      const members = window.historyManager?.getFamilyMembers() || [{ name: "Self", relation: "Primary User" }];
      patientSelect.innerHTML = members.map(m => `<option value="${m.name}">${m.name} (${m.relation})</option>`).join("");
      
      // Select currently selected scanner member if possible
      const currentScannerMember = document.getElementById("scanner-member-select")?.value;
      if (currentScannerMember) {
        patientSelect.value = currentScannerMember;
      }
    }

    // Render list of doctors for the specialty
    this.renderDoctorsList();

    // Show booking form, hide success view
    document.getElementById("booking-form-view").style.display = "block";
    document.getElementById("booking-success-view").style.display = "none";

    // Open Modal Overlay
    const modal = document.getElementById("doctor-booking-modal");
    if (modal) {
      modal.style.display = "flex";
    }
  }

  renderDoctorsList() {
    const listContainer = document.getElementById("booking-doctors-list");
    if (!listContainer) return;

    // Fallback if specialty not found
    const docs = this.doctorsDb[this.specialty] || this.doctorsDb["General Physician"];
    
    listContainer.innerHTML = docs.map((doc, idx) => `
      <div class="doctor-booking-card" data-idx="${idx}" onclick="window.appointmentManager.selectDoctor(${idx})" style="display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border: 1.5px solid rgba(255,255,255,0.06); background: rgba(255,255,255,0.02); border-radius: 10px; cursor: pointer; transition: all var(--transition-base);">
        <div style="display: flex; align-items: center; gap: 12px;">
          <div style="width: 42px; height: 42px; border-radius: 50%; background: linear-gradient(135deg, rgba(0, 212, 255, 0.2) 0%, rgba(124, 58, 237, 0.2) 100%); display: flex; align-items: center; justify-content: center; font-size: 1.3rem; border: 1px solid rgba(0, 212, 255, 0.2);">
            👨‍⚕️
          </div>
          <div>
            <div style="font-weight: 700; color: #fff; font-size: 0.92rem;">${doc.name}</div>
            <div style="font-size: 0.78rem; color: var(--text-secondary);">${doc.exp} · ${doc.hospital}</div>
          </div>
        </div>
        <div style="text-align: right;">
          <div style="font-size: 0.85rem; font-weight: 600; color: var(--color-primary);">${doc.rating}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 2px;">Slot: ${doc.slot}</div>
        </div>
      </div>
    `).join("");

    // Auto-select the first doctor
    this.selectDoctor(0);
  }

  selectDoctor(idx) {
    const cards = document.querySelectorAll(".doctor-booking-card");
    cards.forEach(c => {
      c.style.borderColor = "rgba(255,255,255,0.06)";
      c.style.background = "rgba(255,255,255,0.02)";
      c.style.boxShadow = "none";
    });

    const selectedCard = document.querySelector(`.doctor-booking-card[data-idx="${idx}"]`);
    if (selectedCard) {
      selectedCard.style.borderColor = "var(--color-primary)";
      selectedCard.style.background = "rgba(0, 212, 255, 0.04)";
      selectedCard.style.boxShadow = "0 0 15px rgba(0, 212, 255, 0.15)";
    }

    const docs = this.doctorsDb[this.specialty] || this.doctorsDb["General Physician"];
    this.selectedDoctor = docs[idx];
  }

  confirmBooking() {
    if (!this.selectedDoctor) {
      alert("Please select a doctor to book the appointment.");
      return;
    }

    const patientName = document.getElementById("booking-patient")?.value || "Self";
    const dateVal = document.getElementById("booking-date")?.value;
    if (!dateVal) {
      alert("Please choose a valid appointment date.");
      return;
    }

    // Generate random booking details
    const bookingId = "HL-" + Math.floor(10000 + Math.random() * 90000);
    const options = { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' };
    const dateFormatted = new Date(dateVal).toLocaleDateString("en-IN", options);

    // Populate Success UI
    const idEl = document.getElementById("summary-booking-id");
    const docNameEl = document.getElementById("summary-doctor-name");
    const deptEl = document.getElementById("summary-department");
    const patientEl = document.getElementById("summary-patient");
    const datetimeEl = document.getElementById("summary-datetime");

    if (idEl) idEl.innerText = bookingId;
    if (docNameEl) docNameEl.innerText = this.selectedDoctor.name;
    if (deptEl) deptEl.innerText = this.specialty;
    if (patientEl) patientEl.innerText = patientName;
    if (datetimeEl) datetimeEl.innerText = `${dateFormatted} at ${this.selectedDoctor.slot.split(" - ")[0]}`;

    // Switch Views
    document.getElementById("booking-form-view").style.display = "none";
    document.getElementById("booking-success-view").style.display = "block";
  }

  closeModal() {
    const modal = document.getElementById("doctor-booking-modal");
    if (modal) {
      modal.style.display = "none";
    }
  }
}

// Global instance
window.appointmentManager = new AppointmentManager();
