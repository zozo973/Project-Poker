package com.example.projectpoker.model;

import java.time.LocalDateTime;

public class User {
    // fields that go into the User Database table
    private int userID;
    private String Username;
    private String Password;
    private String Email;
    private LocalDateTime CreatedAt;

    // add constructor to set initial values for object fields
    // method takes parameters which will be set by user
    // UserID and LocalDateTime aren't provided by user directly so not included for now they are created separately
    public User(String Username, String Password, String Email) {
        this.Username = Username;
        this.Password = Password;
        this.Email = Email;
    }

    // get method returns variable value
    public int getUserID() {
        return userID;
    }
    public String getUsername() {
        return Username;
    }
    public String getPassword() {
        return Password;
    }
    public String getEmail() {
        return Email;
    }
    public LocalDateTime getCreatedAt() {
        return CreatedAt;
    }

    // set method sets variable value
    public void setUserID(int userID){
        this.userID = userID;
    }
    public void setUsername(String Username) {
        this.Username = Username;
    }
    public void setPassword(String Password){
        this.Password = Password;
    }
    public void setEmail(String Email){
        this.Email = Email;
    }
    public void setCreatedAt(LocalDateTime CreatedAt) {
        this.CreatedAt = CreatedAt;
    }
}




