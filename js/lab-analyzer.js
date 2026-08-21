// lab-analyzer.js - Core logic for HealLens Lab Report Analyzer
// Exact Biomarker Extraction & Evaluation Engine (No Defaults, No Integer Rounding, Strict Separation)

class LabAnalyzer {
  constructor() {
    this.patientName   = 'Jane Doe';
    this.patientAge    = 30;
    this.patientGender = 'female'; // 'male' | 'female'

    // Active biomarker extraction state
    this.biomarkers = {
      glucose:       { key: 'glucose',       name: 'Blood Glucose (Sugar)',       unit: 'mg/dL',  val: null, isPresent: false, rawLine: '' },
      cholesterol:   { key: 'cholesterol',   name: 'Total Cholesterol',           unit: 'mg/dL',  val: null, isPresent: false, rawLine: '' },
      triglycerides: { key: 'triglycerides', name: 'Triglycerides',               unit: 'mg/dL',  val: null, isPresent: false, rawLine: '' },
      hemoglobin:    { key: 'hemoglobin',    name: 'Hemoglobin (Hb)',            unit: 'g/dL',   val: null, isPresent: false, rawLine: '' },
      creatinine:    { key: 'creatinine',    name: 'Serum Creatinine',            unit: 'mg/dL',  val: null, isPresent: false, rawLine: '' },
      ast:           { key: 'ast',           name: 'AST (Liver Enzyme)',          unit: 'U/L',    val: null, isPresent: false, rawLine: '' },
      tsh:           { key: 'tsh',           name: 'TSH (Thyroid Stimulating)',   unit: 'uIU/mL', val: null, isPresent: false, rawLine: '' }
    };

    // Values compatibility object
    this.currentValues = {
      glucose: null,
      cholesterol: null,
      triglycerides: null,
      hemoglobin: null,
      creatinine: null,
      ast: null,
      tsh: null
    };

    this.extractedCount = 0;
    this.hasExtractionError = false;

    // Base biomarker reference ranges (PRESERVED EXCLUSIVELY FOR EVALUATION)
    this.biomarkerBase = {
      glucose:       { min: 70,   max: 100,  unit: 'mg/dL',   step: 1,   minLimit: 40,   maxLimit: 300  },
      cholesterol:   { min: 120,  max: 200,  unit: 'mg/dL',   step: 5,   minLimit: 80,   maxLimit: 400  },
      triglycerides: { min: 50,   max: 150,  unit: 'mg/dL',   step: 5,   minLimit: 30,   maxLimit: 500  },
      hemoglobin:    { min: 12.0, max: 17.0, unit: 'g/dL',    step: 0.1, minLimit: 5.0,  maxLimit: 22.0 },
      creatinine:    { min: 0.6,  max: 1.2,  unit: 'mg/dL',   step: 0.1, minLimit: 0.2,  maxLimit: 5.0  },
      ast:           { min: 10,   max: 40,   unit: 'U/L',     step: 1,   minLimit: 5,    maxLimit: 200  },
      tsh:           { min: 0.4,  max: 4.5,  unit: 'uIU/mL',  step: 0.1, minLimit: 0.05, maxLimit: 15.0 }
    };

    // Sample data for manual simulation button clicks ONLY
    this.samples = {
      healthy: {
        glucose: 85,
        cholesterol: 170,
        triglycerides: 115,
        hemoglobin: 14.8,
        creatinine: 0.8,
        ast: 22,
        tsh: 1.8
      },
      lipid: {
        glucose: 95,
        cholesterol: 255,
        triglycerides: 220,
        hemoglobin: 13.5,
        creatinine: 0.9,
        ast: 35,
        tsh: 2.2
      },
      diabetic: {
        glucose: 195,
        cholesterol: 185,
        triglycerides: 165,
        hemoglobin: 9.5,
        creatinine: 1.1,
        ast: 45,
        tsh: 5.2
      }
    };

    this.localizedDb = {
      en: {
        name_glucose: "Blood Glucose (Sugar)",
        desc_glucose: "Blood Glucose measures sugar levels in the blood. Elevated levels indicate a result above the reference range, while lower levels indicate below-range values.",
        
        name_cholesterol: "Total Cholesterol",
        desc_cholesterol: "Total Cholesterol measures lipid levels in blood. Higher levels reflect fat transport markers in the vascular system.",
        
        name_triglycerides: "Triglycerides",
        desc_triglycerides: "Triglycerides represent circulating storage fat. Values outside reference ranges reflect dietary and metabolic balance.",
        
        name_hemoglobin: "Hemoglobin (Hb)",
        desc_hemoglobin: "Hemoglobin carries oxygen in red blood cells. Lower values reflect reduced oxygen-carrying capacity in blood.",
        
        name_creatinine: "Serum Creatinine",
        desc_creatinine: "Creatinine is a muscle breakdown byproduct filtered by the kidneys. Elevated levels indicate values above expected renal filtration markers.",
        
        name_ast: "AST (Liver Enzyme)",
        desc_ast: "AST is a cellular enzyme present in liver and muscle tissue. Elevated levels indicate enzyme release above standard reference limits.",
        
        name_tsh: "TSH (Thyroid Stimulating Hormone)",
        desc_tsh: "TSH regulates thyroid hormone output from the pituitary. Values outside range reflect pituitary-thyroid feedback signaling.",

        statusNormal: "Normal",
        statusLow: "Low",
        statusHigh: "High",
        statusCritical: "Critical",
        normalRange: "Reference range"
      }
    };
  }

  getRanges() {
    return JSON.parse(JSON.stringify(this.biomarkerBase));
  }

  resetBiomarkerState() {
    Object.keys(this.biomarkers).forEach(key => {
      this.biomarkers[key].val = null;
      this.biomarkers[key].isPresent = false;
      this.biomarkers[key].rawLine = '';
      this.currentValues[key] = null;
    });
    this.extractedCount = 0;
    this.hasExtractionError = false;
  }

  init() {
    this.setupUploadHandlers();
    this.renderInputs();
    this.setupButtons();

    const resultCard = document.getElementById("lab-result-card");
    if (resultCard) resultCard.style.display = "none";
  }

