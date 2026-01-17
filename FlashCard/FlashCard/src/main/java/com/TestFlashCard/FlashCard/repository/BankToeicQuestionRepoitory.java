package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.BankToeicQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankToeicQuestionRepoitory
        extends JpaRepository<BankToeicQuestion, Long> {
    @Query("""
   select b from BankToeicQuestion b
   where b.sourceToeicId in :ids
""")
    List<BankToeicQuestion> findBySourceToeicIds(@Param("ids") List<Integer> ids);


    @Query("""
        select distinct q from BankToeicQuestion q
        left join fetch q.options
        left join fetch q.images
        where q.id in :ids
    """)
    List<BankToeicQuestion> findFullByIds(List<Integer> ids);
    @Query("""
select distinct q from BankToeicQuestion q
left join fetch q.images
where q.id in :ids
""")
    List<BankToeicQuestion> findWithImages(List<Integer> ids);

}
