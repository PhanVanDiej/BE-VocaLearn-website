package com.TestFlashCard.FlashCard.JpaSpec;

import org.springframework.data.jpa.domain.Specification;

import com.TestFlashCard.FlashCard.entity.Exam;

public class ExamSpecification {
    public static Specification<Exam> hasYear(Integer year) {
        return (root, query, cb) -> year == null ? null : cb.equal(root.get("year"), year);
    }

    public static Specification<Exam> hasType(String type) {
        return (root, query, cb) -> (type == null || type.isEmpty()) ? null
                : cb.equal(root.get("type").get("type"), type);
    }

    public static Specification<Exam> hasCollection(String collection) {
        return (root, query, cb) -> (collection == null || collection.isEmpty()) ? null
                : cb.equal(root.get("collection").get("collection"), collection);
    }

    public static Specification<Exam> containsTitle(String title) {
        return (root, query, cb) -> (title == null || title.isEmpty())
                ? null
                : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }
    //  đề SYSTEM: fileImportName IS NOT NULL
    public static Specification<Exam> isSystemExam() {
        return (root, query, cb) -> cb.isNotNull(root.get("fileImportName"));
    }

    //  đề USER: fileImportName IS NULL
    public static Specification<Exam> isUserExam() {
        return (root, query, cb) -> cb.isNull(root.get("fileImportName"));
    }
}
