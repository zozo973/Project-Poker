package com.example.projectpoker.model;

import com.example.projectpoker.model.game.enums.Difficulty;

import java.util.List;

public class GamePreferences {
    public static final int MIN_OPPONENTS = 1;
    public static final int MAX_OPPONENTS = 5;
    public static final int DEFAULT_OPPONENTS = 5;
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.Addict;
    public static final String DEFAULT_CARD_BACK_KEY = "back1";
    public static final String DEFAULT_BOARD_KEY = "classic1";
    public static final boolean DEFAULT_DARK_MODE = false;

    /**
     * Describes a selectable themed asset used by the options menu.
     *
     * @param key stable preference key stored in the database
     * @param displayName user-facing label shown in the UI
     * @param resourcePath classpath path to the image resource
     */
    public record AssetOption(String key, String displayName, String resourcePath) {}

    public static final List<AssetOption> CARD_BACK_OPTIONS = List.of(
            new AssetOption("back1", "Blue", "/com/example/projectpoker/Images/Back1.png"),
            new AssetOption("back2", "Red", "/com/example/projectpoker/Images/Back2.png"),
            new AssetOption("back3", "Black", "/com/example/projectpoker/Images/Back3.png"),
            new AssetOption("back4", "Suits", "/com/example/projectpoker/Images/Back4.png"),
            new AssetOption("back5", "Brutal", "/com/example/projectpoker/Images/Back5.png"),
            new AssetOption("back6", "Decor", "/com/example/projectpoker/Images/Back6.png"),
            new AssetOption("back7", "Necra", "/com/example/projectpoker/Images/Back7.png"),
            new AssetOption("back13", "Pokemon", "/com/example/projectpoker/Images/Back13.png"),
            new AssetOption("back8", "Yugioh", "/com/example/projectpoker/Images/Back8.png"),
            new AssetOption("back9", "Yugioh Anime", "/com/example/projectpoker/Images/Back9.png"),
            new AssetOption("back10", "Magic the Gathering", "/com/example/projectpoker/Images/Back10.png"),
            new AssetOption("back11", "Tarot Lunar", "/com/example/projectpoker/Images/Back11.png"),
            new AssetOption("back12", "Tarot Sun", "/com/example/projectpoker/Images/Back12.png")

    );

    public static final List<AssetOption> BOARD_OPTIONS = List.of(
            new AssetOption("classic1", "Green", "/com/example/projectpoker/Images/Poker_Board1.png"),
            new AssetOption("classic2", "Deep Red", "/com/example/projectpoker/Images/Poker_Board2.png"),
            new AssetOption("classic3", "Black", "/com/example/projectpoker/Images/Poker_Board3.png"),
            new AssetOption("classic4", "Blue", "/com/example/projectpoker/Images/Poker_Board4.png"),
            new AssetOption("classic5", "Dark Green", "/com/example/projectpoker/Images/Poker_Board5.png"),
            new AssetOption("classic6", "Low Detail Green", "/com/example/projectpoker/Images/Poker_Board6.png"),
            new AssetOption("classic7", "Low Detail Grey", "/com/example/projectpoker/Images/Poker_Board7.png")

    );

    private final int opponentCount;
    private final Difficulty difficulty;
    private final String cardBackKey;
    private final String boardKey;
    private final boolean darkModeEnabled;

    /**
     * Creates game preferences without overriding dark mode.
     *
     * @param opponentCount number of AI opponents requested
     * @param difficulty selected AI difficulty
     * @param cardBackKey selected card-back asset key
     * @param boardKey selected table-board asset key
     */
    public GamePreferences(int opponentCount, Difficulty difficulty, String cardBackKey, String boardKey) {
        this(opponentCount, difficulty, cardBackKey, boardKey, DEFAULT_DARK_MODE);
    }

    /**
     * Creates game preferences with explicit dark-mode preference.
     *
     * @param opponentCount number of AI opponents requested
     * @param difficulty selected AI difficulty
     * @param cardBackKey selected card-back asset key
     * @param boardKey selected table-board asset key
     * @param darkModeEnabled whether dark mode should be enabled
     */
    public GamePreferences(int opponentCount, Difficulty difficulty, String cardBackKey, String boardKey, boolean darkModeEnabled) {
        this.opponentCount = clampOpponents(opponentCount);
        this.difficulty = difficulty == null ? DEFAULT_DIFFICULTY : difficulty;
        this.cardBackKey = containsOption(CARD_BACK_OPTIONS, cardBackKey) ? cardBackKey : DEFAULT_CARD_BACK_KEY;
        this.boardKey = containsOption(BOARD_OPTIONS, boardKey) ? boardKey : DEFAULT_BOARD_KEY;
        this.darkModeEnabled = darkModeEnabled;
    }

    /**
     * Creates a {@link GamePreferences} instance populated with all default values.
     *
     * @return a new {@link GamePreferences} object using all application defaults
     */
    public static GamePreferences defaults() {
        return new GamePreferences(DEFAULT_OPPONENTS, DEFAULT_DIFFICULTY, DEFAULT_CARD_BACK_KEY, DEFAULT_BOARD_KEY, DEFAULT_DARK_MODE);
    }

    /**
     * Returns the number of AI opponents configured for the game.
     *
     * @return the opponent count, clamped to [{@code MIN_OPPONENTS}, {@code MAX_OPPONENTS}]
     */
    public int getOpponentCount() {
        return opponentCount;
    }

    /**
     * Returns the AI difficulty level configured for the game.
     *
     * @return the {@link Difficulty} enum value
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Returns the key identifying which card back design is selected.
     *
     * @return the card back key string (e.g. {@code "back1"})
     */
    public String getCardBackKey() {
        return cardBackKey;
    }

    /**
     * Returns the key identifying which poker board design is selected.
     *
     * @return the board key string (e.g. {@code "classic1"})
     */
    public String getBoardKey() {
        return boardKey;
    }

    /**
     * Returns whether dark mode is enabled for the game UI.
     *
     * @return {@code true} if dark mode is on, {@code false} otherwise
     */
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    /**
     * Resolves the card back key to its full classpath resource path.
     *
     * @return the resource path string for the selected card back image
     */
    public String getCardBackResourcePath() {
        return resourcePathForKey(CARD_BACK_OPTIONS, cardBackKey, DEFAULT_CARD_BACK_KEY);
    }

    /**
     * Resolves the board key to its full classpath resource path.
     *
     * @return the resource path string for the selected board image
     */
    public String getBoardResourcePath() {
        return resourcePathForKey(BOARD_OPTIONS, boardKey, DEFAULT_BOARD_KEY);
    }

    private static int clampOpponents(int value) {
        return Math.max(MIN_OPPONENTS, Math.min(MAX_OPPONENTS, value));
    }

    private static boolean containsOption(List<AssetOption> options, String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        for (AssetOption option : options) {
            if (option.key().equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static String resourcePathForKey(List<AssetOption> options, String key, String fallbackKey) {
        for (AssetOption option : options) {
            if (option.key().equals(key)) {
                return option.resourcePath();
            }
        }
        for (AssetOption option : options) {
            if (option.key().equals(fallbackKey)) {
                return option.resourcePath();
            }
        }
        return options.getFirst().resourcePath();
    }
}
