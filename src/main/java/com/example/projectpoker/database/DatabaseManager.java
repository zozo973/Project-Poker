package com.example.projectpoker.database;

import com.example.projectpoker.model.User;

public class DatabaseManager {

    public static void initializeDatabase() {
        UserDAO userDAO = new UserDAO();
        userDAO.createTable();

        User user = new User("ben", "1234", 0, 0);
        userDAO.insert(user);

        for (User u : userDAO.getAll()) {
            System.out.println(u.getUsername() + " " + u.getTotalWins());
        }

        userDAO.close();
    }
}