package com.example.projectpoker.controller;

import com.example.projectpoker.model.game.Player;

public interface RoundViewUpdater {
    void onUserTurnStarted();
    void onAiTurnStarted();
    void onBalanceChanged(Player player, int oldBalance, int newBalance);
    void onBlindSizeChanged(int newBlindSize);
    void onPotChanged(int newPot);
    void onRoundPhaseChanged(String phase);
}
