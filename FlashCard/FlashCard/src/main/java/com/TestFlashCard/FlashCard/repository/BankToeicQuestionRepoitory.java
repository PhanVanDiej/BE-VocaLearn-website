package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.BankToeicQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface BankToeicQuestionRepoitory
        extends JpaRepository<BankToeicQuestion, Long> {
    @Query("""
   select b from BankToeicQuestion b
   where b.sourceToeicId in :ids
""")
    List<BankToeicQuestion> findBySourceToeicIds(@Param("ids") List<Integer> ids);

    @Query("""
SELECT b FROM BankToeicQuestion b
LEFT JOIN FETCH b.options
LEFT JOIN FETCH b.images
WHERE b.id IN :ids
""")
    List<BankToeicQuestion> findFullByIds(@Param("ids") List<Integer> ids);

}
