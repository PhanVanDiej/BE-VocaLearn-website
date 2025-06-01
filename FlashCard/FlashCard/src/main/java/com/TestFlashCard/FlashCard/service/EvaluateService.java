package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.JpaSpec.EvaluateSpecification;
import com.TestFlashCard.FlashCard.entity.Evaluate;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IEvaluate_Repository;
import com.TestFlashCard.FlashCard.request.EvaluateCreateRequest;
import com.TestFlashCard.FlashCard.response.EvaluateResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluateService {
    @Autowired
    private final IEvaluate_Repository evaluate_Repository;

    @Autowired
    private final MediaService mediaService;

    @Autowired
    private final DigitalOceanStorageService storageService;

    public void createEvaluate(EvaluateCreateRequest request, MultipartFile imagFile, User user) throws IOException{

        Evaluate evaluate = new Evaluate();
        evaluate.setContent(request.getContent());
        evaluate.setStar(request.getStar());
        evaluate.setUser(user);

        if (imagFile != null) {
            String imageUrl = mediaService.getImageUrl(imagFile);
            evaluate.setImage(imageUrl);
        } else {
            evaluate.setImage(null);
        }

        evaluate_Repository.save(evaluate);
    }

    public void deleteEvaluate(int evaluateID) {
        Evaluate evaluate = evaluate_Repository.findById(evaluateID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the evaluate with id: " + evaluateID));

        if (evaluate.getImage() != null)
            storageService.deleteImage(evaluate.getImage());

        evaluate_Repository.delete(evaluate);
    }

    public List<EvaluateResponse> getAllEvaluates() {
        return evaluate_Repository.findAll().stream().map(this::convertToEvaluateResponse).toList();
    }

    public List<EvaluateResponse> getEvaluatesByStar(int star) throws IOException {
        Specification<Evaluate> evaluateSpecification = Specification.where(EvaluateSpecification.hasStar(star));
        return evaluate_Repository.findAll(evaluateSpecification).stream().map(this::convertToEvaluateResponse).toList();
    }

    private EvaluateResponse convertToEvaluateResponse(Evaluate evaluate){
        return new EvaluateResponse(
            evaluate.getId(),
            evaluate.getContent(),
            evaluate.getStar(),
            evaluate.getImage(),
            evaluate.getCreateAt()
        );
    }
}
