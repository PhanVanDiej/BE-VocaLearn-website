package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.Enum.LearningStatus;
import com.TestFlashCard.FlashCard.entity.Card;
import com.TestFlashCard.FlashCard.entity.FlashCard;
import com.TestFlashCard.FlashCard.exception.ResourceExistedException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.exception.StorageException;
import com.TestFlashCard.FlashCard.repository.ICard_Repository;
import com.TestFlashCard.FlashCard.repository.IFlashCard_Repository;
import com.TestFlashCard.FlashCard.request.CardCreateRequest;
import com.TestFlashCard.FlashCard.request.CardUpdateRequest;
import com.TestFlashCard.FlashCard.response.CardsResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {
    @Autowired
    private final ICard_Repository card_Repository;

    // @Autowired
    // private final DigitalOceanStorageService storageService;

    @Autowired
    private final IFlashCard_Repository flashCard_Repository;

    // @Autowired
    // private final MediaService mediaService;

    @Autowired
    private MinIO_MediaService minIO_MediaService;

    private static final String TRANSLITERATION_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public List<CardsResponse> getFlashCardDetail(int flashCardID) {
        return card_Repository.findByFlashCardId(flashCardID).stream().map(this::convertToResponse).toList();
    }

    public CardsResponse getCardDetail(int cardID) {
        Card card = card_Repository.findById(cardID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find Card with id: " + cardID));
        return convertToResponse(card);
    }

    private CardsResponse convertToResponse(Card card) {

        String imageUrl = null;
        if(card.getImage()!=null && !card.getImage().isEmpty())
            imageUrl = minIO_MediaService.getPresignedURL(card.getImage(), Duration.ofMinutes(1));
        return new CardsResponse(
                card.getId(),
                card.getTerminology(),
                card.getDefinition(),
                imageUrl,
                card.getAudio(),
                card.getPronounce(),
                card.getLevel(),
                card.getIsRemember(),
                card.getPartOfSpeech(),
                card.getExample());
    }

    @Transactional
    public void createCard(CardCreateRequest cardDetail, MultipartFile image) throws IOException {
        if (checkDuplicatedTerminology(cardDetail.getTerminology(), cardDetail.getFlashCardID(),
                cardDetail.getPartOfSpeech()))
            throw new ResourceExistedException("The terminology is existed!");

        RestTemplate restTemplate = new RestTemplate();
        try {
            Card card = new Card();

            if (cardDetail.getPronounce() == null || cardDetail.getPronounce().isBlank()
                    || cardDetail.getPronounce().isEmpty()) {
                List<Map<String, Object>> response = restTemplate
                        .getForObject(TRANSLITERATION_API_URL + cardDetail.getTerminology(), List.class);
                if (response != null && !response.isEmpty()) {
                    Map<String, Object> firstEntry = response.get(0);
                    List<Map<String, Object>> phonetics = (List<Map<String, Object>>) firstEntry.get("phonetics");

                    if (phonetics != null && !phonetics.isEmpty()) {
                        String phoneticText = null;
                        String audioUrl = null;
                        for (Map<String, Object> ph : phonetics) {
                            // Lấy phiên âm đầu tiên (ưu tiên có text)
                            if (phoneticText == null && ph.get("text") != null && !((String)ph.get("text")).isBlank()) {
                                phoneticText = (String) ph.get("text");
                            }
                            // Lấy audio đầu tiên khác rỗng
                            if (audioUrl == null && ph.get("audio") != null && !((String)ph.get("audio")).isBlank()) {
                                audioUrl = (String) ph.get("audio");
                            }
                            // Nếu đã tìm được cả hai, có thể break luôn cho nhanh
                            if (phoneticText != null && audioUrl != null) break;
                        }
                        card.setPronounce(phoneticText != null ? phoneticText : "Không thể phiên âm");
                        card.setAudio(audioUrl); // Có thể là null nếu không tìm thấy audio
                    }
                } else
                    card.setPronounce("(Không thể phiên âm)");
            } else
                card.setPronounce(cardDetail.getPronounce());

            FlashCard flashCard = flashCard_Repository.findById(cardDetail.getFlashCardID()).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Cannot find the flash card with id: " + cardDetail.getFlashCardID()));
            
            // Upload Image if not null
            if(image!=null){
                String uniqueName = minIO_MediaService.uploadFile(image);
                card.setImage(uniqueName);
            }
            card.setTerminology(cardDetail.getTerminology());
            card.setDefinition(cardDetail.getDefinition());
            card.setExample(cardDetail.getExample());
            card.setLevel(cardDetail.getLevel());
            card.setIsRemember(0);
            card.setPartOfSpeech(cardDetail.getPartOfSpeech());
            card.setFlashCard(flashCard);

            card_Repository.save(card);
        } catch (Exception exception) {
            throw new IOException("Error when create a new card: "+ exception.getMessage());
        }

    }

    public boolean checkDuplicatedTerminology(String terminology, int flashCardID, String partOfSpeech) {
        Card card = card_Repository.findByTerminologyIgnoreCaseAndFlashCardIdAndPartOfSpeech(terminology, flashCardID,
                partOfSpeech);
        return card != null;
    }

    @Transactional
    public void updateCardDetail(CardUpdateRequest request, int cardID) {
        Card card = card_Repository.findById(cardID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Card with id : " + cardID));

        if (request.getDefinition() != null)
            card.setDefinition(request.getDefinition());
        if (request.getExample() != null)
            card.setExample(request.getExample());
        if (request.getPronounce() != null)
            card.setPronounce(request.getPronounce());
        if (request.getLevel() != null)
            card.setLevel(request.getLevel());
        if (request.getPartOfSpeech() != null)
            card.setPartOfSpeech(request.getPartOfSpeech());
        if (request.getTerminology() != null)
            card.setTerminology(request.getTerminology());
        if(request.getIsRemember()!=null)
            card.setIsRemember(request.getIsRemember());
        card_Repository.save(card);
    }

    @Transactional
    public void changeImage(int cardID, String imageFileName) throws IOException, StorageException {
        Card card = card_Repository.findById(cardID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Card with id : " + cardID));
        if (card.getImage() != null && !card.getImage().isEmpty())
            minIO_MediaService.deleteFile(card.getImage());
        card.setImage(imageFileName);
        card_Repository.save(card);
    }

    @Transactional
    public void deleteImage(int cardID) {
        Card card = card_Repository.findById(cardID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Card with id : " + cardID));
        if (card.getImage() != null && !card.getImage().isEmpty())
            minIO_MediaService.deleteFile(card.getImage());
        card.setImage(null);
        card_Repository.save(card);
    }

    @Transactional
    public void deleteCard(int cardID) {
        Card card = card_Repository.findById(cardID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Card with id : " + cardID));
        if (card.getImage() != null && !card.getImage().isEmpty())
            minIO_MediaService.deleteFile(card.getImage());
        card_Repository.deleteById(cardID);
    }

    @Transactional
    public void resetListCard(int flashcardID){
        List<Card> cards= card_Repository.findByFlashCardId(flashcardID);
        for (Card card : cards) {
            card.setIsRemember(0);
            card_Repository.save(card);
        }
        FlashCard flashCard = flashCard_Repository.findById(flashcardID).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find the Flashcard with id: " + flashcardID)
        );
        flashCard.setLearningStatus(LearningStatus.NEW);
        flashCard.setReviewDate(null);
        flashCard_Repository.save(flashCard);
    }
}
