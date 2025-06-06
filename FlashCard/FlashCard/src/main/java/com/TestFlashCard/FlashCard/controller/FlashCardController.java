package com.TestFlashCard.FlashCard.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.request.FlashCardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicUpdateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardUpdateRequest;
import com.TestFlashCard.FlashCard.response.FlashCardTopicPublicResponse;
import com.TestFlashCard.FlashCard.response.ListFlashCardTopicResponse;
import com.TestFlashCard.FlashCard.response.ListFlashCardsResponse;
import com.TestFlashCard.FlashCard.service.FlashCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/flashcard")
@RequiredArgsConstructor
public class FlashCardController {
    @Autowired
    private final FlashCardService flashCardService;

    @GetMapping("/getTopicsByUser/{userID}")
    public ResponseEntity<?> getAllTopics(@PathVariable int userID) {
        List<ListFlashCardTopicResponse> topics = flashCardService.getFlashCardTopicsByUser(userID);
        return new ResponseEntity<List<ListFlashCardTopicResponse>>(topics, HttpStatus.OK);
    }

    @GetMapping("/getFlashCardsByTopic/{topicID}")
    public ResponseEntity<?> getAllFlashCard(@PathVariable Integer topicID) {
        List<ListFlashCardsResponse> flashCards = flashCardService.getFlashCardsByTopic(topicID);
        return new ResponseEntity<List<ListFlashCardsResponse>>(flashCards, HttpStatus.OK);
    }

    @PostMapping("/createFlashCard")
    public ResponseEntity<?> createFlashCard(@RequestBody @Valid FlashCardCreateRequest request) {
        try {
            flashCardService.createFlashCard(request);
            return ResponseEntity.ok().body("Create Flashcard successfull!");
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Error: " + ex.getMessage());
        }
    }

    @PostMapping("/createTopic")
    public ResponseEntity<?> createFlashCardTopic(@RequestBody @Valid FlashCardTopicCreateRequest request,
            Principal principal) {
        try {
            flashCardService.createFlashCardTopic(request, principal.getName());
            return ResponseEntity.ok().body("Create FlashCard's topic successfull !");
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Error :" + ex.getMessage());
        }
    }

    @PutMapping("/updateTopic")
    public ResponseEntity<?> updateTopic(@RequestBody FlashCardTopicUpdateRequest request) {
        flashCardService.updateTopic(request);
        return ResponseEntity.ok().body("Update topic with id: " + request.getId() + " successfully !");
    }

    @PutMapping("/updateFlashCard")
    public ResponseEntity<?> updateFlashCard(@RequestBody FlashCardUpdateRequest request) {
        flashCardService.updateFlashCard(request);
        return ResponseEntity.ok().body("Update FlashCard with id: " + request.getId() + " successfully !");
    }

    @DeleteMapping("/deleteTopic/{id}")
    public ResponseEntity<?> deleteTopicById(@PathVariable int id) {
        flashCardService.deleteTopic(id);
        return ResponseEntity.ok().body("Topic with id = " + id + " has been deleted successfully !");
    }

    @DeleteMapping("/deleteFlashCard/{id}")
    public ResponseEntity<?> deleteFlashCardById(@PathVariable int id) {
        flashCardService.deleteFlashCard(id);
        return ResponseEntity.ok().body("FlashCard with id = " + id + " has been deleted successfully !");
    }

    @GetMapping("/getPublicTopics")
    public ResponseEntity<?> getPublicTopics() {
        return new ResponseEntity<List<FlashCardTopicPublicResponse>>(flashCardService.getAllsPublicFlashCardTopic(),
                HttpStatus.OK);
    }

}
