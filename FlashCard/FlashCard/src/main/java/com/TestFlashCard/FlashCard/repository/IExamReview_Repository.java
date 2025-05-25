package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.ExamReview;

@Repository
public interface IExamReview_Repository extends JpaRepository<ExamReview,Integer> {

}
