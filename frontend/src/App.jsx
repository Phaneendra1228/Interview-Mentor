import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import QuestionBank from './pages/QuestionBank'
import CategorySelection from './pages/CategorySelection'
import QuizSession from './pages/QuizSession'
import QuizResults from './pages/QuizResults'
import Flashcards from './pages/Flashcards'
import Behavioral from './pages/Behavioral'
import SimulationMode from './pages/SimulationMode'
import Analytics from './pages/Analytics'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="questions" element={<QuestionBank />} />
        <Route path="quiz" element={<CategorySelection />} />
        <Route path="quiz/session" element={<QuizSession />} />
        <Route path="quiz/results" element={<QuizResults />} />
        <Route path="flashcards" element={<Flashcards />} />
        <Route path="behavioral" element={<Behavioral />} />
        <Route path="simulation" element={<SimulationMode />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="profile" element={<div style={{ padding: '40px' }}><h2>Profile</h2><p>Settings coming soon!</p></div>} />
      </Route>
    </Routes>
  )
}
