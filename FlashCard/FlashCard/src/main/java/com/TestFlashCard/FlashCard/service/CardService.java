package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import com.TestFlashCard.FlashCard.response.CardChoiceRespoinse;
import com.TestFlashCard.FlashCard.response.FlashCardNomalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
@Slf4j
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

    public FlashCardNomalResponse getFlashCardDetail(int flashCardID) {
        List<CardsResponse> listCardResponse = card_Repository.findByFlashCardId(flashCardID)
                .stream()
                .map(this::convertToResponse)
                .toList();

        List<CardChoiceRespoinse> listCardChoice = new ArrayList<>();

        for (CardsResponse cardResponse : listCardResponse) {
            // Lấy đáp án đúng
            String correctAnswer = cardResponse.definition();

            // Lấy 3 đáp án sai random
            List<String> wrongAnswers = listCardResponse.stream()
                    .map(CardsResponse::definition)
                    .filter(term -> !term.equals(correctAnswer)) // loại bỏ đáp án đúng
                    .collect(Collectors.toList());

            Collections.shuffle(wrongAnswers); // trộn ngẫu nhiên
            List<String> options = new ArrayList<>();
            options.add(correctAnswer);
            options.addAll(wrongAnswers.stream().limit(3).toList());


            // Nếu không đủ 4 đáp án → thêm placeholder cho đủ
            while (options.size() < 4) {
                options.add("N/A"); // Hoặc thêm một giá trị mặc định khác
            }
            // Trộn đáp án cuối cùng (để không phải lúc nào đáp án đúng cũng ở đầu)
            Collections.shuffle(options);

            // Tạo đối tượng CardChoiceRespoinse
            CardChoiceRespoinse card = new CardChoiceRespoinse( cardResponse.terminology(), cardResponse.hint(),
                    options.get(0),
                    options.get(1),
                    options.get(2),
                    options.get(3),
                    correctAnswer      );
            listCardChoice.add(card);
        }

        return new FlashCardNomalResponse(listCardResponse, listCardChoice);
    }


    public CardsResponse getCardDetail(int cardID) {
        Card card = card_Repository.findById(cardID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find Card with id: " + cardID));
        return convertToResponse(card);
    }

    private CardsResponse convertToResponse(Card card) {
        String imageUrl = null;
        if (card.getImage() != null && !card.getImage().isEmpty()) {
            imageUrl = minIO_MediaService.getPresignedURL(card.getImage(), Duration.ofMinutes(1));
        }

        List<String> examples = card.getExample() != null && !card.getExample().isBlank()
                ? Arrays.stream(card.getExample().split("\\+")).toList()
                : Collections.emptyList();
        List<String> hints = card.getHint() != null && !card.getHint().isBlank()
                ? Arrays.stream(card.getHint().split("\\+")).toList()
                : Collections.emptyList();

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
                examples,
                hints

        );
    }


    @Transactional
    public void createCard(CardCreateRequest cardDetail, MultipartFile image) throws IOException {
        if (checkDuplicatedTerminology(cardDetail.getTerminology(), cardDetail.getFlashCardID(),
                cardDetail.getPartOfSpeech()))
            throw new ResourceExistedException("The terminology is existed!");

        RestTemplate restTemplate = new RestTemplate();

        try {
            Card card = new Card();

            // Nếu client không cung cấp pronounce thì gọi API
            if (cardDetail.getPronounce() == null || cardDetail.getPronounce().isBlank()) {
                String apiUrl = TRANSLITERATION_API_URL + cardDetail.getTerminology();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("User-Agent", "Mozilla/5.0");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<List> responseEntity = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.GET,
                        entity,
                        List.class
                );

                List<Map<String, Object>> response = responseEntity.getBody();

                if (response != null && !response.isEmpty()) {
                    Map<String, String> hintMap = new LinkedHashMap<>();     // partOfSpeech → hint
                    Map<String, String> exampleMap = new LinkedHashMap<>();  // partOfSpeech → example

                    // Duyệt toàn bộ response
                    for (Map<String, Object> entry : response) {
                        List<Map<String, Object>> meanings = (List<Map<String, Object>>) entry.get("meanings");
                        if (meanings == null) continue;

                        for (Map<String, Object> meaning : meanings) {
                            Object posObj = meaning.get("partOfSpeech");
                            if (!(posObj instanceof String) || ((String) posObj).isBlank()) continue;

                            String partOfSpeech = (String) posObj;

                            // Nếu đã có definition cho partOfSpeech này thì bỏ qua
                            if (hintMap.containsKey(partOfSpeech)) continue;

                            List<Map<String, Object>> definitions = (List<Map<String, Object>>) meaning.get("definitions");
                            if (definitions != null && !definitions.isEmpty()) {
                                for (Map<String, Object> def : definitions) {
                                    String defStr = def.get("definition") instanceof String ? (String) def.get("definition") : null;
                                    String exStr = def.get("example") instanceof String ? (String) def.get("example") : null;

                                    // Chỉ lấy khi cả definition và example đều tồn tại và không trống
                                    if (defStr != null && !defStr.isBlank() && exStr != null && !exStr.isBlank()) {
                                        hintMap.put(partOfSpeech, defStr);
                                        exampleMap.put(partOfSpeech, exStr);
                                        break; // Dừng ngay khi tìm được object thỏa điều kiện
                                    }
                                }
                            }
                        }
                    }

                    // Gộp dữ liệu thành chuỗi
                    if (!hintMap.isEmpty()) {
                        card.setHint(String.join("+", hintMap.values()));
                        card.setPartOfSpeech(String.join(", ", hintMap.keySet()));
                    }
                    if (!exampleMap.isEmpty()) {
                        card.setExample(String.join("+", exampleMap.values()));
                    }
                } else {
                    card.setPronounce("(Không thể phiên âm)");
                }
            } else {
                card.setPronounce(cardDetail.getPronounce());
            }

            // Lấy flashcard, upload ảnh nếu có, và set các trường còn lại
            FlashCard flashCard = flashCard_Repository.findById(cardDetail.getFlashCardID()).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Cannot find the flash card with id: " + cardDetail.getFlashCardID()));

            if (image != null) {
                String uniqueName = minIO_MediaService.uploadFile(image);
                card.setImage(uniqueName);
            }

            card.setTerminology(cardDetail.getTerminology());
            card.setDefinition(cardDetail.getDefinition());

            if (cardDetail.getExample() != null && !cardDetail.getExample().isBlank()) {
                card.setExample(cardDetail.getExample());
            }
            if (cardDetail.getPartOfSpeech() != null && !cardDetail.getPartOfSpeech().isBlank()) {
                card.setPartOfSpeech(cardDetail.getPartOfSpeech());
            }

            card.setLevel(cardDetail.getLevel());
            card.setIsRemember(0);
            card.setFlashCard(flashCard);

            card_Repository.save(card);

            log.info("Card created successfully: {}", card.getTerminology());

        } catch (Exception exception) {
            log.error("Error occurred while creating flash card", exception);
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
