package com.TestFlashCard.FlashCard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.request.FlashCardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicCreateRequest;
import com.TestFlashCard.FlashCard.service.FlashCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flashcard")
@RequiredArgsConstructor
public class FlashCardController {
    @Autowired
    private final FlashCardService flashCardService;

    @PostMapping("/createList")
    public ResponseEntity<?> createFlashCard(@RequestBody @Valid FlashCardCreateRequest request){
        try{
            flashCardService.createFlashCard(request);
            return ResponseEntity.ok().body("Create Flashcard successfull!");
        }catch (ResourceNotFoundException ex){
            return new ResponseEntity<>("Error: " + ex.getMessage(),HttpStatus.NOT_FOUND);
        }catch(Exception ex){
            return ResponseEntity.internalServerError().body("Error: "+ ex.getMessage());
        }
    }

    @PostMapping("/createTopic")
    public ResponseEntity<?> createFlashCardTopic(@RequestBody @Valid FlashCardTopicCreateRequest request) {
        try{
            flashCardService.createFlashCardTopic(request);
            return ResponseEntity.ok().body("Create FlashCard's topic successfull !");
        }catch(ResourceNotFoundException ex){
            return new ResponseEntity<>("Error: " + ex.getMessage(),HttpStatus.NOT_FOUND);
        }catch(Exception ex){
            return ResponseEntity.internalServerError().body("Error :"+ ex.getMessage());
        }
    }
    
}
