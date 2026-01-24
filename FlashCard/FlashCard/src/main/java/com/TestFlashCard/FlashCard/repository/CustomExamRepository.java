package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TestFlashCard.FlashCard.entity.CustomExam;
import java.util.List;
import java.util.Optional;


public interface CustomExamRepository extends JpaRepository<CustomExam, Integer>{
    List<CustomExam> findByUserId(int userId);
    Optional<CustomExam> findByUserIdAndCustomExamId(int userId, int examId);
    void deleteByCustomExam_Id(Integer examId);
}