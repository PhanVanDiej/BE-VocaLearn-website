package com.TestFlashCard.FlashCard.JpaSpec;

import org.springframework.data.jpa.domain.Specification;

import com.TestFlashCard.FlashCard.entity.Exam;

public class ExamSpecification {
    public static Specification<Exam> isDeleted(boolean check) {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), check);
    }
    public static Specification<Exam> hasYear(Integer year) {
        return (root, query, cb) -> year == null ? null : cb.equal(root.get("year"), year);
    }

    public static Specification<Exam> hasType(String type) {
        return (root, query, cb) -> (type == null || type.isEmpty()) ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Exam> hasCollection(String collection) {
        return (root, query, cb) -> (collection == null || collection.isEmpty()) ? null
                : cb.equal(root.get("collection"), collection);
    }

    public static Specification<Exam> containsTitle(String title) {
        return (root, query, cb) -> (title == null || title.isEmpty())
                ? null
                : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }
}