  resetOrRender() {
    this.resetBiomarkerState();
    this.renderInputs();
    const resultCard = document.getElementById("lab-result-card");
    if (resultCard) resultCard.style.display = "none";
  }

  getTranslation(key) {
    const lang = window.i18n?.currentLang || "en";
    const db = this.localizedDb[lang] || this.localizedDb["en"];
    return db[key] || key;
  }

  setupUploadHandlers() {
    const dropzone = document.getElementById("lab-upload-area");
    const fileInput = document.getElementById("lab-file-input");
    const browseBtn = document.getElementById("lab-browse-btn");

    if (!dropzone || !fileInput || !browseBtn) return;

    browseBtn.addEventListener("click", () => fileInput.click());

    fileInput.addEventListener("change", (e) => {
      if (e.target.files.length > 0) {
        this.processUploadedFile(e.target.files[0]);
      }
    });

    dropzone.addEventListener("dragover", (e) => {
      e.preventDefault();
      dropzone.classList.add("drag-over");
    });

    dropzone.addEventListener("dragleave", () => {
      dropzone.classList.remove("drag-over");
    });

    dropzone.addEventListener("drop", (e) => {
      e.preventDefault();
      dropzone.classList.remove("drag-over");
      if (e.dataTransfer.files.length > 0) {
        this.processUploadedFile(e.dataTransfer.files[0]);
      }
    });
  }

  async processUploadedFile(file) {
    const dropzone = document.getElementById("lab-upload-area");
    const scanningArea = document.getElementById("lab-scanning-area");
    const subtext = document.getElementById("lab-analyzing-subtext");

    if (dropzone) dropzone.style.display = "none";
    if (scanningArea) scanningArea.style.display = "block";

    // Reset state before every extraction
    this.resetBiomarkerState();

    console.log("[LabAnalyzer Debug] FILE NAME:", file.name);
    console.log("[LabAnalyzer Debug] FILE SIZE:", file.size, "bytes");
    console.log("[LabAnalyzer Debug] FILE TYPE:", file.type);

    if (subtext) subtext.innerText = "Preprocessing document & extracting text...";

    try {
      let rawExtractedText = "";

      if (file.type.includes("text") || file.name.endsWith(".txt") || file.name.endsWith(".csv")) {
        rawExtractedText = await file.text();
      } else if (file.type.includes("pdf") || file.name.toLowerCase().endsWith(".pdf")) {
        if (subtext) subtext.innerText = "Parsing PDF document with PDF.js engine...";
        rawExtractedText = await this.extractPdfTextWithPdfJs(file);
      } else {
        if (subtext) subtext.innerText = "Running neural OCR on uploaded document...";
        if (typeof Tesseract === "undefined") {
          await this.loadTesseractScript();
        }

        if (typeof Tesseract !== "undefined") {
          const worker = await Tesseract.createWorker("eng");
          const ret = await worker.recognize(file);
          rawExtractedText = ret.data.text;
          await worker.terminate();
        } else {
          throw new Error("OCR engine unavailable.");
        }
      }

      console.log("[LabAnalyzer Debug] RAW EXTRACTED TEXT LENGTH:", rawExtractedText.length);
      console.log("[LabAnalyzer Debug] RAW EXTRACTED TEXT:\n", rawExtractedText);

      const normalizedText = this.normalizeExtractedText(rawExtractedText);
      console.log("[LabAnalyzer Debug] NORMALIZED TEXT LENGTH:", normalizedText.length);
      console.log("[LabAnalyzer Debug] NORMALIZED TEXT:\n", normalizedText);

      if (subtext) subtext.innerText = "Validating extracted biomarkers & laboratory metrics...";
      await new Promise(r => setTimeout(r, 400));

      const count = this.extractBiomarkersFromText(normalizedText);

      console.log("[LabAnalyzer Debug] EXTRACTED BIOMARKERS:", JSON.parse(JSON.stringify(this.currentValues)));
      console.log("[LabAnalyzer Debug] EXTRACTED BIOMARKER COUNT:", count);

      if (scanningArea) scanningArea.style.display = "none";
      if (dropzone) dropzone.style.display = "flex";

      if (count === 0) {
        this.renderInsufficientDataNotice();
      } else {
        this.renderInputs();
        this.analyze();
        this.saveCurrentReportToHistory();

        const resultCard = document.getElementById("lab-result-card");
        if (resultCard) {
          resultCard.style.display = "block";
          resultCard.scrollIntoView({ behavior: "smooth" });
        }
      }

    } catch (err) {
      console.warn("[LabAnalyzer] File extraction notice:", err);
      if (scanningArea) scanningArea.style.display = "none";
      if (dropzone) dropzone.style.display = "flex";
      this.renderInsufficientDataNotice();
    }
  }

  loadPdfJsScript() {
    return new Promise((resolve, reject) => {
      if (typeof window.pdfjsLib !== "undefined") {
        window.pdfjsLib.GlobalWorkerOptions.workerSrc = "https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.worker.min.js";
        return resolve();
      }
      const script = document.createElement("script");
      script.src = "https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.min.js";
      script.onload = () => {
        if (typeof window.pdfjsLib !== "undefined") {
          window.pdfjsLib.GlobalWorkerOptions.workerSrc = "https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.worker.min.js";
        }
        resolve();
      };
      script.onerror = () => reject(new Error("Failed to load PDF.js script"));
      document.head.appendChild(script);
    });
  }

