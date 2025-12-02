package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ToeicQuestionRepository extends JpaRepository<ToeicQuestion, Integer> {

    @Query("SELECT MAX(t.indexNumber) FROM ToeicQuestion t WHERE t.exam.id = :examId")
    Integer findMaxIndexByExam(Integer examId);

    @Query("SELECT MAX(t.indexNumber) FROM ToeicQuestion t WHERE t.exam.id = :examId AND t.part = :part")
    Integer findMaxIndexByExamAndPart(Integer examId, String part);

}