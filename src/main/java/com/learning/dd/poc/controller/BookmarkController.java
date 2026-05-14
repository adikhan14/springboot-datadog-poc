package com.learning.dd.poc.controller;

import com.learning.dd.poc.model.Bookmark;
import com.learning.dd.poc.service.BookmarkService;
import com.learning.dd.poc.tracing.SpanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private static final Logger log = LoggerFactory.getLogger(BookmarkController.class);

    private final BookmarkService service;

    public BookmarkController(BookmarkService service) {
        this.service = service;
    }

    @GetMapping
    public List<Bookmark> getAll() {
        log.info("Fetching all bookmarks");
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bookmark> getById(@PathVariable Long id) {
        log.info("Fetching bookmark id={}", id);
        SpanUtils.setRootTag("bookmark.id", id);
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Bookmark> create(@RequestBody Bookmark bookmark) {
        Bookmark created = service.create(bookmark);
        log.info("Created bookmark id={}", created.getId());
        SpanUtils.setRootTag("bookmark.id", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bookmark> update(@PathVariable Long id, @RequestBody Bookmark bookmark) {
        log.info("Updating bookmark id={}", id);
        SpanUtils.setRootTag("bookmark.id", id);
        return service.update(id, bookmark)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting bookmark id={}", id);
        SpanUtils.setRootTag("bookmark.id", id);
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
