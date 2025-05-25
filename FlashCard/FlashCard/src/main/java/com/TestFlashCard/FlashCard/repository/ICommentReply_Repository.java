package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.CommentReply;

@Repository
public interface ICommentReply_Repository extends JpaRepository<CommentReply,Integer> {

    
} 