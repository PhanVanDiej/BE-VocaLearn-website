package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.request.CardCreateRequest;
import com.TestFlashCard.FlashCard.request.CardUpdateRequest;
import com.TestFlashCard.FlashCard.response.CardsResponse;
import com.TestFlashCard.FlashCard.service.CardService;
import com.TestFlashCard.FlashCard.service.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    @Autowired
    private final CardService cardService;

    @Autowired
    private final MediaService mediaService;

    @GetMapping("/detail/{cardID}")
    public ResponseEntity<?> getCardDetail(@PathVariable Integer cardID) throws IOException {
        if (cardID == null)
            throw new IOException("Missing card's ID for this request");
        CardsResponse response = cardService.getCardDetail(cardID);
        return new ResponseEntity<CardsResponse>(response, HttpStatus.OK);
    }

    @GetMapping("/getByFlashCard/{flashCardID}")
    public ResponseEntity<?> getByFlashCard(@PathVariable Integer flashCardID) {
        List<CardsResponse> responses = cardService.getFlashCardDetail(flashCardID);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PostMapping(value = "/createCard", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCard(@RequestParam(required = false) MultipartFile image,
            @RequestParam("data") String dataJson) throws IOException {

        // Transform string to json object
        ObjectMapper object = new ObjectMapper();
        CardCreateRequest request = object.readValue(dataJson, CardCreateRequest.class);

        cardService.createCard(request, image);
        return ResponseEntity.ok("Create new Card successfully!");
    }

    @PutMapping("/update/detail/{cardID}")
    public ResponseEntity<?> updateCard(@PathVariable("cardID") Integer id, @RequestBody CardUpdateRequest request) {
        cardService.updateCardDetail(request, id);
        return ResponseEntity.ok("Updating card with id : " + id + " successfully !");
    }

    @PutMapping(value = "/update/image/{cardID}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> changeImage(@PathVariable Integer cardID, @RequestParam MultipartFile image)
            throws IOException {
        String imageUrl = mediaService.getImageUrl(image);
        System.out.println(imageUrl);
        cardService.changeImage(cardID, imageUrl);
        return ResponseEntity.ok("Changing image successfully!");
    }

    @DeleteMapping("/delete/image/{cardID}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer cardID) {
        cardService.deleteImage(cardID);
        return ResponseEntity.ok("Deleting image successfully!");
    }

    @DeleteMapping("/delete/card/{cardID}")
    public ResponseEntity<?> deleteCard(@PathVariable Integer cardID){
        cardService.deleteCard(cardID);
        return ResponseEntity.ok("Deleting card with id : " + cardID + " successfully !");
    }

    @PostMapping("/createListCard")
    public ResponseEntity<?> createListCards(@RequestParam List<MultipartFile> files) throws Exception {
        List<String> imageUrls = new ArrayList<>();
        return ResponseEntity.ok(null);
    }
}
