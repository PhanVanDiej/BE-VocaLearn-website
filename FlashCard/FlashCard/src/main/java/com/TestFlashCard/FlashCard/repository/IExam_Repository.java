package com.TestFlashCard.FlashCard.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;

@Repository
public interface IExam_Repository extends JpaRepository<Exam, Integer>, JpaSpecificationExecutor<Exam>{
    List<Exam>findAll();
    List<Exam> findByIsDeletedFalseOrderByCreatedAtDesc();
    List<Exam> findAllByOrderByCreatedAtDesc();
    List<Exam> findTop3ByOrderByAttempsDesc();
}