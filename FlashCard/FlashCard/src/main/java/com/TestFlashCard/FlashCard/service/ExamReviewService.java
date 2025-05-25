package com.TestFlashCard.FlashCard.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ExamReview;
import com.TestFlashCard.FlashCard.entity.QuestionReview;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExamReview_Repository;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.repository.IToeicQuestion_Repository;
import com.TestFlashCard.FlashCard.request.ExamSubmitRequest;
import com.TestFlashCard.FlashCard.request.ToeicQuestionRecord;
import com.TestFlashCard.FlashCard.response.ExamReviewResponse;
import com.TestFlashCard.FlashCard.response.QuestionReviewResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamReviewService {
    @Autowired
    private final IExamReview_Repository examReview_Repository;
    @Autowired
    private final IExam_Repository exam_Repository;
    @Autowired
    private final IToeicQuestion_Repository toeicQuestion_Repository;

    @Transactional
    public ExamReviewResponse submitExam(ExamSubmitRequest request, User user) {
        // Lấy Exam
        Exam exam = exam_Repository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        int correctCount = 0;
        List<QuestionReview> questionReviews = new ArrayList<>();

        for (ToeicQuestionRecord record : request.getAnswers()) {
            ToeicQuestion question = toeicQuestion_Repository.findById(record.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + record.getQuestionId()));

            // So sánh đáp án
            boolean isCorrect = question.getResult().equalsIgnoreCase(String.valueOf(record.getAnswer()));
            if (isCorrect)
                correctCount++;

            QuestionReview qr = new QuestionReview();
            qr.setToeicQuestion(question);
            qr.setUserAnswer(String.valueOf(record.getAnswer()));
            questionReviews.add(qr);
        }

        // Tạo ExamReview
        ExamReview examReview = new ExamReview();
        examReview.setExam(exam);
        examReview.setUser(user);
        examReview.setDuration(request.getDuration());
        examReview.setResult(correctCount);
        examReview.setQuestionReviews(questionReviews);

        // Gán liên kết ngược để JPA cascade đúng
        for (QuestionReview qr : questionReviews) {
            qr.setExamReview(examReview);
        }

        examReview_Repository.save(examReview); // cascade lưu cả list
        ExamReviewResponse response = new ExamReviewResponse();
        response.setExamId(exam.getId());
        response.setUserId(user.getId());
        response.setDuration(examReview.getDuration());
        response.setCorrectAnswers(examReview.getResult());
        response.setTotalQuestions(questionReviews.size());
        response.setCreatedAt(examReview.getCreateAt());

        List<QuestionReviewResponse> questionReviewResponses = questionReviews.stream().map(qr -> {
            QuestionReviewResponse qrr = new QuestionReviewResponse();
            qrr.setQuestionId(qr.getToeicQuestion().getId());
            qrr.setUserAnswer(qr.getUserAnswer());
            qrr.setCorrectAnswer(qr.getToeicQuestion().getResult());
            qrr.setCorrect(qr.getUserAnswer() != null &&
                    qr.getUserAnswer().equalsIgnoreCase(qr.getToeicQuestion().getResult()));
            return qrr;
        }).toList();

        response.setQuestionReviews(questionReviewResponses);
        return response;
    }

}
