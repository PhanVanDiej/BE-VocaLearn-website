package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.response.BankGroupQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankToeicQuestionResponse;

import java.util.List;

public interface QuestionBankService {
    List<BankToeicQuestionResponse> contributeManyToeicQuestions(List<Integer> ids);
    List<BankGroupQuestionResponse> contributeManyGroupQuestions(List<Integer> ids);
//    List<Integer> contributeManyGroupQuestions(List<Integer> ids);
}
