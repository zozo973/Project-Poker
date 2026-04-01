package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.BetTypeLogic;
import com.example.projectpoker.model.game.oberserver.AbsSubject;

import java.util.ArrayList;

public class Round extends AbsSubject {

    private RoundStatus state;
    private int numPlayers;
    private int toPlay;
    private BetType betType;
    private CardDeck deck;
    private ArrayList<Pot> pots;
    private ArrayList<Card> communityCards;
    private ArrayList<GameLogEntry> roundLog;
    private ArrayList<Player> players;
    private ArrayList<Integer> turnOrder;


    public Round(ArrayList<Player> players, int blindSize) {
        this.players = players;
        this.toPlay = blindSize;
        this.numPlayers = players.size();
        this.communityCards = new ArrayList<>();
        this.deck = new CardDeck();
        this.pots.add(new Pot(players));
        this.turnOrder = new ArrayList<>();
        this.betType = BetType.NORMAL;
    }

    public ArrayList<GameLogEntry> getRoundLog() { return roundLog; }

    public void Init() {
        createTurnOrder(RoleUtil.findRoleIndices(players));
        for (Player p : players) addObserver(p);
        this.state = RoundStatus.BLINDS;
        this.pots.getFirst().initBlinds(players,turnOrder,toPlay);
        notifyObservers(state);
    }

    public void Start() {
        this.state = RoundStatus.DEAL;
        dealCards();
        notifyObservers(state);

        // wait

        this.state = RoundStatus.BETTING1;
        notifyObservers(state);
        checkBetType();

        this.state = RoundStatus.FLOP;
        deal2Table();
        notifyObservers(state);

        this.state = RoundStatus.BETTING2;
        notifyObservers(state);
        checkBetType();

        this.state = RoundStatus.TURN;
        deal2Table();
        notifyObservers(state);

        this.state = RoundStatus.BETTING3;
        notifyObservers(state);
        checkBetType();

        this.state = RoundStatus.RIVER;
        deal2Table();
        notifyObservers(state);

        this.state = RoundStatus.SHOWDOWN;
        notifyObservers(state);
        checkBetType();
    }

    public void End() {
        // TODO send RoundLog to database exit round
        this.state = RoundStatus.END;
        notifyObservers(state);
        // DAO.add(roundLog);

    }

    private void createTurnOrder(int[] roleIndices) {
        turnOrder.add((roleIndices[1]));
        for (int i = roleIndices[2]; i < numPlayers; i++){
            turnOrder.add(i);
        }
        for (int i = 0 ; i <= roleIndices[0]; i++){
            turnOrder.add(i);
        }
    }

    // TODO Attach to the game controller
    // On click method depending on player choice
    public void playerAction(Player player, int betSize) {
        Action action = player.getAction();
        if (action.isBet(action)) {
            this.roundLog.add(new GameLogEntry(player,toPlay-player.getRoundInvestment(),betSize,action));
        } else {
            this.roundLog.add(new GameLogEntry(player,betSize));
        }
    }

    private void dealCards() {
        for (int repeat = 0; repeat < 2; repeat++) {
            for (int i = 0; i < turnOrder.size(); i++) {
                players.get(turnOrder.get(i)).addCardToHand(deck.draw());
            }
        }
    }

    private void deal2Table() {
        deck.burnCard();
        if(state == RoundStatus.FLOP) {
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
        } else {
            this.communityCards.add(deck.draw());
        }
    }

    private void betting() {
        while (!endBetting()) {
            for (int i = 0; i < turnOrder.size(); i++) {
                players.get(turnOrder.get(i)).setIsTurn(true);
                while (players.get(turnOrder.get(i)).getIsTurn()) {
                    // Wait for input on bet or check or fold
                    // call method to asked user for input
                    // turnOrder.get(i).setAction(actionButtonInput());
                    int betSize = players.get(turnOrder.get(i)).chooseBetSize();
                    int activePotIndex = getOpenPotIndex();
                    if (activePotIndex == -1) System.err.println("No active Pot"); // Replace with exeception.
                    this.pots.get(activePotIndex).addBet(players.get(turnOrder.get(i)), betSize);
                    playerAction(players.get(turnOrder.get(i)), betSize);
                    if (players.get(turnOrder.get(i)).getAction() == Action.RAISE) {
                        this.toPlay = players.get(turnOrder.get(i)).getRoundInvestment();
                    }
                }
                if (endBetting()) break;
            }
        }
        for (Player p : players) {
            if (p.getAction() != Action.FOLD) p.setAction(Action.UNDECIDED);
        }
    }

