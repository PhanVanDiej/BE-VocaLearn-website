package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Blog;

@Repository
public interface IBlog_Repository extends JpaRepository<Blog,Integer> {
    
}