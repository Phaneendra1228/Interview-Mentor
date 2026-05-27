import { useState, useEffect } from 'react'
import { Brain } from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function QuestionBank() {
  const [questions, setQuestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [expandedAns, setExpandedAns] = useState({})

  const toggleAnswer = (id) => {
    setExpandedAns(prev => ({ ...prev, [id]: !prev[id] }))
  }

  const fetchQuestions = async () => {
    setLoading(true)
    try {
      if (!supabase) {
        console.warn('Supabase not configured in .env file')
        setQuestions([])
        return
      }
      
      const { data, error } = await supabase.from('questions').select('*')
      if (error) {
        throw error
      }
      
      if (data) {
        const formattedData = data.map(q => ({
          id: q.id,
          category: q.category,
          text: q.question_text,
          answer: q.correct_answer,
          difficulty: q.difficulty
        }))
        setQuestions(formattedData)
      }
    } catch (err) {
      console.error('Failed to fetch questions from Supabase', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchQuestions()
  }, [])

  return (
    <div className="questions-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <h2>Question Bank</h2>
        <button className="btn btn-secondary" onClick={fetchQuestions}>Refresh</button>
      </div>

      <div>
        {loading ? (
          <div className="loading-spinner" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px' }}>
            <Brain className="lucide-spin" size={32} />
            <span style={{ marginLeft: '12px' }}>Loading questions...</span>
          </div>
        ) : questions.length > 0 ? (
            <div key={q.id} className="question-item glass glass-card" style={{ padding: '24px', marginBottom: '16px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <span className="question-category" style={{ fontSize: '0.8rem', color: 'var(--primary-color)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
                  {q.category || 'General'}
                </span>
                <span style={{ fontSize: '0.8rem', color: q.difficulty === 'Hard' ? 'var(--danger-color)' : q.difficulty === 'Medium' ? 'var(--warning-color)' : 'var(--secondary-color)', fontWeight: 600 }}>
                  {q.difficulty}
                </span>
              </div>
              <div className="question-text" style={{ fontSize: '1.1rem', marginTop: '12px', marginBottom: '16px', fontWeight: 500, color: 'var(--text-main)' }}>
                {q.text}
              </div>
              
              {q.answer && (
                <div style={{ marginTop: '16px' }}>
                  <button 
                    onClick={() => toggleAnswer(q.id)}
                    className="btn btn-secondary"
                    style={{ padding: '8px 16px', fontSize: '0.9rem' }}
                  >
                    {expandedAns[q.id] ? 'Hide Answer' : 'Show Answer'}
                  </button>
                  
                  {expandedAns[q.id] && (
                    <div style={{ 
                      marginTop: '16px', 
                      padding: '16px', 
                      background: 'rgba(0,0,0,0.1)', 
                      borderRadius: '8px', 
                      borderLeft: '4px solid var(--primary-color)',
                      color: 'var(--text-main)',
                      lineHeight: '1.6'
                    }}>
                      <strong style={{ display: 'block', marginBottom: '8px', color: 'var(--primary-color)' }}>Suggested Answer:</strong>
                      {q.answer}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))
        ) : (
          <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
            No questions found. { !supabase ? "Please configure your Supabase URL and Anon Key in the .env file." : "Your Supabase 'questions' table is empty." }
          </div>
        )}
      </div>
    </div>
  )
}
