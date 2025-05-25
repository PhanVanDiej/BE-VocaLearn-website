package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.TestFlashCard.FlashCard.JpaSpec.ExamSpecification;
import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.repository.IToeicQuestion_Repository;
import com.TestFlashCard.FlashCard.request.ExamCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamUpdateRequest;
import com.TestFlashCard.FlashCard.response.ExamFilterdResponse;
import com.TestFlashCard.FlashCard.response.ExamInformationResponse;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;

import org.springframework.util.FileSystemUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamService {
    @Autowired
    private final IExam_Repository exam_Repository;
    @Autowired
    private final IToeicQuestion_Repository toeicQuestion_repository;
    @Autowired
    private final ExcelParser excelParser;
    @Autowired
    private final MediaService mediaService;

    public List<ExamFilterdResponse> getByFilter(Integer year, String type, String collection, String title) {
        Specification<Exam> spec = Specification.where(ExamSpecification.hasYear(year))
                .and(ExamSpecification.hasType(type)).and(ExamSpecification.hasCollection(collection))
                .and(ExamSpecification.containsTitle(title));
        return exam_Repository.findAll(spec).stream().map(this::convertToResponse).toList();
    }

    public ExamFilterdResponse convertToResponse(Exam exam) {
        return new ExamFilterdResponse(
                exam.getId(),
                exam.getTitle());
    }

    public ExamInformationResponse getByID(int examID) {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));

        return new ExamInformationResponse(
                examID,
                exam.getDuration(),
                getNumOfPart(examID),
                getNumOfQuestion(examID),
                exam.getTitle(),
                exam.getYear(),
                exam.getType(),
                exam.getCollection(),
                exam.getQuestions().stream().map(this::convertQuestionToResponse).toList());
    }
    public ToeicQuestionResponse convertQuestionToResponse(ToeicQuestion question){
        return new ToeicQuestionResponse(
            question.getId(),
            question.getPart(),
            question.getDetail(),
            question.getResult(),
            question.getImage(),
            question.getAudio()
        );
    }

    @Transactional
    public void create(ExamCreateRequest examDetail) throws IOException {
        Exam exam = new Exam();
        exam.setCollection(examDetail.getCollection().name());
        exam.setDuration(examDetail.getDuration());
        exam.setTitle(examDetail.getTitle());
        exam.setType(examDetail.getType().name());
        exam.setYear(examDetail.getYear());

        exam_Repository.save(exam);
    }

    public int getNumOfQuestion(int examID) {
        return toeicQuestion_repository.countQuestionsByExamId(examID);
    }

    public int getNumOfPart(int examID) {
        // return default number : 7
        return 7;
    }

    @Transactional
    public void updateExam(ExamUpdateRequest examDetail, int examID) {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));

        if (examDetail.getDuration() != null)
            exam.setDuration(examDetail.getDuration());
        if (examDetail.getCollection() != null)
            exam.setCollection(examDetail.getCollection().name());
        if (examDetail.getTitle() != null)
            exam.setTitle(examDetail.getTitle());
        if (examDetail.getType() != null)
            exam.setType(examDetail.getType().name());
        if (examDetail.getYear() != null)
            exam.setYear(examDetail.getYear());

        exam_Repository.save(exam);
    }

    @Transactional
    public void deleteById(int examID) {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));
        List<ToeicQuestion>questions=exam.getQuestions();
        for(ToeicQuestion question: questions){
            mediaService.deleteQuestionMedia(question);
        }
        exam_Repository.delete(exam);
    }

    @Transactional
    public void importQuestions(MultipartFile zipFile, Integer examId) throws IOException {
        // Lấy exam đã tồn tại
        Exam exam = exam_Repository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        //Xóa danh sách câu hỏi hiện tại
        //Xóa file media
        List<ToeicQuestion>currenQuestions=exam.getQuestions();
        for(ToeicQuestion question: currenQuestions){
            mediaService.deleteQuestionMedia(question);
        }
        exam.getQuestions().clear();
        // Extract file zip
        Path tempDir = Files.createTempDirectory("uploadExam");
        File file = new File(tempDir.toFile(), zipFile.getOriginalFilename());
        zipFile.transferTo(file);
        ZipUtil.unpack(file, tempDir.toFile());

        File excelFile = new File(tempDir.toFile(), "questions.xlsx");
        File mediaDir = new File(tempDir.toFile(), "media");

        // Parse và liên kết với Exam
        List<ToeicQuestion> questions = excelParser.parseQuestions(excelFile, mediaDir);
        for (ToeicQuestion q : questions) {
            q.setExam(exam);
        }
        exam.getQuestions().addAll(questions); // hoặc ghi đè, tuỳ mục đích

        exam_Repository.save(exam);
        FileSystemUtils.deleteRecursively(tempDir);
    }
}
