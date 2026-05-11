package com.example.projectpoker.service;
import com.example.projectpoker.model.GamePreferences;
import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.enums.Difficulty;
import java.util.prefs.Preferences;

public class GamePreferencesService {
    private static final String NODE_NAME = "com.example.projectpoker.preferences";
    private static final String KEY_OPPONENTS = "opponents";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_CARD_BACK = "cardBack";
    private static final String KEY_BOARD = "board";
    private static final String DEFAULT_USER_SCOPE = "guest";

    private final Preferences preferences = Preferences.userRoot().node(NODE_NAME);

    public GamePreferences loadForCurrentUser() {
        User user = SessionManager.getCurrentUser();
        String userScope = user == null ? DEFAULT_USER_SCOPE : user.getUsername();
        return loadForUserScope(userScope);
    }

    public void saveForCurrentUser(GamePreferences gamePreferences) {
        User user = SessionManager.getCurrentUser();
        String userScope = user == null ? DEFAULT_USER_SCOPE : user.getUsername();
        saveForUserScope(userScope, gamePreferences);
    }

    private GamePreferences loadForUserScope(String userScope) {
        GamePreferences defaults = GamePreferences.defaults();

        int opponents = preferences.getInt(prefKey(userScope, KEY_OPPONENTS), defaults.getOpponentCount());
        String difficultyName = preferences.get(prefKey(userScope, KEY_DIFFICULTY), defaults.getDifficulty().name());
        String cardBackKey = preferences.get(prefKey(userScope, KEY_CARD_BACK), defaults.getCardBackKey());
        String boardKey = preferences.get(prefKey(userScope, KEY_BOARD), defaults.getBoardKey());

        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(difficultyName);
        } catch (IllegalArgumentException ex) {
            difficulty = defaults.getDifficulty();
        }

        return new GamePreferences(opponents, difficulty, cardBackKey, boardKey);
    }

    private void saveForUserScope(String userScope, GamePreferences gamePreferences) {
        if (gamePreferences == null) {
            return;
        }

        preferences.putInt(prefKey(userScope, KEY_OPPONENTS), gamePreferences.getOpponentCount());
        preferences.put(prefKey(userScope, KEY_DIFFICULTY), gamePreferences.getDifficulty().name());
        preferences.put(prefKey(userScope, KEY_CARD_BACK), gamePreferences.getCardBackKey());
        preferences.put(prefKey(userScope, KEY_BOARD), gamePreferences.getBoardKey());
    }

    private String prefKey(String userScope, String key) {
        String safeScope = (userScope == null || userScope.isBlank()) ? DEFAULT_USER_SCOPE : userScope;
        return safeScope + "." + key;
    }
}
