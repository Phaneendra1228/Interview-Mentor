import { useLocation, useNavigate } from 'react-router-dom'
import { Target, Clock, CheckCircle, XCircle } from 'lucide-react'

export default function QuizResults() {
  const location = useLocation()
  const navigate = useNavigate()
  
  if (!location.state) {
    return <div style={{ padding: '40px' }}>No result data found. <button onClick={() => navigate('/quiz')}>Go back</button></div>
  }

  const { total, correct, timeTaken, category, questions, answers } = location.state
  const percentage = Math.round((correct / total) * 100)
  
  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${m}m ${s}s`
  }

  return (
    <div className="results-content" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ textAlign: 'center', marginBottom: '40px' }}>
        <h2 style={{ fontSize: '2rem', marginBottom: '8px' }}>Session Complete!</h2>
        <p style={{ color: 'var(--text-muted)' }}>You finished the {category} mock interview.</p>
      </div>

      <div className="dashboard-grid" style={{ marginBottom: '40px' }}>
        <div className="glass glass-card stat-card" style={{ textAlign: 'center' }}>
          <Target className="stat-icon" color="var(--primary-color)" size={32} style={{ margin: '0 auto 16px' }} />
          <div className="stat-title">Accuracy</div>
          <div className="stat-value" style={{ color: percentage >= 70 ? '#10b981' : '#ef4444' }}>{percentage}%</div>
        </div>
        <div className="glass glass-card stat-card" style={{ textAlign: 'center' }}>
          <CheckCircle className="stat-icon" color="#10b981" size={32} style={{ margin: '0 auto 16px' }} />
          <div className="stat-title">Correct</div>
          <div className="stat-value">{correct} / {total}</div>
        </div>
        <div className="glass glass-card stat-card" style={{ textAlign: 'center' }}>
          <Clock className="stat-icon" color="#f59e0b" size={32} style={{ margin: '0 auto 16px' }} />
          <div className="stat-title">Time Taken</div>
          <div className="stat-value">{formatTime(timeTaken)}</div>
        </div>
      </div>

      <h3>Detailed Review</h3>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', marginTop: '24px' }}>
        {questions.map((q, idx) => {
          // If the question is open-ended, grading is manual/subjective for now
          const isMultipleChoice = !!q.option_a;
          const isCorrect = isMultipleChoice && answers[idx] === q.correct_answer;
          
          return (
            <div key={q.id} className="glass glass-card" style={{ borderLeft: `4px solid ${!isMultipleChoice ? '#f59e0b' : isCorrect ? '#10b981' : '#ef4444'}` }}>
              <div style={{ fontWeight: 600, marginBottom: '16px', fontSize: '1.1rem' }}>
                {idx + 1}. {q.question_text}
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px', fontSize: '0.9rem' }}>
                <div style={{ background: 'rgba(255,255,255,0.05)', padding: '12px', borderRadius: '8px' }}>
                  <div style={{ color: 'var(--text-muted)', marginBottom: '4px' }}>Your Answer:</div>
                  <div style={{ color: !isMultipleChoice ? 'white' : isCorrect ? '#10b981' : '#ef4444' }}>
                    {answers[idx] || 'No answer provided'}
                  </div>
                </div>
                <div style={{ background: 'rgba(255,255,255,0.05)', padding: '12px', borderRadius: '8px' }}>
                  <div style={{ color: 'var(--text-muted)', marginBottom: '4px' }}>Expected Answer:</div>
                  <div style={{ color: '#10b981' }}>{q.correct_answer}</div>
                </div>
              </div>
              
              {q.explanation && (
                <div style={{ padding: '16px', background: 'rgba(99, 102, 241, 0.1)', borderRadius: '8px', fontSize: '0.9rem' }}>
                  <strong>Explanation:</strong> {q.explanation}
                </div>
              )}
            </div>
          )
        })}
      </div>

      <div style={{ textAlign: 'center', marginTop: '40px' }}>
        <button className="btn" onClick={() => navigate('/')}>Return to Dashboard</button>
      </div>
    </div>
  )
}
