const express = require("express");
const cors = require("cors");
const dotenv = require("dotenv");
const { verifySupabaseConnection } = require("./config/supabase");
const clinicalRecordsRoutes = require("./routes/clinicalRecords");
const emergencyContactsRoutes = require("./routes/emergencyContacts");

// Load environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use("/api/clinical-records", clinicalRecordsRoutes);
app.use("/api/emergency-contacts", emergencyContactsRoutes);

// Root Endpoint
app.get("/", (req, res) => {
  res.send("HealLens Backend is Running 🚀");
});

// Start Server
app.listen(PORT, async () => {
  console.log(`Server running on http://localhost:${PORT}`);
  await verifySupabaseConnection();
});
