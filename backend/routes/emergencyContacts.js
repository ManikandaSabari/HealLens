const express = require("express");
const router = express.Router();
const emergencyContactController = require("../controllers/emergencyContactController");

// POST /api/emergency-contacts
router.post("/", emergencyContactController.createContact);

// GET /api/emergency-contacts
router.get("/", emergencyContactController.getContacts);

// PUT /api/emergency-contacts/:id
router.put("/:id", emergencyContactController.updateContact);

// DELETE /api/emergency-contacts/:id
router.delete("/:id", emergencyContactController.deleteContact);

module.exports = router;
