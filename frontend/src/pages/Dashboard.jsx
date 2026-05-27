import { Target, BookOpen, Brain, Play } from 'lucide-react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'

export default function Dashboard() {
  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.15
      }
    }
  }

  const itemVariants = {
    hidden: { opacity: 0, y: 30, scale: 0.95 },
    show: { 
      opacity: 1, 
      y: 0, 
      scale: 1,
      transition: { type: 'spring', stiffness: 300, damping: 24 }
    }
  }

  return (
    <motion.div 
      className="dashboard-content"
      variants={containerVariants}
      initial="hidden"
      animate="show"
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h2>Welcome back, Candidate!</h2>
          <p style={{ color: 'var(--text-muted)' }}>Ready for your next interview prep session?</p>
        </div>
        <Link to="/quiz" className="btn" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none' }}>
          <Play size={18} /> Start Mock Interview
        </Link>
      </div>

      <div className="dashboard-grid">
        <motion.div variants={itemVariants} className="glass glass-card stat-card">
          <Target className="stat-icon" color="var(--primary-color)" size={32} />
          <div className="stat-title">Questions Mastered</div>
          <div className="stat-value">24</div>
        </motion.div>
        <motion.div variants={itemVariants} className="glass glass-card stat-card">
          <BookOpen className="stat-icon" color="var(--secondary-color)" size={32} />
          <div className="stat-title">Topics Covered</div>
          <div className="stat-value">6</div>
        </motion.div>
        <motion.div variants={itemVariants} className="glass glass-card stat-card">
          <Brain className="stat-icon" color="#eab308" size={32} />
          <div className="stat-title">Current Streak</div>
          <div className="stat-value">3 Days</div>
        </motion.div>
      </div>
      
      <motion.h3 variants={itemVariants} style={{ marginTop: '48px', marginBottom: '24px' }}>Recommended Practice</motion.h3>
      <motion.div variants={itemVariants} className="glass glass-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h4 style={{ marginBottom: '8px' }}>System Design: Rate Limiting</h4>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: 0 }}>Review common rate limiting algorithms and their implementation tradeoffs.</p>
        </div>
        <button className="btn btn-secondary">Review Now</button>
      </motion.div>
    </motion.div>
  )
}