  async extractPdfTextWithPdfJs(file) {
    console.log("[LabAnalyzer Debug] PDF EXTRACTION STARTED");
    await this.loadPdfJsScript();

    if (typeof window.pdfjsLib === "undefined") {
      const arrayBuffer = await file.arrayBuffer();
      return this.extractPdfTextDirect(arrayBuffer);
    }

    const arrayBuffer = await file.arrayBuffer();
    const loadingTask = window.pdfjsLib.getDocument({ data: arrayBuffer });
    const pdf = await loadingTask.promise;

    console.log("[LabAnalyzer Debug] PDF PAGE COUNT:", pdf.numPages);

    let combinedText = "";

    for (let pageNum = 1; pageNum <= pdf.numPages; pageNum++) {
      const page = await pdf.getPage(pageNum);
      const textContent = await page.getTextContent();
      
      let pageText = "";
      let lastY = null;
      for (const item of textContent.items) {
        if (!item || !item.str) continue;
        const currentY = item.transform ? item.transform[5] : null;
        if (lastY !== null && currentY !== null && Math.abs(currentY - lastY) > 5) {
          pageText += "\n";
        } else if (item.hasEOL) {
          pageText += "\n";
        } else if (pageText.length > 0 && !pageText.endsWith("\n") && !pageText.endsWith(" ")) {
          pageText += " ";
        }
        pageText += item.str;
        if (currentY !== null) lastY = currentY;
      }

      console.log(`[LabAnalyzer Debug] PAGE ${pageNum} TEXT LENGTH:`, pageText.length);
      combinedText += pageText + "\n";
    }

    console.log("[LabAnalyzer Debug] COMBINED PDF TEXT LENGTH:", combinedText.trim().length);

    if (combinedText.trim().length < 10) {
      console.log("[LabAnalyzer Debug] OCR FALLBACK USED: YES (Scanned Image PDF)");
      combinedText = "";

      if (typeof Tesseract === "undefined") {
        await this.loadTesseractScript();
      }

      for (let pageNum = 1; pageNum <= pdf.numPages; pageNum++) {
        const page = await pdf.getPage(pageNum);
        const viewport = page.getViewport({ scale: 2.0 });
        const canvas = document.createElement("canvas");
        const context = canvas.getContext("2d");
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        await page.render({ canvasContext: context, viewport: viewport }).promise;

        if (typeof Tesseract !== "undefined") {
          const worker = await Tesseract.createWorker("eng");
          const ret = await worker.recognize(canvas);
          const ocrPageText = ret.data.text || "";
          console.log(`[LabAnalyzer Debug] PAGE ${pageNum} OCR TEXT LENGTH:`, ocrPageText.length);
          combinedText += ocrPageText + "\n";
          await worker.terminate();
        }
      }
    } else {
      console.log("[LabAnalyzer Debug] OCR FALLBACK USED: NO (Digital PDF)");
    }

    return combinedText;
  }

  extractPdfTextDirect(arrayBuffer) {
    try {
      const bytes = new Uint8Array(arrayBuffer);
      let content = "";
      for (let i = 0; i < bytes.length; i++) {
        content += String.fromCharCode(bytes[i]);
      }

      const textChunks = [];
      let i = 0;
      const n = content.length;

      while (i < n) {
        if (content[i] === '(') {
          i++;
          let depth = 1;
          const chunk = [];
          while (i < n && depth > 0) {
            const c = content[i];
            if (c === '\\' && i + 1 < n) {
              chunk.push(content[i + 1]);
              i += 2;
              continue;
            } else if (c === '(') {
              depth++;
              chunk.push('(');
            } else if (c === ')') {
              depth--;
              if (depth > 0) chunk.push(')');
            } else {
              chunk.push(c);
            }
            i++;
          }
          if (depth === 0) {
            const s = chunk.join("").trim();
            if (s.length > 0) {
              textChunks.push(s);
            }
          }
        } else {
          i++;
        }
      }

      return textChunks.join("\n");
    } catch (e) {
      console.warn("[LabAnalyzer] Direct PDF text extraction error:", e);
      return "";
    }
  }

  normalizeExtractedText(rawText) {
    if (!rawText || typeof rawText !== "string") return "";

    let text = rawText;

    text = text.replace(/[\u00A0\u1680\u180E\u2000-\u200B\u202F\u205F\u3000]/g, " ");
    text = text.replace(/\s*[:=–—]\s*/g, " : ");
    text = text.replace(/\.{2,}/g, " ");

    text = text.replace(/mg\s*\/\s*dL/gi, "mg/dL");
    text = text.replace(/g\s*\/\s*dL/gi, "g/dL");
    text = text.replace(/uIU\s*\/\s*mL/gi, "uIU/mL");
    text = text.replace(/µIU\s*\/\s*mL/gi, "uIU/mL");
    text = text.replace(/U\s*\/\s*L/gi, "U/L");

    const testKeywords = [
      "Blood Glucose", "Fasting Glucose", "Random Glucose", "Fasting Blood Sugar", "Random Blood Sugar",
      "Total Cholesterol", "Serum Cholesterol", "Triglycerides", "Serum Triglycerides",
      "Hemoglobin", "Haemoglobin", "Serum Creatinine", "AST", "SGOT", "TSH", "Thyroid Stimulating"
    ];

    for (const kw of testKeywords) {
      const reKw = new RegExp(`([^\\n])\\s*(${kw})`, "gi");
      text = text.replace(reKw, "$1\n$2");
    }

    const lines = text.split(/\r?\n/).map(l => l.trim()).filter(l => l.length > 0);
    return lines.join("\n");
  }

  loadTesseractScript() {
    return new Promise((resolve, reject) => {
      if (typeof Tesseract !== "undefined") return resolve();
      const script = document.createElement("script");
      script.src = "https://cdn.jsdelivr.net/npm/tesseract.js@5/dist/tesseract.min.js";
      script.onload = () => resolve();
      script.onerror = () => reject(new Error("Failed to load Tesseract.js"));
      document.head.appendChild(script);
    });
  }

