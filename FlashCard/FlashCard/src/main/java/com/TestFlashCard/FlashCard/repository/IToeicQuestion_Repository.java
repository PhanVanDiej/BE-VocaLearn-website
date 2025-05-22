package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.ToeicQuestion;

@Repository
public interface IToeicQuestion_Repository extends JpaRepository<ToeicQuestion,Integer>{
    int countQuestionsByExamId(int examID);
}
