package com.TestFlashCard.FlashCard.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    @Autowired
    private final AttempLogService attempLogService;

    @Transactional
    public ExamReviewResponse submitExam(ExamSubmitRequest request, User user) {
        // Lấy Exam
        if (request.getAnswers() == null) {
            request.setAnswers(new ArrayList<>());
        }

        Exam exam = exam_Repository.findById(request.getExamID())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        // Tăng số lần thi
        attempLogService.createAttemp(exam.getId(), user.getId());

        // Khởi tạo ExamReview
        ExamReview examReview = new ExamReview();
        examReview.setExam(exam);
        examReview.setUser(user);
        examReview.setDuration(request.getDuration());

        int correctCount = 0;
        int incorrectCount = 0;
        List<QuestionReview> questionReviews = new ArrayList<>();

        List<ToeicQuestion> allQuestions = toeicQuestion_Repository.findAllByExam(exam);
        Map<Integer, Character> answerMap = request.getAnswers() != null
                ? request.getAnswers().stream()
                        .collect(Collectors.toMap(ToeicQuestionRecord::getQuestionId, ToeicQuestionRecord::getAnswer))
                : new HashMap<>();

        for (ToeicQuestion question : allQuestions) {
            QuestionReview qr = new QuestionReview();

            Character userAnswer = answerMap.get(question.getId());
            boolean isCorrect = userAnswer != null
                    && question.getResult() != null
                    && question.getResult().equalsIgnoreCase(String.valueOf(userAnswer));

            if (isCorrect)
                correctCount++;
            else if (userAnswer != null)
                incorrectCount++;

            qr.setToeicQuestion(question);
            qr.setUserAnswer(userAnswer != null ? String.valueOf(userAnswer) : null);
            qr.setExamReview(examReview);

            questionReviews.add(qr);
        }
        // Gán danh sách câu hỏi vào examReview
        examReview.setQuestionReviews(questionReviews);
        examReview.setIncorrect(incorrectCount);
        examReview.setResult(correctCount);

        // Lưu vào DB (cascade sẽ lưu luôn QuestionReview)
        examReview_Repository.save(examReview);
        exam_Repository.save(exam);

        // Tạo response trả về
        ExamReviewResponse response = new ExamReviewResponse();
        response.setReviewId(examReview.getId());
        response.setExamID(exam.getId());
        response.setUserID(user.getId());
        response.setExamTitle(exam.getTitle());
        response.setExamCollection(exam.getCollection().getCollection());
        response.setUserName(user.getFullName());
        response.setDuration(examReview.getDuration());
        response.setCorrectAnswers(examReview.getResult());
        response.setIncorrectAnswers(examReview.getIncorrect());
        response.setNullAnswers(questionReviews.size() - examReview.getIncorrect() - examReview.getResult());
        response.setTotalQuestions(questionReviews.size());
        response.setCreatedAt(examReview.getCreateAt());
        response.setSection(getPartSummaryFromReview(questionReviews));
        // Map từng câu hỏi thành response
        List<QuestionReviewResponse> questionReviewResponses = questionReviews.stream().map(qr -> {
            QuestionReviewResponse qrr = new QuestionReviewResponse();
            qrr.setQuestionId(qr.getToeicQuestion().getId());
            qrr.setUserAnswer(qr.getUserAnswer());
            qrr.setCorrectAnswer(qr.getToeicQuestion().getResult());
            qrr.setCorrect(qrr.getUserAnswer() != null &&
                    qrr.getUserAnswer().equalsIgnoreCase(qrr.getCorrectAnswer()));
            return qrr;
        }).toList();

        response.setQuestionReviews(questionReviewResponses);
        return response;
    }

    @Transactional
    public List<ExamReviewResponse> getAllExamResultByUser(User user, Exam exam) {
        List<ExamReview> resultList = examReview_Repository.findByUserAndExam(user, exam);
        return resultList.stream().map(review -> convertToRespone(review, review.getExam(), review.getUser()))
                .collect(Collectors.toList());
    }

    public ExamReviewResponse getById(int id) {
        ExamReview review = examReview_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam review with id: " + id));
        return convertToRespone(review, review.getExam(), review.getUser());
    }

    public ExamReviewResponse convertToRespone(ExamReview review, Exam exam, User user) {
        ExamReviewResponse response = new ExamReviewResponse();
        response.setReviewId(review.getId());
        response.setExamID(review.getExam().getId());
        response.setUserID(review.getUser().getId());
        response.setExamCollection(review.getExam().getCollection().getCollection());
        response.setExamTitle(review.getExam().getTitle());
        response.setUserName(review.getUser().getFullName());
        response.setDuration(review.getDuration());
        response.setCorrectAnswers(review.getResult());
        response.setIncorrectAnswers(review.getIncorrect());
        response.setNullAnswers(review.getQuestionReviews().size() - review.getResult() - review.getIncorrect());
        response.setTotalQuestions(review.getQuestionReviews().size());
        response.setCreatedAt(review.getCreateAt());
        response.setSection(getPartSummaryFromReview(review.getQuestionReviews()));
        response.setQuestionReviews(convertToQuestionsResponse(review.getQuestionReviews()));
        return response;

    }

    public List<QuestionReviewResponse> convertToQuestionsResponse(List<QuestionReview> questionReviews) {
        return questionReviews.stream().map(qr -> {
            QuestionReviewResponse qrr = new QuestionReviewResponse();
            ToeicQuestion question = qr.getToeicQuestion();

            qrr.setQuestionId(question.getId());
            qrr.setIndexNumber(question.getIndexNumber());
            qrr.setDetail(question.getDetail());
            qrr.setImage(question.getImage());
            qrr.setAudio(question.getAudio());
            qrr.setConversation(question.getConversation());
            qrr.setUserAnswer(qr.getUserAnswer());
            qrr.setCorrectAnswer(question.getResult());
            qrr.setCorrect(
                    qr.getUserAnswer() != null && qr.getUserAnswer().equalsIgnoreCase(question.getResult()));

            // Map options A–D
            List<QuestionReviewResponse.OptionResponse> optionResponses = question.getOptions().stream().map(opt -> {
                QuestionReviewResponse.OptionResponse optRes = new QuestionReviewResponse.OptionResponse();
                optRes.setMark(opt.getMark());
                optRes.setDetail(opt.getDetail());
                return optRes;
            }).collect(Collectors.toList());

            qrr.setOptions(optionResponses);

            return qrr;
        }).collect(Collectors.toList());
    }

    public String getPartSummaryFromReview(List<QuestionReview> reviewQuestions) {
        // Lấy các part duy nhất, convert về số nguyên, loại null/rỗng
        Set<Integer> partSet = reviewQuestions.stream()
                .map(rq -> rq.getToeicQuestion() != null ? rq.getToeicQuestion().getPart() : null)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(TreeSet::new));

        // Kiểm tra đủ 1-7
        boolean isFull = IntStream.rangeClosed(1, 7).allMatch(partSet::contains);
        if (isFull)
            return "Toàn bộ";
        if (partSet.isEmpty())
            return "N/A";
        // Trả về chuỗi "Part 1, Part 2, ..."
        return partSet.stream()
                .map(i -> "Part " + i)
                .collect(Collectors.joining(", "));
    }

}
