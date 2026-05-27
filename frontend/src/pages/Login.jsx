import { useState } from 'react'
import { supabase } from '../supabaseClient'
import { useNavigate } from 'react-router-dom'
import { Brain, Sparkles, Code2, Rocket, ArrowRight } from 'lucide-react'
import { motion } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'

export default function Login() {
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

  const handleLogin = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    
    if (!supabase) {
      setError("Supabase is not configured.")
      setLoading(false)
      return
    }

    const { error } = await supabase.auth.signInWithPassword({
      email,
      password,
    })

    if (error) {
      setError(error.message)
    } else {
      navigate('/')
    }
    setLoading(false)
  }

  const handleSignup = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    if (!supabase) {
      setError("Supabase is not configured.")
      setLoading(false)
      return
    }

    const { error } = await supabase.auth.signUp({
      email,
      password,
    })

    if (error) {
      setError(error.message)
    } else {
      setError('Registration successful! Please sign in.')
    }
    setLoading(false)
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
          position: 'relative'
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
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>Welcome Back</h2>
            <p style={{ color: 'var(--text-muted)' }}>Enter your credentials to access your dashboard.</p>
          </div>
          
          <form style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontSize: '0.9rem', fontWeight: 500, color: 'var(--text-muted)' }}>Email Address</label>
              <input 
                type="email" 
                placeholder="you@example.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ width: '100%', padding: '14px', boxSizing: 'border-box' }}
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
                style={{ width: '100%', padding: '14px', boxSizing: 'border-box' }}
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

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '12px' }}>
              <button 
                onClick={handleLogin} 
                disabled={loading} 
                className="btn"
                style={{ width: '100%', padding: '16px', display: 'flex', justifyContent: 'center' }}
              >
                {loading ? 'Authenticating...' : <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>Sign In <ArrowRight size={18} /></span>}
              </button>
              
              <button 
                onClick={handleSignup} 
                disabled={loading} 
                className="btn btn-secondary"
                style={{ width: '100%', padding: '16px', display: 'flex', justifyContent: 'center' }}
              >
                Create Account
              </button>
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
