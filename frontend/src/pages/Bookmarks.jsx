import { useState, useEffect } from 'react'
import { Bookmark, ExternalLink, Trash2 } from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function Bookmarks() {
  const [bookmarks, setBookmarks] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchBookmarks = async () => {
      try {
        if (!supabase) return
        // Fetch random questions to simulate bookmarks since we don't have a joined bookmarks table yet
        const { data, error } = await supabase.from('questions').select('*').limit(5)
        if (error) throw error
        setBookmarks(data)
      } catch (err) {
        console.error('Failed to fetch bookmarks', err)
      } finally {
        setLoading(false)
      }
    }
    fetchBookmarks()
  }, [])

  const removeBookmark = (id) => {
    setBookmarks(bookmarks.filter(b => b.id !== id))
    // In real app, delete from supabase user_bookmarks
  }

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Bookmarks...</div>
  }

  return (
    <div className="bookmarks-content" style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Bookmark color="#a855f7" /> Saved Questions
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Review the questions you've starred for later study.
        </p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {bookmarks.length === 0 ? (
          <div className="glass glass-card" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
            No bookmarks found. Click the star icon during a quiz to save questions here.
          </div>
        ) : (
          bookmarks.map(q => (
            <div key={q.id} className="glass glass-card" style={{ padding: '24px', display: 'flex', gap: '20px' }}>
              <div style={{ width: '40px', display: 'flex', justifyContent: 'center' }}>
                <Bookmark size={24} color="#a855f7" fill="#a855f7" />
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--primary-color)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
                    {q.category}
                  </span>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }} title="Practice this question">
                      <ExternalLink size={18} />
                    </button>
                    <button 
                      onClick={() => removeBookmark(q.id)}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#ef4444' }} 
                      title="Remove Bookmark"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
                
                <h3 style={{ fontSize: '1.2rem', marginBottom: '16px', lineHeight: '1.4' }}>{q.question_text}</h3>
                
                <div style={{ background: 'rgba(255,255,255,0.05)', padding: '16px', borderRadius: '8px' }}>
                  <h4 style={{ fontSize: '0.85rem', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px' }}>Answer:</h4>
                  <div style={{ color: '#10b981', lineHeight: '1.5' }}>{q.correct_answer}</div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
