import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://placeholder.supabase.co'
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'placeholder'

// Only create the client if we have real credentials
export const supabase = import.meta.env.VITE_SUPABASE_URL 
  ? createClient(supabaseUrl, supabaseAnonKey) 
  : null;
