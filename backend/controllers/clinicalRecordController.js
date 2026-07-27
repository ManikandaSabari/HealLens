const clinicalRecordService = require("../services/clinicalRecordService");

/**
 * Controller handler to process POST /api/clinical-records
 */
async function createRecord(req, res) {
  try {
    const {
      patient_name,
      age,
      gender,
      analysis_type,
      category,
      prediction,
      confidence,
      severity,
      symptoms,
      recommendation
    } = req.body || {};

    // Validate required fields
    const missingFields = [];
    if (!patient_name) missingFields.push("patient_name");
    if (age === undefined || age === null || age === "") missingFields.push("age");
    if (!gender) missingFields.push("gender");
    if (!analysis_type) missingFields.push("analysis_type");
    if (!category) missingFields.push("category");
    if (!prediction) missingFields.push("prediction");

    if (missingFields.length > 0) {
      return res.status(400).json({
        success: false,
        error: `Missing required fields: ${missingFields.join(", ")}`
      });
    }

    // Validate analysis_type enum ('Image' | 'Report')
    const validAnalysisTypes = ["Image", "Report"];
    if (!validAnalysisTypes.includes(analysis_type)) {
      return res.status(400).json({
        success: false,
        error: "Invalid analysis_type. Must be either 'Image' or 'Report'."
      });
    }

    const payload = {
      patient_name,
      age: Number(age),
      gender,
      analysis_type,
      category,
      prediction,
      confidence: confidence !== undefined && confidence !== null && confidence !== "" ? Number(confidence) : null,
      severity: severity || null,
      symptoms: symptoms || null,
      recommendation: recommendation || null
    };

    const createdRecord = await clinicalRecordService.createClinicalRecord(payload);

    return res.status(201).json({
      success: true,
      message: "Clinical record created successfully",
      data: createdRecord
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to create clinical record"
    });
  }
}

module.exports = {
  createRecord
};
