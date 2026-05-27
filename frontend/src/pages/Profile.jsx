import { useState } from 'react'
import { User, Mail, Bell, Shield, Camera } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'

export default function Profile() {
  const { user } = useAuth()
  const [profile, setProfile] = useState({
    name: 'Candidate',
    email: user?.email || '',
    notifications: true,
    weeklyDigest: true
  })

  const handleSave = (e) => {
    e.preventDefault()
    alert('Profile settings saved! (Mock)')
  }

  return (
    <div className="profile-content" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <User color="var(--primary-color)" /> Profile & Settings
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Manage your account details and application preferences.
        </p>
      </div>

      <div className="glass glass-card" style={{ padding: '40px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '24px', marginBottom: '40px', paddingBottom: '32px', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
          <div style={{ position: 'relative' }}>
            <div style={{ width: '100px', height: '100px', borderRadius: '50%', background: 'rgba(99, 102, 241, 0.2)', display: 'flex', justifyContent: 'center', alignItems: 'center', border: '2px solid var(--primary-color)' }}>
              <User size={48} color="var(--primary-color)" />
            </div>
            <button style={{ position: 'absolute', bottom: 0, right: 0, background: 'var(--primary-color)', border: 'none', borderRadius: '50%', width: '32px', height: '32px', display: 'flex', justifyContent: 'center', alignItems: 'center', cursor: 'pointer', color: 'white' }}>
              <Camera size={16} />
            </button>
          </div>
          <div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '4px' }}>{profile.name}</h3>
            <p style={{ color: 'var(--text-muted)' }}>Interview Mentor Member</p>
          </div>
        </div>

        <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          <div>
            <h4 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary-color)' }}>
              <Shield size={18} /> Account Details
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)' }}>Full Name</label>
                <input 
                  type="text" 
                  value={profile.name}
                  onChange={(e) => setProfile({...profile, name: e.target.value})}
                  style={{ width: '100%', padding: '12px', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', color: 'white' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)' }}>Email Address</label>
                <input 
                  type="email" 
                  value={profile.email}
                  disabled
                  style={{ width: '100%', padding: '12px', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid rgba(255,255,255,0.05)', borderRadius: '8px', color: 'var(--text-muted)' }}
                />
              </div>
            </div>
          </div>

          <div style={{ borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: '24px' }}>
            <h4 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary-color)' }}>
              <Bell size={18} /> Preferences
            </h4>
            
            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', marginBottom: '16px' }}>
              <input 
                type="checkbox" 
                checked={profile.notifications} 
                onChange={(e) => setProfile({...profile, notifications: e.target.checked})}
                style={{ width: '20px', height: '20px' }}
              />
              <span style={{ color: profile.notifications ? "white" : "var(--text-muted)" }}>Enable Browser Notifications for Study Reminders</span>
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}>
              <input 
                type="checkbox" 
                checked={profile.weeklyDigest} 
                onChange={(e) => setProfile({...profile, weeklyDigest: e.target.checked})}
                style={{ width: '20px', height: '20px' }}
              />
              <span style={{ color: profile.weeklyDigest ? "white" : "var(--text-muted)" }}>Receive Weekly Progress Digest via Email</span>
            </label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '16px' }}>
            <button type="submit" className="btn">Save Changes</button>
          </div>
        </form>

      </div>
    </div>
  )
}
