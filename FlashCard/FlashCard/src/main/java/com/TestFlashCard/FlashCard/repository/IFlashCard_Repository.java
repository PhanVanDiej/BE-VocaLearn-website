package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.FlashCard;

@Repository
public interface IFlashCard_Repository extends JpaRepository<FlashCard,Integer>{
    
}
