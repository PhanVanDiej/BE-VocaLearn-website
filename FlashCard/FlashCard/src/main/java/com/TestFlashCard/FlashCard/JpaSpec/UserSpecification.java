package com.TestFlashCard.FlashCard.JpaSpec;

import org.springframework.data.jpa.domain.Specification;

import com.TestFlashCard.FlashCard.Enum.EUserStatus;
import com.TestFlashCard.FlashCard.Enum.Role;
import com.TestFlashCard.FlashCard.entity.User;

public class UserSpecification {
    public static Specification<User> hasStatus(EUserStatus status){
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("isDeleted"), status.toString());
    }
    public static Specification<User> hasRole(Role role){
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role.toString());
    }
}
