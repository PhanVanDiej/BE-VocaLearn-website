package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.repository.ExamRepository;
import com.TestFlashCard.FlashCard.repository.GroupQuestionRepository;
import com.TestFlashCard.FlashCard.repository.ToeicQuestionRepository;
import com.TestFlashCard.FlashCard.request.GroupQuestionRequestDTO;
import com.TestFlashCard.FlashCard.request.ToeicQuestionRequestDTO;
import com.TestFlashCard.FlashCard.response.GroupQuestionResponseDTO;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupQuestionService {

    private final ExamRepository examRepository;
    private final GroupQuestionRepository groupRepo;
    private final ToeicQuestionRepository questionRepo;

    @Transactional
    public GroupQuestionResponseDTO createGroup(GroupQuestionRequestDTO req) {

        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam không tồn tại"));

        GroupQuestion group = new GroupQuestion();
        group.setPart(req.getPart());
        group.setTitle(req.getTitle());
        group.setQuestionRange(req.getQuestionRange());
        group.setContent(req.getContent());
        group.setExam(exam);

        // Images
        if (req.getImages() != null) {
            for (var img : req.getImages()) {
                GroupQuestionImage i = new GroupQuestionImage();
                i.setUrl(img.getUrl());
                i.setGroup(group);
                group.getImages().add(i);
            }
        }

        // Audios
        if (req.getAudios() != null) {
            for (var audio : req.getAudios()) {
                GroupQuestionAudio a = new GroupQuestionAudio();
                a.setUrl(audio.getUrl());
                a.setGroup(group);
                group.getAudios().add(a);
            }
        }

        // Child questions
        if (req.getQuestions() != null) {
            for (ToeicQuestionRequestDTO qReq : req.getQuestions()) {
                ToeicQuestion q = new ToeicQuestion();
                q.setDetail(qReq.getDetail());
                q.setIndexNumber(qReq.getIndexNumber());
                q.setResult(qReq.getResult());
                q.setClarify(qReq.getClarify());
                q.setAudio(qReq.getAudio());
                q.setExam(exam);
                q.setPart(req.getPart());
                q.setGroup(group);

                // Options
                q.setOptions(
                        qReq.getOptions().stream().map(o -> {
                            ToeicQuestionOption opt = new ToeicQuestionOption();
                            opt.setDetail(o.getDetail());
                            opt.setMark(o.getMark());
                            opt.setToeicQuestion(q);
                            return opt;
                        }).toList()
                );

                // Images
                if (qReq.getImages() != null) {
                    q.setImages(
                            qReq.getImages().stream().map(img -> {
                                ToeicQuestionImage qi = new ToeicQuestionImage();
                                qi.setUrl(img.getUrl());
                                qi.setToeicQuestion(q);
                                return qi;
                            }).toList()
                    );
                }

                group.getQuestions().add(q);
            }
        }

        GroupQuestion saved = groupRepo.save(group);
        return toResponseDTO(saved);
    }

    @Transactional
    public GroupQuestion updateGroup(Integer groupId, GroupQuestionRequestDTO req) {

        GroupQuestion group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));

        group.setPart(req.getPart());
        group.setTitle(req.getTitle());
        group.setQuestionRange(req.getQuestionRange());
        group.setContent(req.getContent());

        // --- Replace Images ---
        group.getImages().clear();
        if (req.getImages() != null) {
            for (var img : req.getImages()) {
                GroupQuestionImage i = new GroupQuestionImage();
                i.setUrl(img.getUrl());
                i.setGroup(group);
                group.getImages().add(i);
            }
        }

        // --- Replace Audios ---
        group.getAudios().clear();
        if (req.getAudios() != null) {
            for (var audio : req.getAudios()) {
                GroupQuestionAudio a = new GroupQuestionAudio();
                a.setUrl(audio.getUrl());
                a.setGroup(group);
                group.getAudios().add(a);
            }
        }

        // --- Replace Questions ---
        group.getQuestions().clear();
        if (req.getQuestions() != null) {
            for (ToeicQuestionRequestDTO qReq : req.getQuestions()) {
                ToeicQuestion q = new ToeicQuestion();
                q.setDetail(qReq.getDetail());
                q.setResult(qReq.getResult());
                q.setClarify(qReq.getClarify());
                q.setAudio(qReq.getAudio());
                q.setPart(req.getPart());
                q.setExam(group.getExam());
                q.setGroup(group);

                // Options
                q.setOptions(
                        qReq.getOptions().stream().map(o -> {
                            ToeicQuestionOption opt = new ToeicQuestionOption();
                            opt.setMark(o.getMark());
                            opt.setDetail(o.getDetail());
                            opt.setToeicQuestion(q);
                            return opt;
                        }).toList()
                );

                // Images
                if (qReq.getImages() != null) {
                    q.setImages(
                            qReq.getImages().stream().map(img -> {
                                ToeicQuestionImage qi = new ToeicQuestionImage();
                                qi.setUrl(img.getUrl());
                                qi.setToeicQuestion(q);
                                return qi;
                            }).toList()
                    );
                }

                group.getQuestions().add(q);
            }
        }

        return groupRepo.save(group);
    }

    @Transactional
    public void deleteGroup(Integer id) {
        GroupQuestion group = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));

        groupRepo.delete(group);
    }

    public GroupQuestion getGroup(Integer id) {
        return groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));
    }

    private GroupQuestionResponseDTO toResponseDTO(GroupQuestion group) {
        GroupQuestionResponseDTO dto = new GroupQuestionResponseDTO();

        dto.setId(group.getId());
        dto.setPart(group.getPart());
        dto.setTitle(group.getTitle());
        dto.setContent(group.getContent());
        dto.setQuestionRange(group.getQuestionRange());
        dto.setExamId(group.getExam().getId());

        // Images
        dto.setImages(
                group.getImages().stream()
                        .map(GroupQuestionImage::getUrl)
                        .toList()
        );

        // Audios
        dto.setAudios(
                group.getAudios().stream()
                        .map(GroupQuestionAudio::getUrl)
                        .toList()
        );

        // Questions
        dto.setQuestions(
                group.getQuestions().stream()
                        .map(q -> new ToeicQuestionResponse(
                                q.getId(),
                                q.getIndexNumber(),
                                q.getPart(),
                                q.getDetail(),
                                q.getResult(),
                                q.getImages().stream().map(ToeicQuestionImage::getUrl).toList(),
                                q.getAudio(),
                                q.getConversation(), // nếu không có thì bỏ
                                q.getClarify(),
                                q.getOptions().stream()
                                        .map(o -> new ToeicQuestionResponse.OptionResponse(
                                                o.getMark(),
                                                o.getDetail()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );

        return dto;
    }
}

