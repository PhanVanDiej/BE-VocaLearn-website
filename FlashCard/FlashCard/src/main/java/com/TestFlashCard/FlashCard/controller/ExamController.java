package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.request.CommentCreateRequest;
import com.TestFlashCard.FlashCard.request.CommentReplyCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamSubmitRequest;
import com.TestFlashCard.FlashCard.request.ExamUpdateRequest;
import com.TestFlashCard.FlashCard.response.CommentResponse;
import com.TestFlashCard.FlashCard.response.ExamFilterdResponse;
import com.TestFlashCard.FlashCard.response.ExamReviewResponse;
import com.TestFlashCard.FlashCard.service.CommentService;
import com.TestFlashCard.FlashCard.service.ExamReviewService;
import com.TestFlashCard.FlashCard.service.ExamService;
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

    @GetMapping("/filter")
    public ResponseEntity<?> getExamsByFilter(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String collection,
            @RequestParam(required = false) String title) {

        List<ExamFilterdResponse> exams = examService.getByFilter(year, type, collection, title);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/detail/{examID}")
    public ResponseEntity<?> getById(@PathVariable Integer examID) {
        return ResponseEntity.ok(examService.getByID(examID));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createExam(@RequestBody @Valid ExamCreateRequest request) throws IOException {
        examService.create(request);
        return ResponseEntity.ok("Create a new Exam successfully !");
    }

    @PutMapping("/update/{examID}")
    public ResponseEntity<?> updateExam(@PathVariable Integer examID, @RequestBody ExamUpdateRequest request) {
        examService.updateExam(request, examID);
        return ResponseEntity.ok("Update Exam with id : " + examID + " successfully!");
    }

    @DeleteMapping("/delete/{examID}")
    public ResponseEntity<?> deleteExamById(@PathVariable Integer examID) {
        examService.deleteById(examID);
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
    public ResponseEntity<ExamReviewResponse> submitExam(@RequestBody @Valid ExamSubmitRequest request,
            Principal principal) {
        String accountName = principal.getName();
        User user = userService.getUserByAccountName(accountName);
        ExamReviewResponse response = examReviewService.submitExam(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examID}/comments")
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
}
