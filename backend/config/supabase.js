const { createClient } = require("@supabase/supabase-js");
const dotenv = require("dotenv");

// Ensure environment variables are loaded
dotenv.config();

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !supabaseServiceKey) {
  console.error("❌ Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY in environment variables.");
}

const supabase = createClient(supabaseUrl, supabaseServiceKey);

async function verifySupabaseConnection() {
  try {
    const { data, error } = await supabase
      .from("clinical_records")
      .select("*")
      .limit(1);

    if (error) {
      console.error("❌ Supabase Connection Failed:", error.message);
    } else {
      console.log("✅ Supabase Connected Successfully");
    }
  } catch (err) {
    console.error("❌ Supabase Connection Error:", err.message);
  }
}

module.exports = {
  supabase,
  verifySupabaseConnection
};
