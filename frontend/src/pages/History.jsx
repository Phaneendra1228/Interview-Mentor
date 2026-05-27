import { useState, useEffect } from 'react'
import { History as HistoryIcon, Clock, CheckCircle, Target } from 'lucide-react'

// Mock Data
const mockHistory = [
  { id: 1, date: '2026-05-26T10:30:00Z', category: 'System Design', score: 85, duration: '45m 12s' },
  { id: 2, date: '2026-05-24T14:15:00Z', category: 'Java', score: 70, duration: '32m 05s' },
  { id: 3, date: '2026-05-23T09:00:00Z', category: 'Behavioral', score: 95, duration: '28m 40s' },
  { id: 4, date: '2026-05-20T16:45:00Z', category: 'React', score: 88, duration: '50m 10s' },
  { id: 5, date: '2026-05-18T11:20:00Z', category: 'Algorithms', score: 60, duration: '55m 00s' }
]

export default function History() {
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // In real app, fetch from supabase 'quiz_sessions' table
    setTimeout(() => {
      setHistory(mockHistory)
      setLoading(false)
    }, 600)
  }, [])

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Session History...</div>
  }

  return (
    <div className="history-content" style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <HistoryIcon color="var(--primary-color)" /> Session History
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Review your past mock interviews and track your progress over time.
        </p>
      </div>

      <div className="glass glass-card" style={{ padding: '0', overflow: 'hidden' }}>
        {history.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            No history found. Take a mock quiz to get started!
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(255,255,255,0.1)', background: 'rgba(255,255,255,0.02)' }}>
                <th style={{ padding: '16px 24px', fontWeight: 600, color: 'var(--text-muted)' }}>Date</th>
                <th style={{ padding: '16px 24px', fontWeight: 600, color: 'var(--text-muted)' }}>Category</th>
                <th style={{ padding: '16px 24px', fontWeight: 600, color: 'var(--text-muted)' }}>Score</th>
                <th style={{ padding: '16px 24px', fontWeight: 600, color: 'var(--text-muted)' }}>Duration</th>
                <th style={{ padding: '16px 24px', fontWeight: 600, color: 'var(--text-muted)' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {history.map((session) => {
                const dateObj = new Date(session.date)
                return (
                  <tr key={session.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                    <td style={{ padding: '16px 24px' }}>
                      <div style={{ fontWeight: 500 }}>{dateObj.toLocaleDateString()}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{dateObj.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</div>
                    </td>
                    <td style={{ padding: '16px 24px' }}>
                      <span style={{ background: 'rgba(99, 102, 241, 0.1)', color: '#818cf8', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem' }}>
                        {session.category}
                      </span>
                    </td>
                    <td style={{ padding: '16px 24px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: session.score >= 80 ? '#10b981' : session.score >= 65 ? '#f59e0b' : '#ef4444' }}>
                        {session.score >= 80 ? <CheckCircle size={16} /> : <Target size={16} />}
                        <span style={{ fontWeight: 'bold' }}>{session.score}%</span>
                      </div>
                    </td>
                    <td style={{ padding: '16px 24px', color: 'var(--text-muted)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Clock size={14} /> {session.duration}
                      </div>
                    </td>
                    <td style={{ padding: '16px 24px' }}>
                      <button className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.85rem' }}>Review Details</button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
