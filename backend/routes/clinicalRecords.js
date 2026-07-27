const express = require("express");
const router = express.Router();
const clinicalRecordController = require("../controllers/clinicalRecordController");

// POST /api/clinical-records
router.post("/", clinicalRecordController.createRecord);

module.exports = router;
