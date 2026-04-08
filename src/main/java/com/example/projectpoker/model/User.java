package com.example.projectpoker.model;

import java.time.LocalDateTime;

public class User {
    // fields that go into the User Database table
    private int UserID;
    private String Username;
    private String Password;
    private String Email;
    private LocalDateTime CreatedAt;

    // add constructor to set initial values for object fields
    // method takes parameters which will be set by user
    // UserID and LocalDateTime aren't provided by user directly, they are created differently
    public User(String Username, String Password, String Email) {
        this.Username = Username;
        this.Password = Password;
        this.Email = Email;
    }

    // get method returns variable value
    public int GetUserID() {
        return UserID;
    }
    public String GetUsername() {
        return Username;
    }
    public String GetPassword() {
        return Password;
    }
    public String GetEmail() {
        return Email;
    }
    public LocalDateTime GetCreatedAt() {
        return CreatedAt;
    }

    // set method sets variable value
    public void SetUserID(int UserID){
        this.UserID = UserID;
    }
    public void SetUsername(String Username) {
        this.Username = Username;
    }
    public void SetPassword(String Password){
        this.Password = Password;
    }
    public void SetEmail(String Email){
        this.Email = Email;
    }
    public void SetCreatedAt(LocalDateTime CreatedAt){
        this.CreatedAt = CreatedAt;
    }
}




