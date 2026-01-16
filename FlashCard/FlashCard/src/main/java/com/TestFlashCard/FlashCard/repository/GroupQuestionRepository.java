package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.GroupQuestion;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupQuestionRepository extends JpaRepository<GroupQuestion, Integer> {

//    @Query("""
//    SELECT DISTINCT g FROM GroupQuestion g
//    LEFT JOIN FETCH g.questions q
//    LEFT JOIN FETCH q.options
//    LEFT JOIN FETCH g.images
//    LEFT JOIN FETCH g.audios
//    WHERE g.id IN :ids
//    """)
//    List<GroupQuestion> findFullByIds(List<Integer> ids);

    @Query("""
select distinct g from GroupQuestion g
left join fetch g.questions q
left join fetch q.options
left join fetch g.images
left join fetch g.audios
where g.id in :ids
""")
    List<GroupQuestion> findFullByIds( List<Integer> ids);


    @Query("""
select distinct g from GroupQuestion g
left join fetch g.images
left join fetch g.audios
where g.id in :ids
""")
    List<GroupQuestion> findGroupsWithMedia(List<Integer> ids);



}
