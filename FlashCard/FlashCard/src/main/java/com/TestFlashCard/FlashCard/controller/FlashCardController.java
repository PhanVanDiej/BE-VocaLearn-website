package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
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
import com.TestFlashCard.FlashCard.response.ApiResponse;
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
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(topics));
    }

    @GetMapping("/getFlashCardsByTopic/{topicID}")
    public ResponseEntity<?> getAllFlashCard(@PathVariable Integer topicID) {
        List<ListFlashCardsResponse> flashCards = flashCardService.getFlashCardsByTopic(topicID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCards));
    }

    @GetMapping("/topic/{id}")
    public ResponseEntity<?> getFlashCardTopicById(@PathVariable Integer id) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCardService.getFlashCardTopicById(id)));
    }

    @GetMapping("/getTopicPopular")
    public ResponseEntity<?> getAllTopicPopular() throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCardService.getFlashCardTopicByVisitCount()));
    }

    @PostMapping("/createFlashCard")
    public ResponseEntity<?> createFlashCard(@RequestBody @Valid FlashCardCreateRequest request) throws IOException{
        try {
            flashCardService.createFlashCard(request);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }

    @PostMapping("/createTopic")
    public ResponseEntity<?> createFlashCardTopic(@RequestBody @Valid FlashCardTopicCreateRequest request,
            Principal principal) throws IOException {
        flashCardService.createFlashCardTopic(request, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    @PutMapping("/updateTopic")
    public ResponseEntity<?> updateTopic(@RequestBody FlashCardTopicUpdateRequest request, Principal principal) {
        flashCardService.updateTopic(request, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    @PutMapping("/updateFlashCard")
    public ResponseEntity<?> updateFlashCard(@RequestBody FlashCardUpdateRequest request) {
        flashCardService.updateFlashCard(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    @DeleteMapping("/deleteTopic/{id}")
    public ResponseEntity<?> deleteTopicById(@PathVariable int id) {
        flashCardService.deleteTopic(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    @DeleteMapping("/deleteFlashCard/{id}")
    public ResponseEntity<?> deleteFlashCardById(@PathVariable int id) {
        flashCardService.deleteFlashCard(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getFlashCardById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCardService.getFlashCardById(id)));
    }

    @PostMapping("/savePublishTopic/{topicID}")
    public ResponseEntity<?> savePublishTopic(@PathVariable Integer topicID,Principal principal)throws IOException {
        flashCardService.savePublishTopic(topicID, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }
    
    @PutMapping("/raiseVisitCount/{id}")
    public ResponseEntity<?> raiseVisitCount(@PathVariable Integer id) {
        flashCardService.updateVisitCountTopic(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

}
