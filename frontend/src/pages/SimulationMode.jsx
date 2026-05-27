import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Video, Mic, ShieldAlert, Play, Clock } from 'lucide-react'

export default function SimulationMode() {
  const navigate = useNavigate()
  const [config, setConfig] = useState({
    format: 'RELAXED',
    lengthMinutes: 45,
    cameraRequired: false,
    strictTiming: false
  })

  const handleStart = () => {
    // In a full implementation, this would navigate to a specialized locked-down QuizSession
    // that enforces the strict timing and tracks camera/audio if enabled.
    navigate(`/quiz/session?category=System%20Design&simulation=true`)
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
        <h3 style={{ marginBottom: '24px', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '16px' }}>Configure Environment</h3>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
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
                    background: config.format === fmt ? 'rgba(99, 102, 241, 0.2)' : 'rgba(255,255,255,0.05)',
                    border: `1px solid ${config.format === fmt ? 'var(--primary-color)' : 'rgba(255,255,255,0.1)'}`,
                    color: config.format === fmt ? 'white' : 'var(--text-muted)',
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

          <div style={{ borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: '24px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', marginBottom: '16px' }}>
              <input 
                type="checkbox" 
                checked={config.cameraRequired} 
                onChange={(e) => setConfig({...config, cameraRequired: e.target.checked})}
                style={{ width: '20px', height: '20px' }}
              />
              <Video size={20} color={config.cameraRequired ? "var(--primary-color)" : "var(--text-muted)"} />
              <span style={{ color: config.cameraRequired ? "white" : "var(--text-muted)" }}>Enable Camera & Audio Recording (Mock)</span>
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}>
              <input 
                type="checkbox" 
                checked={config.strictTiming} 
                onChange={(e) => setConfig({...config, strictTiming: e.target.checked})}
                style={{ width: '20px', height: '20px' }}
              />
              <Clock size={20} color={config.strictTiming ? "var(--primary-color)" : "var(--text-muted)"} />
              <span style={{ color: config.strictTiming ? "white" : "var(--text-muted)" }}>Enforce Strict Per-Question Time Limits</span>
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
