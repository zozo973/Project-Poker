package com.example.projectpoker.service;

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

    /**
     * Returns the currently logged-in user.
     *
     * @return the {@link User} who is logged in, or {@code null} if no user is authenticated
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Stores the given user as the currently logged-in user.
     *
     * @param user the {@link User} who has just logged in
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Clears the current user, effectively logging them out of the application.
     */
    public static void logout(){
        currentUser = null;
    }
}