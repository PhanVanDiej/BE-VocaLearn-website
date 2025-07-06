package com.TestFlashCard.FlashCard.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.repository.IToeicQuestion_Repository;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToeicQuestionService {
    @Autowired
    private final IToeicQuestion_Repository toeicQuestion_Repository;
    @Autowired
    private final IExam_Repository exam_Repository;

    public ToeicQuestionResponse getById(int questionID) {
        ToeicQuestion question = toeicQuestion_Repository.findById(questionID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the toeic question with id: " + questionID));
        return convertQuestionToResponse(question);
    }

    public List<ToeicQuestionResponse> getByExamId(int examID) {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));

        List<ToeicQuestion> questions = toeicQuestion_Repository.findByExamId(examID);
        return questions.stream().map(this::convertQuestionToResponse).toList();
    }

    public ToeicQuestionResponse convertQuestionToResponse(ToeicQuestion question) {
        List<ToeicQuestionResponse.OptionResponse> options = question.getOptions().stream()
                .map(opt -> new ToeicQuestionResponse.OptionResponse(opt.getMark(), opt.getDetail()))
                .collect(Collectors.toList());

        return new ToeicQuestionResponse(
                question.getId(),
                question.getIndexNumber(),
                question.getPart(),
                question.getDetail(),
                question.getResult(),
                question.getImage(),
                question.getAudio(),
                question.getConversation(),
                options);
    }

}
