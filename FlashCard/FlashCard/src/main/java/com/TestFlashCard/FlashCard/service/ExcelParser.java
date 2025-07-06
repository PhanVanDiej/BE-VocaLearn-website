package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.entity.ToeicQuestionOption;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExcelParser {

    @Autowired
    private final MediaService mediaService;

    public List<ToeicQuestion> parseQuestions(File excelFile, File mediaFolder) {
        List<ToeicQuestion> questions = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int indexQuestion=1;
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header

                ToeicQuestion question = new ToeicQuestion();
                question.setIndexNumber(indexQuestion);
                question.setPart(getCellValue(row.getCell(0)));
                question.setDetail(getCellValue(row.getCell(1)));
                question.setResult(getCellValue(row.getCell(2)));
                question.setConversation(getCellValue(row.getCell(9)));

                indexQuestion++;

                // upload file ảnh và setImage
                String imageFileName = getCellValue(row.getCell(3));
                if (imageFileName != null && !imageFileName.isBlank()) {
                    File imageFile = new File(mediaFolder, imageFileName);
                    if (imageFile.exists()) {
                        String imageUrl = mediaService.getImageUrl(imageFile);
                        question.setImage(imageUrl);
                    } else {
                        throw new FileNotFoundException("Image file not found: " + imageFile.getPath());
                    }
                }
                // upload audio và setAudio
                String audioFileName = getCellValue(row.getCell(4));
                if (audioFileName != null && !audioFileName.isBlank()) {
                    File audioFile = new File(mediaFolder, audioFileName);
                    if (audioFile.exists()) {
                        String audioUrl = mediaService.getAudioUrl(audioFile);
                        question.setAudio(audioUrl);
                    } else {
                        throw new FileNotFoundException("Audio file not found: " + audioFile.getPath());
                    }
                }

                List<ToeicQuestionOption> options = new ArrayList<>();
                for (int i = 5; i <= 8; i++) {
                    String detail = getCellValue(row.getCell(i));
                    if (detail != null && !detail.trim().isEmpty()) {
                        ToeicQuestionOption option = new ToeicQuestionOption();
                        option.setDetail(detail);
                        option.setToeicQuestion(question);
                        if (i == 5)
                            option.setMark("A");
                        else if (i == 6)
                            option.setMark("B");
                        else if (i == 7)
                            option.setMark("C");
                        else if (i == 8)
                            option.setMark("D");
                        options.add(option);
                    }
                }



                question.setOptions(options);
                questions.add(question);
            }

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file Excel", e);
        }

        return questions;
    }

    private String getCellValue(Cell cell) {
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

}
