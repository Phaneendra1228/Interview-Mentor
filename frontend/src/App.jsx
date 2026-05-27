import { Routes, Route } from 'react-router-dom'
import { Suspense, lazy } from 'react'
import Layout from './components/Layout'
import Login from './pages/Login'
const Dashboard = lazy(() => import('./pages/Dashboard'))
const QuestionBank = lazy(() => import('./pages/QuestionBank'))
const CategorySelection = lazy(() => import('./pages/CategorySelection'))
const QuizSession = lazy(() => import('./pages/QuizSession'))
const QuizResults = lazy(() => import('./pages/QuizResults'))
const Flashcards = lazy(() => import('./pages/Flashcards'))
const Behavioral = lazy(() => import('./pages/Behavioral'))
const SimulationMode = lazy(() => import('./pages/SimulationMode'))
const Analytics = lazy(() => import('./pages/Analytics'))
const History = lazy(() => import('./pages/History'))
const Bookmarks = lazy(() => import('./pages/Bookmarks'))
const Achievements = lazy(() => import('./pages/Achievements'))
const Resources = lazy(() => import('./pages/Resources'))
const Profile = lazy(() => import('./pages/Profile'))

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Layout />}>
        <Route index element={<Suspense fallback={<div>Loading...</div>}><Dashboard /></Suspense>} />
        <Route path="questions" element={<Suspense fallback={<div>Loading...</div>}><QuestionBank /></Suspense>} />
        <Route path="quiz" element={<Suspense fallback={<div>Loading...</div>}><CategorySelection /></Suspense>} />
        <Route path="quiz/session" element={<Suspense fallback={<div>Loading...</div>}><QuizSession /></Suspense>} />
        <Route path="quiz/results" element={<Suspense fallback={<div>Loading...</div>}><QuizResults /></Suspense>} />
        <Route path="flashcards" element={<Suspense fallback={<div>Loading...</div>}><Flashcards /></Suspense>} />
        <Route path="behavioral" element={<Suspense fallback={<div>Loading...</div>}><Behavioral /></Suspense>} />
        <Route path="simulation" element={<Suspense fallback={<div>Loading...</div>}><SimulationMode /></Suspense>} />
        <Route path="analytics" element={<Suspense fallback={<div>Loading Analytics...</div>}><Analytics /></Suspense>} />
        <Route path="history" element={<Suspense fallback={<div>Loading...</div>}><History /></Suspense>} />
        <Route path="bookmarks" element={<Suspense fallback={<div>Loading...</div>}><Bookmarks /></Suspense>} />
        <Route path="achievements" element={<Suspense fallback={<div>Loading...</div>}><Achievements /></Suspense>} />
        <Route path="resources" element={<Suspense fallback={<div>Loading...</div>}><Resources /></Suspense>} />
        <Route path="profile" element={<Suspense fallback={<div>Loading...</div>}><Profile /></Suspense>} />
      </Route>
    </Routes>
  )
}
