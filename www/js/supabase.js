/**
 * supabase.js — Supabase Client Configuration
 * Phase 6.2 (Step 2): Frontend Supabase Client Configuration
 * 
 * Reusable Supabase client configuration initialized with project credentials.
 */

// Supabase Project Credentials
const SUPABASE_URL = "https://sqitixlmrrksqdqqboyp.supabase.co";
const SUPABASE_ANON_KEY = "sb_publishable_9KGmq4RT3sRuoPGfy5gsHg_l4lShf3O";

// Initialize the Supabase client if Supabase JS SDK is loaded
let supabaseClient = null;

if (typeof window !== "undefined" && window.supabase && typeof window.supabase.createClient === "function") {
  supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
} else if (typeof createClient === "function") {
  supabaseClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
}

// Export / Attach to global window object for standard browser script access
if (typeof window !== "undefined") {
  window.SUPABASE_URL = SUPABASE_URL;
  window.SUPABASE_ANON_KEY = SUPABASE_ANON_KEY;
  window.supabaseClient = supabaseClient;
}

// Export for CommonJS / Node / Module environments if applicable
if (typeof module !== "undefined" && module.exports) {
  module.exports = {
    SUPABASE_URL,
    SUPABASE_ANON_KEY,
    supabaseClient
  };
}
