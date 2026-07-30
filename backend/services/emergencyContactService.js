const { supabase } = require("../config/supabase");

/**
 * Service layer for Emergency Contacts database operations
 * Maps API snake_case fields to Supabase database column names.
 */

function formatContact(row) {
  if (!row) return null;
  return {
    id: row.id,
    user_id: row.user_id || null,
    userId: row.user_id || null,
    patient_name: row.Patient_Name,
    contact_name: row.Contact_Name || null,
    name: row.Contact_Name || null,
    relationship: row.Relationship,
    relation: row.Relationship,
    phone_number: row.Phone_Number,
    phone: row.Phone_Number,
    created_at: row.created_at
  };
}

async function createEmergencyContact(data) {
  const dbPayload = {
    user_id: data.user_id || null,
    Patient_Name: data.patient_name,
    Contact_Name: data.contact_name || null,
    Relationship: data.relationship,
    Phone_Number: data.phone_number
  };

  const { data: created, error } = await supabase
    .from("emergency_contacts")
    .insert([dbPayload])
    .select();

  if (error) {
    throw new Error(error.message);
  }

  return created && created.length > 0 ? formatContact(created[0]) : null;
}

async function getEmergencyContacts(userId = null) {
  let query = supabase.from("emergency_contacts").select("*");

  if (userId) {
    query = query.eq("user_id", userId);
  }

  const { data, error } = await query.order("created_at", { ascending: false });

  if (error) {
    throw new Error(error.message);
  }

  return (data || []).map(formatContact);
}

async function updateEmergencyContact(id, data) {
  const dbPayload = {};
  if (data.patient_name !== undefined) dbPayload.Patient_Name = data.patient_name;
  if (data.contact_name !== undefined) dbPayload.Contact_Name = data.contact_name;
  if (data.relationship !== undefined) dbPayload.Relationship = data.relationship;
  if (data.phone_number !== undefined) dbPayload.Phone_Number = data.phone_number;

  const { data: updated, error } = await supabase
    .from("emergency_contacts")
    .update(dbPayload)
    .eq("id", id)
    .select();

  if (error) {
    throw new Error(error.message);
  }

  return updated && updated.length > 0 ? formatContact(updated[0]) : null;
}

async function deleteEmergencyContact(id) {
  const { data: deleted, error } = await supabase
    .from("emergency_contacts")
    .delete()
    .eq("id", id)
    .select();

  if (error) {
    throw new Error(error.message);
  }

  return deleted && deleted.length > 0 ? formatContact(deleted[0]) : null;
}

module.exports = {
  createEmergencyContact,
  getEmergencyContacts,
  updateEmergencyContact,
  deleteEmergencyContact
};
