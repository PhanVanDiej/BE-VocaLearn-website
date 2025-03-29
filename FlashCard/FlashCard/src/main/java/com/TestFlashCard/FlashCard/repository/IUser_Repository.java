package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.User;

@Repository
public interface IUser_Repository extends JpaRepository<User,Long>{
    
    //Optional<User> findById (Long id);
    public User findByAccountName (String accountName);
    public User findByEmail(String email);
}
