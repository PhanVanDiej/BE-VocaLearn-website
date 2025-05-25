package com.TestFlashCard.FlashCard.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CommentReplyResponse {
    private Integer id;
    private String content;
    private String userName;
    private LocalDateTime createAt;
    private List<CommentReplyResponse> replies; // reply lồng nhau
}
