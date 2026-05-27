import { BookOpen, ExternalLink, FileText, Video, Target } from 'lucide-react'

const resources = [
  { category: 'System Design', items: [
    { title: 'System Design Primer', type: 'Github', icon: BookOpen, url: 'https://github.com/donnemartin/system-design-primer' },
    { title: 'Grokking the System Design Interview', type: 'Course', icon: Video, url: 'https://www.designgurus.io/course/grokking-the-system-design-interview' },
  ]},
  { category: 'Algorithms', items: [
    { title: 'Cracking the Coding Interview', type: 'Book', icon: BookOpen, url: 'https://www.crackingthecodinginterview.com/' },
    { title: 'LeetCode Top 150', type: 'Practice', icon: Target, url: 'https://leetcode.com/studyplan/top-interview-150/' },
  ]},
  { category: 'Behavioral', items: [
    { title: 'The STAR Method Masterclass', type: 'Article', icon: FileText, url: 'https://www.themuse.com/advice/star-interview-method' },
    { title: 'Amazon Leadership Principles', type: 'Guide', icon: FileText, url: 'https://www.amazon.jobs/content/en/our-workplace/leadership-principles' },
  ]}
]

export default function Resources() {
  return (
    <div className="resources-content" style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <BookOpen color="#3b82f6" /> Study Resources
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Curated materials, articles, and links to help you prepare for your interviews.
        </p>
      </div>

      <div style={{ display: 'grid', gap: '32px' }}>
        {resources.map((section, idx) => (
          <div key={idx}>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '16px', color: 'var(--primary-color)' }}>{section.category}</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '16px' }}>
              {section.items.map((item, i) => {
                const Icon = item.icon || ExternalLink
                return (
                  <a href={item.url} target="_blank" rel="noopener noreferrer" key={i} className="glass glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px', textDecoration: 'none', color: 'inherit' }}>
                    <div style={{ background: 'var(--icon-bg)', padding: '12px', borderRadius: '8px' }}>
                      <Icon size={24} color="var(--text-muted)" />
                    </div>
                    <div style={{ flex: 1 }}>
                      <h4 style={{ fontSize: '1.05rem', marginBottom: '4px', color: 'var(--text-main)' }}>{item.title}</h4>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{item.type}</span>
                    </div>
                    <ExternalLink size={18} color="var(--primary-color)" />
                  </a>
                )
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
