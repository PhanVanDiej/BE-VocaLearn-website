package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.exception.DuplicateGroupInBankException;
import com.TestFlashCard.FlashCard.exception.DuplicateQuestionInBankException;
import com.TestFlashCard.FlashCard.mapper.BankMapper;
import com.TestFlashCard.FlashCard.repository.*;
import com.TestFlashCard.FlashCard.response.BankGroupQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankToeicQuestionResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {

    private static final Logger log = LoggerFactory.getLogger(QuestionBankServiceImpl.class);
    private final ToeicQuestionRepository toeicRepo;
    private final BankToeicQuestionRepoitory bankToeicRepo;
    private final ExamRepository examRepo;
    private final BankMapper bankMapper;
    private final IUser_Repository IUser_Repository;
    private final BankGroupQuestionRepository bankGroupQuestionRepository;
    private final GroupQuestionRepository groupQuestionRepository;
    private final ToeicQuestionRepository toeicQuestionRepository;

    @Override
    @Transactional
    public List<BankToeicQuestionResponse> contributeManyToeicQuestions(List<Integer> ids) {
        //Chỉ check trùng khi đóng góp(contribute) cùng 1 câu hỏi vào ngân hàng nhiều lần theo id của câu hỏi
        List<BankToeicQuestion> existed = bankToeicRepo.findBySourceToeicIds(ids);

        if (!existed.isEmpty()) {

            List<BankToeicQuestionResponse> res = existed.stream()
                    .map(bankMapper::mapToResponse)
                    .toList();

            throw new DuplicateQuestionInBankException(res);
        }


        List<ToeicQuestion> questions = toeicRepo.findAllById(ids);
        if (questions.size() != ids.size()) {
            throw new RuntimeException("Some questions not found");
        }

        User contributor = getCurrentUser();

        List<BankToeicQuestion> banks = questions.stream()
                .map(q -> {
                    BankToeicQuestion b = bankMapper.mapToeicToBank(q);
                    b.setContributor(contributor);
                    return b;
                })
                .toList();

        return bankToeicRepo.saveAll(banks).stream().map(bankMapper::mapToResponse).toList();
    }

    private User getCurrentUser() {
        String accountName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        log.info("Current user: {}", accountName);
        User user = IUser_Repository.findByAccountName(accountName);
        if(user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

//    @Override
//    @Transactional
//    public List<BankGroupQuestionResponse> contributeManyGroupQuestions(List<Integer> ids) {
//
//        // ===== check trùng =====
//        List<Integer> existedIds = bankGroupQuestionRepository.findExistingSourceIds(ids);
//
//        if (!existedIds.isEmpty()) {
//
//            List<BankGroupQuestion> existed =
//                    bankGroupQuestionRepository.findBySourceGroupIds(existedIds);
//
//            List<BankGroupQuestionResponse> res = existed.stream()
//                    .map(bankMapper::mapGroupToResponse)
//                    .toList();
//
//            throw new DuplicateGroupInBankException(res);
//        }
//
//        // ===== load full group =====
//        List<GroupQuestion> groups = groupQuestionRepository.findFullByIds(ids);
//
//        if (groups.size() != ids.size()) {
//            throw new RuntimeException("Some group questions not found");
//        }
//
//        User contributor = getCurrentUser();
//
//        // ===== map to bank =====
//        List<BankGroupQuestion> banks = groups.stream()
//                .map(g -> bankMapper.mapGroupToBank(g, contributor))
//                .toList();
//
//        List<BankGroupQuestion> saved = bankGroupQuestionRepository.saveAll(banks);
//
//        return saved.stream()
//                .map(bankMapper::mapGroupToResponse)
//                .toList();
//    }

    @Override
    @Transactional
    public List<BankGroupQuestionResponse> contributeManyGroupQuestions(List<Integer> ids) {

        // ===== check trùng trong bank =====
        List<Integer> existedIds = bankGroupQuestionRepository.findExistingSourceIds(ids);

        if (!existedIds.isEmpty()) {

            List<BankGroupQuestion> existed =
                    bankGroupQuestionRepository.findBySourceGroupIds(existedIds);

            List<BankGroupQuestionResponse> res = existed.stream()
                    .map(bankMapper::mapGroupToResponse)
                    .toList();

            throw new DuplicateGroupInBankException(res);
        }

        // ===== load group + images + audios =====
        List<GroupQuestion> groups =
                groupQuestionRepository.findGroupsWithMedia(ids);

        if (groups.size() != ids.size()) {
            throw new RuntimeException("Some group questions not found");
        }

        // ===== load questions + options =====
        List<ToeicQuestion> questions =
                toeicQuestionRepository.findQuestionsWithOptionsByGroupIds(ids);

        // ===== group questions by groupId =====
        Map<Integer, List<ToeicQuestion>> questionMap =
                questions.stream().collect(Collectors.groupingBy(
                        q -> q.getGroup().getId()
                ));

        User contributor = getCurrentUser();

        // ===== map sang bank =====
        List<BankGroupQuestion> banks = groups.stream()
                .map(g -> bankMapper.mapGroupToBank(
                        g,
                        contributor,
                        questionMap.getOrDefault(g.getId(), List.of())
                ))
                .toList();

        bankGroupQuestionRepository.saveAll(banks);

        // ===== trả DTO =====
        return banks.stream()
                .map(bankMapper::mapGroupToResponse)
                .toList();
    }


    // ==========================
    // BANK → EXAM (SỬ DỤNG)
    // ==========================

//    @Transactional
//    public void useToeicFromBank(Long bankId, Integer examId) {
//
//        BankToeicQuestion b = bankToeicRepo.findById(bankId)
//                .orElseThrow(() -> new RuntimeException("Bank question not found"));
//
//        Exam exam = examRepo.findById(examId)
//                .orElseThrow(() -> new RuntimeException("Exam not found"));
//
//        ToeicQuestion q = new ToeicQuestion();
//        q.setPart(b.getPart());
//        q.setDetail(b.getDetail());
//        q.setResult(b.getResult());
//        q.setClarify(b.getClarify());
//        q.setAudio(b.getAudio()); // copy key
//        q.setExam(exam);
//
//        // ===== OPTIONS =====
//        List<ToeicQuestionOption> options = b.getOptions().stream().map(o -> {
//            ToeicQuestionOption x = new ToeicQuestionOption();
//            x.setMark(o.getMark());
//            x.setDetail(o.getDetail());
//            x.setToeicQuestion(q);
//            return x;
//        }).toList();
//
//        // ===== IMAGES =====
//        List<ToeicQuestionImage> images = b.getImages().stream().map(i -> {
//            ToeicQuestionImage x = new ToeicQuestionImage();
//            x.setUrl(i.getUrl());
//            x.setToeicQuestion(q);
//            return x;
//        }).toList();
//
//        q.setOptions(options);
//        q.setImages(images);
//
//        toeicRepo.save(q);
//    }

}
