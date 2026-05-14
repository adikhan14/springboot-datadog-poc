package com.learning.dd.poc.service;

import com.learning.dd.poc.model.Bookmark;
import com.learning.dd.poc.repository.BookmarkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookmarkService {

    private final BookmarkRepository repository;

    public BookmarkService(BookmarkRepository repository) {
        this.repository = repository;
    }

    public List<Bookmark> findAll() {
        return repository.findAll();
    }

    public Optional<Bookmark> findById(Long id) {
        return repository.findById(id);
    }

    public Bookmark create(Bookmark bookmark) {
        return repository.save(bookmark);
    }

    public Optional<Bookmark> update(Long id, Bookmark incoming) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(incoming.getTitle());
            existing.setDescription(incoming.getDescription());
            existing.setUrl(incoming.getUrl());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
