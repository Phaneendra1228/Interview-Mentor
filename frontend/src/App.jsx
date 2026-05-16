import { useState, useEffect } from 'react'
import { BookOpen, User, CheckCircle, Brain, Target, Play } from 'lucide-react'
import './App.css'

function App() {
  const [activeTab, setActiveTab] = useState('dashboard')
  const [questions, setQuestions] = useState([])
  const [loading, setLoading] = useState(false)

  const fetchQuestions = async () => {
    setLoading(true)
    try {
      const res = await fetch('http://localhost:8080/api/questions')
      if (res.ok) {
        const data = await res.json()
        setQuestions(data)
      }
    } catch (err) {
      console.error('Failed to fetch questions', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (activeTab === 'questions' && questions.length === 0) {
      fetchQuestions()
    }
  }, [activeTab])

  const renderDashboard = () => (
    <div className="dashboard-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h2>Welcome back, Candidate!</h2>
          <p style={{ color: 'var(--text-muted)' }}>Ready for your next interview prep session?</p>
        </div>
        <button className="btn" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Play size={18} /> Start Mock Interview
        </button>
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

  const renderQuestions = () => (
    <div className="questions-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <h2>Question Bank</h2>
        <button className="btn btn-secondary" onClick={fetchQuestions}>Refresh</button>
      </div>

      <div className="glass glass-card">
        {loading ? (
          <div className="loading-spinner">
            <Brain className="lucide-spin" size={32} />
            <span style={{ marginLeft: '12px' }}>Loading questions...</span>
          </div>
        ) : questions.length > 0 ? (
          questions.map(q => (
            <div key={q.id} className="question-item">
              <span className="question-category">{q.category || 'General'}</span>
              <div className="question-text">{q.text}</div>
              {q.answer && <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '8px' }}>{q.answer.substring(0, 100)}...</p>}
            </div>
          ))
        ) : (
          <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
            No questions found. Please ensure your backend is running on port 8080.
          </div>
        )}
      </div>
    </div>
  )

  return (
    <div className="app-container">
      <div className="bg-orb orb-1"></div>
      <div className="bg-orb orb-2"></div>
      
      <aside className="sidebar">
        <div className="sidebar-brand">
          <Brain size={28} color="var(--primary-color)" />
          InterviewMentor
        </div>
        
        <nav className="nav-menu">
          <div 
            className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}
            onClick={() => setActiveTab('dashboard')}
          >
            <Target size={20} />
            Dashboard
          </div>
          <div 
            className={`nav-item ${activeTab === 'questions' ? 'active' : ''}`}
            onClick={() => setActiveTab('questions')}
          >
            <BookOpen size={20} />
            Question Bank
          </div>
          <div 
            className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`}
            onClick={() => setActiveTab('profile')}
          >
            <User size={20} />
            Profile
          </div>
        </nav>
      </aside>

      <main className="main-content">
        {activeTab === 'dashboard' && renderDashboard()}
        {activeTab === 'questions' && renderQuestions()}
        {activeTab === 'profile' && (
          <div>
            <h2>Profile</h2>
            <div className="glass glass-card" style={{ marginTop: '24px' }}>
              <p>Profile settings coming soon...</p>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default App
