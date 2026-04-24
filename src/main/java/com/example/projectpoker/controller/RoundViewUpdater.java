package com.example.projectpoker.controller;

import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.enums.Roles;

import java.util.ArrayList;

public interface RoundViewUpdater {
    void onUserTurnStarted();
    void onAiTurnStarted();
    void onBalanceChanged(Player player, int oldBalance, int newBalance);
    void onBlindSizeChanged(int newBlindSize);
    void onPotChanged(int newPot);
    void onRoundStatusChanged(String phase);
    void onCommunityCardsChanged(ArrayList<Card> newCC, ArrayList<Card> oldCC);
    void onToPlayChange(int toPlay);
    void onGameStatusChanged(GameStatus gameStatus);
    void onRoundCreation(Round round);
    void onPlayerChange(ArrayList<Player> newPlayers, ArrayList<Player> oldPlayers);
    void onPlayerActionChange(Action action);
    void onPlayerRoleUpdate(Roles role);
    void onDealCards();
    void onRoundStarted();
}
