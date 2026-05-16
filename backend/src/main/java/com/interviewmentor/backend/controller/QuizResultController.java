package com.interviewmentor.backend.controller;

import com.interviewmentor.backend.model.QuizResult;
import com.interviewmentor.backend.repository.QuizResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class QuizResultController {

    @Autowired
    private QuizResultRepository quizResultRepository;

    @GetMapping("/session/{sessionId}")
    public List<QuizResult> getResultsBySession(@PathVariable Long sessionId) {
        return quizResultRepository.findBySessionId(sessionId);
    }

    @PostMapping
    public QuizResult createResult(@RequestBody QuizResult result) {
        return quizResultRepository.save(result);
    }
}
