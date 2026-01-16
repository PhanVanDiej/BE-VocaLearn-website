package com.TestFlashCard.FlashCard.repository;


import com.TestFlashCard.FlashCard.entity.BankGroupQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BankGroupQuestionRepository
        extends JpaRepository<BankGroupQuestion, Long> {
    @Query("""
    SELECT b.sourceGroupId FROM BankGroupQuestion b
    WHERE b.sourceGroupId IN :ids
    """)
    List<Integer> findExistingSourceIds( List<Integer> ids);

    @Query("""
    SELECT DISTINCT b FROM BankGroupQuestion b
    LEFT JOIN FETCH b.questions q
    LEFT JOIN FETCH q.options
    LEFT JOIN FETCH b.images
    LEFT JOIN FETCH b.audios
    WHERE b.sourceGroupId IN :ids
    """)
    List<BankGroupQuestion> findBySourceGroupIds( List<Integer> ids);

}