  extractBiomarkersFromText(rawText) {
    this.resetBiomarkerState();

    if (!rawText || typeof rawText !== "string" || rawText.trim().length === 0) {
      return 0;
    }

    const lines = rawText.split(/\r?\n/).map(l => l.trim()).filter(l => l.length > 0);

    const rules = [
      {
        key: 'glucose',
        aliases: [
          /fasting\s+blood\s+sugar/i,
          /random\s+blood\s+sugar/i,
          /fasting\s+blood\s+glucose/i,
          /random\s+blood\s+glucose/i,
          /blood\s+glucose/i,
          /fasting\s+glucose/i,
          /random\s+glucose/i,
          /blood\s+sugar/i,
          /\bfbs\b/i,
          /\brbs\b/i,
          /\bglucose\b/i
        ],
        exclusions: [/hba1c/i, /hb\s*a1c/i, /glycated/i, /urine/i, /microalbumin/i],
        minValid: 20,
        maxValid: 600
      },
      {
        key: 'cholesterol',
        aliases: [
          /total\s+cholesterol/i,
          /cholesterol[,\s]+total/i,
          /serum\s+cholesterol/i,
          /cholesterol\s+total/i,
          /\bcholesterol\b/i
        ],
        exclusions: [/hdl/i, /ldl/i, /vldl/i, /non-hdl/i, /ratio/i],
        minValid: 50,
        maxValid: 600
      },
      {
        key: 'triglycerides',
        aliases: [
          /triglycerides/i,
          /triglyceride/i,
          /serum\s+triglycerides/i,
          /\btg\b/i
        ],
        exclusions: [/hdl/i, /ldl/i, /vldl/i, /ratio/i],
        minValid: 20,
        maxValid: 1000
      },
      {
        key: 'hemoglobin',
        aliases: [
          /hemoglobin/i,
          /haemoglobin/i,
          /\bhb\b/i,
          /\bhgb\b/i
        ],
        exclusions: [/hba1c/i, /hb\s*a1c/i, /mch/i, /mchc/i, /mcv/i, /electrophoresis/i],
        minValid: 3.0,
        maxValid: 25.0
      },
      {
        key: 'creatinine',
        aliases: [
          /serum\s+creatinine/i,
          /creatinine[,\s]+serum/i,
          /\bcreatinine\b/i
        ],
        exclusions: [/clearance/i, /urine/i, /ratio/i, /bun/i, /urea/i, /egfr/i],
        minValid: 0.1,
        maxValid: 15.0
      },
      {
        key: 'ast',
        aliases: [
          /aspartate\s+aminotransferase/i,
          /aspartate\s+transaminase/i,
          /\bast\b/i,
          /\bsgot\b/i
        ],
        exclusions: [/alt/i, /sgpt/i, /ratio/i, /ast\/alt/i, /sgot\/sgpt/i],
        minValid: 2,
        maxValid: 1000
      },
      {
        key: 'tsh',
        aliases: [
          /thyroid\s+stimulating\s+hormone/i,
          /thyrotropin/i,
          /serum\s+tsh/i,
          /\btsh\b/i
        ],
        exclusions: [/free\s+t3/i, /free\s+t4/i, /ft3/i, /ft4/i, /\bt3\b/i, /\bt4\b/i, /anti-tpo/i],
        minValid: 0.01,
        maxValid: 100.0
      }
    ];

    let count = 0;

    for (const rule of rules) {
      for (const line of lines) {
        if (rule.exclusions.some(ex => ex.test(line))) {
          continue;
        }

        const matchedAlias = rule.aliases.find(al => al.test(line));
        if (matchedAlias) {
          const numMatches = line.match(/\b\d+(?:\.\d+)?\b/g);
          if (numMatches) {
            for (const numStr of numMatches) {
              const numVal = parseFloat(numStr);
              if (!isNaN(numVal) && numVal >= rule.minValid && numVal <= rule.maxValid) {
                this.biomarkers[rule.key].val = numVal;
                this.biomarkers[rule.key].isPresent = true;
                this.biomarkers[rule.key].rawLine = line;
                this.currentValues[rule.key] = numVal;
                count++;
                break;
              }
            }
          }
        }
        if (this.biomarkers[rule.key].isPresent) {
          break;
        }
      }
    }

    this.extractedCount = count;
    return count;
  }

  renderInsufficientDataNotice() {
    this.resetBiomarkerState();
    this.renderInputs();

    const resultCard = document.getElementById("lab-result-card");
    if (resultCard) {
      resultCard.style.display = "block";
      
      const visualizer = document.getElementById("lab-range-visualizer");
      const riskOutput = document.getElementById("lab-risk-output");
      const lifestyleOutput = document.getElementById("lab-lifestyle-output");
      const simplifierOutput = document.getElementById("lab-simplifier-output");
      const ayurvedaOutput = document.getElementById("lab-ayurveda-output");

      if (visualizer) {
        visualizer.innerHTML = `
          <div style="background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.25); border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 25px;">
            <div style="font-size: 2.2rem; margin-bottom: 10px;">⚠️</div>
            <h3 style="color: var(--color-danger); font-size: 1.2rem; font-weight: 700; margin-bottom: 8px;">Insufficient Report Data</h3>
            <p style="color: var(--text-secondary); font-size: 0.9rem; max-width: 500px; margin: 0 auto 15px auto; line-height: 1.5;">
              Required biomarkers could not be identified in the uploaded document. Please upload a valid laboratory report containing relevant parameters.
            </p>
            <div style="font-size: 0.8rem; color: var(--text-muted); text-align: left; max-width: 450px; margin: 15px auto 0 auto; background: rgba(0,0,0,0.2); padding: 12px 16px; border-radius: 8px;">
              <strong>Missing Biomarkers:</strong>
              <ul style="margin: 6px 0 0 18px; padding: 0;">
                ${Object.keys(this.biomarkers).map(k => `<li>${this.biomarkers[k].name}: <span style="font-style:italic; color:#ff9800;">Not provided in the report</span></li>`).join('')}
              </ul>
            </div>
          </div>
        `;
      }

      if (riskOutput) {
        riskOutput.innerHTML = `<strong style="color: var(--color-warning);">⚠️ Required biomarkers could not be identified in the uploaded document. No health risks generated.</strong>`;
      }

      if (lifestyleOutput && lifestyleOutput.parentElement) {
        lifestyleOutput.parentElement.style.display = "none";
      }

      if (simplifierOutput) {
        simplifierOutput.innerHTML = `<p style="color: var(--text-muted); font-style: italic;">No biomarkers identified to simplify.</p>`;
      }

      if (ayurvedaOutput && ayurvedaOutput.parentElement) {
        ayurvedaOutput.parentElement.style.display = "none";
      }

      resultCard.scrollIntoView({ behavior: "smooth" });
    }
  }

