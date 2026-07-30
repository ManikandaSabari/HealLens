const { supabase } = require("../config/supabase");

/**
 * Insert a new clinical record into Supabase clinical_records table
 * Maps incoming API snake_case payload to Supabase table column schema.
 * @param {Object} recordData 
 * @returns {Promise<Object>} Inserted record object
 */
async function createClinicalRecord(recordData) {
  const dbPayload = {
    user_id: recordData.user_id || null,
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

/**
 * Retrieve clinical records from Supabase clinical_records table
 * Ordered by created_at descending. Filtered by user_id if provided.
 * @param {string|null} userId
 * @returns {Promise<Array>} List of clinical records
 */
async function getClinicalRecords(userId = null) {
  let query = supabase.from("clinical_records").select("*");

  if (userId) {
    query = query.eq("user_id", userId);
  }

  const { data, error } = await query.order("created_at", { ascending: false });

  if (error) {
    throw new Error(error.message);
  }

  return data || [];
}

module.exports = {
  createClinicalRecord,
  getClinicalRecords
};
