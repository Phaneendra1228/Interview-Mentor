import { Award, Zap, Target, Book, Star, Clock } from 'lucide-react'

const badges = [
  { id: 1, name: 'First Steps', description: 'Complete your first mock interview session.', icon: Target, color: '#3b82f6', unlocked: true },
  { id: 2, name: 'On a Roll', description: 'Maintain a 3-day study streak.', icon: Zap, color: '#f59e0b', unlocked: true },
  { id: 3, name: 'Bookworm', description: 'Review 50 flashcards.', icon: Book, color: '#10b981', unlocked: true },
  { id: 4, name: 'Perfectionist', description: 'Score 100% on a System Design quiz.', icon: Star, color: '#8b5cf6', unlocked: false },
  { id: 5, name: 'Marathon', description: 'Complete a full 120-minute simulation.', icon: Clock, color: '#ef4444', unlocked: false },
  { id: 6, name: 'Top Tier', description: 'Answer 500 questions correctly across all categories.', icon: Award, color: '#ec4899', unlocked: false }
]

export default function Achievements() {
  const unlockedCount = badges.filter(b => b.unlocked).length

  return (
    <div className="achievements-content" style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Award color="#ec4899" /> Achievements
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Track your milestones and earn badges as you progress in your interview prep.
        </p>
      </div>

      <div className="glass glass-card" style={{ marginBottom: '32px', display: 'flex', alignItems: 'center', gap: '24px' }}>
        <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: 'rgba(236, 72, 153, 0.2)', display: 'flex', justifyContent: 'center', alignItems: 'center', border: '2px solid #ec4899' }}>
          <Award size={40} color="#ec4899" />
        </div>
        <div>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '4px' }}>{unlockedCount} / {badges.length} Unlocked</h3>
          <p style={{ color: 'var(--text-muted)' }}>Keep practicing to collect all badges!</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '24px' }}>
        {badges.map(badge => {
          const Icon = badge.icon
          return (
            <div 
              key={badge.id} 
              className="glass glass-card" 
              style={{ 
                padding: '24px', 
                textAlign: 'center',
                opacity: badge.unlocked ? 1 : 0.5,
                filter: badge.unlocked ? 'none' : 'grayscale(100%)',
                position: 'relative'
              }}
            >
              {!badge.unlocked && (
                <div style={{ position: 'absolute', top: '12px', right: '12px', fontSize: '0.75rem', background: 'rgba(255,255,255,0.1)', padding: '2px 8px', borderRadius: '12px' }}>
                  Locked
                </div>
              )}
              <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: `${badge.color}20`, display: 'flex', justifyContent: 'center', alignItems: 'center', margin: '0 auto 16px', boxShadow: badge.unlocked ? `0 0 15px ${badge.color}40` : 'none' }}>
                <Icon size={32} color={badge.color} />
              </div>
              <h4 style={{ fontSize: '1.1rem', marginBottom: '8px' }}>{badge.name}</h4>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>{badge.description}</p>
            </div>
          )
        })}
      </div>
    </div>
  )
}
