// appointment.js - HealLens Dynamic Specialist Discovery & Appointment Manager

class AppointmentManager {
  constructor() {
    this.currentSpecialty = "General Physician";
    this.currentLocation = null; // { lat, lon, name }
    this.providers = [];
    this.userAppointments = [];
    this.isLoading = false;
    this.STORAGE_KEY = "heallens_appointments_cache";
  }

  init() {
    this.loadCachedAppointments();
    this.fetchRemoteAppointments();
    this.bindGlobalEvents();
  }

  bindGlobalEvents() {
    // Delegation or explicit handlers for booking buttons
    document.addEventListener("click", (e) => {
      if (e.target.matches(".btn-visit-doctor") || e.target.closest(".btn-visit-doctor")) {
        const btn = e.target.matches(".btn-visit-doctor") ? e.target : e.target.closest(".btn-visit-doctor");
        const specialty = btn.getAttribute("data-specialty") || document.getElementById("res-doctor")?.innerText || "General Physician";
        this.openBookingModal(0);
      }
    });
  }

  // ─── LOCATION DISCOVERY ───────────────────────────────────────────────────

  async getUserLocation(forceRefresh = false) {
    if (this.currentLocation && !forceRefresh) {
      return this.currentLocation;
    }

    return new Promise((resolve) => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          async (pos) => {
            const lat = pos.coords.latitude;
            const lon = pos.coords.longitude;
            const locationName = await this.reverseGeocode(lat, lon);
            this.currentLocation = { lat, lon, name: locationName };
            resolve(this.currentLocation);
          },
          async (err) => {
            console.warn("[AppointmentManager] Geolocation denied or failed:", err);
            // Default fallback location (e.g. Chennai)
            const fallbackLocation = { lat: 13.0827, lon: 80.2707, name: "Chennai, TN" };
            this.currentLocation = fallbackLocation;
            resolve(fallbackLocation);
          },
          { timeout: 8000, enableHighAccuracy: true }
        );
      } else {
        const fallbackLocation = { lat: 13.0827, lon: 80.2707, name: "Chennai, TN" };
        this.currentLocation = fallbackLocation;
        resolve(fallbackLocation);
      }
    });
  }

  async setManualLocation(locationQuery) {
    if (!locationQuery || !locationQuery.trim()) return false;
    this.isLoading = true;
    try {
      const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(locationQuery.trim())}&limit=1`;
      const res = await fetch(url, { headers: { "User-Agent": "HealLens-HealthApp/1.0" } });
      const data = await res.json();
      if (data && data.length > 0) {
        const item = data[0];
        const lat = parseFloat(item.lat);
        const lon = parseFloat(item.lon);
        const name = item.display_name.split(",").slice(0, 2).join(",");
        this.currentLocation = { lat, lon, name };
        return true;
      }
    } catch (err) {
      console.error("[AppointmentManager] Geocoding failed:", err);
    } finally {
      this.isLoading = false;
    }
    return false;
  }

  async reverseGeocode(lat, lon) {
    try {
      const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`;
      const res = await fetch(url, { headers: { "User-Agent": "HealLens-HealthApp/1.0" } });
      const data = await res.json();
      if (data && data.address) {
        const city = data.address.city || data.address.town || data.address.suburb || data.address.state_district || "Your Location";
        return city;
      }
    } catch (e) {
      console.warn("Reverse geocode failed:", e);
    }
    return `${lat.toFixed(2)}, ${lon.toFixed(2)}`;
  }

  // ─── PROVIDER SEARCH VIA OVERPASS API ──────────────────────────────────────

  async fetchNearbyProviders(specialty, location) {
    this.isLoading = true;
    this.currentSpecialty = specialty;
    this.providers = [];

    const lat = location.lat;
    const lon = location.lon;
    const radiusMeters = 15000; // 15km search radius

    // Overpass query for hospitals, clinics, doctors
    const query = `
      [out:json][timeout:15];
      (
        node["amenity"~"hospital|clinic|doctors"](around:${radiusMeters},${lat},${lon});
        way["amenity"~"hospital|clinic|doctors"](around:${radiusMeters},${lat},${lon});
        node["healthcare"~"hospital|clinic|doctor"](around:${radiusMeters},${lat},${lon});
      );
      out center 25;
    `;

    try {
      const res = await fetch("https://overpass-api.de/api/interpreter", {
        method: "POST",
        body: query
      });
      const data = await res.json();

      if (data && Array.isArray(data.elements)) {
        const parsed = data.elements.map(el => {
          const tags = el.tags || {};
          const elLat = el.lat || (el.center ? el.center.lat : lat);
          const elLon = el.lon || (el.center ? el.center.lon : lon);
          const dist = this.calculateDistance(lat, lon, elLat, elLon);

          const rawName = tags.name || tags["name:en"] || tags.operator || "Healthcare Facility";
          const facilityType = tags.amenity || tags.healthcare || "Clinic";
          const explicitSpecialty = tags["healthcare:speciality"] || tags.speciality || null;
          
          let formattedType = "Healthcare Facility";
          if (explicitSpecialty) {
            formattedType = `Verified Specialist: ${explicitSpecialty.charAt(0).toUpperCase() + explicitSpecialty.slice(1)}`;
          } else {
            formattedType = `${facilityType.charAt(0).toUpperCase() + facilityType.slice(1)} (Services for ${specialty})`;
          }

          const rawPhone = tags.phone || tags["contact:phone"] || tags["phone:mobile"] || null;
          const rawWebsite = tags.website || tags["contact:website"] || tags["url"] || null;
          const rawRating = tags.rating || tags["stars"] || null;

          const addrParts = [
            tags["addr:street"] || tags["addr:housenumber"],
            tags["addr:suburb"] || tags["addr:district"],
            tags["addr:city"] || tags["addr:state"]
          ].filter(Boolean);
          const fullAddress = tags["addr:full"] || (addrParts.length > 0 ? addrParts.join(", ") : `${dist.toFixed(1)} km from ${location.name}`);

          return {
            id: `osm-${el.id}`,
            name: rawName,
            facilityType: formattedType,
            specialization: specialty,
            address: fullAddress,
            phone: rawPhone,
            website: rawWebsite,
            rating: rawRating,
            lat: elLat,
            lon: elLon,
            distanceKm: dist
          };
        });

        // Filter and sort by distance
        this.providers = parsed.sort((a, b) => a.distanceKm - b.distanceKm);
      }
    } catch (err) {
      console.error("[AppointmentManager] Overpass provider search failed:", err);
      this.providers = [];
    } finally {
      this.isLoading = false;
    }

    return this.providers;
  }

  calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Radius of Earth in km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  // ─── UI RENDERING & DISCOVERY WIDGET ───────────────────────────────────────

  async renderSpecialistWidget(containerId, specialty, contextualNote = "") {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.style.display = "block";
    container.innerHTML = `
      <div class="result-box" style="background: rgba(0, 212, 255, 0.04); border: 1px solid rgba(0, 212, 255, 0.2); border-radius: 12px; padding: 20px; margin-top: 24px;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 10px; margin-bottom: 12px;">
          <div>
            <h4 style="color: var(--color-primary); font-family: var(--font-heading); font-size: 1.15rem; margin-bottom: 4px; display: flex; align-items: center; gap: 8px;">
              <span>🩺</span> Consult a Specialist
            </h4>
            <div style="font-size: 0.95rem; font-weight: 700; color: #fff;">Suggested Specialist: <span style="color: #00d4ff;">${specialty}</span></div>
          </div>
          <div style="font-size: 0.76rem; color: var(--text-muted); background: rgba(255,255,255,0.05); padding: 4px 10px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.1);">
            Informational Aid
          </div>
        </div>

        <p style="font-size: 0.84rem; color: var(--text-secondary); line-height: 1.5; margin-bottom: 16px;">
          ${contextualNote || "This recommendation is based on evaluated metrics and is provided for educational routing purposes. It does not constitute a confirmed diagnosis."}
        </p>

        <!-- Location Search Bar -->
        <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center; background: rgba(0,0,0,0.2); padding: 10px 14px; border-radius: 8px; margin-bottom: 16px;">
          <span style="font-size: 0.85rem; color: var(--text-secondary); font-weight: 600;">📍 Location:</span>
          <span id="${containerId}-location-name" style="font-size: 0.88rem; font-weight: 700; color: #fff;">Detecting location...</span>
          <div style="display: flex; gap: 8px; margin-left: auto;">
            <input type="text" id="${containerId}-loc-input" placeholder="Enter City or PIN..." style="padding: 4px 10px; font-size: 0.8rem; background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.15); color: #fff; border-radius: 6px; width: 140px;" />
            <button class="btn btn-ghost" style="padding: 4px 12px; font-size: 0.78rem;" onclick="window.appointmentManager.handleManualSearch('${containerId}', '${specialty}')">Search</button>
            <button class="btn btn-ghost" style="padding: 4px 10px; font-size: 0.78rem;" title="Use Current GPS" onclick="window.appointmentManager.handleGpsRefresh('${containerId}', '${specialty}')">📡 GPS</button>
          </div>
        </div>

        <!-- Providers List Container -->
        <div id="${containerId}-providers-list" style="display: flex; flex-direction: column; gap: 12px;">
          <div style="text-align: center; padding: 20px; color: var(--text-muted); font-size: 0.88rem;">
            🔍 Searching nearby providers...
          </div>
        </div>
      </div>
    `;

    // Fetch location and load providers
    const loc = await this.getUserLocation();
    const locLabel = document.getElementById(`${containerId}-location-name`);
    if (locLabel) locLabel.innerText = loc.name;

    const providers = await this.fetchNearbyProviders(specialty, loc);
    this.renderProvidersCards(`${containerId}-providers-list`, providers, specialty);
  }

  async handleManualSearch(containerId, specialty) {
    const input = document.getElementById(`${containerId}-loc-input`);
    const val = input?.value?.trim();
    if (!val) return;

    const locLabel = document.getElementById(`${containerId}-location-name`);
    if (locLabel) locLabel.innerText = `Searching '${val}'...`;

    const success = await this.setManualLocation(val);
    if (success) {
      if (locLabel) locLabel.innerText = this.currentLocation.name;
      const providers = await this.fetchNearbyProviders(specialty, this.currentLocation);
      this.renderProvidersCards(`${containerId}-providers-list`, providers, specialty);
    } else {
      if (locLabel) locLabel.innerText = "Location search failed.";
      const listContainer = document.getElementById(`${containerId}-providers-list`);
      if (listContainer) {
        listContainer.innerHTML = `
          <div style="background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 8px; padding: 14px; text-align: center; color: var(--color-danger); font-size: 0.85rem;">
            Unable to locate '${val}'. Please try entering a major city or district name.
          </div>
        `;
      }
    }
  }

  async handleGpsRefresh(containerId, specialty) {
    const locLabel = document.getElementById(`${containerId}-location-name`);
    if (locLabel) locLabel.innerText = "Accessing GPS...";

    const loc = await this.getUserLocation(true);
    if (locLabel) locLabel.innerText = loc.name;

    const providers = await this.fetchNearbyProviders(specialty, loc);
    this.renderProvidersCards(`${containerId}-providers-list`, providers, specialty);
  }

  renderProvidersCards(targetElementId, providers, specialty) {
    const listEl = document.getElementById(targetElementId);
    if (!listEl) return;

    if (!providers || providers.length === 0) {
      listEl.innerHTML = `
        <div style="background: rgba(255, 158, 11, 0.06); border: 1px solid rgba(255, 158, 11, 0.2); border-radius: 8px; padding: 16px; text-align: center;">
          <div style="font-size: 1.5rem; margin-bottom: 6px;">🩺</div>
          <div style="font-weight: 700; color: var(--color-warning); font-size: 0.92rem;">No nearby specialists found</div>
          <div style="font-size: 0.82rem; color: var(--text-secondary); margin-top: 4px;">Try searching using another city, district, or PIN code above.</div>
        </div>
      `;
      return;
    }

    listEl.innerHTML = providers.slice(0, 5).map((p, idx) => {
      const mapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${p.lat},${p.lon}`;
      
      const callBtnHtml = p.phone
        ? `<a href="tel:${p.phone}" class="btn btn-ghost" style="padding: 6px 12px; font-size: 0.78rem; color: #10b981; border-color: rgba(16, 185, 129, 0.3);">📞 Call</a>`
        : ``;

      const websiteBtnHtml = p.website
        ? `<a href="${p.website}" target="_blank" rel="noopener" class="btn btn-ghost" style="padding: 6px 12px; font-size: 0.78rem; color: #00d4ff; border-color: rgba(0, 212, 255, 0.3);">🌐 Website</a>`
        : ``;

      const directionsBtnHtml = `<a href="${mapsUrl}" target="_blank" rel="noopener" class="btn btn-ghost" style="padding: 6px 12px; font-size: 0.78rem; color: #a78bfa; border-color: rgba(167, 139, 250, 0.3);">🧭 Directions</a>`;

      // Book / Contact Action Logic
      let bookActionHtml = "";
      if (p.website) {
        bookActionHtml = `<button class="btn btn-primary" style="padding: 6px 14px; font-size: 0.78rem;" onclick="window.appointmentManager.openBookingModal(${idx})">📅 Book / Schedule</button>`;
      } else {
        bookActionHtml = `<button class="btn btn-primary" style="padding: 6px 14px; font-size: 0.78rem;" onclick="window.appointmentManager.openBookingModal(${idx})">📞 Contact Provider to Book</button>`;
      }

      const ratingBadge = p.rating ? `<span style="font-size: 0.8rem; font-weight: 700; color: #ff9800;">⭐ ${p.rating}</span>` : ``;

      return `
        <div class="glass-card" style="padding: 14px 16px; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.08); border-radius: 10px; display: flex; flex-direction: column; gap: 8px;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 10px;">
            <div>
              <div style="font-weight: 700; color: #fff; font-size: 0.95rem;">${p.name}</div>
              <div style="font-size: 0.78rem; color: var(--color-primary); font-weight: 600; margin-top: 2px;">${p.facilityType}</div>
            </div>
            <div style="text-align: right; flex-shrink: 0;">
              ${ratingBadge}
              <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 2px;">📏 ${p.distanceKm.toFixed(1)} km</div>
            </div>
          </div>

          <div style="font-size: 0.8rem; color: var(--text-secondary); display: flex; align-items: center; gap: 6px;">
            <span>📍</span> <span>${p.address}</span>
          </div>

          <div style="display: flex; gap: 8px; flex-wrap: wrap; align-items: center; margin-top: 4px; padding-top: 8px; border-top: 1px dashed rgba(255,255,255,0.06);">
            ${callBtnHtml}
            ${websiteBtnHtml}
            ${directionsBtnHtml}
            <div style="margin-left: auto;">
              ${bookActionHtml}
            </div>
          </div>
        </div>
      `;
    }).join("");
  }

  // ─── BOOKING & SCHEDULING MODAL ────────────────────────────────────────────

  openBookingModal(providerIdx) {
    const provider = this.providers[providerIdx] || {
      name: "Specialist Provider",
      specialization: this.currentSpecialty,
      facilityType: "Healthcare Facility",
      address: this.currentLocation ? this.currentLocation.name : "Local Provider",
      lat: this.currentLocation ? this.currentLocation.lat : 13.08,
      lon: this.currentLocation ? this.currentLocation.lon : 80.27
    };

    this.selectedProvider = provider;

    const modal = document.getElementById("doctor-booking-modal");
    if (!modal) return;

    // Reset view
    const formView = document.getElementById("booking-form-view");
    const successView = document.getElementById("booking-success-view");
    if (formView) formView.style.display = "block";
    if (successView) successView.style.display = "none";

    // Set Provider Details
    const specialtyNameEl = document.getElementById("booking-specialty-name");
    const specialtyIconEl = document.getElementById("booking-specialty-icon");
    if (specialtyNameEl) specialtyNameEl.innerText = `${provider.name} (${provider.specialization})`;
    if (specialtyIconEl) specialtyIconEl.innerText = "🏥";

    // Set default tomorrow date
    const dateInput = document.getElementById("booking-date");
    if (dateInput) {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const yyyy = tomorrow.getFullYear();
      const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
      const dd = String(tomorrow.getDate()).padStart(2, '0');
      dateInput.value = `${yyyy}-${mm}-${dd}`;
      dateInput.min = `${yyyy}-${mm}-${dd}`;
    }

    // Populate patient options
    const patientSelect = document.getElementById("booking-patient");
    if (patientSelect) {
      const members = window.historyManager?.getFamilyMembers() || [{ name: "Self", relation: "Primary User" }];
      patientSelect.innerHTML = members.map(m => `<option value="${m.name}">${m.name} (${m.relation || "Patient"})</option>`).join("");
    }

    // Render selected provider overview in list container
    const listContainer = document.getElementById("booking-doctors-list");
    if (listContainer) {
      listContainer.innerHTML = `
        <div style="background: rgba(0, 212, 255, 0.05); border: 1px solid rgba(0, 212, 255, 0.2); border-radius: 8px; padding: 12px 14px;">
          <div style="font-weight: 700; color: #fff; font-size: 0.95rem;">${provider.name}</div>
          <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 2px;">📍 ${provider.address}</div>
          ${provider.phone ? `<div style="font-size: 0.78rem; color: #10b981; margin-top: 4px;">📞 Phone: ${provider.phone}</div>` : ""}
          ${provider.website ? `<div style="font-size: 0.78rem; color: #00d4ff; margin-top: 2px;">🌐 Website: <a href="${provider.website}" target="_blank" style="color:#00d4ff; text-decoration:underline;">Open Provider Site</a></div>` : ""}
        </div>
      `;
    }

    modal.style.display = "flex";
  }

  _getSupabaseClient() {
    if (typeof window !== "undefined" && window.supabaseClient) {
      return window.supabaseClient;
    }
    if (typeof window !== "undefined" && window.supabase && typeof window.supabase.createClient === 'function') {
      return window.supabase.createClient(window.SUPABASE_URL, window.SUPABASE_ANON_KEY);
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

  async confirmBooking() {
    if (!this.selectedProvider) return;

    const patientName = document.getElementById("booking-patient")?.value || "Self";
    const dateVal = document.getElementById("booking-date")?.value;
    if (!dateVal) {
      alert("Please select a date for your appointment record.");
      return;
    }

    const userId = await this._getUserId();
    if (!userId) {
      alert("Please log in to book an appointment.");
      return;
    }

    const bookingId = "HL-APT-" + Math.floor(10000 + Math.random() * 90000);
    const hasExternalWebsite = Boolean(this.selectedProvider.website);
    
    // Status strictly compliant with Rule 15 & 16:
    // If real website/external exists -> Status is 'External Booking'
    // If contact-based -> Status is 'Pending Confirmation'
    const status = hasExternalWebsite ? "External Booking" : "Pending Confirmation";

    const newAppointment = {
      id: bookingId,
      user_id: userId,
      provider_name: this.selectedProvider.name,
      specialization: this.selectedProvider.specialization,
      facility_name: this.selectedProvider.facilityType || "Clinic",
      address: this.selectedProvider.address || "",
      phone: this.selectedProvider.phone || "",
      website: this.selectedProvider.website || "",
      latitude: this.selectedProvider.lat || 0,
      longitude: this.selectedProvider.lon || 0,
      appointment_date: dateVal,
      appointment_time: "10:00 AM",
      status: status,
      booking_reference: bookingId,
      external_booking_url: this.selectedProvider.website || "",
      patient_name: patientName,
      source: "HealLens Specialist Discovery",
      created_at: new Date().toISOString()
    };

    // Save to local & Supabase
    await this.saveAppointment(newAppointment);

    // Update Confirmation View
    const idEl = document.getElementById("summary-booking-id");
    const docNameEl = document.getElementById("summary-doctor-name");
    const deptEl = document.getElementById("summary-department");
    const patientEl = document.getElementById("summary-patient");
    const datetimeEl = document.getElementById("summary-datetime");

    if (idEl) idEl.innerText = bookingId;
    if (docNameEl) docNameEl.innerText = this.selectedProvider.name;
    if (deptEl) deptEl.innerText = this.selectedProvider.specialization;
    if (patientEl) patientEl.innerText = patientName;
    if (datetimeEl) datetimeEl.innerText = `${dateVal} (${status})`;

    // Switch Views
    document.getElementById("booking-form-view").style.display = "none";
    document.getElementById("booking-success-view").style.display = "block";

    // Refresh profile UI if visible
    this.renderMyAppointments();
  }

  closeModal() {
    const modal = document.getElementById("doctor-booking-modal");
    if (modal) modal.style.display = "none";
  }

  // ─── SUPABASE & LOCAL STORAGE PERSISTENCE ────────────────────────────────

  loadCachedAppointments() {
    try {
      const raw = localStorage.getItem(this.STORAGE_KEY);
      if (raw) {
        this.userAppointments = JSON.parse(raw);
      }
    } catch (e) {
      this.userAppointments = [];
    }
  }

  saveCachedAppointments(list) {
    this.userAppointments = list;
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(list));
    } catch (e) {}
  }

  async fetchRemoteAppointments() {
    const client = window.supabaseClient || (window.supabase && typeof window.supabase.createClient === 'function' ? window.supabase.createClient(window.SUPABASE_URL, window.SUPABASE_ANON_KEY) : null);
    let userId = window.currentUser?.id;
    if (!userId && client && client.auth) {
      const session = client.auth.session ? client.auth.session() : null;
      const user = client.auth.user ? client.auth.user() : (session ? session.user : null);
      userId = user?.id;
    }
    if (!userId) {
      try {
        const rawToken = localStorage.getItem('sb-auth-token') || localStorage.getItem('supabase.auth.token');
        if (rawToken) userId = JSON.parse(rawToken)?.user?.id;
      } catch (e) {}
    }

    if (client && userId) {
      try {
        const { data, error } = await client
          .from("appointments")
          .select("*")
          .eq("user_id", userId)
          .order("created_at", { ascending: false });

        if (!error && Array.isArray(data)) {
          this.saveCachedAppointments(data);
          this.renderMyAppointments();
          return data;
        }
      } catch (err) {
        console.warn("[AppointmentManager] Supabase appointments fetch notice:", err.message);
      }
    }
    this.renderMyAppointments();
    return this.userAppointments;
  }

  async saveAppointment(appt) {
    // Save to local cache first
    const current = [appt, ...this.userAppointments.filter(a => a.id !== appt.id)];
    this.saveCachedAppointments(current);

    // Save to Supabase if authenticated
    const client = this._getSupabaseClient();
    const userId = appt.user_id || await this._getUserId();

    if (client && userId) {
      try {
        const dbPayload = {
          user_id: userId,
          provider_name: appt.provider_name,
          specialization: appt.specialization,
          facility_name: appt.facility_name,
          address: appt.address,
          phone: appt.phone,
          website: appt.website,
          latitude: appt.latitude,
          longitude: appt.longitude,
          appointment_date: appt.appointment_date,
          appointment_time: appt.appointment_time,
          status: appt.status,
          booking_reference: appt.booking_reference,
          external_booking_url: appt.external_booking_url,
          source: appt.source,
          created_at: appt.created_at
        };

        await client.from("appointments").insert([dbPayload]);
      } catch (err) {
        console.warn("[AppointmentManager] Could not save to remote Supabase DB:", err.message);
      }
    }
  }

  async deleteAppointment(id) {
    const filtered = this.userAppointments.filter(a => a.id !== id);
    this.saveCachedAppointments(filtered);
    this.renderMyAppointments();

    const client = this._getSupabaseClient();
    const userId = await this._getUserId();
    if (client && userId) {
      try {
        await client.from("appointments").delete().eq("id", id).eq("user_id", userId);
      } catch (e) {}
    }
  }

  // ─── PROFILE RENDERING: MY APPOINTMENTS ────────────────────────────────────

  renderMyAppointments() {
    const container = document.getElementById("profile-appointments-list");
    if (!container) return;

    if (this.userAppointments.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 24px; color: var(--text-muted); background: rgba(255,255,255,0.02); border: 1px dashed rgba(255,255,255,0.08); border-radius: 10px;">
          <div style="font-size: 2rem; margin-bottom: 8px;">📅</div>
          <div style="font-size: 0.9rem; font-weight: 600; color: var(--text-secondary);">No appointments logged yet</div>
          <div style="font-size: 0.8rem; margin-top: 4px;">Use "Consult a Specialist" in Scanner or Report Analyzer to find and schedule provider visits.</div>
        </div>
      `;
      return;
    }

    container.innerHTML = this.userAppointments.map(a => {
      const mapsUrl = (a.latitude && a.longitude)
        ? `https://www.google.com/maps/dir/?api=1&destination=${a.latitude},${a.longitude}`
        : `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(a.address || a.provider_name)}`;

      let statusBadgeStyle = "background: rgba(255,158,11,0.15); color: #ff9800;";
      if (a.status === "Confirmed") statusBadgeStyle = "background: rgba(16,185,129,0.15); color: #10b981;";
      if (a.status === "Cancelled") statusBadgeStyle = "background: rgba(239,68,68,0.15); color: #ef4444;";

      return `
        <div class="glass-card" style="padding: 16px; margin-bottom: 12px; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.08); border-radius: 10px;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 10px;">
            <div>
              <div style="font-weight: 700; color: #fff; font-size: 1rem;">${a.provider_name}</div>
              <div style="font-size: 0.82rem; color: var(--color-primary); font-weight: 600; margin-top: 2px;">${a.specialization || "Specialist"} — ${a.facility_name || "Clinic"}</div>
            </div>
            <span style="font-size: 0.75rem; font-weight: 700; padding: 3px 10px; border-radius: 12px; ${statusBadgeStyle}">
              ${a.status || "Pending Confirmation"}
            </span>
          </div>

          <div style="display: flex; gap: 16px; margin-top: 10px; font-size: 0.82rem; color: var(--text-secondary);">
            <div>🗓️ <strong>Date:</strong> ${a.appointment_date}</div>
            <div>👤 <strong>Patient:</strong> ${a.patient_name || "Self"}</div>
          </div>

          ${a.address ? `<div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 6px;">📍 ${a.address}</div>` : ""}
          ${a.booking_reference ? `<div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 4px;">Ref ID: <code>${a.booking_reference}</code></div>` : ""}

          <div style="display: flex; gap: 8px; margin-top: 12px; padding-top: 10px; border-top: 1px dashed rgba(255,255,255,0.06); align-items: center;">
            ${a.phone ? `<a href="tel:${a.phone}" class="btn btn-ghost" style="padding: 4px 10px; font-size: 0.75rem; color: #10b981; border-color: rgba(16,185,129,0.3);">📞 Call</a>` : ""}
            ${a.website ? `<a href="${a.website}" target="_blank" rel="noopener" class="btn btn-ghost" style="padding: 4px 10px; font-size: 0.75rem; color: #00d4ff; border-color: rgba(0,212,255,0.3);">🌐 Website</a>` : ""}
            <a href="${mapsUrl}" target="_blank" rel="noopener" class="btn btn-ghost" style="padding: 4px 10px; font-size: 0.75rem; color: #a78bfa; border-color: rgba(167,139,250,0.3);">🧭 Directions</a>
            <button class="btn btn-ghost" style="padding: 4px 10px; font-size: 0.75rem; color: #ef4444; border-color: rgba(239,68,68,0.3); margin-left: auto;" onclick="window.appointmentManager.deleteAppointment('${a.id}')">🗑️ Remove</button>
          </div>
        </div>
      `;
    }).join("");
  }
}

// Global single instance
window.appointmentManager = new AppointmentManager();
