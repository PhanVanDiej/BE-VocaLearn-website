package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.request.ExamCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamUpdateRequest;
import com.TestFlashCard.FlashCard.response.ExamFilterdResponse;
import com.TestFlashCard.FlashCard.service.ExamService;

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
    public ResponseEntity<?> deleteExamById(@PathVariable Integer examID){
        examService.deleteById(examID);
        return ResponseEntity.ok("Delete Exam with id : " + examID + " successfully!");
    }

    @PostMapping("/importQuestions")
    public ResponseEntity<?> importQuestion(@RequestParam("file") MultipartFile file, @RequestParam Integer examID ) throws IOException{
        try{
            examService.importQuestions(file, examID);
            return ResponseEntity.ok().body("Import successfully questions for exam with id : " + examID);
        }catch(Exception exception){
            throw new IOException(exception.getMessage());
        }
    }
    
}
