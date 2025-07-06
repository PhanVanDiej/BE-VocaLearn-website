package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ExamCollection;
import com.TestFlashCard.FlashCard.entity.ExamType;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.request.CommentCreateRequest;
import com.TestFlashCard.FlashCard.request.CommentReplyCreateRequest;
import com.TestFlashCard.FlashCard.request.CommentUpdateRequest;
import com.TestFlashCard.FlashCard.request.ExamCollectionCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamCollectionUpdateRequest;
import com.TestFlashCard.FlashCard.request.ExamCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamSubmitRequest;
import com.TestFlashCard.FlashCard.request.ExamTypeCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamTypeUpdateRequest;
import com.TestFlashCard.FlashCard.request.ExamUpdateRequest;
import com.TestFlashCard.FlashCard.response.CommentResponse;
import com.TestFlashCard.FlashCard.response.ExamInformationResponse;
import com.TestFlashCard.FlashCard.response.ExamReviewResponse;
import com.TestFlashCard.FlashCard.service.CommentService;
import com.TestFlashCard.FlashCard.service.ExamCollectionService;
import com.TestFlashCard.FlashCard.service.ExamReviewService;
import com.TestFlashCard.FlashCard.service.ExamService;
import com.TestFlashCard.FlashCard.service.ExamTypeService;
import com.TestFlashCard.FlashCard.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    @Autowired
    private final ExamService examService;
    @Autowired
    private final ExamReviewService examReviewService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final CommentService commentService;
    @Autowired
    private final ExamTypeService examTypeService;
    @Autowired
    private final ExamCollectionService examCollectionService;
    @Autowired
    private final IExam_Repository exam_Repository;

    @GetMapping("/filter")
    public ResponseEntity<?> getExamsByFilter(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String collection,
            @RequestParam(required = false) String title) {

        List<ExamInformationResponse> exams = examService.getByFilter(year, type, collection, title);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/getByCreateAt")
    public ResponseEntity<?> getByCreateAt() {
        List<ExamInformationResponse> response = examService.getByCreatAt();
        return new ResponseEntity<List<ExamInformationResponse>>(response, HttpStatus.OK);
    }

    @GetMapping("/detail/{examID}")
    public ResponseEntity<?> getById(@PathVariable Integer examID) throws IOException {
        return ResponseEntity.ok(examService.getByID(examID));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createExam(@RequestBody @Valid ExamCreateRequest request) throws IOException {
        return ResponseEntity.ok(examService.create(request));
    }

    @PutMapping("/update/{examID}")
    public ResponseEntity<?> updateExam(@PathVariable Integer examID, @RequestBody ExamUpdateRequest request)
            throws IOException {
        examService.updateExam(request, examID);
        return ResponseEntity.ok("Update Exam with id : " + examID + " successfully!");
    }

    @DeleteMapping("/delete/{examID}")
    public ResponseEntity<?> deleteExamById(@PathVariable Integer examID) {
        examService.DeleteById(examID);
        return ResponseEntity.ok("Delete Exam with id : " + examID + " successfully!");
    }

    @PostMapping("/importQuestions")
    public ResponseEntity<?> importQuestion(@RequestParam("file") MultipartFile file, @RequestParam Integer examID)
            throws IOException {
        try {
            examService.importQuestions(file, examID);
            return ResponseEntity.ok().body("Import successfully questions for exam with id : " + examID);
        } catch (Exception exception) {
            throw new IOException(exception.getMessage());
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitExam(@RequestBody @Valid ExamSubmitRequest request,
            Principal principal) {
        String accountName = principal.getName();
        User user = userService.getUserByAccountName(accountName);

        ExamReviewResponse response = examReviewService.submitExam(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{examID}")
    public ResponseEntity<List<CommentResponse>> getCommentsByExam(@PathVariable Integer examID) {
        List<CommentResponse> responses = commentService.getCommentsByExamId(examID);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/comment/create")
    public ResponseEntity<?> createComment(@RequestBody @Valid CommentCreateRequest request, Principal principal) {
        String accountName = principal.getName();
        User user = userService.getUserByAccountName(accountName);
        commentService.createComment(user, request);
        return ResponseEntity.ok("Comment created!");
    }

    @PostMapping("/reply-comment/create")
    public ResponseEntity<?> createReply(@RequestBody @Valid CommentReplyCreateRequest request, Principal principal) {
        String accountName = principal.getName();
        User user = userService.getUserByAccountName(accountName);

        commentService.createReply(user, request);

        return ResponseEntity.ok("Reply created successfully.");
    }

    @DeleteMapping("/comment/delete/{commentID}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentID, Principal principal) {
        User user = userService.getUserByAccountName(principal.getName());
        commentService.deleteCommentById(commentID, user);
        return ResponseEntity.ok("Delete Comment with id :" + commentID + " successfully!");
    }

    @DeleteMapping("/reply-comment/delete/{commentReplyID}")
    public ResponseEntity<?> deleteCommentReply(@PathVariable Integer commentReplyID, Principal principal) {
        User user = userService.getUserByAccountName(principal.getName());
        commentService.deleteReplyById(commentReplyID, user);
        return ResponseEntity.ok("Delete Comment with id :" + commentReplyID + " successfully!");
    }

    @PutMapping("comment/update/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Integer id, @RequestBody CommentUpdateRequest request) {
        commentService.updateComment(id, request);
        
        return ResponseEntity.ok("Update comment with id: " + id +" successfully");
    }
    @PutMapping("reply-comment/update/{id}")
    public ResponseEntity<?> updateReplyComment(@PathVariable Integer id, @RequestBody CommentUpdateRequest request) {
        commentService.updateCommentReply(id, request);
        return ResponseEntity.ok("Update reply-comment with id: " + id +" successfully");
    }

    @GetMapping("/type/getAll")
    public ResponseEntity<?> getAllExamTypes() throws IOException {
        List<ExamType> examTypes = examTypeService.getAllExamTypes();
        return new ResponseEntity<List<ExamType>>(examTypes, HttpStatus.OK);
    }

    @GetMapping("/type/id/{id}")
    public ResponseEntity<?> getTypeById(@PathVariable Integer id) throws IOException {
        ExamType examType = examTypeService.getDetailById(id);
        return new ResponseEntity<ExamType>(examType, HttpStatus.OK);
    }

    @GetMapping("/type/name/{type}")
    public ResponseEntity<?> getTypeByName(@PathVariable String type) throws IOException {
        ExamType examType = examTypeService.getDetailByType(type);
        return new ResponseEntity<ExamType>(examType, HttpStatus.OK);
    }

    @PostMapping("/type/create")
    public ResponseEntity<?> createExamType(@RequestBody ExamTypeCreateRequest request) throws IOException {
        examTypeService.create(request);
        return ResponseEntity.ok("Create new ExamType successfully");
    }
    

    @PutMapping("/type/update/{id}")
    public ResponseEntity<?> updateExamType(@PathVariable Integer id, @RequestBody ExamTypeUpdateRequest request)
            throws IOException {
        examTypeService.update(request, id);
        return ResponseEntity.ok("Update ExamType with id : " + id + " successfully");
    }

    @DeleteMapping("/type/delete/{id}")
    public ResponseEntity<?> deleteExamType(@PathVariable Integer id) throws IOException {
        examTypeService.softDelete(id);
        return ResponseEntity.ok("Delete ExamType with id: " + id + " successfully!");
    }

    @GetMapping("/collection/getAll")
    public ResponseEntity<?> getAllExamCollections() throws IOException{
        List<ExamCollection> collections = examCollectionService.getAllExamCollection();
        return new ResponseEntity<>(collections,HttpStatus.OK);
    }

    @GetMapping("/collection/id/{id}")
    public ResponseEntity<?> getExamCollectionById(@PathVariable Integer id) throws IOException{
        ExamCollection examCollection=examCollectionService.getDetailById(id);
        return new ResponseEntity<>(examCollection,HttpStatus.OK);
    }

    @GetMapping("/collection/name/{collection}")
    public ResponseEntity<?> getExamCollectionByName(@PathVariable String collection) throws IOException{
        ExamCollection examCollection=examCollectionService.getDetailByCollection(collection);
        return new ResponseEntity<>(examCollection,HttpStatus.OK);
    }

    @PostMapping("/collection/create")
    public ResponseEntity<?> createExamCollection(@RequestBody ExamCollectionCreateRequest request) {
        examCollectionService.create(request);
        return ResponseEntity.ok("Create new ExamCollection successfully!");
    }
    
    @PutMapping("/collection/update/{id}")
    public ResponseEntity<?> updateExamCollection(@PathVariable Integer id, @RequestBody ExamCollectionUpdateRequest request) throws IOException {
        examCollectionService.update(request, id);
        return ResponseEntity.ok("Update ExamCollection with id: " + id + " successfully!");
    }
    
    @DeleteMapping("/collection/delete/{id}")
    public ResponseEntity<?> deleteExamCollection(@PathVariable Integer id) throws IOException{
        examCollectionService.Delete(id);
        return ResponseEntity.ok("Delete ExamCOllection with id: " + id +" successfully!");
    }
    
    @GetMapping("/result/getAllByExam/{examId}")
    public ResponseEntity<?> getAllResult(@PathVariable Integer examId, Principal principal) {
        User user = userService.getUserByAccountName(principal.getName());
        Exam exam = exam_Repository.findById(examId).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find the Exam with id: " + examId)
        );
        List<ExamReviewResponse> responses = examReviewService.getAllExamResultByUser(user, exam);
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/result/id/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Integer id) {
        ExamReviewResponse response = examReviewService.getById(id);
        return ResponseEntity.ok(response);
    }
    
}
