package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.FlashCardTopic;

@Repository
public interface IFlashCardTopic_Repository
        extends JpaRepository<FlashCardTopic, Integer>, JpaSpecificationExecutor<FlashCardTopic> {
    List<FlashCardTopic> findByUserId(int userID);
    List<FlashCardTopic>findAll();
}
