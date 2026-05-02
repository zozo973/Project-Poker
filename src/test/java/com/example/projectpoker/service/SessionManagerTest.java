package com.example.projectpoker.service;

import com.example.projectpoker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    // Reset session before every test so tests don't affect each other
    @BeforeEach
    void setUp() {
        SessionManager.logout();
    }

    // getCurrentUser() tests

    @Test
    void getCurrentUser_returnsNullWhenNoUserLoggedIn() {
        // No user has been set — should be null
        assertNull(SessionManager.getCurrentUser(), "Should return null when no user is logged in");
    }

    @Test
    void getCurrentUser_returnsUserAfterSetCurrentUser() {
        // Arrange
        User user = new User("testuser", "hashedpassword", "test@email.com");
        // Act
        SessionManager.setCurrentUser(user);
        // Assert
        assertEquals(user, SessionManager.getCurrentUser(), "Should return the user that was set");
    }

    @Test
    void getCurrentUser_returnsCorrectUsername() {
        // Arrange
        User user = new User("basil", "hashedpassword", "basil@email.com");
        // Act
        SessionManager.setCurrentUser(user);
        // Assert
        assertEquals("basil", SessionManager.getCurrentUser().getUsername(),
                "Should return the correct username");
    }

    // setCurrentUser() tests

    @Test
    void setCurrentUser_overwritesPreviousUser() {
        // Arrange
        User user1 = new User("firstuser", "hash1", "first@email.com");
        User user2 = new User("seconduser", "hash2", "second@email.com");
        // Act
        SessionManager.setCurrentUser(user1);
        SessionManager.setCurrentUser(user2);
        // Assert
        assertEquals(user2, SessionManager.getCurrentUser(),
                "Should return the most recently set user");
    }

    // logout() tests

    @Test
    void logout_clearsCurrentUser() {
        // Arrange
        User user = new User("testuser", "hashedpassword", "test@email.com");
        SessionManager.setCurrentUser(user);
        // Act
        SessionManager.logout();
        // Assert
        assertNull(SessionManager.getCurrentUser(), "Should return null after logout");
    }

    @Test
    void logout_canBeCalledWhenNoUserLoggedIn() {
        // Should not throw any exception even if no user is logged in
        assertDoesNotThrow(() -> SessionManager.logout(),
                "logout() should not throw when no user is logged in");
    }
}