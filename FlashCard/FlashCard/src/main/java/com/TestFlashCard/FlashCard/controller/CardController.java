package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.util.List;

import com.TestFlashCard.FlashCard.response.FlashCardNomalResponse;
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
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.CardsResponse;
import com.TestFlashCard.FlashCard.service.CardService;
import com.TestFlashCard.FlashCard.service.MinIO_MediaService;
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
    private final ObjectMapper objectMapper;
    @Autowired
    private final MinIO_MediaService minIO_MediaService;

    @GetMapping("/detail/{cardID}")
    public ResponseEntity<?> getCardDetail(@PathVariable Integer cardID) throws IOException {
        if (cardID == null)
            throw new IOException("Missing card's ID for this request");
        CardsResponse response = cardService.getCardDetail(cardID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping("/getByFlashCard/{flashCardID}")
    public ResponseEntity<?> getByFlashCard(@PathVariable Integer flashCardID) {
        FlashCardNomalResponse responses = cardService.getFlashCardDetail(flashCardID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @PostMapping(value = "/createCard", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCard(@RequestParam(required = false) MultipartFile image,
            @RequestParam("data") String dataJson) throws IOException {

        // Transform string to json object
        CardCreateRequest request = objectMapper.readValue(dataJson, CardCreateRequest.class);

        cardService.createCard(request, image);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Created a new Card: " + request.getTerminology()));
    }

    @PutMapping("/update/detail/{cardID}")
    public ResponseEntity<?> updateCard(@PathVariable("cardID") Integer id, @RequestBody CardUpdateRequest request) {
        cardService.updateCardDetail(request, id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Card detail has been updated!"));
    }

    @PutMapping(value = "/update/image/{cardID}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeImage(@PathVariable Integer cardID, @RequestParam MultipartFile image)
            throws IOException {
        if (image != null) {
            String uniqueName = minIO_MediaService.uploadFile(image);
            cardService.changeImage(cardID, uniqueName);
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Card image has been updated!"));
    }

    @DeleteMapping("/delete/image/{cardID}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer cardID) {
        CardsResponse card = cardService.getCardDetail(cardID);
        cardService.deleteImage(cardID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Deleted Card: " + card.definition()));
    }

    @DeleteMapping("/delete/card/{cardID}")
    public ResponseEntity<?> deleteCard(@PathVariable Integer cardID) {
        CardsResponse card = cardService.getCardDetail(cardID);
        cardService.deleteCard(cardID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Deleted Card: " + card.terminology()));
    }

    // @PostMapping("/createListCard")
    // public ResponseEntity<?> createListCards(@RequestParam List<MultipartFile> files) throws Exception {
    //     List<String> imageUrls = new ArrayList<>();
    //     return ResponseEntity.ok(null);
    // }

    @PutMapping("resetAll/{flashcardId}")
    public ResponseEntity<?> resetAllCards(@PathVariable Integer flashcardId) {
        cardService.resetListCard(flashcardId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("All Cards in this FlashCard has been reset."));
    }
}
