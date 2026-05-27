import { Outlet, Navigate, NavLink } from 'react-router-dom'
import { Brain, Target, BookOpen, User, Layers, BarChart2, LogOut, Copy, MessageSquare, ShieldAlert, History as HistoryIcon, Bookmark, Award, FolderOpen } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'

export default function Layout() {
  const { session, signOut } = useAuth()

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
        
        <div style={{ marginTop: 'auto', padding: '16px' }}>
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
