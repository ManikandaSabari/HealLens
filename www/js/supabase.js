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

  // Single-execution tracking flag to prevent duplicate listeners
  let _capacitorDeepLinkInitialized = false;

  // Function to process deep link URL (handles both cold start launchUrl and warm start appUrlOpen)
  async function handleDeepLinkUrl(urlStr) {
    if (!urlStr || typeof urlStr !== 'string' || !urlStr.includes("com.heallens.app://")) return;

    let client = window.supabaseClient;
    if (!client && window.supabase && typeof window.supabase.createClient === "function") {
      client = window.supabase.createClient(window.SUPABASE_URL, window.SUPABASE_ANON_KEY);
    }
    if (!client) return;

    let hash = "";
    let search = "";

    try {
      const parsed = new URL(urlStr);
      hash = parsed.hash || "";
      search = parsed.search || "";
    } catch (e) {
      if (urlStr.includes("#")) hash = "#" + urlStr.split("#")[1];
      if (urlStr.includes("?")) search = "?" + urlStr.split("?")[1].split("#")[0];
    }

    const hashParams = new URLSearchParams(hash.replace(/^#/, ""));
    const searchParams = new URLSearchParams(search.replace(/^\?/, ""));

    const accessToken = hashParams.get("access_token");
    const refreshToken = hashParams.get("refresh_token");
    const code = searchParams.get("code");
    const type = hashParams.get("type") || searchParams.get("type") || (urlStr.includes("type=recovery") ? "recovery" : "");
    const isRecovery = type === "recovery" || urlStr.includes("type=recovery");

    if (isRecovery) {
      window.isPasswordRecovery = true;
      try {
        sessionStorage.setItem("isPasswordRecovery", "true");
      } catch (e) {}
    }

    if (accessToken && refreshToken) {
      const { data: sessionData, error } = await client.auth.setSession({
        access_token: accessToken,
        refresh_token: refreshToken
      });
      if (!error && sessionData && sessionData.session) {
        if (isRecovery) {
          if (!window.location.pathname.endsWith("login.html")) {
            window.location.href = "login.html?type=recovery";
          } else {
            if (typeof openResetPasswordModal === "function") {
              openResetPasswordModal();
            }
          }
        } else {
          if (
            window.isPasswordRecovery === true ||
            (typeof sessionStorage !== 'undefined' && sessionStorage.getItem("isPasswordRecovery") === "true")
          ) {
            console.log("Password recovery active - dashboard redirect blocked");
            return;
          }
          if (!window.location.pathname.endsWith("dashboard.html")) {
            window.location.href = "dashboard.html";
          }
        }
      }
    } else if (code) {
      const { data: sessionData, error } = await client.auth.exchangeCodeForSession(code);
      if (!error && sessionData && sessionData.session) {
        if (isRecovery) {
          if (!window.location.pathname.endsWith("login.html")) {
            window.location.href = "login.html?type=recovery";
          } else {
            if (typeof openResetPasswordModal === "function") {
              openResetPasswordModal();
            }
          }
        } else {
          if (
            window.isPasswordRecovery === true ||
            (typeof sessionStorage !== 'undefined' && sessionStorage.getItem("isPasswordRecovery") === "true")
          ) {
            console.log("Password recovery active - dashboard redirect blocked");
            return;
          }
          if (!window.location.pathname.endsWith("dashboard.html")) {
            window.location.href = "dashboard.html";
          }
        }
      }
    }
  }

  // Setup Deep Link Handler for Capacitor Native Android with safe plugin availability check
  async function setupCapacitorDeepLinkHandler() {
    if (_capacitorDeepLinkInitialized) return true;

    try {
      const isNative = Boolean(window.Capacitor && typeof window.Capacitor.isNativePlatform === "function" && window.Capacitor.isNativePlatform());
      if (!isNative) return true; // Non-native platform: return true so retry loop stops

      const appPlugin = window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.App;
      if (!appPlugin) return false; // Native bridge plugin not ready yet, return false to trigger retry

      _capacitorDeepLinkInitialized = true;

      // 1. Cold Start Check: Retrieve the URL that launched the native app (if launched via deep link)
      if (typeof appPlugin.getLaunchUrl === "function") {
        try {
          const launchData = await appPlugin.getLaunchUrl();
          if (launchData && launchData.url) {
            await handleDeepLinkUrl(launchData.url);
          }
        } catch (lErr) {
          console.error("Error getting launch URL:", lErr);
        }
      }

      // 2. Warm Start Listener: Listen for deep link events while the app is running in background
      if (typeof appPlugin.addListener === "function") {
        appPlugin.addListener("appUrlOpen", async (data) => {
          if (data && data.url) {
            await handleDeepLinkUrl(data.url);
          }
        });
      }

      return true;
    } catch (err) {
      console.error("Error initializing Capacitor deep link handler:", err);
      return false;
    }
  }

  // Robust initialization retry loop to handle early script execution before Capacitor native bridge is ready
  function initDeepLinkWithRetries() {
    if (_capacitorDeepLinkInitialized) return;

    const tryInit = async () => {
      const success = await setupCapacitorDeepLinkHandler();
      if (!success) {
        // Schedule retries at progressive intervals if native plugin was not immediately available
        const delays = [50, 150, 300, 600, 1200, 2500];
        delays.forEach((delay) => {
          setTimeout(async () => {
            if (!_capacitorDeepLinkInitialized) {
              await setupCapacitorDeepLinkHandler();
            }
          }, delay);
        });
      }
    };

    tryInit();
  }

  // Trigger initialization immediately, on DOMContentLoaded, and on window load
  initDeepLinkWithRetries();

  if (typeof document !== "undefined") {
    if (document.readyState === "complete" || document.readyState === "interactive") {
      initDeepLinkWithRetries();
    } else {
      document.addEventListener("DOMContentLoaded", initDeepLinkWithRetries);
      window.addEventListener("load", initDeepLinkWithRetries);
    }
  }
}

// Export for CommonJS / Node / Module environments if applicable
if (typeof module !== "undefined" && module.exports) {
  module.exports = {
    SUPABASE_URL,
    SUPABASE_ANON_KEY,
    supabaseClient
  };
}