  saveCurrentReportToHistory() {
    if (!window.historyManager) return;

    let riskSummary = "All Biomarkers within Healthy Ranges";
    let riskLevel = "normal";

    const gl = this.currentValues.glucose;
    const ch = this.currentValues.cholesterol;
    const tr = this.currentValues.triglycerides;
    const hb = this.currentValues.hemoglobin;
    const cr = this.currentValues.creatinine;
    const ast = this.currentValues.ast;
    const tsh = this.currentValues.tsh;

    if (gl !== null && ch !== null && tr !== null && hb !== null && cr !== null && ast !== null && tsh !== null &&
        gl <= 100 && ch <= 200 && tr <= 150 && hb >= 12 && cr <= 1.2 && ast <= 40 && tsh <= 4.5 && tsh >= 0.4) {
      riskSummary = "All Biomarkers within Healthy Ranges";
      riskLevel = "normal";
    } else if (gl !== null && hb !== null && gl > 120 && hb < 11.5) {
      riskSummary = "Diabetic Tendency & Mild Anemia";
      riskLevel = "moderate";
    } else if ((ch !== null && ch > 200) || (tr !== null && tr > 150)) {
      riskSummary = "Hyperlipidemia & Cardiovascular Stress";
      riskLevel = "moderate";
    } else if (ast !== null && cr !== null && ast > 40 && cr > 1.2) {
      riskSummary = "Hepatorenal Stress (Liver & Kidney)";
      riskLevel = "critical";
    } else if (tsh !== null && tsh > 4.5) {
      riskSummary = "Underactive Thyroid (Hypothyroidism)";
      riskLevel = "moderate";
    } else {
      riskSummary = "Isolated Biomarker Elevation";
      riskLevel = "moderate";
    }

    const reportData = {
      patientName: this.patientName || "Jane Doe",
      patientAge: this.patientAge || 30,
      patientGender: this.patientGender || "female",
      memberName: this.patientName || "Jane Doe",
      reportName: "Blood Biomarker Report",
      riskSummary: riskSummary,
      riskLevel: riskLevel,
      biomarkers: JSON.parse(JSON.stringify(this.currentValues)),
      timestamp: new Date().toISOString()
    };

    window.historyManager.addReport(reportData);
  }

  setupButtons() {
    const runBtn = document.getElementById("lab-run-analysis-btn");
    if (runBtn) {
      runBtn.addEventListener("click", () => {
        this.analyze();
        this.saveCurrentReportToHistory();
        const resultCard = document.getElementById("lab-result-card");
        if (resultCard) {
          resultCard.style.display = "block";
          resultCard.scrollIntoView({ behavior: "smooth" });
        }
      });
    }
  }

  loadSample(type) {
    const sample = this.samples[type];
    if (!sample) return;

    this.resetBiomarkerState();

    Object.keys(sample).forEach(key => {
      this.biomarkers[key].val = sample[key];
      this.biomarkers[key].isPresent = true;
      this.currentValues[key] = sample[key];
      const input = document.getElementById(`lab-input-${key}`);
      if (input) input.value = sample[key];
    });

    this.extractedCount = 7;
    this.renderInputs();
    this.analyze();
    this.saveCurrentReportToHistory();
    
    const resultCard = document.getElementById("lab-result-card");
    if (resultCard) {
      resultCard.style.display = "block";
      resultCard.scrollIntoView({ behavior: "smooth" });
    }
  }

  renderInputs() {
    const container = document.getElementById('biomarker-inputs-container');
    if (!container) return;
    const ranges = this.getRanges();
    container.innerHTML = Object.keys(ranges).map(key => {
      const bio   = ranges[key];
      const bmState = this.biomarkers[key];
      const value = bmState.isPresent && bmState.val !== null ? bmState.val : '';
      const name  = this.getTranslation(`name_${key}`);
      const isPresent = bmState.isPresent;

      return `
        <div class="form-group" style="margin-bottom:15px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
            <label class="form-label" for="lab-input-${key}" style="font-weight:600;color:var(--text-primary);">${name}</label>
            <span style="font-size:0.8rem;color:${isPresent ? 'var(--color-primary)' : 'var(--text-muted)'}; font-weight: 500;">
              ${isPresent ? bio.unit : 'Not provided in the report'}
            </span>
          </div>
          <input type="number"
                 class="form-input"
                 id="lab-input-${key}"
                 placeholder="${isPresent ? bio.unit : 'Not provided in the report'}"
                 value="${value}"
                 min="${bio.minLimit}"
                 max="${bio.maxLimit}"
                 step="${bio.step}"
                 oninput="window.labAnalyzer?.updateValue('${key}', this.value)" />
        </div>`;
    }).join('');
  }

  updateValue(key, val) {
    if (val === '' || val === null || val === undefined) {
      this.biomarkers[key].val = null;
      this.biomarkers[key].isPresent = false;
      this.currentValues[key] = null;
    } else {
      const num = parseFloat(val);
      if (!isNaN(num)) {
        this.biomarkers[key].val = num;
        this.biomarkers[key].isPresent = true;
        this.currentValues[key] = num;
      }
    }
    this.analyze();
  }

  updateName(val) {
    this.patientName = val.trim() || 'Jane Doe';
    this.analyze();
  }

  updateAge(val) {
    const n = parseInt(val);
    if (!isNaN(n) && n > 0) this.patientAge = n;
  }

  updateGender(val) {
    this.patientGender = val;
    this.renderInputs();
  }

  reRenderCurrentResult() {
    this.renderInputs();
    this.analyze();
  }

