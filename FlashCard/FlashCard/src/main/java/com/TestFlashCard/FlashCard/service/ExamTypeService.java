package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.ExamType;
import com.TestFlashCard.FlashCard.exception.ResourceExistedException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExamType_Repository;
import com.TestFlashCard.FlashCard.request.ExamTypeCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamTypeUpdateRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamTypeService {
    @Autowired
    private final IExamType_Repository examType_Repository;

    @Transactional
    public List<ExamType> getAllExamTypes() {
        return examType_Repository.findByIsDeletedFalse();
    }

    @Transactional
    public ExamType getDetailById(int id) throws IOException {
        ExamType examType = examType_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the ExamType with id: " + id));
        if (examType.isDeleted() == true)
            throw new ResourceNotFoundException("Cannot find the ExamType with id: " + id);
        return examType;
    }

    @Transactional
    public ExamType getDetailByType(String type) throws IOException {
        ExamType examType = examType_Repository.findByType(type);
        if(examType == null)
            throw new ResourceNotFoundException("Cannot find the ExamType with type: " + type);

        if (examType.isDeleted() == true)
            throw new ResourceNotFoundException("Cannot find the ExamType with type: " + type);
        return examType;
    }

    @Transactional
    public void create(ExamTypeCreateRequest request) {
        if (examType_Repository.findByType(request.getType()) != null)
            throw new ResourceExistedException("ExamType name : " + request.getType() + " is existed!");
        ExamType examType = new ExamType();
        examType.setType(request.getType());
        examType_Repository.save(examType);
    }

    @Transactional
    public void update(ExamTypeUpdateRequest request, int id) throws IOException {
        ExamType examType = getDetailById(id);
        if (examType_Repository.findByType(request.getType()) != null)
            throw new ResourceExistedException("ExamType name : " + request.getType() + " is existed!");
        examType.setType(request.getType());
        examType_Repository.save(examType);
    }

    @Transactional
    public void softDelete(int id) throws IOException {
        ExamType examType = getDetailById(id);
        examType.setDeleted(true);
        examType_Repository.save(examType);
    }
}
