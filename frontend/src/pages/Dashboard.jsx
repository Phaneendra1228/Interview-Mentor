import { Target, BookOpen, Brain, Play } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function Dashboard() {
  return (
    <div className="dashboard-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h2>Welcome back, Candidate!</h2>
          <p style={{ color: 'var(--text-muted)' }}>Ready for your next interview prep session?</p>
        </div>
        <Link to="/quiz" className="btn" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none' }}>
          <Play size={18} /> Start Mock Interview
        </Link>
      </div>

      <div className="dashboard-grid">
        <div className="glass glass-card stat-card">
          <Target className="stat-icon" color="var(--primary-color)" size={32} />
          <div className="stat-title">Questions Mastered</div>
          <div className="stat-value">24</div>
        </div>
        <div className="glass glass-card stat-card">
          <BookOpen className="stat-icon" color="var(--secondary-color)" size={32} />
          <div className="stat-title">Topics Covered</div>
          <div className="stat-value">6</div>
        </div>
        <div className="glass glass-card stat-card">
          <Brain className="stat-icon" color="#eab308" size={32} />
          <div className="stat-title">Current Streak</div>
          <div className="stat-value">3 Days</div>
        </div>
      </div>
      
      <h3 style={{ marginTop: '48px', marginBottom: '24px' }}>Recommended Practice</h3>
      <div className="glass glass-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h4 style={{ marginBottom: '8px' }}>System Design: Rate Limiting</h4>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: 0 }}>Review common rate limiting algorithms and their implementation tradeoffs.</p>
        </div>
        <button className="btn btn-secondary">Review Now</button>
      </div>
    </div>
  )
}
