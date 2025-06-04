package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.BlogCategory;
import java.util.List;


@Repository
public interface IBlogCategory_Repository extends JpaRepository<BlogCategory,Integer> {
    BlogCategory findByTitle(String title);
    List<BlogCategory> findByIsDeletedFalse();
}
