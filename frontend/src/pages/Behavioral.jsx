import { useState, useEffect } from 'react'
import { Star, MessageSquare, ChevronDown, ChevronUp } from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function Behavioral() {
  const [questions, setQuestions] = useState([])
  const [loading, setLoading] = useState(true)
  const [expandedId, setExpandedId] = useState(null)
  
  // STAR Method state for currently active story
  const [activeStory, setActiveStory] = useState({
    situation: '',
    task: '',
    action: '',
    result: ''
  })

  useEffect(() => {
    const fetchBehavioral = async () => {
      try {
        if (!supabase) return
        // Fetch behavioral questions specifically (assuming category = 'Behavioral' or similar tags)
        const { data, error } = await supabase
          .from('questions')
          .select('*')
          .ilike('category', '%Behavioral%')
        
        if (error) throw error
        setQuestions(data)
      } catch (err) {
        console.error('Failed to fetch behavioral questions', err)
      } finally {
        setLoading(false)
      }
    }
    fetchBehavioral()
  }, [])

  const handleSaveStory = (e) => {
    e.preventDefault()
    // In a real app, save to star_stories table
    alert('STAR story saved successfully! (Mock)')
  }

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Behavioral Scenarios...</div>
  }

  return (
    <div className="behavioral-content" style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Star color="#f59e0b" /> Behavioral Prep (STAR Method)
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Prepare for behavioral interviews by drafting stories using the Situation, Task, Action, Result framework.
        </p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {questions.length === 0 ? (
          <div className="glass glass-card" style={{ textAlign: 'center', padding: '40px' }}>
            No behavioral questions found. Please add questions with the category "Behavioral" to your database.
          </div>
        ) : (
          questions.map(q => (
            <div key={q.id} className="glass glass-card" style={{ padding: '24px', transition: 'all 0.3s' }}>
              <div 
                style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }}
                onClick={() => setExpandedId(expandedId === q.id ? null : q.id)}
              >
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '16px' }}>
                  <MessageSquare size={24} color="var(--primary-color)" style={{ flexShrink: 0, marginTop: '4px' }} />
                  <div style={{ fontSize: '1.1rem', fontWeight: 500, lineHeight: '1.4' }}>
                    {q.question_text}
                  </div>
                </div>
                <div>
                  {expandedId === q.id ? <ChevronUp size={24} /> : <ChevronDown size={24} />}
                </div>
              </div>

              {expandedId === q.id && (
                <div style={{ marginTop: '24px', paddingTop: '24px', borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                  <form onSubmit={handleSaveStory} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div>
                      <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontWeight: 600 }}>
                        <span style={{ color: '#6366f1' }}>S</span>ituation: Context of the scenario
                      </label>
                      <textarea 
                        required
                        value={activeStory.situation}
                        onChange={(e) => setActiveStory({...activeStory, situation: e.target.value})}
                        style={{ width: '100%', height: '80px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', padding: '12px', color: 'white' }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontWeight: 600 }}>
                        <span style={{ color: '#10b981' }}>T</span>ask: Your specific goal or problem
                      </label>
                      <textarea 
                        required
                        value={activeStory.task}
                        onChange={(e) => setActiveStory({...activeStory, task: e.target.value})}
                        style={{ width: '100%', height: '80px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', padding: '12px', color: 'white' }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontWeight: 600 }}>
                        <span style={{ color: '#f59e0b' }}>A</span>ction: What you specifically did
                      </label>
                      <textarea 
                        required
                        value={activeStory.action}
                        onChange={(e) => setActiveStory({...activeStory, action: e.target.value})}
                        style={{ width: '100%', height: '120px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', padding: '12px', color: 'white' }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontWeight: 600 }}>
                        <span style={{ color: '#ef4444' }}>R</span>esult: The outcome and impact
                      </label>
                      <textarea 
                        required
                        value={activeStory.result}
                        onChange={(e) => setActiveStory({...activeStory, result: e.target.value})}
                        style={{ width: '100%', height: '100px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', padding: '12px', color: 'white' }}
                      />
                    </div>
                    
                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                      <button type="submit" className="btn">Save STAR Story</button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}
