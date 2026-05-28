import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Video, Mic, ShieldAlert, Play, Clock } from 'lucide-react'
import { supabase } from '../supabaseClient'
export default function SimulationMode() {
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [config, setConfig] = useState({
    format: 'RELAXED',
    lengthMinutes: 45,
    cameraRequired: false,
    strictTiming: false,
    category: ''
  })

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        if (!supabase) return;
        const { data, error } = await supabase.from('questions').select('category')
        if (error) throw error
        
        if (data) {
          const uniqueCats = [...new Set(data.map(q => q.category).filter(Boolean))]
          setCategories(uniqueCats)
          if (uniqueCats.length > 0) {
            setConfig(prev => ({...prev, category: uniqueCats[0]}))
          }
        }
      } catch (err) {
        console.error('Error fetching categories:', err)
      }
    }
    fetchCategories()
  }, [])

  const handleStart = () => {
    if (!config.category) {
      alert("Please wait for categories to load or add some to the database.")
      return;
    }
    const params = new URLSearchParams({
      category: config.category,
      simulation: 'true',
      camera: config.cameraRequired.toString(),
      strict: config.strictTiming.toString(),
      time: config.lengthMinutes.toString()
    })
    navigate(`/quiz/session?${params.toString()}`)
  }

  return (
    <div className="simulation-content" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px', textAlign: 'center' }}>
        <ShieldAlert size={48} color="#ef4444" style={{ marginBottom: '16px' }} />
        <h2>Simulation Mode</h2>
        <p style={{ color: 'var(--text-muted)', marginTop: '8px' }}>
          Experience a full-length, pressure-tested mock interview environment.
        </p>
      </div>

      <div className="glass glass-card" style={{ padding: '32px' }}>
        <h3 style={{ marginBottom: '24px', borderBottom: '1px solid var(--border-color)', paddingBottom: '16px' }}>Configure Environment</h3>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 500 }}>Category</label>
            <select 
              value={config.category} 
              onChange={(e) => setConfig({...config, category: e.target.value})}
              style={{ width: '100%' }}
            >
              {categories.map(cat => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
              {categories.length === 0 && <option value="">Loading categories...</option>}
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 500 }}>Interview Format</label>
            <div style={{ display: 'flex', gap: '16px' }}>
              {['RELAXED', 'STANDARD', 'STRICT'].map(fmt => (
                <button 
                  key={fmt}
                  onClick={() => setConfig({...config, format: fmt})}
                  style={{ 
                    flex: 1, 
                    padding: '12px', 
                    borderRadius: '8px', 
                    background: config.format === fmt ? 'rgba(99, 102, 241, 0.2)' : 'var(--btn-secondary-bg)',
                    border: `1px solid ${config.format === fmt ? 'var(--primary-color)' : 'var(--border-color)'}`,
                    color: config.format === fmt ? 'var(--text-main)' : 'var(--text-muted)',
                    cursor: 'pointer'
                  }}
                >
                  {fmt}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 500 }}>Duration (Minutes)</label>
            <input 
              type="range" 
              min="15" 
              max="120" 
              step="15" 
              value={config.lengthMinutes}
              onChange={(e) => setConfig({...config, lengthMinutes: parseInt(e.target.value)})}
              style={{ width: '100%', marginBottom: '8px' }}
            />
            <div style={{ textAlign: 'center', color: 'var(--primary-color)', fontWeight: 'bold' }}>
              {config.lengthMinutes} Minutes
            </div>
          </div>

          <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '24px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', marginBottom: '16px' }}>
              <input 
                type="checkbox" 
                checked={config.cameraRequired} 
                onChange={(e) => setConfig({...config, cameraRequired: e.target.checked})}
                style={{ width: '20px', height: '20px' }}
              />
              <Video size={20} color={config.cameraRequired ? "var(--primary-color)" : "var(--text-muted)"} />
              <span style={{ color: config.cameraRequired ? "var(--text-main)" : "var(--text-muted)" }}>Enable Camera & Audio Recording (Mock)</span>
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}>
              <input 
                type="checkbox" 
                checked={config.strictTiming} 
                onChange={(e) => setConfig({...config, strictTiming: e.target.checked})}
                style={{ width: '20px', height: '20px' }}
              />
              <Clock size={20} color={config.strictTiming ? "var(--primary-color)" : "var(--text-muted)"} />
              <span style={{ color: config.strictTiming ? "var(--text-main)" : "var(--text-muted)" }}>Enforce Strict Per-Question Time Limits</span>
            </label>
          </div>

        </div>

        <div style={{ marginTop: '40px', textAlign: 'center' }}>
          <button 
            className="btn" 
            onClick={handleStart}
            style={{ width: '100%', padding: '16px', fontSize: '1.2rem', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px', background: '#ef4444' }}
          >
            <Play size={24} /> Enter Simulation
          </button>
          <p style={{ marginTop: '16px', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Ensure you are in a quiet environment before proceeding.
          </p>
        </div>
      </div>
    </div>
  )
}
