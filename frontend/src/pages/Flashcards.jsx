import { useState, useEffect } from 'react'
import { Brain, RotateCcw, ThumbsUp, ThumbsDown, Check } from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function Flashcards() {
  const [questions, setQuestions] = useState([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [isFlipped, setIsFlipped] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchFlashcards = async () => {
      try {
        if (!supabase) return
        // Fetch a random batch of questions for review
        const { data, error } = await supabase.from('questions').select('*').limit(20)
        if (error) throw error
        // Shuffle client side for variety
        const shuffled = data.sort(() => 0.5 - Math.random())
        setQuestions(shuffled)
      } catch (err) {
        console.error('Failed to fetch flashcards', err)
      } finally {
        setLoading(false)
      }
    }
    fetchFlashcards()
  }, [])

  const handleReview = (quality) => {
    // In a real Spaced Repetition System (SRS), we would update the DB here
    // with SuperMemo-2 or similar algorithm values (interval, ease factor)
    console.log(`Reviewed as: ${quality}`)
    
    setIsFlipped(false)
    // Small delay to allow flip animation to reset before changing content
    setTimeout(() => {
      if (currentIndex < questions.length - 1) {
        setCurrentIndex(prev => prev + 1)
      } else {
        // End of deck
        setCurrentIndex(-1)
      }
    }, 300)
  }

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center' }}><Brain className="lucide-spin" size={48} color="var(--primary-color)" /></div>
  }

  if (currentIndex === -1 || questions.length === 0) {
    return (
      <div className="flashcards-content" style={{ textAlign: 'center', padding: '80px 20px' }}>
        <Check size={64} color="#10b981" style={{ margin: '0 auto 24px' }} />
        <h2>Daily Review Complete!</h2>
        <p style={{ color: 'var(--text-muted)' }}>You've reviewed all due flashcards for today.</p>
        <button className="btn" style={{ marginTop: '24px' }} onClick={() => window.location.reload()}>Review More</button>
      </div>
    )
  }

  const currentCard = questions[currentIndex]

  return (
    <div className="flashcards-content" style={{ maxWidth: '800px', margin: '0 auto', perspective: '1000px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
        <h2>Flashcards Review</h2>
        <span style={{ color: 'var(--text-muted)' }}>Card {currentIndex + 1} of {questions.length}</span>
      </div>

      {/* Card Container */}
      <div 
        style={{ 
          width: '100%', 
          height: '400px', 
          position: 'relative', 
          transformStyle: 'preserve-3d', 
          transition: 'transform 0.6s cubic-bezier(0.4, 0.0, 0.2, 1)',
          transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
          cursor: 'pointer'
        }}
        onClick={() => !isFlipped && setIsFlipped(true)}
      >
        {/* Front of Card (Question) */}
        <div className="glass glass-card" style={{ 
          position: 'absolute', 
          width: '100%', 
          height: '100%', 
          backfaceVisibility: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          padding: '40px',
          textAlign: 'center'
        }}>
          <span style={{ position: 'absolute', top: '24px', left: '24px', fontSize: '0.8rem', color: 'var(--primary-color)', textTransform: 'uppercase', letterSpacing: '0.1em' }}>
            {currentCard.category}
          </span>
          <h3 style={{ fontSize: '1.5rem', lineHeight: '1.5', fontWeight: 500 }}>
            {currentCard.question_text}
          </h3>
          <p style={{ position: 'absolute', bottom: '24px', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Click anywhere to reveal answer
          </p>
        </div>

        {/* Back of Card (Answer) */}
        <div className="glass glass-card" style={{ 
          position: 'absolute', 
          width: '100%', 
          height: '100%', 
          backfaceVisibility: 'hidden',
          transform: 'rotateY(180deg)',
          display: 'flex',
          flexDirection: 'column',
          padding: '40px',
          overflowY: 'auto'
        }}>
          <h4 style={{ color: 'var(--text-muted)', marginBottom: '16px', fontSize: '0.9rem', textTransform: 'uppercase' }}>Expected Answer</h4>
          <div style={{ fontSize: '1.2rem', lineHeight: '1.6', color: '#10b981', marginBottom: '24px' }}>
            {currentCard.correct_answer}
          </div>
          
          {currentCard.explanation && (
            <>
              <h4 style={{ color: 'var(--text-muted)', marginBottom: '8px', fontSize: '0.9rem', textTransform: 'uppercase' }}>Detailed Explanation</h4>
              <p style={{ lineHeight: '1.6', fontSize: '0.95rem' }}>{currentCard.explanation}</p>
            </>
          )}
        </div>
      </div>

      {/* Review Controls - Only visible when card is flipped */}
      <div style={{ 
        marginTop: '32px', 
        display: 'flex', 
        justifyContent: 'center', 
        gap: '16px',
        opacity: isFlipped ? 1 : 0,
        pointerEvents: isFlipped ? 'auto' : 'none',
        transition: 'opacity 0.3s ease'
      }}>
        <button 
          onClick={() => handleReview('hard')}
          style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '12px 24px', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}
        >
          <ThumbsDown size={18} /> Hard (Again)
        </button>
        <button 
          onClick={() => handleReview('good')}
          style={{ background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.2)', padding: '12px 24px', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}
        >
          <RotateCcw size={18} /> Good (1d)
        </button>
        <button 
          onClick={() => handleReview('easy')}
          style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.2)', padding: '12px 24px', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}
        >
          <ThumbsUp size={18} /> Easy (4d)
        </button>
      </div>
    </div>
  )
}
