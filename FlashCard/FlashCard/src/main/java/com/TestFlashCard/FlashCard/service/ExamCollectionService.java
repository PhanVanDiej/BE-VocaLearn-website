package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.ExamCollection;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExamCollection_Repository;
import com.TestFlashCard.FlashCard.request.ExamCollectionCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamCollectionUpdateRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamCollectionService {
    @Autowired
    private final IExamCollection_Repository examCollection_Repository;

    @Transactional
    public List<ExamCollection> getAllExamCollection() {
        return examCollection_Repository.findByIsDeletedFalse();
    }

    @Transactional
    public ExamCollection getDetailById(int id) throws IOException {
        ExamCollection examCollection = examCollection_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the ExamCollection with id: " + id));
        if (examCollection.isDeleted() == true)
            throw new ResourceNotFoundException("Cannot find the ExamCollection with id: " + id);
        return examCollection;
    }
    @Transactional
    public ExamCollection getDetailByCollection(String collecion) throws IOException{
        ExamCollection examCollection=examCollection_Repository.findByCollection(collecion);

        if(examCollection == null)
            throw new ResourceNotFoundException("Cannot find the ExamCollection with collection: " + collecion);

        if (examCollection.isDeleted()== true)
            throw new ResourceNotFoundException("Cannot find the ExamCollection with collection: " + collecion);
        return examCollection;
    }

    @Transactional
    public void create(ExamCollectionCreateRequest request) {
        ExamCollection examCollection = new ExamCollection();
        examCollection.setCollection(request.getCollection());
        examCollection_Repository.save(examCollection);
    }

    @Transactional
    public void update(ExamCollectionUpdateRequest request, int id) throws IOException {
        ExamCollection examCollection = getDetailById(id);

        examCollection.setCollection(request.getCollection());
        examCollection_Repository.save(examCollection);
    }

    @Transactional
    public void softDelete(int id) throws IOException {
        ExamCollection examCollection = getDetailById(id);
        examCollection.setDeleted(true);
        examCollection_Repository.save(examCollection);
    }
}
