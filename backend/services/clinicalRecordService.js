const { supabase } = require("../config/supabase");

/**
 * Insert a new clinical record into Supabase clinical_records table
 * Maps incoming API snake_case payload to Supabase table column schema.
 * @param {Object} recordData 
 * @returns {Promise<Object>} Inserted record object
 */
async function createClinicalRecord(recordData) {
  const dbPayload = {
    Name: recordData.patient_name,
    Age: recordData.age,
    Gender: recordData.gender,
    analysis_type: recordData.analysis_type,
    Category: recordData.category,
    Prediction: recordData.prediction,
    Confidence: recordData.confidence,
    Severity: recordData.severity,
    Symptoms: recordData.symptoms,
    Recommendations: recordData.recommendation
  };

  const { data, error } = await supabase
    .from("clinical_records")
    .insert([dbPayload])
    .select();

  if (error) {
    throw new Error(error.message);
  }

  return data ? data[0] : null;
}

module.exports = {
  createClinicalRecord
};
