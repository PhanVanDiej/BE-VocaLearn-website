package com.TestFlashCard.FlashCard.DTO;

public class LoginResponse {
<<<<<<< Updated upstream:FlashCard/FlashCard/src/main/java/com/TestFlashCard/FlashCard/DTO/LoginResponse.java
    private String token;
    private Long userId;
=======
    private String accessToken;
    private String renewalToken;
    private int userId;
>>>>>>> Stashed changes:FlashCard/FlashCard/src/main/java/com/TestFlashCard/FlashCard/response/LoginResponse.java
    private String accountName;
    private String role;

    public LoginResponse(String token, Long id, String accountName, String role){
        this.token=token;
        this.userId=id;
        this.accountName=accountName;
        this.role=role;
    }

    public void setToken(String token){
        this.token=token;
    }
    public void setId(int id){
        this.userId=id;
    }
    public void setAccountName(String name){
        this.accountName=name;
    }
    public void setRole(String role){
        this.role=role;
    }

    public String getToken(){
        return this.token;
    }
    public int getUserId(){
        return this.userId;
    }
    public String getAccountName(){
        return this.accountName;
    }
    public String getRole(){
        return this.role;
    }
}


