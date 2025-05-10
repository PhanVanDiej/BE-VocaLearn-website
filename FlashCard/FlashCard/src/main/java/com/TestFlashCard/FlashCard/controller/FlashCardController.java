package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.exception.InvalidImageException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.request.CardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicUpdateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardUpdateRequest;
import com.TestFlashCard.FlashCard.response.ImagePathResponse;
import com.TestFlashCard.FlashCard.response.ListFlashCardTopicResponse;
import com.TestFlashCard.FlashCard.response.ListFlashCardsResponse;
import com.TestFlashCard.FlashCard.service.CardService;
import com.TestFlashCard.FlashCard.service.DigitalOceanStorageService;
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

    @Autowired
    private final DigitalOceanStorageService storageService;

    @Autowired
    private final CardService cardService;

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

    @PostMapping("/createList")
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
    public ResponseEntity<?> createFlashCardTopic(@RequestBody @Valid FlashCardTopicCreateRequest request) {
        try {
            flashCardService.createFlashCardTopic(request);
            return ResponseEntity.ok().body("Create FlashCard's topic successfull !");
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Error :" + ex.getMessage());
        }
    }

    @PostMapping("/updateTopic")
    public ResponseEntity<?> updateTopic(@RequestBody FlashCardTopicUpdateRequest request) {
        flashCardService.updateTopic(request);
        return ResponseEntity.ok().body("Update topic with id: " + request.getId() + " successfully !");
    }

    @PostMapping("/updateFlashCard")
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

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCard(
            @RequestParam("image") MultipartFile image) throws IOException {

        String contentType = image.getContentType();
        String fileExtension = getFileExtension(image.getOriginalFilename());

        // Xử lý trường hợp application/octet-stream dựa vào extension
        if (contentType != null && contentType.equals("application/octet-stream")
                && isValidImageExtension(fileExtension)) {
            switch (fileExtension) {
                case "jpg":
                    contentType = "image/jpg";
                    break;
                case "jpeg":
                    contentType = "image/jpeg";
                    break;
                case "png":
                    contentType = "image/png";
                    break;
                case "gif":
                    contentType = "image/gif";
                    break;
                case "webp":
                    contentType = "image/webp";
                    break;
            }
        }

        // Kiểm tra content-type sau khi đã xử lý
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("Invalid image's content type: " + contentType);
        }

        String imageFileUrl = storageService.uploadImage(image.getOriginalFilename(), image.getInputStream(),
                image.getContentType());
                
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageFileUrl);

        return ResponseEntity.ok(response);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isValidImageExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        // Danh sách phần mở rộng hợp lệ
        List<String> validExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        return validExtensions.contains(extension);
    }

    @PostMapping("/createCard")
    public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateRequest request) {

        return ResponseEntity.ok().body(null);
    }

}
