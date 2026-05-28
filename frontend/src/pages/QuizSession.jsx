import { useState, useEffect, useRef } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { supabase } from '../supabaseClient'
import { Clock, CheckCircle, XCircle, AlertCircle, Video, ShieldAlert } from 'lucide-react'
import * as tf from '@tensorflow/tfjs'
import * as blazeface from '@tensorflow-models/blazeface'

export default function QuizSession() {
  const [searchParams] = useSearchParams()
  const category = searchParams.get('category')
  const navigate = useNavigate()

  const isSimulation = searchParams.get('simulation') === 'true'
  const hasCamera = searchParams.get('camera') === 'true'
  const isStrict = searchParams.get('strict') === 'true'
  const timeParam = parseInt(searchParams.get('time'))

  const [questions, setQuestions] = useState([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [answers, setAnswers] = useState({})
  const [loading, setLoading] = useState(true)
  const videoRef = useRef(null)
  
  // Proctoring states
  const [violationWarning, setViolationWarning] = useState(null)
  const [forceSubmitMessage, setForceSubmitMessage] = useState(null)
  const violationCountRef = useRef(0)
  
  // Base initial time
  const [timeLeft, setTimeLeft] = useState(isStrict ? 120 : (timeParam ? timeParam * 60 : 600))

  useEffect(() => {
    const fetchQuizQuestions = async () => {
      if (!category || !supabase) return
      try {
        const { data, error } = await supabase
          .from('questions')
          .select('*')
          .eq('category', category)
          .limit(10)
        
        if (error) throw error
        setQuestions(data)
      } catch (err) {
        console.error('Failed to fetch quiz questions', err)
      } finally {
        setLoading(false)
      }
    }
    fetchQuizQuestions()
  }, [category])

  const handleCompleteRef = useRef(null);

  const handleViolation = (msg) => {
    violationCountRef.current += 1;
    setViolationWarning(`Warning ${violationCountRef.current}/3: ${msg}`);
    
    if (violationCountRef.current >= 3) {
      setForceSubmitMessage(msg);
    }
  }

  // Camera & Proctoring effect
  useEffect(() => {
    let stream = null;
    let model = null;
    let proctorInterval = null;
    let isActive = true;

    const initCameraAndProctoring = async () => {
      if (hasCamera) {
        try {
          stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
          if (videoRef.current && isActive) {
            videoRef.current.srcObject = stream
            
            // Wait for video to start playing
            await new Promise((resolve) => {
              videoRef.current.onloadedmetadata = () => {
                resolve();
              };
            });
            
            // Initialize Proctoring Model
            await tf.ready();
            model = await blazeface.load();
            
            proctorInterval = setInterval(async () => {
              if (!videoRef.current || !model || !isActive) return;
              
              try {
                const predictions = await model.estimateFaces(videoRef.current, false);
                
                if (predictions.length === 0) {
                  handleViolation("No face detected! Please ensure your face is fully visible.");
                } else if (predictions.length > 1) {
                  handleViolation("Multiple people detected! Only you should be in the frame.");
                } else {
                  // Reset violation count on successful check
                  if (violationCountRef.current > 0) {
                    violationCountRef.current = 0;
                    setViolationWarning(null);
                  }
                }
              } catch (e) {
                console.error("Proctoring error:", e);
              }
            }, 1000);
          }
        } catch (err) {
          console.error("Error accessing camera:", err)
        }
      }
    }
    initCameraAndProctoring()
    
    return () => {
      isActive = false;
      if (stream) stream.getTracks().forEach(track => track.stop())
      if (proctorInterval) clearInterval(proctorInterval)
    }
  }, [hasCamera])

  // Timer effect
  useEffect(() => {
    if (loading || questions.length === 0) return
    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          if (isStrict) {
            setCurrentIndex(curr => {
              if (curr < questions.length - 1) {
                return curr + 1;
              } else {
                clearInterval(timer);
                handleComplete();
                return curr;
              }
            });
            return 120; // reset for next question
          } else {
            clearInterval(timer)
            handleComplete()
            return 0
          }
        }
        return prev - 1
      })
    }, 1000)
    return () => clearInterval(timer)
  }, [loading, questions.length, isStrict])

  // Reset timer on manual question change if strict timing is on
  useEffect(() => {
    if (isStrict && !loading && questions.length > 0) {
      setTimeLeft(120)
    }
  }, [currentIndex, isStrict, loading, questions.length])

  const handleSelectOption = (option) => {
    setAnswers(prev => ({ ...prev, [currentIndex]: option }))
  }

  const handleNext = () => {
    if (currentIndex < questions.length - 1) {
      setCurrentIndex(prev => prev + 1)
    } else {
      handleComplete()
    }
  }

  const handleComplete = () => {
    // Calculate results
    let correct = 0
    questions.forEach((q, idx) => {
      if (answers[idx] === q.correct_answer) {
        correct++
      }
    })
    
    // In a real app, save to Supabase quiz_results table here
    
    // Pass state to result page
    navigate('/quiz/results', { 
      state: { 
        total: questions.length, 
        correct, 
        timeTaken: 600 - timeLeft,
        category,
        questions,
        answers,
        violation: forceSubmitMessage
      } 
    })
  }

  useEffect(() => {
    handleCompleteRef.current = handleComplete;
  });

  useEffect(() => {
    if (forceSubmitMessage && handleCompleteRef.current) {
      alert(`PROCTORING VIOLATION: ${forceSubmitMessage}\nYour quiz has been automatically submitted.`);
      handleCompleteRef.current();
    }
  }, [forceSubmitMessage])

  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${m}:${s.toString().padStart(2, '0')}`
  }

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Quiz...</div>
  if (questions.length === 0) return <div style={{ padding: '40px' }}>No questions available for {category}.</div>

  const currentQ = questions[currentIndex]
  
  // Mix options if they exist (assuming options A,B,C,D are populated or we just have text-based answers)
  // For standard MCQs, assuming option_a, option_b etc are in DB:
  const options = [
    { id: 'A', text: currentQ.option_a },
    { id: 'B', text: currentQ.option_b },
    { id: 'C', text: currentQ.option_c },
    { id: 'D', text: currentQ.option_d },
  ].filter(o => o.text != null)

  return (
    <div className="quiz-session-content" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h2 style={{ color: 'var(--primary-color)' }}>{category} Quiz {isSimulation && <span style={{fontSize: '1rem', color: 'var(--text-muted)', marginLeft: '12px'}}>(Simulation)</span>}</h2>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.2rem', color: timeLeft < 60 ? 'var(--danger-color)' : 'var(--text-main)' }}>
            <Clock size={24} />
            <span style={{ fontFamily: 'monospace' }}>{formatTime(timeLeft)}</span>
          </div>
        </div>
      </div>

      <div className="glass glass-card" style={{ padding: '32px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px', color: 'var(--text-muted)' }}>
          <span>Question {currentIndex + 1} of {questions.length}</span>
          <span>Difficulty: {currentQ.difficulty || 'Medium'}</span>
        </div>

        <h3 style={{ fontSize: '1.4rem', lineHeight: '1.6', marginBottom: '32px' }}>
          {currentQ.question_text}
        </h3>

        {options.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {options.map((opt) => (
              <div 
                key={opt.id}
                onClick={() => handleSelectOption(opt.id)}
                style={{ 
                  padding: '16px', 
                  borderRadius: '8px', 
                  border: `2px solid ${answers[currentIndex] === opt.id ? 'var(--primary-color)' : 'var(--border-color)'}`,
                  background: answers[currentIndex] === opt.id ? 'rgba(99, 102, 241, 0.1)' : 'var(--btn-secondary-bg)',
                  cursor: 'pointer',
                  display: 'flex',
                  gap: '16px'
                }}
              >
                <span style={{ fontWeight: 'bold', color: 'var(--text-muted)' }}>{opt.id}.</span>
                <span>{opt.text}</span>
              </div>
            ))}
          </div>
        ) : (
          <div style={{ marginBottom: '24px' }}>
            <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>
              <AlertCircle size={16} style={{ display: 'inline', marginRight: '8px', verticalAlign: 'text-bottom' }}/>
              This is an open-ended question. In a real interview, you would speak your answer. For now, formulate your answer mentally or write it down.
            </p>
            <textarea 
              placeholder="Draft your answer here (optional)..."
              value={answers[currentIndex] || ''}
              onChange={(e) => handleSelectOption(e.target.value)}
              style={{ width: '100%', height: '150px', marginTop: '16px' }}
            />
          </div>
        )}

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '32px' }}>
          <button className="btn" onClick={handleNext}>
            {currentIndex === questions.length - 1 ? 'Finish Quiz' : 'Next Question'}
          </button>
        </div>
      </div>

      {hasCamera && (
        <div style={{
          position: 'fixed',
          bottom: '32px',
          right: '32px',
          width: '280px',
          height: '210px',
          borderRadius: '16px',
          overflow: 'hidden',
          boxShadow: '0 12px 40px rgba(0,0,0,0.4)',
          border: '2px solid var(--border-color)',
          background: '#000',
          zIndex: 1000
        }}>
          <video 
            ref={videoRef} 
            autoPlay 
            muted 
            playsInline
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
          <div style={{ position: 'absolute', top: '12px', right: '12px', display: 'flex', alignItems: 'center', gap: '6px', color: '#ef4444', fontSize: '0.85rem', fontWeight: 'bold', background: 'rgba(0,0,0,0.7)', padding: '6px 12px', borderRadius: '16px' }}>
            <span style={{ width: '8px', height: '8px', background: '#ef4444', borderRadius: '50%', display: 'inline-block', boxShadow: '0 0 8px #ef4444' }}></span>
            REC
          </div>
          
          {violationWarning && (
            <div style={{ position: 'absolute', bottom: '12px', left: '12px', right: '12px', background: 'rgba(239, 68, 68, 0.9)', color: 'white', padding: '8px', borderRadius: '8px', fontSize: '0.8rem', fontWeight: '500', display: 'flex', alignItems: 'center', gap: '8px', animation: 'fade-in-up 0.3s forwards' }}>
              <ShieldAlert size={16} style={{ flexShrink: 0 }} />
              <span>{violationWarning}</span>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
