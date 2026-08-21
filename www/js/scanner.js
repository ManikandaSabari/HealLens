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

    // Automatic camera resource cleanup on page/tab blur
    document.addEventListener("visibilitychange", () => {
      if (document.hidden) {
        this.closeCamera();
      }
    });
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

  ensureImageLoaded(src) {
    if (!src) return Promise.resolve(null);
    return new Promise((resolve) => {
      const img = new Image();
      img.crossOrigin = "anonymous";
      img.onload = () => {
        if (img.complete && img.naturalWidth > 0 && img.naturalHeight > 0) {
          resolve(img);
        } else {
          resolve(null);
        }
      };
      img.onerror = () => resolve(null);
      img.src = src;
    });
  }

  async validateImageSuitability(bodyPart, src) {
    if (!src) {
      return {
        valid: false,
        title: "No Valid Image Found",
        message: "Please upload or capture a valid image first."
      };
    }

    const imgElement = await this.ensureImageLoaded(src);
    if (!imgElement) {
      return {
        valid: false,
        title: "No Valid Image Found",
        message: "Please upload or capture a valid image first."
      };
    }

    try {
      const width = imgElement.naturalWidth;
      const height = imgElement.naturalHeight;

      const isLungs = (bodyPart === "chest" || bodyPart === "lungs");
      const isBone = (bodyPart === "bone");
      const isSkin = (bodyPart === "skin");

      if (width < 32 || height < 32) {
        const title = isLungs ? "Invalid Image for Lung X-Ray Analysis"
                    : (isBone ? "Invalid Image for Bone X-Ray Analysis" : "Invalid Image for Skin Analysis");
        const msg = isLungs ? "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
                  : (isBone ? "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
                            : "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze.");
        return { valid: false, title: title, message: msg };
      }

      const canvas = document.createElement("canvas");
      canvas.width = width;
      canvas.height = height;
      const ctx = canvas.getContext("2d");
      ctx.drawImage(imgElement, 0, 0, width, height);

      const imgData = ctx.getImageData(0, 0, width, height);
      const data = imgData.data;

      const sampleCols = 16;
      const sampleRows = 16;
      let totalSat = 0.0;
      let totalLum = 0.0;
      let minLum = 255.0;
      let maxLum = 0.0;
      let blackPixelCount = 0;
      let skinLikePixelCount = 0;
      const lumValues = new Float32Array(sampleCols * sampleRows);
      let sampleCount = 0;

      const stepX = Math.max(1, Math.floor(width / sampleCols));
      const stepY = Math.max(1, Math.floor(height / sampleRows));

      for (let y = 0; y < sampleRows; y++) {
        const pxY = Math.min(height - 1, y * stepY);
        for (let x = 0; x < sampleCols; x++) {
          const pxX = Math.min(width - 1, x * stepX);
          const index = (pxY * width + pxX) * 4;

          const r = data[index];
          const g = data[index + 1];
          const b = data[index + 2];

          const maxChannel = Math.max(r, g, b);
          const minChannel = Math.min(r, g, b);
          const chroma = maxChannel - minChannel;
          const sat = maxChannel === 0 ? 0.0 : chroma / maxChannel;

          const lum = 0.299 * r + 0.587 * g + 0.114 * b;
          const cb = 128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b;
          const cr = 128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b;

          if (lum < 10.0) blackPixelCount++;
          if (lum >= 20.0 && cb >= 75.0 && cb <= 135.0 && cr >= 130.0 && cr <= 175.0) {
            skinLikePixelCount++;
          }

          totalSat += sat;
          totalLum += lum;
          if (lum < minLum) minLum = lum;
          if (lum > maxLum) maxLum = lum;
          lumValues[sampleCount] = lum;
          sampleCount++;
        }
      }

      const avgSat = totalSat / sampleCount;
      const avgLum = totalLum / sampleCount;
      const blackPixelRatio = blackPixelCount / sampleCount;
      const skinLikePixelRatio = skinLikePixelCount / sampleCount;

      let lumVarianceSum = 0.0;
      for (let i = 0; i < sampleCount; i++) {
        const diff = lumValues[i] - avgLum;
        lumVarianceSum += diff * diff;
      }
      const lumStdDev = Math.sqrt(lumVarianceSum / sampleCount);

      // Check 1: Blank or near-uniform image detection
      if (lumStdDev < 6.0 || (maxLum - minLum) < 12.0) {
        const title = isLungs ? "Invalid Image for Lung X-Ray Analysis"
                    : (isBone ? "Invalid Image for Bone X-Ray Analysis" : "Invalid Image for Skin Analysis");
        const msg = isLungs ? "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
                  : (isBone ? "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
                            : "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze.");
        return { valid: false, title: title, message: msg };
      }

      // Check 2: Overwhelmingly Black / Text-on-Black Screen Protection
      if (blackPixelRatio > 0.85 && avgLum < 25.0) {
        const title = isLungs ? "Invalid Image for Lung X-Ray Analysis"
                    : (isBone ? "Invalid Image for Bone X-Ray Analysis" : "Invalid Image for Skin Analysis");
        const msg = isLungs ? "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
                  : (isBone ? "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
                            : "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze.");
        return { valid: false, title: title, message: msg };
      }

      if (isLungs) {
        if (avgSat > 0.32 || avgLum < 20.0) {
          return {
            valid: false,
            title: "Invalid Image for Lung X-Ray Analysis",
            message: "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
          };
        }
      } else if (isBone) {
        if (avgSat > 0.32 || avgLum < 20.0) {
          return {
            valid: false,
            title: "Invalid Image for Bone X-Ray Analysis",
            message: "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
          };
        }
      } else if (isSkin) {
        if (avgLum < 15.0 || avgLum > 245.0 || skinLikePixelRatio < 0.12) {
          return {
            valid: false,
            title: "Invalid Image for Skin Analysis",
            message: "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze."
          };
        }
      }
    } catch (e) {
      console.warn("[Scanner Validation] Canvas analysis warning:", e);
    }

    return { valid: true };
  }

  showValidationWarningModal(title, message) {
    const warningModal = document.getElementById("body-part-warning-modal");
    if (warningModal) {
      const modalTitle = warningModal.querySelector("h3");
      const modalDesc = warningModal.querySelector("p");
      if (modalTitle) modalTitle.innerText = title;
      if (modalDesc) modalDesc.innerText = message;
      warningModal.style.display = "flex";
    } else {
      alert(`${title}\n\n${message}`);
    }
  }

  async startAnalysis() {
    // 1. Validate Body Part
    const bodyPartSelect = document.getElementById("body-part-select");
    const bodyPart = bodyPartSelect ? bodyPartSelect.value : "none";

    if (bodyPart === "none" || !bodyPart) {
      this.showValidationWarningModal(
        "Medical Selection Required",
        "Clinical Requirement: Please select the Body Part (Lungs, Bone, or Skin) before analyzing the image to ensure diagnostic accuracy."
      );
      return;
    }

    // 2. Validate Image
    if (!this.currentImageSrc) {
      alert("Please upload or capture an image first.");
      return;
    }

    // 2B. Image Suitability Validation Check (Fresh Current-Image Async Synchronization)
    const validationResult = await this.validateImageSuitability(bodyPart, this.currentImageSrc);
    if (!validationResult.valid) {
      this.showValidationWarningModal(validationResult.title, validationResult.message);
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

    if (diseaseNameEl) diseaseNameEl.innerText = result.diseaseName;
    if (bodyPartEl) bodyPartEl.innerText = result.bodyPartLabel;
    if (confidenceEl) confidenceEl.innerText = Math.round(result.confidence) + "%";
    if (descriptionEl) descriptionEl.innerText = result.description;

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

    // Dynamic Specialist Discovery Widget
    if (window.appointmentManager && typeof window.appointmentManager.renderSpecialistWidget === "function") {
      const docType = result.doctorType || "General Physician";
      window.appointmentManager.renderSpecialistWidget(
        "scanner-specialist-container", 
        docType, 
        "Based on evaluated image indicators, consulting a specialist is recommended for formal clinical evaluation. This recommendation is educational and does not replace a medical diagnosis."
      );
    }

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


  sanitizeWellnessText(text) {
    if (!text || typeof text !== "string") return "";
    let clean = text;
    clean = clean.replace(/\b(cures|curing)\b/gi, "traditionally used for");
    clean = clean.replace(/\b(treats|treating)\b/gi, "traditionally referenced for");
    clean = clean.replace(/\bwill\s+eliminate\b/gi, "commonly used in traditional wellness practices for");
    clean = clean.replace(/\bclears?\s+the\s+disease\b/gi, "commonly associated with traditional balance");
    clean = clean.replace(/\breverses?\b/gi, "traditionally associated with");
    clean = clean.replace(/\bguarantees?\s+recovery\b/gi, "used in traditional wellness routines");
    clean = clean.replace(/\bheals?\s+the\s+infection\b/gi, "traditionally referenced for recovery support");
    clean = clean.replace(/\boptimizes?\s+organ\s+function\b/gi, "supports general organ wellness");
    clean = clean.replace(/\bdetoxifies?\s+the\s+disease\b/gi, "traditionally used for wellness balance");
    clean = clean.replace(/\breplaces?\s+medical\s+treatment\b/gi, "complements general lifestyle wellness");
    return clean;
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
            <div style="font-size: 0.85rem; color: var(--text-secondary); margin: 2px 0;"><strong>${labelIngredients}:</strong> ${this.sanitizeWellnessText(r.ingredients)}</div>
            <div style="font-size: 0.85rem; color: var(--text-secondary); margin: 2px 0;"><strong>${labelMethod}:</strong> ${this.sanitizeWellnessText(r.method)}</div>
            <div style="font-size: 0.85rem; color: var(--text-secondary);"><strong>${labelUse}:</strong> ${this.sanitizeWellnessText(r.use || "General lifestyle support.")}</div>
          </div>
        `).join("");
      } else {
        naturalContainer.innerHTML = `<p>${window.i18n?.t("noRemedies") || "Standard clinical care recommended."}</p>`;
      }
    }

    if (ayurvedicContainer) {
      const disclaimerHtml = `
        <div style="background: rgba(255, 152, 0, 0.08); border: 1px solid rgba(255, 152, 0, 0.25); border-radius: 8px; padding: 12px 16px; margin-bottom: 14px; font-size: 0.82rem; color: var(--text-secondary); line-height: 1.5;">
          <strong>Disclaimer:</strong> This section provides general traditional wellness information for educational purposes only. It is not a diagnosis or treatment recommendation. Herbal products and supplements may interact with medicines or may not be appropriate for everyone. Consult a qualified healthcare professional before using them, especially when symptoms are persistent or when the image analysis result indicates an abnormal finding.
        </div>
      `;

      const isAbnormal = result.severityRaw?.toLowerCase() === "critical" ||
                         result.severityRaw?.toLowerCase() === "high" ||
                         result.severityRaw?.toLowerCase() === "moderate" ||
                         (result.condition && !result.condition.toLowerCase().includes("normal") && !result.condition.toLowerCase().includes("healthy"));

      const highRiskReminderHtml = isAbnormal ? `
        <div style="background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.25); border-radius: 8px; padding: 10px 14px; margin-bottom: 14px; font-size: 0.82rem; color: var(--color-danger); line-height: 1.5;">
          <strong>Important:</strong> Traditional wellness information should not replace professional medical evaluation. Please consult a qualified healthcare professional for persistent symptoms, concerning findings, or worsening condition.
        </div>
      ` : "";

      const isLowConfidence = result.isLowConfidence ||
                              result.condition?.toLowerCase().includes("inconclusive") ||
                              result.condition?.toLowerCase().includes("low confidence");

      if (isLowConfidence) {
        ayurvedicContainer.innerHTML = disclaimerHtml + `
          <div style="padding: 12px; font-size: 0.85rem; color: var(--text-secondary); line-height: 1.5; font-style: italic;">
            The scan result is inconclusive. Consult a qualified healthcare professional for persistent symptoms or concerning findings before using any traditional wellness routines.
          </div>
        `;
      } else if (result.ayurveda && result.ayurveda.length > 0) {
        const itemsHtml = result.ayurveda.map(a => `
          <div class="ayurveda-item" style="margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid rgba(255,152,0,0.1);">
            <div style="font-weight: 700; color: #ff9800;">${a.name} <span style="font-weight: 400; opacity: 0.8; font-size: 0.9em;">(${a.sanskrit})</span></div>
            <div style="font-size: 0.85rem; color: var(--text-secondary);"><strong>${window.i18n?.t("dosage") || "Dosage"}:</strong> ${a.dosage}</div>
            <div style="font-size: 0.85rem; color: var(--text-secondary);"><strong>${labelUse}:</strong> ${this.sanitizeWellnessText(a.use)}</div>
          </div>
        `).join("");

        ayurvedicContainer.innerHTML = disclaimerHtml + highRiskReminderHtml + itemsHtml;
      } else {
        ayurvedicContainer.innerHTML = disclaimerHtml + highRiskReminderHtml + `<p style="font-size: 0.85rem; color: var(--text-muted); font-style: italic;">Consult a qualified Ayurvedic specialist for specific guidance.</p>`;
      }
    }
  }

}

// Initialize Global Instance
window.scanner = new Scanner();