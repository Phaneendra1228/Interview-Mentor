import { useState, useEffect } from 'react'
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer,
  Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
  LineChart, Line
} from 'recharts'
import { BarChart2, TrendingUp, Activity, Award } from 'lucide-react'

// Mock data since we don't have full history tracking in Supabase yet
const categoryMastery = [
  { subject: 'System Design', A: 85, fullMark: 100 },
  { subject: 'Algorithms', A: 60, fullMark: 100 },
  { subject: 'React', A: 90, fullMark: 100 },
  { subject: 'Java', A: 75, fullMark: 100 },
  { subject: 'SQL', A: 80, fullMark: 100 },
  { subject: 'Behavioral', A: 95, fullMark: 100 },
]

const recentScores = [
  { name: 'Mon', score: 65 },
  { name: 'Tue', score: 70 },
  { name: 'Wed', score: 68 },
  { name: 'Thu', score: 85 },
  { name: 'Fri', score: 82 },
  { name: 'Sat', score: 90 },
  { name: 'Sun', score: 95 },
]

const difficultyDistribution = [
  { name: 'Easy', count: 120, fill: '#10b981' },
  { name: 'Medium', count: 85, fill: '#f59e0b' },
  { name: 'Hard', count: 32, fill: '#ef4444' },
]

export default function Analytics() {
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Simulate data loading
    setTimeout(() => setLoading(false), 800)
  }, [])

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Analytics Engine...</div>
  }

  return (
    <div className="analytics-content" style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <BarChart2 color="var(--primary-color)" /> Performance Analytics
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Track your interview readiness, category mastery, and historical performance.
        </p>
      </div>

      {/* Top Stats */}
      <div className="dashboard-grid" style={{ marginBottom: '32px' }}>
        <div className="glass glass-card stat-card">
          <TrendingUp className="stat-icon" color="#10b981" size={32} />
          <div className="stat-title">Average Accuracy</div>
          <div className="stat-value">82%</div>
        </div>
        <div className="glass glass-card stat-card">
          <Activity className="stat-icon" color="#6366f1" size={32} />
          <div className="stat-title">Questions Answered</div>
          <div className="stat-value">237</div>
        </div>
        <div className="glass glass-card stat-card">
          <Award className="stat-icon" color="#f59e0b" size={32} />
          <div className="stat-title">Strongest Category</div>
          <div className="stat-value" style={{ fontSize: '1.2rem' }}>Behavioral</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
        
        {/* Radar Chart: Category Mastery */}
        <div className="glass glass-card" style={{ padding: '24px' }}>
          <h3 style={{ marginBottom: '24px', fontSize: '1.1rem' }}>Category Mastery</h3>
          <div style={{ width: '100%', height: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart cx="50%" cy="50%" outerRadius="80%" data={categoryMastery}>
                <PolarGrid stroke="rgba(255,255,255,0.1)" />
                <PolarAngleAxis dataKey="subject" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} />
                <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                <Radar name="Mastery" dataKey="A" stroke="var(--primary-color)" fill="var(--primary-color)" fillOpacity={0.5} />
                <RechartsTooltip contentStyle={{ backgroundColor: '#1e1e2d', border: 'none', borderRadius: '8px', color: 'white' }} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Line Chart: Recent Trend */}
        <div className="glass glass-card" style={{ padding: '24px' }}>
          <h3 style={{ marginBottom: '24px', fontSize: '1.1rem' }}>Accuracy Trend (Last 7 Days)</h3>
          <div style={{ width: '100%', height: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={recentScores}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                <XAxis dataKey="name" stroke="var(--text-muted)" tick={{ fill: 'var(--text-muted)' }} axisLine={false} tickLine={false} />
                <YAxis domain={[0, 100]} stroke="var(--text-muted)" tick={{ fill: 'var(--text-muted)' }} axisLine={false} tickLine={false} />
                <RechartsTooltip contentStyle={{ backgroundColor: '#1e1e2d', border: 'none', borderRadius: '8px', color: 'white' }} />
                <Line type="monotone" dataKey="score" stroke="#10b981" strokeWidth={3} dot={{ r: 4, fill: '#10b981' }} activeDot={{ r: 8 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Bar Chart: Difficulty Breakdown */}
        <div className="glass glass-card" style={{ padding: '24px', gridColumn: '1 / -1' }}>
          <h3 style={{ marginBottom: '24px', fontSize: '1.1rem' }}>Questions Mastered by Difficulty</h3>
          <div style={{ width: '100%', height: '250px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={difficultyDistribution} layout="vertical" margin={{ top: 0, right: 30, left: 20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" horizontal={false} />
                <XAxis type="number" stroke="var(--text-muted)" tick={{ fill: 'var(--text-muted)' }} axisLine={false} tickLine={false} />
                <YAxis dataKey="name" type="category" stroke="var(--text-muted)" tick={{ fill: 'var(--text-muted)' }} axisLine={false} tickLine={false} />
                <RechartsTooltip cursor={{ fill: 'rgba(255,255,255,0.05)' }} contentStyle={{ backgroundColor: '#1e1e2d', border: 'none', borderRadius: '8px', color: 'white' }} />
                <Bar dataKey="count" radius={[0, 4, 4, 0]}>
                  {difficultyDistribution.map((entry, index) => (
                    <cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>
    </div>
  )
}
