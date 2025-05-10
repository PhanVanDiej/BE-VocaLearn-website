package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.Card;
import com.TestFlashCard.FlashCard.exception.ResourceExistedException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.ICard_Repository;
import com.TestFlashCard.FlashCard.request.CardCreateRequest;
import com.TestFlashCard.FlashCard.response.CardsResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {
    @Autowired
    private final ICard_Repository card_Repository;

    @Autowired
    private final DigitalOceanStorageService storageService;

    public List<CardsResponse> getFlashCardDetail(int flashCardID) {
        return card_Repository.findByFlashCardId(flashCardID).stream().map(this::convertToResponse).toList();
    }

    public CardsResponse getCardDetail(int cardID) {
        Card card = card_Repository.findById(cardID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find Card with id: " + cardID));
        return convertToResponse(card);
    }

    private CardsResponse convertToResponse(Card card) {
        return new CardsResponse(
                card.getId(),
                card.getTerminology(),
                card.getDefinition(),
                card.getImage(),
                card.getAudio(),
                card.getPronounce(),
                card.getLevel(),
                card.getPartOfSpeech(),
                card.getExample());
    }

    @Transactional
    public void createCard(CardCreateRequest cardDetail, String imageFileUrl) throws IOException {
        if (checkDuplicatedTerminology(cardDetail.getTerminology(), cardDetail.getFlashCardID(),
                cardDetail.getPartOfSpeech()))
            throw new ResourceExistedException("The terminology is existed!");

        try {
            Card card = new Card();
            card.setTerminology(cardDetail.getTerminology());
            card.setDefinition(cardDetail.getDefinition());
            card.setExample(cardDetail.getExample());
            card.setLevel(cardDetail.getLevel());
            card.setPronounce(cardDetail.getPronounce());
            card.setPartOfSpeech(cardDetail.getPartOfSpeech());
            card.setImage(imageFileUrl);

            card_Repository.save(card);
        } catch (Exception exception) {
            throw new IOException("Error when create a new card: ", exception);
        }

    }

    public boolean checkDuplicatedTerminology(String terminology, int flashCardID, String partOfSpeech) {
        Card card = card_Repository.findByTerminologyIgnoreCaseAndFlashCardIdAndPartOfSpeech(terminology, flashCardID,
                partOfSpeech);
        return card != null;
    }
}
