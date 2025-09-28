package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.response.CardFillResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DictionaryService {
    private static final String TRANSLITERATION_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public CardFillResponse fetchWordData(String word) {
        RestTemplate restTemplate = new RestTemplate();

        String apiUrl = TRANSLITERATION_API_URL + word;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> response = responseEntity.getBody();

            if (response == null || response.isEmpty()) {
                return new CardFillResponse(
                        "(Không thể phiên âm)",
                        null,
                        "unknown",
                        List.of("(no example)"),
                        List.of(),
                        null
                );
            }

            String phoneticText = null;
            String audioUrl = null;

            Map<String, String> hintMap = new LinkedHashMap<>();    // partOfSpeech -> definition
            Map<String, String> exampleMap = new LinkedHashMap<>(); // partOfSpeech -> example (may be empty)

            for (Map<String, Object> entry : response) {
                // === lấy phonetics (text + audio) ===
                List<Map<String, Object>> phonetics = (List<Map<String, Object>>) entry.get("phonetics");
                if (phonetics != null) {
                    for (Map<String, Object> phonetic : phonetics) {
                        if (phoneticText == null) {
                            Object textObj = phonetic.get("text");
                            if (textObj instanceof String && !((String) textObj).isBlank()) {
                                phoneticText = (String) textObj;
                            }
                        }
                        if (audioUrl == null) {
                            Object audioObj = phonetic.get("audio");
                            if (audioObj instanceof String && !((String) audioObj).isBlank()) {
                                audioUrl = (String) audioObj;
                            }
                        }
                        if (phoneticText != null && audioUrl != null) break;
                    }
                }

                // === lấy meanings ===
                List<Map<String, Object>> meanings = (List<Map<String, Object>>) entry.get("meanings");
                if (meanings == null) continue;

                for (Map<String, Object> meaning : meanings) {
                    Object posObj = meaning.get("partOfSpeech");
                    if (!(posObj instanceof String) || ((String) posObj).isBlank()) continue;
                    String partOfSpeech = (String) posObj;

                    // nếu đã có definition cho partOfSpeech này thì bỏ qua
                    if (hintMap.containsKey(partOfSpeech)) continue;

                    List<Map<String, Object>> definitions = (List<Map<String, Object>>) meaning.get("definitions");
                    if (definitions == null || definitions.isEmpty()) continue;

                    // 1) thử tìm definition có example trước
                    boolean found = false;
                    for (Map<String, Object> def : definitions) {
                        String defStr = def.get("definition") instanceof String ? (String) def.get("definition") : null;
                        String exStr = def.get("example") instanceof String ? (String) def.get("example") : null;

                        if (defStr != null && !defStr.isBlank() && exStr != null && !exStr.isBlank()) {
                            hintMap.put(partOfSpeech, defStr);
                            exampleMap.put(partOfSpeech, exStr);
                            found = true;
                            break;
                        }
                    }
                    if (found) continue;

                    // 2) nếu không tìm thấy definition có example, lấy definition đầu tiên (nếu có)
                    for (Map<String, Object> def : definitions) {
                        String defStr = def.get("definition") instanceof String ? (String) def.get("definition") : null;
                        if (defStr != null && !defStr.isBlank()) {
                            hintMap.put(partOfSpeech, defStr);
                            exampleMap.put(partOfSpeech, "");
                            break;
                        }
                    }
                }
            }

            // fallback cho partOfSpeech + hint + example
            String partOfSpeech = !hintMap.isEmpty() ? String.join(", ", hintMap.keySet()) : "unknown";
            String hint = !hintMap.isEmpty() ? String.join("+", hintMap.values()) : "";
            String example;
            if (!exampleMap.isEmpty()) {
                example = exampleMap.values().stream()
                        .map(v -> (v == null || v.isBlank()) ? "(no example)" : v)
                        .collect(Collectors.joining("+"));
            } else {
                example = "(no example)";
            }

            return new CardFillResponse(
                    phoneticText != null ? phoneticText : "(Không thể phiên âm)",
                    audioUrl,
                    partOfSpeech,
                    Arrays.stream(example.split("\\+")).toList(),
                    Arrays.stream(hint.split("\\+")).toList(),
                    "This is definition" // hoặc definition riêng nếu bạn muốn, ở đây dùng hint
            );

        } catch (Exception e) {
            log.error("Error when fetching word data: {}", e.getMessage());
            return new CardFillResponse(
                    "(Không thể phiên âm)",
                    null,
                    "unknown",
                    List.of("(no example)"),
                    List.of(),
                    null
            );
        }
    }

}
