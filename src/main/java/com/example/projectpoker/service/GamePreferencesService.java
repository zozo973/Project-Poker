package com.example.projectpoker.service;

import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.database.UserPreferencesDAO;
import com.example.projectpoker.model.GamePreferences;
import com.example.projectpoker.model.User;

public class GamePreferencesService {

    public GamePreferences loadForCurrentUser() {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getId() <= 0) {
            return GamePreferences.defaults();
        }

        DatabaseManager.initializeDatabase();
        return new UserPreferencesDAO().getByUserId(user.getId());
    }

    public void saveForCurrentUser(GamePreferences gamePreferences) {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getId() <= 0) {
            return;
        }

        DatabaseManager.initializeDatabase();
        new UserPreferencesDAO().saveForUserId(user.getId(), gamePreferences);
    }
}
