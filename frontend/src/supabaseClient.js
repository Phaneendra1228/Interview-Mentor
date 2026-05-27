import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://placeholder.supabase.co'
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'placeholder'

// Only create the client if we have real credentials
let supabaseInstance = null;
try {
  if (import.meta.env.VITE_SUPABASE_URL) {
    // Basic validation to prevent 'new URL()' crash inside createClient
    const urlStr = import.meta.env.VITE_SUPABASE_URL.trim();
    if (urlStr.startsWith('http')) {
      supabaseInstance = createClient(urlStr, supabaseAnonKey);
    }
  }
} catch (err) {
  console.error("Failed to initialize Supabase client. Check your VITE_SUPABASE_URL format.", err);
}

export const supabase = supabaseInstance;
