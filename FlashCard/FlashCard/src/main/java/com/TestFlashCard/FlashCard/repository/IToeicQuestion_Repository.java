package com.TestFlashCard.FlashCard.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;

@Repository
public interface IToeicQuestion_Repository extends JpaRepository<ToeicQuestion,Integer>{
    int countQuestionsByExamId(int examID);
    List<ToeicQuestion> findByExamId(int examID);
    List<ToeicQuestion> findAllByExam(Exam exam);
    List<ToeicQuestion> findAllByExamAndPartIn(Exam exam, Collection<String> parts);
    List<ToeicQuestion> findByExamIdAndPart(Integer examId, String part);
}
