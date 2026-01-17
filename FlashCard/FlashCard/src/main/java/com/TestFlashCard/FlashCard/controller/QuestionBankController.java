package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.exception.DuplicateGroupInBankException;
import com.TestFlashCard.FlashCard.exception.DuplicateQuestionInBankException;
import com.TestFlashCard.FlashCard.mapper.BankMapper;
import com.TestFlashCard.FlashCard.request.ListIdContributeRequest;
import com.TestFlashCard.FlashCard.request.UseFromBankRequest;
import com.TestFlashCard.FlashCard.response.*;
import com.TestFlashCard.FlashCard.service.QuestionBankService;
import com.TestFlashCard.FlashCard.service.QuestionBankServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;
    private final BankMapper bankMapper;

    // ==========================
    // ĐÓNG GÓP VÀO BANK
    // ==========================
    @PostMapping("/contribute/singleQuestion/bulk")
    public ApiResponse<List<BankToeicQuestionResponse>> contributeManyToeic(
            @RequestBody ListIdContributeRequest req
    ) {
        try {
            List<BankToeicQuestionResponse> res =
                    questionBankService.contributeManyToeicQuestions(req.getQuestionIds());

            return new ApiResponse<>(200, "Contributed successfully", res);

        } catch (DuplicateQuestionInBankException ex) {

            return new ApiResponse<>(
                    400,
                    "Some questions already exist in question bank",
                    ex.getDuplicatedResponses()
            );
        }
    }

    @PostMapping("/contribute/groupQuestion/bulk")
    public ApiResponse<List<?>> contributeManyGroup(
            @RequestBody ListIdContributeRequest req
    ) {
        try {
            return new ApiResponse<>(
                    200,
                    "Contributed group questions successfully",
                    questionBankService.contributeManyGroupQuestions(req.getQuestionIds())
            );
        } catch (DuplicateGroupInBankException ex) {
            return new ApiResponse<>(
                    400,
                    "Some groups already exist in question bank",
                    ex.getDuplicatedResponses()
            );
        }
    }

    // ==========================
    // DÙNG TỪ BANK VÀO EXAM
    // ==========================
    @PostMapping("/single")
    public ApiResponse<List<BankUseSingleQuestionResponse>> useSingle(
            @RequestBody UseFromBankRequest req
    ) {
        return ApiResponse.success(
                questionBankService.useSingleQuestions(
                        req.getIds().stream().map(Long::intValue).toList()
                )
        );
    }


    // ===== GROUP =====
    @PostMapping("/group")
    public ApiResponse<List<BankUseGroupQuestionResponse>> useGroup(
            @RequestBody UseFromBankRequest req
    ) {
        return ApiResponse.success(
                questionBankService.useGroupQuestions(req.getIds())
        );
    }
}

