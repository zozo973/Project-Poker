package com.example.projectpoker.model;

import java.time.LocalDateTime;

public class User {
    // fields that go into the User Database table
    private int userID;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;

    // add constructor to set initial values for object fields
    // method takes parameters which will be set by user
    // UserID and LocalDateTime aren't provided by user directly so not included for now they are created separately
    public User(String Username, String Password, String Email) {
        this.username = Username;
        this.password = Password;
        this.email = Email;
    }

    // get method returns variable value
    public int getUserID() {
        return userID;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // set method sets variable value
    public void setUserID(int userID){
        this.userID = userID;
    }
    public void setUsername(String Username) {
        this.username = Username;
    }
    public void setPassword(String Password){
        this.password = Password;
    }
    public void setEmail(String Email){
        this.email = Email;
    }
    public void setCreatedAt(LocalDateTime CreatedAt) {
        this.createdAt = CreatedAt;
    }
}




