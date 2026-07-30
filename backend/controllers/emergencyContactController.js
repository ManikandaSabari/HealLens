const emergencyContactService = require("../services/emergencyContactService");

/**
 * Controller handlers for /api/emergency-contacts
 */

// POST /api/emergency-contacts
async function createContact(req, res) {
  try {
    const { user_id, patient_name, contact_name, relationship, relation, phone_number, phone } = req.body || {};

    const contactNameVal = contact_name || req.body.name || null;
    const relationVal = relationship || relation;
    const phoneVal = phone_number || phone;

    const missingFields = [];
    if (!patient_name && !contactNameVal) missingFields.push("patient_name or contact_name");
    if (!relationVal) missingFields.push("relationship");
    if (!phoneVal) missingFields.push("phone_number");

    if (missingFields.length > 0) {
      return res.status(400).json({
        success: false,
        error: `Missing required fields: ${missingFields.join(", ")}`
      });
    }

    const payload = {
      user_id: user_id || null,
      patient_name: patient_name || "Self",
      contact_name: contactNameVal,
      relationship: relationVal,
      phone_number: phoneVal
    };

    const newContact = await emergencyContactService.createEmergencyContact(payload);

    return res.status(201).json({
      success: true,
      message: "Emergency contact created successfully",
      data: newContact
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to create emergency contact"
    });
  }
}

// GET /api/emergency-contacts
async function getContacts(req, res) {
  try {
    const userId = req.query.user_id || req.query.userId || null;
    const contacts = await emergencyContactService.getEmergencyContacts(userId);

    return res.status(200).json({
      success: true,
      count: contacts.length,
      data: contacts
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to fetch emergency contacts"
    });
  }
}

// PUT /api/emergency-contacts/:id
async function updateContact(req, res) {
  try {
    const { id } = req.params;
    const { patient_name, relationship, phone_number } = req.body || {};

    if (!id) {
      return res.status(400).json({
        success: false,
        error: "Contact ID is required."
      });
    }

    const updateData = {};
    if (patient_name !== undefined) updateData.patient_name = patient_name;
    if (relationship !== undefined) updateData.relationship = relationship;
    if (phone_number !== undefined) updateData.phone_number = phone_number;

    if (Object.keys(updateData).length === 0) {
      return res.status(400).json({
        success: false,
        error: "No fields provided to update."
      });
    }

    const updatedContact = await emergencyContactService.updateEmergencyContact(id, updateData);

    if (!updatedContact) {
      return res.status(404).json({
        success: false,
        error: `Emergency contact with ID ${id} not found.`
      });
    }

    return res.status(200).json({
      success: true,
      message: "Emergency contact updated successfully",
      data: updatedContact
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to update emergency contact"
    });
  }
}

// DELETE /api/emergency-contacts/:id
async function deleteContact(req, res) {
  try {
    const { id } = req.params;

    if (!id) {
      return res.status(400).json({
        success: false,
        error: "Contact ID is required."
      });
    }

    const deletedContact = await emergencyContactService.deleteEmergencyContact(id);

    if (!deletedContact) {
      return res.status(404).json({
        success: false,
        error: `Emergency contact with ID ${id} not found.`
      });
    }

    return res.status(200).json({
      success: true,
      message: "Emergency contact deleted successfully",
      data: deletedContact
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to delete emergency contact"
    });
  }
}

module.exports = {
  createContact,
  getContacts,
  updateContact,
  deleteContact
};
