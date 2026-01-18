package com.TestFlashCard.FlashCard.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.TestFlashCard.FlashCard.entity.GroupQuestion;
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
    private MinIO_MediaService minIO_MediaService;
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

        Set<String> selected = Arrays.stream(request.getSelectedPart().split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // Query lấy tất cả câu hỏi theo Part đã chọn
        // Bao gồm cả câu trong exam.questions và GroupQuestion.questions
        // vì tất cả đều có exam_id được set
        List<ToeicQuestion> allQuestions = toeicQuestion_Repository.findAllByExamAndPartIn(exam, selected);

        Map<Integer, Character> answerMap = new java.util.HashMap<>();
        for (ToeicQuestionRecord a : request.getAnswers()) {
            if (a == null)
                continue;
            Integer qid = a.getQuestionId();
            Character ans = a.getAnswer();
            if (qid == null || ans == null)
                continue; // bài trống sẽ đi qua đây
            answerMap.putIfAbsent(qid, ans);
        }

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
        examReview.setSelectedPart(request.getSelectedPart());
        // Lưu vào DB (cascade sẽ lưu luôn QuestionReview)
        examReview_Repository.save(examReview);
        exam_Repository.save(exam);

        // Tạo response trả về
        ExamReviewResponse response = new ExamReviewResponse();
        response.setReviewId(examReview.getId());
        response.setExamID(exam.getId());
        response.setUserID(user.getId());
        response.setExamTitle(exam.getTitle());
        
        // FIX: Null safety cho collection
        response.setExamCollection(
            exam.getCollection() != null 
                ? exam.getCollection().getCollection() 
                : null
        );
        
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
        return resultList.stream().map(review -> convertToResponse(review, review.getExam(), review.getUser()))
                .collect(Collectors.toList());
    }

    public ExamReviewResponse getById(int id) {
        ExamReview review = examReview_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam review with id: " + id));
        return convertToResponse(review, review.getExam(), review.getUser());
    }

    /**
     * Convert ExamReview entity to response DTO
     * FIX: Added null safety for collection and improved response
     */
    public ExamReviewResponse convertToResponse(ExamReview review, Exam exam, User user) {
        ExamReviewResponse response = new ExamReviewResponse();
        response.setReviewId(review.getId());
        response.setExamID(review.getExam().getId());
        response.setUserID(review.getUser().getId());
        
        // FIX: Null safety cho collection
        response.setExamCollection(
            review.getExam().getCollection() != null 
                ? review.getExam().getCollection().getCollection() 
                : null
        );
        
        response.setExamTitle(review.getExam().getTitle());
        response.setUserName(review.getUser().getFullName());
        response.setDuration(review.getDuration());
        response.setCorrectAnswers(review.getResult());
        response.setIncorrectAnswers(review.getIncorrect());
        response.setNullAnswers(review.getQuestionReviews().size() - review.getResult() - review.getIncorrect());
        response.setTotalQuestions(review.getQuestionReviews().size());
        response.setSelectedPart(review.getSelectedPart());
        response.setCreatedAt(review.getCreateAt());
        response.setSection(getPartSummaryFromReview(review.getQuestionReviews()));
        response.setQuestionReviews(convertToQuestionsResponse(review.getQuestionReviews()));
        return response;
    }

    /**
     * Convert list of QuestionReview to response DTOs
     * Includes group information for questions that belong to a group
     */
    public List<QuestionReviewResponse> convertToQuestionsResponse(List<QuestionReview> questionReviews) {
        return questionReviews.stream().map(qr -> {

            QuestionReviewResponse qrr = new QuestionReviewResponse();
            ToeicQuestion question = qr.getToeicQuestion();

            // --- IMAGE (nhiều ảnh) ---
            List<String> imageUrls = null;
            if (question.getImages() != null && !question.getImages().isEmpty()) {
                imageUrls = question.getImages().stream()
                        .map(img -> minIO_MediaService.getPresignedURL(
                                img.getUrl(),
                                Duration.ofDays(1)))
                        .collect(Collectors.toList());
            }

            // --- AUDIO (mặc định 1 file) ---
            String audio = null;
            if (question.getAudio() != null && !question.getAudio().isEmpty()) {
                audio = minIO_MediaService.getPresignedURL(
                        question.getAudio(),
                        Duration.ofDays(1));
            }

            // --- SET BASIC FIELDS ---
            qrr.setQuestionId(question.getId());
            qrr.setIndexNumber(question.getIndexNumber());
            qrr.setPart(question.getPart());
            qrr.setDetail(question.getDetail());
            qrr.setImages(imageUrls);
            qrr.setAudio(audio);
            qrr.setConversation(question.getConversation());
            qrr.setClarify(question.getClarify());
            qrr.setUserAnswer(qr.getUserAnswer());
            qrr.setCorrectAnswer(question.getResult());
            qrr.setCorrect(
                    qr.getUserAnswer() != null &&
                            qr.getUserAnswer().equalsIgnoreCase(question.getResult()));

            // --- GROUP INFORMATION (NEW) ---
            // Nếu câu hỏi thuộc một group, thêm thông tin group
            if (question.getGroup() != null) {
                GroupQuestion group = question.getGroup();
                qrr.setGroupId(group.getId());
                qrr.setGroupContent(group.getContent());
                qrr.setGroupQuestionRange(group.getQuestionRange());
                
                // Group images
                if (group.getImages() != null && !group.getImages().isEmpty()) {
                    List<String> groupImageUrls = group.getImages().stream()
                            .map(img -> minIO_MediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                            .collect(Collectors.toList());
                    qrr.setGroupImages(groupImageUrls);
                }
                
                // Group audios
                if (group.getAudios() != null && !group.getAudios().isEmpty()) {
                    List<String> groupAudioUrls = group.getAudios().stream()
                            .map(a -> minIO_MediaService.getPresignedURL(a.getUrl(), Duration.ofDays(1)))
                            .collect(Collectors.toList());
                    qrr.setGroupAudios(groupAudioUrls);
                }
            }

            // --- OPTIONS ---
            List<QuestionReviewResponse.OptionResponse> optionResponses =
                    question.getOptions().stream().map(opt -> {
                        QuestionReviewResponse.OptionResponse optRes =
                                new QuestionReviewResponse.OptionResponse();
                        optRes.setMark(opt.getMark());
                        optRes.setDetail(opt.getDetail());
                        return optRes;
                    }).collect(Collectors.toList());

            qrr.setOptions(optionResponses);

            return qrr;

        }).collect(Collectors.toList());
    }

    /**
     * Get part summary string from review questions
     * FIX: Handle both numeric ("1") and string ("Part 1") formats
     */
    public String getPartSummaryFromReview(List<QuestionReview> reviewQuestions) {
        // Lấy các part duy nhất, convert về số nguyên, loại null/rỗng
        Set<Integer> partSet = reviewQuestions.stream()
                .map(rq -> rq.getToeicQuestion() != null ? rq.getToeicQuestion().getPart() : null)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parsePartNumber)
                .filter(Objects::nonNull)
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
    
    /**
     * Parse part string to integer
     * Handles both "1" and "Part 1" formats
     */
    private Integer parsePartNumber(String partStr) {
        if (partStr == null || partStr.isBlank()) return null;
        
        try {
            // Try direct parse first (format: "1", "2", ...)
            return Integer.parseInt(partStr.trim());
        } catch (NumberFormatException e) {
            // Try extracting number from "Part X" format
            try {
                String numStr = partStr.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    return Integer.parseInt(numStr);
                }
            } catch (NumberFormatException e2) {
                // Ignore
            }
        }
        return null;
    }
    
    // Giữ method cũ để backward compatible
    @Deprecated
    public ExamReviewResponse convertToRespone(ExamReview review, Exam exam, User user) {
        return convertToResponse(review, exam, user);
    }
}