package com.interviewmentor.backend.controller;

import com.interviewmentor.backend.model.Bookmark;
import com.interviewmentor.backend.repository.BookmarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @GetMapping("/user/{userId}")
    public List<Bookmark> getBookmarksByUser(@PathVariable Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@RequestBody Bookmark bookmark) {
        Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndQuestionId(bookmark.getUserId(), bookmark.getQuestionId());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().build(); // Already bookmarked
        }
        return ResponseEntity.ok(bookmarkRepository.save(bookmark));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        if (bookmarkRepository.existsById(id)) {
            bookmarkRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