    public boolean endBetting() {
        int numAllIn = 0;
        int numCall = 0;
        int numRaise = 0;
        int numFold = 0;
        int numCheck = 0;
        for (Player p : players) {
            if (p.getAction() == Action.ALLIN) {
                numAllIn += 1;
            } else if (p.getAction() == Action.CALL) {
                numCall += 1;
            } else if (p.getAction() == Action.RAISE) {
                numRaise += 1;
            } else if (p.getAction() == Action.FOLD) {
                numFold += 1;
            } else if (p.getAction() == Action.CHECK) {
                numCheck += 1;
            }
        }
        // end betting condition 1 (Normal or All In):
        //      One player action is Raise or All In,
        //      All other players actions are call or fold.
        boolean cond1 = (numRaise == 1 || numAllIn == 0 ) || (numRaise == 0 || numAllIn == 1 ) && (players.size() - 1 - numCall - numFold) == 0;
        // end betting condition 2 (Normal or End):
        //      All players actions are check, call or fold.
        boolean cond2 = (players.size()-numCall-numCheck-numFold) == 0;
        // end betting condition 3 (End):
        //      One player action is not fold,
        //      All other players actions are fold.
        boolean cond3 = (players.size() - numFold) == 1;
        // end betting condition 4 (All In):
        //      Multiple players action is All In,
        //      no players action is a raise,
        //      All other players actions are call or fold.
        boolean cond4 = (players.size()-numCall-numAllIn-numFold) == 0;
        // end betting condition 5 (SidePot):
        //      One or more players action is all in,
        //      One players action is to raise,
        //      All other players actions are call or fold.
        boolean cond5 = (numRaise == 1) && (numAllIn >= 1) && (players.size() - numFold - numCall - numAllIn - numRaise) == 0;
        if (cond5) {
            ArrayList<Player> sidePotPlayers = new ArrayList<>();
            for (Player p : players) {
                if (p.getAction() != Action.ALLIN || p.getAction() != Action.FOLD) sidePotPlayers.add(p);
            }
            pots.add(new Pot(sidePotPlayers));
            this.betType = BetType.SIDEPOT;
        return true;
        } else if(cond3) {
            this.betType = BetType.ENDROUND;
            return  true;
        } else if(cond4) {
            this.betType = BetType.SKIP2SHOWDOWN;
            return true;
        } else if(cond1) {
            if (numAllIn > 0) this.betType = BetType.SKIP2SHOWDOWN;
            else this.betType = BetType.NORMAL;
            return true;
        } else if(cond2) {
            if (numFold == players.size()-1) this.betType = BetType.SKIP2SHOWDOWN;
            else this.betType = BetType.NORMAL;
            return true;
        }
            return false;
    }

    public void checkBetType() {
        if (state == RoundStatus.BETTING1) {
            BetTypeLogic.executeBets(BetType.NORMAL);
            betting();
        }
        switch (betType) {
            case BetType.NORMAL:
                betting();
            case BetType.ENDROUND:
                pots.getFirst().removeFolded(state);
                pots.getFirst().payOut();
                End();
            case BetType.SKIP2SHOWDOWN:
                if (state == RoundStatus.SHOWDOWN) {
                    betting();
                    for (Pot p : pots) {
                        p.showDown();
                    }
                }
            case BetType.SIDEPOT:
                betting();
        }
    }

    public int getOpenPotIndex() {
        if (pots.size() == 1) return 0;
        for (int i = 0; i < pots.size(); i++) {
            if (pots.get(i).getIsOpen())
                return i;
        }
        return -1;
    }
}
