package com.TestFlashCard.FlashCard.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController

public class test {
    @GetMapping("/test")
    public String TestSpring() {
        return new String("hello spring boot");
    }
    
}
