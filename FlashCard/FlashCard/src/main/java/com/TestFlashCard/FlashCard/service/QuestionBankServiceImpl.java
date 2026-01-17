package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.exception.DuplicateGroupInBankException;
import com.TestFlashCard.FlashCard.exception.DuplicateQuestionInBankException;
import com.TestFlashCard.FlashCard.mapper.BankMapper;
import com.TestFlashCard.FlashCard.repository.*;
import com.TestFlashCard.FlashCard.response.BankGroupQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankToeicQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankUseGroupQuestionResponse;
import com.TestFlashCard.FlashCard.response.BankUseSingleQuestionResponse;
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
    private final BankToeicOptionRepoitory bankToeicOptionRepoitory;
    private final BankGroupChildQuestionRepository bankGroupChildQuestionRepository;



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

    // ===== SINGLE =====
    @Override
    public List<BankUseSingleQuestionResponse> useSingleQuestions(List<Integer> ids) {

        // 1. load question + images
        List<BankToeicQuestion> questions =
                bankToeicRepo.findWithImages(ids);

        if (questions.size() != ids.size()) {
            throw new RuntimeException("Some bank questions not found");
        }

        // 2. load options
        List<BankToeicOption> options =
                bankToeicOptionRepoitory.findOptionsByQuestionIds(ids);

        // 3. group options by questionId
        Map<Integer, List<BankToeicOption>> optionMap =
                options.stream().collect(Collectors.groupingBy(
                        o -> o.getQuestion().getId()
                ));

        // 4. gán options vào question
        for (BankToeicQuestion q : questions) {
            q.setOptions(optionMap.getOrDefault(q.getId(), List.of()));
        }

        // 5. map DTO
        return questions.stream()
                .map(bankMapper::toSingleResponse)
                .toList();
    }


    // ===== GROUP =====
    @Override
    public List<BankUseGroupQuestionResponse> useGroupQuestions(List<Long> ids) {

        // 1. load group + media
        List<BankGroupQuestion> groups =
                bankGroupQuestionRepository.findGroupsWithMedia(ids);

        if (groups.size() != ids.size()) {
            throw new RuntimeException("Some bank group questions not found");
        }

        // 2. load child + options
        List<BankGroupChildQuestion> children =
                bankGroupQuestionRepository.findChildrenWithOptions(ids);

        // 3. group by groupId
        Map<Long, List<BankGroupChildQuestion>> childMap =
                children.stream().collect(Collectors.groupingBy(
                        c -> c.getGroup().getId()
                ));

        // 4. map dto
        return groups.stream()
                .map(g -> bankMapper.toGroupResponse(
                        g,
                        childMap.getOrDefault(g.getId(), List.of())
                ))
                .toList();
    }
    @Override
    public BankToeicQuestionResponse getSingleDetail(Integer id) {

        BankToeicQuestion q =
                bankToeicRepo.findWithImagesById(id)
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        List<BankToeicOption> options =
                bankToeicOptionRepoitory.findByQuestionId(id);

        q.setOptions(options);

        return bankMapper.mapSingleToResponse(q);
    }
    @Override
    public BankGroupQuestionResponse getGroupDetail(Long id) {

        BankGroupQuestion g =
                bankGroupQuestionRepository.findGroupWithMedia(id)
                        .orElseThrow(() -> new RuntimeException("Group not found"));

        List<BankGroupChildQuestion> children =
                bankGroupChildQuestionRepository.findChildrenWithOptionsByGroupId(id);

        return bankMapper.mapGroupToResponse(g, children);
    }
}
