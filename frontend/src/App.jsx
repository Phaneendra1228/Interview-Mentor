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
import History from './pages/History'
import Bookmarks from './pages/Bookmarks'
import Achievements from './pages/Achievements'
import Resources from './pages/Resources'
import Profile from './pages/Profile'

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
        <Route path="history" element={<History />} />
        <Route path="bookmarks" element={<Bookmarks />} />
        <Route path="achievements" element={<Achievements />} />
        <Route path="resources" element={<Resources />} />
        <Route path="profile" element={<Profile />} />
      </Route>
    </Routes>
  )
}
