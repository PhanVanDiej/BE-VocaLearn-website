package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    PaymentTransaction findByOrderId(String orderId);
}