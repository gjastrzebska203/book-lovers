package com.booklovers.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.booklovers.community.model.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}