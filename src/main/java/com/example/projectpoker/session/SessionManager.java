package com.example.projectpoker.session;

import java.time.LocalDateTime;

// app needs to remember who is logged in for as long as the app is open
public class SessionManager {

    // field (not sure if should be public or private)
    // set as boolean cause either logged in or logged out
        // true = logged in
        // false = logged out
    private static Boolean CurrentUser;

    // constructor
    public SessionManager(Boolean CurrentUser){
        this.CurrentUser = CurrentUser;
    }
    public Boolean GetCurrentUser() {
        return GetCurrentUser();
    }
    public void SetCurrentUser_In(Boolean CurrentUser){
        CurrentUser = true;
        this.CurrentUser = CurrentUser;
    }
    public void SetCurrentUser_Out(Boolean CurrentUser){
        CurrentUser = false;
        this.CurrentUser = CurrentUser;
    }
}