  analyze() {
    const visualizer       = document.getElementById('lab-range-visualizer');
    const riskOutput       = document.getElementById('lab-risk-output');
    const lifestyleBox     = document.getElementById('lab-lifestyle-box');
    const lifestyleOutput  = document.getElementById('lab-lifestyle-output');
    const simplifierOutput = document.getElementById('lab-simplifier-output');
    const ayurvedaOutput   = document.getElementById('lab-ayurveda-output');
    if (!visualizer) return;

    const ranges = this.getRanges();
    let abnCount = 0;
    const abnormalPresentBiomarkers = [];

    console.log("[LabAnalyzer Debug] EVALUATED BIOMARKERS:", JSON.parse(JSON.stringify(this.currentValues)));

    const patientBanner = `
      <div class="patient-banner" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; padding: 12px 18px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); border-radius: var(--radius-md);">
        <div style="display: flex; align-items: center; gap: 10px;">
          <span style="font-size: 1.2rem;">👤</span>
          <div>
            <div style="font-size: 0.72rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px;" data-i18n="patientName">Full Name</div>
            <div style="font-weight: 700; font-size: 1.05rem; color: #fff;">${this.patientName}</div>
          </div>
        </div>
        <div style="text-align: right;">
          <div style="font-size: 0.72rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px;">Analysis Date</div>
          <div style="font-weight: 600; font-size: 0.9rem; color: var(--text-primary);">${new Date().toLocaleDateString()}</div>
        </div>
      </div>
    `;

    const visualizerHtml = Object.keys(ranges).map(key => {
      const bio = ranges[key];
      const bmState = this.biomarkers[key];
      const isPresent = bmState.isPresent && bmState.val !== null;
      const val = bmState.val;
      const name = this.getTranslation(`name_${key}`);
      
      if (!isPresent) {
        return `
          <div class="range-gauge-container" style="margin-bottom: 20px; padding-bottom: 14px; border-bottom: 1px dashed rgba(255,255,255,0.05); opacity: 0.75;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <div style="font-weight: 700; font-family: var(--font-heading); font-size: 0.92rem; color: var(--text-secondary);">${name}</div>
              <div style="display: flex; align-items: center; gap: 10px;">
                <span style="font-size: 0.85rem; font-style: italic; color: var(--text-muted);">Not provided in the report</span>
                <span class="severity-badge severity-mild" style="padding: 2px 10px; font-size: 0.7rem; background: rgba(255,255,255,0.05); color: var(--text-muted);">Not Provided</span>
              </div>
            </div>
          </div>
        `;
      }

      const totalRange = bio.maxLimit - bio.minLimit;
      let pct = ((val - bio.minLimit) / totalRange) * 100;
      pct = Math.max(2, Math.min(98, pct));

      let badgeClass = "severity-mild";
      let statusLabel = this.getTranslation("statusNormal");
      let statusColor = "#10b981";

      if (val < bio.min) {
        badgeClass = "severity-moderate";
        statusLabel = this.getTranslation("statusLow");
        statusColor = "#00d4ff";
        abnCount++;
        abnormalPresentBiomarkers.push({ key, name, val, unit: bio.unit, statusLabel, statusColor, type: 'low' });
      } else if (val > bio.max) {
        const critLimit = bio.max * 1.5;
        if (val > critLimit) {
          badgeClass = "severity-critical";
          statusLabel = this.getTranslation("statusCritical");
          statusColor = "#ef4444";
        } else {
          badgeClass = "severity-moderate";
          statusLabel = this.getTranslation("statusHigh");
          statusColor = "#ff9800";
        }
        abnCount++;
        abnormalPresentBiomarkers.push({ key, name, val, unit: bio.unit, statusLabel, statusColor, type: 'high' });
      }

      const lowZonePct = ((bio.min - bio.minLimit) / totalRange) * 100;
      const greenZonePct = ((bio.max - bio.min) / totalRange) * 100;
      const highZonePct = 100 - (lowZonePct + greenZonePct);

      console.log(`[LabAnalyzer Debug] FINAL UI VALUE (${key}):`, val, bio.unit, `(${statusLabel})`);

      return `
        <div class="range-gauge-container" style="margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px dashed rgba(255,255,255,0.05);">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <div style="font-weight: 700; font-family: var(--font-heading); font-size: 0.96rem; color: #fff;">${name}</div>
            <div style="display: flex; align-items: center; gap: 10px;">
              <span style="font-size: 1.1rem; font-weight: 800; color: var(--color-primary);">${val} <span style="font-size: 0.78rem; font-weight: 500; color: var(--text-secondary);">${bio.unit}</span></span>
              <span class="severity-badge ${badgeClass}" style="padding: 2px 10px; font-size: 0.75rem;">${statusLabel}</span>
            </div>
          </div>
          <div class="gauge-track-wrap" style="position: relative; height: 8px; border-radius: 4px; background: rgba(255,255,255,0.1); overflow: visible; display: flex; margin-bottom: 8px;">
            <div class="gauge-zone zone-low" style="width: ${lowZonePct}%; background: rgba(0, 212, 255, 0.2); border-radius: 4px 0 0 4px;"></div>
            <div class="gauge-zone zone-green" style="width: ${greenZonePct}%; background: rgba(16, 185, 129, 0.3);"></div>
            <div class="gauge-zone zone-high" style="width: ${highZonePct}%; background: rgba(239, 68, 68, 0.25); border-radius: 0 4px 4px 0;"></div>
            <div class="gauge-pin" style="position: absolute; top: -4px; left: calc(${pct}% - 8px); width: 16px; height: 16px; border-radius: 50%; background: #fff; border: 3px solid var(--color-primary); box-shadow: 0 0 10px var(--color-primary); transition: left 0.5s cubic-bezier(0.1, 0.8, 0.3, 1);"></div>
          </div>
          <div style="display: flex; justify-content: space-between; font-size: 0.72rem; color: var(--text-muted);">
            <span>${bio.minLimit}</span>
            <span>${this.getTranslation("normalRange")}: ${bio.min} - ${bio.max} ${bio.unit}</span>
            <span>${bio.maxLimit}</span>
          </div>
        </div>
      `;
    }).join("");

    visualizer.innerHTML = patientBanner + visualizerHtml;

    // Dynamic AI Health Risk Profile & Abnormalities Summary
    const presentCount = Object.keys(this.biomarkers).filter(k => this.biomarkers[k].isPresent && this.biomarkers[k].val !== null).length;

    if (presentCount === 0) {
      if (riskOutput) {
        riskOutput.innerHTML = `<strong style="color: var(--color-warning);">⚠️ Required biomarkers could not be identified in the uploaded document.</strong>`;
      }
    } else if (abnormalPresentBiomarkers.length === 0) {
      if (riskOutput) {
        riskOutput.innerHTML = `
          <div style="color: #10b981; font-weight: 700; font-size: 1rem; margin-bottom: 8px;">✅ Biomarkers Within Configured Reference Ranges</div>
          <p style="color: var(--text-secondary); margin: 0; line-height: 1.5;">
            All biomarkers identified in the uploaded report are within the configured reference ranges. Continue healthy lifestyle habits and discuss your results with your healthcare professional as appropriate.
          </p>
        `;
      }
    } else {
      const abnBullets = abnormalPresentBiomarkers.map(bm => {
        return `<li style="margin-bottom: 4px;"><strong>${bm.name}</strong> — <span style="color: var(--color-primary); font-weight: 700;">${bm.val} ${bm.unit}</span> — <span style="color: ${bm.statusColor}; font-weight: 700;">${bm.statusLabel}</span></li>`;
      }).join('');

      if (riskOutput) {
        riskOutput.innerHTML = `
          <div style="color: var(--color-warning); font-weight: 700; font-size: 1rem; margin-bottom: 8px;">⚠️ Biomarker Abnormalities Detected</div>
          <p style="color: var(--text-secondary); margin-bottom: 12px; line-height: 1.5;">
            Several measured biomarkers are outside the configured reference ranges. Elevated or lower values were detected across evaluated metrics.
          </p>
          <div style="background: rgba(0,0,0,0.25); border-radius: 8px; padding: 14px 18px; margin-bottom: 14px;">
            <div style="font-size: 0.82rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700; letter-spacing: 0.5px; margin-bottom: 8px;">Detected abnormalities:</div>
            <ul style="margin: 0; padding-left: 20px; font-size: 0.9rem; line-height: 1.6;">
              ${abnBullets}
            </ul>
          </div>
          <p style="color: var(--text-secondary); font-size: 0.85rem; font-style: italic; margin: 0; line-height: 1.5;">
            These results should be reviewed with a qualified healthcare professional. The report analyzer provides educational interpretation and does not establish a medical diagnosis.
          </p>
        `;
      }
    }

    // 🌱 Natural Lifestyle & Wellness Guidance (Dynamic for present abnormal biomarkers ONLY)
    if (lifestyleOutput) {
      if (abnormalPresentBiomarkers.length === 0) {
        if (lifestyleBox) lifestyleBox.style.display = "none";
      } else {
        if (lifestyleBox) lifestyleBox.style.display = "block";

        const lifestyleHtml = abnormalPresentBiomarkers.map(bm => {
          let advice = "";
          if (bm.key === "glucose") {
            advice = bm.type === "high"
              ? "Reduce sugary drinks and foods with large amounts of added sugar. Prefer fiber-rich whole foods such as vegetables, legumes and whole grains. Maintain regular physical activity appropriate for your circumstances and stay well hydrated."
              : "Maintain balanced meals with complex carbohydrates to stabilize blood glucose. Discuss persistent low glucose episodes with a healthcare professional.";
          } else if (bm.key === "cholesterol") {
            advice = "Favor vegetables, fruits, legumes and whole grains. Include sources of dietary fiber and limit foods high in saturated and trans fats. Prefer healthier unsaturated fat sources where appropriate and maintain regular physical activity.";
          } else if (bm.key === "triglycerides") {
            advice = bm.type === "high"
              ? "Reduce foods and beverages high in added sugar. Limit excessive refined carbohydrates. Prefer balanced meals containing vegetables, fiber and appropriate protein. Maintain regular physical activity and limit alcohol consumption."
              : "Your triglyceride value is below the configured reference range. Maintain a balanced diet and discuss persistently low results with a healthcare professional, particularly if other symptoms or abnormal laboratory results are present.";
          } else if (bm.key === "hemoglobin") {
            advice = bm.type === "low"
              ? "Maintain a balanced diet containing iron-rich foods alongside appropriate sources of vitamin C to support dietary absorption. Discuss the result with a healthcare professional to determine the underlying cause."
              : "Maintain adequate hydration and discuss persistently elevated hemoglobin levels with a healthcare professional to evaluate hydration or environmental influences.";
          } else if (bm.key === "creatinine") {
            advice = "Your creatinine value is above the configured reference range. Adequate hydration may be appropriate unless a healthcare professional has advised fluid restriction. Avoid self-prescribing supplements or herbal products based solely on this result. An elevated creatinine result should be discussed with a qualified healthcare professional because interpretation depends on factors such as kidney function, muscle mass, medications and other clinical information.";
          } else if (bm.key === "ast") {
            advice = "Avoid excessive alcohol consumption. Maintain a balanced diet and avoid unnecessary supplements or unprescribed medications unless recommended by a healthcare professional. Discuss persistent elevation with a qualified healthcare professional.";
          } else if (bm.key === "tsh") {
            advice = "Your TSH value is outside the configured reference range. TSH interpretation can depend on additional thyroid tests and clinical context. Discuss the result with a qualified healthcare professional. Do not start thyroid supplements, iodine products or herbal treatments based solely on this report.";
          }

          return `
            <div style="padding: 12px 14px; border-radius: 8px; background: rgba(16, 185, 129, 0.04); border-left: 3px solid #10b981;">
              <div style="font-weight: 700; font-size: 0.9rem; color: #10b981; margin-bottom: 4px;">
                ${bm.name} (${bm.val} ${bm.unit}) — ${bm.statusLabel}
              </div>
              <p style="font-size: 0.85rem; color: var(--text-secondary); margin: 0; line-height: 1.5;">${advice}</p>
            </div>
          `;
        }).join("");

        lifestyleOutput.innerHTML = lifestyleHtml;
      }
    }

    // Patient Report Simplifier Engine
    const simplifiedHtml = Object.keys(ranges).map(key => {
      const bio = ranges[key];
      const bmState = this.biomarkers[key];
      const isPresent = bmState.isPresent && bmState.val !== null;
      const val = bmState.val;
      const name = this.getTranslation(`name_${key}`);
      const desc = this.getTranslation(`desc_${key}`);

      if (!isPresent) {
        return `
          <div style="padding: 10px; border-radius: 8px; background: transparent; border-left: 3px solid rgba(255,255,255,0.05); opacity: 0.7;">
            <div style="font-weight: 600; font-size: 0.88rem; color: var(--text-muted); margin-bottom: 3px;">
              ${name} — <span style="font-style: italic;">Not provided in the report</span>
            </div>
            <p style="font-size: 0.8rem; color: var(--text-muted); line-height: 1.4;">${desc}</p>
          </div>
        `;
      }

      let highlight = false;
      if (val < bio.min || val > bio.max) highlight = true;

      return `
        <div style="padding: 10px; border-radius: 8px; background: ${highlight ? "rgba(255,158,11,0.03)" : "transparent"}; border-left: 3px solid ${highlight ? "var(--color-warning)" : "rgba(255,255,255,0.05)"};">
          <div style="font-weight: 700; font-size: 0.88rem; color: ${highlight ? "var(--color-warning)" : "var(--text-primary)"}; margin-bottom: 3px;">
            ${name} (${val} ${bio.unit})
          </div>
          <p style="font-size: 0.8rem; color: var(--text-secondary); line-height: 1.4;">${desc}</p>
        </div>
      `;
    }).join("");

    if (simplifierOutput) {
      simplifierOutput.innerHTML = simplifiedHtml;
    }

    // 🌿 Traditional / Ayurvedic Wellness Information (Educational, separate, with safety disclaimer)
    if (ayurvedaOutput) {
      if (abnormalPresentBiomarkers.length === 0) {
        if (ayurvedaOutput.parentElement) ayurvedaOutput.parentElement.style.display = "none";
      } else {
        if (ayurvedaOutput.parentElement) ayurvedaOutput.parentElement.style.display = "block";

        const disclaimerHeader = `
          <div style="grid-column: 1 / -1; background: rgba(255, 158, 11, 0.08); border: 1px solid rgba(255, 158, 11, 0.25); border-radius: 8px; padding: 12px 16px; margin-bottom: 10px; font-size: 0.82rem; color: var(--text-secondary); line-height: 1.5;">
            <strong>Disclaimer:</strong> This section provides general traditional wellness information for educational purposes only. It is not a diagnosis or treatment recommendation. Herbal products and supplements may interact with medicines or may not be appropriate for everyone. Consult a qualified healthcare professional before using them, especially when laboratory results are abnormal.
          </div>
        `;

        const ayurvedaHtml = abnormalPresentBiomarkers.map(bm => {
          let ayurText = "";
          if (bm.key === "glucose") {
            ayurText = "Traditional practices involving herbs like Nisha Amalaki (Turmeric + Amla) or Fenugreek seeds are traditionally used in Ayurveda for general metabolic wellness routines. These should not replace medical sugar management.";
          } else if (bm.key === "cholesterol") {
            ayurText = "Garlic (Lashuna) and Arjuna bark are traditionally referenced in Ayurveda to support general cardiovascular wellness alongside a balanced diet.";
          } else if (bm.key === "triglycerides") {
            ayurText = "Triphala powder taken with warm water is a traditional Ayurvedic digestive routine. It should be used as part of general lifestyle balance, not as a replacement for medical guidance.";
          } else if (bm.key === "hemoglobin") {
            ayurText = "Traditional foods like Pomegranate (Dadima) and Ayurvedic preparations like Lohasava are traditionally used to support general blood nutrition. Professional medical evaluation is required to identify the root cause of low hemoglobin.";
          } else if (bm.key === "creatinine") {
            ayurText = "Traditional herbs like Punarnava are referenced in Ayurvedic literature for general fluid balance. However, an elevated creatinine level requires professional medical evaluation. Do not use herbal products as a substitute for medical renal care.";
          } else if (bm.key === "ast") {
            ayurText = "Bhumi Amla and Giloy are traditionally mentioned in Ayurveda for general liver support routines. Persistent liver enzyme elevation warrants clinical medical evaluation.";
          } else if (bm.key === "tsh") {
            ayurText = "Traditional routines like Dhania (coriander) water or gentle neck massage with warm sesame oil are sometimes used in Ayurvedic lifestyle routines. Thyroid hormone balance requires medical supervision.";
          }

          return `
            <div class="ayurveda-map-card" style="padding: var(--space-md); border-color: rgba(255, 158, 11, 0.25); background: rgba(8, 13, 26, 0.6);">
              <div class="ayurveda-map-name" style="color: var(--color-warning);">🌿 Traditional Note: ${bm.name} (${bm.val} ${bm.unit})</div>
              <div class="ayurveda-map-use" style="font-size: 0.82rem; line-height: 1.5; color: var(--text-secondary); margin-top: 6px;">
                ${ayurText}
              </div>
            </div>
          `;
        }).join("");

        ayurvedaOutput.style.display = "grid";
        ayurvedaOutput.innerHTML = disclaimerHeader + ayurvedaHtml;
      }
    }

    // 🩺 Consult a Specialist (Dynamic Location-Based Provider Discovery)
    let suggestedSpecialty = "General Physician";
    if (abnormalPresentBiomarkers.length === 1) {
      const abnKey = abnormalPresentBiomarkers[0].key;
      if (abnKey === "glucose" || abnKey === "tsh") suggestedSpecialty = "Endocrinologist";
      else if (abnKey === "cholesterol" || abnKey === "triglycerides") suggestedSpecialty = "Cardiologist";
      else if (abnKey === "hemoglobin") suggestedSpecialty = "General Physician";
      else if (abnKey === "creatinine") suggestedSpecialty = "Nephrologist";
      else if (abnKey === "ast") suggestedSpecialty = "Gastroenterologist";
    } else if (abnormalPresentBiomarkers.length > 1) {
      suggestedSpecialty = "General Physician";
    }

    if (window.appointmentManager && typeof window.appointmentManager.renderSpecialistWidget === "function") {
      const note = abnormalPresentBiomarkers.length > 0
        ? "Based on evaluated biomarker metrics outside reference ranges, consulting a specialist is recommended for comprehensive clinical review."
        : "All evaluated biomarkers in the report are within configured reference ranges. Consider discussing your general health with a General Physician for routine wellness.";
      window.appointmentManager.renderSpecialistWidget("lab-specialist-container", suggestedSpecialty, note);
    }
  }
}

// Global initialization
document.addEventListener("DOMContentLoaded", () => {
  window.labAnalyzer = new LabAnalyzer();
  window.labAnalyzer.init();

  const originalSetLanguage = window.i18n?.setLanguage;
  if (originalSetLanguage) {
    window.i18n.setLanguage = function(lang) {
      originalSetLanguage.call(window.i18n, lang);
      window.labAnalyzer?.reRenderCurrentResult();
    };
  }
});
