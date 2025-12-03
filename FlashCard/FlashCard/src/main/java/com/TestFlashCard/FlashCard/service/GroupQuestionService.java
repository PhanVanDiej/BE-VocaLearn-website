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
    private final MinIO_MediaService minIO_MediaService;

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
    public GroupQuestionResponseDTO updateGroup(Integer groupId, GroupQuestionRequestDTO req) {

        GroupQuestion group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));

        group.setPart(req.getPart());
        group.setTitle(req.getTitle());
        group.setQuestionRange(req.getQuestionRange());
        group.setContent(req.getContent());

        // --- Xóa media cũ ---
        if (group.getImages() != null) {
            for (var img : group.getImages()) minIO_MediaService.deleteFile(img.getUrl());
            group.getImages().clear();
        }
        if (group.getAudios() != null) {
            for (var audio : group.getAudios()) minIO_MediaService.deleteFile(audio.getUrl());
            group.getAudios().clear();
        }
        if (group.getQuestions() != null) {
            for (var q : group.getQuestions()) minIO_MediaService.deleteQuestionMedia(q);
            group.getQuestions().clear();
        }

        // --- Add new images ---
        if (req.getImages() != null) {
            for (var img : req.getImages()) {
                GroupQuestionImage i = new GroupQuestionImage();
                i.setUrl(img.getUrl());
                i.setGroup(group);
                group.getImages().add(i);
            }
        }

        // --- Add new audios ---
        if (req.getAudios() != null) {
            for (var audio : req.getAudios()) {
                GroupQuestionAudio a = new GroupQuestionAudio();
                a.setUrl(audio.getUrl());
                a.setGroup(group);
                group.getAudios().add(a);
            }
        }

        // --- Add new questions ---
        if (req.getQuestions() != null) {
            int idx = extractStartIndex(req.getQuestionRange()); // ex: "32-35" -> 32
            for (ToeicQuestionRequestDTO qReq : req.getQuestions()) {
                ToeicQuestion q = new ToeicQuestion();
                q.setIndexNumber(idx++);
                q.setDetail(qReq.getDetail());
                q.setResult(qReq.getResult());
                q.setClarify(qReq.getClarify());
                q.setAudio(qReq.getAudio());
                q.setPart(req.getPart());
                q.setExam(group.getExam());
                q.setGroup(group);

                // Options
                q.setOptions(qReq.getOptions().stream().map(o -> {
                    ToeicQuestionOption opt = new ToeicQuestionOption();
                    opt.setMark(o.getMark());
                    opt.setDetail(o.getDetail());
                    opt.setToeicQuestion(q);
                    return opt;
                }).toList());

                // Images
                if (qReq.getImages() != null) {
                    q.setImages(qReq.getImages().stream().map(img -> {
                        ToeicQuestionImage qi = new ToeicQuestionImage();
                        qi.setUrl(img.getUrl());
                        qi.setToeicQuestion(q);
                        return qi;
                    }).toList());
                }

                group.getQuestions().add(q);
            }
        }

        return toResponseDTO(groupRepo.save(group));
    }

    // Hàm helper: parse "32-35" -> 32
    private int extractStartIndex(String range) {
        try {
            return Integer.parseInt(range.split("-")[0].trim());
        } catch (Exception e) {
            return 1; // default
        }
    }

    @Transactional
    public void deleteGroup(Integer id) {
        GroupQuestion group = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));

        //  Xóa media ảnh của group
        if (group.getImages() != null) {
            for (GroupQuestionImage img : group.getImages()) {
                minIO_MediaService.deleteFile(img.getUrl());
            }
        }

        //  Xóa media audio của group
        if (group.getAudios() != null) {
            for (GroupQuestionAudio audio : group.getAudios()) {
                minIO_MediaService.deleteFile(audio.getUrl());
            }
        }

        // Xóa media cho các ToeicQuestion con trong group
        if (group.getQuestions() != null) {
            for (ToeicQuestion question : group.getQuestions()) {
                minIO_MediaService.deleteQuestionMedia(question);
            }
        }

        // 4️⃣ Xóa group → cascade xóa các entity con trong DB
        groupRepo.delete(group);
    }

    public GroupQuestionResponseDTO getGroup(Integer id) {
        GroupQuestion gr = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));
        return toResponseDTO(gr);
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

