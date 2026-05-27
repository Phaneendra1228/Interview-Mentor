import { useState } from 'react'
import { supabase } from '../supabaseClient'
import { useNavigate } from 'react-router-dom'
import { Brain, Sparkles, Code2, Rocket, ArrowRight, Mail } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'

export default function Login() {
  const [isSignUp, setIsSignUp] = useState(false)
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  
  const navigate = useNavigate()
  const { session } = useAuth()

  // If already logged in, redirect to dashboard
  if (session) {
    navigate('/', { replace: true })
    return null
  }

  const handleAuth = async (e) => {
    e.preventDefault()
    
    if (isSignUp && !fullName) {
      setError("Please enter your full name.")
      return
    }
    
    if (!email || !password) {
      setError("Please enter both email and password.")
      return
    }
    
    setLoading(true)
    setError(null)
    
    if (!supabase) {
      setError("Supabase is not configured.")
      setLoading(false)
      return
    }

    if (isSignUp) {
      const { error } = await supabase.auth.signUp({
        email,
        password,
        options: {
          data: {
            full_name: fullName,
          }
        }
      })

      if (error) {
        setError(error.message)
      } else {
        setError('Registration successful! Please check your email to verify your account or sign in.')
        setIsSignUp(false)
      }
    } else {
      const { error } = await supabase.auth.signInWithPassword({
        email,
        password,
      })

      if (error) {
        setError(error.message)
      } else {
        navigate('/')
      }
    }
    setLoading(false)
  }

  const handleOAuthLogin = async (provider) => {
    setLoading(true)
    setError(null)
    
    if (!supabase) {
      setError("Supabase is not configured.")
      setLoading(false)
      return
    }

    const { error } = await supabase.auth.signInWithOAuth({
      provider: provider,
      options: {
        redirectTo: window.location.origin,
      }
    })

    if (error) {
      setError(error.message)
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', width: '100vw', background: 'var(--bg-color)', overflow: 'hidden' }}>
      
      {/* Left Promotional Side */}
      <motion.div 
        initial={{ x: '-100%' }}
        animate={{ x: 0 }}
        transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
        style={{ 
          flex: 1, 
          display: 'flex', 
          flexDirection: 'column', 
          justifyContent: 'center', 
          padding: '4rem', 
          position: 'relative',
          background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(16, 185, 129, 0.1))',
          borderRight: '1px solid var(--border-color)'
        }}
        className="hidden-mobile"
      >
        <div className="bg-orb orb-1" style={{ top: '20%', left: '10%', opacity: 0.3 }}></div>
        <div className="bg-orb orb-2" style={{ bottom: '10%', right: '20%', opacity: 0.3 }}></div>
        
        <div style={{ position: 'relative', zIndex: 10, maxWidth: '500px' }}>
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3, duration: 0.8 }}
            style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}
          >
            <div style={{ background: 'var(--primary-color)', padding: '12px', borderRadius: '16px', boxShadow: '0 8px 32px rgba(99, 102, 241, 0.4)' }}>
              <Brain size={40} color="white" />
            </div>
            <h1 style={{ fontSize: '3rem', fontWeight: 800, background: 'var(--logo-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', letterSpacing: '-0.04em' }}>
              InterviewMentor
            </h1>
          </motion.div>

          <motion.p 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.8 }}
            style={{ fontSize: '1.25rem', color: 'var(--text-muted)', lineHeight: '1.6', marginBottom: '48px' }}
          >
            The ultimate AI-powered platform to master technical interviews. Practice system design, behavioral questions, and algorithmic challenges.
          </motion.p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {[
              { icon: Sparkles, text: 'AI-Powered Mock Interviews', color: '#ec4899' },
              { icon: Code2, text: '1,000+ Curated Questions', color: '#10b981' },
              { icon: Rocket, text: 'Real-time Performance Analytics', color: '#f59e0b' }
            ].map((feature, idx) => (
              <motion.div 
                key={idx}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.5 + (idx * 0.1), duration: 0.5 }}
                style={{ display: 'flex', alignItems: 'center', gap: '16px', padding: '16px', background: 'var(--card-bg)', borderRadius: '16px', border: '1px solid var(--border-color)', backdropFilter: 'blur(10px)' }}
              >
                <div style={{ background: `${feature.color}20`, padding: '12px', borderRadius: '12px' }}>
                  <feature.icon size={24} color={feature.color} />
                </div>
                <span style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-main)' }}>{feature.text}</span>
              </motion.div>
            ))}
          </div>
        </div>
      </motion.div>

      {/* Right Login Form Side */}
      <motion.div 
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 1 }}
        style={{ 
          flex: 1, 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center', 
          padding: '2rem',
          position: 'relative',
          overflowY: 'auto'
        }}
      >
        <motion.div 
          initial={{ opacity: 0, y: 40, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ delay: 0.2, duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
          className="glass glass-card"
          style={{ width: '100%', maxWidth: '440px', padding: '40px' }}
        >
          <div style={{ textAlign: 'center', marginBottom: '32px' }}>
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>
              {isSignUp ? 'Create an Account' : 'Welcome Back'}
            </h2>
            <p style={{ color: 'var(--text-muted)' }}>
              {isSignUp ? 'Sign up to start mastering your interviews.' : 'Enter your credentials to access your dashboard.'}
            </p>
          </div>
          
          <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
            <button 
              onClick={() => handleOAuthLogin('google')}
              className="btn-secondary" 
              style={{ flex: 1, padding: '12px', borderRadius: '12px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', border: '1px solid var(--border-color)', background: 'var(--btn-secondary-bg)', color: 'var(--text-main)', cursor: 'pointer', transition: 'all 0.2s', fontWeight: 500 }}
              onMouseOver={e => e.currentTarget.style.background = 'var(--btn-secondary-hover)'}
              onMouseOut={e => e.currentTarget.style.background = 'var(--btn-secondary-bg)'}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              Google
            </button>
            <button 
              type="button"
              onClick={() => handleOAuthLogin('github')}
              className="btn-secondary" 
              style={{ flex: 1, padding: '12px', borderRadius: '12px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', border: '1px solid var(--border-color)', background: 'var(--btn-secondary-bg)', color: 'var(--text-main)', cursor: 'pointer', transition: 'all 0.2s', fontWeight: 500 }}
              onMouseOver={e => e.currentTarget.style.background = 'var(--btn-secondary-hover)'}
              onMouseOut={e => e.currentTarget.style.background = 'var(--btn-secondary-bg)'}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.463-1.11-1.463-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.831.092-.646.35-1.086.636-1.336-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.564 9.564 0 0112 6.844c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.379.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.577.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z"/>
              </svg>
              GitHub
            </button>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
            <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }}></div>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>or continue with email</span>
            <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }}></div>
          </div>
          
          <form style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <AnimatePresence>
              {isSignUp && (
                <motion.div
                  initial={{ opacity: 0, height: 0, overflow: 'hidden' }}
                  animate={{ opacity: 1, height: 'auto', overflow: 'visible' }}
                  exit={{ opacity: 0, height: 0, overflow: 'hidden' }}
                  transition={{ duration: 0.3 }}
                >
                  <label style={{ display: 'block', marginBottom: '8px', fontSize: '0.9rem', fontWeight: 500, color: 'var(--text-muted)' }}>Full Name</label>
                  <input 
                    type="text" 
                    placeholder="John Doe" 
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    style={{ width: '100%', padding: '14px', boxSizing: 'border-box', background: 'var(--card-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', color: 'var(--text-main)' }}
                    required={isSignUp}
                  />
                </motion.div>
              )}
            </AnimatePresence>
            
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontSize: '0.9rem', fontWeight: 500, color: 'var(--text-muted)' }}>Email Address</label>
              <input 
                type="email" 
                placeholder="you@example.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ width: '100%', padding: '14px', boxSizing: 'border-box', background: 'var(--card-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', color: 'var(--text-main)' }}
                required
              />
            </div>
            
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontSize: '0.9rem', fontWeight: 500, color: 'var(--text-muted)' }}>Password</label>
              <input 
                type="password" 
                placeholder="••••••••" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ width: '100%', padding: '14px', boxSizing: 'border-box', background: 'var(--card-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', color: 'var(--text-main)' }}
                required
              />
            </div>

            {error && (
              <motion.div 
                initial={{ opacity: 0, y: -10 }} 
                animate={{ opacity: 1, y: 0 }}
                style={{ color: error.includes('successful') ? 'var(--secondary-color)' : 'var(--danger-color)', fontSize: '0.9rem', background: error.includes('successful') ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)', padding: '12px', borderRadius: '8px', borderLeft: `4px solid ${error.includes('successful') ? 'var(--secondary-color)' : 'var(--danger-color)'}` }}
              >
                {error}
              </motion.div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '12px' }}>
              <button 
                onClick={handleAuth} 
                disabled={loading} 
                className="btn"
                style={{ width: '100%', padding: '16px', display: 'flex', justifyContent: 'center' }}
              >
                {loading ? 'Authenticating...' : <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>{isSignUp ? 'Create Account' : 'Sign In'} <ArrowRight size={18} /></span>}
              </button>
              
              <div style={{ textAlign: 'center', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                {isSignUp ? "Already have an account?" : "Don't have an account?"}
                <button 
                  type="button"
                  onClick={() => setIsSignUp(!isSignUp)}
                  style={{ background: 'none', border: 'none', color: 'var(--primary-color)', fontWeight: 600, cursor: 'pointer', marginLeft: '6px' }}
                >
                  {isSignUp ? "Sign In" : "Sign Up"}
                </button>
              </div>
            </div>
          </form>
        </motion.div>
      </motion.div>
      
      {/* Hide left side on mobile using a quick inline style injection */}
      <style>{`
        @media (max-width: 900px) {
          .hidden-mobile { display: none !important; }
        }
      `}</style>
    </div>
  )
}
