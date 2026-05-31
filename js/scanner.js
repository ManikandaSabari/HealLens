// scanner.js — HealLens Pro Orchestrator (Restored stable state)

class Scanner {
  constructor() {
    this.currentImageSrc = null;
    this.selectedMember = "Self";
    this.forceAnalysis = false;
    this.videoStream = null;
  }

  init() {
    console.log("[HealLens Scanner] Initializing...");
    this.initListeners();
    this.updateSymptomVisibility(); // Initial filter
  }

  initListeners() {
    // 1. Upload & File Handling
    const browseBtn = document.getElementById("browse-btn");
    const fileInput = document.getElementById("file-input");
    const uploadArea = document.getElementById("upload-area");

    if (browseBtn && fileInput) {
      browseBtn.addEventListener("click", () => fileInput.click());
      fileInput.addEventListener("change", (e) => this.handleFileSelect(e));
    }

    if (uploadArea) {
      uploadArea.addEventListener("dragover", (e) => {
        e.preventDefault();
        uploadArea.classList.add("dragover");
      });
      uploadArea.addEventListener("dragleave", () => uploadArea.classList.remove("dragover"));
      uploadArea.addEventListener("drop", (e) => {
        e.preventDefault();
        uploadArea.classList.remove("dragover");
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
          this.handleFile(e.dataTransfer.files[0]);
        }
      });
    }

    // 2. Camera Handling
    const cameraBtn = document.getElementById("camera-btn");
    const captureBtn = document.getElementById("capture-btn");
    const closeCamBtn = document.getElementById("close-cam-btn");

    if (cameraBtn) cameraBtn.addEventListener("click", () => this.openCamera());
    if (captureBtn) captureBtn.addEventListener("click", () => this.capturePhoto());
    if (closeCamBtn) closeCamBtn.addEventListener("click", () => this.closeCamera());

    // 3. Analysis Orchestration
    const analyzeBtn = document.getElementById("analyze-btn");
    const analyzeSymptomsBtn = document.getElementById("analyze-symptoms-btn");

    if (analyzeBtn) analyzeBtn.addEventListener("click", () => this.startAnalysis());
    if (analyzeSymptomsBtn) analyzeSymptomsBtn.addEventListener("click", () => this.startAnalysis());

    // 4. Member Select & Body Part Filter
    const patientSelect = document.getElementById("patient-select");
    if (patientSelect) {
      patientSelect.addEventListener("change", () => {
        this.selectedMember = patientSelect.value;
      });
    }

