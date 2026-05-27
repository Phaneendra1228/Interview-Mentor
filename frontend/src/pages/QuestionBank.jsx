import { useState, useEffect } from 'react'
import { Brain } from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function QuestionBank() {
  const [questions, setQuestions] = useState([])
  const [loading, setLoading] = useState(false)

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

      <div className="glass glass-card">
        {loading ? (
          <div className="loading-spinner" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px' }}>
            <Brain className="lucide-spin" size={32} />
            <span style={{ marginLeft: '12px' }}>Loading questions...</span>
          </div>
        ) : questions.length > 0 ? (
          questions.map(q => (
            <div key={q.id} className="question-item" style={{ padding: '16px', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
              <span className="question-category" style={{ fontSize: '0.8rem', color: 'var(--primary-color)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
                {q.category || 'General'}
              </span>
              <div className="question-text" style={{ fontSize: '1.1rem', marginTop: '8px', marginBottom: '8px' }}>
                {q.text}
              </div>
              {q.answer && <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: 0 }}>{q.answer.substring(0, 150)}...</p>}
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
