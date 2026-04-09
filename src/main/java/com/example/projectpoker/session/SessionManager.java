package com.example.projectpoker.session;

import com.example.projectpoker.model.User;

// app needs to remember who is logged in for as long as the app is open
public class SessionManager {

    // field (pretty sure it's meant to be private that's why u need getter and setter methods)

    // use User class and user object
    // initialise variable by setting it to logged out initially
    // null = logged out
    // user = logged in
    private static User currentUser = null;
    // no constructor needed static variables belong to classes not objects
    // constructors initialise objects instead

    // method to get variable to know who is logged in
    public static User getCurrentUser() {
        return currentUser;
    }
    // method to set variable when someone logs in
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    // method to clear variable when someone logs out
    public static void logout(){
        currentUser = null;
    }
}