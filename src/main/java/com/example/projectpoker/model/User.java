package com.example.projectpoker.model;

import java.time.LocalDateTime;

public class User {
    // fields that go into the User Database table
    private int UserID;
    private String userName;
    private String userPassword;
    private String email;
    private LocalDateTime createdAt;

    // add constructor to set initial values for object fields
    // method takes parameters which will be set by user
    private User(String userName, String userPassword, String email) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.email = email;
    }

}




