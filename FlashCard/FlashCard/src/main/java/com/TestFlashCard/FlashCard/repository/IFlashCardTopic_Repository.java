package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TestFlashCard.FlashCard.entity.FlashCardTopic;

public interface IFlashCardTopic_Repository extends JpaRepository<FlashCardTopic,Integer>{
    
}
