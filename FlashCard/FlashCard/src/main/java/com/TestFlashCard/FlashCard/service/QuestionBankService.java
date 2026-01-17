package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.response.BankGroupQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankToeicQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankUseGroupQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankUseSingleQuestionResponse;

import java.util.List;

public interface QuestionBankService {
    List<BankToeicQuestionResponse> contributeManyToeicQuestions(List<Integer> ids);
    List<BankGroupQuestionResponse> contributeManyGroupQuestions(List<Integer> ids);
//    List<Integer> contributeManyGroupQuestions(List<Integer> ids);
    List<BankUseSingleQuestionResponse> useSingleQuestions(List<Integer> ids);
    List<BankUseGroupQuestionResponse> useGroupQuestions(List<Long> ids);
}
