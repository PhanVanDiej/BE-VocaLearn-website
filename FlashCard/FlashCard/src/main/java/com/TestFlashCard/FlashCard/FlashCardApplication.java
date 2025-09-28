package com.TestFlashCard.FlashCard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.TestFlashCard.FlashCard")
public class FlashCardApplication {
	public static void main(String[] args) {
		SpringApplication.run(FlashCardApplication.class, args);
	}
}
