package com.TestFlashCard.FlashCard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.Enum.LearningStatus;
import com.TestFlashCard.FlashCard.entity.FlashCard;
import com.TestFlashCard.FlashCard.entity.FlashCardTopic;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IFlashCardTopic_Repository;
import com.TestFlashCard.FlashCard.repository.IFlashCard_Repository;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;
import com.TestFlashCard.FlashCard.request.FlashCardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicUpdateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardUpdateRequest;
import com.TestFlashCard.FlashCard.response.ListFlashCardTopicResponse;
import com.TestFlashCard.FlashCard.response.ListFlashCardsResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlashCardService {

    @Autowired
    public final IFlashCard_Repository flashCard_Repository;
    @Autowired
    public final IFlashCardTopic_Repository flashCardTopic_Repository;
    @Autowired
    public final IUser_Repository user_Repository;

    public List<ListFlashCardTopicResponse> getFlashCardTopicsByUser(int userID) {
        if (!user_Repository.existsById(userID))
            throw new ResourceNotFoundException("User not found with id: " + userID);
        return flashCardTopic_Repository.findByUserId(userID).stream().map(this::convertTopicsResponse).toList();
    }

    public List<ListFlashCardsResponse> getFlashCardsByTopic(int topicID) {
        if (!flashCardTopic_Repository.existsById(topicID)) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicID);
        }

        return flashCard_Repository.findByTopicId(topicID).stream().map(this::convertToResponse).toList();
    }

    private ListFlashCardsResponse convertToResponse(FlashCard flashCard) {
        return new ListFlashCardsResponse(
                flashCard.getId(),
                flashCard.getTitle(),
                flashCard.getReviewDate(),
                flashCard.getCycle(),
                flashCard.getLearningStatus().name());
    }

    private ListFlashCardTopicResponse convertTopicsResponse(FlashCardTopic topic) {
        return new ListFlashCardTopicResponse(topic.getId(), topic.getTitle(), topic.getStatus().name());
    }

    @Transactional
    public void createFlashCard(FlashCardCreateRequest flashCardDetail) throws Exception {
        FlashCardTopic topic = flashCardTopic_Repository.findById(flashCardDetail.getTopicID()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Cannot find FlashCard's topic with id: " + flashCardDetail.getTopicID()));

        FlashCard flashCard = new FlashCard();
        flashCard.setTitle(flashCardDetail.getTitle());
        flashCard.setCycle(flashCardDetail.getCycle());
        flashCard.setTopic(topic);
        flashCard.setLearningStatus(LearningStatus.NEW);
        flashCard_Repository.save(flashCard);
    }

    @Transactional
    public void createFlashCardTopic(FlashCardTopicCreateRequest flashCardTopicDetail) {
        User user = user_Repository.findById(flashCardTopicDetail.getUserID()).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find User with id: " + flashCardTopicDetail.getUserID()));
        FlashCardTopic topic = new FlashCardTopic();
        topic.setTitle(flashCardTopicDetail.getTitle());
        topic.setUser(user);
        flashCardTopic_Repository.save(topic);
    }

    @Transactional
    public void updateTopic(FlashCardTopicUpdateRequest topicDetail) {
        FlashCardTopic topic = flashCardTopic_Repository.findById(topicDetail.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find topic with id: " + topicDetail.getId()));
        if (topicDetail.getTitle() != null)
            topic.setTitle(topicDetail.getTitle());
        if (topicDetail.getStatus() != null)
            topic.setStatus(topicDetail.getStatus());
        flashCardTopic_Repository.save(topic);
    }

    @Transactional
    public void updateFlashCard(FlashCardUpdateRequest flashCardDetail) {
        FlashCard flashCard = flashCard_Repository.findById(flashCardDetail.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find FlashCard with id: " + flashCardDetail.getId()));
        if (flashCardDetail.getTitle() != null)
            flashCard.setTitle(flashCardDetail.getTitle());
        if (flashCardDetail.getCycle() != null)
            flashCard.setCycle(flashCardDetail.getCycle());
        if (flashCardDetail.getLearningStatus() != null)
            flashCard.setLearningStatus(flashCardDetail.getLearningStatus());
        if (flashCardDetail.getReviewDate() != null)
            flashCard.setReviewDate(flashCardDetail.getReviewDate());

        flashCard_Repository.save(flashCard);
    }

    @Transactional
    public void deleteTopic(int id){
        FlashCardTopic topic=flashCardTopic_Repository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find Topic with id: "+id)
        );
        flashCardTopic_Repository.delete(topic);
    }
    @Transactional
    public void deleteFlashCard(int id){
        FlashCard flashCard=flashCard_Repository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find FlashCard with id: "+ id)
        );
        flashCard_Repository.delete(flashCard);
    }
}
