package com.interviewmentor.backend.controller;

import com.interviewmentor.backend.model.QuizSession;
import com.interviewmentor.backend.repository.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class QuizSessionController {

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @GetMapping
    public List<QuizSession> getAllSessions() {
        return quizSessionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizSession> getSessionById(@PathVariable Long id) {
        return quizSessionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<QuizSession> getSessionsByUser(@PathVariable Long userId) {
        return quizSessionRepository.findByUserId(userId);
    }

    @PostMapping
    public QuizSession createSession(@RequestBody QuizSession session) {
        return quizSessionRepository.save(session);
    }
}
