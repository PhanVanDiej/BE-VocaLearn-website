package com.TestFlashCard.FlashCard.service;

import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.Enum.LearningStatus;
import com.TestFlashCard.FlashCard.entity.FlashCard;
import com.TestFlashCard.FlashCard.entity.FlashCardTopic;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.GlobalExceptionHandler;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IFlashCardTopic_Repository;
import com.TestFlashCard.FlashCard.repository.IFlashCard_Repository;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;
import com.TestFlashCard.FlashCard.request.FlashCardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicCreateRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlashCardService {
    
    public final IFlashCard_Repository flashCard_Repository;
    public final IFlashCardTopic_Repository flashCardTopic_Repository;
    public final IUser_Repository user_Repository;

    @Transactional
    public void createFlashCard (FlashCardCreateRequest flashCardDetail) throws Exception{
        FlashCardTopic topic = flashCardTopic_Repository.findById(flashCardDetail.getTopicID()).orElseThrow( 
            ()-> new ResourceNotFoundException("Cannot find FlashCard's topic with id: "+flashCardDetail.getTopicID())
        );

        FlashCard flashCard = new FlashCard();
        flashCard.setTitle(flashCardDetail.getTitle());
        flashCard.setStatus(flashCardDetail.getStatus());
        flashCard.setCycle(flashCardDetail.getCycle());
        flashCard.setTopic(topic);
        flashCard.setLearningStatus(LearningStatus.NEW);
        flashCard_Repository.save(flashCard);
    }

    @Transactional
    public void createFlashCardTopic(FlashCardTopicCreateRequest flashCardTopicDetail)throws Exception{
        User user=user_Repository.findById(flashCardTopicDetail.getUserID()).orElseThrow(
            () -> new ResourceNotFoundException("Cannot find User with id: "+ flashCardTopicDetail.getUserID())
        );
        FlashCardTopic topic= new FlashCardTopic();
        topic.setTitle(flashCardTopicDetail.getTitle());
        topic.setUser(user);
        flashCardTopic_Repository.save(topic);
    }
}
