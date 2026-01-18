package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.Utils.SearchCriteriaUtils;
import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.exception.DuplicateGroupInBankException;
import com.TestFlashCard.FlashCard.exception.DuplicateQuestionInBankException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.mapper.BankMapper;
import com.TestFlashCard.FlashCard.repository.*;
import com.TestFlashCard.FlashCard.repository.critetia.GenericSearchQueryCriteriaConsumer;
import com.TestFlashCard.FlashCard.repository.critetia.SearchCriteria;
import com.TestFlashCard.FlashCard.repository.critetia.SearchQueryCriteriaConsumer;
import com.TestFlashCard.FlashCard.response.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private final GenericSearchRepository genericSearchRepository;
    private final ExamRepository examRepository;
    private final ToeicQuestionOptionRepository toeicQuestionOptionRepository;
    private final ToeicQuestionImageRepository toeicQuestionImageRepository;


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
    @Transactional
    public List<BankUseSingleQuestionResponse> useSingleQuestions(List<Integer> ids, int examId) {

        // 0. check exam tồn tại
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        // 1. load bank questions
        List<BankToeicQuestion> bankQuestions =
                bankToeicRepo.findWithImages(ids);

        // 2. check câu nào đã được dùng trong exam
        List<Integer> bankIds = bankQuestions.stream()
                .map(BankToeicQuestion::getId)
                .toList();

        List<Integer> usedIds =
                toeicQuestionRepository.findUsedBankQuestionIds(examId, bankIds);

        Set<Integer> usedSet = new HashSet<>(usedIds);

        // 3. filter chỉ giữ câu CHƯA dùng
        List<BankToeicQuestion> newQuestions = bankQuestions.stream()
                .filter(bq -> !usedSet.contains(bq.getId()))
                .toList();

        if (newQuestions.isEmpty()) {
            throw new RuntimeException("All selected questions already exist in this exam");
        }

        // 4. load options CHỈ cho câu mới
        List<Integer> newIds = newQuestions.stream()
                .map(BankToeicQuestion::getId)
                .toList();

        List<BankToeicOption> bankOptions =
                bankToeicOptionRepoitory.findOptionsByQuestionIds(newIds);

        Map<Integer, List<BankToeicOption>> optionMap =
                bankOptions.stream().collect(Collectors.groupingBy(
                        o -> o.getQuestion().getId()
                ));

        boolean isRandom = exam.isRandom();

        // ===== 5. chuẩn bị count để tính indexNumber =====

        int totalCount = 0;
        Map<String, Integer> partCountMap = new HashMap<>();

        if (isRandom) {
            // random: index tăng liên tục toàn đề
            totalCount = toeicQuestionRepository.countByExam_Id(examId);
        } else {
            // custom: mỗi part có index riêng
            for (BankToeicQuestion bq : newQuestions) {
                partCountMap.putIfAbsent(
                        bq.getPart(),
                        toeicQuestionRepository.countByExam_IdAndPart(examId, bq.getPart())
                );
            }
        }

        // ===== 6. add câu hỏi vào exam =====
        for (BankToeicQuestion bq : newQuestions) {

            int indexNumber;

            if (isRandom) {
                indexNumber = ++totalCount;
            } else {
                int current = partCountMap.get(bq.getPart());
                current++;
                partCountMap.put(bq.getPart(), current);
                indexNumber = current;
            }

            // map question
            ToeicQuestion q = bankMapper.mapToeicQuestionFromBank(bq, exam, indexNumber);
            toeicQuestionRepository.save(q);

            // map options
            List<BankToeicOption> ops =
                    optionMap.getOrDefault(bq.getId(), List.of());

            Set<ToeicQuestionOption> examOps = ops.stream()
                    .map(o -> bankMapper.mapOptionFromBank(o, q))
                    .collect(Collectors.toSet());

            toeicQuestionOptionRepository.saveAll(examOps);
            q.setOptions(examOps);

            // map images
            if (bq.getImages() != null && !bq.getImages().isEmpty()) {
                List<ToeicQuestionImage> imgs = bq.getImages().stream()
                        .map(i -> bankMapper.mapImageFromBank(i, q))
                        .toList();
                toeicQuestionImageRepository.saveAll(imgs);
                q.setImages(imgs);
            }
        }

        // 7. trả response (chỉ các câu mới được add)
        return newQuestions.stream()
                .map(bankMapper::toSingleResponse)
                .toList();
    }


    // ===== GROUP =====
    @Override
    public List<BankUseGroupQuestionResponse> useGroupQuestions(List<Long> ids, int examId) {

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

    @Override
    public PageResponse<?> getAllQuestionFromBank(int pageNo, int pageSize, String sortBy, boolean isGroup,String[] search) {
        if(!isGroup) {
          return getAllSingleQuestion(pageNo,pageSize,sortBy,search);

        }else{
            return getAllGroupQuestion(pageNo,pageSize,sortBy,search);
        }
    }
    public PageResponse<?> getAllSingleQuestion(int pageNo, int pageSize, String sortBy,String[] search) {

        // convert search -> criteria
        List<SearchCriteria> criteriaList = SearchCriteriaUtils.convert(search);
        SearchQueryCriteriaConsumer<BankToeicQuestion> consumer =
                new GenericSearchQueryCriteriaConsumer<>(null, null, null);

        // dùng generic search repo
        PageResponse<?> rawPage = genericSearchRepository.searchByCriteria(
                BankToeicQuestion.class,
                pageNo,
                pageSize,
                criteriaList,
                sortBy,
                consumer
        );

        List<BankToeicQuestion> questions = (List<BankToeicQuestion>) rawPage.getItems();

        List<BankToeicQuestionResponse> dtoList = bankMapper.toSingleQuestionDTOList(questions);

        return PageResponse.<List<BankToeicQuestionResponse>>builder()
                .pageNo(rawPage.getPageNo())
                .pageSize(rawPage.getPageSize())
                .totalPage(rawPage.getTotalPage())
                .items(dtoList)
                .build();
    }
    public PageResponse<?> getAllGroupQuestion(int pageNo, int pageSize, String sortBy,String[] search) {

        // convert search -> criteria
        List<SearchCriteria> criteriaList = SearchCriteriaUtils.convert(search);
        SearchQueryCriteriaConsumer<BankGroupQuestion> consumer =
                new GenericSearchQueryCriteriaConsumer<>(null, null, null);

        // dùng generic search repo
        PageResponse<?> rawPage = genericSearchRepository.searchByCriteria(
                BankGroupQuestion.class,
                pageNo,
                pageSize,
                criteriaList,
                sortBy,
                consumer
        );

        List<BankGroupQuestion> questions = (List<BankGroupQuestion>) rawPage.getItems();

        List<BankGroupQuestionResponse> dtoList = bankMapper.toGroupQuestionDTOList(questions);

        return PageResponse.<List<BankGroupQuestionResponse>>builder()
                .pageNo(rawPage.getPageNo())
                .pageSize(rawPage.getPageSize())
                .totalPage(rawPage.getTotalPage())
                .items(dtoList)
                .build();
    }
}
