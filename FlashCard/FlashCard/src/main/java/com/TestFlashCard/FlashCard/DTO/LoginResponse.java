package com.TestFlashCard.FlashCard.DTO;

public class LoginResponse {
    private String token;
    private Long userId;
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
    public void setId(Long id){
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
    public Long getUserId(){
        return this.userId;
    }
    public String getAccountName(){
        return this.accountName;
    }
    public String getRole(){
        return this.role;
    }
}


