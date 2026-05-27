import { useState, useEffect } from 'react'
import {
  Chart as ChartJS,
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement
} from 'chart.js'
import { Radar, Line, Bar } from 'react-chartjs-2'
import { BarChart2, TrendingUp, Activity, Award } from 'lucide-react'

ChartJS.register(
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement
)

ChartJS.defaults.color = 'rgba(255, 255, 255, 0.6)';
ChartJS.defaults.font.family = 'Inter, system-ui, sans-serif';

// Mock data
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

  const radarData = {
    labels: categoryMastery.map(c => c.subject),
    datasets: [
      {
        label: 'Mastery',
        data: categoryMastery.map(c => c.A),
        backgroundColor: 'rgba(99, 102, 241, 0.5)',
        borderColor: 'rgba(99, 102, 241, 1)',
        borderWidth: 2,
      },
    ],
  }

  const lineData = {
    labels: recentScores.map(s => s.name),
    datasets: [
      {
        label: 'Accuracy',
        data: recentScores.map(s => s.score),
        borderColor: '#10b981',
        backgroundColor: '#10b981',
        tension: 0.4
      }
    ]
  }

  const barData = {
    labels: difficultyDistribution.map(d => d.name),
    datasets: [
      {
        label: 'Mastered',
        data: difficultyDistribution.map(d => d.count),
        backgroundColor: difficultyDistribution.map(d => d.fill),
        borderRadius: 4
      }
    ]
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
        <div className="glass glass-card" style={{ padding: '24px', display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ marginBottom: '24px', fontSize: '1.1rem' }}>Category Mastery</h3>
          <div style={{ width: '100%', flex: 1, display: 'flex', justifyContent: 'center', minHeight: '300px' }}>
            <Radar 
              data={radarData} 
              options={{ 
                maintainAspectRatio: false,
                scales: { 
                  r: { 
                    angleLines: { color: 'rgba(255,255,255,0.1)' },
                    grid: { color: 'rgba(255,255,255,0.1)' },
                    pointLabels: { color: 'rgba(255,255,255,0.6)' },
                    ticks: { display: false, max: 100 }
                  } 
                },
                plugins: { legend: { display: false } }
              }} 
            />
          </div>
        </div>

        {/* Line Chart: Recent Trend */}
        <div className="glass glass-card" style={{ padding: '24px', display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ marginBottom: '24px', fontSize: '1.1rem' }}>Accuracy Trend (Last 7 Days)</h3>
          <div style={{ width: '100%', flex: 1, minHeight: '300px' }}>
            <Line 
              data={lineData} 
              options={{ 
                maintainAspectRatio: false,
                scales: {
                  x: { grid: { display: false } },
                  y: { grid: { color: 'rgba(255,255,255,0.1)' }, max: 100, min: 0 }
                },
                plugins: { legend: { display: false } }
              }} 
            />
          </div>
        </div>

        {/* Bar Chart: Difficulty Breakdown */}
        <div className="glass glass-card" style={{ padding: '24px', gridColumn: '1 / -1' }}>
          <h3 style={{ marginBottom: '24px', fontSize: '1.1rem' }}>Questions Mastered by Difficulty</h3>
          <div style={{ width: '100%', height: '300px' }}>
            <Bar 
              data={barData} 
              options={{ 
                indexAxis: 'y',
                maintainAspectRatio: false,
                scales: {
                  x: { grid: { color: 'rgba(255,255,255,0.1)' } },
                  y: { grid: { display: false } }
                },
                plugins: { legend: { display: false } }
              }} 
            />
          </div>
        </div>

      </div>
    </div>
  )
}
