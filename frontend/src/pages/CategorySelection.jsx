import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { supabase } from '../supabaseClient'
import { Brain, Layers, Code, Database, LayoutTemplate, Network, Server } from 'lucide-react'

// Map categories to icons and colors
const categoryConfig = {
  'Java': { icon: Code, color: '#f89820' },
  'Spring Boot': { icon: Server, color: '#6db33f' },
  'React': { icon: LayoutTemplate, color: '#61dafb' },
  'SQL': { icon: Database, color: '#00758f' },
  'System Design': { icon: Network, color: '#a855f7' },
  'General': { icon: Brain, color: 'var(--primary-color)' },
}

export default function CategorySelection() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        if (!supabase) return;
        // In postgres, getting unique categories
        // We'll just select category and filter uniquely in JS for simplicity on small datasets
        const { data, error } = await supabase.from('questions').select('category')
        if (error) throw error
        
        if (data) {
          const uniqueCats = [...new Set(data.map(q => q.category).filter(Boolean))]
          setCategories(uniqueCats)
        }
      } catch (err) {
        console.error('Error fetching categories:', err)
      } finally {
        setLoading(false)
      }
    }
    
    fetchCategories()
  }, [])

  const handleStartQuiz = (category) => {
    navigate(`/quiz/session?category=${encodeURIComponent(category)}`)
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <Brain className="lucide-spin" size={48} color="var(--primary-color)" />
      </div>
    )
  }

  return (
    <div className="category-content">
      <div style={{ marginBottom: '32px' }}>
        <h2>Select a Topic</h2>
        <p style={{ color: 'var(--text-muted)' }}>Choose an interview category to start your mock session.</p>
      </div>

      <div className="dashboard-grid">
        {categories.map(cat => {
          const config = categoryConfig[cat] || categoryConfig['General']
          const Icon = config.icon
          
          return (
            <div 
              key={cat} 
              className="glass glass-card stat-card category-card"
              style={{ cursor: 'pointer', transition: 'transform 0.2s, box-shadow 0.2s', padding: '32px 24px' }}
              onClick={() => handleStartQuiz(cat)}
              onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-4px)'; e.currentTarget.style.boxShadow = '0 12px 24px rgba(0,0,0,0.2)' }}
              onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'none' }}
            >
              <Icon size={48} color={config.color} style={{ marginBottom: '16px' }} />
              <div style={{ fontSize: '1.25rem', fontWeight: 600, color: 'white', marginBottom: '8px' }}>{cat}</div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Practice interview questions for {cat}</div>
            </div>
          )
        })}
      </div>
      
      {categories.length === 0 && (
        <div className="glass glass-card" style={{ textAlign: 'center', padding: '48px' }}>
          No categories found. Make sure you have loaded questions into your Supabase database!
        </div>
      )}
    </div>
  )
}