    const bodyPartSelect = document.getElementById("body-part-select");
    if (bodyPartSelect) {
      bodyPartSelect.addEventListener("change", () => {
        this.updateSymptomVisibility();
      });
    }
  }

  updateSymptomVisibility() {
    const bodyPartSelect = document.getElementById("body-part-select");
    const bodyPart = bodyPartSelect ? bodyPartSelect.value : "none";
    
    const lungGroup = document.getElementById("lung-chips");
    const boneGroup = document.getElementById("bone-chips");
    const skinGroup = document.getElementById("skin-chips");

    if (lungGroup) lungGroup.style.display = (bodyPart === "chest") ? "block" : "none";
    if (boneGroup) boneGroup.style.display = (bodyPart === "bone") ? "block" : "none";
    if (skinGroup) skinGroup.style.display = (bodyPart === "skin") ? "block" : "none";
    
    // Also clear input if body part changes to avoid cross-contamination
    const input = document.getElementById("symptom-input");
    // if (input) input.value = ""; 
  }

  handleFileSelect(e) {
    if (e.target.files && e.target.files[0]) {
      this.handleFile(e.target.files[0]);
    }
  }

  handleFile(file) {
    if (!file.type.startsWith("image/")) {
      alert("Please upload a valid image file.");
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      this.currentImageSrc = e.target.result;
      this.showPreview(this.currentImageSrc);
    };
    reader.readAsDataURL(file);
  }

  showPreview(src) {
    const previewArea = document.getElementById("scanner-preview-area");
    const previewImg = document.getElementById("image-preview");
    const uploadArea = document.getElementById("upload-area");
    const scannerActions = document.getElementById("scanner-actions");

    if (previewImg) {
      previewImg.src = src;
      previewImg.style.display = "block";
    }
    const canvas = document.getElementById("xray-canvas");
    if (canvas) canvas.style.display = "none";


    if (previewArea) previewArea.style.display = "block";
    if (uploadArea) uploadArea.style.display = "none";
    if (scannerActions) scannerActions.style.display = "flex";

    // Smooth scroll to preview
    previewArea.scrollIntoView({ behavior: 'smooth' });
  }

  async openCamera() {
    const cameraSection = document.getElementById("camera-section");
    const uploadArea = document.getElementById("upload-area");
    const video = document.getElementById("camera-video");

    try {
      this.videoStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" }, audio: false });
      if (video) {
        video.srcObject = this.videoStream;
        if (cameraSection) cameraSection.style.display = "block";
        if (uploadArea) uploadArea.style.display = "none";
      }
    } catch (err) {
      console.error("Camera Error:", err);
      alert("Could not access camera. Please check permissions.");
    }
  }

  capturePhoto() {
    const video = document.getElementById("camera-video");
    if (!video || !this.videoStream) return;

    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0);

    this.currentImageSrc = canvas.toDataURL("image/jpeg");
    this.closeCamera();
    this.showPreview(this.currentImageSrc);
  }

  closeCamera() {
    if (this.videoStream) {
      this.videoStream.getTracks().forEach(track => track.stop());
      this.videoStream = null;
    }
    const cameraSection = document.getElementById("camera-section");
    const uploadArea = document.getElementById("upload-area");
    if (cameraSection) cameraSection.style.display = "none";
    if (uploadArea && !this.currentImageSrc) uploadArea.style.display = "flex";
  }

  async startAnalysis() {
    // 1. Validate Body Part
    const bodyPartSelect = document.getElementById("body-part-select");
    const bodyPart = bodyPartSelect ? bodyPartSelect.value : "none";

    if (bodyPart === "none" || !bodyPart) {
      const warningModal = document.getElementById("body-part-warning-modal");
      if (warningModal) {
        warningModal.style.display = "flex";
      } else {
        alert("Please select a Body Part (Lungs, Bone, or Skin) before analyzing.");
      }
      return;
    }

    // 2. Validate Image
    if (!this.currentImageSrc) {
      alert("Please upload or capture an image first.");
      return;
    }

    // 3. Clinical Gate Check (Unless forced)
    const symptomInput = document.getElementById("symptom-input");
    const symptoms = symptomInput ? symptomInput.value.trim() : "";

    if (!symptoms && !this.forceAnalysis) {
      // Show proactive modal with REAL model prediction (not hardcoded)
      const promptModal = document.getElementById("symptom-prompt-modal");
      if (promptModal) {
        const guessEl = document.getElementById("modal-disease-guess");

        // Show popup immediately with scanning state
        promptModal.style.display = "flex";
        if (guessEl) {
          guessEl.innerText = "🔍 Scanning image...";
          guessEl.style.color = "var(--accent-primary)";
        }

        // Always run AI prediction to get the actual disease name (CNN or rule-based)
        let guessName = bodyPart === "chest" ? "Pneumonia"
                      : (bodyPart === "skin" ? "Skin Infection" : "Bone Fracture");

        try {
          const img = document.getElementById("image-preview");
          if (img && window.aiEngine) {
            // Wait for model to be ready (either CNN or fallback)
            if (!window.aiEngine.modelReady) {
              await new Promise(resolve => {
                const check = setInterval(() => {
                  if (window.aiEngine.modelReady) { clearInterval(check); resolve(); }
                }, 200);
                setTimeout(() => { clearInterval(check); resolve(); }, 5000); // max 5s wait
              });
            }
            const quickResult = await window.aiEngine.analyzeImage(img, bodyPart);
            guessName = quickResult.diseaseName;
            console.log("[Scanner] ✅ AI prediction for popup:", guessName);
          }
        } catch (e) {
          console.warn("[Scanner] Quick prediction failed, using default guess:", e);
        }

        // Update popup with real disease name
        if (guessEl) {
          guessEl.innerText = guessName;
          guessEl.style.color = "var(--color-primary)";
        }
        return;
      }
    }

    // 4. Run AI Analysis
    this.showLoading(true);
    
    try {
      const img = document.getElementById("image-preview");
      const result = await window.aiEngine.analyzeImage(img, bodyPart);
      
      this.showLoading(false);
      
      // Ensure preview remains visible
      const previewArea = document.getElementById("scanner-preview-area");
      if (previewArea) previewArea.style.display = "block";

      this.showResult(result);
      
      // Save to history
      if (window.historyManager) {
        window.historyManager.addScan(result, this.selectedMember, this.currentImageSrc);
      }
      
      // Auto-trigger SOS if critical
      if (window.sosManager && result.severity.toLowerCase() === "critical") {
        window.sosManager.autoTriggerIfCritical(result);
      }

      // Reset force flag
      this.forceAnalysis = false;
      
    } catch (err) {
      console.error("Analysis Error:", err);
      this.showLoading(false);
      alert("An error occurred during analysis. Please try again.");
    }
  }

  showLoading(isLoading) {
    const overlay = document.getElementById("analyzing-overlay");
    if (overlay) {
      overlay.style.display = isLoading ? "flex" : "none";
    }
    const analyzeBtn = document.getElementById("analyze-btn");
    if (analyzeBtn) {
      analyzeBtn.disabled = isLoading;
      analyzeBtn.innerHTML = isLoading ? '<span class="analyzing-ring-small"></span> Analyzing...' : '<span class="icon">🧠</span> Analyze Image';
    }
  }

  showResult(result) {
    const resultSection = document.getElementById("result-section");
    const resultCard = document.getElementById("result-card");
    if (!resultSection || !resultCard) return;

    this.currentResult = result; // Store for re-rendering
    resultSection.style.display = "block";
    resultCard.classList.add("active");

    // Update Text Fields
    const diseaseNameEl = document.getElementById("res-disease");
    const bodyPartEl = document.getElementById("res-body-part");
    const confidenceEl = document.getElementById("res-confidence");
    const descriptionEl = document.getElementById("res-description");
    const doctorEl = document.getElementById("res-doctor");

    if (diseaseNameEl) diseaseNameEl.innerText = result.diseaseName;
    if (bodyPartEl) bodyPartEl.innerText = result.bodyPartLabel;
    if (confidenceEl) confidenceEl.innerText = Math.round(result.confidence) + "%";
    if (descriptionEl) descriptionEl.innerText = result.description;
    if (doctorEl) doctorEl.innerText = result.doctorType;

    // Severity Badge
    const sevBadge = document.getElementById("res-severity-badge");
    if (sevBadge) {
      const sevLabel = window.i18n?.t("severityLevel") || "Severity";
      const sevVal = result.severity || "Moderate";
      sevBadge.innerText = sevVal + " " + sevLabel;
      sevBadge.className = "badge severity-" + (result.severityRaw ? result.severityRaw.toLowerCase() : "moderate");
    }

    // X-Ray Badge logic
    const xrayBadge = document.getElementById("xray-badge");
    if (xrayBadge) {
      const currentBodyPart = document.getElementById("body-part-select")?.value;
      xrayBadge.style.display = (currentBodyPart === "chest" || currentBodyPart === "bone") ? "block" : "none";
      xrayBadge.innerText = currentBodyPart === "chest" ? "CHEST X-RAY" : (currentBodyPart === "bone" ? "BONE X-RAY" : "SKIN SCAN");
    }

    // Remedies Rendering
    this.renderRemedies(result);

    // Urgent Badge
    const urgentBadge = document.getElementById("res-urgent-badge");
    if (urgentBadge) {
      urgentBadge.style.display = (result.severityRaw?.toLowerCase() === "critical") ? "block" : "none";
    }

    // SOS Trigger Btn
    const sosBtn = document.getElementById("btn-trigger-sos");
    if (sosBtn) {
      sosBtn.style.display = (result.severityRaw?.toLowerCase() === "critical") ? "inline-flex" : "none";
    }

    // Scroll to results
    resultSection.scrollIntoView({ behavior: "smooth" });

    // Voice feedback
    if (window.voiceEngine) {
      window.voiceEngine.speakResult(result);
    }
  }

  async reRenderCurrentResult() {
    if (!this.currentResult) return;
    // Re-analyze or just re-translate currentResult fields
    // Re-analyzing is safer to get all translations from aiEngine
    const img = document.getElementById("image-preview");
    const bodyPart = document.getElementById("body-part-select")?.value || "chest";
    const result = await window.aiEngine.analyzeImage(img, bodyPart);
    this.showResult(result);
  }


  renderRemedies(result) {
    const naturalContainer = document.getElementById("res-remedies-natural");
    const ayurvedicContainer = document.getElementById("res-remedies-ayurvedic");

    const labelIngredients = window.i18n?.t("ingredients") || "Ingredients";
    const labelMethod = window.i18n?.t("preparation") || "Method";
    const labelUse = window.i18n?.t("use") || "Use";

    if (naturalContainer) {
      if (result.remedies && result.remedies.length > 0) {
        naturalContainer.innerHTML = result.remedies.map(r => `
          <div class="remedy-item" style="margin-bottom: 15px;">
            <div style="font-weight: 700; color: #4caf50;">${r.name}</div>
            <div style="font-size: 0.85rem; color: var(--text-secondary); margin: 2px 0;"><strong>${labelIngredients}:</strong> ${r.ingredients}</div>
            <div style="font-size: 0.85rem; color: var(--text-secondary); margin: 2px 0;"><strong>${labelMethod}:</strong> ${r.method}</div>
            <div style="font-size: 0.85rem; color: var(--text-secondary);"><strong>${labelUse}:</strong> ${r.use || "General healing support."}</div>
          </div>
        `).join("");
      } else {
        naturalContainer.innerHTML = `<p>${window.i18n?.t("noRemedies") || "Standard clinical care recommended."}</p>`;
      }
    }

    if (ayurvedicContainer) {
      if (result.ayurveda && result.ayurveda.length > 0) {
        ayurvedicContainer.innerHTML = result.ayurveda.map(a => `
          <div class="ayurveda-item" style="margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid rgba(255,152,0,0.1);">
            <div style="font-weight: 700; color: #ff9800;">${a.name} <span style="font-weight: 400; opacity: 0.8; font-size: 0.9em;">(${a.sanskrit})</span></div>
            <div style="font-size: 0.85rem; color: var(--text-secondary);"><strong>${window.i18n?.t("dosage") || "Dosage"}:</strong> ${a.dosage}</div>
            <div style="font-size: 0.85rem; color: var(--text-secondary);"><strong>${labelUse}:</strong> ${a.use}</div>
          </div>
        `).join("");
      } else {
        ayurvedicContainer.innerHTML = `<p>${window.i18n?.t("noAyurveda") || "Consult an Ayurvedic specialist for specific guidance."}</p>`;
      }
    }
  }

}

// Initialize Global Instance
window.scanner = new Scanner();