import { Outlet, Navigate, NavLink } from 'react-router-dom'
import { Brain, Target, BookOpen, User, Layers, BarChart2, LogOut, Copy, MessageSquare, ShieldAlert, History as HistoryIcon, Bookmark, Award, FolderOpen, Sun, Moon } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'

export default function Layout() {
  const { session, signOut } = useAuth()
  const { theme, toggleTheme } = useTheme()

  if (!session) {
    return <Navigate to="/login" replace />
  }

  const handleLogout = async () => {
    await signOut()
  }

  return (
    <div className="app-container">
      <div className="bg-orb orb-1"></div>
      <div className="bg-orb orb-2"></div>
      <div className="bg-orb orb-3"></div>
      
      <aside className="sidebar">
        <div className="sidebar-brand">
          <Brain size={28} color="var(--primary-color)" />
          InterviewMentor
        </div>
        
        <nav className="nav-menu">
          <NavLink to="/" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} end>
            <Target size={20} /> Dashboard
          </NavLink>
          <NavLink to="/questions" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <BookOpen size={20} /> Question Bank
          </NavLink>
          <NavLink to="/quiz" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Layers size={20} /> Practice Quiz
          </NavLink>
          <NavLink to="/flashcards" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Copy size={20} /> Flashcards
          </NavLink>
          <NavLink to="/behavioral" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <MessageSquare size={20} /> Behavioral Prep
          </NavLink>
          <NavLink to="/simulation" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <ShieldAlert size={20} /> Simulation
          </NavLink>
          <NavLink to="/analytics" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <BarChart2 size={20} /> Analytics
          </NavLink>
          <NavLink to="/history" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <HistoryIcon size={20} /> History
          </NavLink>
          <NavLink to="/bookmarks" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Bookmark size={20} /> Bookmarks
          </NavLink>
          <NavLink to="/achievements" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Award size={20} /> Achievements
          </NavLink>
          <NavLink to="/resources" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <FolderOpen size={20} /> Resources
          </NavLink>
          <NavLink to="/profile" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <User size={20} /> Profile
          </NavLink>
        </nav>
        
        <div style={{ marginTop: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          
          <button 
            onClick={toggleTheme}
            style={{ 
              background: 'rgba(255, 255, 255, 0.05)', 
              border: '1px solid var(--border-color)', 
              color: 'var(--text-muted)', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'space-between',
              width: '100%', 
              padding: '10px 16px', 
              borderRadius: '12px', 
              cursor: 'pointer',
              transition: 'all 0.3s ease'
            }}
            onMouseOver={(e) => { e.currentTarget.style.borderColor = 'var(--primary-color)'; e.currentTarget.style.color = 'var(--text-main)' }}
            onMouseOut={(e) => { e.currentTarget.style.borderColor = 'var(--border-color)'; e.currentTarget.style.color = 'var(--text-muted)' }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              {theme === 'dark' ? <Moon size={18} /> : <Sun size={18} />}
              <span style={{ fontWeight: 500, fontSize: '0.95rem' }}>{theme === 'dark' ? 'Dark Mode' : 'Light Mode'}</span>
            </div>
            
            {/* The actual toggle switch visual */}
            <div style={{ 
              width: '36px', height: '20px', 
              background: theme === 'dark' ? 'var(--primary-color)' : 'rgba(0,0,0,0.1)', 
              borderRadius: '20px', 
              position: 'relative',
              transition: 'background 0.3s'
            }}>
              <div style={{ 
                width: '14px', height: '14px', 
                background: 'white', 
                borderRadius: '50%', 
                position: 'absolute', 
                top: '3px', 
                left: theme === 'dark' ? '19px' : '3px',
                transition: 'left 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
              }} />
            </div>
          </button>

          <button 
            onClick={handleLogout}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', width: '100%', padding: '12px', borderRadius: '12px', transition: 'all 0.3s ease' }}
            onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)'; e.currentTarget.style.color = '#ef4444' }}
            onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = 'var(--text-muted)' }}
          >
            <LogOut size={20} />
            Logout
          </button>
        </div>
      </aside>

      <main className="main-content animate-in">
        <Outlet />
      </main>
    </div>
  )
}
