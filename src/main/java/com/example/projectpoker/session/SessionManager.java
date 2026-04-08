package com.example.projectpoker.session;

// app needs to remember who is logged in for as long as the app is open
public class SessionManager {

    // field (pretty sure it's meant to be private that's why u need getter and setter methods)
    // set as boolean cause either logged in or logged out
        // true = logged in
        // false = logged out
    private static Boolean CurrentUser;

    // no constructor needed static variables belong to classes not objects with constructors initialising objects

    public Boolean GetCurrentUser() {
        return GetCurrentUser();
    }
    public void SetCurrentUser_In(Boolean CurrentUser){
        CurrentUser = true;
    }
    public void SetCurrentUser_Out(Boolean CurrentUser){
        CurrentUser = false;
    }
}