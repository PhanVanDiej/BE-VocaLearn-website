package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.TestFlashCard.FlashCard.JpaSpec.ExamSpecification;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.ICommentReply_Repository;
import com.TestFlashCard.FlashCard.repository.IComment_Repository;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.repository.IToeicQuestion_Repository;
import com.TestFlashCard.FlashCard.request.ExamCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamUpdateRequest;

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
    private MinIO_MediaService minIO_MediaService;
    @Autowired
    private final IComment_Repository comment_Repository;
    @Autowired
    private final ICommentReply_Repository commentReply_Repository;
    @Autowired
    private final ExamTypeService examTypeService;
    @Autowired
    private final ExamCollectionService examCollectionService;

    public List<ExamInformationResponse> getByFilter(Integer year, String type, String collection, String title) {
        Specification<Exam> spec = Specification.where(ExamSpecification.hasYear(year))
                .and(ExamSpecification.hasType(type)).and(ExamSpecification.hasCollection(collection))
                .and(ExamSpecification.containsTitle(title));
        return exam_Repository.findAll(spec).stream().map(this::convertToExamDetailResponse).toList();
    }

    public ExamFilterdResponse convertToResponse(Exam exam) {
        return new ExamFilterdResponse(
                exam.getId(),
                exam.getTitle());
    }

    public ExamInformationResponse getByID(int examID) throws IOException {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));
        return convertToExamDetailResponse(exam);
    }

    public int countAllCommentsAndReplies(Integer examId) {
        int commentCount = comment_Repository.countByExamId(examId);
        int replyCount = commentReply_Repository.countRepliesByExamId(examId);
        return commentCount + replyCount;
    }

    public List<ExamInformationResponse> getByCreatAt() {
        List<Exam> exams = exam_Repository.findAllByOrderByCreatedAtDesc();
        return exams.stream().map(this::convertToExamDetailResponse).toList();
    }

    public ExamInformationResponse convertToExamDetailResponse(Exam exam) {
        List<ToeicQuestionResponse> singleQuestions = exam.getQuestions().stream()
                .filter(q -> !List.of("3", "4", "7").contains(q.getPart()))
                .map(this::convertQuestionToResponse)
                .toList();

        List<GroupQuestionResponseDTO> groupQuestions = exam.getGroupQuestions().stream()
                .map(this::convertGroupToResponse)
                .toList();

//        return new ExamInformationResponse(
//                exam.getId(),
//                exam.getDuration(),
//                getNumOfPart(exam.getId()),
//                getNumOfQuestion(exam.getId()),
//                exam.getTitle(),
//                exam.getYear(),
//                exam.getType().getType(),
//                exam.getCollection().getCollection(),
//                exam.getAttemps(),
//                countAllCommentsAndReplies(exam.getId()),
//                exam.getFileImportName(),
//                singleQuestions,
//                groupQuestions
//        );
        return new ExamInformationResponse(
                exam.getId(),
                exam.getDuration() !=null ? exam.getDuration(): null,                    // giữ null nếu không có giá trị
                getNumOfPart(exam.getId()),
                getNumOfQuestion(exam.getId()),
                exam.getTitle(),
                exam.getYear() !=null ? exam.getYear() : null,
                exam.getType() !=null ? exam.getType().getType() : null,
                exam.getCollection() != null ?exam.getCollection().getCollection() : null,
                exam.getAttemps(),
                countAllCommentsAndReplies(exam.getId()),
                exam.getFileImportName(),
                singleQuestions,
                groupQuestions
        );
    }
    public GroupQuestionResponseDTO convertGroupToResponse(GroupQuestion group) {
        // Images
        List<String> images = group.getImages() != null
                ? group.getImages().stream()
                .map(i -> minIO_MediaService.getPresignedURL(i.getUrl(), Duration.ofDays(1)))
                .toList()
                : List.of();

        // Audios
        List<String> audios = group.getAudios() != null
                ? group.getAudios().stream()
                .map(a -> minIO_MediaService.getPresignedURL(a.getUrl(), Duration.ofDays(1)))
                .toList()
                : List.of();

        // Child questions
        List<ToeicQuestionResponse> childQuestions = group.getQuestions() != null
                ? group.getQuestions().stream()
                .map(this::convertQuestionToResponse)
                .toList()
                : List.of();

        return new GroupQuestionResponseDTO(
                group.getId(),
                group.getPart(),
                group.getTitle(),
                group.getContent(),
                group.getQuestionRange(),
                group.getExam().getId(),
                images,
                audios,
                childQuestions
        );
    }

    public ToeicQuestionResponse convertQuestionToResponse(ToeicQuestion question) {

        // Options
        List<ToeicQuestionResponse.OptionResponse> options =
                question.getOptions().stream()
                        .map(opt -> new ToeicQuestionResponse.OptionResponse(opt.getMark(), opt.getDetail()))
                        .collect(Collectors.toList());

        // Images (new)
        List<String> imageUrls = question.getImages() != null
                ? question.getImages().stream()
                .map(i -> minIO_MediaService.getPresignedURL(i.getUrl(), Duration.ofMinutes(1)))
                .collect(Collectors.toList())
                : List.of();

        // Audio (unchanged)
        String audio = null;
        if (question.getAudio() != null && !question.getAudio().isEmpty()) {
            audio = minIO_MediaService.getPresignedURL(question.getAudio(), Duration.ofDays(1));
        }

        return new ToeicQuestionResponse(
                question.getId(),
                question.getIndexNumber(),
                question.getPart(),
                question.getDetail(),
                question.getResult(),
                imageUrls,          // <-- LIST mới
                audio,
                question.getConversation(),
                question.getClarify(),
                options
        );
    }

    @Transactional
    public ExamFilterdResponse create(ExamCreateRequest examDetail) throws IOException {
        Exam exam = new Exam();

        ExamType examType = examTypeService.getDetailByType(examDetail.getType());
        ExamCollection examCollection = examCollectionService.getDetailByCollection(examDetail.getCollection());

        System.out.println(examCollection);

        exam.setCollection(examCollection);
        exam.setDuration(examDetail.getDuration());
        exam.setTitle(examDetail.getTitle());
        exam.setType(examType);
        exam.setYear(examDetail.getYear());
        exam.setRandom(examDetail.getIsRandom());
        exam.setAttemps(0);
        exam_Repository.save(exam);
        return convertToResponse(exam);
    }

    public int getNumOfQuestion(int examID) {
        return toeicQuestion_repository.countQuestionsByExamId(examID);
    }

    public int getNumOfPart(int examID) {
        // return default number : 7
        return 7;
    }

    @Transactional
    public void updateExam(ExamUpdateRequest examDetail, int examID) throws IOException {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));

        if (examDetail.getDuration() != null)
            exam.setDuration(examDetail.getDuration());
        if (examDetail.getCollection() != null) {
            ExamCollection examCollection = examCollectionService.getDetailByCollection(examDetail.getCollection());
            exam.setCollection(examCollection);
        }
        if (examDetail.getTitle() != null)
            exam.setTitle(examDetail.getTitle());
        if (examDetail.getType() != null) {
            ExamType examType = examTypeService.getDetailByType(examDetail.getType());
            exam.setType(examType);
        }
        if (examDetail.getYear() != null)
            exam.setYear(examDetail.getYear());
        if (examDetail.getAttemps() != null)
            exam.setAttemps(examDetail.getAttemps());

        exam_Repository.save(exam);
    }

    @Transactional
    public void deleteById(int examID) {
        Exam exam = exam_Repository.findById(examID)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find Exam with id: " + examID));

        // 1️⃣ Xóa media các ToeicQuestion trực tiếp
        for (ToeicQuestion question : exam.getQuestions()) {
            minIO_MediaService.deleteQuestionMedia(question);
        }

        // 2️⃣ Xóa media các GroupQuestion
        if (exam.getGroupQuestions() != null) {
            for (GroupQuestion group : exam.getGroupQuestions()) {
                // Xóa ảnh của group
                if (group.getImages() != null) {
                    for (GroupQuestionImage img : group.getImages()) {
                        minIO_MediaService.deleteFile(img.getUrl());
                    }
                }
                // Xóa audio của group
                if (group.getAudios() != null) {
                    for (GroupQuestionAudio audio : group.getAudios()) {
                        minIO_MediaService.deleteFile(audio.getUrl());
                    }
                }
                // Xóa media cho các ToeicQuestion con trong group
                if (group.getQuestions() != null) {
                    for (ToeicQuestion question : group.getQuestions()) {
                        minIO_MediaService.deleteQuestionMedia(question);
                    }
                }
            }
        }

        // 3️⃣ Xóa Exam → cascade sẽ xóa tất cả entity
        exam_Repository.delete(exam);
    }

    @Transactional
    public void importQuestions(MultipartFile zipFile, Integer examId) throws IOException {
        // Lấy exam đã tồn tại
        Exam exam = exam_Repository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        exam.setFileImportName(zipFile.getOriginalFilename());
        // Xóa danh sách câu hỏi hiện tại
        // Xóa file media
        List<ToeicQuestion> currenQuestions = exam.getQuestions();
        for (ToeicQuestion question : currenQuestions) {
            minIO_MediaService.deleteQuestionMedia(question);
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
        exam.getQuestions().addAll(questions);

        exam_Repository.save(exam);
        FileSystemUtils.deleteRecursively(tempDir);
    }

    public long countAll() {
        return exam_Repository.count();
    }

    public List<ExamAttemp> getTop3ExamByAttemps() {
        List<Exam> exams = exam_Repository.findTop3ByOrderByAttempsDesc();
        return exams.stream()
                .map(e -> {
                    ExamAttemp dto = new ExamAttemp();
                    dto.setTest(e.getTitle());
                    dto.setAttemps(e.getAttemps());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
