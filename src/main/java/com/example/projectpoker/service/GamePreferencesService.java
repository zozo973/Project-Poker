package com.example.projectpoker.service;

import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.database.UserPreferencesDAO;
import com.example.projectpoker.model.GamePreferences;
import com.example.projectpoker.model.User;

public class GamePreferencesService {

    /**
     * Loads the {@link GamePreferences} for the currently logged-in user from the database.
     * Returns default preferences if no user is logged in or the user has no saved preferences.
     *
     * @return the user's saved {@link GamePreferences}, or {@link GamePreferences#defaults()} as a fallback
     */
    public GamePreferences loadForCurrentUser() {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getId() <= 0) {
            return GamePreferences.defaults();
        }

        DatabaseManager.initializeDatabase();
        return new UserPreferencesDAO().getByUserId(user.getId());
    }

    /**
     * Saves the given {@link GamePreferences} for the currently logged-in user to the database.
     * Does nothing if no user is logged in.
     *
     * @param gamePreferences the {@link GamePreferences} to persist for the current user
     */
    public void saveForCurrentUser(GamePreferences gamePreferences) {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getId() <= 0) {
            return;
        }

        DatabaseManager.initializeDatabase();
        new UserPreferencesDAO().saveForUserId(user.getId(), gamePreferences);
    }
}
