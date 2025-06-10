package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.request.EvaluateCreateRequest;
import com.TestFlashCard.FlashCard.request.EvaluateUpdateRequest;
import com.TestFlashCard.FlashCard.response.EvaluateResponse;
import com.TestFlashCard.FlashCard.service.EvaluateService;
import com.TestFlashCard.FlashCard.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/evaluate")
public class EvaluateController {
    @Autowired
    private final EvaluateService evaluateService;
    @Autowired
    private final UserService userService;
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEvaluate(@RequestParam String data,
            @RequestParam(required = false) MultipartFile image, Principal principal) throws IOException {
        User user = userService.getUserByAccountName(principal.getName());

        ObjectMapper object = new ObjectMapper();
        EvaluateCreateRequest request = object.readValue(data, EvaluateCreateRequest.class);

        evaluateService.createEvaluate(request, image, user);

        return ResponseEntity.ok("Create new Evaluate successfully!");
    }

    @DeleteMapping("/delete/{evaluateID}")
    public ResponseEntity<?> deleteEvaluate(@PathVariable("evaluateID") Integer id) {
        evaluateService.deleteEvaluate(id);
        return ResponseEntity.ok("Delete evaluate with id: " + id + "successfully!");
    }

    @GetMapping("/get")
    public ResponseEntity<?> getMethodName(@RequestParam(required = false) Integer star) throws IOException {
        List<EvaluateResponse> evaluates = star!=null ? evaluateService.getEvaluatesByStar(star):evaluateService.getAllEvaluates();
        return new ResponseEntity<List<EvaluateResponse>>(evaluates, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEvaluate(@PathVariable Integer id, @RequestBody EvaluateUpdateRequest request ) throws IOException{
        evaluateService.update(request.getAdminReply(), id);
        return ResponseEntity.ok("Update evaluate successfully");
    }
}
